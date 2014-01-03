package hr.fer.zemris.jcms.service2.course.assessments;

import java.io.BufferedReader;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import hr.fer.zemris.jcms.beans.cached.Dependencies;
import hr.fer.zemris.jcms.beans.cached.DependencyItem;
import hr.fer.zemris.jcms.beans.ext.AssessmentViewChoiceBean;
import hr.fer.zemris.jcms.beans.ext.AssessmentViewProblemsBean;
import hr.fer.zemris.jcms.caching.JCMSCacheFactory;
import hr.fer.zemris.jcms.dao.DAOHelper;
import hr.fer.zemris.jcms.dao.DAOHelperFactory;
import hr.fer.zemris.jcms.model.Assessment;
import hr.fer.zemris.jcms.model.AssessmentConfChoice;
import hr.fer.zemris.jcms.model.AssessmentConfChoiceAnswers;
import hr.fer.zemris.jcms.model.AssessmentConfProblems;
import hr.fer.zemris.jcms.model.AssessmentConfProblemsData;
import hr.fer.zemris.jcms.model.AssessmentConfiguration;
import hr.fer.zemris.jcms.model.AssessmentFile;
import hr.fer.zemris.jcms.model.AssessmentFlag;
import hr.fer.zemris.jcms.model.AssessmentFlagValue;
import hr.fer.zemris.jcms.model.AssessmentRoom;
import hr.fer.zemris.jcms.model.AssessmentScore;
import hr.fer.zemris.jcms.model.CourseInstanceKeyValue;
import hr.fer.zemris.jcms.model.Grade;
import hr.fer.zemris.jcms.model.GradesVisibility;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.model.UserGroup;
import hr.fer.zemris.jcms.security.JCMSSecurityManagerFactory;
import hr.fer.zemris.jcms.service.AssessmentService;
import hr.fer.zemris.jcms.service.util.AssessmentUtil;
import hr.fer.zemris.jcms.service2.course.CourseInstanceServiceSupport;
import hr.fer.zemris.jcms.web.actions.data.AssessmentSummaryViewData;
import hr.fer.zemris.jcms.web.actions.data.AssessmentViewData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.util.StringUtil;

import javax.persistence.EntityManager;

public class AssessmentsStudentViewService {

