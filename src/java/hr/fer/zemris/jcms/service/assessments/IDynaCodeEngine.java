package hr.fer.zemris.jcms.service.assessments;

import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

public interface IDynaCodeEngine {
	public Class<?> classForProgram(String kind, Long id, String program, int programVersion);
	public boolean tryCompile(IMessageLogger messageLogger, String kind, String program);
	public Class<?> oneTimeCompile(IMessageLogger messageLogger, String packagePrefix, String className, String program);
}
