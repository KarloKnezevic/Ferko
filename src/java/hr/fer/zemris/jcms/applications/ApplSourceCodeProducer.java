package hr.fer.zemris.jcms.applications;

import java.util.ArrayList;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import hr.fer.zemris.jcms.applications.parser.ApplCodeSection;
import hr.fer.zemris.jcms.service.assessments.defimpl.SourceCodeUtils;

public class ApplSourceCodeProducer {

	public static String getSource(String className, String packageName, List<ApplCodeSection> sections) {
		ApplCodeSection def = null;
		ApplCodeSection glob = null;
		ApplCodeSection gfilt = null;
		List<ApplCodeSection> filters = new ArrayList<ApplCodeSection>();

		Set<String> imports = new LinkedHashSet<String>();
		
		int len = 0;
		for(ApplCodeSection s : sections) {
			len += s.getCode().length();
			if(s.getSectionName().equals("def")) {
				if(!SourceCodeUtils.checkForIllegalConstructs(s.getCode())) {
					throw new RuntimeException("Sekcija def sadrži nedozvoljene konstrukte.");
				}
				s.setTransformedCode(SourceCodeUtils.preprocessProgram(s.getCode(), imports));
				def = s;
				continue;
			}
			if(s.getSectionName().equals("global")) {
				if(!SourceCodeUtils.checkForIllegalConstructs(s.getCode())) {
					throw new RuntimeException("Sekcija glob sadrži nedozvoljene konstrukte.");
				}
				s.setTransformedCode(SourceCodeUtils.preprocessProgram(s.getCode(), imports));
				glob = s;
				continue;
			}
			if(s.getSectionName().equals("filter")) {
				if(!SourceCodeUtils.checkForIllegalConstructs(s.getCode())) {
					throw new RuntimeException("Sekcija filter"+(s.getArguments().isEmpty()?"" : "("+s.getArguments().get(0)+")")+" sadrži nedozvoljene konstrukte.");
				}
				s.setTransformedCode(SourceCodeUtils.preprocessProgram(s.getCode(), imports));
				if(s.getArguments().isEmpty()) {
					gfilt = s;
				} else {
					filters.add(s);
				}
				continue;
			}
		}
		len += 1000;
		StringBuilder sb = new StringBuilder(len);
		sb.append("package ").append(packageName).append(";\n\n");
		sb.append("import hr.fer.zemris.jcms.applications.ApplBuilder;\n");
		sb.append("import hr.fer.zemris.jcms.applications.ApplContainer;\n");
		sb.append("import hr.fer.zemris.jcms.applications.IApplBuilderRunner;\n");
		sb.append("import hr.fer.zemris.jcms.applications.exceptions.ApplDefinitionException;\n");
		sb.append("import hr.fer.zemris.jcms.service2.course.applications.IApplStudentDataProvider;\n");
		if(!imports.isEmpty()) {
			for(String s : imports) {
				sb.append(s).append("\n");
			}
		}
		sb.append("\n");
		sb.append("public class ").append(className).append(" extends ApplBuilder implements IApplBuilderRunner {\n\n");
		if(glob!=null) {
			sb.append(glob.getTransformedCode());
			sb.append("\n");
		}
		
		sb.append("\tpublic ").append(className).append("(ApplContainer applicationContainer, IApplStudentDataProvider provider) {\n");
		sb.append("\t\tsuper(applicationContainer, provider);\n");
		sb.append("\t}\n");

		sb.append("\tpublic void buildApplication() throws ApplDefinitionException {\n");
		sb.append(def.getTransformedCode());
		sb.append("\n");
		sb.append("\t}\n");
		sb.append("\tpublic void applyGlobalFilter() throws ApplDefinitionException {\n");
		if(gfilt!=null) {
			sb.append(gfilt.getTransformedCode());
			sb.append("\n");
		}
		sb.append("\t}\n");
		sb.append("\tpublic void applyFilters() throws ApplDefinitionException {\n");
		for(ApplCodeSection s : filters) {
			sb.append("\t\tfilterElement = findNamedElement(\"").append(s.getArguments().get(0)).append("\");\n");
			sb.append(s.getTransformedCode());
			sb.append("\n");
		}
		sb.append("\t}\n");
		sb.append("}\n");
		return sb.toString();
	}
}
