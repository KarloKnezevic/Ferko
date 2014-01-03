package hr.fer.zemris.jcms.web.actions2.poll;

import java.util.Map.Entry;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.interceptor.ServletRequestAware;

import hr.fer.zemris.jcms.dao.CourseInstanceDAO;
import hr.fer.zemris.jcms.dao.DAOHelperFactory;
import hr.fer.zemris.jcms.service2.poll.PollService;
import hr.fer.zemris.jcms.web.actions.Ext2ActionSupport;
import hr.fer.zemris.jcms.web.actions.annotations.WebClass;
import hr.fer.zemris.jcms.web.actions.annotations.WebMethodInfo;
import hr.fer.zemris.jcms.web.data.poll.PollEditData;

@WebClass(dataClass=PollEditData.class)
public class EditAction extends Ext2ActionSupport<PollEditData> implements ServletRequestAware {

	private static final long serialVersionUID = 1L;
	private HttpServletRequest request;
	
	public String execute() {
		return createPoll();
	}
	
	@WebMethodInfo
	public String createPoll() {
		PollService.createPoll(getEntityManager(), request, getData());
		for(Entry<String, String> kv : getData().getErrors().entrySet()) {
			this.addFieldError(kv.getKey(), kv.getValue());
		}
		return null;
	}
	
	@WebMethodInfo
	public String editPoll() {
		PollService.editPoll(getEntityManager(), request, getData());
		for(Entry<String, String> kv : getData().getErrors().entrySet()) {
			this.addFieldError(kv.getKey(), kv.getValue());
		}
		return null;
	}
	
	@WebMethodInfo
	public String prolong() {
		PollService.prolongPoll(getEntityManager(), getData());
		for(Entry<String, String> kv : getData().getErrors().entrySet()) {
			this.addFieldError(kv.getKey(), kv.getValue());
		}
		return null;
	}
	
	@WebMethodInfo
	public String deletePoll() {
		PollService.removePoll(getEntityManager(), getData());
		return null;
	}
	
	@WebMethodInfo
	public String addUsers() {
		if(this.request.getMethod().equals("POST")) {
			PollService.addUsers(getEntityManager(), request);
			getData().setResult(PollEditData.RESULT_SUCCESS);
		} else {
			getData().setResult(PollEditData.RESULT_INPUT);
		}
		return null;
	}
	
	@WebMethodInfo
	public String addGroups() {
		if(this.request.getMethod().equals("POST")) {
			PollService.addGroups(getEntityManager(), request);
			getData().setResult(PollEditData.RESULT_SUCCESS);
		} else {
			getData().setResult(PollEditData.RESULT_INPUT);
		}		
		return null;
	}
	
	public void setId(Long id) {
		getData().setId(id);
	}
	
	public Long getId() {
		return getData().getId();
	}

	@Override
	public void setServletRequest(HttpServletRequest req) {
		request=req;
	}
	
	public void setCourseInstanceID(String id) {
		CourseInstanceDAO ciDAO = DAOHelperFactory.getDAOHelper().getCourseInstanceDAO();
		getData().setCourseInstance(ciDAO.get(getEntityManager(), id));
	}
	
	public String getCourseInstanceID() {
		if(getData().getCourseInstance() != null) return getData().getCourseInstance().getId();
		return null;
	}
	
	public String getStartDate() {
		return getData().getStartDate();
	}

	public void setStartDate(String startDate) {
		getData().setStartDate(startDate);
	}

	public String getEndDate() {
		return getData().getEndDate();
	}

	public void setEndDate(String endDate) {
		getData().setEndDate(endDate);
	}

	public String getStartTime() {
		return getData().getStartTime();
	}

	public void setStartTime(String startTime) {
		getData().setStartTime(startTime);
	}

	public String getEndTime() {
		return getData().getEndTime();
	}

	public void setEndTime(String endTime) {
		getData().setEndTime(endTime);
	}

	public String getTitle() {
		return getData().getTitle();
	}

	public void setTitle(String title) {
		getData().setTitle(title);
	}

	public String getDescription() {
		return getData().getDescription();
	}

	public void setDescription(String description) {
		getData().setDescription(description);
	}
	
	public void setPollTagId(String id) {
		getData().setPollTagId(id);
	}
	
	public String getPollTagId() {
		return getData().getPollTagId();
	}

}
