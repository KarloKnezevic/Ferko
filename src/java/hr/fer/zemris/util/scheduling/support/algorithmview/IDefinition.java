package hr.fer.zemris.util.scheduling.support.algorithmview;


import java.util.List;

/**
 * Definition view for schedule generators
 * @author Ivan
 *
 */
public interface IDefinition {
	public List<ITimeParameter> getTimeParameters();
	public List<ILocationParameter> getLocationParameters();
	public List<IGroup> getGroups();
	public List<String> getIndividuals();
}
