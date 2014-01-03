package hr.fer.zemris.jcms.desktop.napredak;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class GenerirajNapredak {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException {
		File f = new File("D:/tmp/napredak");
		for(int i = -1; i <= 100; i++) {
			generirajSliku(f,i,false);
			generirajSliku(f,i,true);
		}
	}

	private static void generirajSliku(File dir, int broj, boolean passed) throws IOException {
		File slika = broj<0 ? new File(dir, "prog_"+(passed?"PASSED":"FAILED")+"_unknown.png") : new File(dir, "prog_"+(passed?"PASSED":"FAILED")+"_"+broj+".png");
		int w = 300;
		int h = 15;
		Color bg = new Color(220,220,220);
		Color border = new Color(0,0,255);
		BufferedImage bim = new BufferedImage(w, h, BufferedImage.TYPE_3BYTE_BGR);
		Graphics2D g = bim.createGraphics();
		g.setColor(bg);
		g.fillRect(0, 0, w, h);
		if(broj>=0) {
			if(passed) {
				g.setColor(new Color(128,255,128));
			} else {
				g.setColor(new Color(255,128,128));
			}
			g.fillRect(0, 0, (int)(broj/100.0*w+0.5), h);
			g.setColor(border);
			//g.drawRect(0, 0, w-1, h-1);
			String poruka = broj+" %";
			Font f = new Font(Font.SANS_SERIF, Font.BOLD, 12);
			g.setFont(f);
			FontMetrics fm = g.getFontMetrics();
			int pw = fm.stringWidth(poruka);
			g.setColor(Color.BLACK);
			g.drawString(poruka, (w-pw)/2, h-(h-fm.getHeight())/2-3);
		} else {
			g.setColor(border);
			//g.drawRect(0, 0, w-1, h-1);
			String poruka = "? (maksimum nije definiran)";
			Font f = new Font(Font.SANS_SERIF, Font.BOLD, 12);
			g.setFont(f);
			FontMetrics fm = g.getFontMetrics();
			g.setColor(Color.BLACK);
			g.drawString(poruka, 4, h-(h-fm.getHeight())/2-3);
		}
		g.dispose();
		ImageIO.write(bim, "png", slika);
	}

	/* Ovo dolje može poslužiti kao kostur za generiranje sličica s nazivima kolegija.
	 * 
	private static void generirajSliku2(File dir, int broj) throws IOException {
		File slika = broj<0 ? new File(dir, "prog_unknown.png") : new File(dir, "prog_"+broj+".png");
		int w = 300;
		int h = 20;
		Color transp = new Color(255,255,255,0);
		Color bg = new Color(200,200,200);
		Color border = new Color(0,0,0);
		BufferedImage bim = new BufferedImage(w, h, BufferedImage.TYPE_4BYTE_ABGR);
		Graphics2D g = bim.createGraphics();
		g.setColor(transp);
		g.fillRect(0, 0, w, h);
		g.setColor(bg);
		g.fillRoundRect(0, 0, w-1, h-1, 7, 7);
		g.setColor(border);
		g.drawRoundRect(0, 0, w-1, h-1, 7, 7);
		g.dispose();
		ImageIO.write(bim, "png", slika);
	}*/
}
