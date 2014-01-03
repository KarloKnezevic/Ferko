package hr.fer.zemris.jcms.planning;

import hr.fer.zemris.jcms.exceptions.IllegalParameterException;
import hr.fer.zemris.jcms.model.planning.*;
import hr.fer.zemris.jcms.model.planning.Definition.IDefinitionParameter;
import hr.fer.zemris.jcms.model.planning.Definition.PeopleParameter;
import hr.fer.zemris.jcms.model.planning.Definition.RoomParameter;
import hr.fer.zemris.jcms.model.planning.PlanEvent.EventDistribution;
import hr.fer.zemris.jcms.model.planning.PlanEvent.Precondition;
import java.applet.Applet;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.xml.parsers.*;
import org.jdesktop.jxlayer.plaf.ext.LockableUI;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Kontroler za vezu model-Ferko s GUI-em
 * 
 */
public class PlanningController {

	private Applet applet = null;
	
	/** Param type list */
	private List<Integer> paramTypes;
	/** View models */
	private Map<Integer, ListModel> viewModels ;
	/** Event view model */
	private ListModel eventModel;
	/** Other event view model  - all events except the active one*/
	private ListModel otherEventModel = new DefaultComboBoxModel();
	/** Segment view model */
	private ListModel segmentModel;
	/** Course instance groups */
	private ComboBoxModel groupListModel;
	/** Pregledni model grupa: temeljni model (groupListModel) - definicija aktivnog entiteta */
	private ComboBoxModel viewingGroupModel;
	/** Temeljni model prostorija */
	private ListModel roomListModel;
	/** Pregledni model soba */
	private ListModel viewingRoomModel;
	
	/** Plan data */
	private Plan planData;
	/** Entity view of planData */
	private Map<String, PlanningEntity> planningEntities;
	/** Active entity */
	private String activeEntityName = null;
	
	/** Ferko URL access map */
	private Map<String, String> ferkoURLMap;
	/** Ferko URL */
	private String ferkoURL="http://localhost:8080/";
	/** Course instance data */
	private String courseInstanceID ;
	
	/** Event precondition model */
	private ComboBoxModel preconditionModel;
	private JButton preconditionAddButton;
	/** Pregledni model događaja - Događaji koji nisu u vezi s trenutno odabranim */
	private ListModel unconnectedEventModel;
	
	/** Pomocni model */
	private DefaultListModel dlm = null;
	
	/** Parameter definition panel (used for seting active entity name on border) */
	private JPanel definitionPanel;
	/** Event detail panel (used for seting event name on border) */
	private JPanel eventDetailPanel;
	
	/** Event duration model */
	private ComboBoxModel eventDurationModel;
	/** Event duration type - days or hours*/
	private ButtonModel eventDurationDays;
	private ButtonModel eventDurationHours;
	/** Event duration activation model */
	private ButtonModel eventDurationActivationModel;
	
	/** Term duration model */
	private ComboBoxModel termDurationModel;
	
	/** Plan level parameter models */
	private ButtonModel equalStudentDistributionModel;
	private ButtonModel equalTermSequenceModel;
	private JTextField termNumberInEachEventModel;
	
	/** Random group distribution parameters */
	JTextField[] minMaxLengthFields;
	/** Group distribution activation model*/
	private ButtonModel[] groupDistributionModels;
	/** Distribution method selection model */
	private SingleSelectionModel groupDistributionSelectionModel;
	
	/** Event detail lock control */
	private LockableUI eventDetailLock;
	/** Parameter locks */
	private Map<Integer, LockableUI> parameterLocks;
	
	/** GUI update in progress flag */
	private boolean guiUpdateInProgress = false;
	
	/** Saved plan ID */
	private String planID;
	
	/** Plan saving status */
	private JLabel infoLabel;
	
	/** Plan parameters button - Enabled only if at least one parameter is added at event level */
	private ButtonModel planParametersButtonModel;
	
	private static PlanningController instance = null;
	public static PlanningController getInstance(){
		if(instance==null) throw new IllegalParameterException("PlanningController not initialized!");
		return instance;
	}
	
