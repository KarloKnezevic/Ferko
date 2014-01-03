package hr.fer.zemris.jcms.applications.model;

/**
 * Ovaj razred predstavlja odabir jedne opcije kod elemenata
 * koji nude odabir opcija. Pri tome {@link #key} predstavlja
 * ključ odabrane opcije. Varijabla {@link #text} će tipično biti
 * <code>null</code>, osim ako odabrana opcija nije opcija tipa
 * "other" - u tom slučaju će {@link #text} čuvati tekst koji je
 * korisnik unio dok je popunjavao obrazloženje.
 * 
 * @author marcupic
 */
public class ApplOptionSelection {

	private String key;
	private String text;
	
	public ApplOptionSelection() {
	}
	
	/**
	 * Ključ odabrane opcije.
	 * 
	 * @return ključ
	 */
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}

	/**
	 * Tekst koji je dodatno upisao kao obrazloženje; bit će različito
	 * od <code>null</code> samo za opcije tipa "other".
	 * 
	 * @return obrazloženje
	 */
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
}
