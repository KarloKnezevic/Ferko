package hr.fer.zemris.util;

import java.util.ArrayList;
import java.util.List;

public class TreePath<T> {
	
	private List<T> path;
	
	public TreePath(List<T> path, boolean reuse) {
		if(reuse) {
			this.path = path;
		} else {
			this.path = new ArrayList<T>(path);
		}
	}

	public TreePath(T singleElement) {
		this.path = new ArrayList<T>(1);
		this.path.add(singleElement);
	}

	public TreePath(T[] path) {
		this.path = new ArrayList<T>(path.length);
		for(int i = 0; i < path.length; i++) {
			this.path.add(path[i]);
		}
	}

	List<T> getPath() {
		return path;
	}
	
}
