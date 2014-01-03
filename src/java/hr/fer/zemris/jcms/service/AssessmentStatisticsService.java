package hr.fer.zemris.jcms.service;

import hr.fer.zemris.jcms.JCMSSettings;
import hr.fer.zemris.jcms.dao.DAOHelper;
import hr.fer.zemris.jcms.dao.DAOHelperFactory;
import hr.fer.zemris.jcms.dao.PersistenceUtil;
import hr.fer.zemris.jcms.model.Assessment;
import hr.fer.zemris.jcms.model.AssessmentConfChoice;
import hr.fer.zemris.jcms.model.AssessmentConfChoiceAnswers;
import hr.fer.zemris.jcms.model.AssessmentConfiguration;
import hr.fer.zemris.jcms.model.AssessmentScore;
import hr.fer.zemris.jcms.model.Group;
import hr.fer.zemris.jcms.statistics.assessments.AssessmentStatistics;
import hr.fer.zemris.jcms.statistics.assessments.ScoreStatistics;
import hr.fer.zemris.jcms.statistics.assessments.SingleChoiceStatistics;
import hr.fer.zemris.jcms.statistics.assessments.SingleChoiceStatisticsRow;
import hr.fer.zemris.jcms.statistics.assessments.StatisticsBase;
import hr.fer.zemris.jcms.statistics.assessments.StatisticsName;
import hr.fer.zemris.jcms.statistics.assessments.beans.UserGroupStat;
import hr.fer.zemris.util.StringUtil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;

public class AssessmentStatisticsService {

	public static interface AssessmentStatisticsRequester {
		public void process(AssessmentStatisticsProvider provider);
	}
	
	public static interface AssessmentStatisticsProvider {
		public AssessmentStatistics getAssessmentStatistics();
		public StatisticsBase getStatisticsBase(int id);
	}

	private static class AssessmentStatisticsProviderDefImpl implements AssessmentStatisticsProvider {
		private String courseID;
		private String courseInstanceID;
		private Long assessmentID;
		private File root;
		private File listingFile;
		public AssessmentStatisticsProviderDefImpl(String courseID,
				String courseInstanceID, Long assessmentID, File root,
				File listingFile) {
			super();
			this.courseID = courseID;
			this.courseInstanceID = courseInstanceID;
			this.assessmentID = assessmentID;
			this.root = root;
			this.listingFile = listingFile;
		}
		
		private AssessmentStatistics astat;
		private Map<Long, StatisticsBase> map = new HashMap<Long, StatisticsBase>();
		
		@Override
		public AssessmentStatistics getAssessmentStatistics() {
			if(astat==null) {
				astat = readStatisticsListing(courseID, courseInstanceID, assessmentID, root, listingFile);
			}
			return astat;
		}
		
		@Override
		public StatisticsBase getStatisticsBase(int id) {
			Long key = Long.valueOf(id);
			StatisticsBase res = map.get(key);
			if(res!=null) return res;
			res = readStatisticsBase(courseID, courseInstanceID, key, root, id);
			map.put(key, res);
			return res;
		}
	}
	
	private static Object syncObject = new Object();
	private static Map<Long,int[]> lockMap = new HashMap<Long,int[]>();

	public static void accessStatistics(String courseID, String courseInstanceID, Long assessmentID, AssessmentStatisticsRequester requester) {
		File fRoot = null;
		File listingFile = null;
		
		getReadLock(assessmentID);
		try {
			fRoot = getAssessmentStatisticsRoot(courseID, courseInstanceID, assessmentID);
			listingFile = new File(fRoot,"listing");
			if(listingFile.exists()) {
				AssessmentStatisticsProvider provider = new AssessmentStatisticsProviderDefImpl(courseID, courseInstanceID, assessmentID, fRoot, listingFile);
				requester.process(provider);
				return;
			}
		} finally {
			releaseReadLock(assessmentID);
		}

		AssessmentStatisticsProvider provider = new AssessmentStatisticsProviderDefImpl(courseID, courseInstanceID, assessmentID, fRoot, listingFile);

		// Cini se da datoteke gore nije bilo; trazimo write lock i idemo to provjeriti
		getWriteLock(assessmentID);
		boolean fileExists;
		try {
			fileExists = listingFile.exists();
		} catch(Exception ex) {
			releaseWriteLock(assessmentID);
			return;
		}
		if(!fileExists) {
			try {
				buildStats(courseID, courseInstanceID, assessmentID, fRoot, listingFile);
			} catch(Exception ex) {
				releaseWriteLock(assessmentID);
				return;
			}
		}
		// Sada ili sam ga izgradio, ili vec postoji od prije...
		degradeWriteLockToReadLock(assessmentID);
		try {
			requester.process(provider);
		} finally {
			releaseReadLock(assessmentID);
		}
	}

