package hr.fer.zemris.jcms.web.actions2.sysadmin;

import hr.fer.zemris.jcms.service2.sysadmin.MarketsAdminService;

import hr.fer.zemris.jcms.web.actions.Ext2ActionSupport;
import hr.fer.zemris.jcms.web.actions.annotations.DataResultMapping;
import hr.fer.zemris.jcms.web.actions.annotations.Struts2ResultMapping;
import hr.fer.zemris.jcms.web.actions.annotations.WebClass;
import hr.fer.zemris.jcms.web.actions.annotations.WebMethodInfo;
import hr.fer.zemris.jcms.web.actions.data.MPGroupSettingsViewData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.navig.builders.MainBuilder;

import java.util.Set;

@WebClass(dataClass=MPGroupSettingsViewData.class,defaultNavigBuilder=MainBuilder.class, defaultNavigBuilderIsRoot=false,additionalMenuItems={"m2","Navigation.globalMPOverview"})
public class MPGroupSettingsView extends Ext2ActionSupport<MPGroupSettingsViewData> {

	private static final long serialVersionUID = 2L;
	
    @WebMethodInfo
    public String execute() throws Exception {
    	return input();
    }
    
    @WebMethodInfo
    public String view() throws Exception {
    	MarketsAdminService.prepareGroupSettingsView(getEntityManager(), data);
        return null;
    }

    @WebMethodInfo
    public String input() throws Exception {
    	MarketsAdminService.prepareGroupSettingsInput(getEntityManager(), data);
        return null;
    }

    @WebMethodInfo(
    	dataResultMappings={@DataResultMapping(dataResult=AbstractActionData.RESULT_SUCCESS, struts2Result="redirect", registerDelayedMessages=true)},
    	struts2ResultMappings={@Struts2ResultMapping(struts2Result="redirect")}
    )
    public String openMPs() throws Exception {
    	MarketsAdminService.changeMarketPlaceStatusByMPIDs(getEntityManager(), data, true);
        return null;
    }

    @WebMethodInfo(
    	dataResultMappings={@DataResultMapping(dataResult=AbstractActionData.RESULT_SUCCESS, struts2Result="redirect", registerDelayedMessages=true)},
    	struts2ResultMappings={@Struts2ResultMapping(struts2Result="redirect")}
    )
    public String closeMPs() throws Exception {
    	MarketsAdminService.changeMarketPlaceStatusByMPIDs(getEntityManager(), data, false);
        return null;
    }

    @WebMethodInfo(
    	dataResultMappings={@DataResultMapping(dataResult=AbstractActionData.RESULT_SUCCESS, struts2Result="redirect", registerDelayedMessages=true)},
    	struts2ResultMappings={@Struts2ResultMapping(struts2Result="redirect")}
    )
    public String openMPsByCourses() throws Exception {
    	MarketsAdminService.changeMarketPlaceStatusByCourses(getEntityManager(), data, true);
        return null;
    }

    @WebMethodInfo(
    	dataResultMappings={@DataResultMapping(dataResult=AbstractActionData.RESULT_SUCCESS, struts2Result="redirect", registerDelayedMessages=true)},
    	struts2ResultMappings={@Struts2ResultMapping(struts2Result="redirect")}
    )
    public String closeMPsByCourses() throws Exception {
    	MarketsAdminService.changeMarketPlaceStatusByCourses(getEntityManager(), data, false);
        return null;
    }

    public String getParentRelativePath() {
		return data.getParentRelativePath();
	}
    public void setParentRelativePath(String parentRelativePath) {
		data.setParentRelativePath(parentRelativePath);
	}
    
    public String getSemesterID() {
		return data.getSemesterID();
	}
    public void setSemesterID(String semesterID) {
		data.setSemesterID(semesterID);
	}
    
    public Set<Long> getSelectedMarketPlaces() {
		return data.getSelectedMarketPlaces();
	}
    public void setSelectedMarketPlaces(Set<Long> selectedMarketPlaces) {
		data.setSelectedMarketPlaces(selectedMarketPlaces);
	}
}
