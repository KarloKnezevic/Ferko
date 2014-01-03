package hr.fer.zemris.jcms.service2.course.market;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import hr.fer.zemris.jcms.JCMSLogger;
import hr.fer.zemris.jcms.JCMSSettings;
import hr.fer.zemris.jcms.activities.types.MarketActivity;
import hr.fer.zemris.jcms.beans.GroupBean;
import hr.fer.zemris.jcms.beans.MPRootInfoBean;
import hr.fer.zemris.jcms.beans.ext.ExchangeDescriptor;
import hr.fer.zemris.jcms.beans.ext.MPFormulaConstraints;
import hr.fer.zemris.jcms.beans.ext.MPFormulaContext;
import hr.fer.zemris.jcms.beans.ext.MPSecurityConstraints;
import hr.fer.zemris.jcms.beans.ext.MPUserGroupState;
import hr.fer.zemris.jcms.beans.ext.MPUserState;
import hr.fer.zemris.jcms.dao.DAOHelper;
import hr.fer.zemris.jcms.dao.DAOHelperFactory;
import hr.fer.zemris.jcms.model.CourseInstance;
import hr.fer.zemris.jcms.model.Group;
import hr.fer.zemris.jcms.model.GroupWideEvent;
import hr.fer.zemris.jcms.model.MPOffer;
import hr.fer.zemris.jcms.model.MarketPlace;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.model.UserGroup;
import hr.fer.zemris.jcms.security.JCMSSecurityManagerFactory;
import hr.fer.zemris.jcms.service.util.GroupUtil;
import hr.fer.zemris.jcms.service2.BasicServiceSupport;
import hr.fer.zemris.jcms.service2.course.CourseInstanceServiceSupport;
import hr.fer.zemris.jcms.web.actions.data.MPAcceptOfferData;
import hr.fer.zemris.jcms.web.actions.data.MPDeleteOfferData;
import hr.fer.zemris.jcms.web.actions.data.MPDirectMoveData;
import hr.fer.zemris.jcms.web.actions.data.MPGroupsAdminData;
import hr.fer.zemris.jcms.web.actions.data.MPGroupsListData;
import hr.fer.zemris.jcms.web.actions.data.MPSendDirectOfferData;
import hr.fer.zemris.jcms.web.actions.data.MPSendGroupOfferData;
import hr.fer.zemris.jcms.web.actions.data.MPViewData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.util.StringUtil;

import javax.persistence.EntityManager;

public class MarketBrowserService {

	public static void getMarketPlacesListForCourseInstance(EntityManager em, MPGroupsListData data) {
		
		// Dohvat kolegija
		if(!CourseInstanceServiceSupport.fillCourseInstance(em, data, data.getCourseInstanceID())) return;

		data.setStudent(JCMSSecurityManagerFactory.getManager().isStudentOnCourse(data.getCourseInstance()));
		List<MPRootInfoBean> list = JCMSSecurityManagerFactory.getManager().getMarketPlacesForUser(data.getCourseInstance());
		data.setMpRoots(list);
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}
	
	public static void viewMarketPlace(EntityManager em, MPViewData data) {
		
		// Dohvat kolegija
		if(!CourseInstanceServiceSupport.fillCourseInstance(em, data, data.getCourseInstanceID())) return;
		
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		Group parent = data.getParentID()!=null ? dh.getGroupDAO().get(em, data.getParentID()) : null;
		if(parent==null || !parent.getCompositeCourseID().equals(data.getCourseInstance().getId())) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		data.setParent(parent);
		MPWorkContext mpwc = new MPWorkContext(em, data.getCourseInstance(), parent, data.getCurrentUser());
		if(mpwc.userGroups.size()==0) {
			// Ups! Tog korisnika tamo nema!!! Je li on uopce na predavanjima?
			List<Group> lectureGroups = dh.getGroupDAO().findSubGroupsForUser(em, data.getCourseInstance().getId(), "0", data.getCurrentUser());
			if(lectureGroups.isEmpty()) {
				// Ovaj korisnik uopce nije na kolegiju! Van!
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
				data.setResult(AbstractActionData.RESULT_FATAL);
				return;
			} else {
				// Korisnik je na kolegiju, ali ne sudjeluje u ovoj burzi...
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.notInMarketPlace"));
				data.setResult(AbstractActionData.RESULT_FATAL);
				return;
			}
		}
		data.setAllGroups(mpwc.allGroups);
		data.setUserGroups(mpwc.userGroups);
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		data.setNow(sdf.format(mpwc.now));
		if(!mpwc.marketPlaceActive) {
			data.setActive(false);
			MPUserState state = new MPUserState();
			for(UserGroup ug : mpwc.userGroups) {
				state.getOrCreateForGroup(ug);
			}
			data.setUserState(state);
		} else {
			List<MPOffer> offers = dh.getMarketPlaceDAO().listOffersRegardingUser(em, parent.getMarketPlace(), data.getCurrentUser());
			MPUserState state = MPUserState.buildFrom(offers, data.getCurrentUser(), mpwc.now, mpwc.scons, mpwc.myUserGroupMap);
			for(UserGroup ug : mpwc.userGroups) {
				MPUserGroupState s = state.getOrCreateForGroup(ug);
				// Mogu li se samo premjestiti u drugu grupu?
				mpwc.prepareCheckMove(ug);
				for(Group g : mpwc.allGroups) {
					if(mpwc.canMoveStudentToGroup(ug, g)) {
						s.getAvailForMove().add(g);
					}
				}
				mpwc.calcSendDestinations(s);
			}
			data.setUserState(state);
			data.setActive(mpwc.marketPlaceActive);
		}
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}

	public static void sendGroupOffer(EntityManager em, MPSendGroupOfferData data) {
		
		// Dohvat kolegija
		if(!CourseInstanceServiceSupport.fillCourseInstance(em, data, data.getBean().getCourseInstanceID())) return;

		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		Group parent = data.getBean().getParentID()!=null ? dh.getGroupDAO().get(em, data.getBean().getParentID()) : null;
		if(parent==null || !parent.getCompositeCourseID().equals(data.getCourseInstance().getId()) || !parent.isManagedRoot()) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		data.setParent(parent);
		
		if(data.getLockPath()==null || data.getLockPath().size()!=4 || !data.getLockPath().getPart(1).equals("ci"+data.getCourseInstance().getId()) || !data.getLockPath().getPart(3).equals("g"+parent.getId())) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}

