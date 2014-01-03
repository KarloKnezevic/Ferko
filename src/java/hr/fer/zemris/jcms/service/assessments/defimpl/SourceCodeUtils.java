package hr.fer.zemris.jcms.service.assessments.defimpl;

import java.io.Reader;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Ovaj razred sadrži različite metode koje se koriste za dinamičku izgradnju / provjeru
 * programa koje asistenti mogu upisivati u Ferko.
 * 
 * @author marcupic
 *
 */
public class SourceCodeUtils {

	/**
	 * Obavlja različite transformacije nad programima koji su dopušteni u Ferku.
	 * 
	 * @param program ulazni program
	 * @param imports set importa koji su već dodani, i koji se mogu dodati novi potrebni importi
	 * @return novi program i set napunjen dodatnim potrebnim importima
	 */
	public static String preprocessProgram(String program, Set<String> imports) {
		try {
			boolean importAdded = false;
			StringBuilder sb2 = new StringBuilder(program.length()*2);
			int start = 0;
			while(true) {
				int p = program.indexOf("@TASKS_LOOP(", start);
				if(p==-1) {
					sb2.append(program, start, program.length());
					break;
				}
				if(!importAdded) {
					importAdded=true;
					imports.add("import hr.fer.zemris.jcms.service.assessments.StudentTask;");
				}
				if(p!=start) {
					sb2.append(program, start, p);
				}
				start = p+12;
				p = program.indexOf(")@", start);
				String args = program.substring(start,p);
				String[] argsArray = new String[3];
				int r = args.lastIndexOf(',');
				argsArray[2] = args.substring(r+1);
				args = args.substring(0,r);
				r = args.lastIndexOf(',');
				argsArray[1] = args.substring(r+1);
				argsArray[0] = args.substring(0,r);
				sb2.append("for(StudentTask ").append(argsArray[2]).append(" : ").append("tasks(").append(argsArray[0]).append(", ").append(argsArray[1]).append(")) ");
				start = p+2;
			}
			String res = sb2.toString();
			if(importAdded) {
				System.out.println(res);
			}
			return res;
		} catch(Exception ex) {
			ex.printStackTrace();
			return program;
		}
	}
	
	/**
	 * Obavlja različite transformacije nad programima koji su dopušteni u Ferku.
	 * 
	 * @param program ulazni program
	 * @param sb builder koji gradi izlazni program, u koji su upisani importi
	 * @return transformirani program i dodani potrebni importi u builder
	 */
	public static String preprocessProgram(String program, StringBuilder sb) {
		// Sacuvajmo redosljed dodavanja importa...
		Set<String> imports = new LinkedHashSet<String>();
		String retVal = preprocessProgram(program, imports);
		for(String imp : imports) {
			sb.append(imp).append("\n");
		}
		return retVal;
	}

	/**
	 * Provjerava sadrži li program nesigurne dijelove koda.
	 * 
	 * @param program program koji treba provjeriti
	 * @return true ako je program prihvatljiv, false inače
	 */
	public static boolean checkForIllegalConstructs(String program) {
		try {
			if(program.indexOf('\\')!=-1) {
				return false;
			}
			Reader r = new StringReader(program);
			StreamTokenizer stok = new StreamTokenizer(r);
			stok.wordChars('_', '_');
			stok.parseNumbers();
			stok.slashSlashComments(true);
			stok.slashStarComments(true);
			while(stok.nextToken()!=StreamTokenizer.TT_EOF) {
				if(stok.ttype==StreamTokenizer.TT_WORD) {
					if(illegalProgramParts.contains(stok.sval)) return false;
					if(stok.sval.startsWith("java.")) return false; // Nema java.awt.Frame i sl... 
					if(stok.sval.startsWith("javax.")) return false; // 
					if(stok.sval.startsWith("File") || stok.sval.startsWith("java.io.File")) return false;
					if(stok.sval.startsWith("Thread") || stok.sval.startsWith("java.lang.Thread")) return false;
					if(stok.sval.startsWith("Process") || stok.sval.startsWith("java.lang.Process")) return false;
					if(stok.sval.startsWith("System") || stok.sval.startsWith("java.lang.System")) return false;
					if(stok.sval.startsWith("Runtime") || stok.sval.startsWith("java.lang.Runtime")) return false;
				}
			}
		} catch(Exception ex) {
			return false;
		}
		return true;
	}

	/**
	 * Popis konstrukata koji su zabranjeni u programu.
	 */
	static final Set<String> illegalProgramParts;
	static {
		illegalProgramParts = new HashSet<String>();
		illegalProgramParts.add("while");
		illegalProgramParts.add("for");
		illegalProgramParts.add("do");
		illegalProgramParts.add("new");
		illegalProgramParts.add("Thread");
		illegalProgramParts.add("class");
		illegalProgramParts.add("interface");
		illegalProgramParts.add("import");
		illegalProgramParts.add("File");
		illegalProgramParts.add("java.util.File");
		illegalProgramParts.add("java.lang.Thread");
		illegalProgramParts.add("extends");
		illegalProgramParts.add("implements");
	}
}
