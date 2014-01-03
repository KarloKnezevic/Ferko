package hr.fer.zemris.jcms.web.actions2.poll;

import java.util.Map.Entry;

import hr.fer.zemris.jcms.service2.poll.PollTagService;
import hr.fer.zemris.jcms.web.actions.Ext2ActionSupport;
import hr.fer.zemris.jcms.web.actions.annotations.WebClass;
import hr.fer.zemris.jcms.web.actions.annotations.WebMethodInfo;
import hr.fer.zemris.jcms.web.data.poll.PollTagEditData;

@WebClass(dataClass=PollTagEditData.class)
public class PollTagEditAction extends Ext2ActionSupport<PollTagEditData> {

	private static final long serialVersionUID = 1L;

	@WebMethodInfo
	public String create() {
		PollTagService.createPollTag(getEntityManager(), getData());
		for(Entry<String, String> kv : getData().getErrors().entrySet()) {
			this.addFieldError(kv.getKey(), kv.getValue());
		}
		return null;
	}
	
	@WebMethodInfo
	public String edit() {
		PollTagService.updatePollTag(getEntityManager(), getData());
		for(Entry<String, String> kv : getData().getErrors().entrySet()) {
			this.addFieldError(kv.getKey(), kv.getValue()); // ovo s setFieldErrors zamijenit?
		}
		return null;
	}
	
	@WebMethodInfo
	public String delete() {
		PollTagService.deletePollTag(getEntityManager(), getData());
		return null;
	}
	
	public Long getId() {
		return getData().getPollTag().getId();
	}
	
	public void setId(Long id) {
		getData().getPollTag().setId(id);
	}
	
	public String getTitle() {
		return getData().getPollTag().getName();
	}
	
	public void setTitle(String title) {
		getData().getPollTag().setName(title);
	}
	
	public String getShortTitle() {
		return getData().getPollTag().getShortName();
	}
	
	public void setShortTitle(String shortTitle) {
		getData().getPollTag().setShortName(shortTitle);
	}
}
