package hr.fer.zemris.util.scheduling.support;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class ItemCache {

	int currentSize;
	public String[] items;
	private Map<String,Integer> map = new HashMap<String, Integer>();
	
	public ItemCache(Set<String> col) {
		items = new String[col.size()];
		col.toArray(items);
		for(int i = 0; i < items.length; i++) {
			map.put(items[i], Integer.valueOf(i));
		}
	}

	public ItemCache() {
	}
	
	public int translate(String obj) {
		Integer res = map.get(obj);
		if(res==null) {
			throw new RuntimeException("Item "+obj+" ne postoji.");
		}
		return res;
	}

	public void addItem(String item) {
		Integer i = map.get(item);
		if(i!=null) return;
		if(items==null) {
			items = new String[16];
			items[currentSize] = item;
			map.put(item, Integer.valueOf(currentSize++));
			return;
		}
		if(currentSize>=items.length) {
			String[] tmp = new String[items.length*2];
			System.arraycopy(items, 0, tmp, 0, items.length);
			items = tmp;
		}
		items[currentSize] = item;
		map.put(item, Integer.valueOf(currentSize++));
	}
	
	public int size() {
		return currentSize;
	}
}
