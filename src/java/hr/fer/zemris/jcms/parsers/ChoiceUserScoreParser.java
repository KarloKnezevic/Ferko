package hr.fer.zemris.jcms.parsers;

import hr.fer.zemris.jcms.beans.ext.ChoiceUserScoreBean;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Parseri za datoteku koja čuva popis bodova za studente za provjere sa obrascima (na zaokruživanje).
 * 
 * @author Ivan Krišto
 *
 */
public class ChoiceUserScoreParser {

	/**
	 * Parsira predani izvor okteta u kojem svaki redak ima sljedeće elemente odvojene znakom tab:
	 * <table border="1">
	 * <tr><th>jmbag</th><td>jmbag studenta</td></tr>
	 * <tr><th>grupa</th><td>grupa studenta</td></tr>
	 * <tr><th>value1</th><td>Studentov odgovor za 1. problem; legalna vrijednost je slovo a-z ili A-Z, ili BLANK što znači da student nije odgovorio na pitanje.</td></tr>
	 * <tr><th>value2</th><td>Studentov odgovor za 2. problem</td></tr>
	 * <tr><th>value3</th><td>Studentov odgovor za 3. problem</td></tr>
	 * <tr><th>...</th><td>...</td></tr>
	 * <tr><th>valueN</th><td>Studentov odgovor za N. problem</td></tr>
	 * <tr><th>slika</th><td>Opcionalno: staza do slike; ovo se ignorira</td></tr>
	 * </table>
	 * Očekuje se da su podaci u streamu zapisani UTF-8 kodnom stranicom.
	 *  
	 * @param is izvor okteta
	 * @return lista bodova
	 * @throws IOException u slučaju pogreške
	 */
	public static List<ChoiceUserScoreBean> parseTabbedMultiValueFormat(InputStream is) throws IOException {
		List<String> lines = TextService.inputStreamToUTF8StringList(is);
		return parseTabbedMultiValueFormat(lines);
	}
	
	/**
	 * Parsira predani reader u kojem svaki redak ima sljedeće elemente odvojene znakom tab:
	 * <table border="1">
	 * <tr><th>jmbag</th><td>jmbag studenta</td></tr>
	 * <tr><th>grupa</th><td>grupa studenta</td></tr>
	 * <tr><th>value1</th><td>Studentov odgovor za 1. problem; legalna vrijednost je slovo a-z ili A-Z, ili BLANK što znači da student nije odgovorio na pitanje.</td></tr>
	 * <tr><th>value2</th><td>Studentov odgovor za 2. problem</td></tr>
	 * <tr><th>value3</th><td>Studentov odgovor za 3. problem</td></tr>
	 * <tr><th>...</th><td>...</td></tr>
	 * <tr><th>valueN</th><td>Studentov odgovor za N. problem</td></tr>
	 * <tr><th>slika</th><td>Opcionalno: staza do slike; ovo se ignorira</td></tr>
	 * </table>
	 *  
	 * @param is reader
	 * @return lista bodova
	 * @throws IOException u slučaju pogreške
	 */
	public static List<ChoiceUserScoreBean> parseTabbedMultiValueFormat(Reader is) throws IOException {
		List<String> lines = TextService.readerToStringList(is);
		return parseTabbedMultiValueFormat(lines);
	}
	
	/**
	 * Parsira predanu listu u kojoj svaki redak ima sljedeće elemente odvojene znakom tab:
	 * <table border="1">
	 * <tr><th>jmbag</th><td>jmbag studenta</td></tr>
	 * <tr><th>grupa</th><td>grupa studenta</td></tr>
	 * <tr><th>value1</th><td>Studentov odgovor za 1. problem; legalna vrijednost je slovo a-z ili A-Z, ili BLANK što znači da student nije odgovorio na pitanje.</td></tr>
	 * <tr><th>value2</th><td>Studentov odgovor za 2. problem</td></tr>
	 * <tr><th>value3</th><td>Studentov odgovor za 3. problem</td></tr>
	 * <tr><th>...</th><td>...</td></tr>
	 * <tr><th>valueN</th><td>Studentov odgovor za N. problem</td></tr>
	 * <tr><th>slika</th><td>Opcionalno: staza do slike; ovo se ignorira</td></tr>
	 * </table>
	 *  
	 * @param lines retci
	 * @return lista bodova
	 * @throws IOException u slučaju pogreške
	 */
	public static List<ChoiceUserScoreBean> parseTabbedMultiValueFormat(List<String> lines) throws IOException {
		List<ChoiceUserScoreBean> resultList = new ArrayList<ChoiceUserScoreBean>(lines.size());
		
		if(lines.isEmpty()) return resultList;
		int pos = lines.get(0).indexOf('\t');
		char separator = pos != -1 ? '\t' : '#';
		for(String line : lines) {
			int firstSeparator = line.indexOf(separator);
			int secondSeparator = line.indexOf(separator, firstSeparator + 1);
			String jmbag = line.substring(0, firstSeparator);
			String group = line.substring(firstSeparator + 1, secondSeparator);
			String answers = line.substring(secondSeparator + 1);
			int lastPos = answers.lastIndexOf(separator);
			if(lastPos!=-1) {
				String end = answers.substring(lastPos+1);
				if(end.indexOf('/')!=-1 || end.indexOf('\\')!=-1) {
					answers = answers.substring(0, lastPos);
				}
			}
			ChoiceUserScoreBean item = new ChoiceUserScoreBean(jmbag.trim(), group.trim(), answers);
			resultList.add(item);
		}
		return resultList;
	}
}
