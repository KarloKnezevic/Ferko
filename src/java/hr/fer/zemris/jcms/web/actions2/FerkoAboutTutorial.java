package hr.fer.zemris.jcms.web.actions2;

import hr.fer.zemris.jcms.web.actions.Ext2ActionSupport;
import hr.fer.zemris.jcms.web.actions.annotations.WebClass;
import hr.fer.zemris.jcms.web.actions.annotations.WebMethodInfo;
import hr.fer.zemris.jcms.web.actions.data.FerkoAboutTutorialData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.navig.builders.MainBuilder;

@WebClass(dataClass=FerkoAboutTutorialData.class, defaultNavigBuilder=MainBuilder.class)
public class FerkoAboutTutorial extends Ext2ActionSupport<FerkoAboutTutorialData> {

	private static final long serialVersionUID = 1L;

	@WebMethodInfo(loginCheck = false)
	public String execute() throws Exception {
		data.setResult(AbstractActionData.RESULT_SUCCESS);
		return null;
	}
	
	@WebMethodInfo()
	public String showTutorials() throws Exception {
		data.setResult(AbstractActionData.RESULT_SUCCESS);
		return null;
	}
	
	@WebMethodInfo(loginCheck = false)
	public String loginProblems() throws Exception {
		data.setResult(AbstractActionData.RESULT_SUCCESS);
		return null;
	}
	
}
