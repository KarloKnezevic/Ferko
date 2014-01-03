package hr.fer.zemris.jcms.web.actions2.course.assessments;

import hr.fer.zemris.jcms.service2.course.assessments.AssessmentsImporting;
import hr.fer.zemris.jcms.web.actions.Ext2ActionSupport;
import hr.fer.zemris.jcms.web.actions.annotations.WebClass;
import hr.fer.zemris.jcms.web.actions.annotations.WebMethodInfo;
import hr.fer.zemris.jcms.web.actions.data.AdminAssessmentFlagImportData;
import hr.fer.zemris.jcms.web.navig.builders.course.assessments.AdminAssessmentListBuilder;

/**
 * Učitavanje vrijednosti zastavice.
 * 
 * @author marcupic
 *
 */
@WebClass(dataClass=AdminAssessmentFlagImportData.class, defaultNavigBuilder=AdminAssessmentListBuilder.class,
		defaultNavigBuilderIsRoot=false,additionalMenuItems={"m2","AssessmentFlags.nav.importValues"})
public class AdminAssessmentFlagImport extends Ext2ActionSupport<AdminAssessmentFlagImportData> {

	private static final long serialVersionUID = 3L;

	@WebMethodInfo
	public String prepare() throws Exception {
		AssessmentsImporting.adminAssessmentFlagPrepareImport(getEntityManager(), data);
		return null;
	}

	@WebMethodInfo(lockPath="ml\\ci${courseInstanceID}\\a\\f${id}")
	public String importValues() throws Exception {
		AssessmentsImporting.adminAssessmentFlagImport(getEntityManager(), data);
		return null;
	}

	@WebMethodInfo
    public String execute() throws Exception {
		// preusmjeri na prepare
		return prepare();
    }

	public String getText() {
		return data.getText();
	}
	public void setText(String text) {
		data.setText(text);
	}

	public Long getId() {
		return data.getId();
	}
	public void setId(Long id) {
		data.setId(id);
	}
	public String getCourseInstanceID() {
		return data.getCourseInstanceID();
	}
	public void setCourseInstanceID(String courseInstanceID) {
		data.setCourseInstanceID(courseInstanceID);
	}
    
    
}
