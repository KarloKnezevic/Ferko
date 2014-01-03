/**
 * 
 */
package hr.fer.zemris.util.scheduling.support.algorithmview;

import hr.fer.zemris.util.time.DateStamp;
import hr.fer.zemris.util.time.TimeStamp;

public interface ITimeParameter{
	public DateStamp getFromDate();
	public TimeStamp getFromTime();
	public DateStamp getToDate();
	public TimeStamp getToTime();
}