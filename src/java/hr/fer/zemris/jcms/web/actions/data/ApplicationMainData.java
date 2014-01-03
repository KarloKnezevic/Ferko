package hr.fer.zemris.jcms.web.actions.data;

import hr.fer.zemris.jcms.model.ApplicationDefinition;

import hr.fer.zemris.jcms.model.StudentApplication;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ApplicationMainData extends BaseCourseInstance {
	
	private List<ApplicationDefinition> definitions;
	private Map<Long, StudentApplication> filledApplications;
	private boolean renderCourseAdministration;
	private SimpleDateFormat sdf;

	private String courseInstanceID;

	/**
	 * Konstruktor.
	 * @param messageLogger lokalizirane poruke
	 */
	public ApplicationMainData(IMessageLogger messageLogger) {
		super(messageLogger);
		List<ApplicationDefinition> l = Collections.emptyList();
		Map<Long, StudentApplication> m = Collections.emptyMap();
		setDefinitions(l);
		setFilledApplications(m);
		sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		}
	
	public List<ApplicationDefinition> getDefinitions() {
		return definitions;
	}

	public void setDefinitions(List<ApplicationDefinition> definitions) {
		this.definitions = definitions;
	}

	public Map<Long, StudentApplication> getFilledApplications() {
		return filledApplications;
	}

	public void setFilledApplications(
			Map<Long, StudentApplication> filledApplications) {
		this.filledApplications = filledApplications;
	}

	public SimpleDateFormat getSdf() {
		return sdf;
	}

	public void setSdf(SimpleDateFormat sdf) {
		this.sdf = sdf;
	}

	public boolean getRenderCourseAdministration() {
		return renderCourseAdministration;
	}

	public void setRenderCourseAdministration(boolean isAdmin) {
		this.renderCourseAdministration = isAdmin;
	}

	public String getCourseInstanceID() {
		return courseInstanceID;
	}
	public void setCourseInstanceID(String courseInstanceID) {
		this.courseInstanceID = courseInstanceID;
	}

}
