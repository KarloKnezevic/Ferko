package hr.fer.zemris.jcms.beans.wiki1;

public enum WikiNodeType {
	ROOT_ELEMENT,// Vršni element
	SIMPLE_TEXT, // Običan tekst
	PARAGRAPH,   // Paragraf
	HEADING,     // Naslov
	STYLE,       // Element koji definira stil: npr. bold, italic, bold-italic, strike-through, underline i sl.
	LISTITEM,    // Element liste
	LIST,        // Lista
	LINK,        // Link
	EXTERNAL_PROBLEMS_LIST // Lista pokušaja rješavanja nekog zadatka
}
