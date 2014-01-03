package hr.fer.zemris.jcms.web.actions2.course;

import hr.fer.zemris.jcms.service2.course.Seminars;
import hr.fer.zemris.jcms.web.actions.Ext2ActionSupport;
import hr.fer.zemris.jcms.web.actions.annotations.WebClass;
import hr.fer.zemris.jcms.web.actions.annotations.WebMethodInfo;
import hr.fer.zemris.jcms.web.actions.data.ViewSeminarInfoData;
import hr.fer.zemris.jcms.web.navig.builders.MainBuilder;

@WebClass(dataClass=ViewSeminarInfoData.class,defaultNavigBuilder=MainBuilder.class, defaultNavigBuilderIsRoot=false,additionalMenuItems={"m2","Navigation.viewSeminarInfo"})
public class ViewSeminarInfo extends Ext2ActionSupport<ViewSeminarInfoData> {

	private static final long serialVersionUID = 1L;
		
	@WebMethodInfo
	public String execute() throws Exception {
		Seminars.fetchSeminarInfoData(getEntityManager(), data);
		return null;
	}

	public Long getId() {
		return data.getId();
	}
	public void setId(Long id) {
		data.setId(id);
	}

}
