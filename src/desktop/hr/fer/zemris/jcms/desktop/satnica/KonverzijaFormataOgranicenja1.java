package hr.fer.zemris.jcms.desktop.satnica;

import hr.fer.zemris.util.StringUtil;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;

public class KonverzijaFormataOgranicenja1 {

	/**
	 * Ove godine (AG2009/2010, zima) ograničenja stižu u novom formatu.
	 * Ovo je skripta koja ih konvertira natrag u stari format. Grupe
	 * pretvara u uppercase i time gubi informacije o malim slovima.
	 * 
	 * Argumenti prilikom poziva moraju biti: ulaznaDatoteka izlaznaDatoteka.
	 * 
	 * Očekuje se da je ulaz u windows-1250 dok će izlaz biti u UTF-8.
	 * 
	 * @param args
	 */
	public static void main(String[] args) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(new BufferedInputStream(new FileInputStream(args[0])),"windows-1250"));
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(args[1])),"UTF-8"));
		while(true) {
			String line = br.readLine();
			if(line == null) break;
			line = line.trim();
			if(line.equals("")) continue;
			String[] elems = StringUtil.split(line, '#');
			bw.write(elems[4]);
			bw.write("\t");
			bw.write(elems[3].toUpperCase());
			bw.write("\t");
			bw.write(elems[5]);
			bw.write("\t");
			bw.write(elems[1]);
			bw.write("\t");
			bw.write(elems[2]);
			bw.write("\r\n");
		}
		bw.close();
		br.close();
	}

}
