package hr.fer.zemris.jcms.service;

import java.util.HashSet;

import java.util.Set;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;

import hr.fer.zemris.jcms.dao.DAOHelper;
import hr.fer.zemris.jcms.dao.DAOHelperFactory;
import hr.fer.zemris.jcms.dao.DatabaseOperation;
import hr.fer.zemris.jcms.dao.PersistenceUtil;
import hr.fer.zemris.jcms.model.CourseInstance;
import hr.fer.zemris.jcms.model.Role;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.model.YearSemester;
import hr.fer.zemris.jcms.service.sso.ISSOChecker;
import hr.fer.zemris.jcms.service.sso.SSOCheckerFactory;
import hr.fer.zemris.jcms.web.actions.data.SSOData;

public class SSOService {

	public static final Logger logger = Logger.getLogger(SSOService.class);

	public static void getSSOData(final SSOData data, final String code, final String courseID, final String time, final String auth, final String system) {

		ISSOChecker checker = SSOCheckerFactory.getInstance(null);
		boolean result = checker.check(code, courseID, time, auth);
		if(!result) {
			data.setValid(false);
			return;
		}
		
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {

				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				String semID = BasicBrowsing.getCurrentSemesterID(em);
				YearSemester ysem = null;
				if(semID!=null && !semID.equals("")) ysem = dh.getYearSemesterDAO().get(em, semID);

				User user = dh.getUserDAO().getUserByJMBAG(em, code);
				if(user==null) {
					logger.warn("[SSOMessage] Nemam korisnika: {"+code+"}");
					data.setValid(false);
					return null;
				}
				
				logger.info("[SSOMessage] Prihvaćam korisnika: {"+code+"}/{"+user.getUsername()+"}");

				Set<String> roles = getRolesAsStringSet(user.getUserDescriptor().getRoles());
				data.setRoles(roles);
				data.setCurrentUser(user);
				data.setFirstName(user.getFirstName());
				data.setLastName(user.getLastName());
				data.setUserID(user.getId());
				data.setUsername(user.getUsername());
				data.setValid(true);

				if(courseID.length()>0 && ysem!=null) {
					String courseInstanceKey = ysem.getId()+"/"+courseID;
					CourseInstance ci = dh.getCourseInstanceDAO().get(em, courseInstanceKey);
					data.setCourseInstance(ci);
					if(ci==null) {
						logger.warn("[SSOMessage] Nemam kolegij: "+courseInstanceKey);
					}
				}
				return null;
			}
		});
	}

	private static Set<String> getRolesAsStringSet(Set<Role> roles) {
		Set<String> sRoles = null;
		if(roles==null) return new HashSet<String>();
		sRoles = new HashSet<String>(roles.size());
		for(Role r : roles) {
			sRoles.add(r.getName());
		}
		return sRoles;
	}

}
