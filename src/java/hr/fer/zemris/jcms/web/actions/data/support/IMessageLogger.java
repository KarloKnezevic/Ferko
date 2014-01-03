package hr.fer.zemris.jcms.web.actions.data.support;


import java.util.Date;
import java.util.List;

public interface IMessageLogger {
	/**
	 * Vraća objekt koji čuva sve poruke.
	 * @return objekt s porukama
	 */
	public IMessageContainer getMessageContainer();
	/**
	 * Vraća listu svih poruka. 
	 * @return lista poruka
	 */
	public List<LoggerMessage> getMessages();
	/**
	 * Čisti sve poruke iz liste.
	 */
	public void clearMessages();
	/**
	 * Dodaje poruku o pogrešci vezanu uz polje formulara.
	 * @param fieldName naziv polja formulara
	 * @param messageText tekst poruke
	 */
	public void addFieldErrorMessage(String fieldName, String messageText);
	/**
	 * Dodaje poruku o pogrešci.
	 * @param messageText tekst poruke
	 */
	public void addErrorMessage(String messageText);
	/**
	 * Dodaje poruku upozorenja.
	 * @param messageText tekst poruke
	 */
	public void addWarningMessage(String messageText);
	/**
	 * Dodaje informativnu poruku.
	 * @param messageText tekst poruke
	 */
	public void addInfoMessage(String messageText);
	/**
	 * Dodaje poruku.
	 * @param messageType vrsta poruke
	 * @param messageText tekst poruke
	 */
	public void addMessage(LoggerMessageType messageType, String messageText);
	/**
	 * Vraća lokalizirani tekst koji odgovara zadanom ključu.
	 * @param key ključ
	 * @return lokalizirani tekst povezan uz ključ
	 */
	public String getText(String key);
	/**
	 * Vraća lokalizirani tekst koji odgovara zadanom ključu, ili ako ključ ne postoji,
	 * tada defaultnu vrijednost.
	 * @param key ključ
	 * @param defaultValue defaultna vrijednost
	 * @return lokalizirani tekst povezan uz ključ
	 */
	public String getText(String key, String defaultValue);
	/**
	 * Vraća lokalizirani tekst koji odgovara zadanom ključu i koji u sebi sadrži
	 * parametre koji se popunjavaju na temelju predanog polja.
	 * @param key ključ
	 * @param args argumenti za popunjavanje parametara
	 * @return lokalizirani tekst povezan uz ključ
	 */
	public String getText(String key, String[] args);
	/**
	 * Ispituje ima li poruka.
	 * @return true ako ima poruka, false inače
	 */
	public boolean hasMessages();
	/**
	 * Ispituje ima li poruka pogrešaka.
	 * @return true ako ima poruka, false inače
	 */
	public boolean hasErrorMessages();
	/**
	 * Ispituje ima li informativnih poruka.
	 * @return true ako ima poruka, false inače
	 */
	public boolean hasInfoMessages();
	/**
	 * Ispituje ima li poruka upozorenja.
	 * @return true ako ima poruka, false inače
	 */
	public boolean hasWarningMessages();
	/**
	 * Registrira trenutne poruke kao zakašnjele, čime će ponovno biti
	 * dostupne nekoj drugoj akciji nakon redirecta. Da bi ovo radilo
	 * ispravno, redirect mora biti izveden tako da akciji preda
	 * parametar {@code dmsgid}. Evo primjera:
	 * <pre>
	 * &lt;result name="dalje" type="redirect-action"&gt;
	 *   &lt;param name="actionName"&gt;AdminAssessmentList&lt;/param&gt;
	 *   &lt;param name="parse"&gt;true&lt;/param&gt;
	 *   &lt;param name="dmsgid"&gt;${dmsgid}&lt;/param&gt;
	 * &lt;/result&gt;
	 * </pre>
	 */
	public void registerAsDelayed();
	/**
	 * Pomoćna metoda koja obavlja formatiranje datuma po formatu "yyyy-MM-dd".
	 * 
	 * @param date datum koji treba formatirati
	 * @return formatirani datum
	 */
	public String formatDate(Date date);
	/**
	 * Pomoćna metoda koja obavlja formatiranje datuma i vremena po formatu "yyyy-MM-dd HH:mm:ss".
	 * 
	 * @param date datum koji treba formatirati
	 * @return formatirani datum i vrijeme
	 */
	public String formatDateTime(Date date);
	
}
