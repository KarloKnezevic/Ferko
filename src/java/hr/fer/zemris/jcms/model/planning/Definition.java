package hr.fer.zemris.jcms.model.planning;

import hr.fer.zemris.jcms.exceptions.IllegalParameterException;
import hr.fer.zemris.util.scheduling.support.algorithmview.IDefinition;
import hr.fer.zemris.util.scheduling.support.algorithmview.IGroup;
import hr.fer.zemris.util.scheduling.support.algorithmview.ILocationParameter;
import hr.fer.zemris.util.scheduling.support.algorithmview.ITimeParameter;
import hr.fer.zemris.util.time.DateStamp;
import hr.fer.zemris.util.time.TimeStamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Definicija osnovnih parametara
 */
public class Definition implements IDefinition {
	
	public static final int TIME_DEF = 0;
	public static final int PEOPLE_DEF = 1;
	public static final int LOCATION_DEF = 2;
	public static int[] paramTypes = {TIME_DEF,PEOPLE_DEF,LOCATION_DEF};
	public static final String TIME_DEF_NAME = "Vremenski periodi";
	public static final String PEOPLE_DEF_NAME = "Osobe";
	public static final String LOCATION_DEF_NAME = "Lokacije";
	public static final Map<Integer, String> defTypeToName = new HashMap<Integer, String>();
	public static final String DATE_FORMAT = "yyyy-MM-dd HH:mm";
	public static final String DATE_MASK = "????-??-?? ??:??";

	/* Tipovi aktivnosti zbog kapaciteta dvorana */
	public static final int LECTURE = 3;
	public static final int EXERCISE = 4;
	public static final int ASSESSMENT = 5;
	public static Integer[] activityTypes = {LECTURE, EXERCISE, ASSESSMENT};
	private Integer selectedActivityType = Definition.LECTURE;
	
	/* Tipovi raspodjele studenata u dogadajima */
	public static final int RANDOM_DISTRIBUTION = 6;
	public static final int GIVEN_DISTRIBUTION = 7;
	public static final String RANDOM_DISTRIBUTION_NAME = "RANDOM";
	public static final String GIVEN_DISTRIBUTION_NAME = "GIVEN";
	
	//Mapa definicija
	private Map<Integer, List<IDefinitionParameter>> defMap;
	//Entitet vlasnik
	private PlanningEntity owner;
	

	static{
		defTypeToName.put(TIME_DEF, TIME_DEF_NAME);
		defTypeToName.put(PEOPLE_DEF, PEOPLE_DEF_NAME);
		defTypeToName.put(LOCATION_DEF, LOCATION_DEF_NAME);
	}
	
	public Definition(PlanningEntity owner){
		this.owner = owner;
		this.defMap = new HashMap<Integer, List<IDefinitionParameter>>();
		this.defMap.put(PEOPLE_DEF, new ArrayList<IDefinitionParameter>());
		this.defMap.put(TIME_DEF, new ArrayList<IDefinitionParameter>());
		this.defMap.put(LOCATION_DEF, new ArrayList<IDefinitionParameter>());
	}
	
	/**
	 * Konstruktor za rekonstrukciju iz XML zapisa na strani usluge
	 * @param xmlPlanData
	 */
	public Definition(Node definitionNode, PlanningEntity owner){
		this(owner);
		NodeList defList = definitionNode.getChildNodes();
		for(int i=0; i<defList.getLength(); i++){
			Node def = defList.item(i);
			
			List<IDefinitionParameter> defs=null;
			if(def.getNodeName().equals(PlanningEntity.PEOPLEDEF_NODE_NAME)) {
				defs = getDefMap().get(PEOPLE_DEF);
				NodeList peopleParameterNodes = def.getChildNodes();
				for(int j=0; j<peopleParameterNodes.getLength(); j++){
					Node n = peopleParameterNodes.item(j);
					if(n.getNodeName().equals("groups")){
						String[] groups = n.getTextContent().split(",");
						if(defs!=null) for(String s : groups) defs.add(new PeopleParameter(s, true));
					}else if(n.getNodeName().equals("jmbags")){
						String[] jmbags = n.getTextContent().split(",");
						if(defs!=null) for(String s : jmbags) defs.add(new PeopleParameter(s, false));
					}else if(n.getNodeName().equals("teams")){
						for(int k=0; k<n.getChildNodes().getLength(); k++){
							TeamParameter tp = new TeamParameter(n.getChildNodes().item(k).getTextContent());
							if(defs!=null) defs.add(tp);
						}
						
					}
				}
			}
			else if(def.getNodeName().equals(PlanningEntity.TIMEDEF_NODE_NAME)){
				defs = getDefMap().get(TIME_DEF);
				String[] params = def.getTextContent().split(",");
				for(String s : params) defs.add(new TimeParameter(s));
			}
			else if(def.getNodeName().equals(PlanningEntity.LOCATIONDEF_NODE_NAME)) {
				defs = getDefMap().get(LOCATION_DEF);
				String[] params = def.getTextContent().split(",");
				for(String s : params) defs.add(new RoomParameter(s));
			}
			
		}

	}

