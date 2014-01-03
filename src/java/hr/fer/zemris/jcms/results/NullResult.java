package hr.fer.zemris.jcms.results;

import org.apache.struts2.dispatcher.StrutsResultSupport;

import com.opensymphony.xwork2.ActionInvocation;

public class NullResult extends StrutsResultSupport {

	private static final long serialVersionUID = 1L;

	@Override
	protected void doExecute(String finalLocation, ActionInvocation invocation) throws Exception {
		// do nothing since the result has already been generated 
	}

}
