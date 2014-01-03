package hr.fer.zemris.jcms.security;

import hr.fer.zemris.jcms.beans.MPRootInfoBean;
import hr.fer.zemris.jcms.model.CourseInstance;
import hr.fer.zemris.jcms.model.Group;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.model.YearSemester;
import hr.fer.zemris.jcms.model.forum.Category;
import hr.fer.zemris.jcms.model.forum.Subforum;
import hr.fer.zemris.jcms.model.forum.Topic;
import hr.fer.zemris.jcms.model.poll.Poll;
import hr.fer.zemris.util.Tree;

import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;

public interface IJCMSSecurityManager {
	
	/**
	 * Ovu metodu potrebno je pozvati PRIJE poziva bilo koje druge metode. Kao parametri se
	 * predaju trenutni korisnik (i sve odluke donose se obzirom na njega), te entity manager
	 * koji sluzi za dohvacanje potrebnih podataka iz baze.
	 * 
	 * @param user korisnik za kojeg se provjeravaju dozvole 
	 * @param em entity manager
	 */
	public void init(User user, EntityManager em);
	/**
	 * Ovim pozivom se čiste svi podaci koji se u memoriji drže za trenutnog korisnika.
	 * Ovo nije potrebno pozivati eksplicitno.
	 */
	public void close();
	
	/**
	 * Metoda provjerava može li trenutni korisnik vidjeti/raditi s privatnim grupama. Pri tome
	 * je semantika sljedeća: ako može raditi administraciju kolegija, tada automatski vidi i sve
	 * privatne grupe. Ako ne može raditi administraciju predmeta, tada može vidjeti samo svoje
	 * vlastite privatne grupe (čiji je on owner).
	 * 
	 * @param courseInstance kolegij
	 * @return <code>true</code> ako može, <code>false</code> inače
	 */
	public boolean canUsePrivateGroups(CourseInstance courseInstance);

	/**
	 * Dohvaća dozvole korisnika za grupu na kolegiju.
	 * 
	 * @param ci kolegij
	 * @param g grupa
	 * @return dozvole; nikada neće biti <code>null</code>
	 */
	public GroupSupportedPermission getGroupPermissionFor(CourseInstance ci, Group g);

