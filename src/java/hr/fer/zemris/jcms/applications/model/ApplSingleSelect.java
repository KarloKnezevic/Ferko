package hr.fer.zemris.jcms.applications.model;

import hr.fer.zemris.jcms.applications.exceptions.ApplDefinitionException;
import hr.fer.zemris.jcms.applications.exceptions.ApplDuplicateNameException;
import hr.fer.zemris.jcms.applications.exceptions.ApplInvalidNameException;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;
import hr.fer.zemris.util.StringUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

/**
 * Kod programski definirane prijave, ovo je interaktivni element 
 * koji omogućava da korisnik odabere jednu od ponuđenih opcija.
 * 
 * @author marcupic
 */
public class ApplSingleSelect extends ApplNamedElement implements ApplOptionContainer {
	
	private String text;
	private List<ApplOption> options = new ArrayList<ApplOption>();
	private boolean containsOther;
	
	public ApplSingleSelect(String name, String text) {
		super(1, name);
		this.text = text;
	}
	
	public String getText() {
		return text;
	}
	
	@Override
	public void addOption(ApplOption option) throws ApplDefinitionException {
		if(option==null) throw new ApplDefinitionException("Elementu "+getName()+" nije moguće dodati null opciju.");
		if(option.isOther()) {
			for(ApplOption o : options) {
				if(o.isOther()) {
					throw new ApplDefinitionException("Element "+getName()+", opcija "+option.getKey()+" - opcija ne može biti označena kao \"ostalo\" jer takva već postoji: "+o.getKey()+".");
				}
			}
		}
		checkName(this.getName(), option.getKey());
		int index = options.size();
		if(containsOther) {
			index--;
		}
		options.add(index, option);
		option.setIndex(index);
		if(option.isOther()) {
			containsOther = true;
		}
	}
	
	@Override
	public ApplOption getOption(int index) {
		return options.get(index);
	}
	
	@Override
	public Iterator<ApplOption> iterator() {
		return options.iterator();
	}
	
	@Override
	public int size() {
		return options.size();
	}
	
	private void checkName(String elemName, String name) throws ApplDefinitionException {
		if(StringUtil.isStringBlank(name)) throw new ApplInvalidNameException("", "Ime opcije kod elementa "+elemName+" je nedozvoljenog formata, ili nije zadano, ili je null.");
		for(ApplOption e : options) {
			if(name.equals(e.getKey())) throw new ApplDuplicateNameException(name, "Opcije kod elementa "+elemName+" s imenom "+name+" je već definirana, pa ne može biti opet.");
		}
	}

	@Override
	public void loadState(Properties prop) {
		super.loadState(prop);
		for(ApplOption o : options) {
			o.setEnabled(prop.getProperty("opt.en."+getName()+"$"+o.getKey(),"0").equals("1"));
		}
	}
	
	@Override
	public void storeState(Properties prop) {
		super.storeState(prop);
		for(ApplOption o : options) {
			prop.setProperty("opt.en."+getName()+"$"+o.getKey(), o.isEnabled()?"1":"0");
		}
	}
	
	@Override
	public void loadUsersData(Properties prop) {
		ApplOptionSelection selection = new ApplOptionSelection();
		selection.setKey(prop.getProperty("d."+getName()));
		String t = prop.getProperty("dt."+getName());
		if(t!=null && t.isEmpty()) t = null;
		selection.setText(t);
		setUserData(selection);
	}
	
	@Override
	public void storeUsersData(Properties prop) {
		ApplOptionSelection selection = (ApplOptionSelection)getUserData();
		if(selection==null) return;
		prop.setProperty("d."+getName(), selection.getKey());
		if(selection.getText()!=null) {
			prop.setProperty("dt."+getName(), selection.getText());
		}
	}

	@Override
	public boolean validate(IMessageLogger messageLogger) {
		ApplOptionSelection optSel = (ApplOptionSelection)getUserData();
		if(optSel==null) {
			messageLogger.addErrorMessage("Interna pogreška.");
			return false;
		}
		ApplOption option = getOption(optSel.getKey());
		if(optSel.getKey()==null) {
			messageLogger.addErrorMessage(messageLogger.getText("Error.applicationFieldProblem", new String[] {getName()}));
			return false;
		} else if (option==null) {
			messageLogger.addErrorMessage(messageLogger.getText("Error.applicationFieldProblem", new String[] {getName()}));
			return false;
		} else if(option.isOther() && StringUtil.isStringBlank(optSel.getText())){
			messageLogger.addErrorMessage(messageLogger.getText("Error.applicationFieldProblem", new String[] {getName()}));
			return false;
		}
		return true;
	}
	
	public ApplOption getOption(String key) {
		if(key==null) return null;
		for(int i = 0; i < options.size(); i++) {
			ApplOption o = options.get(i);
			if(o.getKey().equals(key)) return o;
		}
		return null;
	}
	
	public List<ApplOption> getEnabledOptions() {
		List<ApplOption> list = new ArrayList<ApplOption>(options.size());
		for(ApplOption o : options) {
			if(o.isEnabled()) list.add(o);
		}
		return list;
	}

	public ApplOption getIfEnabledOtherOption() {
		if(!containsOther) return null;
		ApplOption o = options.get(options.size()-1);
		if(o.isEnabled()) return o;
		return null;
	}
	
	@Override
	public String toString() {
		return "Elem[type=chooseOne name="+getName()+" numberOfOptions="+options.size()+"]";
	}
	
	@Override
	public boolean isStateEqual(ApplElement other) {
		if(!super.isStateEqual(other)) return false;
		ApplSingleSelect o = (ApplSingleSelect)other;
		if(this.options.size() != o.options.size()) return false;
		for(int i = 0; i < this.options.size(); i++) {
			ApplOption o1 = this.options.get(i);
			ApplOption o2 = o.options.get(i);
			if(!o1.getKey().equals(o2.getKey())) return false;
			if(o1.isEnabled() != o2.isEnabled()) return false;
			if(o1.isOther() != o2.isOther()) return false;
		}
		return true;
	}

	@Override
	public boolean isUserInput() {
		return true;
	}

}
