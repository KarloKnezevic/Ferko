package hr.fer.zemris.jcms.service2.course.assessments;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;

import hr.fer.zemris.jcms.JCMSSettings;
import hr.fer.zemris.jcms.activities.types.ScoreActivity;
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
import hr.fer.zemris.jcms.model.ApplicationDefinition;
import hr.fer.zemris.jcms.model.Assessment;
import hr.fer.zemris.jcms.model.AssessmentFlag;
import hr.fer.zemris.jcms.model.AssessmentFlagValue;
import hr.fer.zemris.jcms.model.AssessmentScore;
import hr.fer.zemris.jcms.model.CourseComponent;
import hr.fer.zemris.jcms.model.CourseComponentItem;
import hr.fer.zemris.jcms.model.CourseComponentTask;
import hr.fer.zemris.jcms.model.CourseInstance;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.security.JCMSSecurityManagerFactory;
import hr.fer.zemris.jcms.service.AssessmentStatisticsService;
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
import hr.fer.zemris.jcms.service2.course.CourseInstanceServiceSupport;
import hr.fer.zemris.jcms.web.actions.data.AdminAssessmentRecalcData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;
import hr.fer.zemris.util.StringUtil;

public class AssessmentsRecalcService {

	public static void recalculateAssessments(final EntityManager em, final AdminAssessmentRecalcData data) {
		final List<Long> assessmentIDsForStat = new ArrayList<Long>();
		final String[] komponente = new String[2];

		// Dohvat kolegija
		if(!CourseInstanceServiceSupport.fillCourseInstance(em, data, data.getCourseInstanceID())) return;

		// Dozvole
		boolean canManage = JCMSSecurityManagerFactory.getManager().canManageAssessments(data.getCourseInstance());
		if(!canManage) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}

		final DAOHelper dh = DAOHelperFactory.getDAOHelper();
		
		komponente[0] = data.getCourseInstance().getCourse().getIsvuCode();
		komponente[1] = data.getCourseInstance().getId();
		
		List<Assessment> allAssessments = dh.getAssessmentDAO().listForCourseInstance(em, data.getCourseInstance().getId());
		List<AssessmentFlag> allAssessmentFlags = dh.getAssessmentDAO().listFlagsForCourseInstance(em, data.getCourseInstance().getId());
		List<User> courseUsers = dh.getUserDAO().listUsersOnCourseInstance(em, data.getCourseInstance().getId());

		for(Assessment a : allAssessments) {
			assessmentIDsForStat.add(a.getId());
		}
		
		IScoreCalculatorEngine engine = ScoreCalculatorEngineFactory.getEngine();
		SimpleAssessmentDataProvider prov = new SimpleAssessmentDataProvider();
		prov.offerAssessments(allAssessments);
		prov.offerAssessmentFlags(allAssessmentFlags);
		prov.offerAssessmentScore(dh.getAssessmentDAO().listScoresForCourseInstance(em, data.getCourseInstance()));
		prov.offerAssessmentFlagValues(dh.getAssessmentDAO().listFlagValuesForCourseInstance(em, data.getCourseInstance()));

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
				List<StudentTask> list = dh.getCourseComponentDAO().findStudentTasks(em, data.getCourseInstance().getId(), componentShortName, itemPosition);
				List<CourseComponent> ccList = dh.getCourseComponentDAO().listComponentsOnCourse(em, data.getCourseInstance());
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
			public ApplicationDefinition getApplicationDefinition(String applDefShortName) {
				ApplicationDefinition applDef = applDefMap.get(applDefShortName);
				if(applDef==null) {
					if(applDefMap.containsKey(applDefShortName)) {
						return null;
					}
					applDef = dh.getApplicationDAO().getForShortName(em, data.getCourseInstance(), applDefShortName);
					applDefMap.put(applDefShortName, applDef);
				}
				return applDef;
			}
			
			@Override
			public boolean existsApplicationDefinition(String applDefShortName) {
				ApplicationDefinition applDef = applDefMap.get(applDefShortName);
				if(applDef==null) {
					if(applDefMap.containsKey(applDefShortName)) {
						return false;
					}
					applDef = dh.getApplicationDAO().getForShortName(em, data.getCourseInstance(), applDefShortName);
					applDefMap.put(applDefShortName, applDef);
					return applDef != null;
				}
				return true;
			}
			
