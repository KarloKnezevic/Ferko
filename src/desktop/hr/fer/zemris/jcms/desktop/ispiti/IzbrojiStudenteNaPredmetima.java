package hr.fer.zemris.jcms.desktop.ispiti;

import hr.fer.zemris.jcms.parsers.TextService;
import hr.fer.zemris.util.StringUtil;

import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class IzbrojiStudenteNaPredmetima {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException {
		if(args.length!=2) {
			System.out.println("Pogrešan poziv programa. Očekivao sam:");
			System.out.println(" IzbrojiStudenteNaPredmetima in_projekt out_brojStudenata");
			System.exit(-1);
		}

		String inProject = args[0];
		String outStudCount = args[1];

		List<String> list = TextService.inputStreamToUTF8StringList(new FileInputStream(inProject));
		Map<String, Integer> counts = new LinkedHashMap<String, Integer>(200);

		int brojPredmeta = Integer.parseInt(list.get(0));
		for(int i = 1; i<=brojPredmeta; i++) {
			String[] elems = StringUtil.split(list.get(i), '#');
			String isvu = elems[0];
			String[] jmbags = StringUtil.split(elems[2], ',');
			int broj = 0;
			for(int j = 0; j < jmbags.length; j++) {
				if(!jmbags[j].isEmpty()) broj++;
			}
			counts.put(isvu, Integer.valueOf(broj));
		}
		
		BufferedWriter bw = new BufferedWriter(new FileWriter(outStudCount));
		for(Map.Entry<String,Integer> e : counts.entrySet()) {
			bw.write(e.getKey());
			bw.write('\t');
			bw.write(e.getValue().toString());
			bw.write("\r\n");
		}
		bw.close();
	}

}
