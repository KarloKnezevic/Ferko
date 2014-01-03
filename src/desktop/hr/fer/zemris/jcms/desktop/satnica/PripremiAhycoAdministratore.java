package hr.fer.zemris.jcms.desktop.satnica;

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

public class PripremiAhycoAdministratore {

	static class Korisnik {
		String sifra;
		String prezime;
		String ime;
		String login;
		String email;
		
		public Korisnik() {
		}

		public Korisnik(String sifra, String prezime, String ime, String login,
				String email) {
			super();
			this.sifra = sifra;
			this.prezime = prezime;
			this.ime = ime;
			this.login = login;
			this.email = email;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((sifra == null) ? 0 : sifra.hashCode());
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
			Korisnik other = (Korisnik) obj;
			if (sifra == null) {
				if (other.sifra != null)
					return false;
			} else if (!sifra.equals(other.sifra))
				return false;
			return true;
		}
	}
	
	static class Predmet {
		String isvuSifra;
		String naziv;
		public Predmet() {
		}
		public Predmet(String isvuSifra, String naziv) {
			super();
			this.isvuSifra = isvuSifra;
			this.naziv = naziv;
		}
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((isvuSifra == null) ? 0 : isvuSifra.hashCode());
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
			Predmet other = (Predmet) obj;
			if (isvuSifra == null) {
				if (other.isvuSifra != null)
					return false;
			} else if (!isvuSifra.equals(other.isvuSifra))
				return false;
			return true;
		}
		
	}
	
	static class Administrator {
		String isvuSifra;
		String sifraKorisnika;
		
		public Administrator() {
		}

		public Administrator(String isvuSifra, String sifraKorisnika) {
			super();
			this.isvuSifra = isvuSifra;
			this.sifraKorisnika = sifraKorisnika;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result
					+ ((isvuSifra == null) ? 0 : isvuSifra.hashCode());
			result = prime
					* result
					+ ((sifraKorisnika == null) ? 0 : sifraKorisnika.hashCode());
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
			Administrator other = (Administrator) obj;
			if (isvuSifra == null) {
				if (other.isvuSifra != null)
					return false;
			} else if (!isvuSifra.equals(other.isvuSifra))
				return false;
			if (sifraKorisnika == null) {
				if (other.sifraKorisnika != null)
					return false;
			} else if (!sifraKorisnika.equals(other.sifraKorisnika))
				return false;
			return true;
		}
		
	}
	
	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException {
		String djelatniciFile = "C:\\fer\\ferko\\djelatnici\\PopisDjelatnika.csv";
		String administratoriPredmetaFile = "C:\\fer\\ferko\\djelatnici\\AdministratoriPredmeta.csv";
		String predmetiFile = "C:\\fer\\ferko\\djelatnici\\Predmeti.csv";
		String outputKorisniciFile = "C:\\fer\\ferko\\djelatnici\\gen\\KorisniciPredmeta.csv";
		String outputIskoristeniKorisniciFile = "C:\\fer\\ferko\\djelatnici\\gen\\Korisnici.csv";

		Set<Korisnik> korisniciSet = new HashSet<Korisnik>(5000);
		Map<String, Korisnik> korisniciMap = new HashMap<String, Korisnik>(5000);
		
		BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(djelatniciFile),"UTF-8"));
		while(true) {
			String line = r.readLine();
			if(line==null) break;
			if(StringUtil.isStringBlank(line)) continue;
			String[] elems = StringUtil.split(line, '\t');
			Korisnik k = new Korisnik(deNA(elems[0]),deNA(elems[1]),deNA(elems[2]),deNA(elems[3]),deNA(elems[4]));
			korisniciSet.add(k);
			korisniciMap.put(k.sifra, k);
		}
		r.close();
		
		Set<Administrator> administratoriPredmetaSet = new HashSet<Administrator>(500);
		r = new BufferedReader(new InputStreamReader(new FileInputStream(administratoriPredmetaFile),"UTF-8"));
		while(true) {
			String line = r.readLine();
			if(line==null) break;
			if(StringUtil.isStringBlank(line)) continue;
			String[] elems = StringUtil.split(line, '\t');
			Administrator a = new Administrator(deNA(elems[0]),deNA(elems[2]));
			administratoriPredmetaSet.add(a);
		}
		r.close();
		
		Set<Predmet> predmetiSet = new HashSet<Predmet>(500);
		r = new BufferedReader(new InputStreamReader(new FileInputStream(predmetiFile),"UTF-8"));
		while(true) {
			String line = r.readLine();
			if(line==null) break;
			if(StringUtil.isStringBlank(line)) continue;
			String[] elems = StringUtil.split(line, '\t');
			Predmet p = new Predmet(deNA(elems[0]),deNA(elems[1]));
			predmetiSet.add(p);
		}
		r.close();
		
		List<Predmet> predmetiList = new ArrayList<Predmet>(predmetiSet);
		Collections.sort(predmetiList, new Comparator<Predmet>() {
		    Collator HR_COLLATOR = Collator.getInstance(new Locale("HR"));
			@Override
			public int compare(Predmet o1, Predmet o2) {
				int r = HR_COLLATOR.compare(o1.naziv, o2.naziv);
				if(r!=0) return r;
				return o1.isvuSifra.compareTo(o2.isvuSifra);
			}
		});
		
		// Map<isvusifra, Set<korisnik_sifra>>
		Map<String,Set<Korisnik>> administratoriPredmeta = new HashMap<String, Set<Korisnik>>(500);
		for(Administrator a : administratoriPredmetaSet) {
			Korisnik k = korisniciMap.get(a.sifraKorisnika);
			if(k==null) {
				System.out.println("Nema djelatnika sa sifrom "+a.sifraKorisnika);
				continue;
			}
			Set<Korisnik> set = administratoriPredmeta.get(a.isvuSifra);
			if(set==null) {
				set = new HashSet<Korisnik>();
				administratoriPredmeta.put(a.isvuSifra, set);
			}
			set.add(k);
		}

		Set<Korisnik> iskoristeniKorisnici = new HashSet<Korisnik>(128);
		BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputKorisniciFile),"UTF-8"));
		for(Predmet p : predmetiList) {
			// Nadi sve ahyco administratore
			Set<Korisnik> kset = administratoriPredmeta.get(p.isvuSifra);
			if(kset==null || kset.isEmpty()) {
				w.append(p.isvuSifra).append('\t').append(p.naziv).append('\t').append("\t\t\t");
				w.append("\r\n");
				continue;
			}
			for(Korisnik k : kset) {
				iskoristeniKorisnici.add(k);
				w.append(p.isvuSifra).append('\t').append(p.naziv).append('\t').append(k.prezime).append('\t').append(k.ime).append('\t').append(k.login==null || k.login.length()==0 ? "" : k.login).append('\t').append(k.email==null || k.email.length()==0 ? "" : k.email);
				w.append("\r\n");
			}
		}
		w.flush();
		w.close();

		w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(outputIskoristeniKorisniciFile),"UTF-8"));
		for(Korisnik k : iskoristeniKorisnici) {
			w.append(k.sifra).append('\t').append(k.prezime).append('\t').append(k.ime).append('\t').append(k.login==null || k.login.length()==0 ? "" : k.login).append('\t').append(k.email==null || k.email.length()==0 ? "" : k.email);
			w.append("\r\n");
		}
		w.flush();
		w.close();

	}

	private static String deNA(String s) {
		if(s==null) return null;
		if(s.equals("#N/A")) return "";
		return s;
	}

}
