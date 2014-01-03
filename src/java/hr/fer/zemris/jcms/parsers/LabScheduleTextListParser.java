package hr.fer.zemris.jcms.parsers;

import hr.fer.zemris.jcms.beans.ext.LabScheduleBean;
import hr.fer.zemris.jcms.beans.ext.LabScheduleBean.CategoryStudents;
import hr.fer.zemris.util.StringUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Parseri za datoteku koja čuva raspored studenata po labosima.
 * 
 * @author marcupic
 *
 */
public class LabScheduleTextListParser {

	/**
	 * Parsira predani izvor okteta u kojem svaki redak ima sljedeće elemente odvojene znakom "pipe":
	 * <table border="1">
	 * <tr><th>isvuCode</th><td>isvu šifra kolegija</td></tr>
	 * <tr><th>labN</th><td>oznaka labosa, formata 'L''A''B' pa redni broj, ili 'S''E''M' pa redni broj, ili ...</td></tr>
	 * <tr><th>datum</th><td>format yyyy-MM-dd</td></tr>
	 * <tr><th>pocetak</th><td>format HH:mm</td></tr>
	 * <tr><th>kraj</th><td>format HH:mm</td></tr>
	 * <tr><th>dvorana</th><td>npr A101</td></tr>
	 * <tr><th>kategorija 1</th><td>kategorija 1</td></tr>
	 * <tr><th>studenti kategorije 1</th><td>studenti kategorije 1</td></tr>
	 * <tr><th>kategorija 2</th><td>kategorija 2</td></tr>
	 * <tr><th>studenti kategorije 2</th><td>studenti kategorije 2</td></tr>
	 * <tr><th>...</th><td>...</td></tr>
	 * </table>
	 * Očekuje se da su podaci u streamu zapisani UTF-8 kodnom stranicom.
	 *  
	 * @param is izvor okteta
	 * @return lista zapisa
	 * @throws IOException u slučaju pogreške
	 */
	public static List<LabScheduleBean> parseTabbedFormat(InputStream is) throws IOException {
		List<String> lines = TextService.inputStreamToUTF8StringList(is);
		return parseTabbedFormat(lines);
	}
	
	/**
	 * Parsira predani reader u kojem svaki redak ima sljedeće elemente odvojene znakom tab:
	 * <table border="1">
	 * <tr><th>isvuCode</th><td>isvu šifra kolegija</td></tr>
	 * <tr><th>labN</th><td>oznaka labosa, formata 'L''A''B' pa redni broj, ili 'S''E''M' pa redni broj, ili ...</td></tr>
	 * <tr><th>datum</th><td>format yyyy-MM-dd</td></tr>
	 * <tr><th>pocetak</th><td>format HH:mm</td></tr>
	 * <tr><th>kraj</th><td>format HH:mm</td></tr>
	 * <tr><th>dvorana</th><td>npr A101</td></tr>
	 * <tr><th>kategorija 1</th><td>kategorija 1</td></tr>
	 * <tr><th>studenti kategorije 1</th><td>studenti kategorije 1</td></tr>
	 * <tr><th>kategorija 2</th><td>kategorija 2</td></tr>
	 * <tr><th>studenti kategorije 2</th><td>studenti kategorije 2</td></tr>
	 * <tr><th>...</th><td>...</td></tr>
	 * </table>
	 *  
	 * @param is reader
	 * @return lista zapisa
	 * @throws IOException u slučaju pogreške
	 */
	public static List<LabScheduleBean> parseTabbedFormat(Reader is) throws IOException {
		List<String> lines = TextService.readerToStringList(is);
		return parseTabbedFormat(lines);
	}
	
	/**
	 * Parsira predanu listu u kojoj svaki redak ima sljedeće elemente odvojene znakom tab:
	 * <table border="1">
	 * <tr><th>isvuCode</th><td>isvu šifra kolegija</td></tr>
	 * <tr><th>labN</th><td>oznaka labosa, formata 'L''A''B' pa redni broj, ili 'S''E''M' pa redni broj, ili ...</td></tr>
	 * <tr><th>datum</th><td>format yyyy-MM-dd</td></tr>
	 * <tr><th>pocetak</th><td>format HH:mm</td></tr>
	 * <tr><th>kraj</th><td>format HH:mm</td></tr>
	 * <tr><th>dvorana</th><td>npr A101</td></tr>
	 * <tr><th>kategorija 1</th><td>kategorija 1</td></tr>
	 * <tr><th>studenti kategorije 1</th><td>studenti kategorije 1</td></tr>
	 * <tr><th>kategorija 2</th><td>kategorija 2</td></tr>
	 * <tr><th>studenti kategorije 2</th><td>studenti kategorije 2</td></tr>
	 * <tr><th>...</th><td>...</td></tr>
	 * </table>
	 *  
	 * Parser prilikom izvođenja koristi cache za sve stringove, tako da osigurava će u konačnici
	 * svaki različiti string biti alociran samo jednom prilikom pohrane u listu.
	 *  
	 * @param lines retci
	 * @return listu zapisa
	 * @throws IOException u slučaju pogreške
	 */
	public static List<LabScheduleBean> parseTabbedFormat(List<String> lines) throws IOException {
		List<LabScheduleBean> resultList = new ArrayList<LabScheduleBean>(lines.size());
		Map<String, String> cache = new HashMap<String, String>(5000);
		for(String line : lines) {
			String[] elements = TextService.split(line, '|');
			if(elements.length<8 || (elements.length%2)==1) {
				System.out.println("Pronađen redak pogrešne duljine: "+line);
				continue;
			}
			for(int i = 0; i < elements.length; i++) {
				elements[i] = elements[i].trim();
			}
			String kind = elements[1];
			int posBroj = 0;
			while(posBroj<kind.length() && Character.isLetter(kind.charAt(posBroj))) posBroj++;
			if(posBroj>=kind.length()) throw new IOException("Identifikator "+kind+" je pogrešnog formata. Ne mogu očitati vrstu i redni broj komponente.");
			int redniBroj = Integer.parseInt(kind.substring(posBroj));
			kind = kind.substring(0, posBroj).toUpperCase();
			LabScheduleBean item = new LabScheduleBean(
					fromCache(cache, elements[0]),
					fromCache(cache, elements[2]),
					fromCache(cache, elements[3]),
					duration(elements[3],elements[4]),
					fromCache(cache, elements[5]),
					fromCache(cache, "FER"),
					redniBroj,
					fromCache(cache, kind));
			for(int i = 7; i<elements.length; i+=2) {
				String kategorija = elements[i-1];
				String[] jmbags = StringUtil.split(elements[i], ' ');
				List<String> lista = new ArrayList<String>(jmbags.length);
				for(int j=0; j<jmbags.length; j++) {
					lista.add(fromCache(cache, jmbags[j]));
				}
				CategoryStudents cs = new CategoryStudents(fromCache(cache, kategorija), lista);
				item.getStudents().add(cs);
			}
			resultList.add(item);
		}
		return resultList;
	}

	private static int duration(String time1, String time2) {
		int dif = (Integer.parseInt(time2.substring(0,2))*60+Integer.parseInt(time2.substring(3))) - (Integer.parseInt(time1.substring(0,2))*60+Integer.parseInt(time1.substring(3)));
		if(dif<0) dif=-dif;
		return dif;
	}

	private static String fromCache(Map<String, String> cache, String text) {
		String value = cache.get(text);
		if(value!=null) return value;
		value = new String(text);
		cache.put(value, value);
		return value;
	}
}
