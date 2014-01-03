package hr.fer.zemris.jcms.security;

public class GroupSupportedPermission {

	public static enum MarketPlacePlacement {
		/**
		 * Grupa se nalazi ispred burze (dakle, nije pod kontrolom burze)
		 */
		BEFORE_MARKET_PLACE,
		/**
		 * Grupa je burza.
		 */
		IS_MARKET_PLACE,
		/**
		 * Grupa se nalazi iza burze (dakle, je pod kontrolom burze)
		 */
		AFTER_MARKET_PLACE
	}
	
	/**
	 * Kakav je položaj ove grupe u odnosu na burzu?
	 */
	private MarketPlacePlacement placement;
	/**
	 * Može li korisnik mijenjati parametre grupe (tipa naziv i slično).
	 */
	private boolean edit;
	
	/**
	 * Može li je korisnik vidjeti?
	 */
	private boolean view;

	/**
	 * Može li korisnik dodavati podgrupe ovoj grupi?
	 */
	private boolean addSubgroups;

	/**
	 * Može li korisnik obrisati ovu grupu?
	 */
	private boolean delete;

	/**
	 * Može li korisnik uređivati članove grupe?
	 */
	private boolean manageUsers;
	
	/**
	 * Može li korisnik vidjeti članove grupe? Ovo se podrazumijeva ako može uređivati članove grupe.
	 */
	private boolean viewUsers;
	
	/**
	 * Može li korisnik mijenjati/dodavati događaje ove grupe?
	 */
	private boolean manageEvents;
	
	/**
	 * Može li korisnik pregledavati događaje ove grupe? Ovo se podrazumijeva ako može uređivati događaje grupe.
	 */
	private boolean viewEvents;

	/**
	 * Može li korisnik vidjeti vlasnike grupe? 
	 */
	private boolean viewGroupOwners;
	
	/**
	 * Može li korisnik uređivati vlasnike grupe?
	 */
	private boolean manageGroupOwners;
	

	/**
	 * Može li korisnik vidjeti vlasnike grupe? 
	 */
	public boolean getCanViewGroupOwners() {
		return viewGroupOwners;
	}
	public void setCanViewGroupOwners(boolean viewGroupOwners) {
		this.viewGroupOwners = viewGroupOwners;
	}
	
	/**
	 * Može li korisnik uređivati vlasnike grupe?
	 */
	public boolean getCanManageGroupOwners() {
		return manageGroupOwners;
	}
	public void setCanManageGroupOwners(boolean manageGroupOwners) {
		this.manageGroupOwners = manageGroupOwners;
	}
	
	/**
	 * Može li korisnik mijenjati parametre grupe (tipa naziv i slično).
	 */
	public boolean getCanEdit() {
		return edit;
	}

	public void setCanEdit(boolean edit) {
		this.edit = edit;
	}

	/**
	 * Može li je korisnik vidjeti?
	 */
	public boolean getCanView() {
		return view;
	}

	public void setCanView(boolean view) {
		this.view = view;
		if(!view) this.addSubgroups = false;
	}

	/**
	 * Može li korisnik dodavati podgrupe ovoj grupi?
	 */
	public boolean getCanAddSubgroups() {
		return addSubgroups;
	}

	public void setCanAddSubgroups(boolean addSubgroups) {
		this.addSubgroups = addSubgroups;
		if(addSubgroups) this.view = true;
	}

	/**
	 * Može li korisnik obrisati ovu grupu?
	 */
	public boolean getCanDelete() {
		return delete;
	}
	public void setCanDelete(boolean canDelete) {
		this.delete = canDelete;
	}
	
	
	/**
	 * Može li korisnik uređivati članove grupe?
	 */
	public boolean getCanManageUsers() {
		return manageUsers;
	}

	public void setCanManageUsers(boolean manageUsers) {
		this.manageUsers = manageUsers;
		if(manageUsers) this.viewUsers = true;
	}

	/**
	 * Može li korisnik vidjeti članove grupe? Ovo se podrazumijeva ako može uređivati članove grupe.
	 */
	public boolean getCanViewUsers() {
		return viewUsers;
	}

	public void setCanViewUsers(boolean viewUsers) {
		this.viewUsers = viewUsers;
		if(!viewUsers) this.manageUsers = false;
	}

	/**
	 * Može li korisnik mijenjati/dodavati događaje ove grupe?
	 */
	public boolean getCanManageEvents() {
		return manageEvents;
	}

	public void setCanManageEvents(boolean manageEvents) {
		this.manageEvents = manageEvents;
		if(manageEvents) this.viewEvents = true;
	}

	/**
	 * Može li korisnik pregledavati događaje ove grupe? Ovo se podrazumijeva ako može uređivati događaje grupe.
	 */
	public boolean getCanViewEvents() {
		return viewEvents;
	}

	public void setCanViewEvents(boolean viewEvents) {
		this.viewEvents = viewEvents;
		if(!viewEvents) this.manageEvents = false;
	}
	
	/**
	 * Kakav je položaj ove grupe u odnosu na burzu?
	 */
	public MarketPlacePlacement getPlacement() {
		return placement;
	}
	public void setPlacement(MarketPlacePlacement placement) {
		this.placement = placement;
	}
}
