package hr.fer.zemris.jcms.desktop.ispiti;

import java.awt.GridLayout;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

public class GenerirajZIPSaSlikama {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					if(!go()) {
						System.exit(0);
					}
				} catch (Exception e) {
					System.err.println("Dogodila se je pogreška: "+e.getMessage());
					JOptionPane.showMessageDialog(null, "Dogodila se je pogreška: "+e.getMessage(), "Pogreška", JOptionPane.ERROR_MESSAGE);
					System.exit(0);
				}
			}
		});
	}

	protected static boolean go() throws IOException {
		JFrame frame = new JFrame();
		frame.setTitle("Generiranje ZIP arhive i opisnika slika");
		frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		
		JLabel statusLabel = new JLabel();
		JProgressBar pb = new JProgressBar();
		frame.getContentPane().setLayout(new GridLayout(2, 1));
		statusLabel.setText("Biranje direktorija sa slikama...");
		frame.getContentPane().add(statusLabel);
		frame.getContentPane().add(pb);
		frame.setBounds(0, 0, 700, 100);
		
		frame.setVisible(true);
		
		JFileChooser ch = new JFileChooser();
		ch.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		ch.setMultiSelectionEnabled(false);
		ch.setDialogTitle("Odaberite direktorij koji sadrži slike");
		if(ch.showOpenDialog(null)!=JFileChooser.APPROVE_OPTION) {
			return false;
		}
		File imageDir = ch.getSelectedFile(); 
		ch.setSelectedFiles(null);
		
		statusLabel.setText("Biranje RMK datoteke...");
		
		ch.setDialogTitle("Odaberite RMK datoteku s podatcima");
		ch.setFileSelectionMode(JFileChooser.FILES_ONLY);
		if(ch.showOpenDialog(null)!=JFileChooser.APPROVE_OPTION) {
			return false;
		}
		File rmkFile = ch.getSelectedFile(); 
		ch.setSelectedFiles(null);
		
		statusLabel.setText("Biranje ZIP arhive koju treba generirati...");
		
		ch.setDialogTitle("Odaberite naziv ZIP arhive koju treba stvoriti");
		ch.setFileSelectionMode(JFileChooser.FILES_ONLY);
		if(ch.showSaveDialog(null)!=JFileChooser.APPROVE_OPTION) {
			return false;
		}
		File zipFile = ch.getSelectedFile(); 
		ch.setSelectedFiles(null);
		String zipName = zipFile.getName().toUpperCase();
		if(!zipName.endsWith(".ZIP")) {
			zipFile = new File(zipFile.getParentFile(), zipFile.getName() + ".zip");
		}
		
		Set<String> validFiles = new HashSet<String>(1000);
		Map<String,String> map = new HashMap<String, String>();
		
		statusLabel.setText("Pregledavanje RMK datoteke");
		statusLabel.repaint();
		
		BufferedReader br = new BufferedReader(new FileReader(rmkFile));
		br.readLine();
		while(true) {
			String line1 = br.readLine();
			if(line1==null) break;
			br.readLine();
			String[] elems = line1.split("\t");
			int broj = elems.length;
			String zadnji = elems[broj-1];
			int pos = zadnji.indexOf(11);
			if(pos==-1) {
				throw new IOException("U RMK datoteci je pronađen redak neočekivanog formata.");
			}
			zadnji = zadnji.substring(0, pos);
			pos = zadnji.lastIndexOf('\\');
			if(pos==-1) {
				pos = zadnji.lastIndexOf('/');
			}
			if(pos==-1) {
				throw new IOException("U RMK datoteci je pronađen redak neočekivanog formata (2).");
			}
			String datoteka = zadnji.substring(pos+1);
			pos = datoteka.lastIndexOf('.');
			if(pos==-1) {
				throw new IOException("U RMK datoteci je pronađen redak neočekivanog formata (3).");
			}
			datoteka = datoteka.substring(0, pos);
			validFiles.add(datoteka);
			map.put(datoteka, elems[0]);
		}
		br.close();

		int brojZapisa = map.size();
		
		statusLabel.setText("Pronađeno "+brojZapisa+" zapisa. Generiram ZIP arhivu...");
		statusLabel.repaint();

		ZipOutputStream zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(zipFile)));
		StringBuilder sb = new StringBuilder(1024*64);
		if(brojZapisa>0) {
			File[] file = imageDir.listFiles();
			int pronasaoZapis = 0;
			pb.setMinimum(0);
			pb.setMaximum(brojZapisa+1);
			pb.setValue(pronasaoZapis);
			pb.setStringPainted(true);
			pb.repaint();
			for(File f : file) {
				if(f.isDirectory()) {
					continue;
				}
				String n = f.getName();
				int pos = n.lastIndexOf('.');
				if(pos==-1) {
					continue;
				}
				n = n.substring(0, pos);
				if(!map.containsKey(n)) continue;
				pronasaoZapis++;
				String jmbag = map.get(n);
				sb.append(jmbag).append("\t").append(f.getName()).append("\tO1\t\n");
				ZipEntry e = new ZipEntry(f.getName());
				zos.putNextEntry(e);
				kopiraj(f, zos);
				zos.closeEntry();
				pb.setValue(pronasaoZapis);
				pb.repaint();
			}
		}
		ZipEntry e = new ZipEntry("opisnik.txt");
		zos.putNextEntry(e);
		zos.write(sb.toString().getBytes());
		zos.closeEntry();
		pb.setValue(pb.getMaximum());
		pb.repaint();
		zos.close();
		
		statusLabel.setText("Arhiva je generirana. Možete zatvoriti program.");
		return true;
	}

	private static void kopiraj(File f, OutputStream os) throws IOException {
		BufferedInputStream bis = new BufferedInputStream(new FileInputStream(f));
		byte[] buf = new byte[1024*32];
		while(true) {
			int n = bis.read(buf);
			if(n<1) break;
			os.write(buf,0,n);
		}
		bis.close();
	}

}
