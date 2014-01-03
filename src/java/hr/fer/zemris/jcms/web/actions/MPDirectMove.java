package hr.fer.zemris.jcms.web.actions;

import hr.fer.zemris.jcms.beans.ext.MPOfferBean;
import hr.fer.zemris.jcms.service.BasicBrowsing;
import hr.fer.zemris.jcms.web.actions.data.MPDirectMoveData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.MessageLoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Date;

@Deprecated
public class MPDirectMove extends ExtendedActionSupport {

	private static final long serialVersionUID = 2L;

	private	MPOfferBean bean = new MPOfferBean();
	private MPDirectMoveData data = null;

    public String execute() throws Exception {
    	return view();
    }
    
    public String view() throws Exception {
    	// Ako korisnik nije logiran - van!
    	String check = checkUser(null, true);
    	if(check != null) return check;
    	// Stvori objekt koji ce napuniti SVIM potrebnim podacima iz baze za ovu akciju
		data = new MPDirectMoveData(MessageLoggerFactory.createMessageLogger(this, true));
    	// Pozovi sloj usluge koji će napuniti navedenu strukturu...
		BasicBrowsing.getMPDirectMoveData(data, getCurrentUser().getUserID(), getBean(), "directMove");
		if(data.getResult().equals(AbstractActionData.RESULT_FATAL)) {
			return SHOW_FATAL_MESSAGE;
		}
		if(data.getMovedFromGroup()!=null) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			System.out.println("[BURZA]["+sdf.format(new Date())+"] Preselio korisnika "+data.getMovedUser().getJmbag()+" iz "+data.getMovedFromGroup().getName()+" u "+data.getMovedToGroup().getName());
		}
		data.getMessageLogger().registerAsDelayed();
        return SUCCESS;
    }

    public MPDirectMoveData getData() {
		return data;
	}
    public void setData(MPDirectMoveData data) {
		this.data = data;
	}

    public MPOfferBean getBean() {
		return bean;
	}
    public void setBean(MPOfferBean bean) {
		this.bean = bean;
	}
}