	public static void clearStatistics(String courseID, String courseInstanceID, Long assessmentID) {
		File fRoot = null;
		File listingFile = null;
		
		getWriteLock(assessmentID);
		try {
			fRoot = getAssessmentStatisticsRoot(courseID, courseInstanceID, assessmentID);
			listingFile = new File(fRoot,"listing");
			if(listingFile.exists()) {
				clearStatistics(fRoot);
				return;
			}
		} finally {
			releaseWriteLock(assessmentID);
		}
	}

	private static void clearStatistics(File root) {
		if(!root.exists() || !root.isDirectory()) return;
		File[] files = root.listFiles();
		if(files==null) return;
		for(File f : files) {
			f.delete();
		}
	}

	@SuppressWarnings("unchecked")
	private static void buildStats(final String courseID, final String courseInstanceID, final Long assessmentID, final File root, final File listingFile) {
		root.mkdirs();
		EntityManager em = PersistenceUtil.getEntityManager();
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		Assessment assessment = dh.getAssessmentDAO().get(em, assessmentID);
		//AssessmentConfiguration aconf = assessment.getAssessmentConfiguration();
		List<UserGroupStat> uglist = em.createQuery("select new hr.fer.zemris.jcms.statistics.assessments.beans.UserGroupStat(ug.user.id, ug.group.id) from UserGroup as ug where ug.group.compositeCourseID=:ccid and ug.group.relativePath LIKE :relPath")
			.setParameter("ccid", courseInstanceID)
			.setParameter("relPath", "0/%").getResultList();
		
		Map<Long,UserGroupStat> mapUGByUserID = new HashMap<Long, UserGroupStat>(uglist.size());
		for(UserGroupStat ugs : uglist) {
			mapUGByUserID.put(ugs.getUserID(), ugs);
		}
		
		List<Group> lectGroups = dh.getGroupDAO().findLectureSubgroups(em, courseInstanceID);
		Map<Long,String> mapGroupNamesByID = new HashMap<Long, String>();
		for(Group g : lectGroups) {
			mapGroupNamesByID.put(g.getId(), g.getName());
		}
		Set<AssessmentScore> assScoreSet = assessment.getScore();

		AssessmentStatistics astat = new AssessmentStatistics();
		astat.setAssessmentID(assessmentID);
		astat.setAssessmentName(assessment.getName());
		astat.setCourseInstanceID(assessment.getCourseInstance().getId());
		astat.setCourseName(assessment.getCourseInstance().getCourse().getIsvuCode());

		AssessmentConfiguration aConfiguration = assessment.getAssessmentConfiguration();
		int statsCounter = 0;
		String[] kinds = new String[] {"E", "A", "R"};
		for(int statType = 0; statType < 3; statType++) {
			List<UserGroupStat> allList = new ArrayList<UserGroupStat>(mapUGByUserID.size());
			Map<Long,List<UserGroupStat>> mapByGroupID = new HashMap<Long, List<UserGroupStat>>();
		fo:	for(AssessmentScore as : assScoreSet) {
				// Ako nije bio...
				switch(statType) {
				case 0:
					if(!as.getEffectivePresent()) continue fo;
					break;
				case 1:
					if(!as.getPresent()) continue fo;
					break;
				case 2:
					if(!as.getRawPresent()) continue fo;
					break;
				}
				UserGroupStat ugs = mapUGByUserID.get(as.getUser().getId());
				// Ako nije vise u grupi za predavanja...
				if(ugs==null) continue;
				switch(statType) {
				case 0:
					ugs.setScore(as.getEffectiveScore());
					break;
				case 1:
					ugs.setScore(as.getScore());
					break;
				case 2:
					ugs.setScore(as.getRawScore());
					break;
				}
				allList.add(ugs);
				List<UserGroupStat> ugl = mapByGroupID.get(ugs.getGroupID());
				if(ugl==null) {
					ugl = new ArrayList<UserGroupStat>(100);
					mapByGroupID.put(ugs.getGroupID(), ugl);
				}
				ugl.add(ugs);
			}
			StatisticsName name1 = new StatisticsName();
			name1.setId(statsCounter++);
			name1.setLectureGroupID(Long.valueOf(-1));
			name1.setLectureGroupName("");
			switch(statType) {
			case 0:
				name1.setTitle("Efektivno: raspodjela bodova na kolegiju");
				break;
			case 1:
				name1.setTitle("Provjera: raspodjela bodova na kolegiju");
				break;
			case 2:
				name1.setTitle("Sirovi podaci: raspodjela bodova na kolegiju");
				break;
			}
			name1.setKind(kinds[statType]+":G:-1");
			ScoreStatistics scoreStat1 = createScoreStatistics(allList, name1.getId(), name1.getKind());
			writeStatisticsBase(courseID, courseInstanceID, assessmentID, root, scoreStat1);
			scoreStat1 = null;
			astat.getAvailableStatistics().add(name1);
			name1 = null;
			for(Map.Entry<Long, List<UserGroupStat>> e : mapByGroupID.entrySet()) {
				Long groupID = e.getKey();
				List<UserGroupStat> ugl = e.getValue();
				StatisticsName name2 = new StatisticsName();
				name2.setId(statsCounter++);
				name2.setLectureGroupID(groupID);
				name2.setLectureGroupName(mapGroupNamesByID.get(groupID));
				if(name2.getLectureGroupID()==null) {
					name2.setLectureGroupName("?");
				}
				switch(statType) {
				case 0:
					name2.setTitle("Efektivno: raspodjela bodova za grupu "+name2.getLectureGroupName());
					break;
				case 1:
					name2.setTitle("Provjera: raspodjela bodova za grupu "+name2.getLectureGroupName());
					break;
				case 2:
					name2.setTitle("Sirovi podaci: raspodjela bodova za grupu "+name2.getLectureGroupName());
					break;
				}
				name2.setKind(kinds[statType]+":G:"+groupID);
				ScoreStatistics scoreStat2 = createScoreStatistics(ugl, name2.getId(), name2.getKind());
				writeStatisticsBase(courseID, courseInstanceID, assessmentID, root, scoreStat2);
				astat.getAvailableStatistics().add(name2);
			}
		}
		if(aConfiguration instanceof AssessmentConfChoice) {
			try {
				statsCounter = performAssessmentConfChoiceStatistics(em, dh, statsCounter, astat, assessment, assScoreSet, mapGroupNamesByID, mapUGByUserID, courseID, courseInstanceID, assessmentID, root);
			} catch(Exception ex) {
				ex.printStackTrace();
			}
		}
		writeStatisticsListing(courseID, courseInstanceID, assessmentID, root, listingFile, astat);
	}

