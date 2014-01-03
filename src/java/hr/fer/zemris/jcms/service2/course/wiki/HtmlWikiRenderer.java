/**
 * 
 */
package hr.fer.zemris.jcms.service2.course.wiki;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import hr.fer.zemris.jcms.beans.IsolatedProblemInstanceBean;
import hr.fer.zemris.jcms.beans.wiki1.ExternalProblemsListWikiNode;
import hr.fer.zemris.jcms.beans.wiki1.HeadingWikiNode;
import hr.fer.zemris.jcms.beans.wiki1.LinkWikiNode;
import hr.fer.zemris.jcms.beans.wiki1.ListWikiNode;
import hr.fer.zemris.jcms.beans.wiki1.ListWikiType;
import hr.fer.zemris.jcms.beans.wiki1.StyleWikiNode;
import hr.fer.zemris.jcms.beans.wiki1.TextWikiNode;
import hr.fer.zemris.jcms.beans.wiki1.WikiNode;
import hr.fer.zemris.jcms.beans.wiki1.WikiNodeType;

public class HtmlWikiRenderer {
	
	private String wikiAction;
	private String courseInstanceID;
	private Map<Object,Object> wikiContext;
	private String urlBase;
	private String[] currentWikiPage;
	
	public Map<Object, Object> getWikiContext() {
		return wikiContext;
	}
	
	public void setWikiContext(Map<Object, Object> wikiContext) {
		this.wikiContext = wikiContext;
	}

	public String[] getCurrentWikiPage() {
		return currentWikiPage;
	}
	
	public void setCurrentWikiPage(String[] currentWikiPage) {
		this.currentWikiPage = currentWikiPage;
	}
	
	public String getUrlBase() {
		return urlBase;
	}
	
	public void setUrlBase(String urlBase) {
		this.urlBase = urlBase;
	}
	
	public void setCourseInstanceID(String courseInstanceID) {
		this.courseInstanceID = courseInstanceID;
	}
	
	public String getCourseInstanceID() {
		return courseInstanceID;
	}
	
	public void setWikiAction(String wikiAction) {
		this.wikiAction = wikiAction;
	}
	
	public String getWikiAction() {
		return wikiAction;
	}
	
	public String render(WikiNode root) {
		StringBuilder sb = new StringBuilder(1024);
		for(WikiNode child : root.getChildren()) {
			renderRecursive(sb, child);
		}
		return sb.toString();
	}

