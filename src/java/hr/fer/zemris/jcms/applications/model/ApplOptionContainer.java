package hr.fer.zemris.jcms.applications.model;

import hr.fer.zemris.jcms.applications.exceptions.ApplDefinitionException;

public interface ApplOptionContainer extends Iterable<ApplOption> {
	public ApplOption getOption(int index);
	public void addOption(ApplOption option) throws ApplDefinitionException;
	public int size();
}
