package hr.fer.zemris.jcms.service;

import hr.fer.zemris.jcms.beans.AssessmentConfigurationSelectorBean;
import hr.fer.zemris.jcms.beans.cached.CourseScoreTable;
import hr.fer.zemris.jcms.beans.cached.Dependencies;
import hr.fer.zemris.jcms.beans.cached.DependencyItem;
import hr.fer.zemris.jcms.beans.cached.STEFlagValue;
import hr.fer.zemris.jcms.beans.cached.STEScore;
import hr.fer.zemris.jcms.beans.cached.STEStudent;
import hr.fer.zemris.jcms.beans.cached.STHEAssessment;
import hr.fer.zemris.jcms.beans.cached.STHEFlag;
import hr.fer.zemris.jcms.beans.cached.STHEStudent;
import hr.fer.zemris.jcms.beans.cached.ScoreTableEntry;
import hr.fer.zemris.jcms.beans.cached.ScoreTableHeaderEntry;
import hr.fer.zemris.jcms.beans.ext.StudentApplicationShortBean;
import hr.fer.zemris.jcms.caching.JCMSCacheFactory;
import hr.fer.zemris.jcms.dao.DAOHelper;
import hr.fer.zemris.jcms.dao.DAOHelperFactory;
import hr.fer.zemris.jcms.dao.DatabaseOperation;
import hr.fer.zemris.jcms.dao.PersistenceUtil;
import hr.fer.zemris.jcms.model.ApplicationDefinition;
import hr.fer.zemris.jcms.model.Assessment;
import hr.fer.zemris.jcms.model.AssessmentConfChoice;
import hr.fer.zemris.jcms.model.AssessmentConfChoiceAnswers;
import hr.fer.zemris.jcms.model.AssessmentConfEnum;
import hr.fer.zemris.jcms.model.AssessmentConfExternal;
import hr.fer.zemris.jcms.model.AssessmentConfPreload;
import hr.fer.zemris.jcms.model.AssessmentConfProblems;
import hr.fer.zemris.jcms.model.AssessmentConfProblemsData;
import hr.fer.zemris.jcms.model.AssessmentConfRange;
import hr.fer.zemris.jcms.model.AssessmentConfiguration;
import hr.fer.zemris.jcms.model.AssessmentFlag;
import hr.fer.zemris.jcms.model.AssessmentFlagValue;
import hr.fer.zemris.jcms.model.AssessmentScore;
import hr.fer.zemris.jcms.model.CourseComponent;
import hr.fer.zemris.jcms.model.CourseComponentItem;
import hr.fer.zemris.jcms.model.CourseComponentTask;
import hr.fer.zemris.jcms.model.CourseInstance;
import hr.fer.zemris.jcms.model.Group;
import hr.fer.zemris.jcms.model.GroupOwner;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.model.UserGroup;
import hr.fer.zemris.jcms.parsers.TextService;
import hr.fer.zemris.jcms.security.JCMSSecurityManagerFactory;
import hr.fer.zemris.jcms.service.assessments.AssessmentStatus;
import hr.fer.zemris.jcms.service.assessments.CalculationException;
import hr.fer.zemris.jcms.service.assessments.IAssessmentDataProvider;
import hr.fer.zemris.jcms.service.assessments.IOnDemandApplicationsDataCallback;
import hr.fer.zemris.jcms.service.assessments.IOnDemandStudentTaskCallback;
import hr.fer.zemris.jcms.service.assessments.IScoreCalculatorContext;
import hr.fer.zemris.jcms.service.assessments.IScoreCalculatorEngine;
import hr.fer.zemris.jcms.service.assessments.ScoreCalculatorEngineFactory;
import hr.fer.zemris.jcms.service.assessments.StudentFlag;
import hr.fer.zemris.jcms.service.assessments.StudentScore;
import hr.fer.zemris.jcms.service.assessments.StudentTask;
import hr.fer.zemris.jcms.service.assessments.TaskData;
import hr.fer.zemris.jcms.service.assessments.defimpl.SimpleAssessmentDataProvider;
import hr.fer.zemris.jcms.web.actions.data.AssessmentFlagExportData;
import hr.fer.zemris.jcms.web.actions.data.AssessmentScoreExportData;
import hr.fer.zemris.jcms.web.actions.data.AssessmentSummaryExportData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;
import hr.fer.zemris.util.DeleteOnCloseFileInputStream;
import hr.fer.zemris.util.StringUtil;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

public class AssessmentService {

	public static final Logger logger = Logger.getLogger(AssessmentService.class);

	private static final List<AssessmentConfigurationSelectorBean> configurationSelectorsList;
	private static final Map<String, Class<?>> configurationSelectorsMapByKey;
	private static final Map<Class<?>, String> configurationSelectorsMapByClass;
	private static final Set<String> assessTypeChangeSupported;
	private static final Map<String, AssessmentConfigurationDeleter> configurationDeletersMapByKey;

	static interface AssessmentConfigurationDeleter {
		public boolean deleteAssessmentConfiguration(EntityManager em, Assessment assessment);
	}
	
	static {
		configurationSelectorsList = new ArrayList<AssessmentConfigurationSelectorBean>();
		configurationSelectorsMapByKey = new HashMap<String, Class<?>>();
		configurationSelectorsMapByClass = new HashMap<Class<?>, String>();
		configurationDeletersMapByKey = new HashMap<String, AssessmentConfigurationDeleter>();
		assessTypeChangeSupported = new HashSet<String>();

		AssessmentConfigurationSelectorBean bean;
		// Prva vrsta:
		bean = new AssessmentConfigurationSelectorBean("PRELOAD","Provjera znanja sa sumarnim rezultatom");
		configurationSelectorsList.add(bean);
		configurationSelectorsMapByKey.put(bean.getId(), AssessmentConfPreload.class);
		configurationSelectorsMapByClass.put(AssessmentConfPreload.class, bean.getId());
		assessTypeChangeSupported.add(bean.getId());
		configurationDeletersMapByKey.put(bean.getId(), new AssessmentConfigurationDeleter() {
			@Override
			public boolean deleteAssessmentConfiguration(EntityManager em,
					Assessment assessment) {
				AssessmentConfiguration ac = assessment.getAssessmentConfiguration();
				if(ac == null) return true;
				assessment.setAssessmentConfiguration(null);
				em.remove(ac);
				em.flush();
				return true;
			}
		});
		// Sljedeca vrsta:
		bean = new AssessmentConfigurationSelectorBean("PROBLEMS","Provjera znanja sa više zadataka bez obrazaca");
		configurationSelectorsList.add(bean);
		configurationSelectorsMapByKey.put(bean.getId(), AssessmentConfProblems.class);
		configurationSelectorsMapByClass.put(AssessmentConfProblems.class, bean.getId());
		assessTypeChangeSupported.add(bean.getId());
		configurationDeletersMapByKey.put(bean.getId(), new AssessmentConfigurationDeleter() {
			@Override
			public boolean deleteAssessmentConfiguration(EntityManager em,
					Assessment assessment) {
				AssessmentConfProblems ac = (AssessmentConfProblems)assessment.getAssessmentConfiguration();
				if(ac == null) return true;
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				List<AssessmentConfProblemsData> list = dh.getAssessmentDAO().listConfProblemsDataForAssessement(em, ac);
				// Ovo brisanje mora ici ovako glupo jedan po jedan zbog ORM cache-eva
				for(AssessmentConfProblemsData d : list) {
					em.remove(d);
				}
				em.flush();
				assessment.setAssessmentConfiguration(null);
				em.remove(ac);
				em.flush();
				return true;
			}
		});
		// Sljedeca vrsta:
		bean = new AssessmentConfigurationSelectorBean("CHOICE","Provjera znanja sa zaokruživanjem na obrascima");
		configurationSelectorsList.add(bean);
		configurationSelectorsMapByKey.put(bean.getId(), AssessmentConfChoice.class);
		configurationSelectorsMapByClass.put(AssessmentConfChoice.class, bean.getId());
		assessTypeChangeSupported.add(bean.getId());
		configurationDeletersMapByKey.put(bean.getId(), new AssessmentConfigurationDeleter() {
			@Override
			public boolean deleteAssessmentConfiguration(EntityManager em,
					Assessment assessment) {
				AssessmentConfChoice ac = (AssessmentConfChoice)assessment.getAssessmentConfiguration();
				if(ac == null) return true;
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				
				List<AssessmentConfChoiceAnswers> list = dh.getAssessmentDAO().listAssessmentConfChoiceAnswersForAssessement(em, ac);
				// Ovo brisanje mora ici ovako glupo jedan po jedan zbog ORM cache-eva
				for(AssessmentConfChoiceAnswers d : list) {
					em.remove(d);
				}
				em.flush();
				assessment.setAssessmentConfiguration(null);
				em.remove(ac);
				em.flush();
				return true;
			}
		});
		// Sljedeca vrsta:
		bean = new AssessmentConfigurationSelectorBean("EXTERNAL","Vanjska provjera znanja");
		configurationSelectorsList.add(bean);
		configurationSelectorsMapByKey.put(bean.getId(), AssessmentConfExternal.class);
		configurationSelectorsMapByClass.put(AssessmentConfExternal.class, bean.getId());
		assessTypeChangeSupported.add(bean.getId());
		configurationDeletersMapByKey.put(bean.getId(), new AssessmentConfigurationDeleter() {
			@Override
			public boolean deleteAssessmentConfiguration(EntityManager em,
					Assessment assessment) {
				AssessmentConfiguration ac = assessment.getAssessmentConfiguration();
				if(ac == null) return true;
				assessment.setAssessmentConfiguration(null);
				em.remove(ac);
				em.flush();
				return true;
			}
		});
		// Sljedeca vrsta:
		bean = new AssessmentConfigurationSelectorBean("ACENUM","Provjera komponente kolegija (intervalni bodovi)");
		configurationSelectorsList.add(bean);
		configurationSelectorsMapByKey.put(bean.getId(), AssessmentConfEnum.class);
		configurationSelectorsMapByClass.put(AssessmentConfEnum.class, bean.getId());
		// Sljedeca vrsta:
		bean = new AssessmentConfigurationSelectorBean("ACRANGE","Provjera komponente kolegija (pobrojani bodovi)");
		configurationSelectorsList.add(bean);
		configurationSelectorsMapByKey.put(bean.getId(), AssessmentConfRange.class);
		configurationSelectorsMapByClass.put(AssessmentConfRange.class, bean.getId());
		// ...
		
	}

