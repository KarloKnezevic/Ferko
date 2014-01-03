package hr.fer.zemris.jcms.activities.impl;

import hr.fer.zemris.jcms.activities.IActivityReporter;
import hr.fer.zemris.jcms.activities.IActivityServiceProvider;
import hr.fer.zemris.jcms.activities.IActivityWorker;

public class ActivityServiceProviderImpl implements IActivityServiceProvider {
	
	private ActivityReporterImpl reporter;
	private ActivityWorkerImpl worker;
	
	public ActivityServiceProviderImpl() {
		worker = new ActivityWorkerImpl();
		reporter = new ActivityReporterImpl(worker);
	}
	
	public IActivityReporter getActivityReporter() {
		return reporter;
	}
	
	public IActivityWorker getActivityWorker() {
		return worker;
	}

	
}
