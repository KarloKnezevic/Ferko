package hr.fer.zemris.jcms.dao;

import hr.fer.zemris.jcms.beans.PollOptionBean;
import hr.fer.zemris.jcms.model.CourseInstance;
import hr.fer.zemris.jcms.model.Group;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.model.poll.Answer;
import hr.fer.zemris.jcms.model.poll.AnsweredPoll;
import hr.fer.zemris.jcms.model.poll.Poll;
import hr.fer.zemris.jcms.model.poll.PollTag;
import hr.fer.zemris.jcms.model.poll.PollUser;
import hr.fer.zemris.jcms.model.poll.TextAnswer;

import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;

public interface PollDAO {
	
	public void save(EntityManager em, Poll p);
	public void remove(EntityManager em, Poll p);
	public void savePollUser(EntityManager em, PollUser u);
	public void saveAnsweredPoll(EntityManager em, AnsweredPoll ap);
	public void saveAnswer(EntityManager em, Answer	answer);
	
	/**
	 * Dohvaca sve Poll-ove
	 * @param em
	 * @return List of Polls
	 */
	public List<Poll> all(EntityManager em);
	
	/**
	 * Dohvaca Poll s odredjenim id-om
	 * @param em
	 * @param id Poll id
	 * @return Poll
	 */
	public Poll getPoll(EntityManager em, long id);
	
	/**
	 * Dohvati sve aktivne/neodgovorene ankete za odredjenog korisnika
	 * @param em
	 * @param user_id
	 * @return List of Polls
	 */
	public List<Poll> getPollsForUser(EntityManager em, long user);
	
	/**
	 * Dovhati sve ankete s odredjenim nazivom/title
	 * @param em
	 * @param user_id
	 * @return List of Polls
	 */
	public List<Poll> getPollsWithName(EntityManager em, String title);
	
	/**
	 * Dohvati sve ankete koje se mogu urediti
	 * @return List of Polls
	 */
	public List<Poll> getEditablePolls(EntityManager em);
	
	/**
	 * Dohvati PollUser za ankete koje korisnik nije odgovorio
	 * @param em
	 * @param user
	 * @return
	 */
	public List<PollUser> getUnanswerdPUsForUser(EntityManager em, long user);
	
	/**
	 * Dovhati anketu s odredjenim id-om za odredjenog korisnika
	 * @param em
	 * @param poll
	 * @param userId
	 * @return
	 */
	public Poll getPollForUser(EntityManager em, Long pollId, Long userId);
	
	/**
	 * Dohvati odredjenu anketu za odredjenog vlasnika
	 * @param em
	 * @param pollId
	 * @param userId
	 * @return
	 */
	public Poll getPollForOwner(EntityManager em, long pollId, Long userId);
	/**
	 * Dohvati sve ankete kojima je vlasnik odredjenik korisnik
	 * @param em
	 * @param userID
	 * @return
	 */
	public List<Poll> getPollsForOwner(EntityManager em, Long userID);
	
	/**
	 * Dohvati PollUser
	 * @param em
	 * @param id PollUser id
	 * @return
	 */
	public PollUser getPollUser(EntityManager em, Long id);
	
	/**
	 * @param em
	 * @param id Poll id
	 * @return
	 */
	public List<TextAnswer> getAllTextAnswers(EntityManager em, Long id);
	
	
	/**
	 * @param em
	 * @param id Poll id
	 * @return
	 */
	public List<PollOptionBean> countAllOptionAnswers(EntityManager em, Long id);
	
	
	/**
	 * Dohvati sve grupe za koje postoji korisnik kojemu je pridruzena anketa
	 * @param em
	 * @param id Poll id
	 * @return
	 */
	public List<Group> getAllGroupsForPoll(EntityManager em, Long id);
	
	public AnsweredPoll getAnsweredPoll(EntityManager em, Long id);
	public List<TextAnswer> getAllTextAnswers(EntityManager em, Long id,
			AnsweredPoll ap);
	public List<PollOptionBean> countAllOptionAnswers(EntityManager em,
			Long id, AnsweredPoll ap);
	
	/**
	 * Dohvati sve AnsweredPoll određene ankete gdje je owner vlasnik grupe kojoj pripada AnsweredPoll
	 * @param em
	 * @param poll
	 * @param user
	 * @return
	 */
	public Set<AnsweredPoll> getAnsweredPollsForGroupOwner(EntityManager em, Poll poll, User owner);
	
