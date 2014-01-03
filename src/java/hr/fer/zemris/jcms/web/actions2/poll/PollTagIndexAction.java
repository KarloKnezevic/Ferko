package hr.fer.zemris.jcms.web.actions2.poll;

import hr.fer.zemris.jcms.service2.poll.PollTagService;
import hr.fer.zemris.jcms.web.actions.Ext2ActionSupport;
import hr.fer.zemris.jcms.web.actions.annotations.WebClass;
import hr.fer.zemris.jcms.web.actions.annotations.WebMethodInfo;
import hr.fer.zemris.jcms.web.data.poll.PollTagIndexData;

@WebClass(dataClass=PollTagIndexData.class)
public class PollTagIndexAction extends Ext2ActionSupport<PollTagIndexData> {

	private static final long serialVersionUID = 1L;

	@WebMethodInfo
	public String execute() {
		PollTagService.getIndexData(getEntityManager(), getData());
		return null;
	}
}
