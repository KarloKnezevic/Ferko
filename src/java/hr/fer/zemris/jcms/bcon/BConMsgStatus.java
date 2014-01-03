package hr.fer.zemris.jcms.bcon;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class BConMsgStatus extends BConMessage {
	public static final short ID = 2;
	private boolean accepted;
	private String message;

	public BConMsgStatus(boolean accepted, String message) {
		super(ID);
		this.accepted = accepted;
		this.message = message;
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
	
	@Override
	public void write(DataOutputStream dos, BConMessageWriterSupport wSupport) throws IOException {
		super.write(dos, wSupport);
		wSupport.writeBoolean(dos, accepted);
		wSupport.writeString(dos, message);
	}
	
	public static class Reader extends BConMessageReaderSupport implements BConMessageReader {
		
		@Override
		public BConMessage read(DataInputStream dis) throws IOException {
			return new BConMsgStatus(readBoolean(dis),readString(dis));
		}
		
		@Override
		public short getID() {
			return ID;
		}
		
	}
}
