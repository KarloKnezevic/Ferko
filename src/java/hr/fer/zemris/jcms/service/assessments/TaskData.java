package hr.fer.zemris.jcms.service.assessments;

import java.util.List;
import java.util.Map;

/**
 * Pomoćni razred za grupiranje podataka o tome što je uploadano i kako je to ocijenjeno.
 * Ovi podaci najčešće će se prikupljati kada se zada kratko ime komponente (npr. LAB), i pozicija (npr. 1);
 * ovime će (uz poznat primjerak kolegija) jednoznačno biti određeno o kojem se točno item-u radi, i s njega 
 * će se moći za sve taskove i sve korisnike pokupiti podatci što je točno uploadano.
 * 
 * @author marcupic
 *
 */
public class TaskData {
	private List<StudentTask> studentTasks;
	private Map<String,Long> taskNameToIDMap;
	
	public TaskData(List<StudentTask> studentTasks,
			Map<String, Long> taskNameToIDMap) {
		super();
		this.studentTasks = studentTasks;
		this.taskNameToIDMap = taskNameToIDMap;
	}

	/**
	 * Vraća podatke za sve što je do tada uploadano i ocijenjeno. Svaki student
	 * u vraćenoj listi može imati jedan ili više zapisa (ovisno o tome koliko
	 * je datoteka uploadao).
	 * 
	 * @return listu zapisa za sve studente
	 */
	public List<StudentTask> getStudentTasks() {
		return studentTasks;
	}
	
	/**
	 * Vraća mapu koja imena taskova na zadanom itemu (određenom kratkim imenom komponente i pozicijom itema) povezuje s ID-em taskova. 
	 * @return mapu
	 */
	public Map<String, Long> getTaskNameToIDMap() {
		return taskNameToIDMap;
	}
}
