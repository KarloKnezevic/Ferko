package hr.fer.zemris.jcms.parsers;

import hr.fer.zemris.jcms.beans.ext.ChoiceProblemMappingBean;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Parseri mapiranja zadataka kod provjere sa obrascima (na zaokruživanje).
 * 
 * @author Ivan Krišto
 *
 */
public class ChoiceProblemMappingParser {

	/**
	 * Parsira predani izvor okteta u kojem svaki redak ima sljedeće elemente odvojene znakom tab:
	 * <table border="1">
	 *		<tr>
	 *			<td>Grupa 1</td>
	 *			<td>Oznaka 1. zadatka</td>
	 *			<td>Tip zadatka</td>
	 *			<td>Verzija tipa zadatka</td>
	 *		</tr>
	 *		<tr>
	 *			<td>Grupa 1</td>
	 *			<td>Oznaka 2. zadatka</td>
	 *			<td>Tip zadatka</td>
	 *			<td>Verzija tipa zadatka</td>
	 *		</tr>
	 *		<tr>
	 *			<td>Grupa 1</td>
	 *			<td>Oznaka zadnjeg zadatka</td>
	 *			<td>Tip zadatka</td>
	 *			<td>Verzija tipa zadatka</td>
	 *		</tr>
	 *		<tr>
	 *			<td>Grupa 2</td>
	 *			<td>Oznaka 1. zadatka</td>
	 *			<td>Tip zadatka</td>
	 *			<td>Verzija tipa zadatka</td>
	 *		</tr>
	 *		<tr>
	 *			<td>Grupa 2</td>
	 *			<td>Oznaka 2. zadatka</td>
	 *			<td>Tip zadatka</td>
	 *			<td>Verzija tipa zadatka</td>
	 *		</tr>
	 *		<tr>
	 *			<td>Grupa 2</td>
	 *			<td>Oznaka zadnjeg zadatka</td>
	 *			<td>Tip zadatka</td>
	 *			<td>Verzija tipa zadatka</td>
	 *		</tr>
	 *	</table>
	 * Mapiranje je potrebno definirati za sve zadatke i sve grupe!<br /> 
	 * Podatci o mapiranju za pojedini zadatak pojedine grupe su odvojeni tabom.<br /> 
	 * Očekuje se da su podaci u streamu zapisani UTF-8 kodnom stranicom.
	 *  
	 * @param is izvor okteta
	 * @return lista sa podatcima o mapiranju
	 * @throws IOException u slučaju pogreške
	 */
	public static List<ChoiceProblemMappingBean> parseTabbedMultiValueFormat(InputStream is) throws IOException {
		List<String> lines = TextService.inputStreamToUTF8StringList(is);
		return parseTabbedMultiValueFormat(lines);
	}
	
	/**
	 * Parsira predani reader u kojem svaki redak ima sljedeće elemente odvojene znakom tab:
	 * <table border="1">
	 *		<tr>
	 *			<td>Grupa 1</td>
	 *			<td>Oznaka 1. zadatka</td>
	 *			<td>Tip zadatka</td>
	 *			<td>Verzija tipa zadatka</td>
	 *		</tr>
	 *		<tr>
	 *			<td>Grupa 1</td>
	 *			<td>Oznaka 2. zadatka</td>
	 *			<td>Tip zadatka</td>
	 *			<td>Verzija tipa zadatka</td>
	 *		</tr>
	 *		<tr>
	 *			<td>Grupa 1</td>
	 *			<td>Oznaka zadnjeg zadatka</td>
	 *			<td>Tip zadatka</td>
	 *			<td>Verzija tipa zadatka</td>
	 *		</tr>
	 *		<tr>
	 *			<td>Grupa 2</td>
	 *			<td>Oznaka 1. zadatka</td>
	 *			<td>Tip zadatka</td>
	 *			<td>Verzija tipa zadatka</td>
	 *		</tr>
	 *		<tr>
	 *			<td>Grupa 2</td>
	 *			<td>Oznaka 2. zadatka</td>
	 *			<td>Tip zadatka</td>
	 *			<td>Verzija tipa zadatka</td>
	 *		</tr>
	 *		<tr>
	 *			<td>Grupa 2</td>
	 *			<td>Oznaka zadnjeg zadatka</td>
	 *			<td>Tip zadatka</td>
	 *			<td>Verzija tipa zadatka</td>
	 *		</tr>
	 *	</table>
	 * Mapiranje je potrebno definirati za sve zadatke i sve grupe!<br /> 
	 * Podatci o mapiranju za pojedini zadatak pojedine grupe su odvojeni tabom.<br /> 
	 *  
	 * @param is reader
	 * @return lista sa podatcima o mapiranju
	 * @throws IOException u slučaju pogreške
	 */
	public static List<ChoiceProblemMappingBean> parseTabbedMultiValueFormat(Reader is) throws IOException {
		List<String> lines = TextService.readerToStringList(is);
		return parseTabbedMultiValueFormat(lines);
	}
	
