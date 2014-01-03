package hr.fer.zemris.jcms.beans.ext;

import java.util.ArrayList;
import java.util.List;

import hr.fer.zemris.jcms.model.Group;
import hr.fer.zemris.jcms.model.ToDoTask;

/**
 * ToDo pomocni bean za prikaz dostupnih grupa unutar kolegija
 * @author IvanFer
 */
public class ToDoCourseGroupsBean implements Comparable<ToDoCourseGroupsBean>{

	private String courseName;
	private List<Group> groups;
	
	public ToDoCourseGroupsBean(){
		this.groups = new ArrayList<Group>();
	}

	
	public String getCourseName() {
		return courseName;
	}
	public void setCourseName(String courseName) {
		this.courseName = courseName;
	}

	public List<Group> getGroups() {
		return groups;
	}
	public void setGroups(List<Group> groups) {
		this.groups = groups;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getCourseName().hashCode();
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
		final ToDoCourseGroupsBean other = (ToDoCourseGroupsBean) obj;
		if (other.getCourseName().equals(this.getCourseName()))
		{
			return true;
		}else{
			return false;	
		}
	}

	@Override
	public int compareTo(ToDoCourseGroupsBean arg0) {
//		ToDoRealizerBean tdb = (ToDoRealizerBean)arg0;
		return 0;
	}
	
}
