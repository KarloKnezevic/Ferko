package hr.fer.zemris.jcms.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;

/**
 * Vrsta provjere kod koje se studentovi odgovori očitavaju s obrasca.
 * 
 * @author marcupic
 */
@Entity
@DiscriminatorValue("X")
@Table(name="ac_external") 
public class AssessmentConfExternal extends AssessmentConfiguration {
	
	private static final long serialVersionUID = 1L;
	
	/**
	 * Konstruktor.
	 */
	public AssessmentConfExternal() {
	}

}
