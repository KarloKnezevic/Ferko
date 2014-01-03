package hr.fer.zemris.jcms.service2.course.assessments;

import java.awt.Color;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import hr.fer.zemris.jcms.dao.DAOHelper;
import hr.fer.zemris.jcms.dao.DAOHelperFactory;
import hr.fer.zemris.jcms.model.Group;
import hr.fer.zemris.jcms.model.GroupOwner;
import hr.fer.zemris.jcms.security.JCMSSecurityManagerFactory;
import hr.fer.zemris.jcms.service.AssessmentStatisticsService;
import hr.fer.zemris.jcms.service.AssessmentStatisticsService.AssessmentStatisticsProvider;
import hr.fer.zemris.jcms.statistics.assessments.AssessmentStatistics;
import hr.fer.zemris.jcms.statistics.assessments.ScoreStatistics;
import hr.fer.zemris.jcms.statistics.assessments.StatisticsBase;
import hr.fer.zemris.jcms.statistics.assessments.StatisticsName;
import hr.fer.zemris.jcms.web.actions.data.AssessmentStatData;
import hr.fer.zemris.jcms.web.actions.data.StudentAssessmentStatData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.util.DeleteOnCloseFileInputStream;

import javax.persistence.EntityManager;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.statistics.HistogramDataset;

/**
 * Sloj usluge koji nudi statističku obradu provjera.
 * 
 * @author marcupic
 *
 */
public class AssessmentStatService {

	/**
	 * Dohvat statističkih podataka o provjeri.
	 * 
	 * @param em entity manager
	 * @param data podatci
	 */
	public static void getStatistics(EntityManager em, final AssessmentStatData data) {
		final Set<Long> allowedGroupIDs = new HashSet<Long>();
		final boolean[] canViewCourseWide = new boolean[] {false};
		final String[] identifiers = new String[3];
		
		// Dohvat provjere
		if(!AssessmentServiceSupport.fillAssessment(em, data, data.getAssessmentID())) return;
		
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		boolean canView = JCMSSecurityManagerFactory.getManager().canViewAssessments(data.getCourseInstance());
		if(!canView) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		Group lectureGroupsRoot = dh.getGroupDAO().get(em, data.getCourseInstance().getId(), "0");
		if(JCMSSecurityManagerFactory.getManager().canManageAssessments(data.getCourseInstance())) {
			for(Group g : lectureGroupsRoot.getSubgroups()) {
				allowedGroupIDs.add(g.getId());
				canViewCourseWide[0] = true;
			}
		} else {
			List<GroupOwner> allOwners = dh.getGroupDAO().findForSubgroups(em, data.getCourseInstance().getId(), "0");
			for(GroupOwner go : allOwners) {
				if(go.getUser().getId().equals(data.getCurrentUser().getId())) {
					allowedGroupIDs.add(go.getGroup().getId());
				}
			}
		}
		data.setResult(AbstractActionData.RESULT_SUCCESS);
		identifiers[0] = data.getCourseInstance().getCourse().getIsvuCode();
		identifiers[1] = data.getCourseInstance().getId();
		identifiers[2] = data.getAssessment().getId().toString();
		
		AssessmentStatisticsService.accessStatistics(identifiers[0], identifiers[1], Long.valueOf(identifiers[2]), new AssessmentStatisticsService.AssessmentStatisticsRequester() {
			@Override
			public void process(AssessmentStatisticsProvider provider) {
				AssessmentStatistics stat = provider.getAssessmentStatistics();
				if(stat==null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Assessments.stat.unaccessible"));
					return;
				}
				List<StatisticsName> stats = stat.getAvailableStatistics();
				if(!canViewCourseWide[0]) {
					Iterator<StatisticsName> it = stats.iterator();
					while(it.hasNext()) {
						StatisticsName s = it.next();
						// Fix: promjenio iz "!=-1 &&" u "==-1 ||", tako da nastavnik vidi svoje grupe, i globalno stanje.
						if(s.getLectureGroupID().longValue()==-1 || allowedGroupIDs.contains(s.getLectureGroupID())) {
							continue;
						}
						it.remove();
					}
				}
				data.setStat(stat);
				StatisticsName s = null;
				if(data.getLocalID()!=null) {
					for(int i = 0; i < stats.size(); i++) {
						StatisticsName s2 = stats.get(i);
						if(s2.getId()==data.getLocalID().intValue()) {
							s = s2;
							break;
						}
					}
					if(s==null) {
						data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
						data.setResult(AbstractActionData.RESULT_FATAL);
						return;
					}
					StatisticsBase base = provider.getStatisticsBase(data.getLocalID().intValue());
					if(base==null) {
						data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
						data.setResult(AbstractActionData.RESULT_FATAL);
						return;
					}
					data.setStatBase(base);
				}
			}
		});
	}

