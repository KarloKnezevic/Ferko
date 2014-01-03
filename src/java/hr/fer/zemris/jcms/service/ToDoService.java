package hr.fer.zemris.jcms.service;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import hr.fer.zemris.jcms.beans.ext.ToDoBean;
import hr.fer.zemris.jcms.beans.ext.ToDoCourseGroupsBean;
import hr.fer.zemris.jcms.beans.ext.ToDoRealizerBean;
import hr.fer.zemris.jcms.beans.ext.UserText;
import hr.fer.zemris.jcms.dao.CourseDAO;
import hr.fer.zemris.jcms.dao.DAOHelper;
import hr.fer.zemris.jcms.dao.DAOHelperFactory;
import hr.fer.zemris.jcms.dao.DatabaseOperation;
import hr.fer.zemris.jcms.dao.GroupDAO;
import hr.fer.zemris.jcms.dao.PersistenceUtil;
import hr.fer.zemris.jcms.dao.ToDoListDAO;
import hr.fer.zemris.jcms.dao.UserDAO;
import hr.fer.zemris.jcms.model.Course;
import hr.fer.zemris.jcms.model.CourseInstance;
import hr.fer.zemris.jcms.model.Group;
import hr.fer.zemris.jcms.model.Role;
import hr.fer.zemris.jcms.model.ToDoTask;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.model.UserGroup;
import hr.fer.zemris.jcms.model.YearSemester;
import hr.fer.zemris.jcms.model.extra.ToDoTaskStatus;
import hr.fer.zemris.jcms.security.IJCMSSecurityManager;
import hr.fer.zemris.jcms.security.JCMSSecurityConstants;
import hr.fer.zemris.jcms.security.JCMSSecurityManagerFactory;
import hr.fer.zemris.jcms.web.actions.data.ToDoData;
import hr.fer.zemris.jcms.web.actions.data.ToDoUsersListJSONData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;
import hr.fer.zemris.util.StringUtil;
import javax.persistence.EntityManager;

public class ToDoService {

	/**
	 * Izmjena statusa taska
	 * @param data
	 * @param userID
	 * @param taskID
	 * @param param 
	 */
	public static void changeTaskStatus(final ToDoData data, final Long userID, final String param){
		PersistenceUtil.executeSingleDatabaseOperation(
				new DatabaseOperation<Void>() {
					@Override
					public Void executeOperation(EntityManager em) {
						DAOHelper dh = DAOHelperFactory.getDAOHelper();
						Long taskID = Long.parseLong(data.getTaskId());
						User currentUser = dh.getUserDAO().getUserById(em, userID);
						JCMSSecurityManagerFactory.getManager().init(currentUser, em);
						
						if (JCMSSecurityManagerFactory.getManager().canManageToDoTask(taskID, param)){
							ToDoListDAO todoDao = dh.getToDoListDAO();
							ToDoTask task = todoDao.getSingleTask(em, taskID);
							
							//Zatvaranje grupnog taska (treba kaskadirati sve)
							if(task.getStatus()==ToDoTaskStatus.GROUP_TASK){
								//0. korak - Zatvoriti sam grupni task
								changeStatus(todoDao, em, task, param);
								//1. korak - Dohvat sve djece s statusom OPEN
								List<ToDoTask> finalClosingList = new ArrayList<ToDoTask>();
								finalClosingList.addAll(todoDao.getChildrenWithStatus(em, task.getId(), ToDoTaskStatus.OPEN));
								List<ToDoTask> tmp = todoDao.getChildrenWithStatus(em, task.getId(), ToDoTaskStatus.GROUP_TASK);
								finalClosingList.addAll(tmp);
								for(ToDoTask tsk : tmp){
									finalClosingList.addAll(todoDao.getChildrenWithStatus(em, tsk.getId(), ToDoTaskStatus.OPEN));
								}
								for(ToDoTask tsk : finalClosingList){
									changeStatus(todoDao, em, tsk, param);
								}
							}
							//Zatvaranje taska zadanog implicitno putem grupe
							else if(task.getParentTask()!=null && task.getParentTask().getStatus()==ToDoTaskStatus.GROUP_TASK){
								//Ako se radi o tasku koji nema grandparenta - treba sve pronaći
								if(task.getParentTask().getParentTask()==null){
									//0. korak - Zatvoriti sam glavni task
									changeStatus(todoDao, em, task, param);
									//1. korak - Pronaći group taskove djecu glavnog group taska
									List<ToDoTask> groupKids = todoDao.getChildrenWithStatus(em, task.getParentTask().getId(), ToDoTaskStatus.GROUP_TASK);
									//2. korak - Pronaći njihovu djecu čiji je realizer trenutni user
									for(ToDoTask t : groupKids){
										List<ToDoTask> kids = todoDao.getChildrenForRealizer(em, t.getId(), currentUser.getId());
										for(ToDoTask kt : kids){
											changeStatus(todoDao, em, kt, param);
										}
									}
								}else{
									//Ako se radi o tasku koji ima djeda onda se zatvara samo njega
									changeStatus(todoDao, em, task, param);
								}
							}
							//Ako se zatvara individualni task s/bez djece
							else if((task.getStatus()==ToDoTaskStatus.OPEN)){ 
								changeStatus(todoDao, em, task, param);
								List<ToDoTask> childrenTasks = todoDao.getChildrenForParentID(em, taskID);
								for(ToDoTask child : childrenTasks){
									changeStatus(todoDao, em, child, param);
								}
							}

							data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.ToDoTaskClosedSuccesfully"));
						}else{
							data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.ToDoTaskCloseNoPermission"));
							
						}

						return null;
					}
					
					private void changeStatus(ToDoListDAO todoDao, EntityManager em, ToDoTask task, String param){
						if(param=="CLOSE"){
							if(task.getStatus().equals(ToDoTaskStatus.OPEN)) task.setStatus(ToDoTaskStatus.CLOSED);
							else if(task.getStatus().equals(ToDoTaskStatus.GROUP_TASK)) task.setStatus(ToDoTaskStatus.GROUP_TASK_CLOSED);
						}else{
							task.setStatus(ToDoTaskStatus.valueOf(param));
						}
						todoDao.updateTask(em, task);
					}
				}
			);
	}
	
