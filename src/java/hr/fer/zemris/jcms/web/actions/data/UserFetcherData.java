package hr.fer.zemris.jcms.web.actions.data;

import java.util.List;

import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

public class UserFetcherData extends AbstractActionData {

	/**
	 * Lista dohvaćenih korisnika
	 */
	private List<User> users;
	/**
	 * U kojem kontekstu se korisnici traže. Za sada može biti:
	 * <ul>
	 * <li><b><code>ci</code></b> - što znači course instance, i tada se gleda
	 *     dodatno polje courseInstanceID da se vidi o kojem se
	 *     kolegiju radi</li>
	 * </ul> 
	 */
	private String context;
	/**
	 * Identifikator kolegija, ako se traže studenti
	 * na kolegiju.
	 */
	private String courseInstanceID;
	/**
	 * Kriterij po kojem se radi dohvat. Tipično je oblika "Prezime, Ime".
	 */
	private String criteria;
	
	public UserFetcherData(IMessageLogger messageLogger) {
		super(messageLogger);
	}

	public List<User> getUsers() {
		return users;
	}
	public void setUsers(List<User> users) {
		this.users = users;
	}
	
	public String getContext() {
		return context;
	}
	public void setContext(String context) {
		this.context = context;
	}
	
	public String getCourseInstanceID() {
		return courseInstanceID;
	}
	public void setCourseInstanceID(String courseInstanceID) {
		this.courseInstanceID = courseInstanceID;
	}
	
	public String getCriteria() {
		return criteria;
	}
	public void setCriteria(String criteria) {
		this.criteria = criteria;
	}
}