	protected static int performAssessmentConfChoiceStatistics(EntityManager em, DAOHelper dh, int statsCounter, AssessmentStatistics astat,
			Assessment assessment, Set<AssessmentScore> assScoreSet,
			Map<Long, String> mapGroupNamesByID, Map<Long, UserGroupStat> mapUGByUserID, 
			String courseID, String courseInstanceID, Long assessmentID, File root) {
		
		AssessmentConfChoice aConfiguration = (AssessmentConfChoice)assessment.getAssessmentConfiguration();
		String pLabels = aConfiguration.getProblemsLabels();
		if(StringUtil.isStringBlank(pLabels)) return statsCounter;
		Map<String, Integer> mapProbIndexByLabel = createProblemIndexMap(pLabels);
		String pMapping = aConfiguration.getProblemMapping();
		Map<String,Map<Integer,String[]>> problemMapping;
		if(StringUtil.isStringBlank(pMapping)) {
			problemMapping = createDefaultProblemMapping(aConfiguration);
		} else {
			problemMapping = parseProblemMapping(aConfiguration, mapProbIndexByLabel, pMapping);
		}
		if(problemMapping==null) return statsCounter;
		List<AssessmentConfChoiceAnswers> answers = dh.getAssessmentDAO().listAssessmentConfChoiceAnswersForAssessement(em, aConfiguration);
		Map<Long, ParsedAssessmentConfChoiceAnswers> mapAnswersByUID = new HashMap<Long, ParsedAssessmentConfChoiceAnswers>(answers.size());
		try {
			for(AssessmentConfChoiceAnswers ua : answers) {
				mapAnswersByUID.put(ua.getUser().getId(), new ParsedAssessmentConfChoiceAnswers(ua, problemMapping));
			}
		} catch(Exception ex) {
			ex.printStackTrace();
			return statsCounter;
		}
		
		List<UserGroupStat> allList = new ArrayList<UserGroupStat>(mapUGByUserID.size());
		Map<Long,List<UserGroupStat>> mapByGroupID = new HashMap<Long, List<UserGroupStat>>();
	fo:	for(AssessmentScore as : assScoreSet) {
			if(!as.getRawPresent()) continue fo;
			UserGroupStat ugs = mapUGByUserID.get(as.getUser().getId());
			// Ako nije vise u grupi za predavanja...
			if(ugs==null) continue;
			ugs.setScore(as.getRawScore());
			allList.add(ugs);
			List<UserGroupStat> ugl = mapByGroupID.get(ugs.getGroupID());
			if(ugl==null) {
				ugl = new ArrayList<UserGroupStat>(100);
				mapByGroupID.put(ugs.getGroupID(), ugl);
			}
			ugl.add(ugs);
		}
		StatisticsName name1 = new StatisticsName();
		name1.setId(statsCounter++);
		name1.setLectureGroupID(Long.valueOf(-1));
		name1.setLectureGroupName("");
		name1.setTitle("Detaljni podaci: odgovori po grupama na kolegiju");
		name1.setKind("D:G:-1");
		
		SingleChoiceStatistics scoreStat1 = createSingleChoiceStatistics(allList, name1, mapAnswersByUID);
		writeStatisticsBase(courseID, courseInstanceID, assessmentID, root, scoreStat1);
		scoreStat1 = null;
		astat.getAvailableStatistics().add(name1);
		name1 = null;
		for(Map.Entry<Long, List<UserGroupStat>> e : mapByGroupID.entrySet()) {
			Long groupID = e.getKey();
			List<UserGroupStat> ugl = e.getValue();
			StatisticsName name2 = new StatisticsName();
			name2.setId(statsCounter++);
			name2.setLectureGroupID(groupID);
			name2.setLectureGroupName(mapGroupNamesByID.get(groupID));
			if(name2.getLectureGroupID()==null) {
				name2.setLectureGroupName("?");
			}
			name2.setTitle("Detaljni podaci: odgovori po grupama pitanja za grupu "+name2.getLectureGroupName());
			name2.setKind("D:G:"+groupID);
			SingleChoiceStatistics scoreStat2 = createSingleChoiceStatistics(ugl, name2, mapAnswersByUID);
			writeStatisticsBase(courseID, courseInstanceID, assessmentID, root, scoreStat2);
			astat.getAvailableStatistics().add(name2);
		}
		return statsCounter;
	}

