package hr.fer.zemris.jcms.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;


/**
 * Ovo je provjera sa sumarnim rezultatom. Učitava se primjerice iz podataka
 * oblika JMBAG#bodovi. Bodove zapisuje direktno u {@link AssessmentScore#setRawScore(double)}.
 * 
 * @author marcupic
 *
 */
@Entity
@DiscriminatorValue("P")
@Table(name="ac_preload")
public class AssessmentConfPreload extends AssessmentConfiguration {

	private static final long serialVersionUID = 1L;

	public AssessmentConfPreload() {
	}
	
}
