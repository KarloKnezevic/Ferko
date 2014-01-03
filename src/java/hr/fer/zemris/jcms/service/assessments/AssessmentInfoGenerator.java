package hr.fer.zemris.jcms.service.assessments;

import hr.fer.zemris.jcms.model.Assessment;

import hr.fer.zemris.jcms.model.AssessmentRoom;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.model.UserGroup;
import hr.fer.zemris.util.StringUtil;

import java.io.IOException;
import java.io.Writer;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

public class AssessmentInfoGenerator {
	
	private Writer w;
	
	public AssessmentInfoGenerator(Writer w) throws IOException {
		this.w = w;
	}

	public void generateLists(Assessment assessment) throws IOException {
		List<AssessmentRoom> rooms = new ArrayList<AssessmentRoom>(assessment.getRooms());
		Iterator<AssessmentRoom> it = rooms.iterator();
		while(it.hasNext()) {
			AssessmentRoom room = it.next();
			if(!room.isTaken()) it.remove();
		}
		Collections.sort(rooms, StringUtil.ASSESSMENTROOM_COMPARATOR);
		
		w.write("Kolegij#Ispit#Datum#Pocetak#Trajanje#SifDvorane#BrojStudenata#KapacitetStudenata#BrojAsistenata#KapacitetAsistenata#PrezimeImeStudPrvi#PrezimeImeStudZadnji#Asistenti\r\n");
		for(AssessmentRoom room : rooms) {
			List<User> lista;
			if(room.getGroup()==null) {
				lista = new ArrayList<User>();
			} else {
				lista = new ArrayList<User>(room.getGroup().getUsers().size());
				for(UserGroup ug : room.getGroup().getUsers()) {
					lista.add(ug.getUser());
				}
			}
			Collections.sort(lista, StringUtil.USER_COMPARATOR);
			List<User> asistenti = new ArrayList<User>();
			if(room.getUserEvent()!=null && room.getUserEvent().getUsers()!=null) {
				for(User u : room.getUserEvent().getUsers()) {
					asistenti.add(u);
				}
				Collections.sort(asistenti, StringUtil.USER_COMPARATOR);
			}
			ispisiDvoranu(assessment, room, lista, asistenti);
		}
		w.flush();
	}

	private void ispisiDvoranu(Assessment assessment, AssessmentRoom room, List<User> students, List<User> asistants) throws IOException {
		// Kolegij
		w.write(assessment.getCourseInstance().getCourse().getName());
		w.write(" (");
		w.write(assessment.getCourseInstance().getCourse().getIsvuCode());
		w.write(")");
		w.write("#");
		// Ispit
		w.write(assessment.getName());
		w.write("#");
		// Datum#Pocetak#Trajanje
		if(assessment.getEvent()==null || assessment.getEvent().getStart()==null) {
			w.write("????-??-??");
			w.write("#");
			w.write("??:??");
			w.write("#");
			w.write("?");
			w.write("#");
		} else {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			String x = sdf.format(assessment.getEvent().getStart());
			w.write(x.substring(0,10));
			w.write("#");
			w.write(x.substring(11));
			w.write("#");
			w.write(Integer.toString(assessment.getEvent().getDuration()));
			w.write("#");
		}
		// Prostorija
		w.write(room.getRoom()==null ? "?" : room.getRoom().getName());
		w.write("#");
		// Broj studenata unutra
		w.write(Integer.toString(students.size()));
		w.write("#");
		// Kapacitet dvorane
		w.write(Integer.toString(room.getCapacity()));
		w.write("#");
		// Broj asistenata unutra
		w.write(Integer.toString(asistants.size()));
		w.write("#");
		// Potreban broj asistenata
		w.write(Integer.toString(room.getRequiredAssistants()));
		w.write("#");
		// Prvi dodijeljeni student i zadnji dodijeljeni student
		if(students.isEmpty()) {
	        w.write("-");
	        w.write("#");
	        w.write("-");
	        w.write("#");
		} else {
	        w.write(students.get(0).getLastName());
	        w.write(", ");
	        w.write(students.get(0).getFirstName());
	        w.write("#");
	        w.write(students.get(students.size()-1).getLastName());
	        w.write(", ");
	        w.write(students.get(students.size()-1).getFirstName());
	        w.write("#");
		}
		if(asistants.isEmpty()) {
	        w.write("-");
		} else {
			boolean prvi = true;
			for(User a : asistants) {
				if(prvi) {
					prvi = false;
				} else {
					w.write("; ");
				}
				w.write(a.getLastName());
				w.write(", ");
				w.write(a.getFirstName());
			}
		}
        w.write("\r\n");
	}

	public void close() throws IOException {
		w.close();
	}
}
