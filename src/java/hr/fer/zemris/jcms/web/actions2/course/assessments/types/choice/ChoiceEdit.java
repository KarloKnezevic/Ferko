package hr.fer.zemris.jcms.web.actions2.course.assessments.types.choice;

import hr.fer.zemris.jcms.model.AssessmentConfChoice;
import hr.fer.zemris.jcms.service2.course.assessments.types.choice.ChoiceEditingService;
import hr.fer.zemris.jcms.web.actions.Ext2ActionSupport;
import hr.fer.zemris.jcms.web.actions.annotations.DataResultMapping;
import hr.fer.zemris.jcms.web.actions.annotations.Struts2ResultMapping;
import hr.fer.zemris.jcms.web.actions.annotations.WebClass;
import hr.fer.zemris.jcms.web.actions.annotations.WebMethodInfo;
import hr.fer.zemris.jcms.web.actions.data.AdminSetDetailedChoiceConfData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.navig.DefaultNavigationBuilder;

/**
 * Akcija za postavljanje opširnih podataka {@link AssessmentConfChoice} provjere.
 * 
 * @author Ivan Krišto
 */
@WebClass(dataClass=AdminSetDetailedChoiceConfData.class)
public class ChoiceEdit extends Ext2ActionSupport<AdminSetDetailedChoiceConfData> {
	
	private static final long serialVersionUID = 2L;

	@WebMethodInfo
	public String show() throws Exception {

		ChoiceEditingService.fetchChoiceEditingData(getEntityManager(), data);
		return null;
		
	}
	
	@WebMethodInfo(
		dataResultMappings={@DataResultMapping(dataResult=AbstractActionData.RESULT_SUCCESS,struts2Result="redirectBack",registerDelayedMessages=true)},
		struts2ResultMappings={@Struts2ResultMapping(struts2Result="redirectBack",navigBuilder=DefaultNavigationBuilder.class)}
	)
	public String setBasicProperties() {
		
		ChoiceEditingService.setBasicProperties(getEntityManager(), data);
		return null;
		
	}
	
	@WebMethodInfo(
		dataResultMappings={@DataResultMapping(dataResult=AbstractActionData.RESULT_SUCCESS,struts2Result="redirectBack",registerDelayedMessages=true)},
		struts2ResultMappings={@Struts2ResultMapping(struts2Result="redirectBack",navigBuilder=DefaultNavigationBuilder.class)}
	)
	public String setDetailedScore() {
		
		ChoiceEditingService.setDetailedScore(getEntityManager(), data);
		return null;
		
	}

	@WebMethodInfo(
			dataResultMappings={@DataResultMapping(dataResult=AbstractActionData.RESULT_SUCCESS,struts2Result="redirectBack",registerDelayedMessages=true)},
			struts2ResultMappings={@Struts2ResultMapping(struts2Result="redirectBack",navigBuilder=DefaultNavigationBuilder.class)}
		)
	public String uploadDetailedScore() {
		
		ChoiceEditingService.uploadDetailedScore(getEntityManager(), data);
		return null;

	}

	@WebMethodInfo(
		dataResultMappings={@DataResultMapping(dataResult=AbstractActionData.RESULT_SUCCESS,struts2Result="redirectBack",registerDelayedMessages=true)},
		struts2ResultMappings={@Struts2ResultMapping(struts2Result="redirectBack",navigBuilder=DefaultNavigationBuilder.class)}
	)
	public String setCorrectAnswers() {
		
		ChoiceEditingService.setCorrectAnswers(getEntityManager(), data);
		return null;
		
	}

	@WebMethodInfo(
		dataResultMappings={@DataResultMapping(dataResult=AbstractActionData.RESULT_SUCCESS,struts2Result="redirectBack",registerDelayedMessages=true)},
		struts2ResultMappings={@Struts2ResultMapping(struts2Result="redirectBack",navigBuilder=DefaultNavigationBuilder.class)}
	)
	public String uploadCorrectAnswers() {
		
		ChoiceEditingService.uploadCorrectAnswers(getEntityManager(), data);
		return null;
		
	}

