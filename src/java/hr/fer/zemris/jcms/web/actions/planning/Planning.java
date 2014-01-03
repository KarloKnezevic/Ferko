package hr.fer.zemris.jcms.web.actions.planning;

import com.opensymphony.xwork2.Preparable;
import hr.fer.zemris.jcms.service.PlanningService;
import hr.fer.zemris.jcms.web.actions.ExtendedActionSupport;
import hr.fer.zemris.jcms.web.actions.data.PlanningData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;
import hr.fer.zemris.util.InputStreamWrapper;
 
public class Planning extends ExtendedActionSupport implements Preparable{

	private static final long serialVersionUID = 1L;
	
	private String courseInstanceID;
	private PlanningData data;
	/* XML zapis plana dobiven od appleta */
	private String planData;
	private InputStreamWrapper streamWrapper;
	private Long planID;

	public void prepare() throws Exception {
		data = new PlanningData(MessageLoggerFactory.createMessageLogger(this, true));
		
	}
	
	/**
	 * Planiranje - basic
	 * @return
	 * @throws Exception
	 */
	public String execute() throws Exception {
		if(getCourseInstanceID()==null || getCourseInstanceID().equals("")) {
    		// Ako nije zadan courseInstanceID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
    	// Ako korisnik nije logiran - van!
    	String check = checkUser(null, true);
    	if(check != null) return check;
    	PlanningService.getPlans(getData(), getCurrentUser().getUserID(), getCourseInstanceID());
       	if(data.getResult().equals(AbstractActionData.RESULT_FATAL)){
    		getData().getMessageLogger().addErrorMessage(getData().getMessageLogger().getText("Planning.noServiceUsagePermission"));
			getData().getMessageLogger().registerAsDelayed();
			return "gotoCourse";
    	}
    	return SUCCESS;
    }
	
	/**
	 * Novi plan
	 * @return
	 * @throws Exception
	 */
	public String newPlan() throws Exception {
		if(getCourseInstanceID()==null || getCourseInstanceID().equals("")) {
    		// Ako nije zadan courseInstanceID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
    	// Ako korisnik nije logiran - van!
    	String check = checkUser(null, true);
    	if(check != null) return check;
    	PlanningService.prepareForNewPlan(getData(), getCurrentUser().getUserID(), getCourseInstanceID());
       	if(data.getResult().equals(AbstractActionData.RESULT_FATAL)){
    		getData().getMessageLogger().addErrorMessage(getData().getMessageLogger().getText("Planning.noServiceUsagePermission"));
			getData().getMessageLogger().registerAsDelayed();
			return "gotoCourse";
    	}
    	return "newplan";
    }
	
	/**
	 * Dohvat grupa na kolegiju
	 * @return
	 * @throws Exception
	 */
	public String getCourseInstanceGroups() throws Exception{
     	if(getCourseInstanceID()==null || getCourseInstanceID().equals("")) {
    		// Ako nije zadan courseInstanceID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
    	// Ako korisnik nije logiran - van!
    	String check = checkUser(null, true);
    	if(check != null) return check;
    	InputStreamWrapper[] wrapper = new InputStreamWrapper[1];
    	PlanningService.getCourseInstanceGroups(getData(), getCourseInstanceID(), getCurrentUser().getUserID(), wrapper);
      	if(data.getResult().equals(AbstractActionData.RESULT_FATAL)){
    		getData().getMessageLogger().addErrorMessage(getData().getMessageLogger().getText("Planning.noServiceUsagePermission"));
			getData().getMessageLogger().registerAsDelayed();
			return "gotoCourse";
    	}
		setStreamWrapper(wrapper[0]);
		return "wrapped-stream";
	}
	
	/**
	 * Dohvat prostorija na kolegiju
	 * @return
	 * @throws Exception
	 */
	public String getRooms() throws Exception{
     	if(getCourseInstanceID()==null || getCourseInstanceID().equals("")) {
    		// Ako nije zadan courseInstanceID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
    	// Ako korisnik nije logiran - van!
    	String check = checkUser(null, true);
    	if(check != null) return check;
    	InputStreamWrapper[] wrapper = new InputStreamWrapper[1];
    	PlanningService.getRooms(getCourseInstanceID(), getCurrentUser().getUserID(), wrapper);
		setStreamWrapper(wrapper[0]);
		return "wrapped-stream";
	}
	
	/**
	 * Dohvat zapisa plana
	 * @return
	 * @throws Exception
	 */
	public String getPlan() throws Exception{
     	if(getCourseInstanceID()==null || getCourseInstanceID().equals("")) {
    		// Ako nije zadan courseInstanceID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
    	// Ako korisnik nije logiran - van!
    	String check = checkUser(null, true);
    	if(check != null) return check;
    	InputStreamWrapper[] wrapper = new InputStreamWrapper[1];
    	PlanningService.getPlan(getData(), getCourseInstanceID(), getPlanID(), getCurrentUser().getUserID(), wrapper);
		setStreamWrapper(wrapper[0]);
		return "wrapped-stream";
	}
	
	/**
	 * Dohvat statusa plana
	 * @return
	 * @throws Exception
	 */
	public String getPlanStatus() throws Exception{
     	if(getCourseInstanceID()==null || getCourseInstanceID().equals("")) {
    		// Ako nije zadan courseInstanceID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
    	// Ako korisnik nije logiran - van!
    	String check = checkUser(null, true);
    	if(check != null) return check;
    	InputStreamWrapper[] wrapper = new InputStreamWrapper[1];
    	PlanningService.getPlanStatus(getPlanID(), wrapper);
		setStreamWrapper(wrapper[0]);
		return "wrapped-stream";
	}
	
	/**
	 * Pohrana plana na obradu
	 * @return
	 * @throws Exception
	 */
	public String savePlan() throws Exception{
     	if(getCourseInstanceID()==null || getCourseInstanceID().equals("")) {
    		// Ako nije zadan courseInstanceID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
    	// Ako korisnik nije logiran - van!
    	String check = checkUser(null, true);
    	if(check != null) return check;		
    	InputStreamWrapper[] wrapper = new InputStreamWrapper[1];
    	PlanningService.savePlan(getData(), getCourseInstanceID(), getCurrentUser().getUserID(), getPlanData(), wrapper);
      	if(data.getResult().equals(AbstractActionData.RESULT_FATAL)){
    		getData().getMessageLogger().addErrorMessage(getData().getMessageLogger().getText("Planning.noServiceUsagePermission"));
			getData().getMessageLogger().registerAsDelayed();
			return "gotoCourse";
    	}
		setStreamWrapper(wrapper[0]);
		return "wrapped-stream";
	}	
	
	/**
	 * Pohrana izmjena u planu
	 * @return
	 * @throws Exception
	 */
	public String updatePlan() throws Exception{
     	if(getCourseInstanceID()==null || getCourseInstanceID().equals("")) {
    		// Ako nije zadan courseInstanceID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
    	// Ako korisnik nije logiran - van!
    	String check = checkUser(null, true);
    	if(check != null) return check;		
    	InputStreamWrapper[] wrapper = new InputStreamWrapper[1];
    	PlanningService.updatePlan(getData(), getCourseInstanceID(), getCurrentUser().getUserID(), getPlanData(), getPlanID(), wrapper);
      	if(data.getResult().equals(AbstractActionData.RESULT_FATAL)){
    		getData().getMessageLogger().addErrorMessage(getData().getMessageLogger().getText("Planning.noServiceUsagePermission"));
			getData().getMessageLogger().registerAsDelayed();
			return "gotoCourse";
    	}
		setStreamWrapper(wrapper[0]);
		return "wrapped-stream";
	}	
	
	/**
	 * Priprema plana
	 * @return
	 * @throws Exception
	 */
	public String preparePlan() throws Exception{
     	if(getCourseInstanceID()==null || getCourseInstanceID().equals("")) {
    		// Ako nije zadan courseInstanceID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
    	// Ako korisnik nije logiran - van!
    	String check = checkUser(null, true);
    	if(check != null) return check;		
    	InputStreamWrapper[] wrapper = new InputStreamWrapper[1];
    	PlanningService.preparePlanExt(getData(), getCourseInstanceID(), getCurrentUser().getUserID(), getPlanID(), wrapper);
     	if(data.getResult().equals(AbstractActionData.RESULT_FATAL)){
    		getData().getMessageLogger().addErrorMessage(getData().getMessageLogger().getText("Planning.noServiceUsagePermission"));
			getData().getMessageLogger().registerAsDelayed();
			return "gotoCourse";
    	}   	
     	setStreamWrapper(wrapper[0]);
     	return "wrapped-stream";
	}	
	
	/**
	 * Dohvat jar aplikacija za lokalnu izradu rasporeda
	 * @return
	 * @throws Exception
	 */
	public String getLocalScheduler() throws Exception{
     	if(getCourseInstanceID()==null || getCourseInstanceID().equals("")) {
    		// Ako nije zadan courseInstanceID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
    	// Ako korisnik nije logiran - van!
    	String check = checkUser(null, true);
    	if(check != null) return check;		
    	PlanningService.getLocalScheduler(getData(), getCourseInstanceID(), getCurrentUser().getUserID(), getPlanID());
    	if(data.getResult().equals(AbstractActionData.RESULT_FATAL)){
			getData().getMessageLogger().registerAsDelayed();
			return "listPlans";
    	}   	
		return "localScheduler";
	}	
	
	public String getCourseInstanceID() {
		return courseInstanceID;
	}
	public void setCourseInstanceID(String courseInstanceID) {
		this.courseInstanceID = courseInstanceID;
	}

	public PlanningData getData() {
		return data;
	}
	public void setData(PlanningData data) {
		this.data = data;
	}

	public void setStreamWrapper(InputStreamWrapper streamWrapper) {
		this.streamWrapper = streamWrapper;
	}

	public InputStreamWrapper getStreamWrapper() {
		return streamWrapper;
	}

	public void setPlanData(String planData) {
		this.planData = planData;
	}

	public String getPlanData() {
		return planData;
	}
	
	public Long getPlanID() {
		return planID;
	}

	public void setPlanID(Long planID) {
		this.planID = planID;
	}


}
