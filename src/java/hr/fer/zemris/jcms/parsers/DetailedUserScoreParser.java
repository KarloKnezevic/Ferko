package hr.fer.zemris.jcms.parsers;

import hr.fer.zemris.jcms.beans.ext.DetailedUserScoreBean;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Parseri za datoteku koja čuva popis bodova za studente za provjere sa brojem bodova po zadatku.
 * 
 * @author Ivan Krišto
 *
 */
public class DetailedUserScoreParser {

	/**
	 * Parsira predani izvor okteta u kojem svaki redak ima sljedeće elemente odvojene znakom tab:
	 * <table border="1">
	 * <tr><th>jmbag</th><td>jmbag studenta</td></tr>
	 * <tr><th>grupa</th><td>grupa studenta</td></tr>
	 * <tr><th>value1</th><td>broj bodova studenta za 1. problem; legalna vrijednost je i praznina, što znači da student nije pristupio provjeri.</td></tr>
	 * <tr><th>value2</th><td>broj bodova studenta za 2. problem</td></tr>
	 * <tr><th>value3</th><td>broj bodova studenta za 3. problem</td></tr>
	 * <tr><th>...</th><td>...</td></tr>
	 * <tr><th>valueN</th><td>broj bodova studenta za N. problem</td></tr>
	 * </table>
	 * Očekuje se da su podaci u streamu zapisani UTF-8 kodnom stranicom.
	 *  
	 * @param is izvor okteta
	 * @return lista bodova
	 * @throws IOException u slučaju pogreške
	 */
	public static List<DetailedUserScoreBean> parseTabbedMultiValueFormat(InputStream is) throws IOException {
		List<String> lines = TextService.inputStreamToUTF8StringList(is);
		return parseTabbedMultiValueFormat(lines);
	}
	
	/**
	 * Parsira predani reader u kojem svaki redak ima sljedeće elemente odvojene znakom tab:
	 * <table border="1">
	 * <tr><th>jmbag</th><td>jmbag studenta</td></tr>
	 * <tr><th>grupa</th><td>grupa studenta</td></tr>
	 * <tr><th>value1</th><td>broj bodova studenta za 1. problem; legalna vrijednost je i praznina, što znači da student nije pristupio provjeri.</td></tr>
	 * <tr><th>value2</th><td>broj bodova studenta za 2. problem</td></tr>
	 * <tr><th>value3</th><td>broj bodova studenta za 3. problem</td></tr>
	 * <tr><th>...</th><td>...</td></tr>
	 * <tr><th>valueN</th><td>broj bodova studenta za N. problem</td></tr>
	 * </table>
	 *  
	 * @param is reader
	 * @return lista bodova
	 * @throws IOException u slučaju pogreške
	 */
	public static List<DetailedUserScoreBean> parseTabbedMultiValueFormat(Reader is) throws IOException {
		List<String> lines = TextService.readerToStringList(is);
		return parseTabbedMultiValueFormat(lines);
	}
	
	/**
	 * Parsira predanu listu u kojoj svaki redak ima sljedeće elemente odvojene znakom tab:
	 * <table border="1">
	 * <tr><th>jmbag</th><td>jmbag studenta</td></tr>
	 * <tr><th>grupa</th><td>grupa studenta</td></tr>
	 * <tr><th>value1</th><td>broj bodova studenta za 1. problem; legalna vrijednost je i praznina, što znači da student nije pristupio provjeri.</td></tr>
	 * <tr><th>value2</th><td>broj bodova studenta za 2. problem</td></tr>
	 * <tr><th>value3</th><td>broj bodova studenta za 3. problem</td></tr>
	 * <tr><th>...</th><td>...</td></tr>
	 * <tr><th>valueN</th><td>broj bodova studenta za N. problem</td></tr>
	 * </table>
	 *  
	 * @param lines retci
	 * @return lista bodova
	 * @throws IOException u slučaju pogreške
	 */
	public static List<DetailedUserScoreBean> parseTabbedMultiValueFormat(List<String> lines) throws IOException {
		List<DetailedUserScoreBean> resultList = new ArrayList<DetailedUserScoreBean>(lines.size());
		int numberOfValues = -1;
		
		if(lines.isEmpty()) return resultList;
		int pos = lines.get(0).indexOf('\t');
		char separator = pos != -1 ? '\t' : '#';
		// TODO: Nisam shvatio smisao ovog cache-a (Ivan Krišto)
		//Map<String, String> cache = new HashMap<String, String>(500);
		for(String line : lines) {
			String[] elements = TextService.split(line, separator);
			
			if (numberOfValues == -1) {
				numberOfValues = elements.length;
				
			} else if (elements.length != numberOfValues) {
				throw new IOException("Format datoteke je neispravan."
							+	"Pronađen je redak s " + elements.length
							+	" elemenata, a prethodni redci su dugi "
							+	numberOfValues + " (redci moraju biti jednake duljine!).");
			}
			
			DetailedUserScoreBean item = new DetailedUserScoreBean(elements[0].trim(), elements[1].trim(), elements.length-2);
			for (int i = 2; i < elements.length; i++) {
				item.setValue(elements[i], i-2);
			}
			resultList.add(item);
		}
		return resultList;
	}
	
//	private static String fromCache(Map<String, String> cache, String text) {
//		String value = cache.get(text);
//		if(value!=null) return value;
//		value = new String(text);
//		cache.put(value, value);
//		return value;
//	}
}
