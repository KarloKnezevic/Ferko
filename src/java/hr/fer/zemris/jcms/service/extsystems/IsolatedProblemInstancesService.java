package hr.fer.zemris.jcms.service.extsystems;

import hr.fer.zemris.jcms.JCMSSettings;
import hr.fer.zemris.jcms.beans.IsolatedProblemInstanceBean;
import hr.fer.zemris.jcms.beans.IsolatedProblemInstanceStatus;
import hr.fer.zemris.jcms.web.actions.data.support.IMessageLogger;
import hr.fer.zemris.studtest2.comm.Command;
import hr.fer.zemris.studtest2.comm.CommandComposer;
import hr.fer.zemris.studtest2.comm.CommandComposerFactory;
import hr.fer.zemris.studtest2.comm.util.IsolatedProblemInstanceDTO;
import hr.fer.zemris.studtest2.comm.util.IsolatedProblemInstanceDTOIterator;
import hr.fer.zemris.studtest2.conn.client.Connection;
import hr.fer.zemris.studtest2.conn.client.ConnectionPool;
import hr.fer.zemris.studtest2.consts.EvaluationStatus;
import hr.fer.zemris.studtest2.consts.InstantiationStatus;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class IsolatedProblemInstancesService {

	public static List<IsolatedProblemInstanceBean> fetchIPIListForConfiguration(IMessageLogger messageLogger, String userName, String uri) {
		if(uri.startsWith("studtest2:")) {
			uri = uri.substring(10);
			int pos = uri.lastIndexOf('/');
			if(pos==-1) {
				messageLogger.addInfoMessage("Identifikator je pogrešnog formata.");
				return null;
			}
			String problemGeneratorURI = uri.substring(0, pos);
			String configurationURI = uri.substring(pos+1);
			return fetchIPIListForConfiguration_studtest2(messageLogger, userName, problemGeneratorURI, configurationURI);
		} else {
			messageLogger.addInfoMessage("Tražen je nepodržani sustav.");
			return null;
		}
	}
	
	private static List<IsolatedProblemInstanceBean> fetchIPIListForConfiguration_studtest2(IMessageLogger messageLogger, String userName, String problemGeneratorURI, String configurationURI) {
		ConnectionPool cpool = (ConnectionPool) JCMSSettings.getSettings().getObjects().get("studtest2-cpool");
		Connection conn = cpool.getConnection();
		if (conn == null) {
			messageLogger.addInfoMessage(messageLogger.getText("Error.remoteSystemCouldNotBeContacted"));
			return null;
		}
		String userFQN = "http://studtest.zemris.fer.hr/users#" + userName;

		List<IsolatedProblemInstanceBean> list = new ArrayList<IsolatedProblemInstanceBean>();
		try {
			CommandComposer cc = CommandComposerFactory.getInstance((short) 70, false);
			cc.writeString(userFQN, false);
			cc.writeString(problemGeneratorURI, false);
			cc.writeString(configurationURI, false);
		
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
					return null;
				} else {
					IsolatedProblemInstanceDTOIterator iterator = new IsolatedProblemInstanceDTOIterator(resp);
					boolean anySolvable = false;
					while(iterator.hasNext()) {
						IsolatedProblemInstanceDTO ipi = iterator.next();
						IsolatedProblemInstanceBean bean = new IsolatedProblemInstanceBean();
						bean.setCorrectnessMeasure(ipi.getCorrectnessMeasure());
						bean.setCreatedOn(ipi.getCreatedOn());
						bean.setFinishedOn(ipi.getFinishedOn());
						bean.setSolved(ipi.getProblemSolved()==null ? false : ipi.getProblemSolved().booleanValue());
						bean.setId("studtest2:"+problemGeneratorURI+"/"+configurationURI+"/"+String.valueOf(ipi.getIsolatedProblemInstanceId()));
						bean.setStatus(IsolatedProblemInstanceStatus.UNKNOWN);
						if(ipi.getInstantiationStatus()==InstantiationStatus.INSTANTIATED) {
							if(ipi.getEvaluationStatus()==EvaluationStatus.NOT_EVALUATED || ipi.getEvaluationStatus()==null) {
								bean.setStatus(IsolatedProblemInstanceStatus.SOLVABLE);
								anySolvable = true;
							} else if(ipi.getEvaluationStatus()==EvaluationStatus.EVALUATED) {
								bean.setStatus(IsolatedProblemInstanceStatus.FINISHED);
							}
						}
						list.add(bean);
					}
					if(!anySolvable) {
						IsolatedProblemInstanceBean bean = new IsolatedProblemInstanceBean();
						bean.setCorrectnessMeasure(null);
						bean.setCreatedOn(null);
						bean.setFinishedOn(null);
						bean.setSolved(false);
						bean.setId("studtest2:"+problemGeneratorURI+"/"+configurationURI+"/"); // Nema ID-a ==> to ce traziti stvaranje novog zadatka 
						bean.setStatus(IsolatedProblemInstanceStatus.NEW);
						list.add(bean);
					}
					return list;
				}
			} finally {
				resp.dispose();
			}
		} catch (IOException ex) {
			ex.printStackTrace();
		} finally {
			conn.close();
		}
		return null;
	}

}
