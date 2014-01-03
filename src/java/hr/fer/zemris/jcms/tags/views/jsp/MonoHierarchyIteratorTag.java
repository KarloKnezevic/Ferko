package hr.fer.zemris.jcms.tags.views.jsp;

import hr.fer.zemris.jcms.tags.components.MonoHierarchyIterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspException;

import org.apache.struts2.components.Component;
import org.apache.struts2.views.jsp.ComponentTagSupport;

import com.opensymphony.xwork2.util.ValueStack;

public class MonoHierarchyIteratorTag extends ComponentTagSupport {

	private static final long serialVersionUID = 1L;

    private String value;
    private String status;
    private String childGetter;
    
	@Override
	public Component getBean(ValueStack stack, HttpServletRequest req,
			HttpServletResponse resp) {
		return new MonoHierarchyIterator(stack);
	}

	@Override
	protected void populateParams() {
		super.populateParams();
		MonoHierarchyIterator tag = (MonoHierarchyIterator)component;
		tag.setValue(value);
		tag.setStatus(status);
		tag.setChildGetter(childGetter);
	}
	
	public void setChildGetter(String childGetter) {
		this.childGetter = childGetter;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public void setStatus(String status) {
		this.status = status;
	}
	
	public int doEndTag() throws JspException {
		component = null;
		return EVAL_PAGE;
	}
	
    public int doAfterBody() throws JspException {
        boolean again = component.end(pageContext.getOut(), getBody());

        if (again) {
            return EVAL_BODY_AGAIN;
        } else {
            if (bodyContent != null) {
                try {
                    bodyContent.writeOut(bodyContent.getEnclosingWriter());
                } catch (Exception e) {
                    throw new JspException(e.getMessage());
                }
            }
            return SKIP_BODY;
        }
    }

}
