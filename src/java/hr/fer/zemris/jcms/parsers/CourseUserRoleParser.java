package hr.fer.zemris.jcms.parsers;

import hr.fer.zemris.jcms.beans.CourseUserRoleBean;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parseri za format koji za kolegij i osobu govori koja je uloga te osobe.
 * 
 * @author marcupic
 */
public class CourseUserRoleParser {

	/**
	 * Parsira predani izvor okteta u kojem svaki redak ima sljedeće elemente odvojene znakom "lojtri" (#):
	 * <table border="1">
	 * <tr><th>ISVU sifra</th><td>jmbag studenta</td></tr>
	 * <tr><th>jmbag</th><td>jmbag studenta</td></tr>
	 * <tr><th>uloga</th><td>'P' - predavanje, 'N' - nositelj, 'A' - auditorne, 'L' - labosi, '-S' - asistent organizator</td></tr>
	 * </table>
	 * Očekuje se da su podaci u streamu zapisani UTF-8 kodnom stranicom.
	 *  
	 * @param is izvor okteta
	 * @return lista zapisa
	 * @throws IOException u slučaju pogreške
	 */
	public static List<CourseUserRoleBean> parseTabbedFormat(InputStream is) throws IOException {
		List<String> lines = TextService.inputStreamToUTF8StringList(is);
		return parseTabbedFormat(lines);
	}
	
	/**
	 * Parsira predani reader u kojem svaki redak ima sljedeće elemente odvojene znakom "lojtri" (#):
	 * Parsira predani izvor okteta u kojem svaki redak ima sljedeće elemente odvojene znakom "lojtri" (#):
	 * <table border="1">
	 * <tr><th>ISVU sifra</th><td>jmbag studenta</td></tr>
	 * <tr><th>jmbag</th><td>jmbag studenta</td></tr>
	 * <tr><th>uloga</th><td>'P' - predavanje, 'N' - nositelj, 'A' - auditorne, 'L' - labosi, '-S' - asistent organizator</td></tr>
	 * </table>
	 *  
	 * @param is reader
	 * @return lista zapisa
	 * @throws IOException u slučaju pogreške
	 */
	public static List<CourseUserRoleBean> parseTabbedFormat(Reader is) throws IOException {
		List<String> lines = TextService.readerToStringList(is);
		return parseTabbedFormat(lines);
	}
	
	/**
	 * Parsira predanu listu u kojoj svaki redak ima sljedeće elemente odvojene znakom "lojtri" (#):
	 * Parsira predani izvor okteta u kojem svaki redak ima sljedeće elemente odvojene znakom "lojtri" (#):
	 * <table border="1">
	 * <tr><th>ISVU sifra</th><td>jmbag studenta</td></tr>
	 * <tr><th>jmbag</th><td>jmbag studenta</td></tr>
	 * <tr><th>uloga</th><td>'P' - predavanje, 'N' - nositelj, 'A' - auditorne, 'L' - labosi, '-S' - asistent organizator</td></tr>
	 * </table>
	 *  
	 * @param lines retci
	 * @return lista zapisa
	 * @throws IOException u slučaju pogreške
	 */
	public static List<CourseUserRoleBean> parseTabbedFormat(List<String> lines) throws IOException {
		List<CourseUserRoleBean> resultList = new ArrayList<CourseUserRoleBean>(lines.size());
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
				if(elements[i].length()==0) {
					throw new IOException("Format datoteke je neispravan. Pronađen je redak s "+elements.length+" elemenata od kojih je neki element prazan.");
				}
			}
			CourseUserRoleBean item = new CourseUserRoleBean(fromCache(cache,elements[0]),fromCache(cache,elements[1]),fromCache(cache,elements[2].toUpperCase()));
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
