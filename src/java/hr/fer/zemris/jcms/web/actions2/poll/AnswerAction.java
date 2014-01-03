package hr.fer.zemris.jcms.web.actions2.poll;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.interceptor.ServletRequestAware;

import hr.fer.zemris.jcms.service2.poll.PollService;
import hr.fer.zemris.jcms.web.actions.Ext2ActionSupport;
import hr.fer.zemris.jcms.web.actions.annotations.WebClass;
import hr.fer.zemris.jcms.web.actions.annotations.WebMethodInfo;
import hr.fer.zemris.jcms.web.data.poll.PollAnswerData;

@WebClass(dataClass=PollAnswerData.class)
public class AnswerAction extends Ext2ActionSupport<PollAnswerData> implements ServletRequestAware  {

	private static final long serialVersionUID = 1L;
	private HttpServletRequest request;
	
	@WebMethodInfo
	public String execute() {
		if(this.request.getMethod().equals("POST")) { 
			PollService.answerPoll(getEntityManager(), getData(), request);
		} else {
			PollService.getAnswerData(getEntityManager(), getData());
		}
		return null;
	}

	@Override
	public void setServletRequest(HttpServletRequest req) {
		request=req;
	}
	
	public void setId(Long id) {
		//getData().setId(id);
		getData().setPollUserId(id);
	}
	
	public Long getId() {
		return getData().getId();
	}
	
	public String getForm() {
		return data.getForm();
	}
	
	public void setPUID(String pollUserId) {
		getData().setPollUserId(Long.parseLong(pollUserId));
	}
	}
