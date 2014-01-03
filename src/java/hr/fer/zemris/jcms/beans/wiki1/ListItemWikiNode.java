package hr.fer.zemris.jcms.beans.wiki1;

public class ListItemWikiNode extends WikiNode {

	public ListItemWikiNode() {
		super(WikiNodeType.LISTITEM);
	}

	public ListItemWikiNode(WikiNode parent) {
		super(WikiNodeType.LISTITEM, parent);
	}
	
}
