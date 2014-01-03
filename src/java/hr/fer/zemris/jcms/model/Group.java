package hr.fer.zemris.jcms.model;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

/**
 * Model jedne grupe. Grupa je jednoznačno određena na dva jednako-vrijedna načina.
 * <ul>
 * <li><i>Primarnim ključem</i> - svaka grupa ima svoj jedinstveni automatski generirani primarni ključ.</li>
 * <li><i>Kombinacijom compositeCourseID + relativePath</i> - svaka grupa ima svoju jedinstvenu kombinaciju
 *     ovih specifikatora.</li>
 * </ul>
 * <h2>compositeCourseID</h2>
 * compositeCourseID je oblika "2007Z/19674" tj. sastoji se od akademske godine, semestra i ISVU sifre predmeta.
 * Zahvaljujući ovom identifikatoru jednostavno se mogu dohvatiti apsolutno sve grupe i podgrupe na tom primjerku
 * kolegija.
 * <h2>relativePath</h2>
 * relativePath je oblika i/j/k/... Ako je prazan, radi se o primarnoj (vršnoj) grupi primjerka kolegija, i ta grupa
 * ne smije imati postavljenog roditelja. Neke vrijednosti za "i" su propisane, kako slijedi:
 * <ul>
 * <li><i>Vrijednost 0</i> - označava grupu za predavanja. Ovu grupu ujedno treba tretirati kao glavnu i referentnu
 * grupu za kolegij. Samo korisnici koji su tu smiju se pojavljivati i u drugim grupama.</li>
 * <li><i>Vrijednost 1</i> - označava grupu za laboratorijske vježbe i ne bi se smjelo koristiti u druge svrhe.
 * <li><i>Vrijednost 2 do 10</i> - rezervirano za buduće potrebe.
 * <li><i>Vrijednost 11 na više</i> - može se slobodno koristiti za proizvoljne grupacije studenata.
 * </ul>
 * Primjeri relativnih identifikatora i njihovo značenje dani su u nastavku.
 * <h2>Primjer grupa za predavanja</h2>
 * Grupa čiji je relativePath="0" je glavna nadgrupa za predavanja i ona sama ne bi smjela
 * sadržavati korisnike; korisnici su smješteni unutra u podgrupe, a nad samom grupom "0" može
 * raditi burza grupa. Dubina relativne staze trebala bi u ovom slučaju iznositi najviše 2.
 * <table border="1">
 * <tr><th>relativePath</th><th>name</th><th>Opis</th></tr>
 * <tr><td>0/0</td><td></td><td>Posebna namjena - za neraspoređene studente na kolegiju</td></tr>
 * <tr><td>0/1</td><td>2.E1</td><td>Grupa za predavanja 2.E1</td></tr>
 * <tr><td>0/2</td><td>2.E2</td><td>Grupa za predavanja 2.E2</td></tr>
 * <tr><td>0/3</td><td>2.R1</td><td>Grupa za predavanja 2.R1</td></tr>
 * <tr><td>0/4</td><td>2.R1</td><td>Grupa za predavanja 2.R2</td></tr>
 * <table>
 * <h2>Primjer grupa za labose</h2>
 * Grupa čiji je relativePath="1" je glavna nadgrupa za predavanja i ona sama ne bi smjela
 * sadržavati korisnike. Njezine podgrupe su opet virtualne grupe koje odgovaraju pojedinim ciklusima
 * (ili tjednima) laboratorijskih vježbi. Tek su grupe na dubini 3 grupe koje doista sadrže korisnike.
 * Burza se ovdje može definirati nad grupama na razini 2 (grupama ciklusa).
 * <table border="1">
 * <tr><th>relativePath</th><th>name</th><th>Opis</th></tr>
 * <tr><td>1/0</td><td>1. ciklus laboratorijskih vježbi</td><td>Virtualna grupa za 1. ciklus lab. vježbi.</td></tr>
 * <tr><td>1/0/0</td><td>PON08-A101</td><td>Konkretan termin lab. vježbe 1. ciklusa.</td></tr>
 * <tr><td>1/0/1</td><td>PON10-A101</td><td>Konkretan termin lab. vježbe 1. ciklusa.</td></tr>
 * <tr><td>1/1</td><td>2. ciklus laboratorijskih vježbi</td><td>Virtualna grupa za 2. ciklus lab. vježbi.</td></tr>
 * <tr><td>1/1/0</td><td>PON08-A101</td><td>Konkretan termin lab. vježbe 2. ciklusa.</td></tr>
 * <tr><td>1/1/1</td><td>PON10-A101</td><td>Konkretan termin lab. vježbe 2. ciklusa.</td></tr>
 * <table>
 * 
 * @author marcupic
 */
