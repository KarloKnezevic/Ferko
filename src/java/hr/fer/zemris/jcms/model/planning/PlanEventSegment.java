package hr.fer.zemris.jcms.model.planning;

import hr.fer.zemris.jcms.model.planning.Definition.IDefinitionParameter;
import hr.fer.zemris.jcms.model.planning.Definition.PeopleParameter;
import hr.fer.zemris.jcms.model.planning.Definition.RoomParameter;
import hr.fer.zemris.jcms.model.planning.Definition.TimeParameter;
import hr.fer.zemris.util.scheduling.support.IScheduleTerm;
import hr.fer.zemris.util.scheduling.support.RoomData;
import hr.fer.zemris.util.scheduling.support.algorithmview.ITerm;
import hr.fer.zemris.util.time.DateStamp;
import hr.fer.zemris.util.time.TimeSpan;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;


/**
 * Segment dogadaja u rasporedu (termin)
 */
public class PlanEventSegment extends PlanningEntity implements IScheduleTerm, ITerm, Comparable<PlanEventSegment>, Serializable {
	
	private static final long serialVersionUID = -7251465277906299925L;
	private PlanEvent parent;
	private String id;
	private String overridenEventName;
	
	public PlanEventSegment(String name, PlanEvent parent){
		super();
		this.setParent(parent);
		this.setName(name);
	}
	
	/**
	 * Konstruktor za rekonstrukciju iz XML zapisa na strani usluge
	 * @param xmlPlanData
	 */
	public PlanEventSegment(Node termNode, PlanEvent parent){
		this.setParent(parent);
		Element termElement = (Element)termNode;
		setName(termElement.getAttribute("name"));
		setId(termElement.getAttribute("id"));
		setOverridenEventName(termElement.getAttribute("overridenEventName"));
		Node defNode = termNode.getFirstChild();
		if(defNode!=null) setDefinition(new Definition(defNode, this));
	}
	
	
	/**
	 * Validacija segmenta
	 */
	public void validate() {
		ValidationResult.addContextMessage("<b>" + getName() + "</b>");
		getDefinition().validate();
	}

	@Override
	public int getSerialNumber() {
		if(getId()!=null && getId().length()>0){
			String[] id = getId().split("\\.");
			return Integer.parseInt(id[1]);
		}
		return -1;
	}

	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("\nSEGMENT: ");
		sb.append(getName());
		sb.append("\n"+isAllowedToAddParameter(Definition.PEOPLE_DEF)
				+"-"+isAllowedToAddParameter(Definition.TIME_DEF)
				+"-"+isAllowedToAddParameter(Definition.LOCATION_DEF));
		sb.append(getDefinition().toString());
		return sb.toString();
	}
 
	protected void toXML(StringBuilder sb){
		sb.append("<term name=\""+ getName() +"\" id=\""+getId()+"\" overridenEventName=\""+getOverridenEventName()+"\">");
			getDefinition().toXML(sb);
		sb.append("</term>");
	}
	
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setParent(PlanEvent parent) {
		this.parent = parent;
	}

	public PlanEvent getParent() {
		return parent;
	}
	
	public List<String> getStudents(){
		List<IDefinitionParameter> students = getDefinition().getParameters(Definition.PEOPLE_DEF);
		List<String> jmbags = new ArrayList<String>();
		for(IDefinitionParameter idp : students){
			PeopleParameter pp = (PeopleParameter)idp;
			jmbags.add(pp.getJmbag());
		}
		return jmbags;
	}
	
	public String getTermName(){
		return getName();
	}
	
	
	public RoomData getRoom(){
		List<IDefinitionParameter> rooms = getDefinition().getParameters(Definition.LOCATION_DEF);
		RoomParameter rp=null;
		if(rooms.size()>0) rp = (RoomParameter)rooms.get(0);
		return new RoomData(rp.getId(), rp.getName(), rp.getActualCapacity());
	}
	
	public DateStamp getDate(){
		List<IDefinitionParameter> periods = getDefinition().getParameters(Definition.TIME_DEF);
		TimeParameter tp =null;
		//U momentu dohvacanja ovog mora biti samo jedan period jer je raspored izraden
		if(periods.size()>0) {
			tp = (TimeParameter)periods.get(0);
			return tp.getFromDate();
		}
		else return null;
	}
	
	public TimeSpan getTermSpan(){
		List<IDefinitionParameter> periods = getDefinition().getParameters(Definition.TIME_DEF);
		TimeParameter tp =null;
		//U momentu dohvacanja ovog mora biti samo jedan period jer je raspored izraden
		if(periods.size()>0) {
			tp = (TimeParameter)periods.get(0);
			return new TimeSpan(tp.getFromTime(), tp.getToTime());
		}
		else return null;
	}

	@Override
	public String getOverridenEventName() {
		return this.overridenEventName;
	}

	@Override
	public void setOverridenEventName(String eventName) {
		this.overridenEventName=eventName;		
	}

	@Override
	public void overrideTermName(String newTermName) {
		this.setName(newTermName);
	}

	@Override
	public String getEndDateTime() {
		return getDate().toString()+" "+getTermSpan().getEnd().toString();
	}

	@Override
	public String getStartDateTime() {
		return getDate().toString()+" "+getTermSpan().getStart().toString();
	}
	
	@Override
	public Date getStart(){
		SimpleDateFormat sdf = new SimpleDateFormat(Definition.DATE_FORMAT);
		Date result = null;
		try {
			result = sdf.parse(getStartDateTime());
		} catch (ParseException ignored) {}
		return result;
	}

	@Override
	public int compareTo(PlanEventSegment o) {
		List<IDefinitionParameter> params = this.getDefinition().getParameters(Definition.TIME_DEF);
		List<IDefinitionParameter> params2 = o.getDefinition().getParameters(Definition.TIME_DEF);
		if(params==null || params.size()!=1) return 0;
		if(params2==null || params2.size()!=1) return 0;
		TimeParameter param1 = (TimeParameter)params.get(0);
		TimeParameter param2 = (TimeParameter)params2.get(0);
		return param1.compareTo(param2);
	}



}
