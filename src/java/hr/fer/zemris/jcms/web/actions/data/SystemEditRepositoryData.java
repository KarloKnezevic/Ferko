package hr.fer.zemris.jcms.web.actions.data;

import java.util.ArrayList;
import java.util.List;

import hr.fer.zemris.jcms.beans.KeyValueBean;

import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

/**
 * Podatkovna struktura za akciju {@link AdminAssessmentEdit}.
 *  
 * @author marcupic
 *
 */
public class SystemEditRepositoryData extends AbstractActionData {

	private List<KeyValueBean> repository;
	private String newName;
	private String newValue;

	/**
	 * Konstruktor.
	 * @param messageLogger lokalizirane poruke
	 */
	public SystemEditRepositoryData(IMessageLogger messageLogger) {
		super(messageLogger);
	}

	public List<KeyValueBean> getRepository() {
		if(repository==null) {
			repository = new ArrayList<KeyValueBean>();
		}
		return repository;
	}
	public void setRepository(List<KeyValueBean> repository) {
		this.repository = repository;
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
