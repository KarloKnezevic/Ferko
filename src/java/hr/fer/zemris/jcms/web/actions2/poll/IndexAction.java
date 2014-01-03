package hr.fer.zemris.jcms.web.actions2.poll;

import hr.fer.zemris.jcms.service2.poll.PollService;
import hr.fer.zemris.jcms.web.actions.Ext2ActionSupport;
import hr.fer.zemris.jcms.web.actions.annotations.WebClass;
import hr.fer.zemris.jcms.web.actions.annotations.WebMethodInfo;
import hr.fer.zemris.jcms.web.data.poll.PollIndexData;

@WebClass(dataClass=PollIndexData.class)
public class IndexAction extends Ext2ActionSupport<PollIndexData> {

	private static final long serialVersionUID = 1L;

    @WebMethodInfo
    public String execute() throws Exception {
    	PollService.getIndexData(getEntityManager(), data);
    	return null;
    }
}
