package hr.fer.zemris.jcms.model.planning;

import hr.fer.zemris.jcms.exceptions.IllegalParameterException;
import hr.fer.zemris.util.scheduling.support.ISchedule;
import hr.fer.zemris.util.scheduling.support.IScheduleEvent;
import hr.fer.zemris.util.scheduling.support.algorithmview.IEvent;
import hr.fer.zemris.util.scheduling.support.algorithmview.IPlan;

import java.io.Serializable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

/** 
 * Konfiguracija rasporeda
 * @author IvanFer
 */
public class Plan extends PlanningEntity implements IPlan, ISchedule, Serializable{

	private static final long serialVersionUID = -4869478920700550045L;

	private List<PlanEvent> events;
	
	private int termNumberInEachEvent = -1;
	private boolean equalStudentDistributionInEachEvent = false;
	private boolean equalTermSequenceInEachEvent = false;
	
	//Odgovara ID-u pohranjenog plana u bazi. 
	//Primarna namjena: veza plan-raspored
	private String id = null;
	
	public Plan(){
		super();
		this.events = new ArrayList<PlanEvent>();
		setName("Moj raspored");
	}
	
	/**
	 * Konstruktor za rekonstrukciju iz XML zapisa
	 * @param xmlPlanData
	 */
	public Plan(Node planNode){
		this();
		Element planElement = (Element)planNode;
		setName(planElement.getAttribute("name"));
		if(planElement.hasAttribute("id")) setId(planElement.getAttribute("id"));
		if(!planElement.getAttribute("termNumberInEachEvent").isEmpty()) {
			setTermNumberInEachEvent(Integer.parseInt(planElement.getAttribute("termNumberInEachEvent")));
		}
		setEqualStudentDistributionInEachEvent(Boolean.parseBoolean(planElement.getAttribute("equalStudentDistributionInEachEvent")));
		setEqualTermSequenceInEachEvent(Boolean.parseBoolean(planElement.getAttribute("equalTermSequenceInEachEvent")));
		NodeList planNodeChildren = planNode.getChildNodes();
		for(int i=0; i<planNodeChildren.getLength(); i++){
			Node node = planNodeChildren.item(i);
			if(node.getNodeName().equals(PlanningEntity.DEFINITION_NODE_NAME)) {
				setDefinition(new Definition(node, this));
			}else if(node.getNodeName().equals(PlanningEntity.EVENT_NODE_NAME)){
				getEvents().add(new PlanEvent(node, this));
			}
		}
	}
	
	public PlanEvent addPlanEvent(String name, String id){
		//Provjera ima li već događaja takvog naziva
		for(PlanEvent pe : getEvents()) if(pe.getName().equals(name)) 
			throw new IllegalParameterException("Događaj tog naziva već postoji!");
		
		//Usporedba s nazivom plana
		if(getName().equals(name)){
			throw new IllegalParameterException("Događaj ne može imati naziv jednak planu!");
		}
		
		PlanEvent pe = new PlanEvent(this, name, id);
		events.add(pe);
		
		//Update dozvola
		for(int i : Definition.paramTypes) if(hasDefinedParameters(i)) pe.forbid(i);
		
		return pe;
	}
	
	public void removePlanEvent(String name){
		//Provjera nalazi li se ovaj događaj u relaciji s nekim drugim
//		for(PlanEvent pe: getEvents()){
//			String event = pe.getFollowingEvent();
//			if(event!=null && event.contains(name)) 
//				throw new IllegalParameterException("Ovaj događaj se ne može " + 
//						"obrisati jer se nalazi u relaciji s događajem " + pe.getName());
//		}
		
		//Uklanjanje događaja
		Iterator<PlanEvent> ipe = events.iterator();
		while(ipe.hasNext()){
			PlanEvent pe = ipe.next();
			if(pe.getName().equals(name)) {
				pe.removeSelf();
				ipe.remove();
				break;
			}
		}
	}
	
	public List<PlanEvent> getEvents() {
		return events;
	}
	
	public PlanEvent getEvent(String eventName){
		for(PlanEvent pe : events) if(pe.getName().equals(eventName)) return pe;
		return null;
	}
	
	public PlanEvent getEventByID(String id){
		for(PlanEvent pe : events) if(pe.getId().equals(id)) return pe;
		return null;
	}
	
	public List<IScheduleEvent> getScheduleEvents(){
		List<IScheduleEvent> eventsData = new ArrayList<IScheduleEvent>();
		for(PlanEvent pe : getEvents()) eventsData.add(pe);
		return eventsData;
	}
	
	public List<IEvent> getPlanEvents(){
		List<IEvent> eventsData = new ArrayList<IEvent>();
		for(PlanEvent pe : getEvents()) eventsData.add(pe);
		return eventsData;
	}

	public void validate(){
		ValidationResult.addContextMessage("<b>" + getName() + "</b>");
		if(getEvents().size() == 0) ValidationResult.addMessage("Plan mora sadržavati barem jedan događaj.");
		getDefinition().validate();
		for(PlanEvent pe : events) pe.validate();
	}
	 
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("\nPLAN: ");
		sb.append(getName());
		sb.append("\n"+isAllowedToAddParameter(Definition.PEOPLE_DEF)
					+"-"+isAllowedToAddParameter(Definition.TIME_DEF)
					+"-"+isAllowedToAddParameter(Definition.LOCATION_DEF));
		sb.append(getDefinition().toString());
		for(PlanEvent pe : events) sb.append(pe.toString());
		return sb.toString();
	}

	public String toXMLString(){
		StringBuilder sb = new StringBuilder();
		this.toXML(sb);
		return sb.toString();
	}
	
	protected void toXML(StringBuilder sb){
		sb.append("<plan name=\""+ getName() + "\" ");
			if(getId()!=null) sb.append("id=\""+getId()+"\" ");
			if(getTermNumberInEachEvent()!=-1) sb.append("termNumberInEachEvent=\"" + getTermNumberInEachEvent() + "\" ");
			sb.append("equalStudentDistributionInEachEvent=\"" + isEqualStudentDistributionInEachEvent() + "\" ");
			sb.append("equalTermSequenceInEachEvent=\"" + isEqualTermSequenceInEachEvent() + "\"");
		sb.append(">");
		getDefinition().toXML(sb);
		for(PlanEvent pe : events) pe.toXML(sb);
		sb.append("</plan>");
	}

	@Override
	public IScheduleEvent getEventForId(String eventId) {
		for(PlanEvent pe : getEvents()) if(pe.getId().equals(eventId)) return pe;
		return null;
	}

	public int getTermNumberInEachEvent() {
		return termNumberInEachEvent;
	}

	public void setTermNumberInEachEvent(int termNumberInEachEvent) {
		this.termNumberInEachEvent = termNumberInEachEvent;
	}

	public boolean isEqualStudentDistributionInEachEvent() {
		return equalStudentDistributionInEachEvent;
	}

	public void setEqualStudentDistributionInEachEvent(
			boolean equalStudentDistributionInEachEvent) {
		this.equalStudentDistributionInEachEvent = equalStudentDistributionInEachEvent;
	}

	public boolean isEqualTermSequenceInEachEvent() {
		return equalTermSequenceInEachEvent;
	}

	public void setEqualTermSequenceInEachEvent(boolean equalTermSequenceInEachEvent) {
		this.equalTermSequenceInEachEvent = equalTermSequenceInEachEvent;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}
	
	
	
	

}