	/**
	 * Parsira predanu listu u kojoj svaki redak ima sljedeće elemente odvojene znakom tab:
	 * <table border="1">
	 *		<tr>
	 *			<td>Grupa 1</td>
	 *			<td>Oznaka 1. zadatka</td>
	 *			<td>Tip zadatka</td>
	 *			<td>Verzija tipa zadatka</td>
	 *		</tr>
	 *		<tr>
	 *			<td>Grupa 1</td>
	 *			<td>Oznaka 2. zadatka</td>
	 *			<td>Tip zadatka</td>
	 *			<td>Verzija tipa zadatka</td>
	 *		</tr>
	 *		<tr>
	 *			<td>Grupa 1</td>
	 *			<td>Oznaka zadnjeg zadatka</td>
	 *			<td>Tip zadatka</td>
	 *			<td>Verzija tipa zadatka</td>
	 *		</tr>
	 *		<tr>
	 *			<td>Grupa 2</td>
	 *			<td>Oznaka 1. zadatka</td>
	 *			<td>Tip zadatka</td>
	 *			<td>Verzija tipa zadatka</td>
	 *		</tr>
	 *		<tr>
	 *			<td>Grupa 2</td>
	 *			<td>Oznaka 2. zadatka</td>
	 *			<td>Tip zadatka</td>
	 *			<td>Verzija tipa zadatka</td>
	 *		</tr>
	 *		<tr>
	 *			<td>Grupa 2</td>
	 *			<td>Oznaka zadnjeg zadatka</td>
	 *			<td>Tip zadatka</td>
	 *			<td>Verzija tipa zadatka</td>
	 *		</tr>
	 *	</table>
	 * Mapiranje je potrebno definirati za sve zadatke i sve grupe!<br /> 
	 * Podatci o mapiranju za pojedini zadatak pojedine grupe su odvojeni tabom.<br /> 
	 *  
	 * @param lines retci
	 * @return lista sa podatcima o mapiranju
	 * @throws IOException u slučaju pogreške
	 */
	public static List<ChoiceProblemMappingBean> parseTabbedMultiValueFormat(List<String> lines) throws IOException {
		List<ChoiceProblemMappingBean> resultList = new ArrayList<ChoiceProblemMappingBean>(lines.size());
		
		if(lines.isEmpty()) return resultList;
		int pos = lines.get(0).indexOf('\t');
		char separator = pos != -1 ? '\t' : '#';
		for(String line : lines) {
			int firstSeparator = line.indexOf(separator);
			int secondSeparator = line.indexOf(separator, firstSeparator + 1);
			int thirdSeparator = line.indexOf(separator, secondSeparator + 1);
			String group = line.substring(0, firstSeparator);
			String problem = line.substring(firstSeparator + 1, secondSeparator);
			String type = line.substring(secondSeparator + 1, thirdSeparator);
			String version = line.substring(thirdSeparator + 1);
			ChoiceProblemMappingBean item = new ChoiceProblemMappingBean(group.trim(), problem.trim(), type.trim(), version.trim());
			resultList.add(item);
		}
		return resultList;
	}
}
