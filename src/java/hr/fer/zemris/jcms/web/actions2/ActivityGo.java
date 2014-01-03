package hr.fer.zemris.jcms.web.actions2;

import java.io.IOException;

import hr.fer.zemris.jcms.service2.ActivityService;
import hr.fer.zemris.jcms.web.actions.Ext2ActionSupport;
import hr.fer.zemris.jcms.web.actions.annotations.DataResultMapping;
import hr.fer.zemris.jcms.web.actions.annotations.Struts2ResultMapping;
import hr.fer.zemris.jcms.web.actions.annotations.WebClass;
import hr.fer.zemris.jcms.web.actions.annotations.WebMethodInfo;
import hr.fer.zemris.jcms.web.actions.data.ActivityGoData;
import hr.fer.zemris.jcms.web.navig.builders.BuilderDefault;

/**
 * Ova akcija predstavlja "dispatcher" za aktivnosti. Kada se studentu prikaže popis aktivnosti,
 * generira se link koji kao argument ima identifikator aktivnosti. Ova se akcija poziva kada student
 * klikne na taj link, i trebala bi studenta odvesti na stranicu koja odgovara aktivnosti.
 *  
 * @author marcupic
 *
 */
@WebClass(dataClass=ActivityGoData.class, defaultNavigBuilder=BuilderDefault.class)
public class ActivityGo extends Ext2ActionSupport<ActivityGoData> {

	private static final long serialVersionUID = 1L;

	@WebMethodInfo(
		dataResultMappings={
			@DataResultMapping(dataResult="RES_MP_VIEW",struts2Result="RES_MP_VIEW",registerDelayedMessages=false),
			@DataResultMapping(dataResult="RES_ITEM_VIEW",struts2Result="RES_ITEM_VIEW",registerDelayedMessages=false),
			@DataResultMapping(dataResult="RES_COURSEINSTANCE",struts2Result="RES_COURSEINSTANCE",registerDelayedMessages=false),
			@DataResultMapping(dataResult="RES_A_SUMMARYVIEW",struts2Result="RES_A_SUMMARYVIEW",registerDelayedMessages=false),
			@DataResultMapping(dataResult="RES_STUDENT_APPLICATION_VIEW",struts2Result="RES_STUDENT_APPLICATION_VIEW",registerDelayedMessages=false),
			@DataResultMapping(dataResult="RES_ISSUE_VIEW",struts2Result="RES_ISSUE_VIEW",registerDelayedMessages=false)
		},
		struts2ResultMappings={
			@Struts2ResultMapping(struts2Result="RES_MP_VIEW", navigBuilder=BuilderDefault.class),
			@Struts2ResultMapping(struts2Result="RES_ITEM_VIEW", navigBuilder=BuilderDefault.class),
			@Struts2ResultMapping(struts2Result="RES_COURSEINSTANCE", navigBuilder=BuilderDefault.class),
			@Struts2ResultMapping(struts2Result="RES_A_SUMMARYVIEW", navigBuilder=BuilderDefault.class),
			@Struts2ResultMapping(struts2Result="RES_STUDENT_APPLICATION_VIEW", navigBuilder=BuilderDefault.class),
			@Struts2ResultMapping(struts2Result="RES_ISSUE_VIEW", navigBuilder=BuilderDefault.class)
		}
	)
	public String execute() throws IOException {
		ActivityService.dispatch(getEntityManager(), getData());
		return null;
	}
	
	public void setAid(Long aid) {
		data.setAid(aid);
	}

}
