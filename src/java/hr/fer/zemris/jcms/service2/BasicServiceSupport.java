package hr.fer.zemris.jcms.service2;

import hr.fer.zemris.jcms.dao.DAOHelperFactory;
import hr.fer.zemris.jcms.dao.DAOHelperImpl;
import hr.fer.zemris.jcms.model.KeyValue;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.model.UserDescriptor;
import hr.fer.zemris.jcms.service.has.HasCurrentUser;
import hr.fer.zemris.jcms.web.actions.data.support.AbstractActionData;
import hr.fer.zemris.util.StringUtil;

import javax.persistence.EntityManager;

/**
 * Pomoćni razred sloja usluge koji sadrži metode za dohvat/popunjavanje podataka o
 * korisniku.
 *  
 * @author marcupic
 *
 */
public class BasicServiceSupport {

	/**
	 * Popunjavanje podataka o korisniku. Ako je <code>userID</code> jednak <code>null</code> ili je prazan,
	 * dodaje se poruka čiji je ključ određen argumentom <code>message</code>, rezultat postavlja na onaj određen parametrom <code>result</code>,
	 * i metoda vraća false. Ako se dogodi greška kod dohvata korisnika, postupa se na jednak način. Ako je sve u redu, u <code>data</code> se
	 * postavlja korisnik.
	 * @param <T> Vrsta podatkovne strukture
	 * @param em entity manager
	 * @param data podatkovna struktura
	 * @param userID identifikator korisnika
	 * @param message ključ poruke za slučaj pogreške
	 * @param result rezultat koji treba postaviti u slučaju pogreške
	 * @return true ako nema pogreške, inače false.
	 */
	public static <T extends AbstractActionData & HasCurrentUser> boolean fillCurrentUser(EntityManager em, T data, Long userID, String message, String result) {
		User currentUser = null;
		if(userID == null) {
			if(message!=null) data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText(message));
			if(result!=null) data.setResult(result);
			return false;
		}
		currentUser = DAOHelperFactory.getDAOHelper().getUserDAO().getUserById(em, userID);
		if(currentUser==null) {
			if(message!=null) data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText(message));
			if(result!=null) data.setResult(result);
			return false;
		}
		data.setCurrentUser(currentUser);
		return true;
	}

	/**
	 * Popunjavanje podataka o korisniku. Ako je <code>userID</code> jednak <code>null</code> ili je prazan,
	 * dodaje se poruka čiji je ključ <code>Error.invalidParameters</code>, rezultat postavlja na {@linkplain AbstractActionData#RESULT_FATAL},
	 * i metoda vraća false. Ako se dogodi greška kod dohvata korisnika, dodaje se poruka čiji je ključ <code>Error.userNotFound2</code>,
	 * rezultat postavlja na {@linkplain AbstractActionData#RESULT_FATAL) i metoda vraća false. Ako je sve u redu, u <code>data</code> se
	 * postavlja korisnik.
	 * @param <T> Vrsta podatkovne strukture
	 * @param em entity manager
	 * @param data podatkovna struktura
	 * @param userID identifikator korisnika
	 * @return true ako nema pogreške, inače false.
	 */
	public static <T extends AbstractActionData & HasCurrentUser> boolean fillCurrentUser(EntityManager em, T data, Long userID) {
		User currentUser = null;
		if(userID == null) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.invalidParameters"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return false;
		}
		currentUser = DAOHelperFactory.getDAOHelper().getUserDAO().getUserById(em, userID);
		if(currentUser==null) {
			data.getMessageLogger().addErrorMessage(data.getMessageLogger().getText("Error.userNotFound2"));
			data.setResult(AbstractActionData.RESULT_FATAL);
			return false;
		}
		data.setCurrentUser(currentUser);
		return true;
	}

	/**
	 * Vraća identifikator trenutnog semestra (ako je on definiran u repozitoriju).
	 * 
	 * @param em entity manager
	 * @return identifikator trenutnog semestra ili <code>null</code> ako takav ne postoji
	 */
	public static String getCurrentSemesterID(EntityManager em) {
		KeyValue kv = DAOHelperFactory.getDAOHelper().getKeyValueDAO().get(em, "currentSemester");
		if(kv==null || kv.getName().length()==0) return null;
		return kv.getValue();
	}

	/**
	 * Po zadanom vanjskom ključu dohvaća korisnika i postavlja ga u data objekt.
	 * 
	 * @param em entity manager
	 * @param key vanjski ključ
	 * @param data podatkovni objekt
	 */
	public static void setUserFromExternalID(EntityManager em, String key, HasCurrentUser data) {
		data.setCurrentUser(retrieveUserFromExternalID(em, key));
	}

	/**
	 * Po zadanom vanjskom ključu dohvaća korisnika.
	 * 
	 * @param em entity manager
	 * @param key vanjski ključ
	 * 
	 * @return korisnik
	 */
	public static User retrieveUserFromExternalID(EntityManager em, String key) {
		if(StringUtil.isStringBlank(key)) return null;
		Long userID = Long.parseLong(key.split(":")[0],16);
		String externalID = key.split(":")[1];
		User user = new DAOHelperImpl().getUserDAO().getUserById(em, userID); 
		if(user==null) return null;
		UserDescriptor userd = user.getUserDescriptor();
		if(!userd.getExternalID().equals(externalID)) return null;
		return user;
	}

	/**
	 * Metoda dohvaća vrijednost zadanog ključa iz repozitorija Ferka.
	 * 
	 * @param em entity manager
	 * @param name naziv ključa
	 * @return vrijednost ili <code>null</code> ako vrijednosti nema
	 */
	public static String getKeyValue(EntityManager em, String name) {
		KeyValue kv = DAOHelperFactory.getDAOHelper().getKeyValueDAO().get(em, name);
		if(kv==null || kv.getName().length()==0) return null;
		return kv.getValue();
	}

}
