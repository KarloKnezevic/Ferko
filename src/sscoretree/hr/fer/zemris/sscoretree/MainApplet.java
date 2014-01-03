package hr.fer.zemris.sscoretree;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class MainApplet extends JApplet {

	private static final long serialVersionUID = 1L;
	private ScoreComponent scoreComponent;
	
	@Override
	public void init() {
		super.init();
		if(SwingUtilities.isEventDispatchThread()) {
			threadSafeInit();
		} else {
			try {
				SwingUtilities.invokeAndWait(new Runnable() {
					
					@Override
					public void run() {
						threadSafeInit();
					}
				});
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				e.printStackTrace();
			}
		}
	}

	private String ferkoURL;
	private String courseInstanceID;
	
	private void threadSafeInit() {
		ferkoURL = getParameter("ferkourl");
		courseInstanceID = getParameter("courseInstanceID");
		getContentPane().setLayout(new BorderLayout());
		String data = dohvati("flat",null);
		String hierData = dohvati("hier",null);
//		String data2 = "F\t1\tLAB2pris\tLAB2pris\r\n" + 
//                      "A\t1\tPZI\tPZI\r\n" +
//                      "A\t2\tUKU\tUkupno\r\n" +
//                      "A\t3\tDEMPRIP\tDEMPRIP\r\n" +
//                      "A\t4\tDEMPRIP1\tDEMPRIP1\r\n" +
//                      "A\t5\tDEMPRIP4\tDEMPRIP4\r\n" +
//                      "A\t6\tDZ\tDZ\r\n" +
//                      "A\t7\tDZ1\tDZ1\r\n" +
//                      "A\t8\tDZ2\tDZ2\r\n" +
//                      "A\t9\tLAB\tLAB\r\n" +
//                      "A\t10\tLAB1\tLAB1\r\n" +
//                      "A\t11\tLAB1vjez\tLAB1vjez\r\n" +
//                      "A\t12\tLAB3\tLAB3\r\n" +
//                      "A\t13\tLAB3i\tLAB3i\r\n" +
//                      "F\t2\tDEMOS\tDEMOS\r\n" +
//                      "A\t14\tLAB3vjez\tLAB3vjez\r\n" +
//                      "A\t15\tLAB4\tLAB4\r\n" +
//                      "A\t16\tLAB4vjez\tLAB4vjez\r\n" +
//                      "A\t17\tLAB5\tLAB5\r\n" +
//                      "A\t18\tLAB5vjez\tLAB5vjez\r\n" +
//                      "A\t19\tLAB6\tLAB6\r\n" +
//                      "A\t20\tLAB6vjez\tLAB6vjez\r\n" +
//                      "A\t21\tLAB7\tLAB7\r\n" +
//                      "A\t22\tLAB7vjez\tLAB7vjez\r\n" +
//                      "A\t23\tMI1\tMI1\r\n" +
//		              "A\t24\tMI2\tMI2\r\n" + 
//		              "A\t25\tZI\tZI\r\n";
//		String hierData2 = "%addp:A:2\r\n" +
//						"%addp:A:6\r\n" +
//						"%add:A:7\r\n" +
//						"%add:A:8\r\n" +
//						"%pop\r\n" +
//						"%addp:A:9\r\n" +
//						"%add:A:10\r\n" +
//						"%add:A:12\r\n" +
//						"%add:A:15\r\n" +
//						"%add:A:17\r\n" +
//						"%pop\r\n" +
//						"%pop\r\n";
		if(data!=null && hierData!=null) {
			scoreComponent = new ScoreComponent(data, hierData);
			JButton pohrani = new JButton("Pohrani promjene");
			getContentPane().add(scoreComponent.getComponent(), BorderLayout.CENTER);
			getContentPane().add(pohrani, BorderLayout.SOUTH);
			pohrani.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					String res = dohvati("stor", scoreComponent.serializeHierarchy());
					if("OK".equals(res)) {
						scoreComponent.notifySaved();
						JOptionPane.showMessageDialog(null, "Podatci su uspješno pohranjeni.", "Poruka sustava", JOptionPane.INFORMATION_MESSAGE);
					} else {
						JOptionPane.showMessageDialog(null, "Dogodila se je greška prilikom pohranjivanja podataka.", "Poruka sustava", JOptionPane.ERROR_MESSAGE);
					}
				}
			});
		} else {
			JLabel lab = new JLabel("Dogodila se je pogreška u komunikaciji.");
			getContentPane().add(lab, BorderLayout.CENTER);
		}
	}
	
	/**
	 * @param what - moze biti "flat" ili "hier" ili "stor" (u tom slucaju koristi se i data)
	 * @return tekst koji je vratio posluzitelj
	 */
	private String dohvati(String what, String data) {
		String result = null;
		HttpURLConnection conn = null;
		try {
			try {
				URL url = new URL(ferkoURL);
				conn = (HttpURLConnection)url.openConnection();
				conn.setDoOutput(true);
				conn.setDoInput(true);
				conn.setRequestMethod("POST");
				conn.setRequestProperty("Content-type", "application/x-www-form-urlencoded; charset=utf-8");
				StringBuilder sb = new StringBuilder();
				sb.append("courseInstanceID=").append(URLEncoder.encode(courseInstanceID,"UTF-8")).append("&");
				sb.append("what=").append(what);
				if(data!=null) {
					sb.append("&reqdata=").append(URLEncoder.encode(data,"UTF-8"));
				}
				byte[] podatci = sb.toString().getBytes("UTF-8");
				conn.setRequestProperty("Content-Length", Integer.toString(podatci.length));
				OutputStream os = conn.getOutputStream();
				os.write(podatci);
				os.close();
			} catch(Exception ex) {
				ex.printStackTrace();
				return null;
			}		
			try {
				int responseCode = conn.getResponseCode();
				if(responseCode!=200) {
					System.err.println("Greška u komunikaciji. Vraćen status: "+responseCode);
					return null;
				}
				String contentType = conn.getContentType();
				if(!contentType.equals("text/plain")&&!contentType.startsWith("text/plain;")) {
					System.err.println("Nisam dobio očekivani tip podataka. Vraćeno je: "+contentType);
					return null;
				}
				BufferedReader br = new BufferedReader(new InputStreamReader(new BufferedInputStream(conn.getInputStream()),"UTF-8"));
				StringWriter sw = new StringWriter();
				char[] podatci = new char[1024];
				while(true) {
					int r = br.read(podatci);
					if(r<1) break;
					sw.write(podatci, 0, r);
				}
				br.close();
				sw.close();
				result = sw.toString();
			} catch(Exception ex) {
				ex.printStackTrace();
				return null;
			}
			return result;
		} catch(Exception ex) {
			ex.printStackTrace();
			return null;
		} finally {
			if(conn!=null) {
				try { conn.disconnect(); } catch(Exception ignorable) {}
			}
		}
	}

	@Override
	public Dimension getPreferredSize() {
		return new Dimension(700, 400);
	}
}
