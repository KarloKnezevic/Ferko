package hr.fer.zemris.jcms.web.actions.data;

import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;
import hr.fer.zemris.util.DeleteOnCloseFileInputStream;

public class ExportGroupMembershipTreeData extends BaseGroup {

	private Long groupID;
	private String format = "xls";
	DeleteOnCloseFileInputStream stream;

	public ExportGroupMembershipTreeData(IMessageLogger messageLogger) {
		super(messageLogger);
	}

	public Long getGroupID() {
		return groupID;
	}
	public void setGroupID(Long groupID) {
		this.groupID = groupID;
	}

	public String getFormat() {
		return format;
	}
	public void setFormat(String format) {
		this.format = format;
	}

	public DeleteOnCloseFileInputStream getStream() {
		return stream;
	}
	public void setStream(DeleteOnCloseFileInputStream stream) {
		this.stream = stream;
	}
	
}
