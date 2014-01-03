package hr.fer.zemris.jcms.bcon;

import java.io.DataOutputStream;
import java.io.IOException;

public class BConMessage {
	private short id;

	public BConMessage(short id) {
		super();
		this.id = id;
	}
	public short getId() {
		return id;
	}
	public void setId(short id) {
		this.id = id;
	}
	
	public void write(DataOutputStream dos, BConMessageWriterSupport wSupport) throws IOException {
		wSupport.writeShort(dos, id);
	}
}
