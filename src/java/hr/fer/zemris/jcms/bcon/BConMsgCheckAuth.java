package hr.fer.zemris.jcms.bcon;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class BConMsgCheckAuth extends BConMessage {
	public static final short ID = 5;
	private String username;
	private String password;

	public BConMsgCheckAuth(String username, String password) {
		super(ID);
		this.username = username;
		this.password = password;
	}
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	
	@Override
	public void write(DataOutputStream dos, BConMessageWriterSupport wSupport) throws IOException {
		super.write(dos, wSupport);
		wSupport.writeString(dos, username);
		wSupport.writeString(dos, password);
	}
	
	public static class Reader extends BConMessageReaderSupport implements BConMessageReader {
		
		@Override
		public BConMessage read(DataInputStream dis) throws IOException {
			return new BConMsgCheckAuth(readString(dis),readString(dis));
		}
		
		@Override
		public short getID() {
			return ID;
		}
	}
}
