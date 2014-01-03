package hr.fer.zemris.jcms.parsers;

import hr.fer.zemris.jcms.beans.ext.UserRoomBean;

import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class UserRoomParser {
	/**
	 * Parsira predani reader u kojem svaki redak ima sljedeće elemente odvojene znakom # ili znakom tab:
	 * <table border="1">
	 * <tr><th>jmbag</th><td>jmbag korisnika</td></tr>
	 * <tr><th>shortRoomName</th><td>kratko ime dvorane</td></tr>
	 * </table>
	 * 
	 * @param is reader
	 * @return listu beanova koji opisuju smjestanje korisnika u neku sobu
	 * @throws IOException u slučaju pogreške
	 * @throws ParseException u slucaju pogreske prilikom parsiranja
	 */
	public static List<UserRoomBean>parseTabbedFormat(Reader is) throws IOException, ParseException {
		
		List<String> lines = TextService.readerToStringList(is);
		List<UserRoomBean> resultList = new ArrayList<UserRoomBean>(lines.size());
		if(lines.isEmpty()) return resultList;
		int pos = lines.get(0).indexOf('\t');
		char separator = pos != -1 ? '\t' : '#';
		for (String line : lines) {
			String[] elements = TextService.split(line, separator);
			if(elements.length!=2) {
				throw new ParseException("Found unexpected row length: "+line,0);
			}
			UserRoomBean urb = new UserRoomBean();
			
			urb.setJmbag(elements[0]);
			urb.setShortRoomName(elements[1]);
			
			resultList.add(urb);
		}
		
		return resultList;
	}
	
	/**
	 * Parsira predani reader u kojem svaki redak ima sljedeće elemente odvojene znakom # ili znakom tab:
	 * <table border="1">
	 * <tr><th>IDProvjere</th><td>id provjere</td></tr>
	 * <tr><th>SifDvorane</th><td>kratko ime dvorane</td></tr>
	 * <tr><th>PrezimeIme</th><td>prezime, ime (jmbag)</td></tr>
	 * <tr><th>JMBAG</th><td>jmbag korisnika</td></tr>
	 * <tr><th>Rbr</th><td>redni broj korisnika u dvorani</td></tr>
	 * </table>
	 * 
	 * @param is reader
	 * @return listu beanova koji opisuju smjestanje korisnika u neku sobu
	 * @throws IOException u slučaju pogreške
	 * @throws ParseException u slucaju pogreske prilikom parsiranja
	 */
	public static List<UserRoomBean>parseMailMerge(Reader is) throws IOException, ParseException {
		
		List<String> lines = TextService.readerToStringList(is);
		List<UserRoomBean> resultList = new ArrayList<UserRoomBean>(lines.size());
		if(lines.isEmpty()) return resultList;
		int pos = lines.get(0).indexOf('\t');
		char separator = pos != -1 ? '\t' : '#';
		//izbacujemo prvi red
		lines.remove(0);
		
		for (String line : lines) {
			String[] elements = TextService.split(line, separator);
			if(elements.length!=5) {
				throw new ParseException("Found unexpected row length: "+line,0);
			}
			UserRoomBean urb = new UserRoomBean();
			
			String tmpJmbag = elements[3];
			if (tmpJmbag.indexOf('*')!=-1)
				tmpJmbag = tmpJmbag.substring(tmpJmbag.indexOf('*')+1, tmpJmbag.lastIndexOf('*'));
			
			int x = -1;
			try { x = Integer.valueOf(elements[4]); }
			catch (Exception ignorable) {}
			if (x==-1)
				throw new ParseException("Found wrong position in row: "+line,0);
			
			urb.setJmbag(tmpJmbag);
			urb.setShortRoomName(elements[1]);
			urb.setPosition(x);
			
			
			resultList.add(urb);
		}
		
		return resultList;
	}
}
