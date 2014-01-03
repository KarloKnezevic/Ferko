package hr.fer.zemris.jcms.beans.wiki1;

import java.util.ArrayList;
import java.util.List;

public class WikiNode {
	
	private WikiNode parent;
	private List<WikiNode> children = new ArrayList<WikiNode>();
	private WikiNodeType nodeType;
	
	public WikiNode(WikiNodeType nodeType) {
		this(nodeType, null);
	}

	public WikiNode(WikiNodeType nodeType, WikiNode parent) {
		this.parent = parent;
		this.nodeType = nodeType;
		if(this.parent!=null) {
			this.parent.getChildren().add(this);
		}
	}

	public WikiNode getParent() {
		return parent;
	}
	
	public List<WikiNode> getChildren() {
		return children;
	}

	public WikiNodeType getNodeType() {
		return nodeType;
	}
}