	/**
	 * Može li korisnik na zadanom kolegiju mijenjati studentima grupe unutar zadane
	 * vršne podgrupe?
	 * @param courseInstance
	 * @param parentGroupRelativePath
	 * @return
	 */
	public boolean canChangeGroup(CourseInstance courseInstance, String parentGroupRelativePath);
	/**
	 * Vraca sve grupe koje su selektirane relativnom stazom, i vidljive su koriniku na kolegiju.
	 * @param courseInstance
	 * @param relativePath
	 * @return
	 */
	public List<Group> listAccessibleGroups(CourseInstance courseInstance, String relativePath);
	/**
	 * Vraća true ako korisnik na nekim kolegijima može obavljati bilo kakvu administraciju.
	 * Ovo je istina za nastavnike, asistente i sl.
	 * @return
	 */
	public boolean canPerformCourseAdministration();
	/**
	 * Vraća popis kolegija nad kojima korisnik može obavljati administrativne akcije (ma koje vrste).
	 * Ovo je tipično ulaz za nastavnike, asistente i sl.
	 * 
	 * @param em
	 * @param ysem
	 * @return
	 */
	public List<CourseInstance> getCourseAdministrationList(YearSemester ysem);
	/**
	 * Može li korisnik obavljati poslove na administraciji kompletnog sustava?
	 * 
	 * @return
	 */
	public boolean canPerformSystemAdministration();
	/**
	 * Provjera može li korisnik pristupiti navedenom kolegiju. Odgovor je da za studenta koji slusa
	 * taj kolegij te osoblje kolegija. 
	 * 
	 * @param courseInstance
	 * @return
	 */
	public boolean canUserAccessCourse(CourseInstance courseInstance);
	/**
	 * Može li korisnik obavljati administracijske poslove na zadanom kolegiju?
	 *(o čemu se točno tadi, ovdje još ne razmišljamo)
	 * @param courseInstance
	 * @return
	 */
	public boolean canPerformCourseAdministration(CourseInstance courseInstance);
	/**
	 * Može li korisnik pregledavati accounte na sustavu?
	 * @return
	 */
	public boolean canBrowseAccouts();
	/**
	 * Može li korisnik editirati accounte na sustavu?
	 * @return
	 */
	public boolean canEditAccouts();
	/**
	 * Može li korisnik dodavati accounte na sustav?
	 * @return
	 */
	public boolean canAddAccouts();
	/**
	 * Može li korisnik dobiti listu osoblja? Osoblje je definirano kao osobe koje imaju
	 * ulogu staff.
	 * @return
	 */
	public boolean canObtainStaffList();
	/**
	 * Može li korisnik dobiti listu osoba na kolegiju u navedenoj podgrupi?
	 * @param courseInstance
	 * @param relativePath
	 * @return
	 */
	public boolean canObtainCourseUsersList(CourseInstance courseInstance, String relativePath);
	/**
	 * Provjerava je li trenutni korisnik nastavnik zadanog studenta.
	 * 
	 * @param courseInstance
	 * @param student
	 * @return <code>true</code> ako je, <code>false</code> inače
	 */
	public boolean isUserStudentsLecturer(CourseInstance courseInstance, User student);
	/**
	 * Moze li korisnik uredivati dozvole na kolegiju? 
	 * @param courseInstance
	 * @param relativePath
	 * @return
	 */
	public boolean canManageCourseUsersList(CourseInstance courseInstance, String relativePath);
	/**
	 * Moze li korisnik mijenjati sebi ili drugom korisniku dozvole na kolegiju? Recimo, zelimo da nastavnik
	 * moze odabrati svoga asistenta, ali ne zelimo i obratno :-) Barem sto se sigurnosti tice.
	 * @param courseInstance
	 * @param relPath
	 * @return
	 */
	public boolean canModifyCoursePermission(CourseInstance courseInstance, String relPath);
	/**
	 * Može li korisnik raditi (dodijeljivati, mijenjati i sl) tko je nastavnik za koju grupu na kolegiju?
	 * @param courseInstance
	 * @return
	 */
	public boolean canManageLectureGroupOwners(CourseInstance courseInstance);
	/**
	 * Moze li korisnik upravljati burzom grupa na navedenom kolegiju i navedenoj grupi-roditelju?
	 * @param courseInstance
	 * @param relativePath
	 * @return
	 */
	public boolean canManageCourseMarketPlace(CourseInstance courseInstance, String relativePath);
	/**
	 * Vraca informacije o svim market place-ovima na kolegiju za zadanog korisnika.
	 * @param courseInstance
	 * @return
	 */
	public List<MPRootInfoBean> getMarketPlacesForUser(CourseInstance courseInstance);
	/**
	 * Vraća true ako korisnik može modificirati Repozitorij (kolegija)
	 * @param courseInstance
	 * @return
	 */
	public boolean canUserManageRepository(CourseInstance courseInstance);

	/**
	 * Vraća <code>true</code> ako korisnik može raditi s preglednikom pitanja.
	 * 
	 * @param courseInstance primjerak kolegija
	 * @return <code>true</code> ako može, <code>false</code> inače
	 */
	public boolean canUseQuestionBrowser(CourseInstance courseInstance);

	/**
	 * Vraca true ako korisnik moze uredjivati raspored ispita na zadanom kolegiju
	 * @param courseInstance
	 * @return
	 */
	public boolean canManageAssessmentSchedule(CourseInstance courseInstance);
	/**
	 * Može li korisnik vidjeti podatke o ispitima (odnosi se tipicno na osoblje: asistenti ne, nastavnici da).
	 * @param courseInstance
	 * @return
	 */
	public boolean canViewAssessments(CourseInstance courseInstance);
	/**
	 * Može li korisnik mijenjati podatke o ispitima?
	 * @param courseInstance
	 * @return
	 */
	public boolean canManageAssessments(CourseInstance courseInstance);
	/**
	 * Metoda pronalazi sve korisnike u podgrupama zadane grupe kojima je owner zadani korisnik, odnosno, ako 
	 * se izvrsava u ime administratora ili nositelja, onda dohvaća sve.
	 * @param courseInstanceID
	 * @param currentUser
	 * @param parentGroup
	 * @return
	 */
	public List<User> listUsersForOwner(CourseInstance courseInstance, String parentGroupRelativePath);
	/**
	 * Može li korisnik upravljati taskom s zadanim identifikatorom?
	 * @param taskID Identifikator taska
	 * @param param Željena akcija {CLOSE, DELETE, EDIT}
	 * @return
	 */
	public boolean canManageToDoTask(Long taskID, String param);
	/**
	 * Može li trenutni korisnik zadati task zadanom korisniku
	 * @param user ciljani korisnik
	 * @return
	 */
	public boolean canAssignToDoTaskToUser(User user);
	
	/**
	 * @return Može li korisnik vidjeti skrivene dijelove foruma.
	 */
	public boolean canViewHiddenForum(CourseInstance courseInstance);
	
