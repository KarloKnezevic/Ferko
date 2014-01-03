package hr.fer.zemris.jcms.service.assessments;

import hr.fer.zemris.jcms.model.Assessment;
import hr.fer.zemris.jcms.model.AssessmentRoom;
import hr.fer.zemris.jcms.model.UserGroup;
import hr.fer.zemris.util.StringUtil;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class ScheduleToMailMergeGenerator {
	
	private Writer w;
	
	public ScheduleToMailMergeGenerator(Writer w) throws IOException {
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
		w.write("IDProvjere#SifDvorane#PrezimeIme#JMBAG#Rbr\r\n");
		w.write("####\r\n");
		for(AssessmentRoom room : rooms) {
			List<UserGroup> lista;
			if(room.getGroup()==null) {
				lista = new ArrayList<UserGroup>();
			} else {
				lista = new ArrayList<UserGroup>(room.getGroup().getUsers());
			}
			Collections.sort(lista, new Comparator<UserGroup>() {
				@Override
				public int compare(UserGroup o1, UserGroup o2) {
					return o1.getPosition() - o2.getPosition();
				}
			});
			ispisiRaspored(assessment, room, lista);
		}
		w.flush();
	}

	private void ispisiRaspored(Assessment assessment, AssessmentRoom room, List<UserGroup> students) throws IOException {

        int rbr = 0;
        
        for(UserGroup student : students) {
			
	        rbr++;
	        
	        w.write("*");
	        w.write(String.valueOf(assessment.getId().longValue()));
	        w.write("*#");
	        w.write(room.getRoom()==null ? "?" : room.getRoom().getName());
	        w.write("#");
	        w.write(student.getUser().getLastName());
	        w.write(", ");
	        w.write(student.getUser().getFirstName());
	        w.write(" (");
	        w.write(student.getUser().getJmbag());
	        w.write(")#*");
	        w.write(student.getUser().getJmbag());
	        w.write("*#");
	        w.write(String.valueOf(rbr));
	        w.write("\r\n");
		}
	}

	public void close() throws IOException {
		w.close();
	}
}
