package hr.fer.zemris.tests.prijave;

import hr.fer.zemris.jcms.applications.ApplBuilder;
import hr.fer.zemris.jcms.applications.ApplContainer;
import hr.fer.zemris.jcms.applications.IApplBuilderRunner;
import hr.fer.zemris.jcms.applications.exceptions.ApplDefinitionException;
import hr.fer.zemris.jcms.service2.course.applications.IApplStudentDataProvider;

public class PrimjerPrijave extends ApplBuilder implements IApplBuilderRunner {

	public PrimjerPrijave(ApplContainer applicationContainer, IApplStudentDataProvider provider) {
		super(applicationContainer, provider);
	}

	public void buildApplication() throws ApplDefinitionException {
		message("m0", "Na ovom mjestu možete se prijaviti za nadoknadu 4. laboratorijske vježbe. Moguće je nadoknaditi samo jednu laboratorijsku vježbu.");
		text("razlog", "Unesite razlog prijave.");
		chooseOne(
		  "vjezba", "Odaberite vježbu koju nadoknađujete.",
		  option("lab1","Prva laboratorijska vježba"),
		  option("lab2","Druga laboratorijska vježba"),
		  option("lab3","Treća laboratorijska vježba"),
		  option("lab4","Četvrta laboratorijska vježba"),
		  other("drugo","Unesite što točno želite nadoknaditi i zašto?")
		);
		
	}

	public void applyGlobalFilter() throws ApplDefinitionException {
		applicationEnabled(!present("LAB1")||!present("LAB2")||!present("LAB3")||!present("LAB4"));
	}
	
	public void applyFilters() throws ApplDefinitionException {
		filterElement = findNamedElement("vjezba");
		optionEnabled("lab1",!present("LAB1"));
		optionEnabled("lab2",!present("LAB2"));
		optionEnabled("lab3",!present("LAB3"));
		optionEnabled("lab4",!present("LAB4"));
		optionEnabled("drugo",true);
	}
}