	/**
	 * Metoda dohvaća sve potrebne podatke za prikaz bodovnog stanja studentu, ili nastavniku
	 * koji gleda studentove bodove (ovaj drugi dio treba doimplementirati).
	 * 
	 * @param em entity manager
	 * @param data podatkovni objekt
	 */
	public static void prepareStudentSummaryView(EntityManager em, AssessmentSummaryViewData data) {
		// Dohvat kolegija
		if(!CourseInstanceServiceSupport.fillCourseInstance(em, data, data.getCourseInstanceID())) return;

		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		
		User forUser = data.getCurrentUser();
		data.setImposter(false);
		
		if(data.getStudentID()!=null) {
			forUser = dh.getUserDAO().getUserById(em, data.getStudentID());
			boolean okToImposter = false;
			if(JCMSSecurityManagerFactory.getManager().canManageAssessments(data.getCourseInstance())) {
				// Da ako je ovaj stvarno na ovom kolegiju
				List<UserGroup> ugList = forUser==null ? new ArrayList<UserGroup>() : dh.getGroupDAO().findUserGroupsForUser(em, data.getCourseInstance().getId(), "0", forUser);
				okToImposter = !ugList.isEmpty();
			} else {
				if(forUser!=null) {
					if(JCMSSecurityManagerFactory.getManager().isUserStudentsLecturer(data.getCourseInstance(), forUser)) {
						okToImposter = true;
					}
				}
			}
			if(!okToImposter || forUser==null) {
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
				data.setResult(AbstractActionData.RESULT_FATAL);
				return;
			}
			
			data.setImposter(true);
		} else {
			boolean student = JCMSSecurityManagerFactory.getManager().isStudentOnCourse(data.getCourseInstance());
			if(!student) {
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
				data.setResult(AbstractActionData.RESULT_FATAL);
				return;
			}
		}
		
		data.setStudent(forUser);
		
		if(data.getCourseInstance().getGradingPolicy()==null || data.getCourseInstance().getGradingPolicy().getGradesVisibility()!=GradesVisibility.VISIBLE || !data.getCourseInstance().getGradingPolicy().getGradesValid()) {
			data.setGrade(null);
		} else {
			Grade g = dh.getCourseInstanceDAO().findGradeForCIAndUser(em, data.getCourseInstance(), forUser);
			data.setGrade(g);
		}
		List<AssessmentScore> score = dh.getAssessmentDAO().listScoresForCourseInstanceAndUser(em, data.getCourseInstance(), forUser);
		data.setScore(score);
		List<AssessmentFlagValue> flagValues = dh.getAssessmentDAO().listFlagValuesForCourseInstanceAndUser(em, data.getCourseInstance(), forUser);
		data.setFlagValues(flagValues);

		CourseInstanceKeyValue cikv = dh.getCourseInstanceKeyValueDAO().get(em, data.getCourseInstance(), "studScoreTree");
		List<TreeRenderingClues> renderingClues;
		
		CourseInstanceKeyValue cikv2 = dh.getCourseInstanceKeyValueDAO().get(em, data.getCourseInstance(), "studScoreBrowser");
		String kind = SummaryViewService.resolveStudentScoreBrowser(cikv2);

		if(kind.equals("TREE1") && cikv!=null && cikv.getValue()!=null && !cikv.getValue().isEmpty()) {
			renderingClues = new ArrayList<TreeRenderingClues>();
			constructRenderingClues(em, dh, cikv.getValue(), renderingClues, score, flagValues, data.getCourseInstance().getAssessments(), data.getCourseInstance().getFlags());
		} else {
			// ----- POCETAK: POKUSAJ HIJERARHIJSKE PRIPREME PODATKA; NAJNOVIJE IZDANJE (verzija 2) ---
			Dependencies deps = JCMSCacheFactory.getCache().getDependencies(data.getCourseInstance().getId());
			//data.setDependenciesJSON(deps.toJSONStringBuilder().toString());
			if(deps==null) {
				// Necu prikazati nista!
				renderingClues = new ArrayList<TreeRenderingClues>();
			} else {
				deps = cloneDependencies(deps, data.getCourseInstance().getAssessments(), data.getCourseInstance().getFlags());
				Map<String,Object> objectMap = new HashMap<String, Object>(100);
				Map<String,Object> valueMap = new HashMap<String, Object>(100);
				for(Assessment a : data.getCourseInstance().getAssessments()) {
					objectMap.put(a.getShortName()+":A", a);
				}
				for(AssessmentFlag a : data.getCourseInstance().getFlags()) {
					objectMap.put(a.getShortName()+":F", a);
				}
				for(AssessmentScore s : score) {
					valueMap.put(s.getAssessment().getShortName()+":A", s);
				}
				for(AssessmentFlagValue s : flagValues) {
					valueMap.put(s.getAssessmentFlag().getShortName()+":F", s);
				}
				renderingClues = new ArrayList<TreeRenderingClues>(objectMap.size()*4);
				Set<String> visibles = new HashSet<String>(50);
				for(DependencyItem di : deps.getRoots()) {
					checkAssessmentRenderingVisibility(di, objectMap, valueMap, visibles);
				}
				for(DependencyItem di : deps.getRoots()) {
					fillAssessmentRenderingClues(di, objectMap, valueMap, visibles, renderingClues);
				}
			}
			// ----- KRAJ: POKUSAJ HIJERARHIJSKE PRIPREME PODATKA; NAJNOVIJE IZDANJE (verzija 2) ---
		}
		data.setRenderingClues(renderingClues);
		
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}

	private static class TmpTreeNode {
		List<TmpTreeNode> children = new ArrayList<TmpTreeNode>();
		Object object;
		Object value;
		String kind;
		boolean visible = false;
		
		public void addChild(TmpTreeNode node) {
			children.add(node);
		}
	}
	
