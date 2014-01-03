package hr.fer.zemris.jcms.bcon;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class BConMsgGetCurrentSemester extends BConMessage {
	public static final short ID = 13;

	public BConMsgGetCurrentSemester() {
		super(ID);
	}
	
	@Override
	public void write(DataOutputStream dos, BConMessageWriterSupport wSupport) throws IOException {
		super.write(dos, wSupport);
	}
	
	public static class Reader extends BConMessageReaderSupport implements BConMessageReader {
		
		@Override
		public BConMessage read(DataInputStream dis) throws IOException {
			return new BConMsgGetCurrentSemester();
		}
		
		@Override
		public short getID() {
			return ID;
		}
	}
}
