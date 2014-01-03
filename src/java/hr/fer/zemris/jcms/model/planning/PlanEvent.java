package hr.fer.zemris.jcms.model.planning;

import hr.fer.zemris.jcms.model.planning.PlanEventSegment;
import hr.fer.zemris.jcms.model.planning.Definition.IDefinitionParameter;
import hr.fer.zemris.jcms.model.planning.Definition.TimeParameter;
import hr.fer.zemris.util.scheduling.support.IScheduleEvent;
import hr.fer.zemris.util.scheduling.support.IScheduleTerm;
import hr.fer.zemris.util.scheduling.support.algorithmview.IEvent;
import hr.fer.zemris.util.scheduling.support.algorithmview.IEventDistribution;
import hr.fer.zemris.util.scheduling.support.algorithmview.IPrecondition;
import hr.fer.zemris.util.scheduling.support.algorithmview.ITerm;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Događaj u rasporedu
 */ 
public class PlanEvent extends PlanningEntity implements IEvent, IScheduleEvent, Serializable{
	
	private static final long serialVersionUID = 5328789742417829700L;
	private String id = null;
	private Plan parent;
	private List<PlanEventSegment> segments;
	
	private Set<Precondition> preconditions = new HashSet<Precondition>();
	private int maximumDuration = -1;
	private EventDistribution distribution = new EventDistribution(Definition.RANDOM_DISTRIBUTION, 0, 0);
	private int termDuration = 15;
	
	//Koristi se kod objave rasporeda
	private String publicationGroupType;
	private String componentDescriptorId;
	private String eventComponentNumber;
	private String privateGroupName;
	
	public PlanEvent(Plan parent, String name, String id){
		super();
		this.setParent(parent);
		setName(name);
		if(id!=null && id.length()>0) setId(id);
		this.segments = new ArrayList<PlanEventSegment>();
	}
	
	/**
	 * Konstruktor za rekonstrukciju iz XML zapisa
	 * @param xmlPlanData
	 */
	public PlanEvent(Node eventNode, Plan parent){
		this(parent, "", null);
		Element eventElement = (Element)eventNode;
		setName(eventElement.getAttribute("name"));
		setId(eventElement.getAttribute("id"));
		if(eventElement.hasAttribute("componentDescriptor")) setComponentDescriptorId(eventElement.getAttribute("componentDescriptor"));
		if(eventElement.hasAttribute("eventComponentNumber")) setEventComponentNumber(eventElement.getAttribute("eventComponentNumber"));
		if(eventElement.hasAttribute("publicationGroupType")) setPublicationGroupType(eventElement.getAttribute("publicationGroupType"));
		if(eventElement.hasAttribute("privateGroupName")) setPrivateGroupName(eventElement.getAttribute("privateGroupName"));
		try{
			setMaximumDuration(Integer.parseInt(eventElement.getAttribute("maxDuration")));
		}catch(NumberFormatException ignored){}
	
		try{
			setTermDuration(Integer.parseInt(eventElement.getAttribute("termDuration")));
		}catch(NumberFormatException ignored){}
		
		NodeList childNodes = eventNode.getChildNodes();
		for(int i = 0; i<childNodes.getLength(); i++){
			Node node = childNodes.item(i);
			if(node.getNodeName().equals(PlanningEntity.DEFINITION_NODE_NAME)){
				setDefinition(new Definition(node, this));
			}else if(node.getNodeName().equals(PlanningEntity.DISTRIBUTION_NODE_NAME)){
				setDistribution(new EventDistribution(node));
			}else if(node.getNodeName().equals(PlanningEntity.TERM_NODE_NAME)){
				getSegments().add(new PlanEventSegment(node, this));
			}else if(node.getNodeName().equals(PlanningEntity.PRECONDITIONS_NODE_NAME)){
				NodeList childPreconditions = node.getChildNodes();
				for(int j=0; j<childPreconditions.getLength(); j++){
					Element precondition = (Element)childPreconditions.item(j);
					String eventName = precondition.getAttribute("eventName");
					String timeDistance = precondition.getAttribute("timeDistance");
					Precondition p = new Precondition(eventName, timeDistance, this);
					getPreconditions().add(p);
				}
			}
		}		
	}
	
