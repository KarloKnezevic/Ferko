package hr.fer.zemris.jcms.parsers;

import java.text.ParseException;
import java.util.ArrayList;

public class CSVRowSplitter {

	public static String[] split(String row) throws ParseException {
		ArrayList<String> elems = new ArrayList<String>();
		int poc = 0;
		int curr = 0;
		char[] arr = row.toCharArray();
		while(curr < arr.length) {
			poc = curr;
			if(arr[curr]=='"') {
				curr++;
				while(true) {
					while(arr[curr]!='"') curr++;
					if(curr+1<arr.length && arr[curr+1]=='"') {
						curr += 2;
					} else break;
				}
				elems.add(new String(arr,poc+1,curr-poc-1));
				curr++;
				if(curr>=arr.length) break;
				if(arr[curr]!=';') {
					throw new ParseException("Neočekivani znak na poziciji "+curr+" kod parsiranja CSV retka.", curr);
				}
				curr++;
				if(curr>=arr.length) {
					elems.add("");
					break;
				}
				continue;
			}
			curr++;
			while(curr<arr.length && arr[curr]!=';') curr++;
			elems.add(new String(arr,poc,curr-poc));
			if(curr>=arr.length) {
				break;
			}
			curr++;
			if(curr>=arr.length) {
				elems.add("");
				break;
			}
			continue;
		}
		String[] result = new String[elems.size()];
		elems.toArray(result);
		return result;
	}
}