	/**
	 * Validacija definicije
	 */
	public void validate() {
		List<IDefinitionParameter> locationParameters = getParameters(Definition.LOCATION_DEF);
		for(IDefinitionParameter p : locationParameters){
			RoomParameter rp = (RoomParameter)p;
			if(rp.getActualCapacity() < 1) ValidationResult.addMessage("Prostorija " + rp.getName() + " mora imati zadani kapacitet veći od 0.");
		}
	}

	/**
	 * Vraća definiciju prema parametru.
	 * @param param
	 * @return
	 */
	public List<IDefinitionParameter> getParameters(int paramType){ 
		return getDefMap().get(paramType);
	}
	
	/**
	 * Dodaje parametar
	 * @param paramType
	 * @param param
	 */
	public void addParameter(IDefinitionParameter param) {
		if(param==null) throw new IllegalParameterException("Neispravni parametar!");
		int paramSizeBefore = getDefMap().get(param.getType()).size();
		getDefMap().get(param.getType()).add(param);
		int paramSizeAfter =  getDefMap().get(param.getType()).size();
		if(paramSizeBefore==0 && paramSizeAfter>0) owner.lockBranches(param.getType());
	}
	
	/**
	 * Uklanja zadani parametar iz pripadne liste
	 * @param paramType
	 * @param param
	 */
	public void removeParameter(IDefinitionParameter param) {
		getDefMap().get(param.getType()).remove(param);
		int paramSizeAfter =  getDefMap().get(param.getType()).size();
		if(paramSizeAfter==0) owner.unlockBranches(param.getType());
		
	}	
	
	/**
	 * Brise sve parametre zadanog tipa
	 * @param paramType
	 */
	public void removeAllParameters(int paramType){
		getDefMap().get(paramType).clear();
	}
	
	/**
	 * Wrapper metoda za pretragu podnizova u listi stringova
	 * @param r1
	 * @return
	 */
	public boolean containsRoom(RoomParameter r1) {		
		return getDefMap().get(r1.getType()).contains(r1);
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("\nPEOPLEDEF: ");
		for(IDefinitionParameter param : getDefMap().get(PEOPLE_DEF)) sb.append(param.toString() +" ");
		sb.append("\nTIMEDEF: ");
		for(IDefinitionParameter param: getDefMap().get(TIME_DEF)) sb.append(param.toString() +" ");
		sb.append("\nLOCATIONDEF: ");
		for(IDefinitionParameter param : getDefMap().get(LOCATION_DEF)) sb.append(param.toString() +" ");	
		return sb.toString();
	}
	
