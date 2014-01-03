package hr.fer.zemris.jcms.beans;

import hr.fer.zemris.util.StringUtil;

import java.util.Comparator;

public class StringNameStringValue {
	
	private String name;
	private String value;
	
	public StringNameStringValue(String name, String value) {
		super();
		this.name = name;
		this.value = value;
	}
	
	public String getName() {
		return name;
	}
	
	public String getValue() {
		return value;
	}
	
	public static final Comparator<StringNameStringValue> BY_NAME = new Comparator<StringNameStringValue>() {
		@Override
		public int compare(StringNameStringValue o1, StringNameStringValue o2) {
			if(o1==null) {
				return o2==null ? 0 : -1;
			}
			if(o2==null) return 1;
			if(o1.getName()==null) {
				return o2.getName()==null ? 0 : -1;
			}
			if(o2.getName()==null) return 1;
			return StringUtil.HR_COLLATOR.compare(o1.getName(), o2.getName());
		}
	};

	public static final Comparator<StringNameStringValue> BY_VALUE = new Comparator<StringNameStringValue>() {
		@Override
		public int compare(StringNameStringValue o1, StringNameStringValue o2) {
			if(o1==null) {
				return o2==null ? 0 : -1;
			}
			if(o2==null) return 1;
			if(o1.getValue()==null) {
				return o2.getValue()==null ? 0 : -1;
			}
			if(o2.getValue()==null) return 1;
			return StringUtil.HR_COLLATOR.compare(o1.getValue(), o2.getValue());
		}
	};
}
