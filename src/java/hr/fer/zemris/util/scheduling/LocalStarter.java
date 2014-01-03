package hr.fer.zemris.util.scheduling;

import hr.fer.zemris.jcms.model.planning.Definition;

import hr.fer.zemris.jcms.model.planning.Plan;
import hr.fer.zemris.util.scheduling.algorithms.MainScheduler;
import hr.fer.zemris.util.scheduling.support.*;
import hr.fer.zemris.util.scheduling.support.algorithmview.IEvent;
import hr.fer.zemris.util.scheduling.support.algorithmview.IPlan;
import hr.fer.zemris.util.scheduling.support.algorithmview.ITerm;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.prefs.Preferences;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Upravljacki program za izradu rasporeda na racunalu korisnika
 *
 */
public class LocalStarter implements ISchedulingMonitor{
	

	private IPlan plan;
	private Map<String,ISchedulingData> data ;
	private DefaultComboBoxModel algorithms = new DefaultComboBoxModel();
	/* Executing algorithm */
	private ISchedulingAlgorithm runningAlgorithm;
	
	private final JFrame frm = new JFrame("Ferko - Rasporedi");
	private JLabel statusLabel = new JLabel("Status: ");
	private JPanel feedbackPanel= new FeedBackPanel("");
	final JButton startButton = new JButton("Pokreni izradu!");
	final JButton stopButton = new JButton("Prekini izradu!");
	JButton resultsButton = new JButton("Pohrani rezultate");

	private static File selectedFile=null;
	private static ISchedulingAlgorithm selectedTestingAlgorithm=null;
	private String rootPath=null;

	public LocalStarter() {
		selectedFile=null;
		selectedTestingAlgorithm=null;
		rootPath=null;
	}
	
	public LocalStarter(String rootPath) {
		selectedFile=null;
		this.rootPath=rootPath;
	}
	
	public LocalStarter(ISchedulingAlgorithm algorithm) {
		selectedTestingAlgorithm=algorithm;
		System.out.println(selectedTestingAlgorithm.getClassName());
	}


	@Override
	public void algorithmStatusChangeNotification() {
		statusLabel.setText("Status: " +runningAlgorithm.getStatus().name());
		stopButton.setEnabled(false);
		startButton.setEnabled(true);
		frm.pack();
		if(runningAlgorithm.getStatus().equals(SchedulingAlgorithmStatus.SUCCESS))
			resultsButton.setEnabled(true);
	}
	
	/**
	 * Postoje 3 nacina pokretanja LocalStartera. Pokretanjem bez argumenata komandne linije imamo defaultno ponasanje metoda koje
	 * citaju datoteke, tj citaju ih direktno iz .jar datoteke. Ukoliko se preda parametar -g, otvara se graficko sucelje u kojem je moguce
	 * odabrati .jar datoteku iz koje ucitavamo podatke. Takoder, postoji i eksterni nacin pokretanja u kojem se poziva samo konstruktor
	 * razreda, kojem se predaje algoritam koji se zeli testirati.
	 */
	public static void main(String[] args) throws IOException, InterruptedException, InvocationTargetException {
		Locale.setDefault(Locale.US);
		final LocalStarter starter;
		if(args.length==0) {
			starter = new LocalStarter();
		}
		else {
			if(args[0].equals("-g")) {
				starter = new LocalStarter();
				loadChooser();
			}
			else
				starter = null;
		}
		
		SwingUtilities.invokeAndWait(new Runnable(){
			@Override
			public void run() {
				try {
					starter.load();
				} catch (IOException e) {
					
					e.printStackTrace();
				}
				starter.createGUI();
			}
		});
	}
	
