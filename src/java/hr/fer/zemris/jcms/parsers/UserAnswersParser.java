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
 * @author Ivan Krišto
 *
 */
public class UserAnswersParser {

	/**
	 * Parsira predani izvor okteta u kojem svaki redak ima sljedeće elemente odvojene znakom tab.
	 * Format:
	 * <table border="1">
	 * 	<tr>
	 * 		<td>JMBAG</td>
	 * 		<td>Grupa</td>
	 * 		<td>Odgovor1</td>
	 * 		<td>Odgovor2</td>
	 * 		<td>Odgovor3</td>
	 * 		<td>...</td>
	 * 		<td>OdgovorN</td>
	 * 		<td>Opcionalno: staza do slike; ona se ignorira</td>
	 * 	</tr>
	 * </table>
	 * Elementi su međusobno odvojeni tabovima.<br />
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
	public static List<UserAnswersBean> parseTabbedMultiValueFormat(InputStream is, int problemsNum, int answersNum) throws IllegalArgumentException, IOException {
		List<String> lines = TextService.inputStreamToUTF8StringList(is);
		return parseTabbedMultiValueFormat(lines, problemsNum, answersNum);
	}
	
	/**
	 * Parsira predani izvor okteta u kojem svaki redak ima sljedeće elemente odvojene znakom tab.
	 * Format:
	 * <table border="1">
	 * 	<tr>
	 * 		<td>JMBAG</td>
	 * 		<td>Grupa</td>
	 * 		<td>Odgovor1</td>
	 * 		<td>Odgovor2</td>
	 * 		<td>Odgovor3</td>
	 * 		<td>...</td>
	 * 		<td>OdgovorN</td>
	 * 		<td>Opcionalno: staza do slike; ona se ignorira</td>
	 * 	</tr>
	 * </table>
	 * Elementi su međusobno odvojeni tabovima.<br />
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
	public static List<UserAnswersBean> parseTabbedMultiValueFormat(Reader is, int problemsNum, int answersNum) throws IllegalArgumentException, IOException {
		List<String> lines = TextService.readerToStringList(is);
		return parseTabbedMultiValueFormat(lines, problemsNum, answersNum);
	}
	
	/**
	 *Parsira predani izvor okteta u kojem svaki redak ima sljedeće elemente odvojene znakom tab.
	 * Format:
	 * <table border="1">
	 * 	<tr>
	 * 		<td>JMBAG</td>
	 * 		<td>Grupa</td>
	 * 		<td>Odgovor1</td>
	 * 		<td>Odgovor2</td>
	 * 		<td>Odgovor3</td>
	 * 		<td>...</td>
	 * 		<td>OdgovorN</td>
	 * 		<td>Opcionalno: staza do slike; ona se ignorira</td>
	 * 	</tr>
	 * </table>
	 * Elementi su međusobno odvojeni tabovima.<br />
	 * Grupa može biti BLANK (nije označena).<br />
	 * Odgovor može biti BLANK (neodgovoreno).
	 *  
	 * @param lines Retci.
	 * @param problemsNum Broj zadataka provjere.
	 * @param answersNum Broj ponuđenih rješenja po zadatku.
	 * @return lista bodova
	 * @throws IllegalArgumentException u slučaju pogreške
	 */
	public static List<UserAnswersBean> parseTabbedMultiValueFormat(List<String> lines, int problemsNum, int answersNum) throws IllegalArgumentException {
		List<UserAnswersBean> resultList = new ArrayList<UserAnswersBean>(lines.size());
		
		// MČ: Izbacio sam ovu provjeru, jer odgovori ne moraju nuzno ici od A do D; mogu krenuti od E do H...
		//Set<String> answersChoices = new HashSet<String>();
		//answersChoices.add("BLANK");
		
		//char currentChar = 'A';
		//for (int i = 1; i <= answersNum; i++) {
		//	answersChoices.add(Character.toString(currentChar));
		//	currentChar++;
		//}
		
		if(lines.isEmpty()) return resultList;
		int pos = lines.get(0).indexOf('\t');
		char separator = pos != -1 ? '\t' : '#';
		int lineNum = 0;
		for(String line : lines) {
			lineNum++;
			String[] elements = TextService.split(line, separator);
				
			int endIndex = problemsNum + 2;
			// Dopustimo i format koji ima jedan stupac viska: naziv datoteke u kojoj je slika
			if (elements.length != problemsNum + 2 && elements.length != problemsNum + 3) {
				throw new IllegalArgumentException(Integer.toString(lineNum)); // Za ostatak poruke se pobrine data objekt, nije baš OOP princip, ali ok...
			}
			
			UserAnswersBean item = new UserAnswersBean(elements[0].trim(), elements[1].trim(), problemsNum);
			for (int i = 2; i < endIndex; i++) {
				String answer = elements[i].toUpperCase();
				//if (!answersChoices.contains(answer)) {
				//	throw new IllegalArgumentException(Integer.toString(lineNum)); // Pročitaj gornji komentar.
				//}
				item.setValue(answer, i-2);
			}
			resultList.add(item);
		}
		return resultList;
	}
}
