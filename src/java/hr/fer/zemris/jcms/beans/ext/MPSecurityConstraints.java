package hr.fer.zemris.jcms.beans.ext;

import hr.fer.zemris.util.StringUtil;

import java.io.Serializable;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

public class MPSecurityConstraints implements Serializable {

	private static final long serialVersionUID = 1L;

	private String originalText;
	private List<MPSecurityConstraint> allConstraints;
	
	public MPSecurityConstraints(String text) throws ParseException {
		originalText = text;
		parse(text);
	}

	private void parse(String text) throws ParseException {
		if(StringUtil.isStringBlank(text)) {
			allConstraints = new ArrayList<MPSecurityConstraint>();
			return;
		}
		String[] elems = StringUtil.split(text, ',');
		allConstraints = new ArrayList<MPSecurityConstraint>(elems.length);
		for(int i = 0; i < elems.length; i++) {
			String redak = elems[i].trim();
			String[] tagTags = StringUtil.split(redak, ':');
			if(tagTags.length!=2) {
				throw new ParseException("Ulaz "+redak+" je pogrešan.", 0);
			}
			String[] input = StringUtil.split(tagTags[1], '/');
			if(input.length!=2) {
				throw new ParseException("Ulaz "+redak+", tocnije "+tagTags[1]+" je pogrešan.", 0);
			}
			input[0] = input[0].trim();
			input[1] = input[1].trim();
			tagTags[0] = tagTags[0].trim();
			allConstraints.add(new MPSecurityConstraint(tagTags[0], input[0],input[1]));
		}
	}
	
	public String getOriginalText() {
		return originalText;
	}
	
	public boolean canExchange(String fromGroupTag, String fromStudentTag, String toGroupTag, String toStudentTag) {
		// Ako nema ograničenja, tada može...
		if(allConstraints.isEmpty()) return true;
		// Ako su tagovi studenata različiti, opet ne može...
		if(!StringUtil.stringEquals(fromStudentTag, toStudentTag)) return false;
		// Inače provjeri ograničenja...
		// Najprije u jednom smjeru: moze li prvi student u grupu drugoga?
		boolean s1MozeUS2 = false;
		for(int i = 0; i < allConstraints.size(); i++) {
			MPSecurityConstraint c = allConstraints.get(i);
			if(!matches(c.getFromGroupTag(), fromGroupTag)) continue;
			if(!matches(c.getStudentTag(), fromStudentTag)) continue;
			if(!matches(c.getToGroupTag(), toGroupTag)) continue;
			s1MozeUS2 = true;
			break;
		}
		if(!s1MozeUS2) return false;
		// Potom u drugom smjeru: moze li drugi student u grupu prvoga?
		for(int i = 0; i < allConstraints.size(); i++) {
			MPSecurityConstraint c = allConstraints.get(i);
			if(!matches(c.getFromGroupTag(), toGroupTag)) continue;
			if(!matches(c.getStudentTag(), toStudentTag)) continue;
			if(!matches(c.getToGroupTag(), fromGroupTag)) continue;
			return true;
		}
		return false;
	}
	
	public boolean canMove(String studentTag, String fromGroupTag, String toGroupTag) {
		// Ako nema ograničenja, tada može...
		if(allConstraints.isEmpty()) return true;
		// Inače provjeri ograničenja...
		for(int i = 0; i < allConstraints.size(); i++) {
			MPSecurityConstraint c = allConstraints.get(i);
			if(!matches(c.getFromGroupTag(), fromGroupTag)) continue;
			if(!matches(c.getStudentTag(), studentTag)) continue;
			if(!matches(c.getToGroupTag(), toGroupTag)) continue;
			return true;
		}
		return false;
	}
	
	public boolean canExchange(ExchangeDescriptor descriptor) {
		// Ako nema ograničenja, tada može...
		if(allConstraints.isEmpty()) return true;
		// Ako su tagovi studenata različiti, opet ne može...
		if(!StringUtil.stringEquals(descriptor.getFromStudentTag(), descriptor.getToStudentTag())) return false;
		// Inače provjeri ograničenja...
		// Najprije u jednom smjeru: moze li prvi student u grupu drugoga?
		boolean s1MozeUS2 = false;
		for(int i = 0; i < allConstraints.size(); i++) {
			MPSecurityConstraint c = allConstraints.get(i);
			if(!matches(c.getFromGroupTag(), descriptor.getFromGroupTag())) continue;
			if(!matches(c.getStudentTag(), descriptor.getFromStudentTag())) continue;
			if(!matches(c.getToGroupTag(), descriptor.getToGroupTag())) continue;
			s1MozeUS2 = true;
			break;
		}
		if(!s1MozeUS2) return false;
		// Potom u drugom smjeru: moze li drugi student u grupu prvoga?
		for(int i = 0; i < allConstraints.size(); i++) {
			MPSecurityConstraint c = allConstraints.get(i);
			if(!matches(c.getFromGroupTag(), descriptor.getToGroupTag())) continue;
			if(!matches(c.getStudentTag(), descriptor.getToStudentTag())) continue;
			if(!matches(c.getToGroupTag(), descriptor.getFromGroupTag())) continue;
			s1MozeUS2 = true;
			break;
		}
		return false;
	}
	
	private boolean matches(String cond, String text) {
		return cond.equals("?") || cond.equals(text) || (cond.equals("#") && (text==null || text.length()==0));
	}
	
	public static void checkSecurityTagFormat(String tag) throws ParseException {
		if(StringUtil.isStringBlank(tag)) return;
		if(tag.indexOf('?')!=-1 || tag.indexOf('#')!=-1 || tag.indexOf(',')!=-1 | tag.indexOf('/')!=-1 || tag.indexOf(':')!=-1) {
			throw new ParseException("Pronađen nedopušten znak u imenu taga. Zabranjeni tagovi su: '?', '#', ',', '/' te ':'.", 0);
		}
	}
}
