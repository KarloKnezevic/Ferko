package hr.fer.zemris.jcms.tags.views.freemarker.tags;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.opensymphony.xwork2.util.ValueStack;

public class JCMSModels {
    private ValueStack stack;
    private HttpServletRequest req;
    private HttpServletResponse res;
    private HierarchyIteratorModel hierarchyIterator;
    private MonoHierarchyIteratorModel monoHierarchyIterator;
    private NavParamsModel navParams;

    public JCMSModels(ValueStack stack, HttpServletRequest req, HttpServletResponse res) {
        this.stack = stack;
        this.req = req;
        this.res = res;
    }

    public NavParamsModel getNavParams() {
    	if(navParams==null) {
    		navParams = new NavParamsModel(stack, req, res);
    	}
		return navParams;
	}
    
    public HierarchyIteratorModel getHierarchyIterator() {
    	if(hierarchyIterator == null) {
    		hierarchyIterator = new HierarchyIteratorModel(stack, req, res);
    	}
		return hierarchyIterator;
	}
    
    public MonoHierarchyIteratorModel getMonoHierarchyIterator() {
    	if(monoHierarchyIterator == null) {
    		monoHierarchyIterator = new MonoHierarchyIteratorModel(stack, req, res);
    	}
		return monoHierarchyIterator;
	}
}
