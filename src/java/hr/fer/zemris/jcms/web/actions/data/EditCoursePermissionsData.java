package hr.fer.zemris.jcms.web.actions.data;

import hr.fer.zemris.jcms.beans.ext.CoursePermissionBean;
import hr.fer.zemris.jcms.web.actions.EditCoursePermissions;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;

import java.util.List;

/**
 * Podatkovna struktura za akciju {@link EditCoursePermissions}.
 *  
 * @author marcupic
 *
 */
public class EditCoursePermissionsData extends BaseCourseInstance {
	
	List<CoursePermissionBean> availablePermissions;
	
	/**
	 * Konstruktor.
	 * @param messageLogger lokalizirane poruke
	 */
	public EditCoursePermissionsData(IMessageLogger messageLogger) {
		super(messageLogger);
	}

	public List<CoursePermissionBean> getAvailablePermissions() {
		return availablePermissions;
	}
	public void setAvailablePermissions(List<CoursePermissionBean> availablePermissions) {
		this.availablePermissions = availablePermissions;
	}
}
