package hr.fer.zemris.jcms.parsers;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;

/**
 * Pomoćne metode za rad s tekstovima.
 * 
 * @author marcupic
 *
 */
public class TextService {

	/**
	 * Metoda čita retke iz predanog readera. Svi prazni retci se izbacuju, kao i retci
	 * koji počinju znakom '#' (to se smatra komentarima). Metoda vraća listu svih preostalih
	 * redaka (dakle, nepraznih redaka koji nisu komentari). Po završetku reader se zatvara (vrijedi
	 * čak i za slučaj da se je pojavila pogreška). 
	 * @param reader reader iz kojeg se dohvaćaju podaci
	 * @return listu redaka
	 * @throws IOException u slučaju pogreške pri radu s readerom
	 */
	public static List<String> readerToStringList(Reader reader) throws IOException {
		List<String> resultList = new ArrayList<String>();
		try {
			BufferedReader br = new BufferedReader(reader);
			while(true) {
				String line = br.readLine();
				if(line==null) break;
				String t = line.trim();
				if(t.length()==0) continue;
				if(t.charAt(0)=='#') continue; // Komentar...
				resultList.add(line);
			}
		} finally {
			try { reader.close(); } catch(Exception ignorable) {}
		}
		return resultList; 
	}

	/**
	 * Metoda vraća retke iz predanog inputstream-a, pri čemu se okteti tumače kao znakovi
	 * u kodnoj stranici koju definira parametar charset. Interno, metoda na temelju navedenih
	 * podataka stvara Reader i poziva {@link #readerToStringList(Reader)} pa je za detalje
	 * potrebno pogledati tu metodu.
	 * @param is izvor okteta
	 * @param charset kodna stranica
	 * @return listu redaka
	 * @throws IOException u slučaju pogreške
	 */
	public static List<String> inputStreamToStringList(InputStream is, String charset) throws IOException {
		return readerToStringList(new InputStreamReader(is,charset));
	}

	/**
	 * Metoda vraća retke iz predanog inputstream-a, pri čemu se okteti tumače kao znakovi
	 * u kodnoj stranici "UTF-8". Interno, metoda na temelju navedenih
	 * podataka stvara Reader i poziva {@link #readerToStringList(Reader)} pa je za detalje
	 * potrebno pogledati tu metodu.
	 * @param is izvor okteta
	 * @return listu redaka
	 * @throws IOException u slučaju pogreške
	 */
	public static List<String> inputStreamToUTF8StringList(InputStream is) throws IOException {
		return inputStreamToStringList(is, "UTF-8");
	}
	
	/**
	 * Metoda dijeli predani tekst po zadanom separatoru i vraća elemente kao polje.
	 * Važna razlika između ove metode i metode {@link String#split(String)} je što
	 * {@link String#split(String)} spaja više uzastopnih separatora u jedan, što često
	 * nije poželjno ponašanje. Primjerice, rezultat:<br><br>
	 * <code>String[] res = "a#b##c".split("#");</code><br><br>
	 * je polje <br><br> <code>{"a","b","c"}</code>,<br><br> dok će <br><br> <code>String[] res = TextService.split("a#b##c",'#');</code><br><br>
	 * rezultirati s <br><br><code>{"a","b","","c"}</code>.<br><br> Osim navedenog, {@link String#split(String)}
	 * omogućava definiranje separatora kao regularnog izraza, pa dijeljenje može biti nešto sporije
	 * od ove metode.
	 * 
	 * @param text tekst koji treba podijeliti
	 * @param separator znak koji se koristi kao separator
	 * @return polje elemenata nastalih podijelom predanog niza zadanim separatorom
	 */
	public static String[] split(String text, char separator) {
		int n = 0;
		for(int i = 0; i < text.length(); i++) {
			if(text.charAt(i)==separator) n++;
		}
		String[] result = new String[n+1];
		int poc = 0;
		for(int i = 0; i < n; i++) {
			int end = text.indexOf(separator, poc);
			result[i] = text.substring(poc, end);
			poc = end+1;
		}
		result[n] = text.substring(poc);
		return result;
	}
	
	/**
	 * Cita ulazni stream, tumaci ga u zadanoj kodnoj stranici i vraca citav stream
	 * kao jedan string. Na kraju stream zatvara.
	 * 
	 * @param is
	 * @param charset
	 * @return
	 * @throws IOException
	 */
	public static String inputStreamToString(InputStream is, String charset) throws IOException {
		StringBuilder sb = new StringBuilder(10000);
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(is,charset));
			while(true) {
				String line = br.readLine();
				if(line==null) break;
				sb.append(line).append("\n");
			}
		} finally {
			try { is.close(); } catch(Exception ignorable) {}
		}
		return sb.toString(); 
	}
	
	/**
	 * Pretvara listu stringova u jedan string tako da svakom retku doda
	 * znak '\n' na kraj.
	 * 
	 * @param lines linije
	 * @return spojeni tekst
	 */
	public static String textLinesToString(List<String> lines) {
		int len = 0;
		for(int i = 0; i < lines.size(); i++) {
			len += lines.get(i).length() + 1;
		}
		StringBuilder sb = new StringBuilder(len);
		for(int i = 0; i < lines.size(); i++) {
			sb.append(lines.get(i)).append('\n');
		}
		return sb.toString();
	}
}
