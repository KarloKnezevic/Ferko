package hr.fer.zemris.jcms.service.assessments.defimpl;

import hr.fer.zemris.jcms.applications.ApplSourceCodeProducer;
import hr.fer.zemris.jcms.applications.parser.ApplCodeParser;
import hr.fer.zemris.jcms.applications.parser.ApplCodeSection;
import hr.fer.zemris.jcms.model.Assessment;
import hr.fer.zemris.jcms.service.assessments.CalculationException;
import hr.fer.zemris.jcms.service.assessments.CompilationClassException;
import hr.fer.zemris.jcms.service.assessments.IDynaCodeEngine;
import hr.fer.zemris.jcms.service.assessments.IScoreProgramEnvironment;
import hr.fer.zemris.jcms.service.assessments.defimpl.comp.JavaSourceFromString;
import hr.fer.zemris.jcms.service.assessments.defimpl.comp.MyDynamicClassLoader;
import hr.fer.zemris.jcms.service.assessments.defimpl.comp.MyJavaFileManager;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

import java.io.IOException;
import java.io.Writer;
import java.lang.ref.ReferenceQueue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import javax.tools.JavaCompiler.CompilationTask;

import org.apache.log4j.Logger;

public class DynaCodeEngine implements IDynaCodeEngine {

	public static final Logger logger = Logger.getLogger(DynaCodeEngine.class.getCanonicalName());

	private ReferenceQueue<Class<?>> queue = new ReferenceQueue<Class<?>>();
	private Map<String, Map<Long,DynaClassWeakReference>> caches = new HashMap<String, Map<Long,DynaClassWeakReference>>();
	
	public static List<String> opcije;

	static {
		try {
			String p = Assessment.class.getResource("Assessment.class").toURI().toString();
			logger.debug("[PREVODENJE] Staza je: "+p);
			System.out.println("[PREVODENJE] Staza je: "+p);
			String staza = null;
			if(p.startsWith("file:/")) {
				staza = p.substring(5);
				if(staza.indexOf(':')==2) {
					// Makni jos jedan jer sam na Windowsima
					staza = staza.substring(1);
				}
				int index = staza.indexOf("hr/fer/zemris/jcms");
				if(index!=-1) {
					staza = staza.substring(0, index);
				}
				logger.debug("[PREVODENJE] Razriješio sam stazu u: "+staza);
				System.out.println("[PREVODENJE] Razriješio sam stazu u: "+staza);
				opcije = new ArrayList<String>();
				opcije.add("-cp");
				opcije.add(staza);
			} else if(p.startsWith("jar:file:/")) {
				staza = p.substring(9);
				if(staza.indexOf(':')==2) {
					// Makni jos jedan jer sam na Windowsima
					staza = staza.substring(1);
				}
				int index = staza.indexOf('!');
				if(index!=-1) {
					staza = staza.substring(0, index);
				}
				logger.debug("[PREVODENJE] Razriješio sam stazu u: "+staza);
				System.out.println("[PREVODENJE] Razriješio sam stazu u: "+staza);
				opcije = new ArrayList<String>();
				opcije.add("-cp");
				opcije.add(staza);
			}
		} catch(Exception ex) {
			logger.error("[PREVODENJE] Pogreška pri konstrukciji staze.", ex);
			System.out.println("[PREVODENJE] Pogreška pri konstrukciji staze.");
			ex.printStackTrace(System.out);
		}
	}
	
