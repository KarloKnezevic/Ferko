package hr.fer.zemris.jcms.web.actions2.course.applications;

import hr.fer.zemris.jcms.beans.StudentApplicationBean;

import hr.fer.zemris.jcms.service2.course.applications.ApplicationService;
import hr.fer.zemris.jcms.web.actions.Ext2ActionSupport;
import hr.fer.zemris.jcms.web.actions.annotations.DataResultMapping;
import hr.fer.zemris.jcms.web.actions.annotations.Struts2ResultMapping;
import hr.fer.zemris.jcms.web.actions.annotations.WebClass;
import hr.fer.zemris.jcms.web.actions.annotations.WebMethodInfo;
import hr.fer.zemris.jcms.web.actions.data.ApplicationStudentSubmitData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.navig.builders.BuilderDefault;
import hr.fer.zemris.jcms.web.navig.builders.course.applications.ApplicationMainBuilder;

@WebClass(dataClass=ApplicationStudentSubmitData.class, defaultNavigBuilder=ApplicationMainBuilder.class, defaultNavigBuilderIsRoot=false, additionalMenuItems={"m2", "Navigation.applicationEditing"})
public class ApplicationStudentSubmit extends Ext2ActionSupport<ApplicationStudentSubmitData> {

	private static final long serialVersionUID = 2L;

	
    @WebMethodInfo(
    	dataResultMappings={@DataResultMapping(dataResult=AbstractActionData.RESULT_SUCCESS,struts2Result=SUCCESS,registerDelayedMessages=true)},
    	struts2ResultMappings={@Struts2ResultMapping(struts2Result=SUCCESS,navigBuilder=BuilderDefault.class)}
    )
    public String newApplication() throws Exception {
    	ApplicationService.prepareStudentApplication(getEntityManager(), data);
		return null;
    }

    @WebMethodInfo(
    	dataResultMappings={@DataResultMapping(dataResult=AbstractActionData.RESULT_SUCCESS,struts2Result=SUCCESS,registerDelayedMessages=true)},
    	struts2ResultMappings={@Struts2ResultMapping(struts2Result=SUCCESS,navigBuilder=BuilderDefault.class)}
    )
    public String saveApplication() throws Exception {
    	ApplicationService.saveStudentApplication(getEntityManager(), data);
		return null;
    }

	public StudentApplicationBean getBean() {
		return data.getBean();
	}

	public void setBean(StudentApplicationBean bean) {
		data.setBean(bean);
	}
}
