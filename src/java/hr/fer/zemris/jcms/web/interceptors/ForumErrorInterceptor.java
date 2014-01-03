package hr.fer.zemris.jcms.web.interceptors;

import hr.fer.zemris.jcms.exceptions.IllegalParameterException;
import hr.fer.zemris.jcms.exceptions.NotLoggedInException;
import hr.fer.zemris.jcms.web.actions.forum.AbstractAction;

import javax.persistence.NoResultException;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.AbstractInterceptor;

public class ForumErrorInterceptor extends AbstractInterceptor {

	private static final long serialVersionUID = 6712179061646745337L;

	@Override
	public String intercept(ActionInvocation invocation) throws Exception {
		Object act = invocation.getAction();
		if (act instanceof AbstractAction) {
			AbstractAction action = (AbstractAction)act;
			try {
				return invocation.invoke();
			} catch (NoResultException e) {
				action.getData().getMessageLogger().addErrorMessage("Dio foruma kojemu ste pokušali pristupiti ne postoji");
				return "showFatalMessage";
			} catch (IllegalParameterException e) {
				action.getData().getMessageLogger().addErrorMessage("Parametri nisu ispravni");
				return "showFatalMessage";
			} catch (NotLoggedInException e) {
				return "notLoggedIn";
			}
		} else
			return invocation.invoke();
	}

}
