package hr.fer.zemris.jcms.tags.components;

import java.io.Writer;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.struts2.components.Component;

import com.opensymphony.xwork2.util.ValueStack;

public class MonoHierarchyIterator extends Component {

	private static final Log LOG = LogFactory.getLog(MonoHierarchyIterator.class);

	public MonoHierarchyIterator(ValueStack stack) {
		super(stack);
	}

    private String value;
    private String status;
    private HierStatus hierStatus;
    private String childGetter;
    
    public void setChildGetter(String childGetter) {
		this.childGetter = childGetter;
	}
    
    public void setStatus(String status) {
		this.status = status;
	}
    
    public void setValue(String value) {
		this.value = value;
	}
    
    @SuppressWarnings("unchecked")
    public boolean start(Writer writer) {
    	if(status!=null) {
    		hierStatus = new HierStatus();
    	} else {
    		super.end(writer, "(ERR:Atribut status je obavezan)");
    		return false;
    	}
    	ValueStack stack = getStack();
    	if(value==null) {
    		value = "top";
    	}
    	Object obj = findValue(value);
    	Collection value = null;
    	if(!(obj instanceof Collection)) {
    		obj = null;
    	} else {
    		value = (Collection)obj;
    	}
    	if(value==null) {
    		super.end(writer, "");
    		return false;
    	}
    	hierStatus.setRootList(value, childGetter);
    	
    	if(hierStatus.iterator!=null && hierStatus.iterator.hasNext()) {
    		HierStatusItem currentValue = hierStatus.iterator.next();
    		stack.push(currentValue);
    		String id = getId();
    		if(id!=null && currentValue!=null) {
    			stack.getContext().put(id, currentValue);
    		}
    		stack.getContext().put(status, hierStatus);
    		return true;
    	} else {
    		super.end(writer, "");
    		return false;
    	}
    }

    @SuppressWarnings("unchecked")
	@Override
    public boolean end(Writer writer, String body) {
    	ValueStack stack = getStack();
    	if(hierStatus.iterator!=null) {
    		stack.pop();
    	}
    	if(hierStatus.iterator!=null && hierStatus.iterator.hasNext()) {
    		HierStatusItem currentValue = hierStatus.iterator.next();
    		stack.push(currentValue);
    		String id = getId();
    		if(id!=null && currentValue!=null) {
    			stack.getContext().put(id, currentValue);
    		}
    		return true;
    	} else {
    		stack.getContext().remove(status);
    		super.end(writer, "");
    		return false;
    	}
    }
    
    private static class HierStatus {
    	List<HierStatusItem> events = new ArrayList<HierStatusItem>();
    	Iterator<HierStatusItem> iterator;
    	
    	@SuppressWarnings("unchecked")
    	public void setRootList(Collection rootCollection, String childGetter) {
    		String childGetterMethodName = "get"+Character.toUpperCase(childGetter.charAt(0))+childGetter.substring(1);
			if(rootCollection.isEmpty()) return;
			recursiveAdd(rootCollection, childGetterMethodName);
			iterator = events.iterator();
		}

    	@SuppressWarnings("unchecked")
		private void recursiveAdd(Collection l, String childGetterMethodName) {
			if(l.isEmpty()) return;
			for(Object o : l) {
				events.add(new HierStatusItem(o, 0));
				try {
					Method m = o.getClass().getMethod(childGetterMethodName, (Class<?>[])null);
					Collection children = (Collection)m.invoke(o, (Object[])null);
					recursiveAdd(children,childGetterMethodName);
				} catch(Exception ex) {
	    			LOG.error("Greska kod dohvata/obilaska djece.", ex);
				}
				events.add(new HierStatusItem(o, 1));
			}
		}
    }
    
    private static class HierStatusItem {
    	Object value;
    	int kind;
    	
    	public HierStatusItem(Object value, int kind) {
    		this.value = value;
    		this.kind = kind;
    	}
    	
    	public int getKind() {
			return kind;
		}
    	
    	public Object getValue() {
			return value;
		}
    }
}
