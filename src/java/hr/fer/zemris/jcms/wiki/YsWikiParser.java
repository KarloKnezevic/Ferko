package hr.fer.zemris.jcms.wiki;

import ys.wikiparser.WikiParser;;

/**
 * Wrapper za <a href="http://web-tec.info/WikiParser">T4 WikiParser</a>.
 */
public class YsWikiParser implements IWikiParser  {
	
	@Override
	public String parseToXHTML(String wikiText) {
		return WikiParser.renderXHTML(wikiText); 
	}

}
