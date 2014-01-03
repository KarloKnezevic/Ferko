package hr.fer.zemris.jcms.web.actions2.course.grades;

import hr.fer.zemris.jcms.service2.course.grades.GradingPolicyService;
import hr.fer.zemris.jcms.web.actions.Ext2ActionSupport;
import hr.fer.zemris.jcms.web.actions.annotations.DataResultMapping;
import hr.fer.zemris.jcms.web.actions.annotations.Struts2ResultMapping;
import hr.fer.zemris.jcms.web.actions.annotations.TransactionalMethod;
import hr.fer.zemris.jcms.web.actions.annotations.WebClass;
import hr.fer.zemris.jcms.web.actions.annotations.WebMethodInfo;
import hr.fer.zemris.jcms.web.actions.data.GradingPolicyData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.navig.builders.BuilderDefault;
import hr.fer.zemris.jcms.web.navig.builders.course.grades.ShowGradesInternalBuilder;

@WebClass(dataClass=GradingPolicyData.class)
public class ShowGradingPolicy extends Ext2ActionSupport<GradingPolicyData> {

	private static final long serialVersionUID = 1L;

	/**
	 * Metoda dohvaća grading objekt za prikaz. Ako grading objekta nema,
	 * metoda ga ne stvara već vraća "locking-redirect" koji bi trebao biti
	 * mapiran na poziv iste akcije, samo metode {@link #executelo()} koja
	 * će prije svojeg izvođenja obaviti zaključavanje, pa pozvati istu metodu
	 * sloja usluge koja sada može na miru stvoriti potreban objekt bez bojazni
	 * da će nešto poći krivo.
	 * 
	 * @return <code>null</code>
	 **/
	@WebMethodInfo(
		dataResultMappings={
			@DataResultMapping(dataResult="locking-redirect",struts2Result="locking-redirect")
		},
		struts2ResultMappings={
			@Struts2ResultMapping(struts2Result="locking-redirect",navigBuilder=BuilderDefault.class)
		}
	)
	public String execute() {
		GradingPolicyService.show(getEntityManager(), data);
		return null;
	}
	
	/**
	 * Isto kao i {@link #execute()} samo što se prije poziva obavlja
	 * zaključavanje.
	 * 
	 * @return <code>null</code>
	 */
	@WebMethodInfo(lockPath="ml\\ci${courseInstanceID}\\a")
	public String executelo() {
		GradingPolicyService.show(getEntityManager(), data);
		return null;
	}

	@WebMethodInfo(lockPath="ml\\ci${courseInstanceID}\\a",
		dataResultMappings={
			@DataResultMapping(dataResult=AbstractActionData.RESULT_SUCCESS,struts2Result="stream",registerDelayedMessages=false),
			@DataResultMapping(dataResult="redirect-show",struts2Result="redirect-show",registerDelayedMessages=true)},
		struts2ResultMappings={
			@Struts2ResultMapping(struts2Result="stream",navigBuilder=BuilderDefault.class,transactionalMethod=@TransactionalMethod(closeImmediately=true)),
			@Struts2ResultMapping(struts2Result="redirect-show",navigBuilder=BuilderDefault.class)}
	)
	public String exportISVUGrades() {
		GradingPolicyService.exportISVUXML(getEntityManager(), data);
		return null;
	}
	
	@WebMethodInfo(lockPath="ml\\ci${courseInstanceID}\\a",
		dataResultMappings={
			@DataResultMapping(dataResult=AbstractActionData.RESULT_SUCCESS,struts2Result="redirect-show",registerDelayedMessages=true)
		},
		struts2ResultMappings={
			@Struts2ResultMapping(struts2Result="redirect-show",navigBuilder=BuilderDefault.class)
		}
	)
	public String update() {
		GradingPolicyService.update(getEntityManager(), data);
		return null;
	}

	@WebMethodInfo(lockPath="ml\\ci${courseInstanceID}\\a",
		dataResultMappings={
			@DataResultMapping(dataResult=AbstractActionData.RESULT_SUCCESS,struts2Result="redirect-show",registerDelayedMessages=true)
		},
		struts2ResultMappings={
			@Struts2ResultMapping(struts2Result="redirect-show",navigBuilder=BuilderDefault.class)
		}
	)
	public String runGrading() {
		GradingPolicyService.runGrading(getEntityManager(), data);
		return null;
	}
	
	@WebMethodInfo(
			dataResultMappings={
				@DataResultMapping(dataResult=AbstractActionData.RESULT_SUCCESS,struts2Result="show-grades",registerDelayedMessages=false),
				@DataResultMapping(dataResult=AbstractActionData.RESULT_NONFATAL_ERROR,struts2Result="redirect-show",registerDelayedMessages=true)
			},
			struts2ResultMappings={
				@Struts2ResultMapping(struts2Result="show-grades",navigBuilder=ShowGradesInternalBuilder.class),
				@Struts2ResultMapping(struts2Result="redirect-show",navigBuilder=BuilderDefault.class)
			}
		)
	public String showGrades() {
		GradingPolicyService.showGrades(getEntityManager(), data);
		return null;
	}
	
	public String getCourseInstanceID() {
		return data.getCourseInstanceID();
	}

	public void setCourseInstanceID(String courseInstanceID) {
		data.setCourseInstanceID(courseInstanceID);
	}

}
