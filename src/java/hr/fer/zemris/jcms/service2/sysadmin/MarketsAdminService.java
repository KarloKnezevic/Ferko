package hr.fer.zemris.jcms.service2.sysadmin;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import hr.fer.zemris.jcms.beans.ext.MPGSVCourse;
import hr.fer.zemris.jcms.beans.ext.MPGSVGroup;
import hr.fer.zemris.jcms.beans.ext.MPGSVMarketPlace;
import hr.fer.zemris.jcms.dao.DAOHelper;
import hr.fer.zemris.jcms.dao.DAOHelperFactory;
import hr.fer.zemris.jcms.model.CourseInstance;
import hr.fer.zemris.jcms.model.Group;
import hr.fer.zemris.jcms.model.MarketPlace;
import hr.fer.zemris.jcms.model.YearSemester;
import hr.fer.zemris.jcms.parsers.TextService;
import hr.fer.zemris.jcms.security.JCMSSecurityManagerFactory;
import hr.fer.zemris.jcms.service.util.CourseInstanceUtil;
import hr.fer.zemris.jcms.service.util.GroupUtil;
import hr.fer.zemris.jcms.service2.BasicServiceSupport;
import hr.fer.zemris.jcms.web.actions.data.MPGroupSettingsViewData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.util.StringUtil;

import javax.persistence.EntityManager;

public class MarketsAdminService {

	public static void prepareGroupSettingsInput(EntityManager em, MPGroupSettingsViewData data) {
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		if(!JCMSSecurityManagerFactory.getManager().canPerformSystemAdministration()) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		data.setAllSemesters(dh.getYearSemesterDAO().list(em));
		data.setSemesterID(BasicServiceSupport.getCurrentSemesterID(em));
		
		data.setResult(AbstractActionData.RESULT_INPUT);
	}

	public static void prepareGroupSettingsView(EntityManager em, MPGroupSettingsViewData data) {
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		if(!JCMSSecurityManagerFactory.getManager().canPerformSystemAdministration()) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		data.setAllSemesters(dh.getYearSemesterDAO().list(em));
		String prp = StringUtil.isStringBlank(data.getParentRelativePath()) ? "0" : data.getParentRelativePath();
		String cysemID = StringUtil.isStringBlank(data.getSemesterID()) ? BasicServiceSupport.getCurrentSemesterID(em) : data.getSemesterID();
		YearSemester ysem = StringUtil.isStringBlank(cysemID) ? null : dh.getYearSemesterDAO().get(em, cysemID);
		if(ysem==null) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		data.setYearSemester(ysem);
		data.setParentRelativePath(prp);
		List<CourseInstance> allCourses = dh.getCourseInstanceDAO().findForSemester(em, ysem.getId());
		Map<String, CourseInstance> mapByIsvu = CourseInstanceUtil.mapCourseInstanceByID(allCourses);
		List<Group> allGroups = dh.getGroupDAO().findSubgroupsLLE(em, ysem.getId()+"/%", prp, prp+"/%");
		Map<String, List<Group>> groupsByCourses = GroupUtil.mapGroupByCompositeCourseID(allGroups);
		Map<String, Group> courseMarketPlaces = GroupUtil.mapMarketPlacesByCompositeCourseID(allGroups);
		data.setCourses(new ArrayList<MPGSVCourse>(groupsByCourses.size()));
		for(String key : groupsByCourses.keySet()) {
			List<Group> groups = groupsByCourses.get(key);
			Group parentGroup = courseMarketPlaces.get(key);
			CourseInstance courseInstance = mapByIsvu.get(key);
			MPGSVCourse c = new MPGSVCourse();
			c.setCourseName(courseInstance.getCourse().getName());
			c.setIsvuCode(courseInstance.getCourse().getIsvuCode());
			c.setGroups(new ArrayList<MPGSVGroup>(groups.size()));
			MPGSVMarketPlace mp = new MPGSVMarketPlace();
			if(parentGroup.getMarketPlace()==null) {
				mp.setAbsent(true);
				mp.setTimeBuffer(-1);
			} else {
				if(parentGroup.getMarketPlace().getOpen()) {
					data.getSelectedMarketPlaces().add(parentGroup.getMarketPlace().getId());
				}
				mp.setAbsent(false);
				mp.setTimeBuffer(parentGroup.getMarketPlace().getTimeBuffer());
				mp.setFormulaConstraints(parentGroup.getMarketPlace().getFormulaConstraints());
				mp.setId(parentGroup.getMarketPlace().getId());
				mp.setOpen(parentGroup.getMarketPlace().getOpen());
				mp.setOpenFrom(parentGroup.getMarketPlace().getOpenFrom());
				mp.setOpenUntil(parentGroup.getMarketPlace().getOpenUntil());
				mp.setSecurityConstraints(parentGroup.getMarketPlace().getSecurityConstraints());
			}
			c.setMarketPlace(mp);
			for(Group g : groups) {
				if(g.isManagedRoot()) continue;
				MPGSVGroup gr = new MPGSVGroup();
				gr.setCapacity(g.getCapacity());
				gr.setCompositeCourseID(g.getCompositeCourseID());
				gr.setEnteringAllowed(g.isEnteringAllowed());
				gr.setId(g.getId());
				gr.setLeavingAllowed(g.isLeavingAllowed());
				gr.setManagedRoot(g.isManagedRoot());
				gr.setMpSecurityTag(g.getMpSecurityTag());
				gr.setName(g.getName());
				gr.setRelativePath(g.getRelativePath());
				c.getGroups().add(gr);
			}
			Collections.sort(c.getGroups());
			data.getCourses().add(c);
		}
		Collections.sort(data.getCourses());
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}

