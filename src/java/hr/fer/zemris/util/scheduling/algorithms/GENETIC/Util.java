package hr.fer.zemris.util.scheduling.algorithms.GENETIC;

import hr.fer.zemris.util.time.DateStamp;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Random;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

//utility razred sa zajednickim metodama
public class Util {
	private static Random random;
	private static Calendar calendar, currentCalendar;
	private static JPanel feedbackPanel;
	private static JTextArea feedbackArea;
	private static String DATE_FORMAT_NOW;
	private static SimpleDateFormat sdf;

	// vrijednosti vezane uz populaciju
	public static final int populationSize = 100;
	public static final int populationSum;
	public static final double EPS = 0.00001;

	// vrijednosti vezane za mutaciju
	public static final double mutation = 0.2;
	public static final double mutStudCount = 0.3;

	// vrijednosti vezane uz selekciju
	public static final int elitismNumber = 4;
	public static final int newIndividuals = 6;

	// vrijednosti vezane uz krizanje
	public static final double crossover = 0.8;

	static {
		random = new Random();
		calendar = new GregorianCalendar();

		feedbackPanel = new JPanel();
		feedbackArea = new JTextArea("", 20, 30);
		feedbackPanel.add(new JScrollPane(feedbackArea));

		DATE_FORMAT_NOW = "HH:mm:ss:SSS";
		sdf = new SimpleDateFormat(DATE_FORMAT_NOW);

		populationSum = (populationSize * (populationSize + 1)) / 2;
	}

	// vraca random broj u skupu [0, n>
	public static int random(int n) {
		return random.nextInt(n);
	}

	// vraca random double u skupu [0, 1>
	public static double random() {
		return random.nextDouble();
	}

	// racuna DateStamp za n dana prije ili poslije
	public static DateStamp dayCalc(DateStamp dateStamp, Integer days) {
		String string = dateStamp.toString();
		int year = Integer.parseInt(string.substring(0, 4));
		int month = Integer.parseInt(string.substring(5, 7)) - 1;
		int day = Integer.parseInt(string.substring(8, 10));

		calendar.set(year, month, day);
		calendar.add(Calendar.DAY_OF_MONTH, days);

		return new DateStamp(calendar.get(Calendar.YEAR), calendar
				.get(Calendar.MONTH) + 1, calendar.get(Calendar.DAY_OF_MONTH));
	}

	// metoda na temelju stringa iz getTimeDistance vraca broj dana
	public static int getDistance(String distance) {
		return Integer.parseInt((distance.substring(0, distance.length() - 1)));
	}

	public static JPanel getFeedBackPanel() {
		return feedbackPanel;
	}

//	public static void printFeedback(String feedback) {
//		feedbackArea.append(feedback + "\n");
//	}

	public static String currentTime() {
		currentCalendar = Calendar.getInstance();
		return sdf.format(currentCalendar.getTime());
	}

	public static int randomPopElem() {
		int random = Util.random(populationSum) + 1;
		double ret = (Math.sqrt(8 * random + 1) - 1) / 2;

		int result;
		if (Math.abs(ret - Math.round(ret)) <= Util.EPS) {
			result = (int) Math.floor(ret);
		} else {
			result = (int) Math.ceil(ret);
		}
		
		return Util.populationSize - result;
	}
}
