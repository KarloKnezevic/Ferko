package hr.fer.zemris.jcms.dao;

import hr.fer.zemris.jcms.beans.ext.CoarseGroupStat2;
import hr.fer.zemris.jcms.model.CourseInstance;
import hr.fer.zemris.jcms.model.Group;
import hr.fer.zemris.jcms.model.GroupOwner;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.model.UserGroup;

import java.util.List;

import javax.persistence.EntityManager;

public interface GroupDAO {

	public Group get(EntityManager em, Long id);
	public void save(EntityManager em, Group g);
	public void remove(EntityManager em, Group g);
	public Group get(EntityManager em, String compositeCourseID, String relativePath);
	/**
	 * Pronalazi sve grupe korisnika na zadanom kolegiju.
	 * @param em
	 * @param courseInstance
	 * @param user
	 * @return
	 */
	public List<Group> findAllGroupsForUserOnCourse(EntityManager em, String compositeCourseID, User user);
	/**
	 * Vraca sve podgrupe u kojima je korisnik, a koje su strogo podgrupe zadane grupe.
	 * @param em
	 * @param compositeCourseID
	 * @param parentRelativePath
	 * @param user
	 * @return
	 */
	public List<Group> findSubGroupsForUser(EntityManager em, String compositeCourseID, String parentRelativePath, User user);
	/**
	 * Pronalazi sve vrsne grupe kolegija (grupu za predavanja, za labose, ...).
	 * @param em
	 * @param compositeIdentifier
	 * @return
	 */
	public List<Group> findCourseInstanceTopLevelGroups(EntityManager em, String compositeIdentifier);
	/**
	 * Pronalazi primarnu grupu kolegija; to je grupa koja sadrži sve ostale podgrupe, i odgovara courseInstance.getPrimaryGroup().
	 * @param em
	 * @param compositeIdentifier
	 * @return
	 */
	public Group findCourseInstancePrimaryGroup(EntityManager em, String compositeIdentifier);
	/**
	 * Pronalazi grupe za predavanja (ne vraća virtualnog roditelja, već samo podgrupe za predavanja).
	 * @param em
	 * @param compositeIdentifier
	 * @return
	 */
	public List<Group> findLectureSubgroups(EntityManager em, String compositeIdentifier);
	/**
	 * Pronalazi i učitava sve grupe za predavanja i vraća njihovog virtualnog roditelja.
	 * @param em
	 * @param compositeIdentifier
	 * @return
	 */
	public Group findLectureSubgroupTree(EntityManager em, String compositeIdentifier);
	/**
	 * Vraća sve grupe koje zadovoljavaju zadanu kombinaciju uvjeta. RelativePath uspoređuje operatorom LIKE.
	 * @param em
	 * @param compositeIdentifier
	 * @param relativePath
	 * @return
	 */
	public List<Group> findSubgroups(EntityManager em, String compositeIdentifier, String relativePath);
	/**
	 * Vraća sve grupe koje zadovoljavaju zadanu kombinaciju uvjeta. LikeRelativePath uspoređuje operatorom LIKE, a
	 * EqRelativePath uspoređuje operatorom jednakosti. compositeIdentifier uspoređuje operatorom jednakosti.
	 * @param em
	 * @param compositeIdentifier
	 * @param eqRelativePath
	 * @param likeRelativePath
	 * @return
	 */
	public List<Group> findSubgroups(EntityManager em, String compositeIdentifier, String eqRelativePath, String likeRelativePath);
	/**
	 * Vraća sve grupe koje zadovoljavaju zadanu kombinaciju uvjeta. LikeRelativePath uspoređuje operatorom LIKE, a
	 * EqRelativePath uspoređuje operatorom jednakosti. compositeIdentifier uspoređuje operatorom LIKE.
	 * @param em
	 * @param likeCompositeIdentifier
	 * @param eqRelativePath
	 * @param likeRelativePath
	 * @return
	 */
	public List<Group> findSubgroupsLLE(EntityManager em, String likeCompositeIdentifier, String eqRelativePath, String likeRelativePath);
	/**
	 * Pronalazi sve UserGroup objekte studenata za predavanja na svim kolegijima (kao uvjet koristi {@code likeCompositeIdentifier} koji
	 * bi trebao biti oblika "2007Z/%" za ovako nešto.
	 * @param em
	 * @param likeCompositeIdentifier
	 * @return
	 */
	public List<UserGroup> findAllLectureUserGroups(EntityManager em, String likeCompositeIdentifier);
	/**
	 * Ucitava zadani GroupOwner objekt.
	 * @param id
	 * @return
	 */
	public GroupOwner getGroupOwner(EntityManager em, Long id);
	/**
	 * Pronalazi sve GroupOwner objekte koji zadovoljavaju zadane uvjete. LIKE u prefiksu argumenta znači da tu
	 * može doći izraz koji će se provjeriti LIKE operatorom; eq znači da se provjerava na jednakost.
	 * @param em
	 * @param likeCompositeIdentifier
	 * @param eqRelativePath
	 * @param likeRelativePath
	 * @return
	 */
	public List<GroupOwner> findForSubgroupsLLE(EntityManager em, String likeCompositeIdentifier, String eqRelativePath, String likeRelativePath);
	/**
	 * Pronalazi sve GroupOwner objekte koji zadovoljavaju zadane uvjete. LIKE u prefiksu argumenta znači da tu
	 * može doći izraz koji će se provjeriti LIKE operatorom; eq znači da se provjerava na jednakost.
	 * @param em
	 * @param eqCompositeIdentifier
	 * @param eqRelativePath
	 * @param likeRelativePath
	 * @return
	 */
	public List<GroupOwner> findForSubgroupsELE(EntityManager em, String eqCompositeIdentifier, String eqRelativePath, String likeRelativePath);
	/**
	 * Pronalazi sve GroupOwner objekte koji zadovoljavaju zadane uvjete. Pri tome relativePath predstavlja grupu roditelja
	 * a objekti se traže za potomke te grupe.
	 * @param em
	 * @param compositeIdentifier
	 * @param relativePath
	 * @return
	 */
	List<GroupOwner> findForSubgroups(EntityManager em, String compositeIdentifier, String relativePath);
	/**
	 * Pronalazi sve GroupOwner-e za zadanu grupu.
	 * @param em
	 * @param group
	 * @return
	 */
	public List<GroupOwner> findForGroup(EntityManager em, Group group);
	/**
	 * Briše GroupOwner-a.
	 * @param em
	 * @param groupOwner
	 */
	public void remove(EntityManager em, GroupOwner groupOwner);
	/**
	 * Snima GroupOwner-a.
	 * @param em
	 * @param groupOwner
	 */
	public void save(EntityManager em, GroupOwner groupOwner);
	/**
	 * Pronalazi sve grupe na kolegiju koje su u vlasništvu zadanog korisnika.
	 * @param em
	 * @param id
	 * @param user
	 * @return
	 */
	public List<Group> findGroupsOwnedBy(EntityManager em, String compositeCourseID, User user);
	/**
	 * Pronalazi sve korisnike na kolegiju u zadanoj grupi ili nekoj njenoj podgrupi.
	 * @param em
	 * @param courseInstanceID
	 * @param relativePath
	 * @return
	 */
	public List<User> listUsersInGroupTree(EntityManager em, String courseInstanceID, String relativePath);
	/**
	 * Pronalazi sve UserGroup objekte na kolegiju u zadanoj grupi ili nekoj njenoj podgrupi.
	 * @param em
	 * @param courseInstanceID
	 * @param relativePath
	 * @return
	 */
	public List<UserGroup> listUserGroupsInGroupTree(EntityManager em, String courseInstanceID, String relativePath);
	/**
	 * Pronalazi sve korisnike na kolegiju u zadanoj grupi ili nekoj njenoj podgrupi koji zadovoljavaju zadane kriterije.
	 * @param em
	 * @param courseInstanceID
	 * @param relativePath
	 * @param eqLastName
	 * @param eqFirstName
	 * @param likeJmbag
	 * @return
	 */
	public List<User> listUsersInGroupTree(EntityManager em, String courseInstanceID, String relativePath, String eqLastName, String eqFirstName, String likeJmbag);
	/**
	 * @param em
	 * Pronalazi sve korisnike na kolegiju u zadanoj grupi ili nekoj njenoj podgrupi koji zadovoljavaju zadane kriterije.
	 * @param courseInstanceID
	 * @param relativePath
	 * @param eqLastName
	 * @param likeFirstName
	 * @return
	 */
	public List<User> listUsersInGroupTree(EntityManager em, String courseInstanceID, String relativePath, String eqLastName, String likeFirstName);
	/**
	 * @param em
	 * Pronalazi sve korisnike na kolegiju u zadanoj grupi ili nekoj njenoj podgrupi koji zadovoljavaju zadane kriterije.
	 * @param courseInstanceID
	 * @param relativePath
	 * @param likeLastName
	 * @return
	 */
	public List<User> listUsersInGroupTree(EntityManager em, String courseInstanceID, String relativePath, String likeLastName);
	/**
	 * Vraća sve grupe na zadanom kolegiju koje mogu biti korijeni za burzu grupa.
	 * @param em
	 * @param courseInstance
	 * @return
	 */
	public List<Group> listMarketPlaceRootGroupsForCourse(EntityManager em, CourseInstance courseInstance);
	/**
	 * Pronalazi sve objekte UserGroup koji zadanog korisnika smjestaju u neku podgrupu zadane grupe.
	 * @param em
	 * @param courseInstanceID
	 * @param parentRelativePath
	 * @param user
	 * @return
	 */
	public List<UserGroup> findUserGroupsForUser(EntityManager em, String courseInstanceID, String parentRelativePath, User user);
	/**
	 * Za svaku podgrupu navedene grupe i za svaki studentski tag u toj podgrupi vraca koliko ima takvih studenata. Rezultat
	 * je lista trojki {Long groupID, String studentTag, Number count}.
	 * @param em
	 * @param courseInstanceID
	 * @param parentRelativePath
	 * @return
	 */
	public List<Object[]> getGroupStat(EntityManager em, String courseInstanceID, String parentRelativePath);
	/**
	 * Za svaku podgrupu navedene grupe vraca koliko ima studenata. Rezultat
	 * je lista dvojki {Long groupID, Number count}.
	 * @param em
	 * @param courseInstanceID
	 * @param parentRelativePath
	 * @return
	 */
	public List<Object[]> getCoarseGroupStat(EntityManager em, String courseInstanceID, String parentRelativePath);
	/**
	 * Pronalazi sve groupowner objekte na kolegiju koji pripadaju zadanoj osobi a pokrivaju podgrupe zadane grupe.
	 * @param em
	 * @param compositeIdentifier
	 * @param relativePath
	 * @param user
	 * @return
	 */
	List<GroupOwner> findForSubgroupsAndUser(EntityManager em, String compositeIdentifier, String relativePath, User user);
	/**
	 * Vraca statistiku za grupe na selektiranim kolegijima.
	 * @param em
	 * @param likeCompositeCourseID
	 * @param likeRelativePath
	 * @return
	 */
	public List<CoarseGroupStat2> getCoarseGroupStat2(EntityManager em, String likeCompositeCourseID, String likeRelativePath);
	/**
	 * Pronalazi sve UserGroup objekte koji zadovoljavaju zadane kriterije.
	 * @param em
	 * @param likeCourseInstanceID
	 * @param likeRelativePath
	 * @return
	 */
	public List<UserGroup> findUserGroup(EntityManager em, String likeCourseInstanceID, String likeRelativePath);
	public GroupOwner getGroupOwner(EntityManager em, Group group, User user);
	/**
	 * Pronalazi sve compositeCourseID-eve kolegija na kojima je korisnih u definiranom tipu grupe
	 * @param em
	 * @param likeRelativePath
	 * @param user
	 * @return
	 */
	public List<String> listCoursesForUser(EntityManager em, String likeRelativePath, User user);
}