	/**
	 * Potpuno brisanje taska iz baze
	 * @param data
	 * @param userID
	 * @param taskID
	 */
	public static void deleteTask(final ToDoData data, final Long userID){
		PersistenceUtil.executeSingleDatabaseOperation(
				new DatabaseOperation<Void>() {
					@Override
					public Void executeOperation(EntityManager em) {
						DAOHelper dh = DAOHelperFactory.getDAOHelper();
						Long taskID = Long.parseLong(data.getTaskId());
						User currentUser = dh.getUserDAO().getUserById(em, userID);
						JCMSSecurityManagerFactory.getManager().init(currentUser, em);
						
						if (JCMSSecurityManagerFactory.getManager().canManageToDoTask(taskID, "DELETE")){
							ToDoListDAO todoDao = dh.getToDoListDAO();
							todoDao.deleteTask(em, taskID); 
							data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.ToDoTaskDeletedSuccesfully"));
							
						}else{
							data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.ToDoTaskDeleteNoPermission"));
							
						}
						
						return null;
					}
				}
			);
	}
	
	/**
	 * Zadavanje novog taska
	 * @param data
	 * @param newTaskBean
	 * @param userID
	 */
	public static void insertNewTask(final ToDoData data, final ToDoBean newTaskBean, final Long userID){
		PersistenceUtil.executeSingleDatabaseOperation(
				new DatabaseOperation<Void>() {
					@Override
					@SuppressWarnings("unused")
					public Void executeOperation(EntityManager em) {
						DAOHelper dh = DAOHelperFactory.getDAOHelper();
						User owner = dh.getUserDAO().getUserById(em, userID);	
						JCMSSecurityManagerFactory.getManager().init(owner, em);
						ToDoListDAO todoDao = dh.getToDoListDAO();
						
						//Rješavanje izmjene grupnih taskova
						if(newTaskBean.getStatus()==ToDoTaskStatus.GROUP_TASK){
							ToDoTask newTask = resolveTask(todoDao, em, newTaskBean, owner, owner, null);
							//Dohvat sve djece
							List<ToDoTask> children = dh.getToDoListDAO().getChildrenForParentID(em, newTask.getId());
							//Unos izmjena i pohrana
							for(ToDoTask task : children){
								if(task.getStatus()!=ToDoTaskStatus.GROUP_TASK && task.getStatus()!=ToDoTaskStatus.GROUP_TASK_CLOSED){
									task.setTitle(newTask.getTitle());
									if(newTaskBean.getDescription().contains("#_#"))
										task.setDescription(newTaskBean.getGroupTaskDescription());
									else task.setDescription(newTaskBean.getDescription());
									task.setPriority(newTask.getPriority());
									task.setDeadline(newTask.getDeadline());
									todoDao.updateTask(em, task);
								}
							}
							//Novi podzadaci za grupni task
							for(ToDoBean bean : newTaskBean.getSubTasks()){
								if(bean.getId()==null){
									ToDoTask subTask = resolveTask(todoDao, em, bean, newTask.getOwner(), newTask.getOwner(), newTask);
									bean.setStatus(ToDoTaskStatus.OPEN);
									for(User u : todoDao.getGroupTaskRealizers(em, newTask.getId())){
										ToDoTask subTask2 = resolveTask(todoDao, em, bean, newTask.getOwner(), u, subTask);
									}
								}
							}
							if(checkIsStudent(dh, em, userID)) data.setRenderBothToDoLists(false);
							data.setResult(AbstractActionData.RESULT_SUCCESS);
							return null;
						}
						
						//Razdvajanje realizera u individualne i grupne
						ArrayList<String> individualRealizers = new ArrayList<String>();
						ArrayList<Group> groupRealizers = new ArrayList<Group>();
						if(data.getRealizers()!=null){
							for(ToDoRealizerBean tdb: data.getRealizers()){
								if(tdb.getUserRealizer()){
									UserText ut = UserText.parse(tdb.getDescription());
									individualRealizers.add(ut.getJmbag());
								}else{
									Group g = new Group();
									g.setId(tdb.getIDLong());
									g.setName(tdb.getDescription());
									groupRealizers.add(g);
								}
							}
						}
						
						//Zadavanje taska svim definiranim individualnim realizatorima ako ih ima - KOD DODAVANJA NOVIH I UREĐIVANJA POSTOJEĆIH
						for(String jmbag : individualRealizers){
							User realizer = dh.getUserDAO().getUserByJMBAG(em, jmbag);
							//Provjera ima li trenutni korisnik dozvolu zadati task ciljanom korisniku
							if(!JCMSSecurityManagerFactory.getManager().canAssignToDoTaskToUser(realizer)){
								data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.ToDoTaskAssignNoPermission "));
								data.setResult(AbstractActionData.RESULT_INPUT);
								return null;
							}
							ToDoTask newTask = resolveTask(todoDao, em, newTaskBean, owner, realizer, null);
							for(ToDoBean bean : newTaskBean.getSubTasks()){
								ToDoTask subTask = resolveTask(todoDao, em, bean, newTask.getOwner(), newTask.getRealizer(), newTask);
							}
						}
						
						//Rješavanje grupnih realizatora - SAMO KOD DODAVANJA NOVIH
						for(Group g : groupRealizers){
							String name = g.getName();
							//Dohvat članova grupe
							g = dh.getGroupDAO().get(em, g.getId());
			
							//Dohvat korisnika iz cijelog stabla čiji korijen je trenutna grupa
							List<UserGroup> finalUserList = dh.getUserDAO().findForGroupAndSubGroups(em, g.getCompositeCourseID(), g.getRelativePath()+"%", g.getRelativePath());
							
							// Pohrana glavnog taska (i pripadnih subtaskova) s GROUP_TASK statusom
							//Privremeno postavljanje statusa taska i opširnijeg opisa koji sadrži naziv grupe
							newTaskBean.setStatus(ToDoTaskStatus.GROUP_TASK);
							newTaskBean.setDescription(name+ "#_#" + newTaskBean.getDescription());
							ToDoTask newTaskGROUP_TASK = resolveTask(todoDao, em, newTaskBean, owner, owner, null);
							//Vraćanje izvornih postavki statusa i opisa
							newTaskBean.setStatus(ToDoTaskStatus.OPEN);
							String[] desc = newTaskBean.getDescription().split("#_#");
							newTaskBean.setDescription((desc.length>1) ? desc[1] : "");
							//Pohrana roditelja/glavnog taska za sve korisnike u grupi
							
							ToDoTask task = null;
							ToDoTask tmptask = null;
							for(UserGroup user : finalUserList) task = resolveTask(todoDao, em, newTaskBean, owner, user.getUser(), newTaskGROUP_TASK);
							for(ToDoBean mainBean : newTaskBean.getSubTasks()){
								mainBean.setStatus(ToDoTaskStatus.GROUP_TASK);
								ToDoTask subTaskGROUP_TASK = resolveTask(todoDao, em, mainBean, owner, owner, newTaskGROUP_TASK);
								mainBean.setStatus(ToDoTaskStatus.OPEN);
								for(UserGroup user : finalUserList){
									tmptask = resolveTask(todoDao, em, mainBean, owner, user.getUser(), subTaskGROUP_TASK);
								}
							}
						}
						if(checkIsStudent(dh, em, userID)) data.setRenderBothToDoLists(false);
						data.setResult(AbstractActionData.RESULT_SUCCESS);
						data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.ToDoTaskCreatedSuccessfuly"));
						return null;
					}
				
					// Pomoćna metoda za obradu taska: stvaranje taska, popunjavanje podacima iz beana/parametara i perzistiranje u bazu
					private ToDoTask resolveTask(ToDoListDAO todoDao, EntityManager em, ToDoBean bean, User owner, User realizer, ToDoTask parent){
						ToDoTask task = null;
						if (bean.getId()==null) task = new ToDoTask();
						else task = todoDao.getSingleTask(em, bean.getId());
						fillTaskFromBean(task, bean);
						task.setOwner(owner);
						task.setRealizer(realizer);
						if (parent==null) task.setParentTask(task.getParentTask());
						else task.setParentTask(parent);
						if (task.getId()!=null) todoDao.updateTask(em, task);
						else todoDao.insertTask(em, task);
						return task;
					}
				}
			);
	}
	