	/**
	 * Validacija događaja
	 * 
	 * Ako je raspodjela slučajna
	 * 	-(1)ne smije biti segmenata
	 *  -(2)mora biti zadan min. i max. broj termina
	 *  -(2a) max >= min
	 *  -(3)parametar koji nije definiran ovdje, mora biti definiran u roditelju/planu
	 * 
	 * Ako je raspodjela zadana
	 *  -parametar koji nije definiran u događaju mora biti definiran
	 * 
	 * 
	 * Provjera 1: Postoji li u svim granama definicija za svaki parametar?
	 * Provjera 2: Jesu li ispravno zadane raspodjele studenata po grupama/terminima?
	 */
	public void validate(){
		ValidationResult.addContextMessage("<b>" + getName() + "</b>");
		if(getDistribution().getType()==Definition.RANDOM_DISTRIBUTION){
			//(1)
			if(getSegments().size()>0) ValidationResult.addMessage("Kod slučajne raspodjele studenata ne mogu biti zadani termini.");
			
			//(2)
			try{
				if(getDistribution().getMinimumTermNumber()<=0) 
					ValidationResult.addMessage("Minimalni dozvoljeni broj termina mora biti pozitivan broj.");
			}catch(NumberFormatException nfe){
				ValidationResult.addMessage("Minimalni dozvoljeni broj termina mora biti pozitivan broj.");
			}

			try{
				if(getDistribution().getMaximumTermNumber()<=0) 
					ValidationResult.addMessage("Maksimalni dozvoljeni broj termina mora biti pozitivan broj.");
			}catch(NumberFormatException nfe){
				ValidationResult.addMessage("Maksimalni dozvoljeni broj termina mora biti pozitivan broj.");
			}
			
			//(2a)
			if(getDistribution().getMinimumTermNumber() > getDistribution().getMaximumTermNumber()){
				ValidationResult.addMessage("Minimalni broj termina mora biti manji ili jednak maksimalnom broju termina.");
			}
			
			//(3)
			for(int param : Definition.paramTypes){
				if(!hasDefinedParameters(param) && !getParent().hasDefinedParameters(param))
					ValidationResult.addMessage("Nije zadan parametar: " + Definition.defTypeToName.get(param));
			}
		}
		
		getDefinition().validate();
		for(PlanEventSegment pes : getSegments()) pes.validate();
	}
	
	/**
	 * Dodaje novi segment događaja
	 * @return Novi segment
	 */
	public PlanEventSegment addSegment(String segmentName){
		PlanEventSegment seg = new PlanEventSegment(segmentName, this);
		if(segmentName==null) seg.setName(prepareNewTermName());
		
		this.segments.add(seg);
		
		//Update dozvola
		for(int i : Definition.paramTypes) {
			if(hasDefinedParameters(i) || getParent().hasDefinedParameters(i)) seg.forbid(i);
		}
		
		return seg;
	}
	
	private String prepareNewTermName(){
		int startNumber = 1;
		String nameProposal = "Termin" + startNumber;
		Set<String> tmp = new HashSet<String>();
		for(PlanEventSegment pes : this.segments) tmp.add(pes.getName());
		while(tmp.contains(nameProposal)){
			startNumber++;
			nameProposal = "Termin" + startNumber;
		}
		return nameProposal;
	}
	
	/**
	 * Dohvaca termin po imenu.
	 * @param segmentName
	 * @return
	 */
	public PlanEventSegment getSegment(String segmentName){
		for(PlanEventSegment pes : segments) {
			if (pes.getName().equals(segmentName)) return pes;
		}
		return null;
	}
	
	/**
	 * Uklanja segment iz događaja
	 * @param selectedSegmentName
	 */
	public void removeSegment(String selectedSegmentName) {
		Iterator<PlanEventSegment> ipe = segments.iterator();
		while(ipe.hasNext()){
			PlanEventSegment seg = ipe.next();
			if(seg.getName().equals(selectedSegmentName)) {
				seg.removeSelf();
				ipe.remove();
				break;
			}
		}
		
	}
	
	/**
	 * Dohvaća nazive događaja.
	 * Koristi se kod aktivacije pojedinog događaja.
	 * @return
	 */
	public List<String> getSegmentNames(){
		List<String> segNames = new ArrayList<String>();
		for(PlanEventSegment seg : this.segments) segNames.add(seg.getName());
		return segNames;
	}

	@Override
	public List<IScheduleTerm> getScheduleEventTerms() {
		List<IScheduleTerm> terms = new ArrayList<IScheduleTerm>();
		for(PlanEventSegment pes : segments) terms.add(pes);
		return terms;
	}
	
	@Override
	protected void removeSelf() {
		for(PlanEventSegment seg : getSegments()) seg.removeSelf();
		super.removeSelf();
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("\nEVENT:"); 
		sb.append(getName());
		sb.append("\n"+isAllowedToAddParameter(Definition.PEOPLE_DEF)
				+"-"+isAllowedToAddParameter(Definition.TIME_DEF)
				+"-"+isAllowedToAddParameter(Definition.LOCATION_DEF));
		sb.append(getDefinition().toString());
		if(getMaximumDuration()!=-1) sb.append("\nmaxDuration:"+getMaximumDuration());
		sb.append("\ndistribution:"+getDistribution().getType());
		if(getDistribution().getType()==Definition.RANDOM_DISTRIBUTION) 
			sb.append("#"+getDistribution().getMinimumTermNumber()+"#"+getDistribution().getMaximumTermNumber());
		for(PlanEventSegment pe : segments) sb.append(pe.toString());
		return sb.toString();
	}