	protected void toXML(StringBuilder sb){
		if(owner.hasDefinedParameters(PEOPLE_DEF) || owner.hasDefinedParameters(TIME_DEF) 
				|| owner.hasDefinedParameters(LOCATION_DEF)){
			
			sb.append("<def>");
			
				if(owner.hasDefinedParameters(PEOPLE_DEF)){
					sb.append("<people>");
					
						StringBuilder groupBuilder = new StringBuilder();
						StringBuilder jmbagBuilder = new StringBuilder();
						StringBuilder teamBuilder = new StringBuilder();
						
						for(IDefinitionParameter s : getDefMap().get(PEOPLE_DEF)) {
							if(s instanceof PeopleParameter){
								PeopleParameter pp = (PeopleParameter)s;
								if(pp.isGroup) {
									groupBuilder.append(pp.toXMLString());
									groupBuilder.append(",");
								}
								else {
									jmbagBuilder.append(pp.toXMLString());
									jmbagBuilder.append(",");
								}
							}else if(s instanceof TeamParameter){
								TeamParameter tp = (TeamParameter)s;
								teamBuilder.append(tp.toXMLString());
							}
						}
						if(groupBuilder.length()>0) {
							groupBuilder.deleteCharAt(groupBuilder.length()-1);
							sb.append("<groups>");
							sb.append(groupBuilder.toString());
							sb.append("</groups>");
						}
						if(jmbagBuilder.length()>0){
							jmbagBuilder.deleteCharAt(jmbagBuilder.length()-1);
							sb.append("<jmbags>");
							sb.append(jmbagBuilder.toString());
							sb.append("</jmbags>");
						}
						if(teamBuilder.length()>0){
							sb.append("<teams>");
							sb.append(teamBuilder.toString());
							sb.append("</teams>");
						}
						sb.append("</people>");
				}
				
				if(owner.hasDefinedParameters(TIME_DEF)){
					sb.append("<time>");
						for(IDefinitionParameter s : getDefMap().get(TIME_DEF)) {
							TimeParameter tp = (TimeParameter)s;
							sb.append(tp.toXMLString());
							sb.append(",");
						}
						sb.deleteCharAt(sb.length()-1);
						sb.append("</time>");
				}
				if(owner.hasDefinedParameters(LOCATION_DEF)){
					sb.append("<rooms>");
						for(IDefinitionParameter s : getDefMap().get(LOCATION_DEF)) {
							RoomParameter rp = (RoomParameter)s;
							sb.append(rp.toXMLString());
							sb.append(",");
						}
						sb.deleteCharAt(sb.length()-1);
						sb.append("</rooms>");
				}
			sb.append("</def>");
		}
	}
	

	public Map<Integer, List<IDefinitionParameter>> getDefMap() {
		return defMap;
	}
	
	public void setOwner(PlanningEntity owner) {
		this.owner = owner;
	}

	public PlanningEntity getOwner() {
		return owner;
	}

	public void setSelectedActivityType(Integer selectedActivityType) {
		this.selectedActivityType = selectedActivityType;
	}

	public Integer getSelectedActivityType() {
		return selectedActivityType;
	}

	public List<ITimeParameter> getTimeParameters(){
		List<ITimeParameter> result = new ArrayList<ITimeParameter>();
		List<IDefinitionParameter> paramList = getDefMap().get(Definition.TIME_DEF);
		for(IDefinitionParameter idp : paramList){
			result.add((TimeParameter)idp);
		}
		return result;
	}
	
	public List<ILocationParameter> getLocationParameters(){ 
		List<ILocationParameter> result = new ArrayList<ILocationParameter>();
		List<IDefinitionParameter> paramList = getDefMap().get(Definition.LOCATION_DEF);
		for(IDefinitionParameter idp : paramList){
			result.add((ILocationParameter)idp);
		}
		return result;
	}
	
	public List<IGroup> getGroups(){
		List<IGroup> result = new ArrayList<IGroup>();
		List<IDefinitionParameter> paramList = getDefMap().get(Definition.PEOPLE_DEF);
		for(IDefinitionParameter idp : paramList){
			PeopleParameter pp = (PeopleParameter)idp;
			if(pp.isGroup) result.add(pp);
		}
		return result;
	}

	public List<String> getIndividuals(){
		List<String> result = new ArrayList<String>();
		List<IDefinitionParameter> paramList = getDefMap().get(Definition.PEOPLE_DEF);
		for(IDefinitionParameter idp : paramList){
			PeopleParameter pp = (PeopleParameter)idp;
			if(!pp.isGroup) result.add(pp.getJmbag());
		}
		return result;
	}
	
	/**
	 * Moves all definition parameters for the given type from the currenty entity to the destination entity
	 * @param definitionToMove
	 * @param destinationEntity
	 */
	public void moveDefinitionsToEntity(int parameterType, PlanningEntity destinationEntity){
		copyDefinitionsToEntity(parameterType, destinationEntity);
		removeAllParameters(parameterType);
	}
	
