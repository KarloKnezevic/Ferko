package hr.fer.zemris.jcms.parsers;

import hr.fer.zemris.jcms.beans.SeminarGroupsInfoBean;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parseri za datoteku s grupama seminara.
 * 
 * @author marcupic
 */
public class SeminarGroupsInfoParser {

	/**
	 * Parsira predani izvor okteta u kojem svaki redak ima sljedeće elemente odvojene znakom točka-zarez:
	 * <table border="1">
	 * <tr><th>jmbag</th><td>jmbag studenta</td></tr>
	 * <tr><th>grupa</th><td>numerička oznaka grupe u koju je student smješten</td></tr>
	 * <tr><th>vrsta</th><td>'p' za preddiplomski, 'd' za diplomski</td></tr>
	 * <tr><th>mentorID</th><td>identifikator mentora</td></tr>
	 * <tr><th>ime</th><td>ime mentora</td></tr>
	 * <tr><th>prezime</th><td>prezime mentora</td></tr>
	 * <tr><th>naziv rada</th><td>naziv rada</td></tr>
	 * <tr><th>isvu sifra</th><td>isvu sifra kolegija</td></tr>
	 * </table>
	 * Očekuje se da su podaci u streamu zapisani UTF-8 kodnom stranicom.
	 *  
	 * @param is izvor okteta
	 * @return lista zapisa o studentima
	 * @throws IOException u slučaju pogreške
	 */
	public static List<SeminarGroupsInfoBean> parseCSVFormat(InputStream is) throws IOException {
		List<String> lines = TextService.inputStreamToUTF8StringList(is);
		return parseCSVFormat(lines);
	}
	
	/**
	 * Parsira predani izvor okteta u kojem svaki redak ima sljedeće elemente odvojene znakom točka-zarez:
	 * <table border="1">
	 * <tr><th>jmbag</th><td>jmbag studenta</td></tr>
	 * <tr><th>grupa</th><td>numerička oznaka grupe u koju je student smješten</td></tr>
	 * <tr><th>vrsta</th><td>'p' za preddiplomski, 'd' za diplomski</td></tr>
	 * <tr><th>mentorID</th><td>identifikator mentora</td></tr>
	 * <tr><th>ime</th><td>ime mentora</td></tr>
	 * <tr><th>prezime</th><td>prezime mentora</td></tr>
	 * <tr><th>naziv rada</th><td>naziv rada</td></tr>
	 * <tr><th>isvu sifra</th><td>isvu sifra kolegija</td></tr>
	 * </table>
	 *  
	 * @param is reader
	 * @return lista zapisa o studentima
	 * @throws IOException u slučaju pogreške
	 */
	public static List<SeminarGroupsInfoBean> parseCSVFormat(Reader is) throws IOException {
		List<String> lines = TextService.readerToStringList(is);
		return parseCSVFormat(lines);
	}
	
	/**
	 * Parsira predani izvor okteta u kojem svaki redak ima sljedeće elemente odvojene znakom točka-zarez:
	 * <table border="1">
	 * <tr><th>jmbag</th><td>jmbag studenta</td></tr>
	 * <tr><th>grupa</th><td>numerička oznaka grupe u koju je student smješten</td></tr>
	 * <tr><th>vrsta</th><td>'p' za preddiplomski, 'd' za diplomski</td></tr>
	 * <tr><th>mentorID</th><td>identifikator mentora</td></tr>
	 * <tr><th>ime</th><td>ime mentora</td></tr>
	 * <tr><th>prezime</th><td>prezime mentora</td></tr>
	 * <tr><th>naziv rada</th><td>naziv rada</td></tr>
	 * <tr><th>isvu sifra</th><td>isvu sifra kolegija</td></tr>
	 * </table>
	 *  
	 * Parser prilikom izvođenja koristi cache za sve stringove, tako da osigurava će u konačnici
	 * svaki različiti string biti alociran samo jednom prilikom pohrane u listu.
	 *  
	 * @param lines retci
	 * @return lista zapisa o studentima
	 * @throws IOException u slučaju pogreške
	 */
	public static List<SeminarGroupsInfoBean> parseCSVFormat(List<String> lines) throws IOException {
		List<SeminarGroupsInfoBean> resultList = new ArrayList<SeminarGroupsInfoBean>(lines.size());
		Map<String, String> cache = new HashMap<String, String>(1000);
		if(lines.size()<1) {
			throw new IOException("Format datoteke je pogrešan! Nisam dobio niti jedan redak.");
		}
		String first = lines.get(0);
		if(!first.equals("\"jmbag\";\"grupa\";\"studij\";\"nast_isvu_ozn\";\"nast_ime\";\"nast_prezime\";\"tema\";\"sifpred\"")) {
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
				if(elements.length!=8) {
					throw new IOException("Pronađen redak pogrešne duljine: "+line);
				}
				SeminarGroupsInfoBean item = new SeminarGroupsInfoBean();
				item.setJmbag(elements[0]);
				item.setGroupName(fromCache(cache,elements[1]));
				item.setKind(fromCache(cache,elements[2]));
				item.setMentorID(fromCache(cache,elements[3]));
				item.setTitle(elements[6]);
				item.setIsvuCode(fromCache(cache,elements[7]));
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
