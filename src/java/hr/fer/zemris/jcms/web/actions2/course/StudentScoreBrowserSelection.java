package hr.fer.zemris.jcms.web.actions2.course;

import hr.fer.zemris.jcms.service2.course.assessments.SummaryViewService;

import hr.fer.zemris.jcms.web.actions.Ext2ActionSupport;
import hr.fer.zemris.jcms.web.actions.annotations.DataResultMapping;
import hr.fer.zemris.jcms.web.actions.annotations.Struts2ResultMapping;
import hr.fer.zemris.jcms.web.actions.annotations.WebClass;
import hr.fer.zemris.jcms.web.actions.annotations.WebMethodInfo;
import hr.fer.zemris.jcms.web.actions.data.StudentScoreBrowserSelectionData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.navig.DefaultNavigationBuilder;
import hr.fer.zemris.jcms.web.navig.builders.course.StudentScoreBrowserSettingsBuilder;

@WebClass(dataClass=StudentScoreBrowserSelectionData.class,defaultNavigBuilder=StudentScoreBrowserSettingsBuilder.class,defaultNavigBuilderIsRoot=true)
public class StudentScoreBrowserSelection extends Ext2ActionSupport<StudentScoreBrowserSelectionData> {

	private static final long serialVersionUID = 2L;
	
	@WebMethodInfo
    public String execute() throws Exception {
		SummaryViewService.studentScoreBrowserSelection(getEntityManager(), data);
        return null;
    }

	@WebMethodInfo(
		dataResultMappings={@DataResultMapping(dataResult=AbstractActionData.RESULT_SUCCESS,registerDelayedMessages=true,struts2Result="redirect")},
		struts2ResultMappings={@Struts2ResultMapping(struts2Result="redirect",navigBuilder=DefaultNavigationBuilder.class)}
	)
    public String update() throws Exception {
		SummaryViewService.studentScoreBrowserSelectionChange(getEntityManager(), data);
        return null;
    }

    public String getCourseInstanceID() {
		return data.getCourseInstanceID();
	}
    
    public void setCourseInstanceID(String courseInstanceID) {
		data.setCourseInstanceID(courseInstanceID);
	}
    
    public String getKind() {
		return data.getKind();
	}
    public void setKind(String kind) {
		data.setKind(kind);
	}
}
