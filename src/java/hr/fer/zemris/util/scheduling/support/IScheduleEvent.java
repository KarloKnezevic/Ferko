package hr.fer.zemris.util.scheduling.support;

import java.util.Date;
import java.util.List;

public interface IScheduleEvent {

	public List<IScheduleTerm> getScheduleEventTerms();
	public String getName();
	public String getId();
	public IScheduleTerm getTermForId(String termId);
	public int getTermDuration();
	public Date getEventStart();
	
	//Publication info
	public static String COMPONENT_GROUP = "componentgroup";
	public static String PRIVATE_GROUP = "privategroup";
	public void setPublicationGroupType(String type);
	public String getPublicationGroupType();
	public void setComponentDescriptorId(String id);
	public String getComponentDescriptorId();
	public void setEventComponentNumber(String number);
	public String getEventComponentNumber();
	public String getPrivateGroupName();
	public void setPrivateGroupName(String privateGroupName);
}
