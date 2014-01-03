package hr.fer.zemris.jcms.model.forum;

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
 * Podforum pripada jednoj kategoriji i sadrži teme. Može biti otvoren,
 * zaključan (nije moguće stvarati nove teme), premješten u drugu kategoriju
 * ili potpuno izbrisan.
 * 
 * @author Hrvoje Ban
 */
@Entity
@Table(name = "forum_subforums")
@NamedQueries({
	@NamedQuery(name = "Subforum.find",
		query = "SELECT s FROM Subforum s JOIN FETCH s.category WHERE s.id=:id"),
	@NamedQuery(name = "Subforum.list",
		query = "SELECT s FROM Subforum s JOIN FETCH s.category LEFT JOIN FETCH s.firstTopic t " +
			"LEFT JOIN FETCH t.lastPost p LEFT JOIN FETCH p.author WHERE s.category.id=:categoryId"),
	@NamedQuery(name = "Subforum.listNonHidden",
		query = "SELECT s FROM Subforum s JOIN FETCH s.category LEFT JOIN FETCH s.firstTopic t " +
				"LEFT JOIN FETCH t.lastPost p LEFT JOIN FETCH p.author " +
				"WHERE s.category.id=:categoryId AND s.hidden=false"),
	@NamedQuery(name = "Subforum.listOpen",
		query = "SELECT s FROM Subforum s JOIN FETCH s.category c " +
				"WHERE s.category.id=:categoryId AND s.closed=false AND c.closed=false")
})
@Validation
public class Subforum extends AbstractEntity {

	private Category category;
	private String name;
	private String description;
	private boolean closed;
	private boolean hidden;
	private Set<Topic> topics;
	private Topic firstTopic;
	private int topicCount;
	private int postCount;
	
	/**
	 * @return Kategorija kojoj pripada ovaj podforum.
	 */
	@Index(name = "forum_subforum_category_index")
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	public Category getCategory() {
		return category;
	}
	
	public void setCategory(Category category) {
		this.category = category;
	}

	/**
	 * @return Ime podforuma.
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
	 * @return Opis podforuma.
	 */
	@Column(length = 128)
	@StringLengthFieldValidator(maxLength = "128", message = "Opis je predug")
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}
	
	/**
	 * @return Jeli moguće stvarati nove teme i poruke te uređivati postojeće
	 * unutar ovog podforuma.
	 */
	@Column(nullable = false)
	public boolean isClosed() {
		return closed;
	}
	
	public void setClosed(boolean closed) {
		this.closed = closed;
	}
	
	/**
	 * @return Jeli podforum vidljiv korisnicima koji nisu administratori ili
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
	 * @return Popis tema unutar ovog podforuma. Iteriranje po ovom setu
	 * vraća teme silazno sortirane po vremenu zadnje promjene.
	 */
	@OneToMany(mappedBy = "subforum", cascade = { CascadeType.REMOVE })
	@OrderBy(value = "modificationDate desc")
	public Set<Topic> getTopics() {
		return topics;
	}

	public void setTopics(Set<Topic> topics) {
		this.topics = topics;
	}
	
	/**
	 * @return Zadnja izmjenjena tema unutar ovog podforuma.
	 */
	@OneToOne(fetch = FetchType.EAGER)
	public Topic getFirstTopic() {
		return firstTopic;
	}
	
	public void setFirstTopic(Topic lastTopic) {
		this.firstTopic = lastTopic;
	}

	/**
	 * @return Broj tema unutar ovog podforuma. Brže od getTopics().size()
	 * koji prvo mora dohvatiti sve teme iz baze podataka.
	 */
	@Column(nullable = false)
	public int getTopicCount() {
		return topicCount;
	}

	public void setTopicCount(int topicCount) {
		this.topicCount = topicCount;
	}
	
	/**
	 * Povečava brojač teme ovog podforuma za jedan.
	 */
	public void increaseTopicCount() {
		++topicCount;
	}
	
	/**
	 * Smanjuje brojač teme ovog podforuma za jedan.
	 */
	public void decreaseTopicCount() {
		--topicCount;
	}

	/**
	 * @return Broj poruka unutar ovog podforuma.
	 */
	@Column(nullable = false)
	public int getPostCount() {
		return postCount;
	}

	public void setPostCount(int postCount) {
		this.postCount = postCount;
	}
	
	/**
	 * Povećava brojač poruka ovog podforuma za jedan.
	 */
	public void increasePostCount(int count) {
		postCount += count;
	}
	
	/**
	 * Smanjuje brojač poruka ovog podforuma za jedan.
	 */
	public void decreasePostCount(int count) {
		postCount -= count;
	}
	
}
