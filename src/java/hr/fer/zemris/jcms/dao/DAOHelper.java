package hr.fer.zemris.jcms.dao;

public interface DAOHelper {

	public AuthTypeDAO getAuthTypeDAO();
	public UserDAO getUserDAO();
	public RoleDAO getRoleDAO();
	public CourseDAO getCourseDAO();
	public CourseInstanceDAO getCourseInstanceDAO();
	public EventDAO getEventDAO();
	public GroupDAO getGroupDAO();
	public MarketPlaceDAO getMarketPlaceDAO();
	public YearSemesterDAO getYearSemesterDAO();
	public KeyValueDAO getKeyValueDAO();
	public VenueDAO getVenueDAO();
	public RoomDAO getRoomDAO();
	public AssessmentDAO getAssessmentDAO();
	public AssessmentTagDAO getAssessmentTagDAO();
	public AssessmentFlagTagDAO getAssessmentFlagTagDAO();
	public UserGroupDAO getUserGroupDAO();
	public ToDoListDAO getToDoListDAO();
	public PollDAO getPollDAO();
	public RepositoryFileDAO getRepositoryFileDAO();
	public ApplicationDAO getApplicationDAO();
	public CourseComponentDAO getCourseComponentDAO();
	public ForumDAO getForumDAO();
	public IssueTrackingDAO getIssueTrackingDAO();
	public PlanningDAO getPlanningDAO();
	public SeminarDAO getSeminarDAO();
	public RepositoryFilePageDAO getRepositoryFilePageDAO();
	public ActivityDAO getActivityDAO();
	public QuestionsDAO getQuestionsDAO();
	public CourseInstanceKeyValueDAO getCourseInstanceKeyValueDAO();
	public WikiPageDAO getWikiPageDAO();
}