	public void createGUI(){
		
		frm.setVisible(true);
		frm.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		
		frm.addWindowListener(new WindowListener(){
			@Override
			public void windowActivated(WindowEvent e) {}
			@Override
			public void windowClosed(WindowEvent e) {
//				if(runningAlgorithm!=null)
//					runningAlgorithm.stop();
			}
			@Override
			public void windowClosing(WindowEvent e) {
				if(runningAlgorithm!=null)
					runningAlgorithm.stop();
			}
			@Override
			public void windowDeactivated(WindowEvent e) {}
			@Override
			public void windowDeiconified(WindowEvent e) {}
			@Override
			public void windowIconified(WindowEvent e) {}
			@Override
			public void windowOpened(WindowEvent e) {}
			
		});
		
		final JPanel mainPanel = new JPanel();
		JLabel titleLabel = new JLabel("Lokalna izrada rasporeda");
		JLabel planNameLabel = new JLabel("Naziv rasporeda: ");
		JLabel algorithmLabel = new JLabel("Odabir algoritma:");
		final JComboBox algorithmComboBox = new JComboBox(algorithms);
		JPanel buttonPanel= new JPanel();

		
		
		JTextArea planNameArea = new JTextArea(1, 10);
	
		algorithmComboBox.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(algorithmComboBox.getSelectedIndex()>0) startButton.setEnabled(true);
			}
			
		});
		final ISchedulingMonitor monitor = this; 
		startButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				String selectedAlgorithm = (String)algorithmComboBox.getSelectedItem();
				if(selectedTestingAlgorithm!=null) {
					runningAlgorithm=selectedTestingAlgorithm;
				}
				else {
					ClassLoader cl = ClassLoader.getSystemClassLoader();
					try {
						runningAlgorithm = (ISchedulingAlgorithm)cl.loadClass(selectedAlgorithm).newInstance();
					} catch (InstantiationException e1) {
						e1.printStackTrace();
						System.exit(-1);
					} catch (IllegalAccessException e1) {
						e1.printStackTrace();
						System.exit(-1);
					} catch (ClassNotFoundException e1) {
						e1.printStackTrace();
						System.exit(-1);
					}
				}
				if(runningAlgorithm instanceof MainScheduler) {
					AlgorithmSelectionPanel p = new AlgorithmSelectionPanel();
					int ret=JOptionPane.showConfirmDialog(null, new Object[]{"Odaberite prioritete algoritama:", p}, "Prioriteti algoritama", JOptionPane.OK_CANCEL_OPTION);
					int[] sliderValues;
					if(ret==JOptionPane.OK_OPTION) {
						sliderValues=p.getValues();
						p.storeValues();
					}
					else {
						sliderValues=p.getDefaultValues();
					}
					((MainScheduler) runningAlgorithm).setPriorities(sliderValues);
				}
				runningAlgorithm.prepare(plan, data);
				statusLabel.setText("Status: " + runningAlgorithm.getStatus().name());
				feedbackPanel = (JPanel)runningAlgorithm.getExecutionFeedback();
				if(feedbackPanel==null) 				
				{
					feedbackPanel = new FeedBackPanel(" no feedback");
				}
				int componentNumber = mainPanel.getComponentCount();
				for(int i = 0; i< componentNumber; i++){
					Component c = mainPanel.getComponent(i);
					if(c.getName()!=null && c.getName().equals("FEEDBACK")) {
						mainPanel.remove(i);
						feedbackPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
						feedbackPanel.setName("FEEDBACK");
						mainPanel.add(feedbackPanel, i);
						frm.getContentPane().remove(0);
						mainPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
						frm.getContentPane().add(mainPanel);
						frm.pack();
						break;
					}
				}
				runningAlgorithm.registerSchedulingMonitor(monitor);
				runningAlgorithm.start();
				statusLabel.setText("Status: " + runningAlgorithm.getStatus().name());
				
				startButton.setEnabled(false);
				stopButton.setEnabled(true);
				resultsButton.setEnabled(false);
			}
		});
		
		stopButton.addActionListener(new ActionListener(){
			@Override
			public void actionPerformed(ActionEvent e) {
				runningAlgorithm.stop();	
				stopButton.setEnabled(false);
				startButton.setEnabled(true);
				feedbackPanel = (JPanel)runningAlgorithm.getExecutionFeedback();
				if(feedbackPanel==null) 				
				{
					feedbackPanel = new FeedBackPanel(" no feedback");
				}
				int componentNumber = mainPanel.getComponentCount();
				for(int i = 0; i< componentNumber; i++){
					Component c = mainPanel.getComponent(i);
					if(c.getName()!=null && c.getName().equals("FEEDBACK")) {
						mainPanel.remove(i);
						feedbackPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
						feedbackPanel.setName("FEEDBACK");
						mainPanel.add(feedbackPanel, i);
						frm.getContentPane().remove(0);
						mainPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
						frm.getContentPane().add(mainPanel);
						frm.pack();
						break;
					}
				}
			}
		});
		
		resultsButton.addActionListener(new ActionListener(){

			@Override
			public void actionPerformed(ActionEvent e) {
				JFileChooser jfc = new JFileChooser();
				jfc.setSelectedFile(new File(plan.getName()+".xml"));
				int returnVal = jfc.showSaveDialog(frm);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
	                File file = jfc.getSelectedFile();
	                try {
	                	BufferedWriter bw = new BufferedWriter(new FileWriter(file));
	                	StringBuilder res = new StringBuilder();
	                	Plan p = (Plan)plan;
	                	String originalPlanID = null;
	                	if(p!=null) originalPlanID = p.getId();
	                	if(originalPlanID!=null) {
	                		res.append("<schedule planid=\""+ originalPlanID +"\" >");
	                	}
	                	else {
	                		res.append("<schedule>"); 
	                	}
	                	res.append("<algorithm className=\"" + runningAlgorithm.getClassName()+"\"/>");
	                	res.append(runningAlgorithm.getResult().getResultXML().toString());
	                	res.append("</schedule>");
	                	bw.write(res.toString());
	                	bw.flush();
	                	bw.close();
					} catch (FileNotFoundException e1) {
						
						e1.printStackTrace();
					} catch (IOException e2) {
						
						e2.printStackTrace();
					}
				}
			}
			
		});
		feedbackPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		stopButton.setEnabled(false);
		resultsButton.setEnabled(false);
		buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.LINE_AXIS));
		startButton.setAlignmentX(Component.LEFT_ALIGNMENT);
		stopButton.setAlignmentX(Component.RIGHT_ALIGNMENT);
		buttonPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
		algorithmLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		algorithmComboBox.setAlignmentX(Component.LEFT_ALIGNMENT);
		statusLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		statusLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 15));
		statusLabel.setForeground(Color.GRAY);
		resultsButton.setAlignmentX(Component.LEFT_ALIGNMENT);
		mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.PAGE_AXIS));
		mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
