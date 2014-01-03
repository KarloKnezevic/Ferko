package hr.fer.zemris.jcms.service.assessments;

import hr.fer.zemris.jcms.beans.ext.StudentApplicationShortBean;
import hr.fer.zemris.jcms.model.ApplicationDefinition;

import java.util.List;

/**
 * Ovo je sučelje za "pružatelja" usluge dohvata prijava studenata. Naime, kako se ne bi bez veze učitavale
 * sve prijave i time opterećivao proces izračuna svih bodova, {@link IAssessmentDataProvider} ovime ima
 * mogučnost zatražiti lazy učitavanje samo onih prijava koje su doista nužne za daljnje izračunavanje.  
 * Pri tome se čak ne učitavaju kompletne prijave (razlozi i sl) već samo tko, kada i status.
 * 
 * @author marcupic
 *
 */
public interface IOnDemandApplicationsDataCallback {

	/**
	 * @param applDefShortName kratko ime prijave (na kolegiju, ovo je unique)
	 * @return listu postojećih prijava
	 * @throws CalculationException ako dotična prijava uopće nije definirana u sustavu!
	 */
	public List<StudentApplicationShortBean> getData(String applDefShortName) throws CalculationException;
	
	/**
	 * Provjerava postoji li definicija prijave s ovim kratkim imenom. Rezultat cuva u cache-u.
	 * @param applDefShortName kratko ime prijave (na kolegiju, ovo je unique)
	 * @return true ako postoji, false inace
	 */
	public boolean existsApplicationDefinition(String applDefShortName);
	/**
	 * Vraća definiciju prijave.
	 * @param applDefShortName kratko ime
	 * @return definicija prijave
	 */
	public ApplicationDefinition getApplicationDefinition(String applDefShortName);
}
