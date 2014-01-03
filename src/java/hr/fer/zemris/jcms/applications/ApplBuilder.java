package hr.fer.zemris.jcms.applications;

import java.util.Date;
import java.util.List;

import hr.fer.zemris.jcms.applications.exceptions.ApplDefinitionException;
import hr.fer.zemris.jcms.applications.model.ApplNamedElement;
import hr.fer.zemris.jcms.applications.model.ApplOption;
import hr.fer.zemris.jcms.applications.model.ApplSingleSelect;
import hr.fer.zemris.jcms.service.assessments.CalculationException;
import hr.fer.zemris.jcms.service.assessments.StudentTask;
import hr.fer.zemris.jcms.service2.course.applications.IApplStudentDataProvider;

public class ApplBuilder {
	
	private ApplContainer applicationDef;
	private boolean enabled = true;
	private IApplStudentDataProvider provider;
	
	public ApplBuilder(ApplContainer applicationContainer, IApplStudentDataProvider provider) {
		if(applicationContainer==null) throw new NullPointerException("ApplicationContainer can not be null!");
		if(provider==null) throw new NullPointerException("P can not be null!");
		this.applicationDef = applicationContainer;
		this.provider = provider;
	}

	protected void message(String name, String text) throws ApplDefinitionException {
		applicationDef.addMessage(name, text);
	}

	protected void text(String name, String text) throws ApplDefinitionException {
		applicationDef.addText(name, text);
	}
	
	protected void chooseOne(String name, String text, ApplOption... applOptions) throws ApplDefinitionException {
		ApplSingleSelect select = applicationDef.addSingleOption(name, text);
		for(ApplOption option : applOptions) {
			select.addOption(option);
		}
	}
	
	protected ApplOption option(String name, String text) {
		return new ApplOption(name, text, false);
	}
	
	protected ApplOption other(String name, String text) {
		return new ApplOption(name, text, true);
	}
	
	protected void students(String name, int min, int max, String text) throws ApplDefinitionException {
		applicationDef.addStudents(name, min, max, text);
	}
	
	protected ApplNamedElement findNamedElement(String name) throws ApplDefinitionException {
		ApplNamedElement elem = applicationDef.findNamedElement(name);
		if(elem==null) {
			throw new ApplDefinitionException("Tražen je nepostojeći element (ime="+name+").");
		}
		return elem;
	}
	
	protected ApplNamedElement filterElement;
	
	protected void optionEnabled(String key, boolean value) throws ApplDefinitionException {
		if(!(filterElement instanceof ApplSingleSelect)) throw new ApplDefinitionException("Element "+filterElement.getName()+" ne sadrži opcije, pa ih nije moguće omogućiti.");
		ApplOption o = ((ApplSingleSelect)filterElement).getOption(key);
		if(o==null) throw new ApplDefinitionException("Element "+filterElement.getName()+" nema opciju "+key+" pa je nije moguće omogućiti.");
		o.setEnabled(value);
	}
	
	public boolean isEnabled() {
		return enabled;
	}
	
	protected void applicationEnabled(boolean value) {
		enabled = value;
	}
	
	// -------------------------- Funkcije koje predstavljaju vezu s ostatkom Ferka -------------------------- 
	
	protected boolean flagValue(String flagShortName) {
		if(!this.applicationDef.isExecutable()) throw new CalculationException("Izvođenje programa na ovom mjestu nije dozvoljeno.");
		return this.provider.flagValue(flagShortName);
	}

	protected boolean passed(String assessmentShortName) {
		if(!this.applicationDef.isExecutable()) throw new CalculationException("Izvođenje programa na ovom mjestu nije dozvoljeno.");
		return this.provider.passed(assessmentShortName);
	}

	protected boolean present(String assessmentShortName) {
		if(!this.applicationDef.isExecutable()) throw new CalculationException("Izvođenje programa na ovom mjestu nije dozvoljeno.");
		return this.provider.present(assessmentShortName);
	}

	protected double score(String assessmentShortName) {
		if(!this.applicationDef.isExecutable()) throw new CalculationException("Izvođenje programa na ovom mjestu nije dozvoljeno.");
		return this.provider.score(assessmentShortName);
	}
	
