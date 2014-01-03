package hr.fer.zemris.jcms.beans.wiki1;

public class ParagraphWikiNode extends WikiNode {

	public ParagraphWikiNode() {
		super(WikiNodeType.PARAGRAPH);
	}

	public ParagraphWikiNode(WikiNode parent) {
		super(WikiNodeType.PARAGRAPH, parent);
	}
	
}
