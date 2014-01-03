package hr.fer.zemris.jcms.tags.views.jsp;

import hr.fer.zemris.jcms.tags.components.NavParams;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts2.components.Component;
import org.apache.struts2.views.jsp.ComponentTagSupport;

import com.opensymphony.xwork2.util.ValueStack;

public class NavParamsTag extends ComponentTagSupport {

	private static final long serialVersionUID = 1L;

    private String item;
    
	@Override
	public Component getBean(ValueStack stack, HttpServletRequest req,
			HttpServletResponse resp) {
		return new NavParams(stack);
	}

	@Override
	protected void populateParams() {
		super.populateParams();
		NavParams tag = (NavParams)component;
		tag.setItem(item);
	}
	
	public void setItem(String item) {
		this.item = item;
	}
}
