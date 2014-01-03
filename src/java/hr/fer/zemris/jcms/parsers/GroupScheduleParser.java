package hr.fer.zemris.jcms.parsers;

import hr.fer.zemris.jcms.beans.ext.GroupScheduleBean;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parseri za datoteku s rasporedom.
 * 
 * @author marcupic
 *
 */
public class GroupScheduleParser {

	/**
	 * Parsira predani izvor okteta u kojem svaki redak ima sljedeće elemente odvojene znakom tab:
	 * <table border="1">
	 * <tr><th>date</th><td>datum, u formatu yyyy-MM-dd</td></tr>
	 * <tr><th>time</th><td>početak, u formatu HH:mm</td></tr>
	 * <tr><th>duration</th><td>trajanje u minutama</td></tr>
	 * <tr><th>venue</th><td>oznaka lokacije</td></tr>
	 * <tr><th>room</th><td>oznaka dvorane</td></tr>
	 * <tr><th>isvuCode</th><td>isvu šifra kolegija</td></tr>
	 * <tr><th>group</th><td>oznaka grupe</td></tr>
	 * </table>
	 * Očekuje se da su podaci u streamu zapisani UTF-8 kodnom stranicom.
	 *  
	 * @param is izvor okteta
	 * @return raspored
	 * @throws IOException u slučaju pogreške
	 */
	public static List<GroupScheduleBean> parseTabbedFormat(InputStream is) throws IOException {
		List<String> lines = TextService.inputStreamToUTF8StringList(is);
		return parseTabbedFormat(lines);
	}
	
	/**
	 * Parsira predani reader u kojem svaki redak ima sljedeće elemente odvojene znakom tab:
	 * <table border="1">
	 * <tr><th>date</th><td>datum, u formatu yyyy-MM-dd</td></tr>
	 * <tr><th>time</th><td>početak, u formatu HH:mm</td></tr>
	 * <tr><th>duration</th><td>trajanje u minutama</td></tr>
	 * <tr><th>venue</th><td>oznaka lokacije</td></tr>
	 * <tr><th>room</th><td>oznaka dvorane</td></tr>
	 * <tr><th>isvuCode</th><td>isvu šifra kolegija</td></tr>
	 * <tr><th>group</th><td>oznaka grupe</td></tr>
	 * </table>
	 *  
	 * @param is reader
	 * @return raspored
	 * @throws IOException u slučaju pogreške
	 */
	public static List<GroupScheduleBean> parseTabbedFormat(Reader is) throws IOException {
		List<String> lines = TextService.readerToStringList(is);
		return parseTabbedFormat(lines);
	}
	
	/**
	 * Parsira predanu listu u kojoj svaki redak ima sljedeće elemente odvojene znakom tab:
	 * <table border="1">
	 * <tr><th>date</th><td>datum, u formatu yyyy-MM-dd</td></tr>
	 * <tr><th>time</th><td>početak, u formatu HH:mm</td></tr>
	 * <tr><th>duration</th><td>trajanje u minutama</td></tr>
	 * <tr><th>venue</th><td>oznaka lokacije</td></tr>
	 * <tr><th>room</th><td>oznaka dvorane</td></tr>
	 * <tr><th>isvuCode</th><td>isvu šifra kolegija</td></tr>
	 * <tr><th>group</th><td>oznaka grupe</td></tr>
	 * </table>
	 *  
	 * Parser prilikom izvođenja koristi cache za sve stringove, tako da osigurava će u konačnici
	 * svaki različiti string biti alociran samo jednom prilikom pohrane u listu.
	 *  
	 * @param lines retci
	 * @return raspored
	 * @throws IOException u slučaju pogreške
	 */
	public static List<GroupScheduleBean> parseTabbedFormat(List<String> lines) throws IOException {
		List<GroupScheduleBean> resultList = new ArrayList<GroupScheduleBean>(lines.size());
		if(lines.isEmpty()) return resultList;
		int pos = lines.get(0).indexOf('\t');
		char separator = pos != -1 ? '\t' : '#';
		Map<String, String> cache = new HashMap<String, String>(5000);
		for(String line : lines) {
			String[] elements = TextService.split(line, separator);
			if(elements.length!=7) {
				System.out.println("Pronađen redak pogrešne duljine: "+line);
				continue;
			}
			GroupScheduleBean item = new GroupScheduleBean();
			item.setDate(fromCache(cache,elements[0]));
			item.setStart(fromCache(cache,elements[1]));
			item.setDuration(Integer.parseInt(elements[2]));
			item.setVenue(fromCache(cache,elements[3]));
			item.setRoom(fromCache(cache,elements[4]));
			item.setIsvuCode(fromCache(cache,elements[5]));
			String[] groups = TextService.split(elements[6].toUpperCase(), ',');
			for(int i = 0; i < groups.length; i++) {
				item.getGroups().add(fromCache(cache,groups[i]));
			}
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
