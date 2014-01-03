package hr.fer.zemris.util.scheduling;

import hr.fer.zemris.util.scheduling.algorithms.MainScheduler;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

/**
 * Razred koji sluzi za testiranje algoritama. U poziv konstruktora LocalStartera se predaje
 * @author Tin Franovic
 *
 */
public class Tester {


	public static void main(String[] args) throws InterruptedException, InvocationTargetException {
		final LocalStarter starter = new LocalStarter(new MainScheduler());
		LocalStarter.loadChooser();
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
}
