package hr.fer.zemris.jcms.beans.wiki1;

public class TextWikiNode extends WikiNode {

	private String text;
	
	public TextWikiNode(String text) {
		super(WikiNodeType.SIMPLE_TEXT);
		this.text = text;
	}

	public TextWikiNode(WikiNode parent, String text) {
		super(WikiNodeType.SIMPLE_TEXT, parent);
		this.text = text;
	}

	public String getText() {
		return text;
	}
}
