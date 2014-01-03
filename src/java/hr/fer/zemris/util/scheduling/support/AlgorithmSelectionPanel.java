package hr.fer.zemris.util.scheduling.support;

import java.util.Arrays;
import java.util.prefs.Preferences;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class AlgorithmSelectionPanel extends JPanel {
	
	private static final long serialVersionUID = 1L;
	private int[] sliderValues,defaultValues;
	private final String[] sliderLabels = new String[]{"Ant Colony Optimization", "Bee Colony Optimization", "Clonal Selection Algorithm", "Genetic Algorithm", "Harmony Search", "Particle Swarm Optimization",  "Stochastic Diffusion Search"};
	
	public AlgorithmSelectionPanel() {
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		Preferences prefs = Preferences.userRoot().node(AlgorithmSelectionPanel.class.getCanonicalName());
		defaultValues = parseString(prefs.get("defaultValues", null));
		JSlider[] sliders = new JSlider[7];
		if(defaultValues!=null)
			sliderValues=defaultValues;
		else
			sliderValues=new int[7];
		for(int i=0;i<sliders.length;i++) {
			final int idx=i;
			sliders[i]=new JSlider();
			sliders[i].setMajorTickSpacing(1);
		    sliders[i].setMaximum(10);
		    sliders[i].setPaintTicks(true);
		    sliders[i].setPaintLabels(true);
			sliders[i].setValue(sliderValues[i]);
			sliders[i].addChangeListener(new ChangeListener() {
				@Override
				public void stateChanged(ChangeEvent changeEvent) {
					JSlider theSlider = (JSlider) changeEvent.getSource();
			        if (!theSlider.getValueIsAdjusting()) {
			          sliderValues[idx] = theSlider.getValue();
			        }
				}
			});
			add(new JLabel(sliderLabels[i]));
			add(sliders[i]);
		}
	}

	private int[] parseString(String string) {
		if(string==null)
			return null;
		int[] ret = new int[7];
		string=string.substring(1,string.length()-1);
		String[] split = string.split(", ");
		for(int i=0;i<split.length;i++) {
			ret[i]=Integer.parseInt(split[i]);
		}
		return ret;
	}
	
	public int[] getValues() {
		return sliderValues;
	}
	
	public int[] getDefaultValues() {
		return defaultValues;
	}
	
	public void storeValues() {
		Preferences prefs = Preferences.userRoot().node(AlgorithmSelectionPanel.class.getCanonicalName());
		prefs.put("defaultValues", Arrays.toString(sliderValues));
	}

}
