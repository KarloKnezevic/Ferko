package hr.fer.zemris.jcms.security;

import hr.fer.zemris.jcms.JCMSSettings;

import hr.fer.zemris.jcms.beans.MPRootInfoBean;
import hr.fer.zemris.jcms.dao.DAOHelper;
import hr.fer.zemris.jcms.dao.DAOHelperFactory;
import hr.fer.zemris.jcms.dao.GroupDAO;
import hr.fer.zemris.jcms.dao.PollDAO;
import hr.fer.zemris.jcms.model.CourseInstance;
import hr.fer.zemris.jcms.model.CourseInstanceKeyValue;
import hr.fer.zemris.jcms.model.Group;
import hr.fer.zemris.jcms.model.GroupOwner;
import hr.fer.zemris.jcms.model.MarketPlace;
import hr.fer.zemris.jcms.model.Issue;
import hr.fer.zemris.jcms.model.IssueTopic;
import hr.fer.zemris.jcms.model.Role;
import hr.fer.zemris.jcms.model.ToDoTask;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.model.UserGroup;
import hr.fer.zemris.jcms.model.YearSemester;
import hr.fer.zemris.jcms.model.extra.IssueStatus;
import hr.fer.zemris.jcms.model.forum.Category;
import hr.fer.zemris.jcms.model.forum.Subforum;
import hr.fer.zemris.jcms.model.forum.Topic;
import hr.fer.zemris.jcms.model.poll.Poll;
import hr.fer.zemris.jcms.security.GroupSupportedPermission.MarketPlacePlacement;
import hr.fer.zemris.util.StringUtil;
import hr.fer.zemris.util.Tree;
import hr.fer.zemris.util.TreeNode;
import hr.fer.zemris.util.TreePath;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;

/**
 * Jednostavan security manager za JCMS.
 * 
 * @author marcupic
 */
public class JCMSSecurityManager implements IJCMSSecurityManager {

	private static final Map<String,Set<String>> courseSecurityModMatrix;
	
	static {
		courseSecurityModMatrix = new HashMap<String, Set<String>>();
		Set<String> set;
		
		set = new HashSet<String>();
		set.add(JCMSSecurityConstants.ADMIN_KOLEGIJA);
		set.add(JCMSSecurityConstants.NOSITELJ);
		set.add(JCMSSecurityConstants.NASTAVNIK);
		set.add(JCMSSecurityConstants.ASISTENT);
		set.add(JCMSSecurityConstants.ASISTENT_ORG);
		courseSecurityModMatrix.put(JCMSSecurityConstants.ADMIN_KOLEGIJA, set);
		set = new HashSet<String>();
		set.add(JCMSSecurityConstants.NASTAVNIK);
		set.add(JCMSSecurityConstants.ASISTENT);
		set.add(JCMSSecurityConstants.ASISTENT_ORG);
		courseSecurityModMatrix.put(JCMSSecurityConstants.NOSITELJ, set);
		set = new HashSet<String>();
		set.add(JCMSSecurityConstants.ASISTENT);
		courseSecurityModMatrix.put(JCMSSecurityConstants.NASTAVNIK, set);
		set = new HashSet<String>();
		set.add(JCMSSecurityConstants.NOSITELJ);
		set.add(JCMSSecurityConstants.NASTAVNIK);
		set.add(JCMSSecurityConstants.ASISTENT);
		set.add(JCMSSecurityConstants.ASISTENT_ORG);
		courseSecurityModMatrix.put(JCMSSecurityConstants.ASISTENT_ORG, set);
		set = new HashSet<String>();
		courseSecurityModMatrix.put(JCMSSecurityConstants.ASISTENT, set);
	}
	
	@Override
	public boolean canAnalizeGlobalSchedule() {
		return canPerformSystemAdministration();
	}
	
	@Override
	public boolean canAddAccouts() {
		return canPerformSystemAdministration();
	}
	
	@Override
	public boolean canBrowseAccouts() {
		return canPerformSystemAdministration();
	}
	
	@Override
	public boolean canEditAccouts() {
		return canPerformSystemAdministration();
	}
	
	@Override
	public boolean canPerformCourseAdministration() {
		Data d = local.get();
		if(d==null) throw new JCMSSManNotInitializedException();
		Set<String> roles = d.getRoles();
		if(roles.contains(JCMSSecurityConstants.ROLE_ADMIN)) return true;
		if(roles.contains(JCMSSecurityConstants.ROLE_COURSE_STAFF)) return true;
		if(roles.contains(JCMSSecurityConstants.ROLE_ASISTENT)) return true;
		return false;
	}

	@Override
	public boolean canPerformSystemAdministration() {
		Data d = local.get();
		if(d==null) throw new JCMSSManNotInitializedException();
		Set<String> roles = d.getRoles();
		return roles.contains(JCMSSecurityConstants.ROLE_ADMIN);
	}

	@Override
	public boolean canPerformCourseAdministration(CourseInstance courseInstance) {
		Data d = local.get();
		if(d==null) throw new JCMSSManNotInitializedException();
		// Ako je admin:
		Set<String> roles = d.getRoles();
		if(roles.contains(JCMSSecurityConstants.ROLE_ADMIN)) {
			return true;
		}
		// Ako je osoblje kolegija:
		Set<String> inGroups = d.getCourseSecurityGroupsFor(courseInstance);
		if(inGroups.contains(JCMSSecurityConstants.ADMIN_KOLEGIJA)) return true;
		if(inGroups.contains(JCMSSecurityConstants.NOSITELJ)) return true;
		if(inGroups.contains(JCMSSecurityConstants.ASISTENT_ORG)) return true;
		// Inače:
		return false;
	}

	@Override
	public boolean canUsePrivateGroups(CourseInstance courseInstance) {
		Data d = local.get();
		if(d==null) throw new JCMSSManNotInitializedException();
		// Ako je admin:
		Set<String> roles = d.getRoles();
		if(roles.contains(JCMSSecurityConstants.ROLE_ADMIN)) {
			return true;
		}
		// Ako je osoblje kolegija:
		Set<String> inGroups = d.getCourseSecurityGroupsFor(courseInstance);
		if(inGroups.contains(JCMSSecurityConstants.ADMIN_KOLEGIJA)) return true;
		if(inGroups.contains(JCMSSecurityConstants.NOSITELJ)) return true;
		if(inGroups.contains(JCMSSecurityConstants.ASISTENT_ORG)) return true;
		if(inGroups.contains(JCMSSecurityConstants.NASTAVNIK)) return true;
		// Inače:
		return false;
	}
	
	@Override
	public boolean canUseExternalGoToLabosiSSO(CourseInstance courseInstance) {
		if(!"true".equals(JCMSSettings.getSettings().getObjects().get("jcms.external.labosi.enabled"))) {
			return false;
		}
		Data d = local.get();
		if(d==null) throw new JCMSSManNotInitializedException();
		// Samo ako je glavni asistent.
		Set<String> inGroups = d.getCourseSecurityGroupsFor(courseInstance);
		if(inGroups.contains(JCMSSecurityConstants.ASISTENT_ORG)) return true;
		// Inače:
		return false;
	}
	
	@Override
	public boolean canUseQuestionBrowser(CourseInstance courseInstance) {
		if(!"true".equals(JCMSSettings.getSettings().getObjects().get("jcms.questionBrowser.enabled"))) {
			return false;
		}
		Data d = local.get();
		if(d==null) throw new JCMSSManNotInitializedException();
		// Ako je admin:
		Set<String> roles = d.getRoles();
		if(roles.contains(JCMSSecurityConstants.ROLE_ADMIN)) {
			return true;
		}
		// Ako je osoblje kolegija:
		Set<String> inGroups = d.getCourseSecurityGroupsFor(courseInstance);
		if(inGroups.contains(JCMSSecurityConstants.ADMIN_KOLEGIJA)) return true;
		if(inGroups.contains(JCMSSecurityConstants.NOSITELJ)) return true;
		if(inGroups.contains(JCMSSecurityConstants.ASISTENT_ORG)) return true;
		// Inače:
		return false;
	}
	
	@Override
	public boolean canViewCourseAppeals(CourseInstance courseInstance) {
		return checkForCoursePermissions(courseInstance, new String[] {JCMSSecurityConstants.ADMIN_KOLEGIJA, JCMSSecurityConstants.NOSITELJ, JCMSSecurityConstants.ASISTENT_ORG});
	}

	@Override
	public boolean canViewCourseApplications(CourseInstance courseInstance) {
		return checkForCoursePermissions(courseInstance, new String[] {JCMSSecurityConstants.ADMIN_KOLEGIJA, JCMSSecurityConstants.NOSITELJ, JCMSSecurityConstants.ASISTENT_ORG});
	}

	@Override
	public boolean canViewCourseAssessments(CourseInstance courseInstance) {
		return checkForCoursePermissions(courseInstance, new String[] {JCMSSecurityConstants.ADMIN_KOLEGIJA, JCMSSecurityConstants.NOSITELJ, JCMSSecurityConstants.ASISTENT_ORG, JCMSSecurityConstants.NASTAVNIK});
	}

	@Override
	public boolean canViewCourseBarCode(CourseInstance courseInstance) {
		return checkForCoursePermissions(courseInstance, new String[] {JCMSSecurityConstants.ADMIN_KOLEGIJA, JCMSSecurityConstants.NOSITELJ, JCMSSecurityConstants.ASISTENT_ORG});
	}

	@Override
	public boolean canViewCourseGroupTree(CourseInstance courseInstance) {
		return checkForCoursePermissions(courseInstance, new String[] {JCMSSecurityConstants.ADMIN_KOLEGIJA, JCMSSecurityConstants.NOSITELJ, JCMSSecurityConstants.ASISTENT_ORG, JCMSSecurityConstants.NASTAVNIK});
	}

