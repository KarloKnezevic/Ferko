package hr.fer.zemris.util.scheduling.algorithms.HS;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.swing.JTextArea;

import hr.fer.zemris.util.scheduling.algorithms.CLONALG.Antibody;
import hr.fer.zemris.util.scheduling.support.ISchedulingAlgorithm;
import hr.fer.zemris.util.scheduling.support.ISchedulingData;
import hr.fer.zemris.util.scheduling.support.ISchedulingMonitor;
import hr.fer.zemris.util.scheduling.support.ISchedulingResult;
import hr.fer.zemris.util.scheduling.support.ReservationManager2;
import hr.fer.zemris.util.scheduling.support.RoomData;
import hr.fer.zemris.util.scheduling.support.SchedulingAlgorithmStatus;
import hr.fer.zemris.util.scheduling.support.SchedulingException;
import hr.fer.zemris.util.scheduling.support.algorithmview.IDefinition;
import hr.fer.zemris.util.scheduling.support.algorithmview.IEvent;
import hr.fer.zemris.util.scheduling.support.algorithmview.IGroup;
import hr.fer.zemris.util.scheduling.support.algorithmview.ILocationParameter;
import hr.fer.zemris.util.scheduling.support.algorithmview.IPlan;
import hr.fer.zemris.util.scheduling.support.algorithmview.IPrecondition;
import hr.fer.zemris.util.scheduling.support.algorithmview.ITerm;
import hr.fer.zemris.util.scheduling.support.algorithmview.ITimeParameter;
import hr.fer.zemris.util.time.DateStamp;
import hr.fer.zemris.util.time.TimeSpan;

/**
 * @author Kusalic Domagoj
 * 
 */
public class HarmonySearch implements ISchedulingAlgorithm {

	private TreeMap<Double,Jedinka> jedinke = new TreeMap<Double,Jedinka>();
	private SchedulingAlgorithmStatus status;
	private Map<String, ISchedulingData> eventsSchedulingData;
	private IPlan plan;
	private MojPlan mojPlan;
	private final int BROJ_JEDINKI = 10;

	public void start() throws SchedulingException {
		status = SchedulingAlgorithmStatus.RUNNING;
		mojPlan = new MojPlan(plan);
		//ispisiPlan();
		//System.out.println("--");
		//Jedinka jed=new Jedinka(mojPlan,eventsSchedulingData);
		for (int i=0;i<BROJ_JEDINKI*4 && jedinke.size()<BROJ_JEDINKI;i++) {
			Jedinka jed=new Jedinka(mojPlan,eventsSchedulingData);
			if (jed.uspio) {
				procjena(jed);
				jedinke.put(Double.valueOf(jed.dobrota),jed);
			}
		}
		if (jedinke.size()<BROJ_JEDINKI/2)
			status = SchedulingAlgorithmStatus.FAILURE;
	
		//System.exit(1);
	}

	public void step() throws SchedulingException {
		Jedinka jed=new Jedinka(mojPlan, eventsSchedulingData,jedinke);
		if (jed.uspio) {
			procjena(jed);
			jedinke.put(Double.valueOf(jed.dobrota), jed);
			jedinke.remove(jedinke.firstKey());		
		}
	}


	public void use(ISchedulingResult result) throws SchedulingException {
		//System.out.println("Pozvan je use()");
		if (result == null) {
			System.out.println("Laže selo, lažu ljudi... Laže cigan, laže pjesma...");
			return;
		}
		Jedinka jed=new Jedinka(mojPlan, eventsSchedulingData,result);
		//if (jed.uspio) System.out.println("Bravo!");
		//else System.out.println("NEVALJA");
		if (jed.uspio) {
			procjena(jed);
			jedinke.put(Double.valueOf(jed.dobrota), jed);
			jedinke.remove(jedinke.firstKey());
			//System.out.println("--: "+Double.valueOf(jed.dobrota));
		}
	}

	// ---------------------------------------------------------------------------------

	private void procjena(Jedinka jed) {
		Antibody a = Antibody.convertFromResult(jed.toISD());
		ReservationManager2 manager = new ReservationManager2(new ReservationManager2(eventsSchedulingData));
		a.rebuildManager(manager);
		a.evaluate(manager);
		int[] x = a.getFitnessVector();
		double value=100000000-(x[0]*3000000+x[1]*500000+x[2]*1000+x[3]*10000+x[4]*100+x[5])+Math.random();
		jed.dobrota=value;
		//System.out.println(Arrays.toString(x));
	}
	
