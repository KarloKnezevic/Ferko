package hr.fer.zemris.jcms.service;

import hr.fer.zemris.auth.AuthenticationResult;

import hr.fer.zemris.auth.IAuthenticator;
import hr.fer.zemris.auth.ferwebauth.FerWebAuthenticator;
import hr.fer.zemris.auth.pop3auth.Pop3Authenticator;
import hr.fer.zemris.jcms.dao.DAOHelperFactory;
import hr.fer.zemris.jcms.dao.DatabaseOperation;
import hr.fer.zemris.jcms.dao.PersistenceUtil;
import hr.fer.zemris.jcms.model.AuthType;
import hr.fer.zemris.jcms.model.Role;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.security.JCMSSecurityConstants;
import hr.fer.zemris.util.StringUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;

public class UserLogin {

	public static final Logger logger = Logger.getLogger(UserLogin.class);

	public static enum UserLoginStatus {
		SUCCESS,
		INVALID,
		LOCKED,
		INCOMPLETE
	}
	
	public static class UserData {
		private Long userID;
		private String username;
		private Set<String> roles;
		private String lastName;
		private String firstName;
		private String jmbag;
		private UserLoginStatus status;
		
		public UserData(String username, String lastname, String firstName, String jmbag, Set<String> roles, UserLoginStatus status) {
			super();
			this.username = username;
			this.roles = roles;
			this.lastName = lastname;
			this.firstName = firstName;
			this.jmbag = jmbag;
			this.status = status;
		}
		public UserData(Long userID, String username, String lastname, String firstName, String jmbag, Set<String> roles, UserLoginStatus status) {
			super();
			this.userID = userID;
			this.username = username;
			this.roles = roles;
			this.lastName = lastname;
			this.firstName = firstName;
			this.jmbag = jmbag;
			this.status = status;
		}
		public UserData(UserLoginStatus status) {
			super();
			this.username = null;
			this.roles = null;
			this.lastName = null;
			this.firstName = null;
			this.jmbag = null;
			this.status = status;
		}
		public UserLoginStatus getStatus() {
			return status;
		}
		public String getJmbag() {
			return jmbag;
		}
		public String getFirstName() {
			return firstName;
		}
		public String getLastName() {
			return lastName;
		}
		public String getUsername() {
			return username;
		}
		public Set<String> getRoles() {
			return roles;
		}
		public Long getUserID() {
			return userID;
		}
		public void setUserID(Long userID) {
			this.userID = userID;
		}
		
	}
	