	private static void constructRenderingClues(EntityManager em, DAOHelper dh, String hier, 
			List<TreeRenderingClues> renderingClues, List<AssessmentScore> score, List<AssessmentFlagValue> flagValues, Set<Assessment> assessments, Set<AssessmentFlag> flags) {
		Stack<TmpTreeNode> stack = new Stack<TmpTreeNode>();
		TmpTreeNode tree = new TmpTreeNode();
		stack.push(tree);
		BufferedReader br = new BufferedReader(new StringReader(hier));
		try {
			while(true) {
				String line = br.readLine();
				if(line==null) break;
				line = line.trim();
				if(line.isEmpty()) continue;
				if(line.startsWith("%addp:")||line.startsWith("%add:")) {
					String[] x = line.split(":");
					String kind = x[1];
					Long id = Long.valueOf(x[2]);
					// Pronađi takav element
					Object object = null;
					Object value = null;
					if("A".equals(kind)) {
						object = AssessmentUtil.getAssessmentWithID(assessments, id);
						value = AssessmentUtil.getAssessmentScoreWithID(score, id);
						if(value==null) {
							AssessmentScore s = new AssessmentScore();
							s.setAssessment((Assessment)object);
							s.setEffectivePresent(false);
							s.setPresent(false);
							s.setRawPresent(false);
							s.setError(object==null);
							value = s;
						}
						if(object==null) {
							Assessment a = new Assessment();
							a.setName("Obrisana provjera");
							a.setShortName("Obrisana provjera");
							a.setVisibility('H');
							object = a;
						}
					} else if("F".equals(kind)) {
						object = AssessmentUtil.getAssessmentFlagWithID(flags, id);
						value = AssessmentUtil.getAssessmentFlagValueWithID(flagValues, id);
						if(value==null) {
							AssessmentFlagValue s = new AssessmentFlagValue();
							s.setAssessmentFlag((AssessmentFlag)object);
							s.setManuallySet(false);
							s.setValue(false);
							s.setError(object==null);
							value = s;
						}
						if(object==null) {
							AssessmentFlag a = new AssessmentFlag();
							a.setName("Obrisana zastavica");
							a.setShortName("Obrisana zastavica");
							a.setVisibility('H');
							object = a;
						}
					}
					TmpTreeNode newNode = new TmpTreeNode();
					newNode.object = object;
					newNode.value = value;
					newNode.kind = kind;
					stack.peek().addChild(newNode);
					if(x[0].equals("%addp")) {
						stack.push(newNode);
					}
				} else if(line.equals("%pop")) {
					stack.pop();
				}
			}
		} catch(Exception ex) {
			ex.printStackTrace();
		}
		determineVisibilityRek(tree);
		fillTreeRenderingClues(tree, renderingClues);
	}

	private static void fillTreeRenderingClues(TmpTreeNode tree, List<TreeRenderingClues> renderingClues) {
		for(TmpTreeNode child : tree.children) {
			if(child.visible) fillTreeRenderingCluesRecursive(child, renderingClues);
		}
	}

	private static void fillTreeRenderingCluesRecursive(TmpTreeNode node, List<TreeRenderingClues> renderingClues) {
		int kind = 0;
		if("A".equals(node.kind)) kind = TreeRenderingClues.TYPE_ASSESSMENT;
		else if("F".equals(node.kind)) kind = TreeRenderingClues.TYPE_FLAG;
		renderingClues.add(new TreeRenderingClues(TreeRenderingClues.EVENT_ITEM_START, kind, node.object, node.value));
		boolean anyChildVisible = false;
		for(TmpTreeNode child : node.children) {
			if(child.visible) {
				anyChildVisible = true;
				break;
			}
		}
		if(anyChildVisible) {
			renderingClues.add(new TreeRenderingClues(TreeRenderingClues.EVENT_CHILDREN_START, kind, node.object, node.value));
			for(TmpTreeNode child : node.children) {
				if(child.visible) {
					fillTreeRenderingCluesRecursive(child, renderingClues);
				}
			}
			renderingClues.add(new TreeRenderingClues(TreeRenderingClues.EVENT_CHILDREN_END, kind, node.object, node.value));
		}
		renderingClues.add(new TreeRenderingClues(TreeRenderingClues.EVENT_ITEM_END, kind, node.object, node.value));
	}

	private static void determineVisibilityRek(TmpTreeNode tree) {
		if(tree==null) return;
		if("F".equals(tree.kind)) {
			AssessmentFlag f = (AssessmentFlag)tree.object;
			AssessmentFlagValue fv = (AssessmentFlagValue)tree.value;
			if(f.getVisibility()==(char)0 || f.getVisibility()=='V' || (f.getVisibility()=='E' && fv.getValue())) {
				tree.visible = true;
			}
		} else if("A".equals(tree.kind)) {
			Assessment a = (Assessment)tree.object;
			AssessmentScore sc = (AssessmentScore)tree.value;
			if(a.getVisibility()==(char)0 || a.getVisibility()=='V' || (a.getVisibility()=='E' && sc.getEffectivePresent())) {
				tree.visible = true;
			}
		}
		for(TmpTreeNode child : tree.children) {
			determineVisibilityRek(child);
		}
	}

