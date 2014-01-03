package hr.fer.zemris.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import hr.fer.zemris.jcms.beans.ext.ExchangeDescriptor;
import hr.fer.zemris.jcms.beans.ext.MPFormulaConstraints;
import hr.fer.zemris.jcms.beans.ext.MPFormulaContext;
import hr.fer.zemris.jcms.beans.ext.MPSecurityConstraints;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Test;

public class MarketPlaceParsersTesting {

	String[][] trueCases = new String[][] {
			{"KLASICNO", "STUDENT", "KLASICNO", "STUDENT"},
			{"VHDLLAB", "STUDENT", "VHDLLAB", "STUDENT"},
			{"KLASICNO", "DEMOS", "KLASICNO", "DEMOS"},
			{"KLASICNO", "DEMOS", "VHDLLAB", "DEMOS"},
			{"VHDLLAB", "DEMOS", "KLASICNO", "DEMOS"},
			{"VHDLLAB", "DEMOS", "VHDLLAB", "DEMOS"},
			{"KLASICNO", null, "KLASICNO", null},
	};
	String[][] falseCases = new String[][] {
			{"KLASICNO", "STUDENT", "KLASICNO", "DEMOS"},
			{"KLASICNO", "DEMOS", "KLASICNO", "STUDENT"},
			{"KLASICNO", "STUDENT", "VHDLLAB", "DEMOS"},
			{"KLASICNO", "DEMOS", "VHDLLAB", "STUDENT"}
	};
	
	ExchangeDescriptor[] trueDescriptors = new ExchangeDescriptor[] {
		new ExchangeDescriptor("2008-09-21 12:00 A101", "KLASICNO", "STUDENT", "2008-09-21 14:00 A101", "KLASICNO", "STUDENT"),
		new ExchangeDescriptor("2008-09-21 12:00 A101", "VHDLLAB", "STUDENT", "2008-09-21 14:00 A101", "VHDLLAB", "STUDENT"),
		new ExchangeDescriptor("2008-09-21 12:00 A101", "KLASICNO", "DEMOS", "2008-09-21 14:00 A101", "KLASICNO", "DEMOS"),
		new ExchangeDescriptor("2008-09-21 12:00 A101", "KLASICNO", "DEMOS", "2008-09-21 14:00 A101", "VHDLLAB", "DEMOS"),
		new ExchangeDescriptor("2008-09-21 12:00 A101", "VHDLLAB", "DEMOS", "2008-09-21 14:00 A101", "KLASICNO", "DEMOS"),
		new ExchangeDescriptor("2008-09-21 12:00 A101", "VHDLLAB", "DEMOS", "2008-09-21 14:00 A101", "VHDLLAB", "DEMOS"),
		new ExchangeDescriptor("2008-09-21 12:00 A101", "KLASICNO", null, "2008-09-21 14:00 A101", "KLASICNO", null),
		new ExchangeDescriptor("2008-09-21 12:00 A101", null, null, "2008-09-21 14:00 A101", null, null),
	};
	ExchangeDescriptor[] falseDescriptors = new ExchangeDescriptor[] {
			new ExchangeDescriptor("2008-09-21 12:00 A101", "KLASICNO", "STUDENT", "2008-09-21 14:00 A101", "KLASICNO", "DEMOS"),
			new ExchangeDescriptor("2008-09-21 12:00 A101", "KLASICNO", "DEMOS", "2008-09-21 14:00 A101", "KLASICNO", "STUDENT"),
			new ExchangeDescriptor("2008-09-21 12:00 A101", "KLASICNO", "STUDENT", "2008-09-21 14:00 A101", "VHDLLAB", "DEMOS"),
			new ExchangeDescriptor("2008-09-21 12:00 A101", "KLASICNO", "DEMOS", "2008-09-21 14:00 A101", "VHDLLAB", "STUDENT"),
	};
	
