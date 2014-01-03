package hr.fer.zemris.jcms.web.actions2.course.assessments;

import hr.fer.zemris.jcms.service2.course.assessments.AssessmentsUploadService;
import hr.fer.zemris.jcms.web.actions.Ext2ActionSupport;
import hr.fer.zemris.jcms.web.actions.annotations.DataResultMapping;
import hr.fer.zemris.jcms.web.actions.annotations.WebClass;
import hr.fer.zemris.jcms.web.actions.annotations.WebMethodInfo;
import hr.fer.zemris.jcms.web.actions.data.AssessmentFilesUploadData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.navig.builders.BuilderDefault;

import java.io.File;

/**
 * Akcija služi za upload ZIP datoteke s obrascima i drugim dokumentima na provjeru.
 * 
 * @author marcupic
 *
 */
@WebClass(dataClass=AssessmentFilesUploadData.class, defaultNavigBuilder=BuilderDefault.class)
public class AssessmentFilesUpload extends Ext2ActionSupport<AssessmentFilesUploadData> {

	private static final long serialVersionUID = 2L;
	
	@WebMethodInfo(dataResultMappings={@DataResultMapping(dataResult=AbstractActionData.RESULT_NONFATAL_ERROR,struts2Result=SUCCESS,registerDelayedMessages=true)})
    public String upload() throws Exception {
    	AssessmentsUploadService.uploadZippedFilesOnAssessment(getEntityManager(), data);
    	return null;
    }

	@WebMethodInfo(dataResultMappings={@DataResultMapping(dataResult=AbstractActionData.RESULT_NONFATAL_ERROR,struts2Result=SUCCESS,registerDelayedMessages=true)})
    public String execute() throws Exception {
    	return upload();
    }

    public File getArchive() {
		return data.getArchive();
	}
    public void setArchive(File archive) {
		data.setArchive(archive);
	}

    public String getArchiveContentType() {
		return data.getArchiveContentType();
	}
    public void setArchiveContentType(String archiveContentType) {
		data.setArchiveContentType(archiveContentType);
	}

    public String getArchiveFileName() {
		return data.getArchiveFileName();
	}
    public void setArchiveFileName(String archiveFileName) {
		data.setArchiveFileName(archiveFileName);
	}

	public String getAssessmentID() {
		return data.getAssessmentID();
	}
	public void setAssessmentID(String assessmentID) {
		data.setAssessmentID(assessmentID);
	}
}
