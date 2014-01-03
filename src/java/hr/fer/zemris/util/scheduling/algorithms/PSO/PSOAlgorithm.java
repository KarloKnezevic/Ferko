package hr.fer.zemris.util.scheduling.algorithms.PSO;


import hr.fer.zemris.util.scheduling.support.ISchedulingAlgorithm;
import hr.fer.zemris.util.scheduling.support.ISchedulingData;
import hr.fer.zemris.util.scheduling.support.ISchedulingMonitor;
import hr.fer.zemris.util.scheduling.support.ISchedulingResult;
import hr.fer.zemris.util.scheduling.support.SchedulingAlgorithmStatus;
import hr.fer.zemris.util.scheduling.support.SchedulingException;
//import hr.fer.zemris.util.scheduling.support.SchedulingResult;
import hr.fer.zemris.util.scheduling.support.algorithmview.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import javax.swing.JPanel;
import org.jfree.chart.JFreeChart;



public class PSOAlgorithm implements ISchedulingAlgorithm{
	
	/*
	 * Properties
	 */
	
	private Particle globalBestResult = null;
	private List<Particle> particles = new ArrayList<Particle>();
	private SchedulingAlgorithmStatus status; 
	private Map<String, ISchedulingData> eventsSchedulingData;
	private IPlan plan;
	
	private final int NUMBER_OF_PARTICLES = 5;
	
	
	/*
	 * Methods
	 */
	public void prepare(IPlan plan, Map<String, ISchedulingData> eventsSchedulingData) throws SchedulingException
	{		
		this.plan = plan;
		this.eventsSchedulingData = eventsSchedulingData;

		status = SchedulingAlgorithmStatus.PREPARED;
	}

	public void step() throws SchedulingException
	{
		status = SchedulingAlgorithmStatus.RUNNING;
		for(Particle particle : particles)
		{
			particle.switchTerms(globalBestResult);
			particle.switchStudents(globalBestResult);
			for(String event : particle.eventsTermList.keySet())
				for(TermRecord term : particle.eventsTermList.get(event))
					particle.changeRoom(term);
			
			particle.calculateFitness();
//			System.out.println("PSO " + particle.fitness);
			if(particle.fitness < particle.ownBestResult.fitness)
				particle.ownBestResult = new Particle(particle);
			if(particle.fitness < globalBestResult.fitness)
				globalBestResult = new Particle(particle);
		}
	}

	public void start() throws SchedulingException
	{
		
		int i = 0, j = particles.size();
		int n = NUMBER_OF_PARTICLES < particles.size() ? particles.size() : NUMBER_OF_PARTICLES;
		while (j < n) {
			try {
				Particle particle = new Particle(plan, eventsSchedulingData);
				particles.add(particle);
				if (globalBestResult == null
						|| particle.fitness < globalBestResult.fitness)
					globalBestResult = particle;
				
				j++;
				i = 0;
//				System.out.println("PSO " + particle.fitness);
			} catch (SchedulingException e) {
				if(++i > 30)
				{
					status = SchedulingAlgorithmStatus.FAILURE;
					throw new SchedulingException(e);
				}
			}
		}
		
//		for(i = 0; i < 500; i++)
//			step();
		
		status = SchedulingAlgorithmStatus.SUCCESS;
		
//		System.out.println("\nGLOBAL BEST: " + globalBestResult.fitness + "\n");
	}

	public void stop() throws SchedulingException
	{
		
	}
	
	public void use(ISchedulingResult result) throws SchedulingException
	{
		particles.add(new Particle(result, plan, eventsSchedulingData));
	}
	
	public ISchedulingResult getResult() throws SchedulingException
	{		
		return globalBestResult.toResult();
	}
	
	public JPanel getExecutionFeedback() throws SchedulingException {

		return null;
	}
	
	public JFreeChart getStatusChart()
	{
		return null;
	}
	
	public ISchedulingResult[] getResults() throws SchedulingException
	{
		ISchedulingResult[] result = new ISchedulingResult[particles.size()];
		int i = 0;
		for(Particle p : particles)
			result[i++] = p.toResult();
		
		return result;
	}
			
	public void registerSchedulingMonitor(ISchedulingMonitor sm) throws SchedulingException
	{
		
	}

	public SchedulingAlgorithmStatus getStatus()
	{
		return status;
	}
	
	public String getClassName()
	{
		return this.getClass().getCanonicalName();
	}
	
}