	/**
	 * Pomoćna metoda - Punjenje zadanog taska podacima iz zadanog beana
	 * @param task
	 * @param bean
	 */
	protected static void fillTaskFromBean(ToDoTask task, ToDoBean bean){
		task.setId(bean.getId());
		task.setTitle(bean.getTitle());
		task.setDeadline(bean.getDeadline());
		task.setDescription(bean.getDescription());
		task.setOwner(bean.getOwner());
		task.setRealizer(bean.getRealizer());
		task.setVersion(bean.getVersion());
		task.setStatus(bean.getStatus());
		if (task.getStatus().equals(ToDoTaskStatus.TEMPLATE) 
					|| task.getStatus().equals(ToDoTaskStatus.PUBLIC_TEMPLATE)) {
			task.setGarbageCollectable(false);
		}
		else task.setGarbageCollectable(true);
		task.setPriority(bean.getPriority());
	}
	
	/**
	 * Dohvat potrebnih podataka za stvaranje novog taska
	 * @param data
	 * @param userID
	 */
	
	public static void getDataForNewTask(final ToDoData data, final Long userID){
		PersistenceUtil.executeSingleDatabaseOperation(
				new DatabaseOperation<Void>() {
					@Override
					public Void executeOperation(EntityManager em) {
						DAOHelper dh = DAOHelperFactory.getDAOHelper();
						
						User u = dh.getUserDAO().getUserById(em, userID);
						data.setSingleUser(u);
						IJCMSSecurityManager securityManager = JCMSSecurityManagerFactory.getManager();
						securityManager.init(u, em);
						
						if(checkIsStudent(dh, em, userID)) {
							data.setRenderBothToDoLists(false);
						}else{
							//DOHVAT GRUPA DOSTUPNIH KORISNIKU
							String sem = DAOHelperFactory.getDAOHelper().getKeyValueDAO().get(em, "currentSemester").getValue();
							YearSemester ys = new YearSemester();
							ys.setId(sem);
							List<CourseInstance> userCourses = null;
							
							//Ako je admin - dohvat kolegija na kojima je član osoblja
							if(securityManager.canPerformSystemAdministration() && !data.isAllGroups()){
								//1. vidjeti na kojim kolegijima je admin
								List<String> courseIDList= dh.getGroupDAO().listCoursesForUser(em, "3/%", u);
								userCourses = new ArrayList<CourseInstance>();
								if(courseIDList!=null){
									for(String courseID : courseIDList){
										userCourses.add(dh.getCourseInstanceDAO().get(em, courseID));
									}
								}
							}
							
							//Ako nije admin ili je admin koji želi sve grupe
							if(userCourses==null) userCourses = securityManager.getCourseAdministrationList(ys);
							
							List<ToDoCourseGroupsBean> courseGroups = new ArrayList<ToDoCourseGroupsBean>();
							for(CourseInstance ci : userCourses){
								ToDoCourseGroupsBean cg = new ToDoCourseGroupsBean();
								cg.setCourseName(ci.getCourse().getName());
								
								//1. U listu grupa se ubacuju sve konkretne grupe za predavanja
								List<Group> grupePred = securityManager.listAccessibleGroups(ci, "0");
								//izbacivanje 0/0 grupe za predavanja
								Iterator<Group> i = grupePred.iterator();
								while(i.hasNext()){
									Group g = i.next();
									if(g.getRelativePath().equals("0/0")){
										i.remove(); 
										break;
									}
								}
								cg.getGroups().addAll(grupePred);   
								//2. U listu grupa se ubacuju sve konkretne grupe za labose
								List<Group> grupeLab = securityManager.listAccessibleGroups(ci, "1");
								cg.getGroups().addAll(grupeLab);
								
								courseGroups.add(cg);
							}
							em.clear();
							
							//Formatiranje naziva grupa
							for(ToDoCourseGroupsBean bean : courseGroups){
								for(Group g : bean.getGroups()){
									if(g.getRelativePath().equals("0")) g.setName("Sve grupe za predavanja");
									else if(g.getRelativePath().equals("1")) g.setName("Sve grupe za sve lab.vježbe");
									else if(g.getRelativePath().matches("0/[0-9]+")) g.setName(".....Grupa " + g.getName());
									else if(g.getRelativePath().matches("1/[^/]+")) g.setName("Sve grupe za vježbu: " + g.getName());
									else if(g.getRelativePath().matches("1/[0-9]+/[^/]+")) g.setName(".....Grupa " + g.getName());
								}
							}
							data.setUserGroups(courseGroups);
						}
					
						//Radi li se o učitavanje postojećeg taska ili podtaska
						if (data.getTaskId()!=null && !StringUtil.isStringBlank(data.getTaskId())){
							data.setTaskToLoadID(Long.parseLong(data.getTaskId()));
						}else if (data.getSubTaskID()!=null && !StringUtil.isStringBlank(data.getSubTaskID())){
							data.setTaskToLoadID(Long.parseLong(data.getSubTaskID()));
						}
						
						//Ako se radi o učitavanju postojećeg taska ili podtaska
						if (data.getTaskToLoadID()!=null || data.getTaskToInstantiateID()!=null){
							Long taskID = null;
							if(data.getTaskToLoadID()!=null) taskID = data.getTaskToLoadID();
							else taskID = data.getTaskToInstantiateID();
							//Provjera dozvola ako se radi o editiranju
							if(data.getTaskToLoadID()!=null){
								if (!securityManager.canManageToDoTask(taskID, "EDIT")){
									data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.ToDoTaskEditNoPermission"));
									data.setResult(AbstractActionData.RESULT_FATAL);
									return null;
								}
							}
							data.setResult(AbstractActionData.RESULT_SUCCESS);
							List<ToDoTask> tdd = null;
							ToDoTask task = dh.getToDoListDAO().getSingleTask(em, taskID);
							if(task.getStatus()==ToDoTaskStatus.GROUP_TASK){
								tdd = dh.getToDoListDAO().getGroupTask(em, taskID);
							}else{
								tdd = dh.getToDoListDAO().getTasksFamily(em, taskID);
							}
							if(tdd.size()==0) return null;
							List<ToDoBean> beans = new ArrayList<ToDoBean>();
							taskToBeanTransform(tdd, beans, u);
							data.setLoadedTask(beans.get(0));
							//Ako se učitava predložak
							if(data.getTaskToInstantiateID()!=null) {
								data.getLoadedTask().setId(null);
								data.getLoadedTask().setStatus(ToDoTaskStatus.OPEN);
								if(data.getLoadedTask().getSubTaskQuantity()>0){
									for(ToDoBean bean : data.getLoadedTask().getSubTasks()) {
										bean.setId(null);
										bean.setStatus(ToDoTaskStatus.OPEN);
									}
								}
							}
							data.setNewTask(data.getLoadedTask());
							data.setSubTasks(data.getNewTask().getSubTasks());
							if (data.getSubTaskID()!=null) data.setNewSubTask(data.getLoadedTask());
							if(data.getRealizers()==null) data.setRealizers(new ArrayList<ToDoRealizerBean>());
							//Ako korisnik nije student 
							if(data.getRenderBothToDoLists()){
								//Ako se učitava postojeći zadatak onda u realizatore dodaj definiranog realizatora
								if(data.getTaskToLoadID()!=null){
									User r = data.getLoadedTask().getRealizer();
									UserText ut = new UserText(r.getId(),r.getJmbag(),r.getLastName(),r.getFirstName());
									data.getRealizers().add(new ToDoRealizerBean(true,ut.toString(),r.getId().toString()));
								//Inače ako se učitava template onda dodaj trenutnog korisnika
								}else if (data.getTaskToInstantiateID()!=null){
									UserText ut = new UserText(u.getId(),u.getJmbag(),u.getLastName(),u.getFirstName());
									data.getRealizers().add(new ToDoRealizerBean(true,ut.toString(),u.getId().toString()));
								}
							}
							//Inace dodaj trenutnog korisnika tj. studenta jer on samo sebi smije zadati task
							else{
								UserText ut = new UserText(u.getId(),u.getJmbag(),u.getLastName(),u.getFirstName());
								data.getRealizers().add(new ToDoRealizerBean(true,ut.toString(),u.getId().toString()));
							}
							return null;
						}
						
						if(!data.isUserRemoval() && (data.getRealizers()==null || data.getRealizers().size()==0) )
						{
							if(data.getRealizers()==null) data.setRealizers(new ArrayList<ToDoRealizerBean>());
							UserText ut = new UserText(u.getId(),u.getJmbag(),u.getLastName(),u.getFirstName());
							data.getRealizers().add(new ToDoRealizerBean(true,ut.toString(),u.getId().toString()));
						}						
						if(data.getNewTask().getStatus()==null) data.getNewTask().setStatus(ToDoTaskStatus.OPEN);
						return null;
					}
				}
			);
	}

	
	/**
	 * Učitavanje vlastitih i dozvoljenih javnih predložaka za trenutnog korisnika
	 * @param em
	 * @param data
	 */
	
