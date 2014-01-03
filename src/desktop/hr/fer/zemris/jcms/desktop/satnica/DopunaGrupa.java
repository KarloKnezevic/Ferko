package hr.fer.zemris.jcms.desktop.satnica;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;

import hr.fer.zemris.jcms.parsers.TextService;
import hr.fer.zemris.util.StringUtil;

/**
 * Program uzima kratki format ISVU datoteke (jmbag#isvu#grupa), te uzima
 * niz aktora koji mogu modificirati grupu. Aktori se pale redosljedom kojim
 * su definirani; prvi koji pali, obavlja akciju i ostali se ne ispituju.
 * Popis aktora uzima se iz datoteke, čiji je format:
 * <pre>
 * M#isvu#grupa#novaGrupa
 * W#isvu#grupa
 * </pre>
 * <p><b>M</b> - radi modifikaciju zapisa tako da staru grupu mijenja novom.</p>
 * <p><b>W</b> - ispisuje na ekran zapis isvu#grupa svaki puta kada se takav pojavi
 * 
 * @author marcupic
 *
 */
public class DopunaGrupa {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException {
		if(args.length != 3) {
			System.out.println("Ocekivao sam tri argumenta:");
			System.out.println("DopunaGrupa in_kratkiISVU in_aktori out_kratkiISVU");
			System.exit(1);
		}
		List<String> aktoriList = TextService.inputStreamToUTF8StringList(new FileInputStream(args[1]));
		List<Aktor> aList = new ArrayList<Aktor>(aktoriList.size());
		for(String redak : aktoriList) {
			String[] init = StringUtil.split(redak, '#');
			if(init[0].equals("M")) {
				aList.add(new AktorM(init));
			} else if(init[0].equals("W")) {
				aList.add(new AktorW(init));
			} else if(init[0].equals("S")) {
				aList.add(new AktorS(init));
			} else {
				System.out.println("Nepoznat aktor: "+redak);
				System.exit(1);
			}
		}
		Aktor[] aktori = new Aktor[aList.size()];
		aList.toArray(aktori);
		List<String> isvu = TextService.inputStreamToUTF8StringList(new FileInputStream(args[0]));
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(args[2]),"UTF-8"));
		for(String line : isvu) {
			String[] elems = StringUtil.split(line, '#');
			Redak r = new Redak(elems);
			for(int i = 0; i < aktori.length; i++) {
				if(aktori[i].apply(r)) break;
			}
			bw.write(r.toString());
			bw.write("\r\n");
		}
		bw.flush();
		bw.close();
		for(Aktor a : aktori) {
			System.out.println(a.getInfo()+"\t"+a.getCounter());
		}
	}

	static class Redak {
		String jmbag;
		String isvu;
		String grupa;
		public Redak(String[] elems) {
			this.jmbag = elems[0];
			this.isvu = elems[1];
			this.grupa = elems[2];
		}
		@Override
		public String toString() {
			return jmbag+"#"+isvu+"#"+grupa;
		}
	}
	
	abstract static class Aktor {
		private int counter=0;
		public abstract boolean apply(Redak r);
		public abstract String getInfo();
		public int getCounter() {
			return counter;
		}
		protected void incrementCounter() {
			counter++;
		}
	}
	
	static class AktorM extends Aktor {
		private String isvu;
		private String oldGroup;
		private String newGroup;
		public AktorM(String[] init) {
			this.isvu = init[1];
			this.oldGroup = init[2];
			this.newGroup = init[3];
		}
		@Override
		public String getInfo() {
			return isvu+"#"+oldGroup+"#"+newGroup;
		}
		public boolean apply(Redak r) {
			if(r.isvu.equals(isvu) && r.grupa.equals(oldGroup)) {
				r.grupa = newGroup;
				incrementCounter();
				return true;
			}
			return false;
		}
	}
	static class AktorS extends Aktor {
		private String isvu;
		private String jmbag;
		private String newGroup;
		public AktorS(String[] init) {
			this.isvu = init[1];
			this.jmbag = init[2];
			this.newGroup = init[3];
		}
		@Override
		public String getInfo() {
			return isvu+"#"+jmbag+"#"+newGroup;
		}
		public boolean apply(Redak r) {
			if(r.isvu.equals(isvu) && r.jmbag.equals(jmbag)) {
				r.grupa = newGroup;
				incrementCounter();
				return true;
			}
			return false;
		}
	}
	static class AktorW extends Aktor {
		private String isvu;
		private String oldGroup;
		private boolean anyIsvu;

		@Override
		public String getInfo() {
			return isvu+"#"+oldGroup;
		}
		public AktorW(String[] init) {
			this.isvu = init[1];
			this.oldGroup = init[2];
			this.anyIsvu = this.isvu.equals("*");
		}
		public boolean apply(Redak r) {
			if((anyIsvu || r.isvu.equals(isvu)) && r.grupa.equals(oldGroup)) {
				incrementCounter();
				System.out.println(r.toString());
				return true;
			}
			return false;
		}
	}
}
