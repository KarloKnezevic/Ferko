package hr.fer.zemris.jcms.parsers;

/**
 * Iterator po stringu s delimiterima.<br />
 * Za string:<br />
 * <i>I've had a perfectly wonderful evening. But this wasn't it.</i><br />
 * i delimiter " "<br />
 * Iterator.next() će redom vraćati:<br />
 * <ul>
 * 	<li>I've</li>
 * 	<li>had</li>
 * 	<li>a</li>
 * 	<li>...</li>
 *</ul>
 *
 * @author Ivan Krišto
 */
public class ChoiceAnswersIterator {
	
	/** String nad kojim se iterira. */
	private String sourceString;
	
	/** Delimiter kojim su odvojene cjeline stringa. */
	private String delimiter;
	
	/** Pozicija na kojoj smo stali s iteriranjem. */
	private int currentPos;
	
	/**
	 * Konstruktor.
	 * 
	 * @param sourceString String nad kojim se iterira.
	 * @param delimiter Delimiter kojim su odvojene cjeline stringa.
	 */
	public ChoiceAnswersIterator(String sourceString, String delimiter) {
		this.sourceString = sourceString;
		this.delimiter = delimiter;
		this.currentPos = 0;
	}
	
	/**
	 * Konstruktor.
	 * 
	 * @param sourceString String nad kojim se iterira.
	 * @param delimiter Delimiter kojim su odvojene cjeline stringa.
	 */
	public ChoiceAnswersIterator(String sourceString, char delimiter) {
		this.sourceString = sourceString;
		this.delimiter = String.valueOf(delimiter);
		this.currentPos = 0;
	}
	
	/**
	 * Iteriranje stringom.
	 * 
	 * @return Sljedeća cjelina stringa između delimitera ili <code>null</code> ako smo prošli cijeli string. 
	 */
	public String next() {
		if (this.sourceString.length() + 1 == this.currentPos) {
			return null;
		}
		
		int index = this.sourceString.indexOf(this.delimiter, this.currentPos);
		
		if (index == -1) {
			index = this.sourceString.length();
		}
		
		String ret = this.sourceString.substring(this.currentPos, index);
		this.currentPos = index + 1;
		return ret;
	}
	
	/**
	 * Iterira po stringu i provjerava sadrži li novodobiveni podstring odgovor val unutar sebe.
	 * 
	 * @param val Vrijednost koju tražimo unutar novodobivenog podstringa.
	 * @return True ako se vrijednost pojavljuje, inače false.
	 */
	public boolean nextAndTest(String val) {
		String nextStr = next();
		
		if (nextStr == null) {
			return false;
			
		} else if (nextStr.indexOf(val) != -1) {
			return true;
			
		} else {
			return false;
			
		}
	}
}
