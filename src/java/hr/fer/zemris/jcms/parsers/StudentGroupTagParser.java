package hr.fer.zemris.jcms.parsers;

import hr.fer.zemris.jcms.beans.ext.StudentGroupTagBean;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parseri za datoteku koja čuva popis zastavica za studente.
 * 
 * @author marcupic
 *
 */
public class StudentGroupTagParser {

	/**
	 * Parsira predani izvor okteta u kojem svaki redak ima sljedeće elemente odvojene znakom tab:
	 * <table border="1">
	 * <tr><th>jmbag</th><td>jmbag studenta</td></tr>
	 * <tr><th>groupName</th><td>naziv grupe, ili % za bilo koja grupa</td></tr>
	 * <tr><th>tagName</th><td>naziv grupe, ili može ostati prazno ako tag treba postaviti na null.</td></tr>
	 * </table>
	 * Očekuje se da su podaci u streamu zapisani UTF-8 kodnom stranicom.
	 *  
	 * @param is izvor okteta
	 * @return lista tagova za studente
	 * @throws IOException u slučaju pogreške
	 */
	public static List<StudentGroupTagBean> parseTabbedFormat(InputStream is) throws IOException {
		List<String> lines = TextService.inputStreamToUTF8StringList(is);
		return parseTabbedFormat(lines);
	}
	
	/**
	 * Parsira predani reader u kojem svaki redak ima sljedeće elemente odvojene znakom tab:
	 * <table border="1">
	 * <tr><th>jmbag</th><td>jmbag studenta</td></tr>
	 * <tr><th>groupName</th><td>naziv grupe, ili % za bilo koja grupa</td></tr>
	 * <tr><th>tagName</th><td>naziv grupe, ili može ostati prazno ako tag treba postaviti na null.</td></tr>
	 * </table>
	 *  
	 * @param is reader
	 * @return lista tagova za studente
	 * @throws IOException u slučaju pogreške
	 */
	public static List<StudentGroupTagBean> parseTabbedFormat(Reader is) throws IOException {
		List<String> lines = TextService.readerToStringList(is);
		return parseTabbedFormat(lines);
	}
	
	/**
	 * Parsira predanu listu u kojoj svaki redak ima sljedeće elemente odvojene znakom tab:
	 * <table border="1">
	 * <tr><th>jmbag</th><td>jmbag studenta</td></tr>
	 * <tr><th>groupName</th><td>naziv grupe, ili % za bilo koja grupa</td></tr>
	 * <tr><th>tagName</th><td>naziv grupe, ili može ostati prazno ako tag treba postaviti na null.</td></tr>
	 * </table>
	 *  
	 * @param lines retci
	 * @return lista tagova za studente
	 * @throws IOException u slučaju pogreške
	 */
	public static List<StudentGroupTagBean> parseTabbedFormat(List<String> lines) throws IOException {
		List<StudentGroupTagBean> resultList = new ArrayList<StudentGroupTagBean>(lines.size());
		if(lines.isEmpty()) return resultList;
		int pos = lines.get(0).indexOf('\t');
		char separator = pos != -1 ? '\t' : '#';
		Map<String, String> cache = new HashMap<String, String>(500);
		for(String line : lines) {
			String[] elements = TextService.split(line, separator);
			if(elements.length!=3) {
				throw new IOException("Format datoteke je neispravan. Pronađen je redak s "+elements.length+" elemenata.");
			}
			for(int i = 0; i < elements.length; i++) {
				elements[i] = elements[i].trim();
			}
			if(elements[2].length()==0) elements[2]=null;
			StudentGroupTagBean item = new StudentGroupTagBean();
			item.setJmbag(fromCache(cache,elements[0]));
			item.setGroupName(fromCache(cache,elements[1]));
			item.setTagName(fromCache(cache,elements[2]));
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
