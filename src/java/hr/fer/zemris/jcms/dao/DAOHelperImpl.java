package hr.fer.zemris.jcms.dao;

import hr.fer.zemris.jcms.dao.impl.ActivityDAOJPAImpl;
import hr.fer.zemris.jcms.dao.impl.ApplicationDAOJPAImpl;
import hr.fer.zemris.jcms.dao.impl.AssessmentDAOJPAImpl;
import hr.fer.zemris.jcms.dao.impl.AssessmentFlagTagDAOJPAImpl;
import hr.fer.zemris.jcms.dao.impl.AssessmentTagDAOJPAImpl;
import hr.fer.zemris.jcms.dao.impl.AuthTypeDAOJPAImpl;
import hr.fer.zemris.jcms.dao.impl.CourseComponentDAOJPAImpl;
import hr.fer.zemris.jcms.dao.impl.CourseDAOJPAImpl;
import hr.fer.zemris.jcms.dao.impl.CourseInstanceDAOJPAImpl;
import hr.fer.zemris.jcms.dao.impl.CourseInstanceKeyValueDAOJPAImpl;
import hr.fer.zemris.jcms.dao.impl.EventDAOJPAImpl;
import hr.fer.zemris.jcms.dao.impl.ForumDAOJPAImpl;
import hr.fer.zemris.jcms.dao.impl.GroupDAOJPAImpl;
import hr.fer.zemris.jcms.dao.impl.IssueTrackingDAOImpl;
import hr.fer.zemris.jcms.dao.impl.KeyValueDAOJPAImpl;
import hr.fer.zemris.jcms.dao.impl.MarketPlaceDAOJPAImpl;
import hr.fer.zemris.jcms.dao.impl.PlanningDAOImpl;
import hr.fer.zemris.jcms.dao.impl.PollDAOJPAImpl;
import hr.fer.zemris.jcms.dao.impl.QuestionsDAOJPAImpl;
import hr.fer.zemris.jcms.dao.impl.RepositoryFileDAOJPAImpl;
import hr.fer.zemris.jcms.dao.impl.RepositoryFilePageDAOJPAImpl;
import hr.fer.zemris.jcms.dao.impl.RoleDAOJPAImpl;
import hr.fer.zemris.jcms.dao.impl.RoomDAOJPAImpl;
import hr.fer.zemris.jcms.dao.impl.SeminarDAOJPAImpl;
import hr.fer.zemris.jcms.dao.impl.ToDoListDAOJPAImpl;
import hr.fer.zemris.jcms.dao.impl.UserDAOJPAImpl;
import hr.fer.zemris.jcms.dao.impl.UserGroupDAOJPAImpl;
import hr.fer.zemris.jcms.dao.impl.VenueDAOJPAImpl;
import hr.fer.zemris.jcms.dao.impl.WikiPageDAOJPAImpl;
import hr.fer.zemris.jcms.dao.impl.YearSemesterDAOJPAImpl;

public class DAOHelperImpl implements DAOHelper {

	private AuthTypeDAO authTypeDAO = new AuthTypeDAOJPAImpl();
	private UserDAO userDAO = new UserDAOJPAImpl();
	private RoleDAO roleDAO = new RoleDAOJPAImpl();
	private CourseDAO courseDAO = new CourseDAOJPAImpl();
	private CourseInstanceDAO courseInstanceDAO = new CourseInstanceDAOJPAImpl();
	private CourseInstanceKeyValueDAO courseInstanceKeyValueDAO = new CourseInstanceKeyValueDAOJPAImpl();
	private EventDAO eventDAO = new EventDAOJPAImpl();
	private GroupDAO groupDAO = new GroupDAOJPAImpl();
	private MarketPlaceDAO marketPlaceDAO = new MarketPlaceDAOJPAImpl();
	private YearSemesterDAO yearSemesterDAO = new YearSemesterDAOJPAImpl();
	private KeyValueDAO keyValueDAO = new KeyValueDAOJPAImpl();
	private VenueDAO venueDAO = new VenueDAOJPAImpl();
	private RoomDAO roomDAO = new RoomDAOJPAImpl();
	private AssessmentDAO assessmentDAO = new AssessmentDAOJPAImpl();
	private AssessmentTagDAO assessmentTagDAO = new AssessmentTagDAOJPAImpl();
	private AssessmentFlagTagDAO assessmentFlagTagDAO = new AssessmentFlagTagDAOJPAImpl();
	private UserGroupDAO userGroupDAO = new UserGroupDAOJPAImpl();
	private ToDoListDAO todoDAO = new ToDoListDAOJPAImpl();
	private PollDAO pollDAO = new PollDAOJPAImpl();
	private RepositoryFileDAO repositoryFileDAO = new RepositoryFileDAOJPAImpl();
	private ApplicationDAO applicationDAO = new ApplicationDAOJPAImpl();
	private CourseComponentDAO componentDAO = new CourseComponentDAOJPAImpl();
	private ForumDAO forumDAO = new ForumDAOJPAImpl();
	private IssueTrackingDAO issueTrackingDAO = new IssueTrackingDAOImpl();
	private PlanningDAO planningDAO = new PlanningDAOImpl();
	private SeminarDAO seminarDAO = new SeminarDAOJPAImpl();
	private RepositoryFilePageDAO repositoryFilePageDAO = new RepositoryFilePageDAOJPAImpl();
	private ActivityDAO activityDAO = new ActivityDAOJPAImpl();
	private QuestionsDAO questionsDAO = new QuestionsDAOJPAImpl();
	private WikiPageDAO wikiPageDAO = new WikiPageDAOJPAImpl();
	
