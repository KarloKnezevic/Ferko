package hr.fer.zemris.jcms.web.actions.data.support;

import hr.fer.zemris.jcms.model.poll.MultiChoiceQuestion;
import hr.fer.zemris.jcms.model.poll.Option;
import hr.fer.zemris.jcms.model.poll.Poll;
import hr.fer.zemris.jcms.model.poll.Question;
import hr.fer.zemris.jcms.model.poll.SingleChoiceQuestion;
import hr.fer.zemris.jcms.model.poll.TextQuestion;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

public class PollForm {
	
	private Poll poll;
	private HttpServletRequest request = null;
	private Map<String,String> errors = new HashMap<String, String>();
	
	/**
	 * 
	 * @param poll Poll mora imati fetchane sve questions i options!
	 */
	public PollForm(Poll poll) {
		super();
		this.poll = poll;
	}

	public PollForm(Poll poll, HttpServletRequest request) {
		super();
		this.poll = poll;
		this.request = request;
		validate();
	}
	
	public void validate() {
		List<Question> questions = new LinkedList<Question>(poll.getQuestions());
		for(Question question : questions) {
			String validation = question.getValidation();
			if(validation.length()>0) {
				if(question instanceof TextQuestion) {
					if(validation.startsWith("regex:")) {
						String regex = validation.split(":")[1];
						if(!request.getParameter("question_"+question.getId()).matches(regex)) {
							errors.put("question_"+question.getId(), "Krivi format!"); // TODO: i18n?
						}
					}
					if("required".equals(validation) && request.getParameter("question_"+question.getId()).length()==0) {
						errors.put("question_"+question.getId(), "Ovo treba ispuniti!"); // TODO: i18n?
					}
				}
				if(question instanceof SingleChoiceQuestion) {
					if("required".equals(validation) && request.getParameter("question_"+question.getId())==null) {
						errors.put("question_"+question.getId(), "Ovo treba ispuniti!"); // TODO: i18n?
					}
				}
				if(question instanceof MultiChoiceQuestion) {
					if(validation.matches("\\d+,\\d+")) {
						int selected = 0;
						if(request.getParameterValues("question_"+question.getId())!=null) {
							selected = request.getParameterValues("question_"+question.getId()).length;
						}
						int min = Integer.parseInt(validation.split(",")[0]);
						int max = Integer.parseInt(validation.split(",")[1]);
						if(selected > max || selected < min) {
							errors.put("question_"+question.getId(), "Treba označiti najmanje " + min + ", a najviše "+max+".");
						}
					}
				}
			}
		}
	}
	
	public boolean isValid() {
		if(request==null) {
			return false;
		} else {
			return errors.isEmpty();
		}
	}

	public String getHtml() {
		StringBuilder form = new StringBuilder();
		//form.append("<fieldset>");
		//form.append("<ol>");
		List<Question> questions = new LinkedList<Question>(poll.getQuestions());
		Collections.sort(questions, new Comparator<Question>() {
			public int compare(Question arg0, Question arg1) {
				if(arg0 == null || arg1 == null) return 0;
				return arg0.getOrdinal().compareTo(arg1.getOrdinal());
			}
		});
		for(Question question : questions) {
			if(errors.get("question_"+question.getId())!=null) {
				form.append("<li class=\"formError\">");
			} else {
				form.append("<li>\n");
			}
			if(question instanceof TextQuestion) {
				form.append("<label for=\"question")
				    .append(question.getId())
				    .append("\">\n")
				    .append(question.getQuestionText());
				if("required".equals(question.getValidation())) {
					form.append(" <span class=\"required\">*</span>");
				}
				form.append("</label>\n");
				if(!question.getValidation().equals("bigtext")) {
					form.append("<input type=\"text\" id=\"question")
						.append(question.getId())
						.append("\" name=\"question_")
						.append(question.getId());
					if(request!=null) {
						if(request.getParameter("question_"+question.getId())!=null) {
							form.append("\" value=\"")
								.append(request.getParameter("question_"+question.getId()));
						}
					}
					form.append("\" />\n");
				} else {
					form.append("<textarea rows=\"5\" id=\"question")
						.append(question.getId())
						.append("\" name=\"question_")
						.append(question.getId())
						.append("\">");
					if(request!=null) {
						if(request.getParameter("question_"+question.getId())!=null) {
							form.append(request.getParameter("question_"+question.getId()));
						}
					}
					form.append("</textarea>\n");
				}
			}
			if(question instanceof SingleChoiceQuestion || question instanceof MultiChoiceQuestion) {
				if("rating".equals(question.getValidation())) {
					form.append("<fieldset class=\"rating\">\n");
				} else {
					form.append("<fieldset>\n");
				}
				form.append("<legend>").append(question.getQuestionText());
				if("required".equals(question.getValidation())) {
					form.append(" <span class=\"required\">*</span>");
				}
				form.append("</legend>\n");
				List<Option> options = null;
				if(question instanceof SingleChoiceQuestion) {
					options = new LinkedList<Option>(((SingleChoiceQuestion)question).getOptions());
				}
					
				if(question instanceof MultiChoiceQuestion) {
					options = new LinkedList<Option>(((MultiChoiceQuestion)question).getOptions());					
				}
				Collections.sort(options, new Comparator<Option>() {
					public int compare(Option arg0, Option arg1) {
						if(arg0 == null || arg1 == null) return 0;
						return arg0.getOrdinal().compareTo(arg1.getOrdinal());
					}	
				});
				for(Option option : options) {
					form.append("<label>\n")
						.append("<input name=\"question_")
						.append(question.getId())
						.append("\" value=\"")
						.append(option.getId())
						.append("\" type=\"");
					if(question instanceof MultiChoiceQuestion) {
						form.append("checkbox\"");
					}
					if(question instanceof SingleChoiceQuestion) {
						form.append("radio\"");
					}
					if(request!=null) {
						if(request.getParameterValues("question_"+question.getId())!=null) {
							for(String value : request.getParameterValues("question_"+question.getId())) {
								if(value.equals(option.getId().toString())) {
									form.append(" checked=\"checked\" ");
									break;
								}
							}
						}
					}
					form.append(" /> ")
						.append(option.getText())
						.append("</label>\n");
				}
				form.append("</fieldset>\n");
			}
			if(errors.get("question_"+question.getId())!=null) {
				form.append("<span>")
					.append(errors.get("question_"+question.getId()))
					.append("</span>");
			}
			form.append("</li>\n");
		}
		//form.append("</ol>");
		//form.append("</fieldset>");
		return form.toString();
	}
	
	
}
