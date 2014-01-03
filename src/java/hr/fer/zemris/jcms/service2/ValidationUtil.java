package hr.fer.zemris.jcms.service2;

import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.util.StringUtil;

public class ValidationUtil {

	public enum NumberCheck {
		NONE,
		MIN,
		MAX,
		MINMAX
	}
	
	/**
	 * Pokušava napraviti konverziju stringa u integer.

	 * @param value tekst
	 * @return broj
	 * @throws ValidationUtilException u slučaju pogreške pri konverziji
	 */
	public static int convertToInteger(String value) throws ValidationUtilException {
		try {
			return Integer.parseInt(value);
		} catch(Exception ex) {
			throw new ValidationUtilException(ex.getMessage());
		}
	}

	/**
	 * Pokušava napraviti konverziju stringa u integer. Ako je predan <code>null</code> ili prazan
	 * string, vraća nula. Tek ako je stvarno nešto predano, pokušava obaviti konverziju.
	 * 
	 * @param value tekst
	 * @return broj
	 * @throws ValidationUtilException u slučaju pogreške pri konverziji
	 */
	public static int tryConvertToInteger(String value) throws ValidationUtilException {
		if(StringUtil.isStringBlank(value)) return 0;
		try {
			return Integer.parseInt(value);
		} catch(Exception ex) {
			throw new ValidationUtilException(ex.getMessage(), ex);
		}
	}

	/**
	 * Pokušava napraviti konverziju stringa u integer. Ako je predan <code>null</code> ili prazan
	 * string, vraća nula. Tek ako je stvarno nešto predano, pokušava obaviti konverziju.
	 * 
	 * @param value tekst
	 * @param data podatkovni objekt za upisivanje pogreške i internacionalizaciju
	 * @param fieldKey ključ po kojem će se dohvatiti naziv broja koji se pretvara
	 * @param min mimimum koji broj smije poprimiti
	 * @param max maksimum koji broj smije poprimiti
	 * @param checks treba li provjeriti minimum, maksimum, oba ili niti jedan? Može biti <code>null</code>, i tada se tumaći kao {@link NumberCheck#NONE}
	 * @return broj
	 * @throws ValidationUtilException ako dođe do greške u pretvorbi ili provjeri
	 */
	public static int tryConvertToInteger(String value, AbstractActionData data, String fieldKey, int min, int max, NumberCheck checks) throws ValidationUtilException {
		int res = 0;
		if(StringUtil.isStringBlank(value)) return res;
		try {
			res = Integer.parseInt(value);
		} catch(Exception ex) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters")
					+ ": " + data.getMessageLogger().getText(fieldKey)
					+ ": " + value + ".");
			data.setResult(AbstractActionData.RESULT_INPUT);
			throw new ValidationUtilException(ex.getMessage(), ex);
		}
		if(checks==NumberCheck.MIN || checks==NumberCheck.MINMAX) {
			if(res < min) {
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText(fieldKey) + " ("+value+")"
						+ ": " + data.getMessageLogger().getText("Error.canNotBeSmallerThan") + min + ".");
				data.setResult(AbstractActionData.RESULT_INPUT);
				throw new ValidationUtilException("Broj "+fieldKey+" je manji od dozvoljenog ("+res+"<"+min+").");
			}
		}
		if(checks==NumberCheck.MAX || checks==NumberCheck.MINMAX) {
			if(res > max) {
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText(fieldKey) + " ("+value+")"
						+ ": " + data.getMessageLogger().getText("Error.canNotBeBiggerThan") + min + ".");
				data.setResult(AbstractActionData.RESULT_INPUT);
				throw new ValidationUtilException("Broj "+fieldKey+" je veći od dozvoljenog ("+res+">"+max+").");
			}
		}
		return res;
	}

	/**
	 * Pretvara tekst u decimalni broj. Ako se dogodi greška, postavlja odgovarajuće podatke u podatkovni objekt.
	 * Prazan tekst pretvara se u 0.
	 * @param value tekst
	 * @param data podatkovni objekt
	 * @param fieldKey ključ po kojem će se dohvatiti naziv broja koji se pretvara
	 * @return broj
	 * @throws ValidationUtilException ako dođe do greške u pretvorbi ili provjeri
	 */
	public static double tryConvertToDouble(String value, AbstractActionData data, String fieldKey) throws ValidationUtilException {
		return ValidationUtil.tryConvertToDouble(value, data, fieldKey, 0, 0, ValidationUtil.NumberCheck.NONE, 0);
	}
	
	/**
	 * Pretvara tekst u decimalni broj, uz eventualnu provjeru minimuma i maksimuma. Ako se dogodi greška, 
	 * postavlja odgovarajuće podatke u podatkovni objekt. Prazan tekst pretvara se u 0.
	 * @param value tekst
	 * @param data podatkovni objekt
	 * @param fieldKey ključ po kojem će se dohvatiti naziv broja koji se pretvara
	 * @param min mimimum koji broj smije poprimiti
	 * @param max maksimum koji broj smije poprimiti
	 * @param checks treba li provjeriti minimum, maksimum, oba ili niti jedan? Može biti <code>null</code>, i tada se tumaći kao {@link NumberCheck#NONE}
	 * @param tolerance tolerancijs prilikom usporedbe
	 * @return broj
	 * @throws ValidationUtilException ako dođe do greške u pretvorbi ili provjeri
	 */
	public static double tryConvertToDouble(String value, AbstractActionData data, String fieldKey, double min, double max, NumberCheck checks, double tolerance) throws ValidationUtilException {
		double res = 0.0;
		if(StringUtil.isStringBlank(value)) return res;
		try {
			res = StringUtil.stringToDouble(value);
		} catch(Exception ex) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters")
					+ ": " + data.getMessageLogger().getText(fieldKey)
					+ ": " + value + ".");
			data.setResult(AbstractActionData.RESULT_INPUT);
			throw new ValidationUtilException(ex.getMessage(), ex);
		}
		if(checks==NumberCheck.MIN || checks==NumberCheck.MINMAX) {
			if(res < min-tolerance) {
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText(fieldKey) + " ("+value+")"
						+ ": " + data.getMessageLogger().getText("Error.canNotBeSmallerThan") + min + ".");
				data.setResult(AbstractActionData.RESULT_INPUT);
				throw new ValidationUtilException("Broj "+fieldKey+" je manji od dozvoljenog ("+res+"<"+min+").");
			}
		}
		if(checks==NumberCheck.MAX || checks==NumberCheck.MINMAX) {
			if(res > max+tolerance) {
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText(fieldKey) + " ("+value+")"
						+ ": " + data.getMessageLogger().getText("Error.canNotBeBiggerThan") + min + ".");
				data.setResult(AbstractActionData.RESULT_INPUT);
				throw new ValidationUtilException("Broj "+fieldKey+" je veći od dozvoljenog ("+res+">"+max+").");
			}
		}
		return res;
	}

}
