package hr.fer.zemris.jcms.service;

import java.io.ByteArrayInputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import hr.fer.zemris.jcms.beans.IssueAnswerBean;
import hr.fer.zemris.jcms.beans.IssueBean;
import hr.fer.zemris.jcms.dao.DAOHelper;
import hr.fer.zemris.jcms.dao.DAOHelperFactory;
import hr.fer.zemris.jcms.dao.DatabaseOperation;
import hr.fer.zemris.jcms.dao.IssueTrackingDAO;
import hr.fer.zemris.jcms.dao.PersistenceUtil;
import hr.fer.zemris.jcms.model.CourseComponent;
import hr.fer.zemris.jcms.model.CourseInstance;
import hr.fer.zemris.jcms.model.Issue;
import hr.fer.zemris.jcms.model.IssueAnswer;
import hr.fer.zemris.jcms.model.IssueAssistantVersion;
import hr.fer.zemris.jcms.model.IssueTopic;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.model.extra.IssueStatus;
import hr.fer.zemris.jcms.security.IJCMSSecurityManager;
import hr.fer.zemris.jcms.security.JCMSSecurityConstants;
import hr.fer.zemris.jcms.security.JCMSSecurityManagerFactory;
import hr.fer.zemris.jcms.web.actions.data.IssuesData;
import hr.fer.zemris.jcms.web.actions.data.ShowCourseData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.util.InputStreamWrapper;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

/**
 * Komponenta sloja usluge za podršku IssueTracking sustavu
 * 
 * NAPOMENA: U kontekstu Ferkovog issue-tracking sustava, termini: poruka, pitanje, problem, issue i message su EKVIVALENTNI
 * 
 * @author ivanfer
 */
@Deprecated
public class IssueTrackingService {

	//Apstraktne akcije koje u ITS-u mogu izvoditi student i asistent
	//Koristi ih messageStatusManager
	public static final int STUDENT_ASK = 0;
	public static final int STUDENT_READ = 1;
	public static final int STUDENT_REPLY = 2;
	public static final int ASISTENT_READ = 3;
	public static final int ASISTENT_REPLY = 4;
	public static final int EXPLICIT_CLOSE = 4;  //Isti kôd kao i reply jer je ista semantika obrade statusa
	public static final int ASISTENT_DELAY = 5;
	public static final int POSTPONE_DATE_ARRIVAL = 6;
	public static final int POSTPONE_CANCEL = 7;
	
	public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm";
	
	/**
	 * Provjera postoji li novih pitanja
	 * @param courseInstanceID
	 * @param userID
	 */
	public static void newIssuesCheck(EntityManager em, ShowCourseData data, String courseInstanceID, Long userID){
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		IssueTrackingDAO itsDAO = dh.getIssueTrackingDAO();
		User user = data.getCurrentUser();
		//Upit u bazu postoji li novih pitanja
		//Ako je korisnik asistent
		//		za sve poruke na kolegiju
		//		ako je modificationVersion > asistentVersion onda ima novih pitanja ili followupa
		//Inače ako je korisnik student
		//		za sve poruke u vlasništvu
		//		ako je modificationVersion > studentVersion onda ima novih odgovora
		data.setNewIssues(false);
		JCMSSecurityManagerFactory.getManager().init(user, em);
		//Raspoznavanje radi li se o studentu ili asistentu
		boolean isStudent = JCMSSecurityManagerFactory.getManager().canCreateIssue(courseInstanceID);//TODO: staviti asocijativnu dozvolu
		if(!isStudent){
			newAssistantCheck(em, userID, courseInstanceID);
			updatePostponedIssues(em, courseInstanceID);
			if(itsDAO.checkIssueUpdatesStaff(em, courseInstanceID, userID)>0) data.setNewIssues(true); 
		}else{
			if(itsDAO.checkIssueUpdates(em, courseInstanceID, userID) > 0) data.setNewIssues(true);
		}
	}
	