	@Test
	public void testSecurityParser() throws Exception {
		String pravilo = "STUDENT:KLASICNO/KLASICNO,STUDENT:VHDLLAB/VHDLLAB,DEMOS:?/?,#:KLASICNO/KLASICNO,#:#/#";
		MPSecurityConstraints constraints = new MPSecurityConstraints(pravilo);
		for(String[] elem : trueCases) {
			boolean b = constraints.canExchange(elem[0], elem[1], elem[2], elem[3]);
			assertTrue(Arrays.toString(elem)+" bi trebalo dati true.", b);
		}
		for(String[] elem : falseCases) {
			boolean b = constraints.canExchange(elem[0], elem[1], elem[2], elem[3]);
			assertFalse(Arrays.toString(elem)+" bi trebalo dati false.", b);
		}
		for(ExchangeDescriptor ed : trueDescriptors) {
			boolean b = constraints.canExchange(ed.getFromGroupTag(), ed.getFromStudentTag(), ed.getToGroupTag(), ed.getToStudentTag());
			assertTrue(ed+" bi trebalo dati true.", b);
		}
		for(ExchangeDescriptor ed : falseDescriptors) {
			boolean b = constraints.canExchange(ed.getFromGroupTag(), ed.getFromStudentTag(), ed.getToGroupTag(), ed.getToStudentTag());
			assertFalse(ed+" bi trebalo dati false.", b);
		}
	}
	
	@Test
	public void testFormulaParser() throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("\"1.R1\" + \"1.R3\" + \"1.R5\" <= 240\r\n");
		sb.append("\"1.R2\" + \"1.R4\" + \"1.R6\" <= 240\r\n");
		sb.append("\"2008-09-23 12:00 A101\".demos <= 4\r\n");
		sb.append("\"2008-09-23 12:00 A101\".student <= 30\r\n");
		sb.append("\"2008-09-24 12:00 A101\".\"demos\" <= 4\r\n");
		sb.append("\"2008-09-24 12:00 A101\".\"student\" <= 30\r\n");
		String pravila = sb.toString();
		
		MPFormulaConstraints constraints = new MPFormulaConstraints(pravila);

		Set<String> grupe = new HashSet<String>();
		constraints.getConstraint(0).extractGroupNames(grupe);
		Set<String> ocekivano = new HashSet<String>(); ocekivano.add("1.R1"); ocekivano.add("1.R3"); ocekivano.add("1.R5");
		assertEquals("Vadenje grupa ne radi!", ocekivano, grupe);
		
