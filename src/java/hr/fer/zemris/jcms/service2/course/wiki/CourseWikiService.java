package hr.fer.zemris.jcms.service2.course.wiki;

import java.util.Date;
import java.util.List;

import hr.fer.zemris.jcms.beans.IsolatedProblemInstanceBean;
import hr.fer.zemris.jcms.dao.DAOHelper;
import hr.fer.zemris.jcms.dao.DAOHelperFactory;
import hr.fer.zemris.jcms.model.CourseInstance;
import hr.fer.zemris.jcms.model.WikiPage;
import hr.fer.zemris.jcms.security.JCMSSecurityManagerFactory;
import hr.fer.zemris.jcms.service.extsystems.IsolatedProblemInstancesService;
import hr.fer.zemris.jcms.service2.course.CourseInstanceServiceSupport;
import hr.fer.zemris.jcms.web.actions.data.CourseWikiData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;

import javax.persistence.EntityManager;

public class CourseWikiService {

	/**
	 * Glavna metoda za dohvat wiki stranice.
	 * 
	 * @param em
	 * @param data
	 */
	public static void getCourseWikiData(EntityManager em, CourseWikiData data) {

		// Dohvat kolegija
		if(!CourseInstanceServiceSupport.fillCourseInstance(em, data, data.getCourseInstanceID())) return;
		
		// Dozvole
		if(!JCMSSecurityManagerFactory.getManager().canUserAccessCourse(data.getCourseInstance()) || !JCMSSecurityManagerFactory.getManager().canViewCourseWiki(data.getCourseInstance())) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}

		data.setEditingEnabled(JCMSSecurityManagerFactory.getManager().canEditCourseWikiPath(data.getCourseInstance(), data.getPageComponents()));
		data.setEditorMode(false);
		data.setNavigationDisabled(false);
		
		HtmlWikiRenderer r = new HtmlWikiRenderer();
		r.setWikiAction("CourseWiki.action");
		r.setCourseInstanceID(data.getCourseInstance().getId());
		r.setWikiContext(data.getWikiContext());
		r.setUrlBase(data.getUrlBase());
		r.setCurrentWikiPage(data.getPageComponents().toArray(new String[] {}));
		data.setWikiRenderer(r);
		
		if(!data.getPageComponents().isEmpty()) {
			String pageCategory = data.getPageComponents().get(0);
			if(pageCategory.equals("external-problems")) {
				data.setNavigationDisabled(true);
				getWikiCategoryExternalProblems(em, data);
				return;
			}
			WikiPage wikiPage = DAOHelperFactory.getDAOHelper().getWikiPageDAO().getByPath(em, data.getCourseInstance(), data.getPageURL());
			if(wikiPage==null) {
				getErrorWikiPage(em, data, "Tražena Wiki stranica ne postoji.");
				return;
			}
			data.setRootWikiNode(CourseWikiUtil.parseWiki(wikiPage.getContent()));
			data.setResult(AbstractActionData.RESULT_SUCCESS);
			return;
			// getErrorWikiPage(em, data);
		}
		WikiPage wikiPage = DAOHelperFactory.getDAOHelper().getWikiPageDAO().getByPath(em, data.getCourseInstance(), "");
		if(wikiPage==null) {
			data.setRootWikiNode(CourseWikiUtil.parseWiki(buildDefaultWikiPage(data.getCourseInstance())));
			data.setResult(AbstractActionData.RESULT_SUCCESS);
			return;
		}
		data.setRootWikiNode(CourseWikiUtil.parseWiki(wikiPage.getContent()));
		data.setResult(AbstractActionData.RESULT_SUCCESS);
		return;
		//getDefaultWikiPage(em, data);
		//data.setResult(AbstractActionData.RESULT_SUCCESS);
	}

	/**
	 * Metoda koja dohvaća Wiki stranicu za editiranje...
	 * 
	 * @param entityManager
	 * @param data
	 */
	public static void getCourseWikiEditingData(EntityManager em, CourseWikiData data) {

		// Dohvat kolegija
		if(!CourseInstanceServiceSupport.fillCourseInstance(em, data, data.getCourseInstanceID())) return;
		
		// Dozvole
		if(!JCMSSecurityManagerFactory.getManager().canUserAccessCourse(data.getCourseInstance()) || !JCMSSecurityManagerFactory.getManager().canEditCourseWikiPath(data.getCourseInstance(), data.getPageComponents())) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}

		data.setEditorMode(true);

		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		
		WikiPage wikiPage = dh.getWikiPageDAO().getByPath(em, data.getCourseInstance(), CourseWikiUtil.buildPageURL(data.getPageComponents().toArray(new String[] {})));
		if(wikiPage == null) {
			if(!JCMSSecurityManagerFactory.getManager().canManageCourseWikiPath(data.getCourseInstance(), data.getPageComponents())) {
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
				data.setResult(AbstractActionData.RESULT_FATAL);
				return;
			}
			wikiPage = new WikiPage();
			wikiPage.setContent("");
			wikiPage.setVersion(-1);
		}

		data.setWikiPage(wikiPage);
		
		data.setResult(AbstractActionData.RESULT_INPUT);
	}

	/**
	 * Metoda koja snima Wiki stranicu za editiranje...
	 * 
	 * @param entityManager
	 * @param data
	 */
	public static void saveCourseWikiData(EntityManager em, CourseWikiData data) {

		// Dohvat kolegija
		if(!CourseInstanceServiceSupport.fillCourseInstance(em, data, data.getCourseInstanceID())) return;
		
		// Dozvole
		if(!JCMSSecurityManagerFactory.getManager().canUserAccessCourse(data.getCourseInstance()) || !JCMSSecurityManagerFactory.getManager().canEditCourseWikiPath(data.getCourseInstance(), data.getPageComponents())) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}

		data.setEditorMode(true);

		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		
		WikiPage wikiPage = dh.getWikiPageDAO().getByPath(em, data.getCourseInstance(), CourseWikiUtil.buildPageURL(data.getPageComponents().toArray(new String[] {})));
		if(wikiPage == null) {
			if(!JCMSSecurityManagerFactory.getManager().canManageCourseWikiPath(data.getCourseInstance(), data.getPageComponents())) {
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
				data.setResult(AbstractActionData.RESULT_FATAL);
				return;
			}
			wikiPage = new WikiPage();
			wikiPage.setContent("");
			wikiPage.setVersion(-1);
		}
		data.setWikiPage(wikiPage);

		// Ako je moja verzija -1, a u bazi imam stranicu, javi gresku!
		String dbVersion = Integer.toString(wikiPage.getVersion());
		if(!dbVersion.equals(data.getVersion())) {
			data.getMessageLogger().addWarningMessage("Wiki stranica je u međuvremenu promijenjena. Vaše izmjene neće biti pohranjene da ne pregaze izmjene u bazi.");
			wikiPage = new WikiPage();
			wikiPage.setContent(data.getContent());
			int ver;
			try {
				ver = Integer.parseInt(data.getVersion());
			} catch(NumberFormatException ex) {
				ver = -1;
			}
			wikiPage.setVersion(ver);
			data.setResult(AbstractActionData.RESULT_INPUT);
			return;
		}
		
		// Sve je OK, pohrani izmjene
		wikiPage.setContent(data.getContent());
		
		boolean novo = wikiPage.getId()==null;
		
		// Ako ova stranica još nije snimljena:
		if(novo) {
			wikiPage.setCourse(data.getCourseInstance().getCourse());
			wikiPage.setLastModifiedOn(new Date());
			wikiPage.setPath(CourseWikiUtil.buildPageURL(data.getPageComponents().toArray(new String[] {})));
			wikiPage.setUser(data.getCurrentUser());
			wikiPage.setVersion(0);
			dh.getWikiPageDAO().save(em, wikiPage);
		} else {
			wikiPage.setUser(data.getCurrentUser());
		}
		
		if(novo) {
			data.getMessageLogger().addWarningMessage("Wiki stranica je u dodana.");
		} else {
			data.getMessageLogger().addWarningMessage("Izmjene Wiki stranice su pohranjene.");
		}
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}

