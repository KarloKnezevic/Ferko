package hr.fer.zemris.jcms.service2.course.assessments;

import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
import hr.fer.zemris.jcms.caching.JCMSCacheFactory;
import hr.fer.zemris.jcms.dao.DAOHelper;
import hr.fer.zemris.jcms.dao.DAOHelperFactory;
import hr.fer.zemris.jcms.model.Assessment;
import hr.fer.zemris.jcms.model.AssessmentFlag;
import hr.fer.zemris.jcms.model.CourseInstanceKeyValue;
import hr.fer.zemris.jcms.model.Grade;
import hr.fer.zemris.jcms.model.GradesVisibility;
import hr.fer.zemris.jcms.model.Group;
import hr.fer.zemris.jcms.model.GroupOwner;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.model.UserGroup;
import hr.fer.zemris.jcms.security.JCMSSecurityManagerFactory;
import hr.fer.zemris.jcms.service.util.GradesUtil;
import hr.fer.zemris.jcms.service.util.GroupUtil;
import hr.fer.zemris.jcms.service2.course.CourseInstanceServiceSupport;
import hr.fer.zemris.jcms.web.actions.data.AdminAssessmentSummaryViewData;
import hr.fer.zemris.jcms.web.actions.data.StudentScoreBrowserSelectionData;
import hr.fer.zemris.jcms.web.actions.data.StudentScoreBrowserSettingsData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.util.InputStreamWrapper;

import javax.persistence.EntityManager;

/**
 * Sloj usluge koji nudi općenito navigiranje kroz provjere, zastavice i slično.
 * 
 * @author marcupic
 *
 */
public class SummaryViewService {