	private boolean znam() {
		if (plan.isEqualTermSequenceInEachEvent()
				|| plan.isEqualStudentDistributionInEachEvent())
			return false;
		if (plan.getDefinition()!=null && plan.getDefinition().getGroups().size()>0)
			return false;
		for (IEvent ev: plan.getPlanEvents()) {
			if (ev.getDefinition()!=null && ev.getDefinition().getGroups().size()>0)
				return false;
			for (ITerm it: ev.getTerms()) {
				if (it.getDefinition()!=null &&
					it.getDefinition().getIndividuals().size()>0 ||
					it.getDefinition().getTimeParameters().size()>0 ||
					it.getDefinition().getLocationParameters().size()>0 ||
					it.getDefinition().getGroups().size()>0)
					return false;
			}
		}
		return true;
	}

	@SuppressWarnings( { "unused" })
	private void ispisiData() {
		for (String s : eventsSchedulingData.keySet()) {
			System.out
					.println("################################################################ eventId: "
							+ s);
			Map<String, Map<DateStamp, List<TimeSpan>>> people = eventsSchedulingData
					.get(s).getPeopleData();
			for (String ss : people.keySet()) {
				System.out.println("  " + ss);
				for (DateStamp ds : people.get(ss).keySet()) {
					System.out.println("    " + ds + "---"
							+ people.get(ss).get(ds));
				}
			}
			Map<RoomData, Map<DateStamp, List<TimeSpan>>> terms = eventsSchedulingData
					.get(s).getTermData();
			for (RoomData rd : terms.keySet()) {
				System.out.println("  " + rd.getId() + " " + rd.getName() + " "
						+ rd.getCapacity());
				for (DateStamp ds : terms.get(rd).keySet()) {
					System.out.println("    " + ds + "---"
							+ terms.get(rd).get(ds));
				}
			}
		}
	}

	@SuppressWarnings("unused")
	private void ispisiPlan() {
		System.out.println("plan.getName(): " + plan.getName());
		System.out.println("plan.isEqualTermSequenceInEachEvent(): "
				+ plan.isEqualTermSequenceInEachEvent());
		System.out.println("plan.isEqualStudentDistributionInEachEvent(): "
				+ plan.isEqualStudentDistributionInEachEvent());
		System.out.println("plan.getTermNumberInEachEvent(): "
				+ plan.getTermNumberInEachEvent());
		System.out.print("plan.getDefinition()==null: ");
		System.out.println(plan.getDefinition() == null);
		if (plan.getDefinition() != null)
			ispisiDefinition("", plan.getDefinition());
		for (IEvent event : plan.getPlanEvents()) {
			System.out.println("----------------");
			System.out.println("  event.getName(): " + event.getName());
			System.out.println("  event.getId(): " + event.getId());
			System.out.println("  event.getTermDuration(): "
					+ event.getTermDuration());
			System.out.println("  event.getMaximumDuration(): "
					+ event.getMaximumDuration());
			System.out.print("  event.getDefinition()==null: ");
			System.out.println(event.getDefinition() == null);
			if (event.getDefinition() != null)
				ispisiDefinition("  ", event.getDefinition());
			System.out
					.println("  event.getEventDistribution(): Type MinimumTermNumber MaximumTermNumber: "
							+ (event.getEventDistribution().getType() == 6 ? "RANDOM"
									: "GIVEN")
							+ " "
							+ event.getEventDistribution()
									.getMinimumTermNumber()
							+ " "
							+ event.getEventDistribution()
									.getMaximumTermNumber());
			System.out.print("  event.getPreconditionEvents()==null: ");
			System.out.println(event.getPreconditionEvents() == null);
			if (event.getPreconditionEvents() != null)
				for (IPrecondition ip : event.getPreconditionEvents()) {
					System.out
							.println("  IPrecondition - getEvent.getId, timeDistance, timeDistanceValue: "
									+ ip.getEvent().getId()
									+ " "
									+ ip.getTimeDistance()
									+ " "
									+ ip.getTimeDistanceValue());
				}
			for (ITerm term : event.getTerms()) {
				System.out.println("  **************");
				System.out.println("    term.getName(): " + term.getName());
				System.out.println("    term.getId(): " + term.getId());
				System.out.print("    term.getDefinition()==null: ");
				System.out.println(term.getDefinition() == null);
				if (term.getDefinition() != null)
					ispisiDefinition("    ", term.getDefinition());
			}
		}
	}

	private void ispisiDefinition(String str, IDefinition d) {
		for (ITimeParameter itp : d.getTimeParameters())
			System.out.println(str + "IDefinition ITimeParameter: "
					+ itp.getFromDate() + " " + itp.getFromTime() + " "
					+ itp.getToDate() + " " + itp.getToTime());
		for (ILocationParameter ilp : d.getLocationParameters())
			System.out.println(str + "IDefinition ILocationParameter: "
					+ ilp.getId() + " " + ilp.getName() + " "
					+ ilp.getActualCapacity());
		for (IGroup ig : d.getGroups())
			System.out
					.println(str + "IDefinition IGroup: " + ig.getGroupID()
							+ " " + ig.getGroupName() + " "
							+ ig.getGroupRelativePath());
		System.out.print(str + "IDefinition Individualci: ");
		for (String ind : d.getIndividuals()) {
			System.out.print(ind + " ");
		}
		System.out.println("");
	}

