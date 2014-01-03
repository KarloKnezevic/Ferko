package hr.fer.zemris.jcms.web.actions.data.support;

import java.util.List;
import java.util.Map;

public interface IMessageContainer {
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
	 * Ispituje ima li poruka pogrešaka vezanih uz polja formulara.
	 * @return true ako ima poruka, false inače
	 */
	public boolean hasFieldErrorMessages();
	/**
	 * Ispituje ima li poruka pogrešaka koje su globalne (nisu vezane uz polje formulara).
	 * @return true ako ima poruka, false inače
	 */
	public boolean hasNonFieldErrorMessages();
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
	 * U trenutni popis poruka dodaje sve poruke iz predanog izvora.
	 * 
	 * @param container izvor poruka
	 */
	public void addAll(IMessageContainer container);
	/**
	 * Registrira privatnu poruku za samu aplikaciju.
	 * 
	 * @param key ključ
	 * @param value vrijednost
	 */
	public void addPrivateData(String key, String value);
	/**
	 * Dohvaća privatnu poruku zadanog imena, ako takva postoji.
	 * 
	 * @param key ključ
	 * @return poruka ili null ako ista ne postoji
	 */
	public String getPrivateMessage(String key);
	/**
	 * Ima li privatnih poruka?
	 * @return <code>true</code> ako ima, <code>false</code> inače
	 */
	public boolean hasPrivateMessages();
	/**
	 * Dohvaća mapu privatnih poruka. Moguće je da rezultat bude <code>null</code>
	 * ako ništa nije dodavano u privatne poruke.
	 * 
	 * @return mapu privatnih poruka
	 */
	public Map<String,Object> getPrivateMessages();
	
}
