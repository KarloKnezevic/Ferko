package hr.fer.zemris.jcms.bcon;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BConMessageReaderSupport {

	public boolean readBoolean(DataInputStream dis) throws IOException {
		int res = dis.read();
		if(res == 0) return false;
		if(res == 1) return true;
		throw new IOException("Exception while reading boolean.");
	}
	
	public byte readByte(DataInputStream dis) throws IOException {
		return dis.readByte();
	}
	
	public short readShort(DataInputStream dis) throws IOException {
		return dis.readShort();
	}
	
	public int readInt(DataInputStream dis) throws IOException {
		return dis.readInt();
	}
	
	public long readLong(DataInputStream dis) throws IOException {
		return dis.readLong();
	}
	
	public double readDouble(DataInputStream dis) throws IOException {
		return dis.readDouble();
	}
	
	public String readString(DataInputStream dis) throws IOException {
		boolean hasString = readBoolean(dis);
		if(!hasString) return null;
		return dis.readUTF();
	}
	
	public List<String> readStringList(DataInputStream dis) throws IOException {
		ArrayList<String> list = new ArrayList<String>();
		String role;
		while((role=readString(dis))!=null) {
			list.add(role);
		}
		return list;
	}
}
