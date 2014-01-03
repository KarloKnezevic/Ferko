package hr.fer.zemris.util.time;

public class TimeStampCache {
	
	private TimeStamp[] cache = new TimeStamp[24*60+1];
	
	public TimeStamp get(int hour, int minute) {
		int index = hour * 60 + minute;
		TimeStamp ts = cache[index];
		if(ts==null) {
			ts = new TimeStamp(hour, minute);
			cache[index] = ts;
		}
		return ts;
	}
	public TimeStamp get(String timeStamp) {
		return get(Integer.parseInt(timeStamp.substring(0,2)),Integer.parseInt(timeStamp.substring(3,5)));
	}
}