	/**
	 * Copies all definition parameters for the given type from the currenty entity to the destination entity
	 * @param definitionToMove
	 * @param destinationEntity
	 */
	public void copyDefinitionsToEntity(int parameterType, PlanningEntity destinationEntity){
		if(parameterType!=Definition.TIME_DEF && parameterType!=Definition.PEOPLE_DEF && parameterType!=Definition.LOCATION_DEF) return;
		destinationEntity.getParameters(parameterType).addAll(getParameters(parameterType));
	}

	public static class TimeParameter implements IDefinitionParameter, ITimeParameter, Comparable<TimeParameter>{
		
		private DateStamp fromDate;
		private TimeStamp fromTime;
		private DateStamp toDate;
		private TimeStamp toTime;
		
		public TimeParameter(String fromDate, String fromTime, String toDate, String toTime){
			this.fromDate= new DateStamp(fromDate);
			this.fromTime=new TimeStamp(fromTime);
			this.toDate=new DateStamp(toDate);
			this.toTime=new TimeStamp(toTime);
		}

		public TimeParameter(DateStamp fromDate, TimeStamp fromTime, DateStamp toDate, TimeStamp toTime){
			this.fromDate=fromDate;
			this.fromTime=fromTime;
			this.toDate=toDate;
			this.toTime=toTime;
		}
		public TimeParameter(String param){
			String[] elems = param.split("#");
			if(elems.length!=2) throw new IllegalParameterException("Invalid time parameter");
			
			if(elems[0].length()>16 || elems[1].length()>16) 
				throw new IllegalParameterException("Neispravni parametar!");
			
			setFromStamp(elems[0]);
			setToStamp(elems[1]);
			
			if(toDate.compareTo(fromDate)<0) 
				throw new IllegalParameterException("Početak perioda ne može biti nakon završetka!");
			
			if(toDate.compareTo(fromDate)==0 && toTime.compareTo(fromTime)<0)
				throw new IllegalParameterException("Početak perioda ne može biti nakon završetka!");
			
			if(toDate.equals(fromDate) && toTime.equals(fromTime)) 
				throw new IllegalParameterException("Početak perioda ne može jednak završetku!");			
		}
		
		public void setFromStamp(String fromStampString){
			String[] fromStamp = fromStampString.split(" ");
			fromDate = new DateStamp(fromStamp[0]);
			fromTime = new TimeStamp(fromStamp[1]);
		}
		
		public void setToStamp(String toStampString){
			String[] toStamp = toStampString.split(" ");
			toDate = new DateStamp(toStamp[0]);
			toTime = new TimeStamp(toStamp[1]);
		}
		
		public int getType(){
			return Definition.TIME_DEF;
		}
		
		public String getFromDateTimeStamp(){
			return this.fromDate.getStamp()+" "+this.fromTime.toString();
		}
		public String getToDateTimeStamp(){
			return this.toDate.getStamp()+" "+this.toTime.toString();
		}
		
		public DateStamp getFromDate() {
			return fromDate;
		}

		public void setFromDate(DateStamp fromDate) {
			this.fromDate = fromDate;
		}

		public TimeStamp getFromTime() {
			return fromTime;
		}

		public void setFromTime(TimeStamp fromTime) {
			this.fromTime = fromTime;
		}

		public DateStamp getToDate() {
			return toDate;
		}

		public void setToDate(DateStamp toDate) {
			this.toDate = toDate;
		}

		public TimeStamp getToTime() {
			return toTime;
		}

		public void setToTime(TimeStamp toTime) {
			this.toTime = toTime;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((fromDate == null) ? 0 : fromDate.hashCode());
			result = prime * result + ((fromTime == null) ? 0 : fromTime.hashCode());
			result = prime * result + ((toDate == null) ? 0 : toDate.hashCode());
			result = prime * result + ((toTime == null) ? 0 : toTime.hashCode());
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
			TimeParameter other = (TimeParameter) obj;
			
			if (fromDate == null) {
				if (other.fromDate != null)
					return false;
			} else if (!fromDate.equals(other.fromDate))
				return false;
			if (toDate == null) {
				if (other.toDate != null)
					return false;
			} else if (!toDate.equals(other.toDate))
				return false;
			if (fromTime == null) {
				if (other.fromTime != null)
					return false;
			} else if (!fromTime.equals(other.fromTime))
				return false;
			if (toTime == null) {
				if (other.toTime != null)
					return false;
			} else if (!toTime.equals(other.toTime))
				return false;
			
			return true;
		}
		
