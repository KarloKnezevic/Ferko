package hr.fer.zemris.util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class DateUtil {

	public static final String shortDateFormat = "yyyy-MM-dd";
	public static final String dateTimeFormat = "yyyy-MM-dd HH:mm:ss";
	
	/**
	 * Pretvara datum u tekst, i to samo dan (format "yyyy-MM-dd").
	 * 
	 * @param date datum
	 * @return tekst koji predstavlja taj datum
	 */
	public static final String dateToString(Date date) {
		if(date==null) return "";
		StringBuilder sb = new StringBuilder(10);
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		
		int day = cal.get(Calendar.DAY_OF_MONTH);
		int month = cal.get(Calendar.MONTH)-Calendar.JANUARY+1;
		int year = cal.get(Calendar.YEAR);
		
		sb.append(year).append('-');
		if(month<10) sb.append('0');
		sb.append(month).append('-');
		if(day<10) sb.append('0');
		sb.append(day);
		return sb.toString();
	}
	
	/**
	 * Pretvara datum i vrijeme u tekst (format "yyyy-MM-dd HH:mm:ss").
	 * 
	 * @param date datum
	 * @return tekst koji predstavlja taj datum
	 */
	public static final String dateTimeToString(Date date) {
		if(date==null) return "";
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		return sdf.format(date);
	}

	/**
	 * Pretvara tekst u datum (samo dan). Format mora biti "yyyy-MM-dd".
	 * Dobiveni datum predstavlja ponoc.
	 *  
	 * @param date tekst
	 * @return odgovarajuci datum
	 */
	public static final Date stringToDate(String date) {
		String[] x = date.split("-");
		int[] y = new int[3];
		for(int i = 0; i < 3; i++) {
			y[i] = Integer.parseInt(x[i]);
		}
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, y[0]);
		cal.set(Calendar.MONTH, y[1]-1+Calendar.JANUARY);
		cal.set(Calendar.DAY_OF_MONTH, y[2]);
		cal.set(Calendar.HOUR, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}
	
	/**
	 * Pretvara tekst u datum. Format mora biti "yyyy-MM-dd HH:mm:ss".
	 *  
	 * @param date tekst
	 * @return odgovarajuci datum i vrijeme
	 */
	public static final Date stringToDateTime(String dateTime) {
		if(StringUtil.isStringBlank(dateTime)) return null;
		if(!checkFullDateFormat(dateTime)) return null;
		String[] x = dateTime.split("[-: ]");
		int[] y = new int[6];
		for(int i = 0; i < 6; i++) {
			y[i] = Integer.parseInt(x[i]);
		}
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, y[0]);
		cal.set(Calendar.MONTH, y[1]-1+Calendar.JANUARY);
		cal.set(Calendar.DAY_OF_MONTH, y[2]);
		cal.set(Calendar.HOUR_OF_DAY, y[3]);
		cal.set(Calendar.MINUTE, y[4]);
		cal.set(Calendar.SECOND, y[5]);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}
	
	/**
	 * Pomoćno polje koje se koristi u funkciji {@linkplain #checkFullDateFormat(String)}
	 * a sadrži pozicije na kojima se očekuje znamenka.
	 */
	private static int[] numberPositions = {0,1,2,3,5,6,8,9,11,12,14,15,17,18};
	/**
	 * Pomoćno polje koje se koristi u funkciji {@linkplain #checkSemiFullDateFormat(String)}
	 * a sadrži pozicije na kojima se očekuje znamenka.
	 */
	private static int[] numberPositions3 = {0,1,2,3,5,6,8,9,11,12,14,15};
	
	/**
	 * Provjerava je li tekst datum formata "yyyy-MM-dd HH:mm:ss".
	 * 
	 * @param text tekst koji treba provjeriti
	 * @return true ako je, false inace
	 */
	public static final boolean checkFullDateFormat(String text) {
		if(text==null || text.length()!=19) return false;
		for(int i = 0; i < numberPositions.length; i++) {
			if(!Character.isDigit(text.charAt(numberPositions[i]))) return false;
		}
		if(text.charAt(4)!='-') return false;
		if(text.charAt(7)!='-') return false;
		if(text.charAt(10)!=' ') return false;
		if(text.charAt(13)!=':') return false;
		if(text.charAt(16)!=':') return false;
		return true;
	}

	/**
	 * Provjerava je li tekst datum formata "yyyy-MM-dd HH:mm".
	 * 
	 * @param text tekst koji treba provjeriti
	 * @return true ako je, false inace
	 */
	public static final boolean checkSemiFullDateFormat(String text) {
		if(text==null || text.length()!=16) return false;
		for(int i = 0; i < numberPositions3.length; i++) {
			if(!Character.isDigit(text.charAt(numberPositions3[i]))) return false;
		}
		if(text.charAt(4)!='-') return false;
		if(text.charAt(7)!='-') return false;
		if(text.charAt(10)!=' ') return false;
		if(text.charAt(13)!=':') return false;
		return true;
	}

	/**
	 * Pomoćno polje koje se koristi u funkciji {@linkplain #checkDateFormat(String)}
	 * a sadrži pozicije na kojima se očekuje znamenka.
	 */
	private static int[] numberPositions2 = {0,1,2,3,5,6,8,9};
	
	/**
	 * Provjerava je li tekst datum formata "yyyy-MM-dd".
	 * 
	 * @param text tekst koji treba provjeriti
	 * @return true ako je, false inace
	 */
	public static final boolean checkDateFormat(String text) {
		if(text==null || text.length()!=10) return false;
		for(int i = 0; i < numberPositions2.length; i++) {
			if(!Character.isDigit(text.charAt(numberPositions2[i]))) return false;
		}
		if(text.charAt(4)!='-') return false;
		if(text.charAt(7)!='-') return false;
		return true;
	}
	
	/**
	 * Generira sve datume u rasponu od početnog do konačnog (oba uključivo). Važno: period je ograničen na 10 godina.
	 * @param startDate početni datum
	 * @param endDate konačni datum
	 * @param excludeWeekends ako je <code>true</code>, subota i nedjelja se neće vraćati; u suprotnom hoće.
	 * @return polje s kronološki složenim datumima
	 * @throws RuntimeException ako je period dulji od 10 godina
	 */
	public static String[] generateDateRange(String startDate, String endDate, boolean excludeWeekends) {
		String[] x = startDate.split("-");
		int[] y = new int[3];
		for(int i = 0; i < 3; i++) {
			y[i] = Integer.parseInt(x[i]);
		}
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, y[0]);
		cal.set(Calendar.MONTH, y[1]-1+Calendar.JANUARY);
		cal.set(Calendar.DAY_OF_MONTH, y[2]);
		cal.set(Calendar.HOUR_OF_DAY, 8);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
		List<String> allDates = new ArrayList<String>();
		int safetyCounter = 0;
		while(true) {
			String current = sdf.format(cal.getTime());
			if(current.compareTo(endDate)>0) break;
			if(!excludeWeekends) {
				allDates.add(current);
			} else {
				int dan = cal.get(Calendar.DAY_OF_WEEK);
				if(dan != Calendar.SUNDAY && dan != Calendar.SATURDAY) {
					allDates.add(current);
				}
			}
			cal.add(Calendar.HOUR_OF_DAY, 24);
			safetyCounter++;
			if(safetyCounter>3650) {
				throw new RuntimeException("Date range generation aborted. Period is larger than 10 years? Possible endless loop detected?");
			}
		}
		String[] res = new String[allDates.size()];
		allDates.toArray(res);
		return res;
	}

	/**
	 * String oblika HH:mm pretvara u apsolutni broj minuta proteklih od ponoći.
	 * 
	 * @param time trenutak (format je HH:mm)
	 * @return broj minuta od ponoći
	 */
	public static int shortTimeToMinutes(String time) {
		return Integer.parseInt(time.substring(0,2))*60+Integer.parseInt(time.substring(3,5));
	}

	/**
	 * Pretvara broj minuta proteklih od ponoći u kratku vremensku oznaku, formata HH:mm.
	 * 
	 * @param minutes minute protekle od ponoći
	 * @return kratka vremenska oznaka
	 */
	public static String minutesToShortTime(int minutes) {
		StringBuilder sb = new StringBuilder(5);
		int h = minutes / 60;
		int m = minutes % 60;
		if(h<10) sb.append('0');
		sb.append(h);
		sb.append(':');
		if(m<10) sb.append('0');
		sb.append(m);
		return sb.toString();
	}
}
