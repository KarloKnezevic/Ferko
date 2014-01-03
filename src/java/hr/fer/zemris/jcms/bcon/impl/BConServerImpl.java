package hr.fer.zemris.jcms.bcon.impl;

import hr.fer.zemris.jcms.bcon.BConMessage;
import hr.fer.zemris.jcms.bcon.BConMessageReader;
import hr.fer.zemris.jcms.bcon.BConMessageWriterSupport;
import hr.fer.zemris.jcms.bcon.BConMsgCheckAuth;
import hr.fer.zemris.jcms.bcon.BConMsgCheckAuthStatus;
import hr.fer.zemris.jcms.bcon.BConMsgGetCurrentSemester;
import hr.fer.zemris.jcms.bcon.BConMsgHello;
import hr.fer.zemris.jcms.bcon.BConMsgNumberedStatus;
import hr.fer.zemris.jcms.bcon.BConMsgQuit;
import hr.fer.zemris.jcms.bcon.BConMsgSemesterList;
import hr.fer.zemris.jcms.bcon.BConMsgStatus;
import hr.fer.zemris.jcms.bcon.BConServer;
import hr.fer.zemris.jcms.bcon.impl.services.BCONBasicServices;
import hr.fer.zemris.jcms.service.UserLogin;
import hr.fer.zemris.jcms.service.UserLogin.UserLoginStatus;
import hr.fer.zemris.jcms.web.interceptors.data.CurrentUser;
import hr.fer.zemris.jcms.web.interceptors.data.CurrentUserImpl1;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class BConServerImpl implements BConServer {

	Map<Short, BConMessageReader> readers;
	BConMessageWriterSupport wSupport;
	
	int port;
	ServerSocket ssock;
	boolean running;
	boolean stopRequested;
	
	public BConServerImpl(Properties prop) {
		if(prop != null) {
			init(prop);
		} else {
			init(new Properties());
		}
	}

	private void init(Properties prop) {
		port = Integer.valueOf(prop.getProperty("jcms.bcon.port","12845"));
		initReaders();
		wSupport = new BConMessageWriterSupport();
	}

	private void initReaders() {
		readers = new HashMap<Short, BConMessageReader>();
		addReader(readers, new BConMsgHello.Reader());
		addReader(readers, new BConMsgStatus.Reader());
		addReader(readers, new BConMsgNumberedStatus.Reader());
		addReader(readers, new BConMsgQuit.Reader());
		addReader(readers, new BConMsgCheckAuth.Reader());
		addReader(readers, new BConMsgCheckAuthStatus.Reader());
		addReader(readers, new BConMsgGetCurrentSemester.Reader());
		addReader(readers, new BConMsgSemesterList.Reader());
	}

	private void addReader(Map<Short, BConMessageReader> readers, BConMessageReader reader) {
		readers.put(Short.valueOf(reader.getID()), reader);
	}

	@Override
	public void start() {
		synchronized(this) {
			if(running) return;
			Thread t = new Thread(new Runnable() {
				@Override
				public void run() {
					try {
						runServer();
					} finally {
						synchronized(BConServerImpl.this) {
							running = false;
							stopRequested = false;
							BConServerImpl.this.notifyAll();
						}
					}
				}
			});
			t.start();
			running = true;
		}
	}

	@Override
	public void stop() {
		synchronized (this) {
			if(!running) return;
			stopRequested = true;
			while(running) {
				try { this.wait(); } catch(Exception ex) { return; }
			}
		}
	}

	protected void runServer() {
		int errorCounter = 0;
		try {
			ssock = new ServerSocket(port);
			try { ssock.setSoTimeout(10000); } catch(Exception ex) { ex.printStackTrace(); }
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}
		while(true) {
			try {
				Socket client = ssock.accept();
				errorCounter = 0;
				new Thread(new Klijent(client)).start();
			} catch(SocketTimeoutException ex) {
				synchronized (BConServerImpl.this) {
					if(stopRequested) break;
					continue;
				}
			} catch(Exception ex) {
				errorCounter++;
				if(errorCounter>20) { // 20 uzastopnih gresaka?
					System.err.println("BConServer: zbog grešaka gasim server.");
					break;
				}
			}
		}
	}

	private class Klijent implements Runnable {
		Socket client;
		DataInputStream dis;
		DataOutputStream dos;
		Map<String,Object> sessionProperties = new HashMap<String, Object>();
		CurrentUser currentUser;
		
		public Klijent(Socket client) {
			super();
			this.client = client;
		}
		
		@Override
		public void run() {
			try {
				dis = new DataInputStream(new BufferedInputStream(client.getInputStream()));
				dos = new DataOutputStream(new BufferedOutputStream(client.getOutputStream()));
			} catch(Exception ex) {
				ex.printStackTrace();
				try { client.close(); } catch(Exception ignorable) {}
			}
			try {
				stateNew();
			} catch(Exception ex) {
				ex.printStackTrace();
			} finally {
				try { client.close(); } catch(Exception ignorable) {}
			}
		}

		private void stateNew() throws IOException {
			while(true) {
				short id = dis.readShort();
				BConMessageReader mr = readers.get(Short.valueOf(id));
				if(mr==null) {
					throw new IOException("Unknown message (id="+id+").");
				}
				BConMessage m = mr.read(dis);
				if(m.getId()==BConMsgHello.ID) {
					if(!obradi((BConMsgHello)m)) continue;
					stateAuthenticated();
					break;
				} else if(m.getId()==BConMsgCheckAuth.ID) {
					obradi((BConMsgCheckAuth)m);
					continue;
				} else if(m.getId()==BConMsgQuit.ID) {
					return;
				} else {
					BConMessage resp = new BConMsgStatus(false, "Last message is not allowed in STATE_NEW.");
					resp.write(dos, wSupport);
					dos.flush();
				}
			}
		}

		private void stateAuthenticated() throws IOException {
			while(true) {
				short id = dis.readShort();
				BConMessageReader mr = readers.get(Short.valueOf(id));
				if(mr==null) {
					throw new IOException("Unknown message (id="+id+").");
				}
				BConMessage m = mr.read(dis);
				if(m.getId()==BConMsgQuit.ID) {
					return;
				} else if(m.getId()==BConMsgGetCurrentSemester.ID) {
					BConMessage resp = BCONBasicServices.dohvatiTrenutniSemestar(currentUser);
					resp.write(dos, wSupport);
					dos.flush();
					continue;
				} else {
					BConMessage resp = new BConMsgStatus(false, "Last message is not allowed in STATE_AUTHENTICATED.");
					resp.write(dos, wSupport);
					dos.flush();
				}
			}
		}

		private boolean obradi(BConMsgHello m) throws IOException {
			UserLogin.UserData udata = UserLogin.checkUser(m.getUsername(), m.getPassword());
			if(udata == null) {
				new BConMsgStatus(false, "Invalid username and/or password!").write(dos, wSupport);
				dos.flush();
				return false;
			}
			if(udata.getStatus()==UserLoginStatus.INVALID) {
				new BConMsgStatus(false, "Invalid username and/or password!").write(dos, wSupport);
				dos.flush();
				return false;
	        }
	        if(udata.getStatus()==UserLoginStatus.LOCKED) {
				new BConMsgStatus(false, "Account is locked.").write(dos, wSupport);
				dos.flush();
				return false;
	        }
	        if(udata.getStatus()==UserLoginStatus.INCOMPLETE) {
				new BConMsgStatus(false, "Account data is incomplete.").write(dos, wSupport);
				dos.flush();
				return false;
	        }
	        if(udata.getStatus()!=UserLoginStatus.SUCCESS) {
				new BConMsgStatus(false, "Unexpected response. Developers fault!").write(dos, wSupport);
				dos.flush();
				return false;
	        }
			new BConMsgStatus(true, null).write(dos, wSupport);
			dos.flush();
			sessionProperties.put("jcms_currentUserID", udata.getUserID());
			sessionProperties.put("jcms_currentUserUsername", udata.getUsername());
			sessionProperties.put("jcms_currentUserFirstName", udata.getFirstName());
			sessionProperties.put("jcms_currentUserLastName", udata.getLastName());
			sessionProperties.put("jcms_currentUserJmbag", udata.getJmbag());
			sessionProperties.put("jcms_currentUserRoles", udata.getRoles());
			currentUser = new CurrentUserImpl1(udata.getUserID(), udata.getUsername(), udata.getFirstName(), udata.getLastName(), udata.getJmbag(), udata.getRoles());
	        return true; // Prijavljeni smo!
		}

		private void obradi(BConMsgCheckAuth m) throws IOException {
			UserLogin.UserData udata = UserLogin.checkUser(m.getUsername(), m.getPassword());
			if(udata == null) {
				new BConMsgCheckAuthStatus(false, "Invalid username and/or password!", null).write(dos, wSupport);
				dos.flush();
				return;
			}
			if(udata.getStatus()==UserLoginStatus.INVALID) {
				new BConMsgCheckAuthStatus(false, "Invalid username and/or password!", null).write(dos, wSupport);
				dos.flush();
				return;
	        }
	        if(udata.getStatus()==UserLoginStatus.LOCKED) {
				new BConMsgCheckAuthStatus(false, "Account is locked.", null).write(dos, wSupport);
				dos.flush();
				return;
	        }
	        if(udata.getStatus()==UserLoginStatus.INCOMPLETE) {
				new BConMsgCheckAuthStatus(false, "Account data is incomplete.", null).write(dos, wSupport);
				dos.flush();
				return;
	        }
	        if(udata.getStatus()!=UserLoginStatus.SUCCESS) {
				new BConMsgCheckAuthStatus(false, "Unexpected response. Developers fault!", null).write(dos, wSupport);
				dos.flush();
				return;
	        }
			new BConMsgCheckAuthStatus(true, null, udata.getRoles()==null ? null : new ArrayList<String>(udata.getRoles())).write(dos, wSupport);
			dos.flush();
		}
	}
}
