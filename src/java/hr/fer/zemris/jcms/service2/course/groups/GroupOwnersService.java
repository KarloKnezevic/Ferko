package hr.fer.zemris.jcms.service2.course.groups;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import hr.fer.zemris.jcms.beans.GroupBean;
import hr.fer.zemris.jcms.beans.ext.BaseUserBean;
import hr.fer.zemris.jcms.dao.DAOHelper;
import hr.fer.zemris.jcms.dao.DAOHelperFactory;
import hr.fer.zemris.jcms.model.Group;
import hr.fer.zemris.jcms.model.GroupOwner;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.security.GroupSupportedPermission;
import hr.fer.zemris.jcms.security.JCMSSecurityConstants;
import hr.fer.zemris.jcms.security.JCMSSecurityManagerFactory;
import hr.fer.zemris.jcms.security.GroupSupportedPermission.MarketPlacePlacement;
import hr.fer.zemris.jcms.service.util.GroupUtil;
import hr.fer.zemris.jcms.web.actions.data.SubgroupOwnersData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.util.StringUtil;

import javax.persistence.EntityManager;

public class GroupOwnersService {

	public static void prepareShow(EntityManager em, SubgroupOwnersData data) {
		
		if(!GroupServiceSupport.loadGroup(em, data, data.getGroupID())) return;
		
		GroupSupportedPermission gPerm = JCMSSecurityManagerFactory.getManager().getGroupPermissionFor(data.getCourseInstance(), data.getGroup());
		if(gPerm.getPlacement()==MarketPlacePlacement.BEFORE_MARKET_PLACE) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		
		data.setResult(AbstractActionData.RESULT_INPUT);
	}
	
	/**
	 * Dohvaća trenutne podgrupe i pridružene vlasnike istih.
	 * 
	 * @param em entity manager
	 * @param data podatkovni objekt
	 */
	public static void current(EntityManager em, SubgroupOwnersData data) {
		
		if(!GroupServiceSupport.loadGroup(em, data, data.getGroupID())) return;
		
		GroupSupportedPermission gPerm = JCMSSecurityManagerFactory.getManager().getGroupPermissionFor(data.getCourseInstance(), data.getGroup());
		if(gPerm.getPlacement()==MarketPlacePlacement.BEFORE_MARKET_PLACE) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}

		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		List<Group> groupList = GroupUtil.retrieveManageableGroupOwners(data.getCourseInstance(), data.getGroup());
		List<User> ownerList = 
			dh.getGroupDAO().listUsersInGroupTree(em, data.getCourseInstance().getId(), JCMSSecurityConstants.SEC_ROLE_GROUP);
		if (ownerList == null || ownerList.size()==0) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noOwners"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}

		Collections.sort(ownerList, StringUtil.USER_COMPARATOR);
		Collections.sort(groupList, StringUtil.GROUP_COMPARATOR);
		
		List<GroupBean> groupBeanList = new ArrayList<GroupBean>(groupList.size());
		List<GroupOwner> goList = null;
		List<BaseUserBean> owners = null;
		for (Group g : groupList) { 
			GroupBean bean = new GroupBean();
			bean.setId(g.getId());
			bean.setName(g.getName());
			
			goList = dh.getGroupDAO().findForGroup(em, g);
			owners = new ArrayList<BaseUserBean>(goList.size());
			for (GroupOwner go : goList) {
				BaseUserBean b = new BaseUserBean();
				b.setFirstName(go.getUser().getFirstName());
				b.setLastName(go.getUser().getLastName());
				b.setJmbag(go.getUser().getJmbag());
				b.setUserID(go.getId());
				
				owners.add(b);
			}
			
			bean.setOwnerList(owners);
			groupBeanList.add(bean);
		}
		data.setGroupList(groupBeanList);
		data.setUserList(ownerList);
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}

	/**
	 * Dodaje novog vlasnika grupe.
	 * 
	 * @param em entity manager
	 * @param data podatkovni objekt
	 */
	public static void addGroupOwner(EntityManager em, SubgroupOwnersData data) {
		
		Long gid = null;
		try{
			gid = Long.valueOf(data.getGoBean().getGroupID());
		} catch(Exception ex) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		if(!GroupServiceSupport.loadGroup(em, data, gid)) return;
		
		GroupSupportedPermission gPerm = JCMSSecurityManagerFactory.getManager().getGroupPermissionFor(data.getCourseInstance(), data.getGroup());
		if(gPerm.getPlacement()==MarketPlacePlacement.BEFORE_MARKET_PLACE || !gPerm.getCanManageGroupOwners()) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}

		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		Group g = null;
		User u = null;
		try {
			g = data.getGroup();
			u = dh.getUserDAO().getUserById(em, Long.valueOf(data.getGoBean().getUserID()));
		} catch (Exception e) {
		}
		if (u == null) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		
		if (dh.getGroupDAO().getGroupOwner(em, g, u)!=null) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.alreadyGroupOwner"));
			data.setResult(AbstractActionData.RESULT_SUCCESS);
			return;
		}
		
		GroupOwner go = new GroupOwner();
		go.setGroup(g);
		go.setUser(u);
		dh.getGroupDAO().save(em, go);
		
		data.setOwner(go);
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}

	public static void removeGroupOwner(EntityManager em, SubgroupOwnersData data) {
		
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		GroupOwner go = null;
		try {
			go = dh.getGroupDAO().getGroupOwner(em, Long.valueOf(data.getGroupOwnerID()));
		} catch (Exception e) {
		}
		if (go == null) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		
		if(!GroupServiceSupport.loadGroup(em, data, go.getGroup().getId())) return;

		GroupSupportedPermission gPerm = JCMSSecurityManagerFactory.getManager().getGroupPermissionFor(data.getCourseInstance(), data.getGroup());
		if(gPerm.getPlacement()==MarketPlacePlacement.BEFORE_MARKET_PLACE || !gPerm.getCanManageGroupOwners()) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}

		go.setGroup(null);
		go.setUser(null);
		dh.getGroupDAO().remove(em, go);
		
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}
	
}
