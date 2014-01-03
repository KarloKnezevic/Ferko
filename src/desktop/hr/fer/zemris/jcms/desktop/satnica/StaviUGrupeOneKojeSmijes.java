package hr.fer.zemris.jcms.desktop.satnica;

import hr.fer.zemris.jcms.parsers.TextService;
import hr.fer.zemris.util.StringUtil;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class StaviUGrupeOneKojeSmijes {

	/**
	 * <p>Ideja je sljedeca: svi nerasporedeni studenti koji su na predmetu
	 * koji ima predavanja samo u jednom terminu mogu na tom predmetu biti 
	 * automatski rasporedeni u bilo koju postojecu grupu. To radi ovaj 
	 * program. Rezultat pohranjuje u početnu datoteku!</p>
	 * 
	 * <p>Program prima isvu_datoteku, popis_isvu_sifri1 "MIJENJAJ|CUVAJ" popis_isvu_sifri2</p>
	 * 
	 * <p>popis_isvu_sifri1 sadrži isvu šifre kolegija koji se uopće smiju razmatrati. Svi ostali retci se samo kopiraju.
	 *    popis_isvu_sifri2 sadrži isvu šifre kolegija čija je interpretacija ovisna o prethodnom argumentu. Ako je MIJENJAJ,
	 *    to su kolegiji na kojima se neraspoređeni studenti smiju pridjeliti u grupu; ako je CUVAJ na tim se kolegijima studenti
	 *    NE SMIJU pridjeliti u grupu.</p>
	 *    
	 * @param args argumenti komandne linije
	 */
	public static void main(String[] args) throws IOException {
		if(args.length!=5 || (!args[2].equals("MIJENJAJ") && !args[2].equals("CUVAJ"))) {
			System.err.println("Ocekivao sam tri argumenta: in_out/isvuUTF8.txt in/sifre1.txt MIJENJAJ|CUVAJ in/sifre2.txt out/grupe.txt");
		}
		Set<String> zaRazmatranje = citajSifre(args[1]);
		Set<String> sifre = citajSifre(args[3]);
		Set<String> mijenjaj = null;
		if(args[2].equals("MIJENJAJ")) {
			mijenjaj = sifre;
		} else {
			mijenjaj = new HashSet<String>(zaRazmatranje);
			mijenjaj.removeAll(sifre);
		}
		List<String> lines = TextService.inputStreamToUTF8StringList(new BufferedInputStream(new FileInputStream(args[0])));
		Map<String,String> nekaGrupa = new HashMap<String, String>(200);
		for(String line : lines) {
			String[] elems = StringUtil.split(line, '#');
			if(elems.length!=8) {
				System.err.println("Format datoteke ne valja! Ocekivao sam 8 elemenata u retku.");
				System.err.println("Redak: "+line);
				System.exit(1);
			}
			String isvu = elems[2];
			String grupa = elems[4].toUpperCase();
			if(grupa.isEmpty()) continue;
			if(nekaGrupa.containsKey(isvu)) continue;
			nekaGrupa.put(isvu, grupa);
		}
		int brojPokusaja = 0;
		int brojZamjena = 0;
		ByteArrayOutputStream bos = new ByteArrayOutputStream(1024*1024*5);
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(bos, "UTF-8"));
		for(String line : lines) {
			String[] elems = StringUtil.split(line, '#');
			if(elems.length!=8) {
				System.err.println("Format datoteke ne valja! Ocekivao sam 8 elemenata u retku.");
				System.err.println("Redak: "+line);
				System.exit(1);
			}
			String isvu = elems[2];
			String grupa = elems[4].toUpperCase();
			if(grupa.equals("") && mijenjaj.contains(isvu)) {
				brojPokusaja++;
				String zamjena = nekaGrupa.get(isvu);
				if(zamjena!=null) {
					brojZamjena++;
					elems[4]=zamjena;
				}
				StringBuilder sb = new StringBuilder(100);
				sb.append(elems[0]);
				for(int i = 1; i < 8; i++) {
					sb.append("#");
					sb.append(elems[i]);
				}
				String z = sb.toString();
				bw.write(z);
				bw.write("\r\n");
				if(zamjena!=null) {
					System.out.println("-"+line);
					System.out.println("+"+z);
				} else {
					System.out.println("!"+line);
				}
			} else {
				bw.write(line);
				bw.write("\r\n");
			}
		}
		System.out.println("Pokusan broj razmjestanja: "+brojPokusaja);
		System.out.println("Ostvaren broj razmjestanja: "+brojZamjena);
		
		bw.close();
		FileOutputStream fos = new FileOutputStream(args[0]);
		fos.write(bos.toByteArray());
		fos.close();
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
}
