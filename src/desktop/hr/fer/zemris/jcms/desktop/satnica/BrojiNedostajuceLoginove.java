package hr.fer.zemris.jcms.desktop.satnica;

import hr.fer.zemris.util.StringUtil;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

public class BrojiNedostajuceLoginove {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException {
		
		Set<String> prvaGodina = new HashSet<String>(5000);
		
		BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(args[0]),"UTF-8"));
		while(true) {
			String l = r.readLine();
			if(l==null) break;
			if(StringUtil.isStringBlank(l)) continue;
			String[] elems = StringUtil.split(l, '#');
			String jmbag = elems[0];
			String godina = elems[7];
			if(godina.equals("1")) prvaGodina.add(jmbag);
		}
		r.close();
		System.out.println("Imam "+prvaGodina.size()+" studenata brucosa.");

		Set<String> imamVezu = new HashSet<String>(5000);
		r = new BufferedReader(new InputStreamReader(new FileInputStream(args[1]),"UTF-8"));
		while(true) {
			String l = r.readLine();
			if(l==null) break;
			if(StringUtil.isStringBlank(l)) continue;
			String[] elems = StringUtil.split(l, '#');
			String jmbag = elems[0];
			@SuppressWarnings("unused")
			String login = elems[1];
			imamVezu.add(jmbag);
		}
		r.close();
		System.out.println("Imam "+imamVezu.size()+" povezanih studenata.");

		Set<String> povezanaPrvaGodina = new HashSet<String>(prvaGodina);
		povezanaPrvaGodina.retainAll(imamVezu);
		
		System.out.println("Imam "+povezanaPrvaGodina.size()+" povezanih brucosa studenata.");
		
	}

}
