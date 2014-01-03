package hr.fer.zemris.tests;

import hr.fer.zemris.jcms.model.Assessment;
import hr.fer.zemris.jcms.model.AssessmentFlag;
import hr.fer.zemris.jcms.model.AssessmentFlagValue;
import hr.fer.zemris.jcms.model.AssessmentScore;
import hr.fer.zemris.jcms.model.CourseInstance;
import hr.fer.zemris.jcms.model.User;
import hr.fer.zemris.jcms.service.assessments.IScoreCalculatorContext;
import hr.fer.zemris.jcms.service.assessments.IScoreCalculatorEngine;
import hr.fer.zemris.jcms.service.assessments.ScoreCalculatorEngineFactory;
import hr.fer.zemris.jcms.service.assessments.StudentFlag;
import hr.fer.zemris.jcms.service.assessments.StudentScore;
import hr.fer.zemris.jcms.service.assessments.defimpl.SimpleAssessmentDataProvider;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AssessmentCalcTest {
	User u;
	CourseInstance ci;
	List<Assessment> assessments;
	List<AssessmentScore> assessmentScores;
	List<User> users;
	List<AssessmentFlag> assessmentFlags;
	List<AssessmentFlagValue> assessmentFlagValues;

	@Before
	public void setUp() {
		assessments = new ArrayList<Assessment>();
		assessmentScores = new ArrayList<AssessmentScore>();
		assessmentFlags = new ArrayList<AssessmentFlag>();
		assessmentFlagValues = new ArrayList<AssessmentFlagValue>();
		users = new ArrayList<User>();
		
		User u = new User();
		u.setId(Long.valueOf(17));
		u.setJmbag("0000000017");
		users.add(u);
		
		u = new User();
		u.setId(Long.valueOf(18));
		u.setJmbag("0000000018");
		users.add(u);
		
		CourseInstance ci = new CourseInstance();
		ci.setId("2007Z/222");
		Assessment lab1 = new Assessment();
		lab1.setId(Long.valueOf(1));
		lab1.setCourseInstance(ci);
		lab1.setShortName("LAB1");
		ci.getAssessments().add(lab1);
		lab1.setProgramType("java");
		lab1.setProgramVersion(1);
		StringBuilder sb = new StringBuilder();
		sb.append("		setPassed(rawScore()>=5);\n");
		sb.append("		setPresent(rawPresent());\n");
		sb.append("		setScore(5*rawScore());\n");
		lab1.setProgram(sb.toString());
		assessments.add(lab1);
		
		AssessmentScore s1 = new AssessmentScore();
		s1.setAssessment(lab1);
		s1.setUser(users.get(0));
		s1.setId(Long.valueOf(1));
		s1.setRawPresent(true);
		s1.setRawScore(3);
		assessmentScores.add(s1);
		
		s1 = new AssessmentScore();
		s1.setAssessment(lab1);
		s1.setUser(users.get(1));
		s1.setId(Long.valueOf(2));
		s1.setRawPresent(true);
		s1.setRawScore(3);
		assessmentScores.add(s1);
		
		AssessmentFlag flag = new AssessmentFlag();
		flag.setCourseInstance(ci);
		flag.setId(Long.valueOf(1));
		flag.setShortName("PRAVO");
		sb = new StringBuilder();
		sb.append("		setValue(!overrideValue());\n");
		flag.setProgram(sb.toString());
		flag.setProgramType("java");
		flag.setProgramVersion(1);
		assessmentFlags.add(flag);
		
		AssessmentFlagValue v1 = new AssessmentFlagValue();
		v1.setUser(users.get(0));
		v1.setId(Long.valueOf(1));
		v1.setAssessmentFlag(flag);
		v1.setManuallySet(true);
		v1.setManualValue(true);
		assessmentFlagValues.add(v1);
		
		v1 = new AssessmentFlagValue();
		v1.setUser(users.get(1));
		v1.setId(Long.valueOf(2));
		v1.setAssessmentFlag(flag);
		v1.setManuallySet(true);
		v1.setManualValue(true);
		assessmentFlagValues.add(v1);
	}
	
	@After
	public void tearDown() {
		
	}

	@Test
	public void test1() {
		IScoreCalculatorEngine engine = ScoreCalculatorEngineFactory.getEngine();
		
		SimpleAssessmentDataProvider prov = new SimpleAssessmentDataProvider();
		prov.offerAssessmentScore(assessmentScores);
		
		IScoreCalculatorContext context = engine.createContext(ci, prov);
		context.setCurrentUser(users.get(0));

		StudentScore score = engine.calculateScore("LAB1", context);
		System.out.println(score);
		
		//assessments.get(0).setProgramVersion(2);
		context.setCurrentUser(users.get(1));
		
		score = engine.calculateScore("LAB1", context);
		System.out.println(score);
		
		
	}
	
	@Test
	public void test2() {
		IScoreCalculatorEngine engine = ScoreCalculatorEngineFactory.getEngine();
		
		SimpleAssessmentDataProvider prov = new SimpleAssessmentDataProvider();
		prov.offerAssessmentFlagValues(assessmentFlagValues);
		
		IScoreCalculatorContext context = engine.createContext(ci, prov);
		context.setCurrentUser(users.get(0));

		StudentFlag flag = engine.calculateFlag("PRAVO", context);
		System.out.println(flag);		
	}

	public static void main(String[] args) {
		AssessmentCalcTest t = new AssessmentCalcTest();
		t.setUp();
		t.test2();
		t.tearDown();
	}
}
