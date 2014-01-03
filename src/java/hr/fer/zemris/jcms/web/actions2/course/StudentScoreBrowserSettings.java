package hr.fer.zemris.jcms.web.actions2.course;

import hr.fer.zemris.jcms.service2.course.assessments.SummaryViewService;
import hr.fer.zemris.jcms.web.actions.Ext2ActionSupport;
import hr.fer.zemris.jcms.web.actions.annotations.DataResultMapping;
import hr.fer.zemris.jcms.web.actions.annotations.Struts2ResultMapping;
import hr.fer.zemris.jcms.web.actions.annotations.WebClass;
import hr.fer.zemris.jcms.web.actions.annotations.WebMethodInfo;
import hr.fer.zemris.jcms.web.actions.data.StudentScoreBrowserSettingsData;
import hr.fer.zemris.jcms.web.navig.DefaultNavigationBuilder;
import hr.fer.zemris.jcms.web.navig.builders.course.StudentScoreBrowserSettingsBuilder;

@WebClass(dataClass=StudentScoreBrowserSettingsData.class,defaultNavigBuilderIsRoot=false,additionalMenuItems={"m2","StudentScoreBrowser.title"})
public class StudentScoreBrowserSettings extends Ext2ActionSupport<StudentScoreBrowserSettingsData> {

	private static final long serialVersionUID = 2L;
	
	@WebMethodInfo(
		dataResultMappings={@DataResultMapping(dataResult="TEXTOK",struts2Result="TEXTOK")},
		struts2ResultMappings={
			@Struts2ResultMapping(struts2Result="TEXTOK",navigBuilder=DefaultNavigationBuilder.class,navigBuilderIsRoot=true),
			@Struts2ResultMapping(struts2Result="success",navigBuilder=StudentScoreBrowserSettingsBuilder.class,navigBuilderIsRoot=false,additionalMenuItems={"m2","StudentScoreBrowser.title"})
		}
	)
    public String execute() throws Exception {
		SummaryViewService.studentScoreBrowserSettings(getEntityManager(), data);
        return null;
    }

    public String getCourseInstanceID() {
		return data.getCourseInstanceID();
	}
    
    public void setCourseInstanceID(String courseInstanceID) {
		data.setCourseInstanceID(courseInstanceID);
	}
    
	public String getWhat() {
		return data.getWhat();
	}
	public void setWhat(String what) {
		data.setWhat(what);
	}

	public String getReqdata() {
		return data.getReqdata();
	}
	public void setReqdata(String reqdata) {
		data.setReqdata(reqdata);
	}

}
