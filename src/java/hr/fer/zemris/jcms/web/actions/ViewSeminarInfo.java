package hr.fer.zemris.jcms.web.actions;

import hr.fer.zemris.jcms.service.BasicBrowsing;
import hr.fer.zemris.jcms.web.actions.data.ViewSeminarInfoData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;

import com.opensymphony.xwork2.Preparable;

@Deprecated
public class ViewSeminarInfo extends ExtendedActionSupport implements Preparable {

	private static final long serialVersionUID = 2L;

	private ViewSeminarInfoData data = null;
	private Long id;
	
	@Override
	public void prepare() throws Exception {
		data = new ViewSeminarInfoData(MessageLoggerFactory.createMessageLogger(this));
	}

    public String execute() throws Exception {
    	if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
		BasicBrowsing.getViewSeminarInfoData(data, getCurrentUser().getUserID(), getId());
		if(data.getResult().equals(AbstractActionData.RESULT_SUCCESS)) {
			return SUCCESS;
		}
		return SHOW_FATAL_MESSAGE;
    }

    public ViewSeminarInfoData getData() {
		return data;
	}
    
    public void setData(ViewSeminarInfoData data) {
		this.data = data;
	}
    
    public Long getId() {
		return id;
	}
    public void setId(Long id) {
		this.id = id;
	}
}
