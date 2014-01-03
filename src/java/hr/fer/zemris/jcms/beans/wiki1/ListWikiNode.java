package hr.fer.zemris.jcms.beans.wiki1;

public class ListWikiNode extends WikiNode {

	private ListWikiType listType;
	private int indentation;
	private String itemsType;
	
	public ListWikiNode(ListWikiType listType, int indentation, String itemsType) {
		super(WikiNodeType.LIST);
		this.listType = listType;
		this.indentation = indentation;
	}

	public ListWikiNode(WikiNode parent, ListWikiType listType, int indentation, String itemsType) {
		super(WikiNodeType.LIST, parent);
		this.listType = listType;
		this.indentation = indentation;
		this.itemsType = itemsType;
	}

	public ListWikiType getListType() {
		return listType;
	}
	
	public int getIndentation() {
		return indentation;
	}

	public String getItemsType() {
		return itemsType;
	}
}
