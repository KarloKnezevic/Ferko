package hr.fer.zemris.util.scheduling.support.algorithmview;

import java.util.List;
import java.util.Set;

import hr.fer.zemris.util.scheduling.support.algorithmview.IDefinition;

public interface IEvent {

	public String getName();
	public String getId();
	public IDefinition getDefinition();
	public List<ITerm> getTerms(); 
	/**
	 * Term duration. Maximum duration is 12 hours, i.e. 12*60 minutes.
	 * Default is 15 minutes.
	 * @return Duration in minutes.
	 */
	public int getTermDuration();
	/**
	 * Event duration. Maximum duration is 14 days. Minimum is 15 minutes.
	 * @return If returning value >=15 then duration in minutes.
	 * 		   If returning value <15 and >0 then duration in days.
	 * 	       If no maximum limit is set then returns -1
	 * TODO: Ovo je loše i treba ispraviti da uvijek budu minute.
	 */
	public int getMaximumDuration();
	/**
	 * Distribution of students in the event.
	 * Possible distributions are GIVEN and RANDOM.
	 * RANDOM distribution type requires the algoritm to generate terms according to given parameters.
	 * GIVEN distribution type requires the algorithm to check if such terms are possible according to
	 * given parameters.
	 */
	public IEventDistribution getEventDistribution();
	/**
	 * Events that must happen before this event can happen.
	 * @return 
	 */
	public Set<IPrecondition> getPreconditionEvents();

	 
}
