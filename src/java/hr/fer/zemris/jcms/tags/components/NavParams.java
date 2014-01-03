package hr.fer.zemris.jcms.tags.components;

import hr.fer.zemris.jcms.web.navig.NavigationItem;
import hr.fer.zemris.jcms.web.navig.NavigationItemParameter;

import java.io.Writer;
import org.apache.struts2.StrutsException;
import org.apache.struts2.components.Component;

import com.opensymphony.xwork2.util.ValueStack;

public class NavParams extends Component {

	private String item;
	
    public NavParams(ValueStack stack) {
        super(stack);
    }

    public void setItem(String item) {
		this.item = item;
	}
    
    public String getItem() {
		return item;
	}
    
    public boolean end(Writer writer, String body) {
        Component component = findAncestor(Component.class);
        Object value = findValue(this.item);
        if (value == null) {
            throw new StrutsException("No value found for following expression: " + this.item);
        }
        if(!(value instanceof NavigationItem)) {
            throw new StrutsException("Expression: " + this.item + "does not result in NavigationItem.");
        }
        for(NavigationItemParameter p : ((NavigationItem)value).getParameters()) {
            component.addParameter(p.getName(), p.getValue());
        }

        return super.end(writer, "");
    }

    public boolean usesBody() {
        return false;
    }
}
