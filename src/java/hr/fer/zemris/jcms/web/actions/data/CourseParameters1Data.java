package hr.fer.zemris.jcms.web.actions.data;

import java.util.ArrayList;
import java.util.List;

import hr.fer.zemris.jcms.beans.CIP1RoomParams;
import hr.fer.zemris.jcms.beans.CIP1TermDuration;
import hr.fer.zemris.jcms.service2.course.parameters.CourseParametersService.ParameterAttributes;

import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;
import hr.fer.zemris.jcms.web.actions2.course.parameters.CourseParameters1;
import hr.fer.zemris.util.DeleteOnCloseFileInputStream;

/**
 * Podatkovna struktura za akciju {@link CourseParameters1}.
 *  
 * @author marcupic
 *
 */
public class CourseParameters1Data extends BaseCourseInstance {
	
	private String courseInstanceID;
	private ParameterAttributes miSched;
	private List<CIP1RoomParams> rooms = new ArrayList<CIP1RoomParams>();
	private List<CIP1TermDuration> terms = new ArrayList<CIP1TermDuration>();
	private DeleteOnCloseFileInputStream stream;
	private String yearSemesterID;
	
	/**
	 * Konstruktor.
	 * @param messageLogger lokalizirane poruke
	 */
	public CourseParameters1Data(IMessageLogger messageLogger) {
		super(messageLogger);
	}

	public String getYearSemesterID() {
		return yearSemesterID;
	}
	public void setYearSemesterID(String yearSemesterID) {
		this.yearSemesterID = yearSemesterID;
	}
	
    public String getCourseInstanceID() {
		return courseInstanceID;
	}
    
    public void setCourseInstanceID(String courseInstanceID) {
		this.courseInstanceID = courseInstanceID;
	}

    public ParameterAttributes getMiSched() {
		return miSched;
	}

    public void setMiSched(ParameterAttributes miSched) {
		this.miSched = miSched;
	}
    
    public List<CIP1RoomParams> getRooms() {
		return rooms;
	}
    public void setRooms(List<CIP1RoomParams> rooms) {
		this.rooms = rooms;
	}
    public List<CIP1TermDuration> getTerms() {
		return terms;
	}
    public void setTerms(List<CIP1TermDuration> terms) {
		this.terms = terms;
	}
    public DeleteOnCloseFileInputStream getStream() {
		return stream;
	}
    public void setStream(DeleteOnCloseFileInputStream stream) {
		this.stream = stream;
	}
}
