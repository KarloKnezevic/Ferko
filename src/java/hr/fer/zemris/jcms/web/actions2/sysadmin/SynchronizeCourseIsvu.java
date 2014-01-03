package hr.fer.zemris.jcms.web.actions2.sysadmin;

import hr.fer.zemris.jcms.service2.sysadmin.CourseDataService;
import hr.fer.zemris.jcms.web.actions.Ext2ActionSupport;
import hr.fer.zemris.jcms.web.actions.annotations.WebClass;
import hr.fer.zemris.jcms.web.actions.annotations.WebMethodInfo;
import hr.fer.zemris.jcms.web.actions.data.SynchronizeCourseIsvuData;
import hr.fer.zemris.jcms.web.navig.builders.MainBuilder;

import java.io.File;

@WebClass(dataClass=SynchronizeCourseIsvuData.class,defaultNavigBuilder=MainBuilder.class, defaultNavigBuilderIsRoot=false,additionalMenuItems={"m2","Navigation.syncCourseISVU"})
public class SynchronizeCourseIsvu extends Ext2ActionSupport<SynchronizeCourseIsvuData> {

	private static final long serialVersionUID = 2L;
	
	@WebMethodInfo(lockPath="ml")
    public String upload() throws Exception {
		CourseDataService.importISVUZip(getEntityManager(), data);
		return null;
    }

	@WebMethodInfo
    public String execute() throws Exception {
    	return input();
    }

	@WebMethodInfo
    public String input() throws Exception {
		CourseDataService.prepareImportISVUZip(getEntityManager(), data);
		return null;
    }

    public String getSemester() {
		return data.getSemester();
	}
    public void setSemester(String semester) {
		data.setSemester(semester);
	}
    
    public File getArchive() {
		return data.getArchive();
	}
    public void setArchive(File archive) {
		data.setArchive(archive);
	}

    public String getArchiveContentType() {
		return data.getArchiveContentType();
	}
    public void setArchiveContentType(String archiveContentType) {
		data.setArchiveContentType(archiveContentType);
	}

    public String getArchiveFileName() {
		return data.getArchiveFileName();
	}
    public void setArchiveFileName(String archiveFileName) {
		data.setArchiveFileName(archiveFileName);
	}

}