//		mainPanel.setPreferredSize(new Dimension(400,350));
		titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		titleLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 19));
		titleLabel.setForeground(Color.GRAY);
		planNameLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
		planNameArea.setMaximumSize(new Dimension(2000,30));
		algorithmComboBox.setMaximumSize(new Dimension(2000,30));
		planNameArea.setAlignmentX(Component.LEFT_ALIGNMENT);
		planNameArea.setEditable(false);
		planNameArea.setText(plan.getName());
		
		mainPanel.add(titleLabel);
		mainPanel.add(Box.createVerticalStrut(10));
		mainPanel.add(planNameArea);
		mainPanel.add(Box.createVerticalStrut(20));
		mainPanel.add(algorithmComboBox);
		mainPanel.add(Box.createVerticalStrut(10));
		mainPanel.add(algorithmLabel);
		mainPanel.add(algorithmComboBox);
		mainPanel.add(Box.createVerticalStrut(10));
		buttonPanel.add(startButton);
		buttonPanel.add(Box.createHorizontalGlue());
		buttonPanel.add(stopButton);
		mainPanel.add(buttonPanel);
		mainPanel.add(Box.createVerticalStrut(20));
		mainPanel.add(feedbackPanel);
		mainPanel.add(Box.createVerticalStrut(20));
		mainPanel.add(statusLabel);
		mainPanel.add(Box.createVerticalStrut(10));
		mainPanel.add(resultsButton);
		frm.getContentPane().add(mainPanel);
		
		frm.pack();
	}

	private class FeedBackPanel extends JPanel{
		
		private static final long serialVersionUID = 1L;

		public FeedBackPanel(String msg){
			setSize(new Dimension(400,100));
			setName("FEEDBACK");
			setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
			JLabel noFeedbackLabel = new JLabel(msg);
			noFeedbackLabel.setFont(new Font(Font.SANS_SERIF, Font.BOLD, 13));
			noFeedbackLabel.setForeground(Color.GRAY);
			add(noFeedbackLabel);
		}
	}
	
	public void load() throws IOException{
		
		data = new HashMap<String, ISchedulingData>();
		System.out.println("[LocalScheduler] Beginning data loading.");
		loadAlgorithmData(algorithms);
		loadPlanData("planData.xml");
		ItemCache jmbagsCache = new ItemCache();
		ItemCache termsCache = new ItemCache();
//		System.out.println("[LocalScheduler] Events in plan: " + plan.getEventsData().size());
		for(IEvent event : plan.getPlanEvents()) {
			System.out.println("[LocalScheduler] Loading event " + event.getName() + " (" + event.getId() + ")");
			if(event.getDefinition().getIndividuals()!=null || event.getDefinition().getLocationParameters()!=null){
				ISchedulingData entityData = new SchedulingDataImpl(jmbagsCache, termsCache);
				data.put(event.getId(), entityData);
				//System.out.println("[LocalScheduler] Beginning student data loading.");
				loadPeopleData(event.getId(), entityData);
				//System.out.println("[LocalScheduler] Beginning term(room) data loading.");
				loadTermData(event.getId(), entityData);
				entityData.dataLoadingCompleted();
				for(String jmbag : entityData.getPeopleData().keySet()) {
					jmbagsCache.addItem(jmbag);
				}
				for(RoomData room : entityData.getTermData().keySet()) {
					termsCache.addItem(room.getId());
				}
			}else{
				for(ITerm term : event.getTerms()){
					System.out.println("[LocalScheduler] Loading term " + term.getName() + " (" + term.getId() + ")");
					ISchedulingData entityData = new SchedulingDataImpl(jmbagsCache, termsCache);
					data.put(term.getId(), entityData);
					if(!term.getDefinition().getIndividuals().isEmpty())
						loadPeopleData(term.getId(), entityData);
					if(!term.getDefinition().getLocationParameters().isEmpty())
						loadTermData(term.getId(), entityData);
					entityData.dataLoadingCompleted();
					for(String jmbag : entityData.getPeopleData().keySet()) {
						jmbagsCache.addItem(jmbag);
					}
					for(RoomData room : entityData.getTermData().keySet()) {
						termsCache.addItem(room.getId());
					}
				}
			}
		}		
		return;
	}
	
	
	private void loadPlanData(String filename) throws IOException {
		BufferedReader br = prepareBufferedReader(filename);
		String planXML = br.readLine();		
//		System.out.println(planXML);
		System.out.println("[LocalScheduler] Loading plan data.");
		Node planNode = loadXML(planXML);		
		plan = new Plan(planNode);
		br.close();
	}
	
	private static Node loadXML(String planData){
		Node result = null;
		try {
			ByteArrayInputStream bis = new ByteArrayInputStream(planData.getBytes("UTF-8"));
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			org.w3c.dom.Document doc;
			doc = builder.parse(bis);
			Node planNode = doc.getFirstChild(); //<plan>
			result = planNode;
		} catch (UnsupportedEncodingException ignored) {
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			result=null;
		} catch (SAXException e) {
			e.printStackTrace();
			result=null;
		} catch (IOException e) {
			e.printStackTrace();
			result=null;
		} catch(Exception e){
			e.printStackTrace();
			result=null;
		}
		return result;
	}

	
	private void loadPeopleData(final String entityID, final ISchedulingData eventData) throws IOException {
		loadFile(entityID+"_peopleData.csv", new LineReaderCallBack<Void>() {
			
			@Override
			public boolean processLine(String line) {
				System.out.println("[LocalScheduler] " + line);
				eventData.addPeopleDataItem(line);
				return false;
			}
			@Override
			public void prepare() {
			}
			@Override
			public Void getResult() {
				return null;
			}
		});
	}
	
	private void loadTermData(final String entityID, final ISchedulingData eventData) throws IOException {
		loadFile(entityID+"_termData.csv", new LineReaderCallBack<Void>() {
			
			@Override
			public boolean processLine(String line) {
				System.out.println("[LocalScheduler] " + line);
				eventData.addTermDataItem(line);
				return false;
			}
			@Override
			public void prepare() {
			}
			@Override
			public Void getResult() {
				return null;
			}
		});
	}
	
	private void loadAlgorithmData(final DefaultComboBoxModel algorithms) throws IOException {
		if(selectedTestingAlgorithm!=null) {
			algorithms.addElement(selectedTestingAlgorithm.getClassName());
		}
		else {
			loadFile("algorithms.data", new LineReaderCallBack<Void>() {
				
				@Override
				public boolean processLine(String line) {
					//izbacivanje nepotrebnih algoriama iz izbornika
					String prefix = "hr.fer.zemris.util.scheduling.algorithms";
					if(!line.startsWith(prefix)) return false;
					if(line.indexOf('.', prefix.length()+1)!=-1) return false;
					algorithms.addElement(line); 
					System.out.println("[LocalScheduler] " + line);
					return false;
				}
				@Override
				public void prepare() {
				}
				@Override
				public Void getResult() {
					System.out.println("[LocalScheduler] Algorithm loading completed.");
					return null;
				}
			});
		}
	}
	
	private <T> T loadFile(String filename, LineReaderCallBack<T> callback) throws IOException {
		callback.prepare();
		
		BufferedReader br = prepareBufferedReader(filename);
		while(true) {
			String line = br.readLine();
			if(line==null) break;
			line = line.trim();
			if(line.equals("") || line.charAt(0)=='#') continue;
			callback.processLine(line);
		}
		br.close();
		return callback.getResult();
	}
	
	private BufferedReader prepareBufferedReader(String filename) throws UnsupportedEncodingException, IOException {
		if(rootPath != null) {
			return new BufferedReader(new InputStreamReader(new FileInputStream(new File(rootPath+"/"+filename))));
		}
		else if(selectedFile != null) {
			ZipInputStream zipInput = new ZipInputStream(new FileInputStream(selectedFile));
			ZipEntry zipEntry;
			while ((zipEntry = zipInput.getNextEntry()) != null) {
				if(zipEntry.getName().equals(filename))
					return new BufferedReader(new InputStreamReader(zipInput));
		        zipInput.closeEntry();
		    }
			zipInput.close();
			return null;
		}
		else {
			URL url = LocalStarter.class.getResource("/"+filename);
			return new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));
		}
	}
	
	public static void loadChooser() throws InterruptedException, InvocationTargetException {
			SwingUtilities.invokeAndWait(new Runnable() {
				@Override
				public void run() {
					Preferences prefs = Preferences.userRoot().node(LocalStarter.class.getCanonicalName());
					String lastDir = prefs.get("lastDir", null);
					JFileChooser fileChooser = new JFileChooser();
					FileNameExtensionFilter filter = new FileNameExtensionFilter("Jar files", "jar", "zip");
					fileChooser.setAcceptAllFileFilterUsed(true);
					fileChooser.setFileFilter(filter);
					if(lastDir!=null && !lastDir.isEmpty()) {
						fileChooser.setCurrentDirectory(new File(lastDir));
					}
					int returnValue = fileChooser.showOpenDialog(null);
					if(returnValue!=JFileChooser.APPROVE_OPTION) {
						System.err.println("Nije odabrana datoteka!");
						System.exit(13);
					}
					selectedFile = fileChooser.getSelectedFile();
					prefs.put("lastDir", selectedFile.getParentFile().getAbsolutePath());
				}
			});
	}

	static interface LineReaderCallBack<T> {
		public void prepare();
		public boolean processLine(String line);
		public T getResult();
	}
}
