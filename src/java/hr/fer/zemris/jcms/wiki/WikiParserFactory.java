package hr.fer.zemris.jcms.wiki;

/**
 * Služi za dohvaćanje primjerka parsera za Wiki markup. 
 */
public class WikiParserFactory {
	
	private static IWikiParser parser;
	
	public static void init() {
		parser = new YsWikiParser();
	}
	
	/**
	 * @return Primjerak parsera za Wiki markup.
	 */
	public static IWikiParser getWikiParser() {
		return parser;
	}

}
