package hr.fer.zemris.jcms.caching;

import hr.fer.zemris.jcms.beans.cached.CourseScoreTable;
import hr.fer.zemris.jcms.beans.cached.Dependencies;

public interface IJCMSCache {

	public void put(CourseScoreTable table);
	public CourseScoreTable getCourseScoreTable(String courseInstanceID);
	
	public void put(Dependencies dependencies);
	public Dependencies getDependencies(String courseInstanceID);
}