	/**
	 * Dohvat statističkih podataka o provjeri - za studente.
	 * 
	 * @param em entity manager
	 * @param data podatci
	 */
	public static void getStudentStatistics(EntityManager em, final StudentAssessmentStatData data) {
		final String[] identifiers = new String[3];
		
		data.setImposter(false);
		
		// Dohvat provjere
		if(!AssessmentServiceSupport.fillAssessment(em, data, data.getAssessmentID())) return;
		
		boolean canView = JCMSSecurityManagerFactory.getManager().isStudentOnCourse(data.getCourseInstance());
		if(!canView && (JCMSSecurityManagerFactory.getManager().isStaffOnCourse(data.getCourseInstance()) || JCMSSecurityManagerFactory.getManager().isAdmin())) {
			data.setImposter(true);
			canView = true;
		}
		if(!canView) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		
		data.setResult(AbstractActionData.RESULT_SUCCESS);
		identifiers[0] = data.getCourseInstance().getCourse().getIsvuCode();
		identifiers[1] = data.getCourseInstance().getId();
		identifiers[2] = data.getAssessment().getId().toString();
		
		AssessmentStatisticsService.accessStatistics(identifiers[0], identifiers[1], Long.valueOf(identifiers[2]), new AssessmentStatisticsService.AssessmentStatisticsRequester() {
			@Override
			public void process(AssessmentStatisticsProvider provider) {
				AssessmentStatistics stat = provider.getAssessmentStatistics();
				if(stat==null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Assessments.stat.unaccessible"));
					return;
				}
				List<StatisticsName> stats = stat.getAvailableStatistics();
				Iterator<StatisticsName> it = stats.iterator();
				while(it.hasNext()) {
					StatisticsName s = it.next();
					if(s.getLectureGroupID().longValue()!=-1 || (!s.getKind().startsWith("E:G:") && !s.getKind().startsWith("A:G:"))) {
						it.remove();
					}
				}
				data.setStat(stat);
				StatisticsName s = null;
				if(data.getKind()!=null) {
					for(int i = 0; i < stats.size(); i++) {
						StatisticsName s2 = stats.get(i);
						if((data.getKind().equals("A") && s2.getKind().equals("A:G:-1"))||(data.getKind().equals("E") && s2.getKind().equals("E:G:-1"))) {
							s = s2;
							break;
						}
					}
					if(s==null) {
						data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
						data.setResult(AbstractActionData.RESULT_FATAL);
						return;
					}
					StatisticsBase base = provider.getStatisticsBase(s.getId());
					if(base==null) {
						data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
						data.setResult(AbstractActionData.RESULT_FATAL);
						return;
					}
					data.setStatBase(base);
				}
			}
		});
	}
	
