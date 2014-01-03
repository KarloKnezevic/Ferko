package hr.fer.zemris.jcms.bcon;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

public class BConMsgCheckAuthStatus extends BConMessage {
	public static final short ID = 6;
	private boolean accepted;
	private String message;
	private List<String> roles;
	
	public BConMsgCheckAuthStatus(boolean accepted, String message, List<String> roles) {
		super(ID);
		this.accepted = accepted;
		this.message = message;
		this.roles = roles;
	}

	public boolean isAccepted() {
		return accepted;
	}
	public void setAccepted(boolean accepted) {
		this.accepted = accepted;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public List<String> getRoles() {
		return roles;
	}
	public void setRoles(List<String> roles) {
		this.roles = roles;
	}
	
	@Override
	public void write(DataOutputStream dos, BConMessageWriterSupport wSupport) throws IOException {
		super.write(dos, wSupport);
		wSupport.writeBoolean(dos, accepted);
		wSupport.writeString(dos, message);
		wSupport.writeStringList(dos, roles);
	}

	public static class Reader extends BConMessageReaderSupport implements BConMessageReader {
		
		@Override
		public BConMessage read(DataInputStream dis) throws IOException {
			return new BConMsgCheckAuthStatus(readBoolean(dis),readString(dis), readStringList(dis));
		}
		
		@Override
		public short getID() {
			return ID;
		}
	}
}
