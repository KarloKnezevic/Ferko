package hr.fer.zemris.jcms.parsers;

import hr.fer.zemris.jcms.beans.RoomBean;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Parseri za soba (prostorija).
 * 
 * @author marcupic
 *
 */
public class RoomParser {

	/**
	 * Parsira predani reader u kojem svaki redak ima sljedeće elemente odvojene znakom tab:
	 * <table border="1">
	 * <tr><th>venueShortName</th><td>kratki naziv lokacije</td></tr>
	 * <tr><th>roomShortName</th><td>kratki naziv prostorije</td></tr>
	 * <tr><th>roomName</th><td>puni naziv prostorije</td></tr>
	 * <tr><th>roomLocator</th><td>Lokator prostorije</td></tr>
	 * <tr><th>roomLecturePlaces</th><td>broj mjesta za predavanja u prostoriji</td></tr>
	 * <tr><th>roomExercisePlaces</th><td>broj radnih mjesta za laboratorijske vježbe u prostoriji</td></tr>
	 * <tr><th>roomAssessmentPlaces</th><td>broj mjesta za pisanje ispita u prostoriji</td></tr>
	 * <tr><th>roomAssessmentAssistants</th><td>broj asistenata koji su potrebni za čuvanje ispita u prostoriji</td></tr>
	 * <tr><th>roomPublic</th><td>je li prostorija javna</td></tr>
	 * </table>
	 *  
	 * @param is reader
	 * @return lista soba
	 * @throws IOException u slučaju pogreške
	 */
	public static List<RoomBean> parseTabbedFormat(Reader is) throws IOException {
		List<String> lines = TextService.readerToStringList(is);
		return parseTabbedFormat(lines);
	}
	
	/**
	 * Parsira predani izvor redaka teksta u kojem svaki redak ima sljedeće elemente odvojene znakom tab:
	 * <table border="1">
	 * <tr><th>venueShortName</th><td>kratki naziv lokacije</td></tr>
	 * <tr><th>roomShortName</th><td>kratki naziv prostorije</td></tr>
	 * <tr><th>roomName</th><td>puni naziv prostorije</td></tr>
	 * <tr><th>roomLocator</th><td>Lokator prostorije</td></tr>
	 * <tr><th>roomLecturePlaces</th><td>broj mjesta za predavanja u prostoriji</td></tr>
	 * <tr><th>roomExercisePlaces</th><td>broj radnih mjesta za laboratorijske vježbe u prostoriji</td></tr>
	 * <tr><th>roomAssessmentPlaces</th><td>broj mjesta za pisanje ispita u prostoriji</td></tr>
	 * <tr><th>roomAssessmentAssistants</th><td>broj asistenata koji su potrebni za čuvanje ispita u prostoriji</td></tr>
	 * <tr><th>roomPublic</th><td>je li prostorija javna</td></tr>
	 * </table>
	 * Očekuje se da su podaci u streamu zapisani UTF-8 kodnom stranicom.
	 *  
	 * @param is izvor redaka
	 * @return lista soba
	 * @throws IOException u slučaju pogreške
	 */
	public static List<RoomBean> parseTabbedFormat(InputStream is) throws IOException {
		List<String> lines = TextService.inputStreamToUTF8StringList(is);
		return parseTabbedFormat(lines);
	}
	
	/**
	 * Parsira predanu listu u kojoj svaki redak ima sljedeće elemente odvojene znakom tab:
	 * <table border="1">
	 * <tr><th>venueShortName</th><td>kratki naziv lokacije</td></tr>
	 * <tr><th>roomShortName</th><td>kratki naziv prostorije</td></tr>
	 * <tr><th>roomName</th><td>puni naziv prostorije</td></tr>
	 * <tr><th>roomLocator</th><td>Lokator prostorije</td></tr>
	 * <tr><th>roomLecturePlaces</th><td>broj mjesta za predavanja u prostoriji</td></tr>
	 * <tr><th>roomExercisePlaces</th><td>broj radnih mjesta za laboratorijske vježbe u prostoriji</td></tr>
	 * <tr><th>roomAssessmentPlaces</th><td>broj mjesta za pisanje ispita u prostoriji</td></tr>
	 * <tr><th>roomAssessmentAssistants</th><td>broj asistenata koji su potrebni za čuvanje ispita u prostoriji</td></tr>
	 * <tr><th>roomPublic</th><td>je li prostorija javna</td></tr>
	 * </table>
	 *  
	 * @param lines retci
	 * @return lista soba
	 * @throws IOException u slučaju pogreške
	 */
	public static List<RoomBean> parseTabbedFormat(List<String> lines) throws IOException {
		List<RoomBean> resultList = new ArrayList<RoomBean>(lines.size());
		if(lines.isEmpty()) return resultList;
		int pos = lines.get(0).indexOf('\t');
		char separator = pos != -1 ? '\t' : '#';
		for(String line : lines) {
			String[] elements = TextService.split(line, separator);
			RoomBean room = new RoomBean();
			room.setVenueShortName(elements[0]);
			room.setShortName(elements[1]);
			room.setName(elements[2]);
			room.setLocator(elements[3]);
			room.setLecturePlaces(Integer.parseInt(elements[4]));
			room.setExercisePlaces(Integer.parseInt(elements[5]));
			room.setAssessmentPlaces(Integer.parseInt(elements[6]));
			room.setAssessmentAssistants(Integer.parseInt(elements[7]));
			room.setPublicRoom(elements[8].equals("1")||elements[8].equals("true"));
			room.setId(room.getVenueShortName()+"/"+room.getShortName());
			resultList.add(room);
		}
		return resultList;
	}
	
}
