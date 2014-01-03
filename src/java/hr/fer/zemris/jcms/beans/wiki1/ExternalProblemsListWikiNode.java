package hr.fer.zemris.jcms.beans.wiki1;

import java.util.Map;

public class ExternalProblemsListWikiNode extends WikiNode {

	private Map<String,String> attributes;
	
	public ExternalProblemsListWikiNode(Map<String,String> attributes) {
		super(WikiNodeType.EXTERNAL_PROBLEMS_LIST);
		this.attributes = attributes;
	}

	public ExternalProblemsListWikiNode(WikiNode parent, Map<String,String> attributes) {
		super(WikiNodeType.EXTERNAL_PROBLEMS_LIST, parent);
		this.attributes = attributes;
	}

	public Map<String, String> getAttributes() {
		return attributes;
	}
}
