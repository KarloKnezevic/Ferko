package hr.fer.zemris.jcms.beans.ext;

import hr.fer.zemris.jcms.parsers.MPFormulaConstraintParser;
import hr.fer.zemris.jcms.parsers.TextService;
import hr.fer.zemris.util.StringUtil;

import java.io.IOException;
import java.io.Serializable;
import java.io.StringReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author marcupic
 *
 */
public class MPFormulaConstraints implements Serializable {

	private static final long serialVersionUID = 1L;
	
	private List<MPFormulaConstraint> allConstraints;
	private String originalText;
	private Map<String,boolean[]> formulasByGroup;
	
	public MPFormulaConstraints(String text) throws ParseException {
		this.originalText = text;
		parse(text);
	}

	private void parse(String text) throws ParseException {
		if(StringUtil.isStringBlank(text)) {
			allConstraints = new ArrayList<MPFormulaConstraint>();
			formulasByGroup = new HashMap<String, boolean[]>();
			return;
		}
		
		List<String> lines = null;
		try {
			lines = TextService.readerToStringList(new StringReader(text));
		} catch (IOException e) {
			throw new ParseException("Greška prilikom čitanja programa.", 0);
		}
		allConstraints = new ArrayList<MPFormulaConstraint>(lines.size());
		formulasByGroup = new HashMap<String, boolean[]>(lines.size());
		for(String line : lines) {
			MPFormulaConstraint c = MPFormulaConstraintParser.parse(line);
			allConstraints.add(c);
		}
		Set<String> grupe = new HashSet<String>();
		for(int i = 0; i < allConstraints.size(); i++) {
			MPFormulaConstraint c = allConstraints.get(i);
			c.extractGroupNames(grupe);
			for(String grupa : grupe) {
				boolean[] zastavice = formulasByGroup.get(grupa);
				if(zastavice==null) {
					zastavice = new boolean[allConstraints.size()];
					Arrays.fill(zastavice, false);
					formulasByGroup.put(grupa, zastavice);
				}
				zastavice[i] = true;
			}
			grupe.clear();
		}
	}
	
	public String getOriginalText() {
		return originalText;
	}
	
	public int getNumberOfConstraints() {
		return allConstraints.size();
	}
	
	public MPFormulaConstraint getConstraint(int index) {
		return allConstraints.get(index);
	}
	
	
	/**
	 * Metoda provjerava možemo li studenta iz njegove originalne grupe premjestiti u drugu grupu
	 * (ne u smislu zamjene, već doslovno izvaditi ga iz trenutne i gurnuti u drugu).
	 * U ovom slučaju gledaju se sljedeći podaci: fromGroup, fromStudentTag, toGroup, toStudentTag.
	 *  
	 * @param context
	 * @return
	 */
	public boolean canMoveStudent(MPFormulaContext context) {
		boolean[] indeksi1 = formulasByGroup.get(context.getExchangeDescriptor().getFromGroup());
		boolean[] indeksi2 = formulasByGroup.get(context.getExchangeDescriptor().getToGroup());
		// Ako nema ograničenja koja pale za navedene grupe, van!
		if(indeksi1==null && indeksi2==null) return true;
		if(indeksi1==null) {
			indeksi1 = indeksi2;
		}
		if(indeksi2==null) {
			indeksi2 = indeksi1;
		}
		boolean violated = false;
		int totalViolationMeasure = 0;
		for(int i = 0; i < indeksi1.length; i++) {
			if(indeksi1[i] || indeksi2[i]) {
				System.out.println("Provjeravam formulu na poziciji "+i);
				context.clearFormulaAppliesFlag();
				context.resetViolationMeasure();
				MPFormulaConstraint c = allConstraints.get(i);
				boolean sat = c.isSatisfied(context);
				System.out.println("Formula je zadovoljena: "+sat+", formula je primjenjiva: "+context.getFormulaAppliesFlag());
				if(!context.getFormulaAppliesFlag()) continue;
				if(!sat) {
					violated = true;
					totalViolationMeasure += context.getViolationMeasure();
					// return false;
				}
			}
		}
		context.resetViolationMeasure();
		context.addViolationMeasure(totalViolationMeasure);
		return !violated;
		//return true;
	}
	
	public void extractGroupNames(Set<String> groupNames) {
		for(MPFormulaConstraint con : allConstraints) {
			con.extractGroupNames(groupNames);
		}
	}

}
