package hr.fer.zemris.util.scheduling.algorithms;

import hr.fer.zemris.util.scheduling.algorithms.CLONALG.Antibody;
import hr.fer.zemris.util.scheduling.algorithms.CLONALG.Clonalg;
import hr.fer.zemris.util.scheduling.algorithms.GENETIC.GenSched;
import hr.fer.zemris.util.scheduling.algorithms.HS.HarmonySearch;
import hr.fer.zemris.util.scheduling.algorithms.PSO.PSOAlgorithm;
import hr.fer.zemris.util.scheduling.algorithms.filip_boltuzic.BCOScheduler;
import hr.fer.zemris.util.scheduling.algorithms.sds.SDSImplementation;
import hr.fer.zemris.util.scheduling.support.ISchedulingAlgorithm;
import hr.fer.zemris.util.scheduling.support.ISchedulingData;
import hr.fer.zemris.util.scheduling.support.ISchedulingMonitor;
import hr.fer.zemris.util.scheduling.support.ISchedulingResult;
import hr.fer.zemris.util.scheduling.support.ItemCache;
import hr.fer.zemris.util.scheduling.support.ReservationManager2;
import hr.fer.zemris.util.scheduling.support.SchedulingAlgorithmStatus;
import hr.fer.zemris.util.scheduling.support.SchedulingException;
import hr.fer.zemris.util.scheduling.support.algorithmview.IPlan;

import java.awt.Component;
import java.awt.Font;
import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Map;
import java.util.Random;

import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.CategoryAxis;
import org.jfree.chart.axis.CategoryLabelPositions;
import org.jfree.chart.labels.StandardCategoryItemLabelGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer3D;
import org.jfree.chart.renderer.category.CategoryItemRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

public class MainScheduler implements ISchedulingAlgorithm {

	private ISchedulingAlgorithm[] algorithms = new ISchedulingAlgorithm[] {new Clonalg(), new BCOScheduler(), new Clonalg(), new GenSched(), new HarmonySearch(), new PSOAlgorithm(), new SDSImplementation()};
	private int[] algorithmPriorities;
	private int[] algorithmIntervals;
	private ISchedulingAlgorithm runningAlgorithm;
	private ISchedulingMonitor schedulingMonitor;
	private static final int numberOfHardConstraints = 4;
	private ISchedulingResult bestSolution;
	public static IPlan plan;
	Random r = new Random();
	
	private SchedulingAlgorithmStatus algorithmStatus;
	private ChartPanel executionFeedback;
	private DefaultCategoryDataset chartDataset = new DefaultCategoryDataset();
	private String[] fitnessLabels = new String[]{"Unsatisfied preconditions", "Room conflicts", "Student conflicts", "Overcrowded rooms", "Number of terms", "Vacant places"};
	private ReservationManager2 fixedReservationManager;
	private Thread algorithmExecutionThread;
	public static ItemCache jmbagsCache;
	private boolean threadInterruptSignal;
	long start;
	int iter=0;
	
	@Override
	public void prepare(IPlan plan, Map<String, ISchedulingData> eventsSchedulingData) throws SchedulingException {
		executionFeedback=new ChartPanel(null);
		MainScheduler.plan=plan;
		getItemCache(eventsSchedulingData);
		int maxi=0;
		for(int i=0;i<algorithms.length;i++) {
			if(algorithmPriorities[i]!=0)
				algorithms[i].prepare(plan, eventsSchedulingData);
			if(algorithmPriorities[i]>algorithmPriorities[maxi])
				maxi=i;
		}
		runningAlgorithm = algorithms[maxi];
		algorithmStatus=SchedulingAlgorithmStatus.PREPARED;
		this.fixedReservationManager=new ReservationManager2(eventsSchedulingData);
	}
	
	private void getItemCache(Map<String, ISchedulingData> eventsSchedulingData) {
		for(ISchedulingData d:eventsSchedulingData.values()) {
			jmbagsCache=d.getJmbagsCache();
			break;
		}
	}

	@Override
	public void start() throws SchedulingException {
		start=System.currentTimeMillis();
		threadInterruptSignal=false;
		for(int i=0;i<algorithms.length;i++)
			if(algorithmPriorities[i]!=0)
				algorithms[i].start();
		algorithmExecutionThread = new Thread(new Runnable() {
			public void run() {
				while(!threadInterruptSignal) {
						step();
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {}
				}
				schedulingMonitor.algorithmStatusChangeNotification();
				System.out.println(System.currentTimeMillis()-start);
			}
		});
//		feedbackUpdateThread=new Thread(new Runnable() {
//			public void run() {
//				while(!threadInterruptSignal) {
//					updateExecutionFeedbackPanel();
//					try {
//						Thread.sleep(1000);
//					} catch (InterruptedException e) {}
//				}
//			}
//		});
//		feedbackUpdateThread.start();
		algorithmExecutionThread.start();
		algorithmStatus=SchedulingAlgorithmStatus.RUNNING;
		schedulingMonitor.algorithmStatusChangeNotification();
	}

