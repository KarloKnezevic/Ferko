package hr.fer.zemris.jcms.web.actions2.course.applications;

import hr.fer.zemris.jcms.beans.ApplicationDefinitionBean;

import hr.fer.zemris.jcms.service2.course.applications.ApplicationService;
import hr.fer.zemris.jcms.web.actions.Ext2ActionSupport;
import hr.fer.zemris.jcms.web.actions.annotations.DataResultMapping;
import hr.fer.zemris.jcms.web.actions.annotations.Struts2ResultMapping;
import hr.fer.zemris.jcms.web.actions.annotations.WebClass;
import hr.fer.zemris.jcms.web.actions.annotations.WebMethodInfo;
import hr.fer.zemris.jcms.web.actions.data.ApplicationAdminEditData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.navig.builders.BuilderDefault;
import hr.fer.zemris.jcms.web.navig.builders.course.applications.ApplicationMainBuilder;

@WebClass(dataClass=ApplicationAdminEditData.class, defaultNavigBuilder=ApplicationMainBuilder.class, defaultNavigBuilderIsRoot=false, additionalMenuItems={"m2", "Navigation.applicationEditing"})
public class ApplicationAdminEdit extends Ext2ActionSupport<ApplicationAdminEditData> {

	private static final long serialVersionUID = 2L;

	@WebMethodInfo
    public String newDefinition() throws Exception {
		ApplicationService.newApplicationDefinition(getEntityManager(), data);
		return null;
    }

	@WebMethodInfo
    public String editDefinition() throws Exception {
		ApplicationService.editApplicationDefinition(getEntityManager(), data);
		return null;
    }

    @WebMethodInfo(
    	dataResultMappings={@DataResultMapping(dataResult=AbstractActionData.RESULT_SUCCESS,struts2Result=SUCCESS,registerDelayedMessages=true)},
    	struts2ResultMappings={@Struts2ResultMapping(struts2Result=SUCCESS,navigBuilder=BuilderDefault.class)}
    )
    public String saveDefinition() throws Exception {
    	ApplicationService.saveOrUpdateApplicationDefinition(getEntityManager(), data);
		return null;
    }

    @WebMethodInfo
    public String execute() throws Exception {
    	return newDefinition();
    }

    public ApplicationDefinitionBean getBean() {
		return data.getBean();
	}
    public void setBean(ApplicationDefinitionBean bean) {
		data.setBean(bean);
	}
}
