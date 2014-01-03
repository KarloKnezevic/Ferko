package hr.fer.zemris.jcms.parsers;

import hr.fer.zemris.jcms.beans.VenueBean;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Parseri za primjerke lokacija.
 * 
 * @author marcupic
 *
 */
public class VenueParser {

	/**
	 * Parsira predani izvor okteta u kojem svaki redak ima sljedeće elemente odvojene znakom tab:
	 * <table border="1">
	 * <tr><th>venueShortName</th><td>kratki naziv lokacije</td></tr>
	 * <tr><th>venueName</th><td>puni naziv lokacije</td></tr>
	 * <tr><th>venueAddress</th><td>adresa lokacije</td></tr>
	 * <tr><th>venueLocator</th><td>lokator lokacije</td></tr>
	 * </table>
	 * Očekuje se da su podaci u streamu zapisani UTF-8 kodnom stranicom.
	 *  
	 * @param is izvor okteta
	 * @return lista lokacija
	 * @throws IOException u slučaju pogreške
	 */
	public static List<VenueBean> parseTabbedFormat(InputStream is) throws IOException {
		List<String> lines = TextService.inputStreamToUTF8StringList(is);
		return parseTabbedFormat(lines);
	}
	
	/**
	 * Parsira predani reader u kojem svaki redak ima sljedeće elemente odvojene znakom tab:
	 * <table border="1">
	 * <tr><th>venueShortName</th><td>kratki naziv lokacije</td></tr>
	 * <tr><th>venueName</th><td>puni naziv lokacije</td></tr>
	 * <tr><th>venueAddress</th><td>adresa lokacije</td></tr>
	 * <tr><th>venueLocator</th><td>lokator lokacije</td></tr>
	 * </table>
	 *  
	 * @param is reader
	 * @return lista lokacija
	 * @throws IOException u slučaju pogreške
	 */
	public static List<VenueBean> parseTabbedFormat(Reader is) throws IOException {
		List<String> lines = TextService.readerToStringList(is);
		return parseTabbedFormat(lines);
	}
	
	/**
	 * Parsira predanu listu u kojoj svaki redak ima sljedeće elemente odvojene znakom tab:
	 * <table border="1">
	 * <tr><th>venueShortName</th><td>kratki naziv lokacije</td></tr>
	 * <tr><th>venueName</th><td>puni naziv lokacije</td></tr>
	 * <tr><th>venueAddress</th><td>adresa lokacije</td></tr>
	 * <tr><th>venueLocator</th><td>lokator lokacije</td></tr>
	 * </table>
	 *  
	 * @param lines retci
	 * @return listu lokacija
	 * @throws IOException u slučaju pogreške
	 */
	public static List<VenueBean> parseTabbedFormat(List<String> lines) throws IOException {
		List<VenueBean> resultList = new ArrayList<VenueBean>(lines.size());
		if(lines.isEmpty()) return resultList;
		int pos = lines.get(0).indexOf('\t');
		char separator = pos != -1 ? '\t' : '#';
		for(String line : lines) {
			String[] elements = TextService.split(line, separator);
			VenueBean venue = new VenueBean();
			venue.setShortName(elements[0]);
			venue.setName(elements[1]);
			venue.setAddress(elements[2].length()==0 ? null : elements[2]);
			venue.setLocator(elements[3].length()==0 ? null : elements[3]);
			resultList.add(venue);
		}
		return resultList;
	}
}