	/**
	 * @return Može li korisnik vidjeti kategoriju.
	 */
	public boolean canViewCategory(Category category, CourseInstance courseInstance);
	
	/**
	 * @return Može li korisnik otvoriti predmetne kategorije.
	 */
	public boolean canCreateCourseCategory(CourseInstance courseInstance);
	
	/**
	 * @return Može li korisnik otvoriti nepredmetne kategorije.
	 */
	public boolean canCreateNonCourseCategory();
	
	/**
	 * @return Može li korisnik uređivati i brisati kategorije.
	 */
	public boolean canEditCategory(Category category);
	
	/**
	 * @return Može li korisnik vidjeti podforum.
	 */
	public boolean canViewSubforum(Subforum subforum, CourseInstance courseInstance);
	
	/**
	 * @return Može li korisnik otvarati, uređivati i brisati podforume.
	 */
	public boolean canEditSubforum(Category category, CourseInstance courseInstance);
	
	/**
	 * @return Može li korisnik vidjeti kategoriju.
	 */
	public boolean canViewTopic(Topic topic, CourseInstance courseInstance);
	
	/**
	 * @return Može li korisnik otvarati teme.
	 */
	public boolean canCreateTopic(Subforum subforum, CourseInstance courseInstance);
	
	/**
	 * @return Može li korisnik uređivati teme.
	 */
	public boolean canEditTopic(Subforum subforum, CourseInstance courseInstance);
	
	/**
	 * @return Može li korisnik uređivati tuđe poruke.
	 */
	public boolean canEditOthersPost(Topic topic, CourseInstance courseInstance);
	
	/**
	 * @return Može li korisnik stvarati i uređivati vlastite poruke.
	 */
	public boolean canCreatePost(Topic topic, CourseInstance courseInstance);
	
	/**
	 * Vraća true samo ako korisnik može obavljati analizu rasporeda i zauzeća studenata na razini čitavog
	 * semestra.
	 * @return
	 */
	public boolean canAnalizeGlobalSchedule();
	/**
	 * Moze li korisnik traziti prikaz stabla grupa? Detaljno filtriranje ide drugom metodom!
	 * @param ci
	 * @param relativePath
	 * @return
	 */
	public boolean canViewGroupTree(CourseInstance ci, String relativePath);
	/**
	 * Vraca stablo dohvatljivih grupa.
	 * @param ci
	 * @param relativePath
	 * @return
	 */
	@Deprecated
	public Tree<Group, GroupPermissions> getAccessibleGroupTree(CourseInstance ci, String relativePath);
	/**
	 * Vraca stablo dohvatljivih grupa.
	 * @param ci
	 * @param relativePath
	 * @return
	 */
	public Tree<Group, GroupSupportedPermission> getAccessibleGroupTree(CourseInstance ci);	
	/**
	 * Moze li korisnik uredivati / dodavati dogadaje na predanoj grupi.
	 * @param ci
	 * @param group
	 * @return
	 */
	public boolean canManageGroupEventsFor(CourseInstance courseInstance, Group group);
	
	/**
	 * Moze li korisnik stvarati privatne grupe na kolegiju.
	 * @param courseInstance
	 * @return
	 */
	public boolean canCreatePrivateGroups(CourseInstance courseInstance);
	
	/**
	 * Moze li korisnik uredivati clanstvo na predanoj grupi?
	 * @param courseInstance
	 * @param group
	 * @return
	 */
	public boolean canManageUserGroupMembership(CourseInstance courseInstance, Group group);

	/**
	 * Vraća true samo ako korisnik može obavljati analizu rasporeda studenata na kolegiju.
	 * @param courseInstance kolegij
	 * @return
	 */
	public boolean canAnalizeCourseSchedule(CourseInstance courseInstance);
	
	/**
	 * Moze li korisnik aktivirati/deaktivirati ITS teme na nekom kolegiju
	 * @param userID
	 * @return
	 */
	public boolean canManageIssueTopics(String courseInstanceID);
	
	/**
	 * Smije li korisnik vidjeti zadano pitanje
	 * @param issueID
	 * @return
	 */
	public boolean canViewIssue(Long issueID);
	
	/**
	 * Smije li korisnik postaviti novo pitanje na zadanom kolegiju
	 * @param userID
	 * @return
	 */
	public boolean canCreateIssue(String courseInstanceID);
	
	/**
	 * Smije li korisnik promijeniti status poruke/pitanja/issuea
	 * @param userID
	 * @param issueID
	 * @return
	 */
	public String canChangeIssueStatus(Long issueID);
	
	/**
	 * Smije li korisnik poslati odgovor na pitanje/poruku/issue
	 * @param issueID
	 * @return
	 */
	public boolean canSendAnswerToIssue(Long issueID);
	
