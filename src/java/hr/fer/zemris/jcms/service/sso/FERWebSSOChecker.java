package hr.fer.zemris.jcms.service.sso;

import hr.fer.zemris.jcms.service.reservations.impl.ferweb.FERWebReservationManagerFactory;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.MessageDigest;
import java.util.Date;
import java.util.Properties;

import org.apache.log4j.Logger;

public final class FERWebSSOChecker implements ISSOChecker {

	private static final Logger logger = Logger.getLogger(FERWebSSOChecker.class.getCanonicalName());
	private String appendix = "";
	private final long timeLimit = 6*60;

	public FERWebSSOChecker() {
		InputStream is =  FERWebReservationManagerFactory.class.getClassLoader().getResourceAsStream("sso-ferweb.properties");
		if(is!=null) {
			Properties prop = new Properties();
			try {
				prop.load(new InputStreamReader(is,"UTF-8"));
			} catch (Exception e) {
				logger.error("Error reading sso-ferweb.properties.");
			}
			try { is.close(); } catch(Exception ignorable) {}
			appendix = prop.getProperty("key","");
		} else {
			logger.warn("sso-ferweb.properties not found.");
		}
	}
	
	@Override
	public boolean check(String code, String courseID, String time, String auth) {
		boolean ok = false;
		try {
			long now = new Date().getTime() / 1000;
			long givenTime = Long.parseLong(time);
			if(Math.abs(now-givenTime)>timeLimit) return false;

			String ticket = code+courseID+time+appendix;
			
			MessageDigest algorithm = MessageDigest.getInstance("MD5");
			algorithm.reset();
			algorithm.update(ticket.getBytes("UTF-8"));
	
			byte messageDigest[] = algorithm.digest();
	
			StringBuilder hexString = new StringBuilder();
			for (int i=0;i<messageDigest.length;i++) {
				int c = (messageDigest[i] & 0xF0)>>4;
				if(c>9) {
					hexString.append((char)('a'+c-10));
				} else {
					hexString.append((char)('0'+c));
				}
				c = messageDigest[i] & 0x0F;
				if(c>9) {
					hexString.append((char)('a'+c-10));
				} else {
					hexString.append((char)('0'+c));
				}
			}
			String actualAuth = hexString.toString();
	
			ok = auth.equals(actualAuth);
			//System.out.println("ActualAuth="+actualAuth);
			//System.out.println("Auth      ="+auth);
			//System.out.println("Jednaki? "+ok);
		} catch(Exception ex) {
			ok = false;
			ex.printStackTrace();
		}
		return ok;
	}

}
