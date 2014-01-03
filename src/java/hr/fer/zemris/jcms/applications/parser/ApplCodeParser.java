package hr.fer.zemris.jcms.applications.parser;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import hr.fer.zemris.jcms.parsers.TextService;

public class ApplCodeParser {
	
	private List<ApplCodeSection> sections = new ArrayList<ApplCodeSection>();
	private String ime = null;
	private List<String> argumenti = new ArrayList<String>();
	
	public ApplCodeParser(String text) throws IOException {
		List<String> retci = TextService.readerToStringList(new StringReader(text));
		List<String> sekcija = null;
		for(String redak : retci) {
			if(redak.startsWith("@@@")) {
				if(sekcija!=null) {
					if(ime==null) ime = "def";
					sections.add(new ApplCodeSection(ime, argumenti, TextService.textLinesToString(sekcija)));
					sekcija = null;
					ime = null;
					argumenti = new ArrayList<String>();
				}
				readSectionHeader(redak);
			} else {
				if(sekcija==null) sekcija = new ArrayList<String>();
				sekcija.add(redak);
			}
		}
		if(sekcija!=null) {
			if(ime==null) ime = "def";
			sections.add(new ApplCodeSection(ime, argumenti, TextService.textLinesToString(sekcija)));
			sekcija = null;
			ime = null;
			argumenti = null;
		}
		int c = countSectionsWithName("def");
		if(c==0) throw new IOException("Nedostaje sekcija \'@@@def\'.");
		if(c>1) throw new IOException("Sekcija \'@@@def\' smije se pojaviti samo jednom.");
		c = countSectionsWithName("global");
		if(c>1) throw new IOException("Sekcija \'@@@global\' smije se pojaviti samo jednom.");
		Set<String> koristeniFilteri = new HashSet<String>();
		for(ApplCodeSection s : sections) {
			if(s.getSectionName().equals("def")) continue;
			if(s.getSectionName().equals("global")) continue;
			if(s.getSectionName().equals("filter")) {
				if(s.getArguments().size()==0) {
					if(!koristeniFilteri.add("")) throw new IOException("Sekcija \'@@@filter\' bez argumenata smije se pojaviti samo jednom.");
					continue;
				}
				if(s.getArguments().size()>1) {
					throw new IOException("Sekcija \'@@@filter\' može imati najviše jedan argument.");
				}
				if(!koristeniFilteri.add(s.getArguments().get(0))) throw new IOException("Sekcija \'@@@filter(\""+s.getArguments().get(0)+"\")\' je navedena više puta.");
				continue;
				
			}
			
		}
	}

	private int countSectionsWithName(String name) {
		int cntr = 0;
		for(ApplCodeSection s : sections) {
			if(s.getSectionName().equals(name)) cntr++;
		}
		return cntr;
	}

	private void readSectionHeader(String redak) throws IOException {
		redak = redak.substring(3);
		int zag = redak.indexOf('(');
		if(zag==-1) {
			checkSectionName(redak);
			ime = redak;
			return;
		}
		ime = redak.substring(0, zag);
		checkSectionName(ime);
		redak = redak.substring(zag+1);
		char[] a = redak.toCharArray();
		int pos = 0;
		while(true) {
			while(pos<a.length && (a[pos]==' '||a[pos]=='\t')) pos++;
			if(pos>=a.length) break;
			if(a[pos]==')') { pos++; break; }
			if(a[pos]==',') { pos++; continue; }
			if(a[pos]!='\"') throw new IOException("Pogreška u sintaksi deklaracije sekcije. Neočekivan znak \'"+a[pos]+"\' u "+redak);
			pos++;
			int st = pos;
			while(pos<a.length && a[pos]!='\"') pos++;
			if(pos>=a.length) throw new IOException("Pogreška u sintaksi deklaracije sekcije. Očekivao sam znak \'\"\' a dobio \'"+a[pos]+"\' u "+redak);
			if(argumenti==null) {
				argumenti = new ArrayList<String>();
			}
			argumenti.add(redak.substring(st, pos));
			pos++;
		}
		while(pos<a.length && (a[pos]==' '||a[pos]=='\t')) pos++;
		if(pos<a.length) throw new IOException("Pogreška u sintaksi deklaracije sekcije. Pronađen je višak: \'"+redak.substring(pos)+"\' u "+redak);
	}

	private void checkSectionName(String redak) throws IOException {
		for(int i = redak.length()-1; i>=0; i--) {
			char c = redak.charAt(i);
			if(!Character.isJavaIdentifierPart(c)) {
				throw new IOException("Naziv sekcije "+redak+" sadrži nedozvoljene znakove.");
			}
		}
	}
	
	public List<ApplCodeSection> getSections() {
		return sections;
	}
	
}
