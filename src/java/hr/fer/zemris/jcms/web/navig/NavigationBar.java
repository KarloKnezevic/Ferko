package hr.fer.zemris.jcms.web.navig;

import java.util.ArrayList;
import java.util.List;

/**
 * Jedna navigacijska traka.
 * 
 * @author marcupic
 */
public class NavigationBar {

	private String name;
	private List<NavigationItem> items = new ArrayList<NavigationItem>();

	/**
	 * Konstruktor.
	 * @param name naziv navigacijske trake
	 */
	public NavigationBar(String name) {
		this.name = name;
	}
	
	/**
	 * Naziv navigacijske trake.
	 * @return naziv
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Ima li traka stavki?
	 * @return true ako traka ima stavki, false inače
	 */
	public boolean isEmpty() {
		return items.isEmpty();
	}
	
	/**
	 * Dohvaća sve navigacijske stavke trake.
	 * @return listu stavki; nikada neće biti null.
	 */
	public List<NavigationItem> getItems() {
		return items;
	}
	
	/**
	 * Dodavanje nove navigacijske stavke u traku na kraj.
	 * @param item stavka koju treba dodati
	 */
	public NavigationBar addItem(NavigationItem item) {
		items.add(item);
		return this;
	}
}
