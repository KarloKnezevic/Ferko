package hr.fer.zemris.jcms.service.assessments.defimpl;

import hr.fer.zemris.jcms.applications.ApplContainer;
import hr.fer.zemris.jcms.applications.IApplBuilderRunner;
import hr.fer.zemris.jcms.applications.model.ApplNamedElement;
import hr.fer.zemris.jcms.applications.model.ApplOptionSelection;
import hr.fer.zemris.jcms.beans.ext.StudentApplicationShortBean;
import hr.fer.zemris.jcms.model.ApplicationDefinition;
import hr.fer.zemris.jcms.model.Assessment;
import hr.fer.zemris.jcms.model.AssessmentFlag;
import hr.fer.zemris.jcms.model.AssessmentFlagValue;
import hr.fer.zemris.jcms.model.AssessmentScore;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.service.assessments.CalculationException;
import hr.fer.zemris.jcms.service.assessments.DynaCodeEngineFactory;
import hr.fer.zemris.jcms.service.assessments.IAssessmentDataProvider;
import hr.fer.zemris.jcms.service.assessments.IOnDemandApplicationsDataCallback;
import hr.fer.zemris.jcms.service.assessments.IOnDemandStudentTaskCallback;
import hr.fer.zemris.jcms.service.assessments.TaskData;
import hr.fer.zemris.jcms.service2.course.applications.EmptyStudentDataProviderImpl;
import hr.fer.zemris.jcms.service2.course.applications.IApplStudentDataProvider;
import hr.fer.zemris.util.StringUtil;

