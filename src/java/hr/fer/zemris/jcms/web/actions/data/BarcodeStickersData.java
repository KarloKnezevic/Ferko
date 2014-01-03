package hr.fer.zemris.jcms.web.actions.data;

import hr.fer.zemris.jcms.web.actions.BarcodeStickers;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

/**
 * Podatkovna struktura za akciju {@link BarcodeStickers}.
 *  
 * @author marcupic
 *
 */
public class BarcodeStickersData extends BaseCourseInstance {
	
	/**
	 * Konstruktor.
	 * @param messageLogger lokalizirane poruke
	 */
	public BarcodeStickersData(IMessageLogger messageLogger) {
		super(messageLogger);
	}
}
