package hr.fer.zemris.jcms.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

/**
 * ToDo task postavke
 * 
 * @author IvanFer
 * 
 */

@NamedQueries({
	@NamedQuery(name="ToDoPreference.getUserPrefs",query="select pref from ToDoPreference as pref where pref.owner.id=:userID")
})
@Entity
@Table(name="todo_preferences")
public class ToDoPreference implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private Long id;
	private User owner;
	private String name;
	private String value;
	private int version;
	

	/**
	 * Preference ID
	 * @return
	 */
	@Id @GeneratedValue
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	/**
	 * Task owner - user who created the task
	 * @return Owner
	 */
	@JoinColumn(nullable = false)
	@ManyToOne(fetch=FetchType.EAGER)
	@Fetch(FetchMode.SELECT)
	public User getOwner() {
		return owner;
	}
	public void setOwner(User owner) {
		this.owner = owner;
	}
	
	
	/**
	 * Preference name
	 * @return Name
	 */
	@Column(length = 100, nullable = false)
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * Preference value
	 * @return Value
	 */
	@Column(length = 100, nullable = false)
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
	/**
	 * Version. Optimistic lock support.
	 * @return
	 */
	@Version
	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + getOwner().hashCode();
		result = prime * result + getName().hashCode();
		result = prime * result + getValue().hashCode();
		return result;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof ToDoPreference))
			return false;
		final ToDoPreference other = (ToDoPreference) obj;
		if (other.getOwner().equals(this.getOwner()) && 
				other.getName().equals(this.getName()) &&
					other.getValue().equals(this.getValue()))
		{
			return true;
		}else{
			return false;	
		}
		
	}
	
}
