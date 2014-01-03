package hr.fer.zemris.jcms.beans.wiki1;

import java.util.Map;

public class LinkWikiNode extends WikiNode {

	private Map<String,String> attributes;
	
	public LinkWikiNode(Map<String,String> attributes) {
		super(WikiNodeType.LINK);
		this.attributes = attributes;
	}

	public LinkWikiNode(WikiNode parent, Map<String,String> attributes) {
		super(WikiNodeType.LINK, parent);
		this.attributes = attributes;
	}

	public Map<String, String> getAttributes() {
		return attributes;
	}
}
