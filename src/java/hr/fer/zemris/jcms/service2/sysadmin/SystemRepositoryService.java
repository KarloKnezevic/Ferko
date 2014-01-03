package hr.fer.zemris.jcms.service2.sysadmin;

import java.util.ArrayList;

import java.util.List;

import hr.fer.zemris.jcms.beans.KeyValueBean;
import hr.fer.zemris.jcms.dao.DAOHelper;
import hr.fer.zemris.jcms.dao.DAOHelperFactory;
import hr.fer.zemris.jcms.model.KeyValue;
import hr.fer.zemris.jcms.security.JCMSSecurityManagerFactory;
import hr.fer.zemris.jcms.web.actions.data.SystemEditRepositoryData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.util.StringUtil;

import javax.persistence.EntityManager;

public class SystemRepositoryService {

	public static void prepareView(EntityManager em, SystemEditRepositoryData data) {
		if(!JCMSSecurityManagerFactory.getManager().canPerformSystemAdministration()) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		List<KeyValue> res = DAOHelperFactory.getDAOHelper().getKeyValueDAO().list(em);
		List<KeyValueBean> keyValueBeans = new ArrayList<KeyValueBean>(res.size());
		for(KeyValue kv : res) {
			KeyValueBean kvb = new KeyValueBean(kv.getName(), kv.getValue());
			keyValueBeans.add(kvb);
		}
		data.setRepository(keyValueBeans);
		data.setResult(AbstractActionData.RESULT_INPUT);
	}
	
	public static void updateRepository(EntityManager em, SystemEditRepositoryData data) {
		if(!JCMSSecurityManagerFactory.getManager().canPerformSystemAdministration()) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}
		List<KeyValueBean> keyValueBeans = data.getRepository();
		List<KeyValue> keyValues = DAOHelperFactory.getDAOHelper().getKeyValueDAO().list(em);
		for(KeyValue kv : keyValues) {
			KeyValueBean kvb = findKeyValueBean(keyValueBeans, kv.getName());
			if(kvb == null) continue;
			if(!StringUtil.stringEqualsLoosly(kvb.getValue(), kv.getValue())) {
				kv.setValue(kvb.getValue());
			}
		}
		data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.dataSuccessfullyUpdated"));
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}

	public static void addNewKeyInRepository(EntityManager em, SystemEditRepositoryData data) {
		if(!JCMSSecurityManagerFactory.getManager().canPerformSystemAdministration()) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.noPermission"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return;
		}

		if(StringUtil.isStringBlank(data.getNewName())) {
			data.getMessageLogger().addFieldErrorMessage("newName", "Name must be set!");
			data.setResult(AbstractActionData.RESULT_INPUT);
			return;
		}
		if(data.getNewValue()!=null) data.setNewValue(data.getNewValue().trim());
		if(data.getNewName().equals("academicYear")) {
			if(!StringUtil.isStringBlank(data.getNewValue())) {
				try {
					int a = Integer.parseInt(data.getNewValue().substring(0,4));
					int b = Integer.parseInt(data.getNewValue().substring(5,9));
					if(a+1!=b || data.getNewValue().charAt(4)!='/') throw new Exception();
				} catch(Exception ex) {
					data.getMessageLogger().addFieldErrorMessage("newName", "Missformatted academic year. Use xxxx/yyyy.");
					data.setResult(AbstractActionData.RESULT_INPUT);
					return;
				}
			}
		}
		if(data.getNewName().equals("currentSemester")) {
			if(!StringUtil.isStringBlank(data.getNewValue())) {
				if(data.getNewValue().length()!=5) {
					data.getMessageLogger().addFieldErrorMessage("newName", "Missformatted currentSemester. Format is ddddS (e.g. 2007L).");
					data.setResult(AbstractActionData.RESULT_INPUT);
					return;
				}
				if(!Character.isDigit(data.getNewValue().charAt(0)) || !Character.isDigit(data.getNewValue().charAt(1)) || !Character.isDigit(data.getNewValue().charAt(2)) || !Character.isDigit(data.getNewValue().charAt(3)) || !Character.isUpperCase(data.getNewValue().charAt(4))) {
					data.getMessageLogger().addFieldErrorMessage("newName", "Missformatted currentSemester. Format is ddddS (e.g. 2007L).");
					data.setResult(AbstractActionData.RESULT_INPUT);
					return;
				}
			}
		}
		if(data.getNewName().equals("miScheduleParam")) {
			if(!StringUtil.isStringBlank(data.getNewValue())) {
				if(data.getNewValue().length()!=5) {
					data.getMessageLogger().addFieldErrorMessage("newName", "Missformatted miScheduleParam. Format is ddddS (e.g. 2007L).");
					data.setResult(AbstractActionData.RESULT_INPUT);
					return;
				}
				if(!Character.isDigit(data.getNewValue().charAt(0)) || !Character.isDigit(data.getNewValue().charAt(1)) || !Character.isDigit(data.getNewValue().charAt(2)) || !Character.isDigit(data.getNewValue().charAt(3)) || !Character.isUpperCase(data.getNewValue().charAt(4))) {
					data.getMessageLogger().addFieldErrorMessage("newName", "Missformatted miScheduleParam. Format is ddddS (e.g. 2007L).");
					data.setResult(AbstractActionData.RESULT_INPUT);
					return;
				}
			}
		}
		if(data.getNewName().equals("miScheduleParamDate")) {
			if(!StringUtil.isStringBlank(data.getNewValue())) {
				if(!StringUtil.checkStandardDateTimeFullFormat(data.getNewValue())) {
					data.getMessageLogger().addFieldErrorMessage("newName", "Missformatted academic year. Use xxxx/yyyy.");
					data.setResult(AbstractActionData.RESULT_INPUT);
					return;
				}
			}
		}

		if(data.getNewName().equals("marketPlace")) {
			if(!StringUtil.isStringBlank(data.getNewValue())) {
				if(!data.getNewValue().equals("yes") && !data.getNewValue().equals("no")) {
					data.getMessageLogger().addFieldErrorMessage("newName", "Variable marketPlace must have value 'yes' or 'no'.");
					data.setResult(AbstractActionData.RESULT_INPUT);
					return;
				}
			}
		}
		
		addOrUpdateKeyValue(em, data.getNewName(), data.getNewValue()!=null && data.getNewValue().length()==0 ? null : data.getNewValue());

		data.getMessageLogger().addInfoMessage(data.getMessageLogger().getText("Info.dataSuccessfullyUpdated"));
		data.setResult(AbstractActionData.RESULT_SUCCESS);
	}
	
	public static void addOrUpdateKeyValue(EntityManager em, String name, String newValue) {
		DAOHelper dh = DAOHelperFactory.getDAOHelper();
		KeyValue kv = dh.getKeyValueDAO().get(em, name);
		if(kv==null) {
			kv = new KeyValue(name, newValue);
			dh.getKeyValueDAO().save(em, kv);
		} else {
			kv.setValue(newValue);
		}
	}

	private static KeyValueBean findKeyValueBean(List<KeyValueBean> keyValueBeans, String name) {
		for(KeyValueBean kvb : keyValueBeans) {
			if(name.equals(kvb.getKey())) return kvb;
		}
		return null;
	}
}
