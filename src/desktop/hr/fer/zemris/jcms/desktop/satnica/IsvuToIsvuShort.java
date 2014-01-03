package hr.fer.zemris.jcms.desktop.satnica;

import hr.fer.zemris.jcms.parsers.TextService;
import hr.fer.zemris.util.StringUtil;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.List;

/**
 * Konverzija ISVU datoteke u sazetu ISVU datoteku.
 * 
 * @author marcupic
 *
 */
public class IsvuToIsvuShort {

	public static void main(String[] args) throws IOException {
		if(args.length != 2) {
			System.out.println("Ocekivao sam dva argumenta:");
			System.out.println("DopunaGrupa in_ISVU out_kratkiISVU");
			System.exit(1);
		}
		List<String> retci = TextService.inputStreamToUTF8StringList(new FileInputStream(args[0]));
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(args[1]),"UTF-8"));
		for(String line : retci) {
			String[] el = StringUtil.split(line, '#');
			bw.write(el[0]);
			bw.write("#");
			bw.write(el[2]);
			bw.write("#");
			bw.write(el[4]);
			bw.write("\r\n");
		}
		bw.close();
	}
}
