package hr.fer.zemris.jcms.service2.course;

import hr.fer.zemris.jcms.dao.DAOHelper;
import hr.fer.zemris.jcms.dao.DAOHelperFactory;
import hr.fer.zemris.jcms.model.CourseInstance;
import hr.fer.zemris.jcms.model.Group;
import hr.fer.zemris.jcms.model.GroupOwner;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.security.JCMSSecurityManager;
import hr.fer.zemris.jcms.security.JCMSSecurityManagerFactory;
import hr.fer.zemris.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;

/**
 * Pomoćne metode vezane uz kolegij.
 * 
 * @author marcupic
 *
 */
public class CourseServiceUtil {

	/**
	 * Metoda za trenutno prijavljenog korisnika dohvaća sve grupe studenata za predavanja koje on smije
	 * vidjeti na kolegiju. Popis se sortira abecedno. Ako je zastavica <code>addAll</code> postavljena na true, za slučaj
	 * da korisnik smije vidjeti sve, dodat će se još jedna fiktivna grupa koja će zapravo predstavljati "sve".
	 * Ta će se grupa prepoznati po {@link Group#getCompositeCourseID()}=<code>-</code> i
	 * {@link Group#getRelativePath()}=<code>0/-</code>. Pojam trenutni korisnik ovdje znači onaj za koga
	 * je inicijaliziran {@link JCMSSecurityManager}.
	 * 
	 * @param em entity manager
	 * @param currentUser trenutni korisnik
	 * @param courseInstance
	 * @param addAll
	 * @return sortiranu listu dohvatljivih grupa za predavanja
	 */
	public static List<Group> filterAccesibleLectureGroups(EntityManager em, User currentUser, CourseInstance courseInstance, boolean addAll) {
		DAOHelper dh = DAOHelperFactory.getDAOHelper();

		// Dohvati popis grupa studenata
		Group lectureGroupsRoot = dh.getGroupDAO().get(em, courseInstance.getId(), "0");
		List<Group> groupsToDisplay = new ArrayList<Group>();
		if(JCMSSecurityManagerFactory.getManager().canManageAssessments(courseInstance)) {
			groupsToDisplay.addAll(lectureGroupsRoot.getSubgroups());
			Collections.sort(groupsToDisplay, StringUtil.GROUP_COMPARATOR);
			if(addAll) {
				Group g = new Group();
				g.setName("----");
				g.setCompositeCourseID("-");
				g.setRelativePath("0/-");
				g.setId(null);
				groupsToDisplay.add(0, g);
			}
		} else {
			List<GroupOwner> allOwners = dh.getGroupDAO().findForSubgroups(em, courseInstance.getId(), "0");
			Set<Group> allowedGroups = new HashSet<Group>(lectureGroupsRoot.getSubgroups().size());
			for(GroupOwner go : allOwners) {
				if(go.getUser().getId().equals(currentUser.getId())) {
					allowedGroups.add(go.getGroup());
				}
			}
			groupsToDisplay.addAll(allowedGroups);
			Collections.sort(groupsToDisplay, StringUtil.GROUP_COMPARATOR);
		}
		return groupsToDisplay;
	}
}