	@Override
	public boolean canViewCourseLectureGroups(CourseInstance courseInstance) {
		return checkForCoursePermissions(courseInstance, new String[] {JCMSSecurityConstants.ADMIN_KOLEGIJA, JCMSSecurityConstants.NOSITELJ, JCMSSecurityConstants.ASISTENT_ORG, JCMSSecurityConstants.NASTAVNIK});
	}

	@Override
	public boolean canViewCoursePermissions(CourseInstance courseInstance) {
		return checkForCoursePermissions(courseInstance, new String[] {JCMSSecurityConstants.ADMIN_KOLEGIJA, JCMSSecurityConstants.NOSITELJ, JCMSSecurityConstants.ASISTENT_ORG, JCMSSecurityConstants.NASTAVNIK});
	}

	@Override
	public boolean canViewCourseScheduleAnalyzer(CourseInstance courseInstance) {
		return checkForCoursePermissions(courseInstance, new String[] {JCMSSecurityConstants.ADMIN_KOLEGIJA, JCMSSecurityConstants.NOSITELJ, JCMSSecurityConstants.ASISTENT_ORG, JCMSSecurityConstants.NASTAVNIK});
	}

	@Override
	public boolean canViewCourseTeachers(CourseInstance courseInstance) {
		return checkForCoursePermissions(courseInstance, new String[] {JCMSSecurityConstants.ADMIN_KOLEGIJA, JCMSSecurityConstants.NOSITELJ, JCMSSecurityConstants.ASISTENT_ORG});
	}

	@Override
	public boolean canViewGradingPolicy(CourseInstance courseInstance) {
		return checkForCoursePermissions(courseInstance, new String[] {JCMSSecurityConstants.ADMIN_KOLEGIJA, JCMSSecurityConstants.NOSITELJ, JCMSSecurityConstants.ASISTENT_ORG, JCMSSecurityConstants.NASTAVNIK});
	}
	@Override
	public boolean canManageCourseParameters(CourseInstance courseInstance) {
		return checkForCoursePermissions(courseInstance, new String[] {JCMSSecurityConstants.ADMIN_KOLEGIJA, JCMSSecurityConstants.NOSITELJ, JCMSSecurityConstants.ASISTENT_ORG});
	}
	@Override
	public boolean canEditGradingPolicy(CourseInstance courseInstance) {
		return checkForCoursePermissions(courseInstance, new String[] {JCMSSecurityConstants.ADMIN_KOLEGIJA, JCMSSecurityConstants.NOSITELJ, JCMSSecurityConstants.ASISTENT_ORG});
	}
	
	public boolean checkForCoursePermissions(CourseInstance courseInstance, String[] permissions) {
		if(canPerformSystemAdministration()) return true;
		Data d = local.get();
		if(d==null) throw new JCMSSManNotInitializedException();
		return setContainsAnyOf(d.getCourseSecurityGroupsFor(courseInstance), permissions);
	}

	private boolean setContainsAnyOf(Set<String> set, String[] strings) {
		if(set==null || strings==null || set.isEmpty()) return false;
		for(int i = 0; i < strings.length; i++) {
			if(set.contains(strings[i])) return true;
		}
		return false;
	}

	@Override
	public boolean canAnalizeCourseSchedule(CourseInstance courseInstance) {
		return canPerformCourseAdministration(courseInstance);
	}
	
	@Override
	public boolean canViewGroupTree(CourseInstance courseInstance, String relativePath) {
		Data d = local.get();
		if(d==null) throw new JCMSSManNotInitializedException();
		// Ako je admin:
		Set<String> roles = d.getRoles();
		if(roles.contains(JCMSSecurityConstants.ROLE_ADMIN)) {
			return true;
		}
		// Ako je osoblje kolegija:
		Set<String> inGroups = d.getCourseSecurityGroupsFor(courseInstance);
		if(inGroups.contains(JCMSSecurityConstants.ADMIN_KOLEGIJA)) return true;
		if(inGroups.contains(JCMSSecurityConstants.NOSITELJ)) return true;
		if(inGroups.contains(JCMSSecurityConstants.ASISTENT_ORG)) return true;
		if(inGroups.contains(JCMSSecurityConstants.ASISTENT)) return true;
		if(inGroups.contains(JCMSSecurityConstants.NASTAVNIK)) return true;
		// Inače:
		return false;
	}
	
	@Override
	public boolean canManageGroupEventsFor(CourseInstance courseInstance, Group group) {
		Data d = local.get();
		if(d==null) throw new JCMSSManNotInitializedException();
		// Ako je admin:
		Set<String> roles = d.getRoles();
		if(roles.contains(JCMSSecurityConstants.ROLE_ADMIN)) {
			return true;
		}
		// Ako je osoblje kolegija:
		Set<String> inGroups = d.getCourseSecurityGroupsFor(courseInstance);
		if(inGroups.contains(JCMSSecurityConstants.ADMIN_KOLEGIJA)) return true;
		if(inGroups.contains(JCMSSecurityConstants.NOSITELJ)) return true;
		if(inGroups.contains(JCMSSecurityConstants.ASISTENT_ORG)) return true;
		if(inGroups.contains(JCMSSecurityConstants.ASISTENT)) {
			if(group!=null && (group.getRelativePath().equals("6") || group.getRelativePath().startsWith("6/"))) return true;
			return false;
		}
		if(inGroups.contains(JCMSSecurityConstants.NASTAVNIK)) {
			if(group!=null && (group.getRelativePath().equals("6") || group.getRelativePath().startsWith("6/"))) return true;
			return false;
		}
		// Inače:
		return false;
	}
	
	@Override
	public boolean canCreatePrivateGroups(CourseInstance courseInstance) {
		Data d = local.get();
		if(d==null) throw new JCMSSManNotInitializedException();
		// Ako je admin:
		Set<String> roles = d.getRoles();
		if(roles.contains(JCMSSecurityConstants.ROLE_ADMIN)) {
			return true;
		}
		// Ako je osoblje kolegija:
		Set<String> inGroups = d.getCourseSecurityGroupsFor(courseInstance);
		if(inGroups.contains(JCMSSecurityConstants.ADMIN_KOLEGIJA)) return true;
		if(inGroups.contains(JCMSSecurityConstants.NOSITELJ)) return true;
		if(inGroups.contains(JCMSSecurityConstants.ASISTENT_ORG)) return true;
		if(inGroups.contains(JCMSSecurityConstants.ASISTENT)) return true;
		if(inGroups.contains(JCMSSecurityConstants.NASTAVNIK)) return true;
		// Inače:
		return false;
	}
	
	@Override
	public boolean canManageUserGroupMembership(CourseInstance courseInstance, Group group) {
		Data d = local.get();
		if(d==null) throw new JCMSSManNotInitializedException();
		if(group==null || courseInstance==null || group.getCompositeCourseID()==null || courseInstance.getId()==null) return false;
		if(!group.getCompositeCourseID().equals(courseInstance.getId())) return false;
		// Ako je admin:
		Set<String> roles = d.getRoles();
		if(roles.contains(JCMSSecurityConstants.ROLE_ADMIN)) {
			return true;
		}
		// Ako je osoblje kolegija:
		if(group.getRelativePath().equals("0") || group.getRelativePath().startsWith("0/") || group.getRelativePath().equals("3") || group.getRelativePath().startsWith("3/")) {
			// Sigurnosne grupe ne moze mijenjati bilo tko, kao niti pripadnost predmetu.
			return false;
		}
		Set<String> inGroups = d.getCourseSecurityGroupsFor(courseInstance);
		if(inGroups.contains(JCMSSecurityConstants.ADMIN_KOLEGIJA)) return true;
		if(inGroups.contains(JCMSSecurityConstants.NOSITELJ)) return true;
		if(inGroups.contains(JCMSSecurityConstants.ASISTENT_ORG)) return true;
		if(inGroups.contains(JCMSSecurityConstants.ASISTENT)) {
			if(group!=null && (group.getRelativePath().equals("6") || group.getRelativePath().startsWith("6/"))) return true;
			return false;
		}
		if(inGroups.contains(JCMSSecurityConstants.NASTAVNIK)) {
			if(group!=null && (group.getRelativePath().equals("6") || group.getRelativePath().startsWith("6/"))) return true;
			return false;
		}
		// Inače:
		return false;
	}
	
	
	@Override
	public List<User> listUsersForOwner(CourseInstance courseInstance, String parentGroupRelativePath) {
		Data d = local.get();
		if(d==null) throw new JCMSSManNotInitializedException();
		if(canPerformCourseAdministration(courseInstance)) {
			List<User> courseUsers = DAOHelperFactory.getDAOHelper().getUserDAO().listUsersOnCourseInstance(d.em, courseInstance.getId());
			return courseUsers;
		}
		List<GroupOwner> gowners = DAOHelperFactory.getDAOHelper().getGroupDAO().findForSubgroupsAndUser(d.em, courseInstance.getId(), parentGroupRelativePath, d.user);
		ArrayList<User> users = new ArrayList<User>(500);
		for(GroupOwner go : gowners) {
			for(UserGroup ug : go.getGroup().getUsers()) {
				users.add(ug.getUser());
			}
		}
		return users;
	}
	
	@Override
	public boolean canManageAssessments(CourseInstance courseInstance) {
		return canPerformCourseAdministration(courseInstance);
	}
	
