package hr.fer.zemris.jcms.model;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@NamedQueries({
    @NamedQuery(name="AuthType.findByName",query="select au from AuthType as au where au.name=:name"),
    @NamedQuery(name="AuthType.list",query="select au from AuthType as au")
})
@Entity
@Table(name="auth_types")
@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class AuthType implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private Long id;
	private String name;
	private String description;
	
	public AuthType() {
	}
	
	public AuthType(String name, String description) {
		super();
		this.name = name;
		this.description = description;
	}

	@Id @GeneratedValue
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	@Column(nullable=false,length=150,unique=true)
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	
	@Column(nullable=false,length=150,unique=true)
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((getId() == null) ? 0 : getId().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof AuthType))
			return false;
		final AuthType other = (AuthType) obj;
		if (getId() == null) {
			if (other.getId() != null)
				return false;
		} else if (!getId().equals(other.getId()))
			return false;
		return true;
	}

	
}