		@Override
		public String toXMLString() {
			return toString();
		}
		
		public String toString(){
			return fromDate.getStamp()+" "+fromTime.toString()+"#"+toDate.getStamp()+" "+toTime.toString();
		}

		@Override
		public int compareTo(TimeParameter o) {
			if(getFromDate().compareTo(o.getFromDate()) == 0){
				return getFromTime().compareTo(o.getFromTime());
			}else
				return getFromDate().compareTo(o.getFromDate());
		}
		
		
	}
	
	public static class PeopleParameter implements IDefinitionParameter, IGroup{
		
		private boolean isGroup = false;
		private String groupID = null;
		private String groupName = null;
		private String groupRelativePath = null;
		
		private String jmbag = null;
		
		public PeopleParameter(String id, String name, String path){
			this.groupID=id;
			this.groupName=name;
			this.groupRelativePath=path;
			this.isGroup=true;
		}
		
		public PeopleParameter(String param, boolean groupFlag){
			this.isGroup=groupFlag;
			if(groupFlag) {
				String[] elems = param.split("#");
				this.groupName=elems[0];
				this.groupID=elems[1];
				this.groupRelativePath=elems[2];
			}else{
				if(param == null) throw new IllegalParameterException("Pogrešan JMBAG (null)");
				else{
					if(param.length() != 10) throw new IllegalParameterException("JMBAG mora imati duljinu 10 znakova, pogrešni jmbag: " + param);
					try{
						Long.parseLong(param);
					}catch(NumberFormatException nfe){
						throw new IllegalParameterException("JMBAG mora biti broj, pogrešni jmbag: " + param);
					}
					this.jmbag=param;	
				}
				
			}
		}

		public boolean containsInRelativePath(PeopleParameter keenGroup){
			if (keenGroup.getGroupRelativePath().startsWith(this.getGroupRelativePath())) return true;
			else return false;
		}
		
		public int getType(){
			return Definition.PEOPLE_DEF;
		}
		
		public boolean isGroup() {
			return isGroup;
		}

		public void setGroup(boolean isGroup) {
			this.isGroup = isGroup;
		}

		public String getGroupID() {
			return groupID;
		}

		public void setGroupID(String groupID) {
			this.groupID = groupID;
		}

		public String getGroupName() {
			return groupName;
		}

		public void setGroupName(String groupName) {
			this.groupName = groupName;
		}

		public String getGroupRelativePath() {
			return groupRelativePath;
		}

		public void setGroupRelativePath(String groupRelativePath) {
			this.groupRelativePath = groupRelativePath;
		}

		public String getJmbag() {
			return jmbag;
		}

		public void setJmbag(String jmbag) {
			this.jmbag = jmbag;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((groupID == null) ? 0 : groupID.hashCode());
			result = prime * result + ((groupName == null) ? 0 : groupName.hashCode());
			result = prime * result + ((groupRelativePath == null) ? 0 : groupRelativePath.hashCode());
			result = prime * result + ((jmbag == null) ? 0 : jmbag.hashCode());
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
			PeopleParameter other = (PeopleParameter) obj;
			if(isGroup!=other.isGroup) return false;
			if(!isGroup){
				if(!jmbag.equals(other.jmbag)) return false;
				return true;
			}
			if (groupID == null) {
				if (other.groupID != null)
					return false;
			} else if (!groupID.equals(other.groupID))
				return false;
			if (groupName == null) {
				if (other.groupName != null)
					return false;
			} else if (!groupName.equals(other.groupName))
				return false;
			if (groupRelativePath == null) {
				if (other.groupRelativePath != null)
					return false;
			} else if (!groupRelativePath.equals(other.groupRelativePath))
				return false;
			
			return true;
		}
		
		
		@Override
		public String toXMLString() {
			return toString();
		}
		