	/**
	 * Smije li korisnik odgoditi odgovor na pitanje/poruku/issue
	 * @return
	 */
	public boolean canPostponeIssue(Long issueID);

	/**
	 * Smije li korisnik mijenjati javnost pitanja
	 * @param courseInstanceID
	 * @return
	 */
	public boolean canChangeIssuePublicity(String courseInstanceID);
	
	/**
	 * Dohvaca osoblje koje prima aktivnosti iz ITS-a: nositelj i glavni asistent.
	 * @param courseInstanceID
	 * @return
	 */
	public Set<User> getIssueActivityReceivers(String courseInstanceID);
	
	/**
	 * Smije li korisnik vidjeti JMBAG studenta? Da, ako nije student
	 * @param courseInstanceID
	 * @return
	 */
	public boolean canViewStudentsJMBAG(String courseInstanceID);
	
	/**
	 * Smije li korisnik vidjeti listu pitanja za zadani kolegij
	 * @param courseInstanceID
	 * @return
	 */
	public String canViewIssueList(String courseInstanceID);
	
	/**
	 * Smije li korisnik eksplicitno zatvoriti pitanje
	 * @param issueID
	 * @return
	 */
	public boolean canCloseIssue(String courseInstanceID);
	
	/**
	 * Moze li korisnik vidjeti dozvole na kolegiju?
	 * @param courseInstance primjerak kolegija
	 * @return true ako moze, false inace
	 */
	public boolean canViewCoursePermissions(CourseInstance courseInstance);
	
	/**
	 * Moze li korisnik vidjeti nastavnike na kolegiju?
	 * @param courseInstance primjerak kolegija
	 * @return true ako moze, false inace
	 */
	public boolean canViewCourseTeachers(CourseInstance courseInstance);
	
	/**
	 * Moze li korisnik vidjeti grupe za predavanja na kolegiju?
	 * @param courseInstance primjerak kolegija
	 * @return true ako moze, false inace
	 */
	public boolean canViewCourseLectureGroups(CourseInstance courseInstance);
	
	/**
	 * Moze li korisnik vidjeti stablo grupa na kolegiju?
	 * @param courseInstance primjerak kolegija
	 * @return true ako moze, false inace
	 */
	public boolean canViewCourseGroupTree(CourseInstance courseInstance);
	
	/**
	 * Moze li korisnik vidjeti provjere/zastavice na kolegiju?
	 * @param courseInstance primjerak kolegija
	 * @return true ako moze, false inace
	 */
	public boolean canViewCourseAssessments(CourseInstance courseInstance);
	
	/**
	 * Moze li korisnik vidjeti prijave na kolegiju?
	 * @param courseInstance primjerak kolegija
	 * @return true ako moze, false inace
	 */
	public boolean canViewCourseApplications(CourseInstance courseInstance);
	
	/**
	 * Moze li korisnik vidjeti izradu bar-kod naljepnica na kolegiju?
	 * @param courseInstance primjerak kolegija
	 * @return true ako moze, false inace
	 */
	public boolean canViewCourseBarCode(CourseInstance courseInstance);
	
	/**
	 * Moze li korisnik vidjeti zalbe na ispite na kolegiju?
	 * @param courseInstance primjerak kolegija
	 * @return true ako moze, false inace
	 */
	public boolean canViewCourseAppeals(CourseInstance courseInstance);
	
	/**
	 * Moze li korisnik vidjeti analizator rasporeda studenata na kolegiju?
	 * @param courseInstance primjerak kolegija
	 * @return true ako moze, false inace
	 */
	public boolean canViewCourseScheduleAnalyzer(CourseInstance courseInstance);
	/**
	 * Provjerava je li korisnik student na kolegiju...
	 * @param courseInstance primjerak kolegija
	 * @return true ako je, false inace
	 */
	public boolean isStudentOnCourse(CourseInstance courseInstance);
	/**
	 * Provjerava je li korisnik osoblje na kolegiju...
	 * @param courseInstance primjerak kolegija
	 * @return true ako je, false inace
	 */
	public boolean isStaffOnCourse(CourseInstance courseInstance);
	/**
	 * Moze li na navedenom kolegiju korisnik doci na stranicu za popunjavanje zahtjeva
	 * o labosima? 
	 * @param courseInstance
	 * @return
	 */
	public boolean canUseExternalGoToLabosiSSO(CourseInstance courseInstance);
	
