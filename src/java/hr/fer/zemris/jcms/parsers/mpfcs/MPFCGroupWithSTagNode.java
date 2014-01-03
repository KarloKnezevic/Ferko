package hr.fer.zemris.jcms.parsers.mpfcs;

import hr.fer.zemris.jcms.beans.ext.MPFormulaContext;

import java.io.Serializable;
import java.util.Set;

public class MPFCGroupWithSTagNode extends MPFCIntValueNode implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String groupName;
	private String studentTagName;
	
	public MPFCGroupWithSTagNode(String groupName, String studentTagName) {
		super();
		this.groupName = groupName;
		this.studentTagName = studentTagName;
	}

	@Override
	public int value(MPFormulaContext context) {
		if(matches(context.getExchangeDescriptor().getFromGroup(), context.getExchangeDescriptor().getFromStudentTag()) || matches(context.getExchangeDescriptor().getToGroup(), context.getExchangeDescriptor().getToStudentTag())) {
			context.setFormulaAppliesFlag();
		}
		int ret = context.getNumberOfStudentsWithTag(groupName, studentTagName);
		return ret;
	}
	
	@Override
	public void extractGroupNames(Set<String> groupNames) {
		groupNames.add(groupName);
	}
	
	private boolean matches(String group, String studentTag) {
		if(!groupName.equals(group)) return false;
		if(studentTagName.equals("#")) {
			return studentTag==null || studentTag.length()==0;
		}
		return studentTagName.equals(studentTag);
	}
}