		public String toString(){
			if(isGroup) return groupName+"#"+groupID+"#"+groupRelativePath;
			else return jmbag;
		}

	}
	
	public static class TeamParameter implements IDefinitionParameter{
		
		private String teamName = null;
		private String assistantName = null;
		private List<String> teamMembers = null;

		public TeamParameter(String teamName, String assistantName){
			setTeamName(teamName);
			setAssistantName(assistantName);
			teamMembers = new ArrayList<String>();
		}

		public TeamParameter(String teamString){
			if(teamString==null || teamString.isEmpty()) throw new IllegalParameterException("Invalid team parameter.");
			String[] teamElements = teamString.split(":");
			if(teamElements.length!=3) throw new IllegalParameterException("Invalid team parameter.");
			String[] students = teamElements[2].split(" ");
			if(students.length < 1) throw new IllegalParameterException("Invalid team parameter");
			setTeamName(teamElements[0]);
			setAssistantName(teamElements[1]);
			teamMembers = new ArrayList<String>();
			for(String s : students) teamMembers.add(s);			
		}

		public int getType(){
			return Definition.PEOPLE_DEF;
		}

		public String getTeamName() {
			return teamName;
		}

		public void setTeamName(String teamName) {
			if(teamName==null || teamName.isEmpty()) throw new IllegalParameterException("Neispravan naziv tima.");
			this.teamName = teamName;
		}

		public String getAssistantName() {
			return assistantName;
		}

		public void setAssistantName(String assistantName) {
			if(assistantName==null || assistantName.isEmpty()) throw new IllegalParameterException("Neispravno ime asistenta.");
			this.assistantName = assistantName;
		}

		public void addTeamMember(String userJMBAG){
			if(userJMBAG == null || userJMBAG.isEmpty()) throw new IllegalParameterException("Pogrešan JMBAG (null)");
			if(userJMBAG.length() != 10) throw new IllegalParameterException("JMBAG mora imati duljinu 10 znakova, pogrešni jmbag: " + userJMBAG);
			try{
				Long.parseLong(userJMBAG);
			}catch(NumberFormatException nfe){
				throw new IllegalParameterException("JMBAG mora biti broj, pogrešni jmbag: " + userJMBAG);
			}
			this.teamMembers.add(userJMBAG);
		}
		
		public List<String> getTeamMembers() {
			return teamMembers;
		}

		public void addTeamMembers(List<String> teamMembers) {
			if(teamMembers==null || teamMembers.isEmpty()) throw new IllegalParameterException("Tim mora sadržavati barem jednog studenta.");
			for(String s : teamMembers) addTeamMember(s);
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((teamName == null) ? 0 : teamName.hashCode());
			result = prime * result + ((assistantName == null) ? 0 : assistantName.hashCode());
			result = prime * result + ((teamMembers == null) ? 0 : teamMembers.hashCode());
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
			TeamParameter other = (TeamParameter) obj;
			if (teamName == null) {
				if (other.teamName != null)
					return false;
			} else if (!teamName.equals(other.teamName))
				return false;
			if (assistantName == null) {
				if (other.assistantName != null)
					return false;
			} else if (!assistantName.equals(other.assistantName))
				return false;
			if (teamMembers == null) {
				if (other.teamMembers != null)
					return false;
			} else if (!teamMembers.equals(other.teamMembers))
				return false;
			return true;
		}
		
		
		@Override
		public String toXMLString() {
			StringBuilder result = new StringBuilder();
			result.append("<team>");
				result.append(toString());
			result.append("</team>");
			return result.toString();
		}
		
		public String toString(){
			StringBuilder result = new StringBuilder();
			result.append(teamName + ":" + assistantName + ":");
			for(String jmbag : teamMembers) result.append(jmbag + " ");
			return result.toString();
		}

	}
	
	public static class RoomParameter implements IDefinitionParameter, ILocationParameter{
		private String id;
		private String name;
		private int lecturePlaces;
		private int exercisePlaces;
		private int assessmentPlaces;
		private int actualCapacity=-1;
		private int selectedActivity=Definition.LECTURE;

		public RoomParameter(String id, String name, int lp, int ep, int ap){
			this.id=id;
			this.name=name;
			this.lecturePlaces=lp;
			this.exercisePlaces=ep;
			this.assessmentPlaces=ap;
		}
		
