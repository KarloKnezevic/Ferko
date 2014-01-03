package hr.fer.zemris.jcms.web.actions;

import hr.fer.zemris.jcms.service.PrepareService;

public class Prepare extends ExtendedActionSupport {

	private static final long serialVersionUID = 2L;

    public String execute() throws Exception {
    	
    	try {
    		PrepareService.prepare();
        	setMessage(getText(MESSAGE) + "OK.");
    	} catch(RuntimeException ex) {
        	setMessage(getText(MESSAGE) + ex.getMessage());
    		ex.printStackTrace();
    	}
        return SUCCESS;
    }

    /**
     * Provide default value for Message property.
     */
    public static final String MESSAGE = "Prepare.message";

    /**
     * Field for Message property.
     */
    private String message;

    /**
     * Return Message property.
     *
     * @return Message property
     */
    public String getMessage() {
        return message;
    }

    /**
     * Set Message property.
     *
     * @param message Text to display on HelloWorld page.
     */
    public void setMessage(String message) {
        this.message = message;
    }

}
