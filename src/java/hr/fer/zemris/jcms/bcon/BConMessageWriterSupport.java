package hr.fer.zemris.jcms.bcon;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.List;

public class BConMessageWriterSupport {

	public void writeBoolean(DataOutputStream dos, boolean value) throws IOException {
		dos.write(value ? 1 : 0);
	}
	
	public void writeByte(DataOutputStream dos, byte value) throws IOException {
		dos.write(value);
	}
	
	public void writeShort(DataOutputStream dos, short value) throws IOException {
		dos.writeShort(value);
	}
	
	public void  writeInt(DataOutputStream dos, int value) throws IOException {
		dos.writeInt(value);
	}
	
	public void writeLong(DataOutputStream dos, long value) throws IOException {
		dos.writeLong(value);
	}
	
	public void writeDouble(DataOutputStream dos, double value) throws IOException {
		dos.writeDouble(value);
	}
	
	public void writeString(DataOutputStream dos, String value) throws IOException {
		if(value==null) {
			writeBoolean(dos, false);
		} else {
			writeBoolean(dos, true);
			dos.writeUTF(value);
		}
	}
	
	public void writeStringList(DataOutputStream dos, List<String> value) throws IOException {
		if(value != null && !value.isEmpty()) {
			for(String v : value) {
				writeString(dos, v);
			}
		}
		writeString(dos, null);
	}
}