	private static final Comparator<UserGroupStat> userGroupStatComparator = new Comparator<UserGroupStat>() {
		@Override
		public int compare(UserGroupStat o1, UserGroupStat o2) {
			double d = o1.getScore()-o2.getScore();
			if(d<-1E-5) return -1;
			if(d>1E-5) return 1;
			return 0;
		}
	};
	
	private static SingleChoiceStatistics createSingleChoiceStatistics(List<UserGroupStat> uList, StatisticsName name,
			Map<Long, ParsedAssessmentConfChoiceAnswers> mapAnswersByUID) {
		Map<String,List<UserGroupStat>> ugMapByProblems = new HashMap<String, List<UserGroupStat>>(500);
		Map<String, SingleABCStatRow> rowMap = new HashMap<String, SingleABCStatRow>(1000);
		for(UserGroupStat ug : uList) {
			ParsedAssessmentConfChoiceAnswers pa = mapAnswersByUID.get(ug.getUserID());
			if(pa==null) continue;
			for(int problemIndex = 0; problemIndex < pa.coarseProblems.length; problemIndex++) {
				SingleABCStatRow row = rowMap.get(pa.coarseProblems[problemIndex]);
				if(row == null) {
					row = new SingleABCStatRow();
					row.coarse = true;
					row.key = new String(pa.coarseProblems[problemIndex]);
					rowMap.put(row.key, row);
				}
				List<UserGroupStat> list = ugMapByProblems.get(row.key);
				if(list==null) {
					list = new ArrayList<UserGroupStat>();
					ugMapByProblems.put(row.key, list);
				}
				SingleABCStatRow rowF = rowMap.get(pa.fineProblems[problemIndex]);
				if(rowF == null) {
					rowF = new SingleABCStatRow();
					rowF.coarse = false;
					rowF.key = new String(pa.fineProblems[problemIndex]);
					rowMap.put(rowF.key, rowF);
				}
				List<UserGroupStat> listF = ugMapByProblems.get(rowF.key);
				if(listF==null) {
					listF = new ArrayList<UserGroupStat>();
					ugMapByProblems.put(rowF.key, listF);
				}
				char c = pa.correctness[problemIndex].charAt(0);
				if(c=='T') {
					row.correctStudents++;
					row.totalStudents++;
					rowF.correctStudents++;
					rowF.totalStudents++;
					list.add(ug);
					listF.add(ug);
				} else if(c=='N') {
					row.wrongStudents++;
					row.totalStudents++;
					rowF.wrongStudents++;
					rowF.totalStudents++;
					list.add(ug);
					listF.add(ug);
				} else if(c=='-') {
					row.unansweredStudents++;
					row.totalStudents++;
					rowF.unansweredStudents++;
					rowF.totalStudents++;
					list.add(ug);
					listF.add(ug);
				}
			}
		}
		for(Map.Entry<String, List<UserGroupStat>> e : ugMapByProblems.entrySet()) {
			String key = e.getKey();
			List<UserGroupStat> list = e.getValue();
			Collections.sort(list, userGroupStatComparator);
			SingleABCStatRow row = rowMap.get(key);
			SingleABCStatRow rowF = row;
			if(row.coarse) {
				rowF = null;
			} else {
				row = null;
			}
			int n = list.size()/4;
			// prodi kroz prvih n...
			// prodi kroz zadnjih n...
			if(n>=3) {
				// Ako imam coarse:
				if(row!=null) {
					for(int i = 0; i < n; i++) {
						UserGroupStat ug = list.get(i);
						ParsedAssessmentConfChoiceAnswers pa = mapAnswersByUID.get(ug.getUserID());
						for(int j=0; j<pa.coarseProblems.length; j++) {
							if(pa.coarseProblems[j].equals(key)) {
								row.lowerCount++;
								if(pa.correctness[j].charAt(0)=='T') {
									row.lowerSum += 1;
								}
								row.lowerMaxSum += 1;
								break;
							}
						}
					}
					for(int i = 0; i < n; i++) {
						UserGroupStat ug = list.get(list.size()-1-i);
						ParsedAssessmentConfChoiceAnswers pa = mapAnswersByUID.get(ug.getUserID());
						for(int j=0; j<pa.coarseProblems.length; j++) {
							if(pa.coarseProblems[j].equals(key)) {
								row.upperCount++;
								if(pa.correctness[j].charAt(0)=='T') {
									row.upperSum += 1;
								}
								row.upperMaxSum += 1;
								break;
							}
						}
					}
					row.discriminationIndex = (row.upperCount==0 ? 0 : row.upperSum/row.upperMaxSum) - (row.lowerCount==0 ? 0 : row.lowerSum/row.lowerMaxSum);
					row.weightAbsolute = row.totalStudents==0 ? Double.NaN : 1-(double)row.correctStudents/(double)row.totalStudents;
					int tocnoIliKrivo = row.correctStudents + row.wrongStudents;
					row.weightRelative = tocnoIliKrivo==0 ? Double.NaN : 1-(double)row.correctStudents/(double)tocnoIliKrivo;
				} else {
					// Inace imam fine
					for(int i = 0; i < n; i++) {
						UserGroupStat ug = list.get(i);
						ParsedAssessmentConfChoiceAnswers pa = mapAnswersByUID.get(ug.getUserID());
						for(int j=0; j<pa.fineProblems.length; j++) {
							if(pa.fineProblems[j].equals(key)) {
								rowF.lowerCount++;
								if(pa.correctness[j].charAt(0)=='T') {
									rowF.lowerSum += 1;
								}
								rowF.lowerMaxSum += 1;
								break;
							}
						}
					}
					for(int i = 0; i < n; i++) {
						UserGroupStat ug = list.get(list.size()-1-i);
						ParsedAssessmentConfChoiceAnswers pa = mapAnswersByUID.get(ug.getUserID());
						for(int j=0; j<pa.fineProblems.length; j++) {
							if(pa.fineProblems[j].equals(key)) {
								rowF.upperCount++;
								if(pa.correctness[j].charAt(0)=='T') {
									rowF.upperSum += 1;
								}
								rowF.upperMaxSum += 1;
								break;
							}
						}
					}
					rowF.discriminationIndex = (rowF.upperCount==0 ? 0 : rowF.upperSum/rowF.upperMaxSum) - (rowF.lowerCount==0 ? 0 : rowF.lowerSum/rowF.lowerMaxSum);
					rowF.weightAbsolute = rowF.totalStudents==0 ? Double.NaN : 1-(double)rowF.correctStudents/(double)rowF.totalStudents;
					int tocnoIliKrivo = rowF.correctStudents + rowF.wrongStudents;
					rowF.weightRelative = tocnoIliKrivo==0 ? Double.NaN : 1-(double)rowF.correctStudents/(double)tocnoIliKrivo;
				}
			} else {
				if(row!=null) {
					row.discriminationIndex = Double.NaN;
					row.weightAbsolute = row.totalStudents==0 ? Double.NaN : 1-(double)row.correctStudents/(double)row.totalStudents;
					int tocnoIliKrivo = row.correctStudents + row.wrongStudents;
					row.weightRelative = tocnoIliKrivo==0 ? Double.NaN : 1-(double)row.correctStudents/(double)tocnoIliKrivo;
				} else {
					rowF.discriminationIndex = Double.NaN;
					rowF.weightAbsolute = rowF.totalStudents==0 ? Double.NaN : 1-(double)rowF.correctStudents/(double)rowF.totalStudents;
					int tocnoIliKrivo = rowF.correctStudents + rowF.wrongStudents;
					rowF.weightRelative = tocnoIliKrivo==0 ? Double.NaN : 1-(double)rowF.correctStudents/(double)tocnoIliKrivo;
				}
			}
		}
		List<SingleABCStatRow> list = new ArrayList<SingleABCStatRow>(rowMap.values());
		Collections.sort(list, new Comparator<SingleABCStatRow>() {
			@Override
			public int compare(SingleABCStatRow o1, SingleABCStatRow o2) {
				return o1.key.compareTo(o2.key);
			}
		});

		List<SingleChoiceStatisticsRow> rows = new ArrayList<SingleChoiceStatisticsRow>(list.size());
		for(SingleABCStatRow r : list) {
			SingleChoiceStatisticsRow row = new SingleChoiceStatisticsRow(
				r.key, r.totalStudents, r.correctStudents, r.unansweredStudents, r.wrongStudents, r.discriminationIndex, r.weightAbsolute, r.weightRelative, r.coarse
			);
			rows.add(row);
		}
		SingleChoiceStatistics s = new SingleChoiceStatistics(rows);
		s.setKind(name.getKind());
		s.setId(name.getId());
		return s;
	}

