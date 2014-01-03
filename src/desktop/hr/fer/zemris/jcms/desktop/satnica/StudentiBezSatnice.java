package hr.fer.zemris.jcms.desktop.satnica;

import hr.fer.zemris.jcms.parsers.TextService;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StudentiBezSatnice {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException {
		if(args.length != 3) {
			System.err.println("Ocekivao sam: isvu grupe_bez_satnice out/procisceniISVU");
			System.exit(0);
		}
		InputStream is = new BufferedInputStream(new FileInputStream(args[1]));
		List<String> satnicaLines = TextService.inputStreamToUTF8StringList(is);
		Map<String,String> grupeBezSatnice = new HashMap<String, String>(100);
		for(String line : satnicaLines) {
			grupeBezSatnice.put(line, line);
			//String[] elems = TextService.split(line, '/');
		}
		
		is = new BufferedInputStream(new FileInputStream(args[0]));
		List<String> isvuLines = TextService.inputStreamToUTF8StringList(is);
		Map<String,List<String>> studentiBezSatnice = new HashMap<String, List<String>>(100);
		Map<String,String> kolegiji = new HashMap<String, String>(100);
		
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(args[2])),"UTF-8"));
		for(String line : isvuLines) {
			String[] elems = TextService.split(line, '#');
			String kljuc = elems[2]+"/"+elems[4]; 
			kljuc = grupeBezSatnice.get(kljuc);
			if(kljuc==null){
				bw.write(line);
				bw.write("\r\n");
				continue;
			}
			List<String> list = studentiBezSatnice.get(kljuc);
			if(list==null) {
				list = new ArrayList<String>();
				studentiBezSatnice.put(kljuc, list);
				kolegiji.put(kljuc, elems[6]);
			}
			list.add(elems[0]);
			elems[4]="";
			bw.write(elems[0]);
			for(int i = 1; i < elems.length; i++) {
				bw.write('#');
				bw.write(elems[i]);
			}
			bw.write("\r\n");
		}
		bw.flush();
		bw.close();

		List<String> keys = new ArrayList<String>(grupeBezSatnice.keySet());
		Collections.sort(keys);
		for(String kljuc : keys) {
			List<String> studenti = studentiBezSatnice.get(kljuc);
			if(studenti==null) {
				System.out.println(kljuc+": 0");
			} else {
				System.out.println(kljuc+": "+studenti.size()+" ("+kolegiji.get(kljuc)+")");
			}
		}
	}

}
