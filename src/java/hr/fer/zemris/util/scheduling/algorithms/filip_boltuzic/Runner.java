package hr.fer.zemris.util.scheduling.algorithms.filip_boltuzic;

import hr.fer.zemris.util.scheduling.LocalStarter;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

public class Runner {

	public static void main(String[] args) throws InterruptedException, InvocationTargetException {
		final LocalStarter starter = new LocalStarter(new BCOScheduler());
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
