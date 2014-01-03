package hr.fer.zemris.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Tree<T,X> {
	
	Map<T, TreeNode<T,X>> roots = new HashMap<T, TreeNode<T,X>>();
	List<TreeNode<T,X>> order = new ArrayList<TreeNode<T,X>>();
	
	public Tree() {
	}
	
	public boolean isEmpty() {
		return roots.isEmpty();
	}
	
	public boolean isLeaf() {
		return roots.isEmpty();
	}

	public boolean isRootElement(T element) {
		for(int i = 0; i < order.size(); i++) {
			TreeNode<T,X> node = order.get(i);
			if(node.getElement().equals(element)) return true;
		}
		return false;
	}

	public List<TreeNode<T,X>> getChildren() {
		return order;
	}

	public TreeNode<T,X> getNode(TreePath<T> path) {
		Map<T, TreeNode<T,X>> parent = roots;
		TreeNode<T,X> node = null;
		
		for(T elem : path.getPath()) {
			if(parent==null) return null;
			node = parent.get(elem);
			if(node==null) {
				return null;
			}
			parent = node.children;
		}
		return node;
	}
	
	public boolean addPath(TreePath<T> path, X data) {
		boolean modified = false;
		Map<T, TreeNode<T,X>> parent = roots;
		List<TreeNode<T,X>> order = this.order;
		TreeNode<T,X> currentNode = null;
		if(path.getPath().size()>0) {
			T lastElem = path.getPath().get(path.getPath().size()-1);
			int level = -1;
			for(T elem : path.getPath()) {
				level++;
				if(parent==null) {
					currentNode.children = new HashMap<T, TreeNode<T,X>>();
					parent = currentNode.children;
				}
				if(order==null) {
					currentNode.order = new ArrayList<TreeNode<T,X>>();
					order = currentNode.order;
				}
				TreeNode<T,X> node = parent.get(elem);
				if(node==null) {
					node = new TreeNode<T,X>(elem, elem==lastElem ? data : null, level);
					parent.put(elem, node);
					order.add(node);
					modified = true;
				}
				parent = node.children;
				order = node.order;
				currentNode = node;
			}
		}
		return modified;
	}
	
	public void sort(final Comparator<T> comparator) {
		Comparator<TreeNode<T,X>> comp = new Comparator<TreeNode<T,X>>() {
			@Override
			public int compare(TreeNode<T,X> o1, TreeNode<T,X> o2) {
				return comparator.compare(o1.getElement(), o2.getElement());
			}
		};
		Collections.sort(order, comp);
		for(TreeNode<T,X> node : order) {
			recursiveSort(node, comp);
		}
	}

	private void recursiveSort(TreeNode<T,X> node, Comparator<TreeNode<T,X>> comp) {
		if(node.order==null || node.order.isEmpty()) return; 
		Collections.sort(node.order, comp);
		for(TreeNode<T,X> subnode : node.order) {
			recursiveSort(subnode, comp);
		}
	}

	public void sort() {
		Comparator<TreeNode<T,X>> comp = new Comparator<TreeNode<T,X>>() {
			@SuppressWarnings("unchecked")
			@Override
			public int compare(TreeNode<T,X> o1, TreeNode<T,X> o2) {
				Comparable<T> c1 = (Comparable<T>)o1.getElement();
				return c1.compareTo(o2.getElement());
			}
		};
		Collections.sort(order, comp);
		for(TreeNode<T,X> node : order) {
			recursiveSort(node, comp);
		}
	}
	
	public void updateInverseLevel() {
		if(roots!=null && !roots.isEmpty()) {
			for(TreeNode<T,X> node : order) {
				node.updateInverseLevel();
			}
		}
	}

}
