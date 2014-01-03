package hr.fer.zemris.jcms.web.actions2.course.assessments.types.choice;

import hr.fer.zemris.jcms.service2.course.assessments.types.choice.ChoiceUploadService;
import hr.fer.zemris.jcms.web.actions.Ext2ActionSupport;
import hr.fer.zemris.jcms.web.actions.annotations.WebClass;
import hr.fer.zemris.jcms.web.actions.annotations.WebMethodInfo;
import hr.fer.zemris.jcms.web.actions.data.AdminUploadChoiceConfData;
import hr.fer.zemris.jcms.web.navig.builders.course.assessments.AdminAssessmentViewBuilder;

@WebClass(dataClass=AdminUploadChoiceConfData.class, defaultNavigBuilder=AdminAssessmentViewBuilder.class,
		defaultNavigBuilderIsRoot=false, additionalMenuItems={"m2","Assessments.nav.uploadResults"})
public class UploadStudentResults extends Ext2ActionSupport<AdminUploadChoiceConfData> {

	private static final long serialVersionUID = 2L;

    /**
     * Obavlja upload iz teksta unesenog u formular.
     */
    @WebMethodInfo
    public String upload() throws Exception {
    	// Pozovi sloj usluge koji će napuniti navedenu strukturu...
    	ChoiceUploadService.setStudentResults(getEntityManager(), data);
		return null;
    }
    
    /**
     * Obavlja upload iz datoteke.
     */
    @WebMethodInfo
    public String uploadFile() throws Exception {
    	// Pozovi sloj usluge koji će napuniti navedenu strukturu...
    	ChoiceUploadService.uploadStudentResults(getEntityManager(), data);
		return null;
    }
    
    /**
     * Služi radi početnog prikaza praznog formulara.
     */
    @WebMethodInfo
    public String execute() throws Exception {
    	// Pozovi sloj usluge koji će napuniti navedenu strukturu...
    	ChoiceUploadService.fetchChoiceData(getEntityManager(), data);
    	return null;
	}


    public String getAssessmentID() {
		return data.getAssessmentID();
	}
    
    public void setAssessmentID(String assessmentID) {
		data.setAssessmentID(assessmentID);
	}
}
