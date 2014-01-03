package hr.fer.zemris.jcms.desktop.satnica;

import hr.fer.zemris.jcms.beans.CourseBean;
import hr.fer.zemris.util.StringUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class IzvadiKolegije {

	/**
	 * Cita isvu datoteku (args[0]) i vraca sve pronadene kolegije (args[1]).
	 * Opcionalno, ako je kao args[2] zadana datoteka, nju koristi kao filter
	 * kolegija koje ne smije prihvatiti.
	 * 
	 * Primjer 1. Procitaj sve kolegije dodiplomskog studija (iz male isvu datoteke):
	 * c:/fer/ferko/cupic20080904.txt c:/fer/ferko/kolegiji_dodiplomskog.txt
	 * 
	 * primjer 2. Sada procitaj sve kolegije diploskog iz velike isvu datoteke
	 * c:/fer/ferko/cupic20080909.txt c:/fer/ferko/kolegiji_diplomskog.txt c:/fer/ferko/kolegiji_dodiplomskog.txt
	 * 
	 * @param args
	 */
	public static void main(String[] args) throws IOException {
		Set<CourseBean> ignored = readIgnored(args.length==3 ? args[2] : null);

		Map<String,CourseBean> map = new HashMap<String, CourseBean>(200);
		BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(args[0]),"UTF-8"));
		while(true) {
			String l = r.readLine();
			if(l==null) break;
			if(StringUtil.isStringBlank(l)) continue;
			String[] elems = StringUtil.split(l, '#');
			String courseIsvuCode = elems[2];
			String courseName = elems[6];
			CourseBean bean = new CourseBean(courseIsvuCode, courseName);
			if(ignored.contains(bean)) continue;
			map.put(courseIsvuCode, bean);
		}
		r.close();
		System.out.println("Imam "+map.size()+" elemenata.");
		
		BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(args[1]),"UTF-8"));
		List<Long> l = new ArrayList<Long>(map.size());
		for(String code : map.keySet()) {
			l.add(Long.valueOf(code));
		}
		Collections.sort(l);
		for(Long lcode : l) {
			String code = String.valueOf(lcode);
			CourseBean bean = map.get(code);
			w.write(bean.getIsvuCode());
			w.write('\t');
			w.write(bean.getName());
			w.write("\r\n");
		}
		w.flush();
		w.close();
	}

	public static Set<CourseBean> readIgnored(String fileName) throws IOException {
		if(fileName==null) return new HashSet<CourseBean>();
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
