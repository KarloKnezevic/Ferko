package hr.fer.zemris.jcms.service2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import hr.fer.zemris.jcms.dao.DAOHelperFactory;
import hr.fer.zemris.jcms.model.CourseInstance;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.security.JCMSSecurityManagerFactory;
import hr.fer.zemris.jcms.web.actions.data.UserFetcherData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions2.UserFetcher;
import hr.fer.zemris.util.StringUtil;

import javax.persistence.EntityManager;

public class UserFetcherService {

	/**
	 * Ulazna metoda za dohvat studenata po različitim kriterijima. Za sade je jedini podržani kontekst "ci".
	 * Za detalje vidi akciju {@link UserFetcher}.
	 * @param em entity manager
	 * @param data podatkovni objekt
	 */
	public static void fetchUsers(EntityManager em, UserFetcherData data) {
		String crit = data.getCriteria();
		// Ovaj uvjet kasnije prebaci u manji od 3
		if(StringUtil.isStringBlank(crit) || crit.length()<1) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		if("ci".equals(data.getContext())) {
			fetchCourseInstanceUsers(em, data);
			return;
		}
		data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
		data.setResult(AbstractActionData.RESULT_FATAL);
		return;
	}

	private static void fetchCourseInstanceUsers(EntityManager em, UserFetcherData data) {
		CourseInstance ci = null;
		String courseInstanceID = data.getCourseInstanceID();
		if(courseInstanceID!=null && courseInstanceID.length()>0) {
			ci = DAOHelperFactory.getDAOHelper().getCourseInstanceDAO().get(em, courseInstanceID);
		}
		if(ci==null) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		if(!JCMSSecurityManagerFactory.getManager().isStaffOnCourse(ci)) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		List<User> allUsers = DAOHelperFactory.getDAOHelper().getCourseInstanceDAO().findCourseUsers(em, ci.getId());
		List<User> users = new ArrayList<User>(256);
		// Crit sigurno nije prazan. Idemo vidjeti što je točno:
		String crit = data.getCriteria().trim();
		char[] critChars = crit.toCharArray();
		boolean allNumbers = true;
		for(int i = 0; i < critChars.length; i++) {
			if(!Character.isDigit(critChars[i])) {
				allNumbers = false;
				break;
			}
		}
		if(allNumbers) {
			filterUsersByJMBAG(crit, allUsers, users);
		} else {
			int index = crit.indexOf(',');
			if(index==-1) {
				filterUsersByPartialNameComponent(crit, allUsers, users);
			} else {
				filterUsersByCompositeNameComponent(crit.substring(0, index).trim(), crit.substring(index+1).trim(), allUsers, users);
			}
		}
		Collections.sort(users, StringUtil.USER_COMPARATOR);
		data.setUsers(users);
		data.setResult(AbstractActionData.RESULT_SUCCESS);
		return;
	}

	private static void filterUsersByCompositeNameComponent(String name1, String name2, List<User> allUsers, List<User> users) {
		name1 = name1.toUpperCase(StringUtil.HR_LOCALE);
		name2 = name2.toUpperCase(StringUtil.HR_LOCALE);
		for(User user : allUsers) {
			String fName = user.getFirstName().toUpperCase(StringUtil.HR_LOCALE);
			String lName = user.getLastName().toUpperCase(StringUtil.HR_LOCALE);
			if((lName.equals(name1) && fName.startsWith(name2)) || (fName.equals(name1) && lName.startsWith(name2))) {
				users.add(user);
			}
		}
	}

	private static void filterUsersByPartialNameComponent(String name1, List<User> allUsers, List<User> users) {
		name1 = name1.toUpperCase(StringUtil.HR_LOCALE);
		for(User user : allUsers) {
			String lName = user.getLastName().toUpperCase(StringUtil.HR_LOCALE);
			if(lName.startsWith(name1)) {
				users.add(user);
			}
		}
	}

	private static void filterUsersByJMBAG(String crit, List<User> allUsers, List<User> users) {
		for(User user : allUsers) {
			if(user.getJmbag().equals(crit)) {
				users.add(user);
			}
		}
	}
}
