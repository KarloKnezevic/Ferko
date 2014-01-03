package hr.fer.zemris.jcms.model;

import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Version;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.CollectionOfElements;

/**
 * Dopuštenja za izvođenje akcija.
 * 
 * @author Ivan Krišto
 */
@Entity
@Table(name="permissions")
@Cache(usage=CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class Permission implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
		
	/** Primarni ključ. */
	private Long id;
	
	/** Skup identifikatora dozvoljenih akcija. */
	private Set<String> actionPermissions;
	
	/** Verzija. Podrška za optimistično zaključavanje. */
	private int version;
	
	/** Opis dozvole (neobavezno, pomaže kod generičkih dozvola). */
	private String description;
	
	/** name + relativePath grupe za koju vrijedi dozvola. */
	private String groupFullPath;
	
	/**
	 * Konstruktor.
	 */
	public Permission() {
		
	}
	
	/**
	 * Konstruktor.
	 * 
	 * @param actionPermissions Skup identifikatora dozvoljenih akcija.
	 */
	public Permission(Set<String> actionPermissions, String groupFullPath) {
		super();
		this.actionPermissions = actionPermissions;
		this.groupFullPath = groupFullPath;
	}
	
	/**
	 * Identifikator.
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
	 * Provjera dozvole nad akcijom određenom actionIdentifierom.
	 * 
	 * @param actionIdentifier Identifikator akcije koju provjeravamo.
	 * @return True ako je akcija dozvoljena, inače false.
	 */
	public boolean checkPermission(String actionIdentifier) {
		return getActionPermissions().contains(actionIdentifier);
	}
	
	/**
	 * Pokriva li ova dozvola grupu određenu putanjom <code>groupPath</code>.
	 * 
	 * @param groupPath Putanja do grupe za koju provjeravamo dozvolu. 
	 * @return True ako dozvola pokriva grupu određenu putanjom <code>groupPath</code>, inače false.
	 */
	public boolean containsGroup(String groupPath) {
		return groupPath.startsWith(getGroupFullPath());
	}
	
	/**
	 * Dohvat dozvola po akciji.
	 * 
	 * @return Skup identifikatora dozvoljenih akcija.
	 */
	@CollectionOfElements(fetch=FetchType.LAZY)
	public Set<String> getActionPermissions() {
		return this.actionPermissions;
	}
	
	/**
	 * Postavljanje dozvola po akciji.
	 * 
	 * @param actionPermissions Novi skup identifikatora dozvoljenih akcija.
	 */
	public void setActionPermissions(Set<String> actionPermissions) {
		this.actionPermissions = actionPermissions;
	}
	
	/**
	 * Verzija. Podrška za optimistično zaključavanje. 
	 * 
	 * @return
	 */
	@Version
	public int getVersion() {
		return this.version;
	}
	
	/**
	 * Postavljanje verzije.
	 * 
	 * @param version Nova verzija.
	 */
	public void setVersion(int version) {
		this.version = version;
	}
	
	/**
	 * Dohvat opisa dozvole.
	 * 
	 * @return Opis dozvole.
	 */
	@Column(nullable=false,length=50)
	public String getDescription() {
		return this.description;
	}
	
	/**
	 * Puna "putanja" do grupe za koju vrijede dozvole.
	 * 
	 * @return Putanja do grupe.
	 */
	@Column(nullable=false,length=100,unique=true)
	public String getGroupFullPath() {
		return this.groupFullPath;
	}
	
	/**
	 * Postavljanje novog opisa dozvole.
	 * 
	 * @param description Novi opis.
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	
	public void setGroupFullPath(String groupFullPath) {
		this.groupFullPath = groupFullPath;
	}
}
