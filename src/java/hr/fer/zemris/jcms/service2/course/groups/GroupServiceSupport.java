package hr.fer.zemris.jcms.service2.course.groups;

import hr.fer.zemris.jcms.dao.DAOHelper;
import hr.fer.zemris.jcms.dao.DAOHelperFactory;
import hr.fer.zemris.jcms.model.Group;
import hr.fer.zemris.jcms.service.has.HasCourseInstance;
import hr.fer.zemris.jcms.service.has.HasGroup;
import hr.fer.zemris.jcms.service2.course.CourseInstanceServiceSupport;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;

import javax.persistence.EntityManager;

public class GroupServiceSupport {

	public static <T extends AbstractActionData & HasGroup & HasCourseInstance> boolean loadGroup(EntityManager em, T data, Long groupID) {
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		
		// Dohvati grupu
		Group g = dh.getGroupDAO().get(em, groupID);
		if(g==null) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return false;
		}
		data.setGroup(g);
		
		// Dohvat kolegija
		if(!CourseInstanceServiceSupport.fillCourseInstance(em, data, g.getCompositeCourseID())) return false;

		return true;
	}
	
	public static <T extends AbstractActionData & HasGroup & HasCourseInstance> boolean loadGroup(EntityManager em, T data, String courseInstanceID, String relativePath) {
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		
		// Dohvati grupu
		Group g = dh.getGroupDAO().get(em, courseInstanceID, relativePath);
		if(g==null) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return false;
		}
		data.setGroup(g);
		
		// Dohvat kolegija
		if(!CourseInstanceServiceSupport.fillCourseInstance(em, data, g.getCompositeCourseID())) return false;

		return true;
	}
}
