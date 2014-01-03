package hr.fer.zemris.jcms.service.extsystems;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts2.StrutsStatics;

import com.opensymphony.xwork2.ActionContext;

import hr.fer.zemris.jcms.JCMSSettings;
import hr.fer.zemris.jcms.beans.TestDataBean;
import hr.fer.zemris.jcms.beans.TestInstanceDataBean;
import hr.fer.zemris.jcms.model.Assessment;
import hr.fer.zemris.jcms.model.AssessmentScore;
import hr.fer.zemris.jcms.model.CourseComponentItemAssessment;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;
import hr.fer.zemris.studtest2.comm.Command;
import hr.fer.zemris.studtest2.comm.CommandComposer;
import hr.fer.zemris.studtest2.comm.CommandComposerFactory;
import hr.fer.zemris.studtest2.comm.CommandUtil;
import hr.fer.zemris.studtest2.conn.client.Connection;
import hr.fer.zemris.studtest2.conn.client.ConnectionPool;

public class TestsService {

	public static TestDataBean retrieveTestData(String testDescriptorID,
			String user, boolean createIfNotPresent) {
		if (testDescriptorID == null) {
			TestDataBean bean = new TestDataBean();
			bean.setOverallStatus("?");
			bean.setSourceSystem("?");
			bean.setTestScore(0);
			bean.setTotalAttempts(0);
			bean.setValid(false);
			bean.setInvalidReason("Invalid test identifier!");
			return bean;
		}
		String userFQN = "http://studtest.zemris.fer.hr/users#" + user;
		if (testDescriptorID.startsWith("studtest2:")) {
			return retrieveTestData_Studtest2(testDescriptorID, userFQN,
					createIfNotPresent);
		} else {
			return retrieveTestData_notSupported();
		}
	}

	private static TestDataBean retrieveTestData_notSupported() {
		TestDataBean bean = new TestDataBean();
		bean.setOverallStatus("?");
		bean.setSourceSystem("?");
		bean.setTestScore(0);
		bean.setTotalAttempts(0);
		bean.setValid(false);
		bean
				.setInvalidReason("Temporary failure: remote system could not be contacted.");
		return bean;
	}

