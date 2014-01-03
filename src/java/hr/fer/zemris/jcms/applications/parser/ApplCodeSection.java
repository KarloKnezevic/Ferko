package hr.fer.zemris.jcms.applications.parser;

import java.util.ArrayList;
import java.util.List;

public class ApplCodeSection {
	private String sectionName;
	private List<String> arguments = new ArrayList<String>();
	private String code;
	private String transformedCode;
	
	public ApplCodeSection(String sectionName, List<String> arguments, String code) {
		super();
		this.sectionName = sectionName;
		this.arguments = arguments;
		this.code = code;
	}
	
	public List<String> getArguments() {
		return arguments;
	}
	
	public String getSectionName() {
		return sectionName;
	}
	
	public String getCode() {
		return code;
	}
	
	public String getTransformedCode() {
		return transformedCode;
	}
	public void setTransformedCode(String transformedCode) {
		this.transformedCode = transformedCode;
	}
}