	public static List<AssessmentConfigurationSelectorBean> getAllConfigurationSelectors() {
		List<AssessmentConfigurationSelectorBean> list = new ArrayList<AssessmentConfigurationSelectorBean>(configurationSelectorsList);
		return list;
	}

	public static String getKeyForAssessmentConfiguration(AssessmentConfiguration assessmentConfiguration) {
		if(assessmentConfiguration==null) return "";
		Class<?> c = assessmentConfiguration.getClass();
		logger.debug("Vrsta provjere ciji je "+c.getCanonicalName());
		return configurationSelectorsMapByClass.get(c); 
	}
	
	public static List<AssessmentConfigurationSelectorBean> getAllConfigurationSelectors(Assessment assessment) {
		String key = null;
		if(assessment==null) return new ArrayList<AssessmentConfigurationSelectorBean>();
		if(assessment.getAssessmentConfiguration()!=null) {
			key = getKeyForAssessmentConfiguration(assessment.getAssessmentConfiguration());
			if(key==null) {
				return new ArrayList<AssessmentConfigurationSelectorBean>();
			}
			if(!assessTypeChangeSupported.contains(key)) {
				return new ArrayList<AssessmentConfigurationSelectorBean>();
			}
		}
		List<AssessmentConfigurationSelectorBean> list = new ArrayList<AssessmentConfigurationSelectorBean>(configurationSelectorsList);
		Iterator<AssessmentConfigurationSelectorBean> it2 = list.iterator();
		while(it2.hasNext()) {
			AssessmentConfigurationSelectorBean bean = it2.next();
			if(!assessTypeChangeSupported.contains(bean.getId())) {
				it2.remove();
			} else if(key!=null && key.equals(bean.getId())) {
				it2.remove();
			}
		}
		return list;
	}
	
	public static AssessmentConfiguration createAssessmentConfigurationForKey(String key) {
		Class<?> c = configurationSelectorsMapByKey.get(key);
		if(c==null) return null;
		try {
			Object o = c.newInstance();
			return (AssessmentConfiguration)o;
		} catch(Exception ex) {
			return null;
		}
	}

	/**
	 * Metoda briše sve podatke koji su importani za određenu konfiguraciju i briše samu konfiguraciju. Ovo je
	 * potrebno kako bi se konfiguracija mogla promijeniti.
	 * 
	 * @param assessment provjera kojoj je potrebno očistiti konfiguraciju
	 * @return true ako je brisanje konfiguracije uspjelo; false inače
	 */
	public static boolean clearAllAssessmentConfigurationData(EntityManager em, Assessment assessment) {
		if(assessment==null) return true;
		if(assessment.getAssessmentConfiguration()==null) return true;
		String key = getKeyForAssessmentConfiguration(assessment.getAssessmentConfiguration());
		if(key==null) return false;
		AssessmentConfigurationDeleter deleter = configurationDeletersMapByKey.get(key);
		if(deleter==null) return false;
		return deleter.deleteAssessmentConfiguration(em, assessment);
	}
	