	protected static void loadTemplates(EntityManager em, ToDoData data){
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		JCMSSecurityManagerFactory.getManager().init(data.getSingleUser(), em);
		List<ToDoTask> publicTemplateList = dh.getToDoListDAO().getPublicTemplates(em);
		Iterator<ToDoTask> it = publicTemplateList.iterator();
		while(it.hasNext()){
			ToDoTask tdd = it.next();
			if(!JCMSSecurityManagerFactory.getManager().canManageToDoTask(tdd.getId(), "USE_TEMPLATE")){
				it.remove();
			}
		}
		List<ToDoBean> publicTemplateBeanList = new ArrayList<ToDoBean>();
		if (publicTemplateList.size()>0) taskToBeanTransform(publicTemplateList, publicTemplateBeanList, data.getSingleUser());
		List<ToDoTask> ownTemplateList = dh.getToDoListDAO().getOwnTasksByStatus(em, data.getSingleUser().getId(), ToDoTaskStatus.TEMPLATE);
		List<ToDoBean> templateBeanList = new ArrayList<ToDoBean>();
		if (ownTemplateList.size()>0) taskToBeanTransform(ownTemplateList, templateBeanList, data.getSingleUser());
		if (templateBeanList.size()>0){
			templateBeanList.addAll(publicTemplateBeanList);
			data.setTemplateList(templateBeanList);
		}
		else if (publicTemplateBeanList.size()>0){
			publicTemplateBeanList.addAll(templateBeanList);
			data.setTemplateList(publicTemplateBeanList);
		}else{
			data.setTemplateList(new ArrayList<ToDoBean>());
		}
		data.setTemplateMap(new HashMap<Long, ToDoBean>());
		for(ToDoBean tdb : data.getTemplateList()){
			data.getTemplateMap().put(tdb.getId(), tdb);
		}
	}
	
