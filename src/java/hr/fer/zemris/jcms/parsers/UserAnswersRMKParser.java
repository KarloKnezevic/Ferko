package hr.fer.zemris.jcms.parsers;

import hr.fer.zemris.jcms.beans.ext.UserAnswersBean;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Parseri za datoteku koja čuva popis bodova za studente za provjere sa brojem bodova po zadatku.
 * 
 * @author marcupic
 *
 */
public class UserAnswersRMKParser {

	/**
	 * Parsira predani izvor okteta koji po formatu odgovara onoj nesretnoj RMK datoteci.
	 * Grupa može biti BLANK (nije označena).<br />
	 * Odgovor može biti BLANK (neodgovoreno).<br />
	 * Očekuje se da su podaci u streamu zapisani UTF-8 kodnom stranicom.
	 *  
	 * @param is izvor okteta
	 * @param problemsNum Broj zadataka provjere.
	 * @param answersNum Broj ponuđenih rješenja po zadatku.
	 * @return Lista odgovora.
	 * @throws IllegalArgumentException U slučaju pogreške.
	 * @throws IOException Ako dođe do problema sa čitanjem toka podataka.
	 */
	public static List<UserAnswersBean> parseRMKFormat(InputStream is, int problemsNum, int answersNum) throws IllegalArgumentException, IOException {
		List<String> lines = TextService.inputStreamToUTF8StringList(is);
		return parseRMKFormat(lines, problemsNum, answersNum);
	}
	
	/**
	 * Parsira predani izvor okteta koji po formatu odgovara onoj nesretnoj RMK datoteci.
	 * Grupa može biti BLANK (nije označena).<br />
	 * Odgovor može biti BLANK (neodgovoreno).
	 *  
	 * @param is reader
	 * @param problemsNum Broj zadataka provjere.
	 * @param answersNum Broj ponuđenih rješenja po zadatku.
	 * @return lista bodova
	 * @throws IllegalArgumentException u slučaju pogreške.
	 * @throws IOException Ako dođe do problema sa čitanjem toka podataka.
	 */
	public static List<UserAnswersBean> parseRMKFormat(Reader is, int problemsNum, int answersNum) throws IllegalArgumentException, IOException {
		List<String> lines = TextService.readerToStringList(is);
		return parseRMKFormat(lines, problemsNum, answersNum);
	}
	
	/**
	 * Parsira predani izvor okteta koji po formatu odgovara onoj nesretnoj RMK datoteci.
	 * Grupa može biti BLANK (nije označena).<br />
	 * Odgovor može biti BLANK (neodgovoreno).
	 *  
	 * @param lines Retci.
	 * @param problemsNum Broj zadataka provjere.
	 * @param answersNum Broj ponuđenih rješenja po zadatku.
	 * @return lista bodova
	 * @throws IllegalArgumentException u slučaju pogreške
	 */
	public static List<UserAnswersBean> parseRMKFormat(List<String> lines, int problemsNum, int answersNum) throws IllegalArgumentException {
		List<UserAnswersBean> resultList = new ArrayList<UserAnswersBean>(lines.size());
		
		if(lines.isEmpty()) return resultList;
		char separator = '\t';
		int lineNum = 0;
		for(String line : lines) {
			lineNum++;
			if(lineNum==1) continue; // prvu liniju preskoci jer sadrzi smece...
			if((lineNum%2) == 1) continue; // ako je linija neparna, sadrzi smece pa je preskoci!
			String[] elements = TextService.split(line, separator);
				
			int endIndex = problemsNum + 2;
			// Dopustimo i format koji ima jedan stupac viska: naziv datoteke u kojoj je slika
			if (elements.length != problemsNum + 2 && elements.length != problemsNum + 3) {
				throw new IllegalArgumentException(Integer.toString(lineNum)); // Za ostatak poruke se pobrine data objekt, nije baš OOP princip, ali ok...
			}
			
			UserAnswersBean item = new UserAnswersBean(elements[0].trim(), elements[1].trim(), problemsNum);
			for (int i = 2; i < endIndex; i++) {
				String answer = elements[i].toUpperCase();
				item.setValue(answer, i-2);
			}
			resultList.add(item);
		}
		return resultList;
	}
}