	protected boolean assessmentPassed(String assessmentShortName) {
		if(!this.applicationDef.isExecutable()) throw new CalculationException("Izvođenje programa na ovom mjestu nije dozvoljeno.");
		return this.provider.assessmentPassed(assessmentShortName);
	}

	protected boolean assessmentPresent(String assessmentShortName) {
		if(!this.applicationDef.isExecutable()) throw new CalculationException("Izvođenje programa na ovom mjestu nije dozvoljeno.");
		return this.provider.assessmentPresent(assessmentShortName);
	}

	protected double assessmentScore(String assessmentShortName) {
		if(!this.applicationDef.isExecutable()) throw new CalculationException("Izvođenje programa na ovom mjestu nije dozvoljeno.");
		return this.provider.assessmentScore(assessmentShortName);
	}

	protected boolean hasApplication(String applShortName) {
		if(!this.applicationDef.isExecutable()) throw new CalculationException("Izvođenje programa na ovom mjestu nije dozvoljeno.");
		return this.provider.hasApplication(applShortName);
	}
	
	protected boolean hasApplicationInStatus(String applShortName, String status) {
		if(!this.applicationDef.isExecutable()) throw new CalculationException("Izvođenje programa na ovom mjestu nije dozvoljeno.");
		return this.provider.hasApplicationInStatus(applShortName, status);
	}
	
	protected Date getApplicationDate(String applShortName) {
		if(!this.applicationDef.isExecutable()) throw new CalculationException("Izvođenje programa na ovom mjestu nije dozvoljeno.");
		return this.provider.getApplicationDate(applShortName);
	}
	
	public String getApplicationElementValue(String applShortName, String elementName) {
		if(!this.applicationDef.isExecutable()) throw new CalculationException("Izvođenje programa na ovom mjestu nije dozvoljeno.");
		return this.provider.getApplicationElementValue(applShortName, elementName);
	}
	
	protected boolean existsApplication(String applShortName) {
		if(!this.applicationDef.isExecutable()) throw new CalculationException("Izvođenje programa na ovom mjestu nije dozvoljeno.");
		return this.provider.existsApplication(applShortName);
	}
	
	protected boolean existsAssessment(String assessmentName) {
		if(!this.applicationDef.isExecutable()) throw new CalculationException("Izvođenje programa na ovom mjestu nije dozvoljeno.");
		return this.provider.existsAssessment(assessmentName);
	}

	protected boolean existsFlag(String flagName) {
		if(!this.applicationDef.isExecutable()) throw new CalculationException("Izvođenje programa na ovom mjestu nije dozvoljeno.");
		return this.provider.existsFlag(flagName);
	}

	protected String assessmentShortNameForTag(String tagShortName) {
		if(!this.applicationDef.isExecutable()) throw new CalculationException("Izvođenje programa na ovom mjestu nije dozvoljeno.");
		return this.provider.assessmentShortNameForTag(tagShortName);
	}

	protected String flagShortNameForTag(String tagShortName) {
		if(!this.applicationDef.isExecutable()) throw new CalculationException("Izvođenje programa na ovom mjestu nije dozvoljeno.");
		return this.provider.flagShortNameForTag(tagShortName);
	}

	protected StudentTask task(String componentShortName, int position, String taskName) {
		if(!this.applicationDef.isExecutable()) throw new CalculationException("Izvođenje programa na ovom mjestu nije dozvoljeno.");
		return this.provider.task(componentShortName, position, taskName);
	}
	
	protected List<StudentTask> tasks(String componentShortName, int position) {
		if(!this.applicationDef.isExecutable()) throw new CalculationException("Izvođenje programa na ovom mjestu nije dozvoljeno.");
		return this.provider.tasks(componentShortName, position);
	}
	
	protected boolean hasAssignedTask(String componentShortName, int position, String taskName) {
		if(!this.applicationDef.isExecutable()) throw new CalculationException("Izvođenje programa na ovom mjestu nije dozvoljeno.");
		return this.provider.hasAssignedTask(componentShortName, position, taskName);
	}
}
