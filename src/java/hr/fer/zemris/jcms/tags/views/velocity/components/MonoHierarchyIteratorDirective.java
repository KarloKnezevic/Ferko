package hr.fer.zemris.jcms.tags.views.velocity.components;

import hr.fer.zemris.jcms.tags.components.MonoHierarchyIterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.components.Component;
import org.apache.struts2.views.velocity.components.AbstractDirective;

import com.opensymphony.xwork2.util.ValueStack;

public class MonoHierarchyIteratorDirective extends AbstractDirective {

	@Override
	protected Component getBean(ValueStack stack, HttpServletRequest req,
			HttpServletResponse resp) {
		return new MonoHierarchyIterator(stack);
	}

	@Override
	public String getBeanName() {
		return "monoHierarchyIterator";
	}

}