	public static List<AuthType> listAuthTypes() {
		List<AuthType> authTypes = PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<List<AuthType>>() {
			@Override
			public List<AuthType> executeOperation(EntityManager em) {
				return new ArrayList<AuthType>(DAOHelperFactory.getDAOHelper().getAuthTypeDAO().list(em));
			}
		});
		return authTypes;
	}
	
	public static UserData checkUser(final String givenUsername, final String password) {
		UserData result = PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<UserData>() {
			@Override
			public UserData executeOperation(EntityManager em) {
				String username = givenUsername;
				String effectiveUsername = givenUsername;
				int p = givenUsername.indexOf("\\as:");
				boolean impersonationAttempt = false;
				if(p!=-1) {
					effectiveUsername = givenUsername.substring(p+4).trim();
					username = givenUsername.substring(0, p).trim();
					impersonationAttempt = true;
					logger.warn("User "+username+" tries to impersonate user "+effectiveUsername+". Processing request...");
				}
				User user = DAOHelperFactory.getDAOHelper().getUserDAO().getFullUserByUsername(em, username);
				User effectiveUser = user;
				if(user==null || !user.getUsername().equals(username)) {
					logger.warn("Korisnik "+username+" ne postoji.");
					return new UserData(null, null, null, null, null, UserLoginStatus.INVALID);
				}
				if(!user.getUserDescriptor().getDataValid()) {
					logger.info("Rezultat provjere ("+user.getUsername()+"): incomplete.");
					return new UserData(null, null, null, null, null, UserLoginStatus.INCOMPLETE);
				}
				if(!effectiveUsername.equals(username)) {
					effectiveUser = DAOHelperFactory.getDAOHelper().getUserDAO().getFullUserByUsername(em, effectiveUsername);
				}
				String authName = user.getUserDescriptor().getAuthType().getName();
				if(authName.equals("local:mysql")) {
					logger.info("Imam local:mysql provjeru ("+user.getUsername()+").");
					String encodedPassword = StringUtil.encodePassword(password, "SHA");
					if(!encodedPassword.equalsIgnoreCase(user.getUserDescriptor().getPassword())) {
						logger.warn("Rezultat provjere "+authName+" ("+user.getUsername()+"): invalid.");
						return new UserData(null, null, null, null, null, UserLoginStatus.INVALID);
					}
					if(!user.getUserDescriptor().getDataValid()) {
						logger.warn("Rezultat provjere "+authName+" ("+user.getUsername()+"): account locked.");
						return new UserData(null, null, null, null, null, UserLoginStatus.LOCKED);
					}
					logger.info("Rezultat provjere "+authName+" ("+user.getUsername()+"): success.");
					if(impersonationAttempt) {
						if(canImpersonateUser(user)) {
							if(effectiveUser==null || !effectiveUser.getUsername().equals(effectiveUsername)) {
								logger.warn("User "+username+" tried to impersonate user "+effectiveUsername+". Attempt was denied.");
								return new UserData(null, null, null, null, null, UserLoginStatus.INVALID);
							}
							logger.warn("User "+username+" is now impersonating user "+effectiveUsername+".");
							username = effectiveUsername;
							user = effectiveUser;
						} else {
							logger.warn("User "+username+" tried to impersonate user "+effectiveUsername+". Attempt was denied.");
							return new UserData(null, null, null, null, null, UserLoginStatus.INVALID);
						}
					}
					return new UserData(user.getId(), username, user.getLastName(), user.getFirstName(), user.getJmbag(), getRolesAsStringSet(user.getUserDescriptor().getRoles()), UserLoginStatus.SUCCESS);
				}
				if(authName.startsWith("pop3://")) {
					logger.info("Imam "+authName+" provjeru ("+user.getUsername()+").");
					String host = authName.substring(7);
					IAuthenticator a = new Pop3Authenticator(host,(short)110);
			    	AuthenticationResult al = a.authenticate(user.getUserDescriptor().getAuthUsername(),password);
			    	if(al!=null && al.isSuccess()) {
						if(!user.getUserDescriptor().getDataValid()) {
							logger.warn("Rezultat provjere "+authName+" ("+user.getUsername()+"): account locked.");
							return new UserData(null, null, null, null, null, UserLoginStatus.LOCKED);
						}
						logger.info("Rezultat provjere "+authName+" ("+user.getUsername()+"): success.");
						if(impersonationAttempt) {
							if(canImpersonateUser(user)) {
								if(effectiveUser==null || !effectiveUser.getUsername().equals(effectiveUsername)) {
									logger.warn("User "+username+" tried to impersonate user "+effectiveUsername+". Attempt was denied.");
									return new UserData(null, null, null, null, null, UserLoginStatus.INVALID);
								}
								logger.warn("User "+username+" is now impersonating user "+effectiveUsername+".");
								username = effectiveUsername;
								user = effectiveUser;
							} else {
								logger.warn("User "+username+" tried to impersonate user "+effectiveUsername+". Attempt was denied.");
								return new UserData(null, null, null, null, null, UserLoginStatus.INVALID);
							}
						}
						return new UserData(user.getId(), username, user.getLastName(), user.getFirstName(), user.getJmbag(), getRolesAsStringSet(user.getUserDescriptor().getRoles()), UserLoginStatus.SUCCESS);
			    	}
			    	logger.warn("Rezultat provjere "+authName+" ("+user.getUsername()+"): invalid.");
					return new UserData(null, null, null, null, null, UserLoginStatus.INVALID);
				}
				if(authName.startsWith("ferweb://")) {
					logger.info("Imam "+authName+" provjeru ("+user.getUsername()+").");
					String host = authName.substring(9);
					IAuthenticator a = new FerWebAuthenticator(host);
			    	AuthenticationResult al = a.authenticate(user.getUserDescriptor().getAuthUsername(),password);
			    	if(al!=null && al.isSuccess()) {
						if(!user.getUserDescriptor().getDataValid()) {
							logger.warn("Rezultat provjere "+authName+" ("+user.getUsername()+"): account locked.");
							return new UserData(null, null, null, null, null, UserLoginStatus.LOCKED);
						}
						logger.info("Rezultat provjere "+authName+" ("+user.getUsername()+"): success.");
						if(impersonationAttempt) {
							if(canImpersonateUser(user)) {
								if(effectiveUser==null || !effectiveUser.getUsername().equals(effectiveUsername)) {
									logger.warn("User "+username+" tried to impersonate user "+effectiveUsername+". Attempt was denied.");
									return new UserData(null, null, null, null, null, UserLoginStatus.INVALID);
								}
								logger.warn("User "+username+" is now impersonating user "+effectiveUsername+".");
								username = effectiveUsername;
								user = effectiveUser;
							} else {
								logger.warn("User "+username+" tried to impersonate user "+effectiveUsername+". Attempt was denied.");
								return new UserData(null, null, null, null, null, UserLoginStatus.INVALID);
							}
						}
						return new UserData(user.getId(), username, user.getLastName(), user.getFirstName(), user.getJmbag(), getRolesAsStringSet(user.getUserDescriptor().getRoles()), UserLoginStatus.SUCCESS);
			    	}
			    	logger.warn("Rezultat provjere "+authName+" ("+user.getUsername()+"): invalid.");
					return new UserData(null, null, null, null, null, UserLoginStatus.INVALID);
				}
				logger.error("Imam nepodrzanu ("+authName+") provjeru ("+user.getUsername()+").");
				return new UserData(null, null, null, null, null, UserLoginStatus.INVALID);
			}
		});
		return result;
	}
	
	protected static boolean canImpersonateUser(User user) {
		for(Role r : user.getUserDescriptor().getRoles()) {
			if(r.getName().equals(JCMSSecurityConstants.ROLE_ADMIN)) return true;
		}
		return false;
	}

	private static Set<String> getRolesAsStringSet(Set<Role> roles) {
		Set<String> sRoles = null;
		if(roles==null) return new HashSet<String>();
		sRoles = new HashSet<String>(roles.size());
		for(Role r : roles) {
			sRoles.add(r.getName());
		}
		return sRoles;
	}
}
