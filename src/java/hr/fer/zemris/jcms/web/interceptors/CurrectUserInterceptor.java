package hr.fer.zemris.jcms.web.interceptors;

import hr.fer.zemris.jcms.JCMSSettings;
import hr.fer.zemris.jcms.dao.PersistenceUtil;
import hr.fer.zemris.jcms.locking.LockPath;
import hr.fer.zemris.jcms.locking.UnlockException;
import hr.fer.zemris.jcms.security.JCMSSecurityManagerFactory;
import hr.fer.zemris.jcms.service2.BasicServiceSupport;
import hr.fer.zemris.jcms.web.actions.Ext2ActionSupport;
import hr.fer.zemris.jcms.web.actions.ExtendedActionSupport;
import hr.fer.zemris.jcms.web.actions.annotations.DataResultMapping;
import hr.fer.zemris.jcms.web.actions.annotations.Struts2ResultMapping;
import hr.fer.zemris.jcms.web.actions.annotations.WebClass;
import hr.fer.zemris.jcms.web.actions.annotations.WebMethodInfo;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageContainer;
import hr.fer.zemris.jcms.web.interceptors.data.CurrentUser;
import hr.fer.zemris.jcms.web.interceptors.data.CurrentUserAware;
import hr.fer.zemris.jcms.web.interceptors.data.CurrentUserImpl1;
import hr.fer.zemris.jcms.web.interceptors.data.DelayedMessagesAware;
import hr.fer.zemris.jcms.web.interceptors.data.DelayedMessagesStore;
import hr.fer.zemris.jcms.web.navig.DefaultNavigationBuilder;
import hr.fer.zemris.jcms.web.navig.Navigation;
import hr.fer.zemris.jcms.web.navig.NavigationBuilder;
import hr.fer.zemris.jcms.web.navig.TextNavigationItem;
import hr.fer.zemris.jcms.web.navig.builders.BuilderDefault;
import hr.fer.zemris.jcms.web.support.TransactionalMethodSupport;
import hr.fer.zemris.util.StringUtil;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.Set;

import javax.persistence.EntityManager;

import ognl.Ognl;
import ognl.OgnlException;

import com.opensymphony.xwork2.ActionInvocation;
import com.opensymphony.xwork2.interceptor.Interceptor;
import com.opensymphony.xwork2.interceptor.PreResultListener;

public class CurrectUserInterceptor implements Interceptor {

	private static final long serialVersionUID = 1L;

	@Override
	public void destroy() {
	}

	@Override
	public void init() {
	}

	@SuppressWarnings("unchecked")
	@Override
	public String intercept(ActionInvocation invocation) throws Exception {
		Long userID = null;
		Object action = invocation.getAction();
		if(action instanceof CurrentUserAware) {
			Map<?,?> map = invocation.getInvocationContext().getSession();
			userID = (Long)map.get("jcms_currentUserID");
			String username = (String)map.get("jcms_currentUserUsername");
			String firstName = (String)map.get("jcms_currentUserFirstName");
			String lastName = (String)map.get("jcms_currentUserLastName");
			String jmbag = (String)map.get("jcms_currentUserJmbag");
			Set<String> roles = (Set<String>)map.get("jcms_currentUserRoles");
			if(username != null) {
				CurrentUserAware a = (CurrentUserAware)action;
				CurrentUser currentUser = new CurrentUserImpl1(userID, username, firstName, lastName, jmbag, roles);
				a.setCurrentUser(currentUser);
			}
		}
		if(action instanceof DelayedMessagesAware) {
			Map<?,?> map = invocation.getInvocationContext().getParameters();
			Object dmsgid_obj = map.get("dmsgid");
			String dmsgid_key = null;
			if(dmsgid_obj != null) {
				if(dmsgid_obj instanceof String[]) {
					String[] array = (String[])dmsgid_obj;
					if(array.length>0) {
						dmsgid_key = array[0];
					}
				}  else if(dmsgid_obj instanceof String) {
					dmsgid_key = (String)dmsgid_obj;
				}
			}
			if(dmsgid_key!=null) {
				Map<?,?> context = invocation.getInvocationContext().getApplication();
				DelayedMessagesStore messageStore = (DelayedMessagesStore)context.get("jcms.messageStore");
				IMessageContainer container = messageStore.getAndRemove(dmsgid_key);
				if(container!=null) {
					DelayedMessagesAware a = (DelayedMessagesAware)action;
					a.setDelayedMessagesContainer(container);
				}
			}
		}
		Ext2ActionSupport ext2 = (action instanceof Ext2ActionSupport) ? (Ext2ActionSupport)action : null;
		if(ext2!=null) {
			ext2.constructData();
			WebClass dc = action.getClass().getAnnotation(WebClass.class);
			String methodName = invocation.getProxy().getMethod();
			if(methodName==null) methodName = "execute";
			Method m = null;
			try {
				m = action.getClass().getMethod(methodName, (Class<?>[])null);
			} catch(NoSuchMethodException ex) {
				throw new IllegalArgumentException("Metoda "+methodName+" u razredu "+action.getClass().getName()+" ne postoji!");
			}
			WebMethodInfo wmi = m.getAnnotation(WebMethodInfo.class);
			if(wmi==null) {
				throw new IllegalArgumentException("Metodi "+methodName+" u razredu "+action.getClass().getName()+" nedostaje anotacija "+WebMethodInfo.class.getName()+"!");
			}
			return obradiEx2(invocation, ext2, dc, wmi, userID);
		} else {
			try {
				String res = invocation.invoke();
				return res;
			} finally {
				try {
					if(JCMSSettings.getSettings().getActivityReporter().isSessionOpen()) {
						System.out.println("Found forgotten activities. Doing activity reporter rollback.");
						JCMSSettings.getSettings().getActivityReporter().rollbackAndCloseSession();
					}
				} catch(Exception ignorable) {}
				try { JCMSSecurityManagerFactory.getManager().close(); } catch(Exception ignorable) {}
			}
		}
	}

