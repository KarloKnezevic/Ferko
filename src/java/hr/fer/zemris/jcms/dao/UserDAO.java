package hr.fer.zemris.jcms.dao;

import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.model.UserGroup;

import java.util.List;

import javax.persistence.EntityManager;

public interface UserDAO {

	public User getUserByJMBAG(EntityManager em, String jmbag);
	public User getUserByUsername(EntityManager em, String username);
	public User getUserById(EntityManager em, Long id);
	public User getFullUserByUsername(EntityManager em, String username);
	public void save(EntityManager em, User user);
	public void remove(EntityManager em, User user);
	/**
	 * Vraća sve korisnike (studente) koji su na zadanom kolegiju (vadi ih iz grupa 
	 * za predavanje, što je u skladu s dogovorenom konvencijom da je ta grupa referentna).
	 *  
	 * @param em entity manager
	 * @param compositeCourseID kompozitni identifikator kolegija
	 * @return
	 */
	public List<User> listUsersOnCourseInstance(EntityManager em, String compositeCourseID);
	/**
	 * Metoda dohvaća listu korisnika čiji su jmbagovi zadani u listi. Pri tome se ne traže svi
	 * jmbagovi, već samo oni na pozicijama definiranim uključivim granicama {@code startIndex}
	 * do {@code endIndex}. Prilikom rada s ovom metodom moguće je da se u interval uključi
	 * previše korisnika odjednom pa da stvar pukne. Pozivatelj je dužan o tome voditi računa,
	 * ili koristiti drugu metodu.
	 * 
	 * @param em entity manager
	 * @param jmbags lista jmbagova korisnika koje se želi dohvatiti
	 * @param startIndex uključivi početak
	 * @param endIndex uključivi kraj
	 * @return
	 */
	public List<User> getForJmbagSublist(EntityManager em, List<String> jmbags, int startIndex, int endIndex);

	/**
	 * Dohvaća sve korisnike čiji su jmbagovi zadani u listi. Ako je lista velika,
	 * dohvaćanje će se interno razbiti na više manjih dohvata.
	 * 
	 * @param em entity manager
	 * @param jmbags lista jmbagova
	 * @return
	 */
	public List<User> getForJmbagSublistBatching(EntityManager em, List<String> jmbags);
	public List<UserGroup> findForGroupAndSubGroups(EntityManager em, String compositeCourseID, String likeRelativePath, String eqRelativePath);
	/**
	 * Dohvaca broj korisnika u grupi i svim podgrupama te grupe
	 * 
	 * @param em
	 * @param compositeCourseID
	 * @param likeRelativePath
	 * @param eqRelativePath
	 * @return
	 */
	public Number getUserNumber(EntityManager em, String compositeCourseID, String likeRelativePath, String eqRelativePath);
	/**
	 * Metoda dohvaća listu korisnika čiji su jmbagovi zadani u listi, i pri tome odmah dohvaća
	 * i inicijalizira property userDescriptor. Pri tome se ne traže svi
	 * jmbagovi, već samo oni na pozicijama definiranim uključivim granicama {@code startIndex}
	 * do {@code endIndex}. Prilikom rada s ovom metodom moguće je da se u interval uključi
	 * previše korisnika odjednom pa da stvar pukne. Pozivatelj je dužan o tome voditi računa,
	 * ili koristiti drugu metodu.
	 * 
	 * @param em entity manager
	 * @param jmbags lista jmbagova korisnika koje se želi dohvatiti
	 * @param startIndex uključivi početak
	 * @param endIndex uključivi kraj
	 * @return
	 */
	public List<User> getFullForJmbagSublist(EntityManager em, List<String> jmbags, int startIndex, int endIndex);
	/**
	 * Dohvaća sve korisnike čiji su jmbagovi zadani u listi, i pri tome odmah dohvaća
	 * i inicijalizira property userDescriptor. Ako je lista velika,
	 * dohvaćanje će se interno razbiti na više manjih dohvata.
	 * 
	 * @param em entity manager
	 * @param jmbags lista jmbagova
	 * @return
	 */
	public List<User> getFullForJmbagSublistBatching(EntityManager em, List<String> jmbags);
}
