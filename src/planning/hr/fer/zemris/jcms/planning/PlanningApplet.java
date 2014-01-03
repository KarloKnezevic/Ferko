package hr.fer.zemris.jcms.planning;

import hr.fer.zemris.jcms.exceptions.IllegalParameterException;
import hr.fer.zemris.jcms.model.planning.Definition;
import hr.fer.zemris.jcms.model.planning.ValidationResult;
import hr.fer.zemris.jcms.model.planning.Definition.IDefinitionParameter;
import hr.fer.zemris.jcms.model.planning.Definition.PeopleParameter;
import hr.fer.zemris.jcms.model.planning.Definition.RoomParameter;
import hr.fer.zemris.jcms.model.planning.Definition.TeamParameter;
import hr.fer.zemris.jcms.model.planning.Definition.TimeParameter;
import hr.fer.zemris.jcms.model.planning.PlanEvent.Precondition;
import hr.fer.zemris.jcms.planning.PlanningController.SimpleGenericResult;

import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import javax.swing.event.*;
import org.jdesktop.jxlayer.JXLayer;
import org.jdesktop.jxlayer.plaf.effect.BufferedImageOpEffect;
import org.jdesktop.jxlayer.plaf.ext.LockableUI;
import com.jhlabs.image.GrayFilter;
 
/**
 * GUI usluge planiranja
 *  
 */
public class PlanningApplet extends JApplet {

	/* Zastavica koja indicira radi li se o pokretanju appleta u normalnom
	 * produkcijskom okruzenju kroz FERKO (true) ili se radi o lokalnom 
	 * pokretanju appleta u svrhu testiranja (false).
	 */
	private static boolean productionContext = true;
	
	
	private static final long serialVersionUID = 1L;
	private PlanningController controller;
	private JTextField planName;
	private PlanningFrame frm;