	@Override
	public WikiPageDAO getWikiPageDAO() {
		return wikiPageDAO;
	}
	
	@Override
	public CourseInstanceKeyValueDAO getCourseInstanceKeyValueDAO() {
		return courseInstanceKeyValueDAO;
	}
	
	@Override
	public ActivityDAO getActivityDAO() {
		return activityDAO;
	}
	
	@Override
	public AuthTypeDAO getAuthTypeDAO() {
		return authTypeDAO;
	}

	@Override
	public UserDAO getUserDAO() {
		return userDAO;
	}

	@Override
	public RoleDAO getRoleDAO() {
		return roleDAO;
	}

	@Override
	public CourseDAO getCourseDAO() {
		return courseDAO;
	}

	@Override
	public CourseInstanceDAO getCourseInstanceDAO() {
		return courseInstanceDAO;
	}

	@Override
	public EventDAO getEventDAO() {
		return eventDAO;
	}

	@Override
	public GroupDAO getGroupDAO() {
		return groupDAO;
	}

	@Override
	public YearSemesterDAO getYearSemesterDAO() {
		return yearSemesterDAO;
	}
	
	@Override
	public KeyValueDAO getKeyValueDAO() {
		return keyValueDAO;
	}

	@Override
	public RoomDAO getRoomDAO() {
		return roomDAO;
	}

	@Override
	public VenueDAO getVenueDAO() {
		return venueDAO;
	}

	@Override
	public AssessmentDAO getAssessmentDAO() {
		return assessmentDAO;
	}
	
	@Override
	public UserGroupDAO getUserGroupDAO() {
		return userGroupDAO;
	}

	@Override
	public AssessmentTagDAO getAssessmentTagDAO() {
		return assessmentTagDAO;
	}

	@Override
	public AssessmentFlagTagDAO getAssessmentFlagTagDAO() {
		return assessmentFlagTagDAO;
	}

	@Override
	public ToDoListDAO getToDoListDAO() {
		return todoDAO;
	}

	public PollDAO getPollDAO() {
		return pollDAO;
	}

	public void setPollDAO(PollDAO pollDAO) {
		this.pollDAO = pollDAO;
	}
	
	public RepositoryFileDAO getRepositoryFileDAO(){
		return repositoryFileDAO;
	}

	@Override
	public ApplicationDAO getApplicationDAO() {
		return applicationDAO;
	}
	
	@Override
	public CourseComponentDAO getCourseComponentDAO() {
		return componentDAO;
	}
	
	public MarketPlaceDAO getMarketPlaceDAO() {
		return marketPlaceDAO;
	}
	
	public void setMarketPlaceDAO(MarketPlaceDAO marketPlaceDAO) {
		this.marketPlaceDAO = marketPlaceDAO;
	}
	
	@Override
	public ForumDAO getForumDAO() {
		return forumDAO;
	}
	
	public void setForumDAO(ForumDAO forumDAO) {
		this.forumDAO = forumDAO;
	}

	public IssueTrackingDAO getIssueTrackingDAO() {
		return issueTrackingDAO;
	}

	@Override
	public PlanningDAO getPlanningDAO() {
		return planningDAO;
	}

	@Override
	public SeminarDAO getSeminarDAO() {
		return seminarDAO;
	}
	
	public RepositoryFilePageDAO getRepositoryFilePageDAO(){
		return repositoryFilePageDAO;
	}

	public void setQuestionsDAO(QuestionsDAO questionsDAO) {
		this.questionsDAO = questionsDAO;
	}

	public QuestionsDAO getQuestionsDAO() {
		return questionsDAO;
	}
		
}
