package hr.fer.zemris.jcms.model;

import java.io.Serializable;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.persistence.Version;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * Wiki stranica modelira jednu Wiki stranicu. Prostor naziva je lokalan
 * unutar svakog kolegija (ne primjerka - wiki se prenosi iz godine u godinu).
 * 
 * @author marcupic
 */
@NamedQueries({
    @NamedQuery(name="WikiPage.find",query="select wp from WikiPage as wp where wp.course=:course and wp.path=:path")
})
@Entity
@Table(name="wiki_pages", uniqueConstraints={@UniqueConstraint(columnNames={"course_isvuCode","path"})})
@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class WikiPage implements Serializable {

	private static final long serialVersionUID = 1L;

	private Long id;
	private String path;
	private String content;
	private Course course;
	private int version;
	private Date lastModifiedOn;
	private User user;
	
	public WikiPage() {
	}
	
	@Id @GeneratedValue
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	@ManyToOne
	@JoinColumn(nullable=false)
	public User getUser() {
		return user;
	}
	public void setUser(User user) {
		this.user = user;
	}
	
	@Temporal(TemporalType.TIMESTAMP)
	public Date getLastModifiedOn() {
		return lastModifiedOn;
	}
	public void setLastModifiedOn(Date lastModifiedOn) {
		this.lastModifiedOn = lastModifiedOn;
	}
	
	@Column(nullable=false,length=128)
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}

	@Column(nullable=false,length=1024*1024*10)
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	
	@Version
	public int getVersion() {
		return version;
	}
	public void setVersion(int version) {
		this.version = version;
	}

	@ManyToOne
	@JoinColumn(nullable=false)
	public Course getCourse() {
		return course;
	}
	public void setCourse(Course course) {
		this.course = course;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
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
		WikiPage other = (WikiPage) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
}
