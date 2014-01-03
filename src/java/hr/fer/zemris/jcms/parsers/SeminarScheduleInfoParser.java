package hr.fer.zemris.jcms.parsers;

import hr.fer.zemris.jcms.beans.SeminarScheduleInfoBean;
import hr.fer.zemris.util.DateUtil;
import hr.fer.zemris.util.StringUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parseri za datoteku s rasporedom prezentacija seminara.
 * 
 * @author marcupic
 */
public class SeminarScheduleInfoParser {

	/**
	 * Parsira predani izvor okteta u kojem svaki redak ima sljedeće elemente odvojene znakom točka-zarez:
	 * <table border="1">
	 * <tr><th>jmbag</th><td>jmbag studenta</td></tr>
	 * <tr><th>termin</th><td>termin prezentacije, format je yyyy-MM-dd HH:mm:ss</td></tr>
	 * <tr><th>dvorana</th><td>oznaka dvorane</td></tr>
	 * </table>
	 * Očekuje se da su podaci u streamu zapisani UTF-8 kodnom stranicom.
	 *  
	 * @param is izvor okteta
	 * @return lista termina prezentacija
	 * @throws IOException u slučaju pogreške
	 */
	public static List<SeminarScheduleInfoBean> parseCSVFormat(InputStream is) throws IOException {
		List<String> lines = TextService.inputStreamToUTF8StringList(is);
		return parseCSVFormat(lines);
	}
	
	/**
	 * Parsira predani izvor okteta u kojem svaki redak ima sljedeće elemente odvojene znakom točka-zarez:
	 * <table border="1">
	 * <tr><th>jmbag</th><td>jmbag studenta</td></tr>
	 * <tr><th>termin</th><td>termin prezentacije, format je yyyy-MM-dd HH:mm:ss</td></tr>
	 * <tr><th>dvorana</th><td>oznaka dvorane</td></tr>
	 * </table>
	 *  
	 * @param is reader
	 * @return lista termina prezentacija
	 * @throws IOException u slučaju pogreške
	 */
	public static List<SeminarScheduleInfoBean> parseCSVFormat(Reader is) throws IOException {
		List<String> lines = TextService.readerToStringList(is);
		return parseCSVFormat(lines);
	}
	
	/**
	 * Parsira predani izvor okteta u kojem svaki redak ima sljedeće elemente odvojene znakom točka-zarez:
	 * <table border="1">
	 * <tr><th>jmbag</th><td>jmbag studenta</td></tr>
	 * <tr><th>termin</th><td>termin prezentacije, format je yyyy-MM-dd HH:mm:ss</td></tr>
	 * <tr><th>dvorana</th><td>oznaka dvorane</td></tr>
	 * </table>
	 *  
	 * Parser prilikom izvođenja koristi cache za sve stringove, tako da osigurava će u konačnici
	 * svaki različiti string biti alociran samo jednom prilikom pohrane u listu.
	 *  
	 * @param lines retci
	 * @return lista termina prezentacija
	 * @throws IOException u slučaju pogreške
	 */
	public static List<SeminarScheduleInfoBean> parseCSVFormat(List<String> lines) throws IOException {
		List<SeminarScheduleInfoBean> resultList = new ArrayList<SeminarScheduleInfoBean>(lines.size());
		Map<String, String> cache = new HashMap<String, String>(1000);
		if(lines.size()<1) {
			throw new IOException("Format datoteke je pogrešan! Nisam dobio niti jedan redak.");
		}
		String first = lines.get(0);
		if(!first.equals("\"jmbag\";\"termin_id\";\"dvorana\"")) {
			throw new IOException("Format datoteke je pogrešan! Prvi redak nije očekivano zaglavlje.");
		}
		try {
			boolean prvi = true;
			for(String line : lines) {
				if(prvi) {
					prvi = false;
					continue;
				}
				String[] elements = CSVRowSplitter.split(line);
				if(elements.length!=3) {
					throw new IOException("Pronađen redak pogrešne duljine: "+line);
				}
				SeminarScheduleInfoBean item = new SeminarScheduleInfoBean();
				item.setJmbag(elements[0]);
				item.setDateTime(fromCache(cache,elements[1]));
				item.setRoomText(fromCache(cache,elements[2]));
				if(!StringUtil.isStringBlank(item.getDateTime()) && !DateUtil.checkFullDateFormat(item.getDateTime())) {
					throw new IOException("Datum "+item.getDateTime()+" je pogrešnog formata.");
				}
				resultList.add(item);
			}
		} catch(ParseException ex) {
			throw new IOException("Parse exception.", ex);
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
