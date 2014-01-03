package hr.fer.zemris.jcms.service2.course;

import hr.fer.zemris.jcms.dao.DAOHelperFactory;
import hr.fer.zemris.jcms.model.Group;
import hr.fer.zemris.jcms.model.GroupOwner;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.security.JCMSSecurityManagerFactory;
import hr.fer.zemris.jcms.service2.course.issues.IssueTrackingService;
import hr.fer.zemris.jcms.web.actions.data.CourseInstanceImageData;
import hr.fer.zemris.jcms.web.actions.data.ShowCourseData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.util.InputStreamWrapper;
import hr.fer.zemris.util.StringUtil;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.persistence.EntityManager;

/**
 * Razred sloja usluge koji nudi osnovnu funkcionalnost na razini kolegija.
 * Pojedini moduli bi trebali imati zasebne razrede u podpaketima.
 * 
 * @author marcupic
 */
public class CourseService {

	public static void getCourseInstanceImage(EntityManager em, CourseInstanceImageData data) {
		// Dohvat kolegija
		if(!CourseInstanceServiceSupport.fillCourseInstance(em, data, data.getCourseInstanceID())) return;
		
		BufferedImage bim = generirajCourseInstanceImage(data.getCourseInstance().getCourse().getName());
		ByteArrayOutputStream bos = new ByteArrayOutputStream(1024);
		try {
			ImageIO.write(bim, "png", bos);
		} catch (IOException e) {
			e.printStackTrace();
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		byte[] podatci = bos.toByteArray();
		ByteArrayInputStream bis = new ByteArrayInputStream(podatci);
		data.setStream(new InputStreamWrapper(bis, "slika.png", podatci.length, "image/png"));
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}
	
	/**
	 * Dohvati sve potrebne podatke za prikaz stranice o kolegiju.
	 * @param data spremište podataka
	 * @param userID korisničko ime prijavljenog korisnika; može biti null
	 * @param courseInstanceID identifikator kolegija
	 */
	public static void getShowCourseData(EntityManager em, ShowCourseData data) {
		// Dohvat kolegija
		if(!CourseInstanceServiceSupport.fillCourseInstance(em, data, data.getCourseInstanceID())) return;
		
		// Dozvole
		if(!JCMSSecurityManagerFactory.getManager().canUserAccessCourse(data.getCourseInstance())) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		//Provjera ima li novih pitanja i problema
		IssueTrackingService.newIssuesCheck(em, data, data.getCourseInstanceID(), data.getCurrentUser().getId()); //ITS check

		// Samo ako je student na kolegiju
		if(JCMSSecurityManagerFactory.getManager().isStudentOnCourse(data.getCourseInstance())) {
			List<GroupLecturers> lects = new ArrayList<GroupLecturers>();
			List<Group> grupe = DAOHelperFactory.getDAOHelper().getGroupDAO().findSubGroupsForUser(em, data.getCourseInstance().getId(), "0", data.getCurrentUser());
			for(Group g : grupe) {
				List<GroupOwner> gOwners = DAOHelperFactory.getDAOHelper().getGroupDAO().findForGroup(em, g);
				List<User> users = new ArrayList<User>(gOwners.size());
				for(GroupOwner go : gOwners) {
					users.add(go.getUser());
				}
				Collections.sort(users, StringUtil.USER_COMPARATOR);
				lects.add(new GroupLecturers(g, users));
			}
			Collections.sort(lects);
			data.setLecturers(lects);
		}
		
		// Tko vidi što?
		Set<String> administrationPermissions = new HashSet<String>();
		data.setAdministrationPermissions(administrationPermissions);
		if(JCMSSecurityManagerFactory.getManager().canViewCoursePermissions(data.getCourseInstance())) {
			administrationPermissions.add("canViewCoursePermissions");
		}
		if(JCMSSecurityManagerFactory.getManager().canViewCourseTeachers(data.getCourseInstance())) {
			administrationPermissions.add("canViewCourseTeachers");
		}
		if(JCMSSecurityManagerFactory.getManager().canViewCourseLectureGroups(data.getCourseInstance())) {
			administrationPermissions.add("canViewCourseLectureGroups");
		}
		if(JCMSSecurityManagerFactory.getManager().canViewCourseGroupTree(data.getCourseInstance())) {
			administrationPermissions.add("canViewCourseGroupTree");
		}
		if(JCMSSecurityManagerFactory.getManager().canViewCourseAssessments(data.getCourseInstance())) {
			administrationPermissions.add("canViewCourseAssessments");
		}
		if(JCMSSecurityManagerFactory.getManager().canViewCourseApplications(data.getCourseInstance())) {
			administrationPermissions.add("canViewCourseApplications");
		}
		if(JCMSSecurityManagerFactory.getManager().canViewGradingPolicy(data.getCourseInstance())) {
			administrationPermissions.add("canViewGradingPolicy");
		}
		if(JCMSSecurityManagerFactory.getManager().canManageCourseParameters(data.getCourseInstance())) {
			administrationPermissions.add("canManageCourseParameters");
		}
		if(JCMSSecurityManagerFactory.getManager().canViewCourseBarCode(data.getCourseInstance())) {
			administrationPermissions.add("canViewCourseBarCode");
		}
		if(JCMSSecurityManagerFactory.getManager().canViewCourseAppeals(data.getCourseInstance())) {
			administrationPermissions.add("canViewCourseAppeals");
		}
		if(JCMSSecurityManagerFactory.getManager().canViewCourseScheduleAnalyzer(data.getCourseInstance())) {
			administrationPermissions.add("canViewCourseScheduleAnalyzer");
		}
		if(JCMSSecurityManagerFactory.getManager().canUseExternalGoToLabosiSSO(data.getCourseInstance())) {
			administrationPermissions.add("canUseExternalGoToLabosiSSO");
		}
		if(JCMSSecurityManagerFactory.getManager().isStudentOnCourse(data.getCourseInstance())) {
			administrationPermissions.add("canViewStudentAssessments");
			administrationPermissions.add("canViewStudentApplications");
		}
		if(JCMSSecurityManagerFactory.getManager().canCreatePoll(data.getCourseInstance())) {
			administrationPermissions.add("canCreatePoll");
		}
		if(JCMSSecurityManagerFactory.getManager().canUseQuestionBrowser(data.getCourseInstance())) {
			administrationPermissions.add("canBrowseQuestions");
		}
		if(JCMSSecurityManagerFactory.getManager().canUsePlanningService(data.getCourseInstance())) {
			administrationPermissions.add("canUsePlanningService");
		}
		if(JCMSSecurityManagerFactory.getManager().canViewCourseWiki(data.getCourseInstance())) {
			administrationPermissions.add("canViewCourseWiki");
		}
		// Ovo dvoje vide svi, jer same stranice dalje paze tko je na njima - osoblje ili studenti.
		administrationPermissions.add("canViewCourseComponents");
		administrationPermissions.add("canViewCourseMarketPlace");
		data.setRenderCourseAdministration(JCMSSecurityManagerFactory.getManager().isStaffOnCourse(data.getCourseInstance()));
		
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}

	public static class GroupLecturers implements Comparable<GroupLecturers>{
		private Group group;
		private List<User> lecturers;
		public GroupLecturers(Group group, List<User> lecturers) {
			super();
			this.group = group;
			this.lecturers = lecturers;
		}
		public Group getGroup() {
			return group;
		}
		public List<User> getLecturers() {
			return lecturers;
		}
		@Override
		public int compareTo(GroupLecturers o) {
			return StringUtil.GROUP_COMPARATOR.compare(this.group, o.group);
		}
	}
	
	private static BufferedImage generirajCourseInstanceImage(String naziv) {
		int w = 200;
		int h_margin = 10;
		int tw = w-2*h_margin;
		int h = 40;
		int radiusX = 15;
		int radiusY = 15;
		Color transp = new Color(255,255,255,0);
		Color bg = new Color(255,158,0);
		Color border = new Color(0,0,0);
		BufferedImage bim = new BufferedImage(w, h, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g = bim.createGraphics();
		g.setColor(transp);
		g.fillRect(0, 0, w, h);
		g.setColor(bg);
		g.fillRoundRect(0, 0, w-1, h-1, radiusX, radiusY);
		g.setColor(border);
		g.drawRoundRect(0, 0, w-1, h-1, radiusX, radiusY);
		String[] retci = new String[] {null, null};
		int[] retciW = new int[] {0, 0};
		int redak = 0;
		Font f = new Font(Font.SERIF, Font.BOLD, 12);
		g.setFont(f);
		FontMetrics fm = g.getFontMetrics();
		g.setColor(Color.BLACK);
		char[] elems = naziv.toCharArray();
		int curr = 0;
o:		while(redak<2) {
			int poc = curr;
			int acked = curr;
			while(true) {
				int krenuo = curr;
				while(curr<elems.length && elems[curr]!=' ') curr++;
				if(poc==curr || curr==krenuo) break o;
				String s = new String(elems, poc, curr-poc);
				int sw = fm.stringWidth(s);
				if(sw<tw) {
					retci[redak] = s;
					retciW[redak] = sw;
					acked = curr;
					while(curr<elems.length && elems[curr]==' ') curr++;
				} else if(poc==acked) {
					retci[redak] = s;
					retciW[redak] = sw;
					while(curr<elems.length && elems[curr]==' ') curr++;
					redak++;
					break;
				} else {
					curr = acked;
					while(curr<elems.length && elems[curr]==' ') curr++;
					redak++;
					break;
				}
			}
		}
		if(retci[1]==null) {
			// Sve pišem samo u jednom retku...
			g.drawString(retci[0], h_margin+(tw-retciW[0])/2, h-(h-fm.getAscent())/2-fm.getDescent());
		} else {
			// Imamo dva retka...
			if(curr<elems.length) retci[1]=retci[1]+"...";
			g.drawString(retci[0], h_margin+(tw-retciW[0])/2, h/2-(h/2-fm.getAscent())/2);
			g.drawString(retci[1], h_margin+(tw-retciW[1])/2, h-(h/2-fm.getAscent())/2-fm.getDescent());
		}
		g.dispose();
		return bim;
	}

}
