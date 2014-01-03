package hr.fer.zemris.jcms.desktop.satnica;

import hr.fer.zemris.util.StringUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.Map;

public class DiffGrupa {

	/**
	 * @param args
	 */
	public static void main(String[] args) throws IOException {
		
		Map<String,String[]> map = new HashMap<String, String[]>(20000);
		
		BufferedReader r = new BufferedReader(new InputStreamReader(new FileInputStream(args[0]),"UTF-8"));
		while(true) {
			String l = r.readLine();
			if(l==null) break;
			if(StringUtil.isStringBlank(l)) continue;
			String[] elems = StringUtil.split(l, '#');
			String key = elems[0]+"/"+elems[2];
			//String value = elems[4];
			map.put(key, elems);
		}
		r.close();
		System.out.println("Imam "+map.size()+" elemenata.");
		
		BufferedWriter w = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(args[2]),"UTF-8"));
		r = new BufferedReader(new InputStreamReader(new FileInputStream(args[1]),"UTF-8"));
		while(true) {
			String l = r.readLine();
			if(l==null) break;
			if(StringUtil.isStringBlank(l)) continue;
			String[] elems = StringUtil.split(l, '#');
			String key = elems[0]+"/"+elems[1];
			String value = elems[3];
			String[] oldValue = map.get(key);
			if(!StringUtil.stringEquals(oldValue[4], value)) {
				// Imam razliku!
				oldValue[4] = value;
				w.write(oldValue[0]);
				for(int i = 1; i < oldValue.length; i++) {
					w.write('#');
					w.write(oldValue[i]);
				}
				w.write("\r\n");
			}
		}
		w.flush();
		w.close();
		r.close();
	}

}
