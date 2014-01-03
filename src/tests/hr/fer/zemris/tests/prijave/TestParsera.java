package hr.fer.zemris.tests.prijave;

import java.io.InputStream;

import java.lang.reflect.Constructor;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import hr.fer.zemris.jcms.applications.ApplContainer;
import hr.fer.zemris.jcms.applications.ApplSourceCodeProducer;
import hr.fer.zemris.jcms.applications.IApplBuilderRunner;
import hr.fer.zemris.jcms.applications.model.ApplElement;
import hr.fer.zemris.jcms.applications.model.ApplNamedElement;
import hr.fer.zemris.jcms.applications.parser.ApplCodeParser;
import hr.fer.zemris.jcms.applications.parser.ApplCodeSection;
import hr.fer.zemris.jcms.parsers.TextService;
import hr.fer.zemris.jcms.service.assessments.DynaCodeEngineFactory;
import hr.fer.zemris.jcms.service2.course.applications.ApplStudentDataProviderImpl;
import hr.fer.zemris.jcms.service2.course.applications.IApplStudentDataProvider;
import hr.fer.zemris.jcms.web.actions.data.support.DummyMessageLoggerImpl;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;
import hr.fer.zemris.jcms.web.actions.data.support.LoggerMessage;

public class TestParsera {

	@Test
	public void test1() throws Exception {
		InputStream is = this.getClass().getResourceAsStream("NadoknadaLabosa.txt");
		String text = TextService.inputStreamToString(is, "UTF-8");
		ApplCodeParser parser = new ApplCodeParser(text);
		List<ApplCodeSection> sections = parser.getSections();
		Assert.assertEquals("Greška u broju sekcija.", 1, sections.size());
		Assert.assertEquals("Greška u imenu sekcije.", "def", sections.get(0).getSectionName());
		Assert.assertEquals("Greška u broju argumenata sekcije.", 0, sections.get(0).getArguments().size());
	}
	
	@Test
	public void test2() throws Exception {
		InputStream is = this.getClass().getResourceAsStream("NadoknadaJednogOdLab.txt");
		String text = TextService.inputStreamToString(is, "UTF-8");
		ApplCodeParser parser = new ApplCodeParser(text);
		List<ApplCodeSection> sections = parser.getSections();
		Assert.assertEquals("Greška u broju sekcija.", 3, sections.size());
		Assert.assertEquals("Greška u imenu sekcije 1.", "def", sections.get(0).getSectionName());
		Assert.assertEquals("Greška u imenu sekcije 2.", "filter", sections.get(1).getSectionName());
		Assert.assertEquals("Greška u imenu sekcije 3.", "filter", sections.get(2).getSectionName());
		Assert.assertEquals("Greška u broju argumenata sekcije 1.", 0, sections.get(0).getArguments().size());
		Assert.assertEquals("Greška u broju argumenata sekcije 2.", 1, sections.get(1).getArguments().size());
		Assert.assertEquals("Greška u broju argumenata sekcije 3.", 0, sections.get(2).getArguments().size());
		Assert.assertEquals("Greška u argumentu sekcije 3.", "vjezba", sections.get(1).getArguments().get(0));
	}
	@Test
	public void test3() throws Exception {
		InputStream is = this.getClass().getResourceAsStream("PrijavaPonovljeneProvjere.txt");
		String text = TextService.inputStreamToString(is, "UTF-8");
		ApplCodeParser parser = new ApplCodeParser(text);
		List<ApplCodeSection> sections = parser.getSections();
		Assert.assertEquals("Greška u broju sekcija.", 4, sections.size());
		Assert.assertEquals("Greška u imenu sekcije 1.", "global", sections.get(0).getSectionName());
		Assert.assertEquals("Greška u imenu sekcije 2.", "def", sections.get(1).getSectionName());
		Assert.assertEquals("Greška u imenu sekcije 3.", "filter", sections.get(2).getSectionName());
		Assert.assertEquals("Greška u imenu sekcije 4.", "filter", sections.get(3).getSectionName());
		Assert.assertEquals("Greška u broju argumenata sekcije 1.", 0, sections.get(0).getArguments().size());
		Assert.assertEquals("Greška u broju argumenata sekcije 2.", 0, sections.get(1).getArguments().size());
		Assert.assertEquals("Greška u broju argumenata sekcije 3.", 0, sections.get(2).getArguments().size());
		Assert.assertEquals("Greška u broju argumenata sekcije 4.", 1, sections.get(3).getArguments().size());
		Assert.assertEquals("Greška u argumentu sekcije 4.", "provjera", sections.get(3).getArguments().get(0));
	}

