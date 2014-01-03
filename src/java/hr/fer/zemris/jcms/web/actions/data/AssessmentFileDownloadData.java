package hr.fer.zemris.jcms.web.actions.data;

import hr.fer.zemris.jcms.model.AssessmentFile;
import hr.fer.zemris.jcms.web.actions.AssessmentFileDownload;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

import java.io.File;

/**
 * Podatkovna struktura za akciju {@link AssessmentFileDownload}.
 *  
 * @author marcupic
 *
 */
public class AssessmentFileDownloadData extends BaseAssessment {
	
	private AssessmentFile file;
	private File filePath;
	private String nameToSend;
	
	/**
	 * Konstruktor.
	 * @param messageLogger lokalizirane poruke
	 */
	public AssessmentFileDownloadData(IMessageLogger messageLogger) {
		super(messageLogger);
	}
	
	public AssessmentFile getFile() {
		return file;
	}
	public void setFile(AssessmentFile file) {
		this.file = file;
	}
	
	public File getFilePath() {
		return filePath;
	}
	public void setFilePath(File filePath) {
		this.filePath = filePath;
	}
	
	public String getNameToSend() {
		return nameToSend;
	}
	public void setNameToSend(String nameToSend) {
		this.nameToSend = nameToSend;
	}
}
