package hr.fer.zemris.jcms.model.forum;

import hr.fer.zemris.jcms.model.User;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

import org.hibernate.annotations.Index;

import com.opensymphony.xwork2.validator.annotations.RequiredStringValidator;
import com.opensymphony.xwork2.validator.annotations.StringLengthFieldValidator;
import com.opensymphony.xwork2.validator.annotations.Validation;

/**
 * Poruka pripada jednoj temi, ima svoj autora i sadrži tekst koji je
 * on napisao. Može biti vidljiva, premještana u drugu temu ili
 * potpuno izbrisana.
 * 
 * @author Hrvoje Ban
 */
@Entity
@Table(name = "forum_posts")
@NamedQueries({
	@NamedQuery(name = "Post.find",
		query = "SELECT p FROM Post p JOIN FETCH p.author LEFT JOIN FETCH p.replyTo " +
				"JOIN FETCH p.topic t JOIN FETCH t.subforum s JOIN FETCH s.category WHERE p.id=:id"),
	@NamedQuery(name = "Post.byOrdinal",
		query = "SELECT p FROM Post p JOIN FETCH p.author WHERE p.topic.id=:topicId AND p.ordinal=:ordinal"),	
	@NamedQuery(name = "Post.list",
		query = "SELECT p FROM Post p JOIN FETCH p.author LEFT JOIN FETCH p.replyTo LEFT JOIN FETCH p.editor " +
				"WHERE p.topic.id=:topicId AND p.ordinal BETWEEN :start AND :end ORDER BY p.ordinal"),
	@NamedQuery(name = "Post.push",
		query = "UPDATE Post p SET p.ordinal=p.ordinal-1, p.replyTo.id=:replyId " +
				"WHERE p.topic.id=:topicId AND p.ordinal > :fromOrdinal")	
})
@Validation
public class Post extends AbstractEntity {

	private Topic topic;
	private User author;
	private String name;
	private Date creationDate;
	private int ordinal;
	private Post replyTo;
	private String message;
	private Date modificationDate;
	private User editor;
	
	/**
	 * @return Tema kojoj pripada ova poruka.
	 */
	@Index(name = "forum_post_topic_index")
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	public Topic getTopic() {
		return topic;
	}
	
	public void setTopic(Topic topic) {
		this.topic = topic;
	}

	/**
	 * @return Autor poruke.
	 */
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	public User getAuthor() {
		return author;
	}

	public void setAuthor(User author) {
		this.author = author;
	}
	
	/**
	 * @return Naziv poruke.
	 */
	@Column(length = 64, nullable = false)
	@RequiredStringValidator(message = "Ime ne smije bit prazno")
	// Dodajemo "Re: " na početak pa moramo skratiti maksimalnu dužinu za četiri znaka
	@StringLengthFieldValidator(minLength = "4", maxLength = "60", message = "Ime nije dopuštene dužine")
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * @return Vrijeme nastanka poruke.
	 */
	@Column(nullable = false)
	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}
	
	/**
	 * @return Redni broj poruke u temi.
	 */
	@Index(name = "forum_post_ordinal_index")
	@Column(nullable = false)
	public int getOrdinal() {
		return ordinal;
	}
	
	public void setOrdinal(int ordinal) {
		this.ordinal = ordinal;
	}

	/**
	 * @return Poruka na koju je ova odgovor.
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	public Post getReplyTo() {
		return replyTo;
	}
	
	public void setReplyTo(Post replyTo) {
		this.replyTo = replyTo;
	}
	
	/**
	 * @return Tekst poruke u HTML obliku.
	 */
	@Column(nullable = false, length = 4096)
	@RequiredStringValidator(message = "Poruka ne smije biti prazna")
	@StringLengthFieldValidator(minLength = "2", maxLength = "4096", message = "Poruka nije dopuštene dužine")
	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	/**
	 * @return Datum zadnje izmjene poruke. null ako poruka nikad nije bio izmjenjena.
	 */
	@Column
	public Date getModificationDate() {
		return modificationDate;
	}

	public void setModificationDate(Date modificationDate) {
		this.modificationDate = modificationDate;
	}

	/**
	 * @return Autor zadnje izmjene poruke. null ako poruka nikad nije bila izmjenjena.
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	public User getEditor() {
		return editor;
	}

	public void setEditor(User editor) {
		this.editor = editor;
	}

}