//	private static void getDefaultWikiPage(EntityManager em, CourseWikiData data) {
//		String text = "= Popis zadataka =\r\n[[@link type=\"external-problems/list\" url=\"studtest2:http://studtest.zemris.fer.hr/problemGenerators#custom/zad_rg_001/variant1\"]]zadatak[[/link]]\r\n";
//		data.setRootWikiNode(CourseWikiUtil.parseWiki(text));
//		data.setResult(AbstractActionData.RESULT_SUCCESS);
//	}

	private static String buildDefaultWikiPage(CourseInstance courseInstance) {
		StringBuilder sb = new StringBuilder(200);
		sb.append(" = Kolegij: ").append(courseInstance.getCourse().getName()).append(" =\r\nOvo je osnovna Wiki stranica kolegija, koja trenutno nema dodatnog sadržaja.\r\n");
		return sb.toString();
	}

	private static void getWikiCategoryExternalProblems(EntityManager em, CourseWikiData data) {
		String text = null;
		if(data.getPageComponents().size()>1) {
			String command = data.getPageComponents().get(1);
			if(command.equals("list")) {
				// Ovdje dohvatim sve pokušaje tog studenta u rješavanju ovog zadatka.
				if(data.getPageComponents().size()!=3) {
					text = "= Pogreška =\r\nIdentifikator je pogrešan.\r\n";
				} else {
					String uri = data.getPageComponents().get(2);
					List<IsolatedProblemInstanceBean> beanList = IsolatedProblemInstancesService.fetchIPIListForConfiguration(data.getMessageLogger(), data.getCurrentUser().getUsername(), uri);
					// gornju listu zakvaci u kontekst wikija.
					data.getWikiContext().put(uri, beanList);
					text = "= Vaši primjerci zadatka =\r\n[[@external-problems-list url=\""+uri+"\"]][[/external-problems-list]]\r\n<p>Identifikator ovog zadatka je: ''"+
						data.getPageComponents().get(2) +
						"''. U slučaju problema s ovim zadatkom, ili ako imate kakvih sugestija, slobodno pošaljite e-mail, te obavezno u njemu navedite identifikator zadatka.";
				}
			} else if(command.equals("access")) {
				// Ovdje pokrenem dohvat i prikaz starog zadatka ili zatražim stvaranje novog zadatka.
				text = "= Prikaz primjerka zadatka =\r\nTu će doći tablica s prethodnim pokušajima rješavanja zadatka.\r\n";
			} else {
				text = "= Stranica s ispisom pogreške =\r\nLink koji ste unijeli nije dobar.\r\n";
			}
		} else {
			text = "= Stranica s ispisom pogreške =\r\nLink koji ste unijeli nije dobar.\r\n";
		}
		data.setRootWikiNode(CourseWikiUtil.parseWiki(text));
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}

	private static void getErrorWikiPage(EntityManager em, CourseWikiData data, String message) {
		String text = message;
		data.setRootWikiNode(CourseWikiUtil.parseWiki(text));
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}
	
}