	@Override
	public Class<?> classForProgram(String kind, Long id,
			String program, int programVersion) {
		synchronized(caches) {
			DynaClassWeakReference r;
			while((r = (DynaClassWeakReference)queue.poll()) != null) {
				DynaClassWeakReference r2 = r.getMap().get(r.getId());
				if(r2!=null && r2.getVersion()==r.getVersion()) {
					r.getMap().remove(r.getId());
				}
			}
			Map<Long,DynaClassWeakReference> m = caches.get(kind);
			if(m == null) {
				m = new HashMap<Long, DynaClassWeakReference>();
				caches.put(kind, m);
			}
			DynaClassWeakReference ref = m.get(id);
			
			Class<?> env = ref != null ? ref.get() : null;
			if(env != null) {
				// Ako imam dobru verziju programa, vrati taj razred
				if(ref.getVersion()==programVersion) {
					//System.out.println("Vraćam razred.");
					return env;
				}
				// Inače imam krivu verziju razreda
				env = null;
				//System.out.println("Imam krivu verziju razreda.");
			}
			// Inače je env==null; idemo ponovno kompajlirati
			String className = "DynaClassTmp"+kind+"_"+id+"_"+programVersion;
			//System.out.println("Prevodim trenutnu verziju razreda "+className+".");
			if(kind.equals("A")) {
				env = compileScore(id, className, program);
			} else if(kind.equals("F")) {
				env = compileFlag(id, className, program);
			} else if(kind.equals("P")) {
				env = compileApplication(id, className, program);
			} else {
				throw new CalculationException("Zatraženo prevođenje programa nepoznate vrste.");
			}
			ref = new DynaClassWeakReference(env, queue, id, programVersion, m);
			m.put(id, ref);
			return env;
		}
	}

	@Override
	public boolean tryCompile(IMessageLogger messageLogger, String kind, String program) {
		if(kind.equals("A")) return tryCompileAssessment(messageLogger, "DynaClassTmpA_tmp", program);
		if(kind.equals("F")) return tryCompileFlag(messageLogger, "DynaClassTmpF_tmp", program);
		if(kind.equals("P")) return tryCompileApplication(messageLogger, "DynaClassTmpP_tmp", program);
		messageLogger.addErrorMessage("Interna greška: nepoznata vrsta programa.");
		return false;
	}
	
	private boolean tryCompileFlag(IMessageLogger messageLogger, String className, String program) {
		String packagePrefix = "studtest2.dynamic";
		String prog = buildFlagProgramSource(packagePrefix, className, program);
		return tryCompileProgram(messageLogger, packagePrefix, className, prog);
	}

	private boolean tryCompileAssessment(IMessageLogger messageLogger, String className, String program) {
		String packagePrefix = "studtest2.dynamic";
		String prog = buildAssessmentProgramSource(packagePrefix, className, program);
		return tryCompileProgram(messageLogger, packagePrefix, className, prog);
	}

	private boolean tryCompileApplication(IMessageLogger messageLogger, String className, String program) {
		String packagePrefix = "studtest2.dynamic";
		return tryCompileProgram(messageLogger, packagePrefix, className, program);
	}