		public RoomParameter(String room){
			String[] roomInfo = room.split("\\$");
			if(roomInfo.length!=3) throw new IllegalParameterException("Invalid room parameter");
			this.name=roomInfo[0];
			this.id=roomInfo[1];
			if(roomInfo[2].contains("-")){
				String[] cap = roomInfo[2].split("-");
				this.lecturePlaces=Integer.parseInt(cap[0]);
				this.exercisePlaces=Integer.parseInt(cap[1]);
				this.assessmentPlaces=Integer.parseInt(cap[2]);
			}else{
				this.actualCapacity=Integer.parseInt(roomInfo[2]);
				this.selectedActivity = -1;
			}
		}
			
		public int getType(){
			return Definition.LOCATION_DEF;
		}
		public String printForSelectedActivity() {
			StringBuilder sb = new StringBuilder();
			sb.append(getName());
			sb.append(", kapacitet: ");
			if(this.selectedActivity==Definition.LECTURE) sb.append(Integer.toString(getLecturePlaces()));
			else if(this.selectedActivity==Definition.EXERCISE) sb.append(Integer.toString(getExercisePlaces()));
			else if(this.selectedActivity==Definition.ASSESSMENT) sb.append(Integer.toString(getAssessmentPlaces()));
			else if(this.selectedActivity==-1) sb.append(Integer.toString(getActualCapacity()));
			return sb.toString();
		}
		
//		public String toString(){
//			StringBuilder sb = new StringBuilder();
//			sb.append(this.name+"#"+this.id+"#");
//			if(actualCapacity==-1) {
//				if(this.selectedActivity==Definition.LECTURE) sb.append(getLecturePlaces());
//				else if(this.selectedActivity==Definition.EXERCISE) sb.append(getExercisePlaces());
//				else if(this.selectedActivity==Definition.ASSESSMENT) sb.append(getAssessmentPlaces());			
//			}
//			else sb.append(this.actualCapacity);
//			return sb.toString();
//		}
//		
		public String toString(){
			StringBuilder sb = new StringBuilder();
			sb.append(this.name+"$"+this.id+"$");
			if(actualCapacity==-1) {
				if(this.selectedActivity==Definition.LECTURE) sb.append(getLecturePlaces());
				else if(this.selectedActivity==Definition.EXERCISE) sb.append(getExercisePlaces());
				else if(this.selectedActivity==Definition.ASSESSMENT) sb.append(getAssessmentPlaces());			
			}
			else sb.append(this.actualCapacity);
			return sb.toString();
		}
		
		public void updateActualCapacityToSelected () {
			if(actualCapacity==-1) {
				if(this.selectedActivity==Definition.LECTURE) actualCapacity = getLecturePlaces();
				else if(this.selectedActivity==Definition.EXERCISE) actualCapacity = getExercisePlaces();
				else if(this.selectedActivity==Definition.ASSESSMENT) actualCapacity = getAssessmentPlaces();			
			}
		}
		
		public String getId() {
			return id;
		}
		public void setID(String id){
			this.id = id;
		}
		
		public String getName() {
			return name;
		}
		public void setName(String name){
			this.name=name;
		}
		
		public int getLecturePlaces() {
			return lecturePlaces;
		}
		public int getExercisePlaces() {
			return exercisePlaces;
		}
		public int getAssessmentPlaces() {
			return assessmentPlaces;
		}

		public void setActualCapacity(int actualCapacity) {
			this.actualCapacity = actualCapacity;
			this.selectedActivity = -1;
		}

		public int getActualCapacity() {
			return actualCapacity;
		}

		public void setSelectedActivity(int selectedActivity) {
			this.selectedActivity = selectedActivity;
		}

		public int getSelectedActivity() {
			return selectedActivity;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((id == null) ? 0 : id.hashCode());
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
			RoomParameter other = (RoomParameter) obj;
			if (id == null) {
				if (other.id != null)
					return false;
			} else if (!id.equals(other.id))
				return false;
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			return true;
		}
		
		@Override
		public String toXMLString() {
			return toString();
		}
	}
	
	public interface IDefinitionParameter{
		public String toXMLString();
		public int getType();
	}
}

