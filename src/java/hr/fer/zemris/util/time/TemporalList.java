package hr.fer.zemris.util.time;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class TemporalList {
	private Map<DateStamp, TL> map = new HashMap<DateStamp, TL>();
	private TimeSpanCache timeSpanCache;
	
	public TemporalList() {
		
	}
	
	public TemporalList(TimeSpanCache timeSpanCache) {
		this.timeSpanCache = timeSpanCache;
	}

	public Map<DateStamp, TL> getMap() {
		return map;
	}
	
	public void addInterval(DateStamp dateStamp, TimeSpan timeSpan, String descriptor) {
		TL tl = map.get(dateStamp);
		if(timeSpan.getStart().equals(timeSpan.getEnd())) {
			System.out.println("Nesto ne valja (1)");
		}
		if(tl == null) {
			tl = new TL();
			map.put(dateStamp, tl);
			TemporalNode node = new TemporalNode();
			node.dateStamp = dateStamp;
			node.timeSpan = timeSpan;
			node.next = null;
			node.previous = null;
			if(descriptor!=null) node.descriptors.add(descriptor);
			tl.first = node;
			tl.last = node;
		} else {
			TemporalNode tnode = tl.first;
			while(tnode != null && timeSpan.getStart().afterOrAt(tnode.timeSpan.getEnd())) {
				tnode = tnode.next;
			}
			// OK, sada sam ovdje jer je tnode == null, ili moj pocetak nije prije kraja trenutnog tnode-a
			if(tnode==null) {
				// Dva su razloga; ili je first==null, ili sam sve prosao
				TemporalNode node = new TemporalNode();
				node.dateStamp = dateStamp;
				node.timeSpan = timeSpan;
				node.next = null;
				node.previous = tl.last;
				if(tl.first==null) {
					tl.first = node;
					tl.last = node;
				} else {
					tl.last.next = node;
					tl.last = node;
				}
				if(descriptor!=null) node.descriptors.add(descriptor);
			} else {
				TimeStamp curentStart = timeSpan.getStart(); 
				while(tnode!=null) {
					if(curentStart.before(tnode.timeSpan.getStart())) {
						TimeStamp end = timeSpan.getEnd();
						if(end.after(tnode.timeSpan.getStart())) {
							end = tnode.timeSpan.getStart();
						}
						// ubaci node koji traje do end
						TemporalNode node = new TemporalNode();
						node.dateStamp = dateStamp;
						node.timeSpan = timeSpanCache.get(curentStart, end);
						node.next = tnode;
						node.previous = tnode.previous;
						if(tnode.previous!=null) {
							tnode.previous.next = node;
						} else {
							tl.first = node;
						}
						tnode.previous = node;
						if(descriptor!=null) node.descriptors.add(descriptor);
						curentStart = end;
						if(timeSpan.getEnd().beforeOrAt(tnode.timeSpan.getStart())) break; // gotovi smo
					}
					if(curentStart.after(tnode.timeSpan.getStart())) {
						TemporalNode node = new TemporalNode();
						node.dateStamp = dateStamp;
						node.timeSpan = timeSpanCache.get(tnode.timeSpan.getStart(), curentStart);
						tnode.timeSpan = timeSpanCache.get(curentStart, tnode.timeSpan.getEnd());
						node.getDescriptors().addAll(tnode.getDescriptors());
						node.next = tnode;
						node.previous = tnode.previous;
						if(tnode.previous!=null) {
							tnode.previous.next = node;
						} else {
							tl.first = node;
						}
						tnode.previous = node;
						//if(descriptor!=null) node.descriptors.add(descriptor);
						// ovime smo gotovi
						//curentStart = tnode.timeSpan.getEnd();
					}
					
					// Sada je start sigurno poravnat s pocetkom intervala, ili ide jos kasnije
					if(timeSpan.getEnd().afterOrAt(tnode.timeSpan.getEnd())) {
						// citavom tnode dodaj jos opisnik
						if(descriptor!=null) tnode.descriptors.add(descriptor);
						// i potom premotaj pocetak na kraj trenutno pokrivenog intervala
						curentStart = tnode.timeSpan.getEnd();
						TimeSpan currentEnd = tnode.timeSpan;
						tnode = tnode.next;
						if(timeSpan.getEnd().equals(currentEnd.getEnd())) {
							// gotovi smo
							break;
						}
						continue;
					}
					// Inace, ja zavrsavam negdje usred trenutnog intervala...
					// To znaci da interval moram podijeliti
					TemporalNode node = new TemporalNode();
					node.dateStamp = dateStamp;
					node.timeSpan = timeSpanCache.get(curentStart, timeSpan.getEnd());
					tnode.timeSpan = timeSpanCache.get(timeSpan.getEnd(), tnode.timeSpan.getEnd());
					node.getDescriptors().addAll(tnode.getDescriptors());
					node.next = tnode;
					node.previous = tnode.previous;
					if(tnode.previous!=null) {
						tnode.previous.next = node;
					} else {
						tl.first = node;
					}
					tnode.previous = node;
					if(descriptor!=null) node.descriptors.add(descriptor);
					// ovime smo gotovi
					curentStart = tnode.timeSpan.getEnd();
					break;
				}
				if(tnode==null && curentStart.before(timeSpan.getEnd())) {
					// Kraj se proteze cak iza zadnjeg; dodaj element
					TemporalNode node = new TemporalNode();
					node.dateStamp = dateStamp;
					node.timeSpan = timeSpanCache.get(curentStart, timeSpan.getEnd());
					node.next = null;
					node.previous = tl.last;
					node.previous.next = node;
					tl.last = node;
					if(descriptor!=null) node.descriptors.add(descriptor);
				}
			}
		}
	}
	
	public TemporalList createInversionList(Set<DateStamp> dateStamps, TimeStamp start, TimeStamp end) {
		TemporalList newList = new TemporalList(timeSpanCache);
		for(DateStamp dateStamp : dateStamps) {
			TL tl = map.get(dateStamp);
			if(tl == null) {
				// Ovdje za taj dan nema nicega!
				tl = new TL();
				newList.map.put(dateStamp, tl);
				TemporalNode node = new TemporalNode();
				node.dateStamp = dateStamp;
				node.timeSpan = timeSpanCache.get(start, end);
				node.next = null;
				node.previous = null;
				tl.first = node;
				tl.last = node;
				continue;
			}
			// inace nesto imam...
			TL newTl = new TL();
			newList.map.put(dateStamp, newTl);
			TemporalNode tnode = tl.first;
			TimeStamp curentTime = start;
			while(tnode != null && tnode.timeSpan.getEnd().beforeOrAt(curentTime)) {
				tnode = tnode.next;
			}
			while(tnode != null && !tnode.timeSpan.getStart().afterOrAt(end)) {
				// Imam nekakav node...
				if(curentTime.before(tnode.timeSpan.getStart())) {
					// Imam nesto praznog vremena!
					TemporalNode node = new TemporalNode();
					node.dateStamp = dateStamp;
					node.timeSpan = timeSpanCache.get(curentTime, tnode.timeSpan.getStart());
					node.next = null;
					node.previous = newTl.last;
					if(node.previous!=null) node.previous.next = node;
					newTl.last = node;
					if(newTl.first==null) newTl.first = node;
				}
				curentTime = tnode.timeSpan.getEnd();
				tnode = tnode.next;
			}
			if(curentTime.before(end)) {
				// Imam nesto praznog vremena na kraju!
				TemporalNode node = new TemporalNode();
				node.dateStamp = dateStamp;
				node.timeSpan = timeSpanCache.get(curentTime, end);
				node.next = null;
				node.previous = newTl.last;
				if(node.previous!=null) node.previous.next = node;
				newTl.last = node;
				if(newTl.first==null) newTl.first = node;
			}
		}
		return newList;
	}
	
	public class TL {
		public TemporalNode first;
		public TemporalNode last;
	}
	
	@Override
	public String toString() {
		return toString(new StringBuilder()).toString();
	}

	public StringBuilder toString(StringBuilder sb) {
		sb.append('{');
		for(DateStamp ds : map.keySet()) {
			TL tl = map.get(ds);
			TemporalNode node = tl.first;
			while(node!=null) {
				node.toString(sb);
				node = node.next;
			}
		}
		sb.append('}');
		return sb;
	}
}
