package hr.fer.zemris.jcms.wiki;

/**
 * Sučelje Wiki parsera. Definira sve moguće izlazne formate.
 */
public interface IWikiParser {
	
	/**
	 * Parsira predani tekst s Wiki markupom u XHTML.
	 */
	public String parseToXHTML(String wikiText);

}