	/**
	 * Inicijalizacija appleta
	 */
    public void init() {
         try {
        	final Applet thisApplet = this;
        	
            SwingUtilities.invokeAndWait(new Runnable() {
                public void run() {                    

                	String ferkoURL = getCodeBase().getProtocol() + "://" + getCodeBase().getHost();
                	if(getCodeBase().getPort() != -1) ferkoURL += ":" + getCodeBase().getPort();
                	ferkoURL += "/ferko/";
                	
                	if(productionContext){
                		controller = new PlanningController(ferkoURL, getParameter("courseInstanceID"), getParameter("planIDToLoad"));
                	}else{
                		controller = new PlanningController();
                	}
                	controller.setApplet(thisApplet);
                	
                	frm = new PlanningFrame();
                	
                	controller.initiate();
                	
                	createActivationGUI();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void start() {
        
    }

    public void stop() {
       
    }

    public void destroy() {
    	
    }
    
    private void createActivationGUI(){
    	String buttonText = "Započni izradu plana!";
    	if(controller.getPlanID()!=null) buttonText = "Započni izmjenu plana!";
    	JButton act = new JButton(buttonText);
    	act.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e) {
				frm.setVisible(true);
			}
    	});
    	setSize(200, 40);
    	getContentPane().add(act);
    }
    
    /**
     * Glavni prozor
     * @author Ivan
     *
     */
    private class PlanningFrame extends JFrame{
    	
		private static final long serialVersionUID = 1L;

		public PlanningFrame(){
    		createGUI();
    	}
    
	    // Generiranje sučelja 
	    private void createGUI() {    
	    	try{
		    	setTitle("FERKO - Izrada rasporeda");
		     	setSize(840, 680);
		     	setDefaultCloseOperation(HIDE_ON_CLOSE);
		     	setResizable(false);
		    	getContentPane().setLayout(new BoxLayout(this.getContentPane(), BoxLayout.Y_AXIS));
		    	final JList eventList = new JList();
		    	createPlanDetailsPanel(eventList);
		    	createEventPanel(eventList);
		    	createParameterPanel();
	    	}catch(Exception unrecoverable){
	    		JOptionPane.showMessageDialog(this, "Došlo je do pogreške u radu.\n\n"+unrecoverable.getMessage(), "Iznimka", JOptionPane.ERROR_MESSAGE);
	    		try {
					getAppletContext().showDocument(new URL(controller.getFerkoURL()+controller.getFerkoURLMap().get("home")+"?courseInstanceID="+controller.getCourseInstanceID()));
				} catch (MalformedURLException ignored) {}
	    	}
	    }

	    /**
	     * Panel s osnovnim informacijama o rasporedu
	     * @param eventList
	     */
	    private void createPlanDetailsPanel(final JList eventList){
	    	final FinalDialog dialog = new FinalDialog(frm, "Pohrana i izrada", true);
	    	
	       	//Panel za naziv rasporeda
	    	JPanel planNamePanel = new JPanel();
	    	planName = new JTextField(30);
	    	final JButton displayPlanParametersButton = new JButton("Parametri rasporeda");
	    	final JButton savePlanButton = new JButton("Pohrani raspored");
	    	JPanel planLevelSegmentDefPanel = new JPanel();
	    	final JTextField planLevelSegmentNumber = new JTextField(2);
	    	JLabel planLevelSegmentNumberInfo = new JLabel("Broj termina svakog događaja");
	    	JCheckBox identicalSegmentDivision = new JCheckBox("Jednaka podjela studenata u svakom događaju");
	    	JCheckBox identicalSegmentSequence = new JCheckBox("Jednak redoslijed grupa u svakom događaju");
	    	
	    	displayPlanParametersButton.setEnabled(false);
	    	
	    	//Privremeno onemoguceni parametri:
	    	identicalSegmentDivision.setEnabled(false);
	    	identicalSegmentSequence.setEnabled(false);
	    	
	    	controller.setPlanParametersButtonModel(displayPlanParametersButton.getModel());
	    	controller.setEqualStudentDistributionModel(identicalSegmentDivision.getModel());
	    	controller.setEqualTermSequenceModel(identicalSegmentSequence.getModel());
	    	controller.setTermNumberInEachEventModel(planLevelSegmentNumber);
	    	
	    	planLevelSegmentNumber.getDocument().addDocumentListener(new DocumentListener(){
				public void changedUpdate(DocumentEvent e) {}
				public void insertUpdate(DocumentEvent e) {
					controller.updatePlanLevelParameters();
				} 
				public void removeUpdate(DocumentEvent e) {
					controller.updatePlanLevelParameters();
				}
	    	});
	    	identicalSegmentDivision.addItemListener(new ItemListener(){
				public void itemStateChanged(ItemEvent e) {
					controller.updatePlanLevelParameters();
				}
	    	});
	    	identicalSegmentSequence.addItemListener(new ItemListener(){
				public void itemStateChanged(ItemEvent e) {
					controller.updatePlanLevelParameters();
				}
	    	});
	    	
	    	planNamePanel.setBorder(BorderFactory.createCompoundBorder(
	    			BorderFactory.createTitledBorder(null, "Osnovno o rasporedu", TitledBorder.DEFAULT_JUSTIFICATION, 
	    					TitledBorder.DEFAULT_POSITION, new Font(Font.SANS_SERIF, Font.BOLD, 15)),
	                BorderFactory.createEmptyBorder(5,5,5,5)));
	    	planNamePanel.setLayout(new FlowLayout(FlowLayout.LEADING));
	    	planName.setText(controller.getPlanData().getName());
	    	planName.getDocument().addDocumentListener(new DocumentListener(){
				public void changedUpdate(DocumentEvent e) {}
				public void insertUpdate(DocumentEvent e) {
					common();
				}
				public void removeUpdate(DocumentEvent e) {
					common();
				}
				private void common(){
					if(planName.getText().length()<5 || planName.getText().length()>45) {
						planName.setBackground(Color.RED);
						planName.grabFocus();
						savePlanButton.setEnabled(false);
						displayPlanParametersButton.setEnabled(false);
					}else{
						planName.setBackground(Color.WHITE);
						savePlanButton.setEnabled(true);
						displayPlanParametersButton.setEnabled(true);
						try{
							controller.setPlanName(planName.getText());
						}catch(IllegalParameterException ipe){
							errorMessage(ipe.getMessage());
						}
					}
				}
	    	});
	    	displayPlanParametersButton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent arg0) {
					controller.setActivePlanningEntity(planName.getText());
					eventList.clearSelection();
				}
	    	});
	    	savePlanButton.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e) {
					dialog.setVisible(true);
				}
	    	});
	    	planNamePanel.setMaximumSize(new Dimension(820,100));
	    	planNamePanel.setPreferredSize(new Dimension(820,120));
	    	planNamePanel.setAlignmentX(Component.LEFT_ALIGNMENT);    	
	    	planLevelSegmentDefPanel.setLayout(new FlowLayout());
	    	
	    	
	    	planNamePanel.add(new JLabel(" Naziv rasporeda:  "));
	    	planNamePanel.add(planName);
	    	planNamePanel.add(displayPlanParametersButton);
	    	planNamePanel.add(savePlanButton);
	    	//planLevelSegmentDefPanel.add(planLevelSegmentNumber);
	    	//planLevelSegmentDefPanel.add(planLevelSegmentNumberInfo);
	    	planLevelSegmentDefPanel.add(identicalSegmentDivision);
	    	planLevelSegmentDefPanel.add(identicalSegmentSequence);
	    	planNamePanel.add(planLevelSegmentDefPanel);
	
	    	getContentPane().add(planNamePanel);
	    }
		
	    /**
	     * Glavni panel događaja i panel s detaljima događaja
	     * @param eventList
	     */
	    private void createEventPanel(final JList eventList){
	    	GridBagConstraints constraints;
	
	    	//Glavni panel za dogadaje
	    	JPanel eventPanel = new JPanel();
	    	eventPanel.setMinimumSize(new Dimension(700,200));
	    	eventPanel.setPreferredSize(new Dimension(700,400));
	    	eventPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
	    	eventPanel.setLayout(new GridBagLayout());
	    	
	    	//Komponente vezane za segmente događaja koje koristi više panela
	    	final JButton newTermButton = new JButton("Dodaj termin");
	    	final JList termList = new JList(); 
	    	
	    	//Stvaranje panela s listom događaja
	    	JPanel eventListPanel = createEventListPanel(eventList, newTermButton, termList);
	    	constraints = new GridBagConstraints();
	    	constraints.gridx=0;
	    	constraints.gridy=0;
	    	constraints.weighty=0.6;
	    	constraints.weightx=0.2;
	    	constraints.fill = GridBagConstraints.BOTH;
	    	eventPanel.add(eventListPanel, constraints);
	    	
	    	//Podpanel s detaljima događaja
	    	JPanel eventDetailPanel = new JPanel(new GridBagLayout()); 
	    	eventDetailPanel.setBorder(BorderFactory.createTitledBorder(null, "Detalji događaja", TitledBorder.DEFAULT_JUSTIFICATION, 
					TitledBorder.DEFAULT_POSITION, new Font(Font.SANS_SERIF, Font.BOLD, 15)));
	    	JPanel eventSubPanel = new JPanel();
	    	eventSubPanel.setLayout(new GridBagLayout());
	       	controller.setEventDetailPanel(eventDetailPanel); //Za postavljanje teksta bordera
	       	
	    	//Stvaranje panela za definiranje max. trajanja događaja unutar perioda
	    	JPanel eventDurationPanel = createEventDurationPanel();
	    	constraints = new GridBagConstraints();
	    	constraints.gridx=0;
	    	constraints.gridy=0;
	    	constraints.weightx=0.1;
	    	constraints.weighty=0.7;
	    	constraints.fill = GridBagConstraints.BOTH;
	    	eventSubPanel.add(eventDurationPanel,constraints);
	
	    	//Stvaranje panela za definiranje odnosa između događaja
	    	JPanel eventRelationPanel = createEventRelationPanel();
	    	constraints = new GridBagConstraints();
	    	constraints.gridx=1;
	    	constraints.gridy=0;
	    	constraints.weightx=0.9;
	    	constraints.weighty=0.7;
	    	constraints.fill = GridBagConstraints.BOTH;
	    	eventSubPanel.add(eventRelationPanel, constraints);
	    	
	    	//Stvaranje panela sa segmentima događaja
	    	JPanel eventSegmentsPanel = createEventSegmentsPanel(termList, newTermButton, eventList);
	    	constraints = new GridBagConstraints();
	    	constraints.gridx=1;
	    	constraints.gridy=1;
	    	constraints.weighty=1.0;
	    	constraints.weightx=0.8;
	    	constraints.fill = GridBagConstraints.BOTH;
	    	eventSubPanel.add(eventSegmentsPanel, constraints);
	
	    	//Podpanel s trajanjem termina
	    	JPanel termDurationPanel = createTermDurationPanel();
	    	constraints = new GridBagConstraints();
	    	constraints.gridx=0;
	    	constraints.gridy=1;
	    	constraints.weighty=1.0;
	    	constraints.weightx=0.2;
	    	constraints.fill = GridBagConstraints.BOTH;
	    	eventSubPanel.add(termDurationPanel, constraints);	    	
	
	    	
	    	LockableUI lockableUI = new LockableUI(new BufferedImageOpEffect(new GrayFilter()));
	    	JXLayer<JComponent> jxPanel = new JXLayer<JComponent>(eventSubPanel, lockableUI);   
	    	lockableUI.setLocked(true);
	    	controller.setEventDetailLockControl(lockableUI);
	    	
	    	constraints = new GridBagConstraints();
	    	constraints.gridx=0;
	    	constraints.gridy=0;
	    	constraints.weightx=1;
	    	constraints.weighty=1;
	    	constraints.fill = GridBagConstraints.BOTH;
	    	eventDetailPanel.add(jxPanel, constraints);
	    	
	    	constraints.gridx=1;
	    	constraints.gridy=0;
	    	constraints.weighty=0.6;
	    	constraints.weightx=0.8;
	    	constraints.fill = GridBagConstraints.BOTH;
	    	eventPanel.add(eventDetailPanel, constraints);
	    	
	
	    	getContentPane().add(eventPanel);
		}
    
	    /**
	     * Stvaranje panela s listom događaja
	     * @return
	     */
		private JPanel createEventListPanel(final JList eventList, final JButton newTermButton, final JList termList){
	    	//-----Podpanel s listom dogadaja-----
	    	JPanel eventListPanel = new JPanel(new GridBagLayout());
	    	GridBagConstraints gbc = new GridBagConstraints();
	    	final DefaultListModel eventModel = new DefaultListModel();
	    	JScrollPane eventScrollPane = new JScrollPane(eventList);
	    	final JTextField eventNameField = new JTextField("Naziv događaja", 10);
	    	final JButton newEventButton = new JButton("Novi događaj");
	    	final JButton deleteEventButton = new JButton("Izbriši događaj");    	
	    	
	    	eventScrollPane.setPreferredSize(new Dimension(150,180));
	    	eventListPanel.setBorder(BorderFactory.createTitledBorder(null, "Događaji rasporeda", TitledBorder.DEFAULT_JUSTIFICATION, 
					TitledBorder.DEFAULT_POSITION, new Font(Font.SANS_SERIF, Font.BOLD, 15)));
	     	eventNameField.addFocusListener(new FocusListener(){
				public void focusGained(FocusEvent e) {
					if(eventNameField.hasFocus()){
						if(eventNameField.getText().equals("Naziv događaja")) eventNameField.setText("");
					}
				}
				public void focusLost(FocusEvent e) {
					if(eventNameField.getText().equals("")) eventNameField.setText("Naziv događaja");
				}
	    	});
	    	eventNameField.getDocument().addDocumentListener(new DocumentListener(){
				public void changedUpdate(DocumentEvent e) {}
				public void insertUpdate(DocumentEvent e) {
					common();
				}
				public void removeUpdate(DocumentEvent e) {
					common();
				}
				private void common(){
					if(eventNameField.getText().length()<5 || eventNameField.getText().length()>45) {
						eventNameField.setBackground(Color.RED);
						eventNameField.grabFocus();
						newEventButton.setEnabled(false);
					}else{
						eventNameField.setBackground(Color.WHITE);
						newEventButton.setEnabled(true);
					}
				}
	    	});
	    	
	    	controller.addViewModel(eventModel, -1);
	    	eventListPanel.setPreferredSize(new Dimension(150,300));
	    	newEventButton.setAlignmentX(Component.LEFT_ALIGNMENT);
	    	newEventButton.setEnabled(false);
	    	
	    	NewEventAction nea = new NewEventAction(eventNameField, eventList, deleteEventButton, newTermButton);
	    	eventNameField.addKeyListener(nea);
	    	newEventButton.addActionListener(nea);
	
	    	eventList.setModel(eventModel);
	    	eventList.getModel().addListDataListener(new ListDataListener(){
				@Override
				public void contentsChanged(ListDataEvent e) {
					common();
				}
				@Override
				public void intervalAdded(ListDataEvent e) {
					common();
				}
				@Override
				public void intervalRemoved(ListDataEvent e) {
					common();
				}
	    		private void common(){
					if(eventList.getModel().getSize()>1) {
	    				controller.getPreconditionAddButton().setEnabled(true);
	    			}else{
	    				controller.getPreconditionAddButton().setEnabled(false);
	    			}
					
	    		}
	    	});
	    	
	    	eventList.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
				public void valueChanged(ListSelectionEvent e) {
					if(controller.isGuiUpdateInProgress()) return;
					
					String selectedValue = (String)eventList.getSelectedValue();
					if(selectedValue!=null) {
						controller.setActivePlanningEntity(selectedValue);
						termList.clearSelection();
					}
				}
	    	});
	    	eventNameField.setAlignmentX(Component.LEFT_ALIGNMENT);
	
	    	
	    	eventList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	    	eventScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
	
	    	deleteEventButton.setEnabled(false);
	    	deleteEventButton.setAlignmentX(Component.LEFT_ALIGNMENT);
	    	deleteEventButton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					if(JOptionPane.showOptionDialog(null, "Jeste li sigurni da želite obrisati cijeli događaj?", 
							"Upozorenje", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, new String[]{"Da", "Ne"} , "Ne")==0){
					
						String selectedEvent = (String)eventList.getSelectedValue();
						try{
							if(selectedEvent!=null) controller.deletePlanEvent(selectedEvent);
						}catch(IllegalParameterException ipe){
							errorMessage(ipe.getMessage());
						}
						if(eventList.getModel().getSize()==0) deleteEventButton.setEnabled(false);
					}
				}
	    	});
	    	
	    	gbc.gridx=0;
	    	gbc.gridy=0;
	    	gbc.fill = GridBagConstraints.BOTH;
	    	eventListPanel.add(newEventButton,gbc);
	    	gbc.gridx=0;
	    	gbc.gridy=1;
	    	gbc.fill = GridBagConstraints.BOTH;
	    	eventListPanel.add(eventNameField,gbc);
	    	gbc.gridx=0;
	    	gbc.gridy=2;
	    	gbc.fill = GridBagConstraints.BOTH;
	    	eventListPanel.add(eventScrollPane,gbc);
	    	gbc.gridx=0;
	    	gbc.gridy=3;
	    	gbc.fill = GridBagConstraints.BOTH;
	    	eventListPanel.add(deleteEventButton,gbc);
	    	
	    	return eventListPanel;
	    }
    
		private class NewEventAction implements ActionListener,KeyListener{

			JTextField eventNameField;
			JList eventList;
			JButton deleteEventButton;
			JButton newTermButton ;
			
			public NewEventAction(JTextField eventNameField, JList eventList, JButton deleteEventButton, JButton newTermButton ){
				this.eventNameField=eventNameField;
				this.eventList=eventList;
				this.deleteEventButton=deleteEventButton;
				this.newTermButton=newTermButton;
			}
			
			@Override
			public void actionPerformed(ActionEvent e) {
				common();
			}

			@Override
			public void keyPressed(KeyEvent e) {
				if(e.getKeyCode()==KeyEvent.VK_ENTER) common();
				
			}

			private void common(){
			if(controller.isGuiUpdateInProgress()) return;
				try{ 
					String eventName = eventNameField.getText();
					controller.addNewPlanEvent(eventName);
					controller.setActivePlanningEntity(eventName);
				}catch(IllegalParameterException ipe){
					errorMessage(ipe.getMessage());
				}

	            int index = eventList.getModel().getSize() - 1;
	            eventList.setSelectedIndex(index);
	            eventList.ensureIndexIsVisible(index);
	            eventNameField.setText("");
	            deleteEventButton.setEnabled(true);
	            newTermButton.setEnabled(true);
	            eventNameField.setText("Naziv događaja");
	            eventList.grabFocus();				
			}
			
			@Override
			public void keyReleased(KeyEvent e) {}
			@Override
			public void keyTyped(KeyEvent e) {}

		}

		/**
	     * Stvaranje panela za definiciju odnosa prema drugim događajima
	     * @return
	     */
	    private JPanel createEventRelationPanel(){
	    	JPanel mainRelationPanel = new JPanel();
	    	final JPanel relationPanel1 = new JPanel();
	    	final JPanel relationPanel2 = new JPanel();
	    	
	    	final JButton addPreconditionButton = new JButton("Dodaj");
	    	final JButton deletePreconditionButton = new JButton("Obriši");
	    	final JButton editPreconditionButton = new JButton("Izmijeni");
	    	final ComboBoxModel preconditionModel = new DefaultComboBoxModel();
	    	final JComboBox preconditionCombo = new JComboBox(preconditionModel);
	    	final JLabel preconditionCount = new JLabel ("Broj preduvjeta: 0");

	    	final EventPreconditionDialog dialog = new EventPreconditionDialog((JFrame)null, "Preduvjeti", true, preconditionCombo);
	    	
	    	addPreconditionButton.setEnabled(false);
	    	deletePreconditionButton.setEnabled(false);
	    	editPreconditionButton.setEnabled(false);
	    	
	    	mainRelationPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
	    	mainRelationPanel.setBorder(BorderFactory.createTitledBorder("Preduvjeti"));
	    	mainRelationPanel.setLayout(new BoxLayout(mainRelationPanel, BoxLayout.PAGE_AXIS));
	    	relationPanel1.setLayout(new BoxLayout(relationPanel1, BoxLayout.LINE_AXIS));
	    	relationPanel1.setAlignmentX(Component.LEFT_ALIGNMENT);
	    	preconditionCombo.setEnabled(false);
	    	preconditionCombo.setAlignmentX(Component.LEFT_ALIGNMENT);
	    	relationPanel1.setAlignmentX(Component.LEFT_ALIGNMENT);
	    	relationPanel2.setAlignmentX(Component.LEFT_ALIGNMENT);
	    	
	    	preconditionCombo.setRenderer(new DurationRenderer());
	    	
	    	controller.setPreconditionModel(preconditionModel);
	    	controller.setPreconditionAddButton(addPreconditionButton);
	    	
	    	preconditionModel.addListDataListener(new ListDataListener(){
				@Override
				public void contentsChanged(ListDataEvent e) {
					common();	
				}
				@Override
				public void intervalAdded(ListDataEvent e) {
					common();
				}
				@Override
				public void intervalRemoved(ListDataEvent e) {
					common();
				}
				private void common(){
					if(preconditionModel.getSize()>0) {
						preconditionCombo.setEnabled(true);
						preconditionCombo.setSelectedIndex(0);
						deletePreconditionButton.setEnabled(true);
						editPreconditionButton.setEnabled(true);
					}else{
						preconditionCombo.getModel().setSelectedItem(null);
						preconditionCombo.setEnabled(false);
						deletePreconditionButton.setEnabled(false);
						editPreconditionButton.setEnabled(false);
					}
					preconditionCount.setText("Broj preduvjeta: " + preconditionModel.getSize());
				}
	    	});
	    	addPreconditionButton.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e) {
					dialog.addPrecondition();
				}
	    	});
	    	editPreconditionButton.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e) {
					dialog.editPrecondition();
				}
	    	});
	    	
	    	deletePreconditionButton.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e) {
					if(JOptionPane.showOptionDialog(null, "Jeste li sigurni da želite izbrisati odabrani preduvjet ", 
							"Upozorenje", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, new String[]{"Da", "Ne"} , "Ne")==0)
					{
						controller.deleteEventPrecondition(preconditionModel.getSelectedItem());
					}else{
						
					}
				}
	    	});
	    	
	    	relationPanel1.add(preconditionCombo);
	    	
	    	relationPanel2.add(addPreconditionButton);
	    	relationPanel2.add(editPreconditionButton);
	    	relationPanel2.add(deletePreconditionButton);
	    	relationPanel2.add(Box.createHorizontalStrut(30));
	    	relationPanel2.add(preconditionCount);
	    	
	    	mainRelationPanel.add(relationPanel1);
	    	mainRelationPanel.add(Box.createVerticalStrut(5));
	    	mainRelationPanel.add(relationPanel2);
	    	
	    	return mainRelationPanel;
	    }
	   
		/**
		 * Dijalog za dodavanje preduvjeta događaja
		 */
		private class EventPreconditionDialog extends JDialog{
			private static final long serialVersionUID = 1L;

			final JButton confirmButton = new JButton("Potvrdi");
			final JButton cancelButton = new JButton("Zatvori");
			final JLabel relationText0 = new JLabel("Prije  mora se dogoditi ");
			JComboBox preconditionCombo = null;
	    	final JRadioButton dayDurationButton = new JRadioButton();
	    	final JRadioButton hourDurationButton = new JRadioButton();
	    	final ButtonGroup buttonGroup = new ButtonGroup();	    	
	    	final ComboBoxModel dayDurationModel = new DefaultComboBoxModel();
	    	final ComboBoxModel hourDurationModel = new DefaultComboBoxModel();
	    	final JComboBox dayDurationCombo = new JComboBox(dayDurationModel);
	    	final JComboBox hourDurationCombo = new JComboBox(hourDurationModel);
	    	final ComboBoxModel otherEventModel  = new DefaultComboBoxModel(); 
	    	final JComboBox otherEventList = new JComboBox(otherEventModel);
	    	
	    	private Precondition preconditionInEditing = null;
	    	private boolean edit = false;
			
			public EventPreconditionDialog(JFrame frm, String name, boolean isModal, final JComboBox preconditionCombo){
				super(frm,name,isModal);
				
				this.preconditionCombo = preconditionCombo;
				JPanel mainPanel = new JPanel();
		    	JPanel relationPanel1 = new JPanel();
		    	JPanel relationPanel2 = new JPanel();
		    	final JLabel relationText1 = new JLabel("s razmakom od najmanje ");
		    	final JDialog thisDialog = this;
		    	
		    	final JLabel noticeLabel = new JLabel(" ");
		    	
		    	dayDurationButton.setEnabled(true);
		    	hourDurationButton.setEnabled(true);
		    	dayDurationCombo.setEnabled(false);
		    	hourDurationCombo.setEnabled(false);
		    	dayDurationCombo.setRenderer(new DurationRenderer());
		    	hourDurationCombo.setRenderer(new DurationRenderer());
		    	buttonGroup.add(dayDurationButton);
		    	buttonGroup.add(hourDurationButton);
		    	mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
		    	relationPanel1.setLayout(new FlowLayout(FlowLayout.LEFT));
		    	relationPanel1.setAlignmentX(Component.LEFT_ALIGNMENT);
		    	relationPanel2.setAlignmentX(Component.LEFT_ALIGNMENT);
		    	relationPanel2.setLayout(new FlowLayout(FlowLayout.LEFT));
		    	relationText0.setAlignmentX(Component.LEFT_ALIGNMENT);
		    	confirmButton.setAlignmentX(Component.LEFT_ALIGNMENT);
		    	otherEventList.setAlignmentX(Component.LEFT_ALIGNMENT);
				noticeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
				noticeLabel.setForeground(new Color(50,205,50));
				
		    	fillTimeModel(dayDurationModel, "days");
		    	fillTimeModel(hourDurationModel, "hours");
		    	
				final FadeOutTimer timer = new FadeOutTimer(100, null);
				timer.setTarget(noticeLabel);
				
		    	controller.setUnconnectedEventModel(otherEventModel);
		    	
		    	hourDurationButton.addChangeListener(new ChangeListener(){
					@Override
					public void stateChanged(ChangeEvent e) {
						if(hourDurationButton.getModel().isSelected()){
							hourDurationCombo.setEnabled(true);
							dayDurationCombo.setEnabled(false);
						}else{
							hourDurationCombo.setEnabled(false);
							dayDurationCombo.setEnabled(true);
						}
					}
		    		
		    	});
		    	hourDurationButton.addActionListener(new ActionListener(){
					@Override
					public void actionPerformed(ActionEvent e) {
						
					}
		    	});
		    	confirmButton.addActionListener(new ActionListener(){
					@Override
					public void actionPerformed(ActionEvent e) {
						if(isEdit()){
							controller.deleteEventPrecondition(getPreconditionInEditing());
							noticeLabel.setText(" Preduvjet promijenjen!");
						}else{
							noticeLabel.setText(" Preduvjet dodan!");
						}
						
						String timeDistance = "";
						if(buttonGroup.isSelected(dayDurationButton.getModel())){
							timeDistance = dayDurationCombo.getSelectedItem() + "d";
						}else{
							timeDistance = hourDurationCombo.getSelectedItem() + "m";
						}
						controller.addEventPrecondition((String)otherEventList.getSelectedItem(), timeDistance);
						
						timer.resetAlpha();
						timer.start();
						
					}
		    	});
		    	cancelButton.addActionListener(new ActionListener(){
					@Override
					public void actionPerformed(ActionEvent e) {
						thisDialog.setVisible(false);
					}
		    	});
		    	otherEventList.getModel().addListDataListener(new ListDataListener(){
					@Override
					public void contentsChanged(ListDataEvent e) {
						common();
					}
					@Override
					public void intervalAdded(ListDataEvent e) {
						common();
					}
					@Override
					public void intervalRemoved(ListDataEvent e) {
						common();
					}
					private void common(){
						if(otherEventList.getModel().getSize()>0) {
							confirmButton.setEnabled(true);
							otherEventList.setEnabled(true);
						}
						else {
							confirmButton.setEnabled(false);
//							DefaultComboBoxModel dcbm = (DefaultComboBoxModel)otherEventList.getModel();
//							dcbm.removeAllElements();
//							dcbm.addElement(new String("Trenutno nijedan događaj ne može biti preduvjet ovom događaju"));
							otherEventList.setEnabled(false);
						}
					}
		    	});

		    	
		    	relationPanel1.add(relationText1);
		    	relationPanel1.add(hourDurationButton);
		    	relationPanel1.add(hourDurationCombo);
		    	relationPanel1.add(Box.createHorizontalGlue());
		    	relationPanel1.add(dayDurationButton);
		    	relationPanel1.add(dayDurationCombo);
		    	
		    	relationPanel2.add(confirmButton);
		    	relationPanel2.add(cancelButton);
		    	
		    	mainPanel.add(relationText0);
		    	mainPanel.add(Box.createVerticalStrut(10));
		    	mainPanel.add(otherEventList);
		    	mainPanel.add(relationPanel1);
		    	mainPanel.add(Box.createVerticalStrut(10));
		    	mainPanel.add(relationPanel2);
		    	mainPanel.add(Box.createVerticalStrut(10));
		    	mainPanel.add(noticeLabel);

		    	mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
				setContentPane(mainPanel);
				pack();
			}
			
			public void addPrecondition(){
				setEdit(false);
				setMessage();
				
				buttonGroup.setSelected(hourDurationButton.getModel(), true);
				hourDurationCombo.setEnabled(true);
				dayDurationCombo.setEnabled(false);
				
				this.setVisible(true);
			}
			
			public void editPrecondition(){
				setEdit(true);
				setMessage();
				
				Precondition p = (Precondition)preconditionCombo.getModel().getSelectedItem();
				setPreconditionInEditing(p);
				
				if(p.getTimeDistance().contains("d")){
					if(!buttonGroup.isSelected(dayDurationButton.getModel())){
						buttonGroup.setSelected(dayDurationButton.getModel(), true);
						dayDurationCombo.setEnabled(true);
						hourDurationCombo.setEnabled(false);
					}
					dayDurationModel.setSelectedItem(p.getTimeDistanceValue());
				}else if(p.getTimeDistance().contains("m")){
					if(!buttonGroup.isSelected(hourDurationButton.getModel())){
						buttonGroup.setSelected(hourDurationButton.getModel(), true);
						dayDurationCombo.setEnabled(false);
						hourDurationCombo.setEnabled(true);
					}
					hourDurationModel.setSelectedItem(p.getTimeDistanceValue());
				}
				otherEventModel.setSelectedItem(p.getEventName());
				otherEventList.setEnabled(true);
				confirmButton.setEnabled(true);
				this.setVisible(true);
			}
			
			private void setMessage(){
				String eventName = controller.getActiveEntityName();
				setTitle("Preduvjeti za " + eventName);
				relationText0.setText("Prije \"" + eventName + "\" mora se dogoditi ");	
			}
			
			public void setVisible(boolean visible){
				super.setVisible(visible);
			}
			
			private void parameterErrorDialog(String msg){
				JOptionPane.showMessageDialog(null, msg, "Greška", JOptionPane.ERROR_MESSAGE);
			}

			public boolean isEdit() {
				return edit;
			}

			public void setEdit(boolean editingInProgress) {
				this.edit = editingInProgress;
			}

			public Precondition getPreconditionInEditing() {
				return preconditionInEditing;
			}

			public void setPreconditionInEditing(Precondition preconditionInEditing) {
				this.preconditionInEditing = preconditionInEditing;
			}

		}
		

		
	    private void fillTimeModel(ComboBoxModel cbm, String cmd){
	    	DefaultComboBoxModel dcbm = (DefaultComboBoxModel)cbm;
	    	dcbm.removeAllElements();
	    	if(cmd.equals("days")){
				for(int i = 1; i<15; i++) {
					dcbm.addElement(i);
				}
	    	}else if(cmd.equals("hours")){
	    		for(int i=15; i<735; i+=15){
	    			dcbm.addElement(i);
	    		}
//				for(int i = 0; i<12; i++) {
//					for(int j = 1; j<4; j++){
//						dcbm.addElement(Integer.toString(i)+"h:"+Integer.toString(j*15)+"m");
//					}
//					dcbm.addElement(Integer.toString(i+1)+"h:00m");
//				}
	    	}
	    }
    
	    /** 
	     * Stvaranje panela za definiranje max. trajanja događaja
	     * @return
	     */
	    private JPanel createEventDurationPanel(){
	    	JPanel durationPanel = new JPanel();
	    	final JCheckBox eventDurationCheckBox = new JCheckBox("Uključi opciju");
	    	JPanel durationDataPanel = new JPanel();
	    	final JRadioButton dayDurationButton = new JRadioButton("u danima");
	    	final JRadioButton hourDurationButton = new JRadioButton("u satima");
	    	ButtonGroup buttonGroup = new ButtonGroup();
	    	final ComboBoxModel durationModel = new DefaultComboBoxModel();
	    	final JComboBox durationCombo = new JComboBox(durationModel);
	    	JPanel comboPanel = new JPanel();
	    	
	    	durationCombo.setRenderer(new DurationRenderer());    	
	    	dayDurationButton.setAlignmentX(Component.LEFT_ALIGNMENT);
	    	hourDurationButton.setAlignmentX(Component.LEFT_ALIGNMENT);
	    	durationDataPanel.setLayout(new BoxLayout(durationDataPanel, BoxLayout.LINE_AXIS));
	    	durationCombo.setEnabled(false);
	    	durationCombo.setMaximumSize(new Dimension(130,30));
	    	durationCombo.setPreferredSize(new Dimension(130,30));
	    	comboPanel.setLayout(new BoxLayout(comboPanel, BoxLayout.LINE_AXIS));
	    	comboPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
	    	buttonGroup.add(dayDurationButton);
	    	buttonGroup.add(hourDurationButton);
	    	durationPanel.setLayout(new BoxLayout(durationPanel, BoxLayout.PAGE_AXIS));
	    	durationPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
	    	durationPanel.setBorder(BorderFactory.createTitledBorder("Max. trajanje događaja"));
	       	durationDataPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
	    	eventDurationCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
	    	dayDurationButton.setEnabled(false);
	    	hourDurationButton.setEnabled(false);
	    	
	    	controller.setEventDurationActivationModel(eventDurationCheckBox.getModel());
	    	controller.setEventDurationModel(durationCombo.getModel());
	    	controller.setEventDurationDays(dayDurationButton.getModel());
	    	controller.setEventDurationHours(hourDurationButton.getModel());
	    	
	    	dayDurationButton.addFocusListener(new FocusListener(){
				public void focusGained(FocusEvent e) {
					fillTimeModel(durationModel, "days");
					controller.updateEventDurationDefinition("COLLECT_DATA");
				}
				public void focusLost(FocusEvent e) {}
	    	});
	    	hourDurationButton.addFocusListener(new FocusListener(){
				public void focusGained(FocusEvent e) {
					fillTimeModel(durationModel, "hours");
					controller.updateEventDurationDefinition("COLLECT_DATA");
				}
				public void focusLost(FocusEvent e) {}
	    	});
	    	eventDurationCheckBox.addItemListener(new ItemListener(){
				public void itemStateChanged(ItemEvent e) {
			        if (e.getStateChange() == ItemEvent.DESELECTED) {
			        	dayDurationButton.setEnabled(false);
			        	hourDurationButton.setEnabled(false);
			        	durationCombo.setEnabled(false);
				        controller.updateEventDurationDefinition("NO_DURATION");
				        eventDurationCheckBox.setText("Uključi opciju");
			        }else{
			        	dayDurationButton.setEnabled(true);
			        	hourDurationButton.setEnabled(true);
			        	dayDurationButton.setSelected(true);
			        	durationCombo.setEnabled(true);
			        	dayDurationButton.requestFocusInWindow();
					    eventDurationCheckBox.setText("Isključi opciju");
			        }
				}
	    	});
	    	durationCombo.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					controller.updateEventDurationDefinition("COLLECT_DATA");
				}
	    	});
	
	    	durationPanel.add(eventDurationCheckBox);
	    	durationDataPanel.add(dayDurationButton);
	    	durationDataPanel.add(hourDurationButton);
	    	durationPanel.add(durationDataPanel);
	    	comboPanel.add(Box.createHorizontalGlue());
	    	comboPanel.add(durationCombo);
	    	comboPanel.add(Box.createHorizontalGlue());
	    	durationPanel.add(comboPanel);
	    	
	    	return durationPanel;
	    }
    
	    /**
	     * Panel sa segmentima događaja
	     */
	    private JPanel createEventSegmentsPanel(final JList termList, final JButton newTermButton, final JList eventList){
	    	//-----Segmenti događaja-----
	    	JPanel eventSegmentsPanel = new JPanel(); //Podpanel s segmentima događaja
	    	eventSegmentsPanel.setLayout(new BoxLayout(eventSegmentsPanel, BoxLayout.LINE_AXIS));
	    	eventSegmentsPanel.setBorder(BorderFactory.createTitledBorder("Raspodjela studenata u događaju"));
	    	
	    	final JPanel randomDistributionPanel = new JPanel();
	    	final JPanel givenDistributionPanel = new JPanel(new GridBagLayout());
	    	
	    	final JTextField minGroupNumberField = new JTextField(3);
			final JTextField maxGroupNumberField = new JTextField(3);
			final JTextField[] minMaxTextFields = {minGroupNumberField, maxGroupNumberField};
	    	
	    	final JTabbedPane selectionPane = new JTabbedPane();
	    	controller.setGroupDistributionSelectionModel(selectionPane.getModel());
	    	selectionPane.setTabPlacement(JTabbedPane.LEFT);
	    	final JCheckBox proizvCBox = new JCheckBox();
	    	proizvCBox.setSelected(true);
	    	final JCheckBox zadanaCBox = new JCheckBox();
	    	zadanaCBox.setSelected(false);
	    	controller.setGroupDistributionModel(new ButtonModel[]{proizvCBox.getModel(), zadanaCBox.getModel()});
	    	final Component proizv = new DistributionTabComponent(proizvCBox, "Proizvoljna");
	    	final int randomDistributionComponentIndex = 0;
	    	final int givenDistributionComponentIndex = 1;
	    	final Component zadana = new DistributionTabComponent(zadanaCBox, "Zadana");
	    	newTermButton.setEnabled(false);
	     	
	    	proizvCBox.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					if(JOptionPane.showOptionDialog(null, "Jeste li sigurni da želite promijeniti način raspodjele " +
							"(gube se trenutne informacije) ?", 
							"Upozorenje", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, new String[]{"Da", "Ne"} , "Ne")==0)
					{
						if(proizvCBox.isSelected()) 
						{ 
							controller.updateEventDistributionType(Definition.RANDOM_DISTRIBUTION);
							zadanaCBox.setSelected(false);
							enabler(minMaxTextFields, true);
							newTermButton.setEnabled(false);
							selectionPane.setSelectedIndex(randomDistributionComponentIndex);
						}
						else {
							controller.updateEventDistributionType(Definition.GIVEN_DISTRIBUTION);
							zadanaCBox.setSelected(true);
							enabler(minMaxTextFields, false);
							selectionPane.setSelectedIndex(givenDistributionComponentIndex);
						}
					}else proizvCBox.setSelected(!proizvCBox.isSelected());
				}
	    	});
	       	zadanaCBox.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					if(JOptionPane.showOptionDialog(null, "Jeste li sigurni da želite promijeniti način raspodjele " +
							"(gube se trenutne informacije) ?", 
							"Upozorenje", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE, null, new String[]{"Da", "Ne"} , "Ne")==0)
					{
						if(zadanaCBox.isSelected()) {
							controller.updateEventDistributionType(Definition.GIVEN_DISTRIBUTION);
							proizvCBox.setSelected(false);
							enabler(minMaxTextFields, false);
							newTermButton.setEnabled(true);
							selectionPane.setSelectedIndex(givenDistributionComponentIndex);
						}
						else { 
							controller.updateEventDistributionType(Definition.RANDOM_DISTRIBUTION);
							enabler(minMaxTextFields, true);
							proizvCBox.setSelected(true);
							selectionPane.setSelectedIndex(randomDistributionComponentIndex);
						}
					}else zadanaCBox.setSelected(!zadanaCBox.isSelected());
				}
	    	});
	    	eventSegmentsPanel.add(selectionPane);
	
	    	//Panel za proizvoljnu raspodjelu
	    	randomDistributionPanel.setLayout(new BoxLayout(randomDistributionPanel, BoxLayout.PAGE_AXIS));
	    	JLabel minGroupNumber = new JLabel("Minimalni broj grupa/termina");
	    	JLabel maxGroupNumber = new JLabel("Maksimalni broj grupa/termina");
	    	JPanel minPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
	    	minPanel.setOpaque(false);
	    	JPanel maxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
	    	maxPanel.setOpaque(false);
	    	controller.setMinMaxLengthFields(minMaxTextFields);
	    	minGroupNumberField.getDocument().addDocumentListener(new DocumentListener(){
				public void changedUpdate(DocumentEvent e) {}
				public void insertUpdate(DocumentEvent e) {
					
					if(!minGroupNumberField.getText().equals("") && !maxGroupNumberField.getText().equals("")){
						if(proizvCBox.isSelected()) controller.updateMinimumTermNumber();
					}
				}
				public void removeUpdate(DocumentEvent e) {
//					if(!minGroupNumberField.getText().equals("") && !maxGroupNumberField.getText().equals("")){
//						if(proizvCBox.isSelected()) controller.updateEventDistributionDefinition("RANDOM");
//					}
				}
	    	});
	    	maxGroupNumberField.getDocument().addDocumentListener(new DocumentListener(){
				public void changedUpdate(DocumentEvent e) {}
				public void insertUpdate(DocumentEvent e) {
					if(!minGroupNumberField.getText().equals("") && !maxGroupNumberField.getText().equals("")){
						if(proizvCBox.isSelected()) controller.updateMaximumTermNumber();
					}
				}
				public void removeUpdate(DocumentEvent e) {
//					if(!minGroupNumberField.getText().equals("") && !maxGroupNumberField.getText().equals("")){
//						if(proizvCBox.isSelected()) controller.updateEventDistributionDefinition("RANDOM");
//					}
				}
	    	});
	    	minPanel.add(minGroupNumber);
	    	minPanel.add(minGroupNumberField);
	    	maxPanel.add(maxGroupNumber);
	    	maxPanel.add(maxGroupNumberField);
	    	randomDistributionPanel.add(minPanel);
	    	randomDistributionPanel.add(maxPanel);
	    	randomDistributionPanel.add(Box.createVerticalStrut(70));
	    	randomDistributionPanel.setOpaque(false);
	    	selectionPane.addTab("Proizvoljna", randomDistributionPanel);
	    	selectionPane.setTabComponentAt(randomDistributionComponentIndex, proizv);
	    	
	    	//Panel za zadanu raspodjelu
	    	GridBagConstraints gbc = new GridBagConstraints();
	    	givenDistributionPanel.setOpaque(false);
	    	JScrollPane termScrollPane = new JScrollPane(termList);
	    	final JButton deleteTermButton = new JButton("Izbriši termin");
	    	final JButton editTermButton = new JButton("Uredi termin");
	    	final DefaultListModel termModel = new DefaultListModel();
	    	termList.setModel(termModel);
	    	
	    	controller.addViewModel(termModel, -2);
	    	
	    	termModel.addListDataListener(new ListDataListener(){
				@Override
				public void contentsChanged(ListDataEvent e) {
					common();
				}
				@Override
				public void intervalAdded(ListDataEvent e) {
					common();
				}
				@Override
				public void intervalRemoved(ListDataEvent e) {
					common();
				}
				private void common(){
					if(termModel.size()==0) {
						deleteTermButton.setEnabled(false);
						newTermButton.setEnabled(true);
					}else{
						deleteTermButton.setEnabled(true);
						editTermButton.setEnabled(true);
						newTermButton.setEnabled(true);
					}
				}
	    		
	    	});

	       	newTermButton.setAlignmentX(Component.LEFT_ALIGNMENT);
	       	newTermButton.setEnabled(false);
	       	newTermButton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					try{
						controller.managePlanEventSegment("ADD", null);
						eventList.clearSelection();
			            int index = termList.getModel().getSize() -1 ;
			            termList.setSelectedIndex(index);
			            deleteTermButton.setEnabled(true);
					}catch(IllegalParameterException ipe){
						errorMessage(ipe.getMessage());
					}
				}
	       	});
	       	gbc.gridx=1;
	       	gbc.gridy=0;
	    	givenDistributionPanel.add(newTermButton,gbc);
	    	termList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	    	termList.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
				public void valueChanged(ListSelectionEvent e) {
					if(controller.isGuiUpdateInProgress()) return;
					
					String selectedValue = (String)termList.getSelectedValue();
					
					if(selectedValue!=null) {
						controller.setActivePlanningEntity(selectedValue);
						eventList.clearSelection();
					}
				}
	    	});
	    	termScrollPane.setMinimumSize(new Dimension(100,100));
	    	termScrollPane.setPreferredSize(new Dimension(100,100));
	    	termScrollPane.setMaximumSize(new Dimension(100,100));
	    	termScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
	    	deleteTermButton.setAlignmentX(Component.LEFT_ALIGNMENT);
	    	deleteTermButton.setEnabled(false);
	    	deleteTermButton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					try{	
						String selectedSegment = (String)termList.getSelectedValue();
						int deletedIndex = termList.getSelectedIndex();
						if(selectedSegment!=null) controller.managePlanEventSegment("DEL", selectedSegment);
						if(termList.getModel().getSize()==0) deleteTermButton.setEnabled(false);
						if(deletedIndex==0) deletedIndex=1;
						termList.setSelectedIndex(deletedIndex-1);
					}catch(IllegalParameterException ipe){
						errorMessage(ipe.getMessage());
					}
				}
	    	});
	    	gbc.gridx=1;
	    	gbc.gridy=1;
	    	givenDistributionPanel.add(deleteTermButton,gbc);
	    	
	    	editTermButton.setAlignmentX(Component.LEFT_ALIGNMENT);
	    	editTermButton.setEnabled(false);
	    	editTermButton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					String selectedSegment = (String)termList.getSelectedValue();
					
					String s = (String)JOptionPane.showInputDialog(
		                    frm,
		                    "Naziv termina:",
		                    "Termin",
		                    JOptionPane.PLAIN_MESSAGE,
		                    null,
		                    null,
		                    selectedSegment);
					try{
						if(s!=null) controller.renameTerm(selectedSegment, s);
					}catch(IllegalParameterException ipe){
						errorMessage(ipe.getMessage());
					}
				}
	    	});
	    	gbc.gridx=1;
	    	gbc.gridy=2;
	    	givenDistributionPanel.add(editTermButton,gbc);
	    	
	    	gbc.gridx=0;
	    	gbc.gridy=0;
	    	gbc.gridheight=3;
	    	givenDistributionPanel.add(termScrollPane,gbc);  
	    	
	    	selectionPane.addTab("Zadana", givenDistributionPanel);
	    	selectionPane.setTabComponentAt(givenDistributionComponentIndex, zadana);
	 
	    	return eventSegmentsPanel;
	    }
    

	    private void enabler (Component[] components, boolean newStatus){
	    	for(Component c: components) c.setEnabled(newStatus);
	    }
    
	    /** 
	     * Stvaranje panela za definiranje trajanja termina
	     * @return
	     */
	    private JPanel createTermDurationPanel(){
	    	JPanel durationPanel = new JPanel();
	    	final ComboBoxModel durationModel = new DefaultComboBoxModel();
	    	final JComboBox durationCombo = new JComboBox(durationModel);
	    	JPanel comboPanel = new JPanel();
	    	
	    	durationCombo.setRenderer(new DurationRenderer());    	
	    	durationCombo.setEnabled(true);
	    	durationCombo.setMaximumSize(new Dimension(130,30));
	    	durationCombo.setPreferredSize(new Dimension(130,30));
	    	comboPanel.setLayout(new BoxLayout(comboPanel, BoxLayout.LINE_AXIS));
	    	comboPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
	    	durationPanel.setLayout(new BoxLayout(durationPanel, BoxLayout.PAGE_AXIS));
	    	durationPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
	    	durationPanel.setBorder(BorderFactory.createTitledBorder("Trajanje termina"));
	
	    	controller.setTermDurationModel(durationCombo.getModel());
	    	
	    	fillTimeModel(durationModel, "hours");
	    	
	    	durationCombo.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					controller.updateTermDurationDefinition();
				}
	    	});
	
	    	comboPanel.add(Box.createHorizontalGlue());
	    	comboPanel.add(durationCombo);
	    	comboPanel.add(Box.createHorizontalGlue());
	    	comboPanel.add(Box.createVerticalGlue());
	    	durationPanel.add(comboPanel);
	    	
	    	return durationPanel;
	    }
    
	     /**
	     * Panel za definiciju parametara entiteta (osobe-vrijeme-lokacije)
	     */
	    private void createParameterPanel(){
	    	
	       	//Paneli za definicije parametara: osobe, vrijeme, lokacije
	       	JPanel definitionPanel = new JPanel();
	       	definitionPanel.setLayout(new GridBagLayout());
	       	definitionPanel.setMaximumSize(new Dimension(820, 400));
	       	definitionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
	       	controller.setDefinitionPanel(definitionPanel);
	
	       	//Model podataka za prikaz liste grupa na kolegiju
	       	ComboBoxModel viewingGroupModel = new DefaultComboBoxModel();
	       	controller.setViewingGroupModel(viewingGroupModel);
	       	
	    	//Panel za definiranje parametara osoba
	       	PeopleDialog peopleDialog = new PeopleDialog((JFrame)null, "Osobe", true, viewingGroupModel);
	       	ParameterPanel peoplePanel = new ParameterPanel(Definition.PEOPLE_DEF_NAME, 
	       			Definition.PEOPLE_DEF, controller, peopleDialog);
	
	    	GridBagConstraints c = new GridBagConstraints(); 
	    	c.gridx=0;
	    	c.gridy=0;
	    	c.weightx=1;
	    	c.weighty=1;
	    	c.fill=GridBagConstraints.BOTH;
	       	definitionPanel.add(peoplePanel, c);
	       	
	       	//Panel za definiranje vremenskih parametara
	       	TimeDialog timeDialog =  new TimeDialog((JFrame)null, "Periodi", true);
	       	ParameterPanel timePanel = new ParameterPanel(Definition.TIME_DEF_NAME, 
	       			Definition.TIME_DEF, controller, timeDialog);
	    	c.gridx=1;
	       	definitionPanel.add(timePanel, c);
	       	
	       	//Panel za definiranje parametara lokacija
	       	LocationDialog locationDialog =  new LocationDialog((JFrame)null, "Lokacije", true);
	       	ParameterPanel locationPanel = new ParameterPanel(Definition.LOCATION_DEF_NAME, 
	       			Definition.LOCATION_DEF, controller, locationDialog);
	    	c.gridx=2;
	       	definitionPanel.add(locationPanel, c);
	      	
	       	getContentPane().add(definitionPanel);
	    }
     
	    private void errorMessage(String msg){
	    	JOptionPane.showMessageDialog(null, msg, "Greška", JOptionPane.ERROR_MESSAGE);
	    }
    

    /**
     * Generički panel za definiciju parametara
     */
    private class ParameterPanel extends JPanel implements ActionListener{
    	
    	private static final long serialVersionUID = 1L;
    	private int panelType;
    	private String panelName;
    	private JLabel parameterCountLabel;
    	private JList parameterList;
    	private JButton removeParameterButton;
    	private PlanningController controller;
    	private JDialog dialog;
    	//Unutarnji panel - za zakljucavanje
    	private JPanel internalPanel;
    	
    	public ParameterPanel(String name, int panelType, PlanningController controller, 
    			JDialog dialog){
    		
    		this.controller=controller;
    		this.panelName=name;
    		this.parameterCountLabel = new JLabel("0 parametara.");
    		this.panelType=panelType;
    		this.dialog=dialog;
    		this.removeParameterButton=new JButton();
    		this.internalPanel = new JPanel();
    		
    		JButton addParameterButton = new JButton("Dodaj parametar");
    		final DefaultListModel parameterModel = new DefaultListModel();
    		parameterList = new JList(parameterModel);
    		JScrollPane parameterScrollPane = new JScrollPane(parameterList);
    		
			setLayout(new BorderLayout());
			parameterCountLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 5, 0));
			
    		if(this.panelType==Definition.LOCATION_DEF) {
    			resolveRenderer(new RoomListRenderer());
    		}
    		else if (this.panelType==Definition.PEOPLE_DEF) {
    			resolveRenderer(new PeopleParameterRenderer());
    		}
    		else if (this.panelType==Definition.TIME_DEF) {
    			resolveRenderer(new PeriodRenderer());
    		}
    		internalPanel.setLayout(new BorderLayout());
    		internalPanel.setPreferredSize(new Dimension(260,190));
    		setBorder(BorderFactory.createCompoundBorder(
                  BorderFactory.createTitledBorder(this.panelName),
                  BorderFactory.createEmptyBorder(5,5,5,5)));
    		addParameterButton.setActionCommand("addParam");
    		addParameterButton.addActionListener(this);
    		internalPanel.add(addParameterButton, BorderLayout.PAGE_START);
    		parameterModel.addListDataListener(new ListDataListener(){
    			public void contentsChanged(ListDataEvent e) {
    				common(); 
    			}
    			public void intervalAdded(ListDataEvent e) {
    				common();
    			}
    			public void intervalRemoved(ListDataEvent e) {
    				common();
    			}
    			private void common(){
    				if(parameterModel.size()==0) removeParameterButton.setEnabled(false);
    				parameterCountLabel.setText("Broj parametara: " + parameterModel.size());
    			}
    		});
    		parameterList.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
				public void valueChanged(ListSelectionEvent e) {
					if(parameterList.getSelectedIndex()!=-1) removeParameterButton.setEnabled(true);
					else removeParameterButton.setEnabled(false);
				}
    		});
    		
    		final JDialog thisdialog = dialog;
    		parameterList.addMouseListener(new MouseListener(){

				@Override
				public void mouseClicked(MouseEvent arg0) {
					if(arg0.getClickCount()==2){
						Object o = parameterList.getSelectedValue();
						final int selectedIndex = parameterList.getSelectedIndex();
						
						IDefinitionParameter pp = (IDefinitionParameter)o;
						Updateable pd = (Updateable)thisdialog;
						
						pd.setParameterToUpdate(pp, new ParameterUpdateCallBack(){
							@Override
							public void parameterUpdated() {
								parameterList.setSelectedIndex(selectedIndex);
								parameterList.repaint();
							}
						});
						thisdialog.setVisible(true);
					}
				}
				@Override
				public void mouseEntered(MouseEvent arg0) {}
				@Override
				public void mouseExited(MouseEvent arg0) {}
				@Override
				public void mousePressed(MouseEvent arg0) {}
				@Override
				public void mouseReleased(MouseEvent arg0) {}
    		});
    		
    		controller.addViewModel(parameterModel, panelType);
    		parameterList.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
    		internalPanel.add(parameterScrollPane, BorderLayout.CENTER);
    		removeParameterButton.setText("Ukloni parametar");	
    		removeParameterButton.setEnabled(false);
    		removeParameterButton.setActionCommand("removeParam");
    		removeParameterButton.addActionListener(this);
    		internalPanel.add(removeParameterButton, BorderLayout.PAGE_END);
    		
           	//Override za zaključavanje
        	LockableUI lockableUI = new LockableUI(new BufferedImageOpEffect(new GrayFilter()));
        	JXLayer<JComponent> lockingPanel = new JXLayer<JComponent>(internalPanel, lockableUI);   
        	lockableUI.setLocked(false);
        	controller.getParameterLocks().put(this.panelType, lockableUI);
        	
        	add(BorderLayout.NORTH, parameterCountLabel);
    		add(BorderLayout.CENTER, lockingPanel);
       	}

    	private void resolveRenderer(JComponent renderer){
			renderer.setToolTipText("Parametar se može izmijeniti dvostrukim klikom miša.");
			parameterList.setCellRenderer((ListCellRenderer)renderer);
    	}
    	
    	public void actionPerformed(ActionEvent e) {
    		String cmd = e.getActionCommand();
    		if(cmd.equals("addParam")) this.dialog.setVisible(true);
    		else if (cmd.equals("removeParam")){
    			//Uklanjanje parametra iz liste i putem kontrolera iz plandata objekta
    			int deletedIndex = parameterList.getMinSelectionIndex();
    			Object[] params = parameterList.getSelectedValues();
    			if(params!=null) System.out.println("params length " + params.length);
    			for(int i=0; i<params.length; i++){
    				IDefinitionParameter param = (IDefinitionParameter)params[i];
    				controller.removeParameter(param);
    			}
    			if(parameterList.getModel().getSize()==0) removeParameterButton.setEnabled(false);
    			if(deletedIndex==0) deletedIndex=1;
    			parameterList.setSelectedIndex(deletedIndex-1);
    			
    			//Implementation for single selection
//    			IDefinitionParameter param = (IDefinitionParameter)parameterList.getSelectedValue();
//    			int deletedIndex = parameterList.getSelectedIndex();
//    			controller.removeParameter(param);
//    			if(parameterList.getModel().getSize()==0) removeParameterButton.setEnabled(false);
//    			if(deletedIndex==0) deletedIndex=1;
//    			parameterList.setSelectedIndex(deletedIndex-1);
    		}
    	}

	}
    
	/**
	 * Dijalog za dodavanje novog parametra o osobama
	 */
	private class PeopleDialog extends JDialog implements Updateable{
		private static final long serialVersionUID = 1L;
		
		private ComboBoxModel viewingGroupModel;
		final JButton addGroupParamButton = new JButton("Dodaj grupu");
		final JButton addPeopleParamButton = new JButton("Dodaj jmbagove");
		final JButton addTeamParamButton = new JButton("Dodaj tim");
		JTabbedPane peopleTabbedPane;
		
		private PeopleParameter parameterToUpdate;
		private ParameterUpdateCallBack parameterUpdateCallback;
		
		final JTextArea teamJmbagList;
		final JTextArea jmbagList;
		
		public PeopleDialog(JFrame owner, String name, boolean isModal, ComboBoxModel vgm){
			super(owner, name, isModal);
			this.viewingGroupModel=vgm;
			
			JPanel mainPanel = new JPanel();
			final JLabel noticeLabel = new JLabel(" ");
			peopleTabbedPane = new JTabbedPane(JTabbedPane.TOP);
			JButton closeButton1 = new JButton("Zatvori");
			JButton closeButton2 = new JButton("Zatvori");
			JButton closeButton3 = new JButton("Zatvori");
			//Pomocni panel za dodavanje liste jmbagova
			JPanel singleUserPanel = new JPanel();			
			jmbagList = new JTextArea();	
			JScrollPane jmbagsScrollPane = new JScrollPane(jmbagList);
			JPanel buttonPanel1 = new JPanel();
			buttonPanel1.setLayout(new BoxLayout(buttonPanel1, BoxLayout.LINE_AXIS));
			//Pomocni panel za dodavanje grupa
			JPanel multiUserPanel = new JPanel();			
			final JComboBox groupList = new JComboBox(viewingGroupModel);
			final GroupListRenderer glr = new GroupListRenderer();		
			JPanel buttonPanel2 = new JPanel();
			buttonPanel2.setLayout(new BoxLayout(buttonPanel2, BoxLayout.LINE_AXIS));
			//Pomocni panel za dodavanje timova
			JPanel teamPanel = new JPanel();
			JLabel teamNameLabel = new JLabel("Naziv tima: ");
			final JTextField teamName = new JTextField(15);
			JLabel assistantNameLabel = new JLabel("Asistent: ");
			final JTextField assistantName = new JTextField(15);
			JLabel teamJmbagsLabel = new JLabel("Studenti u timu (1 redak = 1 jmbag)");
			teamJmbagList = new JTextArea();	
			JScrollPane teamJmbagsScrollPane = new JScrollPane(teamJmbagList);
			JPanel buttonPanel3 = new JPanel();
			buttonPanel3.setLayout(new BoxLayout(buttonPanel3, BoxLayout.LINE_AXIS));
			
			final FadeOutTimer timer = new FadeOutTimer(100, null);
			timer.setTarget(noticeLabel);
			
			mainPanel.setLayout(new BoxLayout(mainPanel,BoxLayout.PAGE_AXIS));
			peopleTabbedPane.setAlignmentX(Component.LEFT_ALIGNMENT);
			noticeLabel.setForeground(new Color(50,205,50));
			noticeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
			
			//Team panel
			addTeamParamButton.setAlignmentX(Component.LEFT_ALIGNMENT);
			teamPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
			teamPanel.setLayout(new BoxLayout(teamPanel, BoxLayout.PAGE_AXIS));
			teamPanel.setBorder(BorderFactory.createCompoundBorder(
	                  BorderFactory.createTitledBorder("Tim studenata"),
	                  BorderFactory.createEmptyBorder(5,5,5,5)));
			teamJmbagList.setRows(5);
			teamJmbagsScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
			
			//Single student panel
			addPeopleParamButton.setAlignmentX(Component.LEFT_ALIGNMENT);
			singleUserPanel.setAlignmentX(0);
			singleUserPanel.setLayout(new BoxLayout(singleUserPanel,BoxLayout.PAGE_AXIS));
			singleUserPanel.setBorder(BorderFactory.createCompoundBorder(
	                  BorderFactory.createTitledBorder("Lista JMBAG-ova studenata (1 redak = 1 jmbag)"),
	                  BorderFactory.createEmptyBorder(5,5,5,5)));
			jmbagList.setRows(5);
			jmbagsScrollPane.setAlignmentX(Component.LEFT_ALIGNMENT);
			
			//Group panel
			addGroupParamButton.setAlignmentX(Component.LEFT_ALIGNMENT);
			multiUserPanel.setLayout(new BoxLayout(multiUserPanel,BoxLayout.PAGE_AXIS));
			multiUserPanel.setPreferredSize(new Dimension(300,50));
			multiUserPanel.setAlignmentX(0);
			multiUserPanel.setBorder(BorderFactory.createCompoundBorder(
	                  BorderFactory.createTitledBorder("Grupe na kolegiju"),
	                  BorderFactory.createEmptyBorder(5,5,5,5)));
			glr.setPreferredSize(new Dimension(250, 20));
			groupList.setRenderer(glr);
			groupList.setMaximumSize(new Dimension(300,30));
			groupList.setAlignmentX(Component.LEFT_ALIGNMENT);
	
			final JDialog thisDialog = this;
			closeButton1.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e) {
					thisDialog.setVisible(false);
				}
			});
			closeButton2.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e) {
					thisDialog.setVisible(false);
				}
			});
			closeButton3.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e) {
					thisDialog.setVisible(false);
				}
			});
			viewingGroupModel.addListDataListener(new ListDataListener(){
				public void contentsChanged(ListDataEvent e) { 
					common();
				}
				public void intervalAdded(ListDataEvent e) {
					common();
				}
				public void intervalRemoved(ListDataEvent e) {
					common();
				}
				private void common(){
					if(viewingGroupModel.getSize()>0){
						addGroupParamButton.setEnabled(true);
						groupList.setEnabled(true);
					}
					noticeLabel.setText(" ");
				}
			});
			addPeopleParamButton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					String params = jmbagList.getText();
					if(parameterToUpdate==null) addNewParam(params);
					else updateParam(params);
				}
				
				private void addNewParam(String params){
					String[] jmbags = params.split("\\n");
					
					try{
						//1. Priprema liste parametara kako bi se provjerila valjanost
						//svih parametara prije dodavanja u plan
						List<PeopleParameter> peopleParams = new ArrayList<PeopleParameter>();
						for(String s : jmbags){
							peopleParams.add(new PeopleParameter(s, false));
						}	
						
						//Ako su svi parametri u redu sada ih se dodaje u plan
						for(PeopleParameter pp : peopleParams){
							controller.addParameter(pp);
						}	
						
						triggerTimer(" Dodano " + peopleParams.size() + " JMBAG-a!");
					
						jmbagList.setText("");
					}catch(IllegalParameterException ipe){
						parameterErrorDialog(ipe.getMessage());
					}
				}
				
				private void updateParam(String param){
					try{
						//Pokusa se stvoriti novi parametar - to je implicitni test na greske
						new PeopleParameter(param, false);
						//Ako je sve OK
						parameterToUpdate.setJmbag(param);
						parameterUpdateCallback.parameterUpdated();
						
						triggerTimer(" JMBAG izmijenjen!");
						
						jmbagList.setText("");
						
					}catch(IllegalParameterException ipe){
						parameterErrorDialog(ipe.getMessage());
					}
				}
				
				private void triggerTimer(String msg){
					noticeLabel.setText(msg);
					timer.resetAlpha();
					timer.start();
				}
			});
			addGroupParamButton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					PeopleParameter param = (PeopleParameter)groupList.getSelectedItem();
					if(parameterToUpdate==null) addNewGroup(param);
					else updateGroup(param);
				}
				
				private void updateGroup(PeopleParameter param){
					parameterToUpdate.setGroupID(param.getGroupID());
					parameterToUpdate.setGroupName(param.getGroupName());
					parameterToUpdate.setGroupRelativePath(param.getGroupRelativePath());
					
					parameterUpdateCallback.parameterUpdated();
					
					noticeLabel.setText(" Grupa izmijenjena!");
					timer.resetAlpha();
					timer.start();
				}
				
				private void addNewGroup(PeopleParameter param){
					try{
						controller.addParameter(param);
						if(viewingGroupModel.getSize()==0) {
							addGroupParamButton.setEnabled(false);
							groupList.setEnabled(false);
						}
						noticeLabel.setText(" Grupa dodana!");
						timer.resetAlpha();
						timer.start();
					}catch(IllegalParameterException ipe){
						parameterErrorDialog(ipe.getMessage());
					}
				}
			});
			addTeamParamButton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e){
					String params = teamJmbagList.getText();
					String[] jmbags = params.split("\\n");
					try{
						TeamParameter tp = new TeamParameter(teamName.getText(), assistantName.getText());
						for(String s : jmbags){
							tp.addTeamMember(s);
						}
						controller.addParameter(tp);
						noticeLabel.setText(" Parametar dodan!");
						timer.resetAlpha();
						timer.start();
						teamName.setText("");
						assistantName.setText("");
						teamJmbagList.setText("");
					}catch(IllegalParameterException ipe){
						parameterErrorDialog(ipe.getMessage());
					}
				}
			});
			//Single user panel
			buttonPanel1.setAlignmentX(Component.LEFT_ALIGNMENT);
			closeButton1.setAlignmentX(Component.RIGHT_ALIGNMENT);
			addPeopleParamButton.setAlignmentX(Component.LEFT_ALIGNMENT);
			singleUserPanel.add(jmbagsScrollPane);
			buttonPanel1.add(addPeopleParamButton);
			buttonPanel1.add(Box.createHorizontalGlue());
			buttonPanel1.add(closeButton1);
			singleUserPanel.add(buttonPanel1);
			
			//Group panel
			buttonPanel2.setAlignmentX(Component.LEFT_ALIGNMENT);
			closeButton2.setAlignmentX(Component.RIGHT_ALIGNMENT);
			addGroupParamButton.setAlignmentX(Component.LEFT_ALIGNMENT);			
			multiUserPanel.add(groupList);
			multiUserPanel.add(Box.createVerticalStrut(55));			
			buttonPanel2.add(addGroupParamButton);
			buttonPanel2.add(Box.createHorizontalGlue());
			buttonPanel2.add(closeButton2);
			multiUserPanel.add(buttonPanel2);
			
			//Team panel
			buttonPanel3.setAlignmentX(Component.LEFT_ALIGNMENT);
			closeButton3.setAlignmentX(Component.RIGHT_ALIGNMENT);
			addTeamParamButton.setAlignmentX(Component.LEFT_ALIGNMENT);
			teamPanel.add(teamNameLabel);
			teamPanel.add(teamName);
			teamPanel.add(assistantNameLabel);
			teamPanel.add(assistantName);
			teamPanel.add(teamJmbagsLabel);
			teamPanel.add(teamJmbagsScrollPane);
			teamPanel.add(Box.createVerticalStrut(55));
			buttonPanel3.add(addTeamParamButton);
			buttonPanel3.add(Box.createHorizontalGlue());
			buttonPanel3.add(closeButton3);
			teamPanel.add(buttonPanel3);
			
			//People dialog
			peopleTabbedPane.addTab("Grupe studenata", multiUserPanel);
			peopleTabbedPane.addTab("Individualni studenti", singleUserPanel);
			//peopleTabbedPane.addTab("Timovi studenata", teamPanel);
			mainPanel.add(peopleTabbedPane);
			mainPanel.add(Box.createVerticalStrut(5));
			mainPanel.add(noticeLabel);
			mainPanel.add(Box.createVerticalStrut(5));
			
			this.setContentPane(mainPanel);
			this.pack();
		}
		
		public void setVisible(boolean visible){
			resetDialog();
			
			//Normalno dodavanje parametara
			if(this.parameterToUpdate==null){
				if(viewingGroupModel.getSize()==0) addGroupParamButton.setEnabled(false);
				else addGroupParamButton.setEnabled(true);
			//Izmjena parametara
			}else{
				if(parameterToUpdate.isGroup()){
					peopleTabbedPane.setSelectedIndex(0);
					peopleTabbedPane.setEnabledAt(1, false);
					addGroupParamButton.setText("Izmijeni grupu");
					
				}else{
					peopleTabbedPane.setSelectedIndex(1);
					peopleTabbedPane.setEnabledAt(0, false);
					jmbagList.setText(parameterToUpdate.getJmbag());
					addPeopleParamButton.setText("Izmijeni JMBAG");
				}
			}
			//Ciscenje eventualno mijenjanog parametra
			if(!visible) this.parameterToUpdate=null;
			
			super.setVisible(visible);
		}
		
		private void resetDialog(){
			addGroupParamButton.setText("Dodaj grupu");
			addPeopleParamButton.setText("Dodaj jmbagove");
			addTeamParamButton.setText("Dodaj tim");
			teamJmbagList.setText("");
			jmbagList.setText("");
			peopleTabbedPane.setEnabledAt(0, true);
			peopleTabbedPane.setEnabledAt(1, true);
		}
		
		private void parameterErrorDialog(String msg){
			JOptionPane.showMessageDialog(null, msg, "Greška", JOptionPane.ERROR_MESSAGE);
		}
		
		
		public void setParameterToUpdate(IDefinitionParameter pp, ParameterUpdateCallBack cb){
			this.parameterToUpdate=(PeopleParameter)pp;
			this.parameterUpdateCallback=cb;
		}
	}
	
	/**
	 * Dijalog za dodavanje novog parametra o terminima
	 */
	private class TimeDialog extends JDialog implements Updateable{
		private static final long serialVersionUID = 1L;

		private TimeParameter parameterToUpdate;
		private ParameterUpdateCallBack parameterUpdateCallback;
		
		JButton addTimeParamButton;
		final JTextField periodStartField;
		final JTextField periodEndField;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		
		public TimeDialog(JFrame frm, String name, boolean isModal){
			super(frm,name,isModal);
			
			JPanel mainPanel = new JPanel();
			//Panel za dodavanje termina
			JPanel timePanel = new JPanel();	
			final JLabel noticeLabel= new JLabel(" ");
			JLabel periodStartLabel = new JLabel("Početak:");	
			
			Date d = new Date();
			
			periodStartField = new JTextField(sdf.format(d), 10);
			JLabel periodEndLabel = new JLabel("Kraj:");	
			periodEndField = new JTextField(sdf.format(d), 10);
			JLabel correctFormat = new JLabel("Ispravni format:  yyyy-mm-dd hh:mm ");
//			final String dateMask = "????-??-?? ??:??";
//			final JCheckBox anyDateCheckBox = new JCheckBox("Datum nebitan");
			//Pomocni panel za gumbe
			JPanel buttonPanel = new JPanel();
			buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
			addTimeParamButton = new JButton("Dodaj period!");
			JButton closeButton = new JButton("Zatvori");
			final JDialog thisDialog = this;
			
			buttonPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
			addTimeParamButton.setAlignmentX(Component.LEFT_ALIGNMENT);
			closeButton.setAlignmentX(Component.RIGHT_ALIGNMENT);
			noticeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
			noticeLabel.setForeground(new Color(50,205,50));

			final FadeOutTimer timer = new FadeOutTimer(100, null);
			timer.setTarget(noticeLabel);

			mainPanel.setLayout(new BoxLayout(mainPanel,BoxLayout.PAGE_AXIS));
			timePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
			timePanel.setLayout(new BoxLayout(timePanel,BoxLayout.PAGE_AXIS));
			timePanel.setBorder(BorderFactory.createCompoundBorder(
	                 BorderFactory.createTitledBorder("Unos perioda"),
	                 BorderFactory.createEmptyBorder(5,5,5,5)));
			buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);

			addTimeParamButton.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e) {
					String periodStart = periodStartField.getText();
					String periodEnd = periodEndField.getText();
					if(parameterToUpdate==null) addNewParam(periodStart, periodEnd);
					else updateParam(periodStart, periodEnd);
				}
				
				private void addNewParam(String start, String end){
					try{
						IDefinitionParameter timeParam = new Definition.TimeParameter(start+"#"+end);
						controller.addParameter(timeParam);
						triggerTimer(" Parametar dodan!");
						
					}catch(IllegalParameterException ipe){
						parameterErrorDialog(ipe.getMessage());
					}
				}
				
				private void updateParam(String start, String end){
					try{
						//Test ispravnosti novih podataka
						new Definition.TimeParameter(start+"#"+end);
						
						parameterToUpdate.setFromStamp(start);
						parameterToUpdate.setToStamp(end);
						parameterUpdateCallback.parameterUpdated();
						
						triggerTimer(" Parametar izmijenjen!");
					
					}catch(IllegalParameterException ipe){
						parameterErrorDialog(ipe.getMessage());
					}
				}
				
				private void triggerTimer(String msg){
					noticeLabel.setText(msg);
					timer.resetAlpha();
					timer.start();
				}
			});
			closeButton.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e) {
					thisDialog.setVisible(false);
				}
			});			
			timePanel.add(periodStartLabel);
			timePanel.add(periodStartField);
			timePanel.add(periodEndLabel);
			timePanel.add(periodEndField);
			timePanel.add(correctFormat);