	static class SingleABCStatRow {
		String key;
		int totalStudents;
		int correctStudents;
		int wrongStudents;
		int unansweredStudents;
		double upperSum;
		double upperMaxSum;
		int upperCount;
		double lowerSum;
		double lowerMaxSum;
		int lowerCount;
		double weightAbsolute;
		double weightRelative;
		double discriminationIndex;
		boolean coarse;
	}
	
	private static Map<String, Map<Integer, String[]>> parseProblemMapping(AssessmentConfChoice aConfiguration, Map<String, Integer> mapProbIndexByLabel, String mapping) {
		if(mapping==null) mapping="";
		String[] elems = StringUtil.split(mapping,'\n');
		Map<String, Map<Integer, String[]>> gmap = new HashMap<String, Map<Integer,String[]>>(aConfiguration.getGroupsNum());
		for(int i = 0; i < elems.length; i++) {
			elems[i] = elems[i].trim();
			if(elems[i].isEmpty()) continue;
			String[] elems2 = StringUtil.split(elems[i], '\t');
			if(elems2.length!=4) return null;
			for(int j = 0; j < elems2.length; j++) {
				elems2[j] = elems2[j].trim();
				if(elems2[j].isEmpty()) return null;
			}
			Map<Integer, String[]> map = gmap.get(elems2[0]);
			if(map==null) {
				map = new HashMap<Integer, String[]>(aConfiguration.getProblemsNum());
				gmap.put(elems2[0], map);
			}
			Integer key = mapProbIndexByLabel.get(elems2[1]);
			if(key==null) return null;
			map.put(key, new String[] {elems2[2], elems2[3]});
		}
		return gmap;
	}

