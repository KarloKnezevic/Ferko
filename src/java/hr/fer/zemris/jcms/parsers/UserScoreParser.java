package hr.fer.zemris.jcms.parsers;

import hr.fer.zemris.jcms.beans.ext.UserScoreBean;
import hr.fer.zemris.util.StringUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parseri za datoteku koja čuva popis bodova za studente.
 * 
 * @author marcupic
 *
 */
public class UserScoreParser {

	/**
	 * Parsira predani izvor okteta u kojem svaki redak ima sljedeće elemente odvojene znakom tab:
	 * <table border="1">
	 * <tr><th>jmbag</th><td>jmbag studenta</td></tr>
	 * <tr><th>value</th><td>broj bodova studenta; legalna vrijednost je i praznina, što znači da student nije pristupio provjeri.</td></tr>
	 * </table>
	 * Očekuje se da su podaci u streamu zapisani UTF-8 kodnom stranicom.
	 *  
	 * @param is izvor okteta
	 * @return lista bodova
	 * @throws IOException u slučaju pogreške
	 */
	public static List<UserScoreBean> parseTabbedFormat(InputStream is) throws IOException {
		List<String> lines = TextService.inputStreamToUTF8StringList(is);
		return parseTabbedFormat(lines);
	}
	
	/**
	 * Parsira predani reader u kojem svaki redak ima sljedeće elemente odvojene znakom tab:
	 * <table border="1">
	 * <tr><th>jmbag</th><td>jmbag studenta</td></tr>
	 * <tr><th>value</th><td>broj bodova studenta; legalna vrijednost je i praznina, što znači da student nije pristupio provjeri.</td></tr>
	 * </table>
	 *  
	 * @param is reader
	 * @return lista bodova
	 * @throws IOException u slučaju pogreške
	 */
	public static List<UserScoreBean> parseTabbedFormat(Reader is) throws IOException {
		List<String> lines = TextService.readerToStringList(is);
		return parseTabbedFormat(lines);
	}
	
	/**
	 * Parsira predanu listu u kojoj svaki redak ima sljedeće elemente odvojene znakom tab:
	 * <table border="1">
	 * <tr><th>jmbag</th><td>jmbag studenta</td></tr>
	 * <tr><th>value</th><td>broj bodova studenta; legalna vrijednost je i praznina, što znači da student nije pristupio provjeri.</td></tr>
	 * </table>
	 *  
	 * @param lines retci
	 * @return lista bodova
	 * @throws IOException u slučaju pogreške
	 */
	public static List<UserScoreBean> parseTabbedFormat(List<String> lines) throws IOException {
		List<UserScoreBean> resultList = new ArrayList<UserScoreBean>(lines.size());
		if(lines.isEmpty()) return resultList;
		int pos = lines.get(0).indexOf('\t');
		char separator = pos != -1 ? '\t' : '#';
		Map<String, String> cache = new HashMap<String, String>(500);
		for(String line : lines) {
			String[] elements = TextService.split(line, separator);
			if(elements.length!=2) {
				throw new IOException("Format datoteke je neispravan. Pronađen je redak s "+elements.length+" elemenata.");
			}
			for(int i = 0; i < elements.length; i++) {
				elements[i] = elements[i].trim();
			}
			Double value;
			try {
				value = StringUtil.stringToDouble(elements[1]);
			} catch(NumberFormatException ex) {
				throw new IOException(ex.getMessage());
			}
			UserScoreBean item = new UserScoreBean();
			item.setJmbag(fromCache(cache,elements[0].trim()));
			item.setValue(fromCache(cache,elements[1]));
			item.setDoubleValue(value);
			resultList.add(item);
		}
		return resultList;
	}

	private static String fromCache(Map<String, String> cache, String text) {
		String value = cache.get(text);
		if(value!=null) return value;
		value = new String(text);
		cache.put(value, value);
		return value;
	}
}
