package hr.fer.zemris.jcms.web.actions2.course.parameters;

import hr.fer.zemris.jcms.service2.course.parameters.CourseParametersService;


import hr.fer.zemris.jcms.web.actions.Ext2ActionSupport;
import hr.fer.zemris.jcms.web.actions.annotations.DataResultMapping;
import hr.fer.zemris.jcms.web.actions.annotations.Struts2ResultMapping;
import hr.fer.zemris.jcms.web.actions.annotations.WebClass;
import hr.fer.zemris.jcms.web.actions.annotations.WebMethodInfo;
import hr.fer.zemris.jcms.web.actions.data.CourseParameters2Data;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.navig.DefaultNavigationBuilder;
import hr.fer.zemris.jcms.web.navig.builders.course.parameters.CourseParametersListBuilder;

@WebClass(dataClass=CourseParameters2Data.class,defaultNavigBuilder=CourseParametersListBuilder.class,defaultNavigBuilderIsRoot=false,additionalMenuItems={"m2","Navigation.courseParameters2"})
public class CourseParameters2 extends Ext2ActionSupport<CourseParameters2Data> {

	private static final long serialVersionUID = 2L;

	@WebMethodInfo
    public String execute() throws Exception {
		CourseParametersService.showCourseParameters2(getEntityManager(), data);
        return null;
    }

	@WebMethodInfo(
		dataResultMappings={@DataResultMapping(dataResult=AbstractActionData.RESULT_SUCCESS, struts2Result="redirect", registerDelayedMessages=true)},
		struts2ResultMappings={@Struts2ResultMapping(struts2Result="redirect", navigBuilder=DefaultNavigationBuilder.class)}
	)
    public String update() throws Exception {
		CourseParametersService.updateCourseParameters2(getEntityManager(), data);
        return null;
    }

    /**
     * Geter identifikatora kolegija. Uočimo da je to zapravo delegat.
     * 
     * @return identifikator kolegija
     */
    public String getCourseInstanceID() {
		return data.getCourseInstanceID();
	}
    
    /**
     * Seter identifikatora kolegija. Uočimo da je to zapravo delegat.
     * 
     * @param courseInstanceID identifikator kolegija
     */
    public void setCourseInstanceID(String courseInstanceID) {
		data.setCourseInstanceID(courseInstanceID);
	}

    /**
     * Getter za wikiEnabled. Ako je wikiEnabled=<code>true</code>, link na wiki prikazat će se
     * na stranici kolegija. Ako je <code>false</code>, neće biti linka. Uočimo da je to zapravo delegat.
     * @return <code>true</code> ako je prikaz wikija omogućen; inače <code>false</code>.
     */
    public boolean isWikiEnabled() {
		return data.isWikiEnabled();
	}
    /**
     * Setter za wikiEnabled.
     * @param wikiEnabled
     */
    public void setWikiEnabled(boolean wikiEnabled) {
		data.setWikiEnabled(wikiEnabled);
	}

}