	private boolean tryCompileProgram(IMessageLogger messageLogger, String packagePrefix, String className, String prog) {
		try {
			String fullClassName = packagePrefix+"."+className;
			
			javax.tools.JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
			DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
	
			StandardJavaFileManager stdFileManager = compiler.getStandardFileManager(null, null, null);
			MyJavaFileManager fileManager = new MyJavaFileManager(stdFileManager, packagePrefix);
	
			JavaFileObject file = new JavaSourceFromString(fullClassName, prog);
	
			Iterable<? extends JavaFileObject> compilationUnits1 = Arrays.asList(file);
			CompilationTask task = compiler.getTask(new DevNullWriter(), fileManager, diagnostics, opcije, null, compilationUnits1);
			
			logger.debug("[PREVODENJE] Pokrecem prevodenje koda. Opcije su "+(opcije==null ? "null" : opcije.toString()));
			System.out.println("[PREVODENJE] Pokrecem prevodenje koda. Opcije su "+(opcije==null ? "null" : opcije.toString()));
			boolean success = task.call();
			fileManager.releaseDelegateFileManager();

			if(!success) {
				messageLogger.addErrorMessage("Ne mogu prevesti program. Slijede poruke.");
			}

			for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
				messageLogger.addErrorMessage(
				      //"Code: " + diagnostic.getCode() +
				      "Kind: " + diagnostic.getKind() +
				      //", source: " + diagnostic.getSource() +
				      ", message: " + diagnostic.getMessage(null)
				);
			}
			
			return success;
		} catch(Exception ex) {
			return false;
		}
	}

	private String buildFlagProgramSource(String packagePrefix, String className, String program) {
		StringBuilder sb = new StringBuilder(1024*2);
		sb.append("package ").append(packagePrefix).append(";\n");
		sb.append("\n");
		sb.append("import hr.fer.zemris.jcms.service.assessments.AbstractFlagProgramEnvironment;\n");
		sb.append("import hr.fer.zemris.jcms.service.assessments.IScoreCalculatorContext;\n");
		sb.append("import hr.fer.zemris.jcms.service.assessments.IScoreCalculatorEngine;\n");
		program = SourceCodeUtils.preprocessProgram(program, sb);
		sb.append("\n");
		sb.append("public class ").append(className).append(" extends AbstractFlagProgramEnvironment {\n");
		sb.append("\n");
		sb.append("	public ").append(className).append("(IScoreCalculatorEngine engine,\n");
		sb.append("			IScoreCalculatorContext context, boolean _overrideSet, boolean _overrideValue) {\n");
		sb.append("		super(engine, context, _overrideSet, _overrideValue);\n");
		sb.append("	}\n");
		sb.append("\n");
		sb.append("	@Override\n");
		sb.append("	public void execute() {\n");
		sb.append(program);
		sb.append("	}\n");
		sb.append("\n");
		sb.append("}\n");
		return sb.toString();
	}

	private String buildAssessmentProgramSource(String packagePrefix, String className, String program) {
		StringBuilder sb = new StringBuilder(1024*2);
		sb.append("package ").append(packagePrefix).append(";\n");
		sb.append("\n");
		sb.append("import hr.fer.zemris.jcms.service.assessments.AbstractScoreProgramEnvironment;\n");
		sb.append("import hr.fer.zemris.jcms.service.assessments.IScoreCalculatorContext;\n");
		sb.append("import hr.fer.zemris.jcms.service.assessments.IScoreCalculatorEngine;\n");
		program = SourceCodeUtils.preprocessProgram(program, sb);
		sb.append("\n");
		sb.append("public class ").append(className).append(" extends AbstractScoreProgramEnvironment {\n");
		sb.append("\n");
		sb.append("	public ").append(className).append("(IScoreCalculatorEngine engine,\n");
		sb.append("			IScoreCalculatorContext context, boolean present, double score, String thisAssessmentShortName) {\n");
		sb.append("		super(engine, context, present, score, thisAssessmentShortName);\n");
		sb.append("	}\n");
		sb.append("\n");
		sb.append("	@Override\n");
		sb.append("	public void execute() {\n");
		sb.append(program);
		sb.append("	}\n");
		sb.append("\n");
		sb.append("}\n");
		return sb.toString();
	}


	@SuppressWarnings("unchecked")
	private Class<?> compileScore(Long id, String className, String program) {
		try {
			String packagePrefix = "studtest2.dynamic";
	
			String fullClassName = packagePrefix+"."+className;
			
			javax.tools.JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
			DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
	
			StandardJavaFileManager stdFileManager = compiler.getStandardFileManager(null, null, null);
			MyJavaFileManager fileManager = new MyJavaFileManager(stdFileManager, packagePrefix);
	
			JavaFileObject file = new JavaSourceFromString(fullClassName, buildAssessmentProgramSource(packagePrefix, className, program));
	
			Iterable<? extends JavaFileObject> compilationUnits1 = Arrays.asList(file);
			CompilationTask task = compiler.getTask(null, fileManager, diagnostics, opcije, null, compilationUnits1);
			
			logger.debug("[PREVODENJE] Pokrecem prevodenje koda 2. Opcije su "+(opcije==null ? "null" : opcije.toString()));
			System.out.println("[PREVODENJE] Pokrecem prevodenje koda 2. Opcije su "+(opcije==null ? "null" : opcije.toString()));
			boolean success = task.call();
			fileManager.releaseDelegateFileManager();

			if(!success) {
				throw new CompilationClassException("Ne mogu prevesti program za provjeru id="+id+".", diagnostics);
			}
			
			ClassLoader cl = new MyDynamicClassLoader(fileManager.getMap(),this.getClass().getClassLoader());
			Class<? extends IScoreProgramEnvironment> clazz = (Class<? extends IScoreProgramEnvironment>)cl.loadClass(fullClassName);
			return clazz;
		} catch(CompilationClassException ex) {
			throw ex;
		} catch(Throwable ex) {
			throw new CompilationClassException("Ne mogu prevesti ili stvoriti program za provjeru id="+id+".", ex, null);
		}
		//return DummyScoreProgramEnvironment.class;
	}

	private Class<?> compileFlag(Long id, String className, String program) {
		try {
			String packagePrefix = "studtest2.dynamic";

			String fullClassName = packagePrefix+"."+className;
			
			javax.tools.JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
			DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
	
			StandardJavaFileManager stdFileManager = compiler.getStandardFileManager(null, null, null);
			MyJavaFileManager fileManager = new MyJavaFileManager(stdFileManager, packagePrefix);
	
			JavaFileObject file = new JavaSourceFromString(fullClassName, buildFlagProgramSource(packagePrefix, className, program));
	
			Iterable<? extends JavaFileObject> compilationUnits1 = Arrays.asList(file);
			CompilationTask task = compiler.getTask(null, fileManager, diagnostics, opcije, null, compilationUnits1);
			
			logger.debug("[PREVODENJE] Pokrecem prevodenje koda 3. Opcije su "+(opcije==null ? "null" : opcije.toString()));
			System.out.println("[PREVODENJE] Pokrecem prevodenje koda 3. Opcije su "+(opcije==null ? "null" : opcije.toString()));
			boolean success = task.call();
			fileManager.releaseDelegateFileManager();

			if(!success) {
				throw new CompilationClassException("Ne mogu prevesti program za zastavicu id="+id+".", diagnostics);
			}
			
			ClassLoader cl = new MyDynamicClassLoader(fileManager.getMap(),this.getClass().getClassLoader());
			Class<?> clazz = (Class<?>)cl.loadClass(fullClassName);
			return clazz;
		} catch(CompilationClassException ex) {
			throw ex;
		} catch(Throwable ex) {
			throw new CompilationClassException("Ne mogu prevesti ili stvoriti program za zastavicu id="+id+".", ex, null);
		}
		//return DummyScoreProgramEnvironment.class;
	}

	private Class<?> compileApplication(Long id, String className, String program) {
		try {
			String packagePrefix = "studtest2.dynamic";
			
			ApplCodeParser parser = new ApplCodeParser(program);
			List<ApplCodeSection> sections = parser.getSections();
			String source = ApplSourceCodeProducer.getSource(className, packagePrefix, sections);

			String fullClassName = packagePrefix+"."+className;
			
			javax.tools.JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
			DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
	
			StandardJavaFileManager stdFileManager = compiler.getStandardFileManager(null, null, null);
			MyJavaFileManager fileManager = new MyJavaFileManager(stdFileManager, packagePrefix);
	
			JavaFileObject file = new JavaSourceFromString(fullClassName, source);
	
			Iterable<? extends JavaFileObject> compilationUnits1 = Arrays.asList(file);
			CompilationTask task = compiler.getTask(null, fileManager, diagnostics, opcije, null, compilationUnits1);
			
			logger.debug("[PREVODENJE] Pokrecem prevodenje koda 4. Opcije su "+(opcije==null ? "null" : opcije.toString()));
			System.out.println("[PREVODENJE] Pokrecem prevodenje koda 4. Opcije su "+(opcije==null ? "null" : opcije.toString()));
			boolean success = task.call();
			fileManager.releaseDelegateFileManager();

			if(!success) {
				throw new CompilationClassException("Ne mogu prevesti program za prijavu id="+id+".", diagnostics);
			}
			
			ClassLoader cl = new MyDynamicClassLoader(fileManager.getMap(),this.getClass().getClassLoader());
			Class<?> clazz = (Class<?>)cl.loadClass(fullClassName);
			return clazz;
		} catch(CompilationClassException ex) {
			throw ex;
		} catch(Throwable ex) {
			throw new CompilationClassException("Ne mogu prevesti ili stvoriti program za prijavu id="+id+". Razlog: "+ex.getMessage(), ex, null);
		}
	}

	public Class<?> oneTimeCompile(IMessageLogger messageLogger, String packagePrefix, String className, String program) {
		try {
			String fullClassName = packagePrefix+"."+className;
			
			javax.tools.JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
			DiagnosticCollector<JavaFileObject> diagnostics = new DiagnosticCollector<JavaFileObject>();
	
			StandardJavaFileManager stdFileManager = compiler.getStandardFileManager(null, null, null);
			MyJavaFileManager fileManager = new MyJavaFileManager(stdFileManager, packagePrefix);
	
			JavaFileObject file = new JavaSourceFromString(fullClassName, program);
	
			Iterable<? extends JavaFileObject> compilationUnits1 = Arrays.asList(file);
			CompilationTask task = compiler.getTask(null, fileManager, diagnostics, opcije, null, compilationUnits1);
			
			logger.debug("[PREVODENJE] Pokrecem prevodenje koda 3. Opcije su "+(opcije==null ? "null" : opcije.toString()));
			System.out.println("[PREVODENJE] Pokrecem prevodenje koda 3. Opcije su "+(opcije==null ? "null" : opcije.toString()));
			boolean success = task.call();
			fileManager.releaseDelegateFileManager();

			if(!success) {
				messageLogger.addErrorMessage("Ne mogu prevesti program. Slijede poruke.");
			}

			for (Diagnostic<? extends JavaFileObject> diagnostic : diagnostics.getDiagnostics()) {
				messageLogger.addErrorMessage(
				      //"Code: " + diagnostic.getCode() +
				      "Kind: " + diagnostic.getKind() +
				      //", source: " + diagnostic.getSource() +
				      ", message: " + diagnostic.getMessage(null)
				);
			}

			if(!success) {
				return null;
			}

			ClassLoader cl = new MyDynamicClassLoader(fileManager.getMap(),this.getClass().getClassLoader());
			Class<?> clazz = (Class<?>)cl.loadClass(fullClassName);
			return clazz;
		} catch(CompilationClassException ex) {
			throw ex;
		} catch(Throwable ex) {
			throw new CompilationClassException("Ne mogu prevesti program.", ex, null);
		}
	}

	static class DevNullWriter extends Writer {

		@Override
		public Writer append(char c) throws IOException {
			return this;
		}

		@Override
		public Writer append(CharSequence csq, int start, int end) throws IOException {
			return this;
		}

		@Override
		public Writer append(CharSequence csq) throws IOException {
			return this;
		}

		@Override
		public void close() throws IOException {
		}

		@Override
		public void flush() throws IOException {
		}

		@Override
		public void write(char[] cbuf, int off, int len) throws IOException {
		}

		@Override
		public void write(char[] cbuf) throws IOException {
		}

		@Override
		public void write(int c) throws IOException {
		}

		@Override
		public void write(String str, int off, int len) throws IOException {
		}

		@Override
		public void write(String str) throws IOException {
		}
		
	}
}
