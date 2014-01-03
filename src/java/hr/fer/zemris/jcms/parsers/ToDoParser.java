package hr.fer.zemris.jcms.parsers;

import hr.fer.zemris.jcms.beans.ToDoTaskBean;
import hr.fer.zemris.jcms.model.extra.ToDoTaskPriority;
import hr.fer.zemris.jcms.model.extra.ToDoTaskStatus;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ToDoParser {

	public static List<ToDoTaskBean> parseTabbedFormat(InputStream is) throws IOException {
		List<String> lines = TextService.inputStreamToUTF8StringList(is);
		
		List<ToDoTaskBean> resultList = new ArrayList<ToDoTaskBean>(lines.size());
		if(lines.isEmpty()) return resultList;
		int pos = lines.get(0).indexOf('\t');
		char separator = pos != -1 ? '\t' : '#';
		System.out.println("In todo parser. Line list: ");
		for(String line : lines) System.out.println(line);
		
		for(String line : lines) {
			String[] elements = TextService.split(line, separator);
			ToDoTaskBean todo = new ToDoTaskBean();
			todo.setVirtualID(elements[0]);
			todo.setOwnerUserName(elements[1]);
			todo.setRealizerUserName(elements[2]);
			todo.setParentTask(elements[3].equals("null") ? null : elements[3]);
			todo.setStatus(elements[4].equals("OPEN") ? ToDoTaskStatus.OPEN : ToDoTaskStatus.CLOSED);
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			try{
				todo.setDeadline(sdf.parse(elements[5]));
			}catch(ParseException pe){
				System.out.println("Error parsing todotask date. Using current date instead.");
				todo.setDeadline(new Date());
			}
			
			todo.setTitle(elements[6]);
			todo.setDescription(elements[7].equals("null") ? null : elements[7]);
			todo.setGarbageCollectable(elements[8].equals("F") ? Boolean.FALSE : Boolean.TRUE);
			ToDoTaskPriority prio;
			if (elements[9].equals("TRIVIAL")) prio = ToDoTaskPriority.TRIVIAL; 
			else if (elements[9].equals("MEDIUM")) prio = ToDoTaskPriority.MEDIUM;
			else prio = ToDoTaskPriority.CRITICAL;
			todo.setPriority(prio);
			
			resultList.add(todo);
		}
		return resultList;
	}
}