	/**
	 * Dohvat histograma za provjeru znanja.
	 * 
	 * @param em entity manager
	 * @param data podatci
	 */
	public static void getScoreHistogram(EntityManager em, final AssessmentStatData data) {
		final Set<Long> allowedGroupIDs = new HashSet<Long>();
		final boolean[] canViewCourseWide = new boolean[] {false};
		final String[] identifiers = new String[3];
		
		// Dohvat zastavice
		AssessmentServiceSupport.fillAssessment(em, data, data.getAssessmentID());
		
		// Pripazi na broj podjela
    	if(data.getBins()==null || data.getBins().intValue()<3) {
    		data.setBins(Integer.valueOf(10));
    	}
    	if(data.getBins().intValue()>100) data.setBins(Integer.valueOf(100));
    	
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		boolean canView = JCMSSecurityManagerFactory.getManager().canViewAssessments(data.getCourseInstance());
		if(!canView) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		Group lectureGroupsRoot = dh.getGroupDAO().get(em, data.getCourseInstance().getId(), "0");
		if(JCMSSecurityManagerFactory.getManager().canManageAssessments(data.getCourseInstance())) {
			for(Group g : lectureGroupsRoot.getSubgroups()) {
				allowedGroupIDs.add(g.getId());
				canViewCourseWide[0] = true;
			}
		} else {
			List<GroupOwner> allOwners = dh.getGroupDAO().findForSubgroups(em, data.getCourseInstance().getId(), "0");
			for(GroupOwner go : allOwners) {
				if(go.getUser().getId().equals(data.getCurrentUser().getId())) {
					allowedGroupIDs.add(go.getGroup().getId());
				}
			}
		}
		data.setResult(AbstractActionData.RESULT_SUCCESS);
		identifiers[0] = data.getCourseInstance().getCourse().getIsvuCode();
		identifiers[1] = data.getCourseInstance().getId();
		identifiers[2] = data.getAssessment().getId().toString();

		AssessmentStatisticsService.accessStatistics(identifiers[0], identifiers[1], Long.valueOf(identifiers[2]), new AssessmentStatisticsService.AssessmentStatisticsRequester() {
			@Override
			public void process(AssessmentStatisticsProvider provider) {
				AssessmentStatistics stat = provider.getAssessmentStatistics();
				if(stat==null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Assessments.stat.unaccessible"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return;
				}
				if(data.getLocalID()==null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Assessments.stat.unaccessible"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return;
				}
				List<StatisticsName> stats = stat.getAvailableStatistics();

				StatisticsName s = null;
				for(int i = 0; i < stats.size(); i++) {
					StatisticsName s2 = stats.get(i);
					if(s2.getId()==data.getLocalID().intValue()) {
						s = s2;
						break;
					}
				}
				if(s==null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return;
				}
				if(!canViewCourseWide[0]) {
					// Fix: dodan uvjet: "s.getLectureGroupID()!=-1 && " kojim se osigurava da nastavnik moze vidjeti globalnu statistiku
					if(s.getLectureGroupID()!=-1 && !allowedGroupIDs.contains(s.getLectureGroupID())) {
						data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
						data.setResult(AbstractActionData.RESULT_FATAL);
						return;
					}
				}
				
				StatisticsBase base = provider.getStatisticsBase(data.getLocalID().intValue());
				if(base==null || !(base instanceof ScoreStatistics)) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return;
				}

				File f = null;
				try {
					f = File.createTempFile("JCMS_", ".png");
				} catch (IOException e) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.couldNotCreateTmpFile"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return;
				}

				BufferedOutputStream os = null;
				try {
					os = new BufferedOutputStream(new FileOutputStream(f));
					HistogramDataset hdataset = new HistogramDataset();
					hdataset.addSeries("Kljuc", ((ScoreStatistics)base).getAllScore(),data.getBins());
					
					JFreeChart chart = ChartFactory.createHistogram("Raspodjela bodova", "Bodovi", "Broj studenata", hdataset, PlotOrientation.VERTICAL, false, true, false);
					chart.setBackgroundPaint(Color.yellow);
					chart.getTitle().setPaint(Color.blue);
					ChartUtilities.writeChartAsPNG(os, chart, 600, 300);
				} catch (IOException e) {
					try { if(os!=null) os.close(); } catch(Exception ignorable) {}
					f.delete();
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.couldNotGeneratePDF"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return;
				}
				try { if(os!=null) os.close(); } catch(Exception ignorable) {}
				DeleteOnCloseFileInputStream stream = null;
				try {
					stream = new DeleteOnCloseFileInputStream(f);
				} catch (IOException e) {
					f.delete();
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.couldNotGeneratePDF"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return;
				}
				stream.setFileName("histogram.png");
				stream.setMimeType("image/png");
				data.setStream(stream);
				data.setResult(AbstractActionData.RESULT_SUCCESS);
			}
		});
	}

	/**
	 * Dohvat histograma za provjeru znanja, za studente.
	 * 
	 * @param em entity manager
	 * @param data podatci
	 */
	public static void getStudentScoreHistogram(EntityManager em, final StudentAssessmentStatData data) {
		final String[] identifiers = new String[3];
		
		// Dohvat zastavice
		AssessmentServiceSupport.fillAssessment(em, data, data.getAssessmentID());
		
		// Pripazi na broj podjela
    	if(data.getBins()==null || data.getBins().intValue()<3) {
    		data.setBins(Integer.valueOf(10));
    	}
    	if(data.getBins().intValue()>100) data.setBins(Integer.valueOf(100));
    	
		boolean canView = JCMSSecurityManagerFactory.getManager().isStudentOnCourse(data.getCourseInstance()) || JCMSSecurityManagerFactory.getManager().isAdmin() || JCMSSecurityManagerFactory.getManager().isStaffOnCourse(data.getCourseInstance());
		if(!canView) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		data.setResult(AbstractActionData.RESULT_SUCCESS);
		identifiers[0] = data.getCourseInstance().getCourse().getIsvuCode();
		identifiers[1] = data.getCourseInstance().getId();
		identifiers[2] = data.getAssessment().getId().toString();

		AssessmentStatisticsService.accessStatistics(identifiers[0], identifiers[1], Long.valueOf(identifiers[2]), new AssessmentStatisticsService.AssessmentStatisticsRequester() {
			@Override
			public void process(AssessmentStatisticsProvider provider) {
				AssessmentStatistics stat = provider.getAssessmentStatistics();
				if(stat==null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Assessments.stat.unaccessible"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return;
				}
				if(!"A".equals(data.getKind()) && !"E".equals(data.getKind())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Assessments.stat.unaccessible"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return;
				}
				List<StatisticsName> stats = stat.getAvailableStatistics();

				StatisticsName s = null;
				for(int i = 0; i < stats.size(); i++) {
					StatisticsName s2 = stats.get(i);
					if((data.getKind().equals("A") && s2.getKind().equals("A:G:-1")) || (data.getKind().equals("E") && s2.getKind().equals("E:G:-1"))) {
						s = s2;
						break;
					}
				}
				if(s==null) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return;
				}
				
				StatisticsBase base = provider.getStatisticsBase(s.getId());
				if(base==null || !(base instanceof ScoreStatistics)) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return;
				}

				File f = null;
				try {
					f = File.createTempFile("JCMS_", ".png");
				} catch (IOException e) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.couldNotCreateTmpFile"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return;
				}

				BufferedOutputStream os = null;
				try {
					os = new BufferedOutputStream(new FileOutputStream(f));
					HistogramDataset hdataset = new HistogramDataset();
					hdataset.addSeries("Kljuc", ((ScoreStatistics)base).getAllScore(),data.getBins());
					
					JFreeChart chart = ChartFactory.createHistogram("Raspodjela bodova", "Bodovi", "Broj studenata", hdataset, PlotOrientation.VERTICAL, false, true, false);
					chart.setBackgroundPaint(Color.yellow);
					chart.getTitle().setPaint(Color.blue);
					ChartUtilities.writeChartAsPNG(os, chart, 600, 300);
				} catch (IOException e) {
					try { if(os!=null) os.close(); } catch(Exception ignorable) {}
					f.delete();
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.couldNotGeneratePDF"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return;
				}
				try { if(os!=null) os.close(); } catch(Exception ignorable) {}
				DeleteOnCloseFileInputStream stream = null;
				try {
					stream = new DeleteOnCloseFileInputStream(f);
				} catch (IOException e) {
					f.delete();
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.couldNotGeneratePDF"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return;
				}
				stream.setFileName("histogram.png");
				stream.setMimeType("image/png");
				data.setStream(stream);
				data.setResult(AbstractActionData.RESULT_SUCCESS);
			}
		});
	}

}