	@WebMethodInfo(
		dataResultMappings={@DataResultMapping(dataResult=AbstractActionData.RESULT_SUCCESS,struts2Result="redirectBack",registerDelayedMessages=true)},
		struts2ResultMappings={@Struts2ResultMapping(struts2Result="redirectBack",navigBuilder=DefaultNavigationBuilder.class)}
	)
	public String setGroupLabels() {
		
		ChoiceEditingService.setGroupLabels(getEntityManager(), data);
		return null;
		
	}

	@WebMethodInfo(
		dataResultMappings={@DataResultMapping(dataResult=AbstractActionData.RESULT_SUCCESS,struts2Result="redirectBack",registerDelayedMessages=true)},
		struts2ResultMappings={@Struts2ResultMapping(struts2Result="redirectBack",navigBuilder=DefaultNavigationBuilder.class)}
	)
	public String uploadGroupLabels() {
		
		ChoiceEditingService.uploadGroupLabels(getEntityManager(), data);
		return null;
		
	}

	@WebMethodInfo(
		dataResultMappings={@DataResultMapping(dataResult=AbstractActionData.RESULT_SUCCESS,struts2Result="redirectBack",registerDelayedMessages=true)},
		struts2ResultMappings={@Struts2ResultMapping(struts2Result="redirectBack",navigBuilder=DefaultNavigationBuilder.class)}
	)
	public String setProblemLabels() {
		
		ChoiceEditingService.setProblemLabels(getEntityManager(), data);
		return null;
		
	}

	@WebMethodInfo(
		dataResultMappings={@DataResultMapping(dataResult=AbstractActionData.RESULT_SUCCESS,struts2Result="redirectBack",registerDelayedMessages=true)},
		struts2ResultMappings={@Struts2ResultMapping(struts2Result="redirectBack",navigBuilder=DefaultNavigationBuilder.class)}
	)
	public String uploadProblemLabels() {
		
		ChoiceEditingService.uploadProblemLabels(getEntityManager(), data);
		return null;
		
	}

	@WebMethodInfo(
		dataResultMappings={@DataResultMapping(dataResult=AbstractActionData.RESULT_SUCCESS,struts2Result="redirectBack",registerDelayedMessages=true)},
		struts2ResultMappings={@Struts2ResultMapping(struts2Result="redirectBack",navigBuilder=DefaultNavigationBuilder.class)}
	)
	public String setProblemMapping() {
		
		ChoiceEditingService.setProblemMapping(getEntityManager(), data);
		return null;
		
	}

	@WebMethodInfo(
		dataResultMappings={@DataResultMapping(dataResult=AbstractActionData.RESULT_SUCCESS,struts2Result="redirectBack",registerDelayedMessages=true)},
		struts2ResultMappings={@Struts2ResultMapping(struts2Result="redirectBack",navigBuilder=DefaultNavigationBuilder.class)}
	)
	public String uploadProblemMapping() {
		
		ChoiceEditingService.uploadProblemMapping(getEntityManager(), data);
		return null;
		
	}

	@WebMethodInfo(
		dataResultMappings={@DataResultMapping(dataResult=AbstractActionData.RESULT_SUCCESS,struts2Result="redirectBack",registerDelayedMessages=true)},
		struts2ResultMappings={@Struts2ResultMapping(struts2Result="redirectBack",navigBuilder=DefaultNavigationBuilder.class)}
	)
	public String setProblemManipulators() {
		
		ChoiceEditingService.setProblemManipulators(getEntityManager(), data);
		return null;
		
	}

	@WebMethodInfo(
		dataResultMappings={@DataResultMapping(dataResult=AbstractActionData.RESULT_SUCCESS,struts2Result="redirectBack",registerDelayedMessages=true)},
		struts2ResultMappings={@Struts2ResultMapping(struts2Result="redirectBack",navigBuilder=DefaultNavigationBuilder.class)}
	)
	public String uploadProblemManipulators() {
		
		ChoiceEditingService.uploadProblemManipulators(getEntityManager(), data);
		return null;
		
	}

	@WebMethodInfo
	public String execute() throws Exception {
		return show();
	}

	public String getAssessmentID() {
		return data.getAssessmentID();
	}

	public void setAssessmentID(String assessmentID) {
		data.setAssessmentID(assessmentID);
	}

	public String getSelectedView() {
		return data.getSelectedView();
	}
	public void setSelectedView(String selectedView) {
		data.setSelectedView(selectedView);
	}

}
