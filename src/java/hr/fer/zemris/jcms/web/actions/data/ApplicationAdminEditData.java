package hr.fer.zemris.jcms.web.actions.data;

import hr.fer.zemris.jcms.beans.ApplicationDefinitionBean;
import hr.fer.zemris.jcms.model.ApplicationDefinition;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

public class ApplicationAdminEditData extends BaseCourseInstance {
	
	private ApplicationDefinition definition;
	private ApplicationDefinitionBean bean = null; 

	/**
	 * Konstruktor.
	 * @param messageLogger lokalizirane poruke
	 */
	public ApplicationAdminEditData(IMessageLogger messageLogger) {
		super(messageLogger);
		bean = new ApplicationDefinitionBean();
	}
	
	public ApplicationDefinition getDefinition() {
		return definition;
	}
	public void setDefinition(ApplicationDefinition definition) {
		this.definition = definition;
	}
	
	public ApplicationDefinitionBean getBean() {
		return bean;
	}
	public void setBean(ApplicationDefinitionBean bean) {
		this.bean = bean;
	}
}
