package hr.fer.zemris.util.time;

import java.util.ArrayList;
import java.util.List;

public class TemporalNode {
	DateStamp dateStamp;
	TimeSpan timeSpan;
	TemporalNode next;
	TemporalNode previous;
	List<String> descriptors = new ArrayList<String>();
	
	public TemporalNode() {
	}
	
	public TemporalNode(DateStamp dateStamp, TimeSpan timeSpan, TemporalNode next, TemporalNode previous) {
		this.dateStamp=dateStamp;
		this.timeSpan=timeSpan;
		this.next=next;
		this.previous=previous;
	}
	
	public DateStamp getDateStamp() {
		return dateStamp;
	}
	
	public TimeSpan getTimeSpan() {
		return timeSpan;
	}
	
	public List<String> getDescriptors() {
		return descriptors;
	}
	
	@Override
	public String toString() {
		return toString(new StringBuilder()).toString();
	}

	public TemporalNode getNext() {
		return next;
	}
	public void setNext(TemporalNode next) {
		this.next = next;
	}
	
	public TemporalNode getPrevious() {
		return previous;
	}
	
	public StringBuilder toString(StringBuilder sb) {
		sb.append('[');
		dateStamp.toString(sb);
		sb.append(' ');
		timeSpan.toString(sb);
		sb.append(", ").append(descriptors);
		sb.append(']');
		return sb;
	}
}
