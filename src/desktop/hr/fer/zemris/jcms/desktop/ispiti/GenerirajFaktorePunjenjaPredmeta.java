package hr.fer.zemris.jcms.desktop.ispiti;

import hr.fer.zemris.jcms.parsers.TextService;

import hr.fer.zemris.util.StringUtil;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class GenerirajFaktorePunjenjaPredmeta {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException {
		if(args.length!=2) {
			System.out.println("Pogrešan poziv programa. Očekivao sam:");
			System.out.println(" GenerirajFaktorePunjenjaPredmeta in_projekt out_faktori");
			System.exit(-1);
		}

		String inProject = args[0];
		String outFaktori = args[1];

		List<String> list = TextService.inputStreamToUTF8StringList(new FileInputStream(inProject));
		Set<String> courses = new LinkedHashSet<String>(200);

		int brojPredmeta = Integer.parseInt(list.get(0));
		for(int i = 1; i<=brojPredmeta; i++) {
			String[] elems = StringUtil.split(list.get(i), '#');
			String isvu = elems[0];
			courses.add(isvu);
		}
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(outFaktori));
		for(String isvu : courses) {
			bw.write(isvu);
			bw.write('\t');
			bw.write("1\r\n");
		}
		bw.close();
	}

}
