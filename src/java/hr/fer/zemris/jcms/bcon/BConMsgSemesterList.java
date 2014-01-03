package hr.fer.zemris.jcms.bcon;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public class BConMsgSemesterList extends BConMessage {
	public static final short ID = 14;
	private int count;
	private Semester[] semesters;
	
	public BConMsgSemesterList(Semester[] semesters) {
		super(ID);
		this.semesters = semesters;
		this.count = semesters==null ? 0 : semesters.length;
	}

	public int getCount() {
		return count;
	}
	
	public Semester[] getSemesters() {
		return semesters;
	}
	
	@Override
	public void write(DataOutputStream dos, BConMessageWriterSupport wSupport) throws IOException {
		super.write(dos, wSupport);
		wSupport.writeInt(dos, count);
		for(int i = 0; i < count; i++) {
			Semester s = semesters[i];
			wSupport.writeString(dos, s.id);
			wSupport.writeString(dos, s.academicYear);
			wSupport.writeString(dos, s.semester);
		}
	}

	public static class Reader extends BConMessageReaderSupport implements BConMessageReader {
		
		@Override
		public BConMessage read(DataInputStream dis) throws IOException {
			int count = readInt(dis);
			Semester[] semesters = new Semester[count];
			for(int i = 0; i < count; i++) {
				semesters[i] = new Semester(readString(dis), readString(dis), readString(dis));
			}			
			return new BConMsgSemesterList(semesters);
		}
		
		@Override
		public short getID() {
			return ID;
		}
	}
	
	public static class Semester {
		String id;
		String academicYear;
		String semester;
		public Semester(String id, String academicYear, String semester) {
			super();
			this.id = id;
			this.academicYear = academicYear;
			this.semester = semester;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((id == null) ? 0 : id.hashCode());
			return result;
		}
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Semester other = (Semester) obj;
			if (id == null) {
				if (other.id != null)
					return false;
			} else if (!id.equals(other.id))
				return false;
			return true;
		}
		
		public String getAcademicYear() {
			return academicYear;
		}
		
		public String getId() {
			return id;
		}
		
		public String getSemester() {
			return semester;
		}
	}
}
