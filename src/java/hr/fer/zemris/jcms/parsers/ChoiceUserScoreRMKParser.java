package hr.fer.zemris.jcms.parsers;

import hr.fer.zemris.jcms.beans.ext.ChoiceUserScoreBean;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Parseri za datoteku koja čuva popis bodova za studente za provjere sa obrascima (na zaokruživanje).
 * 
 * @author marcupic
 *
 */
public class ChoiceUserScoreRMKParser {

	/**
	 * Parsira predani izvor okteta koji po formatu odgovara onoj nesretnoj RMK datoteci.
	 * Očekuje se da su podaci u streamu zapisani UTF-8 kodnom stranicom.
	 *  
	 * @param is izvor okteta
	 * @return lista bodova
	 * @throws IOException u slučaju pogreške
	 */
	public static List<ChoiceUserScoreBean> parseRMKFormat(InputStream is) throws IOException {
		List<String> lines = TextService.inputStreamToUTF8StringList(is);
		return parseRMKFormat(lines);
	}
	
	/**
	 * Parsira predani izvor okteta koji po formatu odgovara onoj nesretnoj RMK datoteci.
	 *  
	 * @param is reader
	 * @return lista bodova
	 * @throws IOException u slučaju pogreške
	 */
	public static List<ChoiceUserScoreBean> parseRMKFormat(Reader is) throws IOException {
		List<String> lines = TextService.readerToStringList(is);
		return parseRMKFormat(lines);
	}
	
	/**
	 * Parsira predani izvor okteta koji po formatu odgovara onoj nesretnoj RMK datoteci.
	 *  
	 * @param lines retci
	 * @return lista bodova
	 * @throws IOException u slučaju pogreške
	 */
	public static List<ChoiceUserScoreBean> parseRMKFormat(List<String> lines) throws IOException {
		List<ChoiceUserScoreBean> resultList = new ArrayList<ChoiceUserScoreBean>(lines.size());
		
		if(lines.isEmpty()) return resultList;
		char separator = '\t';
		int lineno = 0;
		for(String line : lines) {
			lineno++;
			if((lineno % 2)==1) continue; // neparne retke preskaci jer sadrze smece
			int firstSeparator = line.indexOf(separator);
			int secondSeparator = line.indexOf(separator, firstSeparator + 1);
			String jmbag = line.substring(0, firstSeparator);
			String group = line.substring(firstSeparator + 1, secondSeparator);
			String answers = line.substring(secondSeparator + 1);
			int lastPos = answers.lastIndexOf(separator);
			if(lastPos!=-1) {
				answers = answers.substring(0, lastPos);
			}
			ChoiceUserScoreBean item = new ChoiceUserScoreBean(jmbag.trim(), group.trim(), answers);
			resultList.add(item);
		}
		return resultList;
	}
}
