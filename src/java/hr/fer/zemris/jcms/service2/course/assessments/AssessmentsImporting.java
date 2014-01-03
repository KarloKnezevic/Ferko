package hr.fer.zemris.jcms.service2.course.assessments;

import java.io.StringReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import hr.fer.zemris.jcms.beans.ext.UserFlagValueBean;
import hr.fer.zemris.jcms.dao.DAOHelper;
import hr.fer.zemris.jcms.dao.DAOHelperFactory;
import hr.fer.zemris.jcms.locking.LockPath;
import hr.fer.zemris.jcms.model.AssessmentFlag;
import hr.fer.zemris.jcms.model.AssessmentFlagValue;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.parsers.AssessmentFlagValuesParser;
import hr.fer.zemris.jcms.security.JCMSSecurityManagerFactory;
import hr.fer.zemris.jcms.web.actions.data.AdminAssessmentFlagImportData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;

import javax.persistence.EntityManager;

/**
 * Sloj usluge koji radi import provjera i zastavica.
 * 
 * @author marcupic
 *
 */
public class AssessmentsImporting {

	/**
	 * Metoda koja se poziva kako bi se ponudio unos vrijednosti zastavice.
	 * 
	 * @param em entity manager
	 * @param data podatci
	 */
	public static void adminAssessmentFlagPrepareImport(EntityManager em, AdminAssessmentFlagImportData data) {

		// Pripremi podatke
		if(!adminAssessmentFlagImportCommon(em, data)) return;
		
		// Gotovi smo
		data.setResult(AbstractActionData.RESULT_INPUT);
	}

	/**
	 * Metoda koja se poziva kako bi se obavio import.
	 * 
	 * @param em entity manager
	 * @param data podatci
	 */
	public static void adminAssessmentFlagImport(EntityManager em, AdminAssessmentFlagImportData data) {
		
		// Pripremi podatke
		if(!adminAssessmentFlagImportCommon(em, data)) return;

		LockPath lp = data.getLockPath();
		if(lp==null || !lp.getPart(0).equals("ml") || !lp.getPart(1).startsWith("ci") || !data.getCourseInstance().getId().equals(lp.getPart(1).substring(2)) || !lp.getPart(2).equals("a") || !lp.getPart(3).startsWith("f") || !data.getAssessmentFlag().getId().toString().equals(lp.getPart(3).substring(1)) || !data.getCourseInstanceID().equals(data.getCourseInstance().getId())) {
			data.getMessageLogger().addWarningMessage("Pogrešan poziv.");
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}

		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		List<UserFlagValueBean> beanList = null; 
		try {
			beanList = AssessmentFlagValuesParser.parseTabbedFormat(new StringReader(data.getText()==null ? "" : data.getText()));
		} catch(Exception ex) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.importParseError")+" "+ex.getMessage());
			data.setResult(AbstractActionData.RESULT_INPUT);
			return;
		}

		AssessmentFlag flag = data.getAssessmentFlag();
		List<User> courseUsers = dh.getUserDAO().listUsersOnCourseInstance(em, flag.getCourseInstance().getId());
		Set<String> regularJmbags = new HashSet<String>();
		Map<String,User> userMap = new HashMap<String, User>(courseUsers.size());
		for(User u : courseUsers) {
			regularJmbags.add(u.getJmbag());
			userMap.put(u.getJmbag(), u);
		}
		boolean errors = false;
		for(UserFlagValueBean bean : beanList) {
			if(!regularJmbags.contains(bean.getJmbag())) {
				errors = true;
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.userNotFound")+" "+bean.getJmbag());
			}
		}
		
		if(!errors) {
			Map<String, AssessmentFlagValue> map = new HashMap<String, AssessmentFlagValue>(courseUsers.size());
			for(AssessmentFlagValue v : flag.getValues()) {
				map.put(v.getUser().getJmbag(), v);
			}
			for(UserFlagValueBean bean : beanList) {
				AssessmentFlagValue v = map.get(bean.getJmbag());
				if(v==null) {
					v = new AssessmentFlagValue();
					v.setAssessmentFlag(flag);
					flag.getValues().add(v);
					v.setError(false);
					v.setManuallySet(true);
					v.setManualValue(bean.getValue());
					v.setUser(userMap.get(bean.getJmbag()));
					v.setValue(false);
					v.setAssigner(data.getCurrentUser());
					dh.getAssessmentDAO().save(em, v);
				} else {
					v.setError(false);
					v.setManuallySet(true);
					v.setManualValue(bean.getValue());
					// v.setValue(bean.getValue());
					v.setAssigner(data.getCurrentUser());
				}
			}
			data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("AssessmentFlags.info.values.updated"));
			data.setResult(AbstractActionData.RESULT_SUCCESS);
		} else {
			data.getMessageLogger().addWarningMessage(data.getMessageLogger().getText("AssessmentFlags.info.values.notUpdated"));
			data.setResult(AbstractActionData.RESULT_INPUT);
		}
		
	}

	/**
	 * Pomoćna metoda koja priprema podatke za metode {@link #adminAssessmentFlagPrepareImport(EntityManager, AdminAssessmentFlagImportData)}
	 * i {@link #adminAssessmentFlagImport(EntityManager, AdminAssessmentFlagImportData)}.
	 * 
	 * @param em entity manager
	 * @param data podatci
	 * @return <code>true</code> ako nema pogreške, <code>false</code> inače
	 */
	private static boolean adminAssessmentFlagImportCommon(EntityManager em, AdminAssessmentFlagImportData data) {
		
		// Dohvat zastavice
		AssessmentServiceSupport.fillAssessmentFlag(em, data, data.getId()==null ? "" : data.getId().toString());
		
		boolean canManage = JCMSSecurityManagerFactory.getManager().canManageAssessments(data.getCourseInstance());
		if(!canManage) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return false;
		}

		return true;
	}

}
