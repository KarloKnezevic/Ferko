package hr.fer.zemris.jcms.parsers;

import hr.fer.zemris.jcms.beans.UserBean;
import hr.fer.zemris.util.StringUtil;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Parseri za datoteku koja čuva vezu JMBAG - username za studente.
 * 
 * @author marcupic
 *
 */
public class UserImportParser {

	/**
	 * Parsira predani izvor okteta u kojem svaki redak ima sljedeće elemente odvojene znakom tab:
	 * <table border="1">
	 * <tr><th>jmbag</th><td>jmbag studenta</td></tr>
	 * <tr><th>lastName</th><td>prezime studenta</td></tr>
	 * <tr><th>firstName</th><td>ime studenta</td></tr>
	 * <tr><th>username</th><td>korisničko ime</td></tr>
	 * <tr><th>email</th><td>email</td></tr>
	 * </table>
	 * Očekuje se da su podaci u streamu zapisani UTF-8 kodnom stranicom.
	 *  
	 * @param is izvor okteta
	 * @return lista korisnika s popunjenim odgovarajućim poljima
	 * @throws IOException u slučaju pogreške
	 */
	public static List<UserBean> parseTabbedFormat(InputStream is) throws IOException {
		List<String> lines = TextService.inputStreamToUTF8StringList(is);
		return parseTabbedFormat(lines);
	}
	
	/**
	 * Parsira predani reader u kojem svaki redak ima sljedeće elemente odvojene znakom tab:
	 * <table border="1">
	 * <tr><th>jmbag</th><td>jmbag studenta</td></tr>
	 * <tr><th>lastName</th><td>prezime studenta</td></tr>
	 * <tr><th>firstName</th><td>ime studenta</td></tr>
	 * <tr><th>username</th><td>korisničko ime</td></tr>
	 * <tr><th>email</th><td>email</td></tr>
	 * </table>
	 *  
	 * @param is reader
	 * @return lista korisnika s popunjenim odgovarajućim poljima
	 * @throws IOException u slučaju pogreške
	 */
	public static List<UserBean> parseTabbedFormat(Reader is) throws IOException {
		List<String> lines = TextService.readerToStringList(is);
		return parseTabbedFormat(lines);
	}
	
	/**
	 * Parsira predanu listu u kojoj svaki redak ima sljedeće elemente odvojene znakom tab:
	 * <table border="1">
	 * <tr><th>jmbag</th><td>jmbag studenta</td></tr>
	 * <tr><th>lastName</th><td>prezime studenta</td></tr>
	 * <tr><th>firstName</th><td>ime studenta</td></tr>
	 * <tr><th>username</th><td>korisničko ime</td></tr>
	 * <tr><th>email</th><td>email</td></tr>
	 * </table>
	 *  
	 * @param lines retci
	 * @return lista korisnika s popunjenim odgovarajućim poljima
	 * @throws IOException u slučaju pogreške
	 */
	public static List<UserBean> parseTabbedFormat(List<String> lines) throws IOException {
		List<UserBean> resultList = new ArrayList<UserBean>(lines.size());
		if(lines.isEmpty()) return resultList;
		int pos = lines.get(0).indexOf('\t');
		char separator = pos != -1 ? '\t' : '#';
		for(String line : lines) {
			String[] elements = TextService.split(line, separator);
			if(elements.length!=5) {
				throw new IOException("Format datoteke je neispravan. Pronađen je redak s "+elements.length+" elemenata.");
			}
			for(int i = 0; i < elements.length; i++) {
				elements[i] = elements[i].trim();
			}
			if(StringUtil.isStringBlank(elements[0]) || StringUtil.isStringBlank(elements[1]) || StringUtil.isStringBlank(elements[2])) {
				throw new IOException("Pronađen je redak koji ne sadrži jmbag ili prezime ili ime.");
			}
			UserBean bean = new UserBean();
			bean.setJmbag(elements[0]);
			bean.setLastName(elements[1]);
			bean.setFirstName(elements[2]);
			bean.setUsername(elements[3]);
			bean.setAuthUsername(elements[3]);
			bean.setEmail(elements[4]);
			resultList.add(bean);
		}
		return resultList;
	}
	
	public static List<String> parseJmbags(Reader is) throws IOException {
		
		List<String> resultList = TextService.readerToStringList(is);
		
		return resultList;
		
	}
}
