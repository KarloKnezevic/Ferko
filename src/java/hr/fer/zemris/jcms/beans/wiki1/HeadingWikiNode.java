package hr.fer.zemris.jcms.beans.wiki1;

public class HeadingWikiNode extends WikiNode {

	private int headingLevel;
	
	public HeadingWikiNode(int headingLevel) {
		super(WikiNodeType.HEADING);
		this.headingLevel = headingLevel;
	}

	public HeadingWikiNode(WikiNode parent, int headingLevel) {
		super(WikiNodeType.HEADING, parent);
		this.headingLevel = headingLevel;
	}

	public int getHeadingLevel() {
		return headingLevel;
	}
}