@NamedQueries({
    @NamedQuery(name="Group.findAllSemUsers",query="select distinct ug.user from Group as g, IN(g.users) ug where g.compositeCourseID LIKE :compositeCourseID AND relativePath LIKE '0/%'"),
    @NamedQuery(name="Group.findForUser",query="select g from Group as g, IN(g.users) ug where g.compositeCourseID LIKE :compositeCourseID AND relativePath LIKE '0/%' AND ug.user=:user"),
    @NamedQuery(name="Group.findForUser2",query="select g from Group as g, IN(g.users) ug where g.compositeCourseID=:compositeCourseID AND ug.user=:user"),
    @NamedQuery(name="Group.findForUser3",query="select g from Group as g, IN(g.users) ug where g.compositeCourseID=:compositeCourseID AND g.relativePath LIKE :relativePath AND ug.user=:user"),
    @NamedQuery(name="Group.getCoarseGroupStat2",query="select new hr.fer.zemris.jcms.beans.ext.CoarseGroupStat2(g.id, g.name, g.compositeCourseID, g.relativePath, g.capacity, count(ug.id)) from Group as g LEFT OUTER JOIN g.users ug where g.compositeCourseID LIKE :compositeCourseID AND g.relativePath LIKE :relativePath GROUP BY g.id, g.name, g.compositeCourseID, g.relativePath, g.capacity"),
    @NamedQuery(name="Group.getGroupStat",query="select g.id, ug.tag, count(*) from Group as g, IN(g.users) ug where g.compositeCourseID=:compositeCourseID AND g.relativePath LIKE :relativePath GROUP BY g.id, ug.tag"),
    @NamedQuery(name="Group.getCoarseGroupStat",query="select g.id, count(*) from Group as g, IN(g.users) ug where g.compositeCourseID=:compositeCourseID AND g.relativePath LIKE :relativePath GROUP BY g.id"),
    @NamedQuery(name="Group.findUGForUser",query="select ug from UserGroup as ug where ug.group.compositeCourseID=:compositeCourseID AND ug.group.relativePath LIKE :relativePath AND ug.user=:user"),
    @NamedQuery(name="Group.findTopLevel",query="select g from Group as p, in(p.subgroups) g where p.compositeCourseID=:compositeCourseID AND p.parent IS NULL"),
    @NamedQuery(name="Group.findPrimary",query="select g from Group as g where g.compositeCourseID=:compositeCourseID AND g.relativePath=''"),
    @NamedQuery(name="Group.findGroup",query="select g from Group as g where g.compositeCourseID=:compositeCourseID AND g.relativePath=:relativePath"),
    @NamedQuery(name="Group.findSubgroups",query="select g from Group as g where g.compositeCourseID=:compositeCourseID AND relativePath LIKE :relativePath"),
    @NamedQuery(name="Group.findSubgroups2",query="select g from Group as g where g.compositeCourseID=:compositeCourseID AND (relativePath LIKE :likeRelativePath OR relativePath=:eqRelativePath)"),
    @NamedQuery(name="Group.findSubgroups3",query="select g from Group as g where g.compositeCourseID LIKE :compositeCourseID AND (relativePath LIKE :likeRelativePath OR relativePath=:eqRelativePath)"),
    @NamedQuery(name="Group.findCourseUsers",query="select distinct u.user from Group as g JOIN g.users as u where g.compositeCourseID=:compositeCourseID AND relativePath LIKE '0/%'"),
    @NamedQuery(name="Group.findGroupTreeUserGroups",query="select u from Group as g JOIN g.users as u where g.compositeCourseID=:compositeCourseID AND (relativePath=:eqRelativePath OR relativePath LIKE :likeRelativePath)"),
    @NamedQuery(name="Group.findGroupTreeUsers",query="select distinct u.user from Group as g JOIN g.users as u where g.compositeCourseID=:compositeCourseID AND (relativePath=:eqRelativePath OR relativePath LIKE :likeRelativePath)"),
    @NamedQuery(name="Group.findGroupTreeUsers2",query="select distinct u.user from Group as g JOIN g.users as u where g.compositeCourseID=:compositeCourseID AND (relativePath=:eqRelativePath OR relativePath LIKE :likeRelativePath) AND u.user.lastName LIKE :lastName"),
    @NamedQuery(name="Group.findGroupTreeUsers3",query="select distinct u.user from Group as g JOIN g.users as u where g.compositeCourseID=:compositeCourseID AND (relativePath=:eqRelativePath OR relativePath LIKE :likeRelativePath) AND u.user.lastName=:lastName AND u.user.firstName LIKE :firstName"),
    @NamedQuery(name="Group.findGroupTreeUsers4",query="select distinct u.user from Group as g JOIN g.users as u where g.compositeCourseID=:compositeCourseID AND (relativePath=:eqRelativePath OR relativePath LIKE :likeRelativePath) AND u.user.lastName=:lastName AND u.user.firstName=:firstName AND u.user.jmbag LIKE :jmbag"),
    @NamedQuery(name="Group.isUserOnCourseStaff",query="select u from Group as g JOIN g.users as u where g.compositeCourseID=:compositeCourseID AND relativePath LIKE '3/%' and u.user=:user"),
    @NamedQuery(name="Group.findCourseStaffUsers",query="select u from Group as g JOIN g.users as u where g.compositeCourseID=:compositeCourseID AND relativePath LIKE '3/%'"),
    @NamedQuery(name="Group.findMarketPlaceRootsOnCourse",query="select g from Group as g where g.compositeCourseID=:compositeCourseID and managedRoot=true"),
    @NamedQuery(name="Group.findCourseAssessmentGroups",query="select g from Group as g where g.compositeCourseID=:compositeCourseID AND g.relativePath LIKE '4/%'"),
    @NamedQuery(name="Group.listCoursesForUser",query="select distinct g.compositeCourseID from Group as g, IN(g.users) ug where g.relativePath LIKE :relativePath AND ug.user=:user")
})
@Entity
@Table(name="groups",uniqueConstraints={
		// Ne mogu postojati dvije grupe s istim compositeCourseID i relativePath
		@UniqueConstraint(columnNames={"compositeCourseID","relativePath"}),
		// Roditelj ne može imati dva djeteta koja se zovu isto
		@UniqueConstraint(columnNames={"parent_id","name"})
})
@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Group implements Serializable {
	
	private static final long serialVersionUID = 1L;

	private Long id;
	private String compositeCourseID;
	private String relativePath;
	private String name;
	private Group parent;
	private Set<Group> subgroups = new HashSet<Group>();
	private boolean managedRoot;
	private int capacity = -1;
	private boolean enteringAllowed = false;
	private boolean leavingAllowed = false;
	private Set<GroupWideEvent> events = new HashSet<GroupWideEvent>();
	private Set<UserGroup> users = new HashSet<UserGroup>();
	private String mpSecurityTag;
	private MarketPlace marketPlace;
	
	//private String place;  ==> Ove stvari idu u GroupEvent koji moze biti povezan s grupom
	//private Date time;     ==> Ove stvari idu u GroupEvent koji moze biti povezan s grupom
	//private int duration;  ==> Ove stvari idu u GroupEvent koji moze biti povezan s grupom

	public Group() {
	}
	
	/**
	 * Identifikator grupe.
	 * @return
	 */
	@Id @GeneratedValue
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * Ime grupe.
	 * @return
	 */
	@Column(nullable=false,length=50,unique=false)
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Je li ovo grupa čiju djecu može razmještati burza? Ako je true,
	 * burza će moći djelovati nad djecom.
	 * 
	 * @return
	 */
	public boolean isManagedRoot() {
		return managedRoot;
	}
	public void setManagedRoot(boolean managedRoot) {
		this.managedRoot = managedRoot;
	}

	/**
	 * Grupa roditelj ove grupe.
	 * @return
	 */
	@ManyToOne(fetch=FetchType.LAZY)
	public Group getParent() {
		return parent;
	}
	public void setParent(Group parent) {
		this.parent = parent;
	}

	/**
	 * Podgrupe ove grupe (djeca).
	 * @return
	 */
	@OneToMany(fetch=FetchType.LAZY,cascade={CascadeType.PERSIST,CascadeType.REMOVE},mappedBy="parent")
	public Set<Group> getSubgroups() {
		return subgroups;
	}
	public void setSubgroups(Set<Group> subgroups) {
		this.subgroups = subgroups;
	}

	@OneToOne(cascade=CascadeType.PERSIST,fetch=FetchType.EAGER)
	@Fetch(FetchMode.SELECT)
	@JoinColumn(nullable=true)
	public MarketPlace getMarketPlace() {
		return marketPlace;
	}
	public void setMarketPlace(MarketPlace marketPlace) {
		this.marketPlace = marketPlace;
	}
	
	/**
	 * Kompozitni identifikator kolegija. Za detalje vidi opis samog razreda {@linkplain Group}.
	 * @return
	 */
	@Column(nullable=false,length=16,unique=false)
	public String getCompositeCourseID() {
		return compositeCourseID;
	}
	public void setCompositeCourseID(String compositeCourseID) {
		this.compositeCourseID = compositeCourseID;
	}

	/**
	 * Relativna staza grupe na kolegiju. Za detalje vidi opis samog razreda {@linkplain Group}.
	 * @return
	 */
	@Column(nullable=false,length=50,unique=false)
	public String getRelativePath() {
		return relativePath;
	}
	public void setRelativePath(String relativePath) {
		this.relativePath = relativePath;
	}
	
	/**
	 * Dohvat pune "putanje" grupe.
	 * 
	 * @return Za grupu sa CompositeCourseID-om 2007Z/19674
	 * i relative pathom 0/1, full path je: 2007Z/19674/0/1
	 */
	@Transient
	public String getFullPath() {
		return getCompositeCourseID() + "/" + getRelativePath(); 
	}

	/**
	 * QuestionTag koji je pridijeljen ovoj grupi. Može biti i null. Služi za
	 * omogućavanje fine kontrole tko može kuda, temeljeći se na oznakama
	 * grupa. Ne smije sadržavati znakove: '?', '#', ',', '/', ':'.
	 * 
	 * @return
	 */
	@Column(length=10,nullable=true)
	public String getMpSecurityTag() {
		return mpSecurityTag;
	}
	public void setMpSecurityTag(String mpSecurityTag) {
		this.mpSecurityTag = mpSecurityTag;
	}
	
	/**
	 * Kapacitet grupe. Ako je -1, ograničenje nije postavljeno. Ako je 0 ili više,
	 * to je limit koji se ne bi smio preći. Potrebno je zbog burze grupa.
	 * @return
	 */
	public int getCapacity() {
		return capacity;
	}
	public void setCapacity(int capacity) {
		this.capacity = capacity;
	}

	/**
	 * Smije li burza dopustiti korisnicima iz neke druge grupe da uđu u ovu grupu?
	 * @return
	 */
	public boolean isEnteringAllowed() {
		return enteringAllowed;
	}
	public void setEnteringAllowed(boolean enteringAllowed) {
		this.enteringAllowed = enteringAllowed;
	}

	/**
	 * Smije li burza dopustiti korisnicima iz ove grupe izlazak?
	 * @return
	 */
	public boolean isLeavingAllowed() {
		return leavingAllowed;
	}
	public void setLeavingAllowed(boolean leavingAllowed) {
		this.leavingAllowed = leavingAllowed;
	}

	/**
	 * Popis događaja koji su povezani s ovom grupom. U slučaju predavanja,
	 * ovoga se očekuje više. U slučaju grupa za lab. vježbe, očekuje se da će
	 * svaka grupa biti povezana s jednim terminom (događajem).
	 * @return
	 */
	@ManyToMany(mappedBy="groups",fetch=FetchType.LAZY)
	public Set<GroupWideEvent> getEvents() {
		return events;
	}
	public void setEvents(Set<GroupWideEvent> events) {
		this.events = events;
	}

	// Ovako je to prije bilo mapirano; sada idemo sami upravljati ovom vezom.
	// @ManyToMany(fetch=FetchType.LAZY)
	// @JoinTable(
	//		name="user_groups",
	//		joinColumns=@JoinColumn(name="group_id",referencedColumnName="id"),
	//		inverseJoinColumns=@JoinColumn(name="user_id",referencedColumnName="id")
	// )
	/**
	 * Popis korisnika koji su u ovoj grupi, uz eventualni poredak.
	 * @return korisnici
	 */
	@OneToMany(fetch=FetchType.LAZY,mappedBy="group",cascade={CascadeType.PERSIST,CascadeType.REMOVE})
	public Set<UserGroup> getUsers() {
		return users;
	}
	public void setUsers(Set<UserGroup> users) {
		this.users = users;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((getCompositeCourseID() == null) ? 0 : getCompositeCourseID()
						.hashCode());
		result = prime * result
				+ ((getRelativePath() == null) ? 0 : getRelativePath().hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof Group))
			return false;
		final Group other = (Group) obj;
		if (getCompositeCourseID() == null) {
			if (other.getCompositeCourseID() != null)
				return false;
		} else if (!getCompositeCourseID().equals(other.getCompositeCourseID()))
			return false;
		if (getRelativePath() == null) {
			if (other.getRelativePath() != null)
				return false;
		} else if (!getRelativePath().equals(other.getRelativePath()))
			return false;
		return true;
	}

	public int calcTotalNumberOfUsers() {
		int n = getUsers().size();
		for(Group g : getSubgroups()) {
			n += g.calcTotalNumberOfUsers();
		}
		return n;
	}
	
	@Override
	public String toString() {
		return "Name: "+getName()+", rp="+getRelativePath()+", ccid"+getCompositeCourseID()+", id="+getId();
	}
}