			@Override
			public List<StudentApplicationShortBean> getData(String applDefShortName)
					throws CalculationException {
				ApplicationDefinition applDef = applDefMap.get(applDefShortName);
				if(applDef==null) {
					if(applDefMap.containsKey(applDefShortName)) {
						throw new CalculationException("Prijava "+applDefShortName+" ne postoji.");
					}
					applDef = dh.getApplicationDAO().getForShortName(em, data.getCourseInstance(), applDefShortName);
					applDefMap.put(applDefShortName, applDef);
					if(applDef==null) {
						throw new CalculationException("Prijava "+applDefShortName+" ne postoji.");
					}
				}
				return dh.getApplicationDAO().listShortBeansFor(em, data.getCourseInstance(), applDefShortName);
			}
		});
		
		Map<Long,ScoreActivity> activities = new HashMap<Long, ScoreActivity>((int)(courseUsers.size()*1.3));
		Date now = new Date();
		DecimalFormat df = new DecimalFormat("###.##");
		IScoreCalculatorContext context = engine.createContext(data.getCourseInstance(), prov);
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
					// Stvaram po prvi puta zapis o bodovima
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
						// Ako treba stvoriti activity (a treba ako je postavljena zastavica effectivePresent:
						if(score.getEffectivePresent() && a.getVisibility()!='H') {
							ScoreActivity act = activities.get(user.getId());
							String ident = "a"+a.getId()+"#"+df.format(score.getEffectiveScore())+"#"+a.getShortName();
							if(act==null) {
								act = new ScoreActivity(now, data.getCourseInstance().getId(), user.getId(), ident);
								activities.put(user.getId(), act);
							} else {
								act.getComponents().add(ident);
							}
						}
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
						// Ako treba stvoriti activity:
						if(assessmentScore.getEffectivePresent()!=score.getEffectivePresent() || assessmentScore.getEffectiveStatus()!=score.getEffectiveStatus() || Math.abs(assessmentScore.getEffectiveScore()-score.getEffectiveScore())>1E-5) {
							// Ovo vidi ako je visibility na V, ili ako je E i bio je, ili ako je na E i promjena je s bio na nije bio
							if(a.getVisibility()=='V' || (a.getVisibility()=='E' && (score.getEffectivePresent() || (assessmentScore.getEffectivePresent() && !score.getEffectivePresent())))) {
								ScoreActivity act = activities.get(user.getId());
								String ident = "a"+a.getId()+"#"+df.format(score.getEffectiveScore())+"#"+a.getShortName();
								if(act==null) {
									act = new ScoreActivity(now, data.getCourseInstance().getId(), user.getId(), ident);
									activities.put(user.getId(), act);
								} else {
									act.getComponents().add(ident);
							}
							}
						}
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
						if(flag.getValue() && a.getVisibility()!='H') {
							ScoreActivity act = activities.get(user.getId());
							String ident = "f"+a.getId()+"#"+(flag.getValue()?"1":"0")+"#"+a.getShortName();
							if(act==null) {
								act = new ScoreActivity(now, data.getCourseInstance().getId(), user.getId(), ident);
								activities.put(user.getId(), act);
							} else {
								act.getComponents().add(ident);
							}
						}
						assessmentFlagValue.setValue(flag.getValue());
					}
					dh.getAssessmentDAO().save(em, assessmentFlagValue);
					prov.offerSingleAssessmentFlagValue(assessmentFlagValue);
				} else {
					assessmentFlagValue.setError(exc || flag.hasErrorOccured());
					if(exc) {
						assessmentFlagValue.setValue(false);
					} else {
						if(assessmentFlagValue.getValue()!=flag.getValue()) {
							// Samo ako je V, ili E && (true ili true->false)
							if(a.getVisibility()=='V' ||(a.getVisibility()=='E' && (flag.getValue() || (assessmentFlagValue.getValue() && !flag.getValue())))) {
								ScoreActivity act = activities.get(user.getId());
								String ident = "f"+a.getId()+"#"+(flag.getValue()?"1":"0")+"#"+a.getShortName();
								if(act==null) {
									act = new ScoreActivity(now, data.getCourseInstance().getId(), user.getId(), ident);
									activities.put(user.getId(), act);
								} else {
									act.getComponents().add(ident);
								}
							}
						}
						assessmentFlagValue.setValue(flag.getValue());
					}
				}
			}
		}
		
		// Napravi flush tako da ako nesto treba puknuti, pukne sada...
		em.flush();
		
		for(Map.Entry<Long, ScoreActivity> e : activities.entrySet()) {
			JCMSSettings.getSettings().getActivityReporter().addActivity(e.getValue());
		}
		
		CourseScoreTable table = updateRanks(data.getMessageLogger(), data.getCourseInstance(), courseUsers, allAssessments, allAssessmentFlags, prov, context.getDependencies());
		if(table!=null) JCMSCacheFactory.getCache().put(table);
		
		if(!assessmentIDsForStat.isEmpty()) {
			for(Long aid : assessmentIDsForStat) {
				AssessmentStatisticsService.clearStatistics(komponente[0], komponente[1], aid);
			}
		}
		
		if(table!=null) data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.scoreCalculated"));
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}
	
	private static CourseScoreTable updateRanks(IMessageLogger logger, CourseInstance courseInstance, List<User> courseUsers, List<Assessment> allAssessments, List<AssessmentFlag> allFlags, IAssessmentDataProvider prov, Map<String, Set<String>> dependencies) {
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

		if(orderedElements.isEmpty()) {
			if(!unusedNames.isEmpty()) {
				// Moguce cirkularne ovisnosti
				logger.addErrorMessage("Ovisnosti provjera su cirkularne - ne postoji vršni element. Molim podesite ovisnosti tako da nema cikličkih ovisnosti.");
				return null;
			}
			// Upozorenje: nema niti provjera niti zastavica:
			logger.addWarningMessage("Nema definiranih provjera niti zastavica?");
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

	/**
	 * Komparator po vrijednostima zastavice. Napomena: naprije idu vrijednosti <code>true</code> a potom <code>false</code>.
	 */
	public static final Comparator<AssessmentFlagValue> FLAGVALUE_COMPARATOR = new Comparator<AssessmentFlagValue>() {
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

	/**
	 * Komparator po bodovima; prije idu veći bodovi. Napomena: naprije idu svi koji su prošli, a tek onda oni koji su pali, neovisno
	 * o broju bodova!
	 */
	public static final Comparator<AssessmentScore> SCORE_COMPARATOR = new Comparator<AssessmentScore>() {
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
			if(o1.getStatus()==AssessmentStatus.PASSED && o2.getStatus()==AssessmentStatus.FAILED) {
				return -1;
			}
			if(o1.getStatus()==AssessmentStatus.FAILED && o2.getStatus()==AssessmentStatus.PASSED) {
				return 1;
			}
			double razlika = o1.getScore() - o2.getScore();
			double absRazlika = Math.abs(razlika);
			if(absRazlika < 1E-5) return 0; // isti su!
			if(razlika > 0) return -1;
			return 1;
		}
	};
	
	/**
	 * Komparator po efektivnim bodovima; prije idu veći bodovi. Napomena: naprije idu svi koji su prošli, a tek onda oni koji su pali, neovisno
	 * o broju bodova!
	 */
	public static final Comparator<AssessmentScore> EFFECTIVE_SCORE_COMPARATOR = new Comparator<AssessmentScore>() {
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
			if(!o1.getEffectivePresent()) {
				if(!o2.getEffectivePresent()) return 0;
				return 1;
			}
			if(!o2.getEffectivePresent()) return -1;
			if(o1.getEffectiveStatus()==AssessmentStatus.PASSED && o2.getEffectiveStatus()==AssessmentStatus.FAILED) {
				return -1;
			}
			if(o1.getEffectiveStatus()==AssessmentStatus.FAILED && o2.getEffectiveStatus()==AssessmentStatus.PASSED) {
				return 1;
			}
			double razlika = o1.getEffectiveScore() - o2.getEffectiveScore();
			double absRazlika = Math.abs(razlika);
			if(absRazlika < 1E-5) return 0; // isti su!
			if(razlika > 0) return -1;
			return 1;
		}
	};
}
