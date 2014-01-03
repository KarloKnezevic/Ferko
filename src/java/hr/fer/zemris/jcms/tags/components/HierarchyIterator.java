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

public class HierarchyIterator extends Component {

	private static final Log LOG = LogFactory.getLog(HierarchyIterator.class);

	public HierarchyIterator(ValueStack stack) {
		super(stack);
	}

    private String value;
    private String status;
    private HierStatus hierStatus;
    private String childGetter;
    private String itemGetter;
    private boolean itemsFirst;
    
    public void setItemsFirst(boolean itemsFirst) {
		this.itemsFirst = itemsFirst;
	}

    public void setChildGetter(String childGetter) {
		this.childGetter = childGetter;
	}
    
    public void setItemGetter(String itemGetter) {
		this.itemGetter = itemGetter;
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
    	hierStatus.setRootList(value, childGetter, itemGetter, itemsFirst);
    	
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
    	public void setRootList(Collection rootCollection, String childGetter, String itemGetter, boolean itemsFirst) {
    		String childGetterMethodName = "get"+Character.toUpperCase(childGetter.charAt(0))+childGetter.substring(1);
    		String itemGetterMethodName = "get"+Character.toUpperCase(itemGetter.charAt(0))+itemGetter.substring(1);
			if(rootCollection.isEmpty()) return;
			recursiveAdd(rootCollection, childGetterMethodName, itemGetterMethodName, itemsFirst);
			iterator = events.iterator();
		}

    	@SuppressWarnings("unchecked")
		private void addItems(String itemGetterMethodName, Object o) {
    		try {
				Method m = o.getClass().getMethod(itemGetterMethodName, (Class<?>[])null);
				Collection children = (Collection)m.invoke(o, (Object[])null);
				for(Object i : children) {
					events.add(new HierStatusItem(i, 2));
				}
    		} catch(Exception ex) {
    			LOG.error("Greska kod dohvata/obilaska itema.", ex);
    		}
		}

    	@SuppressWarnings("unchecked")
		private void recursiveAdd(Collection l, String childGetterMethodName, String itemGetterMethodName, boolean itemsFirst) {
			if(l.isEmpty()) return;
			for(Object o : l) {
				events.add(new HierStatusItem(o, 0));
				if(itemsFirst) {
					addItems(itemGetterMethodName, o);
				}
				try {
					Method m = o.getClass().getMethod(childGetterMethodName, (Class<?>[])null);
					Collection children = (Collection)m.invoke(o, (Object[])null);
					recursiveAdd(children,childGetterMethodName, itemGetterMethodName, itemsFirst);
				} catch(Exception ex) {
	    			LOG.error("Greska kod dohvata/obilaska djece.", ex);
				}
				if(!itemsFirst) {
					addItems(itemGetterMethodName, o);
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
