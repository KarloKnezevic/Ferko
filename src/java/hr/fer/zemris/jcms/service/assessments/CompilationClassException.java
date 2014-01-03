package hr.fer.zemris.jcms.service.assessments;

import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;

public class CompilationClassException extends CalculationException {

	private static final long serialVersionUID = 1L;
	private DiagnosticCollector<JavaFileObject> diagnostics;
	
	public CompilationClassException(String message, DiagnosticCollector<JavaFileObject> diagnostics) {
		super(message);
		this.diagnostics = diagnostics;
	}

	public CompilationClassException(String message, Throwable cause, DiagnosticCollector<JavaFileObject> diagnostics) {
		super(message, cause);
		this.diagnostics = diagnostics;
	}

	public DiagnosticCollector<JavaFileObject> getDiagnostics() {
		return diagnostics;
	}
}