	// ***********************************************************************************
	public ISchedulingResult getResult() throws SchedulingException {
		return jedinke.get(jedinke.lastKey()).toISD();
	}

	public ISchedulingResult[] getResults() throws SchedulingException {
		ISchedulingResult[] ret = new ISchedulingResult[jedinke.size()];
		int indeks = 0;
		for (Double j : jedinke.keySet()) {
			ret[indeks++] = jedinke.get(j).toISD();
		}
		return ret;
	}

	public void stop() throws SchedulingException {
		status = SchedulingAlgorithmStatus.INTERRUPTED;
	}

	// @see
	// hr.fer.zemris.util.scheduling.support.ISchedulingAlgorithm#prepare(hr.fer.zemris.util.scheduling.support.algorithmview.IPlan,
	// java.util.Map)
	public void prepare(IPlan plan,
			Map<String, ISchedulingData> eventsSchedulingData)
			throws SchedulingException {
		//System.out.println("Pozvan je prepare()");
		this.eventsSchedulingData = eventsSchedulingData;
		this.plan = plan;
		if (znam())
			status = SchedulingAlgorithmStatus.PREPARED;
		else
			status = SchedulingAlgorithmStatus.FAILURE;
	}

	@Override
	public SchedulingAlgorithmStatus getStatus() {
		return status;
	}

	@Override
	public String getClassName() {
		return "HarmonySearch";
	}

	@Override
	public Component getExecutionFeedback() throws SchedulingException {
		return null;
	}

	/*
	 * @seehr.fer.zemris.util.scheduling.support.ISchedulingAlgorithm#
	 * registerSchedulingMonitor
	 * (hr.fer.zemris.util.scheduling.support.ISchedulingMonitor)
	 */
	@Override
	public void registerSchedulingMonitor(ISchedulingMonitor sm)
			throws SchedulingException {
	}
}

class MojPlan {
	String name;
	ArrayList<MojEvent> events;

	MojPlan(IPlan plan) {
		name = plan.getName();
		events = new ArrayList<MojEvent>();
		for (IEvent e : plan.getPlanEvents()) {
			events.add(new MojEvent(plan, e));
		}
		HashSet<String> hsie = new HashSet<String>();
		for (MojEvent x:events){
			for (IPrecondition ip: x.preconditions) {
				hsie.add(ip.getEvent().getId());
			}
		}
		for (MojEvent e : events) {
			if (hsie.contains(e.id) && e.type==3) e.type=2;
		}
	}

	@Override
	public String toString() {
		return "MojPlan \n|name=" + name + "\n|events=" + events;
	}
}

class MojEvent { // sve moje osim ITimeParameter, ILocationParameter i IPrecondition
	String id;
	String name;
	int duration;
	Set<IPrecondition> preconditions;
	int maxTerms;
	int minTerms;
	ArrayList<String> termNames;
	int type=0;
	List<String> people;
	List<ILocationParameter> location;
	List<ITimeParameter> time;
	
	public MojEvent(IPlan plan, IEvent e) {
		id = e.getId();
		name = e.getName();
		duration = e.getTermDuration();
		preconditions = e.getPreconditionEvents();
		termNames = new ArrayList<String>();
		if (e.getEventDistribution().getType()==7) {
			minTerms=maxTerms=e.getTerms().size();
			for (int i=0;i<maxTerms;i++) termNames.add(e.getTerms().get(i).getName());
		} else {
			minTerms=e.getEventDistribution().getMinimumTermNumber();
			maxTerms=e.getEventDistribution().getMaximumTermNumber();
			for (int i=0;i<maxTerms;i++) termNames.add("T"+(i+1));
		}
		if (e.getPreconditionEvents().size()>0) type=3;
		else type=1;
		people=e.getDefinition().getIndividuals();
		if (people.size()==0) people=plan.getDefinition().getIndividuals();
		location=e.getDefinition().getLocationParameters();
		if (location.size()==0) location=plan.getDefinition().getLocationParameters();
		time = e.getDefinition().getTimeParameters();
		if (time.size()==0) time=plan.getDefinition().getTimeParameters();
	}

	@Override
	public String toString() {
		return "\n MojEvent-ispis \n |duration=" + duration + "\n |id=" + id + "\n |location="
				+ location + "\n |maxTerms=" + maxTerms + "\n |minTerms="
				+ minTerms + "\n |name=" + name + "\n |people=" + people
				+ "\n |preconditions=" + preconditions + "\n |termNames="
				+ termNames + "\n |time=" + time + "\n |type=" + type;
	}
}