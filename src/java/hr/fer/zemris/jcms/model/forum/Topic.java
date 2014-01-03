package hr.fer.zemris.jcms.model.forum;

import hr.fer.zemris.jcms.model.User;

import java.util.Date;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.OrderBy;
import javax.persistence.Table;

import org.hibernate.annotations.Index;

import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;
import com.opensymphony.xwork2.validator.annotations.StringLengthFieldValidator;
import com.opensymphony.xwork2.validator.annotations.Validation;

/**
 * Tema pripada jednom podforumu i sadrži poruke. Može biti otvorena, zaključana
 * (nije moguće stvarati nove poruke, ali je moguće uređivati postojeće),
 * premještena pod drugi podforum unutar iste kategorije ili potpuno izbrisana.
 * 
 * @author Hrvoje Ban
 */
@Entity
@Table(name = "forum_topics")
@NamedQueries({
	@NamedQuery(name = "Topic.find",
		query = "SELECT t FROM Topic t JOIN FETCH t.subforum s JOIN FETCH s.category " +
				"WHERE t.id=:id"),
	@NamedQuery(name = "Topic.list",
			query = "SELECT t FROM Topic t JOIN FETCH t.lastPost p JOIN FETCH p.author " +
					"WHERE t.subforum.id=:subforumId AND t.hidden=false ORDER BY t.modificationDate DESC"),
	@NamedQuery(name = "Topic.listPinnedFirst",
		query = "SELECT t FROM Topic t JOIN FETCH t.lastPost p JOIN FETCH p.author " +
				"WHERE t.subforum.id=:subforumId ORDER BY t.pinned DESC, t.modificationDate DESC"),
	@NamedQuery(name = "Topic.listPinnedFirstNonHidden",
		query = "SELECT t FROM Topic t JOIN FETCH t.lastPost p JOIN FETCH p.author " +
				"WHERE t.subforum.id=:subforumId AND t.hidden=false " +
				"ORDER BY t.pinned DESC, t.modificationDate DESC")
})
@Validation
public class Topic extends AbstractEntity {

	private Subforum subforum;
	private String name;
	private User author;
	private boolean pinned;
	private boolean closed;
	private boolean hidden;
	private Date modificationDate;
	private Set<Post> posts;
	private Post lastPost;
	private int postCount;
	
	/**
	 * @return Podforum kojem pripada ova tema.
	 */
	@Index(name = "forum_topic_subforum_index")
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	public Subforum getSubforum() {
		return subforum;
	}
	
	public void setSubforum(Subforum subforum) {
		this.subforum = subforum;
	}
	
	/**
	 * @return Ime teme.
	 */
	@Column(nullable = false, length = 64)
	@RequiredStringValidator(message = "Ime ne smije bit prazno")
	@StringLengthFieldValidator(minLength = "4", maxLength = "64", message = "Ime nije dopuštene dužine")
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	/**
	 * @return Autor teme.
	 */
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	public User getAuthor() {
		return author;
	}
	
	public void setAuthor(User author) {
		this.author = author;
	}
	
	/**
	 * @return Treba li se tema uvijek prikazivati pri vrhu podforuma.
	 */
	@Column(nullable = false)
	public boolean isPinned() {
		return pinned;
	}

	public void setPinned(boolean pinned) {
		this.pinned = pinned;
	}

	/**
	 * @return Jeli tema zatvorena (nije moguće stvaranje novih poruka).
	 */
	@Column(nullable = false)
	public boolean isClosed() {
		return closed;
	}

	public void setClosed(boolean closed) {
		this.closed = closed;
	}
	
	/**
	 * @return Jeli tema vidljiva korisnicima koji nisu administratori ili
	 * moderatori.
	 */
	@Column(nullable = false)
	public boolean isHidden() {
		return hidden;
	}
	
	public void setHidden(boolean hidden) {
		this.hidden = hidden;
	}

	/**
	 * @return Datum zadnje izmjene teme. Najčešće datum nastanka zadnje poruke.
	 */
	@Column(nullable = false)
	public Date getModificationDate() {
		return modificationDate;
	}
	
	public void setModificationDate(Date modificationDate) {
		this.modificationDate = modificationDate;
	}

	/**
	 * @return Popis poruka unutar ove teme. Iteriranje po ovom setu
	 * vraća poruke uzlazno sortirane po rednom broju.
	 */
	@OneToMany(mappedBy = "topic", cascade = { CascadeType.REMOVE })
	@OrderBy(value = "ordinal asc")
	public Set<Post> getPosts() {
		return posts;
	}

	public void setPosts(Set<Post> posts) {
		this.posts = posts;
	}
	
	/**
	 * @return Zadnja poruka unutar ove teme.
	 */
	@OneToOne(fetch = FetchType.LAZY)
	public Post getLastPost() {
		return lastPost;
	}
	
	public void setLastPost(Post lastPost) {
		this.lastPost = lastPost;
	}

	/**
	 * @return Broj poruka unutar ove teme. Brže od getPosts().size() koji
	 * prvo mora dohvatiti sve poruke iz baze podataka.
	 */
	@Column(nullable = false)
	public int getPostCount() {
		return postCount;
	}

	public void setPostCount(int postCount) {
		this.postCount = postCount;
	}
	
	/**
	 * Povečava brojač poruka ove teme i njezinog podforuma za jedan.
	 * @param count 
	 */
	public void increasePostCount(int count) {
		postCount += count;
		subforum.increasePostCount(count);
	}
	
	/**
	 * Smanjuje brojač poruka ove teme i njezinog podforuma za jedan.
	 */
	public void decreasePostCount(int count) {
		postCount -= count;
		subforum.decreasePostCount(count);
	}

}
