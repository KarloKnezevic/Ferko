package hr.fer.zemris.jcms.applications;

import hr.fer.zemris.jcms.applications.exceptions.ApplDefinitionException;
import hr.fer.zemris.jcms.applications.exceptions.ApplDuplicateNameException;
import hr.fer.zemris.jcms.applications.exceptions.ApplInvalidNameException;
import hr.fer.zemris.jcms.applications.exceptions.ApplMissingTextException;
import hr.fer.zemris.jcms.applications.model.ApplElement;
import hr.fer.zemris.jcms.applications.model.ApplMessage;
import hr.fer.zemris.jcms.applications.model.ApplNamedElement;
import hr.fer.zemris.jcms.applications.model.ApplSingleSelect;
import hr.fer.zemris.jcms.applications.model.ApplStudents;
import hr.fer.zemris.jcms.applications.model.ApplText;
import hr.fer.zemris.util.StringUtil;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class ApplContainer {

	/**
	 * Svi elementi koje sadrži ova prijava.
	 */
	private List<ApplElement> elements = new ArrayList<ApplElement>();
	/**
	 * Može li korisnik definirati elemente prijave? U smislu
	 * definiranja tekstova, poruka i sl.
	 */
	private boolean definable;
	/**
	 * Je li dozvoljeno izvođenje koda koji obavlja dohvat
	 * podataka studenta? U sekciji gdje se prijava definira
	 * ovo mora biti <code>false</code>, a kod filtriranja
	 * mora biti <code>true</code>. Komplement vrijedi za
	 * varijablu {@link #definable}.
	 */
	private boolean executable;
	/**
	 * Pomoćna mapa koja služi brzom dohvatu elemenata po imenu.
	 */
	private Map<String,ApplNamedElement> elementsByName = new HashMap<String, ApplNamedElement>();
	
	public ApplContainer() {
	}
	
	public boolean isDefinable() {
		return definable;
	}
	
	public boolean isExecutable() {
		return executable;
	}

	public List<ApplElement> getElements() {
		return elements;
	}
	
	public void setDefinable(boolean definable) {
		this.definable = definable;
	}
	
	public void setExecutable(boolean executable) {
		this.executable = executable;
	}

	public ApplMessage addMessage(String name, String text) throws ApplDefinitionException {
		if(!definable) throw new ApplDefinitionException("Promjena strukture prijave nije dozvoljena.");
		checkName(name);
		if(StringUtil.isStringBlank(text)) throw new ApplMissingTextException("", "Element tipa tekst nema zadan tekst (tekst ne može biti prazan).");
		ApplMessage elem = new ApplMessage(name, text);
		elements.add(elem);
		elementsByName.put(elem.getName(), elem);
		return elem;
	}
	
	public ApplText addText(String name, String text) throws ApplDefinitionException {
		if(!definable) throw new ApplDefinitionException("Promjena strukture prijave nije dozvoljena.");
		checkName(name);
		if(StringUtil.isStringBlank(text)) throw new ApplMissingTextException(name, "Tekst kod elementa \""+name+"\" nije zadan (tekst ne može biti prazan).");
		ApplText elem = new ApplText(name, text);
		elements.add(elem);
		elementsByName.put(elem.getName(), elem);
		return elem;
	}

	public ApplSingleSelect addSingleOption(String name, String text) throws ApplDefinitionException {
		if(!definable) throw new ApplDefinitionException("Promjena strukture prijave nije dozvoljena.");
		checkName(name);
		if(StringUtil.isStringBlank(text)) throw new ApplMissingTextException(name, "Tekst kod elementa \""+name+"\" nije zadan (tekst ne može biti prazan).");
		ApplSingleSelect elem = new ApplSingleSelect(name, text);
		elements.add(elem);
		elementsByName.put(elem.getName(), elem);
		return elem;
	}

	public ApplStudents addStudents(String name, int min, int max, String text) throws ApplDefinitionException {
		if(!definable) throw new ApplDefinitionException("Promjena strukture prijave nije dozvoljena.");
		checkName(name);
		if(StringUtil.isStringBlank(text)) throw new ApplMissingTextException(name, "Tekst kod elementa \""+name+"\" nije zadan (tekst ne može biti prazan).");
		if(min<0) {
			throw new ApplDefinitionException("Minimum kod elementa "+name+" ne može biti manji od 0.");
		}
		if(max<min) {
			throw new ApplDefinitionException("Maksimum kod elementa "+name+" ne može biti manji od minimuma.");
		}
		if(max==0) {
			throw new ApplDefinitionException("Element "+name+" je besmislen, obzirom da mu je maksimum postavljen na 0.");
		}
		ApplStudents elem = new ApplStudents(name, min, max, text);
		elements.add(elem);
		elementsByName.put(elem.getName(), elem);
		return elem;
	}
	
	public ApplNamedElement getElementByName(String name) {
		for(ApplElement e : elements) {
			if(!(e instanceof ApplNamedElement)) continue;
			ApplNamedElement nel = (ApplNamedElement)e;
			if(nel.getName().equals(name)) return nel;
		}
		return null;
	}
	
	private void checkName(String name) throws ApplDefinitionException {
		if(StringUtil.isStringBlank(name)) throw new ApplInvalidNameException("", "Ime je nedozvoljenog formata, ili nije zadano, ili je null.");
		for(ApplElement e : elements) {
			if(e instanceof ApplNamedElement) {
				ApplNamedElement ne = (ApplNamedElement)e;
				if(name.equals(ne.getName())) throw new ApplDuplicateNameException(name, "Element s imenom "+name+" je već definiran, pa ne može biti opet.");
			}
		}
	}
	
	protected ApplNamedElement findNamedElement(String name) {
		return elementsByName.get(name);
	}
	
	public void loadState(Properties stateProperties) {
		for(ApplElement e : elements) {
			e.loadState(stateProperties);
		}
	}
	
	public void storeState(Properties stateProperties) {
		for(ApplElement e : elements) {
			e.storeState(stateProperties);
		}
	}
	
	public void loadUserData(Properties properties) {
		for(ApplElement e : elements) {
			e.loadUsersData(properties);
		}
	}
	
	public void storeUserData(Properties properties) {
		for(ApplElement e : elements) {
			e.storeUsersData(properties);
		}
	}
	
	public boolean isStructurallyEquals(ApplContainer cont2) {
		if(elements.size() != cont2.elements.size()) return false;
		for(int i = 0; i < elements.size(); i++) {
			ApplElement elem1 = elements.get(i);
			ApplElement elem2 = cont2.elements.get(i);
			if(!elem1.isStateEqual(elem2)) return false;
		}
		return true;
	}
}
