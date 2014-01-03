/**
 * 
 */
package hr.fer.zemris.util.scheduling.support.algorithmview;


public interface IPrecondition {
	
	public IEvent getEvent();
	public String getTimeDistance();
	public int getTimeDistanceValue();
}
