package hr.fer.zemris.jcms.web.actions2.course.applications;

import hr.fer.zemris.jcms.service2.course.applications.ApplicationService;
import hr.fer.zemris.jcms.web.actions.Ext2ActionSupport;
import hr.fer.zemris.jcms.web.actions.annotations.DataResultMapping;
import hr.fer.zemris.jcms.web.actions.annotations.Struts2ResultMapping;
import hr.fer.zemris.jcms.web.actions.annotations.WebClass;
import hr.fer.zemris.jcms.web.actions.annotations.WebMethodInfo;
import hr.fer.zemris.jcms.web.actions.data.ApplicationAdminAproveData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.navig.builders.BuilderDefault;

@WebClass(dataClass=ApplicationAdminAproveData.class)
public class ApplicationAdminAprove extends Ext2ActionSupport<ApplicationAdminAproveData> {

	private static final long serialVersionUID = 2L;

	@WebMethodInfo
    public String viewStudent() throws Exception {
    	ApplicationService.viewStudentApplicationForApproval(getEntityManager(), data);
		return null;
    }

	@WebMethodInfo(
		dataResultMappings={
			@DataResultMapping(dataResult=AbstractActionData.RESULT_SUCCESS, struts2Result=SUCCESS, registerDelayedMessages=true),
			@DataResultMapping(dataResult="success-list", struts2Result="success-list", registerDelayedMessages=true)
		},
		struts2ResultMappings={
			@Struts2ResultMapping(struts2Result=SUCCESS,navigBuilder=BuilderDefault.class, navigBuilderIsRoot=false),
			@Struts2ResultMapping(struts2Result="success-list",navigBuilder=BuilderDefault.class, navigBuilderIsRoot=false)
		}
	)
    public String aprove() throws Exception {
		ApplicationService.saveStudentApplicationApproval(getEntityManager(), data);
		if(data.getResult().equals(AbstractActionData.RESULT_SUCCESS)) {
			if(data.getFromDefinitionID()!=null) {
				data.setResult("success-list");
			}
		}
		return null;
    }

	@WebMethodInfo
    public String execute() throws Exception {
    	return viewStudent();
    }
}
