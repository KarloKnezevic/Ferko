package hr.fer.zemris.jcms.parsers;

import hr.fer.zemris.jcms.beans.ext.UserFlagValueBean;

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
public class AssessmentFlagValuesParser {

	/**
	 * Parsira predani izvor okteta u kojem svaki redak ima sljedeće elemente odvojene znakom tab:
	 * <table border="1">
	 * <tr><th>jmbag</th><td>jmbag studenta</td></tr>
	 * <tr><th>value</th><td>vrijednost zastavice; legalne vrijednosti su 0, 1, true, false, on, off</td></tr>
	 * </table>
	 * Očekuje se da su podaci u streamu zapisani UTF-8 kodnom stranicom.
	 *  
	 * @param is izvor okteta
	 * @return lista vrijednosti zastavica
	 * @throws IOException u slučaju pogreške
	 */
	public static List<UserFlagValueBean> parseTabbedFormat(InputStream is) throws IOException {
		List<String> lines = TextService.inputStreamToUTF8StringList(is);
		return parseTabbedFormat(lines);
	}
	
	/**
	 * Parsira predani reader u kojem svaki redak ima sljedeće elemente odvojene znakom tab:
	 * <table border="1">
	 * <tr><th>jmbag</th><td>jmbag studenta</td></tr>
	 * <tr><th>value</th><td>vrijednost zastavice; legalne vrijednosti su 0, 1, true, false, on, off</td></tr>
	 * </table>
	 *  
	 * @param is reader
	 * @return lista vrijednosti zastavica
	 * @throws IOException u slučaju pogreške
	 */
	public static List<UserFlagValueBean> parseTabbedFormat(Reader is) throws IOException {
		List<String> lines = TextService.readerToStringList(is);
		return parseTabbedFormat(lines);
	}
	
	/**
	 * Parsira predanu listu u kojoj svaki redak ima sljedeće elemente odvojene znakom tab:
	 * <table border="1">
	 * <tr><th>jmbag</th><td>jmbag studenta</td></tr>
	 * <tr><th>value</th><td>vrijednost zastavice; legalne vrijednosti su 0, 1, true, false, on, off</td></tr>
	 * </table>
	 *  
	 * @param lines retci
	 * @return lista vrijednosti zastavica
	 * @throws IOException u slučaju pogreške
	 */
	public static List<UserFlagValueBean> parseTabbedFormat(List<String> lines) throws IOException {
		List<UserFlagValueBean> resultList = new ArrayList<UserFlagValueBean>(lines.size());
		if(lines.isEmpty()) return resultList;
		int pos = lines.get(0).indexOf('\t');
		char separator = pos != -1 ? '\t' : '#';
		Map<String, String> cache = new HashMap<String, String>(500);
		for(String line : lines) {
			String[] elements = TextService.split(line, separator);
			if(elements.length!=2) {
				throw new IOException("Format datoteke je neispravan. Pronađen je redak s "+elements.length+" elemenata.");
			}
			boolean v;
			if(elements[1].equals("1") || elements[1].equalsIgnoreCase("on") || elements[1].equalsIgnoreCase("true")) {
				v = true;
			} else if(elements[1].equals("0") || elements[1].equalsIgnoreCase("off") || elements[1].equalsIgnoreCase("false")) {
				v = false;
			} else {
				throw new IOException("Vrijednost "+elements[1]+" nije valjana vrijednost za zastavicu.");
			}
			UserFlagValueBean item = new UserFlagValueBean();
			item.setJmbag(fromCache(cache,elements[0]));
			item.setValue(v);
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
