package hr.fer.zemris.jcms.service2.poll;


import hr.fer.zemris.jcms.dao.DAOHelperFactory;
import hr.fer.zemris.jcms.dao.PollDAO;
import hr.fer.zemris.jcms.model.poll.PollTag;
import hr.fer.zemris.jcms.security.JCMSSecurityManagerFactory;
import hr.fer.zemris.jcms.web.data.poll.PollTagEditData;
import hr.fer.zemris.jcms.web.data.poll.PollTagIndexData;

import javax.persistence.EntityManager;

public class PollTagService {
	
	// umjesto isAdmin bi trebalo ici nesto vezano za PollTag, no ovo ce posluziti
	
	private static void validateData(PollTagEditData data) {
		String name = data.getPollTag().getName().trim();
		if(name.length() == 0) {
			data.getErrors().put("title", "Naziv mora sadržavati bar jedan znak.");
		}
		if(name.length() > 120) {
			data.getErrors().put("title", "Naziv može sadržavati najviše 120 znakova.");
		}
		String shortName = data.getPollTag().getShortName().trim();
		if(shortName.length() == 0) {
			data.getErrors().put("shortTitle", "Kratki naziv mora sadržavati bar jedan znak.");
		}
		if(shortName.length() > 16) {
			data.getErrors().put("shortTitle", "Naziv može sadržavati najviše 16 znakova.");
		}
		data.getPollTag().setName(name);
		data.getPollTag().setShortName(shortName);
	}

	public static void createPollTag(EntityManager em, PollTagEditData data) {
		if(!JCMSSecurityManagerFactory.getManager().isAdmin()) {
			data.setResult(PollTagEditData.RESULT_FATAL);
			return;
		}
		if(data.getPollTag().getName() == null && data.getPollTag().getShortName() == null) {
			data.setResult(PollTagEditData.RESULT_INPUT);
			return;
		}
		validateData(data);
		if(!data.getErrors().isEmpty()) {
			data.setResult(PollTagEditData.RESULT_INPUT);
			return;
		}
		PollDAO pollDAO = DAOHelperFactory.getDAOHelper().getPollDAO();
		pollDAO.save(em, data.getPollTag());
		data.setResult(PollTagEditData.RESULT_SUCCESS);
	}
	
	public static void updatePollTag(EntityManager em, PollTagEditData data) {
		if(!JCMSSecurityManagerFactory.getManager().isAdmin()) {
			data.setResult(PollTagEditData.RESULT_FATAL);
			return;
		}
		if(data.getPollTag().getName() == null && data.getPollTag().getShortName() == null) {
			PollTag pollTag = DAOHelperFactory.getDAOHelper().getPollDAO().getPollTag(em, data.getPollTag().getId());
			data.setPollTag(pollTag);
			data.setResult(PollTagEditData.RESULT_INPUT);
			return;
		}
		validateData(data);
		if(!data.getErrors().isEmpty()) {
			data.setResult(PollTagEditData.RESULT_INPUT);
			return;
		}
		if(data.getPollTag().getId()==null) {
			data.setResult(PollTagEditData.RESULT_FATAL);
			return;
		}
		PollDAO pollDAO = DAOHelperFactory.getDAOHelper().getPollDAO();
		pollDAO.save(em, data.getPollTag());
		data.setResult(PollTagEditData.RESULT_SUCCESS);
	}
	
	public static void deletePollTag(EntityManager em, PollTagEditData data) {
		if(!JCMSSecurityManagerFactory.getManager().isAdmin()) {
			data.setResult(PollTagEditData.RESULT_FATAL);
			return;
		}
		PollDAO pollDAO = DAOHelperFactory.getDAOHelper().getPollDAO();
		PollTag pollTag = pollDAO.getPollTag(em, data.getPollTag().getId());
		pollDAO.remove(em, pollTag);
		data.setResult(PollTagEditData.RESULT_SUCCESS);
	}
	
	public static void getIndexData(EntityManager em, PollTagIndexData data) {
		if(!JCMSSecurityManagerFactory.getManager().isAdmin()) {
			data.setResult(PollTagEditData.RESULT_FATAL);
			return;
		}
		PollDAO pollDAO = DAOHelperFactory.getDAOHelper().getPollDAO();
		data.setPollTags(pollDAO.getPollTags(em));
		data.setResult(PollTagIndexData.RESULT_SUCCESS);
	}
}
