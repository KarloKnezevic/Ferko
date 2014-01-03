package hr.fer.zemris.jcms.model;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@DiscriminatorValue("E")
@Table(name="ac_enum")
public class AssessmentConfEnum extends AssessmentConfiguration {

	private static final long serialVersionUID = 1L;
	
	private double intervalStart;
	private double intervalEnd;
	private double step;
	
	public AssessmentConfEnum() {
	}

	public double getIntervalStart() {
		return intervalStart;
	}

	public void setIntervalStart(double intervalStart) {
		this.intervalStart = intervalStart;
	}

	public double getIntervalEnd() {
		return intervalEnd;
	}

	public void setIntervalEnd(double intervalEnd) {
		this.intervalEnd = intervalEnd;
	}

	public double getStep() {
		return step;
	}

	public void setStep(double step) {
		this.step = step;
	}
}
