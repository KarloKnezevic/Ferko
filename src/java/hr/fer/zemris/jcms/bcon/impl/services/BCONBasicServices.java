package hr.fer.zemris.jcms.bcon.impl.services;

import javax.persistence.EntityManager;

import hr.fer.zemris.jcms.bcon.BConMessage;
import hr.fer.zemris.jcms.bcon.BConMsgSemesterList;
import hr.fer.zemris.jcms.bcon.BConMsgStatus;
import hr.fer.zemris.jcms.bcon.BConMsgSemesterList.Semester;
import hr.fer.zemris.jcms.dao.DAOHelperFactory;
import hr.fer.zemris.jcms.dao.DatabaseOperation;
import hr.fer.zemris.jcms.dao.PersistenceUtil;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.model.YearSemester;
import hr.fer.zemris.jcms.security.JCMSSecurityManagerFactory;
import hr.fer.zemris.jcms.service2.BasicServiceSupport;
import hr.fer.zemris.jcms.web.interceptors.data.CurrentUser;

public class BCONBasicServices {

	/**
	 * Metoda dohvaća trenutni semestar.
	 * 
	 * @param currentUser trenutni korisnik (morao bi postojati jer ovu metodu smijemo zvati samo kada smo prijavljeni)
	 * @return poruku tipa {@link BConMsgSemesterList} ako je sve u redu (poruka će imati jedan zapis), ili {@link BConMsgStatus}
	 */
	public static BConMessage dohvatiTrenutniSemestar(final CurrentUser currentUser) {
		// Ovo mogu raditi samo logirani korisnici
		if(currentUser==null) {
			return new BConMsgStatus(false, "Last message is not allowed in !STATE_AUTHENTICATED.");
		}
		final BConMessage[] result = new BConMessage[] {null};
		PersistenceUtil.executeSingleDatabaseOperation(new DatabaseOperation<Void>() {
			@Override
			public Void executeOperation(EntityManager em) {
				// Dohvati trenutnog korisnika:
				User user = DAOHelperFactory.getDAOHelper().getUserDAO().getUserById(em, currentUser.getUserID());
				if(user==null) {
					result[0] = new BConMsgStatus(false, "Last message is not allowed in !STATE_AUTHENTICATED.");
					return null;
				}
				// Ako nam treba security manager, tu ga imamo:
				JCMSSecurityManagerFactory.getManager().init(user, em);

				// Obavimo posao:
				String csemID = BasicServiceSupport.getCurrentSemesterID(em);
				YearSemester ysem = null;
				if(csemID!=null) {
					ysem = DAOHelperFactory.getDAOHelper().getYearSemesterDAO().get(em, csemID);
				}
				Semester[] sems = null;
				if(ysem!=null) {
					sems = new Semester[] {new Semester(ysem.getId(), ysem.getAcademicYear(), ysem.getSemester())};
				}
				
				// Stvorimo rezultat koji ćemo poslati dalje
				BConMsgSemesterList list = new BConMsgSemesterList(sems);
				result[0] = list;
				return null;
			}
		});
		return result[0];
	}
	
}