		grupe.clear();
		constraints.getConstraint(5).extractGroupNames(grupe);
		ocekivano = new HashSet<String>(); ocekivano.add("2008-09-24 12:00 A101");
		assertEquals("Vadenje grupa ne radi!", ocekivano, grupe);
	}

	@Test
	public void testPrelazak() throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("\"PON08-A101\" <= 34\r\n");
		sb.append("\"PON08-A101\".student <= 30\r\n");
		sb.append("\"PON10-A101\".student <= 30\r\n");
		sb.append("\"PON12-A101\".student <= 30\r\n");
		sb.append("\"PON08-A101\".demos <= 4\r\n");
		sb.append("\"PON08-A101\".demos >= 3\r\n");
		sb.append("\"PON10-A101\".demos <= 4\r\n");
		sb.append("\"PON10-A101\".demos >= 3\r\n");
		sb.append("\"PON12-A101\".demos <= 4\r\n");
		sb.append("\"PON12-A101\".demos >= 3\r\n");
		String pravila = sb.toString();
		
		MPFormulaConstraints constraints = new MPFormulaConstraints(pravila);

		boolean odgovor = constraints.canMoveStudent(new Baza1());
		assertTrue("Prva selidba je legalna!", odgovor);
		odgovor = constraints.canMoveStudent(new Baza2());
		assertFalse("Druga selidba nije legalna!", odgovor);
		odgovor = constraints.canMoveStudent(new Baza3());
		assertTrue("Treća selidba je legalna!", odgovor);
	}

	@Test
	public void testPrelazak2() throws Exception {
		StringBuilder sb = new StringBuilder();
		sb.append("\"PON08-A101\" <= 34\r\n");
		sb.append("\"PON08-A101\".student <= 30\r\n");
		sb.append("\"PON10-A101\".student <= 30\r\n");
		sb.append("\"PON12-A101\".student <= 30\r\n");
		sb.append("\"PON08-A101\".demos <= 4\r\n");
		sb.append("\"PON08-A101\".demos >= 3\r\n");
		sb.append("\"PON10-A101\".demos <= 4\r\n");
		sb.append("\"PON10-A101\".demos >= 3\r\n");
		sb.append("\"PON12-A101\".demos <= 4\r\n");
		sb.append("\"PON12-A101\".demos >= 3\r\n");
		String pravila = sb.toString();
		
		MPFormulaConstraints constraints = new MPFormulaConstraints(pravila);
		MPFormulaContext c = new Baza4();
		constraints.canMoveStudent(c);
		int before = c.getViolationMeasure();
		c.decrease(c.getExchangeDescriptor().getFromGroup(), c.getExchangeDescriptor().getFromStudentTag());
		c.increase(c.getExchangeDescriptor().getToGroup(), c.getExchangeDescriptor().getToStudentTag());
		boolean odgovor = constraints.canMoveStudent(c);
		int after = c.getViolationMeasure();
		
		assertFalse("Selidba direktno nije legalna!", odgovor);
		assertTrue("Selidba postaje legalna jer ublazava krsenja!", after<before);

		c = new Baza5();
		constraints.canMoveStudent(c);
		before = c.getViolationMeasure();
		c.decrease(c.getExchangeDescriptor().getFromGroup(), c.getExchangeDescriptor().getFromStudentTag());
		c.increase(c.getExchangeDescriptor().getToGroup(), c.getExchangeDescriptor().getToStudentTag());
		odgovor = constraints.canMoveStudent(c);
		after = c.getViolationMeasure();
		
		assertFalse("Selidba direktno nije legalna!", odgovor);
		assertFalse("Selidba ne postaje legalna jer povecava krsenja!", after<before);
	}

	class Baza1 extends Baza {
		public Baza1() {
			exchangeDescriptor = new ExchangeDescriptor("PON08-A101",null,"demos","PON10-A101",null,"demos");
			// Ovo dolje je situacija nakon selidbe - sve je OK
			podaci.put("PON08-A101++demos", 3);    // Dakle, ovdje je broj demosa na donjoj granici - demos ne moze van!
			podaci.put("PON08-A101++student", 29); // Broj studenata je takav da student može doći
			podaci.put("PON10-A101++demos", 4);    // Jedan demos može van
			podaci.put("PON10-A101++student", 30); // studenti mogu van ali ne i unutra
			podaci.put("PON12-A101++demos", 2);    // Broj demosa je premali
			podaci.put("PON12-A101++student", 30); // Studenti mogu van
		}
	}
	
	class Baza2 extends Baza {
		public Baza2() {
			exchangeDescriptor = new ExchangeDescriptor("PON08-A101",null,"demos","PON10-A101",null,"demos");
			// Ovo dolje je situacija nakon selidbe - ne valja!
			podaci.put("PON08-A101++demos", 2);    // Dakle, ovdje je broj demosa ispod donje granice - demos ne moze van!
			podaci.put("PON08-A101++student", 29); // Broj studenata je takav da student može doći
			podaci.put("PON10-A101++demos", 5);    // Previse demosa
			podaci.put("PON10-A101++student", 30); // studenti mogu van ali ne i unutra
			podaci.put("PON12-A101++demos", 2);    // Broj demosa je premali
			podaci.put("PON12-A101++student", 30); // Studenti mogu van
		}
	}
	
	class Baza3 extends Baza {
		public Baza3() {
			exchangeDescriptor = new ExchangeDescriptor("PON08-A101",null,"student","PON10-A101",null,"student");
			// Ovo dolje je situacija nakon selidbe - sve je OK
			podaci.put("PON08-A101++demos", 2);    // Dakle, ovdje je broj demosa ispod donje granice - demos ne moze van!
			podaci.put("PON08-A101++student", 29); // Broj studenata je takav da student može doći
			podaci.put("PON10-A101++demos", 5);    // Previse demosa
			podaci.put("PON10-A101++student", 30); // studenti mogu van ali ne i unutra
			podaci.put("PON12-A101++demos", 2);    // Broj demosa je premali
			podaci.put("PON12-A101++student", 30); // Studenti mogu van
		}
	}
	
	class Baza4 extends Baza {
		public Baza4() {
			exchangeDescriptor = new ExchangeDescriptor("PON10-A101",null,"demos","PON12-A101",null,"demos");
			// Ovo dolje je situacija prije selidbe - vec ima problema
			podaci.put("PON08-A101++demos", 3);    // Dakle, ovdje je broj demosa na donjoj granici - demos ne moze van!
			podaci.put("PON08-A101++student", 29); // Broj studenata je takav da student može doći
			podaci.put("PON10-A101++demos", 4);    // Jedan demos može van
			podaci.put("PON10-A101++student", 30); // studenti mogu van ali ne i unutra
			podaci.put("PON12-A101++demos", 1);    // Broj demosa je premali
			podaci.put("PON12-A101++student", 30); // Studenti mogu van
		}
	}
	
	class Baza5 extends Baza {
		public Baza5() {
			exchangeDescriptor = new ExchangeDescriptor("PON12-A101",null,"demos","PON08-A101",null,"demos");
			// Ovo dolje je situacija prije selidbe - vec ima problema
			podaci.put("PON08-A101++demos", 3);    // Dakle, ovdje je broj demosa na donjoj granici - demos ne moze van!
			podaci.put("PON08-A101++student", 29); // Broj studenata je takav da student može doći
			podaci.put("PON10-A101++demos", 4);    // Jedan demos može van
			podaci.put("PON10-A101++student", 30); // studenti mogu van ali ne i unutra
			podaci.put("PON12-A101++demos", 1);    // Broj demosa je premali
			podaci.put("PON12-A101++student", 30); // Studenti mogu van
		}
	}
	
	class Baza implements MPFormulaContext {
		int violationMeasure;
		private boolean appl = false;
		protected Map<String,Integer> podaci;
		protected ExchangeDescriptor exchangeDescriptor;
		
		public Baza() {
			podaci = new HashMap<String, Integer>();
		}

		@Override
		public void setFormulaAppliesFlag() {
			appl = true;
		}
		@Override
		public int getTotalSizeForGroup(String groupName) {
			groupName = groupName+"++";
			int n = 0;
			for(Map.Entry<String, Integer> e : podaci.entrySet()) {
				if(e.getKey().startsWith(groupName)) n += e.getValue().intValue();
			}
			return n;
		}
		@Override
		public int getNumberOfStudentsWithTag(String groupName, String tagName) {
			return podaci.get(groupName+"++"+tagName).intValue();
		}
		@Override
		public boolean getFormulaAppliesFlag() {
			return appl;
		}
		@Override
		public ExchangeDescriptor getExchangeDescriptor() {
			return exchangeDescriptor;
		}
		@Override
		public void clearFormulaAppliesFlag() {
			appl = false;
		}
		@Override
		public void decrease(String groupName, String studentTag) {
			String key = groupName+"++"+studentTag;
			Integer i = podaci.get(key);
			if(i==null) i = Integer.valueOf(0);
			podaci.put(key, Integer.valueOf(i.intValue()-1));
		}
		@Override
		public void increase(String groupName, String studentTag) {
			String key = groupName+"++"+studentTag;
			Integer i = podaci.get(key);
			if(i==null) i = Integer.valueOf(0);
			podaci.put(key, Integer.valueOf(i.intValue()+1));
		}
		@Override
		public void addViolationMeasure(int measure) {
			violationMeasure += measure;
		}
		@Override
		public int getViolationMeasure() {
			return violationMeasure;
		}
		@Override
		public void resetViolationMeasure() {
			violationMeasure = 0;
		}
	};
}
