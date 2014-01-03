package hr.fer.zemris.jcms.bni;

import hr.fer.zemris.jcms.bcon.BConMessageWriterSupport;

import hr.fer.zemris.jcms.bcon.BConMsgGetCurrentSemester;
import hr.fer.zemris.jcms.bcon.BConMsgHello;
import hr.fer.zemris.jcms.bcon.BConMsgQuit;
import hr.fer.zemris.jcms.bcon.BConMsgSemesterList;
import hr.fer.zemris.jcms.bcon.BConMsgStatus;
import hr.fer.zemris.jcms.bcon.BConMsgSemesterList.Semester;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

/**
 * Ovo je demo aplikacija koja pokazuje kako se klijent moze spojiti binarnim konektorom na Ferko,
 * poslati autentifikacijske podatke i potom traziti koji je aktivni semestar.
 * 
 * @author marcupic
 *
 */
public class Demo {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		BConMessageWriterSupport wSupport = new BConMessageWriterSupport();
		
		Socket s = new Socket("localhost", 12845);
		DataInputStream dis = new DataInputStream(s.getInputStream());
		DataOutputStream dos = new DataOutputStream(s.getOutputStream());
		BConMsgHello msg = new BConMsgHello("admin","adminPass");
		msg.write(dos, wSupport);
		dos.flush();
		
		short id = dis.readShort();
		if(id==BConMsgStatus.ID) {
			BConMsgStatus res = (BConMsgStatus)new BConMsgStatus.Reader().read(dis);
			if(!res.isAccepted()) {
				System.out.println("Autorizacija je odbijena!");
				new BConMsgQuit().write(dos, wSupport);
				dos.flush();
				s.close();
				return;
			}
		}

		new BConMsgGetCurrentSemester().write(dos, wSupport);
		dos.flush();
		
		id = dis.readShort();
		if(id==BConMsgSemesterList.ID) {
			BConMsgSemesterList res = (BConMsgSemesterList)new BConMsgSemesterList.Reader().read(dis);
			System.out.println("Dobio sam odgovora: "+res.getCount());
			for(int i = 0; i < res.getCount(); i++) {
				Semester sem = res.getSemesters()[i];
				System.out.println(" > "+sem.getId()+", "+sem.getAcademicYear()+", "+sem.getSemester());
			}
		} else if(id==BConMsgStatus.ID) {
			BConMsgStatus res = (BConMsgStatus)new BConMsgStatus.Reader().read(dis);
			System.out.println("Status: "+res.isAccepted()+", "+res.getMessage());
		} else {
			System.out.println("Neocekivani odgovor.");
		}
		new BConMsgQuit().write(dos, wSupport);
		dos.flush();
		s.close();
		return;
	}

}
