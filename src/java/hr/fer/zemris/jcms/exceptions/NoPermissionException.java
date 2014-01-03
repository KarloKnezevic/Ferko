package hr.fer.zemris.jcms.exceptions;


/**
 * Iznimka koja se baca pri pokušaju izvršavanja akcije za koju korisnik nema
 * pravo na izvođenje.
 * 
 * @author Ivan Krišto
 */
public class NoPermissionException extends Exception {
	
	/**
   * 
   */
  private static final long serialVersionUID = 7772693240554723334L;

  /** Korisničko ime korisnika koji je pokušao izvršiti nedopuštenu akciju. */
	private String username = "";
	
	/** Akcija koju je korisnik pokušao izvršiti. */
	private String actionIdentifier = "";
	
	/** Grupa nad kojom je pokušano izvršavanje akcije. */
	private String groupPath = "";
	
	/**
	 * Konstruktor.
	 * 
	 * @param msg Poruka o greški.
	 */
	public NoPermissionException(String msg) {
		super(msg);
	}

	/**
	 * Konstruktor.
	 * 
	 * @param msg Poruka o greški.
	 * @param cause Uzrok (radi gniježđenja).
	 */
	public NoPermissionException(String msg, Throwable cause) {
		super(msg, cause);
	}
	
	/**
	 * Konstruktor.
	 * 
	 * @param msg Poruka o greški.
	 * @param username Korisničko ime korisnika koji je pokušao izvršiti nedopuštenu akciju.
	 * @param actionIdentifier Akcija koju je korisnik pokušao izvršiti.
	 * @param groupPath Grupa nad kojom je pokušano izvršavanje akcije.
	 */
	public NoPermissionException(String msg, String username,
			String actionIdentifier, String groupPath) {
		super(msg);
		this.actionIdentifier = actionIdentifier;
		this.groupPath = groupPath;
		this.username = username;
	}
	
	/**
	 * Getter actionIdentifiera.
	 * 
	 * @return Akcija koju je korisnik pokušao izvršiti.
	 */
	public String getActionIdentifier() {
		return this.actionIdentifier;
	}
	
	/**
	 * Getter groupPatha.
	 * 
	 * @return Grupa nad kojom je pokušano izvršavanje akcije.
	 */
	public String getGroupPath() {
		return this.groupPath;
	}
	
	/**
	 * Getter usernamea.
	 * 
	 * @return Korisničko ime korisnika koji je pokušao izvršiti nedopuštenu akciju.
	 */
	public String getUsername() {
		return this.username;
	}
}
