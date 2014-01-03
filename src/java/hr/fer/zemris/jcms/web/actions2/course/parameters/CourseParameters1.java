package hr.fer.zemris.jcms.web.actions2.course.parameters;

import hr.fer.zemris.jcms.service2.course.parameters.CourseParametersService;

import hr.fer.zemris.jcms.web.actions.Ext2ActionSupport;
import hr.fer.zemris.jcms.web.actions.annotations.DataResultMapping;
import hr.fer.zemris.jcms.web.actions.annotations.Struts2ResultMapping;
import hr.fer.zemris.jcms.web.actions.annotations.TransactionalMethod;
import hr.fer.zemris.jcms.web.actions.annotations.WebClass;
import hr.fer.zemris.jcms.web.actions.annotations.WebMethodInfo;
import hr.fer.zemris.jcms.web.actions.data.CourseParameters1Data;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.navig.DefaultNavigationBuilder;
import hr.fer.zemris.jcms.web.navig.builders.BuilderDefault;
import hr.fer.zemris.jcms.web.navig.builders.course.parameters.CourseParametersListBuilder;

@WebClass(dataClass=CourseParameters1Data.class,defaultNavigBuilder=CourseParametersListBuilder.class,defaultNavigBuilderIsRoot=false,additionalMenuItems={"m2","Navigation.courseParameters1"})
public class CourseParameters1 extends Ext2ActionSupport<CourseParameters1Data> {

	private static final long serialVersionUID = 2L;

	@WebMethodInfo
    public String execute() throws Exception {
		CourseParametersService.showCourseParameters1(getEntityManager(), data);
        return null;
    }

	@WebMethodInfo(
		dataResultMappings={@DataResultMapping(dataResult=AbstractActionData.RESULT_SUCCESS, struts2Result="redirect", registerDelayedMessages=true)},
		struts2ResultMappings={@Struts2ResultMapping(struts2Result="redirect", navigBuilder=DefaultNavigationBuilder.class)}
	)
    public String update() throws Exception {
		CourseParametersService.updateCourseParameters1(getEntityManager(), data);
        return null;
    }

	@WebMethodInfo( 
		dataResultMappings={ 
			@DataResultMapping(dataResult=AbstractActionData.RESULT_SUCCESS,struts2Result="stream",registerDelayedMessages=false)}, 
		struts2ResultMappings={ 
			@Struts2ResultMapping(struts2Result="stream",navigBuilder=BuilderDefault.class,transactionalMethod=@TransactionalMethod(closeImmediately=true))}
	)
    public String exportRoomParameters() throws Exception {
		CourseParametersService.exportRoomParameters1(getEntityManager(), data);
        return null;
    }

	@WebMethodInfo( 
			dataResultMappings={ 
				@DataResultMapping(dataResult=AbstractActionData.RESULT_SUCCESS,struts2Result="stream",registerDelayedMessages=false)}, 
			struts2ResultMappings={ 
				@Struts2ResultMapping(struts2Result="stream",navigBuilder=BuilderDefault.class,transactionalMethod=@TransactionalMethod(closeImmediately=true))}
		)
	    public String exportExamDurations() throws Exception {
			CourseParametersService.exportRoomParameters1B(getEntityManager(), data);
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
     * Geter za odabrani semestar. Uočimo da je to zapravo delegat.
     * 
     * @return identifikator semestra
     */
	public String getYearSemesterID() {
		return data.getYearSemesterID();
	}
	
    /**
     * Seter identifikatora semestra. Uočimo da je to zapravo delegat.
     * 
     * @param yearSemesterID identifikator semestra
     */
	public void setYearSemesterID(String yearSemesterID) {
		data.setYearSemesterID(yearSemesterID);
	}
}