	public static void prepareStudentView(EntityManager em, AssessmentViewData data) {
		// Dohvat provjere
		if(!AssessmentServiceSupport.fillAssessment(em, data, data.getAssessmentID())) return;
		data.setCourseInstanceID(data.getAssessment().getCourseInstance().getId());
		
		Long reqUserID = null;
		if(!StringUtil.isStringBlank(data.getUserID())) {
			try {
				reqUserID = Long.parseLong(data.getUserID());
			} catch(Exception ex) {
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
				data.setResult(AbstractActionData.RESULT_FATAL);
				return;
			}
		}
		DAOHelper dh = DAOHelperFactory.getDAOHelper();

		data.setImposter(false);
		
		User user = data.getCurrentUser();
		if(reqUserID!=null && !reqUserID.equals(data.getCurrentUser().getId())) {
			// Dohvati korisnika, ali ne odmah provjeravati; ne zelimo da ovo netko koristi za provjeru postoji li korisnik
			user = dh.getUserDAO().getUserById(em, reqUserID);
			// Ako je eksplicitno zadan korisnik, to je moglo traziti samo
			// administrativno osoblje ili njegov nastavnik
			boolean canManage = JCMSSecurityManagerFactory.getManager().canManageAssessments(data.getCourseInstance());
			boolean canView = canManage;
			if(!canView && user!=null) {
				if(JCMSSecurityManagerFactory.getManager().isUserStudentsLecturer(data.getCourseInstance(), user)) {
					canView = true;
				}
			}
			if(canView && user==null) {
				canView = false;
			}
			if(!canView) {
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
				data.setResult(AbstractActionData.RESULT_FATAL);
				return;
			}
			data.setImposter(true);
		} else {
			// Vidi je li user uopce na kolegiju
			boolean isStudent = JCMSSecurityManagerFactory.getManager().isStudentOnCourse(data.getCourseInstance());
			if(!isStudent) {
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
				data.setResult(AbstractActionData.RESULT_FATAL);
				return;
			}
		}
		
		data.setStudent(user);
		
		//AssessmentScore
		AssessmentScore score = dh.getAssessmentDAO().getScore(em, data.getAssessment(), user);
		data.setScore(score);

		List<AssessmentFile> myFiles = dh.getAssessmentDAO().listAssessmentFilesForUser(em, data.getAssessment(), user);
		List<AssessmentFile> aFiles = dh.getAssessmentDAO().listAssessmentFilesForAssessment(em, data.getAssessment());
		Collections.sort(myFiles);
		Collections.sort(aFiles);
		List<AssessmentFile> allFiles = new ArrayList<AssessmentFile>(myFiles.size()+aFiles.size());
		allFiles.addAll(aFiles);
		allFiles.addAll(myFiles);
		data.setFiles(allFiles);
		data.setAssessmentConfigurationKey(AssessmentService.getKeyForAssessmentConfiguration(data.getAssessment().getAssessmentConfiguration()));

		// Ima li postavljenu zastavicu
		if(data.getAssessment().getAssessmentFlag()!=null) {
			AssessmentFlagValue flagValue = dh.getAssessmentDAO().getFlagValue(em, data.getAssessment().getAssessmentFlag(), user);
			data.setFlagValue(flagValue);
			if(flagValue==null) {
				data.setCanTake(false);
			} else {
				data.setCanTake(flagValue.getValue());
			}
		} else {
			data.setCanTake(true);
		}

		AssessmentConfiguration ac = data.getAssessment().getAssessmentConfiguration();
		if(score==null || !score.getRawPresent()) {
			// Ako nije bio, nista dalje ne dohvacamo...
		} else {
			// Punjenje bean-a
			if (ac instanceof AssessmentConfProblems) {
				AssessmentConfProblems acp = (AssessmentConfProblems) ac;
				AssessmentViewProblemsBean beanData = new AssessmentViewProblemsBean();
				AssessmentConfProblemsData acpData = null;
				if(score.getRawPresent()) {
					// Ako je bio na tome, pokusaj dohvatiti zapis. Ovo ce se zbog uporabe
					// getSingleResult() raspasti ako zapisa NEMA.
					acpData = dh.getAssessmentDAO().getConfProblemsDataForAssessementAndUserId(em, acp, user.getId());
				} else {
					// Ako nije bio na tome, stvori mu defaultni prazan objekt koji se ne pohranjuje u bazu
					acpData = new AssessmentConfProblemsData();
					acpData.setAssessmentConfProblems(acp);
					acpData.setDscore(new Double[acp.getNumberOfProblems()]);
					acpData.setGroup("");
					acpData.setPresent(false);
					acpData.setUser(score.getUser());
				}
				int problemsNum = acp.getNumberOfProblems();
				beanData.setNumberOfProblems(problemsNum);
				beanData.setGroup(acpData==null ? "" : acpData.getGroup());
				beanData.setScores(acpData==null ? new Double[acp.getNumberOfProblems()] : acpData.getDscore());
				Double[] maxScores = null;
				if(!StringUtil.isStringBlank(acp.getScorePerProblem())) {
					String[] rows = StringUtil.split(acp.getScorePerProblem(), '\n');
					String group = beanData.getGroup()+"\t";
					for(String row : rows) {
						if(row.startsWith(group)) {
							maxScores = StringUtil.stringArrayToDoubleArray(StringUtil.split(row, '\t'), 1);
							break;
						}
					}
				}
				beanData.setMaxScores(maxScores);
				data.getBean().setConfType("PROBLEMS");
				data.getBean().setData(beanData);
				String[] problemsIds = new String[problemsNum];
				for (int i = 1; i <= problemsNum; i++) {
					problemsIds[i-1] = Integer.toString(i);
				}
				data.setProblemsIds(problemsIds);
				data.setUserAppeals(dh.getAssessmentDAO().listAppealsForUserAndAssessment(em, data.getAssessment(), user));
				
			} else if (ac instanceof AssessmentConfChoice) {
				AssessmentConfChoice acc = (AssessmentConfChoice) ac;
				AssessmentConfChoiceAnswers accAnswers = null;
				if(score.getRawPresent()) {
					// Ako je bio na tome, pokusaj dohvatiti zapis. Ovo ce se zbog uporabe
					// getSingleResult() raspasti ako zapisa NEMA.
					accAnswers = dh	.getAssessmentDAO()
						.getAssessmentConfChoiceAnswersForAssessementAndStudent(em, user, acc);
				} else {
					// Ako nije bio na tome, stvori mu defaultni prazan objekt koji se ne pohranjuje u bazu
					accAnswers = new AssessmentConfChoiceAnswers();
					int numberOfProblems = acc.getProblemsNum();
					StringBuilder sb = new StringBuilder(6*numberOfProblems);
					for(int i = 0; i < numberOfProblems; i++) {
						if(i!=0) sb.append('\t');
						sb.append("BLANK");
					}
					accAnswers.setAnswers(sb.toString());
					sb = new StringBuilder(2*numberOfProblems);
					for(int i = 0; i < numberOfProblems; i++) {
						if(i!=0) sb.append('\t');
						sb.append("-");
					}
					accAnswers.setAnswersStatus(sb.toString());
					accAnswers.setGroup("");
					accAnswers.setPresent(false);
					accAnswers.setUser(score.getUser());
					accAnswers.setAssessmentConfChoice(acc);
				}
				AssessmentViewChoiceBean beanData = new AssessmentViewChoiceBean();
				
				int numberOfProblems = acc.getProblemsNum();
				beanData.setNumberOfProblems(numberOfProblems);
				beanData.setProblemsLabels(StringUtil.split(acc.getProblemsLabels(), '\t'));
				beanData.setAnswers(StringUtil.split(accAnswers.getAnswers().trim(), '\t'));
				data.setProblemsIds(beanData.getProblemsLabels());
				String group = accAnswers.getGroup();
				beanData.setGroup(group);
				group.concat("\t");
				String groupAnswersStr = null;
				String[] allCorrectAnswers = null;
				if(acc.getCorrectAnswers()!=null) {
					allCorrectAnswers = StringUtil.split(acc.getCorrectAnswers().trim(), '\n');
					for (int i = 0; i < allCorrectAnswers.length; i++) {
						if (allCorrectAnswers[i].startsWith(group)) {
							groupAnswersStr = allCorrectAnswers[i];
							break;
						}
					}
				} else {
					allCorrectAnswers = new String[numberOfProblems];
					for(int i = 0; i < numberOfProblems; i++) {
						allCorrectAnswers[i] = "";
					}
				}
				if (groupAnswersStr != null) {
					String[] groupAnswersWithGroup = StringUtil.split(groupAnswersStr.trim(), '\t');
					String[] studentGroupCorrectAnwers = new String[groupAnswersWithGroup.length-1];
					for (int i = 0; i < studentGroupCorrectAnwers.length; i++) {
						studentGroupCorrectAnwers[i] = groupAnswersWithGroup[i+1];
					}
					beanData.setCorrectAnswers(studentGroupCorrectAnwers);
				} else {
					beanData.setCorrectAnswers(new String[numberOfProblems]);
				}
				if (accAnswers.getAnswersStatus() != null) {
					beanData.setAnswersStatus(StringUtil.split(accAnswers.getAnswersStatus(), '\t'));
				} else {
					beanData.setAnswersStatus(new String[numberOfProblems]);
				}
				
				if (acc.getDetailTaskScores() == null || acc.getDetailTaskScores().equals("")) {
					beanData.setUsingDetailedTaskScores(false);
					beanData.setScoreIncorrect(acc.getScoreIncorrect());
					beanData.setScoreUnanswered(acc.getScoreUnanswered());
					beanData.setScoreCorrect(acc.getScoreCorrect());
				} else {
					beanData.setUsingDetailedTaskScores(true);
					String[] detailedScores = StringUtil.split(acc.getDetailTaskScores(), '\n');
					String[] detailedScoresCorrect = new String[numberOfProblems];
					String[] detailedScoresIncorrect = new String[numberOfProblems];
					String[] detailedScoresUnanswered = new String[numberOfProblems];
					
					// varijabla group završava sa '\t'!
					for (int i = 0, dpNum = 0; i < detailedScores.length; i++) {
						if (detailedScores[i].startsWith(group)) {
							String[] dScores = StringUtil.split(detailedScores[i], '\t');
							detailedScoresCorrect[dpNum] = dScores[2];
							detailedScoresIncorrect[dpNum] = dScores[3];
							detailedScoresUnanswered[dpNum] = dScores[4];
							dpNum++;
						}
					}
					beanData.setDetailedScoresCorrect(detailedScoresCorrect);
					beanData.setDetailedScoresIncorrect(detailedScoresIncorrect);
					beanData.setDetailedScoresUnanswered(detailedScoresUnanswered);
				}
				
				data.getBean().setData(beanData);
				data.getBean().setConfType("CHOICE");
				
				int answersNum = acc.getAnswersNumber();
				String[] answers = new String[answersNum];
				for (int i = 0; i < answersNum; i++) {
					answers[i] = Character.toString((char)('A' + i));
				}
				data.setAnswers(answers);
				
				data.setUserAppeals(dh.getAssessmentDAO().listAppealsForUserAndAssessment(em, data.getAssessment(), user));
				
			} else {
				String[] problemsIds = new String[] {"1"};
				data.setProblemsIds(problemsIds);
				data.setUserAppeals(dh.getAssessmentDAO().listAppealsForUserAndAssessment(em, data.getAssessment(), user));
			}
		}
		
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}
	
