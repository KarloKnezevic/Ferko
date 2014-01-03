package hr.fer.zemris.jcms.beans;

import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import hr.fer.zemris.jcms.model.poll.MultiChoiceQuestion;
import hr.fer.zemris.jcms.model.poll.Question;
import hr.fer.zemris.jcms.model.poll.SingleChoiceQuestion;
import hr.fer.zemris.jcms.model.poll.TextAnswer;
import hr.fer.zemris.jcms.model.poll.TextQuestion;

public class PollQuestionBean extends Question implements Comparable<PollQuestionBean> {

	private static final long serialVersionUID = 1L;
	
	private String type = null;
	private List<TextAnswerBean> textAnswers;
	private Set<PollOptionBean> optionAnswers;

	
	public PollQuestionBean(Question question) {
		setId(question.getId());
		setQuestionText(question.getQuestionText());
		setOrdinal(question.getOrdinal());
		setValidation(question.getValidation());
		setPoll(null);
		if(question instanceof TextQuestion) {
			setType("TEXT");
			textAnswers = new LinkedList<TextAnswerBean>();
		}else if(question instanceof SingleChoiceQuestion || question instanceof MultiChoiceQuestion){
			optionAnswers = new LinkedHashSet<PollOptionBean>();
			setType("OPTION");
			if("rating".equals(question.getValidation())) {
				setType("RATING");
			}
		};
	}

	public String getType() {
		return type;
	}

	public Double getAvgRating() {
		if(!"RATING".equals(getType())) return null;
		int sum = 0;
		int br = 0;
		for(PollOptionBean o : getOptionAnswers()) {
			sum += o.getOrdinal()*o.getCounter();
			br += o.getCounter();
		}
		if(br==0) return Double.valueOf((double)0);
		return Double.valueOf(((double)sum)/((double)br));
	}
	
	public void setType(String type) {
		this.type = type;
	}

	public List<TextAnswerBean> getTextAnswers() {
		return textAnswers;
	}

	public void addTextAnswer(TextAnswer answer) {
		if(textAnswers == null) return;
		textAnswers.add(new TextAnswerBean(answer));
	}
	
	public void addOptionBean(PollOptionBean bean) {
		if(optionAnswers == null) return;
		optionAnswers.add(bean);
	}
	
	public Set<PollOptionBean> getOptionAnswers() {
		return optionAnswers;
	}

	@Override
	public int compareTo(PollQuestionBean o) {
		return getOrdinal().compareTo(o.getOrdinal());
	}

}
