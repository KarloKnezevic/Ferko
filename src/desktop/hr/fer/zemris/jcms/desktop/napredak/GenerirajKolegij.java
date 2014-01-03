package hr.fer.zemris.jcms.desktop.napredak;

import hr.fer.zemris.jcms.service2.course.CourseService;

import java.awt.Color;

import java.awt.Font;
import java.awt.FontMetrics;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class GenerirajKolegij {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException {
		File f = new File("D:/tmp/kolegij");
		String[] kolegiji = new String[] {"1","Digitalna logika", "2","Moderne metode fizike u elektrotehnici i informacijskoj tehnologiji"};
		for(int i = 0; i < kolegiji.length/2; i++) {
			generirajKolegij(f,kolegiji[2*i],kolegiji[2*i+1]);
		}
	}

	private static void generirajKolegij(File dir, String broj, String naziv) throws IOException {
		File slika = new File(dir, "kolegij_"+broj+".png");
		BufferedImage bim = generirajCourseInstanceImage(naziv);
		ImageIO.write(bim, "png", slika);
	}
	
	/** OVA METODA JE ISKOPIRANA U {@link CourseService}. Tu se može ekperimentirati s bojama i ostalime,
	 *  a potom konacnu metodu samo treba prebaciti tamo! **/
	private static BufferedImage generirajCourseInstanceImage(String naziv) throws IOException {
		int w = 200;
		int h_margin = 10;
		int tw = w-2*h_margin;
		int h = 40;
		int radiusX = 15;
		int radiusY = 15;
		Color transp = new Color(255,255,255,0);
		Color bg = new Color(200,200,100);
		Color border = new Color(0,0,0);
		BufferedImage bim = new BufferedImage(w, h, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g = bim.createGraphics();
		g.setColor(transp);
		g.fillRect(0, 0, w, h);
		g.setColor(bg);
		g.fillRoundRect(0, 0, w-1, h-1, radiusX, radiusY);
		g.setColor(border);
		g.drawRoundRect(0, 0, w-1, h-1, radiusX, radiusY);
		String[] retci = new String[] {null, null};
		int[] retciW = new int[] {0, 0};
		int redak = 0;
		Font f = new Font(Font.SERIF, Font.BOLD, 12);
		g.setFont(f);
		FontMetrics fm = g.getFontMetrics();
		g.setColor(Color.BLACK);
		char[] elems = naziv.toCharArray();
		int curr = 0;
o:		while(redak<2) {
			int poc = curr;
			int acked = curr;
			while(true) {
				int krenuo = curr;
				while(curr<elems.length && elems[curr]!=' ') curr++;
				if(poc==curr || curr==krenuo) break o;
				String s = new String(elems, poc, curr-poc);
				int sw = fm.stringWidth(s);
				if(sw<tw) {
					retci[redak] = s;
					retciW[redak] = sw;
					acked = curr;
					while(curr<elems.length && elems[curr]==' ') curr++;
				} else if(poc==acked) {
					retci[redak] = s;
					retciW[redak] = sw;
					while(curr<elems.length && elems[curr]==' ') curr++;
					redak++;
					break;
				} else {
					curr = acked;
					while(curr<elems.length && elems[curr]==' ') curr++;
					redak++;
					break;
				}
			}
		}
		if(retci[1]==null) {
			// Sve pišem samo u jednom retku...
			g.drawString(retci[0], h_margin+(tw-retciW[0])/2, h-(h-fm.getAscent())/2-fm.getDescent());
		} else {
			// Imamo dva retka...
			if(curr<elems.length) retci[1]=retci[1]+"...";
			g.drawString(retci[0], h_margin+(tw-retciW[0])/2, h/2-(h/2-fm.getAscent())/2);
			g.drawString(retci[1], h_margin+(tw-retciW[1])/2, h-(h/2-fm.getAscent())/2-fm.getDescent());
		}
		g.dispose();
		return bim;
	}

}
