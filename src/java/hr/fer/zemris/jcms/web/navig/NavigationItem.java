package hr.fer.zemris.jcms.web.navig;

import java.util.ArrayList;
import java.util.List;

/**
 * Jedan item u navigacijskoj traci.
 * 
 * @author marcupic
 *
 */
public class NavigationItem {

	private boolean titleIsKey = true;
	private String titleKey;
	private String kind;
	private List<NavigationItemParameter> parameters = new ArrayList<NavigationItemParameter>();

	/**
	 * @param kind Vrsta stavke. Koristi se kod renderiranja.
	 * @param titleKey Ključ koji predstavlja tekst koji će se ispisati kao naziv stavke.
	 */
	public NavigationItem(String kind, String titleKey) {
		this.kind = kind;
		this.titleKey = titleKey;
	}

	/**
	 * @param kind Vrsta stavke. Koristi se kod renderiranja.
	 * @param titleKey Ključ koji predstavlja tekst koji će se ispisati kao naziv stavke.
	 * @param titleIsKey <code>true</code> ako <code>titleKey</code> treba tumačiti kao ključ, false ako je to konačni tekst.
	 */
	public NavigationItem(String kind, String titleKey, boolean titleIsKey) {
		this.kind = kind;
		this.titleKey = titleKey;
		this.titleIsKey = titleIsKey;
	}

	/**
	 * Vraća <code>true</code> ako se naziv još treba internacionalizirati (jer se tunači kao ključ); inače vraća <code>false</code>.
	 * @return je li naslov ključ
	 */
	public boolean getTitleIsKey() {
		return titleIsKey;
	}
	
	/**
	 * Vraća ključ po kojem se dohvaća tekst koji će predstavljati naslov stavke.
	 * @return ključ stavke
	 */
	public String getTitleKey() {
		return titleKey;
	}
	
	/**
	 * Vraća vrstu stavke (je li to direktno URL, ili je možda akcija, ili ...).
	 * @return vrstu stavke
	 */
	public String getKind() {
		return kind;
	}
	
	/**
	 * Lista svih parametara koje je dodatno potrebno predati.
	 * @return lista parametara
	 */
	public List<NavigationItemParameter> getParameters() {
		return parameters;
	}
	
	/**
	 * Pomoćna metoda za dodavanje parametra.
	 * @param name naziv parametra
	 * @param value vrijednost parametra
	 */
	public NavigationItem addParameter(String name, Object value) {
		parameters.add(new NavigationItemParameter(name, value));
		return this;
	}
	
}