	/**
	 * Provjera radi li se o novom članu osoblja (nekog kolegija) za kojeg treba dodati verzije
	 * @param em
	 * @param userID
	 * @param courseInstanceID
	 */
	public static void newAssistantCheck(EntityManager em, Long userID, String courseInstanceID){
		//Dohvat svih poruka s statusom postponed na trenutnom kolegiju
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		List<Long> assistantsWithVersions = dh.getIssueTrackingDAO().listAssistantsWithVersions(em, courseInstanceID);
		User currentUser = dh.getUserDAO().getUserById(em, userID);
		if(assistantsWithVersions==null || !assistantsWithVersions.contains(userID)){
			List<Issue> issuesOnCourse = dh.getIssueTrackingDAO().listIssuesOnCourse(em, courseInstanceID);
			if(issuesOnCourse!=null) {
				for(Issue i : issuesOnCourse){
					IssueAssistantVersion iav = new IssueAssistantVersion(i, currentUser, 0);
					dh.getIssueTrackingDAO().saveIssueVersion(em, iav);
				}
			}
		}
	}

	/**
	 * Prebacivanje svih poruka sa statusom postponed, a čiji je datum odgode prošao
	 * Izvodi se kad nestudent tj. asistent otvori stranicu kolegija
	 * @param em
	 * @param courseInstanceID
	 */
	public static void updatePostponedIssues(EntityManager em, String courseInstanceID){
		//Dohvat svih poruka s statusom postponed na trenutnom kolegiju
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		List<Issue> issues = dh.getIssueTrackingDAO().listIssuesForActivation(em, courseInstanceID);
		for(Issue message : issues){
			messageStatusManager(message, IssueTrackingService.POSTPONE_DATE_ARRIVAL, null);
			dh.getIssueTrackingDAO().save(em, message);
		}
	}
	
	
	
	/**
	 * Koristi se kod dodavanja novih komponenti i itema komponenti u CourseComponentService
	 * @param em
	 * @param oldTopicName 
	 * @param newTopicName
	 * @param cc Ako je null onda se dodaje vršna tema (CourseComponent), inače se dodaje CourseComponentItem i dobije se referenca na roditeljski CourseComponent
	 * @param typeIndicator -   "CCI" = CourseComponentItem, "CC" = CourseComponent
	 */
	public static void updateMessageTopic(EntityManager em, String oldTopicName, String newTopicName, CourseComponent cc, String typeIndicator){
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		String topicName;
		if (typeIndicator.equals("CC")) topicName = cc.getDescriptor().getName() + " - općenito"; //TODO: lokalizirati ovo općenito
		else topicName = cc.getDescriptor().getName() + ": " + newTopicName;
		IssueTopic newTopic = null;
		IssueTopic test = dh.getIssueTrackingDAO().findTopic(em, topicName, cc.getCourseInstance().getId());
		if(test!=null) return;
		if(oldTopicName==null ||oldTopicName.isEmpty()) newTopic = new IssueTopic();
		else newTopic = dh.getIssueTrackingDAO().findTopic(em, cc.getDescriptor().getName() + ": " + oldTopicName, cc.getCourseInstance().getId());
		if (newTopic == null) newTopic = new IssueTopic();
		newTopic.setName(topicName);
		newTopic.setShortName(null);
		newTopic.setActive(true);
		newTopic.setCourseInstance(cc.getCourseInstance());
		dh.getIssueTrackingDAO().saveTopic(em, newTopic);
	}
	