	@SuppressWarnings("unchecked")
	private void renderRecursive(StringBuilder sb, WikiNode child) {
		if(child.getNodeType()==WikiNodeType.HEADING) {
			HeadingWikiNode n = (HeadingWikiNode)child;
			sb.append("<h").append(n.getHeadingLevel()).append(">");
			if(child.getChildren().isEmpty()) {
				sb.append("&nbsp;");
			} else {
				for(WikiNode child2 : child.getChildren()) {
					renderRecursive(sb, child2);
				}
			}
			sb.append("</h").append(n.getHeadingLevel()).append(">");
			return;
		}
		if(child.getNodeType()==WikiNodeType.PARAGRAPH) {
			sb.append("<p>");
			if(child.getChildren().isEmpty()) {
				sb.append("&nbsp;");
			} else {
				for(WikiNode child2 : child.getChildren()) {
					renderRecursive(sb, child2);
				}
			}
			sb.append("</p>");
			return;
		}
		if(child.getNodeType()==WikiNodeType.EXTERNAL_PROBLEMS_LIST) {
			ExternalProblemsListWikiNode l = (ExternalProblemsListWikiNode)child;
			if(!l.getAttributes().containsKey("url")) {
				System.out.println("[WIKI:Renderer]: ExternalProblemsListWikiNode does not have url attribute.");
				return;
			}
			List<IsolatedProblemInstanceBean> list = (List<IsolatedProblemInstanceBean>)getWikiContext().get(l.getAttributes().get("url")); 
			if(list==null) {
				sb.append("Nema dohvaćenih podataka.");
				return;
			}
			sb.append("<ol>\r\n");
			for(IsolatedProblemInstanceBean bean : list) {
				sb.append("<li>\r\n");
				// Treba dati url=studtest2:pguri/configuri/ipiid, cmd=new|reopen|write, retURL=... ==> taj URL će automatski dobiti još i url=... pri redirekciji
				switch(bean.getStatus()) {
				case FINISHED:
					sb.append("Uneseno rješenje: ").append(bean.isSolved()? "DA" : "NE").append(", bodovi: ").append(bean.getCorrectnessMeasure()).append(". ").append(" <a href=\"external/StudTest2P!fetch.action?retURL=").append(urlEncode(dummyBackURL())).append("&cmd=show&url=").append(urlEncode(bean.getId())).append("\">Pogledaj zadatak</a>.");
					break;
				case NEW:
					sb.append("Započni novi zadatak: ").append(" <a href=\"external/StudTest2P!fetch.action?retURL=").append(urlEncode(dummyBackURL())).append("&cmd=new&url=").append(urlEncode(bean.getId())).append("\">link</a>.");
					break;
				case SOLVABLE:
					sb.append("Nastavi rješavanje zadatka: ").append(" <a href=\"external/StudTest2P!fetch.action?retURL=").append(urlEncode(dummyBackURL())).append("&cmd=reopen&url=").append(urlEncode(bean.getId())).append("\">link</a>.");
					break;
				case UNKNOWN:
					sb.append("Nešto nije u redu sa zadatkom.");
					break;
				}
				sb.append("</li>\r\n");
			}
			sb.append("</ol>\r\n");
			return;
		}
		if(child.getNodeType()==WikiNodeType.LINK) {
			LinkWikiNode l = (LinkWikiNode)child;
			if(!l.getAttributes().containsKey("type")) {
				System.out.println("[WIKI:Renderer]: Link does not have type attribute.");
				return;
			}
			String linkType = l.getAttributes().get("type");
			String[] pageURLParts = null;
			if(linkType.equals("external-problems/list")) {
				pageURLParts = new String[] {"external-problems","list",l.getAttributes().get("url")};
				sb.append("<a href=\"").append(getWikiAction()).append("?pageURL=").append(urlEncode(CourseWikiUtil.buildPageURL(pageURLParts))); 
				if(getCourseInstanceID()!=null) {
					sb.append("&courseInstanceID=").append(urlEncode(getCourseInstanceID()));
				}
				sb.append("\">");
				if(child.getChildren().isEmpty()) {
					sb.append("&nbsp;");
				} else {
					for(WikiNode child2 : child.getChildren()) {
						renderRecursive(sb, child2);
					}
				}
				sb.append("</a>");
				return;
			} else if(linkType.equals("external-problems/access")) {
				pageURLParts = new String[] {"external-problems","access",l.getAttributes().get("url")};
				sb.append("<a href=\"").append(getWikiAction()).append("?pageURL=").append(urlEncode(CourseWikiUtil.buildPageURL(pageURLParts))); 
				if(getCourseInstanceID()!=null) {
					sb.append("&courseInstanceID=").append(urlEncode(getCourseInstanceID()));
				}
				sb.append("\">");
				if(child.getChildren().isEmpty()) {
					sb.append("&nbsp;");
				} else {
					for(WikiNode child2 : child.getChildren()) {
						renderRecursive(sb, child2);
					}
				}
				sb.append("</a>");
				return;
			} else if(linkType.equals("page")) {
				sb.append("<a href=\"").append(getWikiAction()).append("?pageURL=").append(urlEncode(l.getAttributes().get("url")));
				if(getCourseInstanceID()!=null) {
					sb.append("&courseInstanceID=").append(urlEncode(getCourseInstanceID()));
				}
				sb.append("\">");
				String info = l.getAttributes().get("info");
				sb.append(info==null || info.isEmpty() ? "EMPTY" : info);
				sb.append("</a>");
				return;
			} else if(linkType.equals("url")) {
				sb.append("<a href=\"").append(l.getAttributes().get("url"));
				sb.append("\" target=\"_blank\">");
				String info = l.getAttributes().get("info");
				if(info==null || info.isEmpty()) {
					for(WikiNode child2 : child.getChildren()) {
						renderRecursive(sb, child2);
					}
				} else {
					sb.append(info);
				}
				sb.append("</a>");
				return;
			} else {
				System.out.println("[WIKI:Renderer]: Unknown link type: "+linkType+".");
				return;
			}
//			sb.append("<link>");
//			if(child.getChildren().isEmpty()) {
//				sb.append("&nbsp;");
//			} else {
//				for(WikiNode child2 : child.getChildren()) {
//					renderRecursive(sb, child2);
//				}
//			}
//			sb.append("</link>");
//			return;
		}
		if(child.getNodeType()==WikiNodeType.STYLE) {
			StyleWikiNode swn = (StyleWikiNode)child;
			switch(swn.getStyleType()) {
			case BOLD: sb.append("<b>"); break;
			case ITALIC: sb.append("<i>"); break;
			case BOLD_ITALIC: sb.append("<b><i>"); break;
			case UNDERLINE: sb.append("<u>"); break;
			case STRIKETHROUGH: sb.append("<strike>"); break;
			}
			if(child.getChildren().isEmpty()) {
				sb.append("&nbsp;");
			} else {
				for(WikiNode child2 : child.getChildren()) {
					renderRecursive(sb, child2);
				}
			}
			switch(swn.getStyleType()) {
			case BOLD: sb.append("</b>"); break;
			case ITALIC: sb.append("</i>"); break;
			case BOLD_ITALIC: sb.append("</i></b>"); break;
			case UNDERLINE: sb.append("</u>"); break;
			case STRIKETHROUGH: sb.append("</strike>"); break;
			}
			return;
		}
		if(child.getNodeType()==WikiNodeType.LIST) {
			ListWikiNode n = (ListWikiNode)child;
			if(n.getListType()==ListWikiType.UNNUMBERED) {
				sb.append("<ul>");
			} else {
				if(n.getItemsType()==null) {
					sb.append("<ol>");
				} else {
					sb.append("<ol type=\"").append(n.getItemsType()).append("\">");
				}
			}
			if(child.getChildren().isEmpty()) {
				sb.append("&nbsp;");
			} else {
				for(WikiNode child2 : child.getChildren()) {
					renderRecursive(sb, child2);
				}
			}
			if(n.getListType()==ListWikiType.UNNUMBERED) {
				sb.append("</ul>");
			} else {
				sb.append("</ol>");
			}
			return;
		}
		if(child.getNodeType()==WikiNodeType.LISTITEM) {
			sb.append("<li>");
			if(child.getChildren().isEmpty()) {
				sb.append("&nbsp;");
			} else {
				for(WikiNode child2 : child.getChildren()) {
					renderRecursive(sb, child2);
				}
			}
			sb.append("</li>");
			return;
		}
		if(child.getNodeType()==WikiNodeType.SIMPLE_TEXT) {
			TextWikiNode n = (TextWikiNode)child;
			sb.append(n.getText());
			return;
		}
		// Inače ignoriramo taj element
	}

	private String dummyBackURL() {
		return getUrlBase()+"CourseWiki.action?pageURL="+urlEncode(CourseWikiUtil.buildPageURL(getCurrentWikiPage()))+"&courseInstanceID="+urlEncode(getCourseInstanceID());
	}

	@SuppressWarnings("deprecation")
	private Object urlEncode(String url) {
		try {
			return URLEncoder.encode(url, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			return URLEncoder.encode(url);
		}
	}
}