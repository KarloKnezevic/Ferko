package hr.fer.zemris.jcms.parsers;

import hr.fer.zemris.jcms.beans.ext.ComponentDefBean;

import java.io.IOException;
import java.io.Reader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.MatchResult;

public class CourseComponentDefParser {
	
	public static List<ComponentDefBean> parse(Reader is) throws IOException, ParseException {
		List<String> lines = TextService.readerToStringList(is);
		List<ComponentDefBean> resultList = new ArrayList<ComponentDefBean>(lines.size());
		
		if(lines.isEmpty()) return resultList;
		
		for (String line : lines) {
			String p = "^boolean\\s+([^{]{1,5}+)\\{([^}]+)\\}$";
			String p1 = "^double\\s+([^{]{1,5}+)\\{([^}]+)\\}\\s+enum\\s+([^\\s]+)\\s+to\\s+([^\\s]+)\\s+step\\s+([^\\s]+)$";
			String p2 = "^double\\s+([^{]{1,5}+)\\{([^}]+)\\}\\s+range\\s+([^\\s]+)\\s+to\\s+([^\\s]+)$";
			Scanner s = new Scanner(line);
			ComponentDefBean bean = new ComponentDefBean();
			try {
				if (s.findInLine(p)!=null) {
					MatchResult result = s.match();
					bean.setType("boolean");
					bean.setShortName(result.group(1));
					bean.setName(result.group(2));
					s.close(); 
				}
				else if (s.findInLine(p1)!=null) {
					MatchResult result = s.match();
					bean.setType("enum");
					bean.setShortName(result.group(1));
					bean.setName(result.group(2));
					double start = Double.valueOf(result.group(3));
					double end = Double.valueOf(result.group(4));
					double step = Double.valueOf(result.group(5));
					if (start>=end || step<0 || (end-start)/step<1)
						throw new Exception();
					bean.setStart(start);
					bean.setEnd(end);
					bean.setStep(step);
					s.close();
				}
				else if (s.findInLine(p2)!=null) {
					MatchResult result = s.match();
					bean.setType("range");
					bean.setShortName(result.group(1));
					bean.setName(result.group(2));
					double start = Double.valueOf(result.group(3));
					double end = Double.valueOf(result.group(4));
					if (start>=end)
						throw new Exception();

					bean.setStart(Double.valueOf(result.group(3)));
					bean.setEnd(Double.valueOf(result.group(4)));
					s.close();
				}
				else
					throw new Exception();
				resultList.add(bean);
			} catch (Exception e) {
				throw new ParseException("Found unexpected row: "+line,0);
			}
		
		}
		
		return resultList;
	}
}
