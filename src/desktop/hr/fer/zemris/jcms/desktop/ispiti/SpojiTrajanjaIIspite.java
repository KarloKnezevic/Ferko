package hr.fer.zemris.jcms.desktop.ispiti;

import hr.fer.zemris.jcms.parsers.TextService;
import hr.fer.zemris.util.StringUtil;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>Pomocni razred koji uzima datoteku s generiranim rasporedom ispita i datoteku s unesenim
 * trajanjima ispita, i generira novu datoteku s rasporedom ispita u kojoj trajanje vise nije
 * 2 (defaultno) vec ono navedeno u datoteci s trajanjima.</p>
 * <p>Teoretski, programu se moze zadati da je ulazna datoteka i izlazna ista, jer ce
 * najprije procitati ulaz, pa tek tada krenuti u stvaranje izlaza; medutim, u slucaju pogreske,
 * moguce je da se datoteka unisti, pa je bolje koristiti razlicite datoteke za ulaz i izlaz.</p>
 * <p>Pokretanje:</p>
 * <p><code> SpojiTrajanjaIIspite in_rasporedIspita in_trajanjaIspita in_kratkaOznakaIspita out_izlazniRaspored</code></p>
 * 
 * @author marcupic
 *
 */
public class SpojiTrajanjaIIspite {

	public static void main(String[] args) throws IOException {
		if(args.length!=4) {
			System.out.println("Pogrešan poziv programa. Očekivao sam:");
			System.out.println(" SpojiTrajanjaIIspite in_rasporedIspita in_trajanjaIspita in_kratkaOznakaIspita out_izlazniRaspored");
			System.exit(-1);
		}
		
		String inScheduleFile = args[0];
		String inExamDurationsFile = args[1];
		String targetExamShortName = args[2];
		String outScheduleFile = args[3];

		List<String> list = TextService.inputStreamToUTF8StringList(new FileInputStream(inExamDurationsFile));
		Map<String, Integer> durations = new HashMap<String, Integer>(200);
		for(String line : list) {
			String[] elems = StringUtil.split(line, '\t');
			String isvu = elems[0];
			String examShortName = elems[1];
			String duration = elems[2];
			if(!targetExamShortName.equals(examShortName)) continue;
			durations.put(isvu, Integer.valueOf(duration));
		}

		list = TextService.inputStreamToUTF8StringList(new FileInputStream(inScheduleFile));
		BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream(outScheduleFile)),"UTF-8"));
		for(String line : list) {
			String[] elems = StringUtil.split(line, '\t');
			String date = elems[0];
			String startsAt = elems[1];
			String name = elems[3];
			String isvu = elems[4];
			Integer dur = durations.get(isvu);
			if(dur==null) {
				dur = Integer.valueOf(120);
				System.out.println("Nemam podataka za ispit: ("+isvu+") "+name+". Koristim 120 minuta.");
			}
			bw.write(date);
			bw.write("\t");
			bw.write(startsAt);
			bw.write("\t");
			bw.write(dur.toString());
			bw.write("m\t");
			bw.write(name);
			bw.write("\t");
			bw.write(isvu);
			bw.write("\r");
		}
		bw.flush();
		bw.close();
	}
	
}
