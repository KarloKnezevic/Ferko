package hr.fer.zemris.jcms.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@NamedQueries({
    @NamedQuery(name="KeyValue.list",query="select kv from KeyValue as kv")
})
@Entity
@Table(name="repository")
@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class KeyValue implements Serializable, Comparable<KeyValue>  {

	private static final long serialVersionUID = 1L;

	private String name;
	private String value;
	private int version;

	public KeyValue() {
	}
	
	public KeyValue(String name, String value) {
		super();
		if(name==null) throw new NullPointerException("Name can not be null!");
		this.name = name;
		this.value = value;
	}
	
	@Id @Column(length=50)
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	@Column(unique=false,length=100,nullable=true)
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	
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
		result = prime * result + ((getName() == null) ? 0 : getName().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof KeyValue))
			return false;
		final KeyValue other = (KeyValue) obj;
		if (getName() == null) {
			if (other.getName() != null)
				return false;
		} else if (!getName().equals(other.getName()))
			return false;
		return true;
	}

	@Override
	public int compareTo(KeyValue o) {
		if(o==null) return 1;
		return this.getName().compareToIgnoreCase(o.getName());
	}
}
