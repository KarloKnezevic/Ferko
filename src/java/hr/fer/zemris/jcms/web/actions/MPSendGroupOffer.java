package hr.fer.zemris.jcms.web.actions;

import hr.fer.zemris.jcms.beans.ext.MPOfferBean;
import hr.fer.zemris.jcms.service.BasicBrowsing;
import hr.fer.zemris.jcms.web.actions.data.MPSendGroupOfferData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;

@Deprecated
public class MPSendGroupOffer extends ExtendedActionSupport {

	private static final long serialVersionUID = 2L;

	private	MPOfferBean bean = new MPOfferBean();
	private MPSendGroupOfferData data = null;

    public String execute() throws Exception {
    	return view();
    }
    
    public String view() throws Exception {
    	// Ako korisnik nije logiran - van!
    	String check = checkUser(null, true);
    	if(check != null) return check;
    	// Stvori objekt koji ce napuniti SVIM potrebnim podacima iz baze za ovu akciju
		data = new MPSendGroupOfferData(MessageLoggerFactory.createMessageLogger(this, true));
    	// Pozovi sloj usluge koji će napuniti navedenu strukturu...
		BasicBrowsing.getMPSendGroupOfferData(data, getCurrentUser().getUserID(), getBean());
		if(data.getResult().equals(AbstractActionData.RESULT_FATAL)) {
			return SHOW_FATAL_MESSAGE;
		}
		data.getMessageLogger().registerAsDelayed();
        return SUCCESS;
    }

    public MPSendGroupOfferData getData() {
		return data;
	}
    public void setData(MPSendGroupOfferData data) {
		this.data = data;
	}

    public MPOfferBean getBean() {
		return bean;
	}
    public void setBean(MPOfferBean bean) {
		this.bean = bean;
	}
}
