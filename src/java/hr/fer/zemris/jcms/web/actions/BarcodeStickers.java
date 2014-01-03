package hr.fer.zemris.jcms.web.actions;

import hr.fer.zemris.jcms.beans.BarcodeStickersBean;
import hr.fer.zemris.jcms.service.BarCodePDFCreator;
import hr.fer.zemris.jcms.web.actions.data.BarcodeStickersData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;
import hr.fer.zemris.util.DeleteOnCloseFileInputStream;

import com.opensymphony.xwork2.Preparable;

public class BarcodeStickers extends ExtendedActionSupport implements Preparable {

	private static final long serialVersionUID = 2L;

	private BarcodeStickersData data = null;
	private BarcodeStickersBean bean = null; 
	private String courseInstanceID;
	private DeleteOnCloseFileInputStream stream;

	@Override
	public void prepare() throws Exception {
		data = new BarcodeStickersData(MessageLoggerFactory.createMessageLogger(this));
		bean = new BarcodeStickersBean();
	}

    public String input() throws Exception {
    	if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
		BarCodePDFCreator.getBarcodeStickersData(data, bean, getCurrentUser().getUserID(), getCourseInstanceID(), null, "input");
		if(data.getResult().equals(AbstractActionData.RESULT_INPUT)) return INPUT;
		return SHOW_FATAL_MESSAGE;
    }

    public String create() throws Exception {
    	if(!hasCurrentUser()) {
    		// Ako nije zadan ID, pretvarajmo se da je to pokušaj varanja...
    		return NO_PERMISSION;
    	}
		DeleteOnCloseFileInputStream[] reference = new DeleteOnCloseFileInputStream[] {null};
		BarCodePDFCreator.getBarcodeStickersData(data, bean, getCurrentUser().getUserID(), getCourseInstanceID(), reference, "create");
		if(data.getResult().equals(AbstractActionData.RESULT_INPUT)) return INPUT;
		if(!data.getResult().equals(AbstractActionData.RESULT_SUCCESS)) return SHOW_FATAL_MESSAGE;
		stream = reference[0];
		return "stream";
    }

    public String execute() throws Exception {
    	return input();
    }

    public String getCourseInstanceID() {
		return courseInstanceID;
	}
    public void setCourseInstanceID(String courseInstanceID) {
		this.courseInstanceID = courseInstanceID;
	}
    
    public BarcodeStickersData getData() {
		return data;
	}
    public void setData(BarcodeStickersData data) {
		this.data = data;
	}
    
    public BarcodeStickersBean getBean() {
		return bean;
	}
    public void setBean(BarcodeStickersBean bean) {
		this.bean = bean;
	}
    
    public DeleteOnCloseFileInputStream getStream() {
		return stream;
	}
}
