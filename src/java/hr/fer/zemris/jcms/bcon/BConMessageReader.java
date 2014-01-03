package hr.fer.zemris.jcms.bcon;

import java.io.DataInputStream;
import java.io.IOException;

public interface BConMessageReader {
	public BConMessage read(DataInputStream dis) throws IOException;
	public short getID();
}
