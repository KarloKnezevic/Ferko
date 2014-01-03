package hr.fer.zemris.jcms.applications.model;

import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

import java.util.Properties;

/**
 * Kod programski definirane prijave, ovo je temeljni razred
 * svih interaktivnih i neinteraktivnih elemenata.
 *  
 * @author marcupic
 */
public abstract class ApplElement {
	
	/**
	 * Vrsta elementa.
	 */
	private int kind;
	/**
	 * Je li element omogućen ili ne.
	 */
	private boolean enabled = true;

	private Object userData;
	private Object renderingData;
	
	public ApplElement(int kind) {
		super();
		this.kind = kind;
	}
	
	public int getKind() {
		return kind;
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	
	/**
	 * U predani objekt pohranjuje što je omogućeno a što ne,
	 * kako bi se kasnije temeljem toga moglo restaurirati
	 * stanje koje se renderira asistentu/samom studentu nakon
	 * što se prijava ispuni.
	 * 
	 * @param prop objekt za pohranu stanja
	 */
	public abstract void storeState(Properties prop);
	/**
	 * Iz predanog objekta čita što je bilo omogućeno a što ne,
	 * i tako restaurira stanje ovog elementa.
	 * 
	 * @param prop objekt sa pohranjenim stanjem
	 */
	public abstract void loadState(Properties prop);
	/**
	 * U predani objekt pohranjuje što je unio korisnik.
	 * 
	 * @param prop objekt za pohranu
	 */
	public abstract void storeUsersData(Properties prop);
	/**
	 * Iz predanog objekta čita što je unio korisnik i 
	 * rekonstruira stanje ovog objekta.
	 * 
	 * @param prop objekt sa pohranjenim unosom
	 */
	public abstract void loadUsersData(Properties prop);
	
	public Object getUserData() {
		return userData;
	}
	
	public void setUserData(Object userData) {
		this.userData = userData;
	}
	
	/**
	 * Provjerava jesu li stanja ovog i drugog elementa ista (uključivo "enabled").
	 * @param other drugi element
	 * @return <code>true</code> ako jesu, <code>false</code> inače
	 */
	public abstract boolean isStateEqual(ApplElement other);
	
	public Object getRenderingData() {
		return renderingData;
	}
	public void setRenderingData(Object renderingData) {
		this.renderingData = renderingData;
	}
	
	/**
	 * Metoda koja obavlja validaciju podataka. Poruke može dodati
	 * u messageLogger. Vraća <code>true</code> ako je sve u redu
	 * i ako se prijava može prihvatiti, odnosno <code>false</code>
	 * ako su podatci neispravni i ako ih treba vratiti na doradu.
	 * 
	 * @param messageLogger logger
	 * @return <code>true</code> za OK, <code>false</code> inače
	 */
	public boolean validate(IMessageLogger messageLogger) {
		return true;
	}
	
	public boolean isUserInput() {
		return false;
	}
}
