package hr.fer.zemris.jcms.parsers;

import hr.fer.zemris.jcms.beans.ext.AssessmentFileUploadBean;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parseri za isvu datoteku koja čuva vezu između datoteka, studenata i opisnika,
 * korištena prilikom uploada ZIP datoteke na ispit.
 * 
 * @author marcupic
 *
 */
public class AssessmentFilesUploadParser {

	/**
	 * Parsira predani izvor okteta u kojem svaki redak ima sljedeće elemente odvojene znakom tab:
	 * <table border="1">
	 * <tr><th>jmbag</th><td>jmbag studenta; može biti null</td></tr>
	 * <tr><th>fileName</th><td>naziv datoteke</td></tr>
	 * <tr><th>descriptor</th><td>opisnik datoteke</td></tr>
	 * </table>
	 * Očekuje se da su podaci u streamu zapisani UTF-8 kodnom stranicom.
	 *  
	 * @param is izvor okteta
	 * @return lista datoteka
	 * @throws IOException u slučaju pogreške
	 */
	public static List<AssessmentFileUploadBean> parseTabbedFormat(InputStream is) throws IOException {
		List<String> lines = TextService.inputStreamToUTF8StringList(is);
		return parseTabbedFormat(lines);
	}
	
	/**
	 * Parsira predani reader u kojem svaki redak ima sljedeće elemente odvojene znakom tab:
	 * <table border="1">
	 * <tr><th>jmbag</th><td>jmbag studenta; može biti null</td></tr>
	 * <tr><th>fileName</th><td>naziv datoteke</td></tr>
	 * <tr><th>descriptor</th><td>opisnik datoteke</td></tr>
	 * </table>
	 *  
	 * @param is reader
	 * @return lista datoteka
	 * @throws IOException u slučaju pogreške
	 */
	public static List<AssessmentFileUploadBean> parseTabbedFormat(Reader is) throws IOException {
		List<String> lines = TextService.readerToStringList(is);
		return parseTabbedFormat(lines);
	}
	
	/**
	 * Parsira predanu listu u kojoj svaki redak ima sljedeće elemente odvojene znakom tab:
	 * <table border="1">
	 * <tr><th>jmbag</th><td>jmbag studenta; može biti null</td></tr>
	 * <tr><th>fileName</th><td>naziv datoteke</td></tr>
	 * <tr><th>descriptor</th><td>opisnik datoteke</td></tr>
	 * </table>
	 *  
	 * Parser prilikom izvođenja koristi cache za sve stringove, tako da osigurava će u konačnici
	 * svaki različiti string biti alociran samo jednom prilikom pohrane u listu.
	 *  
	 * @param lines retci
	 * @return lista datoteka
	 * @throws IOException u slučaju pogreške
	 */
	public static List<AssessmentFileUploadBean> parseTabbedFormat(List<String> lines) throws IOException {
		List<AssessmentFileUploadBean> resultList = new ArrayList<AssessmentFileUploadBean>(lines.size());
		if(lines.isEmpty()) return resultList;
		int pos = lines.get(0).indexOf('\t');
		char separator = pos != -1 ? '\t' : '#';
		Map<String, String> cache = new HashMap<String, String>(5000);
		for(String line : lines) {
			String[] elements = TextService.split(line, separator);
			if(elements.length!=4) {
				System.out.println("Pronađen redak pogrešne duljine: "+line);
				continue;
			}
			AssessmentFileUploadBean item = new AssessmentFileUploadBean();
			item.setJmbag(fromCache(cache,elements[0]));
			item.setFileName(fromCache(cache,elements[1]));
			item.setDescriptor(fromCache(cache,elements[2]));
			item.setDescription(fromCache(cache,elements[3]));
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