	/**
	 * Metoda dohvaca podatke o provjeri za neadministrativno osoblje kolegija - asistente koji inace jesu na doticnom kolegiju,
	 * i zaduzeni su za cuvanje ispita iz istoga, ali inace nemaju nikakva prava administriranja na kolegiju.
	 * 
	 * @param em
	 * @param data
	 */
	public static void prepareStaffView(EntityManager em, AssessmentViewData data) {
		// Dohvat provjere
		if(!AssessmentServiceSupport.fillAssessment(em, data, data.getAssessmentID())) return;
		data.setCourseInstanceID(data.getAssessment().getCourseInstance().getId());
		DAOHelper dh = DAOHelperFactory.getDAOHelper();

		if(!JCMSSecurityManagerFactory.getManager().isStaffOnCourse(data.getCourseInstance())) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		
		User user = data.getCurrentUser();
		
		for(AssessmentRoom ar : data.getAssessment().getRooms()) {
			if(ar.getUserEvent()!=null && ar.getUserEvent().getUsers().contains(user)) {
				data.setUserSpecificEvent(ar.getUserEvent());
				break;
			}
		}
		
		List<AssessmentFile> aFiles = dh.getAssessmentDAO().listAssessmentFilesForAssessment(em, data.getAssessment());
		Collections.sort(aFiles);
		List<AssessmentFile> allFiles = new ArrayList<AssessmentFile>(aFiles.size());
		allFiles.addAll(aFiles);
		data.setFiles(allFiles);

		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}
	
