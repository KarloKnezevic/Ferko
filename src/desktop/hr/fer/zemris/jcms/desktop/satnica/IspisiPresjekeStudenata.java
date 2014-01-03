package hr.fer.zemris.jcms.desktop.satnica;

import hr.fer.zemris.jcms.beans.CourseBean;
import hr.fer.zemris.jcms.beans.ext.ISVUFileItemBean;
import hr.fer.zemris.jcms.parsers.ISVUFileParser;
import hr.fer.zemris.util.StringUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class IspisiPresjekeStudenata {

	/**
	 * Cita isvu datoteku (args[0]) i vraca broj zajednickih studenata.
	 * Opcionalno, ako je kao args[1] zadana datoteka, nju koristi kao filter
	 * kolegija koje smije prihvatiti.
	 * 
	 * Primjer 1. Nadi presjeke na dodiplomskim kolegijima:
	 * c:/fer/ferko/cupic20080909.txt c:/fer/ferko/matrica_zajednickih_studenata.txt c:/fer/ferko/kolegiji_dodiplomskog.txt
	 * 
	 * @param args
	 */
	public static void main(String[] args) throws IOException {
		Set<CourseBean> filtered = readFiltered(args.length==3 ? args[2] : null);
		
		Map<String,CourseBean> map = new HashMap<String, CourseBean>(filtered!=null ? filtered.size() : 16);
		if(filtered!=null) {
			for(CourseBean cb : filtered) {
				map.put(cb.getIsvuCode(), cb);
			}
		}
		Map<String,CourseBean> foundCoursesMap = new HashMap<String, CourseBean>(200);
		BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(args[0]),"UTF-8"));
		List<ISVUFileItemBean> tmpBeans = ISVUFileParser.parseTabbedFormat(r);
		System.out.println("Privremeno imam "+tmpBeans.size()+" elemenata.");
		List<ISVUFileItemBean> beans = new ArrayList<ISVUFileItemBean>(tmpBeans.size());
		for(ISVUFileItemBean b : tmpBeans) {
			if(filtered!=null && !map.containsKey(b.getIsvuCode())) continue;
			beans.add(b);
			foundCoursesMap.put(b.getIsvuCode(), new CourseBean(b.getIsvuCode(), b.getCourseName()));
		}
		tmpBeans = null; // gc list
		
		Map<String, Set<String>> courseUsers = new HashMap<String, Set<String>>(200);
		for(ISVUFileItemBean b : beans) {
			Set<String> usersSet = courseUsers.get(b.getIsvuCode());
			if(usersSet==null) {
				usersSet = new HashSet<String>(500);
				courseUsers.put(b.getIsvuCode(), usersSet);
			}
			usersSet.add(b.getJmbag());
		}

		List<CourseBean> list = new ArrayList<CourseBean>(foundCoursesMap.values());
		Collections.sort(list, new Comparator<CourseBean>() {
		    Collator HR_COLLATOR = Collator.getInstance(new Locale("HR"));
			@Override
			public int compare(CourseBean o1, CourseBean o2) {
				int r = HR_COLLATOR.compare(o1.getName(), o2.getName());
				if(r!=0) return r;
				return o1.getIsvuCode().compareTo(o2.getIsvuCode());
			}
		});
		BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(args[1]),"UTF-8"));
		int[][] matrix = new int[list.size()][list.size()];
		for(int i = 0; i < list.size(); i++) {
			Set<String> currentCourseUsers = courseUsers.get(list.get(i).getIsvuCode());
			matrix[i][i] = currentCourseUsers.size();
			for(int j = i+1; j < list.size(); j++) {
				Set<String> otherCourseUsers = courseUsers.get(list.get(j).getIsvuCode());
				Set<String> intersection = new HashSet<String>(currentCourseUsers);
				intersection.retainAll(otherCourseUsers);
				matrix[i][j] = intersection.size();
				matrix[j][i] = intersection.size();
			}
		}
		for(int i = 0; i < list.size(); i++) {
			CourseBean currentCourse = list.get(i);
			for(int j = 0; j < list.size(); j++) {
				CourseBean otherCourse = list.get(j);
				w.write(currentCourse.getIsvuCode());
				w.write('\t');
				w.write(currentCourse.getName());
				w.write('\t');
				w.write(otherCourse.getIsvuCode());
				w.write('\t');
				w.write(otherCourse.getName());
				w.write('\t');
				w.write(String.valueOf(matrix[i][j]));
				w.write("\r\n");
			}
		}		
		w.flush();
		w.close();
	}

	public static Set<CourseBean> readFiltered(String fileName) throws IOException {
		if(fileName==null) return null;
		Set<CourseBean> set = new HashSet<CourseBean>(200);
		BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(fileName),"UTF-8"));
		while(true) {
			String l = r.readLine();
			if(l==null) break;
			if(StringUtil.isStringBlank(l)) continue;
			String[] elems = StringUtil.split(l, '\t');
			String courseIsvuCode = elems[0];
			String courseName = elems[1];
			set.add(new CourseBean(courseIsvuCode, courseName));
		}
		r.close();
		return set;
	}
}
