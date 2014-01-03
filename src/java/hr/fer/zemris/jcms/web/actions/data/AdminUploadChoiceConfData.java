package hr.fer.zemris.jcms.web.actions.data;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import hr.fer.zemris.jcms.beans.StringNameStringValue;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

/**
 * Podatkovna struktura za akciju {@link AdminUploadChoiceConf}.
 * 
 * @author Ivan Krišto
 * 
 */
public class AdminUploadChoiceConfData extends BaseAssessment {

	private String assessmentID;
	private String text;
	private String appendOrReplace;
	private String dataFormat = "PLAIN";
	public List<StringNameStringValue> formats;

	private File dataFile;
	private String dataFileContentType;
	private String dataFileFileName;

	/**
	 * Konstruktor.
	 * 
	 * @param messageLogger
	 *            lokalizirane poruke
	 */
	public AdminUploadChoiceConfData(IMessageLogger messageLogger) {
		super(messageLogger);

		formats = new ArrayList<StringNameStringValue>();
		formats.add(new StringNameStringValue("PLAIN", messageLogger
				.getText("choiceformat.plain")));
		formats.add(new StringNameStringValue("RMK", messageLogger
				.getText("choiceformat.rmk")));
	}

	public String getAssessmentID() {
		return assessmentID;
	}

	public void setAssessmentID(String assessmentID) {
		this.assessmentID = assessmentID;
	}

	public List<StringNameStringValue> getFormats() {
		return formats;
	}

	public String getDataFormat() {
		return dataFormat;
	}

	public void setDataFormat(String dataFormat) {
		this.dataFormat = dataFormat;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String getAppendOrReplace() {
		return appendOrReplace;
	}

	public void setAppendOrReplace(String appendOrReplace) {
		this.appendOrReplace = appendOrReplace;
	}

	public File getDataFile() {
		return dataFile;
	}

	public void setDataFile(File dataFile) {
		this.dataFile = dataFile;
	}

	public String getDataFileContentType() {
		return dataFileContentType;
	}

	public void setDataFileContentType(String dataFileContentType) {
		this.dataFileContentType = dataFileContentType;
	}

	public String getDataFileFileName() {
		return dataFileFileName;
	}

	public void setDataFileFileName(String dataFileFileName) {
		this.dataFileFileName = dataFileFileName;
	}

}