	@Override
	public void step() throws SchedulingException {
		int next = r.nextInt(algorithmIntervals.length);
		if(runningAlgorithm!=algorithms[algorithmIntervals[next]]) {
			System.out.println("Change: " + runningAlgorithm.getClassName() + "->" + algorithms[algorithmIntervals[next]].getClassName());
			algorithms[algorithmIntervals[next]].use(runningAlgorithm.getResult());
			runningAlgorithm=algorithms[algorithmIntervals[next]];
		}
		runningAlgorithm.step();
		bestSolution=runningAlgorithm.getResult();
		updateExecutionFeedbackPanel();
	}

	@Override
	public void stop() throws SchedulingException {
		algorithmStatus=SchedulingAlgorithmStatus.SUCCESS;
		threadInterruptSignal=true;
		try {
			algorithmExecutionThread.join();
//			feedbackUpdateThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		runningAlgorithm.stop();
		updateExecutionFeedbackPanelFinal();
	}

	@Override
	public void use(ISchedulingResult result) throws SchedulingException {}
	
	@Override
	public ISchedulingResult getResult() throws SchedulingException {
		return bestSolution;
	}

	@Override
	public ISchedulingResult[] getResults() throws SchedulingException {return null;}

	@Override
	public Component getExecutionFeedback() throws SchedulingException {
		return executionFeedback;
	}
	
	@Override
	public SchedulingAlgorithmStatus getStatus() {
		return algorithmStatus;
	}
	
	@Override
	public void registerSchedulingMonitor(ISchedulingMonitor sm) throws SchedulingException {
		schedulingMonitor=sm;
	}
	
	@Override
	public String getClassName() {
		return this.getClass().getCanonicalName();
	}
	
	private void updateExecutionFeedbackPanel() {
		if(bestSolution==null)
			return;
		ReservationManager2 manager = new ReservationManager2(fixedReservationManager);
		Antibody a = Antibody.convertFromResult(bestSolution);
		a.rebuildManager(manager);
		a.evaluate(manager);
		if(executionFeedback.getChart()==null) {
			executionFeedback.removeAll();
			if(bestSolution==null) {
				executionFeedback =  null;
				return;
			}
			int[] fitness = a.getFitnessVector();
			for(int i=0;i<fitness.length;i++) {
				if(i<numberOfHardConstraints)
					chartDataset.addValue(fitness[i], "Hard constraint", fitnessLabels[i]);
				else
					chartDataset.addValue(fitness[i], "Soft constraint", fitnessLabels[i]);
			}
			JFreeChart chart = ChartFactory.createBarChart3D(null, null, null, chartDataset, PlotOrientation.VERTICAL,true,true,false);
			CategoryItemRenderer renderer = new BarRenderer3D();
			DecimalFormat decimalformat1 = new DecimalFormat("##,###");
			renderer.setBaseItemLabelGenerator(new StandardCategoryItemLabelGenerator("{2}", decimalformat1));
			renderer.setBaseItemLabelsVisible(true);
			chart.setBackgroundPaint(null);
			CategoryAxis domainAxis = chart.getCategoryPlot().getDomainAxis();
	        domainAxis.setCategoryLabelPositions(CategoryLabelPositions.UP_45);
	        Font f=domainAxis.getTickLabelFont();
	        domainAxis.setTickLabelFont(new Font(f.getFontName(), f.getStyle(), 9));
			chart.getCategoryPlot().setRenderer(renderer);
			executionFeedback.setChart(chart);
			return;
		}
		int[] fitness = a.getFitnessVector();
		for(int i=0;i<fitness.length;i++) {
			if(i<numberOfHardConstraints)
				chartDataset.setValue(fitness[i], "Hard constraint", fitnessLabels[i]);
			else
				chartDataset.setValue(fitness[i], "Soft constraint", fitnessLabels[i]);
		}
	}
	
	private void updateExecutionFeedbackPanelFinal() {
		executionFeedback.setChart(null);
		Antibody a = Antibody.convertFromResult(bestSolution);
		ReservationManager2 manager = new ReservationManager2(fixedReservationManager);
		a.rebuildManager(manager);
		a.evaluate(manager);
		JTextArea textArea = new JTextArea();
		System.out.println(Arrays.toString(a.getFitnessVector()));
		String text = a.toString();
		textArea.setText(text);
		JScrollPane sp = new JScrollPane(textArea);
		sp.setPreferredSize(executionFeedback.getSize());
		executionFeedback.add(sp);
	}

	public void setPriorities(int[] priorities) {
		int sum=0;
		for(int i:priorities)
			sum+=i;
		algorithmIntervals=new int[sum];
		int next=0;
		for(int i=0;i<priorities.length;i++) {
			for(int j=0;j<priorities[i];j++) {
				algorithmIntervals[next+j]=i;
			}
			next+=priorities[i];
		}
		algorithmPriorities=priorities;
	}
}
