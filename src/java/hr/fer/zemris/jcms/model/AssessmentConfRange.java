package hr.fer.zemris.jcms.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@DiscriminatorValue("N")
@Table(name="ac_range")
public class AssessmentConfRange extends AssessmentConfiguration {

	private static final long serialVersionUID = 1L;

	private double rangeStart;
	private double rangeEnd;
	
	public AssessmentConfRange() {
	}

	public double getRangeStart() {
		return rangeStart;
	}

	public void setRangeStart(double rangeStart) {
		this.rangeStart = rangeStart;
	}

	public double getRangeEnd() {
		return rangeEnd;
	}

	public void setRangeEnd(double rangeEnd) {
		this.rangeEnd = rangeEnd;
	}
}
