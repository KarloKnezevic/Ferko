package hr.fer.zemris.jcms.beans.wiki1;

public class StyleWikiNode extends WikiNode {

	private StyleWikiType styleType;
	
	public StyleWikiNode(StyleWikiType styleType) {
		super(WikiNodeType.STYLE);
		this.styleType = styleType;
	}

	public StyleWikiNode(WikiNode parent, StyleWikiType styleType) {
		super(WikiNodeType.STYLE, parent);
		this.styleType = styleType;
	}

	public StyleWikiType getStyleType() {
		return styleType;
	}
}
