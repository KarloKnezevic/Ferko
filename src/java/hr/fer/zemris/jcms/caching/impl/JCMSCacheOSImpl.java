package hr.fer.zemris.jcms.caching.impl;

import hr.fer.zemris.jcms.JCMSSettings;
import hr.fer.zemris.jcms.beans.cached.CourseScoreTable;
import hr.fer.zemris.jcms.beans.cached.Dependencies;
import hr.fer.zemris.jcms.caching.IJCMSCache;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import com.opensymphony.oscache.base.NeedsRefreshException;
import com.opensymphony.oscache.general.GeneralCacheAdministrator;

public class JCMSCacheOSImpl implements IJCMSCache {

	private GeneralCacheAdministrator gcaScoreTable;
	private GeneralCacheAdministrator gcaDependencies;
	
	public JCMSCacheOSImpl(Properties p) {
		gcaScoreTable = new GeneralCacheAdministrator(p);
		gcaDependencies = new GeneralCacheAdministrator(p);
	}
	
	public JCMSCacheOSImpl() throws IOException {
		File rootDir = JCMSSettings.getSettings().getRootDir();
		File cacheDir = new File(rootDir,"jcms-oscache");
		cacheDir.mkdir();
		createCacheScoreTable(cacheDir);
		createCacheDependencies(cacheDir);
	}

	private void createCacheScoreTable(File cacheDir) throws IOException {
		File cacheDir1 = new File(cacheDir,"scoreTables");
		cacheDir1.mkdir();
		Properties p = new Properties();
		p.setProperty("cache.memory", "true");
		p.setProperty("cache.persistence.class", "com.opensymphony.oscache.plugins.diskpersistence.DiskPersistenceListener");
		p.setProperty("cache.path", cacheDir1.getCanonicalPath());
		p.setProperty("cache.persistence.overflow.only", "false");
		p.setProperty("cache.algorithm", "com.opensymphony.oscache.base.algorithm.LRUCache");
		p.setProperty("cache.blocking", "false");
		p.setProperty("cache.capacity", "2");
		p.setProperty("cache.unlimited.disk", "true");
		gcaScoreTable = new GeneralCacheAdministrator(p);
	}

	private void createCacheDependencies(File cacheDir) throws IOException {
		File cacheDir1 = new File(cacheDir,"dependencies");
		cacheDir1.mkdir();
		Properties p = new Properties();
		p.setProperty("cache.memory", "true");
		p.setProperty("cache.persistence.class", "com.opensymphony.oscache.plugins.diskpersistence.DiskPersistenceListener");
		p.setProperty("cache.path", cacheDir1.getCanonicalPath());
		p.setProperty("cache.persistence.overflow.only", "false");
		p.setProperty("cache.algorithm", "com.opensymphony.oscache.base.algorithm.LRUCache");
		p.setProperty("cache.blocking", "false");
		p.setProperty("cache.capacity", "100");
		p.setProperty("cache.unlimited.disk", "true");
		gcaDependencies = new GeneralCacheAdministrator(p);
	}

	@Override
	public CourseScoreTable getCourseScoreTable(String courseInstanceID) {
		String key = "CST:"+courseInstanceID;
		CourseScoreTable table;
		try {
			table = (CourseScoreTable)gcaScoreTable.getFromCache(key);
		} catch (NeedsRefreshException e) {
			table = (CourseScoreTable)e.getCacheContent();
			gcaScoreTable.cancelUpdate(key);
		}
		return table;
	}

	@Override
	public void put(CourseScoreTable table) {
		gcaScoreTable.putInCache("CST:"+table.getCourseInstanceID(), table);
	}
	
	@Override
	public Dependencies getDependencies(String courseInstanceID) {
		String key = "DEP:"+courseInstanceID;
		Dependencies dep;
		try {
			dep = (Dependencies)gcaDependencies.getFromCache(key);
		} catch (NeedsRefreshException e) {
			dep = (Dependencies)e.getCacheContent();
			gcaDependencies.cancelUpdate(key);
		}
		return dep;
	}
	
	@Override
	public void put(Dependencies dependencies) {
		gcaDependencies.putInCache("DEP:"+dependencies.getCourseInstanceID(), dependencies);
	}
	
}
