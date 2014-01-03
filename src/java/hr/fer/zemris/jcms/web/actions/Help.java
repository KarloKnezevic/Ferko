package hr.fer.zemris.jcms.web.actions;

public class Help extends ExtendedActionSupport {

	private static final long serialVersionUID = 1L;
	private String helpKey;
	private static char[] trimChars = new char[] {'\\','/','\'','\"',':','&','%'};
	
    public String execute() throws Exception {
    	if(helpKey!=null) {
    		for(int i = 0; i < trimChars.length; i++) {
        		int p = helpKey.lastIndexOf(trimChars[i]);
        		if(p!=-1) {
        			helpKey = helpKey.substring(p+1);
        		}
    		}
    		helpKey = helpKey.trim();
    		if(helpKey.length()==0) {
    			helpKey = null;
    		}
    	}
		return SUCCESS;
    }

    public String getHelpKey() {
		return helpKey;
	}
    
    public void setHelpKey(String helpKey) {
		this.helpKey = helpKey;
	}
}
