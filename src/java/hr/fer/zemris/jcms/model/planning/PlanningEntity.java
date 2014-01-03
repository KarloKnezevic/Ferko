package hr.fer.zemris.jcms.model.planning;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import hr.fer.zemris.jcms.model.planning.Definition;
import hr.fer.zemris.jcms.model.planning.Definition.IDefinitionParameter;
import hr.fer.zemris.jcms.model.planning.Definition.RoomParameter;
import hr.fer.zemris.jcms.model.planning.Definition.TimeParameter;

/**
 * Temeljni razred svih entiteta koji se koriste u planiranju: plan, dogadaj, segment dogadaja (termin).
 * Koristi se kod izravnog pristupa entitetu te se ne mora pretrazivati stablo plana.
 */
public abstract class PlanningEntity {
	
	//XML node names
	public static String EVENT_NODE_NAME = "event";
	public static String DISTRIBUTION_NODE_NAME = "distribution";
	public static String PRECONDITIONS_NODE_NAME = "preconditions";
	public static String PRECONDITION_ELEMENT_NODE_NAME = "precondition";
	public static String TERM_NODE_NAME = "term";
	public static String DEFINITION_NODE_NAME = "def";
	public static String PEOPLEDEF_NODE_NAME = "people";
	public static String TIMEDEF_NODE_NAME = "time";
	public static String LOCATIONDEF_NODE_NAME = "rooms";

	/**
	 * Naziv entiteta
	 */
	private String name;
	/**
	 * Definicija parametara entiteta
	 */
	private Definition definition;
	/**
	 * Dozvole za dodavanje parametara
	 */
	private Map<Integer, Integer> parameterAddPermission;
		
	public PlanningEntity(){
		setParameterAddPermission(new HashMap<Integer, Integer>());
		getParameterAddPermission().put(Definition.PEOPLE_DEF, 0);
		getParameterAddPermission().put(Definition.TIME_DEF, 0);
		getParameterAddPermission().put(Definition.LOCATION_DEF, 0);
		setDefinition(new Definition(this));
	}
	
	/**
	 * Ima li entitet definiranih parametara zadanog tipa?
	 * @param paramType
	 * @return
	 */
	public boolean hasDefinedParameters(int paramType){
		if(getDefinition().getDefMap().get(paramType).size()==0) return false;
		else return true;
	}
	
	/**
	 * Dodavanje parametara zadanog tipa
	 * @param paramType
	 * @param params
	 */
	public void addParameter(IDefinitionParameter param){
		getDefinition().addParameter(param); 
	}
	
	/**
	 * Brisanje parametara zadanog tipa
	 * @param paramType
	 * @param params
	 */
	public void removeParameter(IDefinitionParameter param){
		getDefinition().removeParameter(param);
	}
	
	/**
	 * Brise sve parametre zadanog tipa
	 * @param paramType
	 */
	public void removeAllParameters(int paramType){
		getDefinition().removeAllParameters(paramType);
	}
	
	/**
	 * Dohvat parametara zadanog tipa
	 * @param paramType
	 * @return
	 */
	public List<IDefinitionParameter> getParameters(int paramType){
		return getDefinition().getDefMap().get(paramType);
	}
	
	public List<DateSpan> getTimeParameters(){
		List<IDefinitionParameter> paramList = getParameters(Definition.TIME_DEF);
		List<DateSpan> spanList = new ArrayList<DateSpan>();
		for(IDefinitionParameter period : paramList){
			TimeParameter tp = (TimeParameter)period;
			DateSpan span = new DateSpan(tp.getFromDateTimeStamp(), tp.getToDateTimeStamp());
			spanList.add(span);
		}
		return spanList;
	}
	
	public List<RoomParameter> getRoomParameters(){
		List<IDefinitionParameter> paramList = getParameters(Definition.LOCATION_DEF);
		List<RoomParameter> roomList = new ArrayList<RoomParameter>();
		for(IDefinitionParameter room : paramList) roomList.add((RoomParameter)room);
		return roomList;
	}
	
	/**
	 * Validacija entiteta - mora se overrideati u pojedinom entitetu.
	 * Za pohranu rezultata validacije koristi se ValidationResult razred.
	 * @return
	 */
	protected abstract void validate();

	/**
	 * Zaključavanje zadanog parametra u svim granama (u stablu entiteta) 
	 * kojima pripada trenutni entitet
	 * @param paramType
	 */
	public void lockBranches(int paramType){
		if(this instanceof Plan){
			Plan p = (Plan)this;
			for(PlanEvent event : p.getEvents()) {
				event.forbid(paramType);
				for(PlanEventSegment seg : event.getSegments()) seg.forbid(paramType);
			}
		}else if(this instanceof PlanEvent){
			PlanEvent event = (PlanEvent)this;
			event.getParent().forbid(paramType);
			for(PlanEventSegment seg : event.getSegments()) seg.forbid(paramType);
		}else if(this instanceof PlanEventSegment){
			PlanEventSegment seg = (PlanEventSegment)this;
			seg.getParent().forbid(paramType);
			seg.getParent().getParent().forbid(paramType);
		}
	}
	