	/**
	 * 
	 * @param em
	 * @param poll
	 * @param owner
	 * @return
	 */
	public int countAnsweredPollsForGroupOwner(EntityManager em, Poll poll, User owner);
	
	/**
	 * Dohvaća sve ankete na kojima korisnik može vidjeti rezultate. Ovisno o pravilima:
	 * 2. Vlasnici grupa mogu vidjeti rezultate svojih grupa i to sumarno i pojedinačno.
	 * 3. Nastavnik nositelj vidi sumarne i pojedinačne rezultate svih grupa osim privatnih.
	 * 4. Admin kolegija vidi sumarne i pojedinačne rezultate grupa za labose/seminare/ispite/zadaće. I sumarno za nastavu. 
	 * 5. Glavni asistent vidi sve kao i admin kolegija.
	 * 6. Nastavnik vidi sumarne i pojedinačne rezultate grupa za labose/seminare/ispite/zadaće.
	 * (Ukoliko je korisnik administrator sustava onda je potrebno koristiti drugu metodu za dohvacanje svih anketa na kolegiju.) 
	 * @param em
	 * @param user
	 * @param courseInstance
	 * @param rolesOnCourse
	 * @return
	 */
	public List<Poll> getAllPollsForView(EntityManager em, User user, CourseInstance courseInstance, Set<String> rolesOnCourse);

	
	/**
	 * Dohvati sve ankete na kolegiju.
	 * @param em
	 * @param courseInstance
	 * @return
	 */
	public List<Poll> getAllPollsOnCourse(EntityManager em, CourseInstance courseInstance);
	
	/**
	 * Izbrisi sva pitanja.
	 * @param poll
	 */
	public void removeAllQuestions(EntityManager em, Poll poll);
	
	/**
	 * Dohvati PollTag
	 * @param em
	 * @param id
	 * @return
	 */
	public PollTag getPollTag(EntityManager em, Long id);
	
	/**
	 * Izbrisi PollTag
	 * @param em
	 * @param pollTag
	 */
	public void remove(EntityManager em, PollTag pollTag);
	
	/**
	 * Spremi ili updateaj pollTag
	 * @param em
	 * @param pollTag
	 */
	public void save(EntityManager em, PollTag pollTag);
	
	/**
	 * Dohvati sve PollTagove
	 * @param em
	 * @return
	 */
	public List<PollTag> getPollTags(EntityManager em);
	/**
	 * 
	 * @param em
	 * @param courseInstance
	 * @param poll
	 */
	public List<Group> getAllGroupsForPollOnCourse(EntityManager em,
			CourseInstance courseInstance, Poll poll);
	
	public List<Group> getGroupsWhereUserCanSeeGroupResults(EntityManager em, User user, CourseInstance courseInstance, Set<String> rolesOnCourse);
	
	/**
	 * Dohvati sve "text" odgovore za grupe
	 * @param em
	 * @param poll
	 * @param pollGroups
	 * @return
	 */
	public List<TextAnswer> getAllTextAnswers(EntityManager em, Poll poll,
			List<Group> pollGroups);
	
	/**
	 * Dohvati sve "option" odgovore za grupe
	 * @param em
	 * @param poll
	 * @param pollGroups
	 * @return
	 */
	public List<PollOptionBean> countAllOptionAnswers(EntityManager em,
			Poll poll, List<Group> pollGroups);
	
	/**
	 * Dohvati sve odgovore.
	 * @param em
	 * @param poll
	 * @param singlePollResultsGroups
	 * @return
	 */
	public List<Answer> getAllAnswers(EntityManager em, Poll poll,
			List<Group> singlePollResultsGroups);
	
	/**
	 * Prebroji broj studenata koji su pristupili anketama iz grupa pollGroups
	 * @param em
	 * @param poll
	 * @param pollGroups
	 * @return
	 */
	public long countAnsweredPolls(EntityManager em, Poll poll,
			List<Group> pollGroups);
	
	/**
	 * @param em
	 * @param ap
	 * @param singlePollResultsGroups
	 * @return
	 */
	public AnsweredPoll[] getAnsweredPollNeighbours(EntityManager em, AnsweredPoll ap, List<Group> singlePollResultsGroups);
	
	/**
	 * @param em
	 * @param poll
	 * @param singlePollResultsGroups
	 * @return
	 */
	public AnsweredPoll getFirstAnsweredPoll(EntityManager em, Poll poll, List<Group> singlePollResultsGroups);
}