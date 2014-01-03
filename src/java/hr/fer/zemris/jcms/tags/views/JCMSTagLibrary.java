package hr.fer.zemris.jcms.tags.views;

import hr.fer.zemris.jcms.tags.views.freemarker.tags.JCMSModels;
import hr.fer.zemris.jcms.tags.views.velocity.components.HierarchyIteratorDirective;
import hr.fer.zemris.jcms.tags.views.velocity.components.MonoHierarchyIteratorDirective;
import hr.fer.zemris.jcms.tags.views.velocity.components.NavParamsDirective;

import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.views.TagLibrary;

import com.opensymphony.xwork2.util.ValueStack;

public class JCMSTagLibrary implements TagLibrary {

	@Override
	public Object getFreemarkerModels(ValueStack stack, HttpServletRequest req, HttpServletResponse resp) {
		return new JCMSModels(stack, req, resp);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<Class> getVelocityDirectiveClasses() {
		Class[] directives = new Class[] {
				HierarchyIteratorDirective.class,
				MonoHierarchyIteratorDirective.class,
				NavParamsDirective.class
		};
		return Arrays.asList(directives);
	}

}
