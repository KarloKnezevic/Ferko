package hr.fer.zemris.jcms.service2.course.applications;

import hr.fer.zemris.jcms.applications.ApplContainer;
import hr.fer.zemris.jcms.applications.IApplBuilderRunner;
import hr.fer.zemris.jcms.applications.model.ApplNamedElement;
import hr.fer.zemris.jcms.applications.model.ApplOptionSelection;
import hr.fer.zemris.jcms.dao.DAOHelperFactory;
import hr.fer.zemris.jcms.model.ApplicationDefinition;
import hr.fer.zemris.jcms.model.Assessment;
import hr.fer.zemris.jcms.model.AssessmentFlag;
import hr.fer.zemris.jcms.model.AssessmentFlagValue;
import hr.fer.zemris.jcms.model.AssessmentScore;
import hr.fer.zemris.jcms.model.CourseComponentTask;
import hr.fer.zemris.jcms.model.CourseInstance;
import hr.fer.zemris.jcms.model.StudentApplication;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.service.assessments.AssessmentStatus;
import hr.fer.zemris.jcms.service.assessments.DynaCodeEngineFactory;
import hr.fer.zemris.jcms.service.assessments.StudentTask;
import hr.fer.zemris.util.StringUtil;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;

public class ApplStudentDataProviderImpl implements IApplStudentDataProvider {
	
	private EntityManager em;
	private CourseInstance ci;
	private User user;
	
	// Sve mape su po kratkim imenima
	private Map<String, Assessment> assessmentsMap = null;
	private Map<String, AssessmentFlag> assessmentFlagsMap = null;
	private Map<String, AssessmentScore> assessmentScoresMap = null;
	private Map<String, AssessmentFlagValue> assessmentFlagValuesMap = null;
	private Map<String, Assessment> assessmentsByTag = null;
	private Map<String, AssessmentFlag> assessmentFlagsByTag = null;
	private Map<String, ApplicationDefinition> applDefsMap = null;
	private Map<String, StudentApplication> studentApplsMap = null;
	// Map<componentDefName, Map<position,List<StudentTask>>>
	private Map<String, Map<Integer,List<StudentTask>>> studentTasks = null;
	// Map<componentDefName, Map<position,Map<taskName,taskID>>>
	private Map<String, Map<Integer,Map<String,Long>>> tasksIDsByName = null;
	private Map<String, ApplContainer> applContainersMap = new HashMap<String, ApplContainer>();

	public void clearStudentData() {
		assessmentScoresMap = null;
		assessmentFlagValuesMap = null;
		studentApplsMap = null;
		studentTasks = null;
	}
	
	public ApplStudentDataProviderImpl(EntityManager em, CourseInstance ci, User user) {
		this.em = em;
		this.ci = ci;
		this.user = user;
	}
	
	@Override
	public boolean assessmentPassed(String assessmentShortName) {
		Assessment a = getAssessment(assessmentShortName);
		AssessmentScore ac = getAssessmentScore(a);
		return ac!=null && ac.getStatus()!=null && ac.getStatus()==AssessmentStatus.PASSED;
	}

	@Override
	public boolean assessmentPresent(String assessmentShortName) {
		Assessment a = getAssessment(assessmentShortName);
		AssessmentScore ac = getAssessmentScore(a);
		return ac!=null && ac.getPresent();
	}

	@Override
	public double assessmentScore(String assessmentShortName) {
		Assessment a = getAssessment(assessmentShortName);
		AssessmentScore ac = getAssessmentScore(a);
		return ac!=null ? ac.getScore() : 0.0;
	}

	@Override
	public boolean existsAssessment(String assessmentName) {
		Assessment a = getAssessment(assessmentName);
		return a!=null;
	}
	
	@Override
	public boolean passed(String assessmentShortName) {
		Assessment a = getAssessment(assessmentShortName);
		AssessmentScore ac = getAssessmentScore(a);
		return ac!=null && ac.getEffectiveStatus()!=null && ac.getEffectiveStatus()==AssessmentStatus.PASSED;
	}

	@Override
	public boolean present(String assessmentShortName) {
		Assessment a = getAssessment(assessmentShortName);
		AssessmentScore ac = getAssessmentScore(a);
		return ac!=null && ac.getEffectivePresent();
	}
	
	@Override
	public double score(String assessmentShortName) {
		Assessment a = getAssessment(assessmentShortName);
		AssessmentScore ac = getAssessmentScore(a);
		return ac!=null ? ac.getEffectiveScore() : 0.0;
	}

	@Override
	public String assessmentShortNameForTag(String tagShortName) {
		if(assessmentsMap==null) initAssessments();
		Assessment a = assessmentsByTag.get(tagShortName);
		return a==null ? null : a.getShortName();
	}

	@Override
	public boolean existsFlag(String flagName) {
		AssessmentFlag a = getAssessmentFlag(flagName);
		return a!=null;
	}
	
	@Override
	public String flagShortNameForTag(String tagShortName) {
		if(assessmentFlagsMap==null) initAssessmentFlags();
		AssessmentFlag a = assessmentFlagsByTag.get(tagShortName);
		return a==null ? null : a.getShortName();
	}