	/**
	 * Metoda dohvaca podatke o provjeri za cuvare provjere - vanjske asistente koji nemaju veze s doticnim kolegijem,
	 * ali su zaduzeni za cuvanje ispita iz istoga.
	 * 
	 * @param em
	 * @param data
	 */
	public static void prepareGuestView(EntityManager em, AssessmentViewData data) {
		// Dohvat provjere
		if(!AssessmentServiceSupport.fillAssessment(em, data, data.getAssessmentID())) return;
		data.setCourseInstanceID(data.getAssessment().getCourseInstance().getId());
		DAOHelper dh = DAOHelperFactory.getDAOHelper();

		User user = data.getCurrentUser();
		
		boolean guestAssistant = false;
		for(AssessmentRoom ar : data.getAssessment().getRooms()) {
			if(ar.getUserEvent()!=null && ar.getUserEvent().getUsers().contains(user)) {
				data.setUserSpecificEvent(ar.getUserEvent());
				guestAssistant = true;
				break;
			}
		}
		if(!guestAssistant) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		
		List<AssessmentFile> aFiles = dh.getAssessmentDAO().listAssessmentFilesForAssessment(em, data.getAssessment());
		Collections.sort(aFiles);
		List<AssessmentFile> allFiles = new ArrayList<AssessmentFile>(aFiles.size());
		allFiles.addAll(aFiles);
		data.setFiles(allFiles);

		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}
	