	/**
	 * Metoda dohvaća sve provjere i zastavice i priprema ih za prikaz korisniku.
	 * Konačni podatci pripremaju se i u JSON obliku kako bi se njima lakše manipuliralo
	 * u JavaScript-u.
	 * 
	 * @param em entity manager
	 * @param data podatci
	 */
	public static void showAdminAssessmentSummaryView(EntityManager em, AdminAssessmentSummaryViewData data) {

		// Dohvat kolegija
		if(!CourseInstanceServiceSupport.fillCourseInstance(em, data, data.getCourseInstanceID())) return;

		boolean canView = JCMSSecurityManagerFactory.getManager().canViewAssessments(data.getCourseInstance());
		if(!canView) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}

		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		List<User> courseUsers = null;
		if(data.getSelectedGroupID()==null || data.getSelectedGroupID().longValue()==-1) {
			// ako moze vidjeti sve, OK, inace error
			if(!JCMSSecurityManagerFactory.getManager().canManageAssessments(data.getCourseInstance())) {
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
				data.setResult(AbstractActionData.RESULT_FATAL);
				return;
			}
			courseUsers = dh.getCourseInstanceDAO().findCourseUsers(em, data.getCourseInstance().getId());
		} else {
			Group g = dh.getGroupDAO().get(em, data.getSelectedGroupID());
			if(g==null || !g.getCompositeCourseID().equals(data.getCourseInstance().getId()) || !g.getRelativePath().startsWith("0/")) {
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
				data.setResult(AbstractActionData.RESULT_FATAL);
				return;
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
					return;
				}
			}
			courseUsers = new ArrayList<User>(g.getUsers().size());
			for(UserGroup ug : g.getUsers()) {
				courseUsers.add(ug.getUser());
			}
			data.setSelectedGroup(g);
		}
		Set<Long> ids = new HashSet<Long>(courseUsers.size());
		for(User u : courseUsers) {
			ids.add(u.getId());
		}
		data.setAllowedUsers(ids);
		data.setResult(AbstractActionData.RESULT_SUCCESS);

		Map<Long,Grade> gradeMap = null;
		if(data.getCourseInstance().getGradingPolicy()!=null && (data.getCourseInstance().getGradingPolicy().getGradesValid() && data.getCourseInstance().getGradingPolicy().getGradesVisibility()!=GradesVisibility.NOT_VISIBLE)) {
			// Dohvati još i ocjene:
			List<Grade> allGrades = DAOHelperFactory.getDAOHelper().getCourseInstanceDAO().listGradesFor(em, data.getCourseInstance());
			gradeMap = GradesUtil.mapGradeByUserID(allGrades);
		}
		
		List<UserGroup> lectureUGs = dh.getGroupDAO().findAllLectureUserGroups(em, data.getCourseInstance().getId());
		Map<Long,UserGroup> lectureGroups = GroupUtil.mapUserGroupByUserID(lectureUGs);
		
		// ======================================
		// Ovaj dio se je radio izvan transakcije
		// ======================================
		
		if(data.getCourseInstance()!=null) {
			CourseScoreTable table = JCMSCacheFactory.getCache().getCourseScoreTable(data.getCourseInstance().getId());
			data.setTable(table);
			if(table!=null) {
				// Najprije dohvati stablo ovisnosti
				Dependencies deps = JCMSCacheFactory.getCache().getDependencies(data.getCourseInstance().getId());
				if(deps==null) {
					// Imamo problem! Ajmo sve proglasiti root komponentama...
					int brojProvjera = 0;
					for(int i = 0; i < table.getTableHeader().size(); i++) {
						ScoreTableHeaderEntry e = table.getTableHeader().get(i);
						if(e.getUniqueID().endsWith(":A") || e.getUniqueID().endsWith(":F")) {
							brojProvjera++;
						}
					}
					DependencyItem[] roots = new DependencyItem[brojProvjera];
					brojProvjera = 0;
					for(int i = 0; i < table.getTableHeader().size(); i++) {
						ScoreTableHeaderEntry e = table.getTableHeader().get(i);
						if(e.getUniqueID().endsWith(":A") || e.getUniqueID().endsWith(":F")) {
							roots[brojProvjera] = new DependencyItem(e.getUniqueID(), new DependencyItem[0]);
							brojProvjera++;
						}
					}
					Arrays.sort(roots);
					deps = new Dependencies(data.getCourseInstance().getId(), roots);
				}
				data.setDependenciesJSON(deps.toJSONStringBuilder().toString());
				// Zatim u JSON prebaci zaglavlja
				StringBuilder headersJSON = new StringBuilder(500);
				headersJSON.append("[");
				for(int i = 0; i < table.getTableHeader().size(); i++) {
					ScoreTableHeaderEntry e = table.getTableHeader().get(i);
					if(i>0) headersJSON.append(",\n");
					headersJSON.append("{");
					headersJSON.append("\"headerID\": ").append(e.getId()).append(", ");
					headersJSON.append("\"headerUniqueID\": \"").append(e.getUniqueID()).append("\", ");
					if(e instanceof STHEAssessment) {
						STHEAssessment s = (STHEAssessment)e;
						headersJSON.append("\"shortName\": \"").append(s.getShortName()).append("\", ");
					} else if(e instanceof STHEFlag) {
						STHEFlag s = (STHEFlag)e;
						headersJSON.append("\"shortName\": \"").append(s.getShortName()).append("\", ");
					} else if(e instanceof STHEStudent) {
						headersJSON.append("\"shortName\": \"Student\", ");
					}
					headersJSON.append("\"headerType\": \"").append(e.getUniqueID().substring(e.getUniqueID().length()-1)).append("\"");
					headersJSON.append("}");
				}
				if(table.getTableHeader().size()>0) headersJSON.append(",\n");
				headersJSON.append("{");
				headersJSON.append("\"headerID\": -3, ");
				headersJSON.append("\"headerUniqueID\": \"lgroup\", ");
				headersJSON.append("\"shortName\": \"Grupa\", ");
				headersJSON.append("\"headerType\": \"L\"");
				headersJSON.append("}");
				if(gradeMap!=null) {
					headersJSON.append(",\n");
					headersJSON.append("{");
					headersJSON.append("\"headerID\": -2, ");
					headersJSON.append("\"headerUniqueID\": \"grade\", ");
					headersJSON.append("\"shortName\": \"Ocjena\", ");
					headersJSON.append("\"headerType\": \"G\"");
					headersJSON.append("}");
				}
				headersJSON.append("]");
				data.setHeadersJSON(headersJSON.toString());
				headersJSON = null;

				int indexNo = 30000;
				boolean normal = true;
				for(int i = 0; i < table.getTableHeader().size(); i++) {
					ScoreTableHeaderEntry e = table.getTableHeader().get(i);
					if(e.getSortKey().equals(data.getSortKey())) {
						indexNo = i;
						normal = true;
						break;
					}
					if(e.getReverseSortKey().equals(data.getSortKey())) {
						indexNo = i;
						normal = false;
						break;
					}
				}
				if(indexNo==30000) {
					indexNo = 0;
				}
				List<ScoreTableEntry[]> entries = new ArrayList<ScoreTableEntry[]>(data.getAllowedUsers().size());
				int[] origIndex = table.getIndexes().get(indexNo);
				Set<Long> allowedUsers = data.getAllowedUsers();
				if(normal) {
					for(int i = 0; i < origIndex.length; i++) {
						ScoreTableEntry[] e = table.getTableItems().get(origIndex[i]);
						if(!allowedUsers.contains(e[0].getId())) continue;
						entries.add(e);
					}
				} else {
					for(int i = origIndex.length-1; i >= 0; i--) {
						ScoreTableEntry[] e = table.getTableItems().get(origIndex[i]);
						if(!allowedUsers.contains(e[0].getId())) continue;
						entries.add(e);
					}
				}
				data.setEntries(entries);
				StringBuilder dataJSON = new StringBuilder(1024*1024);
				dataJSON.append("[");
				boolean prvi = true;
				for(ScoreTableEntry[] row : entries) {
					if(prvi) {
						prvi = false;
					} else {
						dataJSON.append(", ");
					}
					dataJSON.append("[");
					boolean prviRedak = true;
					Grade grade = null;
					UserGroup ug = null;
					for(ScoreTableEntry e : row) {
						if(prviRedak) {
							prviRedak = false;
						} else {
							dataJSON.append(", ");
						}
						dataJSON.append("\n");
						dataJSON.append("{");
						dataJSON.append("\"id\": ").append(e.getId()).append(", ");
						if(e instanceof STEStudent) {
							STEStudent s = (STEStudent)e;
							dataJSON.append("\"j\": \"").append(s.getJmbag()).append("\", "); // jmbag
							dataJSON.append("\"l\": \"").append(s.getLastName()).append("\", "); // lastName
							dataJSON.append("\"f\": \"").append(s.getFirstName()).append("\"");  // firstName
							if(gradeMap!=null) grade = gradeMap.get(s.getId());
							ug = lectureGroups.get(s.getId());
						} else if(e instanceof STEScore) {
							STEScore s = (STEScore)e;
							dataJSON.append("\"ep\": ").append(s.getEffectivePresent()).append(", "); // effectivePresent
							dataJSON.append("\"er\": ").append(s.getEffectiveRank()).append(", "); // effectiveRank
							dataJSON.append("\"ea\": \"").append(s.getEffectiveScoreAsString()).append("\", "); // effectiveScoreAsString
							dataJSON.append("\"es\": ").append(s.getEffectiveScore()).append(", "); // effectiveScore

							dataJSON.append("\"e\": ").append(s.isError()).append(", ");    // error
							dataJSON.append("\"p\": ").append(s.isPresent()).append(", ");  // present
							
							dataJSON.append("\"sa\": \"").append(s.getScoreAsString()).append("\", "); // scoreAsString
							dataJSON.append("\"s\": ").append(s.getScore()).append(", ");  // score
							dataJSON.append("\"ra\": \"").append(s.getRawScoreAsString()).append("\", "); // rawScoreAsString
							dataJSON.append("\"rs\": ").append(s.getRawScore()).append(", ");  // rawScore
							dataJSON.append("\"x\": \"").append(s.getStatus().toString()).append("\", "); // status
							dataJSON.append("\"y\": \"").append(s.getEffectiveStatus().toString()).append("\", ");  // effectiveStatus
							dataJSON.append("\"r\": ").append(s.getRank());  // rank
						} else if(e instanceof STEFlagValue) {
							STEFlagValue s = (STEFlagValue)e;
							dataJSON.append("\"v\": ").append(s.isValue()).append(", "); // value
							dataJSON.append("\"ms\": ").append(s.isManuallySet()).append(", "); // manuallySet
							dataJSON.append("\"mv\": ").append(s.isManualValue()).append(", "); // manualValue
							dataJSON.append("\"e\": ").append(s.isError()); // error
						}
						dataJSON.append("}");
					}
					if(row.length>0) {
						dataJSON.append(", ");
						dataJSON.append("\n");
					}
					dataJSON.append("{");
					if(ug==null) {
						dataJSON.append("\"id\": -1, ");
						dataJSON.append("\"g\": \"\""); // grade
					} else {
						dataJSON.append("\"id\": -1, ");
						dataJSON.append("\"n\": \"").append(ug.getGroup().getName()).append("\""); // name of group
					}
					dataJSON.append("}");
					if(gradeMap!=null) {
						dataJSON.append(", ");
						dataJSON.append("\n");
						dataJSON.append("{");
						if(grade==null) {
							dataJSON.append("\"id\": -1, ");
							dataJSON.append("\"g\": \"\""); // grade
						} else {
							dataJSON.append("\"id\": ").append(grade.getId()).append(", ");
							dataJSON.append("\"g\": \"").append(grade.getGrade()).append("\""); // grade
						}
						dataJSON.append("}");
					}
					dataJSON.append("]");
				}
				dataJSON.append("]");
				data.setDataJSON(dataJSON.toString());
			}
		}
	}

	public static void studentScoreBrowserSelection(EntityManager em, StudentScoreBrowserSelectionData data) {
		// Dohvat kolegija
		if(!CourseInstanceServiceSupport.fillCourseInstance(em, data, data.getCourseInstanceID())) return;

		boolean canManage = JCMSSecurityManagerFactory.getManager().canManageAssessments(data.getCourseInstance());
		if(!canManage) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}

		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		CourseInstanceKeyValue cikv = dh.getCourseInstanceKeyValueDAO().get(em, data.getCourseInstance(), "studScoreBrowser");
		String kind = resolveStudentScoreBrowser(cikv);
		data.setKind(kind);
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}

	public static void studentScoreBrowserSelectionChange(EntityManager em, StudentScoreBrowserSelectionData data) {
		// Dohvat kolegija
		if(!CourseInstanceServiceSupport.fillCourseInstance(em, data, data.getCourseInstanceID())) return;

		boolean canManage = JCMSSecurityManagerFactory.getManager().canManageAssessments(data.getCourseInstance());
		if(!canManage) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}

		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		CourseInstanceKeyValue cikv = dh.getCourseInstanceKeyValueDAO().get(em, data.getCourseInstance(), "studScoreBrowser");
		String kind = data.getKind();
		if(!isValidStudentScoreBrowser(kind)) {
			kind = "AUTO";
		}
		if(cikv==null) {
			cikv = new CourseInstanceKeyValue(data.getCourseInstance(), "studScoreBrowser", kind);
			dh.getCourseInstanceKeyValueDAO().save(em, cikv);
		} else {
			cikv.setValue(kind);
		}
		data.setKind(kind);
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}

	public static boolean isValidStudentScoreBrowser(String kind) {
		if(kind==null) return false;
		return kind.equals("AUTO") || kind.equals("TREE1");
	}
	
	public static String resolveStudentScoreBrowser(CourseInstanceKeyValue cikv) {
		String kind = "AUTO";
		if(cikv!=null && cikv.getValue()!=null && !cikv.getValue().isEmpty()) {
			kind = cikv.getValue();
		}
		if(!isValidStudentScoreBrowser(kind)) {
			kind = "AUTO";
		}
		return kind;
	}
	
	public static void studentScoreBrowserSettings(EntityManager em, StudentScoreBrowserSettingsData data) {
		// Dohvat kolegija
		if(!CourseInstanceServiceSupport.fillCourseInstance(em, data, data.getCourseInstanceID())) return;

		boolean canManage = JCMSSecurityManagerFactory.getManager().canManageAssessments(data.getCourseInstance());
		if(!canManage) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}

		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		
		if(data.getWhat()==null || data.getWhat().isEmpty()) {
			data.setResult(AbstractActionData.RESULT_SUCCESS);
			return;
		}
		
		if("flat".equals(data.getWhat())) {
			List<Assessment> assessments = AssessmentsUtil.getSortedCourseInstanceAssessments(em, data.getCourseInstance());
			List<AssessmentFlag> assessmentFlags = AssessmentsUtil.getSortedCourseInstanceAssessmentFlags(em, data.getCourseInstance());
			StringBuilder sb = new StringBuilder(1024);
			for(Assessment a : assessments) {
				sb.append("A\t").append(a.getId()).append("\t").append(a.getShortName()).append("\t").append(a.getName()).append("\r\n");
			}
			for(AssessmentFlag a : assessmentFlags) {
				sb.append("F\t").append(a.getId()).append("\t").append(a.getShortName()).append("\t").append(a.getName()).append("\r\n");
			}
			data.setResult("TEXTOK");
			try {
				data.setStream(InputStreamWrapper.createInputStreamWrapperFromText(sb.toString(), "text/plain"));
			} catch(IOException ex) {}
			return;
		}
		if("hier".equals(data.getWhat())) {
			CourseInstanceKeyValue cikv = dh.getCourseInstanceKeyValueDAO().get(em, data.getCourseInstance(), "studScoreTree");
			String res = "";
			if(cikv!=null && cikv.getValue()!=null) {
				res = cikv.getValue();
				if(res==null) res = "";
			}
			data.setResult("TEXTOK");
			try {
				data.setStream(InputStreamWrapper.createInputStreamWrapperFromText(res, "text/plain"));
			} catch(IOException ex) {}
			return;
		}
		if("stor".equals(data.getWhat())) {
			CourseInstanceKeyValue cikv = dh.getCourseInstanceKeyValueDAO().get(em, data.getCourseInstance(), "studScoreTree");
			String res = "";
			if(data.getReqdata()!=null) {
				res = data.getReqdata();
			}
			if(cikv==null) {
				cikv = new CourseInstanceKeyValue(data.getCourseInstance(), "studScoreTree", res);
				dh.getCourseInstanceKeyValueDAO().save(em, cikv);
			} else {
				cikv.setValue(res);
			}
			data.setResult("TEXTOK");
			try {
				data.setStream(InputStreamWrapper.createInputStreamWrapperFromText("OK", "text/plain"));
			} catch(IOException ex) {}
			return;
		}
		
		data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
		data.setResult(AbstractActionData.RESULT_FATAL);
		return;
	}
}