	@SuppressWarnings("unchecked")
	private static TestDataBean retrieveTestData_Studtest2(
			String testDescriptorID, String userFQN, boolean createIfNotPresent) {
		TestDataBean td = new TestDataBean();
		String[] tident = splitTestIdentifier(testDescriptorID);
		if (tident == null || tident[0].length() == 0
				|| tident[1].length() == 0 || tident[2].length() == 0) {
			td.setOverallStatus("?");
			td.setSourceSystem("?");
			td.setTestScore(0);
			td.setTotalAttempts(0);
			td.setValid(false);
			td.setInvalidReason("Invalid test specifier: " + testDescriptorID
					+ ".");
			return td;
		}
		ConnectionPool cpool = (ConnectionPool) JCMSSettings.getSettings()
				.getObjects().get("studtest2-cpool");
		Connection conn = cpool.getConnection();
		if (conn == null) {
			td.setOverallStatus("?");
			td.setSourceSystem("?");
			td.setTestScore(0);
			td.setTotalAttempts(0);
			td.setValid(false);
			td
					.setInvalidReason("Temporary failure: studtest2 system could not be contacted.");
			return td;
		}
		try {
			CommandComposer cc = CommandComposerFactory.getInstance((short) 22,
					false);
			cc.writeString(userFQN, false);
			cc.writeString(tident[2], false);
			cc.writeString(userFQN, false);
			cc.writeBoolean(createIfNotPresent);

			Command cmd = cc.getCommand();
			cmd.writeTo(conn.getOutputStream());
			conn.getOutputStream().flush();
			cmd.dispose();

			Command resp = CommandComposerFactory.getStreamCommand(conn
					.getInputStream());
			try {
				byte status = resp.readByte();
				if (status == 0) {
					String err = resp.readString();
					System.out.println(err);
					td.setOverallStatus("?");
					td.setSourceSystem("?");
					td.setTestScore(0);
					td.setTotalAttempts(0);
					td.setValid(false);
					td.setInvalidReason(err);
				} else {
					List<Map<String, String>> list = (List<Map<String, String>>) (List) CommandUtil
							.readListObject(resp);
					Map<String, String> overall = CommandUtil
							.readMapStringString(resp);
					for (Map<String, String> ti : list) {
						TestInstanceDataBean tid = new TestInstanceDataBean();
						tid.setId(ti.get("id"));
						tid.setTitle(ti.get("title"));
						tid.setDescription(ti.get("description"));
						tid.setPreviousAttempts(Integer.parseInt(ti
								.get("previousAttempts")));
						tid.setTestScore(Double.valueOf(ti.get("testScore"))
								.doubleValue());
						tid.setTestStatus(ti.get("testStatus"));
						tid.setStartedAt(ti.get("started"));
						tid.setFinishedAt(ti.get("finished"));
						td.getTestInstanceData().add(tid);
					}
					td.setOverallStatus(overall.get("testStatus"));
					td.setTestScore(Double
							.parseDouble(overall.get("testScore")));
					td.setTotalAttempts(Integer.parseInt(overall
							.get("totalAttempts")));
					td.setSourceSystem("studtest2:");
					td.setValid(true);
				}
				return td;
			} finally {
				resp.dispose();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			conn.close();
		}
		td.setOverallStatus("?");
		td.setSourceSystem("?");
		td.setTestScore(0);
		td.setTotalAttempts(0);
		td.setValid(false);
		td.setInvalidReason("Exception occured while fething data.");
		return td;
	}

	public static String[] splitTestIdentifier(String identifier) {
		try {
			String[] elems = new String[3];
			int pos1 = identifier.indexOf(':', 0);
			int pos2 = identifier.indexOf(':', pos1 + 1);
			elems[0] = identifier.substring(0, pos1);
			elems[1] = identifier.substring(pos1 + 1, pos2);
			elems[2] = identifier.substring(pos2 + 1);
			return elems;
		} catch (Exception ex) {
			return null;
		}
	}

	public static String getTestPath(TestDataBean td, TestInstanceDataBean ti) {
		String base = null;
		// bad, bad, bad! URL je hardkodiran!
		if (td.getSourceSystem().equals("studtest2:")) {
			base = "/external/StudTest2.action";
		} else {
			// Unknown!
			return "#";
		}
		ActionContext context = ActionContext.getContext();
		HttpServletRequest request = (HttpServletRequest) context
				.get(StrutsStatics.HTTP_REQUEST);
		String cp = getEncodedCurrentRequestURL();
		if (cp != null) {
			base += "?prq=" + cp + "&tdid=" + ti.getId();
		} else {
			base += "?tdid=" + ti.getId();
		}
		return request.getContextPath() + base;
	}

	public static String getEncodedCurrentRequestURL() {
		ActionContext context = ActionContext.getContext();
		HttpServletRequest request = (HttpServletRequest) context
				.get(StrutsStatics.HTTP_REQUEST);
		return RevHexCoder.encode(getRequestURL(request));
	}

	public static String getEncodedRequestURL(HttpServletRequest request) {
		return RevHexCoder.encode(getRequestURL(request));
	}

	public static String getRequestURL(HttpServletRequest request) {
		StringBuffer sb = request.getRequestURL();
		String q = request.getQueryString();
		if (q != null)
			sb.append('?').append(q);
		return sb.toString();
	}

	public static class RevHexCoder {

		public static String encode(String text) {
			byte[] buf = null;
			try {
				buf = text.getBytes("UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				return "";
			}
			StringBuilder sb = new StringBuilder(buf.length * 2);
			for (int i = 0; i < buf.length; i++) {
				byte b = (byte) (15 - (buf[i] & 0x0f));
				if (b < 10)
					sb.append((char) ('0' + b));
				else
					sb.append((char) ('A' + (b - 10)));
				b = (byte) (15 - ((buf[i] >> 4) & 0x0f));
				if (b < 10)
					sb.append((char) ('0' + b));
				else
					sb.append((char) ('A' + (b - 10)));
			}
			return sb.toString();
		}

		public static String decode(String text) {
			char[] buf = text.toCharArray();
			int ub = buf.length / 2;
			byte[] data = new byte[ub];
			int index = 0;
			for (int i = 0; i < ub; i++) {
				char c = buf[index];
				byte x1 = 0;
				if (c >= '0' && c <= '9') {
					x1 = (byte) (c - '0');
				} else if (c >= 'A' && c <= 'F') {
					x1 = (byte) (c - 'A' + 10);
				} else if (c >= 'a' && c <= 'f') {
					x1 = (byte) (c - 'a' + 10);
				} else
					x1 = 0;
				index++;
				c = buf[index];
				byte x2 = 0;
				if (c >= '0' && c <= '9') {
					x2 = (byte) (c - '0');
				} else if (c >= 'A' && c <= 'F') {
					x2 = (byte) (c - 'A' + 10);
				} else if (c >= 'a' && c <= 'f') {
					x2 = (byte) (c - 'a' + 10);
				} else
					x2 = 0;
				index++;
				x1 = (byte) (15 - x1);
				x2 = (byte) (15 - x2);
				data[i] = (byte) ((x2 << 4) | x1);
			}
			try {
				return new String(data, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			return "";
		}
	}
	
	public static void fetchAndStoreExternalResults(IMessageLogger messageLogger, String userName, CourseComponentItemAssessment itemAssessment, List<String> users, Assessment assessment, Map<String, AssessmentScore> scoreMap) {
		String[] tident = itemAssessment.getAssessmentIdentifier().split(":");
		if(tident[0].equals("studtest2")) {
			fetchAndStoreExternalResults_studtest2(messageLogger, userName, tident, itemAssessment, users, assessment, scoreMap);
		} else {
			messageLogger.addInfoMessage(messageLogger.getText("Error.remoteSystemNotSupported"));
		}
	}

	private static void fetchAndStoreExternalResults_studtest2(IMessageLogger messageLogger, String userName, String[] tident, CourseComponentItemAssessment itemAssessment, List<String> users, Assessment assessment, Map<String, AssessmentScore> scoreMap) {
		ConnectionPool cpool = (ConnectionPool) JCMSSettings.getSettings().getObjects().get("studtest2-cpool");
		Connection conn = cpool.getConnection();
		if (conn == null) {
			messageLogger.addInfoMessage(messageLogger.getText("Error.remoteSystemCouldNotBeContacted"));
			return;
		}
		String userFQN = "http://studtest.zemris.fer.hr/users#" + userName;

		List<String> writeList = new ArrayList<String>(users.size());
		for(String u : users) {
			writeList.add("http://studtest.zemris.fer.hr/users#" + u);
		}
		try {
			CommandComposer cc = CommandComposerFactory.getInstance((short) 36, false);
			cc.writeString(userFQN, false);
			cc.writeString(tident[2], false);
			CommandUtil.writeListString(cc, writeList);
		
			Command cmd = cc.getCommand();
			cmd.writeTo(conn.getOutputStream());
			conn.getOutputStream().flush();
			cmd.dispose();
		
			Command resp = CommandComposerFactory.getStreamCommand(conn
					.getInputStream());
			try {
				byte status = resp.readByte();
				if (status == 0) {
					String err = resp.readString();
					messageLogger.addErrorMessage(err);
				} else {
					while(resp.readBoolean()) {
						int index = resp.readInt();
						boolean present = resp.readBoolean();
						double result = 0.0;
						if(present) {
							result = resp.readDouble();
						}
						String u = users.get(index);
						AssessmentScore score = scoreMap.get(u);
						if(present) {
							if(score==null) {
								score = new AssessmentScore();
								score.setAssessment(assessment);
								score.setRawPresent(true);
								score.setRawScore(result);
								scoreMap.put(u, score);
							} else {
								if(!score.getRawPresent() || Math.abs(score.getRawScore()-result)>1E-6) {
									score.setRawPresent(true);
									score.setRawScore(result);
								}
							}
						} else {
							if(score!=null) {
								if(score.getRawPresent() || Math.abs(score.getRawScore()-0.0)>1E-6) {
									score.setRawPresent(false);
									score.setRawScore(0.0);
								}
							}
						}
					}
				}
			} finally {
				resp.dispose();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			conn.close();
		}
	}
}