	private static Dependencies cloneDependencies(Dependencies deps,	Set<Assessment> assessments, Set<AssessmentFlag> flags) {
		DependencyItem[] originalRoots = deps.getRoots();
		final Map<String,SortData> sortIndexes = new HashMap<String, SortData>(assessments.size()+flags.size());
		for(Assessment a : assessments) {
			SortData sd = new SortData(a.getShortName()+":A", a.getName(), a.getSortIndex());
			sortIndexes.put(sd.uniqueID,sd);
		}
		for(AssessmentFlag f : flags) {
			SortData sd = new SortData(f.getShortName()+":F", f.getName(), f.getSortIndex());
			sortIndexes.put(sd.uniqueID,sd);
		}
		Comparator<DependencyItem> comparator = new Comparator<DependencyItem>() {
			@Override
			public int compare(DependencyItem o1, DependencyItem o2) {
				if(o1==null) {
					if(o2!=null) return -1;
					return 0;
				} else if(o2==null) {
					return 1;
				}
				SortData sd1 = sortIndexes.get(o1.getUniqueID());
				SortData sd2 = sortIndexes.get(o2.getUniqueID());
				if(sd1==null) {
					if(sd2!=null) return -1;
					return 0;
				} else if(sd2==null) {
					return 1;
				}
				int d = sd1.sortIndex - sd2.sortIndex;
				if(d!=0) return d;
				return StringUtil.HR_COLLATOR.compare(sd1.uniqueID, sd2.uniqueID);
			}
		};
		return new Dependencies(deps.getCourseInstanceID(), cloneDependencyItemArray(originalRoots, sortIndexes, new HashMap<Object,Object>(200), comparator, new HashSet<Object>(200)));
	}

	private static DependencyItem[] cloneDependencyItemArray(DependencyItem[] originalArray, Map<String, SortData> sortIndexes, Map<Object,Object> cache, Comparator<DependencyItem> comparator, Set<Object> cyclePrevention) {
		if(originalArray==null) return null;
		DependencyItem[] cloned = (DependencyItem[])cache.get(originalArray);
		if(cloned!=null) return cloned;
		if(!cyclePrevention.add(originalArray)) {
			// Ups! Vec je bio tamo! Vrtim se u krug!
			System.out.println("Otkriven ciklus kod kloniranja DependencyItem[].");
			return null;
		}
		cloned = new DependencyItem[originalArray.length];
		for(int i = 0; i < originalArray.length; i++) {
			DependencyItem item = originalArray[i];
			cloned[i] = cloneDependencyItem(item, sortIndexes, cache, comparator, cyclePrevention);
		}
		Arrays.sort(cloned, comparator);
		cache.put(originalArray, cloned);
		cyclePrevention.remove(originalArray);
		return cloned;
	}

	private static DependencyItem cloneDependencyItem(DependencyItem item,	Map<String, SortData> sortIndexes, Map<Object, Object> cache, Comparator<DependencyItem> comparator, Set<Object> cyclePrevention) {
		if(item==null) return null;
		DependencyItem cloned = (DependencyItem)cache.get(item);
		if(cloned!=null) return cloned;
		if(!cyclePrevention.add(item)) {
			// Ups! Vec je bio tamo! Vrtim se u krug!
			System.out.println("Otkriven ciklus kod kloniranja DependencyItem.");
			return null;
		}
		cloned = new DependencyItem(item.getUniqueID(), cloneDependencyItemArray(item.getDependencies(), sortIndexes, cache, comparator, cyclePrevention));
		cache.put(item, cloned);
		cyclePrevention.remove(item);
		return cloned;
	}

	private static class SortData {
		String uniqueID;
		int sortIndex;
		public SortData(String uniqueID, String name, int sortIndex) {
			super();
			this.uniqueID = uniqueID;
			this.sortIndex = sortIndex;
		}
	}