	public PlanningController(String ferkoURL, String courseInstanceID, String planID){
		
		instance = this;
		
		setFerkoURL(ferkoURL);
		setCourseInstanceID(courseInstanceID);
		setPlanID(planID);
		
		this.viewModels = new HashMap<Integer, ListModel>();
		this.planningEntities = new HashMap<String, PlanningEntity>();

		this.paramTypes = new ArrayList<Integer>();
		getParamTypes().add(Definition.TIME_DEF);
		getParamTypes().add(Definition.PEOPLE_DEF);
		getParamTypes().add(Definition.LOCATION_DEF);
		
		this.ferkoURLMap = new HashMap<String, String>();
		getFerkoURLMap().put("home", "Planning.action");
		getFerkoURLMap().put("getCourseGroups", "Planning!getCourseInstanceGroups.action");
		getFerkoURLMap().put("getRoomList", "Planning!getRooms.action");
		getFerkoURLMap().put("savePlan", "Planning!savePlan.action");
		getFerkoURLMap().put("updatePlan", "Planning!updatePlan.action");
		getFerkoURLMap().put("getPlanStatus", "Planning!getPlanStatus.action");
		getFerkoURLMap().put("preparePlan", "Planning!preparePlan.action");
		getFerkoURLMap().put("getPlan", "Planning!getPlan.action");
		getFerkoURLMap().put("getLocalScheduler", "Planning!getLocalScheduler.action");
		
		this.groupListModel = new DefaultComboBoxModel();
		this.roomListModel = new DefaultListModel();
		this.parameterLocks = new HashMap<Integer, LockableUI>();
		
		
		if(getPlanID() == null ){ //Ako se radi o novom planu
			setPlanData(new Plan());
			
		}else{ //Ako se radi o izmjeni postojećeg plana
			
			Thread planLoader = new Thread(new Runnable(){
				@Override
				public void run() {
					getExternalPlanData();
				}
			});
			planLoader.start();
			try {
				System.out.println("[Planning] EDT waiting for plan loader.");
				planLoader.join();
				System.out.println("[Planning] EDT continues.");
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		Thread externalDataLoader = new Thread(new Runnable(){
			@Override
			public void run() {
				getExternalRoomAndGroupData();				
			}
		});
		externalDataLoader.start();
		
		try {
			externalDataLoader.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
	}
	
	public PlanningController(){
		
		instance = this;
		
		setFerkoURL(null);
		setCourseInstanceID(null);
		setPlanID(null);
		
		this.viewModels = new HashMap<Integer, ListModel>();
		this.planningEntities = new HashMap<String, PlanningEntity>();

		this.paramTypes = new ArrayList<Integer>();
		getParamTypes().add(Definition.TIME_DEF);
		getParamTypes().add(Definition.PEOPLE_DEF);
		getParamTypes().add(Definition.LOCATION_DEF);
		
		this.ferkoURLMap = new HashMap<String, String>();
		
		this.groupListModel = new DefaultComboBoxModel();
		this.roomListModel = new DefaultListModel();
		this.parameterLocks = new HashMap<Integer, LockableUI>();
		
		setPlanData(new Plan());
		
	}
	
	
	public void initiate(){
		//Priprema entiteta
		getPlanningEntities().put(getPlanData().getName(), getPlanData());
		System.out.println("[Planning] Initiated planning entity " + getPlanData().getName());
		
		//Ovo će postojati samo kod učitavanja postojećeg plana
		for(PlanEvent pe : getPlanData().getEvents()) {
			
			getPlanningEntities().put(pe.getName(), pe);
			System.out.println("[Planning] Initiated planning entity " + pe.getName());
			
			for(PlanEventSegment pes : pe.getSegments()){
				getPlanningEntities().put(pes.getName(), pes);
				System.out.println("[Planning] Initiated planning entity " + pes.getName());
				
			}
		}
		
		updateEventModelFromDefinition();
		
		setActivePlanningEntity(getPlanData().getName());
	}
	
	/**
	 * Postavlja novi aktivni entitet i prikazuje sve parametre u view modelima
	 * @param entityName
	 */
	public void setActivePlanningEntity(String entityName) {
		setActiveEntityName(entityName);
		PlanningEntity pe = getPlanningEntities().get(entityName);
		
		setGuiUpdateInProgress(true);
		
		//Ispis naziva aktivnog entiteta na borderu parametara
		setParameterBorderTitle("Parametri za " + entityTypeName(pe) + " " + getActiveEntityName());
		
		//Update modela za prikaz parametara
		updateAllViewModelsFromDefinition();
		
		if(pe instanceof Plan) {
			Plan p = (Plan)pe;
			if(p.getEvents()==null || p.getEvents().size()==0) getPlanParametersButtonModel().setEnabled(false);
			getEventDetailLock().setLocked(true);
		}
		else getEventDetailLock().setLocked(false);

		updatePlanLevelParametersFromDefinition();
		

		if(pe instanceof PlanEvent) {
			updateSegmentModelFromDefinition(pe);
			updateOtherEventModel();
			updateEventRelationModelFromDefinition();
			updateEventDurationModelFromDefinition();
			updateTermDurationModelFromDefinition();
			updateEventDistributionModelFromDefinition();
			setEventDetailBorderTitle(getActiveEntityName());
			
			boolean enablePlanParameters = false;
			for(int i=0; i< Definition.paramTypes.length; i++){
				if(pe.hasDefinedParameters(Definition.paramTypes[i])){
					enablePlanParameters = true;
					break;
				}
			}
			getPlanParametersButtonModel().setEnabled(enablePlanParameters);
			
		}else setEventDetailBorderTitle("");

		for(Integer i: Definition.paramTypes){
			LockableUI lock = getParameterLocks().get(i);
			if(pe.isAllowedToAddParameter(i)) lock.setLocked(false);
			else lock.setLocked(true);
		}
		 
		//Priprema modela za prikaz grupa
		updateViewingGroupModel();
		//Priprema modela za prikaz lokacija
		updateViewingRoomModel();
		
		setGuiUpdateInProgress(false);
		
		//Tmp prikaz plana
//		System.out.println(getPlanningEntities().get(getActiveEntityName()).toString());
		System.out.println(planData.toXMLString());
	}
	
	
	/**
	 * Prikazuje događaje aktivnog plana
	 * @param pe
	 */
	private void updateEventModelFromDefinition() {
		Plan p = getPlanData();
		dlm = (DefaultListModel)getEventModel();
		dlm.clear();
		for(PlanEvent pe : p.getEvents()) dlm.addElement(pe.getName()); 
	}

	
	/**
	 * Prikazuje segmente aktivnog događaja
	 * @param pe
	 */
	private void updateSegmentModelFromDefinition(PlanningEntity pe) {
		PlanEvent event = (PlanEvent)pe;
		dlm = (DefaultListModel)getSegmentModel();
		dlm.clear();
		for(String name : event.getSegmentNames()) dlm.addElement(name); 
	}

	/**
	 * Vraća opis tipa entiteta. 
	 * Koristi se kod informacije o trenutno prikazanim parametrima.
	 * @param pe
	 * @return
	 */
	private String entityTypeName(PlanningEntity pe){
		String rez ="";
		if(pe instanceof Plan) rez = "raspored";
		else if (pe instanceof PlanEvent) rez = "događaj";
		else if (pe instanceof PlanEventSegment) rez = "segment";
		return rez;
	}
	
	public void addParameter(IDefinitionParameter param){
		PlanningEntity pe = getPlanningEntities().get(activeEntityName);
		pe.addParameter(param);
		setActivePlanningEntity(getActiveEntityName());
	}
	
	public void removeParameter(IDefinitionParameter param){
		PlanningEntity pe = getPlanningEntities().get(activeEntityName);
		pe.removeParameter(param);
		setActivePlanningEntity(getActiveEntityName());
	}
	
	/**
	 * Update view modela prema definiciji (Definition) aktivnog entiteta (PlanningEntity: PlanData,PlanEvent,PlanEventSegment)
	 * @param defs
	 */
	private void updateAllViewModelsFromDefinition(){
		PlanningEntity pe = getPlanningEntities().get(getActiveEntityName());
		for(Integer paramType : getParamTypes()){
			dlm = (DefaultListModel)getViewModels().get(paramType);
			dlm.clear();
			for(IDefinitionParameter s: pe.getParameters(paramType)) dlm.addElement(s);
		}
	}
	
	/**
	 * Update modela sa svim dogadajima osim aktivnog
	 */
	private void updateOtherEventModel(){
		DefaultComboBoxModel eModel = (DefaultComboBoxModel)otherEventModel;
		eModel.removeAllElements();
		for(int i=0; i<getEventModel().getSize(); i++){
			String ev = (String)getEventModel().getElementAt(i);
			if(!ev.equals(activeEntityName)) eModel.addElement(ev);
		}
	}
	
	/**
	 * Registrira view model
	 * @param parameterModel
	 * @param panelType 0,1,2 za modele parametara, -1 za event model, -2 za segment model, -3 za otherEvent model
	 */
	public void addViewModel(ListModel model, int panelType) {
		if(panelType==-1) this.eventModel=model;
		else if(panelType==-2) this.segmentModel=model;
		else if(panelType==-3) this.setOtherEventModel(model);
		else if(!getViewModels().containsKey(panelType)) getViewModels().put(panelType, model);
	}
	
	/**
	 * Postavlja ime plana
	 * @param text
	 */
	public void setPlanName(String newName) {
		String oldName = getPlanData().getName();
		getPlanData().setName(newName);		
		PlanningEntity pe = getPlanningEntities().get(oldName);
		getPlanningEntities().remove(oldName);
		getPlanningEntities().put(newName, pe);
		setActivePlanningEntity(newName);
		setParameterBorderTitle("Parametri za raspored " + newName);
	}
	
	/**
	 * Izmjena naziva termina
	 * @param oldName
	 * @param newName
	 */
	public void renameTerm(String oldName, String newName){
		PlanningEntity pe = getPlanningEntities().get(oldName);
		PlanEventSegment pes = (PlanEventSegment)pe;
		PlanEvent event = pes.getParent();
		if(event.getName().equals(newName) || event.getSegment(newName)!=null){
			throw new IllegalParameterException("Taj naziv je već upotrijebljen u ovom događaju!");
		}
		
		pe.setName(newName);
		getPlanningEntities().remove(oldName);
		getPlanningEntities().put(newName, pe);
		setActivePlanningEntity(newName);
		updateSegmentModelFromDefinition(event);
		setParameterBorderTitle("Parametri za termin " + newName);
	}
	
	/**
	 * Dodavanje novog događaja u planu
	 * @param name 
	 */
	public void addNewPlanEvent(String name) {
		PlanEvent pe = getPlanData().addPlanEvent(name, null);
		getPlanningEntities().put(pe.getName(), pe);
		dlm = (DefaultListModel)getEventModel();
		dlm.addElement(pe.getName());
		
	}
	
	/**
	 * Brisanje događaja iz plana
	 * @param selectedEvent
	 */
	public void deletePlanEvent(String selectedEvent) {
		//Maknuti iz plana, aktivnih entiteta
		getPlanData().removePlanEvent(selectedEvent);
		getPlanningEntities().remove(selectedEvent);
		dlm = (DefaultListModel)getEventModel();
		dlm.removeElement(selectedEvent);
		//Postaviti raspored kao aktivni entitet
		setActivePlanningEntity(getPlanData().getName());
	}

	/**
	 * Upravljanje segmentima
	 * @param param ADD, DEL
	 * @param selectedSegment naziv kod ADD
	 */
	public void managePlanEventSegment(String param, String selectedSegment) {
		//Dohvat eventa nad kojim radimo
		PlanningEntity pe = getPlanningEntities().get(getActiveEntityName());
		PlanEvent event = null;
		PlanEventSegment segment = null;
		if(!(pe instanceof PlanEvent)) {
			segment = (PlanEventSegment)pe;
			event = segment.getParent();
		}
		else {
			event = (PlanEvent)pe;
		}
		
		dlm = (DefaultListModel)getSegmentModel();
		if(param.equals("ADD")){			
			segment = event.addSegment(null);
			getPlanningEntities().put(segment.getName(), segment);
			//setActivePlanningEntity(segment.getName());
			dlm.addElement(segment.getName());
		}else if (param.equals("DEL")){
			event.removeSegment(selectedSegment);
			getPlanningEntities().remove(selectedSegment);
			dlm.removeElement(selectedSegment);
			setActivePlanningEntity(event.getName());
		}
	}
	
	/**
	 * Dohvaća potrebne podatke o sobama i grupama s Ferka
	 * @param string
	 */
	public void getExternalRoomAndGroupData() {
		URL url = null;
		URLConnection urlc = null;
		
		try {		
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();			
			DefaultComboBoxModel model2 = (DefaultComboBoxModel)groupListModel;
					
			url = new URL(getFerkoURL()+getFerkoURLMap().get("getCourseGroups")+ "?courseInstanceID=" + getCourseInstanceID());
			urlc = url.openConnection();
			org.w3c.dom.Document doc = builder.parse(urlc.getInputStream());
			Node groupsNode = doc.getFirstChild().getFirstChild();
			org.w3c.dom.NodeList groups = groupsNode.getChildNodes();
			//Flat lista svih grupa
			for(int i = 0; i<groups.getLength(); i++){ 
				Node n = groups.item(i);
				Node nameNode = n.getFirstChild();
				Node idNode = nameNode.getNextSibling();
				Node pathNode = idNode.getNextSibling();
				model2.addElement(new PeopleParameter(nameNode.getTextContent()+"#"+idNode.getTextContent()+"#"+pathNode.getTextContent(), true));
			}
			System.out.println("[Planning] Loaded " + model2.getSize() + " groups from Ferko");
				
			DefaultListModel model3 = (DefaultListModel)roomListModel;
			
			url = new URL(getFerkoURL()+getFerkoURLMap().get("getRoomList")+ "?courseInstanceID=" + getCourseInstanceID());
			urlc = url.openConnection();
			doc = builder.parse(urlc.getInputStream());
			Node roomsNode = doc.getFirstChild().getFirstChild();
			org.w3c.dom.NodeList rooms = roomsNode.getChildNodes();
			for(int i = 0; i<rooms.getLength(); i++){ 
				Node n = rooms.item(i);
				Node nameNode = n.getFirstChild();
				Node idNode = nameNode.getNextSibling();
				Node capacityNode = idNode.getNextSibling();
				model3.addElement(new RoomParameter(nameNode.getTextContent()+"$"+idNode.getTextContent()+"$"+capacityNode.getTextContent()));
			}
			System.out.println("[Planning] Loaded " + model3.getSize() + " rooms from Ferko");
	
			
		}catch(Exception e){
			e.printStackTrace();
			manageException("Greška kod učitavanja podataka o sobama i grupama s FERKA", 0);
		}
		
//		} catch (MalformedURLException e) {
//			e.printStackTrace();
//			manageException("Greška kod učitavanja podataka o sobama i grupama s FERKA", 0);
//		} catch (IOException e) {
//			e.printStackTrace();
//			manageException("Greška kod učitavanja podataka o sobama i grupama s FERKA", 0);
//		} catch (ParserConfigurationException e) {
//			e.printStackTrace();
//			manageException("Greška kod učitavanja podataka o sobama i grupama s FERKA", 0);
//		} catch (SAXException e) {
//			e.printStackTrace();
//			manageException("Greška kod učitavanja podataka o sobama i grupama s FERKA", 0);
//		} 
		return;
	}
	
	/**
	 * Dohvaća podatke o planu s FERKA (potrebno za učitavanje plana kod uređivanja)
	 * @param string
	 */
	public void getExternalPlanData() {
		URL url = null;
		URLConnection urlc = null;
		System.out.println("[Planning] Beginning to load plan data.");
		try {		
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();			
					
			url = new URL(getFerkoURL()+getFerkoURLMap().get("getPlan")+ "?courseInstanceID=" + getCourseInstanceID() +"&planID=" + getPlanID());
			urlc = url.openConnection();
			org.w3c.dom.Document doc = builder.parse(urlc.getInputStream());
			Node planResult = doc.getFirstChild().getFirstChild();
			Node message = planResult.getNextSibling();
			
			if(planResult.getTextContent().equals("SUCCESS")){
				Node planNode = message.getFirstChild();
				if(planNode == null){
					manageException("Greška kod učitavanja plana s FERKA. (XML parsing error)", 0);
				}
				Plan plan = new Plan(planNode);
				setPlanData(plan);
				
				System.out.println("[Planning] Plan data loading completed.");
			}else{
				manageException("Greška kod učitavanja plana s FERKA. (Failure result)", 0);
			}
			
		}catch(Exception e){
			manageException("Greška kod učitavanja plana s FERKA.", 0);
			e.printStackTrace();
			
		}
		return;
	}
	
	/**
	 * Simple exception manager.
	 * Display a dialog with the given message and optionaly exits the applet.
	 * @param message
	 * @param severity 0 = exit the applet
	 * 				   1 = no change in applet
	 */
	public void manageException(final String message, final int severity){

		try{
			SwingUtilities.invokeAndWait(new Runnable(){
				@Override
				public void run() {
					JOptionPane.showMessageDialog(null, message, "Greška", JOptionPane.ERROR_MESSAGE);
					if(severity == 0)
					{
						try {
							getApplet().getAppletContext().showDocument(new URL(getFerkoURL()+getFerkoURLMap().get("home")+ "?courseInstanceID=" + getCourseInstanceID()));
						} catch (MalformedURLException ignored) {}
					}
				}
			});
		}catch(Exception e){
			e.printStackTrace();
		}
	}
	
	public void getLocalScheduler(){
		try {
			getApplet().getAppletContext().showDocument(new URL(getFerkoURL()+getFerkoURLMap().get("getLocalScheduler")+ "?courseInstanceID=" + getCourseInstanceID() + "&planID=" + getPlanID()));
		} catch (MalformedURLException ignored) {}
	}
	
	
	/**
	 * Provjera ispravnosti plana
	 * @return
	 */
	public void validatePlan(){
		getPlanData().validate();
	}
	
	/**
	 * Pohrana plana
	 */
	public SimpleGenericResult savePlan() {
		
		OutputStreamWriter osw=null;
		HttpURLConnection httpurlc=null;
		
		SimpleGenericResult result = new SimpleGenericResult();
		try {
			URL url = null;
			if(getPlanID()==null){
				url = new URL(getFerkoURL()+getFerkoURLMap().get("savePlan")+ 
						"?courseInstanceID=" + URLEncoder.encode(getCourseInstanceID(),"UTF-8"));
			}else{
				url = new URL(getFerkoURL()+getFerkoURLMap().get("updatePlan")+ 
						"?courseInstanceID=" + URLEncoder.encode(getCourseInstanceID(),"UTF-8") + "&planID=" + getPlanID());
			}
			httpurlc = (HttpURLConnection) url.openConnection();
			httpurlc.setDoOutput(true);
			httpurlc.setRequestMethod("POST");
			httpurlc.setRequestProperty( "Content-type", "application/x-www-form-urlencoded" );
			String body = "planData="+getPlanData().toXMLString();
			httpurlc.setRequestProperty( "Content-length", Integer.toString(body.length()));
			osw = new OutputStreamWriter(httpurlc.getOutputStream(), "UTF-8");
			osw.write(body);
			osw.flush();

			int responseCode = httpurlc.getResponseCode();
			//Ako 200-OK
			if(responseCode==200){
				DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
				org.w3c.dom.Document doc = builder.parse(httpurlc.getInputStream());
				Node planResult = doc.getFirstChild().getFirstChild();
				Node message = planResult.getNextSibling();
				
				String pid = message.getTextContent();
				setPlanID(pid);
				
				if(planResult.getTextContent().equals("SUCCESS")){
					result.setResultState(SimpleGenericResult.SUCCESS);
					result.addMessage("<html><font color=green>Plan uspješno pohranjen i pripremljen.</font>");
				}else{
					result.setResultState(SimpleGenericResult.FAILURE);
					result.addMessage("<html><font color=red>Greška u pohrani i pripremi plana!</font>");
				}

			}else{
				BufferedReader br = new BufferedReader(new InputStreamReader(httpurlc.getInputStream()));
				String str = br.readLine();
				while(str.equals(null)){
					System.out.println(str);
					str = br.readLine();
				}
				result.setResultState(SimpleGenericResult.FAILURE);
				result.addMessage("<html><font color=red>Greška u komunikaciji s Ferkom!</font>");
			}
			

		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} finally{
			try {
				osw.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			httpurlc.disconnect();
		}
		return result;
	}
	
	/**
	 * Update prikaznog modela za biranje grupa.
	 * Grupe koje su vec dodane u definiciji se ne nude.
	 */
	public void updateViewingGroupModel() {
//		System.out.println("[Planning] Began viewing group model update");
		//Dohvat grupa dodanih u definiciju
		List<IDefinitionParameter> defGroups = getPlanningEntities().get(getActiveEntityName()).getParameters(Definition.PEOPLE_DEF);
		
		DefaultComboBoxModel gModel = (DefaultComboBoxModel)getGroupListModel();
		DefaultComboBoxModel vModel = (DefaultComboBoxModel)getViewingGroupModel(); 
		vModel.removeAllElements();
		
		//Ubaci u prikazni model: grupe iz temeljnog modela bez grupa iz modela u definiciji
		for(int i = 0; i < gModel.getSize(); i++){
			PeopleParameter keenGroup = (PeopleParameter)gModel.getElementAt(i);
			if(groupHierarchyValidation(keenGroup, defGroups)) vModel.addElement(keenGroup);
		}
//		System.out.println("[Planning] Completed viewing group model update with " + vModel.getSize() + " groups");
	}
	private boolean groupHierarchyValidation(PeopleParameter keenGroup, List<IDefinitionParameter> defGroups){
		if(defGroups.contains(keenGroup)) return false;
		//Provjera obuhvaca li neka grupa iz definicija grupu koja se zeli ubaciti u definiciju (keenGroup)
		for(IDefinitionParameter s : defGroups) {
			if(s instanceof PeopleParameter){
				PeopleParameter pp = (PeopleParameter)s;
				if(pp.isGroup() && pp.containsInRelativePath(keenGroup)) return false;
			}
		}
		return true;
	}
	
	/**
	 * Update prikaznog modela za biranje lokacija.
	 * Lokacije koje su vec dodane u definiciji se ne nude.
	 */
	public void updateViewingRoomModel() {

		//Dohvat grupa dodanih u definiciju
		PlanningEntity pe = getPlanningEntities().get(getActiveEntityName());
		
		DefaultListModel roomModel = (DefaultListModel)getRoomListModel();
		DefaultListModel viewModel = (DefaultListModel)getViewingRoomModel(); 
		viewModel.clear();
		
		//Ubaci u prikazni model: temeljni model - model u definiciji
		for(int i = 0; i < roomModel.getSize(); i++){
			RoomParameter r1 = (RoomParameter)roomModel.getElementAt(i);
			if(!pe.containsRoom(r1)) viewModel.addElement(r1);
		}
		
		//Koji tip aktivnosti je u definiciji? Po defaultu je 0, čak i ako nema parametara 
		//tj. prikazuju se kapaciteti za predavanja
		refreshViewingRoomModel(pe.getSelectedActivityType());
		
	}
	
	/**
	 * Refresh kapaciteta prikaznog modela za biranje lokacija.
	 * Update samo lokacija u view modelu s novim kapacitetima iz temeljnog modela
	 */
	public void refreshViewingRoomModel(Integer activityType) {
		DefaultListModel viewModel = (DefaultListModel)getViewingRoomModel(); 
		
		//Za svaki element view modela dohvatiti novi kapacitet ovisno o tipu aktivnosti
		for(int i = 0; i < viewModel.getSize(); i++){
			RoomParameter room = (RoomParameter)viewModel.getElementAt(i);
			room.setSelectedActivity(activityType);
			viewModel.set(i, room);
		}
		
		//Spremanje odabranog tipa u definiciju
		getPlanningEntities().get(getActiveEntityName()).setSelectedActivityType(activityType);
	}
	
	/**
	 * Dodavanje preduvjeta događaju
	 * @param string
	 * @throws  
	 */
	public void addEventPrecondition(String eventName, String timeDistance) throws IllegalParameterException {
		PlanEvent activeEvent = fetchActiveEvent();
		activeEvent.addPrecondition(getPlanData().getEvent(eventName), timeDistance);
		updateEventRelationModelFromDefinition();
	}
	
	/**
	 * Brisanje odabranog preduvjeta
	 * @param precondition
	 */
	public void deleteEventPrecondition(Object precondition){
		fetchActiveEvent().getPreconditions().remove((Precondition)precondition);
		updateEventRelationModelFromDefinition();
	}
	
	/**
	 * Učitavanje podataka o odnosima događaja iz definicije događaja u view model
	 */
	private void updateEventRelationModelFromDefinition(){
		PlanEvent activeEvent = fetchActiveEvent();
		DefaultComboBoxModel d = (DefaultComboBoxModel)getPreconditionModel();
		d.removeAllElements();
		for(Precondition p : activeEvent.getPreconditions()) d.addElement(p);
		
		//Priprema view modela za prikaz događaja s kojima trenutni događaj nije u odnosu
		DefaultComboBoxModel dlm = (DefaultComboBoxModel)otherEventModel;
		DefaultComboBoxModel unconn = (DefaultComboBoxModel)getUnconnectedEventModel();
		unconn.removeAllElements();
		for(int i = 0; i<dlm.getSize(); i++){
			String otherEvent = (String)dlm.getElementAt(i);
			//1. smjer
			if(!activeEvent.preconditionExists(otherEvent)){
				//2. smjer
				PlanEvent pe = getPlanData().getEvent(otherEvent);
				if(!pe.preconditionExists(activeEvent.getName())) {
					unconn.addElement(otherEvent);
				}
				
			}
		}
	}

	/** 
	 * Postavljanje naziva na borderu oko parametara
	 * @param title
	 */
	public void setParameterBorderTitle(String title) {
       	getDefinitionPanel().setBorder(BorderFactory.createCompoundBorder(
       			BorderFactory.createTitledBorder(null, title, TitledBorder.DEFAULT_JUSTIFICATION, 
       					TitledBorder.DEFAULT_POSITION, new Font(Font.SANS_SERIF, Font.BOLD, 15)), 
       			BorderFactory.createEmptyBorder(5,5,5,5)));
	}
	
	/**
	 * Postavljanje naziva događaja na borderu oko detalja događaja
	 * @param title
	 */
	public void setEventDetailBorderTitle(String title) {
    	getEventDetailPanel().setBorder(BorderFactory.createCompoundBorder(
    			BorderFactory.createTitledBorder(null, "Detalji događaja " + title, TitledBorder.DEFAULT_JUSTIFICATION, 
    					TitledBorder.DEFAULT_POSITION, new Font(Font.SANS_SERIF, Font.BOLD, 15)),
                BorderFactory.createEmptyBorder(5,5,5,5)));
	}
	
	/** Spremanje informacije o maksimalnom trajanju događaja */
	public void updateEventDurationDefinition(String cmd) {
		PlanEvent activeEvent = fetchActiveEvent();System.out.println("updateEventDurationDefinition");
		if(cmd.equals("NO_DURATION")) {
			activeEvent.setMaximumDuration(-1);
		}else if(cmd.equals("COLLECT_DATA")){
			Object selectedItem = getEventDurationModel().getSelectedItem();
			if(selectedItem!=null) {
				System.out.println("updating with " + (Integer)selectedItem);
				activeEvent.setMaximumDuration((Integer)selectedItem);
			}
			else System.out.println("selected item is null");
		}
	}
	/**
	 * Učitavanje podataka o max trajanju događaja iz definicije događaja u view modele 
	 */
	private void updateEventDurationModelFromDefinition() {
		PlanEvent activeEvent = fetchActiveEvent();
		int eventDuration = activeEvent.getMaximumDuration();
		if(eventDuration==-1){
			getEventDurationActivationModel().setSelected(false);
		}else{
			getEventDurationActivationModel().setSelected(true);
			if(eventDuration<15) getEventDurationDays().setSelected(true);
			else getEventDurationHours().setSelected(true);
			getEventDurationModel().setSelectedItem(eventDuration);
		}
	}
	

	/** Spremanje informacije o trajanju termina */
	public void updateTermDurationDefinition() {
		PlanEvent activeEvent = fetchActiveEvent();
		activeEvent.setTermDuration((Integer)getTermDurationModel().getSelectedItem());
	}
	
	/**
	 * Učitavanje podataka o trajanju termina iz definicije događaja u view modele 
	 */
	private void updateTermDurationModelFromDefinition() {
		PlanEvent activeEvent = fetchActiveEvent();
		int eventDuration = activeEvent.getTermDuration();
		getTermDurationModel().setSelectedItem(eventDuration);
	}
	
	public void updateEventDistributionType(int type){
		PlanEvent activeEvent = fetchActiveEvent();
		activeEvent.changeEventDistributionType(type);
		setActivePlanningEntity(activeEvent.getName());
	}
	
	public void updatePlanLevelParameters(){
		Plan p = getPlanData();
		//Broj termina
		int termNumber;
		String termNumberText = getTermNumberInEachEventModel().getText();
		if(termNumberText.isEmpty()) p.setTermNumberInEachEvent(-1);
		try{
			termNumber = Integer.parseInt(termNumberText);
			if(termNumber > 0) p.setTermNumberInEachEvent(termNumber);
			else p.setTermNumberInEachEvent(-1);
		}catch(NumberFormatException nfe){
			p.setTermNumberInEachEvent(-1);
		}

		//Jednaka podjela studenata
		p.setEqualStudentDistributionInEachEvent(getEqualStudentDistributionModel().isSelected());
		
		//Jednak redoslijed termina
		p.setEqualTermSequenceInEachEvent(getEqualTermSequenceModel().isSelected());
	}
	
	public void updatePlanLevelParametersFromDefinition(){
		Plan p = getPlanData();
		//Broj termina
		if(p.getTermNumberInEachEvent() > 0) getTermNumberInEachEventModel().setText(Integer.toString(p.getTermNumberInEachEvent()));

		//Jednaka podjela studenata
		getEqualStudentDistributionModel().setSelected(p.isEqualStudentDistributionInEachEvent());
		
		//Jednak redoslijed termina
		getEqualTermSequenceModel().setSelected(p.isEqualTermSequenceInEachEvent());
	}	
	
	
	public void updateMinimumTermNumber(){
		PlanEvent activeEvent = fetchActiveEvent();
		EventDistribution distrib = activeEvent.getDistribution();
		int minimum;
		try{
			minimum = Integer.parseInt(minMaxLengthFields[0].getText());
		}catch(NumberFormatException nfe){
			return;
		}
		if(minimum==distrib.getMinimumTermNumber()) return;
		distrib.setMinimumTermNumber(minimum);
	}
	
	public void updateMaximumTermNumber(){
		PlanEvent activeEvent = fetchActiveEvent();
		EventDistribution distrib = activeEvent.getDistribution();
		int maximum;
		try{
			maximum = Integer.parseInt(minMaxLengthFields[1].getText());
		}catch(NumberFormatException nfe){
			return;
		}
		if(maximum==distrib.getMaximumTermNumber()) return;
		distrib.setMaximumTermNumber(maximum);
	}
	
	
	
	/**
	 * Učitavanje podataka o raspodjeli iz plana u view model 
	 */
	private void updateEventDistributionModelFromDefinition(){
		PlanEvent activeEvent = fetchActiveEvent();
		EventDistribution distribution = activeEvent.getDistribution();
		getMinMaxLengthFields()[0].setEnabled(true);
		getMinMaxLengthFields()[1].setEnabled(true);
		if(distribution.getType()==Definition.RANDOM_DISTRIBUTION){
			getGroupDistributionSelectionModel().setSelectedIndex(0);
			getGroupDistributionModel()[0].setSelected(true);
			getGroupDistributionModel()[1].setSelected(false);

			String newMinValue = Integer.toString(distribution.getMinimumTermNumber());
			if(!newMinValue.equals(getMinMaxLengthFields()[0].getText())) getMinMaxLengthFields()[0].setText(newMinValue);
			
			String newMaxValue = Integer.toString(distribution.getMaximumTermNumber());
			if(!newMaxValue.equals(getMinMaxLengthFields()[1].getText())) getMinMaxLengthFields()[1].setText(newMaxValue);
			
		}else if(distribution.getType()==Definition.GIVEN_DISTRIBUTION){
			getGroupDistributionSelectionModel().setSelectedIndex(1);
			getGroupDistributionModel()[1].setSelected(true);
			getGroupDistributionModel()[0].setSelected(false);

		}
	}
 
	/**
	 * Dohvaća aktivni događaj eksplicitno ili implicitno preko djeteta
	 * @return
	 */
	private PlanEvent fetchActiveEvent(){
		PlanningEntity activeEntity = getPlanningEntities().get(getActiveEntityName());
		PlanEvent activeEvent=null;
		if(activeEntity instanceof PlanEvent) {
			activeEvent = (PlanEvent)activeEntity;
		}
		else if (activeEntity instanceof PlanEventSegment){
			PlanEventSegment pes = (PlanEventSegment)activeEntity;
			activeEvent = pes.getParent();
		}
		return activeEvent;
	}
	
	/**
	 * Smije li se aktivnom entitetu dodati parametar zadanog tipa
	 * @param paramType
	 * @return
	 */
	public boolean isAllowedToAddParameter(int paramType){
		PlanningEntity pe = getPlanningEntities().get(getActiveEntityName());
		return pe.isAllowedToAddParameter(paramType);
	}
	
	public boolean doesEntityExistWithName(String entityName){
		return this.getPlanningEntities().get(entityName) != null;
	}
	
	public void setViewModels(Map<Integer, ListModel> viewModels) {
		this.viewModels = viewModels;
	}

	public Map<Integer, ListModel> getViewModels() {
		return viewModels;
	}

	public void setPlanData(Plan planData) {
		this.planData = planData;
	}

	public Plan getPlanData() {
		return planData;
	}

	public void setParamTypes(List<Integer> paramTypes) {
		this.paramTypes = paramTypes;
	}

	public List<Integer> getParamTypes() {
		return paramTypes;
	}

	public void setPlanningEntities(Map<String, PlanningEntity> planningEntities) {
		this.planningEntities = planningEntities;
	}

	public Map<String, PlanningEntity> getPlanningEntities() {
		return planningEntities;
	}

	public void setActiveEntityName(String activeEntityName) {
		this.activeEntityName = activeEntityName;
	}

	public String getActiveEntityName() {
		return activeEntityName;
	}

	public void setEventModel(ListModel eventModel) {
		this.eventModel = eventModel;
	}

	public ListModel getEventModel() {
		return eventModel;
	}

	public void setSegmentModel(ListModel segmentModel) {
		this.segmentModel = segmentModel;
	}

	public ListModel getSegmentModel() {
		return segmentModel;
	}

	public ComboBoxModel getGroupListModel() {
		return groupListModel;
	}

	public void setFerkoURLMap(Map<String, String> ferkoURLMap) {
		this.ferkoURLMap = ferkoURLMap;
	}

	public Map<String, String> getFerkoURLMap() {
		return ferkoURLMap;
	}

	public void setCourseInstanceID(String courseInstanceID) {
		this.courseInstanceID = courseInstanceID;
	}

	public String getCourseInstanceID() {
		return courseInstanceID;
	}

	public void setViewingGroupModel(ComboBoxModel viewingGroupModel) {
		this.viewingGroupModel = viewingGroupModel;
	}

	public ComboBoxModel getViewingGroupModel() {
		return viewingGroupModel;
	}

	public void setOtherEventModel(ListModel otherEventModel) {
		this.otherEventModel = otherEventModel;
	}

	public ListModel getOtherEventModel() {
		return otherEventModel;
	}

	public void setDefinitionPanel(JPanel definitionPanel) {
		this.definitionPanel = definitionPanel;
	}
	
	public JPanel getDefinitionPanel() {
		return this.definitionPanel;
	}

	public void setEventDetailPanel(JPanel eventAllPanel) {
		this.eventDetailPanel = eventAllPanel;
	}

	public JPanel getEventDetailPanel() {
		return this.eventDetailPanel;
	}

	public void setEventDurationActivationModel(
			ButtonModel eventDurationActivationModel) {
		this.eventDurationActivationModel = eventDurationActivationModel;
	}

	public ButtonModel getEventDurationActivationModel() {
		return eventDurationActivationModel;
	}

	public JTextField[] getMinMaxLengthFields() {
		return minMaxLengthFields;
	}

	public void setMinMaxLengthFields(JTextField[] minMaxLengthFields) {
		this.minMaxLengthFields = minMaxLengthFields;
	}

	public ButtonModel[] getGroupDistributionModel() {
		return groupDistributionModels;
	}

	public void setGroupDistributionModel(ButtonModel[] groupDistributionModels) {
		this.groupDistributionModels = groupDistributionModels;
	}

	public void setGroupDistributionSelectionModel(
			SingleSelectionModel groupDistributionSelectionModel) {
		this.groupDistributionSelectionModel = groupDistributionSelectionModel;
	}

	public SingleSelectionModel getGroupDistributionSelectionModel() {
		return groupDistributionSelectionModel;
	}

	public void setEventDetailLockControl(LockableUI lockableUI) {
		this.eventDetailLock = lockableUI;
	}
	
	public LockableUI getEventDetailLock(){
		return this.eventDetailLock;
	}

	public void setParameterLocks(Map<Integer, LockableUI> parameterLocks) {
		this.parameterLocks = parameterLocks;
	}

	public Map<Integer, LockableUI> getParameterLocks() {
		return parameterLocks;
	}

	public void setViewingRoomModel(ListModel viewingRoomModel) {
		this.viewingRoomModel = viewingRoomModel;
	}

	public ListModel getViewingRoomModel() {
		return viewingRoomModel;
	}

	public ListModel getRoomListModel() {
		return roomListModel;
	}

	public void setRoomListModel(ListModel roomListModel) {
		this.roomListModel = roomListModel;
	}


	public void setEventDurationModel(ComboBoxModel eventDurationModel) {
		this.eventDurationModel = eventDurationModel;
	}

	public ComboBoxModel getEventDurationModel() {
		return eventDurationModel;
	}


	public void setEventDurationDays(ButtonModel eventDurationDays) {
		this.eventDurationDays = eventDurationDays;
	}

	public ButtonModel getEventDurationDays() {
		return eventDurationDays;
	}


	public void setEventDurationHours(ButtonModel eventDurationHours) {
		this.eventDurationHours = eventDurationHours;
	}

	public ButtonModel getEventDurationHours() {
		return eventDurationHours;
	}


	public String getFerkoURL() {
		return ferkoURL;
	}

	public void setFerkoURL(String ferkoURL) {
		this.ferkoURL = ferkoURL;
	}

	/**
	 * @param savedPlanID the savedPlanID to set
	 */
	public void setPlanID(String savedPlanID) {
		this.planID = savedPlanID;
	}

	/**
	 * @return the savedPlanID
	 */
	public String getPlanID() {
		return planID;
	}

	/**
	 * @param termDurationModel the termDurationModel to set
	 */
	public void setTermDurationModel(ComboBoxModel termDurationModel) {
		this.termDurationModel = termDurationModel;
	}

	/**
	 * @return the termDurationModel
	 */
	public ComboBoxModel getTermDurationModel() {
		return termDurationModel;
	}

	public JLabel getInfoLabel() {
		return infoLabel;
	}

	public void setInfoLabel(JLabel infoLabel) {
		this.infoLabel = infoLabel;
	}

	public ButtonModel getEqualStudentDistributionModel() {
		return equalStudentDistributionModel;
	}

	public void setEqualStudentDistributionModel(
			ButtonModel equalStudentDistributionModel) {
		this.equalStudentDistributionModel = equalStudentDistributionModel;
	}

	public ButtonModel getEqualTermSequenceModel() {
		return equalTermSequenceModel;
	}

	public void setEqualTermSequenceModel(ButtonModel equalTermSequenceModel) {
		this.equalTermSequenceModel = equalTermSequenceModel;
	}

	public JTextField getTermNumberInEachEventModel() {
		return termNumberInEachEventModel;
	}

	public void setTermNumberInEachEventModel(JTextField termNumberInEachEventModel) {
		this.termNumberInEachEventModel = termNumberInEachEventModel;
	}

	public boolean isGuiUpdateInProgress() {
		return guiUpdateInProgress;
	}

	public void setGuiUpdateInProgress(boolean guiUpdateInProgress) {
		this.guiUpdateInProgress = guiUpdateInProgress;
	}

	public void setPreconditionModel(ComboBoxModel preconditionModel) {
		this.preconditionModel = preconditionModel;
	}

	public ComboBoxModel getPreconditionModel() {
		return preconditionModel;
	}
	
	public JButton getPreconditionAddButton() {
		return preconditionAddButton;
	}

	public void setPreconditionAddButton(JButton preconditionAddButton) {
		this.preconditionAddButton = preconditionAddButton;
	}

	public ListModel getUnconnectedEventModel() {
		return unconnectedEventModel;
	}

	public void setUnconnectedEventModel(ListModel unconnectedEventModel) {
		this.unconnectedEventModel = unconnectedEventModel;
	}

	public Applet getApplet() {
		return applet;
	}

	public void setApplet(Applet applet) {
		this.applet = applet;
	}

	public void setPlanParametersButtonModel(ButtonModel planParametersButtonModel) {
		this.planParametersButtonModel = planParametersButtonModel;
	}

	public ButtonModel getPlanParametersButtonModel() {
		return planParametersButtonModel;
	}

	public class SimpleGenericResult{
		
		public static final int SUCCESS=0;
		public static final int FAILURE=1;
		
		private int resultState;
		private List<String> messages = new ArrayList<String>();
		
		public SimpleGenericResult(){
		}

		public SimpleGenericResult addMessage(String msg){
			this.messages.add(msg);
			return this;
		}
		
		public int getResultState() {
			return resultState;
		}

		public void setResultState(int resultState) {
			this.resultState = resultState;
		}

		public List<String> getMessages() {
			return messages;
		}

		public void setMessages(List<String> messages) {
			this.messages = messages;
		}
		
		
	}
	

}
