package hr.fer.zemris.jcms.service2.course;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import hr.fer.zemris.jcms.dao.DAOHelper;
import hr.fer.zemris.jcms.dao.DAOHelperFactory;
import hr.fer.zemris.jcms.model.GroupOwner;
import hr.fer.zemris.jcms.model.SeminarInfo;
import hr.fer.zemris.jcms.model.UserGroup;
import hr.fer.zemris.jcms.security.JCMSSecurityManagerFactory;
import hr.fer.zemris.jcms.web.actions.data.ViewSeminarInfoData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.util.StringUtil;

import javax.persistence.EntityManager;

public class Seminars {

	/**
	 * Metoda dohvaća podatke potrebe za prikaz stranice za prezentaciju seminara (podatci o samom seminaru
	 * te podatci o ostalim studentima u grupi i njihovim seminarima).
	 * 
	 * @param em
	 * @param data
	 */
	public static void fetchSeminarInfoData(EntityManager em, ViewSeminarInfoData data) {
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		Long seminarInfoID = data.getId();
		JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
		SeminarInfo si = seminarInfoID==null ? null : dh.getSeminarDAO().getSeminarInfo(em, seminarInfoID);
		if(si==null) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		boolean canProcceed = JCMSSecurityManagerFactory.getManager().canPerformSystemAdministration();
		if(!canProcceed) {
			canProcceed = si.getStudent().equals(data.getCurrentUser());
		}
		if(!canProcceed) {
			GroupOwner go = dh.getGroupDAO().getGroupOwner(em, si.getGroup(), data.getCurrentUser());
			canProcceed = go!=null;
		}
		if(!canProcceed) {
			for(UserGroup ug : si.getGroup().getUsers()) {
				if(ug.getUser().equals(data.getCurrentUser())) {
					canProcceed = true;
					break;
				}
			}
		}
		if(!canProcceed) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		List<SeminarInfo> seminars = dh.getSeminarDAO().findSeminarInfosFor(em, si.getGroup());
		Collections.sort(seminars, new Comparator<SeminarInfo>() {
			@Override
			public int compare(SeminarInfo o1, SeminarInfo o2) {
				int r = StringUtil.HR_COLLATOR.compare(o1.getTitle(), o2.getTitle());
				if(r!=0) return r;
				return StringUtil.USER_COMPARATOR.compare(o1.getStudent(), o2.getStudent());
			}
		});
		data.setSelectedSeminar(si);
		data.setAllSeminars(seminars);
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}
	
}