	protected void toXML(StringBuilder sb){
		sb.append("<event name=\""+ getName() +"\" ");
			sb.append("id=\""+getId()+"\" ");
			sb.append("termDuration=\"" +getTermDuration()+"\" ");
			if(getMaximumDuration()!=-1) sb.append("maxDuration=\""+getMaximumDuration()+"\" ");
			if(getComponentDescriptorId()!=null) sb.append("componentDescriptor=\""+this.componentDescriptorId+"\" ");
			if(getEventComponentNumber()!=null) sb.append("eventComponentNumber=\""+this.eventComponentNumber+"\" ");
			if(getPublicationGroupType()!=null) sb.append("publicationGroupType=\""+this.publicationGroupType+"\" ");
			if(getPrivateGroupName()!=null) sb.append("privateGroupName=\""+this.privateGroupName+"\" ");
		sb.append(">");
			getDistribution().toXML(sb);
			if(getPreconditions().size()>0){
				sb.append("<" + PlanningEntity.PRECONDITIONS_NODE_NAME + ">");
				for(Precondition p : getPreconditions()){
					p.toXML(sb);
				}
				sb.append("</" + PlanningEntity.PRECONDITIONS_NODE_NAME + ">");
			}
			getDefinition().toXML(sb);
			for(PlanEventSegment seg : segments) seg.toXML(sb);
		sb.append("</event>");
	}

	public void setMaximumDuration(int maximumDuration) {
		this.maximumDuration = maximumDuration;
	}

	public int getMaximumDuration() {
		return maximumDuration;
	}

	public EventDistribution getDistribution() {
		return distribution;
	}
	
	public IEventDistribution getEventDistribution() {
		return distribution;
	}

	public void setDistribution(EventDistribution distribution) {
		this.distribution = distribution;
	}

	public void setParent(Plan parent) {
		this.parent = parent;
	}

	public Plan getParent() {
		return parent;
	}

	public List<PlanEventSegment> getSegments() {
		return segments;
	}

	/**
	 * @param termDuration the termDuration to set
	 */
	public void setTermDuration(int termDuration) {
		this.termDuration = termDuration;
	}

