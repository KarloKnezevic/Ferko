package hr.fer.zemris.util.scheduling.support;

import hr.fer.zemris.jcms.model.planning.Definition;
import hr.fer.zemris.jcms.model.planning.Plan;
import hr.fer.zemris.jcms.model.planning.PlanEvent;
import hr.fer.zemris.jcms.model.planning.PlanEventSegment;
import hr.fer.zemris.util.scheduling.support.algorithmview.IEvent;
import hr.fer.zemris.util.scheduling.support.algorithmview.IPlan;
import hr.fer.zemris.util.scheduling.support.algorithmview.ITerm;
import hr.fer.zemris.util.time.DateStamp;
import hr.fer.zemris.util.time.TimeStamp;


public class SchedulingResult implements ISchedulingResult{

	private Plan plan;

	public SchedulingResult(){
		this.plan = new Plan();
	}
	
	public void addEvent(String eventName, String id){
		this.plan.addPlanEvent(eventName, id);
	}
	
	public void addPlan(String planName){
		this.plan.setName(planName);
	}
	
	public void addStudentToTerm(String eventName, String termName, String jmbag){
		PlanEventSegment term = this.plan.getEvent(eventName).getSegment(termName);
		term.addParameter(new Definition.PeopleParameter(jmbag, false));
	}
	
	public void addTerm(String eventName, String termName, String roomID, int capacity, String termDate, int startTimeOffset, int endTimeOffset){
	  PlanEvent event = this.plan.getEvent(eventName);
	  PlanEventSegment term = event.addSegment(termName);
	  int eventQuantity=0;
	  if(event.getSegments()!=null) eventQuantity = event.getSegments().size();
	  term.setId(event.getId().substring(1)+"."+(eventQuantity+1));
	  term.addParameter(new Definition.RoomParameter(roomID + "$" + roomID + "$" + Integer.toString(capacity)));
	  DateStamp termDateStamp = new DateStamp(termDate);
	  term.addParameter(new Definition.TimeParameter(termDateStamp, new TimeStamp(startTimeOffset), termDateStamp, new TimeStamp(endTimeOffset)));
	}
	
	public String getResultXML(){
		return this.plan.toXMLString();
	}


	@Override
	public IPlan getPlan() {
		return this.plan;
	}
	
	@Override
	public String toString() {
		String out="";
		for(IEvent e : plan.getEvents()) {
			out += e.getId() + "\n\n";
			for(ITerm t:e.getTerms()) {
				out += t.getName() + "/" + t.getDefinition().getLocationParameters().get(0).getName() + "(" + t.getDefinition().getLocationParameters().get(0).getActualCapacity() + ")/" + t.getDefinition().getTimeParameters().get(0).getFromDate().getStamp() + "/" + t.getDefinition().getTimeParameters().get(0).getFromTime() + "-" + t.getDefinition().getTimeParameters().get(0).getToTime() + "\n";
				for(String s:t.getDefinition().getIndividuals()) {
					out += s + "\n";
				}
				out += t.getDefinition().getIndividuals().size() + "\n\n";
			}
			out += "-----------------------------------------";
			out += "\n\n";
		}
		return out;
	}
}