	@Override
	public boolean canViewAssessments(CourseInstance courseInstance) {
		Data d = local.get();
		if(d==null) throw new JCMSSManNotInitializedException();
		// Ako je admin:
		Set<String> roles = d.getRoles();
		if(roles.contains(JCMSSecurityConstants.ROLE_ADMIN)) {
			return true;
		}
		// Ako je osoblje kolegija:
		Set<String> inGroups = d.getCourseSecurityGroupsFor(courseInstance);
		if(inGroups.contains(JCMSSecurityConstants.ADMIN_KOLEGIJA)) return true;
		if(inGroups.contains(JCMSSecurityConstants.NOSITELJ)) return true;
		if(inGroups.contains(JCMSSecurityConstants.NASTAVNIK)) return true;
		if(inGroups.contains(JCMSSecurityConstants.ASISTENT_ORG)) return true;
		// Inače:
		return false;
	}
	
	public boolean isUserStudentsLecturer(CourseInstance courseInstance, User student) {
		Data d = local.get();
		if(d==null) throw new JCMSSManNotInitializedException();
		if(!isStaffOnCourse(courseInstance)) return false;
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		List<UserGroup> ugList = dh.getGroupDAO().findUserGroupsForUser(d.em, courseInstance.getId(), "0", student);
		for(UserGroup ug : ugList) {
			GroupOwner gOwner = dh.getGroupDAO().getGroupOwner(d.em, ug.getGroup(), d.user);
			if(gOwner!=null) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean canManageAssessmentSchedule(CourseInstance courseInstance) {
		Data d = local.get();
		if(d==null) throw new JCMSSManNotInitializedException();
		// Ako je admin:
		Set<String> roles = d.getRoles();
		if(roles.contains(JCMSSecurityConstants.ROLE_ADMIN)) {
			return true;
		}
		// Ako je osoblje kolegija:
		Set<String> inGroups = d.getCourseSecurityGroupsFor(courseInstance);
		if(inGroups.contains(JCMSSecurityConstants.ADMIN_KOLEGIJA)) return true;
		if(inGroups.contains(JCMSSecurityConstants.NOSITELJ)) return true;
		if(inGroups.contains(JCMSSecurityConstants.ASISTENT_ORG)) return true;
		// Inače:
		return false;
	}
	
	@Override
	public boolean canUserManageRepository(CourseInstance courseInstance) {
		return canPerformCourseAdministration(courseInstance);
	}
	@Override
	public boolean canManageLectureGroupOwners(CourseInstance courseInstance) {
		Data d = local.get();
		if(d==null) throw new JCMSSManNotInitializedException();
		// Ako je admin:
		Set<String> roles = d.getRoles();
		if(roles.contains(JCMSSecurityConstants.ROLE_ADMIN)) {
			return true;
		}
		// Ako je osoblje kolegija:
		Set<String> inGroups = d.getCourseSecurityGroupsFor(courseInstance);
		if(inGroups.contains(JCMSSecurityConstants.ASISTENT_ORG)) return true;
		if(inGroups.contains(JCMSSecurityConstants.ADMIN_KOLEGIJA)) return true;
		if(inGroups.contains(JCMSSecurityConstants.NOSITELJ)) return true;
		// Inače:
		return false;
	}
	
	@Override
	public boolean canManageCourseMarketPlace(CourseInstance courseInstance, String relativePath) {
		Data d = local.get();
		if(d==null) throw new JCMSSManNotInitializedException();
		// Ako je admin:
		Set<String> roles = d.getRoles();
		if(roles.contains(JCMSSecurityConstants.ROLE_ADMIN)) {
			return true;
		}
		// Ako je osoblje kolegija:
		Set<String> inGroups = d.getCourseSecurityGroupsFor(courseInstance);
		if(inGroups.contains(JCMSSecurityConstants.ADMIN_KOLEGIJA)) return true;
		if(inGroups.contains(JCMSSecurityConstants.NOSITELJ)) return true;
		// Ako je to bila grupa za predavanja, onda smo gotovi:
		if(relativePath.equals("0")) return false;
		// Ostale grupe mogu uređivati i glavni asisistenti:
		if(inGroups.contains(JCMSSecurityConstants.ASISTENT_ORG)) return true;
		// Inače:
		return false;
	}
	
	@Override
	public List<MPRootInfoBean> getMarketPlacesForUser(CourseInstance courseInstance) {
		Data d = local.get();
		if(d==null) throw new JCMSSManNotInitializedException();
		List<Group> roots = DAOHelperFactory.getDAOHelper().getGroupDAO().listMarketPlaceRootGroupsForCourse(d.em, courseInstance);
		Set<Group> canAdminGroups = new HashSet<Group>(roots.size());
		Set<Group> canViewGroups = new HashSet<Group>(roots.size());
		for(Group g : roots) {
			if(canManageCourseMarketPlace(courseInstance, g.getRelativePath())) {
				canAdminGroups.add(g);
				canViewGroups.add(g);
			}
		}
		// Ako je admin:
		Set<String> roles = d.getRoles();
		if(roles.contains(JCMSSecurityConstants.ROLE_STUDENT)) {
			List<Group> usersGroups = DAOHelperFactory.getDAOHelper().getGroupDAO().findAllGroupsForUserOnCourse(d.em, courseInstance.getId(), d.user);
			for(Group g : usersGroups) {
				Group cur = g;
				int kontrolaDubine = 100; // Za slucaj da je roditelj spojen sam na sebe ili nesto ciklicki...
				while(cur != null && kontrolaDubine>0) {
					if(cur.isManagedRoot()) {
						canViewGroups.add(cur);
						break;
					}
					cur = cur.getParent();
					kontrolaDubine--;
				}
			}
		}
		Date now = new Date();
		List<MPRootInfoBean> beans = new ArrayList<MPRootInfoBean>(roots.size());
		for(Group g : roots) {
			if(!canViewGroups.contains(g)) continue;
			MPRootInfoBean b = new MPRootInfoBean();
			b.setId(g.getId());
			b.setCompositeCourseID(g.getCompositeCourseID());
			b.setRelativePath(g.getRelativePath());
			b.setName(g.getName());
			b.setCanManage(canAdminGroups.contains(g));
			boolean active = false;
			MarketPlace mp = g.getMarketPlace();
			if(mp!=null) {
				active = mp.isActive(now);
			}
			b.setActive(active);
			beans.add(b);
		}
		return beans;
	}

	@Override
	public boolean isStudentOnCourse(CourseInstance courseInstance) {
		Data d = local.get();
		if(d==null) throw new JCMSSManNotInitializedException();
		GroupData gd = d.getGroupsForMembership(courseInstance);
		if(gd.set.isEmpty()) return false;
		for(Group g : gd.set) {
			if(g.getRelativePath().startsWith("0/")) return true;
		}
		return false;
	}
	
	@Override
	public boolean canUserAccessCourse(CourseInstance courseInstance) {
		Data d = local.get();
		if(d==null) throw new JCMSSManNotInitializedException();
		// Ako je admin:
		Set<String> roles = d.getRoles();
		if(roles.contains(JCMSSecurityConstants.ROLE_ADMIN)) {
			return true;
		}
		// Ako je student; ispitujemo prije jer je ovo češći slučaj...
		GroupData gd = d.getGroupsForMembership(courseInstance);
		if(!gd.set.isEmpty()) return true;
		// Ako je osoblje kolegija:
		Set<String> inGroups = d.getCourseSecurityGroupsFor(courseInstance);
		if(inGroups.size()!=0) return true;
		// Inače:
		return false;
	}

	@Override
	public boolean isStaffOnCourse(CourseInstance courseInstance) {
		Data d = local.get();
		if(d==null) throw new JCMSSManNotInitializedException();
		// Ako je admin:
		Set<String> roles = d.getRoles();
		if(roles.contains(JCMSSecurityConstants.ROLE_ADMIN)) {
			return true;
		}
		// Ako je osoblje kolegija:
		Set<String> inGroups = d.getCourseSecurityGroupsFor(courseInstance);
		if(inGroups.size()!=0) return true;
		// Inače:
		return false;
	}

	@Override
	public boolean canManageCourseUsersList(CourseInstance courseInstance,
			String relativePath) {
		Data d = local.get();
		if(d==null) throw new JCMSSManNotInitializedException();
		// Ako relativna staza nije zadana:
		if(relativePath==null || relativePath.length()==0) return false;
		// Ako je admin:
		Set<String> roles = d.getRoles();
		if(roles.contains(JCMSSecurityConstants.ROLE_ADMIN)) {
			return true;
		}
		// Ako je "probrano" osoblje kolegija:
		Set<String> inGroups = d.getCourseSecurityGroupsFor(courseInstance);
		if(inGroups.contains(JCMSSecurityConstants.ADMIN_KOLEGIJA)) return true;
		if(inGroups.contains(JCMSSecurityConstants.NOSITELJ)) return true;
		if(inGroups.contains(JCMSSecurityConstants.ASISTENT_ORG)) return true;
		// Inače:
		return false;
	}

	@Override
	public boolean canModifyCoursePermission(CourseInstance courseInstance,
			String relativePath) {
		Data d = local.get();
		if(d==null) throw new JCMSSManNotInitializedException();
		// Ako relativna staza nije zadana:
		if(relativePath==null || relativePath.length()==0) return false;
		if(!relativePath.startsWith("3/")) return false;
		// Ako je admin:
		Set<String> roles = d.getRoles();
		if(roles.contains(JCMSSecurityConstants.ROLE_ADMIN)) {
			return true;
		}
		Set<String> inGroups = d.getCourseSecurityGroupsFor(courseInstance);
		for(int i = 0; i < JCMSSecurityConstants.ALL_COURSE_ROLES.length; i++) {
			if(inGroups.contains(JCMSSecurityConstants.ALL_COURSE_ROLES[i]) && courseSecurityModMatrix.get(JCMSSecurityConstants.ALL_COURSE_ROLES[i]).contains(relativePath)) return true;
		}
		return false;
	}
	
	@Override
	public boolean canObtainCourseUsersList(CourseInstance courseInstance, String relativePath) {
		Data d = local.get();
		if(d==null) throw new JCMSSManNotInitializedException();
		// Ako relativna staza nije zadana:
		if(relativePath==null || relativePath.length()==0) return false;
		// Ako je admin:
		Set<String> roles = d.getRoles();
		if(roles.contains(JCMSSecurityConstants.ROLE_ADMIN)) {
			return true;
		}
		// Ako je osoblje kolegija:
		Set<String> inGroups = d.getCourseSecurityGroupsFor(courseInstance);
		if(inGroups.size()!=0) return true;
		// Inače:
		return false;
	}

	@Override
	public boolean canObtainStaffList() {
		return canPerformCourseAdministration();
	}

	@Override
	public boolean canManageToDoTask(Long taskID, String param) {
		Data d = local.get();
		if(d==null) throw new JCMSSManNotInitializedException();
		// Ako je admin:
		Set<String> roles = d.getRoles();
		if(roles.contains(JCMSSecurityConstants.ROLE_ADMIN)) {
			return true;
		}
		
		ToDoTask task = DAOHelperFactory.getDAOHelper().getToDoListDAO().getSingleTask(d.em, taskID);
		if (task.getOwner().equals(d.user)) return true;
		if (param.equals("CLOSE") && task.getRealizer().equals(d.user)) return true; 
		if (param.equals("USE_TEMPLATE")){
			User owner = task.getOwner();
			Set<String> ownerRoles = new HashSet<String>(owner.getUserDescriptor().getRoles().size());
			for(Role r : owner.getUserDescriptor().getRoles()) ownerRoles.add(r.getName());
				
			if (roles.contains(JCMSSecurityConstants.ROLE_STUDENT) 
					&& ownerRoles.contains(JCMSSecurityConstants.ROLE_STUDENT))
				return true;
			if(!roles.contains(JCMSSecurityConstants.ROLE_STUDENT) 
					&& !ownerRoles.contains(JCMSSecurityConstants.ROLE_STUDENT))
					return true;
		}
		return false;
	}
	
	@Override
	public boolean canAssignToDoTaskToUser(User user){
		Data d = local.get();
		if(d==null) throw new JCMSSManNotInitializedException();
		// Ako je admin:
		Set<String> roles = d.getRoles();
		if(roles.contains(JCMSSecurityConstants.ROLE_ADMIN)) {
			return true;
		}
		if(roles.contains(JCMSSecurityConstants.ROLE_STUDENT) &&
			!user.equals(d.user)) return false;
		
		return true;
	}
	
	@Override
	public List<CourseInstance> getCourseAdministrationList(YearSemester ysem) {
		Data d = local.get();
		if(d==null) throw new JCMSSManNotInitializedException();
		Set<String> roles = d.getRoles();
		if(roles.contains(JCMSSecurityConstants.ROLE_ADMIN)) {
			return DAOHelperFactory.getDAOHelper().getCourseInstanceDAO().findForSemester(d.em, ysem.getId());
		}
		return DAOHelperFactory.getDAOHelper().getCourseInstanceDAO().findForCourseStaff(d.em, ysem, d.user);
	}

	@Override
	public boolean canChangeGroup(CourseInstance courseInstance, String parentGroupRelativePath) {
		Data d = local.get();
		if(d==null) throw new JCMSSManNotInitializedException();
		Set<String> roles = d.getRoles();
		if(roles.contains(JCMSSecurityConstants.ROLE_ADMIN)) return true;
		Set<String> inGroups = d.getCourseSecurityGroupsFor(courseInstance);
		if(inGroups.contains(JCMSSecurityConstants.ADMIN_KOLEGIJA)) return true;
		if(inGroups.contains(JCMSSecurityConstants.ASISTENT_ORG)) return true;
		if(inGroups.contains(JCMSSecurityConstants.NOSITELJ)) return true;
		return false;
	}

	public List<Group> listAccessibleGroups(CourseInstance courseInstance, String relativePath) {
		Data d = local.get();
		if(d==null) throw new JCMSSManNotInitializedException();
		List<Group> groupsList = DAOHelperFactory.getDAOHelper().getGroupDAO().findSubgroupsLLE(d.em, courseInstance.getId(), relativePath, relativePath+"/%");
		Set<String> roles = d.getRoles();
		// Ako je admin, nema potrebe za daljnjim filtriranjem...
		if(roles.contains(JCMSSecurityConstants.ROLE_ADMIN)) return groupsList;
		// Ako je na kolegiju u potrebnim grupama, opet prosljedi sve...
		Set<String> inGroups = d.getCourseSecurityGroupsFor(courseInstance);
		if(inGroups.contains(JCMSSecurityConstants.ADMIN_KOLEGIJA) || inGroups.contains(JCMSSecurityConstants.ASISTENT_ORG) || inGroups.contains(JCMSSecurityConstants.NOSITELJ)) return groupsList;
		// Inace, ajmo pogledati kojim je grupama on vlasnik:
		GroupData gd = d.getOwnedCourseGroupsFor(courseInstance);
		Iterator<Group> it = groupsList.iterator();
		while(it.hasNext()) {
			Group g = it.next();
			// Ako je on vlasnik te grupe:
			if(gd.set.contains(g)) continue;
			// Inace makni tu grupu! 
			it.remove();
		}
		return groupsList;
	}
	
	/* (non-Javadoc)
	 * @see hr.fer.zemris.jcms.security.IJCMSSecurityManager#getAccessibleGroupTree(hr.fer.zemris.jcms.model.CourseInstance)
	 * Ova metoda je ZLA. Iskorijeniti njenu uporabu.
	 */
	@Override @Deprecated
	public Tree<Group, GroupPermissions> getAccessibleGroupTree(CourseInstance ci, String relativePath) {
		Data d = local.get();
		if(d==null) throw new JCMSSManNotInitializedException();
		Tree<Group, GroupPermissions> tree = new Tree<Group, GroupPermissions>();
		Group g = DAOHelperFactory.getDAOHelper().getGroupDAO().get(d.em, ci.getId(), relativePath);
		if(g==null) return tree;
		if(g.getRelativePath().equals("3") || g.getRelativePath().startsWith("3/")) return tree;
		ArrayList<Group> pathStart = new ArrayList<Group>();
		for(Group gg = g; gg!=null; gg=gg.getParent()) {
			pathStart.add(gg);
		}
		GroupPermissions[] gpermArray = new GroupPermissions[3];
		// -- ne vidi evente niti popis
		gpermArray[0] = new GroupPermissions(false, false, false, false);
		// -- vidi samo popis
		gpermArray[1] = new GroupPermissions(false, false, false, true);
		// -- vidi i evente i popis
		gpermArray[2] = new GroupPermissions(true, true, true, true);
		Collections.reverse(pathStart);
		if(canPerformCourseAdministration(ci)) {
			// Vidi sve podgrupe; izgradi po principu top-down
			if(!g.getRelativePath().equals("3") && !g.getRelativePath().startsWith("3/")) {
				tree.addPath(new TreePath<Group>(pathStart, true), null);
				recursivelyAddSubgroups(tree, pathStart, g.getSubgroups(), gpermArray);
			}
			recursivelyFixGroupPermissions(tree.getChildren(), gpermArray, -1);
		} else {
			boolean priv = canUsePrivateGroups(ci);
			Group privGroupRoot = null;
			if(priv) {
				for(Group gr : ci.getPrimaryGroup().getSubgroups()) {
					if(gr.getRelativePath().equals("6")) {
						privGroupRoot = gr;
						break;
					}
				}
			}
			// Ovo je klasican asistent koji ima nesto svojih grupa...
			// izgradi po principu bottom-up
			GroupData gd = d.getOwnedCourseGroupsFor(ci);
			if(privGroupRoot!=null) {
				gd.add(privGroupRoot);
			}
			String gStart = g.getRelativePath().isEmpty() ? "" : g.getRelativePath()+"/";
			for(Group ggg : gd.set) {
				if(!ggg.getRelativePath().equals(g.getRelativePath()) && !ggg.getRelativePath().startsWith(gStart)) {
					continue;
				}
				pathStart.clear();
				for(Group gg = ggg; gg!=null; gg=gg.getParent()) {
					pathStart.add(gg);
				}
				Collections.reverse(pathStart);
				tree.addPath(new TreePath<Group>(pathStart, true), null);
			}
			recursivelyFixGroupPermissions(tree.getChildren(), gpermArray, -1);
		}
		tree.sort(StringUtil.GROUP_COMPARATOR);
		return tree;
	}

	@Override
	public GroupSupportedPermission getGroupPermissionFor(CourseInstance ci, Group g) {
		Data d = local.get();
		if(d==null) throw new JCMSSManNotInitializedException();
		// Ako ta grupa uopće nije na predmetu, korisnik ne može ama baš ništa; 
		// netko tu pokušava nešto mutno...
		if(!g.getCompositeCourseID().equals(ci.getId())) {
			return new GroupSupportedPermission();
		}
		// Inače dohvati dozvole za sve grupe, pa pogledaj za traženu
		GroupSupportedPermission p = d.getGroupPermissionsOnCourse(ci).get(g);
		// Null u principu ne bi smjelo biti, no bolje spriječiti, nego da se raspadne :-)
		if(p==null) {
			return new GroupSupportedPermission();
		}
		return p;
	}
	
	@Override
	public Tree<Group, GroupSupportedPermission> getAccessibleGroupTree(CourseInstance ci) {
		Data d = local.get();
		if(d==null) throw new JCMSSManNotInitializedException();
		Tree<Group, GroupSupportedPermission> tree = new Tree<Group, GroupSupportedPermission>();
		Map<Group,GroupSupportedPermission> perms = d.getGroupPermissionsOnCourse(ci);
		
		Group g = ci.getPrimaryGroup();
		ArrayList<Group> pathStart = new ArrayList<Group>();
		pathStart.add(g);
		tree.addPath(new TreePath<Group>(pathStart, true), perms.get(g));

		recursivelyAddSubgroups(tree, pathStart, g.getSubgroups(), perms);
		
		tree.sort(StringUtil.GROUP_COMPARATOR);
		return tree;
	}

	private void recursivelyFixGroupPermissions(List<TreeNode<Group, GroupPermissions>> nodeList, GroupPermissions[] gpermArray, int marketPlacePassed) {
		for(TreeNode<Group, GroupPermissions> node : nodeList) {
			int currentMarketPlacePassed = marketPlacePassed;
			// Ako sam na burzi, reci sljedećima da su iza
			if(node.getElement().isManagedRoot()) {
				currentMarketPlacePassed = 0;
				marketPlacePassed = 1;
			}
			if(currentMarketPlacePassed==-1) {
				node.setData(gpermArray[0]);
			} else if(currentMarketPlacePassed==0) {
				node.setData(gpermArray[1]);
			} else {
				node.setData(gpermArray[2]);
			}
			if(!node.getChildren().isEmpty()) {
				recursivelyFixGroupPermissions(node.getChildren(), gpermArray, marketPlacePassed);
				//if(node.getElement().getRelativePath().equals("0")) {
				//	node.setData(gpermArray[1]);
				//} else if(node.getLevel()>=2) {
				//	node.setData(gpermArray[1]);
				//}
			}
		}
	}

	private void recursivelyAddSubgroups(Tree<Group, GroupPermissions> tree, ArrayList<Group> pathStart, Set<Group> subgroups, GroupPermissions[] gpermArray) {
		for(Group g : subgroups) {
			if(!g.getRelativePath().equals("3") && !g.getRelativePath().startsWith("3/")) {
				pathStart.add(g);
				tree.addPath(new TreePath<Group>(pathStart, true), g.getSubgroups().isEmpty() ? gpermArray[2] : null);
				recursivelyAddSubgroups(tree, pathStart, g.getSubgroups(), gpermArray);
				pathStart.remove(pathStart.size()-1);
			}
		}
	}

	private void recursivelyAddSubgroups(Tree<Group, GroupSupportedPermission> tree, ArrayList<Group> pathStart, Set<Group> subgroups, Map<Group,GroupSupportedPermission> perms) {
		for(Group g : subgroups) {
			GroupSupportedPermission p = perms.get(g);
			if(!p.getCanView()) continue;
			pathStart.add(g);
			tree.addPath(new TreePath<Group>(pathStart, true), p);
			recursivelyAddSubgroups(tree, pathStart, g.getSubgroups(), perms);
			pathStart.remove(pathStart.size()-1);
		}
	}

	private ThreadLocal<Data> local = new ThreadLocal<Data>();
	
	public void init(User user, EntityManager em) {
		Data d = local.get();
		if(d==null) {
			d = new Data();
			local.set(d);
		}
		d.user = user;
		d.em = em;
	}

	public void close() {
		local.remove();
	}
	
	/**
	 * Ovaj razred je privremeni cache potrebnih informacija za trenutnog korisnika, i na temelju njega se donose sve odluke.
	 * Razred se puni podacima po potrebi, pa je stoga vazno da se njegove metode pozivaju samo unutar aktivnog entity
	 * managera!
	 * 
	 * @author marcupic
	 */
	private static class Data {
		User user;
		EntityManager em;
		Map<CourseInstance, Set<String>> courseSecurityGroups;
		Map<CourseInstance, GroupData> ownerCourseOfGroups;
		Set<String> roles;
		Map<CourseInstance, GroupData> memberOfCourseGroups;
		Map<CourseInstance, Map<Group, GroupSupportedPermission>> groupPermissionsOnCourse;
		Map<CourseInstance, Map<String, CourseInstanceKeyValue>> ciKeyValues;

		public String getCourseInstanceKeyValue(CourseInstance courseInstance, String name) {
			if(courseInstance==null || name==null) return null;
			if(ciKeyValues==null) {
				ciKeyValues = new HashMap<CourseInstance, Map<String,CourseInstanceKeyValue>>();
			}
			Map<String, CourseInstanceKeyValue> map = ciKeyValues.get(courseInstance);
			if(map == null) {
				map = new HashMap<String, CourseInstanceKeyValue>();
				ciKeyValues.put(courseInstance, map);
			}
			CourseInstanceKeyValue cikv = map.get(name);
			if(cikv==null) {
				// Ako je vraćen null, i ako je to stvarno upisano, zavrsi
				if(map.containsKey(name)) return null;
				// Inace imamo null jer tog mappinga trenutno nema; dohvati ga!
				cikv = DAOHelperFactory.getDAOHelper().getCourseInstanceKeyValueDAO().get(em, courseInstance, name);
				map.put(name, cikv);
			}
			return cikv==null ? null : cikv.getValue();
		}

		/**
		 * Konačno sve dozvole za grupe kolegija sređene na jednom mjestu.
		 * 
		 * @param ci kolegij
		 * @return mapa dozvola; ključ je identifikator grupe
		 */
		Map<Group, GroupSupportedPermission> getGroupPermissionsOnCourse(CourseInstance ci) {
			if(groupPermissionsOnCourse==null) {
				groupPermissionsOnCourse = new HashMap<CourseInstance, Map<Group,GroupSupportedPermission>>();
			}
			Map<Group, GroupSupportedPermission> perms = groupPermissionsOnCourse.get(ci);
			if(perms!=null) return perms;
			List<Group> lista = DAOHelperFactory.getDAOHelper().getGroupDAO().findSubgroups(em, ci.getId(), "%");
			perms = new HashMap<Group, GroupSupportedPermission>(2*lista.size());
			for(Group g : lista) {
				perms.put(g, new GroupSupportedPermission());
			}

			// Određivanje položaja u odnosu na burzu.
			for(Group g : lista) {
				GroupSupportedPermission p = perms.get(g);
				String rp = g.getRelativePath();
				if(rp.isEmpty()) {
					p.setPlacement(MarketPlacePlacement.BEFORE_MARKET_PLACE);
					continue;
				}
				if(rp.equals("0") || rp.equals("3")) {
					p.setPlacement(MarketPlacePlacement.IS_MARKET_PLACE);
					continue;
				}
				if(rp.startsWith("0/") || rp.equals("3/")) {
					p.setPlacement(MarketPlacePlacement.AFTER_MARKET_PLACE);
					continue;
				}
				char[] c = rp.toCharArray();
				int broj = 0;
				for(int i = 0; i < c.length; i++) {
					if(c[i]=='/') broj++;
				}
				if(broj==0) {
					p.setPlacement(MarketPlacePlacement.BEFORE_MARKET_PLACE);
					continue;
				}
				if(broj==1) {
					p.setPlacement(MarketPlacePlacement.IS_MARKET_PLACE);
					continue;
				}
				if(broj>1) {
					p.setPlacement(MarketPlacePlacement.AFTER_MARKET_PLACE);
					continue;
				}
			}

			// Ako može administrirati:
			Set<String> roles = getRoles();
			boolean admin = roles.contains(JCMSSecurityConstants.ROLE_ADMIN);
			// Ako je osoblje kolegija:
			Set<String> inGroups = getCourseSecurityGroupsFor(ci);
			boolean courseAdmin = inGroups.contains(JCMSSecurityConstants.ADMIN_KOLEGIJA);
			boolean nositelj = inGroups.contains(JCMSSecurityConstants.NOSITELJ);
			boolean organizator = inGroups.contains(JCMSSecurityConstants.ASISTENT_ORG);
			boolean nastavnik = inGroups.contains(JCMSSecurityConstants.NASTAVNIK);
			boolean asistent = inGroups.contains(JCMSSecurityConstants.ASISTENT);
			GroupData owned = getOwnedCourseGroupsFor(ci);
			
			// Uredi dozvole
			for(Group g : lista) {
				boolean owner = owned.set.contains(g);
				GroupSupportedPermission p = perms.get(g);
				// Grupu 3 ne vidi nitko!
				if(g.getRelativePath().equals("3") || g.getRelativePath().startsWith("3/")) {
					p.setCanEdit(false);
					p.setCanManageEvents(false);
					p.setCanAddSubgroups(false);
					p.setCanDelete(false);
					p.setCanManageUsers(false);
					p.setCanViewEvents(false);
					p.setCanView(false);
					p.setCanViewUsers(false);
					continue;
				}
				// Grupu za predavanja:
				if(g.getRelativePath().equals("0")) {
					p.setCanEdit(admin);
					p.setCanManageEvents(false);
					p.setCanAddSubgroups(admin);
					p.setCanDelete(false);
					p.setCanManageUsers(false);
					p.setCanViewEvents(false);
					// Jednom postavljena vidljivost ne moze se srusiti! Ocito sam negdje imao dovoljno
					// informacija da je postavim ako je bila postavljena
					p.setCanView(p.getCanView() || admin || courseAdmin || nositelj || organizator);
					if(p.getCanView() && g.getParent()!=null) ensureParentVisibility(g.getParent(), perms);
					p.setCanViewUsers(admin || courseAdmin || nositelj || organizator);
					continue;
				}
				if(g.getRelativePath().startsWith("0/")) {
					p.setCanEdit(admin);
					p.setCanViewEvents(true);
					p.setCanManageEvents(admin || courseAdmin || nositelj || organizator || owner);
					p.setCanView(p.getCanView() || admin || courseAdmin || nositelj || organizator || owner);
					if(p.getCanView() && g.getParent()!=null) ensureParentVisibility(g.getParent(), perms);
					p.setCanAddSubgroups(false);
					p.setCanDelete(admin);
					p.setCanViewUsers(admin || courseAdmin || nositelj || organizator || owner);
					p.setCanManageUsers(admin);
					p.setCanViewGroupOwners(true);
					p.setCanManageGroupOwners(admin || courseAdmin || nositelj || organizator);
					continue;
				}
				// Grupe za labose, domaće zadaće, ispite i seminare; to su trorazinske grupe
				if(g.getRelativePath().equals("1") || g.getRelativePath().equals("2") || g.getRelativePath().equals("4") || g.getRelativePath().equals("5")) {
					p.setCanEdit(admin || organizator);
					p.setCanManageEvents(false);
					p.setCanAddSubgroups(false);
					p.setCanDelete(false);
					p.setCanManageUsers(false);
					p.setCanViewEvents(false);
					// Jednom postavljena vidljivost ne moze se srusiti! Ocito sam negdje imao dovoljno
					// informacija da je postavim ako je bila postavljena
					p.setCanView(p.getCanView() || admin || courseAdmin || nositelj || organizator || owner);
					if(p.getCanView() && g.getParent()!=null) ensureParentVisibility(g.getParent(), perms);
					p.setCanViewUsers(false);
					continue;
				}
				if(g.getRelativePath().startsWith("1/") || g.getRelativePath().startsWith("2/") || g.getRelativePath().startsWith("4/") || g.getRelativePath().startsWith("5/")) {
					if(p.getPlacement()==MarketPlacePlacement.AFTER_MARKET_PLACE) {
						p.setCanEdit(admin || organizator || nositelj);
						p.setCanViewEvents(true);
						p.setCanManageEvents(admin || courseAdmin || nositelj || organizator);
						p.setCanView(p.getCanView() || admin || courseAdmin || nositelj || organizator || owner);
						if(p.getCanView() && g.getParent()!=null) ensureParentVisibility(g.getParent(), perms);
						p.setCanAddSubgroups(false);
						p.setCanDelete(admin || courseAdmin || nositelj || organizator);
						p.setCanViewUsers(admin || courseAdmin || nositelj || organizator || owner);
						p.setCanManageUsers(admin || courseAdmin || nositelj || organizator);
						p.setCanViewGroupOwners(true);
						p.setCanManageGroupOwners(admin || courseAdmin || nositelj || organizator);
						continue;
					}
					// Ovo tehnicki nije moguce; robusnosti radi:
					if(p.getPlacement()==MarketPlacePlacement.BEFORE_MARKET_PLACE) {
						p.setCanEdit(admin);
						p.setCanViewEvents(false);
						p.setCanManageEvents(false);
						p.setCanView(p.getCanView() || admin || courseAdmin || nositelj || organizator || owner);
						if(p.getCanView() && g.getParent()!=null) ensureParentVisibility(g.getParent(), perms);
						p.setCanAddSubgroups(false);
						p.setCanDelete(false);
						p.setCanViewUsers(false);
						p.setCanManageUsers(false);
						continue;
					}
					if(p.getPlacement()==MarketPlacePlacement.IS_MARKET_PLACE) {
						p.setCanEdit(admin);
						p.setCanViewEvents(false);
						p.setCanManageEvents(false);
						p.setCanView(p.getCanView() || admin || courseAdmin || nositelj || organizator || owner);
						if(p.getCanView() && g.getParent()!=null) ensureParentVisibility(g.getParent(), perms);
						p.setCanAddSubgroups(admin || courseAdmin || nositelj || organizator);
						p.setCanDelete(false);
						p.setCanViewUsers(admin || courseAdmin || nositelj || organizator || owner);
						p.setCanManageUsers(admin);
						continue;
					}
				}
				// Privatne grupe:
				if(g.getRelativePath().equals("6")) {
					p.setCanEdit(admin);
					p.setCanManageEvents(false);
					p.setCanAddSubgroups(admin || courseAdmin || nositelj || organizator || nastavnik || asistent);
					p.setCanDelete(false);
					p.setCanManageUsers(false);
					p.setCanViewEvents(false);
					// Jednom postavljena vidljivost ne moze se srusiti! Ocito sam negdje imao dovoljno
					// informacija da je postavim ako je bila postavljena
					p.setCanView(p.getCanView() || admin || courseAdmin || nositelj || organizator || nastavnik || asistent || owner);
					if(p.getCanView() && g.getParent()!=null) ensureParentVisibility(g.getParent(), perms);
					p.setCanViewUsers(false);
					continue;
				}
				if(g.getRelativePath().startsWith("6/")) {
					if(p.getPlacement()==MarketPlacePlacement.IS_MARKET_PLACE) {
						p.setCanEdit(admin || courseAdmin || nositelj || organizator || owner);
						p.setCanViewEvents(false);
						p.setCanManageEvents(false);
						p.setCanView(p.getCanView() || admin || courseAdmin || nositelj || organizator || nastavnik || asistent || owner);
						if(p.getCanView() && g.getParent()!=null) ensureParentVisibility(g.getParent(), perms);
						p.setCanAddSubgroups(admin || courseAdmin || nositelj || organizator || nastavnik || asistent || owner);
						p.setCanDelete(admin || courseAdmin || nositelj || organizator || owner);
						p.setCanViewUsers(admin || courseAdmin || nositelj || organizator || owner);
						p.setCanManageUsers(false);
						continue;
					}
					if(p.getPlacement()==MarketPlacePlacement.AFTER_MARKET_PLACE) {
						boolean ownerOfParent = getOwnedCourseGroupsFor(ci).set.contains(g.getParent());
						p.setCanEdit(admin || courseAdmin || nositelj || organizator || owner || ownerOfParent);
						p.setCanViewEvents(true);
						p.setCanManageEvents(true);
						p.setCanView(p.getCanView() || admin || courseAdmin || nositelj || organizator || owner || ownerOfParent);
						if(p.getCanView() && g.getParent()!=null) ensureParentVisibility(g.getParent(), perms);
						p.setCanAddSubgroups(false);
						p.setCanDelete(admin || courseAdmin || nositelj || organizator || owner || ownerOfParent);
						p.setCanViewUsers(admin || courseAdmin || nositelj || organizator || owner || ownerOfParent); // owner ove grupe ili owner parenta; treba i jedno i drugo!!! 
						p.setCanManageUsers(admin || courseAdmin || nositelj || organizator || owner || ownerOfParent);
						p.setCanViewGroupOwners(true);
						p.setCanManageGroupOwners(admin || courseAdmin || nositelj || organizator || owner || ownerOfParent);
						continue;
					}
				}
			}
			return perms;
		}
		
		/**
		 * Pomoćna metoda koja će vidljivost propagirati sve do korijena (i samo vidljivost).
		 * @param parent grupa
		 * @param perms mapa dozvola za sve postojeće grupe
		 */
		private void ensureParentVisibility(Group parent, Map<Group, GroupSupportedPermission> perms) {
			while(parent!=null) {
				GroupSupportedPermission p = perms.get(parent);
				// Ako sam naletio na vidljivu, ispred su sigurno sve vidljive pa sam gotov
				if(p.getCanView()) return;
				p.setCanView(true);
				parent = parent.getParent();
			}
		}

		Set<String> getRoles() {
			if(roles!=null) return roles;
			if(user==null) {
				roles = Collections.emptySet();
				return roles;
			}
			roles = new HashSet<String>(user.getUserDescriptor().getRoles().size());
			for(Role r : user.getUserDescriptor().getRoles()) {
				roles.add(r.getName());
			}
			return roles;
		}
		
		Set<String> getCourseSecurityGroupsFor(CourseInstance courseInstance) {
			if(courseInstance==null) throw new NullPointerException();
			if(courseSecurityGroups==null) courseSecurityGroups = new HashMap<CourseInstance, Set<String>>();
			Set<String> set = courseSecurityGroups.get(courseInstance);
			if(set==null) {
				List<Group> list = DAOHelperFactory.getDAOHelper().getGroupDAO().findSubGroupsForUser(em, courseInstance.getId(), "3", user);
				set = new HashSet<String>(list.size());
				for(int i = 0; i < list.size(); i++) {
					set.add(list.get(i).getRelativePath());
				}
				courseSecurityGroups.put(courseInstance, set);
			}
			return set;
		}

		
		GroupData getOwnedCourseGroupsFor(CourseInstance courseInstance) {
			if(courseInstance==null) throw new NullPointerException();
			if(ownerCourseOfGroups==null) ownerCourseOfGroups = new HashMap<CourseInstance, GroupData>();
			GroupData gd = ownerCourseOfGroups.get(courseInstance);
			if(gd==null) {
				List<Group> list;
				if(user!=null) {
					list = DAOHelperFactory.getDAOHelper().getGroupDAO().findGroupsOwnedBy(em, courseInstance.getId(), user);
				} else {
					list = Collections.emptyList();
				}
				gd = new GroupData(list);
				ownerCourseOfGroups.put(courseInstance, gd);
			}
			return gd;
		}

		private GroupData initGroupMembership(CourseInstance courseInstance) {
			List<Group> list;
			if(user!=null) {
				list = DAOHelperFactory.getDAOHelper().getGroupDAO().findAllGroupsForUserOnCourse(em, courseInstance.getId(), user);
			} else {
				list = Collections.emptyList();
			}
			GroupData gd = new GroupData(list);
			memberOfCourseGroups.put(courseInstance, gd);
			return gd;
		}
		
		GroupData getGroupsForMembership(CourseInstance courseInstance) {
			if(courseInstance==null) throw new NullPointerException();
			if(memberOfCourseGroups==null) memberOfCourseGroups = new HashMap<CourseInstance, GroupData>(50);
			GroupData gd = memberOfCourseGroups.get(courseInstance);
			if(gd==null) {
				gd = initGroupMembership(courseInstance);
			}
			return gd;
		}

	}
	
	private static class GroupData {
		Set<Group> set;
		Map<Long,Group> map;
		Map<String,Group> map2;
		
		public GroupData(List<Group> list) {
			set = new HashSet<Group>(list.size());
			map = new HashMap<Long, Group>(list.size());
			map2 = new HashMap<String, Group>(list.size());
			for(int i = 0; i < list.size(); i++) {
				Group g = list.get(i);
				set.add(g);
				map.put(g.getId(), g);
				map2.put(g.getRelativePath(), g);
			}
		}
		
		public void add(Group g) {
			set.add(g);
			map.put(g.getId(), g);
			map2.put(g.getRelativePath(), g);
		}
	}
	
	public boolean canViewHiddenForum(CourseInstance courseInstance) {
		if (courseInstance != null)
			return canPerformCourseAdministration(courseInstance);
		else
			return canPerformSystemAdministration();
	}
	
	@Override
	public boolean canViewCategory(Category category, CourseInstance courseInstance) {	
		if (category.isHidden()) {
			return canViewHiddenForum(courseInstance);
		} else {		
			if (courseInstance != null)
				return canUserAccessCourse(courseInstance);
			else
				return true;
		}
	}
	
	@Override
	public boolean canCreateCourseCategory(CourseInstance courseInstance) {
		if (courseInstance != null)
			return canPerformCourseAdministration(courseInstance);
		else
			return canPerformSystemAdministration();
		
	}
	
	@Override
	public boolean canCreateNonCourseCategory() {
		return canPerformSystemAdministration();
	}
	
	@Override
	public boolean canEditCategory(Category category) {
		return canPerformSystemAdministration();
	}
	
	@Override
	public boolean canViewSubforum(Subforum subforum, CourseInstance courseInstance) {
		if (!canViewCategory(subforum.getCategory(), courseInstance))
			return false;
		else if (subforum.isHidden())
			return canViewHiddenForum(courseInstance);
		else
			return true;
	}
	
	@Override
	public boolean canEditSubforum(Category category, CourseInstance courseInstance) {
		if (category.isClosed())
			return false;
		else if (courseInstance != null)
			return canPerformCourseAdministration();
		else
			return canPerformSystemAdministration();
	}
	
	@Override
	public boolean canViewTopic(Topic topic, CourseInstance courseInstance) {
		if (!canViewSubforum(topic.getSubforum(), courseInstance))
			return false;
		else
			return true;
	}
	
	@Override
	public boolean canCreateTopic(Subforum subforum, CourseInstance courseInstance) {
		if (subforum.isClosed() || subforum.getCategory().isClosed())
			return false;
		else if (courseInstance != null)
			return canUserAccessCourse(courseInstance);
		else
			return true;
	}
	
	@Override
	public boolean canEditTopic(Subforum subforum, CourseInstance courseInstance) {
		if (subforum.isClosed() || subforum.getCategory().isClosed())
			return false;
		else if (courseInstance != null)
			return canPerformCourseAdministration(courseInstance);
		else
			return canPerformSystemAdministration();
	}
	
	@Override
	public boolean canEditOthersPost(Topic topic, CourseInstance courseInstance) {
		if (topic.getSubforum().isClosed() || topic.getSubforum().getCategory().isClosed())
			return false;
		else if (courseInstance != null)
			return canPerformCourseAdministration(courseInstance);
		else
			return canPerformSystemAdministration();
	}
	
	@Override
	public boolean canCreatePost(Topic topic, CourseInstance courseInstance) {
		if (topic.isClosed() || topic.getSubforum().isClosed() || topic.getSubforum().getCategory().isClosed())
			return false;
		else if (courseInstance != null)
			return canUserAccessCourse(courseInstance);
		else
			return true;
	}

	//ITS - BEGIN
	@Override
	public boolean canManageIssueTopics(String courseInstanceID) {
		Data d = local.get();
		if(d==null) throw new JCMSSManNotInitializedException();
		Set<String> roles = d.getRoles();
		if(!canUserAccessCourse(DAOHelperFactory.getDAOHelper().getCourseInstanceDAO().get(d.em, courseInstanceID))) return false;
		if(roles.contains(JCMSSecurityConstants.ROLE_STUDENT)) return false;
		return true;
	}

	@Override
	public boolean canViewIssue(Long issueID) {
		Data d = local.get();
		Set<String> roles = d.getRoles();
		Issue issue = DAOHelperFactory.getDAOHelper().getIssueTrackingDAO().get(d.em, issueID);
		if(!canUserAccessCourse(issue.getTopic().getCourseInstance())) return false;
		if(issue.isDeclaredPublic()) return true;
		else if(!roles.contains(JCMSSecurityConstants.ROLE_STUDENT)) return true;
		else if(issue.getUser().equals(d.user)) return true;
		else return false;
	}

	@Override
	public boolean canCreateIssue(String courseInstanceID) {
		Data d = local.get();
		Set<String> roles = d.getRoles();
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		if(!canUserAccessCourse(dh.getCourseInstanceDAO().get(d.em, courseInstanceID))) return false;
		List<IssueTopic> topics = dh.getIssueTrackingDAO().listCourseTopics(d.em, courseInstanceID, "ACTIVE_TOPICS_ONLY");
		if(topics==null) topics = new ArrayList<IssueTopic>();
		if(roles.contains(JCMSSecurityConstants.ROLE_STUDENT) && topics.size()>0) return true;
		else return false;
	}

	@Override
	public String canChangeIssueStatus(Long issueID) {
		//True ako je korisnik student i vlasnik pitanja ili ako je korisnik asistent
		Data d = local.get();
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		Issue issue = dh.getIssueTrackingDAO().get(d.em, issueID);
		if(!canUserAccessCourse(issue.getTopic().getCourseInstance())) return "NO_PERMISSION";
		Set<String> roles = d.getRoles();
		//Ako je student i vlasnik 
		if(roles.contains(JCMSSecurityConstants.ROLE_STUDENT) && issue.getUser().getId().equals(d.user.getId())) return "STUDENT";
		//Inače Ako je student, a nije vlasnik
		else if (roles.contains(JCMSSecurityConstants.ROLE_STUDENT)) return "NO_PERMISSION";
		//Inače, ako nije student
		else return "ASISTENT";
	}

	@Override
	public boolean canSendAnswerToIssue(Long issueID) {
		String result = this.canChangeIssueStatus(issueID);
		if(result.equals("STUDENT") || result.equals("ASISTENT")) return true;
		else return false;
	}

	@Override
	public boolean canPostponeIssue(Long issueID) {
		Data d = local.get();
		Set<String> roles = d.getRoles();
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		Issue issue = dh.getIssueTrackingDAO().get(d.em, issueID);
		if(!canUserAccessCourse(issue.getTopic().getCourseInstance())) return false;
		if(roles.contains(JCMSSecurityConstants.ROLE_STUDENT)) return false;
		if(issue.getStatus().equals(IssueStatus.NEW)) return false;
		return true;
	}

	@Override
	public boolean canChangeIssuePublicity(String courseInstanceID) {
		return canManageIssueTopics(courseInstanceID);
	}

	@Override
	public boolean canViewStudentsJMBAG(String courseInstanceID) {
		return canManageIssueTopics(courseInstanceID);
	}

	@Override
	public String canViewIssueList(String courseInstanceID) {
		//True ako je korisnik student i vlasnik pitanja ili ako je korisnik asistent
		Data d = local.get();
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		CourseInstance ci = dh.getCourseInstanceDAO().get(d.em, courseInstanceID);
		if(!canUserAccessCourse(ci)) return "NO_PERMISSION";
		Set<String> roles = d.getRoles();
		if(roles.contains(JCMSSecurityConstants.ROLE_STUDENT)) return "STUDENT";
		return "ASISTENT";
	}

	@Override
	public boolean canCloseIssue(String courseInstanceID) {
		Data d = local.get();
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		CourseInstance ci = dh.getCourseInstanceDAO().get(d.em, courseInstanceID);
		if(!canUserAccessCourse(ci)) return false;
		Set<String> roles = d.getRoles();
		if(roles.contains(JCMSSecurityConstants.ROLE_STUDENT)) return false;
		return true;

	}

	@Override
	public Set<User> getIssueActivityReceivers(String courseInstanceID) {
		Set<User> result = new HashSet<User>();
		//dohvat osoblja na kolegiju
		Data d = local.get();
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		//Nositelji
		List<User> userList = dh.getGroupDAO().listUsersInGroupTree(d.em, courseInstanceID, JCMSSecurityConstants.NOSITELJ);
		result.addAll(userList);
		//Glavni asistenti
		userList = dh.getGroupDAO().listUsersInGroupTree(d.em, courseInstanceID, JCMSSecurityConstants.ASISTENT_ORG);
		result.addAll(userList);
		return result;
	}
	//ITS - END

	private boolean isWikiEnabled(CourseInstance courseInstance) {
		Data d = local.get();
		String keyValue = d.getCourseInstanceKeyValue(courseInstance, "wikiEnabled");
		return keyValue!=null && keyValue.equals("1");
	}
	
	@Override
	public boolean canViewCourseWiki(CourseInstance courseInstance) {
		if(courseInstance==null || !canUserAccessCourse(courseInstance)) return false;
		return isWikiEnabled(courseInstance);
		//return false;
	};
	
	/* Za sada su sve staze u wikiju dostupne svima. Ako treba nesto stititi ovisno o stazi, to se moze ovdje
	 * podesiti.
	 * 
	 * (non-Javadoc)
	 * @see hr.fer.zemris.jcms.security.IJCMSSecurityManager#canAccessCourseWikiPath(hr.fer.zemris.jcms.model.CourseInstance, java.util.List)
	 */
	@Override
	public boolean canAccessCourseWikiPath(CourseInstance courseInstance,
			List<String> path) {
		if(courseInstance==null || !canUserAccessCourse(courseInstance)) return false;
		return isWikiEnabled(courseInstance);
	}

	@Override
	public boolean canEditCourseWikiPath(CourseInstance courseInstance,
			List<String> path) {
		if(courseInstance==null || !canUserAccessCourse(courseInstance)) return false;
		return isWikiEnabled(courseInstance) && isStaffOnCourse(courseInstance) && !isGeneratedWikiPage(courseInstance, path);
	}

	@Override
	public boolean canManageCourseWikiPath(CourseInstance courseInstance,
			List<String> path) {
		if(courseInstance==null || !canUserAccessCourse(courseInstance)) return false;
		return isWikiEnabled(courseInstance) && isStaffOnCourse(courseInstance) && !isGeneratedWikiPage(courseInstance, path);
	}
	
	
	private boolean isGeneratedWikiPage(CourseInstance courseInstance, List<String> path) {
		if(path.isEmpty()) return false;
		String elem1 = path.get(0);
		if(elem1.equals("external-problems")) return true;
		return false;
	}

	@Override
	public boolean canUsePlanningService(CourseInstance courseInstance) {
		Data d = local.get();
		Set<String> roles = d.getRoles();
		if(roles.contains(JCMSSecurityConstants.ROLE_ADMIN)) {
			return true;
		}
		if(roles.contains(JCMSSecurityConstants.ROLE_STUDENT)) return false;
		return checkForCoursePermissions(courseInstance, new String[] {JCMSSecurityConstants.ADMIN_KOLEGIJA, JCMSSecurityConstants.NOSITELJ, JCMSSecurityConstants.ASISTENT_ORG, JCMSSecurityConstants.NASTAVNIK});
	}

	@Override
	public boolean canCreatePoll(CourseInstance courseInstance) {
		Data d = local.get();
		Set<String> roles = d.getRoles();
		this.canPerformCourseAdministration(courseInstance);
		if(roles.contains(JCMSSecurityConstants.ROLE_ADMIN)) {
			return true;
		}
		return checkForCoursePermissions(courseInstance, new String[] {JCMSSecurityConstants.ADMIN_KOLEGIJA, JCMSSecurityConstants.NOSITELJ, JCMSSecurityConstants.ASISTENT_ORG, JCMSSecurityConstants.NASTAVNIK});
	}

	@Override
	public boolean canEditPoll(Poll poll) {
		if(poll.getStartDate().before(new Date())) return false;
		Data d = local.get();
		if(d.getRoles().contains(JCMSSecurityConstants.ROLE_ADMIN)) return true;
		if(poll.getOwners().contains(d.user)) return true;
		return false;
	}

	@Override
	public boolean canProlongPoll(Poll poll) {
		Data d = local.get();
		if(d.getRoles().contains(JCMSSecurityConstants.ROLE_ADMIN)) return true;
		if(poll.getOwners().contains(d.user)) return true;
		return false;
	}

	@Override
	@Deprecated
	public boolean canViewGroupPollResults(CourseInstance courseInstance, Poll poll) {
		if(poll.getViewablePublic()) return true;
		Data d = local.get();
		if(d.getRoles().contains(JCMSSecurityConstants.ROLE_ADMIN)) return true;
		PollDAO pollDAO = DAOHelperFactory.getDAOHelper().getPollDAO();
		List<Group> pollGroups = pollDAO.getAllGroupsForPollOnCourse(d.em, courseInstance, poll);
		List<Group> groups = pollDAO.getGroupsWhereUserCanSeeGroupResults(d.em, d.user, courseInstance, getRolesOnCourse(courseInstance));
		pollGroups.retainAll(groups);
		return !pollGroups.isEmpty();
	}
	
	//todo: ucinkvoitije! jedan upit
	@Override
	public List<Group> getGroupsForViewGroupPollResults(
			CourseInstance courseInstance) {
		Data d = local.get();
		GroupDAO groupDAO = DAOHelperFactory.getDAOHelper().getGroupDAO();
		List<Group> all;
		boolean admin = d.getRoles().contains(JCMSSecurityConstants.ROLE_ADMIN);
		if(admin) {
			all = new LinkedList<Group>();
		} else {
			all = groupDAO.findGroupsOwnedBy(d.em, courseInstance.getId(), d.user);
		}
		Set<String> roles = d.getCourseSecurityGroupsFor(courseInstance);
		if(admin || roles.contains(JCMSSecurityConstants.NOSITELJ)
				 || roles.contains(JCMSSecurityConstants.ADMIN_KOLEGIJA)
				 || roles.contains(JCMSSecurityConstants.ASISTENT_ORG)) {
			all.addAll(groupDAO.findLectureSubgroups(d.em, courseInstance.getId()));
		}
		if(admin || roles.contains(JCMSSecurityConstants.NOSITELJ)
				 || roles.contains(JCMSSecurityConstants.ADMIN_KOLEGIJA)
				 || roles.contains(JCMSSecurityConstants.ASISTENT_ORG)
				 || roles.contains(JCMSSecurityConstants.ASISTENT)) {
			all.addAll(groupDAO.findSubgroups(d.em, courseInstance.getId(), "1/%"));
		}
		if(admin) {
			all.addAll(groupDAO.findSubgroups(d.em, courseInstance.getId(), "6/%"));
		}
		return all;
	}
	
	//todo: ucinkvoitije! jedan upit
	@Override
	public List<Group> getGroupsForViewSinglePollResults(
			CourseInstance courseInstance) {
		Data d = local.get();
		GroupDAO groupDAO = DAOHelperFactory.getDAOHelper().getGroupDAO();
		List<Group> all;
		boolean admin = d.getRoles().contains(JCMSSecurityConstants.ROLE_ADMIN);
		if(admin) {
			all = new LinkedList<Group>();
		} else {
			all = groupDAO.findGroupsOwnedBy(d.em, courseInstance.getId(), d.user);
		}
		Set<String> roles = d.getCourseSecurityGroupsFor(courseInstance);
		if(admin || roles.contains(JCMSSecurityConstants.NOSITELJ)) {
			all.addAll(groupDAO.findLectureSubgroups(d.em, courseInstance.getId()));
		}
		if(admin || roles.contains(JCMSSecurityConstants.NOSITELJ)
				 || roles.contains(JCMSSecurityConstants.ADMIN_KOLEGIJA)
				 || roles.contains(JCMSSecurityConstants.ASISTENT_ORG)
				 || roles.contains(JCMSSecurityConstants.ASISTENT)) {
			all.addAll(groupDAO.findSubgroups(d.em, courseInstance.getId(), "1/%"));
		}
		if(admin) {
			all.addAll(groupDAO.findSubgroups(d.em, courseInstance.getId(), "6/%"));
		}
		return all;
	}

	@Override
	@Deprecated
	public boolean canViewSinglePollResults(CourseInstance courseInstance, Poll poll) {
		if(poll.getViewablePublic()) return true;
		Data d = local.get();
		if(d.getRoles().contains(JCMSSecurityConstants.ROLE_ADMIN)) return true;
		PollDAO pollDAO = DAOHelperFactory.getDAOHelper().getPollDAO();
		int apcount = pollDAO.countAnsweredPollsForGroupOwner(d.em, poll, d.user);
		if(apcount > 0) return true;
		return false;
	}
	
	@Override
	public boolean canDeletePoll(Poll poll) {
		Data d = local.get();
		if(d.getRoles().contains(JCMSSecurityConstants.ROLE_ADMIN)) return true;
		if(poll.getOwners().contains(d.user)) return true;
		return false;
	}
	
	
	//todo: ucinkvoitije! jedan upit
	@Override
	public List<Group> getGroupsOnCourseWithPollAssignPermission(
			CourseInstance courseInstance) {
		Data d = local.get();
		GroupDAO groupDAO = DAOHelperFactory.getDAOHelper().getGroupDAO();
		List<Group> all = new LinkedList<Group>();
		if(d.getRoles().contains(JCMSSecurityConstants.ROLE_ADMIN)) {
			List<Group> lectureGroups = groupDAO.findLectureSubgroups(d.em, courseInstance.getId());
			List<Group> labGroups = groupDAO.findSubgroups(d.em, courseInstance.getId(), "1/%");
			List<Group> privateGroups = groupDAO.findSubgroups(d.em, courseInstance.getId(), "6/%");
			all.addAll(lectureGroups); all.addAll(privateGroups); all.addAll(labGroups);
			return all;
		}
		all.addAll(groupDAO.findGroupsOwnedBy(d.em, courseInstance.getId(), d.user));
		if(checkForCoursePermissions(courseInstance, new String[]{JCMSSecurityConstants.ASISTENT_ORG})) {
			all.addAll(groupDAO.findSubgroups(d.em, courseInstance.getId(), "1/%"));
		}
		if(checkForCoursePermissions(courseInstance, new String[]{JCMSSecurityConstants.NOSITELJ})) {
			all.addAll(groupDAO.findLectureSubgroups(d.em, courseInstance.getId()));
		}
		return all;
	}
	
	@Override
	public Set<String> getRolesOnCourse(CourseInstance courseInstance) {
		Data d = local.get();
		return d.getCourseSecurityGroupsFor(courseInstance);
	}

	@Override
	public boolean isAdmin() {
		Data d = local.get();
		return d.getRoles().contains(JCMSSecurityConstants.ROLE_ADMIN);
	}

}
