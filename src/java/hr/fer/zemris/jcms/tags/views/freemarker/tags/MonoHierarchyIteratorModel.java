package hr.fer.zemris.jcms.tags.views.freemarker.tags;

import hr.fer.zemris.jcms.tags.components.MonoHierarchyIterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.components.Component;
import org.apache.struts2.views.freemarker.tags.TagModel;

import com.opensymphony.xwork2.util.ValueStack;

public class MonoHierarchyIteratorModel extends TagModel {

	public MonoHierarchyIteratorModel(ValueStack stack, HttpServletRequest req, HttpServletResponse res) {
		super(stack, req, res);
	}

	@Override
	protected Component getBean() {
		return new MonoHierarchyIterator(stack);
	}

}