	/**
	 * Formiranje lista taskova: lista mojih taskova, lista zadanih taskova i lista dostupnih predložaka
	 * @param userID
	 * @param data
	 */
	
	public static void list(final Long userID, final ToDoData data) {
		PersistenceUtil.executeSingleDatabaseOperation(
			new DatabaseOperation<Void>() {

				@Override
				public Void executeOperation(EntityManager em) {
					DAOHelper dh = DAOHelperFactory.getDAOHelper();
					User u = dh.getUserDAO().getUserById(em, userID);
					data.setSingleUser(u);
					
					JCMSSecurityManagerFactory.getManager().init(u, em);
					if(JCMSSecurityManagerFactory.getManager().canPerformSystemAdministration()) data.setIsAdmin(true);
					else data.setIsAdmin(false);
					
					if(checkIsStudent(dh, em, userID)) data.setRenderBothToDoLists(false);	
					
					//Dohvat "Moje ToDo liste"
					//1. Dohvat individualnih taskova (taskova koji su dodijeljeni direktno, a ne implicitno putem grupe)
					List<ToDoTask> taskList = null;
					taskList = dh.getToDoListDAO().getOwnTasks(em, userID);  
					List<ToDoBean> beanList = new ArrayList<ToDoBean>();
					taskToBeanTransform(taskList, beanList, u);
					data.setOwnList(beanList); 
					//2. Dohvat taskova dobivenih implicitno putem pripadnosti grupi
					taskList = dh.getToDoListDAO().getOwnGroupTasks(em, userID); 
					em.clear(); 
					beanList = new ArrayList<ToDoBean>();
					taskToBeanTransform(taskList, beanList, u);
					data.getOwnList().addAll(beanList); 
					Collections.sort(data.getOwnList()); //Prirodni sort za beanove - po datumu
					
					//Dohvat liste zadanih zadataka
					if(data.isRenderBothToDoLists()){
						//1. Dohvat liste taskova zadanih individualnim korisnicima tj. direktno.
						List<ToDoTask> assignedTaskList = null;
						assignedTaskList = dh.getToDoListDAO().getTasksAssignedToOthers(em, userID);
						beanList = new ArrayList<ToDoBean>();
						taskToBeanTransform(assignedTaskList, beanList, u);
						data.setAssignedList(beanList);		
						//2. Dohvat zadanih grupnih taskova
						List<ToDoTask> groupTasks = dh.getToDoListDAO().getGroupTasksAssignedToOthers(em, userID);
						List<ToDoBean> groupBeans = new ArrayList<ToDoBean>(groupTasks.size());
						taskToBeanTransform(groupTasks, groupBeans, u);
						for(ToDoBean bean : groupBeans){
							if(bean.getSubTasks()!=null){
								for(ToDoBean child : bean.getSubTasks()){
									child.setPercentClosed(taskStatistics(dh, em, child));
								}
							}
							bean.setPercentClosed(taskStatistics(dh, em, bean));
						}
						data.getAssignedList().addAll(groupBeans);
						
						Collections.sort(data.getAssignedList(), new Comparator<ToDoBean>() {
							@Override
							public int compare(ToDoBean o1, ToDoBean o2) {
								if( (o1.getStatus().equals(ToDoTaskStatus.OPEN) || o1.getStatus().equals(ToDoTaskStatus.GROUP_TASK))
										&&
									(o2.getStatus().equals(ToDoTaskStatus.OPEN) || o2.getStatus().equals(ToDoTaskStatus.GROUP_TASK))	
								){
									return o1.getDeadline().compareTo(o2.getDeadline());
								}
								else	
								return o1.getStatus().compareTo(o2.getStatus());
							}
						});
					}
					
					//Dohvat dostupnih predložaka (vlastitih + javnih)
					loadTemplates(em, data);
					return null;
				}
				
				private String taskStatistics(DAOHelper dh, EntityManager em, ToDoBean bean){
					Integer openCount = dh.getToDoListDAO().getChildrenWithStatus(em, bean.getId(), ToDoTaskStatus.OPEN).size();
					Integer closedCount = dh.getToDoListDAO().getChildrenWithStatus(em, bean.getId(), ToDoTaskStatus.CLOSED).size();
					float c = ((float)closedCount / (openCount + closedCount)) * 100;
					DecimalFormat df = new DecimalFormat("00.0");
					return closedCount + "/" + (openCount+closedCount) + " closed (" + df.format(c) + "%)";
				}
			}
		);
	}
	
