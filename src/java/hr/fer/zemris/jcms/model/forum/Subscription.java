package hr.fer.zemris.jcms.model.forum;

import hr.fer.zemris.jcms.model.CourseInstance;
import hr.fer.zemris.jcms.model.User;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;

/**
 * Pretplata jednog korisnika za jednu kategoriju. Za predmetne kategorije se
 * dodatno pamti i instanca kolegija preko koje je korisnik pretplaćen.
 * 
 * @author Hrvoje Ban
 */
@Entity
@Table(name = "forum_subscriptions")
@NamedQueries({
	@NamedQuery(name = "Subscription.byUser",
		query = "SELECT s FROM Subscription s WHERE user.id=:userId"),
	@NamedQuery(name = "Subscription.byUserNonHidden",
		query = "SELECT s FROM Subscription s WHERE user.id=:userId AND category.hidden=false"),
	@NamedQuery(name = "Subscription.byUserAndCategory",
			query = "SELECT s FROM Subscription s WHERE user.id=:userId AND category.id =:categoryId")
})
public class Subscription extends AbstractEntity {
	
	private User user;
	private Category category;
	private CourseInstance courseInstance;
	
	/**
	 * @return Korisnik čija je ovo pretplata.
	 */
	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	public User getUser() {
		return user;
	}
	
	public void setUser(User user) {
		this.user = user;
	}

	/**
	 * @return Kategorija na koju je korisnik pretplaćen.
	 */
	@ManyToOne(fetch = FetchType.EAGER, optional = false)
	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	/**
	 * @return Primjerak kolegija preko kojega je korisnik pretplaćen.
	 */
	@ManyToOne(fetch = FetchType.EAGER)
	public CourseInstance getCourseInstance() {
		return courseInstance;
	}

	public void setCourseInstance(CourseInstance courseInstance) {
		this.courseInstance = courseInstance;
	}

}