	@Test
	public void test4() throws Exception {
		InputStream is = this.getClass().getResourceAsStream("PrijavaZaLab1.txt");
		String text = TextService.inputStreamToString(is, "UTF-8");
		ApplCodeParser parser = new ApplCodeParser(text);
		List<ApplCodeSection> sections = parser.getSections();
		Assert.assertEquals("Greška u broju sekcija.", 1, sections.size());
		Assert.assertEquals("Greška u imenu sekcije.", "def", sections.get(0).getSectionName());
		Assert.assertEquals("Greška u broju argumenata sekcije.", 0, sections.get(0).getArguments().size());
	}

	@Test
	public void test5() throws Exception {
		InputStream is = this.getClass().getResourceAsStream("PrijavaPonovljeneProvjere.txt");
		String text = TextService.inputStreamToString(is, "UTF-8");
		ApplCodeParser parser = new ApplCodeParser(text);
		List<ApplCodeSection> sections = parser.getSections();
		String source = ApplSourceCodeProducer.getSource("DynaClassTmpP_tmp", "studtest2.dynamic", sections);
		IMessageLogger messageLogger = new DummyMessageLoggerImpl();
		Class<?> c = DynaCodeEngineFactory.getEngine().oneTimeCompile(messageLogger, "studtest2.dynamic", "DynaClassTmpP_tmp", source);
		if(c==null) {
			for(LoggerMessage m : messageLogger.getMessages()) {
				System.out.println(m.getMessageText());
			}
		}
		Assert.assertNotNull("Nisam uspio prevesti source!", c);
		ApplContainer cont = new ApplContainer();
		cont.setDefinable(false); cont.setExecutable(false);
		IApplStudentDataProvider prov = new ApplStudentDataProviderImpl(null, null, null);
		Constructor<?> constr = c.getConstructor(ApplContainer.class, IApplStudentDataProvider.class);
		IApplBuilderRunner builderRunner = (IApplBuilderRunner)constr.newInstance(cont, prov);
		cont.setDefinable(true);
		builderRunner.buildApplication();
		// Sada provjeri koje ima elemente a koje filter sekcije
		Set<String> elemNames = new HashSet<String>();
		for(ApplElement e : cont.getElements()) {
			System.out.println("Imam element "+e);
			if(e instanceof ApplNamedElement) {
				elemNames.add(((ApplNamedElement)e).getName());
			}
		}
		for(ApplCodeSection s : sections) {
			if(!s.getSectionName().equals("filter")) continue;
			if(s.getArguments().isEmpty()) continue;
			if(!elemNames.contains(s.getArguments().get(0))) {
				System.out.println("Definiran je filter za element "+s.getArguments().get(0)+", međutim, tog elementa nema u prijavi!");
			}
		}
		
	}
	
	@Test
	public void test6() throws Exception {
		InputStream is = this.getClass().getResourceAsStream("PrijavaPonovljeneProvjere.txt");
		String text = TextService.inputStreamToString(is, "UTF-8");
		Class<?> c = DynaCodeEngineFactory.getEngine().classForProgram("P", 17L, text, 1);
		if(c==null) {
			return;
		}
		Assert.assertNotNull("Nisam uspio prevesti source!", c);
		ApplContainer cont = new ApplContainer();
		cont.setDefinable(false); cont.setExecutable(false);
		IApplStudentDataProvider prov = new ApplStudentDataProviderImpl(null, null, null);
		Constructor<?> constr = c.getConstructor(ApplContainer.class, IApplStudentDataProvider.class);
		IApplBuilderRunner builderRunner = (IApplBuilderRunner)constr.newInstance(cont, prov);
		cont.setDefinable(true);
		builderRunner.buildApplication();
		for(ApplElement e : cont.getElements()) {
			System.out.println("Imam element "+e);
		}
	}
}
