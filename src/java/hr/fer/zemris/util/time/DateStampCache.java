package hr.fer.zemris.util.time;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

public class DateStampCache {
	
	private Map<String,DateStamp> map = new LinkedHashMap<String, DateStamp>(64);
	private boolean modifiable;
	
	public DateStampCache() {
		modifiable = true;
	}
	
	public DateStampCache(String[] dates) {
		modifiable = true;
		for(String date : dates) {
			get(date);
		}
		modifiable = false;
	}
	
	public DateStamp get(String date) {
		DateStamp ds = map.get(date);
		if(ds == null && modifiable) {
			ds = new DateStamp(date);
			map.put(date, ds);
		}
		return ds;
	}
	
	public Set<String> getDatesAsStrings() {
		return Collections.unmodifiableSet(map.keySet());
	}

	public Set<DateStamp> getDates() {
		Set<DateStamp> set = new LinkedHashSet<DateStamp>(map.size());
		for(Map.Entry<String,DateStamp> e : map.entrySet()) {
			set.add(e.getValue());
		}
		return Collections.unmodifiableSet(set);
	}
}
