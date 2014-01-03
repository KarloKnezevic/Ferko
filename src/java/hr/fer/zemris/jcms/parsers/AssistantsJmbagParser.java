package hr.fer.zemris.jcms.parsers;

import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class AssistantsJmbagParser {
	
	/**
	 * Metoda koja parsira sljedeci format:
	 * bilošta (jmbag) bilošta
	 * Pri tome je jmbag prilagođen FER-ovoj notaciji: dva slova i tri broja.
	 * Metoda vraca listu parsiranih jmbagova.
	 * Ovo je promijenjeno u odnosu na staru implementaciju jer je stara pucala na ljudima
	 * s dva prezimena.
	 *  
	 * @param is
	 * @return
	 * @throws IOException
	 * @throws ParseException
	 */
	public static List<String> parse(Reader is) throws IOException, ParseException {
		
		List<String> lines = TextService.readerToStringList(is);
		List<String> resultList = new ArrayList<String>(lines.size());
		
		if(lines.isEmpty()) return resultList;
		
		for (String line : lines) {
			int poc = 0;
			while(true) {
				int l = line.indexOf('(', poc);
				if(l<0) break;
				int r = line.indexOf(')', l+1);
				if(r<0) break;
				poc = r+1;
				String jmbag = line.substring(l+1, r);
				// Ako nije nešto između 4 i 6, vozi dalje (glumimo robusno otkrivanje)
				if(jmbag.length()<4 || jmbag.length()>6) continue;
				// Da vidimo još da li počinje s 2 ne-brojke, i završava s 2 brojke
				if(Character.isDigit(jmbag.charAt(0))) continue;
				if(Character.isDigit(jmbag.charAt(1))) continue;
				int n = jmbag.length();
				if(!Character.isDigit(jmbag.charAt(n-1))) continue;
				if(!Character.isDigit(jmbag.charAt(n-2))) continue;
				// Čini se da je dobar kandidat za JMBAG! Dodaj ga!
				resultList.add(jmbag);
			}
		}
		
		return resultList;
	}
}
