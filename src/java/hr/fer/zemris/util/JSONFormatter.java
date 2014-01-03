package hr.fer.zemris.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import hr.fer.zemris.jcms.beans.StringNameStringValue;

public class JSONFormatter {

	public static enum SortOrder {
		NO_SORT,
		BY_NAME,
		BY_VALUE
	}
	
	public static String getJSONKeyValue(List<StringNameStringValue> list, SortOrder sortOrder, boolean appendKeyInValue) {
		List<StringNameStringValue> l = list;
		if(sortOrder!=null && sortOrder!=SortOrder.NO_SORT) {
			l = new ArrayList<StringNameStringValue>(list);
			if(sortOrder==SortOrder.BY_NAME) {
				Collections.sort(list, StringNameStringValue.BY_NAME);
			} else {
				Collections.sort(list, StringNameStringValue.BY_VALUE);
			}
		}
		StringBuilder sb = new StringBuilder(1000);
		sb.append("[\n");
		for(int i = 0; i < l.size(); i++) {
			if(i>0) sb.append(',');
			sb.append('\n');
			StringNameStringValue nv = l.get(i);
			sb.append("  {'");
			sb.append(nv.getName());
			sb.append("':'");
			sb.append(nv.getValue());
			if(appendKeyInValue) {
				sb.append(" (");
				sb.append(nv.getName());
				sb.append(")");
			}
			sb.append("'}");
		}
		sb.append("\n]");
		return sb.toString();
	}

	public static String getJSONKeyValue(List<StringNameStringValue> list, SortOrder sortOrder, boolean appendKeyInValue, String nameKey, String valueKey) {
		List<StringNameStringValue> l = list;
		if(sortOrder!=null && sortOrder!=SortOrder.NO_SORT) {
			l = new ArrayList<StringNameStringValue>(list);
			if(sortOrder==SortOrder.BY_NAME) {
				Collections.sort(list, StringNameStringValue.BY_NAME);
			} else {
				Collections.sort(list, StringNameStringValue.BY_VALUE);
			}
		}
		StringBuilder sb = new StringBuilder(1000);
		sb.append("[\n");
		for(int i = 0; i < l.size(); i++) {
			if(i>0) sb.append(',');
			sb.append('\n');
			StringNameStringValue nv = l.get(i);
			sb.append("  {");
			sb.append(nameKey);
			sb.append(": '");
			sb.append(nv.getName());
			sb.append("', ");
			sb.append(valueKey);
			sb.append(": '");
			sb.append(nv.getValue());
			if(appendKeyInValue) {
				sb.append(" (");
				sb.append(nv.getName());
				sb.append(")");
			}
			sb.append("'}");
		}
		sb.append("\n]");
		return sb.toString();
	}
}
