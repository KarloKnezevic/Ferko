package hr.fer.zemris.jcms.parsers;

import hr.fer.zemris.jcms.beans.ext.AssessmentScheduleBean;

import java.io.IOException;
import java.io.Reader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Parser za datoteku s rasporedom ispita
 * @author TOMISLAV
 *
 */
public class AssessmentScheduleParser {
	
	/**
	 * Parsira predani reader u kojem svaki redak ima sljedeće elemente odvojene znakom tab:
	 * <table border="1">
	 * <tr><th>date</th><td>datum, u formatu yyyy-MM-dd</td></tr>
	 * <tr><th>time</th><td>početak, u formatu HH:mm</td></tr>
	 * <tr><th>duration</th><td>trajanje u satima</td></tr>
	 * <tr><th>name</th><td>ime kolegija</td></tr>
	 * <tr><th>isvuCode</th><td>isvu šifra kolegija</td></tr>
	 * </table>
	 * NAPOMENA: mozda staviti duration u minutama umjesto u satima, ukoliko je to moguce
	 * 
	 * @param is reader
	 * @return listu beanova koji opisuju CourseWideEvente
	 * @throws IOException u slučaju pogreške
	 * @throws ParseException u slucaju pogreske prilikom parsiranja
	 */
	public static List<AssessmentScheduleBean> parseTabbedFormat(Reader is) throws IOException, ParseException {
		
		List<String> lines = TextService.readerToStringList(is);
		List<AssessmentScheduleBean> resultList = new ArrayList<AssessmentScheduleBean>(lines.size());
		if(lines.isEmpty()) return resultList;
		int pos = lines.get(0).indexOf('\t');
		char separator = pos != -1 ? '\t' : '#';
		
		for (String line : lines) {
			String[] elements = TextService.split(line, separator);
			if(elements.length!=5) {
				throw new ParseException("Found unexpected row length: "+line,0);
			}
			AssessmentScheduleBean esb = new AssessmentScheduleBean();
			String ISVUCode = elements[4]; 
			esb.setCourseISVUCode(ISVUCode);
			
			DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
			Date start = null;
			try {
				start = df.parse(elements[0]+" "+elements[1]);
			} catch (ParseException ex) {
				throw new ParseException("Found wrong formatted date. Row: "+line,0);
			}
			esb.setStart(start);
			
			int duration;
			if(elements[2].endsWith("m")) {
				duration = Integer.parseInt(elements[2].substring(0, elements[2].length()-1));
			} else {
				duration = 60*Integer.parseInt(elements[2]);
			}
			esb.setDuration(duration);
			
			resultList.add(esb);
		}
		
		return resultList;
	}
}
