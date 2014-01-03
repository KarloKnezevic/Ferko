package hr.fer.zemris.jcms.parsers;

import hr.fer.zemris.jcms.beans.ext.ConstraintsImportBean;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parseri za datoteku koja čuva popis ograničenja za burzu za grupe, po predmetima.
 * 
 * @author marcupic
 *
 */
public class ConstraintsImportParser {

	/**
	 * Parsira predani izvor okteta u kojem svaki redak ima sljedeće elemente odvojene znakom tab:
	 * <table border="1">
	 * <tr><th>isvuCode</th><td>isvu šifra kolegija</td></tr>
	 * <tr><th>type</th><td>1 ako je ograničenje na jednu grupu, 2 ako je sumarno ograničenje</td></tr>
	 * <tr><th>count</th><td>broj koji predstavlja ograničenje</td></tr>
	 * <tr><th>constraint</th><td>za tip 1 ovo je ime grupe čija je veličina ograničena s count; za tip 2 ovo je čitav izraz, a count se ne gleda.</td></tr>
	 * </table>
	 * Očekuje se da su podaci u streamu zapisani UTF-8 kodnom stranicom.
	 *  
	 * @param is izvor okteta
	 * @return lista isvu zapisa
	 * @throws IOException u slučaju pogreške
	 */
	public static List<ConstraintsImportBean> parseTabbedFormat(InputStream is) throws IOException {
		List<String> lines = TextService.inputStreamToUTF8StringList(is);
		return parseTabbedFormat(lines);
	}
	
	/**
	 * Parsira predani reader u kojem svaki redak ima sljedeće elemente odvojene znakom tab:
	 * <table border="1">
	 * <tr><th>isvuCode</th><td>isvu šifra kolegija</td></tr>
	 * <tr><th>type</th><td>1 ako je ograničenje na jednu grupu, 2 ako je sumarno ograničenje</td></tr>
	 * <tr><th>count</th><td>broj koji predstavlja ograničenje</td></tr>
	 * <tr><th>constraint</th><td>za tip 1 ovo je ime grupe čija je veličina ograničena s count; za tip 2 ovo je čitav izraz, a count se ne gleda.</td></tr>
	 * </table>
	 *  
	 * @param is reader
	 * @return lista isvu zapisa
	 * @throws IOException u slučaju pogreške
	 */
	public static List<ConstraintsImportBean> parseTabbedFormat(Reader is) throws IOException {
		List<String> lines = TextService.readerToStringList(is);
		return parseTabbedFormat(lines);
	}
	
	/**
	 * Parsira predanu listu u kojoj svaki redak ima sljedeće elemente odvojene znakom tab:
	 * <table border="1">
	 * <tr><th>isvuCode</th><td>isvu šifra kolegija</td></tr>
	 * <tr><th>type</th><td>1 ako je ograničenje na jednu grupu, 2 ako je sumarno ograničenje</td></tr>
	 * <tr><th>count</th><td>broj koji predstavlja ograničenje</td></tr>
	 * <tr><th>constraint</th><td>za tip 1 ovo je ime grupe čija je veličina ograničena s count; za tip 2 ovo je čitav izraz, a count se ne gleda.</td></tr>
	 * </table>
	 *  
	 * Parser prilikom izvođenja koristi cache za sve stringove, tako da osigurava će u konačnici
	 * svaki različiti string biti alociran samo jednom prilikom pohrane u listu.
	 *  
	 * @param lines retci
	 * @return listu isvu zapisa
	 * @throws IOException u slučaju pogreške
	 */
	public static List<ConstraintsImportBean> parseTabbedFormat(List<String> lines) throws IOException {
		List<ConstraintsImportBean> resultList = new ArrayList<ConstraintsImportBean>(lines.size());
		Map<String, String> cache = new HashMap<String, String>(500);
		if(lines.isEmpty()) return resultList;
		int pos = lines.get(0).indexOf('\t');
		char separator = pos != -1 ? '\t' : '#';
		for(String line : lines) {
			String[] elements = TextService.split(line, separator);
			if(elements.length!=4) {
				System.out.println("Pronađen redak pogrešne duljine: "+line);
				continue;
			}
			ConstraintsImportBean item = new ConstraintsImportBean();
			item.setIsvuCode(fromCache(cache,elements[0]));
			item.setType(Integer.parseInt(elements[1]));
			item.setCount(Integer.parseInt(elements[2]));
			item.setConstraint(fromCache(cache,elements[3].toUpperCase()));
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