	private static void checkAssessmentRenderingVisibility(DependencyItem di, Map<String, Object> objectMap,
			Map<String, Object> valueMap, Set<String> visibles) {
		Object assessmentOrFlag = objectMap.get(di.getUniqueID());
		if(assessmentOrFlag==null) {
			// Zasto imamo null!? Bjez van...
			return;
		}
		if(assessmentOrFlag instanceof Assessment) {
			Assessment a = (Assessment)assessmentOrFlag;
			char visibility = a.getVisibility();
			if(visibility=='H') return; // Student ovo ne vidi
			if(visibility=='E') {
				// ovo mozda vidi...
				AssessmentScore sc = (AssessmentScore)valueMap.get(di.getUniqueID());
				// Ako nema bodova, ili ako nije bio, ne vidi!
				if(sc==null || !sc.getEffectivePresent()) {
					return;
				}
			}
			// Inace ovo vidi
		} else {
			AssessmentFlag a = (AssessmentFlag)assessmentOrFlag;
			char visibility = a.getVisibility();
			if(visibility=='H') return; // Student ovo ne vidi
			if(visibility=='E') {
				// ovo mozda vidi...
				AssessmentFlagValue sc = (AssessmentFlagValue)valueMap.get(di.getUniqueID());
				// Ako nema zastavice, ili ako je ona false, ne vidi!
				if(sc==null || !sc.getValue()) {
					return;
				}
			}
			// Inace ovo vidi
		}
		visibles.add(di.getUniqueID());
		// Ako sam tu, ovo je vidljivo; ajmo pogledati sto je s djecom...
		for(DependencyItem diChild : di.getDependencies()) {
			checkAssessmentRenderingVisibility(diChild, objectMap, valueMap, visibles);
		}
	}

	private static void fillAssessmentRenderingClues(DependencyItem di, Map<String, Object> objectMap,
			Map<String, Object> valueMap, Set<String> visibles, List<TreeRenderingClues> renderingClues) {

		if(!visibles.contains(di.getUniqueID())) {
			return;
		}
		
		Object assessmentOrFlag = objectMap.get(di.getUniqueID());
		
		int type;
		Object object;
		Object value;
		
		if(assessmentOrFlag instanceof Assessment) {
			Assessment a = (Assessment)assessmentOrFlag;
			AssessmentScore sc = (AssessmentScore)valueMap.get(di.getUniqueID());
			type = TreeRenderingClues.TYPE_ASSESSMENT;
			object = a;
			value = sc;
		} else {
			AssessmentFlag a = (AssessmentFlag)assessmentOrFlag;
			AssessmentFlagValue sc = (AssessmentFlagValue)valueMap.get(di.getUniqueID());
			type = TreeRenderingClues.TYPE_FLAG;
			object = a;
			value = sc;
		}

		renderingClues.add(new TreeRenderingClues(TreeRenderingClues.EVENT_ITEM_START,type,object,value));

		boolean hasVisibleChildren = false;
		
		for(DependencyItem diChild : di.getDependencies()) {
			if(visibles.contains(diChild.getUniqueID())) {
				hasVisibleChildren = true;
				break;
			}
		}

		if(hasVisibleChildren) {
			renderingClues.add(new TreeRenderingClues(TreeRenderingClues.EVENT_CHILDREN_START,type,object,value));
			for(DependencyItem diChild : di.getDependencies()) {
				fillAssessmentRenderingClues(diChild, objectMap, valueMap, visibles, renderingClues);
			}
			renderingClues.add(new TreeRenderingClues(TreeRenderingClues.EVENT_CHILDREN_END,type,object,value));
		}
		
		renderingClues.add(new TreeRenderingClues(TreeRenderingClues.EVENT_ITEM_END,type,object,value));
	}

	public static class TreeRenderingClues {
		public static final int EVENT_ITEM_START     = 1;
		public static final int EVENT_CHILDREN_START = 2;
		public static final int EVENT_CHILDREN_END   = 3;
		public static final int EVENT_ITEM_END       = 4;

		public static final int TYPE_ASSESSMENT     = 1;
		public static final int TYPE_FLAG           = 2;
		
		private Object object; // Ovo ce kod renderiranja bodova biti assessment ili assessmentflag
		private Object value; // Ovo ce kod renderiranja bodova biti assessmentScore ili assessmentFlagValue
		private int event; // 1...item start, 2...children group start, 3...children group end, 4...item end  
		private int objectType; // 1...assessment, 2...flag, ...
		
		public TreeRenderingClues(int event, int objectType, Object object,
				Object value) {
			super();
			this.event = event;
			this.objectType = objectType;
			this.object = object;
			this.value = value;
		}
		
		public Object getObject() {
			return object;
		}
		
		public int getObjectType() {
			return objectType;
		}
		
		public Object getValue() {
			return value;
		}
		
		public int getEvent() {
			return event;
		}
	}
}
