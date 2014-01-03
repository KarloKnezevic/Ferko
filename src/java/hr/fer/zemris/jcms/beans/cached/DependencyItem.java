package hr.fer.zemris.jcms.beans.cached;

import java.io.Serializable;

public class DependencyItem implements Serializable, Comparable<DependencyItem> {

	private static final long serialVersionUID = 1L;

	private String uniqueID;
	private DependencyItem[] dependencies;
	
	public DependencyItem(String uniqueID, DependencyItem[] dependencies) {
		super();
		this.uniqueID = uniqueID;
		this.dependencies = dependencies;
	}

	public String getUniqueID() {
		return uniqueID;
	}
	
	public DependencyItem[] getDependencies() {
		return dependencies;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((uniqueID == null) ? 0 : uniqueID.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DependencyItem other = (DependencyItem) obj;
		if (uniqueID == null) {
			if (other.uniqueID != null)
				return false;
		} else if (!uniqueID.equals(other.uniqueID))
			return false;
		return true;
	}
	
	@Override
	public int compareTo(DependencyItem o) {
		return this.uniqueID.compareTo(o.uniqueID);
	}
	
	@Override
	public String toString() {
		return toJSONStringBuilder().toString();
	}
	
	public StringBuilder toJSONStringBuilder() {
		StringBuilder sb = new StringBuilder();
		return toJSONStringBuilder(sb);
	}

	public StringBuilder toJSONStringBuilder(StringBuilder sb) {
		sb.append("{ ");
		sb.append("\"uniqueID\": \"");
		sb.append(uniqueID);
		sb.append("\", \"deps\": [");
		for(int i = 0; i < dependencies.length; i++) {
			if(i>0) sb.append(",");
			sb.append("\n    ");
			dependencies[i].toJSONStringBuilder(sb);
		}
		sb.append("\n]}");
		return sb;
	}

}
