package hr.fer.zemris.jcms.desktop.satnica;

import hr.fer.zemris.jcms.parsers.TextService;
import hr.fer.zemris.util.StringUtil;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

public class ProvjeraOgranicenjaSISVUGrupama {

	//     Map<isvu_code,Set<group_name_uppercase>>
	static Map<String,Set<String>> grupeIzIsvua = new HashMap<String, Set<String>>(200);
	//     Map<isvu_code,Set<group_name_uppercase>>
	static Map<String,Set<String>> grupeIzOgranicenja = new HashMap<String, Set<String>>(200);
	//     Map<isvu_code,name>
	static Map<String,String> courseNames = new HashMap<String, String>(200);
	//     Map<isvu_code,broj_studenata>
	static Map<String,Num> courseStudCount = new HashMap<String, Num>(200);

	static class Num {
		int value;
		public Num() {
		}
		public Num(int value) {
			this.value = value;
		}
		public void increment() {
			value++;
		}
	}
	
	private static Set<String> citajSifre(String file) throws IOException {
		Set<String> set = new HashSet<String>(200);
		List<String> lines = TextService.inputStreamToUTF8StringList(new BufferedInputStream(new FileInputStream(file)));
		for(String line : lines) {
			line = line.trim();
			if(line.isEmpty()) continue;
			set.add(line);
		}
		return set;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException {

		Set<String> kolegijiZaBurzu = args.length<3 ? null : citajSifre(args[2]);
		BufferedReader br = new BufferedReader(new InputStreamReader(new BufferedInputStream(new FileInputStream(args[0])),"UTF-8"));
		while(true) {
			String line = br.readLine();
			if(line == null) break;
			line = line.trim();
			if(line.equals("")) continue;
			String[] elems = StringUtil.split(line, '#');
			String isvu_code = elems[2];
			if(!courseNames.containsKey(isvu_code)) {
				courseNames.put(isvu_code, elems[6]);
			}
			Num num = courseStudCount.get(isvu_code);
			if(num==null) {
				num = new Num();
				courseStudCount.put(isvu_code, num);
			}
			num.increment();
			String group_name = elems[4].toUpperCase();
			// Neraspoređene studente preskačemo
			if(group_name.equals("")) continue;
			Set<String> grupe = grupeIzIsvua.get(isvu_code);
			if(grupe==null) {
				grupe = new HashSet<String>();
				grupeIzIsvua.put(isvu_code, grupe);
			}
			grupe.add(group_name);
		}
		br.close();
		br = new BufferedReader(new InputStreamReader(new BufferedInputStream(new FileInputStream(args[1])),"UTF-8"));
		while(true) {
			String line = br.readLine();
			if(line == null) break;
			line = line.trim();
			if(line.equals("")) continue;
			String[] elems = StringUtil.split(line, '\t');
			String isvu_code = elems[0];
			String group_names = elems[1];
			String[] foundNames = StringUtil.split(group_names, ',');
			Set<String> grupe = grupeIzOgranicenja.get(isvu_code);
			if(grupe==null) {
				grupe = new HashSet<String>();
				grupeIzOgranicenja.put(isvu_code, grupe);
			}
			for(String gn : foundNames) {
				grupe.add(gn);
			}
		}
		br.close();
		Set<String> nepoznatiOgraniceniKolegiji = new HashSet<String>(grupeIzOgranicenja.keySet());
		nepoznatiOgraniceniKolegiji.removeAll(grupeIzIsvua.keySet());
		if(!nepoznatiOgraniceniKolegiji.isEmpty()) {
			System.out.println("Nepoznati ograniceni kolegiji (nema ih u ISVU)");
			System.out.println("----------------------------------------------");
			List<String> l = new ArrayList<String>(nepoznatiOgraniceniKolegiji);
			Collections.sort(l);
			for(String isvuCode : l) {
				System.out.println(isvuCode);
			}
			System.out.println();
		}
		boolean any = false;
		List<Map.Entry<String, Set<String>>> l = new ArrayList<Map.Entry<String,Set<String>>>(grupeIzIsvua.entrySet());
		Collections.sort(l, new Comparator<Map.Entry<String, Set<String>>>() {
			@Override
			public int compare(Entry<String, Set<String>> o1,
					Entry<String, Set<String>> o2) {
				return o1.getKey().compareTo(o2.getKey());
			}
		});
		for(Map.Entry<String, Set<String>> course : l) {
			String isvuCode = course.getKey();
			if(kolegijiZaBurzu!=null && !kolegijiZaBurzu.contains(isvuCode)) continue;
			Set<String> isvuGrupe = course.getValue();
			Set<String> ogranGrupe = grupeIzOgranicenja.get(isvuCode);
			if(ogranGrupe==null) {
				ogranGrupe = new HashSet<String>();
			}
			Set<String> set1 = new HashSet<String>(isvuGrupe);
			set1.removeAll(ogranGrupe);
			Set<String> set2 = new HashSet<String>(ogranGrupe);
			set2.removeAll(isvuGrupe);
			if(set1.isEmpty() && set2.isEmpty()) continue;
			Num num = courseStudCount.get(isvuCode);
			if(!any) {
				any = true;
				System.out.println("Kolegiji koji nisu ograničeni (isvu,count,name,notInIsvu,notConstrained)");
				System.out.println("------------------------------------------------------------------------");
			}
			System.out.println(isvuCode+"\t"+num.value+"\t"+courseNames.get(isvuCode)+"\t"+set2+"\t"+set1);
		}
		
	}

}
