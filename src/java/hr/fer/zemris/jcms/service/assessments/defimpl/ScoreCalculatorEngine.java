package hr.fer.zemris.jcms.service.assessments.defimpl;

import hr.fer.zemris.jcms.model.Assessment;
import hr.fer.zemris.jcms.model.AssessmentFlag;
import hr.fer.zemris.jcms.model.AssessmentFlagValue;
import hr.fer.zemris.jcms.model.AssessmentScore;
import hr.fer.zemris.jcms.model.CourseInstance;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.service.assessments.AssessmentStatus;
import hr.fer.zemris.jcms.service.assessments.CalculationException;
import hr.fer.zemris.jcms.service.assessments.DynaCodeEngineFactory;
import hr.fer.zemris.jcms.service.assessments.IAssessmentDataProvider;
import hr.fer.zemris.jcms.service.assessments.IDynaCodeEngine;
import hr.fer.zemris.jcms.service.assessments.IFlagProgramEnvironment;
import hr.fer.zemris.jcms.service.assessments.IScoreCalculatorContext;
import hr.fer.zemris.jcms.service.assessments.IScoreCalculatorEngine;
import hr.fer.zemris.jcms.service.assessments.IScoreProgramEnvironment;
import hr.fer.zemris.jcms.service.assessments.StudentFlag;
import hr.fer.zemris.jcms.service.assessments.StudentScore;
import hr.fer.zemris.jcms.service.assessments.StudentTask;
import hr.fer.zemris.jcms.service.assessments.TaskData;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ScoreCalculatorEngine implements IScoreCalculatorEngine {

	@Override
	public void initCalc(IScoreCalculatorContext context) {
		context.initCalc();
	}
	
	@Override
	public StudentFlag calculateFlag(String flagShortName,
			IScoreCalculatorContext context) {

		context.markFlagCalculation(flagShortName);
		try {
			User user = context.getCurrentUser();
			
			StudentFlag flag = context.getStudentFlag(user.getId(), flagShortName);
			if(flag!=null) {
				return flag;
			}
			
			AssessmentFlag assessmentFlag = context.getAssessmentDataProvider().getAssessmentFlagByShortName(flagShortName);
			AssessmentFlagValue assessmentFlagValue = context.getAssessmentDataProvider().getAssessmentFlagValue(assessmentFlag, user);
			
			try {
			// Sada mozda imam raw podatke zapisane u bazi; krenimo odrediti koja je konačna vrijednost:
			IFlagProgramEnvironment pge = createFlagProgramEnvironment(assessmentFlag, assessmentFlagValue, context);
			pge.execute();
			
			boolean value = false;
			if(!pge.isValueSet()) {
				// System.out.println("Value not set by program! Using default=false.");
			} else {
				value = pge.getValue();
			}
			
			flag = new StudentFlag(assessmentFlag.getId(), user.getId(), value, false);
			context.setStudentFlag(user.getId(), flagShortName, flag);
			return flag;
			} catch(RuntimeException ex) {
				flag = new StudentFlag(assessmentFlag.getId(), user.getId(), false, true);
				context.setStudentFlag(user.getId(), flagShortName, flag);
				throw ex;
			}
		} finally {
			context.unmarkFlagCalculation(flagShortName);
		}
	}

	@Override
	public StudentScore calculateScore(String assessmentShortName,
			IScoreCalculatorContext context) {

		context.markScoreCalculation(assessmentShortName);
		try {
			User user = context.getCurrentUser();
			
			StudentScore score = context.getStudentScore(user.getId(), assessmentShortName);
			if(score!=null) {
				return score;
			}
			
			Assessment assessment = context.getAssessmentDataProvider().getAssessmentByShortName(assessmentShortName);
			AssessmentScore assessmentScore = context.getAssessmentDataProvider().getAssessmentScore(assessment, user);
			try {
				// Sada mozda imam raw podatke zapisane u bazi; krenimo odrediti koja je konačna vrijednost:
				IScoreProgramEnvironment pge = createScoreProgramEnvironment(assessment, assessmentScore, context);
				pge.execute();
				
				boolean passed = false;
				if(!pge.isPassedSet()) {
					// System.out.println("Passed not set by program! Using default=false.");
				} else {
					passed = pge.getPassed();
				}
				
				boolean present = false;
				if(!pge.isPresentSet()) {
					// System.out.println("Present not set by program! Using default=false.");
				} else {
					present = pge.getPresent();
				}
		
				double scoreValue = 0;
				if(!pge.isScoreSet()) {
					// System.out.println("Score not set by program! Using default=0.");
				} else {
					scoreValue = pge.getScore();
				}

				// Ako je provjera zaključana, pregazi je prethodno pohranjenim vrijednostima, ili neutralnim ako prve ne postoje...
				// Jedino na ovaj način možemo osigurati ispravnu propagaciju  efektivnih vrijednosti dalje...
				if(assessment.getAssessmentLocked() || (assessmentScore!=null && assessmentScore.getScoreLocked())) {
					if(assessmentScore!=null) {
						passed = AssessmentStatus.PASSED.equals(assessmentScore.getStatus());
						present = assessmentScore.getPresent();
						scoreValue = assessmentScore.getScore();
					} else {
						passed = false;
						present = false;
						scoreValue = 0;
					}
				}

				// Sada vidi imamo li mozda ulancano dijete?
				Assessment chainedChildAssessment = assessment.getChainedChild();
				// Ako ga imamo:
				if(chainedChildAssessment != null) {
					// Dohvati njegove bodove
					StudentScore childScore = calculateScore(chainedChildAssessment.getShortName(), context);
					// Kao efektivne vrijednosti postavi vrijednosti od njega, ali samo ako je student bio na njemu...
					if(childScore==null) {
						// Ovo se ne smije dogoditi!!!
						throw new RuntimeException("Calculate score returned null.");
					}
					if(childScore.hasErrorOccured()) {
						throw new RuntimeException("Calculate score returned errornous data.");
					}
					if(childScore.getEffectivePresent()) {
						// Dakle, bio je pa pregazi efektivne vrijednosti njegovima
						score = new StudentScore(assessment.getId(), user.getId(), present, scoreValue, passed ? AssessmentStatus.PASSED : AssessmentStatus.FAILED, null, false, childScore.getEffectiveScore(), childScore.getEffectivePresent(), childScore.getEffectiveStatus());
					} else {
						// Dakle, nije bio pa kao efektivne vrijednosti uzmi svoje
						score = new StudentScore(assessment.getId(), user.getId(), present, scoreValue, passed ? AssessmentStatus.PASSED : AssessmentStatus.FAILED, null, false, scoreValue, present, passed ? AssessmentStatus.PASSED : AssessmentStatus.FAILED);
					}
				} else {
					// Nema ulančanog dijeteta, pa kao efektivne vrijednosti uzmi svoje
					score = new StudentScore(assessment.getId(), user.getId(), present, scoreValue, passed ? AssessmentStatus.PASSED : AssessmentStatus.FAILED, null, false, scoreValue, present, passed ? AssessmentStatus.PASSED : AssessmentStatus.FAILED);
				}
				context.setStudentScore(user.getId(), assessmentShortName, score);
				return score;
			} catch(RuntimeException ex) {
				score = new StudentScore(assessment.getId(), user.getId(), false, 0, AssessmentStatus.FAILED, null, true, 0, false, AssessmentStatus.FAILED);
				context.setStudentScore(user.getId(), assessmentShortName, score);
				throw ex;
			}
		} finally {
			context.unmarkScoreCalculation(assessmentShortName);
		}
	}

	private static Class<?>[] scoreConParams = new Class<?>[] {
		IScoreCalculatorEngine.class,
		IScoreCalculatorContext.class,
		boolean.class,
		double.class,
		String.class
	};
	
	private static Class<?>[] flagConParams = new Class<?>[] {
		IScoreCalculatorEngine.class,
		IScoreCalculatorContext.class,
		boolean.class,
		boolean.class
	};
	
	@SuppressWarnings("unchecked")
	private IScoreProgramEnvironment createScoreProgramEnvironment(
			Assessment assessment, AssessmentScore assessmentScore,
			IScoreCalculatorContext context) {
		IDynaCodeEngine codeEngine = DynaCodeEngineFactory.getEngine();
		Class<?> cl = codeEngine.classForProgram("A", assessment.getId(), assessment.getProgram(), assessment.getProgramVersion());
		try {
			Constructor<? extends IScoreProgramEnvironment> con = (Constructor<? extends IScoreProgramEnvironment>)cl.getConstructor(scoreConParams);
			IScoreProgramEnvironment progEnv = con.newInstance(
				this, 
				context, 
				assessmentScore==null?false:assessmentScore.getRawPresent(),
				assessmentScore==null?0:assessmentScore.getRawScore(),
				assessment.getShortName()
			);
			return progEnv;
		} catch(Throwable t) {
			throw new CalculationException("Pogreška pri prevođenju programa ocjenjivanja provjere "+assessment.getShortName()+".", t);
		}
	}

	@SuppressWarnings("unchecked")
	private IFlagProgramEnvironment createFlagProgramEnvironment(
			AssessmentFlag assessmentFlag,
			AssessmentFlagValue assessmentFlagValue,
			IScoreCalculatorContext context) {
		IDynaCodeEngine codeEngine = DynaCodeEngineFactory.getEngine();
		Class<?> cl = codeEngine.classForProgram("F", assessmentFlag.getId(), assessmentFlag.getProgram(), assessmentFlag.getProgramVersion());
		try {
			Constructor<? extends IFlagProgramEnvironment> con = (Constructor<? extends IFlagProgramEnvironment>)cl.getConstructor(flagConParams);
			IFlagProgramEnvironment progEnv = con.newInstance(
				this, 
				context, 
				assessmentFlagValue==null ? false : assessmentFlagValue.getManuallySet(),
				assessmentFlagValue==null ? false : assessmentFlagValue.getManualValue()
			);
			return progEnv;
		} catch(Throwable t) {
			throw new CalculationException("Pogreška pri prevođenju programa zastavice "+assessmentFlag.getShortName()+".", t);
		}
	}

	@Override
	public IScoreCalculatorContext createContext(CourseInstance courseInstance,
			IAssessmentDataProvider assessmentDataProvider) {
		return new ScoreCalculatorContext(assessmentDataProvider, courseInstance);
	}

	private static class ScoreCalculatorContext implements IScoreCalculatorContext {
		private Set<String> markedScoreCalculations = new HashSet<String>();
		private Set<String> markedFlagCalculations = new HashSet<String>();
		private List<String> markOrderStack = new ArrayList<String>();
		private Map<String,String> assessmentNamesCache = new HashMap<String, String>();
		private Map<String,String> flagNamesCache = new HashMap<String, String>();
		private CourseInstance courseInstance;
		private IAssessmentDataProvider assessmentDataProvider;
		// Ključ je identifikator studenta, vrijednost mapa čiji su ključevi kratka imena provjera a vrijednosti bodovi studenta
		private Map<Long,Map<String,StudentScore>> scoreCache = new HashMap<Long, Map<String,StudentScore>>();
		// Ključ je identifikator studenta, vrijednost mapa čiji su ključevi kratka imena zastavica a vrijednosti zastavice studenta
		private Map<Long,Map<String,StudentFlag>> flagCache = new HashMap<Long, Map<String,StudentFlag>>();
		private User currentUser;
		private Map<String,Set<String>> dependencies = new HashMap<String, Set<String>>();
		private Map<String,Map<Integer,ExtendedTaskData>> taskMap = new HashMap<String, Map<Integer,ExtendedTaskData>>();
		private Map<String,String[]> assessmentChildrenShortNames = new HashMap<String, String[]>();
		
		static class ExtendedTaskData {
			private TaskData taskData;
			private Map<Long,List<StudentTask>> studentUploads;
		}

		@Override
		public StudentTask getTask(String componentShortName, int position, String taskName, User user) {
			ExtendedTaskData etd = accessExtendedTaskData(componentShortName, position);
			if(etd==null) return null;
			if(taskName==null) return null;
			Long taskID = etd.taskData.getTaskNameToIDMap().get(taskName);
			List<StudentTask> list = etd.studentUploads.get(user.getId());
			if(list==null) return null;
			for(int i=list.size()-1; i>=0; i--) {
				StudentTask st = list.get(i);
				if(st.getTaskId().equals(taskID)) return st;
			}
			return null;
		}

		@Override
		public List<StudentTask> getTasks(String componentShortName, int position, User user) {
			ExtendedTaskData etd = accessExtendedTaskData(componentShortName, position);
			if(etd==null) return Collections.emptyList();
			List<StudentTask> list = etd.studentUploads.get(user.getId());
			if(list==null) return Collections.emptyList();
			return list;
		}
		
		private String cachedComponentShortName;
		private int cachedPosition;
		private ExtendedTaskData cachedExtendedTaskData;
		
		private ExtendedTaskData accessExtendedTaskData(String componentShortName, int position) {
			if(componentShortName==null) return null;
			if(componentShortName.equals(cachedComponentShortName) && position==cachedPosition) return cachedExtendedTaskData;
			Map<Integer,ExtendedTaskData> map1 = taskMap.get(componentShortName);
			if(map1==null) {
				map1 = new HashMap<Integer, ExtendedTaskData>();
				taskMap.put(componentShortName, map1);
			}
			Integer pos = Integer.valueOf(position);
			ExtendedTaskData etd = map1.get(pos);
			if(etd!=null || map1.containsKey(pos)) {
				cachedComponentShortName = componentShortName;
				cachedPosition = position;
				cachedExtendedTaskData = etd;
				return etd;
			}
			TaskData td = assessmentDataProvider.getAllStudentTaskData(componentShortName, position);
			if(td!=null) {
				Map<Long,List<StudentTask>> studentMap = new HashMap<Long, List<StudentTask>>(1000);
				for(StudentTask st : td.getStudentTasks()) {
					List<StudentTask> l = studentMap.get(st.getUserId());
					if(l == null) {
						l = new ArrayList<StudentTask>(4);
						studentMap.put(st.getUserId(), l);
					}
					l.add(st);
				}
				etd = new ExtendedTaskData();
				etd.studentUploads = studentMap;
				etd.taskData = td;
				map1.put(pos, etd);
				cachedComponentShortName = componentShortName;
				cachedPosition = position;
				cachedExtendedTaskData = etd;
				return etd;
			}
			map1.put(pos, null);
			cachedComponentShortName = componentShortName;
			cachedPosition = position;
			cachedExtendedTaskData = null;
			return null;
		}
		
		@Override
		public boolean existsApplication(String applShortName) {
			return assessmentDataProvider.existsApplication(applShortName);
		}
		
		@Override
		public boolean existsAssessment(String assessmentShortName) {
			return assessmentDataProvider.getAssessmentByShortName(assessmentShortName)!=null;
		}
		
		@Override
		public boolean existsFlag(String flagShortName) {
			return assessmentDataProvider.getAssessmentFlagByShortName(flagShortName)!=null;
		}
		
		@Override
		public String assessmentShortNameForTag(String tagShortName) {
			return assessmentDataProvider.assessmentShortNameForTag(tagShortName);
		}
		
		@Override
		public String flagShortNameForTag(String tagShortName) {
			return assessmentDataProvider.flagShortNameForTag(tagShortName);
		}
		
		@Override
		public Map<String, Set<String>> getDependencies() {
			return dependencies;
		}
		
		@Override
		public void initCalc() {
			markedScoreCalculations.clear();
			markedFlagCalculations.clear();
			markOrderStack.clear();
		}
		
		public ScoreCalculatorContext(
				IAssessmentDataProvider assessmentDataProvider,
				CourseInstance courseInstance) {
			super();
			this.assessmentDataProvider = assessmentDataProvider;
			this.courseInstance = courseInstance;
			// Ova ce petlja vec u mapu dependency-ja dodati poznate relacije roditelj-dijete
			// Ostalo ce kasnije biti dinamici otkriveno
			for(Assessment a : assessmentDataProvider.getKnownAssessments()) {
				for(Assessment c : a.getChildren()) {
					updateAADependencies(a.getShortName(), c.getShortName());
				}
				if(a.getChainedChild() != null) {
					updateAADependencies(a.getShortName(), a.getChainedChild().getShortName());
				}
				// FIX: ovo je posredna veza koja se provjerava kasnije; zato je ovdje izbacena
				// if(a.getAssessmentFlag()!=null) {
				// 	updateAFDependencies(a.getShortName(), a.getAssessmentFlag().getShortName());
				// }
			}
		}
		
		@Override
		public String[] getAssessmentChildren(String assessmentShortName) {
			String[] children = assessmentChildrenShortNames.get(assessmentShortName);
			if(children!=null) return children;
			Assessment a = this.assessmentDataProvider.getAssessmentByShortName(assessmentShortName);
			children = new String[a.getChildren().size()];
			int i = 0;
			for(Assessment c : a.getChildren()) {
				children[i] = c.getShortName();
				i++;
			}
			assessmentChildrenShortNames.put(assessmentShortName, children);
			return children;
		}
		
		public IAssessmentDataProvider getAssessmentDataProvider() {
			return assessmentDataProvider;
		}
		
		public CourseInstance getCourseInstance() {
			return courseInstance;
		}
		
		public StudentScore getStudentScore(Long userID, String assessmentShortName) {
			Map<String,StudentScore> map = scoreCache.get(userID);
			if(map == null) return null;
			StudentScore score = map.get(assessmentShortName);
			return score;
		}
		
		public void setStudentScore(Long userID, String assessmentShortName, StudentScore score) {
			Map<String,StudentScore> map = scoreCache.get(userID);
			if(map == null) {
				map = new HashMap<String, StudentScore>();
				scoreCache.put(userID, map);
			}
			map.put(assessmentShortName, score);
		}
		
		public StudentFlag getStudentFlag(Long userID, String flagShortName) {
			Map<String,StudentFlag> map = flagCache.get(userID);
			if(map == null) return null;
			StudentFlag flag = map.get(flagShortName);
			return flag;
		}
		
		public void setStudentFlag(Long userID, String flagShortName, StudentFlag flag) {
			Map<String,StudentFlag> map = flagCache.get(userID);
			if(map == null) {
				map = new HashMap<String, StudentFlag>();
				flagCache.put(userID, map);
			}
			map.put(flagShortName, flag);
		}

		@Override
		public void markScoreCalculation(String assessmentShortName) {
			if(!markedScoreCalculations.add(assessmentShortName)) {
				// Ako sam vec unutra, imam cirkularni poziv!
				throw new CalculationException("Otkrivena cirkularna ovisnost za provjeru "+assessmentShortName+".");
			}
			String name = assessmentNamesCache.get(assessmentShortName);
			if(name==null) {
				name = "A:"+assessmentShortName;
				assessmentNamesCache.put(assessmentShortName, name);
			}
			markOrderStack.add(name);
		}

		@Override
		public void unmarkScoreCalculation(String assessmentShortName) {
			markedScoreCalculations.remove(assessmentShortName);
			String name = assessmentNamesCache.get(assessmentShortName);
			if(name==null) {
				name = "A:"+assessmentShortName;
				assessmentNamesCache.put(assessmentShortName, name);
			}
			String lastName = markOrderStack.get(markOrderStack.size()-1);
			if(name.equals(lastName)) {
				// sve je OK
				if(markOrderStack.size()>1) updateDependencies();
				markOrderStack.remove(markOrderStack.size()-1);
			} else {
				throw new CalculationException("Pogreška u redosljedu skidanja sa stoga! "+assessmentShortName+".");
			}
		}

		@Override
		public void markFlagCalculation(String flagShortName) {
			if(!markedFlagCalculations.add(flagShortName)) {
				// Ako sam vec unutra, imam cirkularni poziv!
				throw new CalculationException("Otkrivena cirkularna ovisnost za zastavicu "+flagShortName+".");
			}
			String name = flagNamesCache.get(flagShortName);
			if(name==null) {
				name = "F:"+flagShortName;
				flagNamesCache.put(flagShortName, name);
			}
			markOrderStack.add(name);
		}

		@Override
		public void unmarkFlagCalculation(String flagShortName) {
			markedFlagCalculations.remove(flagShortName);
			String name = flagNamesCache.get(flagShortName);
			if(name==null) {
				name = "F:"+flagShortName;
				flagNamesCache.put(flagShortName, name);
			}
			String lastName = markOrderStack.get(markOrderStack.size()-1);
			if(name.equals(lastName)) {
				// sve je OK
				if(markOrderStack.size()>1) updateDependencies();
				markOrderStack.remove(markOrderStack.size()-1);
			} else {
				throw new CalculationException("Pogreška u redosljedu skidanja sa stoga! "+flagShortName+".");
			}
		}

		private void updateDependencies() {
			int s = markOrderStack.size()-1;
			String nameParent = markOrderStack.get(s-1);
			String nameChild = markOrderStack.get(s);
			Set<String> children = dependencies.get(nameParent);
			if(children==null) {
				children = new HashSet<String>();
				dependencies.put(nameParent, children);
			}
			children.add(nameChild);
		}

		private void updateAADependencies(String nonCanonicalParentName, String nonCanonicalChildName) {
			String nameParent = assessmentNamesCache.get(nonCanonicalParentName);
			if(nameParent==null) {
				nameParent = "A:"+nonCanonicalParentName;
				assessmentNamesCache.put(nonCanonicalParentName, nameParent);
			}
			String nameChild = assessmentNamesCache.get(nonCanonicalChildName);
			if(nameChild==null) {
				nameChild = "A:"+nonCanonicalChildName;
				assessmentNamesCache.put(nonCanonicalChildName, nameChild);
			}
			Set<String> children = dependencies.get(nameParent);
			if(children==null) {
				children = new HashSet<String>();
				dependencies.put(nameParent, children);
			}
			children.add(nameChild);
		}

		private void updateAFDependencies(String nonCanonicalAssessmentParentName, String nonCanonicalFlagChildName) {
			String nameParent = assessmentNamesCache.get(nonCanonicalAssessmentParentName);
			if(nameParent==null) {
				nameParent = "A:"+nonCanonicalAssessmentParentName;
				assessmentNamesCache.put(nonCanonicalAssessmentParentName, nameParent);
			}
			String nameChild = flagNamesCache.get(nonCanonicalFlagChildName);
			if(nameChild==null) {
				nameChild = "F:"+nonCanonicalFlagChildName;
				flagNamesCache.put(nonCanonicalFlagChildName, nameChild);
			}
			Set<String> children = dependencies.get(nameParent);
			if(children==null) {
				children = new HashSet<String>();
				dependencies.put(nameParent, children);
			}
			children.add(nameChild);
		}

		@Override
		public User getCurrentUser() {
			return currentUser;
		}

		@Override
		public void setCurrentUser(User user) {
			currentUser = user;
		}
	}
}