	private static Map<String, Map<Integer, String[]>> createDefaultProblemMapping(AssessmentConfChoice configuration) {
		List<String> glabels = new ArrayList<String>();
		String pGroups = configuration.getGroupsLabels();
		if(pGroups==null) pGroups="";
		String[] elems = StringUtil.split(pGroups,'\n');
		for(int i = 0; i < elems.length; i++) {
			elems[i] = elems[i].trim();
			if(elems[i].isEmpty()) continue;
			String[] elems2 = StringUtil.split(elems[i], '\t');
			for(int j = 0; j < elems2.length; j++) {
				elems2[j] = elems2[j].trim();
				if(elems2[j].isEmpty()) continue;
				glabels.add(elems2[j]);
			}				
		}
		Map<String, Map<Integer, String[]>> gmap = new HashMap<String, Map<Integer,String[]>>(glabels.size());
		for(String groupLabel : glabels) {
			Map<Integer, String[]> map = new HashMap<Integer, String[]>(configuration.getProblemsNum());
			for(int i = 0; i < configuration.getProblemsNum(); i++) {
				map.put(Integer.valueOf(i), new String[] {Integer.toString(i+1), (i+1)+"/"+groupLabel});
			}
			gmap.put(groupLabel, map);
		}
		return gmap;
	}

