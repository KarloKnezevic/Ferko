package hr.fer.zemris.jcms.dao;

public class DAOHelperFactory {

	private static DAOHelper daoHelper = new DAOHelperImpl();

	public static DAOHelper getDAOHelper() {
		return daoHelper;
	}
	
}