	@Override
	public boolean flagValue(String flagShortName) {
		AssessmentFlag a = getAssessmentFlag(flagShortName);
		AssessmentFlagValue fv = getAssessmentFlagValue(a);
		return fv!=null ? fv.getValue() : false;
	}

	@Override
	public boolean existsApplication(String applShortName) {
		ApplicationDefinition a = getApplicationDefinition(applShortName);
		return a!=null;
	}
	
	@Override
	public boolean hasApplication(String applShortName) {
		StudentApplication a = getStudentApplication(applShortName);
		return a!=null;
	}
	
	@Override
	public Date getApplicationDate(String applShortName) {
		StudentApplication a = getStudentApplication(applShortName);
		return a!=null ? a.getDate() : null;
	}

	@Override
	public String getApplicationElementValue(String applShortName, String elementName) {
		ApplicationDefinition a = getApplicationDefinition(applShortName);
		if(a==null || StringUtil.isStringBlank(a.getProgram())) return null;
		StudentApplication sa = getStudentApplication(applShortName);
		if(sa==null) return null;
		ApplContainer cont = applContainersMap.get(applShortName);
		if(cont == null) {
			try {
				Class<?> c = DynaCodeEngineFactory.getEngine().classForProgram("P", a.getId(), a.getProgram(), a.getProgramVersion());
				if(c==null) {
					return null;
				}
				cont = new ApplContainer();
				cont.setDefinable(false); cont.setExecutable(false);
				Constructor<?> constr = c.getConstructor(ApplContainer.class, IApplStudentDataProvider.class);
				IApplBuilderRunner builderRunner = (IApplBuilderRunner)constr.newInstance(cont, new EmptyStudentDataProviderImpl());
				cont.setDefinable(true);
				builderRunner.buildApplication();
				applContainersMap.put(applShortName, cont);
			} catch(Throwable th) {
				return null;
			}
		}
		cont.loadUserData(StringUtil.getPropertiesFromString(sa.getDetailedData()));
		ApplNamedElement e = cont.getElementByName(elementName);
		if(e==null) return null;
		switch(e.getKind()) {
		case 4: {
			String val = (String)e.getUserData();
			if(val==null) val = "";
			return val;
		}
		case 2: {
			String val = (String)e.getUserData();
			if(val==null) val = "";
			return val;
		}
		case 1: {
			ApplOptionSelection optSel = (ApplOptionSelection)e.getUserData();
			if(optSel==null) return null;
			String text = optSel.getText();
			if(text!=null && text.isEmpty()) text = null;
			if(text==null) return optSel.getKey();
			return optSel.getKey() + ": "+text;
		}
		}
		return null;
	}
	
	@Override
	public boolean hasApplicationInStatus(String applShortName, String status) {
		StudentApplication a = getStudentApplication(applShortName);
		return a!=null ? a.getStatus().name().equals(status) : false;
	}

	@Override
	public boolean hasAssignedTask(String componentShortName, int position, String taskName) {
		return findTask(componentShortName, position, taskName)!=null;
	}
	
	@Override
	public StudentTask task(String componentShortName, int position, String taskName) {
		return findTask(componentShortName, position, taskName);
	}
	
	@Override
	public List<StudentTask> tasks(String componentShortName, int position) {
		return getTaskList(componentShortName, position);
	}
	
	// ------------------------------- POMOĆNE METODE -------------------------------

	private StudentTask findTask(String componentShortName, int position, String taskName) {
		List<StudentTask> list = getTaskList(componentShortName, position);
		Map<String, Long> map = getTaskNamesMap(componentShortName, position);
		if(map==null) return null;
		Long taskID = map.get(taskName);
		if(taskID==null) return null;
		for(int i = 0; i < list.size(); i++) {
			StudentTask st = list.get(i);
			if(st.getTaskId().equals(taskID)) return st;
		}
		return null;
	}
	
	private Map<String, Long> getTaskNamesMap(String componentShortName, int position) {
		boolean query = false;
		Map<Integer,Map<String, Long>> mapByPos = null;
		if(tasksIDsByName==null) {
			tasksIDsByName = new HashMap<String, Map<Integer,Map<String,Long>>>();
			mapByPos = new HashMap<Integer, Map<String,Long>>();
			tasksIDsByName.put(componentShortName, mapByPos);
			query = true;
		} else {
			mapByPos = tasksIDsByName.get(componentShortName);
		}
		Map<String, Long> taskMap = null;
		Integer pos = Integer.valueOf(position);
		if(!query) {
			taskMap = mapByPos.get(pos);
			if(taskMap==null) {
				query = true;
			} else {
				return taskMap;
			}
		}
		// OK, inače radim query
		List<CourseComponentTask> list = DAOHelperFactory.getDAOHelper().getCourseComponentDAO().listTasksForItem(em, ci, componentShortName, position);
		taskMap = new HashMap<String, Long>();
		if(list!=null) {
			for(CourseComponentTask t : list) {
				taskMap.put(t.getTitle(), t.getId());
			}
		}
		mapByPos.put(pos, taskMap);
		return taskMap;
	}
	
