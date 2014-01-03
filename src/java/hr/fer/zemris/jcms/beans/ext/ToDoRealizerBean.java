package hr.fer.zemris.jcms.beans.ext;

import hr.fer.zemris.jcms.model.ToDoTask;

/**
 * ToDo task bean
 * @author IvanFer
 */
public class ToDoRealizerBean implements Comparable<ToDoRealizerBean>{

	private Boolean userRealizer;
	private String description;
	private String id;
	private Boolean checked;
	
	public ToDoRealizerBean(){
	}

	public ToDoRealizerBean(Boolean userRealizer, String desc, String id){
		this.userRealizer = userRealizer;
		this.description = desc;
		this.id=id;
		this.checked=false;
	}
	
	public Boolean getUserRealizer() {
		return userRealizer;
	}
	public void setUserRealizer(Boolean userRealizer) {
		this.userRealizer = userRealizer;
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	public void setIDLong(Long id){
		this.id = id.toString();
	}
	public Long getIDLong(){
		return Long.parseLong(this.id);
	}
	
	public Boolean getChecked() {
		return checked;
	}
	public void setChecked(Boolean checked) {
		this.checked = checked;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getDescription().hashCode();
		result = prime * result + getId().hashCode();
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof ToDoTask))
			return false;
		final ToDoRealizerBean other = (ToDoRealizerBean) obj;
		if (other.getDescription().equals(this.getDescription()) && 
				other.getId().equals(this.getId()))
		{
			return true;
		}else{
			return false;	
		}
	}

	@Override
	public int compareTo(ToDoRealizerBean arg0) {
//		ToDoRealizerBean tdb = (ToDoRealizerBean)arg0;
		return 0;
	}
	
}
