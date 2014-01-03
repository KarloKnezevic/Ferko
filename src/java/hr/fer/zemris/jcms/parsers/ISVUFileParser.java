package hr.fer.zemris.jcms.parsers;

import hr.fer.zemris.jcms.beans.ext.ISVUFileItemBean;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parseri za isvu datoteku koja čuva popis upisanih studenata po predmetima.
 * 
 * @author marcupic
 *
 */
public class ISVUFileParser {

	/**
	 * Parsira predani izvor okteta u kojem svaki redak ima sljedeće elemente odvojene znakom tab:
	 * <table border="1">
	 * <tr><th>jmbag</th><td>kratki naziv lokacije</td></tr>
	 * <tr><th>?</th><td>prazno polje koje ne čuva nikakvu informaciju</td></tr>
	 * <tr><th>isvuCode</th><td>isvu šifra kolegija</td></tr>
	 * <tr><th>?</th><td>prazno polje koje ne čuva nikakvu informaciju</td></tr>
	 * <tr><th>group</th><td>oznaka grupe</td></tr>
	 * <tr><th>lastName, firstName</th><td>prezime i ime studenta razdvojeno zarezom</td></tr>
	 * <tr><th>courseName</th><td>naziv kolegija</td></tr>
	 * <tr><th>year</th><td>godina; ovaj podatak ignoriramo kod parsiranja</td></tr>
	 * </table>
	 * Očekuje se da su podaci u streamu zapisani UTF-8 kodnom stranicom.
	 *  
	 * @param is izvor okteta
	 * @return lista isvu zapisa
	 * @throws IOException u slučaju pogreške
	 */
	public static List<ISVUFileItemBean> parseTabbedFormat(InputStream is) throws IOException {
		List<String> lines = TextService.inputStreamToUTF8StringList(is);
		return parseTabbedFormat(lines);
	}
	
	/**
	 * Parsira predani reader u kojem svaki redak ima sljedeće elemente odvojene znakom tab:
	 * <table border="1">
	 * <tr><th>jmbag</th><td>kratki naziv lokacije</td></tr>
	 * <tr><th>?</th><td>prazno polje koje ne čuva nikakvu informaciju</td></tr>
	 * <tr><th>isvuCode</th><td>isvu šifra kolegija</td></tr>
	 * <tr><th>?</th><td>prazno polje koje ne čuva nikakvu informaciju</td></tr>
	 * <tr><th>group</th><td>oznaka grupe</td></tr>
	 * <tr><th>lastName, firstName</th><td>prezime i ime studenta razdvojeno zarezom</td></tr>
	 * <tr><th>courseName</th><td>naziv kolegija</td></tr>
	 * <tr><th>year</th><td>godina; ovaj podatak ignoriramo kod parsiranja</td></tr>
	 * </table>
	 *  
	 * @param is reader
	 * @return lista isvu zapisa
	 * @throws IOException u slučaju pogreške
	 */
	public static List<ISVUFileItemBean> parseTabbedFormat(Reader is) throws IOException {
		List<String> lines = TextService.readerToStringList(is);
		return parseTabbedFormat(lines);
	}
	
	/**
	 * Parsira predanu listu u kojoj svaki redak ima sljedeće elemente odvojene znakom tab:
	 * <table border="1">
	 * <tr><th>jmbag</th><td>kratki naziv lokacije</td></tr>
	 * <tr><th>?</th><td>prazno polje koje ne čuva nikakvu informaciju</td></tr>
	 * <tr><th>isvuCode</th><td>isvu šifra kolegija</td></tr>
	 * <tr><th>?</th><td>prazno polje koje ne čuva nikakvu informaciju</td></tr>
	 * <tr><th>group</th><td>oznaka grupe</td></tr>
	 * <tr><th>lastName, firstName</th><td>prezime i ime studenta razdvojeno zarezom</td></tr>
	 * <tr><th>courseName</th><td>naziv kolegija</td></tr>
	 * <tr><th>year</th><td>godina; ovaj podatak ignoriramo kod parsiranja</td></tr>
	 * </table>
	 *  
	 * Parser prilikom izvođenja koristi cache za sve stringove, tako da osigurava će u konačnici
	 * svaki različiti string biti alociran samo jednom prilikom pohrane u listu.
	 *  
	 * @param lines retci
	 * @return listu isvu zapisa
	 * @throws IOException u slučaju pogreške
	 */
	public static List<ISVUFileItemBean> parseTabbedFormat(List<String> lines) throws IOException {
		List<ISVUFileItemBean> resultList = new ArrayList<ISVUFileItemBean>(lines.size());
		Map<String, String> cache = new HashMap<String, String>(5000);
		for(String line : lines) {
			String[] elements = TextService.split(line, '#');
			if(elements.length!=8) {
				System.out.println("Pronađen redak pogrešne duljine: "+line);
				continue;
			}
			ISVUFileItemBean item = new ISVUFileItemBean();
			item.setJmbag(fromCache(cache,elements[0]));
			item.setIsvuCode(fromCache(cache,elements[2]));
			item.setGroup(fromCache(cache,elements[4].toUpperCase()));
			int pos = elements[5].indexOf(',');
			String prezime = pos > -1 ? elements[5].substring(0,pos) : elements[5];
			String ime = pos > -1 ? elements[5].substring(pos+1).trim() : "";
			item.setLastName(fromCache(cache,prezime));
			item.setFirstName(fromCache(cache,ime));
			item.setCourseName(fromCache(cache,elements[6]));
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