	@Deprecated
	public static void updateAllAssessments(final IMessageLogger messageLogger, final Long userID, final String courseInstanceID) {
		final List<Long> assessmentIDsForStat = new ArrayList<Long>();
		final String[] komponente = new String[2];
		
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(final EntityManager em) {
				final DAOHelper dh = DAOHelperFactory.getDAOHelper();
				User currentUser = dh.getUserDAO().getUserById(em, userID);
				if(currentUser==null) {
					messageLogger.addErrorMessage(messageLogger.getText("Error.invalidParameters"));
					return null;
				}

				final CourseInstance ci = dh.getCourseInstanceDAO().get(em, courseInstanceID);
				
				JCMSSecurityManagerFactory.getManager().init(currentUser, em);
				boolean canManage = JCMSSecurityManagerFactory.getManager().canManageAssessments(ci);
				if(!canManage) {
					messageLogger.addErrorMessage(messageLogger.getText("Error.noPermission"));
					return null;
				}

				komponente[0] = ci.getCourse().getIsvuCode();
				komponente[1] = ci.getId();
				
				List<Assessment> allAssessments = dh.getAssessmentDAO().listForCourseInstance(em, courseInstanceID);
				List<AssessmentFlag> allAssessmentFlags = dh.getAssessmentDAO().listFlagsForCourseInstance(em, courseInstanceID);
				List<User> courseUsers = dh.getUserDAO().listUsersOnCourseInstance(em, courseInstanceID);

				for(Assessment a : allAssessments) {
					assessmentIDsForStat.add(a.getId());
				}
				IScoreCalculatorEngine engine = ScoreCalculatorEngineFactory.getEngine();
				SimpleAssessmentDataProvider prov = new SimpleAssessmentDataProvider();
				prov.offerAssessments(allAssessments);
				prov.offerAssessmentFlags(allAssessmentFlags);
				prov.offerAssessmentScore(dh.getAssessmentDAO().listScoresForCourseInstance(em, ci));
				prov.offerAssessmentFlagValues(dh.getAssessmentDAO().listFlagValuesForCourseInstance(em, ci));

				prov.installOnDemandStudentTaskCallback(new IOnDemandStudentTaskCallback() {
					
					Map<String,Map<Integer,TaskData>> cache = new HashMap<String,Map<Integer,TaskData>>();

					@Override
					public TaskData getAllStudentTaskData(String componentShortName, int itemPosition) {
						return accessTaskData(componentShortName, itemPosition);
					}
				
					private TaskData accessTaskData(String componentShortName, int itemPosition) {
						Map<Integer,TaskData> taskDataByPositionMap = cache.get(componentShortName);
						if(taskDataByPositionMap==null) {
							taskDataByPositionMap = new HashMap<Integer, TaskData>();
							cache.put(componentShortName, taskDataByPositionMap);
						}
						Integer pos = Integer.valueOf(itemPosition);
						TaskData td = taskDataByPositionMap.get(pos);
						if(td!=null) return td;
						if(taskDataByPositionMap.containsKey(pos)) return null;
						List<StudentTask> list = dh.getCourseComponentDAO().findStudentTasks(em, courseInstanceID, componentShortName, itemPosition);
						List<CourseComponent> ccList = dh.getCourseComponentDAO().listComponentsOnCourse(em, ci);
						CourseComponent targetCourseComponent = null;
						for(CourseComponent cc : ccList) {
							if(cc.getDescriptor().getShortName().equals(componentShortName)) {
								targetCourseComponent = cc;
								break;
							}
						}
						if(targetCourseComponent!=null) {
							CourseComponentItem targetItem = null;
							for(CourseComponentItem cci : targetCourseComponent.getItems()) {
								if(cci.getPosition()==itemPosition) {
									targetItem = cci;
									break;
								}
							}
							if(targetItem!=null) {
								Map<String, Long> m = new HashMap<String, Long>();
								for(CourseComponentTask cct : targetItem.getTasks()) {
									m.put(cct.getTitle(), cct.getId());
								}
								td = new TaskData(list, m);
								taskDataByPositionMap.put(pos, td);
								return td;
							}
						}
						taskDataByPositionMap.put(pos, null);
						return null;
					}
				});
				
				prov.installOnDemandApplicationsDataCallback(new IOnDemandApplicationsDataCallback() {
					
					Map<String, ApplicationDefinition> applDefMap = new HashMap<String, ApplicationDefinition>();
					
					@Override
					public boolean existsApplicationDefinition(String applDefShortName) {
						ApplicationDefinition applDef = applDefMap.get(applDefShortName);
						if(applDef==null) {
							if(applDefMap.containsKey(applDefShortName)) {
								return false;
							}
							applDef = dh.getApplicationDAO().getForShortName(em, ci, applDefShortName);
							applDefMap.put(applDefShortName, applDef);
							return applDef != null;
						}
						return true;
					}
					
					@Override
					public ApplicationDefinition getApplicationDefinition(
							String applDefShortName) {
						ApplicationDefinition applDef = applDefMap.get(applDefShortName);
						if(applDef==null) {
							if(applDefMap.containsKey(applDefShortName)) {
								return null;
							}
							applDef = dh.getApplicationDAO().getForShortName(em, ci, applDefShortName);
							applDefMap.put(applDefShortName, applDef);
						}
						return applDef;
					}
					
					@Override
					public List<StudentApplicationShortBean> getData(String applDefShortName)
							throws CalculationException {
						ApplicationDefinition applDef = applDefMap.get(applDefShortName);
						if(applDef==null) {
							if(applDefMap.containsKey(applDefShortName)) {
								throw new CalculationException("Prijava "+applDefShortName+" ne postoji.");
							}
							applDef = dh.getApplicationDAO().getForShortName(em, ci, applDefShortName);
							applDefMap.put(applDefShortName, applDef);
							if(applDef==null) {
								throw new CalculationException("Prijava "+applDefShortName+" ne postoji.");
							}
						}
						return dh.getApplicationDAO().listShortBeansFor(em, ci, applDefShortName);
					}
				});
				
				IScoreCalculatorContext context = engine.createContext(ci, prov);
				for(User user : courseUsers) {
					context.setCurrentUser(user);
					for(int i = 0; i < allAssessments.size(); i++) {
						Assessment a = allAssessments.get(i);
						boolean exc = false;
						StudentScore score = null;
						try {
							engine.initCalc(context);
							score = engine.calculateScore(a.getShortName(), context);
						} catch(Exception ex) {
							exc = true;
						}
						AssessmentScore assessmentScore = context.getAssessmentDataProvider().getAssessmentScore(a, user);
						if(assessmentScore==null) {
							assessmentScore = new AssessmentScore();
							assessmentScore.setAssessment(a);
							assessmentScore.setUser(user);
							assessmentScore.setError(exc || score.hasErrorOccured());
							if(exc) {
								assessmentScore.setPresent(false);
								assessmentScore.setRawPresent(false);
								assessmentScore.setRawScore(0);
								assessmentScore.setScore(0);
								assessmentScore.setStatus(AssessmentStatus.FAILED);
								assessmentScore.setEffectivePresent(false);
								assessmentScore.setEffectiveStatus(AssessmentStatus.FAILED);
								assessmentScore.setEffectiveScore(0);
							} else {
								assessmentScore.setPresent(score.getPresent());
								assessmentScore.setRawPresent(false);
								assessmentScore.setRawScore(0);
								assessmentScore.setScore(score.getScore());
								assessmentScore.setStatus(score.getStatus());
								assessmentScore.setEffectivePresent(score.getEffectivePresent());
								assessmentScore.setEffectiveStatus(score.getEffectiveStatus());
								assessmentScore.setEffectiveScore(score.getEffectiveScore());
							}
							em.persist(assessmentScore);
							prov.offerSingleAssessmentScore(assessmentScore);
						} else {
							assessmentScore.setError(exc || score.hasErrorOccured());
							if(exc) {
								assessmentScore.setPresent(false);
								assessmentScore.setScore(0);
								assessmentScore.setStatus(AssessmentStatus.FAILED);
								assessmentScore.setEffectivePresent(false);
								assessmentScore.setEffectiveStatus(AssessmentStatus.FAILED);
								assessmentScore.setEffectiveScore(0);
							} else {
								assessmentScore.setPresent(score.getPresent());
								assessmentScore.setScore(score.getScore());
								assessmentScore.setStatus(score.getStatus());
								assessmentScore.setEffectivePresent(score.getEffectivePresent());
								assessmentScore.setEffectiveStatus(score.getEffectiveStatus());
								assessmentScore.setEffectiveScore(score.getEffectiveScore());
							}
						}
					}
					for(int i = 0; i < allAssessmentFlags.size(); i++) {
						AssessmentFlag a = allAssessmentFlags.get(i);
						boolean exc = false;
						StudentFlag flag = null;
						try {
							engine.initCalc(context);
							flag = engine.calculateFlag(a.getShortName(), context);
						} catch(Exception ex) {
							exc = true;
						}
						// System.out.println(flag);
						AssessmentFlagValue assessmentFlagValue = context.getAssessmentDataProvider().getAssessmentFlagValue(a, user);
						if(assessmentFlagValue==null) {
							assessmentFlagValue = new AssessmentFlagValue();
							assessmentFlagValue.setAssessmentFlag(a);
							assessmentFlagValue.setUser(user);
							assessmentFlagValue.setManuallySet(false);
							assessmentFlagValue.setManualValue(false);
							assessmentFlagValue.setError(exc || flag.hasErrorOccured());
							if(exc) {
								assessmentFlagValue.setValue(false);
							} else {
								assessmentFlagValue.setValue(flag.getValue());
							}
							dh.getAssessmentDAO().save(em, assessmentFlagValue);
							prov.offerSingleAssessmentFlagValue(assessmentFlagValue);
						} else {
							assessmentFlagValue.setError(exc || flag.hasErrorOccured());
							if(exc) {
								assessmentFlagValue.setValue(false);
							} else {
								assessmentFlagValue.setValue(flag.getValue());
							}
						}
					}
				}
				CourseScoreTable table = updateRanks(ci, courseUsers, allAssessments, allAssessmentFlags, prov, context.getDependencies());
				JCMSCacheFactory.getCache().put(table);
				return null;
			}
		});
		if(!assessmentIDsForStat.isEmpty()) {
			for(Long aid : assessmentIDsForStat) {
				AssessmentStatisticsService.clearStatistics(komponente[0], komponente[1], aid);
			}
		}
		messageLogger.addInfoMessage(messageLogger.getText("Info.scoreCalculated"));
	}

	@Deprecated
	public static CourseScoreTable updateRanks(CourseInstance courseInstance, List<User> courseUsers, List<Assessment> allAssessments, List<AssessmentFlag> allFlags, IAssessmentDataProvider prov, Map<String, Set<String>> dependencies) {
		CourseScoreTable cst = new CourseScoreTable();
		Collections.sort(courseUsers, StringUtil.USER_COMPARATOR);

		Map<String,Object> allObjects = new HashMap<String, Object>(allAssessments.size()+allFlags.size());
		Set<String> allNames = new HashSet<String>(allAssessments.size()+allFlags.size());
		Map<String,String> reverseNamesMap = new HashMap<String, String>(allAssessments.size()+allFlags.size());
		for(int i = 0; i < allAssessments.size(); i++) {
			Assessment a = allAssessments.get(i);
			String n = "A:"+a.getShortName();
			String r = a.getShortName()+":A";
			allNames.add(n);
			reverseNamesMap.put(n, r);
			allObjects.put(n, a);
		}
		for(int i = 0; i < allFlags.size(); i++) {
			AssessmentFlag a = allFlags.get(i);
			String n = "F:"+a.getShortName();
			String r = a.getShortName()+":F";
			allNames.add(n);
			reverseNamesMap.put(n, r);
			allObjects.put(n, a);
		}
		Set<String> topNodes = new HashSet<String>(allNames);
		for(Set<String> childrenSet : dependencies.values()) {
			for(String name : childrenSet) {
				topNodes.remove(name);
			}
		}
		
		DependencyItem[] roots = new DependencyItem[topNodes.size()];
		try {
			Map<String,DependencyItem[]> resolvedDependencies = new HashMap<String, DependencyItem[]>(50);
			Map<String,DependencyItem> createdDependencyItems = new HashMap<String, DependencyItem>(50);
			int index = -1;
			for(String name : topNodes) {
				index++;
				DependencyItem[] deps = getDependencies(name, dependencies, createdDependencyItems, resolvedDependencies, reverseNamesMap, 0, allObjects.size());
				DependencyItem de = createdDependencyItems.get(name);
				if(de==null) {
					de = new DependencyItem(reverseNamesMap.get(name), deps);
					createdDependencyItems.put(name, de);
				}
				roots[index] = de;
			}
		} catch(Exception ex) {
			ex.printStackTrace();
			// Ako se je dogodio exception pa ne mogu sloziti hijerarhiju, pomeći ih sve u flat strukturu...
			roots =  new DependencyItem[allNames.size()];
			List<String> allReverseNamesList = new ArrayList<String>();
			for(String n : allNames) {
				allReverseNamesList.add(reverseNamesMap.get(n));
			}
			for(int i = 0; i < roots.length; i++) {
				roots[i] = new DependencyItem(allReverseNamesList.get(i), new DependencyItem[0]);
			}
		}
		Arrays.sort(roots);
		JCMSCacheFactory.getCache().put(new Dependencies(courseInstance.getId(), roots));
		
		// Ono sto preostane u topNodes su sada iskljucivo provjere/bodovi koji nisu nicija djeca - dakle, vrsne komponente.
		List<String> orderedElements = new ArrayList<String>(allObjects.size()*2);
		izgradiTekstStablo(orderedElements, allObjects, topNodes, "", dependencies);
		Set<String> unusedNames = new HashSet<String>(allNames);
		
		// Cache koji cuva na i-toj poziciji Integer objekt cija je vrijednost upravo i
		int brojStupaca = 1 + allAssessments.size() + allFlags.size();
		int cacheSize = courseUsers.size();
		if(cacheSize < brojStupaca) cacheSize = brojStupaca;
		Integer[] icache = new Integer[cacheSize];
		for(int i = 0; i < icache.length; i++) {
			icache[i] = Integer.valueOf(i);
		}

		// Mapa koja cuva za svakog korisnika poziciju retka u tablici gdje se nalaze zapisi za njega...
		Map<Long,Integer> mapUserToRow = new HashMap<Long, Integer>(courseUsers.size());
		for(int i = 0; i < courseUsers.size(); i++) {
			mapUserToRow.put(courseUsers.get(i).getId(), icache[i]);
		}

		List<int[]> indexes = new ArrayList<int[]>(brojStupaca);
		Map<Long, Integer> mapAssessmentToColumn = new HashMap<Long, Integer>();
		Map<Long, Integer> mapFlagToColumn = new HashMap<Long, Integer>();

		for(int i = 0; i < brojStupaca; i++) {
			indexes.add(new int[courseUsers.size()]);
		}
		// Korisnik "i" ima podatke u i-tom retku; inicijalno, to znaci da je sort order po prezimena i imenima korisnika...
		int[] userIndex = indexes.get(0);
		for(int i = 0; i < userIndex.length; i++) {
			userIndex[i] = i;
		}

		List<ScoreTableHeaderEntry> tableHeader = new ArrayList<ScoreTableHeaderEntry>(brojStupaca);
		tableHeader.add(new STHEStudent(null));
		int currentIndex = 1;
		for(String compositeName : orderedElements) {
			int pos = compositeName.lastIndexOf('\t');
			// U ovom imenu oznaka je na kraju! LAB1:A ==> Moras vratiti u original!
			String name = compositeName.substring(pos+1);
			int nodeLength = name.length();
			name = name.substring(nodeLength-1,nodeLength)+":"+name.substring(0,nodeLength-2);
			if(!unusedNames.contains(name)) {
				continue;
			}
			unusedNames.remove(name);
			if(!allNames.contains(name)) {
				System.out.println("Provjera ili zastavica pod imenom "+name+" ne postoji!");
				continue;
			}
			if(name.charAt(0)=='A') {
				Assessment a = (Assessment)allObjects.get(name);
				tableHeader.add(new STHEAssessment(a.getId(), a.getShortName()));
				mapAssessmentToColumn.put(a.getId(), icache[currentIndex]);
				currentIndex++;
			} else {
				AssessmentFlag a = (AssessmentFlag)allObjects.get(name);
				tableHeader.add(new STHEFlag(a.getId(), a.getShortName()));
				mapFlagToColumn.put(a.getId(), icache[currentIndex]);
				currentIndex++;
			}
		}
//		for(int i = 0; i < allAssessments.size(); i++) {
//			Assessment a = allAssessments.get(i);
//			tableHeader.add(new STHEAssessment(a.getId(), a.getShortName()));
//			mapAssessmentToColumn.put(a.getId(), icache[i+1]);
//		}
//		for(int i = 0; i < allFlags.size(); i++) {
//			AssessmentFlag a = allFlags.get(i);
//			tableHeader.add(new STHEFlag(a.getId(), a.getShortName()));
//			mapFlagToColumn.put(a.getId(), icache[i+1+allAssessments.size()]);
//		}
		
		List<ScoreTableEntry[]> table = new ArrayList<ScoreTableEntry[]>(courseUsers.size());
		for(int i = 0; i < courseUsers.size(); i++) {
			table.add(new ScoreTableEntry[brojStupaca]);
		}

		// Popuni prvi stupac
		for(int i = 0; i < courseUsers.size(); i++) {
			User u = courseUsers.get(i);
			table.get(i)[0] = new STEStudent(u.getId(), u.getJmbag(), u.getFirstName(), u.getLastName());
		}
		
		AssessmentScore[] acolumn = new AssessmentScore[courseUsers.size()];
		for(int i = 0; i < allAssessments.size(); i++) {
			Assessment a = allAssessments.get(i);
			int pos = -1;
			for(User user : courseUsers) {
				pos++;
				acolumn[pos] = prov.getAssessmentScore(a, user);
			}
			// Utvrđivanje ranga provjere...
			Arrays.sort(acolumn, SCORE_COMPARATOR);
			int actualRank = 0;
			if(acolumn.length>0) {
				if(acolumn[0]!=null) {
					acolumn[0].setRank(acolumn[0].isError() || !acolumn[0].getPresent() ? (short)30000 : (short)(actualRank+1));
				}
			}
			for(int j = 1; j < acolumn.length; j++) {
				AssessmentScore as = acolumn[j];
				if(as==null) { // Poslije su sve NULL objekti...
					break;
				}
				if(as.isError() || !as.getPresent()) {
					as.setRank((short)30000);
					continue;
				}
				// Inace imam jos jedan normalan zapis:
				double delta = Math.abs(acolumn[j-1].getScore()-acolumn[j].getScore());
				if(delta >= 1E-5) {
					actualRank = j;
				}
				acolumn[j].setRank((short)(actualRank+1));
			}
			// Utvrđivanje efektivnog ranga provjere...
			Arrays.sort(acolumn, EFFECTIVE_SCORE_COMPARATOR);
			actualRank = 0;
			if(acolumn.length>0) {
				if(acolumn[0]!=null) {
					acolumn[0].setEffectiveRank(acolumn[0].isError() || !acolumn[0].getEffectivePresent() ? (short)30000 : (short)(actualRank+1));
				}
			}
			for(int j = 1; j < acolumn.length; j++) {
				AssessmentScore as = acolumn[j];
				if(as==null) { // Poslije su sve NULL objekti...
					break;
				}
				if(as.isError() || !as.getEffectivePresent()) {
					as.setEffectiveRank((short)30000);
					continue;
				}
				// Inace imam jos jedan normalan zapis:
				double delta = Math.abs(acolumn[j-1].getEffectiveScore()-acolumn[j].getEffectiveScore());
				if(delta >= 1E-5) {
					actualRank = j;
				}
				acolumn[j].setEffectiveRank((short)(actualRank+1));
			}
			// Popunjavanje cache tablice i podesavanje indeksa
			for(int j = 0; j < acolumn.length; j++) {
				AssessmentScore as = acolumn[j];
				if(as==null || as.getAssessment()==null || as.getAssessment().getId()==null) {
					continue;
				}
				int row = mapUserToRow.get(as.getUser().getId());
				Integer colInteger = mapAssessmentToColumn.get(as.getAssessment().getId());
				if(colInteger==null) {
					System.out.println("Nisam mapirao nigdje provjeru ID="+as.getAssessment().getId());
					continue;
				}
				int col = colInteger.intValue();
				table.get(row)[col] = new STEScore(as);
				// Ovdje sam po kvaliteti (sortiranju po efektivnim vrijednostima) na j-tom mjestu; stavi u index da se tada treba dohvatiti row-ti redak tablice!
				indexes.get(col)[j] = row;
			}
			
		}

		AssessmentFlagValue[] fcolumn = new AssessmentFlagValue[courseUsers.size()];
		for(int i = 0; i < allFlags.size(); i++) {
			AssessmentFlag a = allFlags.get(i);
			int pos = -1;
			for(User user : courseUsers) {
				pos++;
				fcolumn[pos] = prov.getAssessmentFlagValue(a, user);
			}
			Arrays.sort(fcolumn, FLAGVALUE_COMPARATOR);
			for(int j = 0; j < fcolumn.length; j++) {
				AssessmentFlagValue as = fcolumn[j];
				if(as==null) {
					continue;
				}
				int row = mapUserToRow.get(as.getUser().getId());
				int col = mapFlagToColumn.get(as.getAssessmentFlag().getId());
				table.get(row)[col] = new STEFlagValue(as);
				// Ovdje sam po kvaliteti (sortiranju) na j-tom mjestu; stavi u index da se tada treba dohvatiti row-ti redak tablice!
				indexes.get(col)[j] = row;
			}
		}
		
		cst.setCourseInstanceID(courseInstance.getId());
		cst.setIndexes(indexes);
		cst.setMapUserToRow(mapUserToRow);
		cst.setTableHeader(tableHeader);
		cst.setTableItems(table);
		
		return cst;
	}

	@Deprecated
	private static DependencyItem[] getDependencies(String name,
			Map<String, Set<String>> dependencies,
			Map<String, DependencyItem> createdDependencyItems,
			Map<String, DependencyItem[]> resolvedDependencies,
			Map<String, String> reverseNamesMap, int level, int maxDepth) {
		DependencyItem[] deps = resolvedDependencies.get(name);
		if(deps!=null) return deps;
		if(level>maxDepth) {
			throw new RuntimeException("Circular dependencies?");
		}
		level++;
		Set<String> discoveredDeps = dependencies.get(name);
		if(discoveredDeps==null || discoveredDeps.isEmpty()) {
			deps = new DependencyItem[0];
			resolvedDependencies.put(name, deps);
			return deps;
		}
		deps = new DependencyItem[discoveredDeps.size()];
		int index = -1;
		for(String depName : discoveredDeps) {
			index++;
			DependencyItem de = createdDependencyItems.get(depName);
			if(de==null) {
				DependencyItem[] childDeps = getDependencies(depName, dependencies, createdDependencyItems, resolvedDependencies, reverseNamesMap, level, maxDepth);
				de = createdDependencyItems.get(depName);
				if(de==null) {
					de = new DependencyItem(reverseNamesMap.get(depName), childDeps);
					createdDependencyItems.put(depName, de);
				}
			}
			deps[index] = de;
		}
		DependencyItem de = createdDependencyItems.get(name);
		if(de==null) {
			de = new DependencyItem(reverseNamesMap.get(name), deps);
			createdDependencyItems.put(name, de);
		}
		resolvedDependencies.put(name, deps);
		Arrays.sort(deps);
		return deps;
	}

	@Deprecated
	private static void izgradiTekstStablo(List<String> orderedElements, Map<String, Object> allObjects, Set<String> nodes, String prefix, Map<String, Set<String>> dependencies) {
		if(nodes==null || nodes.isEmpty()) {
			return;
		}
		// Okreni naopacke oznaku je li flag ili provjera, zbog sortiranja! A:LAB1 ==> LAB1:A
		List<String> nodesList = new ArrayList<String>(nodes.size());
		for(String node : nodes) {
			nodesList.add(node.substring(2)+":"+node.substring(0,1));
		}
		Collections.sort(nodesList);
		for(String node : nodesList) {
			String me = prefix.length()==0 ? node : prefix + "\t" + node;
			orderedElements.add(me);
			int nodeLength = node.length();
			String actualName = node.substring(nodeLength-1,nodeLength)+":"+node.substring(0,nodeLength-2);
			Set<String> children = dependencies.get(actualName);
			if(children!=null) {
				izgradiTekstStablo(orderedElements, allObjects, children, me, dependencies);
			}
		}
	}

	@Deprecated
	public static Comparator<AssessmentFlagValue> FLAGVALUE_COMPARATOR = new Comparator<AssessmentFlagValue>() {
		@Override
		public int compare(AssessmentFlagValue o1, AssessmentFlagValue o2) {
			if(o1==null) {
				if(o2==null) return 0;
				return 1;
			}
			if(o2==null) {
				return -1;
			}
			if(o1.getError()) {
				if(o2.getError()) return 0;
				return 1;
			}
			if(o2.getError()) {
				return -1;
			}
			if(o1.getValue()) {
				if(o2.getValue()) return 0;
				return -1;
			}
			if(o2.getValue()) return 1;
			return 0;
		}
	};

	@Deprecated
	public static Comparator<AssessmentScore> SCORE_COMPARATOR = new Comparator<AssessmentScore>() {
		@Override
		public int compare(AssessmentScore o1, AssessmentScore o2) {
			if(o1==null) {
				if(o2==null) return 0;
				return 1;
			}
			if(o2==null) {
				return -1;
			}
			if(o1.isError()) {
				if(o2.isError()) return 0;
				return 1;
			}
			if(o2.isError()) {
				return -1;
			}
			if(!o1.getPresent()) {
				if(!o2.getPresent()) return 0;
				return 1;
			}
			if(!o2.getPresent()) return -1;
			double razlika = o1.getScore() - o2.getScore();
			double absRazlika = Math.abs(razlika);
			if(absRazlika < 1E-5) return 0; // isti su!
			if(razlika > 0) return -1;
			return 1;
		}
	};
	
	@Deprecated
	public static Comparator<AssessmentScore> EFFECTIVE_SCORE_COMPARATOR = new Comparator<AssessmentScore>() {
		@Override
		public int compare(AssessmentScore o1, AssessmentScore o2) {
			if(o1==null) {
				if(o2==null) return 0;
				return 1;
			}
			if(o2==null) {
				return -1;
			}
			if(o1.isError()) {
				if(o2.isError()) return 0;
				return 1;
			}
			if(o2.isError()) {
				return -1;
			}
			if(!o1.getPresent()) {
				if(!o2.getPresent()) return 0;
				return 1;
			}
			if(!o2.getPresent()) return -1;
			double razlika = o1.getEffectiveScore() - o2.getEffectiveScore();
			double absRazlika = Math.abs(razlika);
			if(absRazlika < 1E-5) return 0; // isti su!
			if(razlika > 0) return -1;
			return 1;
		}
	};
	
	public static void prepareScoreExport(final AssessmentScoreExportData data, final Long userID, final String assessmentID, final DeleteOnCloseFileInputStream[] reference, final String format) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				if(!BasicBrowsing.fillCurrentUser(em, data, userID)) return null;
				if(!BasicBrowsing.fillAssessment(em, data, assessmentID)) return null;
				data.setCourseInstance(data.getAssessment().getCourseInstance());

				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				if(!JCMSSecurityManagerFactory.getManager().canManageAssessments(data.getCourseInstance())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}

				String exportFormat = format;
				if(exportFormat==null || (!exportFormat.equals("csv") && !exportFormat.equals("xls"))) {
					exportFormat = "csv";
				}
				String extension = "csv";
				if(exportFormat.equals("xls")) extension = "xls";
				File f = null;
				try {
					f = File.createTempFile("JCMS_", "."+extension);
				} catch (IOException e) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.couldNotCreateTmpFile"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}

				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				List<AssessmentScore> scores = dh.getAssessmentDAO().listScoresForAssessment(em, data.getAssessment());
				IScoreExportAdditionalWriter additionalWriter = null;
				if(data.getAssessment().getAssessmentConfiguration() instanceof AssessmentConfChoice) {
					AssessmentConfChoice ch = (AssessmentConfChoice)data.getAssessment().getAssessmentConfiguration();
					List<AssessmentConfChoiceAnswers> answers = dh.getAssessmentDAO().listAssessmentConfChoiceAnswersForAssessement(em, ch);
					additionalWriter = new ChoiceConfScoreExportAdditionalWriter(ch, answers);
				}
				if(data.getAssessment().getAssessmentConfiguration() instanceof AssessmentConfProblems) {
					AssessmentConfProblems ch = (AssessmentConfProblems)data.getAssessment().getAssessmentConfiguration();
					List<AssessmentConfProblemsData> detailedScores = dh.getAssessmentDAO().listConfProblemsDataForAssessement(em, ch);
					additionalWriter = new ProblemConfScoreExportAdditionalWriter(ch, detailedScores);
				}
				
				BufferedOutputStream os = null;
				try {
					os = new BufferedOutputStream(new FileOutputStream(f));
					if(exportFormat.equals("xls")) {
						exportAssessmentScoreToExcel(data.getMessageLogger(), os, scores, additionalWriter);
					} else {
						exportAssessmentScoreToCsv(data.getMessageLogger(), os, scores, additionalWriter);
					}
					os.close();
				} catch (IOException e) {
					try { if(os!=null) os.close(); } catch(Exception ignorable) {}
					f.delete();
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.couldNotExportData"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				try { if(os!=null) os.close(); } catch(Exception ignorable) {}
				DeleteOnCloseFileInputStream stream = null;
				try {
					stream = new DeleteOnCloseFileInputStream(f);
				} catch (IOException e) {
					f.delete();
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.couldNotExportData"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				stream.setFileName(data.getAssessment().getShortName()+"."+extension);
				if(exportFormat.equals("xls")) {
					stream.setMimeType("application/vnd.ms-excel");
				} else {
					stream.setMimeType("text/csv");
				}
				reference[0] = stream;
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});
	}

	/**
	 * Sučelje koje omogućava podešavanje ispisa različitih vrsta provjera u Csv te Excel dokumente.
	 * @author marcupic
	 *
	 */
	private static interface IScoreExportAdditionalWriter {
		public int getColsCount();
		public String getColumnTitle(int index);
		public String getColumnValue(int index);
		public void setCurrent(AssessmentScore score);
		public boolean useTypedGetter();
		public Object getColumnTypedValue(int index);
	}

	/**
	 * Implementacija koja dodatne podatke čita iz provjere s obrascima.
	 * @author marcupic
	 *
	 */
	private static class ChoiceConfScoreExportAdditionalWriter implements IScoreExportAdditionalWriter {
		private Map<Long, AssessmentConfChoiceAnswers> answersMap;
		private AssessmentConfChoiceAnswers current;
		private String[] cache;
		private int numberOfAnswers;

		@Override
		public boolean useTypedGetter() {
			return false;
		}

		public ChoiceConfScoreExportAdditionalWriter(AssessmentConfChoice conf, List<AssessmentConfChoiceAnswers> answers) {
			answersMap = new HashMap<Long, AssessmentConfChoiceAnswers>(answers.size());
			for(AssessmentConfChoiceAnswers a : answers) {
				answersMap.put(a.getUser().getId(), a);
			}
			numberOfAnswers = conf.getProblemsNum();
			cache = new String[numberOfAnswers*2+1];
		}
		
		@Override
		public String getColumnTitle(int index) {
			if(index==0) {
				return "Grupa";
			}
			index--;
			if(index<numberOfAnswers) {
				return "Z"+(index+1);
			}
			index-=numberOfAnswers;
			return "S"+(index+1);
		}

		@Override
		public String getColumnValue(int index) {
			return cache[index];
		}

		@Override
		public Object getColumnTypedValue(int index) {
			return cache[index];
		}
		
		@Override
		public void setCurrent(AssessmentScore score) {
			current = answersMap.get(score.getUser().getId());
			if(current==null) {
				Arrays.fill(cache, "");
			} else {
				int index = 0;
				cache[index++] = StringUtil.denullify(current.getGroup());
				
				String userAnswers = current.getAnswers();
				String[] userAnswersArray = null;
				if(userAnswers!=null) {
					userAnswersArray = TextService.split(userAnswers, '\t');
				}
				if(userAnswersArray!=null && userAnswersArray.length == numberOfAnswers) {
					for(int i = 0; i < numberOfAnswers; i++) {
						cache[index++] = userAnswersArray[i];
					}
				} else {
					for(int i = 0; i < numberOfAnswers; i++) {
						cache[index++] = "";
					}
				}
				
				String answerStatus = current.getAnswersStatus();
				String[] answerStatusArray = null;
				if(answerStatus!=null) {
					answerStatusArray = TextService.split(answerStatus, '\t');
				}
				if(answerStatusArray!=null && answerStatusArray.length == numberOfAnswers) {
					for(int i = 0; i < numberOfAnswers; i++) {
						cache[index++] = answerStatusArray[i];
					}
				} else {
					for(int i = 0; i < numberOfAnswers; i++) {
						cache[index++] = "";
					}
				}
			}
		}
		
		@Override
		public int getColsCount() {
			return 2*numberOfAnswers+1;
		}
	}

	/**
	 * Implementacija koja dodatne podatke čita iz provjere s brojem bodova po pitanjima.
	 * @author marcupic
	 *
	 */
	private static class ProblemConfScoreExportAdditionalWriter implements IScoreExportAdditionalWriter {
		private Map<Long, AssessmentConfProblemsData> scoreMap;
		private AssessmentConfProblemsData current;
		private int numberOfProblems;
		Object[] cache;
		NumberFormat nf = NumberFormat.getNumberInstance(Locale.ENGLISH);
		
		public ProblemConfScoreExportAdditionalWriter(AssessmentConfProblems conf, List<AssessmentConfProblemsData> detailedScore) {
			scoreMap = new HashMap<Long, AssessmentConfProblemsData>(detailedScore.size());
			for(AssessmentConfProblemsData a : detailedScore) {
				scoreMap.put(a.getUser().getId(), a);
			}
			numberOfProblems = conf.getNumberOfProblems();
			cache = new Object[numberOfProblems+1];
		}

		@Override
		public boolean useTypedGetter() {
			return true;
		}
		
		@Override
		public String getColumnTitle(int index) {
			if(index==0) {
				return "Grupa";
			}
			return "Zad. "+index;
		}

		@Override
		public String getColumnValue(int index) {
			Object o = cache[index];
			if(o==null) return "";
			if(o instanceof String) return (String)o;
			if(o instanceof Double) return nf.format(((Double)o).doubleValue());
			return cache[index].toString();
		}

		@Override
		public Object getColumnTypedValue(int index) {
			Object o = cache[index];
			if(o==null) return "";
			return o;
		}
		
		@Override
		public void setCurrent(AssessmentScore score) {
			current = scoreMap.get(score.getUser().getId());
			if(current==null) {
				Arrays.fill(cache, "");
			} else {
				cache[0] = StringUtil.denullify(current.getGroup());
				
				Double[] userScore = current.getDscore();
				if(userScore==null || userScore.length==0) {
					for(int i = 1; i <= numberOfProblems; i++) {
						cache[i] = "";
					}
				} else {
					for(int i = 1; i <= numberOfProblems; i++) {
						cache[i] = userScore[i-1];
					}
				}
			}
		}
		
		@Override
		public int getColsCount() {
			return numberOfProblems+1;
		}
	}

	protected static void exportAssessmentScoreToExcel(IMessageLogger messageLogger, BufferedOutputStream os, List<AssessmentScore> scores, IScoreExportAdditionalWriter additionalWriter) throws IOException {
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet("Bodovi");
		int numOfAdditionals = additionalWriter==null ? 0 : additionalWriter.getColsCount();
		boolean useTyped = additionalWriter==null ? false : additionalWriter.useTypedGetter();
		sheet.setDefaultColumnWidth((short)20);
		HSSFCellStyle cellStyle = wb.createCellStyle();
		// postavi kao stil tekst, prema http://poi.apache.org/apidocs/org/apache/poi/hssf/usermodel/HSSFDataFormat.html
		cellStyle.setDataFormat((short)0x31); 
		int rowIndex = 0;
		HSSFRow headerRow = sheet.createRow((short)rowIndex);
		HSSFCell headerCell = headerRow.createCell((short)0);
		headerCell.setCellStyle(cellStyle);
		headerCell.setCellValue(new HSSFRichTextString("JMBAG"));
		headerCell = headerRow.createCell((short)1);
		headerCell.setCellStyle(cellStyle);
		headerCell.setCellValue(new HSSFRichTextString("Bodovi"));
		for(int i = 0; i < numOfAdditionals; i++) {
			headerCell = headerRow.createCell((short)(2+i));
			headerCell.setCellStyle(cellStyle);
			headerCell.setCellValue(new HSSFRichTextString(additionalWriter.getColumnTitle(i)));
		}
		for(AssessmentScore s : scores) {
			if(!s.getPresent()) continue;
			if(additionalWriter!=null) {
				additionalWriter.setCurrent(s);
			}
			rowIndex++;
			int columnIndex = 0;
			HSSFRow row = sheet.createRow((short)rowIndex);
			HSSFCell cell = row.createCell((short)columnIndex); columnIndex++;
			cell.setCellStyle(cellStyle);
			cell.setCellValue(new HSSFRichTextString(s.getUser().getJmbag()));
			cell = row.createCell((short)columnIndex); columnIndex++;
			cell.setCellStyle(cellStyle);
			cell.setCellValue(s.getScore());
			for(int i = 0; i < numOfAdditionals; i++) {
				cell = row.createCell((short)columnIndex); columnIndex++;
				cell.setCellStyle(cellStyle);
				if(useTyped) {
					Object o = additionalWriter.getColumnTypedValue(i);
					if(o instanceof String) {
						cell.setCellValue(new HSSFRichTextString((String)o));
					} else if(o instanceof Double) {
						cell.setCellValue(((Double)o).doubleValue());
					} else {
						cell.setCellValue(new HSSFRichTextString(o.toString()));
					}
				} else {
					cell.setCellValue(new HSSFRichTextString(additionalWriter.getColumnValue(i)));
				}
			}
		}
		wb.write(os);
	}

	protected static void exportAssessmentScoreToCsv(IMessageLogger messageLogger, BufferedOutputStream os, List<AssessmentScore> scores, IScoreExportAdditionalWriter additionalWriter) throws IOException {
		OutputStreamWriter w = new OutputStreamWriter(os,"UTF-8");
		int numOfAdditionals = additionalWriter==null ? 0 : additionalWriter.getColsCount();
		w.write("JMBAG\tBodovi");
		for(int i = 0; i < numOfAdditionals; i++) {
			w.write('\t');
			w.write(additionalWriter.getColumnTitle(i));
		}		
		w.write('\r');
		w.write('\n');
		for(AssessmentScore s : scores) {
			if(s.getPresent()) {
				if(additionalWriter!=null) {
					additionalWriter.setCurrent(s);
				}
				w.write(s.getUser().getJmbag());
				w.write('\t');
				w.write(Double.toString(s.getScore()));
				for(int i = 0; i < numOfAdditionals; i++) {
					w.write('\t');
					w.write(additionalWriter.getColumnValue(i));
				}		
				w.write('\r');
				w.write('\n');
			}
		}
		w.flush();
	}

	public static void prepareFlagExport(final AssessmentFlagExportData data, final Long userID, final String assessmentFlagID, final DeleteOnCloseFileInputStream[] reference, final String format) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				if(!BasicBrowsing.fillCurrentUser(em, data, userID)) return null;
				if(!BasicBrowsing.fillAssessmentFlag(em, data, assessmentFlagID)) return null;
				data.setCourseInstance(data.getAssessmentFlag().getCourseInstance());

				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				if(!JCMSSecurityManagerFactory.getManager().canManageAssessments(data.getCourseInstance())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}

				String exportFormat = format;
				if(exportFormat==null || (!exportFormat.equals("csv") && !exportFormat.equals("xls"))) {
					exportFormat = "csv";
				}
				String extension = "csv";
				if(exportFormat.equals("xls")) extension = "xls";
				File f = null;
				try {
					f = File.createTempFile("JCMS_", "."+extension);
				} catch (IOException e) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.couldNotCreateTmpFile"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}

				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				List<AssessmentFlagValue> flagValues = dh.getAssessmentDAO().listFlagValuesForAssessmentFlag(em, data.getAssessmentFlag());
				BufferedOutputStream os = null;
				try {
					os = new BufferedOutputStream(new FileOutputStream(f));
					if(exportFormat.equals("xls")) {
						exportAssessmentFlagToExcel(data.getMessageLogger(), os, flagValues);
					} else {
						OutputStreamWriter w = new OutputStreamWriter(os,"UTF-8");
						for(AssessmentFlagValue s : flagValues) {
							w.write(s.getUser().getJmbag());
							w.write('\t');
							w.write(s.getValue()?"1":"0");
							w.write('\r');
							w.write('\n');
						}
						w.flush();
					}
					os.close();
				} catch (IOException e) {
					try { if(os!=null) os.close(); } catch(Exception ignorable) {}
					f.delete();
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.couldNotExportData"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				try { if(os!=null) os.close(); } catch(Exception ignorable) {}
				DeleteOnCloseFileInputStream stream = null;
				try {
					stream = new DeleteOnCloseFileInputStream(f);
				} catch (IOException e) {
					f.delete();
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.couldNotExportData"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				stream.setFileName(data.getAssessmentFlag().getShortName()+"."+extension);
				if(exportFormat.equals("xls")) {
					stream.setMimeType("application/vnd.ms-excel");
				} else {
					stream.setMimeType("text/csv");
				}
				reference[0] = stream;
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});
	}


	protected static void exportAssessmentFlagToExcel(IMessageLogger messageLogger, BufferedOutputStream os, List<AssessmentFlagValue> flagValues) throws IOException {
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet("Zastavice");
		sheet.setDefaultColumnWidth((short)20);
		HSSFCellStyle cellStyle = wb.createCellStyle();
		// postavi kao stil tekst, prema http://poi.apache.org/apidocs/org/apache/poi/hssf/usermodel/HSSFDataFormat.html
		cellStyle.setDataFormat((short)0x31); 
		int rowIndex = -1;
		for(AssessmentFlagValue s : flagValues) {
			rowIndex++;
			int columnIndex = 0;
			HSSFRow row = sheet.createRow((short)rowIndex);
			HSSFCell cell = row.createCell((short)columnIndex); columnIndex++;
			cell.setCellStyle(cellStyle);
			cell.setCellValue(new HSSFRichTextString(s.getUser().getJmbag()));
			cell = row.createCell((short)columnIndex); columnIndex++;
			cell.setCellStyle(cellStyle);
			cell.setCellValue(s.getValue());
		}
		wb.write(os);
	}

	public static void assessmentSummaryExport(final AssessmentSummaryExportData data, final Long userID, final String courseInstanceID, final DeleteOnCloseFileInputStream[] reference, final String format, final Long selectedGroup) {
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				if(!BasicBrowsing.fillCurrentUser(em, data, userID)) return null;
				if(!BasicBrowsing.fillCourseInstance(em, data, courseInstanceID)) return null;

				JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
				if(!JCMSSecurityManagerFactory.getManager().canViewAssessments(data.getCourseInstance())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}

				String exportFormat = format;
				if(exportFormat==null || (!exportFormat.equals("csv") && !exportFormat.equals("xls"))) {
					exportFormat = "csv";
				}
				String extension = "csv";
				if(exportFormat.equals("xls")) extension = "xls";
				File f = null;
				try {
					f = File.createTempFile("JCMS_", "."+extension);
				} catch (IOException e) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.couldNotCreateTmpFile"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}

				CourseScoreTable table = JCMSCacheFactory.getCache().getCourseScoreTable(data.getCourseInstance().getId());

				if(table==null) {
					data.getMessageLogger().addErrorMessage("Podaci nisu stvoreni. Molim najprije ažurirajte sve bodove i zastavice.");
					data.setResult(AbstractActionData.RESULT_FATAL);
					f.delete();
					return null;
				}
				
				DAOHelper dh = DAOHelperFactory.getDAOHelper();
				List<User> courseUsers = null;
				Group selGroup = null;
				if(selectedGroup==null || selectedGroup.longValue()==-1) {
					// ako moze vidjeti sve, OK, inace error
					if(!JCMSSecurityManagerFactory.getManager().canManageAssessments(data.getCourseInstance())) {
						data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
						data.setResult(AbstractActionData.RESULT_FATAL);
						f.delete();
						return null;
					}
					courseUsers = dh.getCourseInstanceDAO().findCourseUsers(em, data.getCourseInstance().getId());
				} else {
					Group g = dh.getGroupDAO().get(em, selectedGroup);
					if(g==null || !g.getCompositeCourseID().equals(data.getCourseInstance().getId()) || !g.getRelativePath().startsWith("0/")) {
						data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
						data.setResult(AbstractActionData.RESULT_FATAL);
						f.delete();
						return null;
					}
					if(!JCMSSecurityManagerFactory.getManager().canManageAssessments(data.getCourseInstance())) {
						List<GroupOwner> gowners = dh.getGroupDAO().findForGroup(em, g);
						boolean present = false;
						for(GroupOwner go : gowners) {
							if(go.getUser().equals(data.getCurrentUser())) {
								present = true;
								break;
							}
						}
						if(!present) {
							data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
							data.setResult(AbstractActionData.RESULT_FATAL);
							f.delete();
							return null;
						}
					}
					courseUsers = new ArrayList<User>(g.getUsers().size());
					for(UserGroup ug : g.getUsers()) {
						courseUsers.add(ug.getUser());
					}
					selGroup = g;
				}
				Set<Long> ids = new HashSet<Long>(courseUsers.size());
				for(User u : courseUsers) {
					ids.add(u.getId());
				}

				BufferedOutputStream os = null;
				try {
					os = new BufferedOutputStream(new FileOutputStream(f));
					if(exportFormat.equals("xls")) {
						exportAssessmentSummaryToExcel(data.getMessageLogger(), os, table, ids, selGroup);
					} else {
						OutputStreamWriter w = new OutputStreamWriter(os,"UTF-8");
						exportAssessmentSummaryToCsv(data.getMessageLogger(), w, table, ids);
						w.flush();
					}
					os.close();
				} catch (IOException e) {
					try { if(os!=null) os.close(); } catch(Exception ignorable) {}
					f.delete();
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.couldNotExportData"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				try { if(os!=null) os.close(); } catch(Exception ignorable) {}
				DeleteOnCloseFileInputStream stream = null;
				try {
					stream = new DeleteOnCloseFileInputStream(f);
				} catch (IOException e) {
					f.delete();
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.couldNotExportData"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return null;
				}
				stream.setFileName("Summary_"+data.getCourseInstance().getCourse().getIsvuCode()+"."+extension);
				if(exportFormat.equals("xls")) {
					stream.setMimeType("application/vnd.ms-excel");
				} else {
					stream.setMimeType("text/csv");
				}
				reference[0] = stream;
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return null;
			}
		});
	}

	protected static void exportAssessmentSummaryToExcel(IMessageLogger messageLogger, BufferedOutputStream os, CourseScoreTable table, Set<Long> ids, Group selectedGroup) throws IOException {
		HSSFWorkbook wb = new HSSFWorkbook();
		HSSFSheet sheet = wb.createSheet("Stanje");
		sheet.setDefaultColumnWidth((short)20);
		HSSFCellStyle cellStyle = wb.createCellStyle();
		// postavi kao stil tekst, prema http://poi.apache.org/apidocs/org/apache/poi/hssf/usermodel/HSSFDataFormat.html
		cellStyle.setDataFormat((short)0x31); 
		int rowIndex = -1;
		int colIndex = -1;
		rowIndex++;
		HSSFRow row = sheet.createRow((short)rowIndex);
		colIndex++;
		HSSFCell cell = row.createCell((short)colIndex);
		cell.setCellStyle(cellStyle);
		cell.setCellValue(new HSSFRichTextString("JMBAG"));
		colIndex++;
		cell = row.createCell((short)colIndex);
		cell.setCellStyle(cellStyle);
		cell.setCellValue(new HSSFRichTextString("Prezime, Ime"));
		boolean prvi = true;
		for(ScoreTableHeaderEntry e : table.getTableHeader()) {
			if(prvi) {
				prvi = false;
				continue;
			}
			colIndex++;
			cell = row.createCell((short)colIndex);
			cell.setCellStyle(cellStyle);
			cell.setCellValue(new HSSFRichTextString(e.toString()));
		}
		if(selectedGroup!=null) {
			colIndex++;
			cell = row.createCell((short)colIndex);
			cell.setCellStyle(cellStyle);
			cell.setCellValue(new HSSFRichTextString("Grupa"));
		}
		for(ScoreTableEntry[] entries : table.getTableItems()) {
			STEStudent s = (STEStudent)entries[0];
			if(!ids.contains(s.getId())) continue;
			rowIndex++;
			row = sheet.createRow((short)rowIndex);
			cell = row.createCell((short)0);
			cell.setCellStyle(cellStyle);
			cell.setCellValue(new HSSFRichTextString(s.getJmbag()));
			cell = row.createCell((short)1);
			cell.setCellStyle(cellStyle);
			cell.setCellValue(new HSSFRichTextString(s.getLastName()+", "+s.getFirstName()));
			colIndex = 1;
			for(int i = 1; i < entries.length; i++) {
				colIndex++;
				cell = row.createCell((short)colIndex);
				cell.setCellStyle(cellStyle);
				if(entries[i] instanceof STEScore) {
					STEScore en = (STEScore)entries[i];
					if(!en.isError() && en.isPresent()) {
						cell.setCellValue(en.getScore());
					}
				} else {
					STEFlagValue en = (STEFlagValue)entries[i];
					if(!en.isError()) {
						cell.setCellValue(en.isValue());
					}
				}
			}
			if(selectedGroup!=null) {
				colIndex++;
				cell = row.createCell((short)colIndex);
				cell.setCellStyle(cellStyle);
				cell.setCellValue(new HSSFRichTextString(selectedGroup.getName()));
			}
		}
		wb.write(os);
	}
	
	protected static void exportAssessmentSummaryToCsv(IMessageLogger messageLogger, Writer w, CourseScoreTable table, Set<Long> ids) throws IOException {
		w.write("JMBAG\t");
		w.write("Prezime, Ime");
		boolean prvi = true;
		for(ScoreTableHeaderEntry e : table.getTableHeader()) {
			if(prvi) {
				prvi = false;
				continue;
			}
			w.write("\t");
			w.write(e.toString());
		}
		w.write("\n");
		for(ScoreTableEntry[] entries : table.getTableItems()) {
			STEStudent s = (STEStudent)entries[0];
			if(!ids.contains(s.getId())) continue;
			w.write(s.getJmbag());
			w.write("\t");
			w.write(s.getLastName());
			w.write(", ");
			w.write(s.getFirstName());
			for(int i = 1; i < entries.length; i++) {
				w.write("\t");
				if(entries[i] instanceof STEScore) {
					STEScore en = (STEScore)entries[i];
					if(!en.isError() && en.isPresent()) {
						w.write(Double.toString(en.getScore()));
					}
				} else {
					STEFlagValue en = (STEFlagValue)entries[i];
					if(!en.isError()) {
						w.write(en.isValue() ? "1" : "0");
					}
				}
			}
			w.write("\n");
		}
	}
}