	/**
	 * Otključavanje zadanog parametra u svim granama (u stablu entiteta) 
	 * kojima pripada trenutni entitet
	 * @param paramType
	 */
	public void unlockBranches(int paramType){
		if(this instanceof Plan){
			Plan p = (Plan)this;
			for(PlanEvent event : p.getEvents()) {
				event.allow(paramType);
				for(PlanEventSegment seg : event.getSegments()) seg.allow(paramType);
			}
		}else if(this instanceof PlanEvent){
			PlanEvent event = (PlanEvent)this;
			event.getParent().allow(paramType);
			for(PlanEventSegment seg : event.getSegments()) seg.allow(paramType);
		}else if(this instanceof PlanEventSegment){
			PlanEventSegment seg = (PlanEventSegment)this;
			seg.getParent().allow(paramType);
			seg.getParent().getParent().allow(paramType);
		}
	}	
	
	/**
	 * Otključavanje trenutnog entiteta
	 * @param paramType
	 */
	public void allow(int paramType){
		int value = getParameterAddPermission().get(paramType);
		if(value<0) getParameterAddPermission().put(paramType, value+1);
	}
	
	/**
	 * Zaključavanje trenutnog entiteta
	 * @param paramType
	 */
	public void forbid(int paramType){
		int value = getParameterAddPermission().get(paramType);
		getParameterAddPermission().put(paramType, value-1);
	}
	
	/**
	 * Smije li se aktivnom entitetu dodati parametar zadanog tipa
	 * @param paramType
	 * @return
	 */
	public boolean isAllowedToAddParameter(int paramType){
		int value = getParameterAddPermission().get(paramType);
		if(value<0) return false;
		else return true;
	}
	
	/**
	 * Uklanjanje vlastitih definicija i lockova prije brisanja
	 */
	protected void removeSelf(){
		for(int i : Definition.paramTypes){
			if(hasDefinedParameters(i)) unlockBranches(i);
		}
		setDefinition(null);
	};
	
	/**
	 * {@inheritDoc}
	 * @param r1
	 * @return
	 */
	public boolean containsRoom(RoomParameter r1) {
		return getDefinition().containsRoom(r1);
	}
	
	/**
	 *  {@inheritDoc}
	 * @return
	 */
	public Integer getSelectedActivityType(){
		return getDefinition().getSelectedActivityType();
	}
	
	/**
	 *  {@inheritDoc}
	 * @return
	 */
	public void setSelectedActivityType(Integer selectedActivityType) {
		getDefinition().setSelectedActivityType(selectedActivityType);
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name=name;
	}

	public Definition getDefinition() {
		return definition;
	}

	protected void setDefinition(Definition definition) {
		this.definition = definition;
	}

	private Map<Integer, Integer> getParameterAddPermission() {
		return parameterAddPermission;
	}

	private void setParameterAddPermission(
			Map<Integer, Integer> parameterAddPermission) {
		this.parameterAddPermission = parameterAddPermission;
	}
	
	public void printDefinitionSummary(){
		System.out.println(this.definitionSummaryToString());
		if(this instanceof Plan){
			Plan p = (Plan)this;
			for(PlanEvent pe : p.getEvents()) pe.printDefinitionSummary();
		}else if(this instanceof PlanEvent){
			PlanEvent pe = (PlanEvent)this;
			for(PlanEventSegment pes : pe.getSegments()) pes.printDefinitionSummary();
		}
	}
	
	private String definitionSummaryToString(){
		String result = this.getName() + "      ";
		for(int i=0; i< Definition.paramTypes.length ; i++){
			List<IDefinitionParameter> params = this.getParameters(i);
			int size = 0;
			if(params != null) size = params.size();
			result+= (String)Definition.defTypeToName.get(i) + " " + size + "   ";
		}
		return result;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
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
		PlanningEntity other = (PlanningEntity) obj;
		
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		
		return true;
	}

	public class DateSpan {
		Date startDate;
		Date endDate;
		SimpleDateFormat sdf = new SimpleDateFormat(Definition.DATE_FORMAT);
		
		public DateSpan (String startDate, String endDate){
			
			try{
				this.startDate = sdf.parse(startDate);
				this.endDate = sdf.parse(endDate);
			}catch(ParseException ignored){}
		}
		
		public Date getStartDate() {
			return startDate;
		}
		public void setStartDate(Date startDate) {
			this.startDate = startDate;
		}
		public Date getEndDate() {
			return endDate;
		}
		public void setEndDate(Date endDate) {
			this.endDate = endDate;
		}
		
		public String toString(){
			return sdf.format(startDate) + "  " + sdf.format(endDate);
		}
		
	}
	

	
}
