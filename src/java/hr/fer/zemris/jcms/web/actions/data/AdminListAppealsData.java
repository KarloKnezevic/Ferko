package hr.fer.zemris.jcms.web.actions.data;

import java.util.Comparator;

import hr.fer.zemris.jcms.model.appeals.AppealInstanceStatus;
import hr.fer.zemris.jcms.model.appeals.AssessmentAppealInstance;
import hr.fer.zemris.jcms.web.actions.AdminListAppeals;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

/**
 * Podatkovna struktura za akciju {@link AdminListAppeals}.
 *  
 * @author Ivan Krišto
 *
 */
public class AdminListAppealsData extends BaseCourseInstance {
	
	private Comparator<AssessmentAppealInstance> statusComp;
	
	/**
	 * Konstruktor.
	 * @param messageLogger lokalizirane poruke
	 */
	public AdminListAppealsData(IMessageLogger messageLogger) {
		super(messageLogger);
		
		statusComp = new Comparator<AssessmentAppealInstance>() {
		
			@Override
			public int compare(AssessmentAppealInstance o1, AssessmentAppealInstance o2) {
				if (o1.getStatus().equals(o2.getStatus())) {
					return o2.getCreationDate().compareTo(o1.getCreationDate());
				} else {
					if (o1.getStatus().equals(AppealInstanceStatus.OPENED)) {
						return -1;
					} else if (o1.getStatus().equals(AppealInstanceStatus.LOCKED)) {
						if (o2.getStatus().equals(AppealInstanceStatus.OPENED)) {
							return 1;
						} else {
							return -1;
						}
					} else if (o1.getStatus().equals(AppealInstanceStatus.ACCEPTED)
								|| o1.getStatus().equals(AppealInstanceStatus.MODIFIED_ACCEPTED)) {
						// Neka su accepted i mod_accepted nedefinirano raspoređeni
						if (o2.getStatus().equals(AppealInstanceStatus.OPENED)
								|| o2.getStatus().equals(AppealInstanceStatus.LOCKED)) {
							return 1;
						} else {
							return -1;
						}
					} else if (o1.getStatus().equals(AppealInstanceStatus.REJECTED)) {
							return 1;
					}
					return 0;
				}
			}
		};
	}
	
	public void setStatusComp() {
	}
	
	public Comparator<AssessmentAppealInstance> getStatusComp() {
		return statusComp;
	}
}
