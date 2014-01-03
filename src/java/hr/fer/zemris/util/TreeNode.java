package hr.fer.zemris.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class TreeNode<T,X> {
	private T element;
	X data;
	Map<T,TreeNode<T,X>> children;
	List<TreeNode<T,X>> order = new ArrayList<TreeNode<T,X>>();
	private int inverseLevel;
	private int level;
	
	public TreeNode(T element, X data, int level) {
		this.element = element;
		this.data = data;
		this.level = level;
	}
	
	public boolean isLeaf() {
		return children==null || children.isEmpty();
	}
	
	public T getElement() {
		return element;
	}
	
	public List<TreeNode<T,X>> getChildren() {
		return order;
	}

	public int getInverseLevel() {
		return inverseLevel;
	}

	public int getLevel() {
		return level;
	}
	
	public int updateInverseLevel() {
		if(children==null || children.isEmpty()) {
			inverseLevel = 1;
		} else {
			inverseLevel = 0;
			for(TreeNode<T,X> node : order) {
				inverseLevel = Math.max(inverseLevel, node.updateInverseLevel()+1);
			}
		}
		return inverseLevel;
	}
	
	public X getData() {
		return data;
	}
	
	public void setData(X data) {
		this.data = data;
	}
}
