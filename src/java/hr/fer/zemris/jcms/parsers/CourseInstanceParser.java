package hr.fer.zemris.jcms.parsers;

import hr.fer.zemris.jcms.beans.ext.CourseInstanceBeanExt;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Parseri za primjerke kolegija.
 * 
 * @author marcupic
 *
 */
public class CourseInstanceParser {

	/**
	 * Parsira tekstualni stream u kojem svaki redak ima sljedeće elemente odvojene znakom tab:
	 * <table border="1">
	 * <tr><th>isvuCode</th><td>ISVU šifra kolegija</td></tr>
	 * <tr><th>courseName</th><td>Naziv kolegija</td></tr>
	 * </table>
	 * Očekuje se da su podaci u streamu zapisani UTF-8 kodnom stranicom.
	 *  
	 * @param is izvor podataka
	 * @return listu objekata {@link CourseInstanceBeanExt}
	 * @throws IOException u slučaju pogreške
	 */
	public static List<CourseInstanceBeanExt> parseTabbedFormat(InputStream is) throws IOException {
		List<String> lines = TextService.inputStreamToUTF8StringList(is);
		return parseTabbedFormat(lines);
	}
	
	/**
	 * Parsira tekstualni reader u kojem svaki redak ima sljedeće elemente odvojene znakom tab:
	 * <table border="1">
	 * <tr><th>isvuCode</th><td>ISVU šifra kolegija</td></tr>
	 * <tr><th>courseName</th><td>Naziv kolegija</td></tr>
	 * </table>
	 *  
	 * @param is izvor podataka
	 * @return listu objekata {@link CourseInstanceBeanExt}
	 * @throws IOException u slučaju pogreške
	 */
	public static List<CourseInstanceBeanExt> parseTabbedFormat(Reader is) throws IOException {
		List<String> lines = TextService.readerToStringList(is);
		return parseTabbedFormat(lines);
	}
	
	/**
	 * Parsira predanu listu u kojoj svaki redak ima sljedeće elemente odvojene znakom tab:
	 * <table border="1">
	 * <tr><th>isvuCode</th><td>ISVU šifra kolegija</td></tr>
	 * <tr><th>courseName</th><td>Naziv kolegija</td></tr>
	 * </table>
	 *  
	 * @param lines izvor podataka
	 * @return listu objekata {@link CourseInstanceBeanExt}
	 * @throws IOException u slučaju pogreške
	 */
	public static List<CourseInstanceBeanExt> parseTabbedFormat(List<String> lines) throws IOException {
		List<CourseInstanceBeanExt> resultList = new ArrayList<CourseInstanceBeanExt>(lines.size());
		if(lines.isEmpty()) return resultList;
		int pos = lines.get(0).indexOf('\t');
		char separator = pos != -1 ? '\t' : '#';
		for(String line : lines) {
			String[] elements = TextService.split(line, separator);
			CourseInstanceBeanExt cibx = new CourseInstanceBeanExt();
			cibx.setIsvuCode(elements[0]);
			cibx.setName(elements[1]);
			resultList.add(cibx);
		}
		return resultList;
	}
}