	private static final Class<?>[] setLockedPathParams = new Class[] {LockPath.class};
	
	private String obradiEx2(ActionInvocation invocation, Ext2ActionSupport<?> ext2, WebClass dc, WebMethodInfo wmi, Long userID) throws Exception {
		String res = null;
		boolean skip = false;
		boolean closeImmediately = wmi.transactionalMethod().closeImmediately();
		LockPath lockPath = null;
		if(!wmi.lockPath().isEmpty()) {
			try {
				if(userID==null) {
					System.out.println("Deklarativno zaključavanje nije dozvoljeno neautoriziranim korisnicima!");
					invocation.setResultCode(ExtendedActionSupport.NOT_LOGGED_IN);
					return ExtendedActionSupport.NOT_LOGGED_IN;
				}
				String lp = buildLockPath(wmi.lockPath(), invocation.getInvocationContext().getParameters());
				if(lp!=null) {
					lockPath = new LockPath(lp);
				} else {
					System.out.println("Greška pri izgradnji staze za zaključavanje: "+wmi.lockPath());
					invocation.setResultCode(ExtendedActionSupport.SHOW_FATAL_MESSAGE);
					return ExtendedActionSupport.SHOW_FATAL_MESSAGE;
				}
			} catch(Exception ex) {
				ex.printStackTrace();
				invocation.setResultCode(ExtendedActionSupport.SHOW_FATAL_MESSAGE);
				return ExtendedActionSupport.SHOW_FATAL_MESSAGE;
			}
		}
		Ext2PreResultListener listener = new Ext2PreResultListener(invocation, ext2, closeImmediately, dc, wmi, lockPath);
		invocation.addPreResultListener(listener);
		if(lockPath!=null) {
			JCMSSettings.getSettings().getLockManager().acquireLock(lockPath);
		}
		try {
			EntityManager em = PersistenceUtil.getEntityManager();
			try {
				PersistenceUtil.beginTransaction();
				TransactionalMethodSupport.set(em);
				try {
					if(wmi.loginCheck()) {
				    	String check = ext2.checkUser(null, true);
				    	if(check != null) {
				    		res = check;
				    		skip = true;
							invocation.setResultCode(res);
				    	}
					}
					if(!skip) {
						if(ext2.getCurrentUser()!=null) {
							if(!BasicServiceSupport.fillCurrentUser(em, ext2.getData(), ext2.getCurrentUser().getUserID(), "Error.noPermission", AbstractActionData.RESULT_FATAL)) {
								res = Ext2ActionSupport.NOT_LOGGED_IN;
								skip = true;
								invocation.setResultCode(res);
							}
						}
					}
					if(!skip && lockPath!=null) {
						try {
							Method m2 = ext2.getData().getClass().getMethod("setLockPath", setLockedPathParams);
							m2.invoke(ext2.getData(), lockPath);
						} catch(Exception ignorable) {
							// Ako podatkovni objekt nema set metode, to znaci da ga ovo ne zanima; to je OK.
						}
					}
					if(!skip) {
						if(ext2.getCurrentUser()!=null) { 
							JCMSSecurityManagerFactory.getManager().init(ext2.getData().getCurrentUser(), em);
						}
						res = invocation.invoke();
					}
				} finally {
					try { JCMSSecurityManagerFactory.getManager().close(); } catch(Exception ignorable) {}
				}
				if(!listener.transClosed) {
					PersistenceUtil.commitTransaction();
					try {
						if(JCMSSettings.getSettings().getActivityReporter().isSessionOpen()) {
							JCMSSettings.getSettings().getActivityReporter().commitAndCloseSession();
						}
					} catch(Exception ignorable) {}
				}
			} catch(RuntimeException ex) {
				if(!listener.transClosed) {
					try {
						if(JCMSSettings.getSettings().getActivityReporter().isSessionOpen()) {
							JCMSSettings.getSettings().getActivityReporter().rollbackAndCloseSession();
						}
					} catch(Exception ignorable) {}
					PersistenceUtil.rollbackIfNeeded();
				}
				throw ex;
			} finally {
				if(!listener.transClosed)  {
					try {
						if(JCMSSettings.getSettings().getActivityReporter().isSessionOpen()) {
							JCMSSettings.getSettings().getActivityReporter().rollbackAndCloseSession();
						}
					} catch(Exception ignorable) {}
					TransactionalMethodSupport.clear();
					PersistenceUtil.closeEntityManager();
				}
			}
		} finally {
			if(lockPath!=null && !listener.lockReleased) {
				JCMSSettings.getSettings().getLockManager().releaseLock(lockPath);
				listener.lockReleased = true;
			}
		}
		return res;
	}