	/**
	 * Mini verzija dohvata samo vlastite liste za naslovnicu - NE DIRA PERSISTENCE CONTEXT! 
	 * @param dh
	 * @param em
	 * @param userID
	 * @return
	 */
	public static List<ToDoBean> getOwnList(DAOHelper dh,EntityManager em, Long userID){
		List<ToDoBean> finalList = new ArrayList<ToDoBean>();
		List<ToDoTask> taskList = null;
		taskList = dh.getToDoListDAO().getOwnTasks(em, userID);
		List<ToDoBean> beanList = new ArrayList<ToDoBean>();
		taskToBeanTransform(taskList, beanList, null);
		finalList.addAll(beanList);
		//2. Dohvat taskova dobivenih implicitno putem pripadnosti grupi
		taskList = dh.getToDoListDAO().getOwnGroupTasks(em, userID);
		em.clear();
		beanList = new ArrayList<ToDoBean>();
		taskToBeanTransform(taskList, beanList, null);
		finalList.addAll(beanList);
		Collections.sort(finalList);
		return finalList;
	}
	
	/**
	 * Provjera je li neki korisnik student
	 * @param dh
	 * @param em
	 * @param param
	 * @return
	 */
	protected static boolean checkIsStudent(DAOHelper dh, EntityManager em, Long param){
		//Dohvat uloge korisnika
		UserDAO usr = dh.getUserDAO();
		User user = usr.getUserById(em, param);
		user = usr.getFullUserByUsername(em, user.getUsername());
		boolean returnValue = false;
		for(Role r : user.getUserDescriptor().getRoles()){
			if (r.getName().equals("student")) returnValue=true;
		}
		return returnValue;
	}

