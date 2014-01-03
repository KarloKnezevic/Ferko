package hr.fer.zemris.util.time;

public class TimeSpanCache {

	public TimeSpan get(TimeStamp start, TimeStamp end) {
		if(start.equals(end)) {
			System.out.println("Uhvatio sam isti start i kraj!");
		}
		return new TimeSpan(start, end);
	}
}
