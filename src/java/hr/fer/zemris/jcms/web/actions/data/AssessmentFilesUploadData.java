package hr.fer.zemris.jcms.web.actions.data;

import java.io.File;

import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

/**
 * Podatkovna struktura za akciju {@link AssessmentFilesUpload}.
 *  
 * @author marcupic
 *
 */
public class AssessmentFilesUploadData extends BaseAssessment {

	private File archive;
	private String archiveContentType;
	private String archiveFileName;
	private String assessmentID;
	
	/**
	 * Konstruktor.
	 * @param messageLogger lokalizirane poruke
	 */
	public AssessmentFilesUploadData(IMessageLogger messageLogger) {
		super(messageLogger);
	}
	
    public File getArchive() {
		return archive;
	}
    public void setArchive(File archive) {
		this.archive = archive;
	}

    public String getArchiveContentType() {
		return archiveContentType;
	}
    public void setArchiveContentType(String archiveContentType) {
		this.archiveContentType = archiveContentType;
	}

    public String getArchiveFileName() {
		return archiveFileName;
	}
    public void setArchiveFileName(String archiveFileName) {
		this.archiveFileName = archiveFileName;
	}

	public String getAssessmentID() {
		return assessmentID;
	}
	public void setAssessmentID(String assessmentID) {
		this.assessmentID = assessmentID;
	}

}