	/**
	 * Pomoćna metoda - Transformacija liste dohvaćenih taskova u strukturu beanova prikladnu za prikaz
	 * @param taskList
	 * @param beanList
	 * @param currentUser
	 */
	protected static void taskToBeanTransform(List<ToDoTask> taskList, List<ToDoBean> beanList, User currentUser){
		Map<Long, List<ToDoBean>> helperMap = new HashMap<Long, List<ToDoBean>>();
		if (taskList.size()>1){
			//Prilagodba eventualnih taskova nastalih implicitno putem grupe
			if(taskList.get(0).getParentTask()!=null 
					&& (taskList.get(0).getParentTask().getStatus()==ToDoTaskStatus.GROUP_TASK 
							|| taskList.get(0).getParentTask().getStatus()==ToDoTaskStatus.GROUP_TASK_CLOSED)){
				for(ToDoTask task : taskList){
						//Ako ima grandparenta
						if(task.getParentTask().getParentTask()!=null){
							for(int i=0; i< taskList.size(); i++) {
								if(taskList.get(i).getParentTask().getId()==task.getParentTask().getParentTask().getId()){
									task.setParentTask(taskList.get(i));
								}
							}
						}
				}
				for(ToDoTask task : taskList){
					//Ako nema grandparenta onda mu poništi GROUP_TASK roditelja
					if(task.getParentTask()!=null 
							&& (task.getParentTask().getStatus()==ToDoTaskStatus.GROUP_TASK
							|| task.getParentTask().getStatus()==ToDoTaskStatus.GROUP_TASK_CLOSED)) task.setParentTask(null);
				}
			}
			for(ToDoTask task : taskList) {
				//Ima li trenutni korisnik pravo uređivati/zatvoriti ovaj task
				ToDoBean bean = new ToDoBean(task);
				if(currentUser!=null) bean = testCanEdit(bean, currentUser);
				//1. korak transformacije: Ako task nema roditelja znači da je 
				//on sam roditelj pa ga stavi u konačnu listu beanova
				if (task.getParentTask()==null){
					if(task.getStatus()!=ToDoTaskStatus.CLOSED) beanList.add(bean);
				//Inače ako task ima roditelja, dodaj ga u listu djece za tog roditelja
				//i stvori listu ako je već nema
				}else{
					if (helperMap.get(task.getParentTask().getId()) == null){
						helperMap.put(task.getParentTask().getId(), new ArrayList<ToDoBean>());
					}
					List<ToDoBean> helperList = helperMap.get(task.getParentTask().getId());
					helperList.add(bean);
					helperMap.put(task.getParentTask().getId(), helperList);
				}
			}
			//2. korak transformacije: Pridjeljivanje lista djece roditeljima
			for(ToDoBean bean : beanList){
				if(helperMap.containsKey(bean.getId())){
					bean.setSubTasks(helperMap.get(bean.getId()));
					helperMap.remove(bean.getId());
				}
			}
		}else if (taskList.size()==1){
			if(taskList.get(0).getStatus()!=ToDoTaskStatus.CLOSED){
				beanList.add(new ToDoBean(taskList.get(0)));
				if(currentUser!=null) beanList.set(0, testCanEdit(beanList.get(0), currentUser));
			}
		}
	}
	
	/**
	 * Validacija podataka za novi task
	 * @param messageLogger
	 * @param bean
	 */
	public static boolean checkBeanData(ToDoData data, ToDoBean bean, IMessageLogger messageLogger){
		boolean ok = true;
		if(StringUtil.isStringBlank(bean.getTitle())) {
			messageLogger.addErrorMessage(data.getMessageLogger().getText("Error.ToDoTaskMissingTitle"));
			ok = false;
		}
		if(bean.getDeadline().before(new Date())) {
			messageLogger.addErrorMessage(data.getMessageLogger().getText("Error.ToDoTaskInvalidDeadline"));
			ok = false;
		}
		return ok;
	}
	
	/**
	 * Dodavanje novog podzadatka u aktualni zadatak
	 * @param data
	 * @param newSubTask
	 */
	public static void addSubTask(ToDoData data, ToDoBean newSubTask){
	   	ToDoBean subTask = new ToDoBean();
    	subTask.setTitle(newSubTask.getTitle());
    	subTask.setDescription(newSubTask.getDescription());
    	subTask.setDeadline(newSubTask.getDeadline());
    	subTask.setPriority(newSubTask.getPriority());
    	subTask.setStatus(data.getNewTask().getStatus());
    	data.getSubTasks().add(subTask);
       	//Postavljanje defaultnih vrijednosti polja
    	data.getNewSubTask().setTitle("");
    	data.getNewSubTask().setDescription("");
		SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");
		try{
			data.getNewSubTask().setDeadline(sdf.parse(sdf.format(new Date())));
		}catch(ParseException ignored)
		{}
	}

	/**
	 * Dodavanje novog realizatora
	 * @param data
	 */
	public static void addTaskRealizer(ToDoData data, String user){
     	data.setUserRemoval(false);
     	
     	boolean deletionFlag = false;
     	
     	//Uklanjanje selektiranih realizatora
     	if(data.getRealizers()!=null){
     		Iterator<ToDoRealizerBean> tdrb = data.getRealizers().iterator();
     		while(tdrb.hasNext()){
     			ToDoRealizerBean b = tdrb.next();
     			if(b.getChecked()){
     				deletionFlag = true;
     				data.setUserRemoval(true);
     				tdrb.remove();
     			}
     		}
     	}
     	
     	
     	if(!deletionFlag){
	     	//Pokupiti usera
	     	if(!StringUtil.isStringBlank(user)){
	     		ToDoService.validateRealizer(data, "USER", user);
		    	if(data.getSingleUser()!=null){
		    		data.getRealizers().add(new ToDoRealizerBean(true,user,data.getSingleUser().getId().toString()));
		    	}else if (!data.isUserRemoval()){
		    		data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.ToDoTaskNoSuchRealizer"));
		    	}
	     	}
	     	//Pokupiti grupu
	     	if (!data.getSelectedGroupID().equals("null/null") && !data.getSelectedGroupID().equals("-1")) {
	    		Group t = new Group();
	    		t.setId(Long.valueOf(data.getSelectedGroupID()));
	    		data.setGroupForValidation(t);
	    		ToDoService.validateRealizer(data, "GROUP", null);
	    		data.getRealizers().add(new ToDoRealizerBean(false,data.getGroupForValidation().getName(), data.getGroupForValidation().getId().toString()));
	    		//Dodatno - iz liste individualnih se miče trenutni korisnik
	    		Iterator<ToDoRealizerBean> uii = data.getRealizers().iterator();
	        	while(uii.hasNext()){
	        		ToDoRealizerBean u = uii.next();
	         		if (u.getIDLong().equals(data.getCurrUsr().getUserID())) uii.remove();
	        	}
	    	}
     	}
    	data.setSelectedGroupID(null);
	}

