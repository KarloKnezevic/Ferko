package hr.fer.zemris.jcms.desktop.satnica;

import hr.fer.zemris.jcms.parsers.TextService;
import hr.fer.zemris.util.StringUtil;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Program analizira satnicu, trazi sve kolegije koji se predaju samo
 * u jednom terminu, i potom nerasporedene studente u takvom kolegiju rasporeduje
 * u bilo koju grupu, samo da nisu nerasporedeni.
 * 
 * @author marcupic
 *
 */
public class RazmjestiSJednimTerminom {
	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException {
		if(args.length != 3) {
			System.out.println("Ocekivao sam tri argumenta:");
			System.out.println("RazmjestiSJednimTerminom in_kratkiISVU in_satnica out_kratkiISVU");
			System.exit(1);
		}
		List<String> satnica = TextService.inputStreamToUTF8StringList(new FileInputStream(args[1]));
		Map<String,Set<String>> terminiGrupaNaKolegiju = new HashMap<String, Set<String>>();
		for(String line : satnica) {
			String[] elems = StringUtil.split(line, '#');
			String isvu = elems[6];
			String grupe = "("+normaliziraj(elems[5])+")";
			Set<String> set = terminiGrupaNaKolegiju.get(isvu);
			if(set==null) {
				set = new HashSet<String>();
				terminiGrupaNaKolegiju.put(isvu, set);
			}
			set.add(grupe.toUpperCase());
		}
		Set<String> kolegijiSJednimTerminom = new HashSet<String>();
		for(Map.Entry<String,Set<String>> e : terminiGrupaNaKolegiju.entrySet()) {
			if(e.getValue().size()<2) {
				kolegijiSJednimTerminom.add(e.getKey());
				// System.out.println(e.getKey()+": "+e.getValue());
			} else {
				// System.out.println("!!!"+e.getKey()+": "+e.getValue());
			}
		}
		List<String> isvu = TextService.inputStreamToUTF8StringList(new FileInputStream(args[0]));
		Map<String,String> nekaGrupaNaKolegiju = new HashMap<String, String>();
		for(String line : isvu) {
			String[] elems = StringUtil.split(line, '#');
			String c_isvu = elems[1];
			String grupa = elems[2];
			if(!grupa.equals("")) {
				nekaGrupaNaKolegiju.put(c_isvu, grupa);
			}
		}
		int counter=0;
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(args[2]),"UTF-8"));
		for(String line : isvu) {
			String[] elems = StringUtil.split(line, '#');
			Redak r = new Redak(elems);
			if(r.grupa.equals("") && kolegijiSJednimTerminom.contains(r.isvu)) {
				String newGroup = nekaGrupaNaKolegiju.get(r.isvu); 
				if(newGroup==null) {
					System.out.println("Kolegij "+r.isvu+" nema niti jednu uporabljivu grupu.");
					System.exit(1);
				}
				r.grupa = newGroup;
				counter++;
				System.out.println("S#"+r.isvu+"#"+r.jmbag+"#"+r.grupa);
			}
			bw.write(r.toString());
			bw.write("\r\n");
		}
		bw.flush();
		bw.close();
		System.out.println("Napravio sam "+counter+" razmjestanja.");
	}

	private static String normaliziraj(String s) {
		if(s.equals("")) return s;
		String[] el = StringUtil.split(s, ',');
		for(int i = 0; i < el.length; i++) {
			el[i] = el[i].trim();
		}
		Arrays.sort(el);
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i < el.length; i++) {
			sb.append(el[i]).append(", ");
		}
		return sb.toString();
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
}
