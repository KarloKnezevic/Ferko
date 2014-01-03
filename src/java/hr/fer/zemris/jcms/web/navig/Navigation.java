package hr.fer.zemris.jcms.web.navig;

import hr.fer.zemris.util.StringUtil;

import java.util.HashMap;
import java.util.Map;

/**
 * Navigacija stranice. Ovaj objekt predstavlja kompletnu
 * navigacijsku strukturu stranice. Tri su preddefinirana
 * imena koja imaju posebna značenja u Ferku:
 * <ol>
 *  <li>m0 - glavna traka koja se renderira na samom vrhu stranice</li>
 *  <li>m1 - pomoćna traka koja se renderira tik ispod glavne 
 *  trake; tipično glavni izbornik kolegija.</li>
 *  <li>m2 - dodatna pomoćna traka koja se renderira ispod pomoćne trake;
 *  tipično za neku vrstu lokalne navigacije unutar nekog podmodula.</li>
 * 
 * @author marcupic
 */
public class Navigation {

	private String pageTitle;
	private String pageDescription;
	private Map<String, NavigationBar> navigationBars = new HashMap<String, NavigationBar>();
	
	/**
	 * Metoda dohvaća navigacijsku traku predanog imena. Ukoliko
	 * takva ne postoji, automatski će se stvoriti prazna.
	 * 
	 * @param name naziv trake
	 * @return navigacijska traka
	 */
	public NavigationBar getNavigationBar(String name) {
		NavigationBar navBar = navigationBars.get(name);
		if(navBar==null) {
			navBar = new NavigationBar(name);
			navigationBars.put(name, navBar);
		}
		return navBar;
	}

	/**
	 * Metoda predlaže da se kao naslov stranice postavi predani tekst.
	 * Međutim, ukoliko je naziv već definiran, ostat će stari (naslov se
	 * neće ažurirati).
	 * 
	 * @param pageTitle predloženi naslov stranice
	 */
	public void suggestPageTitle(String pageTitle) {
		if(StringUtil.isStringBlank(this.pageTitle)) {
			this.pageTitle = pageTitle;
		}
	}

	/**
	 * Metoda postavlja naslov stranice. Ako je naslov već prethodno definiran,
	 * bit će pregažen. Ukoliko to nije željeno ponašanje, pogledajte metodu
	 * {@link #suggestPageTitle(String)}.
	 * 
	 * @param pageTitle naslov stranice
	 */
	public void setPageTitle(String pageTitle) {
		this.pageTitle = pageTitle;
	}
	
	/**
	 * Metoda postavlja opis stranice. Ukoliko je opis već prethodno postavljen,
	 * bit će pregažen. Ukoliko to nije željeno ponašanje, pogledajte metodu
	 * {@link #suggestPageDescription(String)}
	 * 
	 * @param pageDescription opis stranice
	 */
	public void setPageDescription(String pageDescription) {
		this.pageDescription = pageDescription;
	}

	/**
	 * Metoda predlaže da se kao opis stranice postavi predani tekst. Ukoliko je 
	 * opis već prethodno postavljen, neće se pregaziti.
	 * 
	 * @param pageDescription opis stranice
	 */
	public void suggestPageDescription(String pageDescription) {
		if(StringUtil.isStringBlank(this.pageDescription)) {
			this.pageDescription = pageDescription;
		}
	}

	/**
	 * Metoda dohvaća naslov stranice.
	 * 
	 * @return naslov stranice
	 */
	public String getPageTitle() {
		return pageTitle;
	}
	
	/**
	 * Metoda dohvaća opis stranice.
	 * 
	 * @return opis stranice
	 */
	public String getPageDescription() {
		return pageDescription;
	}
	
}