	/**
	 * Validacija i dohvat podataka o zadanom realizatoru zadatka
	 * @param data
	 * @param param
	 */
	public static void validateRealizer(final ToDoData data, final String param, final String userParam){
		PersistenceUtil.executeSingleDatabaseOperation(
				new DatabaseOperation<Void>() {
					@Override
					public Void executeOperation(EntityManager em) {
						DAOHelper dh = DAOHelperFactory.getDAOHelper();
						if(param.equals("USER")){
							UserDAO userDao = dh.getUserDAO();
							User user = null;
							UserText ut = UserText.parse(userParam);
							user = userDao.getUserByJMBAG(em, ut.getJmbag());
							data.setSingleUser(user);
						}else if (param.equals("GROUP")){
							GroupDAO groupDao = dh.getGroupDAO();
							Group g = new Group();
							Group g2 = null;
							g2 = groupDao.get(em, data.getGroupForValidation().getId());
							String isvuCode = g2.getCompositeCourseID().split("/")[1];
							CourseDAO cid = dh.getCourseDAO();
							Course c = cid.get(em, isvuCode);

							//Uredivanje imena grupe
							String fullGroupName = "";
							fullGroupName = c.getName();
							//Dohvat imena svih parent grupa isključivši primarnu grupu predmeta
							String pathElements[] = g2.getRelativePath().split("/");
							String search;
							if (g2.getRelativePath().contains("/")) search = pathElements[0]+"%";
							else search = g2.getRelativePath();
							
							List<Group> groupList = dh.getGroupDAO().findSubgroups(em, g2.getCompositeCourseID(), search);
							String con = "";
							for(String s : pathElements){
								if(con=="") con=s;
								else con += "/" + s;
								for(Group gg : groupList) if(gg.getRelativePath().equals(con)) fullGroupName+= " - " + gg.getName();
							}
							
							
							g.setName(fullGroupName);
							g.setId(g2.getId());
							data.setGroupForValidation(g);
						}
						return null;
					}
				}
			);
	}
	
	/**
	 * Setiranje editabilnosti beana od strane trenutnog korisnika
	 * @param bean
	 * @param u
	 * @return
	 */
	protected static ToDoBean testCanEdit(ToDoBean bean, User u){
		if(bean.getOwner().equals(u)) bean.setCanEdit(true);
		else bean.setCanEdit(false);
		return bean;
	}
	
	/**
	 * Dohvat liste usera za autocomplete 
	 * @param data
	 * @param param
	 */
	public static void getUsersListJSONData(final ToDoUsersListJSONData data, final Long currentUserID, final String userText){
		PersistenceUtil.executeSingleDatabaseOperation(
				new DatabaseOperation<Void>() {
					@Override
					public Void executeOperation(EntityManager em) {
						DAOHelper dh = DAOHelperFactory.getDAOHelper();
						UserText ut = UserText.parse(userText);
						List<User> users = null;
						if(ut.getLastName() != null) {
							if(ut.getFirstName() != null) {
								if(ut.getJmbag() != null) {
									users = dh.getRoleDAO().listWithRole(em, JCMSSecurityConstants.ROLE_COURSE_STAFF, ut.getLastName(), ut.getFirstName(), ut.getJmbag());
									users.addAll(dh.getRoleDAO().listWithRole(em, JCMSSecurityConstants.ROLE_STUDENT, ut.getLastName(), ut.getFirstName(), ut.getJmbag()));
								} else {
									users = dh.getRoleDAO().listWithRole(em, JCMSSecurityConstants.ROLE_COURSE_STAFF, ut.getLastName(), ut.getFirstName());
									users.addAll(dh.getRoleDAO().listWithRole(em, JCMSSecurityConstants.ROLE_STUDENT, ut.getLastName(), ut.getFirstName()));
								}
							} else {
								users = dh.getRoleDAO().listWithRole(em, JCMSSecurityConstants.ROLE_COURSE_STAFF, ut.getLastName());
								users.addAll(dh.getRoleDAO().listWithRole(em, JCMSSecurityConstants.ROLE_STUDENT, ut.getLastName()));
							}
						} else {
							users = new ArrayList<User>();
							// Za omoguciti globalni bezkriterijski dohvat svih koristiti ovo dolje - no ne preporuca se!
							// users = dh.getRoleDAO().listWithRole(em, JCMSSecurityConstants.ROLE_COURSE_STAFF);
						}
						Collections.sort(users, StringUtil.USER_COMPARATOR);
						data.setUsers(users);
						data.setResult(AbstractActionData.RESULT_SUCCESS);
						return null;
					}
				}
			);
	}

}