	/**
	 * Dohvat liste poruka - koristi ga JSON akcija
	 * Sadržaj liste ovisi o aktualnom korisniku. Ako je korisnik asistent dohvaćaju se sve poruke, a ako je student
	 * dohvaćaju se samo javne poruke i poruke čiji je vlasnik trenutni student.
	 * 
	 * @param data
	 * @param username
	 * @param courseInstanceID
	 */    
	public static void getMessages(final IssuesData data, final Long userID, final String courseInstanceID) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				User user = dh.getUserDAO().getUserById(em, userID);
				List<Issue> msgs = null;
				JCMSSecurityManagerFactory.getManager().init(user, em);
				String permission = JCMSSecurityManagerFactory.getManager().canViewIssueList(courseInstanceID);
				data.setCanCreateIssue((permission.equals(("STUDENT"))?true:false)); //indikacija coloring indikatoru kod formiranja beana
				//Ako je asistent
				if(permission.equals("ASISTENT")){
					if(data.getArchive()) msgs = dh.getIssueTrackingDAO().listResolvedForAsistent(em, courseInstanceID, userID);
					else msgs = dh.getIssueTrackingDAO().listActiveForAsistent(em, courseInstanceID, userID);
				}else if (permission.equals("STUDENT")){ //Inače ako je student
					//Za public pitanja se gleda samo resolved status, za private pitanja se gleda resolved + (mv=sv)
					if(data.getArchive()) msgs = dh.getIssueTrackingDAO().listResolvedForStudent(em,user.getId(), courseInstanceID);
					//Za public pitanja se uzima sve osim resolved statusa, za private pitanja se gleda resolved + (mv>sv)
					else msgs = dh.getIssueTrackingDAO().listActiveForStudent(em, user.getId(), courseInstanceID);
				}else{
					return null;
				}
				if(msgs==null) msgs = new ArrayList<Issue>();
				else{
					Collections.sort(msgs); 
				}
				List<IssueBean> msgBeans = new ArrayList<IssueBean>();
				for(Issue m : msgs){
					IssueBean bean = new IssueBean(m, data, userID);
					msgBeans.add(bean);
				}
				data.setMessageBeans(msgBeans);
				return null;
			}
		});
	}

	/**
	 * Postavljanje potrebnih dozvola i courseInstancea
	 * Ext(ernal) verzija - otvara novu transakciju
	 * @param data
	 * @param username
	 * @param courseInstanceID
	 */ 
	public static void setPermissionsExt(final IssuesData data, final Long userID, final String courseInstanceID) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				setPermissionsInt(em, data, userID, courseInstanceID);
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				User user = dh.getUserDAO().getUserById(em, userID);
				CourseInstance courseInstance = dh.getCourseInstanceDAO().get(em, courseInstanceID);
				JCMSSecurityManagerFactory.getManager().init(user, em);
				if(!JCMSSecurityManagerFactory.getManager().canUserAccessCourse(courseInstance)){
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("ITS.noPermissionToAccessCourse"));
					data.getMessageLogger().registerAsDelayed();
					data.setResult(AbstractActionData.RESULT_FATAL);
				}
				return null;
			}
		});
	}
	
	/**
	 * Postavljanje potrebnih dozvola i courseInstancea
	 * Int(ernal) verzija - koristi postojeću transakciju
	 * @param em
	 * @param data
	 * @param userID
	 * @param courseInstanceID
	 */
	public static void setPermissionsInt(EntityManager em, IssuesData data, Long userID, String courseInstanceID){
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		User user = dh.getUserDAO().getUserById(em, userID);
		JCMSSecurityManagerFactory.getManager().init(user, em);
		IJCMSSecurityManager guardian = JCMSSecurityManagerFactory.getManager();
		data.setCanManageTopics(guardian.canManageIssueTopics(courseInstanceID));
		data.setCanCreateIssue(guardian.canCreateIssue(courseInstanceID));
		data.setCanChangeIssuePublicity(guardian.canChangeIssuePublicity(courseInstanceID));
		data.setCourseInstance(dh.getCourseInstanceDAO().get(em, courseInstanceID));
		data.setCanCloseIssue(guardian.canCloseIssue(courseInstanceID));
		Long iid = (data.getIssueID()==null) ? data.getMessageBean().getID() : data.getIssueID();
		if(iid!=null){
			data.setCanSendAnswer(guardian.canSendAnswerToIssue(iid));
			data.setCanDelayAnswer(guardian.canPostponeIssue(iid));
		}
	}
	
	/**
	 * Dohvat liste tema
	 * @param data
	 * @param courseInstanceID
	 */
	public static void getTopics(final IssuesData data, final Long userID, final String courseInstanceID, final String activityFilter) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				setPermissionsInt(em, data, userID, courseInstanceID);
				if(!activityFilter.equals("ACTIVE_TOPICS_ONLY")){
					if(!data.isCanManageTopics()){
						data.setResult(AbstractActionData.RESULT_FATAL);
						return null;
					}
				}
				List<IssueTopic> topics = dh.getIssueTrackingDAO().listCourseTopics(em, courseInstanceID, activityFilter);
				if(topics==null) {
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				Collections.sort(topics);
				data.setMessageTopics(topics);
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});
	}
	
	/**
	 * Dohvat issuea s odgovorima
	 * @param data
	 * @param courseInstanceID
	 */
	public static void getIssue(final IssuesData data, final Long issueID, final Long userID) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				User currentUser = dh.getUserDAO().getUserById(em, userID);
				//0. Provjera dozvola za gledanje
				try{
					JCMSSecurityManagerFactory.getManager().init(currentUser, em);
					if(!JCMSSecurityManagerFactory.getManager().canViewIssue(issueID)){ 
						data.setResult(AbstractActionData.RESULT_FATAL);
						return null;
					}
				}
				catch(NoResultException exc){
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				//1. dohvat issuea
				Issue issue = dh.getIssueTrackingDAO().get(em, issueID);
				//2. Update statusa i verzija issuea
				JCMSSecurityManagerFactory.getManager().init(currentUser, em);
				String permission = JCMSSecurityManagerFactory.getManager().canChangeIssueStatus(issueID);
				if(permission.equals("STUDENT")) {
					issue = messageStatusManager(issue, IssueTrackingService.STUDENT_READ, null);
					dh.getIssueTrackingDAO().save(em, issue);
				}else if (permission.equals("ASISTENT")){
					issue = messageStatusManager(issue, IssueTrackingService.ASISTENT_READ, userID);
					dh.getIssueTrackingDAO().save(em, issue);
				}else if (permission.equals("NO_PERMISSON")){
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				data.setCanViewStudentsJMBAG(JCMSSecurityManagerFactory.getManager().canViewStudentsJMBAG(issue.getTopic().getCourseInstance().getId()));
				//3. formiranje beana
				IssueBean issueBean = new IssueBean(issue, data, userID);
				//4. dohvat djece - eksplicitno zbog FetchType.LAZY
				List<IssueAnswer> issueAnswers = dh.getIssueTrackingDAO().getAnswersForIssue(em,issueID);
				if(issueAnswers==null) issueAnswers = new ArrayList<IssueAnswer>();
				List<IssueAnswerBean> children = new ArrayList<IssueAnswerBean>();
				for(IssueAnswer ans : issueAnswers){
					children.add(new IssueAnswerBean(ans));
				}
				issueBean.setAnswers(children);
				data.setMessageBean(issueBean);
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				setPermissionsInt(em, data, userID, issue.getTopic().getCourseInstance().getId());
				return null;
			}
		});
	}
	
	/**
	 * Slanje odgovora na issue
	 * @param data
	 * @param courseInstanceID
	 */
	public static void sendAnswer(final IssuesData data, final Long userID, final Long issueID) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				User currentUser = dh.getUserDAO().getUserById(em, userID);
				Issue issue = dh.getIssueTrackingDAO().get(em, issueID);
				JCMSSecurityManagerFactory.getManager().init(currentUser, em);
				if(!JCMSSecurityManagerFactory.getManager().canSendAnswerToIssue(issueID)){
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				//1. Slaganje odgovora
				IssueAnswer newAnswer = new IssueAnswer(issue);
				newAnswer.setContent(data.getAnswerBean().getContent());
				newAnswer.setDate(new Date());
				newAnswer.setUser(dh.getUserDAO().getUserById(em, userID));
				dh.getIssueTrackingDAO().sendAnswer(em,newAnswer);
				//2. Update statusa i verzija issuea
				String permission = JCMSSecurityManagerFactory.getManager().canChangeIssueStatus(issueID);
				if(permission.equals("STUDENT")) {
					issue = messageStatusManager(issue, IssueTrackingService.STUDENT_REPLY, null);
					dh.getIssueTrackingDAO().save(em, issue);
				}else if (permission.equals("ASISTENT")){
					issue = messageStatusManager(issue, IssueTrackingService.ASISTENT_REPLY, userID);
					dh.getIssueTrackingDAO().save(em, issue);
				}else if (permission.equals("NO_PERMISSION")){
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				issue.setLastModificationDate(newAnswer.getDate());
				data.setIssueID(issueID);
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				setPermissionsInt(em, data, userID, issue.getTopic().getCourseInstance().getId());
				return null;
			}
		});
	}

	/**
	 * Dohvat liste tema
	 * @param data
	 * @param courseInstanceID
	 */
	public static void updateMessageTopicsActivity(final IssuesData data, final Long userID, final String courseInstanceID, final Long topicID, 
			final InputStreamWrapper[] wrapper ) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				setPermissionsInt(em, data, userID, courseInstanceID);
				if(!data.isCanManageTopics()){
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				IssueTopic topic = dh.getIssueTrackingDAO().getTopicByID(em, topicID);
				if(topic!=null){
					topic.setActive(!topic.getActive());
					if(topic.getActive()) wrapper[0] = createInputStreamWrapperFromText("<code>1</code><present>1</present>");
					else wrapper[0] = createInputStreamWrapperFromText("<code>1</code><present>0</present>");
				}else{
					wrapper[0] = createInputStreamWrapperFromText("<code>-1</code>");
				}
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});
	}
	
	/**
	 * Slanje nove poruke
	 * @param data
	 * @param courseInstanceID
	 */
	public static void sendMessage(final IssuesData data, final String courseInstanceID, final Long userID) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				User user = dh.getUserDAO().getUserById(em, userID);
				JCMSSecurityManagerFactory.getManager().init(user, em);
				if(!JCMSSecurityManagerFactory.getManager().canCreateIssue(courseInstanceID)){
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				Issue newMessage = new Issue();
				newMessage.setName(data.getMessageBean().getMessageName());
				newMessage.setContent(data.getMessageBean().getMessageContent());
				newMessage.setDeclaredPublic(data.getMessageBean().getDeclaredPublic());
				newMessage.setTopic(dh.getIssueTrackingDAO().getTopicByID(em, data.getMessageBean().getTopicID()));
				newMessage.setCreationDate(new Date());
				newMessage.setLastModificationDate(newMessage.getCreationDate());
				newMessage.setUser(user);
				messageStatusManager(newMessage, IssueTrackingService.STUDENT_ASK, null);
				dh.getIssueTrackingDAO().save(em, newMessage);
				//dodavanje asistentskih verzija
				//dohvat osoblja na kolegiju
				List<User> ulist = dh.getGroupDAO().listUsersInGroupTree(em, courseInstanceID, JCMSSecurityConstants.SEC_ROLE_GROUP);
				for(User u : ulist){
					newMessage.getVersions().add(new IssueAssistantVersion(newMessage, u, 0));
				}
				dh.getIssueTrackingDAO().save(em, newMessage);
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				setPermissionsInt(em, data, userID, courseInstanceID);
				return null;
			}
		});
	}
	
	/**
	 * Jednostavna provjera valjanosti poruke - valjana poruka mora imati naslov i sadržaj
	 * @param data
	 * @return
	 */
	public static boolean validateNewMessage(final IssuesData data){
		if(data.getMessageBean().getMessageName()==null || data.getMessageBean().getMessageName().equals("")) return false;
		if(data.getMessageBean().getMessageContent()==null || data.getMessageBean().getMessageContent().equals("")) return false;
		return true;
	}
	
	/**
	 * Upravljanje statusom i verzijama poruke
	 * @param msg
	 * @param action
	 * @return
	 */
	public static Issue messageStatusManager(Issue msg, int action, Long userID){
		switch(action){
			//Student postavio pitanje
			case 0:
				msg.setStatus(IssueStatus.NEW);
				msg = increaseStudentVersion(msg);
				break;
			//Student pogledao/procitao pitanje
			case 1:
				updateStudentVersion(msg);
				break;
			//Student poslao odgovor/reply/followup
			case 2:
				if(msg.getStatus().equals(IssueStatus.NEW)){
				}else if(msg.getStatus().equals(IssueStatus.READ)){
					msg.setStatus(IssueStatus.NEW);
				}else {
					msg.setStatus(IssueStatus.UNREAD);
				}
				msg = increaseStudentVersion(msg);
				break;
			//Asistent pogledao/procitao pitanje
			case 3:
				if(msg.getStatus().equals(IssueStatus.NEW)){
					msg.setStatus(IssueStatus.READ);
					msg = increaseAssistantVersion(msg, userID);
				}else if(msg.getStatus().equals(IssueStatus.UNREAD) && msg.getDelayDate()!=null){
					msg.setStatus(IssueStatus.POSTPONED);
					msg = increaseAssistantVersion(msg, userID);
				}else {
					updateAssistantVersion(msg, userID);
				}
				break;
			//Asistent poslao odgovor/reply
			case 4:
				msg.setStatus(IssueStatus.RESOLVED);
				msg.setDelayDate(null);
				msg = increaseAssistantVersion(msg, userID);
				break;				
			//Asistent odgodio odgovaranje na pitanje
			case 5:
				msg.setStatus(IssueStatus.POSTPONED);
				msg.setLastModificationDate(new Date());
				msg = increaseAssistantVersion(msg, userID);
				break;
			//Stigao je datum odgode odgovora
			case 6:
				msg.setStatus(IssueStatus.NEW);
				msg.setLastModificationDate(new Date());
				msg.setDelayDate(null);
				msg = increaseStudentVersion(msg);
				break;
			//Poništenje odgode odgovora
			case 7:
				msg.setStatus(IssueStatus.READ);
				msg.setDelayDate(null);
				msg.setLastModificationDate(new Date());
				msg = increaseAssistantVersion(msg, userID);
				break;
		}
		return msg;
	}
	
	private static Issue increaseStudentVersion(Issue msg){
		int v = msg.getModificationVersion();
		v++;
		msg.setStudentVersion(v);
		msg.setModificationVersion(v);
		return msg;
	}
	
	private static Issue increaseAssistantVersion(Issue msg, Long userID){
		for(IssueAssistantVersion mav : msg.getVersions()){
			if(mav.getUser().getId().equals(userID)){
				int v = msg.getModificationVersion();
				v++;
				msg.setModificationVersion(v);
				mav.setIssueVersion(v);
				break;
			}
		}
		return msg;
	}
	
	private static Issue updateAssistantVersion(Issue msg, Long userID){
		for(IssueAssistantVersion mav : msg.getVersions()){
			if(mav.getUser().getId().equals(userID)){
				mav.setIssueVersion(msg.getModificationVersion());
				break;
			}
		}
		return msg;
	}
	
	private static Issue updateStudentVersion(Issue msg){
		int v = msg.getModificationVersion();
		msg.setStudentVersion(v);
		return msg;
	}
	
	/**
	 * Provjera valjanosti datuma odgode
	 * @param data
	 * @return
	 */
	public static boolean checkDelayDateValidity(String date){
		SimpleDateFormat sdf = new SimpleDateFormat(IssueTrackingService.DATE_FORMAT);
		Date d;
		try{
			d = sdf.parse(date);
		}catch(ParseException pe){
			return false;
		}
		if(d.before(new Date())) return false;
		return true;
	}
	
	/**
	 * Odgađanje odgovora na issue
	 * @param data
	 * @param courseInstanceID
	 */
	public static void postponeIssue(final IssuesData data, final Long userID, final Long issueID, final String date, final InputStreamWrapper[] wrapper) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				User currentUser = dh.getUserDAO().getUserById(em, userID);
				Issue issue = dh.getIssueTrackingDAO().get(em, issueID);
				//Provjera dozvola
				JCMSSecurityManagerFactory.getManager().init(currentUser, em);
				if(!JCMSSecurityManagerFactory.getManager().canPostponeIssue(issueID)) {
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				//Validacija datuma
		    	if(!IssueTrackingService.checkDelayDateValidity(date)){
		    		wrapper[0] = createInputStreamWrapperFromText("<code>0</code>");
		    		data.setResult(AbstractActionData.RESULT_SUCCESS);
		    		return null;
		    	}
				//Datum je ok
				SimpleDateFormat sdf = new SimpleDateFormat(IssueTrackingService.DATE_FORMAT);
				Date newDeadline = null;
				try{
					newDeadline = sdf.parse(date);
				}catch(ParseException ignored){}
				issue = messageStatusManager(issue, IssueTrackingService.ASISTENT_DELAY, userID);
				issue.setDelayDate(newDeadline);
				dh.getIssueTrackingDAO().save(em, issue);
				String lastModified = sdf.format(issue.getLastModificationDate());
				wrapper[0] = createInputStreamWrapperFromText("<code>1</code><newstatus>"+"Odgođeno"+"</newstatus><lastmodified>"+lastModified+"</lastmodified>");
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("ITS.delaySuccessfull"));
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				setPermissionsInt(em, data, userID, issue.getTopic().getCourseInstance().getId());
				return null;
			}
		});
	}
	
	/**
	 * Inverzija javnosti pitanja
	 * @param data
	 * @param courseInstanceID
	 */
	public static void alterIssuePublicity(final IssuesData data, final Long userID, final Long issueID, final String courseInstanceID, 
			final InputStreamWrapper[] wrapper) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				User currentUser = dh.getUserDAO().getUserById(em, userID);
				Issue issue = dh.getIssueTrackingDAO().get(em, issueID);
				//Provjera dozvola
				JCMSSecurityManagerFactory.getManager().init(currentUser, em);
				if(!JCMSSecurityManagerFactory.getManager().canChangeIssuePublicity(courseInstanceID)) {
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				issue.setDeclaredPublic(!issue.isDeclaredPublic());
				dh.getIssueTrackingDAO().save(em, issue);
				if(issue.isDeclaredPublic()){
					wrapper[0] = createInputStreamWrapperFromText("<code>1</code>");
				}else{
					wrapper[0] = createInputStreamWrapperFromText("<code>0</code>");
				}
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				setPermissionsInt(em, data, userID, issue.getTopic().getCourseInstance().getId());
				return null;
			}
		});
	}

	protected static InputStreamWrapper createInputStreamWrapperFromText(String param) {
		String text = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><result>" + param + "</result>";
		try {
			byte[] buf = text.getBytes("UTF-8");
			return new InputStreamWrapper(new ByteArrayInputStream(buf), "result", buf.length, "text/xml; charset=utf-8");
		} catch(Exception ex) {
			byte[] buf = "Encoding error. Could not generate original message.".getBytes();
			return new InputStreamWrapper(new ByteArrayInputStream(buf), "result", buf.length, "text/plain; charset=utf-8");
		}
	}

	/**
	 * Poništenje odgode odgovora na pitanje
	 * @param data
	 * @param userID
	 * @param issueID
	 */
	public static void cancelIssueDelay(final IssuesData data, final Long userID, final Long issueID, final InputStreamWrapper[] wrapper) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				User currentUser = dh.getUserDAO().getUserById(em, userID);
				Issue issue = dh.getIssueTrackingDAO().get(em, issueID);
				//Provjera dozvola - ako smije odgoditi onda smije i poništiti odgodu
				JCMSSecurityManagerFactory.getManager().init(currentUser, em);
				if(!JCMSSecurityManagerFactory.getManager().canPostponeIssue(issueID)) {
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				messageStatusManager(issue, IssueTrackingService.POSTPONE_CANCEL, userID);
				dh.getIssueTrackingDAO().save(em, issue);
				SimpleDateFormat sdf = new SimpleDateFormat(IssueTrackingService.DATE_FORMAT);
				String lastModified = sdf.format(issue.getLastModificationDate());
				wrapper[0] = createInputStreamWrapperFromText("<code>1</code><newstatus>"+"Pročitano"+"</newstatus><lastmodified>"+lastModified+"</lastmodified>");
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				setPermissionsInt(em, data, userID, issue.getTopic().getCourseInstance().getId());
				return null;
			}
		});
		
	}
	
	/**
	 * Zatvaranje pitanja
	 * @param data
	 * @param userID
	 * @param issueID
	 */
	public static void closeIssue(final IssuesData data, final Long userID, final Long issueID) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				User currentUser = dh.getUserDAO().getUserById(em, userID);
				Issue issue = dh.getIssueTrackingDAO().get(em, issueID);
				JCMSSecurityManagerFactory.getManager().init(currentUser, em);
				if(!JCMSSecurityManagerFactory.getManager().canCloseIssue(issue.getTopic().getCourseInstance().getId())) {
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				messageStatusManager(issue, IssueTrackingService.EXPLICIT_CLOSE, userID);
				dh.getIssueTrackingDAO().save(em, issue);
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				setPermissionsInt(em, data, userID, issue.getTopic().getCourseInstance().getId());
				return null;
			}
		});
		
	}


}
