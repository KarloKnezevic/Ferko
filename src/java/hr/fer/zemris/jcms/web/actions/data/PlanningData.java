package hr.fer.zemris.jcms.web.actions.data;

import java.util.ArrayList;
import java.util.List;

import hr.fer.zemris.jcms.beans.CourseComponentBean;
import hr.fer.zemris.jcms.beans.PlanDescriptorBean;
import hr.fer.zemris.jcms.beans.ScheduleBean;
import hr.fer.zemris.jcms.model.CourseInstance;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;
import hr.fer.zemris.util.DeleteOnCloseFileInputStream;

public class PlanningData extends AbstractActionData{
	
	private CourseInstance courseInstance;
	private List<PlanDescriptorBean> planBeans;
	private DeleteOnCloseFileInputStream stream;
	private ScheduleBean scheduleBean;
	private List<CourseComponentBean> components; 
	private List<String> numbers;
	private List<String> scheduleValidationMessages = new ArrayList<String>();
	private boolean scheduleValidationResult;
	
	/**
	 * Konstruktor.
	 * @param messageLogger lokalizirane poruke
	 */
	public PlanningData(IMessageLogger messageLogger) {
		super(messageLogger);
		scheduleBean = new ScheduleBean();
		

	}

	public void setCourseInstance(CourseInstance courseInstance) {
		this.courseInstance = courseInstance;
	}

	public CourseInstance getCourseInstance() {
		return courseInstance;
	}

	public void setPlanBeans(List<PlanDescriptorBean> planBeans) {
		this.planBeans = planBeans;
	}

	public List<PlanDescriptorBean> getPlanBeans() {
		return planBeans;
	}

	public void setStream(DeleteOnCloseFileInputStream stream) {
		this.stream = stream;
	}

	public DeleteOnCloseFileInputStream getStream() {
		return stream;
	}

	public ScheduleBean getScheduleBean() {
		return scheduleBean;
	}

	public void setScheduleBean(ScheduleBean scheduleBean) {
		this.scheduleBean = scheduleBean;
	}


	public List<CourseComponentBean> getComponents() {
		return components;
	}

	public void setComponents(List<CourseComponentBean> components) {
		this.components = components;
	}

	public List<String> getNumbers() {
		return numbers;
	}

	public void setNumbers(List<String> numbers) {
		this.numbers = numbers;
	}

	public List<String> getScheduleValidationMessages() {
		return scheduleValidationMessages;
	}

	public void setScheduleValidationMessages(List<String> scheduleValidationMessages) {
		this.scheduleValidationMessages = scheduleValidationMessages;
	}

	public boolean isScheduleValidationResult() {
		return scheduleValidationResult;
	}

	public void setScheduleValidationResult(boolean scheduleValidationResult) {
		this.scheduleValidationResult = scheduleValidationResult;
	}


}
