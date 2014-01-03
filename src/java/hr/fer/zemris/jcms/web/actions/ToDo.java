package hr.fer.zemris.jcms.web.actions;

import com.opensymphony.xwork2.Preparable;
import hr.fer.zemris.jcms.service.ToDoService;
import hr.fer.zemris.jcms.web.actions.data.ToDoData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;

public class ToDo extends ExtendedActionSupport implements Preparable{

	private static final long serialVersionUID = 1L;

	private ToDoData data = null;
	// Popunit ce se kod dodavanja novog korisnika
	private String user;
	private String userKey;

	@Override
	public void prepare() throws Exception {
		data = new ToDoData(MessageLoggerFactory.createMessageLogger(this, true));
	}
	
	//Formiranje lista
	public String execute() throws Exception {
    	// Ako korisnik nije logiran - van!
    	String check = checkUser(null, true);
    	if(check != null) return check;
    	ToDoService.list(getCurrentUser().getUserID(), getData());
		return SUCCESS;
	}

	//Zatvaranje taska - postavljanje statusa iz OPEN u CLOSED
	public String closeTask() throws Exception {
    	// Ako korisnik nije logiran - van!
    	String check = checkUser(null, true);
    	if(check != null) return check; 
		ToDoService.changeTaskStatus(getData(), getCurrentUser().getUserID(), "CLOSE");
		data.getMessageLogger().registerAsDelayed();
		return "proslijedi";
	}
	
	//Brisanje taska iz baze
	public String deleteTask() throws Exception {
    	// Ako korisnik nije logiran - van!
    	String check = checkUser(null, true);
    	if(check != null) return check;
     	ToDoService.deleteTask(getData(), getCurrentUser().getUserID());
		data.getMessageLogger().registerAsDelayed();
		return "proslijedi";
	}
	
	//Priprema za zadavanje novog taska
	//Izvodi se svaki puta kod učitavanja forme za stvaranje novog taska ili predloška
	public String newTask() throws Exception {
    	// Ako korisnik nije logiran - van!
    	String check = checkUser(null, true);
    	if(check != null) return check;
    	data.getNewTask().getOwner().setId(getCurrentUser().getUserID());
		data.getNewTask().getOwner().setUsername(getCurrentUser().getUsername());
		//Učitavanje podataka potrebnih za stvaranje novog zadatka
		ToDoService.getDataForNewTask(getData(), getCurrentUser().getUserID());
		return INPUT;
	}
	
	public String editTask() throws Exception {
    	// Ako korisnik nije logiran - van!
    	String check = checkUser(null, true);
    	if(check != null) return check;
     	data.getNewTask().getOwner().setId(getCurrentUser().getUserID());
		data.getNewTask().getOwner().setUsername(getCurrentUser().getUsername());
		ToDoService.getDataForNewTask(getData(), getCurrentUser().getUserID());
		if(data.getResult().equals(AbstractActionData.RESULT_FATAL)) {
			data.getMessageLogger().registerAsDelayed();
			return "proslijedi";
		}
		if (data.getTaskId()!=null) return INPUT;
		else return "subtasks";
	}	

	//Stvaranje novog taska
	public String insertNewTask() throws Exception {
    	// Ako korisnik nije logiran - van!
    	String check = checkUser(null, true);
    	if(check != null) return check;
    	//Ako je novi task i nema zadanih realizatora
    	if(data.getSubTaskID()==null && data.getNewTask().getId()==null && data.getRealizers().size()==0){
    		ToDoService.getDataForNewTask(getData(), getCurrentUser().getUserID()); 
    		data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.ToDoTaskNoRealizers"));
    		return INPUT;
    	}
    	//Pohrana novog/editiranog (glavnog) taska
    	if(data.getSubTaskID()==null){
			data.getNewTask().getOwner().setId(getCurrentUser().getUserID());
			data.getNewTask().getOwner().setUsername(getCurrentUser().getUsername());
			data.getNewTask().setSubTasks(data.getSubTasks());
			//Provjera jesu li svi podaci u beanu uneseni ispravno
			if(!ToDoService.checkBeanData(getData(), data.getNewTask(), getData().getMessageLogger())) {
				ToDoService.getDataForNewTask(getData(), getCurrentUser().getUserID()); 
				return INPUT;
			}
			ToDoService.insertNewTask(getData(), data.getNewTask(), getCurrentUser().getUserID());
			if(data.getResult().equals(AbstractActionData.RESULT_INPUT)) {
				ToDoService.getDataForNewTask(getData(), getCurrentUser().getUserID()); 
				return INPUT;
			}
    	}else{
    		//Pohrana editiranog podtaska
			if(!ToDoService.checkBeanData(getData(), data.getNewSubTask(), getData().getMessageLogger())) {
				return "subtasks";
			}
			data.getNewSubTask().setSubTasks(data.getSubTasks());
			ToDoService.insertNewTask(getData(), data.getNewSubTask(), getCurrentUser().getUserID());
    	}
    	return "proslijedi";
	} 
	
	//Dodavanje podtaska tijekom zadavanja novog taska
	public String addSubTask() throws Exception {
		// Ako korisnik nije logiran - van!
    	String check = checkUser(null, true);
    	if(check != null) return check;
    	// Provjera ispravnosti unesenih podataka u novog podtasku
    	if(!ToDoService.checkBeanData(getData(), data.getNewSubTask(), getData().getMessageLogger())) {
			return "subtasks";
		}
    	//Dodavavanje novog podtaska
    	ToDoService.addSubTask(data, data.getNewSubTask());
 		return "subtasks";
	}
	
	//Dodavanje novog realizatora (individualnog ili grupnog)
	public String addTaskRealizer() throws Exception {
		// Ako korisnik nije logiran - van!
    	String check = checkUser(null, true);
    	if(check != null) return check;
    	getData().setCurrUsr(getCurrentUser());
    	getData().setSingleUser(data.getNewTask().getRealizer());
    	ToDoService.addTaskRealizer(data, user);
    	if (data.isUserRemoval()) data.setUserRemoval(true);
       	ToDoService.getDataForNewTask(data, getCurrentUser().getUserID());
       	this.setUser(null);
    	return INPUT;
	}
	
	public ToDoData getData() {
		return data;
	}
	public void setData(ToDoData data) {
		this.data = data;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getUserKey() {
		return userKey;
	}

	public void setUserKey(String userKey) {
		this.userKey = userKey;
	}


}
