package hr.fer.zemris.jcms.desktop.satnica;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import hr.fer.zemris.jcms.parsers.TextService;
import hr.fer.zemris.util.StringUtil;

/**
 * Program uzima ISVU datoteku i iz nje vadi sve kolegije i sve grupe na kolegijima.
 * Ocekuje se da isvu datoteka bude u UTF-8 kodnoj stranici!
 * 
 * @author marcupic
 *
 */
public class PripremiGrupePoPredmetima {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException {
		if(args.length!=4) {
			System.err.println("Ocekivao sam četiri argumenta: in/isvuUTF8.txt out/predmeti.txt out/grupe.txt out/studenti.txt");
		}
		
		Map<String,C> mapa = new HashMap<String, C>(100);
		Map<String,S> studenti = new HashMap<String, S>(5000);
		
		List<String> lines = TextService.inputStreamToUTF8StringList(new BufferedInputStream(new FileInputStream(args[0])));
		for(String line : lines) {
			String[] elems = StringUtil.split(line, '#');
			if(elems.length!=8) {
				System.err.println("Format datoteke ne valja! Ocekivao sam 8 elemenata u retku.");
				System.err.println("Redak: "+line);
				System.exit(1);
			}
			String isvu = elems[2];
			String cname = elems[6];
			String grupa = elems[4].toUpperCase();
			String jmbag = elems[0];
			String prezime = elems[5];
			String ime = elems[5];
			int ind = prezime.indexOf(',');
			if(ind!=-1) {
				ime = prezime.substring(ind+1).trim();
				prezime = prezime.substring(0, ind);
			}
			S s = studenti.get(jmbag);
			if(s==null) {
				studenti.put(jmbag, new S(jmbag, prezime, ime));
			}
			C c = mapa.get(isvu);
			if(c==null) {
				c = new C(isvu, cname);
				mapa.put(isvu, c);
			}
			c.getGroups().add(grupa);
		}
		
		List<C> clist = new ArrayList<C>(mapa.values());
		Collections.sort(clist);
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(args[1]),"utf-8"));
		for(C c : clist) {
			bw.write(c.isvu);
			bw.write('\t');
			bw.write(c.name);
			bw.write("\r\n");
		}
		bw.flush();
		bw.close();
		
		Collections.sort(clist, new Comparator<C>() {
			@Override
			public int compare(C o1, C o2) {
				int d = o1.isvu.length() - o2.isvu.length();
				if(d!=0) return d;
				return o1.isvu.compareTo(o2.isvu);
			}
		});
		
		bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(args[2]),"utf-8"));
		for(C c : clist) {
			List<String> glist = new ArrayList<String>(c.getGroups());
			Collections.sort(glist);
			bw.write(c.isvu);
			for(String g : glist) {
				bw.write('\t');
				bw.write(g);
			}
			bw.write("\r\n");
		}
		bw.flush();
		bw.close();
		
		List<S> slist = new ArrayList<S>(studenti.values());
		Collections.sort(slist);
		bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(args[3]),"utf-8"));
		for(S s : slist) {
			bw.write(s.jmbag);
			bw.write('\t');
			bw.write(s.prezime);
			bw.write('\t');
			bw.write(s.ime);
			bw.write("\r\n");
		}
		bw.flush();
		bw.close();
	}

	static class S implements Comparable<S> {
		private String jmbag;
		private String prezime;
		private String ime;
		
		public S(String jmbag, String prezime, String ime) {
			super();
			this.jmbag = jmbag;
			this.prezime = prezime;
			this.ime = ime;
		}

		public String getJmbag() {
			return jmbag;
		}

		public String getPrezime() {
			return prezime;
		}

		public String getIme() {
			return ime;
		}
		
		@Override
		public int compareTo(S o) {
			int r = StringUtil.HR_COLLATOR.compare(this.prezime, o.prezime);
			if(r!=0) return r;
			r = StringUtil.HR_COLLATOR.compare(this.ime, o.ime);
			if(r!=0) return r;
			r = this.jmbag.compareTo(o.jmbag);
			return r;
		}
	}
	
	static class C implements Comparable<C> {
		
		private String isvu;
		private String name;
		private Set<String> groups = new HashSet<String>(20);
		
		public C(String isvu, String name) {
			super();
			this.isvu = isvu;
			this.name = name;
		}

		public String getIsvu() {
			return isvu;
		}
		
		public String getName() {
			return name;
		}
		
		public Set<String> getGroups() {
			return groups;
		}
		
		@Override
		public int compareTo(C o) {
			return StringUtil.HR_COLLATOR.compare(this.name, o.name);
		}
		
	}
}
