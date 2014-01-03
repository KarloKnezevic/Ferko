package hr.fer.zemris.jcms.web.actions.data;

import hr.fer.zemris.jcms.beans.AssessmentFlagBean;
import hr.fer.zemris.jcms.beans.StringNameStringValue;
import hr.fer.zemris.jcms.model.AssessmentFlagTag;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

import java.util.Collections;
import java.util.List;

/**
 * Podatkovna struktura za akciju {@link AdminAssessmentFlagEdit}.
 *  
 * @author marcupic
 *
 */
public class AdminAssessmentFlagEditData extends BaseAssessmentFlag {
	
	private AssessmentFlagBean bean = new AssessmentFlagBean();
	private List<AssessmentFlagTag> tags = Collections.emptyList();
	private List<StringNameStringValue> visibilities;

	/**
	 * Konstruktor.
	 * @param messageLogger lokalizirane poruke
	 */
	public AdminAssessmentFlagEditData(IMessageLogger messageLogger) {
		super(messageLogger);
	}
	
	public List<AssessmentFlagTag> getTags() {
		return tags;
	}
	public void setTags(List<AssessmentFlagTag> tags) {
		this.tags = tags;
	}

	public List<StringNameStringValue> getVisibilities() {
		return visibilities;
	}
	public void setVisibilities(List<StringNameStringValue> visibilities) {
		this.visibilities = visibilities;
	}
	
    public AssessmentFlagBean getBean() {
		return bean;
	}
    public void setBean(AssessmentFlagBean bean) {
		this.bean = bean;
	}

}