	private String buildLockPath(String lockPath, Map<?,?> parameters) {
		int poc = lockPath.indexOf("${");
		if(poc==-1) return lockPath;
		// Inače
		StringBuilder sb = new StringBuilder(lockPath.length()*2);
		if(poc>0) {
			sb.append(lockPath, 0, poc);
		}
		while(true) {
			int kraj = lockPath.indexOf('}', poc+2);
			if(kraj == -1 || kraj==poc+2) {
				throw new IllegalArgumentException("U stazi za zakljucavanje je pronaden parametar bez imena.");
			}
			String pname = lockPath.substring(poc+2, kraj);
			Object param = parameters.get(pname);
			if(param==null) {
				throw new RuntimeException("Vrijednost parametra "+pname+" za zaključavanje nije zadana.");
			} else if(param instanceof String) {
				String t = ((String)param).trim();
				if(t.isEmpty()) throw new RuntimeException("Vrijednost parametra "+pname+" za zaključavanje nije predana.");
				if(t.indexOf('\\')!=-1) throw new RuntimeException("Vrijednost parametra "+pname+" za zaključavanje sadrži nedozvoljene znakove.");
				sb.append(param);
			} else if(param instanceof String[]) {
				String[] sa = (String[])param;
				if(sa.length<1) throw new RuntimeException("Vrijednost parametra "+pname+" za zaključavanje se ne može utvrditi.");
				String t = sa[0].trim();
				if(t.isEmpty()) throw new RuntimeException("Vrijednost parametra "+pname+" za zaključavanje nije predana.");
				if(t.indexOf('\\')!=-1) throw new RuntimeException("Vrijednost parametra "+pname+" za zaključavanje sadrži nedozvoljene znakove.");
				sb.append(t);
			} else {
				throw new RuntimeException("Vrijednost parametra "+pname+" za zaključavanje se ne može utvrditi (nepoznat tip parametra).");
			}
			kraj++;
			if(kraj>=lockPath.length()) break;
			poc = lockPath.indexOf("${", kraj);
			if(poc==-1) {
				sb.append(lockPath, kraj, lockPath.length());
				break;
			}
			if(poc>kraj) {
				sb.append(lockPath, kraj, poc);
			}
		}
		return sb.toString();
	}

	static Class<?>[] buildParams = new Class[] {Navigation.class, AbstractActionData.class, Boolean.TYPE};

