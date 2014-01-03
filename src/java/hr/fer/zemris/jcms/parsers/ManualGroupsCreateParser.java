package hr.fer.zemris.jcms.parsers;

import hr.fer.zemris.jcms.beans.ManualGroupsCreateBean;
import hr.fer.zemris.util.StringUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

public class ManualGroupsCreateParser {
	
	/**
	 * Parsira predani izvor okteta u kojem svaki redak ima sljedeće elemente odvojene znakom tab ili ljestvama:
	 * <table border="1">
	 * <tr><th>Naziv događaja</th><td>Naziv koji želite dati događaju. Primjerice: "Digitalna logika - lab. vježba 3".</td></tr>
	 * <tr><th>Datum</th><td>Datum kada se termin odvija. Format je YYYY-MM-DD, primjerice 2009-02-20.</td></tr>
	 * <tr><th>Početak termina</th><td>Vrijeme kada termin počinje. Format je HH:mm, primjerice 13:45.</td></tr>
	 * <tr><th>Kraj termina</th><td>Vrijeme kada termin završava. Format je HH:mm, primjerice 15:00.</td></tr>
	 * <tr><th>Lokacija</th><td>Gdje se događaj odvija? Primjerice: FER.</td></tr>
	 * <tr><th>Prostorija</th><td>U kojoj prostoriji se događaj odvija? Primjerice: PCLAB2</td></tr>
	 * </table>
	 * Očekuje se da su podaci u streamu zapisani UTF-8 kodnom stranicom.
	 * 
	 * @param is
	 * @return
	 * @throws IOException
	 * @throws ParseException
	 */
	public static List<ManualGroupsCreateBean> parseTabbedFormat(InputStream is) throws IOException, ParseException {
		List<String> lines = TextService.inputStreamToUTF8StringList(is);
		return parseTabbedFormat(lines);
	}

	/**
	 * Parsira predani izvor okteta u kojem svaki redak ima sljedeće elemente odvojene znakom tab ili ljestvama:
	 * <table border="1">
	 * <tr><th>Naziv događaja</th><td>Naziv koji želite dati događaju. Primjerice: "Digitalna logika - lab. vježba 3".</td></tr>
	 * <tr><th>Datum</th><td>Datum kada se termin odvija. Format je YYYY-MM-DD, primjerice 2009-02-20.</td></tr>
	 * <tr><th>Početak termina</th><td>Vrijeme kada termin počinje. Format je HH:mm, primjerice 13:45.</td></tr>
	 * <tr><th>Kraj termina</th><td>Vrijeme kada termin završava. Format je HH:mm, primjerice 15:00.</td></tr>
	 * <tr><th>Lokacija</th><td>Gdje se događaj odvija? Primjerice: FER.</td></tr>
	 * <tr><th>Prostorija</th><td>U kojoj prostoriji se događaj odvija? Primjerice: PCLAB2</td></tr>
	 * </table>
	 * 
	 * @param is
	 * @return
	 * @throws IOException
	 * @throws ParseException
	 */
	public static List<ManualGroupsCreateBean> parseTabbedFormat(Reader is) throws IOException, ParseException {
		List<String> lines = TextService.readerToStringList(is);
		return parseTabbedFormat(lines);
	}
	
	/**
	 * Parsira predani izvor okteta u kojem svaki redak ima sljedeće elemente odvojene znakom tab ili ljestvama:
	 * <table border="1">
	 * <tr><th>Naziv događaja</th><td>Naziv koji želite dati događaju. Primjerice: "Digitalna logika - lab. vježba 3".</td></tr>
	 * <tr><th>Datum</th><td>Datum kada se termin odvija. Format je YYYY-MM-DD, primjerice 2009-02-20.</td></tr>
	 * <tr><th>Početak termina</th><td>Vrijeme kada termin počinje. Format je HH:mm, primjerice 13:45.</td></tr>
	 * <tr><th>Kraj termina</th><td>Vrijeme kada termin završava. Format je HH:mm, primjerice 15:00.</td></tr>
	 * <tr><th>Lokacija</th><td>Gdje se događaj odvija? Primjerice: FER.</td></tr>
	 * <tr><th>Prostorija</th><td>U kojoj prostoriji se događaj odvija? Primjerice: PCLAB2</td></tr>
	 * </table>
	 * 
	 * @param lines
	 * @return
	 * @throws IOException
	 * @throws ParseException
	 */
	public static List<ManualGroupsCreateBean> parseTabbedFormat(List<String> lines) throws IOException, ParseException {
		
		List<ManualGroupsCreateBean> resultList = new ArrayList<ManualGroupsCreateBean>(lines.size());
		Set<String> duplicates = new HashSet<String>(lines.size());
		if(lines.isEmpty()) return resultList;
		int pos = lines.get(0).indexOf('\t');
		char separator = pos != -1 ? '\t' : '#';
		Pattern patternTime = Pattern.compile("^[0-9][0-9]:[0-9][0-9]$");
		Pattern patternYear = Pattern.compile("^[0-9][0-9][0-9][0-9]-[0-9][0-9]-[0-9][0-9]$");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		for (String line : lines) {
			if(!duplicates.add(line)) continue;
			String[] elements = TextService.split(line, separator);
			if(elements.length!=6) {
				System.out.println("Pronađen redak pogrešne duljine: "+line);
				throw new ParseException("Pronađen redak pogrešne duljine: "+line, 0);
			}
			for(int i = 0; i < elements.length; i++) {
				elements[i] = elements[i].trim();
			}
			if(!patternYear.matcher(elements[1]).matches()) {
				System.out.println("Format godine nije ispravan: "+elements[1]);
				throw new ParseException("Format godine nije ispravan: "+elements[1], 0);
			}
			if(!patternTime.matcher(elements[2]).matches()) {
				System.out.println("Format vremena početka nije ispravan: "+elements[2]);
				throw new ParseException("Format vremena početka nije ispravan: "+elements[2], 0);
			}
			if(!patternTime.matcher(elements[3]).matches()) {
				System.out.println("Format vremena kraja nije ispravan: "+elements[3]);
				throw new ParseException("Format vremena kraja nije ispravan: "+elements[3], 0);
			}
			if(StringUtil.isStringBlank(elements[0])||StringUtil.isStringBlank(elements[4])||StringUtil.isStringBlank(elements[5])) {
				System.out.println("Naziv, lokacija i/ili prostorija nisu zadani: "+line);
				throw new ParseException("Naziv, lokacija i/ili prostorija nisu zadani: "+line, 0);
			}
			ManualGroupsCreateBean item = new ManualGroupsCreateBean();
			item.setTitle(elements[0]);
			item.setDate(elements[1]);
			item.setStartTime(elements[2]);
			item.setEndTime(elements[3]);
			item.setVenue(elements[4]);
			item.setRoomName(elements[5]);
			item.setDuration(Integer.parseInt(item.getEndTime().substring(0, 2))*60+Integer.parseInt(item.getEndTime().substring(3, 5))
					- (Integer.parseInt(item.getStartTime().substring(0, 2))*60+Integer.parseInt(item.getStartTime().substring(3, 5))));
			item.setStartDate(sdf.parse(item.getDate()+" "+item.getStartTime()));

			if(item.getDuration()<0) {
				throw new ParseException("Trajanje dogadaja ne može biti negativno: "+line, 0);
			}
			resultList.add(item);
		}
		
		return resultList;
	}
}