//			timePanel.add(anyDateCheckBox);
			mainPanel.add(timePanel);
			buttonPanel.add(addTimeParamButton);
			buttonPanel.add(Box.createHorizontalGlue());
			buttonPanel.add(closeButton);
			mainPanel.add(buttonPanel);
			mainPanel.add(noticeLabel);
			mainPanel.add(Box.createVerticalStrut(5));

			setContentPane(mainPanel);
			this.pack();
		}
		
		private void parameterErrorDialog(String msg){
			JOptionPane.showMessageDialog(null, msg, "Greška", JOptionPane.ERROR_MESSAGE);
		}
		
		public void setVisible(boolean visible){
			addTimeParamButton.setText(" Dodaj period!");
			
			if(parameterToUpdate!=null){
				addTimeParamButton.setText(" Izmjeni period!");
				periodStartField.setText(parameterToUpdate.getFromDateTimeStamp());
				periodEndField.setText(parameterToUpdate.getToDateTimeStamp());
			}else{
				Date d = new Date(); 
				periodStartField.setText(sdf.format(d));
				periodEndField.setText(sdf.format(d));
			}
			
			//Ciscenje eventualno mijenjanog parametra
			if(!visible) this.parameterToUpdate=null;
			
			super.setVisible(visible);
		}
		
		public void setParameterToUpdate(IDefinitionParameter pp, ParameterUpdateCallBack cb){
			this.parameterToUpdate=(TimeParameter)pp;
			this.parameterUpdateCallback=cb;
		}
	}
	
	/**
	 * Dijalog za dodavanje novog parametra o lokacijama
	 */
	private class LocationDialog extends JDialog implements Updateable{
		private static final long serialVersionUID = 1L;
		
		ListModel viewingRoomModel = new DefaultListModel();
		final JButton addLocationButton = new JButton("Dodaj odabrane prostorije");
		final JList roomList;
		
		private RoomParameter parameterToUpdate;
		private ParameterUpdateCallBack parameterUpdateCallback;
		
		public LocationDialog(JFrame frm, String name, boolean isModal){
			super(frm,name,isModal);
			
			JPanel mainPanel = new JPanel();
			//Panel za odabir pocetnog kapaciteta prostorije ovisno o aktivnosti
			JPanel initCapacityPanel = new JPanel();
			JLabel initCapLabel = new JLabel("Prostorije se koriste za ");
			final JComboBox activityCombo = new JComboBox(Definition.activityTypes);
			final JLabel noticeLabel= new JLabel(" ");
			//Pomocni panel za dodavanje lokacija
			JPanel locationPanel = new JPanel();
			roomList = new JList(viewingRoomModel);
			final JScrollPane roomPane = new JScrollPane(roomList);
			//Panel za kapacitete
			JPanel capacityPanel = new JPanel();
			capacityPanel.setBorder(BorderFactory.createTitledBorder("Promjena početnih kapaciteta"));
			JLabel capacityLabel = new JLabel("  Kapacitet: ");
			JLabel capacityInfoLabel = new JLabel("   (za sve odabrane prostorije)");
			final JTextField capacityField = new JTextField(3);						
			//Pomocni panel za gumbe
			JPanel buttonPanel = new JPanel();
			JButton addCompletedButton = new JButton("Zatvori");
			final JDialog thisDialog = this;			

			controller.setViewingRoomModel(viewingRoomModel);			

			buttonPanel.setLayout(new BoxLayout(buttonPanel,BoxLayout.LINE_AXIS));
			buttonPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
			buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
			
			noticeLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
			noticeLabel.setForeground(new Color(50,205,50));
			
			mainPanel.setLayout(new BoxLayout(mainPanel,BoxLayout.PAGE_AXIS));
			
			initCapacityPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
			initCapacityPanel.setLayout(new BoxLayout(initCapacityPanel, BoxLayout.LINE_AXIS));
			initCapacityPanel.setBorder(BorderFactory.createCompoundBorder(
						BorderFactory.createTitledBorder("Početni kapaciteti prostorija"),
						BorderFactory.createEmptyBorder(5,5,5,5)));
			
			activityCombo.setMaximumSize(new Dimension(120,45));
			activityCombo.setRenderer(new ActivityTypeRenderer());
			
			locationPanel.setLayout(new BoxLayout(locationPanel, BoxLayout.PAGE_AXIS));
			locationPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
			locationPanel.setBorder(BorderFactory.createCompoundBorder(
	                  BorderFactory.createTitledBorder("Prostorije na FER-u"),
	                  BorderFactory.createEmptyBorder(5,5,5,5)));
			
			
			roomList.setCellRenderer(new RoomListRenderer());			
			
			capacityPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
			capacityPanel.setLayout(new BoxLayout(capacityPanel, BoxLayout.LINE_AXIS));
			
			addLocationButton.setEnabled(false);
			
			activityCombo.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					controller.refreshViewingRoomModel((Integer)activityCombo.getSelectedItem());
				}
			});
			
			final FadeOutTimer timer = new FadeOutTimer(100, null);
			timer.setTarget(noticeLabel);

			roomList.getSelectionModel().addListSelectionListener(new ListSelectionListener(){
				@Override
				public void valueChanged(ListSelectionEvent e) {
					if(roomList.getSelectedIndex()!=-1) addLocationButton.setEnabled(true);
					else addLocationButton.setEnabled(false);
				}
			});
			addLocationButton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					if(parameterToUpdate==null) addNewLocationParameter();
					else updateLocationParameter();
				}
				
				private void updateLocationParameter(){
					Object selected = roomList.getSelectedValue();
					if(selected==null) return;
					RoomParameter selectedRoom = (RoomParameter)selected;
					String overrideCapacity = capacityField.getText();
					resolveRoom(selectedRoom, overrideCapacity);
					
					parameterToUpdate.setID(selectedRoom.getId());
					parameterToUpdate.setName(selectedRoom.getName());
					parameterToUpdate.setActualCapacity(selectedRoom.getActualCapacity());
					
					triggerTimer(" Parametar izmijenjen!");
					capacityField.setText("");
				}
				
				private void addNewLocationParameter(){
					int lowestSelectedIndex = roomList.getSelectedIndex();
					Object[] selected = roomList.getSelectedValues();
//					String[] selectedRooms = new String[selected.length];
					String overrideCapacity = capacityField.getText();
//					StringBuilder sb = new StringBuilder();
					for(int i = 0; i<selected.length; i++){
						RoomParameter room = (RoomParameter)selected[i];
						resolveRoom(room, overrideCapacity);
						
						try{
							controller.addParameter(room);
							triggerTimer(" Parametar dodan!");
						}catch(IllegalParameterException ipe){
							parameterErrorDialog(ipe.getMessage());
						}
					}

					capacityField.setText("");
					if(viewingRoomModel.getSize()==0) addLocationButton.setEnabled(false);
					else{
						if(lowestSelectedIndex==0) lowestSelectedIndex=1;
						roomList.setSelectedIndex(lowestSelectedIndex-1);
					}
				}
				
				private void resolveRoom(RoomParameter room, String overrideCapacity){
					if(overrideCapacity!=null && overrideCapacity.length()>0){
						try{
							room.setActualCapacity(Integer.parseInt(overrideCapacity));
						}catch(NumberFormatException nfe){
							errorMessage("Kapacitet mora biti broj!");
							capacityField.setText("");
							return;
						}
					}else{
						room.updateActualCapacityToSelected();
					}
				}
				
				private void triggerTimer(String msg){
					noticeLabel.setText(msg);
					timer.resetAlpha();
					timer.start();
				}
			});
			addCompletedButton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					thisDialog.setVisible(false);
				}
			});

			
			initCapacityPanel.add(initCapLabel);
			initCapacityPanel.add(activityCombo);
			initCapacityPanel.add(Box.createHorizontalGlue());
			mainPanel.add(initCapacityPanel);
			locationPanel.add(roomPane);
			mainPanel.add(locationPanel);			
			capacityPanel.add(capacityLabel);
			capacityPanel.add(capacityField);
			capacityPanel.add(capacityInfoLabel);
			mainPanel.add(Box.createVerticalStrut(5));
			mainPanel.add(capacityPanel);
			buttonPanel.add(addLocationButton);
			buttonPanel.add(Box.createHorizontalGlue());
			buttonPanel.add(addCompletedButton);
			mainPanel.add(Box.createVerticalStrut(5));
			mainPanel.add(buttonPanel);
			mainPanel.add(noticeLabel);
			mainPanel.add(Box.createVerticalStrut(5));
			
			setContentPane(mainPanel);
			pack();
		}
		
		public void setVisible(boolean visible){
			if(visible) resetDialog();
			
			if(parameterToUpdate!=null){
				addLocationButton.setText("Izmijeni parametar!");
				roomList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
			}
			super.setVisible(visible);
		}
		
		public void setParameterToUpdate(IDefinitionParameter pp, ParameterUpdateCallBack cb){
			this.parameterToUpdate=(RoomParameter)pp;
			this.parameterUpdateCallback=cb;
		}
		
		private void resetDialog(){
			addLocationButton.setText("Dodaj odabrane prostorije!");
			roomList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		}
		
		private void parameterErrorDialog(String msg){
			JOptionPane.showMessageDialog(null, msg, "Greška", JOptionPane.ERROR_MESSAGE);
		}
	}
	
	private class FadeOutTimer extends Timer{

		private static final long serialVersionUID = 1L;
		private int alpha = 255;
		private Component target= null;
		
		public FadeOutTimer(int delay, ActionListener listener) {
			super(delay, listener);
			final Timer tm = this;
			this.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e) {
		        	
		        	Color c = target.getForeground();
		            alpha -= 10;
		            if (alpha < 0) alpha = 0;
		            Color newc = new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha);
		            target.setForeground(newc);
		            if (alpha == 0){
		                tm.stop();
		                alpha=255;
		            }
		        }
			});
		}
		
		public void resetAlpha(){
			this.alpha=255;
		}
		public void setTarget(Component target){
			this.target=target;
		}
	}
	
	/**
	 * Dijalog za obavljanje završnih radnji - validacija, pohrana, pokretanje izrade
	 */
	private class FinalDialog extends JDialog{
		private static final long serialVersionUID = 1L;
		
		private JTextPane validationStatusPane;
		private JButton runButton;
		private JLabel saveStatusLabel;
		private JButton cancelButton;
		
		public FinalDialog(JFrame frm, String name, boolean isModal){
			super(frm,name,isModal);
			
			/* Panel locks */
			final LockableUI savePanelLock = new LockableUI(new BufferedImageOpEffect(new GrayFilter()));
			final LockableUI schedulerPanelLock = new LockableUI(new BufferedImageOpEffect(new GrayFilter()));
			
			runButton = new JButton("Pokreni izradu!");
			cancelButton = new JButton("Odustani");
			
			JPanel mainPanel = new JPanel();
			mainPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
			mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
			//mainPanel.setPreferredSize(new Dimension(500,260));
			mainPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
			setResizable(true);
			
			/* Validation panel */
			JPanel validationPanel = new JPanel();
			validationPanel.setLayout(new BoxLayout(validationPanel, BoxLayout.Y_AXIS));
			validationPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
			
			JLabel planValidationTitleLabel = new JLabel("1. Provjera ispravnosti plana");
			planValidationTitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
			planValidationTitleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 19));
			planValidationTitleLabel.setForeground(Color.GRAY);
			
			JLabel statusLabel = new JLabel("Statusne informacije: ");
			statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
			statusLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
			statusLabel.setForeground(Color.GRAY);
			
			JButton validatePlanButton = new JButton("Provjeri ispravnost plana!");
			validatePlanButton.setAlignmentX(Component.LEFT_ALIGNMENT);
			
			validatePlanButton.addActionListener(new ActionListener(){
				@Override
				public void actionPerformed(ActionEvent e) {
					ValidationResult.clear();
					controller.validatePlan();
					if(ValidationResult.isSuccess()){
						validationStatusPane.setText("<html><font color=\"green\" > <b>Plan je ispravan! </b></font><br>");
						validationStatusPane.setCaretPosition(0);
						savePanelLock.setLocked(false);
					}
					else
					{
						StringBuffer resultString = new StringBuffer();
						resultString.append("<html>");
						resultString.append("<font color=\"red\" > <b>Plan je neispravan! </b></font><br><br>");
						resultString.append("<font color=\"black\">");
						for(String s : ValidationResult.getMessages()) resultString.append(s + "<br>");
						resultString.append("</font>");
						
						validationStatusPane.setText(resultString.toString());
						validationStatusPane.setCaretPosition(0);
						savePanelLock.setLocked(true);
						schedulerPanelLock.setLocked(true);
					}
				}
			});
			
			validationStatusPane = new JTextPane();
			validationStatusPane.setAlignmentX(Component.LEFT_ALIGNMENT);
			validationStatusPane.setContentType("text/html");
			validationStatusPane.setEditable(false);
			validationStatusPane.setEnabled(true);
			
			JScrollPane infoPane = new JScrollPane(validationStatusPane);
			infoPane.setPreferredSize(new Dimension(500,90));
			infoPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
			infoPane.setAlignmentX(Component.LEFT_ALIGNMENT);
			
			
			validationPanel.add(planValidationTitleLabel);
			validationPanel.add(Box.createVerticalStrut(10));
			validationPanel.add(validatePlanButton);
			validationPanel.add(Box.createVerticalStrut(10));
			validationPanel.add(statusLabel);
			validationPanel.add(infoPane);
			validationPanel.add(Box.createVerticalStrut(10));
			
			/* Save panel */
			JPanel savePanel = new JPanel();
			savePanel.setLayout(new BoxLayout(savePanel, BoxLayout.Y_AXIS));
			savePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
			
			JLabel saveTitleLabel = new JLabel("2. Pohrana plana");
			saveTitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
			saveTitleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 19));
			saveTitleLabel.setForeground(Color.GRAY);
			
			saveStatusLabel = new JLabel(" ");
			controller.setInfoLabel(saveStatusLabel);
			saveStatusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);

			JLabel saveReadyCheckLabel = new JLabel("Ako ste spremni za pohranu plana pritisnite gumb:");
			saveReadyCheckLabel.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 11));
			saveReadyCheckLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
			
			final JButton saveButton = new JButton("Pohrani plan!");
			saveButton.setAlignmentX(Component.LEFT_ALIGNMENT);

			savePanel.add(saveTitleLabel);
			savePanel.add(Box.createVerticalStrut(10));
			savePanel.add(saveReadyCheckLabel);
			savePanel.add(Box.createVerticalStrut(10));
			savePanel.add(saveButton);
			savePanel.add(Box.createVerticalStrut(10));
			savePanel.add(saveStatusLabel);
			savePanel.add(Box.createVerticalStrut(10));
			
			
			saveButton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
					saveButton.setText("Ponovo pohrani plan!");
					saveButton.setEnabled(false);
					saveStatusLabel.setText("<html>Pričekajte trenutak, pohrana plana u tijeku.");
					SwingWorker<SimpleGenericResult, Void> planSaver = new SwingWorker<SimpleGenericResult, Void>() {
					    @Override
					    public SimpleGenericResult doInBackground() {
							return controller.savePlan();
					    }

					    @Override
					    public void done() {
					        try {
					        	SimpleGenericResult result = get();
					            if(result.getResultState()==SimpleGenericResult.SUCCESS) schedulerPanelLock.setLocked(false);
					            else if(result.getResultState()==SimpleGenericResult.FAILURE) schedulerPanelLock.setLocked(true);
					            saveStatusLabel.setText(result.getMessages().get(0));
					            saveButton.setEnabled(true);
					        } catch (InterruptedException ignore) {}
					        catch (java.util.concurrent.ExecutionException e) {
					            e.printStackTrace();
					        }
					    }
					};
					planSaver.execute();
				}
			});
			
			saveButton.setAlignmentX(Component.LEFT_ALIGNMENT);
			
			
			/* Get scheduler panel */
			JPanel schedulerAcquisitionPanel = new JPanel();
			schedulerAcquisitionPanel.setLayout(new BoxLayout(schedulerAcquisitionPanel, BoxLayout.PAGE_AXIS));
			schedulerAcquisitionPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
			
			JLabel runTitleLabel = new JLabel("3. Izrada rasporeda");
			runTitleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
			runTitleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 19));
			runTitleLabel.setForeground(Color.GRAY);

			runButton.setAlignmentX(Component.LEFT_ALIGNMENT);
			runButton.addActionListener(new ActionListener(){
				public void actionPerformed(ActionEvent e) {
						controller.getLocalScheduler();
				}
			});

			schedulerAcquisitionPanel.add(runTitleLabel);
			schedulerAcquisitionPanel.add(Box.createVerticalStrut(10));
			schedulerAcquisitionPanel.add(runButton);
			schedulerAcquisitionPanel.add(Box.createVerticalStrut(10));
			
			JSeparator line = new JSeparator(SwingConstants.HORIZONTAL);
			line.setAlignmentX(Component.LEFT_ALIGNMENT);
			JSeparator line2 = new JSeparator(SwingConstants.HORIZONTAL);
			line2.setAlignmentX(Component.LEFT_ALIGNMENT);
			JSeparator line3 = new JSeparator(SwingConstants.HORIZONTAL);
			line3.setAlignmentX(Component.LEFT_ALIGNMENT);
			
			/* Locking setup */
        	JXLayer<JComponent> saveLockPanel = new JXLayer<JComponent>(savePanel, savePanelLock);   
        	savePanelLock.setLocked(true);
        	saveLockPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        	saveLockPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        	JXLayer<JComponent> schedulerLockPanel = new JXLayer<JComponent>(schedulerAcquisitionPanel, schedulerPanelLock);   
        	schedulerPanelLock.setLocked(true);
        	schedulerLockPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        	schedulerLockPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
        	
			/* Main panel */
			mainPanel.add(validationPanel);
			mainPanel.add(line);
			mainPanel.add(saveLockPanel);
			mainPanel.add(line2);
			mainPanel.add(schedulerLockPanel);
			mainPanel.add(line3);

			final JDialog thisdialog = this;
			cancelButton.setAlignmentX(Component.LEFT_ALIGNMENT);
			cancelButton.addActionListener(new ActionListener(){

				@Override
				public void actionPerformed(ActionEvent e) {
					thisdialog.setVisible(false);
				}
			});
			
			JPanel superPanel = new JPanel();
			superPanel.setLayout(new BorderLayout());
			
			JPanel cancelPanel = new JPanel();
			cancelPanel.setLayout(new BorderLayout());
			cancelPanel.add(BorderLayout.WEST, Box.createHorizontalStrut((int)mainPanel.getPreferredSize().getWidth()/3));
			cancelPanel.add(BorderLayout.CENTER, cancelButton);
			cancelPanel.add(BorderLayout.EAST, Box.createHorizontalStrut((int)mainPanel.getPreferredSize().getWidth()/3));
			cancelPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
			superPanel.add(BorderLayout.CENTER, mainPanel);
			superPanel.add(BorderLayout.SOUTH, cancelPanel);
			
			setContentPane(superPanel);
			this.pack();
		}
		
		public void setVisible(boolean visible){
			super.setVisible(visible);
			saveStatusLabel.setText(" ");
			validationStatusPane.setText(" ");
		}
		

	}
	
	
	/**
	 * Custom razred za prikaz taba kod odabira načina raspodjele studenata
	 */
	private class DistributionTabComponent extends JPanel {
		private static final long serialVersionUID = 1L;

		public DistributionTabComponent(JCheckBox cbox, String title){
    		setOpaque(false);
    		add(new JLabel(title));
    		cbox.setHorizontalTextPosition(JCheckBox.LEFT);
    		cbox.setOpaque(false);
    		add(cbox);
    	}
    }
    
	/**
     * Custom renderer za hijerarhijski prikaz grupa u listi
     */
	private class GroupListRenderer extends JLabel implements ListCellRenderer{
		private static final long serialVersionUID = 1L;

			public GroupListRenderer(){
		        setOpaque(true);
		        setHorizontalAlignment(LEFT);
		        setVerticalAlignment(CENTER);
    		}
    		
			public Component getListCellRendererComponent(JList list, 
					Object value, int index, boolean isSelected, boolean cellHasFocus) {

			         if (isSelected) {
			             setBackground(list.getSelectionBackground());
			             setForeground(list.getSelectionForeground());
			         } else {
			             setBackground(list.getBackground());
			             setForeground(list.getForeground());
			         }
			         setEnabled(list.isEnabled());
			         setFont(list.getFont());
			         
			         PeopleParameter pp = (PeopleParameter)value;
			        
			         if(pp!=null){
			        	 
			        	//Naziv se postavlja odmah - prethodujuce praznine se dodaju naknadno
						this.setText(pp.getGroupName());
						//Oblikovanje prikaza prema relativnog pathu grupe
						//Path duljine jedne znamenke je vršna grupa za predavanje ili za labose, npr. 0 ili 1
						if(pp.getGroupRelativePath().length()==1) {
							this.setFont(new Font("Arial", Font.BOLD, 17));
						//Konkretna grupa za predavanje ili ciklus labosa
						}else if(pp.getGroupRelativePath().matches("[0-1]/[0-9]+")){
							this.setFont(new Font("Arial", Font.BOLD, 15));
							this.setText("     "+this.getText());
						//Konkretna grupa za neki termin labosa
						}else if(pp.getGroupRelativePath().matches("1/[0-9]+/[^/]+")){
							this.setFont(new Font("Arial", Font.PLAIN, 13));
							this.setText("          "+this.getText());
						}

			         }
			         else setText("");
			         return this;
				
			}
		
    	
    }

	/**
     * Custom renderer za prikaz prostorija u listi
     */
	private class RoomListRenderer extends JLabel implements ListCellRenderer{
		private static final long serialVersionUID = 1L;

			public RoomListRenderer(){
		        setOpaque(true);
		        setHorizontalAlignment(LEFT);
		        setVerticalAlignment(CENTER);
    		}
    		 
			public Component getListCellRendererComponent(JList list, 
					Object value, int index, boolean isSelected, boolean cellHasFocus) {
				
		         if (isSelected) {
		             setBackground(list.getSelectionBackground());
		             setForeground(list.getSelectionForeground());
		         } else {
		             setBackground(list.getBackground());
		             setForeground(list.getForeground());
		         }
		         setEnabled(list.isEnabled());
		         setFont(list.getFont());

		         RoomParameter room = (RoomParameter)value;
		         setText(room.printForSelectedActivity());
				 return this;
			}
    }
	
	/**
     * Custom renderer za prikaz aktivnosti u prostorijama
     */
	private class ActivityTypeRenderer extends JLabel implements ListCellRenderer{
		private static final long serialVersionUID = 1L;

			public ActivityTypeRenderer(){
		        setOpaque(true);
		        setHorizontalAlignment(LEFT);
		        setVerticalAlignment(CENTER);
    		}
    		
			public Component getListCellRendererComponent(JList list, 
					Object value, int index, boolean isSelected, boolean cellHasFocus) {
				
		         if (isSelected) {
		             setBackground(list.getSelectionBackground());
		             setForeground(list.getSelectionForeground());
		         } else {
		             setBackground(list.getBackground());
		             setForeground(list.getForeground());
		         }
		         setEnabled(list.isEnabled());
		         setFont(list.getFont());

		     	int activityType = (Integer)value;
		    	if(activityType==Definition.LECTURE) setText(" predavanja");
		    	else if(activityType==Definition.EXERCISE) setText(" lab.vježbe");
		    	else if(activityType==Definition.ASSESSMENT) setText(" provjeru znanja");
				return this;
			}
    }
	
	/**
     * Custom renderer za parametara osoba
     */
	private class PeopleParameterRenderer extends JLabel implements ListCellRenderer{
		private static final long serialVersionUID = 1L;

			public PeopleParameterRenderer(){
		        setOpaque(true);
		        setHorizontalAlignment(LEFT);
		        setVerticalAlignment(CENTER);
    		}
    		
			public Component getListCellRendererComponent(JList list, 
					Object value, int index, boolean isSelected, boolean cellHasFocus) {
				
		         if (isSelected) {
		             setBackground(list.getSelectionBackground());
		             setForeground(list.getSelectionForeground());
		         } else {
		             setBackground(list.getBackground());
		             setForeground(list.getForeground());
		         }
		         setEnabled(list.isEnabled());
		         setFont(list.getFont());

		         if(value instanceof PeopleParameter){
			     	PeopleParameter param = (PeopleParameter)value;
			     	if(param.isGroup()) setText(param.getGroupName());
			     	else setText(param.getJmbag());
		         }else if(value instanceof TeamParameter){
		        	 TeamParameter param = (TeamParameter)value;
		        	 setText(param.getTeamName());
		         }
		     	
				return this;
			}
    }
	
	/**
     * Custom renderer za prikaz trajanja/odnosa u danima i satima
     */
	private class DurationRenderer extends JLabel implements ListCellRenderer{
		private static final long serialVersionUID = 1L;

			public DurationRenderer(){
		        setOpaque(true);
		        setHorizontalAlignment(LEFT);
		        setVerticalAlignment(CENTER);
    		}
    		
			public Component getListCellRendererComponent(JList list, 
					Object value, int index, boolean isSelected, boolean cellHasFocus) {
				
		         if (isSelected) {
		             setBackground(list.getSelectionBackground());
		             setForeground(list.getSelectionForeground());
		         } else {
		             setBackground(list.getBackground());
		             setForeground(list.getForeground());
		         }
		         setEnabled(list.isEnabled());
		         setFont(list.getFont());
	
		         String result = "";
		         String result2 = "";
		         int parameter=0;
		         Integer v = null;
		         
		         if(value==null) return this;
		         if(value instanceof Integer) {
		        	 v = (Integer)value;
		        	 if(v!=null) parameter=v.intValue();
		        	 else return this;
		         }
		         if(value instanceof Precondition){
		        	 Precondition p = (Precondition)value;
		        	 parameter = p.getTimeDistanceValue();
		        	 result = p.getEventName() + ", najmanje ";
		        	 result2 = " prije.";
		         }

		     	 if(parameter<15){
		     		 int days = parameter;
		     		 String dayNoun="dana";
		     		 if(days==1) dayNoun="dan"; 
		     		 result += days + " " + dayNoun;
		     	 }else{
 		     		 int hours = parameter/60;
  		     		String hourNoun="sati";
 		     		if(hours==1) hourNoun="sat";
 		     		else if (hours>1 && hours<5) hourNoun="sata";
		     		String minuteNoun = "minuta";
		     		int minutes = parameter%60;
		     		String hourText="",minuteText="", connection="";		     		
		     		if(hours>0) hourText = hours + " " + hourNoun;
		     		if(minutes>0)  minuteText = minutes + " " + minuteNoun;
		     		if(hours>0 && minutes>0) connection=" i ";
		     		result += hourText + connection + minuteText;
		     	 }
		     	 result += result2;
		     	
		     	 setText(result);
				 return this;
			}
    	}
	
	/**
     * Custom renderer za vremenske periode u definiciji entiteta
     */
	private class PeriodRenderer extends JLabel implements ListCellRenderer{
		private static final long serialVersionUID = 1L;

			public PeriodRenderer(){
		        setOpaque(true);
		        setHorizontalAlignment(LEFT);
		        setVerticalAlignment(CENTER);
    		}
    		
			public Component getListCellRendererComponent(JList list, 
					Object value, int index, boolean isSelected, boolean cellHasFocus) {
				
		         if (isSelected) {
		             setBackground(list.getSelectionBackground());
		             setForeground(list.getSelectionForeground());
		         } else {
		             setBackground(list.getBackground());
		             setForeground(list.getForeground());
		         }
		         setEnabled(list.isEnabled());
		         setFont(list.getFont());

		     	TimeParameter param = (TimeParameter)value;
		     	
		     	setText(param.getFromDate()+" "+param.getFromTime()+" do "+param.getToDate()+" "+param.getToTime());
				return this;
			}
    }
	
    }
    
    public interface ParameterUpdateCallBack{
    	public void parameterUpdated();
    }
    
    public interface Updateable{
    	public void setParameterToUpdate(IDefinitionParameter pp, ParameterUpdateCallBack cb);
    }
}
