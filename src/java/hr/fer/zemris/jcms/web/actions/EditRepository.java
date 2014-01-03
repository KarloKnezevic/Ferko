package hr.fer.zemris.jcms.web.actions;

import hr.fer.zemris.jcms.model.KeyValue;
import hr.fer.zemris.jcms.service.BasicBrowsing;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.opensymphony.xwork2.Preparable;

@Deprecated
public class EditRepository extends ExtendedActionSupport implements Preparable {

	private static final long serialVersionUID = 1L;

	private List<KeyValue> repository;
	private Map<String, KeyValue> repositoryMap;
	private String newName;
	private String newValue;
	
	@Override
	public void prepare() throws Exception {
		repository = BasicBrowsing.listKeyValues();
		repositoryMap = new HashMap<String, KeyValue>(repository.size()*2);
		for(KeyValue kv : repository) {
			repositoryMap.put(kv.getName(), kv);
		}
	}
	
	public String execute() throws Exception {
    	String check = checkUser(new String[] {"admin"}, true);
    	if(check != null) return check;
		BasicBrowsing.updateKeyValues(repository);
		return SUCCESS;
	}
	
	public String input() throws Exception {
    	String check = checkUser(new String[] {"admin"}, true);
    	if(check != null) return check;
		return SUCCESS;
	}

	public String addNew() throws Exception {
    	String check = checkUser(new String[] {"admin"}, true);
    	if(check != null) return check;
		if(newName==null || newName.equals("")) {
			addFieldError("newName", "Name must be set!");
			return SUCCESS;
		}
		if(newValue!=null) newValue = newValue.trim();
		if(newName.equals("academicYear")) {
			if(newValue!=null && !newValue.equals("")) {
				try {
					int a = Integer.parseInt(newValue.substring(0,4));
					int b = Integer.parseInt(newValue.substring(5,9));
					if(a+1!=b || newValue.charAt(4)!='/') throw new Exception();
				} catch(Exception ex) {
					addFieldError("newName", "Missformatted academic year. Use xxxx/yyyy.");
					return SUCCESS;
				}
			}
		}
		if(newName.equals("currentSemester")) {
			if(newValue!=null && !newValue.equals("")) {
				if(newValue.length()!=5) {
					addFieldError("newName", "Missformatted currentSemester. Format is ddddS (e.g. 2007L).");
					return SUCCESS;
				}
				if(!Character.isDigit(newValue.charAt(0)) || !Character.isDigit(newValue.charAt(1)) || !Character.isDigit(newValue.charAt(2)) || !Character.isDigit(newValue.charAt(3)) || !Character.isUpperCase(newValue.charAt(4))) {
					addFieldError("newName", "Missformatted currentSemester. Format is ddddS (e.g. 2007L).");
					return SUCCESS;
				}
			}
		}
		if(newName.equals("marketPlace")) {
			if(newValue!=null && !newValue.equals("")) {
				if(!newValue.equals("yes") && !newValue.equals("no")) {
					addFieldError("newName", "Variable marketPlace must have value 'yes' or 'no'.");
					return SUCCESS;
				}
			}
		}
		BasicBrowsing.addKeyValue(newName, newValue!=null && newValue.length()==0 ? null : newValue);
		newName = "";
		newValue = "";
		prepare();
		return SUCCESS;
	}
	
	public List<KeyValue> getRepository() {
		return repository;
	}
	public void setRepository(List<KeyValue> repository) {
		this.repository = repository;
	}
	public Map<String, KeyValue> getRepositoryMap() {
		return repositoryMap;
	}

	public String getNewName() {
		return newName;
	}
	public void setNewName(String newName) {
		this.newName = newName;
	}

	public String getNewValue() {
		return newValue;
	}
	public void setNewValue(String newValue) {
		this.newValue = newValue;
	}
}
