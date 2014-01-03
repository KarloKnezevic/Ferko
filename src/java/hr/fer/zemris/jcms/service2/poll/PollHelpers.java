package hr.fer.zemris.jcms.service2.poll;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import hr.fer.zemris.jcms.model.poll.MultiChoiceQuestion;
import hr.fer.zemris.jcms.model.poll.Option;
import hr.fer.zemris.jcms.model.poll.Question;
import hr.fer.zemris.jcms.model.poll.SingleChoiceQuestion;
import hr.fer.zemris.jcms.model.poll.TextQuestion;

public class PollHelpers {

	public static String getJSONQuestionsDescription(Set<Question> questions) {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		for(Question q : questions) {
			sb.append("\n{");
			sb.append("\"question\":\"").append(q.getQuestionText()).append("\",");
			sb.append("\"type\":\"");
			if(q instanceof TextQuestion) {
				sb.append("bigText\", ");
			}
			if(q instanceof SingleChoiceQuestion) {
				if(q.getValidation() != null && q.getValidation().equals("rating")) {
					sb.append("rating\",");
				} else {
					sb.append("singleChoice");
					sb.append("\", \"options\":[");
					List<Option> options = new ArrayList<Option>(((SingleChoiceQuestion)q).getOptions());
					Collections.sort(options, new Comparator<Option>() {
						@Override
						public int compare(Option o1, Option o2) {
							return o1.getOrdinal().compareTo(o2.getOrdinal());
						}
					});
					for(Option o : options) {
						sb.append("\"").append(o.getText()).append("\",");
					}
					sb.deleteCharAt(sb.length()-1);
					sb.append("],");
				}
			}
			if(q instanceof MultiChoiceQuestion) {
				sb.append("multiChoice");
				sb.append("\", \"options\":[");
				List<Option> options = new ArrayList<Option>(((MultiChoiceQuestion)q).getOptions());
				Collections.sort(options, new Comparator<Option>() {
					@Override
					public int compare(Option o1, Option o2) {
						return o1.getOrdinal().compareTo(o2.getOrdinal());
					}
				});
				for(Option o : options) {
					sb.append("\"").append(o.getText()).append("\",");
				}
				sb.deleteCharAt(sb.length()-1);
				sb.append("],");
			}
			sb.append("\"ordinal\":").append(q.getOrdinal());
			sb.append("},");
		}
		sb.deleteCharAt(sb.length()-1);
		sb.append("]");
		return sb.toString();
	}
	
	@SuppressWarnings("unchecked")
	public static String getJSONQuestionsDescription(HttpServletRequest request) {
		Map<Integer, String> questionType = new HashMap<Integer, String>();
		Map<Integer, String> questionText = new HashMap<Integer, String>();
		Map<Integer, String> questionOptions = new HashMap<Integer, String>();
		
    	String name = null;
    	String[] nameParts = null;
    	Enumeration<String> e = request.getParameterNames();
    	while(e.hasMoreElements()) {
    		name = (String)e.nextElement();
    		if(name.startsWith("question_")) {
    			nameParts = name.split("_");
    			if(nameParts[2].equals("text")) {
    				questionText.put(Integer.parseInt(nameParts[1]), request.getParameter(name));
    			}
    			if(nameParts[2].equals("type")) {
    				questionType.put(Integer.parseInt(nameParts[1]), request.getParameter(name));
    			}
    			if(nameParts[2].equals("options")) {
    				questionOptions.put(Integer.parseInt(nameParts[1]), request.getParameter(name));
    			}
    		}
    	}
    	
    	return getJSONQuestionsDescription(questionType, questionText, questionOptions);
	}
	
	public static String getJSONQuestionsDescription(Map<Integer, String> types, Map<Integer, String> texts, Map<Integer, String> options) {
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		List<Integer> keys = new ArrayList<Integer>(types.keySet());
		Collections.sort(keys);
		for(Integer i : keys) {
			sb.append("\n{");
			sb.append("\"question\":\"").append(texts.get(i)).append("\",");
			String type = types.get(i);
			sb.append("\"type\":\"").append(type).append("\",");
			if(type.equals("singleChoice") || type.equals("multiChoice")) {
				sb.append("\"options\":[");
				String[] opts = options.get(i).split("\n");
				for(String o : opts) {
					o = o.trim();
					if(o.length()>0) {
						sb.append("\"").append(o).append("\",");
					}
				}
				sb.deleteCharAt(sb.length()-1);
				sb.append("],");
			}
			sb.append("\"ordinal\":").append(i);
			sb.append("},");
		}
		if(sb.length()>1) sb.deleteCharAt(sb.length()-1);
		sb.append("]");
		return sb.toString();
	}
}