	/**
	 * Vraca mapu: labela-zadatka, pozicija-zadatka (pozicije idu od 0).
	 * @param labels
	 * @return
	 */
	private static Map<String, Integer> createProblemIndexMap(String labels) {
		String[] labelsArray = StringUtil.split(labels, '\t');
		Map<String, Integer> map = new HashMap<String, Integer>();
		for(int i = 0; i < labelsArray.length; i++) {
			map.put(labelsArray[i], Integer.valueOf(i));
		}
		return map;
	}

	protected static class ParsedAssessmentConfChoiceAnswers {
		AssessmentConfChoiceAnswers userAnswers;
		String[] correctness;
		String[] letters;
		String[] coarseProblems;
		String[] fineProblems;
		
		public ParsedAssessmentConfChoiceAnswers(AssessmentConfChoiceAnswers userAnswers, Map<String, Map<Integer, String[]>> problemMapping) {
			this.userAnswers = userAnswers;
			correctness = StringUtil.split(userAnswers.getAnswersStatus(), '\t');
			letters = StringUtil.split(userAnswers.getAnswers(), '\t');
			coarseProblems = new String[letters.length];
			fineProblems = new String[letters.length];
			Map<Integer, String[]> map = problemMapping.get(userAnswers.getGroup());
			if(map==null) {
				throw new IllegalArgumentException("Pronadena nepoznata grupa.");
			}
			for(int i = 0; i < coarseProblems.length; i++) {
				Integer key = Integer.valueOf(i);
				coarseProblems[i] = map.get(key)[0];
				fineProblems[i] = map.get(key)[1];
			}
		}
	}
	
