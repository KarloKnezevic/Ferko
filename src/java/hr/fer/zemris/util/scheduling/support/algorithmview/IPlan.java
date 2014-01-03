package hr.fer.zemris.util.scheduling.support.algorithmview;

import hr.fer.zemris.util.scheduling.support.algorithmview.IDefinition;

import java.util.List;

public interface IPlan {

	public String getName();
	public List<IEvent> getPlanEvents();
	public IDefinition getDefinition();
	/**
	 * Is an equal number of terms required in all events?
	 */
	public boolean isEqualStudentDistributionInEachEvent();
	/**
	 * Is an equal sequence of terms required in all events?
	 */
	public boolean isEqualTermSequenceInEachEvent();
	/**
	 * Returns a required number of terms in each event, or -1 if no such requirement.
	 */
	public int getTermNumberInEachEvent();
}
