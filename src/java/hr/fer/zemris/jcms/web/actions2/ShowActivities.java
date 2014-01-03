package hr.fer.zemris.jcms.web.actions2;

import java.io.IOException;

import hr.fer.zemris.jcms.parsers.json.JSONException;
import hr.fer.zemris.jcms.service2.ActivityService;
import hr.fer.zemris.jcms.web.actions.Ext2ActionSupport;
import hr.fer.zemris.jcms.web.actions.annotations.WebClass;
import hr.fer.zemris.jcms.web.actions.annotations.WebMethodInfo;
import hr.fer.zemris.jcms.web.actions.data.ShowActivitiesData;
import hr.fer.zemris.jcms.web.navig.builders.MainBuilderPart;

/**
 * Prikaz svih aktivnosti korisnika unutar aktualnog semestra.
 *  
 * @author marcupic
 *
 */
@WebClass(dataClass=ShowActivitiesData.class, defaultNavigBuilder=MainBuilderPart.class)
public class ShowActivities extends Ext2ActionSupport<ShowActivitiesData> {

	private static final long serialVersionUID = 1L;

	@WebMethodInfo
	public String execute() throws IOException, JSONException {
		ActivityService.fetchForCurrentSemestar(getEntityManager(), getData());
		return null;
	}
}
