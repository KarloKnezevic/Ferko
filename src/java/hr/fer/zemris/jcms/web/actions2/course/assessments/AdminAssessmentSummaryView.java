package hr.fer.zemris.jcms.web.actions2.course.assessments;

import hr.fer.zemris.jcms.service2.course.assessments.SummaryViewService;
import hr.fer.zemris.jcms.web.actions.Ext2ActionSupport;
import hr.fer.zemris.jcms.web.actions.annotations.WebClass;
import hr.fer.zemris.jcms.web.actions.annotations.WebMethodInfo;
import hr.fer.zemris.jcms.web.actions.data.AdminAssessmentSummaryViewData;

/**
 * Akcija koja renderira prikaz svih rezultata provjera na kolegiju.
 * 
 * @author marcupic
 *
 */
@WebClass(dataClass=AdminAssessmentSummaryViewData.class)
public class AdminAssessmentSummaryView extends Ext2ActionSupport<AdminAssessmentSummaryViewData> {

	private static final long serialVersionUID = 2L;

	@WebMethodInfo
	public String execute() throws Exception {
		SummaryViewService.showAdminAssessmentSummaryView(getEntityManager(), data);
        return null;
    }

    public String getCourseInstanceID() {
		return data.getCourseInstanceID();
	}
    public void setCourseInstanceID(String courseInstanceID) {
		data.setCourseInstanceID(courseInstanceID);
	}
    
    public String getSortKey() {
		return data.getSortKey();
	}
    public void setSortKey(String sortKey) {
		data.setSortKey(sortKey);
	}

    public Long getSelectedGroup() {
		return data.getSelectedGroupID();
	}
    public void setSelectedGroup(Long selectedGroup) {
		data.setSelectedGroupID(selectedGroup);
	}
}