	private class Ext2PreResultListener implements PreResultListener {
		Ext2ActionSupport<?> ext2;
		boolean closeImmediately;
		WebClass dc;
		WebMethodInfo wmi;
		boolean transClosed = false;
		LockPath lockPath;
		boolean lockReleased = false;
		
		public Ext2PreResultListener(ActionInvocation invocation, Ext2ActionSupport<?> ext2, boolean closeImmediately, WebClass dc, WebMethodInfo wmi, LockPath lockPath) {
			this.ext2 = ext2;
			this.closeImmediately = closeImmediately;
			this.dc = dc;
			this.wmi = wmi;
			this.lockPath = lockPath;
		}

		@Override
		public void beforeResult(ActionInvocation invocation, String resultCode) {
			// Dakle, tu samo dobio rezultat...
			// Ajmo ga mapirat u struts2 result
			if(resultCode==null) {
				resultCode = ext2.getData().getResult();
			}
			String struts2Result = mapStruts2Result(invocation, resultCode);
			invocation.setResultCode(struts2Result);
			if(closeImmediately) {
				try {
					PersistenceUtil.commitTransaction();
					try {
						if(JCMSSettings.getSettings().getActivityReporter().isSessionOpen()) {
							JCMSSettings.getSettings().getActivityReporter().commitAndCloseSession();
						}
					} catch(Exception ignorable) {}
				} catch(RuntimeException ex) {
					try {
						if(JCMSSettings.getSettings().getActivityReporter().isSessionOpen()) {
							JCMSSettings.getSettings().getActivityReporter().rollbackAndCloseSession();
						}
					} catch(Exception ignorable) {}
					PersistenceUtil.rollbackIfNeeded();
					throw ex;
				} finally {
					try {
						if(JCMSSettings.getSettings().getActivityReporter().isSessionOpen()) {
							JCMSSettings.getSettings().getActivityReporter().rollbackAndCloseSession();
						}
					} catch(Exception ignorable) {}
					transClosed = true;
					TransactionalMethodSupport.clear();
					PersistenceUtil.closeEntityManager();
				}
				if(lockPath!=null) {
					try {
						lockReleased = true;
						JCMSSettings.getSettings().getLockManager().releaseLock(lockPath);
					} catch (UnlockException e) {
						e.printStackTrace();
						throw new RuntimeException("Wrapped exception for unlocking attempt.", e);
					}
				}
			}
		}

		@SuppressWarnings("unchecked")
		private String mapStruts2Result(ActionInvocation invocation2, String resultCode) {
			if(resultCode!=null && resultCode.equals(Ext2ActionSupport.NOT_LOGGED_IN)) return resultCode;
			String res = null;
			DataResultMapping[] mappings = wmi.dataResultMappings(); 
			if(mappings!=null) {
				for(int i = 0; i < mappings.length; i++) {
					DataResultMapping m = mappings[i];
					if(m.dataResult().equals(resultCode)) {
						res = m.struts2Result();
						if(m.registerDelayedMessages()) {
							ext2.getData().getMessageLogger().registerAsDelayed();
						}
						break;
					}
				}
			}
			
			// Ako ga nisam uspio mapirati:
			if(res==null) res = defaultStruts2Mapping(resultCode);

			boolean foundS2Mapping = false;
			Class<? extends NavigationBuilder> nbClass = null;
			String[] additionalMenus = null;
			boolean callAsRoot = true;
			Struts2ResultMapping[] sMappings = wmi.struts2ResultMappings();
			if(sMappings != null) {
				for(int i = 0; i < sMappings.length; i++) {
					Struts2ResultMapping m = sMappings[i];
					if(m.struts2Result().equals(res)) {
						nbClass = m.navigBuilder();
						closeImmediately = m.transactionalMethod().closeImmediately();
						additionalMenus = m.additionalMenuItems();
						callAsRoot = m.navigBuilderIsRoot();
						foundS2Mapping = true;
						break;
					}
				}
			}
			if(nbClass==null) {
				nbClass = dc.defaultNavigBuilder();
				if(nbClass!=null && !nbClass.equals(DefaultNavigationBuilder.class)) {
					additionalMenus = dc.additionalMenuItems();
					callAsRoot = dc.defaultNavigBuilderIsRoot();
					if(additionalMenus!=null && additionalMenus.length==0) additionalMenus=null;
				}
			}
			if(!foundS2Mapping && (nbClass==null || nbClass.equals(DefaultNavigationBuilder.class))) {
				nbClass = lookupDefaultNavigBuilder(res);	
			}
			if(!foundS2Mapping && (nbClass==null || nbClass.equals(DefaultNavigationBuilder.class))) {
				// Nisam nasao navigation builder
				try {
					// hr.fer.zemris.jcms.web.actions2.
					String aName = invocation2.getAction().getClass().getName();
					if(aName.startsWith("hr.fer.zemris.jcms.web.actions2.")) {
						aName = aName.substring("hr.fer.zemris.jcms.web.actions2.".length());
					} else {
						aName = invocation2.getAction().getClass().getSimpleName();
					}
					nbClass = (Class<? extends NavigationBuilder>)this.getClass().getClassLoader().loadClass("hr.fer.zemris.jcms.web.navig.builders."+aName+"Builder");
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
					res = Ext2ActionSupport.NO_NAVBUILDER_FOUND;
				}
			}
			if(nbClass!=null) {
				Method m = null;
				try {
					m = nbClass.getMethod("build", buildParams);
					if(m!=null) {
						if(Modifier.isStatic(m.getModifiers())) {
							m.invoke(null, new Object[] {ext2.getNavigation(), ext2.getData(), callAsRoot ? Boolean.TRUE : Boolean.FALSE});
							if(additionalMenus!=null) {
								resolveAdditionalMenus(additionalMenus);
							}
						} else {
							throw new Exception("Metoda build u razredu "+nbClass.getName()+" mora biti statička!");
						}
					} else {
						throw new Exception("Metoda build ne postoji u razredu "+nbClass.getName());
					}
				} catch(Exception ex) {
					ex.printStackTrace();
					
				}
			}
			return res;
		}

