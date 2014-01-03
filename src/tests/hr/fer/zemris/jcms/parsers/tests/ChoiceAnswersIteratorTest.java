package hr.fer.zemris.jcms.parsers.tests;

import org.junit.Assert;
import org.junit.Test;

import hr.fer.zemris.jcms.parsers.ChoiceAnswersIterator;

/**
 * Testiranje razreda {@link ChoiceAnswersIterator}.
 * @author Ivan Krišto
 *
 */
public class ChoiceAnswersIteratorTest {
	
	@Test
	public void nextTest01() {
		String testString = "I could dance with you till the cows come home...But I would rather dance with the cows till you come home";
		StringBuilder sb = new StringBuilder();
		ChoiceAnswersIterator iter = new ChoiceAnswersIterator(testString, " ");
		sb.append(iter.next()).append(iter.next()).append(iter.next()).append(iter.next());
		Assert.assertEquals("Icoulddancewith", sb.toString());
	}
	
	@Test
	public void nextTest02() {
		String testString = "I\tcould\tdance\twith\tyou\ttill\tthe cows come home...But I would rather dance with the cows till you come home";
		StringBuilder sb = new StringBuilder();
		ChoiceAnswersIterator iter = new ChoiceAnswersIterator(testString, "\t");
		sb.append(iter.next()).append(iter.next()).append(iter.next()).append(iter.next());
		Assert.assertEquals("Icoulddancewith", sb.toString());
	}
	
	@Test
	public void nextTest03() {
		String testString = "A\tB\tC";
		StringBuilder sb = new StringBuilder();
		ChoiceAnswersIterator iter = new ChoiceAnswersIterator(testString, "\t");
		sb.append(iter.next()).append(iter.next()).append(iter.next());
		Assert.assertEquals("ABC", sb.toString());
	}
}
