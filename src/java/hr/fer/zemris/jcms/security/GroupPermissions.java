package hr.fer.zemris.jcms.security;

public class GroupPermissions {
	private boolean membersListRetrievable;
	private boolean membersListModifiable;
	private boolean eventsRetrievable;
	private boolean eventsModifiable;
	
	public GroupPermissions(boolean eventsModifiable,
			boolean eventsRetrievable, boolean membersListModifiable,
			boolean membersListRetrievable) {
		super();
		this.eventsModifiable = eventsModifiable;
		this.eventsRetrievable = eventsRetrievable;
		this.membersListModifiable = membersListModifiable;
		this.membersListRetrievable = membersListRetrievable;
	}
	
	public boolean isEventsModifiable() {
		return eventsModifiable;
	}
	public boolean isEventsRetrievable() {
		return eventsRetrievable;
	}
	public boolean isMembersListModifiable() {
		return membersListModifiable;
	}
	public boolean isMembersListRetrievable() {
		return membersListRetrievable;
	}
	
}
