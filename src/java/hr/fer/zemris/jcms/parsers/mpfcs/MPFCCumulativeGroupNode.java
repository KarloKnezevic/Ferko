package hr.fer.zemris.jcms.parsers.mpfcs;

import hr.fer.zemris.jcms.beans.ext.MPFormulaContext;

import java.io.Serializable;
import java.util.Set;

public class MPFCCumulativeGroupNode extends MPFCIntValueNode implements Serializable {
	
	private static final long serialVersionUID = 1L;
	
	private String groupName;
	
	public MPFCCumulativeGroupNode(String groupName) {
		super();
		this.groupName = groupName;
	}

	@Override
	public int value(MPFormulaContext context) {
		if(groupName.equals(context.getExchangeDescriptor().getFromGroup()) || groupName.equals(context.getExchangeDescriptor().getToGroup())) {
			context.setFormulaAppliesFlag();
		}
		int ret = context.getTotalSizeForGroup(groupName);
		return ret;
	}
	
	@Override
	public void extractGroupNames(Set<String> groupNames) {
		groupNames.add(groupName);
	}
}
