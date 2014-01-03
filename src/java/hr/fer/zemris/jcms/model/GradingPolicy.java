package hr.fer.zemris.jcms.model;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.PostLoad;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

/**
 * Politika ocjenjivanja na kolegiju. Nalazi se u 1-na-1 relaciji s primjerkom kolegija.
 * 
 * @author marcupic
 */
@Entity
@Table(name="grading_policies")
public class GradingPolicy implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private Long id;
	private GradesVisibility gradesVisibility = GradesVisibility.NOT_VISIBLE;
	private boolean gradesValid;
	private boolean gradesLocked;
	private String rules;
	private String policyImplementation;
	private String gradingStatSer;
	private GradingStat gradingStat;
	private Date termDate;
	
	/**
	 * Primarni ključ.
	 * 
	 * @return identifikator
	 */
	@Id @GeneratedValue
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	/**
	 * Jesu li ocjene vidljive, i ako jesu, onda kome?
	 * 
	 * @return vidljivost ocjena
	 */
	@Enumerated(EnumType.STRING)
	public GradesVisibility getGradesVisibility() {
		return gradesVisibility;
	}
	public void setGradesVisibility(GradesVisibility gradesVisibility) {
		this.gradesVisibility = gradesVisibility;
	}
	
	/**
	 * Jesu li podijeljene ocjene valjane? Ako nisu, ne prikazivati ih!
	 * 
	 * @return <code>true</code> ako su ocjene valjane, <code>false</code> inače
	 */
	public boolean getGradesValid() {
		return gradesValid;
	}
	public void setGradesValid(boolean gradesValid) {
		this.gradesValid = gradesValid;
	}

	/**
	 * Jesu li ocjene zaključane? Ako jesu, više ih se ne može mijenjati
	 * ponovnim pokretanjem ocjenjivanja.
	 * 
	 * @return <code>true</code> ako su ocjene zaključane, <code>false</code> inače
	 */
	public boolean getGradesLocked() {
		return gradesLocked;
	}
	public void setGradesLocked(boolean gradesLocked) {
		this.gradesLocked = gradesLocked;
	}
	
	/**
	 * Po kojim se pravilima radi ocjenjivanje? Ovo je mjesto gdje odabrana implementacija
	 * može pohraniti svoje podatke.
	 * 
	 * @return podatci potrebni na ocjenjivanje
	 */
	@Column(length=1024*1024, nullable=true)
	public String getRules() {
		return rules;
	}
	public void setRules(String rules) {
		this.rules = rules;
	}
	
	/**
	 * <p>Koja je implementacija politike ocjenjivanja? Ovo je kao polje "vrsta"
	 * kod izracuna bodova provjere. Jednom kada je implementacija odabrana,
	 * podaci su joj pohranjeni u polju rules kao string (sama implementacija
	 * zadužena je za interpretaciju i uređivanje tih podataka).</p>
	 * 
	 * <p>Trenutno su podržane sljedeće politike ocjenjivanja:</p>
	 * <ul>
	 *  <li><code>SC</code> - definira se broj bodova za ocjene 3, 4 i 5. Ocjena 2 izvodi se iz
	 *                        prolaza na povezanoj provjeri.</li>
	 *  <li><code>SP</code> - definira se postotak studenata koji moraju dobiti 2, 3, 4 i 5 od
	 *                        onih koji su prošli. Ovo je trenutno preporučeno na FER-u, uz 
	 *                        postotke 15%, 35%, 35% i 15%.</li>
	 * </ul>
	 * 
	 * <p>Svaka odabana politika ocjenjivanja može korisnika pitati za dodatne parametre (primjerice,
	 * iz koje se provjere uzimaju bodovi i sl). Te parametre zapisuje si u property {@link #rules}.</p>
	 * 
	 * @return odabrana implementacija ili <code>null</code> ako implementacija
	 *         nije odabrana
	 * 
	 */
	@Column(length=20, nullable=true)
	public String getPolicyImplementation() {
		return policyImplementation;
	}

	public void setPolicyImplementation(String policyImplementation) {
		this.policyImplementation = policyImplementation;
	}
	
	/**
	 * Ovo je serijalizirani oblik generalne statistike nastale u postupku
	 * raspodjele ocjena. Za rad se preporuča uporaba automatski deserijaliziranog
	 * objekta {@link #getGradingStat()}.
	 * 
	 * @return serijalizirani oblik statistike
	 */
	public String getGradingStatSer() {
		return gradingStatSer;
	}
	public void setGradingStatSer(String gradingStatSer) {
		this.gradingStatSer = gradingStatSer;
	}
	
	/**
	 * Ovaj objekt predstavlja deserijalizirani oblik statističkih podataka.
	 * 
	 * @return deserijalizirani oblik statističkih podataka
	 */
	@Transient
	public GradingStat getGradingStat() {
		return gradingStat;
	}
	public void setGradingStat(GradingStat gradingStat) {
		this.gradingStat = gradingStat;
	}
	
	/**
	 * Metoda koja serijalizira statistiku.
	 */
	@PrePersist @PreUpdate
	public void serializeGS() {
		if(gradingStat!=null) {
			setGradingStatSer(gradingStat.serialize());
		}
	}
	
	/**
	 * Metoda koja deserijalizira statistiku, ako ista postoji.
	 */
	@PostLoad
	public void deserializeGS() {
		gradingStat = GradingStat.deserialize(gradingStatSer);
	}
	
	/**
	 * Datum ispitnog roka. Služi za eksport u ISVU.
	 * 
	 * @return datum ispitnog roka
	 */
	@Temporal(TemporalType.DATE)
	@Column(nullable=true)
	public Date getTermDate() {
		return termDate;
	}
	public void setTermDate(Date termDate) {
		this.termDate = termDate;
	}
}