	public static void changeMarketPlaceStatusByMPIDs(EntityManager em, MPGroupSettingsViewData data, boolean shouldOpen) {
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		if(!JCMSSecurityManagerFactory.getManager().canPerformSystemAdministration()) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		
		int brojPromjena = 0;
		for(Long id : data.getSelectedMarketPlaces()) {
			MarketPlace mp = dh.getMarketPlaceDAO().getMarketPlace(em, id);
			if(mp!=null && mp.getOpen() != shouldOpen) {
				mp.setOpen(shouldOpen);
				brojPromjena++;
			}
		}

		if(brojPromjena==0) {
			data.getMessageLogger().addInfoMessage("Niti jedna burza nije mijenjana.");
		} else {
			if(shouldOpen) {
				data.getMessageLogger().addInfoMessage("Broj burzi koje su otvorene: "+brojPromjena+".");
			} else {
				data.getMessageLogger().addInfoMessage("Broj burzi koje su zatvorene: "+brojPromjena+".");
			}
		}
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}


	public static void changeMarketPlaceStatusByCourses(EntityManager em, MPGroupSettingsViewData data, boolean shouldOpen) {
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		if(!JCMSSecurityManagerFactory.getManager().canPerformSystemAdministration()) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		
		List<String> lines = null;
		try {
			lines = TextService.readerToStringList(new StringReader(data.getIds()==null ? "" : data.getIds()));
		} catch (IOException e) {
			lines = new ArrayList<String>();
		}

		String cysemID = StringUtil.isStringBlank(data.getSemesterID()) ? BasicServiceSupport.getCurrentSemesterID(em) : data.getSemesterID();
		YearSemester ysem = StringUtil.isStringBlank(cysemID) ? null : dh.getYearSemesterDAO().get(em, cysemID);
		if(ysem==null) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		
		String prp = StringUtil.isStringBlank(data.getParentRelativePath()) ? "0" : data.getParentRelativePath();
		List<Group> allGroups = new ArrayList<Group>(100);
		List<MarketPlace> selectedMarketPlaces = new ArrayList<MarketPlace>(allGroups.size());
		for(String isvuCode : lines) {
			if(isvuCode.isEmpty()) continue;
			Group group = dh.getGroupDAO().get(em, ysem.getId()+"/"+isvuCode, prp);
			if(group!=null) {
				allGroups.add(group);
			} else {
				data.getMessageLogger().addWarningMessage("Kolegij "+isvuCode+" nema grupu sa relPath="+prp+".");
				continue;
			}
			if(!group.isManagedRoot()) {
				data.getMessageLogger().addWarningMessage("Kolegij "+isvuCode+", grupa sa relPath="+prp+": grupa nije burza!");
				continue;
			}
			if(group.getMarketPlace()==null) {
				data.getMessageLogger().addWarningMessage("Kolegij "+isvuCode+", grupa sa relPath="+prp+": grupa nema stvorenu burzu!");
				continue;
			}
			selectedMarketPlaces.add(group.getMarketPlace());
		}
		int brojPromjena = 0;
		for(MarketPlace mp : selectedMarketPlaces) {
			if(mp.getOpen() != shouldOpen) {
				mp.setOpen(shouldOpen);
				brojPromjena++;
			}
		}
		if(brojPromjena==0) {
			data.getMessageLogger().addInfoMessage("Niti jedna burza nije mijenjana.");
		} else {
			if(shouldOpen) {
				data.getMessageLogger().addInfoMessage("Broj burzi koje su otvorene: "+brojPromjena+".");
			} else {
				data.getMessageLogger().addInfoMessage("Broj burzi koje su zatvorene: "+brojPromjena+".");
			}
		}
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}

}