		private void resolveAdditionalMenus(String[] additionalMenus) {
			int l = additionalMenus.length/2;
			for(int i = 0; i < l; i++) {
				String menu = additionalMenus[2*i];
				String propOrKey = additionalMenus[2*i+1];
				if(menu==null || StringUtil.isStringBlank(propOrKey)) {
					System.err.println("Pogreška pri dodavanju u izbornike. Ključ ("+menu+") ili vrijednost ("+propOrKey+") su neispravni.");
					continue;
				}
				if(propOrKey.charAt(0)=='#') {
					// Radi se o property-ju.
					propOrKey = propOrKey.substring(1);
					Object value = null;
					try {
						value = Ognl.getValue(propOrKey, ext2.getData());
					} catch (OgnlException e) {
						e.printStackTrace();
					}
					if(value!=null) {
						ext2.getNavigation().getNavigationBar(menu)
							.addItem(
									new TextNavigationItem(value.toString(), false)
							);
					}
				} else {
					ext2.getNavigation().getNavigationBar(menu)
						.addItem(
								new TextNavigationItem(propOrKey, true)
						);
				}
			}
		}

		private Class<? extends NavigationBuilder> lookupDefaultNavigBuilder(String res) {
			if(res.equals("nopermission")) return BuilderDefault.class;
			if(res.equals("notLoggedIn")) return BuilderDefault.class;
			if(res.equals("defaultShowMessage")) return BuilderDefault.class;
			if(res.equals("showFatalMessage")) return BuilderDefault.class;
			return DefaultNavigationBuilder.class;
		}

		private String defaultStruts2Mapping(String resultCode) {
			if(resultCode==null) {
				ext2.getData().getMessageLogger().registerAsDelayed();
				return Ext2ActionSupport.SHOW_FATAL_MESSAGE;
			}
			if(resultCode.equals(AbstractActionData.RESULT_INPUT)) {
				ext2.getData().getMessageLogger().registerAsDelayed();
				return Ext2ActionSupport.INPUT;
			}
			if(resultCode.equals(AbstractActionData.RESULT_FATAL)) {
				ext2.getData().getMessageLogger().registerAsDelayed();
				return Ext2ActionSupport.SHOW_FATAL_MESSAGE;
			}
			if(resultCode.equals(AbstractActionData.RESULT_SUCCESS)) {
				ext2.getData().getMessageLogger().registerAsDelayed();
				return Ext2ActionSupport.SUCCESS;
			}
			if(resultCode.equals(Ext2ActionSupport.NOT_LOGGED_IN)) {
				return Ext2ActionSupport.NOT_LOGGED_IN;
			}
			ext2.getData().getMessageLogger().registerAsDelayed();
			return Ext2ActionSupport.SHOW_FATAL_MESSAGE;
		}
	}
}