	protected static void writeStatisticsListing(String courseID, String courseInstanceID, Long assessmentID, File root, File listingFile, AssessmentStatistics astat) {
		File outFile = listingFile;
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(outFile))); 
			oos.writeObject(astat);
			oos.close();
		} catch(Exception ex) {
			if(oos!=null) try { oos.close(); } catch(Exception ignorable) {}
			ex.printStackTrace();
		}
	}

	protected static AssessmentStatistics readStatisticsListing(String courseID, String courseInstanceID, Long assessmentID, File root, File listingFile) {
		File inFile = listingFile;
		AssessmentStatistics astat = null;
		ObjectInputStream oos = null;
		try {
			oos = new ObjectInputStream(new BufferedInputStream(new FileInputStream(inFile))); 
			astat = (AssessmentStatistics)oos.readObject();
			oos.close();
		} catch(Exception ex) {
			if(oos!=null) try { oos.close(); } catch(Exception ignorable) {}
			ex.printStackTrace();
		}
		return astat;
	}

	protected static void writeStatisticsBase(String courseID, String courseInstanceID, Long assessmentID, File root, StatisticsBase stat) {
		File outFile = new File(root, Integer.toString(stat.getId()));
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(outFile))); 
			oos.writeObject(stat);
			oos.close();
		} catch(Exception ex) {
			if(oos!=null) try { oos.close(); } catch(Exception ignorable) {}
			ex.printStackTrace();
		}
	}

	protected static StatisticsBase readStatisticsBase(String courseID, String courseInstanceID, Long assessmentID, File root, int id) {
		File inFile = new File(root, Integer.toString(id));
		StatisticsBase stat = null;
		ObjectInputStream oos = null;
		try {
			oos = new ObjectInputStream(new BufferedInputStream(new FileInputStream(inFile))); 
			stat = (StatisticsBase)oos.readObject();
			oos.close();
		} catch(Exception ex) {
			if(oos!=null) try { oos.close(); } catch(Exception ignorable) {}
			ex.printStackTrace();
		}
		return stat;
	}

	protected static ScoreStatistics createScoreStatistics(List<UserGroupStat> allList, int id, String kind) {
		ScoreStatistics s = new ScoreStatistics();
		s.setCount(allList.size());
		s.setKind(kind);
		s.setId(id);
		double[] data = new double[allList.size()];
		double avg = 0;
		double min = 0;
		double max = 0;
		if(data.length>0) {
			min = allList.get(0).getScore();
			max = min;
		}
		for(int i = 0; i < data.length; i++) {
			data[i] = allList.get(i).getScore();
			avg += data[i];
			if(min > data[i]) min = data[i];
			if(max < data[i]) max = data[i];
		}
		s.setMinimum(min);
		s.setMaximum(max);
		if(s.getCount()==0) {
			s.setAverage(0);
			s.setMedian(0);
		} else if(s.getCount()==1) {
			s.setAverage(data[0]);
			s.setMedian(data[0]);
		} else {
			s.setAverage(avg/s.getCount());
			Arrays.sort(data);
			if(data.length%2==1) {
				s.setMedian(data[data.length/2]);
			} else {
				int c = data.length/2;
				s.setMedian((data[c-1]+data[c])/2);
			}
			s.setAllScore(data);
		}
		return s;
	}

	private static File getAssessmentStatisticsRoot(String courseID, String courseInstanceID, Long assessmentID) {
		String ciid = courseInstanceID.replaceAll("/", "_");
		ciid = ciid.replaceAll("\\\\", "_");
		return new File(new File(new File(JCMSSettings.getSettings().getAssessmentStatsRootDir(),courseID), ciid), assessmentID.toString());
	}
	
	private static void getReadLock(Long assessmentID) {
		synchronized(syncObject) {
			while(true) {
				int[] counter = lockMap.get(assessmentID);
				if(counter==null) {
					counter = new int[] {1};
					lockMap.put(assessmentID, counter);
					return;
				}
				if(counter[0]>=0) {
					counter[0]++;
					return;
				}
				// Inace je counter[0] negativan, a to znaci da
				// netko drzi write lock! Cekaj!
				try {
					syncObject.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	private static void releaseReadLock(Long assessmentID) {
		synchronized(syncObject) {
			while(true) {
				int[] counter = lockMap.get(assessmentID);
				if(counter==null) {
					// Nesto gadno se dogada!
					return;
				}
				if(counter[0]>0) {
					counter[0]--;
					if(counter[0]<=0) {
						lockMap.remove(assessmentID);
						syncObject.notifyAll();
					}
					return;
				}
				// Inace se opet nesto gadno se dogada!
				break;
			}
		}
	}
	
	private static void getWriteLock(Long assessmentID) {
		synchronized(syncObject) {
			while(true) {
				int[] counter = lockMap.get(assessmentID);
				if(counter==null) {
					counter = new int[] {-1};
					lockMap.put(assessmentID, counter);
					return;
				}
				// Inace counter[0] postoji, a to znaci da
				// netko drzi ili read ili write lock! Cekaj!
				try {
					syncObject.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private static void releaseWriteLock(Long assessmentID) {
		synchronized(syncObject) {
			while(true) {
				int[] counter = lockMap.get(assessmentID);
				if(counter==null) {
					// Nesto gadno se dogada!
					return;
				}
				if(counter[0]==-1) {
					lockMap.remove(assessmentID);
					syncObject.notifyAll();
					return;
				}
				// Inace se opet nesto gadno se dogada!
				break;
			}
		}
	}

	private static void degradeWriteLockToReadLock(Long assessmentID) {
		synchronized(syncObject) {
			while(true) {
				int[] counter = lockMap.get(assessmentID);
				if(counter==null) {
					// Nesto gadno se dogada!
					return;
				}
				if(counter[0]==-1) {
					counter[0] = 1;
					syncObject.notifyAll();
					return;
				}
				// Inace se opet nesto gadno se dogada!
				break;
			}
		}
	}

	public static void init() {
	}
	
	public static void destroy() {
	}
}