	/**
	 * @return the termDuration
	 */
	public int getTermDuration() {
		return termDuration;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Override
	public String getComponentDescriptorId() {
		return this.componentDescriptorId;
	}

	@Override
	public String getEventComponentNumber() {
		return this.eventComponentNumber;
	}

	@Override
	public void setComponentDescriptorId(String id) {
		this.componentDescriptorId=id;
	}

	@Override
	public void setEventComponentNumber(String number) {
		this.eventComponentNumber=number;
	}
	
	@Override
	public String getPublicationGroupType() {
		return this.publicationGroupType;
	}

	@Override
	public void setPublicationGroupType(String type) {
		this.publicationGroupType=type;
	}

	
	public String getPrivateGroupName() {
		return privateGroupName;
	}

	public void setPrivateGroupName(String privateGroupName) {
		this.privateGroupName = privateGroupName;
	}

	@Override
	public IScheduleTerm getTermForId(String termId) {
		for(PlanEventSegment pes : getSegments()) if(pes.getId().equals(termId)) return pes;
		return null;
	}

	
	public Set<Precondition> getPreconditions() {
		return preconditions;
	}

	public void setPreconditions(Set<Precondition> preconditions) {
		this.preconditions = preconditions;
	}

	@Override
	public Date getEventStart() {
		Collections.sort(getSegments());
		PlanEventSegment beginningSegment = getSegments().get(0);
		List<IDefinitionParameter> params = beginningSegment.getDefinition().getParameters(Definition.TIME_DEF);
		TimeParameter timeParam = (TimeParameter)params.get(0);
		SimpleDateFormat sdf = new SimpleDateFormat(Definition.DATE_FORMAT);
		Date result = null;
		try {
			result =  sdf.parse(timeParam.getFromDateTimeStamp());
		} catch (ParseException ignored) {}
		return result;
	}
	
	public void changeEventDistributionType(int distributionType){
		getDistribution().setType(distributionType);
		//Ako se postavlja random raspodjela - brisanje termina ako postoje
		if(distributionType==Definition.RANDOM_DISTRIBUTION){
			this.segments.clear();

		}
	}

	public void addPrecondition(PlanEvent event, String timeDistance){
		this.getPreconditions().add(new Precondition(event.getName(), timeDistance, this));
	}
	
	public boolean preconditionExists(String planEventName){
		for(Precondition p : this.getPreconditions()){
			if(p.getEventName().equals(planEventName)) return true;
		}
		return false;
	}
	
	@Override
	public Set<IPrecondition> getPreconditionEvents() {
		Set<IPrecondition> result = new HashSet<IPrecondition>();
		for(Precondition p : this.preconditions){
			result.add(p);
		}
		return result;
	}

	public List<ITerm> getTerms(){
		List<ITerm> results = new ArrayList<ITerm>();
		for(PlanEventSegment pes : getSegments()) results.add((ITerm)pes);
		return results;
	}
	
	public class EventDistribution implements IEventDistribution{
		int type;
		int minimumTermNumber;
		int maximumTermNumber;
		
		public EventDistribution(Node distributionNode){
			Node distribTypeNode = distributionNode.getFirstChild();
			String typeName = distribTypeNode.getTextContent();
			if(typeName.equals(Definition.GIVEN_DISTRIBUTION_NAME)) this.setType(Definition.GIVEN_DISTRIBUTION);
			else this.setType(Definition.RANDOM_DISTRIBUTION);
			
			if(type==Definition.RANDOM_DISTRIBUTION){
				Node minNode=distribTypeNode.getNextSibling();
				Node maxNode = minNode.getNextSibling();
				int min = Integer.parseInt(minNode.getTextContent());
				int max = Integer.parseInt(maxNode.getTextContent());		
				this.setMinimumTermNumber(min);
				this.setMaximumTermNumber(max);
			}
		}
		
		public EventDistribution(int type){
			this.type=type;
		}
		
		public EventDistribution(int type, int min, int max){
			this.type=type;
			this.minimumTermNumber=min;
			this.maximumTermNumber=max;
		}

		public int getType() {
			return type;
		}

		public String getTypeName(){
			if(getType()==Definition.RANDOM_DISTRIBUTION) return Definition.RANDOM_DISTRIBUTION_NAME;
			else return Definition.GIVEN_DISTRIBUTION_NAME;
		}
		
		public int getMinimumTermNumber() {
			return minimumTermNumber;
		}

		public int getMaximumTermNumber() {
			return maximumTermNumber;
		}

		public void setType(int type) {
			this.type = type;
		}

		public void setMinimumTermNumber(int minimumTermNumber) {
			this.minimumTermNumber = minimumTermNumber;
		}

		public void setMaximumTermNumber(int maximumTermNumber) {
			this.maximumTermNumber = maximumTermNumber;
		}
		
		public void toXML(StringBuilder sb){
			sb.append("<" + PlanningEntity.DISTRIBUTION_NODE_NAME + ">");
			sb.append("<type>"+getDistribution().getTypeName()+"</type>");
					if(getDistribution().getType()==Definition.RANDOM_DISTRIBUTION) {
						sb.append("<min>" + getDistribution().getMinimumTermNumber()+ "</min><max>"+ getDistribution().getMaximumTermNumber()+ "</max>");
					}
		    sb.append("</" + PlanningEntity.DISTRIBUTION_NODE_NAME + ">");
		}
	}
	 
	public static class Precondition implements IPrecondition{
		
		private String eventName;
		private String timeDistance;
		private PlanEvent parent;
		
		public Precondition(String eventName, String timeDistance, PlanEvent parent){
			this.eventName = eventName;
			this.timeDistance = timeDistance;
			this.parent = parent;
		}

		public String getEventName() {
			return eventName;
		}

		public void setEventName(String eventName) {
			this.eventName = eventName;
		}

		public String getTimeDistance() {
			return timeDistance;
		}

		public void setTimeDistance(String timeDistance) {
			this.timeDistance = timeDistance;
		}

		public int getTimeDistanceValue(){
			return Integer.parseInt(getTimeDistance().substring(0, getTimeDistance().length()-1));
		}
		
		public void toXML(StringBuilder sb){
			sb.append("<" + PlanningEntity.PRECONDITION_ELEMENT_NODE_NAME + " eventName=\""+ eventName +"\" timeDistance=\"" + timeDistance + "\" />");	
		}

		public String toString(){
			return eventName + " " + timeDistance;
		}
		
		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((eventName == null) ? 0 : eventName.hashCode());
			result = prime * result + ((timeDistance == null) ? 0 : timeDistance.hashCode());
			return result;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Precondition other = (Precondition) obj;
			
			if (eventName == null) {
				if (other.eventName != null)
					return false;
			} else if (!eventName.equals(other.eventName))
				return false;
			if (timeDistance == null) {
				if (other.timeDistance != null)
					return false;
			} else if (!timeDistance.equals(other.timeDistance))
				return false;
			
			return true;
		}

		@Override
		public IEvent getEvent() {
			return this.parent.getParent().getEvent(eventName);
		}
		
	}



	
}
