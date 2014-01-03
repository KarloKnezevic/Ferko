package hr.fer.zemris.jcms.beans.cached;

import java.io.Serializable;

public class Dependencies implements Serializable {

	private static final long serialVersionUID = 1L;

	private DependencyItem[] roots;
	private String courseInstanceID;
	
	public Dependencies(String courseInstanceID, DependencyItem[] roots) {
		super();
		this.roots = roots;
		this.courseInstanceID = courseInstanceID;
	}

	public String getCourseInstanceID() {
		return courseInstanceID;
	}
	
	public DependencyItem[] getRoots() {
		return roots;
	}

	@Override
	public String toString() {
		return toJSONStringBuilder().toString();
	}
	
	public StringBuilder toJSONStringBuilder() {
		StringBuilder sb = new StringBuilder(1000);
		return toJSONStringBuilder(sb);
	}

	public StringBuilder toJSONStringBuilder(StringBuilder sb) {
		sb.append("[");
		for(int i = 0; i < roots.length; i++) {
			if(i>0) sb.append(",");
			sb.append("\n    ");
			roots[i].toJSONStringBuilder(sb);
		}
		sb.append("\n];");
		return sb;
	}
}
