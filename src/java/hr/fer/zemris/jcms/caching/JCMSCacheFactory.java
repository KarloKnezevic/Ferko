package hr.fer.zemris.jcms.caching;

import hr.fer.zemris.jcms.caching.impl.JCMSCacheOSImpl;

import java.io.IOException;

public class JCMSCacheFactory {
	
	private static IJCMSCache cache;
	
	public static IJCMSCache getCache() {
		return cache;
	}
	
	public static void init() throws IOException {
		cache = new JCMSCacheOSImpl();
	}
}