import java.lang.reflect.Constructor;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class SimpleAssessmentDataProvider implements IAssessmentDataProvider {

	private Map<String, Assessment> mapAssessmentByShortName = new HashMap<String, Assessment>();
	private Map<String, Map<User,AssessmentScore>> mapUsersByAssessmentShortName = new HashMap<String, Map<User,AssessmentScore>>();
	private Map<String, AssessmentFlag> mapAssessmentFlagByShortName = new HashMap<String, AssessmentFlag>();
	private Map<String, Map<User,AssessmentFlagValue>> mapUsersByAssessmentFlagShortName = new HashMap<String, Map<User,AssessmentFlagValue>>();
	private IOnDemandApplicationsDataCallback applicationDataCallback;
	private IOnDemandStudentTaskCallback studentTaskCallback;
	private Map<String,Map<Long,StudentApplicationShortBean>> applicationMap = new HashMap<String, Map<Long,StudentApplicationShortBean>>();
	private Map<String,String> mapAssessmentShortNameByTag = new HashMap<String, String>();
	private Map<String,String> mapAssessmentFlagShortNameByTag = new HashMap<String, String>();
	private Map<String, ApplContainer> applContainersMap = new HashMap<String, ApplContainer>();
	
	private StudentApplicationShortBean dummyStudentApplicationShortBean;
	
	@Override
	public TaskData getAllStudentTaskData(String componentShortName, int itemPosition) {
		if(studentTaskCallback==null) return null;
		return studentTaskCallback.getAllStudentTaskData(componentShortName, itemPosition);
	}
	
	public SimpleAssessmentDataProvider() {
		dummyStudentApplicationShortBean = new StudentApplicationShortBean(null, "", new Date(), null);
	}

	@Override
	public void installOnDemandStudentTaskCallback(
			IOnDemandStudentTaskCallback callback) {
		this.studentTaskCallback = callback;
	}
	
	@Override
	public void installOnDemandApplicationsDataCallback(
			IOnDemandApplicationsDataCallback callback) {
		this.applicationDataCallback = callback;
	}

	@Override
	public String assessmentShortNameForTag(String tagShortName) {
		return mapAssessmentShortNameByTag.get(tagShortName);
	}
	
	@Override
	public String flagShortNameForTag(String tagShortName) {
		return mapAssessmentFlagShortNameByTag.get(tagShortName);
	}
	
	@Override
	public boolean existsApplication(String applShortName) {
		if(applicationDataCallback==null) {
			throw new CalculationException("Prijave su nedostupne ("+applShortName+").");
		}
		return applicationDataCallback.existsApplicationDefinition(applShortName);
	}

	public Map<Long,StudentApplicationShortBean> getApplicationUserMap(String applShortName) {
		Map<Long,StudentApplicationShortBean> map = applicationMap.get(applShortName);
		if(map==null) {
			if(applicationDataCallback==null) {
				throw new CalculationException("Prijave su nedostupne ("+applShortName+").");
			}
			List<StudentApplicationShortBean> list = applicationDataCallback.getData(applShortName);
			map = new HashMap<Long, StudentApplicationShortBean>(list.size());
			for(StudentApplicationShortBean bean : list) {
				map.put(bean.getUserID(), bean);
			}
			applicationMap.put(applShortName, map);
		}
		return map;
	}
	
	@Override
	public String getApplicationElementValue(String applShortName, User user, String elementName) {
		if(applicationDataCallback==null) {
			throw new CalculationException("Prijave su nedostupne ("+applShortName+").");
		}
		ApplContainer cont = applContainersMap.get(applShortName);
		if(cont == null) {
			try {
				ApplicationDefinition def = applicationDataCallback.getApplicationDefinition(applShortName);
				if(def==null) return null;
				if(StringUtil.isStringBlank(def.getProgram())) return null;
				Class<?> c = DynaCodeEngineFactory.getEngine().classForProgram("P", def.getId(), def.getProgram(), def.getProgramVersion());
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
		StudentApplicationShortBean bean = getStudentApplication(applShortName, user);
		cont.loadUserData(StringUtil.getPropertiesFromString(bean.getDetailedData()));
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
	
	public StudentApplicationShortBean getStudentApplication(String shortName, User user) {
		Map<Long,StudentApplicationShortBean> map = getApplicationUserMap(shortName);
		StudentApplicationShortBean bean = map.get(user.getId());
		return bean==null ? dummyStudentApplicationShortBean : bean;
	}
	
	@Override
	public Assessment getAssessmentByShortName(String assessmentShortName) {
		Assessment a = mapAssessmentByShortName.get(assessmentShortName);
		if(a==null) {
			throw new CalculationException("Provjera čije je kratko ime "+assessmentShortName+" nije dohvatljiva.");
		}
		return a;
	}

	@Override
	public AssessmentScore getAssessmentScore(Assessment assessment, User user) {
		Map<User,AssessmentScore> m = mapUsersByAssessmentShortName.get(assessment.getShortName());
		if(m==null) {
			return null;
		}
		return m.get(user);
	}

	@Override
	public AssessmentFlag getAssessmentFlagByShortName(String flagShortName) {
		AssessmentFlag a = mapAssessmentFlagByShortName.get(flagShortName);
		if(a==null) {
			throw new CalculationException("Zastavica čije je kratko ime "+flagShortName+" nije dohvatljiva.");
		}
		return a;
	}
	
	@Override
	public AssessmentFlagValue getAssessmentFlagValue(
			AssessmentFlag assessmentFlag, User user) {
		Map<User,AssessmentFlagValue> m = mapUsersByAssessmentFlagShortName.get(assessmentFlag.getShortName());
		if(m==null) {
			return null;
		}
		return m.get(user);
	}
	
	public void offerAssessmentScore(List<AssessmentScore> assessmentScores) {
		for(AssessmentScore s : assessmentScores) {
			Assessment a = s.getAssessment();
			if(!mapAssessmentByShortName.containsKey(a.getShortName())) {
				processAssessment(a);
			}
			Map<User,AssessmentScore> m = mapUsersByAssessmentShortName.get(a.getShortName());
			if(m==null) {
				m = new HashMap<User,AssessmentScore>(500);
				mapUsersByAssessmentShortName.put(a.getShortName(), m);
			}
			m.put(s.getUser(), s);
		}
	}

	public void offerAssessments(List<Assessment> assessments) {
		for(Assessment a : assessments) {
			if(!mapAssessmentByShortName.containsKey(a.getShortName())) {
				processAssessment(a);
			}
		}
	}

	private void processAssessment(Assessment a) {
		mapAssessmentByShortName.put(a.getShortName(),a);
		if(a.getAssessmentTag()!=null) {
			mapAssessmentShortNameByTag.put(a.getAssessmentTag().getShortName(), a.getShortName());
		}
	}
	
	public void offerAssessmentFlags(List<AssessmentFlag> assessmentFlags) {
		for(AssessmentFlag a : assessmentFlags) {
			if(!mapAssessmentFlagByShortName.containsKey(a.getShortName())) {
				processAssessmentFlag(a);
			}
		}
	}

	public void offerAssessmentFlagValues(List<AssessmentFlagValue> assessmentFlagValues) {
		for(AssessmentFlagValue s : assessmentFlagValues) {
			AssessmentFlag a = s.getAssessmentFlag();
			if(!mapAssessmentFlagByShortName.containsKey(a.getShortName())) {
				processAssessmentFlag(a);
			}
			Map<User,AssessmentFlagValue> m = mapUsersByAssessmentFlagShortName.get(a.getShortName());
			if(m==null) {
				m = new HashMap<User,AssessmentFlagValue>(500);
				mapUsersByAssessmentFlagShortName.put(a.getShortName(), m);
			}
			m.put(s.getUser(), s);
		}
	}
	
	private void processAssessmentFlag(AssessmentFlag a) {
		mapAssessmentFlagByShortName.put(a.getShortName(),a);
		if(a.getAssesmentFlagTag()!=null) {
			mapAssessmentFlagShortNameByTag.put(a.getAssesmentFlagTag().getShortName(), a.getShortName());
		}
	}
	
	@Override
	public Set<AssessmentFlag> getKnownAssessmentFlags() {
		Set<AssessmentFlag> set = new HashSet<AssessmentFlag>(mapAssessmentFlagByShortName.values());
		return set;
	}
	
	@Override
	public Set<Assessment> getKnownAssessments() {
		Set<Assessment> set = new HashSet<Assessment>(mapAssessmentByShortName.values());
		return set;
	}

	public void offerSingleAssessmentFlagValue(AssessmentFlagValue s) {
		AssessmentFlag a = s.getAssessmentFlag();
		if(!mapAssessmentFlagByShortName.containsKey(a.getShortName())) {
			processAssessmentFlag(a);
		}
		Map<User,AssessmentFlagValue> m = mapUsersByAssessmentFlagShortName.get(a.getShortName());
		if(m==null) {
			m = new HashMap<User,AssessmentFlagValue>(500);
			mapUsersByAssessmentFlagShortName.put(a.getShortName(), m);
		}
		m.put(s.getUser(), s);
	}
	
	public void offerSingleAssessmentScore(AssessmentScore s) {
		Assessment a = s.getAssessment();
		if(!mapAssessmentByShortName.containsKey(a.getShortName())) {
			processAssessment(a);
		}
		Map<User,AssessmentScore> m = mapUsersByAssessmentShortName.get(a.getShortName());
		if(m==null) {
			m = new HashMap<User,AssessmentScore>(500);
			mapUsersByAssessmentShortName.put(a.getShortName(), m);
		}
		m.put(s.getUser(), s);
	}

}
