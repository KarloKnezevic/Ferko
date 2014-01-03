package hr.fer.zemris.jcms.web.actions2.course.wiki;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.StrutsStatics;

import com.opensymphony.xwork2.ActionContext;

import hr.fer.zemris.jcms.service2.course.wiki.CourseWikiService;

import hr.fer.zemris.jcms.web.actions.Ext2ActionSupport;
import hr.fer.zemris.jcms.web.actions.annotations.DataResultMapping;
import hr.fer.zemris.jcms.web.actions.annotations.Struts2ResultMapping;
import hr.fer.zemris.jcms.web.actions.annotations.WebClass;
import hr.fer.zemris.jcms.web.actions.annotations.WebMethodInfo;
import hr.fer.zemris.jcms.web.actions.data.CourseWikiData;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.navig.DefaultNavigationBuilder;

/**
 * Akcija koja popunjava wiki stranicu kolegija.
 * 
 * @author marcupic
 */
@WebClass(dataClass=CourseWikiData.class)
public class CourseWiki extends Ext2ActionSupport<CourseWikiData> {

	private static final long serialVersionUID = 2L;

	@WebMethodInfo
	public String edit() throws Exception {
		CourseWikiService.getCourseWikiEditingData(getEntityManager(), data);
		return null;
	}
	
	@WebMethodInfo(
		dataResultMappings={@DataResultMapping(dataResult=AbstractActionData.RESULT_SUCCESS,struts2Result="redirect",registerDelayedMessages=true)},
		struts2ResultMappings={@Struts2ResultMapping(struts2Result="redirect",navigBuilder=DefaultNavigationBuilder.class,navigBuilderIsRoot=true)}
	)
	public String save() throws Exception {
		CourseWikiService.saveCourseWikiData(getEntityManager(), data);
		return null;
	}
	
	@WebMethodInfo
    public String execute() throws Exception {

    	ActionContext context = ActionContext.getContext();
		HttpServletRequest request = (HttpServletRequest) context.get(StrutsStatics.HTTP_REQUEST);
		String url = request.getRequestURI();
		int pos = url.lastIndexOf('/');
		url = url.substring(0, pos+1);
		if(!url.startsWith("http:") && !url.startsWith("https:")) {
			String scheme = request.getScheme();
			String sname = request.getServerName();
			int port = request.getServerPort();
			if(scheme.equals("http")) {
				if(port==80) {
					url = "http://"+sname+url;
				} else {
					url = "http://"+sname+":"+port+url;
				}
			} else if(scheme.equals("https")) {
				if(port==443) {
					url = "https://"+sname+url;
				} else {
					url = "https://"+sname+":"+port+url;
				}
			}
		}
		data.setUrlBase(url);
		CourseWikiService.getCourseWikiData(getEntityManager(), data);
		return null;
    }
	
    /**
     * Geter identifikatora kolegija. Uočimo da je to zapravo delegat.
     * 
     * @return identifikator kolegija
     */
    public String getCourseInstanceID() {
		return data.getCourseInstanceID();
	}
    
    /**
     * Seter identifikatora kolegija. Uočimo da je to zapravo delegat.
     * 
     * @param courseInstanceID identifikator kolegija
     */
    public void setCourseInstanceID(String courseInstanceID) {
		data.setCourseInstanceID(courseInstanceID);
	}
    
	public String getPageURL() {
		return data.getPageURL();
	}
	
	public void setPageURL(String pageURL) {
		data.setPageURL(pageURL);
	}

	public void setUrl(String url) {
		// Setter za callback; potreban za "mete" povratka iz prikaza zadataka
	}
}
