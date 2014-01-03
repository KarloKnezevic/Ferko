package hr.fer.zemris.jcms.applications;

import hr.fer.zemris.jcms.applications.exceptions.ApplDefinitionException;

public interface IApplBuilderRunner {

	public void buildApplication() throws ApplDefinitionException;
	public void applyGlobalFilter() throws ApplDefinitionException;
	public void applyFilters() throws ApplDefinitionException;
	public boolean isEnabled();

}
