package hr.fer.zemris.jcms.web.actions2.course.applications;

import hr.fer.zemris.jcms.service2.course.applications.ApplicationService;
import hr.fer.zemris.jcms.web.actions.Ext2ActionSupport;
import hr.fer.zemris.jcms.web.actions.annotations.DataResultMapping;
import hr.fer.zemris.jcms.web.actions.annotations.Struts2ResultMapping;
import hr.fer.zemris.jcms.web.actions.annotations.TransactionalMethod;
import hr.fer.zemris.jcms.web.actions.annotations.WebClass;
import hr.fer.zemris.jcms.web.actions.annotations.WebMethodInfo;
import hr.fer.zemris.jcms.web.actions.data.ApplicationExportTableData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.navig.builders.BuilderDefault;

@WebClass(dataClass=ApplicationExportTableData.class,defaultNavigBuilder=BuilderDefault.class)
public class ApplicationExportTable extends Ext2ActionSupport<ApplicationExportTableData> {

	private static final long serialVersionUID = 2L;

	@WebMethodInfo(
			dataResultMappings={@DataResultMapping(dataResult=AbstractActionData.RESULT_SUCCESS,struts2Result=SUCCESS,registerDelayedMessages=false)},
			struts2ResultMappings={@Struts2ResultMapping(struts2Result=SUCCESS,navigBuilder=BuilderDefault.class,transactionalMethod=@TransactionalMethod(closeImmediately=true))}
		)
    public String execute() throws Exception {
		ApplicationService.exportApplicationsMatrixOnCourse(getEntityManager(), data);
        return null;
    }

	public String getFormat() {
		return data.getFormat();
	}
	public void setFormat(String format) {
		data.setFormat(format);
	}
	
	public String getCourseInstanceID() {
		return data.getCourseInstanceID();
	}
	public void setCourseInstanceID(String courseInstanceID) {
		data.setCourseInstanceID(courseInstanceID);
	}

}
