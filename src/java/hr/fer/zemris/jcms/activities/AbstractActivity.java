package hr.fer.zemris.jcms.activities;

import java.io.Serializable;
import java.util.Date;

/**
 * Osnovni razred iz kojeg se izvode sve aktivnosti. Pažnja: ovo nije razred modela podataka
 * koji se premaju u bazu podataka! Ovo je razred koji se koristi za komunikaciju s objektima
 * tipa {@link IActivityReporter}.
 * 
 * @author marcupic
 *
 */
public class AbstractActivity implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	/** identifikator korisnika za kojega je ovo aktivnost **/
	private Long userID;
	/** datum aktivnosti **/
	private Date date;
	
	/**
	 * Konstruktor.
	 * 
	 * @param userID identifikator korisnika čija je ovo aktivnost.
	 * @param date datum aktivnosti
	 */
	public AbstractActivity(Long userID, Date date) {
		super();
		this.userID = userID;
		this.date = date;
	}

	/**
	 * Dohvat identifikatora korisnika.
	 * 
	 * @return identifikator korisnika
	 */
	public Long getUserID() {
		return userID;
	}
	
	/**
	 * Dohvat datuma aktivnosti.
	 * 
	 * @return datum aktivnosti
	 */
	public Date getDate() {
		return date;
	}
}