	/**
	 * Smije li korisnik na naedenom kolegiju koristiti uslugu izrade rasporeda
	 * @param courseInstanceID
	 * @return
	 */
	public boolean canUsePlanningService(CourseInstance courseInstance);
	/**
	 * Smije li korisnik vidjeti politiku ocjenjivanja?
	 * 
	 * @param courseInstance kolegij
	 * @return <code>true</code> ako smije, <code>false</code> inače
	 */
	public boolean canViewGradingPolicy(CourseInstance courseInstance);
	/**
	 * Can manage course parameters (specification of rooms for automatic scheduler etc).
	 * 
	 * @param courseInstance kolegij
	 * @return <code>true</code> ako smije, <code>false</code> inače
	 */
	public boolean canManageCourseParameters(CourseInstance courseInstance);
	/**
	 * Smije li korisnik uređivati politiku ocjenjivanja?
	 * 
	 * @param courseInstance kolegij
	 * @return <code>true</code> ako smije, <code>false</code> inače
	 */
	public boolean canEditGradingPolicy(CourseInstance courseInstance);
	

	/**
	 * Provjerava smije li korisnik stvoriti novu anketu na kolegiju
	 * @param courseInstance
	 * @return
	 */
	public boolean canCreatePoll(CourseInstance courseInstance);
	
	/**
	 * Provjerava smije li korisnik urediti anketu.
	 * @param poll
	 * @return
	 */
	public boolean canEditPoll(Poll poll);
	
	/**
	 * Provjerava smije li korisnik produžiti anketu.
	 * @param poll
	 * @return
	 */
	public boolean canProlongPoll(Poll poll);
	
	/**
	 * Provjerava moze li korisnik pobrisati anketu.
	 * @param poll
	 * @return
	 */
	public boolean canDeletePoll(Poll poll);

	/**
	 * Provjerava smije li korisnik vidjeti pojedinačne rezultate ankete.
	 * @param courseInstance
	 * @param poll
	 * @return
	 */
	public boolean canViewSinglePollResults(CourseInstance courseInstance, Poll poll);
	
	
	/**
	 * Provjerava smije li korisnik vidjeti grupne rezultate ankete.
	 * @param courseInstance
	 * @param poll
	 * @return
	 */
	public boolean canViewGroupPollResults(CourseInstance courseInstance, Poll poll);
	
	/**
	 * Dohvati sve grupe na kolegiju čije grupne rezultate korinik moze vidjeti
	 * @param courseInstance
	 * @return
	 */
	public List<Group> getGroupsForViewGroupPollResults(CourseInstance courseInstance);
	
	/**
	 * Dohvati sve grupe na kolegiju čije pojedinačne rezultate korinik moze vidjeti
	 * @param courseInstance
	 * @return
	 */
	public List<Group> getGroupsForViewSinglePollResults(CourseInstance courseInstance);
	
	/**
	 * Dohvati sve uloge (asistent, administrator kolegija, nastavnik...) koje korisnik ima
	 * na kolegiju.
	 * @param courseInstance
	 * @return
	 */
	public Set<String> getRolesOnCourse(CourseInstance courseInstance);
	
	/**
	 * Dohvati sve grupe kojima korisnik moze poslati anketu.
	 * @param courseInstance
	 * @return
	 */
	public List<Group> getGroupsOnCourseWithPollAssignPermission(CourseInstance courseInstance); 
	/**
	 * Provjerava je li korisnik administrator sustava
	 * @return
	 */
	public boolean isAdmin();
	
	/**
	 * Može li korisnik vidjeti Wiki stranicu kolegija?
	 * @param courseInstance
	 * @return <code>true</code> ako može, <code>false</code> inače
	 */
	public boolean canViewCourseWiki(CourseInstance courseInstance);
	
	/**
	 * Može li korisnik pristupiti točno zadanoj stranici stranici unutar wikija?
	 * @param courseInstance primjerak kolegija
	 * @param path staza
	 * @return <code>true</code> ako može, <code>false</code> inače
	 */
	public boolean canAccessCourseWikiPath(CourseInstance courseInstance, List<String> path);
	
	/**
	 * Može li korisnik uređivati točno određenu stranicu unutar wikija?
	 * @param courseInstance primjerak kolegija
	 * @param path staza
	 * @return <code>true</code> ako može, <code>false</code> inače
	 */
	public boolean canEditCourseWikiPath(CourseInstance courseInstance, List<String> path);
	
	/**
	 * Može li korisnik stvoriti/uređivati/obrisati točno određenu stranicu unutar wikija?
	 * @param courseInstance primjerak kolegija
	 * @param path staza
	 * @return <code>true</code> ako može, <code>false</code> inače
	 */
	public boolean canManageCourseWikiPath(CourseInstance courseInstance, List<String> path);
}