		Date validUntil = null;
		if(!StringUtil.isStringBlank(data.getBean().getValidUntil())) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			try {
				validUntil = sdf.parse(data.getBean().getValidUntil());
			} catch(ParseException ex) {
				data.getMessageLogger().addErrorMessage("Datum je pogrešnog formata.");
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return;
			}
		}
		if(validUntil!=null && new Date().after(validUntil)) {
			data.getMessageLogger().addErrorMessage("Ne može poslati ponudu koja je već u startu istekla.");
			data.setResult(AbstractActionData.RESULT_SUCCESS);
			return;
		}
		Group otherGroup = data.getBean().getGroupID()!=null ? dh.getGroupDAO().get(em, data.getBean().getGroupID()) : null;
		UserGroup myUserGroup = dh.getUserGroupDAO().get(em, data.getBean().getMyUserGroupID());
		if(myUserGroup==null  || !myUserGroup.getUser().equals(data.getCurrentUser())
				|| !myUserGroup.getGroup().getParent().equals(parent) || otherGroup==null 
				|| !otherGroup.getParent().equals(parent)) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		Group myGroup = myUserGroup.getGroup();
		if(!myUserGroup.getGroup().getId().equals(data.getBean().getMyGroupID())) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.staleData"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}

		MPWorkContext mpwc = new MPWorkContext(em, data.getCourseInstance(), parent, data.getCurrentUser());
		if(!mpwc.marketPlaceActive) {
			data.getMessageLogger().addErrorMessage("Burza je zatvorena.");
			data.setResult(AbstractActionData.RESULT_SUCCESS);
			return;
		} else {
			List<MPOffer> offers = dh.getMarketPlaceDAO().listOffersRegardingUser(em, parent.getMarketPlace(), data.getCurrentUser(), myGroup);
			MPUserState state = MPUserState.buildFrom(offers, data.getCurrentUser(), mpwc.now, mpwc.scons, mpwc.myUserGroupMap);
			MPUserGroupState s = state.getOrCreateForGroup(myUserGroup);
			mpwc.calcSendDestinations(s);
			// Kada mu smijem poslati grupnu ponudu? Ako je "s" aktivan i ako odredisna grupa nije blokirana:
			if(s.isActive() && !s.getBlockedGroups().contains(otherGroup)) {
				// Sada mogu poslati ponudu...
				MPOffer offer = new MPOffer();
				offer.setFromGroup(myGroup);
				offer.setFromUser(myUserGroup.getUser());
				offer.setFromTag(myUserGroup.getTag());
				offer.setMarketPlace(parent.getMarketPlace());
				offer.setNeedsAck(data.getBean().isRequireApr());
				offer.setToGroup(otherGroup);
				offer.setToUser(null);
				offer.setValidUntil(validUntil);
				if(!StringUtil.isStringBlank(data.getBean().getReason())) {
					if(data.getBean().getReason().length()>100) {
						offer.setReason(data.getBean().getReason().substring(0, 100));
					} else {
						offer.setReason(data.getBean().getReason());
					}
				}
				dh.getMarketPlaceDAO().save(em, offer);
				data.getMessageLogger().addInfoMessage("Ponuda je poslana.");
			} else {
				// Ne mogu poslati ponudu...
				data.getMessageLogger().addErrorMessage("Ponudu nije moguće poslati.");
			}
		}
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}

	public static void sendDirectOffer(EntityManager em, MPSendDirectOfferData data) {
		
		// Dohvat kolegija
		if(!CourseInstanceServiceSupport.fillCourseInstance(em, data, data.getBean().getCourseInstanceID())) return;
		
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		Group parent = data.getBean().getParentID()!=null ? dh.getGroupDAO().get(em, data.getBean().getParentID()) : null;
		if(parent==null || !parent.getCompositeCourseID().equals(data.getCourseInstance().getId()) || !parent.isManagedRoot() || StringUtil.isStringBlank(data.getBean().getToUsername())) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		data.setParent(parent);
		
		if(data.getLockPath()==null || data.getLockPath().size()!=4 || !data.getLockPath().getPart(1).equals("ci"+data.getCourseInstance().getId()) || !data.getLockPath().getPart(3).equals("g"+parent.getId())) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}

		Date validUntil = null;
		if(!StringUtil.isStringBlank(data.getBean().getValidUntil())) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			try {
				validUntil = sdf.parse(data.getBean().getValidUntil());
			} catch(ParseException ex) {
				data.getMessageLogger().addErrorMessage("Datum je pogrešnog formata.");
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return;
			}
		}
		if(validUntil!=null && new Date().after(validUntil)) {
			data.getMessageLogger().addErrorMessage("Ne može poslati ponudu koja je već u startu istekla.");
			data.setResult(AbstractActionData.RESULT_SUCCESS);
			return;
		}
		User otherUser = dh.getUserDAO().getUserByUsername(em, data.getBean().getToUsername());
		if(otherUser==null) {
			// Dajmo generalnu poruku, tako da se ne moze zakljuciti sto je tocno problem s korisnikom.
			data.getMessageLogger().addErrorMessage("Korisnik "+data.getBean().getToUsername()+" ne postoji ili nije na kolegiju u zadanoj grupi.");
			data.setResult(AbstractActionData.RESULT_SUCCESS);
			return;
		}
		Group otherGroup = data.getBean().getGroupID()!=null ? dh.getGroupDAO().get(em, data.getBean().getGroupID()) : null;
		UserGroup otherUserGroup = otherGroup==null ? null : dh.getUserGroupDAO().find(em, otherUser, otherGroup);
		if(otherUserGroup==null) {
			// Dajmo namjerno istu poruku kao i maloprije, tako da se ne moze zakljuciti sto je tocno problem s korisnikom.
			data.getMessageLogger().addErrorMessage("Korisnik "+data.getBean().getToUsername()+" ne postoji ili nije na kolegiju u zadanoj grupi.");
			data.setResult(AbstractActionData.RESULT_SUCCESS);
			return;
		}
		UserGroup myUserGroup = dh.getUserGroupDAO().get(em, data.getBean().getMyUserGroupID());
		if(myUserGroup==null  || !myUserGroup.getUser().equals(data.getCurrentUser())
				|| !myUserGroup.getGroup().getParent().equals(parent) || otherUserGroup==null || otherGroup==null 
				|| !otherGroup.getParent().equals(parent)) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		if(otherUser.equals(myUserGroup.getUser())) {
			data.getMessageLogger().addErrorMessage("Stvarno duhovito :-)");
			data.setResult(AbstractActionData.RESULT_SUCCESS);
			return;
		}
		if(otherGroup.equals(myUserGroup.getGroup())) {
			data.getMessageLogger().addErrorMessage("Već ste u traženoj grupi!");
			data.setResult(AbstractActionData.RESULT_SUCCESS);
			return;
		}
		Group myGroup = myUserGroup.getGroup();
		if(!myUserGroup.getGroup().getId().equals(data.getBean().getMyGroupID())) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.staleData"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		MPWorkContext mpwc = new MPWorkContext(em, data.getCourseInstance(), parent, data.getCurrentUser());
		if(!mpwc.marketPlaceActive) {
			data.getMessageLogger().addErrorMessage("Burza je zatvorena.");
			data.setResult(AbstractActionData.RESULT_SUCCESS);
			return;
		} else {
			if(!mpwc.canExchangeUTagControl(myUserGroup, otherUserGroup)) {
				data.getMessageLogger().addErrorMessage("Zatraženu akciju nije moguće izvesti: nekompatibilni tip studenta (primjerice, ne-demos i demos).");
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return;
			}
			List<MPOffer> offers = dh.getMarketPlaceDAO().listOffersRegardingUser(em, parent.getMarketPlace(), data.getCurrentUser(), myGroup);
			MPUserState state = MPUserState.buildFrom(offers, data.getCurrentUser(), mpwc.now, mpwc.scons, mpwc.myUserGroupMap);
			MPUserGroupState s = state.getOrCreateForGroup(myUserGroup);
			mpwc.calcSendDestinations(s);
			for(MPOffer o : s.getMyDirectOffers()) {
				if(o.getToGroup().equals(otherGroup) && o.getToUser().equals(otherUser)) {
					data.getMessageLogger().addErrorMessage("Tom ste korisniku već poslali direktnu ponudu.");
					data.setResult(AbstractActionData.RESULT_SUCCESS);
					return;
				}
			}
			// Kada mu smijem poslati grupnu ponudu? Ako je "s" aktivan i ako odredisna grupa nije blokirana:
			if(s.isActive() && !s.getBlockedGroups().contains(otherGroup)) {
				// Sada mogu poslati ponudu...
				MPOffer offer = new MPOffer();
				offer.setFromGroup(myGroup);
				offer.setFromUser(myUserGroup.getUser());
				offer.setFromTag(myUserGroup.getTag());
				offer.setMarketPlace(parent.getMarketPlace());
				offer.setNeedsAck(false); // Ove ponude po definiciji ne mogu biti s potvrdom!
				offer.setToGroup(otherGroup);
				offer.setToUser(otherUser);
				offer.setValidUntil(validUntil);
				if(!StringUtil.isStringBlank(data.getBean().getReason())) {
					if(data.getBean().getReason().length()>100) {
						offer.setReason(data.getBean().getReason().substring(0, 100));
					} else {
						offer.setReason(data.getBean().getReason());
					}
				}
				dh.getMarketPlaceDAO().save(em, offer);
				Date now = new Date();
				JCMSSettings.getSettings().getActivityReporter().addActivity(new MarketActivity(now, data.getCourseInstance().getId(), otherUser.getId(), 1, myGroup.getName(), data.getCurrentUser().getUsername(), myGroup.getParent().getId()));
				data.getMessageLogger().addInfoMessage("Ponuda je poslana.");
			} else {
				// Ne mogu poslati ponudu...
				data.getMessageLogger().addErrorMessage("Ponudu nije moguće poslati.");
			}
		}
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}
	
	public static void deleteOffer(EntityManager em, MPDeleteOfferData data) {
		
		// Dohvat kolegija
		if(!CourseInstanceServiceSupport.fillCourseInstance(em, data, data.getBean().getCourseInstanceID())) return;
		
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		Group parent = data.getBean().getParentID()!=null ? dh.getGroupDAO().get(em, data.getBean().getParentID()) : null;
		if(parent==null || !parent.getCompositeCourseID().equals(data.getCourseInstance().getId()) || !parent.isManagedRoot()) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		data.setParent(parent);
		
		if(data.getLockPath()==null || data.getLockPath().size()!=4 || !data.getLockPath().getPart(1).equals("ci"+data.getCourseInstance().getId()) || !data.getLockPath().getPart(3).equals("g"+parent.getId())) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}

		MPOffer offer = data.getBean().getOfferID()==null ? null : dh.getMarketPlaceDAO().getMPOffer(em, data.getBean().getOfferID());
		UserGroup myUserGroup = dh.getUserGroupDAO().get(em, data.getBean().getMyUserGroupID());
		if(myUserGroup==null || offer == null || !offer.getFromUser().equals(data.getCurrentUser()) || !offer.getFromGroup().equals(myUserGroup.getGroup())
				|| !myUserGroup.getGroup().getParent().equals(parent) || !myUserGroup.getUser().equals(data.getCurrentUser())) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		if(!myUserGroup.getGroup().getId().equals(data.getBean().getMyGroupID())) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.staleData"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}

		MPWorkContext mpwc = new MPWorkContext(em, data.getCourseInstance(), parent, data.getCurrentUser());
		if(!mpwc.marketPlaceActive) {
			data.getMessageLogger().addErrorMessage("Burza je zatvorena.");
			data.setResult(AbstractActionData.RESULT_SUCCESS);
			return;
		} else {
			dh.getMarketPlaceDAO().deleteReplysTo(em, parent.getMarketPlace(), offer);
			dh.getMarketPlaceDAO().remove(em, offer);
			data.getMessageLogger().addInfoMessage("Ponuda je uspješno obrisana.");
		}
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}

	public static void acceptGroupOffer(EntityManager em, MPAcceptOfferData data, String task) {
		
		// Dohvat kolegija
		if(!CourseInstanceServiceSupport.fillCourseInstance(em, data, data.getBean().getCourseInstanceID())) return;

		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		Group parent = data.getBean().getParentID()!=null ? dh.getGroupDAO().get(em, data.getBean().getParentID()) : null;
		if(parent==null || !parent.getCompositeCourseID().equals(data.getCourseInstance().getId()) || !parent.isManagedRoot()) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		data.setParent(parent);
		
		if(data.getLockPath()==null || data.getLockPath().size()!=4 || !data.getLockPath().getPart(1).equals("ci"+data.getCourseInstance().getId()) || !data.getLockPath().getPart(3).equals("g"+parent.getId())) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}

		MPOffer offer = data.getBean().getOfferID()==null ? null : dh.getMarketPlaceDAO().getMPOffer(em, data.getBean().getOfferID());
		UserGroup myUserGroup = dh.getUserGroupDAO().get(em, data.getBean().getMyUserGroupID());
		if(myUserGroup==null || offer == null || !myUserGroup.getUser().equals(data.getCurrentUser()) || !offer.getToGroup().equals(myUserGroup.getGroup())
				|| !myUserGroup.getGroup().getParent().equals(parent)) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		if(!myUserGroup.getGroup().getId().equals(data.getBean().getMyGroupID())) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.staleData"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		// Pronadi UserGroup objekt za drugog korisnika
		UserGroup otherUserGroup = dh.getUserGroupDAO().find(em, offer.getFromUser(), offer.getFromGroup());
		if(otherUserGroup==null) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.staleData"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		Date now = new Date();
		if(MPUserState.isOfferExpired(offer.getValidUntil(), now)) {
			data.getMessageLogger().addErrorMessage("Ponuda je istekla.");
			data.setResult(AbstractActionData.RESULT_SUCCESS);
			return;
		}
		if(offer.getReplyTo()!=null && MPUserState.isOfferExpired(offer.getReplyTo().getValidUntil(), now)) {
			data.getMessageLogger().addErrorMessage("Originalna ponuda je istekla.");
			data.setResult(AbstractActionData.RESULT_SUCCESS);
			return;
		}
		MPWorkContext mpwc = new MPWorkContext(em, data.getCourseInstance(), parent, data.getCurrentUser());
		if(!mpwc.marketPlaceActive) {
			data.getMessageLogger().addErrorMessage("Burza je zatvorena.");
			data.setResult(AbstractActionData.RESULT_SUCCESS);
			return;
		} else {
			if(!mpwc.canExchangeUTagControl(myUserGroup, otherUserGroup)) {
				data.getMessageLogger().addErrorMessage("Zatraženu akciju nije moguće izvesti: nekompatibilni tip studenta (primjerice, ne-demos i demos).");
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return;
			}
			if(!mpwc.canExchange(offer.getFromGroup(), offer.getFromGroup().getMpSecurityTag(), offer.getFromTag(), myUserGroup.getGroup(), myUserGroup.getGroup().getMpSecurityTag(), myUserGroup.getTag())) {
				data.getMessageLogger().addErrorMessage("Zatraženu akciju nije moguće izvesti zbog ograničenja koja je administrator postavio na burzu.");
				data.setResult(AbstractActionData.RESULT_SUCCESS);
				return;
			}
			if(task.equals("acceptGroupOffer") || task.equals("acceptApproval") || task.equals("acceptDirectOffer")) {
				if(offer.getToUser()!=null && !offer.getToUser().equals(myUserGroup.getUser())) {
					// Hm... Netko nesto muti...
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return;
				}
				// obavi zamjenu
				Group myGroup = myUserGroup.getGroup();
				Group otherGroup = otherUserGroup.getGroup();
				dh.getMarketPlaceDAO().clearAllOffersForUsers(em, parent.getMarketPlace(), offer.getFromUser(), otherGroup, myUserGroup.getUser(), myGroup);
				myGroup.getUsers().remove(myUserGroup);
				otherGroup.getUsers().remove(otherUserGroup);
				myGroup.getUsers().add(otherUserGroup);
				otherGroup.getUsers().add(myUserGroup);
				myUserGroup.setGroup(otherGroup);
				otherUserGroup.setGroup(myGroup);
				data.getMessageLogger().addInfoMessage("Zamjena je uspješno obavljena.");
				JCMSSettings.getSettings().getActivityReporter().addActivity(new MarketActivity(now, data.getCourseInstance().getId(), myUserGroup.getUser().getId(), 2, otherGroup.getName(), "", otherGroup.getParent().getId()));
				JCMSSettings.getSettings().getActivityReporter().addActivity(new MarketActivity(now, data.getCourseInstance().getId(), otherUserGroup.getUser().getId(), 2, myGroup.getName(), "", myGroup.getParent().getId()));
				JCMSLogger.getLogger().mpLogSwitch(offer.getMarketPlace(), myUserGroup.getUser(), myGroup, otherUserGroup.getUser(), otherGroup, offer.getReplyTo()!=null ? "APPROVAL" : (offer.getToUser()!=null ? "DIRECT" : "GROUP"));
			} else if(task.equals("sendApprovalRequest")) {
				if(offer.getToUser()!=null || offer.getReplyTo()!=null || !offer.getNeedsAck()) {
					// Hm... Netko nesto muti...
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return;
				}
				List<MPOffer> offers = dh.getMarketPlaceDAO().listOffersRegardingUser(em, parent.getMarketPlace(), data.getCurrentUser(), myUserGroup.getGroup());
				MPUserState state = MPUserState.buildFrom(offers, data.getCurrentUser(), mpwc.now, mpwc.scons, mpwc.myUserGroupMap);
				MPUserGroupState s = state.getOrCreateForGroup(myUserGroup);
				mpwc.calcSendDestinations(s);
				if(!s.isActive()) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return;
				}
				boolean alreadySent = false;
				for(MPOffer mpoffer : s.getMyAckReqForGroupOffers()) {
					if(mpoffer.getReplyTo()!=null && mpoffer.getReplyTo().equals(offer)) {
						alreadySent = true;
						break;
					}
				}
				if(alreadySent) {
					data.getMessageLogger().addErrorMessage("Zahtjev za zamjenom već ste poslali tom korisniku.");
					data.setResult(AbstractActionData.RESULT_SUCCESS);
					return;
				}
				MPOffer o = new MPOffer();
				o.setFromGroup(myUserGroup.getGroup());
				o.setFromTag(myUserGroup.getTag());
				o.setFromUser(myUserGroup.getUser());
				o.setMarketPlace(parent.getMarketPlace());
				o.setNeedsAck(false);
				o.setReplyTo(offer);
				o.setToGroup(offer.getFromGroup());
				o.setToUser(offer.getFromUser());
				dh.getMarketPlaceDAO().save(em, o);
				data.getMessageLogger().addInfoMessage("Zahtjev za zamjenom poslan je korisniku.");
			} else {
				data.getMessageLogger().addErrorMessage("Interna pogreška.");
			}
		}
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}
	
	public static void directMove(EntityManager em, MPDirectMoveData data) {
		
		// Dohvat kolegija
		if(!CourseInstanceServiceSupport.fillCourseInstance(em, data, data.getBean().getCourseInstanceID())) return;

		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		Group parent = data.getBean().getParentID()!=null ? dh.getGroupDAO().get(em, data.getBean().getParentID()) : null;
		if(parent==null || !parent.getCompositeCourseID().equals(data.getCourseInstance().getId()) || !parent.isManagedRoot()) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		data.setParent(parent);
		
		if(data.getLockPath()==null || data.getLockPath().size()!=4 || !data.getLockPath().getPart(1).equals("ci"+data.getCourseInstance().getId()) || !data.getLockPath().getPart(3).equals("g"+parent.getId())) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}

		data.setMarketPlace(parent.getMarketPlace());
		JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
		Group otherGroup = data.getBean().getGroupID()!=null ? dh.getGroupDAO().get(em, data.getBean().getGroupID()) : null;
		UserGroup myUserGroup = dh.getUserGroupDAO().get(em, data.getBean().getMyUserGroupID());
		if(myUserGroup==null || !myUserGroup.getUser().equals(data.getCurrentUser())
				|| !myUserGroup.getGroup().getParent().equals(parent) || otherGroup==null 
				|| !otherGroup.getParent().equals(parent)) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		Group myGroup = myUserGroup.getGroup();
		if(!myUserGroup.getGroup().getId().equals(data.getBean().getMyGroupID())) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.staleData"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}

		MPWorkContext mpwc = new MPWorkContext(em, data.getCourseInstance(), parent, data.getCurrentUser());
		if(!mpwc.marketPlaceActive) {
			data.getMessageLogger().addErrorMessage("Burza je zatvorena.");
			data.setResult(AbstractActionData.RESULT_SUCCESS);
			return;
		} else {
			mpwc.prepareCheckMove(myUserGroup);
			if(!mpwc.canMoveStudentToGroup(myUserGroup, otherGroup)) {
				// javi da nije moguce premjestiti
				data.getMessageLogger().addErrorMessage("Premještanje nije moguće obaviti.");
			} else {
				// premjesti ga...
				myUserGroup.getGroup().getUsers().remove(myUserGroup);
				myUserGroup.setGroup(otherGroup);
				otherGroup.getUsers().add(myUserGroup);
				em.flush();
				dh.getMarketPlaceDAO().clearAllOffersForUser(em, mpwc.marketPlace, myUserGroup.getUser(), myGroup);
				data.setMovedFromGroup(myGroup);
				data.setMovedToGroup(otherGroup);
				data.setMovedUser(myUserGroup.getUser());
				Date now = new Date();
				JCMSSettings.getSettings().getActivityReporter().addActivity(new MarketActivity(now, data.getCourseInstance().getId(), myUserGroup.getUser().getId(), 2, otherGroup.getName(), "", otherGroup.getParent().getId()));
				data.getMessageLogger().addInfoMessage("Premještanje je uspješno obavljeno.");
				JCMSLogger.getLogger().mpLogMove(mpwc.marketPlace, myUserGroup.getUser(), myGroup, otherGroup, (User)null);
			}
		}
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}

	public static void adminMarketPlace(EntityManager em, MPGroupsAdminData data, String task) {
		
		// Dohvat kolegija
		if(!CourseInstanceServiceSupport.fillCourseInstance(em, data, data.getCourseInstanceID())) return;

		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		
		Group parent = data.getParentID()!=null ? dh.getGroupDAO().get(em, data.getParentID()) : null;
		if(parent==null) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		data.setParent(parent);
		if(!parent.isManagedRoot()) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.nonManagedRoot"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		JCMSSecurityManagerFactory.getManager().init(data.getCurrentUser(), em);
		boolean canManage = JCMSSecurityManagerFactory.getManager().canManageCourseMarketPlace(data.getCourseInstance(), parent.getRelativePath());
		if(!canManage) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		MarketPlace mp = parent.getMarketPlace(); 
		if(mp==null) {
			mp = new MarketPlace();
			mp.setFormulaConstraints(null);
			mp.setOpen(false);
			mp.setTimeBuffer(-1);
			dh.getMarketPlaceDAO().save(em, mp);
			mp.setGroup(parent);
			parent.setMarketPlace(mp);
		}

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		Date now = new Date();
		
		List<Group> groupList = new ArrayList<Group>(parent.getSubgroups());
		List<GroupBean> newGroups = new ArrayList<GroupBean>(groupList.size());
		Collections.sort(groupList, StringUtil.GROUP_COMPARATOR);
		for(Group g : groupList) {
			GroupBean gb = new GroupBean();
			gb.setCapacity(g.getCapacity());
			gb.setCompositeCourseID(g.getCompositeCourseID());
			gb.setEnteringAllowed(g.isEnteringAllowed());
			gb.setId(g.getId());
			gb.setLeavingAllowed(g.isLeavingAllowed());
			gb.setManagedRoot(g.isManagedRoot());
			gb.setMpSecurityTag(g.getMpSecurityTag());
			gb.setName(g.getName());
			gb.setRelativePath(g.getRelativePath());
			newGroups.add(gb);
		}
		if(task.equals("input")) {
			data.getBean().setGroups(newGroups);
			data.getBean().setFormulaConstraints(mp.getFormulaConstraints());
			data.getBean().setId(mp.getId());
			data.getBean().setOpen(mp.getOpen());
			data.getBean().setOpenFrom(mp.getOpenFrom()==null ? null : sdf.format(mp.getOpenFrom()));
			data.getBean().setOpenUntil(mp.getOpenUntil()==null ? null : sdf.format(mp.getOpenUntil()));
			data.getBean().setSecurityConstraints(mp.getSecurityConstraints());
			data.getBean().setTimeBuffer(mp.getTimeBuffer());
			data.setResult(AbstractActionData.RESULT_INPUT);
			data.setActive(mp.isActive(now));
			return;
		}
		if(!task.equals("update")) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		if(data.getBean().getTimeBuffer()<0 && data.getBean().getTimeBuffer()!=-1) {
			data.getBean().setTimeBuffer(-1);
		}
		boolean errors = false;
		Date openFrom = null;
		if(!StringUtil.isStringBlank(data.getBean().getOpenFrom())) {
			try {
				openFrom = sdf.parse(data.getBean().getOpenFrom());
			} catch (ParseException e) {
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.wrongDateFormat"));
				errors = true;
			}
		}
		Date openUntil = null;
		if(!StringUtil.isStringBlank(data.getBean().getOpenUntil())) {
			try {
				openUntil = sdf.parse(data.getBean().getOpenUntil());
			} catch (ParseException e) {
				data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.wrongDateFormat"));
				errors = true;
			}
		}
		if(!StringUtil.isStringBlank(data.getBean().getFormulaConstraints())) {
			try {
				new MPFormulaConstraints(data.getBean().getFormulaConstraints());
			} catch (ParseException e) {
				data.getMessageLogger().addErrorMessage("Greska u tumacenju formule: "+e.getMessage());
				errors = true;
			}
		}
		if(!StringUtil.isStringBlank(data.getBean().getSecurityConstraints())) {
			try {
				new MPSecurityConstraints(data.getBean().getSecurityConstraints());
			} catch (ParseException e) {
				data.getMessageLogger().addErrorMessage("Greska u tumacenju ogranicenja: "+e.getMessage());
				errors = true;
			}
		}
		Map<Long, Group> mapByID = GroupUtil.mapGroupByID(groupList);
		if(!errors && data.getBean().getGroups()!=null) {
			for(GroupBean gb : data.getBean().getGroups()) {
				if(gb.getId()==null || !mapByID.containsKey(gb.getId())) {
					data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
					data.setResult(AbstractActionData.RESULT_FATAL);
					return;
				}
				try {
					MPSecurityConstraints.checkSecurityTagFormat(gb.getMpSecurityTag());
				} catch(ParseException ex) {
					data.getMessageLogger().addErrorMessage(ex.getMessage());
					data.setResult(AbstractActionData.RESULT_INPUT);
					return;
				}
			}
			for(GroupBean gb : data.getBean().getGroups()) {
				Group g = mapByID.get(gb.getId());
				g.setEnteringAllowed(gb.isEnteringAllowed());
				g.setLeavingAllowed(gb.isLeavingAllowed());
				if(gb.getCapacity()<0 && gb.getCapacity()!=-1) {
					g.setCapacity(-1);
				} else {
					g.setCapacity(gb.getCapacity());
				}
				g.setMpSecurityTag(gb.getMpSecurityTag());
			}
		}
		if(errors) {
			data.setResult(AbstractActionData.RESULT_INPUT);
			return;
		}
		mp.setFormulaConstraints(data.getBean().getFormulaConstraints());
		mp.setOpen(data.getBean().isOpen());
		mp.setOpenFrom(openFrom);
		mp.setOpenUntil(openUntil);
		mp.setSecurityConstraints(data.getBean().getSecurityConstraints());
		mp.setTimeBuffer(data.getBean().getTimeBuffer());
		data.setActive(mp.isActive(now));
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}
	
	private static boolean checkMarketPlaceActive(EntityManager em, MarketPlace marketPlace, Date now) {
		if(marketPlace == null) return false;
		String burzaAktivna = BasicServiceSupport.getKeyValue(em, "marketPlace");
		if(!"yes".equals(burzaAktivna)) return false;
		return marketPlace.isActive(now);
	}

	private static class MPWorkContext {
		Date now;
		// Group parent;
		MarketPlace marketPlace;
		boolean marketPlaceActive;
		MPSecurityConstraints scons;
		MPFormulaConstraints fcons;
		MPFormulaContext context;
		List<Group> allGroups;
		Map<Long, Group> groupsByIDMap;
		List<UserGroup> userGroups;
		Map<Group,UserGroup> myUserGroupMap;
		Set<Group> myGroups;
		Date newLimit;
		
		public MPWorkContext(EntityManager em, CourseInstance courseInstance, Group parent, User user) {
			now = new Date();
			userGroups = DAOHelperFactory.getDAOHelper().getGroupDAO().findUserGroupsForUser(em, courseInstance.getId(), parent.getRelativePath(), user);
			Collections.sort(userGroups, StringUtil.USER_GROUP_COMPARATOR2);
			myUserGroupMap = new HashMap<Group, UserGroup>();
			myGroups = new HashSet<Group>();
			for(UserGroup ug : userGroups) {
				myUserGroupMap.put(ug.getGroup(),ug);
				myGroups.add(ug.getGroup());
			}
			marketPlace = parent.getMarketPlace();
			marketPlaceActive = checkMarketPlaceActive(em, marketPlace, now);
			try {
				if(marketPlaceActive) scons = new MPSecurityConstraints(marketPlace.getSecurityConstraints());
			} catch (ParseException e) {
				marketPlaceActive = false;
			}
			try {
				if(marketPlaceActive) fcons = new MPFormulaConstraints(marketPlace.getFormulaConstraints());
			} catch (ParseException e) {
				marketPlaceActive = false;
			}
			if(marketPlaceActive) {
				allGroups = DAOHelperFactory.getDAOHelper().getGroupDAO().findSubgroups(em, courseInstance.getId(), parent.getRelativePath()+"/%");
				Collections.sort(allGroups, StringUtil.GROUP_COMPARATOR);
				groupsByIDMap = GroupUtil.mapGroupByID(allGroups);
				if(fcons!=null && fcons.getNumberOfConstraints()!=0) {
					List<Object[]> res = DAOHelperFactory.getDAOHelper().getGroupDAO().getGroupStat(em, parent.getCompositeCourseID(), parent.getRelativePath());
					context = new SimpleMPFormulaContext(res,groupsByIDMap);
				} else {
					List<Object[]> res = DAOHelperFactory.getDAOHelper().getGroupDAO().getCoarseGroupStat(em, parent.getCompositeCourseID(), parent.getRelativePath());
					context = new BlankMPFormulaContext(res,groupsByIDMap);
				}
				if(marketPlace.getTimeBuffer()!=-1) {
					Calendar cal = Calendar.getInstance();
					cal.setTime(now);
					cal.add(Calendar.SECOND, marketPlace.getTimeBuffer());
					newLimit = cal.getTime();
				}
			}
		}

		private boolean precalculatedCheckMoveDeny;
		private String initialUserGroupName;
		private String initialStudentTag;
		
		public void prepareCheckMove(UserGroup ug) {
			initialUserGroupName = ug.getGroup().getName();
			initialStudentTag = ug.getTag();
			context.getExchangeDescriptor().setFromGroup(ug.getGroup().getName());
			context.getExchangeDescriptor().setFromGroupTag(ug.getGroup().getMpSecurityTag());
			context.getExchangeDescriptor().setFromStudentTag(ug.getTag());
			context.getExchangeDescriptor().setToStudentTag(ug.getTag());
			context.getExchangeDescriptor().setToGroup(null);
			context.getExchangeDescriptor().setToGroupTag(null);
			// Ako se iz grupe ne moze van, ili se zbog vremenskog ogranicenja vise ne moze van, to zabiljezi:
			if(!ug.getGroup().isLeavingAllowed() || checkGroupTimeConstraintViolated(ug.getGroup(),newLimit)) {
				precalculatedCheckMoveDeny = true; 
			} else {
				precalculatedCheckMoveDeny = false; 
			}
		}

		public boolean canMoveStudentToGroup(UserGroup ug, Group g) {
			// ako vec unaprijed znamo da ne mozemo van:
			if(precalculatedCheckMoveDeny) return false;
			// nije dopušten ulazak u grupu...
			if(!g.isEnteringAllowed()) return false;
			// ako je definirano ograničenje kapaciteta, onda ne:
			if(g.getCapacity()!=-1 && context.getTotalSizeForGroup(g.getName())>=g.getCapacity()) return false;
			// ako je to grupa nerasporedenih, onda ne
			if(StringUtil.isStringBlank(g.getName())) return false;
			// ako sam vec tamo, onda ne
			if(myGroups.contains(g)) return false;
			// ako to zbog sigurnosnih pravila nije moguće
			if(scons!=null && !scons.canMove(ug.getTag(), ug.getGroup().getMpSecurityTag(), g.getMpSecurityTag())) return false;
			// ako je ciljna grupa u konfliktu s vremenskim ograničenjem...
			if(checkGroupTimeConstraintViolated(g,newLimit)) return false;
			context.getExchangeDescriptor().setToGroup(g.getName());
			context.getExchangeDescriptor().setToGroupTag(g.getMpSecurityTag());
			if(fcons!=null && fcons.getNumberOfConstraints()>0) {
				// Ovdje sada znam otkud selim i kamo selim. Izracunajmo neslaganje prije:
				fcons.canMoveStudent(context);
				int vmBefore = context.getViolationMeasure();
				// OK, ovdje umanji za jedan aktualnog studenta:
				context.decrease(initialUserGroupName, initialStudentTag);
				context.increase(g.getName(), ug.getTag());
				boolean canMove = fcons.canMoveStudent(context);
				int vmAfter = context.getViolationMeasure();
				context.increase(initialUserGroupName, initialStudentTag);
				context.decrease(g.getName(), ug.getTag());
				if(!canMove) {
					// Ako se ovim preseljenjem popravlja stanje
					// (recimo, iz prenatrpane grupe jedan student želi izaci)
					// to pak dopustimo...
					if(vmAfter < vmBefore) return true;
					return false;
				}
			}
			// Inace se cini da mogu!
			return true;
		}

		public boolean checkGroupTimeConstraintViolated(Group group, Date timeConstraint) {
			if(timeConstraint==null) return false;
			Set<GroupWideEvent> gwes = group.getEvents();
			for(GroupWideEvent gwe : gwes) {
				if(gwe.getStart().before(timeConstraint)) {
					return true;
				}
			}
			return false;
		}

		public boolean checkGroupTimeConstraintViolated(Group group) {
			return checkGroupTimeConstraintViolated(group, newLimit);
		}

		public void calcSendDestinations(MPUserGroupState s) {
			// U svim grupama blokiraj moje vlastite grupe
			s.getBlockedGroups().addAll(myGroups);
			s.getDirectBlockedGroups().addAll(myGroups);
			// Ako ja iz ove grupe ne mogu van, ili sam u grupi "nerasporedeni", tada su zamjene zabranjene
			if(StringUtil.isStringBlank(s.getMyUserGroup().getGroup().getName()) || !s.getMyUserGroup().getGroup().isLeavingAllowed()) {
				s.getBlockedGroups().addAll(allGroups);
				s.getDirectBlockedGroups().addAll(allGroups);
			} else {
				for(Group g : allGroups) {
					// Ako ne mogu tamo zbog sigurnosnih ogranicenja, ili zbog isteka vremena, ili zato sto ja ne mogu unutra:
					if((scons!=null && !scons.canMove(myUserGroupMap.get(s.getMyUserGroup().getGroup()).getTag(), s.getMyUserGroup().getGroup().getMpSecurityTag(), g.getMpSecurityTag())) || checkGroupTimeConstraintViolated(g) || !g.isEnteringAllowed()) {
						s.getBlockedGroups().add(g);
						s.getDirectBlockedGroups().add(g);
					} else if(StringUtil.isStringBlank(g.getName())) {
						s.getBlockedGroups().add(g);
						s.getDirectBlockedGroups().add(g);
					}
				}
			}
			s.getAvailForDirectOffers().addAll(allGroups);
			s.getAvailForDirectOffers().removeAll(s.getDirectBlockedGroups());
			s.getAvailForGroupOffers().addAll(allGroups);
			s.getAvailForGroupOffers().removeAll(s.getBlockedGroups());
			s.setActive(true);
			if(!s.getMyUserGroup().getGroup().isLeavingAllowed()) {
				s.setActive(false);
			} else if(newLimit!=null) {
				boolean violated = checkGroupTimeConstraintViolated(s.getMyUserGroup().getGroup(), newLimit);
				if(violated) s.setActive(false);
			}
		}
		
		public boolean canExchangeUTagControl(UserGroup ug1, UserGroup ug2) {
			boolean blank1 = StringUtil.isStringBlank(ug1.getTag());
			boolean blank2 = StringUtil.isStringBlank(ug2.getTag());
			// Ako oba nemaju taga, moze
			if(blank1 && blank2) return true;
			// Ako je jedan prazan a drugi nije (ovo slijedi iz prethodne provjere gdje smo vidjeli da nisu oba prazna), ne moze
			if(blank1 || blank2) return false;
			// Inace su oba puna; tada moraju biti isti:
			return ug1.getTag().equals(ug2.getTag());
		}
		
		public boolean canExchange(Group fromGroup, String fromGroupTag, String fromStudentTag, Group toGroup, String toGroupTag, String toStudentTag) {
			if(StringUtil.isStringBlank(fromGroup.getName()) || !fromGroup.isEnteringAllowed() || !fromGroup.isLeavingAllowed()) return false;
			if(StringUtil.isStringBlank(toGroup.getName()) || !toGroup.isEnteringAllowed() || !toGroup.isLeavingAllowed()) return false;
			if(newLimit!=null) {
				if(checkGroupTimeConstraintViolated(fromGroup, newLimit)) return false;
				if(checkGroupTimeConstraintViolated(toGroup, newLimit)) return false;
			}
			if(scons!=null && !scons.canExchange(fromGroupTag, fromStudentTag, toGroupTag, toStudentTag)) return false;
			return true;
		}
	}
	
	private static class SimpleMPFormulaContext implements MPFormulaContext {
		int violationMeasure;

		Map<String,int[]> totals = new HashMap<String, int[]>();
		Map<String,int[]> details = new HashMap<String,int[]>(64);
		boolean formulaApplies;
		ExchangeDescriptor exchangeDescriptor = new ExchangeDescriptor();

		public SimpleMPFormulaContext(List<Object[]> res, Map<Long, Group> groupsByIDMap) {
			for(Object[] o : res) {
				Long id = (Long)o[0];
				String tag = (String)o[1];
				int broj = ((Number)o[2]).intValue();
				String groupName = groupsByIDMap.get(id).getName();
				String detKey = groupName+"'''"+(tag==null ? "" : tag);
				details.put(detKey, new int[] {broj});
				int[] data = totals.get(groupName);
				if(data==null) {
					data = new int[] {broj};
					totals.put(groupName, data);
				} else {
					data[0] += broj;
				}
			}
		}

		@Override
		public void clearFormulaAppliesFlag() {
			formulaApplies = false;
		}

		@Override
		public ExchangeDescriptor getExchangeDescriptor() {
			return exchangeDescriptor;
		}

		@Override
		public boolean getFormulaAppliesFlag() {
			return formulaApplies;
		}

		@Override
		public int getNumberOfStudentsWithTag(String groupName, String tagName) {
			String detKey = groupName+"'''"+(tagName==null ? "" : tagName);
			int[] res = details.get(detKey);
			if(res==null) return 0;
			return res[0];
		}

		@Override
		public int getTotalSizeForGroup(String groupName) {
			int[] data = totals.get(groupName);
			if(data==null) return 0;
			return data[0];
		}

		@Override
		public void setFormulaAppliesFlag() {
			formulaApplies = true;
		}
		
		@Override
		public void decrease(String groupName, String studentTag) {
			String detKey = groupName+"'''"+(studentTag==null ? "" : studentTag);
			int[] res = details.get(detKey);
			if(res==null) {
				res = new int[] {0};
				details.put(detKey, res);
			}
			res[0]--;
			res = totals.get(groupName);
			if(res==null) {
				res = new int[] {0};
				totals.put(groupName, res);
			}
			res[0]--;
		}

		@Override
		public void increase(String groupName, String studentTag) {
			String detKey = groupName+"'''"+(studentTag==null ? "" : studentTag);
			int[] res = details.get(detKey);
			if(res==null) {
				res = new int[] {0};
				details.put(detKey, res);
			}
			res[0]++;
			res = totals.get(groupName);
			if(res==null) {
				res = new int[] {0};
				totals.put(groupName, res);
			}
			res[0]++;
		}
		@Override
		public void addViolationMeasure(int measure) {
			violationMeasure += measure;
		}
		@Override
		public int getViolationMeasure() {
			return violationMeasure;
		}
		@Override
		public void resetViolationMeasure() {
			violationMeasure = 0;
		}
	}

	private static class BlankMPFormulaContext implements MPFormulaContext {
		int violationMeasure;
		boolean formulaApplies;
		ExchangeDescriptor exchangeDescriptor = new ExchangeDescriptor();
		Map<String,Integer> totals = new HashMap<String, Integer>();
		public BlankMPFormulaContext(List<Object[]> res, Map<Long, Group> groupsByIDMap) {
			for(Object[] o : res) {
				totals.put(groupsByIDMap.get((Long)o[0]).getName(), Integer.valueOf(((Number)o[1]).intValue()));
			}
		}

		@Override
		public void clearFormulaAppliesFlag() {
			formulaApplies = false;
		}

		@Override
		public ExchangeDescriptor getExchangeDescriptor() {
			return exchangeDescriptor;
		}

		@Override
		public boolean getFormulaAppliesFlag() {
			return formulaApplies;
		}

		@Override
		public int getNumberOfStudentsWithTag(String groupName, String tagName) {
			return 0;
		}

		@Override
		public int getTotalSizeForGroup(String groupName) {
			if(totals==null) return 0;
			Integer i = totals.get(groupName);
			if(i==null) return 0;
			return i.intValue();
		}

		@Override
		public void setFormulaAppliesFlag() {
			formulaApplies = true;
		}
		
		@Override
		public void decrease(String groupName, String studentTag) {
		}
		
		@Override
		public void increase(String groupName, String studentTag) {
		}
		@Override
		public void addViolationMeasure(int measure) {
			violationMeasure += measure;
		}
		@Override
		public int getViolationMeasure() {
			return violationMeasure;
		}
		@Override
		public void resetViolationMeasure() {
			violationMeasure = 0;
		}
	}
}