	private List<StudentTask> getTaskList(String componentShortName, int position) {
		boolean query = false;
		Map<Integer,List<StudentTask>> mapByPos = null;
		if(studentTasks==null) {
			studentTasks = new HashMap<String, Map<Integer,List<StudentTask>>>();
			mapByPos = new HashMap<Integer, List<StudentTask>>();
			studentTasks.put(componentShortName, mapByPos);
			query = true;
		} else {
			mapByPos = studentTasks.get(componentShortName);
		}
		List<StudentTask> taskList = null;
		Integer pos = Integer.valueOf(position);
		if(!query) {
			taskList = mapByPos.get(pos);
			if(taskList==null) {
				query = true;
			} else {
				return taskList;
			}
		}
		// OK, inače radim query
		List<StudentTask> list = DAOHelperFactory.getDAOHelper().getCourseComponentDAO().findStudentTasks(em, ci.getId(), componentShortName, position);
		taskList = list==null ? new ArrayList<StudentTask>() : list;
		mapByPos.put(pos, Collections.unmodifiableList(taskList));
		return taskList;
	}
	
	private ApplicationDefinition getApplicationDefinition(String applShortName) {
		if(applDefsMap==null) initApplDefsMap();
		return applDefsMap.get(applShortName);
	}
	
	private void initApplDefsMap() {
		if(applDefsMap==null) {
			List<ApplicationDefinition> list = DAOHelperFactory.getDAOHelper().getApplicationDAO().listDefinitions(em, ci.getId());
			applDefsMap = new HashMap<String, ApplicationDefinition>();
			for(ApplicationDefinition a : list) {
				applDefsMap.put(a.getShortName(), a);
			}
		}
	}

	private StudentApplication getStudentApplication(String applShortName) {
		if(studentApplsMap==null) initStudentAppls();
		return studentApplsMap.get(applShortName);
	}
	
	private void initStudentAppls() {
		if(studentApplsMap==null) {
			studentApplsMap = new HashMap<String, StudentApplication>();
			List<StudentApplication> list = DAOHelperFactory.getDAOHelper().getApplicationDAO().listForUser(em, user, ci.getId());
			for(StudentApplication a : list) {
				studentApplsMap.put(a.getApplicationDefinition().getShortName(), a);
			}
		}
	}
	
	private Assessment getAssessment(String assessmentShortName) {
		if(assessmentsMap==null) initAssessments();
		return assessmentsMap.get(assessmentShortName);
	}

	private void initAssessments() {
		if(assessmentsMap==null) {
			assessmentsMap = new HashMap<String, Assessment>();
			assessmentsByTag = new HashMap<String, Assessment>();
			for(Assessment a : ci.getAssessments()) {
				assessmentsMap.put(a.getShortName(), a);
				if(a.getAssessmentTag()!=null) {
					assessmentsByTag.put(a.getAssessmentTag().getShortName(), a);
				}
			}
		}
	}
	private AssessmentScore getAssessmentScore(Assessment a) {
		if(assessmentScoresMap==null) {
			assessmentScoresMap = new HashMap<String, AssessmentScore>();
			List<AssessmentScore> list = DAOHelperFactory.getDAOHelper().getAssessmentDAO().listScoresForCourseInstanceAndUser(em, ci, user);
			for(AssessmentScore ac : list) {
				assessmentScoresMap.put(ac.getAssessment().getShortName(), ac);
			}
		}
		if(a==null) return null;
		return assessmentScoresMap.get(a.getShortName());
	}

	private AssessmentFlagValue getAssessmentFlagValue(AssessmentFlag a) {
		if(assessmentFlagValuesMap==null) {
			assessmentFlagValuesMap = new HashMap<String, AssessmentFlagValue>();
			List<AssessmentFlagValue> list = DAOHelperFactory.getDAOHelper().getAssessmentDAO().listFlagValuesForCourseInstanceAndUser(em, ci, user);
			for(AssessmentFlagValue ac : list) {
				assessmentFlagValuesMap.put(ac.getAssessmentFlag().getShortName(), ac);
			}
		}
		if(a==null) return null;
		return assessmentFlagValuesMap.get(a.getShortName());
	}
	
	private AssessmentFlag getAssessmentFlag(String assessmentFlagShortName) {
		if(assessmentFlagsMap==null) initAssessmentFlags();
		return assessmentFlagsMap.get(assessmentFlagShortName);
	}

	private void initAssessmentFlags() {
		if(assessmentFlagsMap==null) {
			assessmentFlagsMap = new HashMap<String, AssessmentFlag>();
			assessmentFlagsByTag = new HashMap<String, AssessmentFlag>();
			for(AssessmentFlag a : ci.getFlags()) {
				assessmentFlagsMap.put(a.getShortName(), a);
				if(a.getAssesmentFlagTag()!=null) {
					assessmentFlagsByTag.put(a.getAssesmentFlagTag().getShortName(), a);
				}
			}
		}
	}
}
