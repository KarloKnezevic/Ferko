package hr.fer.zemris.jcms.bcon;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class BConMsgNumberedStatus extends BConMessage {
	public static final short ID = 3;
	private boolean accepted;
	private String message;
	private int number;
	
	public BConMsgNumberedStatus(boolean accepted, String message, int number) {
		super(ID);
		this.accepted = accepted;
		this.message = message;
		this.number = number;
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
	public int getNumber() {
		return number;
	}
	public void setNumber(int number) {
		this.number = number;
	}
	
	@Override
	public void write(DataOutputStream dos, BConMessageWriterSupport wSupport) throws IOException {
		super.write(dos, wSupport);
		wSupport.writeBoolean(dos, accepted);
		wSupport.writeString(dos, message);
		wSupport.writeInt(dos, number);
	}

	public static class Reader extends BConMessageReaderSupport implements BConMessageReader {
		
		@Override
		public BConMessage read(DataInputStream dis) throws IOException {
			return new BConMsgNumberedStatus(readBoolean(dis),readString(dis), readInt(dis));
		}
		
		@Override
		public short getID() {
			return ID;
		}
	}
}
