package hr.fer.zemris.jcms.web.actions.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hr.fer.zemris.jcms.beans.wiki1.WikiNode;
import hr.fer.zemris.jcms.model.WikiPage;
import hr.fer.zemris.jcms.service2.course.wiki.CourseWikiUtil;
import hr.fer.zemris.jcms.service2.course.wiki.HtmlWikiRenderer;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;
import hr.fer.zemris.jcms.web.actions2.course.wiki.CourseWiki;

/**
 * Podatkovna struktura za akciju {@link CourseWiki}.
 *  
 * @author marcupic
 *
 */
public class CourseWikiData extends BaseCourseInstance {

	private String urlBase;
	private String courseInstanceID;
	private String pageURL;
	private List<String> pageComponents = Collections.emptyList();
	private WikiNode rootWikiNode;
	private HtmlWikiRenderer wikiRenderer;
	private Map<Object, Object> wikiContext = new HashMap<Object, Object>();
	private boolean editingEnabled;
	private boolean editorMode;
	
	private String treeRendering;

	// Editing support
	private String version;
	private String content;
	private WikiPage wikiPage;
	private boolean navigationDisabled;
	
	/**
	 * Konstruktor.
	 * @param messageLogger lokalizirane poruke
	 */
	public CourseWikiData(IMessageLogger messageLogger) {
		super(messageLogger);
	}

	public boolean isNavigationDisabled() {
		return navigationDisabled;
	}
	public void setNavigationDisabled(boolean navigationDisabled) {
		this.navigationDisabled = navigationDisabled;
	}
	
	public boolean isEditingEnabled() {
		return editingEnabled;
	}
	public void setEditingEnabled(boolean editingEnabled) {
		this.editingEnabled = editingEnabled;
	}
	
	public boolean isEditorMode() {
		return editorMode;
	}
	public void setEditorMode(boolean editorMode) {
		this.editorMode = editorMode;
	}
	
	public String getUrlBase() {
		return urlBase;
	}
	
	public void setUrlBase(String urlBase) {
		this.urlBase = urlBase;
	}
	
	public String getCourseInstanceID() {
		return courseInstanceID;
	}

	public void setCourseInstanceID(String courseInstanceID) {
		this.courseInstanceID = courseInstanceID;
	}
	
	public String getPageURL() {
		return pageURL;
	}
	
	public void setPageURL(String pageURL) {
		this.pageURL = pageURL;
		this.pageComponents = CourseWikiUtil.parsePageURL(this.pageURL);
	}
	
	public List<String> getPageComponents() {
		return pageComponents;
	}
	
	public WikiNode getRootWikiNode() {
		return rootWikiNode;
	}
	
	public void setRootWikiNode(WikiNode rootWikiNode) {
		this.rootWikiNode = rootWikiNode;
	}

	public HtmlWikiRenderer getWikiRenderer() {
		return wikiRenderer;
	}
	
	public void setWikiRenderer(HtmlWikiRenderer wikiRenderer) {
		this.wikiRenderer = wikiRenderer;
	}

	public String getWiki() {
		if(treeRendering==null) {
			treeRendering = getWikiRenderer().render(getRootWikiNode());
		}
		return treeRendering;
	}
	
	public Map<Object, Object> getWikiContext() {
		return wikiContext;
	}
	
	public String getVersion() {
		return version;
	}
	public void setVersion(String version) {
		this.version = version;
	}
	
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	
	public WikiPage getWikiPage() {
		return wikiPage;
	}
	public void setWikiPage(WikiPage wikiPage) {
		this.wikiPage = wikiPage;
	}
	
}
