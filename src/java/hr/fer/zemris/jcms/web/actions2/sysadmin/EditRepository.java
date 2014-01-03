package hr.fer.zemris.jcms.web.actions2.sysadmin;

import hr.fer.zemris.jcms.beans.KeyValueBean;

import hr.fer.zemris.jcms.service2.sysadmin.SystemRepositoryService;
import hr.fer.zemris.jcms.web.actions.Ext2ActionSupport;
import hr.fer.zemris.jcms.web.actions.annotations.WebClass;
import hr.fer.zemris.jcms.web.actions.annotations.WebMethodInfo;
import hr.fer.zemris.jcms.web.actions.data.SystemEditRepositoryData;
import hr.fer.zemris.jcms.web.navig.builders.MainBuilder;

import java.util.List;

@WebClass(dataClass=SystemEditRepositoryData.class,defaultNavigBuilder=MainBuilder.class, defaultNavigBuilderIsRoot=false,additionalMenuItems={"m2","Navigation.editRepository"})
public class EditRepository extends Ext2ActionSupport<SystemEditRepositoryData> {

	private static final long serialVersionUID = 1L;
		
	@WebMethodInfo
	public String execute() throws Exception {
		SystemRepositoryService.updateRepository(getEntityManager(), data);
		return null;
	}

	@WebMethodInfo
	public String input() throws Exception {
		SystemRepositoryService.prepareView(getEntityManager(), data);
		return null;
	}

	@WebMethodInfo
	public String addNew() throws Exception {
		SystemRepositoryService.addNewKeyInRepository(getEntityManager(), data);
		return null;
	}
	
	public List<KeyValueBean> getRepository() {
		return data.getRepository();
	}
	public void setRepository(List<KeyValueBean> repository) {
		data.setRepository(repository);
	}

	public String getNewName() {
		return data.getNewName();
	}
	public void setNewName(String newName) {
		data.setNewName(newName);
	}

	public String getNewValue() {
		return data.getNewValue();
	}
	public void setNewValue(String newValue) {
		data.setNewValue(newValue);
	}
}
