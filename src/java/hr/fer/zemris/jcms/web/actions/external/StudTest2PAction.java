package hr.fer.zemris.jcms.web.actions.external;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.apache.struts2.StrutsStatics;

import com.opensymphony.xwork2.ActionContext;
import com.opensymphony.xwork2.interceptor.ParameterNameAware;

import hr.fer.zemris.jcms.JCMSSettings;
import hr.fer.zemris.jcms.service.extsystems.TestsService;
import hr.fer.zemris.jcms.web.actions.ExtendedActionSupport;
import hr.fer.zemris.studtest2.comm.Command;
import hr.fer.zemris.studtest2.comm.CommandComposer;
import hr.fer.zemris.studtest2.comm.CommandComposerFactory;
import hr.fer.zemris.studtest2.comm.CommandUtil;
import hr.fer.zemris.studtest2.conn.client.Connection;
import hr.fer.zemris.studtest2.conn.client.ConnectionPool;
import hr.fer.zemris.studtest2.shared.queries.Variable;
import hr.fer.zemris.studtest2.shared.queries.VariableUtil;
import hr.fer.zemris.studtest2.web.util.IPartialVariableRenderer;
import hr.fer.zemris.studtest2.web.util.VariableRenderer;
import hr.fer.zemris.util.StringUtil;

public class StudTest2PAction extends ExtendedActionSupport implements ParameterNameAware {

	private static final Logger logger = Logger.getLogger(StudTest2PAction.class.getCanonicalName());
	private static final long serialVersionUID = 2L;

	public String fetch() throws Exception {
		
    	boolean traceEnabled = logger.isTraceEnabled();
    	
    	// Ako korisnik nije logiran - van!
    	String check = checkUser(null, true);
    	if(check != null) return check;

    	ActionContext context = ActionContext.getContext();
		HttpServletRequest request = (HttpServletRequest) context.get(StrutsStatics.HTTP_REQUEST);
		HttpServletResponse response = (HttpServletResponse) context.get(StrutsStatics.HTTP_RESPONSE);

		String dataURLService = (String)JCMSSettings.getSettings().getObjects().get("studtest2.dataURLService");
		if(dataURLService==null) {
			logger.warn("StudTest2 module is unavailable due to misconfiguration (parameter studtest2.dataURLService).");
			displayError(request, response, 503, "StudTest2 module is unavailable due to misconfiguration (parameter studtest2.dataURLService).");
			return "success";
		}

    	ConnectionPool cpool = (ConnectionPool)JCMSSettings.getSettings().getObjects().get("studtest2-cpool");
    	if(cpool==null) {
			logger.warn("StudTest2 module is unavailable due to misconfiguration (no connection pool available).");
			displayError(request, response, 503, "StudTest2 module is unavailable due to misconfiguration (no connection pool available).");
			return "success";
    	}
    	
    	String url = request.getParameter("url");
    	
    	if(StringUtil.isStringBlank(url) || !url.startsWith("studtest2:")) {
			logger.error("Invalid call (0).");
			displayError(request, response, 503, "Invalid call (0).");
			return "success";
    	}

    	String pguri = null;
    	String cfuri = null;
    	String ipiid = null;

    	try {
	    	if(url.endsWith("/")) {
	    		String u = url.substring(10, url.length()-1);
	    		int pos = u.lastIndexOf('/');
	    		pguri = u.substring(0,pos);
	    		cfuri = u.substring(pos+1);
	    	} else {
	    		String u = url.substring(10, url.length());
	    		int pos = u.lastIndexOf('/');
	    		ipiid = u.substring(pos+1);
	    		u = u.substring(0,pos);
	    		pos = u.lastIndexOf('/');
	    		pguri = u.substring(0,pos);
	    		cfuri = u.substring(pos+1);
	    	}
    	} catch(Exception ignorable) {
    		// Kod kasnije ce vidjeti sto se je dogodilo...
    	}
    	
    	String retURL = request.getParameter("retURL");
    	String command = request.getParameter("cmd");
    	
    	if(StringUtil.isStringBlank(pguri) || StringUtil.isStringBlank(cfuri) || StringUtil.isStringBlank(retURL) || StringUtil.isStringBlank(command)) {
			logger.error("Invalid call.");
			displayError(request, response, 503, "Invalid call.");
			return "success";
    	}
    	if(!command.equals("new")&&!command.equals("show")&&!command.equals("reopen")) {
			logger.error("Invalid call (3).");
			displayError(request, response, 503, "Invalid call (3).");
			return "success";
    	}
    	
    	Long ipiID = null;
    	if(!StringUtil.isStringBlank(ipiid)) {
    		try {
    			ipiID = Long.valueOf(ipiid);
    		} catch(NumberFormatException ex) {
    			logger.error("Invalid call (2).");
    			displayError(request, response, 503, "Invalid call (2).");
    			return "success";
    		}
    	}

    	if((!command.equals("new") && ipiID==null) || (command.equals("new") && ipiID!=null)) {
			logger.error("Invalid call (4).");
			displayError(request, response, 503, "Invalid call (4).");
			return "success";
    	}

    	String userPrefix = "http://studtest.zemris.fer.hr/users#";
    	String userToSend = userPrefix+getCurrentUser().getUsername();
    	
		Map<String,Object> outProp = new HashMap<String, Object>();
		List<Variable> variables = new ArrayList<Variable>();

		outProp.put("callback.f_pguri",pguri);
		outProp.put("callback.f_cfuri",cfuri);
		outProp.put("callback.f_ipiid",ipiid);
		outProp.put("callback.f_retURL",retURL);
		outProp.put("callback.f_url",url);
		outProp.put("callback.userFQN",userToSend);
		outProp.put("callback.f_command",command);
		outProp.put("formAction",response.encodeURL(removeFetch(request.getRequestURI())));
		// IN_dataURLService = "p_in_dataURLService"
		outProp.put("p_in_dataURLService", dataURLService);

		Connection conn = cpool.getConnection();
		if(conn==null) {
			logger.warn("StudTest2 is currently unavailable.");
			displayError(request, response, 503, "StudTest2 is currently unavailable.");
			return "success";
		}

		try {
			// Ako nemam identifikator zadatka:
			if(ipiID==null) {
	    		// Zatraži stvaranje novog zadatka:
				CommandComposer cc = CommandComposerFactory.getInstance((short)71, false);
				cc.writeString(userToSend, false);
				cc.writeString(pguri, false);
				cc.writeString(cfuri, false);
				Command cmd = cc.getCommand(); 
				cmd.writeTo(conn.getOutputStream());
				conn.getOutputStream().flush();
				cmd.dispose();
				
				Command resp = CommandComposerFactory.getStreamCommand(conn.getInputStream());
				try {
					byte status = resp.readByte();
					if(status==0) {
						String err = resp.readString();
						displayError(request, response, 500, err);
						return "success";
					} else {
						ipiID = Long.valueOf(resp.readLong());
						outProp.put("callback.f_ipiid",ipiID.toString());
						outProp.put("callback.f_url",url+ipiID.toString());
					}
				} finally {
					resp.dispose();
				}
	    	}
			// Sada imam postavljen ipiID
			CommandComposer cc = CommandComposerFactory.getInstance((short)72, false);
			cc.writeString(userToSend, false);
			// TE_HTML = "http://www.zemris.fer.hr/onto/technologies#HTML"
			cc.writeString("http://www.zemris.fer.hr/onto/technologies#HTML", false);
			cc.writeByteArray(VariableUtil.serializeVariableList(variables, false));
			CommandUtil.writeMapStringObject(cc, outProp);
			Command cmd = cc.getCommand(); 
			cmd.writeTo(conn.getOutputStream());
			conn.getOutputStream().flush();
			cmd.dispose();
			
			Command resp = CommandComposerFactory.getStreamCommand(conn.getInputStream());
			try {
				byte status = resp.readByte();
				if(status==0) {
					String err = resp.readString();
					displayError(request, response, 500, err);
					return "success";
				} else {
					variables = VariableUtil.deserializeVariableList(resp.readByteArray(), false);
					outProp = CommandUtil.readMapStringObject(resp);
				}
			} finally {
				resp.dispose();
			}
		} finally {
			conn.close();
		} 		

		if(traceEnabled) logger.trace("Property testWritingDone = "+outProp.get("testWritingDone"));
		if(outProp.get("testWritingDone")!=null) {
			String returnURL =	(String)outProp.get("callback.f_retURL");
			if(returnURL==null) returnURL = "";
			if(traceEnabled) logger.trace("Property returnURL = "+returnURL);
			if(!returnURL.equals("")) {
				if(returnURL.indexOf('?')!=-1) {
					returnURL += "&url="+encodeURL("studtest2:"+outProp.get("callback.f_pguri")+"/"+outProp.get("callback.f_cfuri")+"/"+outProp.get("callback.f_ipiid"));
				}
				response.sendRedirect(returnURL);
				return "success";
			}
			if(traceEnabled) logger.trace("Prazan povratni url");
		} else {
			if(traceEnabled) logger.trace("Nije testwritingdone!");
		}
		response.setContentType("text/html; charset=utf-8");
		response.setHeader("Pragma", "No-cache");
		response.setHeader("Cache-Control", "no-cache, must-revalidate");
		response.setHeader("Expires", "Thu, 01 Jan 1970 01:00:00 GMT");
		response.setDateHeader("Last-Modified",System.currentTimeMillis());
		
		VariableRenderer vren = new VariableRenderer();
		request.setAttribute("partialVREN", vren.getPartialVariableRenderer((Map<String,String>)(Map)outProp,variables));
		directRender(request, response); 		

		return "success";
	}
	
    private String removeFetch(String requestURI) {
    	int pos = requestURI.indexOf("!fetch.");
    	if(pos!=-1) {
    		requestURI = requestURI.substring(0, pos)+requestURI.substring(pos+6);
    	}
		return requestURI;
	}

	@SuppressWarnings("deprecation")
	private String encodeURL(String part) {
		try {
			return URLEncoder.encode(part, "utf-8");
		} catch (UnsupportedEncodingException e) {
			return URLEncoder.encode(part);
		}
	}

	@SuppressWarnings("unchecked")
	public String execute() throws Exception {
		
    	boolean traceEnabled = logger.isTraceEnabled();
    	
    	// Ako korisnik nije logiran - van!
    	String check = checkUser(null, true);
    	if(check != null) return check;

    	ActionContext context = ActionContext.getContext();
		HttpServletRequest request = (HttpServletRequest) context.get(StrutsStatics.HTTP_REQUEST);
		HttpServletResponse response = (HttpServletResponse) context.get(StrutsStatics.HTTP_RESPONSE);

		String dataURLService = (String)JCMSSettings.getSettings().getObjects().get("studtest2.dataURLService");
		if(dataURLService==null) {
			logger.warn("StudTest2 module is unavailable due to misconfiguration (parameter studtest2.dataURLService).");
			displayError(request, response, 503, "StudTest2 module is unavailable due to misconfiguration (parameter studtest2.dataURLService).");
			return "success";
		}

    	ConnectionPool cpool = (ConnectionPool)JCMSSettings.getSettings().getObjects().get("studtest2-cpool");
    	if(cpool==null) {
			logger.warn("StudTest2 module is unavailable due to misconfiguration (no connection pool available).");
			displayError(request, response, 503, "StudTest2 module is unavailable due to misconfiguration (no connection pool available).");
			return "success";
    	}
    	
		VariableRenderer vren = new VariableRenderer();
		Map<String,Object> outProp = new HashMap<String, Object>();
		List<Variable> variables = vren.restoreVariables(request, (Map<String,String>)(Map)outProp);
		if(variables==null) variables = new ArrayList<Variable>();

		
    	String url = (String)outProp.get("callback.f_url");
    	
    	if(StringUtil.isStringBlank(url) || !url.startsWith("studtest2:")) {
			logger.error("Invalid call (0).");
			displayError(request, response, 503, "Invalid call (0).");
			return "success";
    	}

    	String pguri = (String)outProp.get("callback.f_pguri");
    	String cfuri = (String)outProp.get("callback.f_cfuri");
    	String ipiid = (String)outProp.get("callback.f_ipiid");

    	String retURL = (String)outProp.get("callback.f_retURL");
    	String command = (String)outProp.get("callback.f_command");
    	
    	if(StringUtil.isStringBlank(pguri) || StringUtil.isStringBlank(cfuri) || StringUtil.isStringBlank(retURL) || StringUtil.isStringBlank(command)) {
			logger.error("Invalid call.");
			displayError(request, response, 503, "Invalid call.");
			return "success";
    	}
    	if(!command.equals("new")&&!command.equals("show")&&!command.equals("reopen")) {
			logger.error("Invalid call (3).");
			displayError(request, response, 503, "Invalid call (3).");
			return "success";
    	}
    	
    	Long ipiID = null;
    	if(!StringUtil.isStringBlank(ipiid)) {
    		try {
    			ipiID = Long.valueOf(ipiid);
    		} catch(NumberFormatException ex) {
    			logger.error("Invalid call (2).");
    			displayError(request, response, 503, "Invalid call (2).");
    			return "success";
    		}
    	}

    	if(ipiID==null) {
			logger.error("Invalid call (4).");
			displayError(request, response, 503, "Invalid call (4).");
			return "success";
    	}

		String userToSend = (String)outProp.get("callback.userFQN");
    	if(StringUtil.isStringBlank(userToSend)) {
			logger.error("Invalid call (4).");
			displayError(request, response, 503, "Invalid call (4).");
			return "success";
    	}

		// outProp.put("callback.f_pguri",pguri);
    	// outProp.put("callback.f_cfuri",cfuri);
    	// outProp.put("callback.f_ipiid",ipiid);
    	// outProp.put("callback.f_retURL",retURL);
    	// outProp.put("callback.f_url",url);
    	// outProp.put("callback.userFQN",userToSend);
    	// outProp.put("callback.f_command",command);
    	// outProp.put("formAction",response.encodeURL(request.getRequestURI()));
		// IN_dataURLService = "p_in_dataURLService"
    	// outProp.put("p_in_dataURLService", dataURLService);

		Connection conn = cpool.getConnection();
		if(conn==null) {
			logger.warn("StudTest2 is currently unavailable.");
			displayError(request, response, 503, "StudTest2 is currently unavailable.");
			return "success";
		}

		
		try {
			// Sada imam postavljen ipiID
			CommandComposer cc = CommandComposerFactory.getInstance((short)72, false);
			cc.writeString(userToSend, false);
			// TE_HTML = "http://www.zemris.fer.hr/onto/technologies#HTML"
			cc.writeString("http://www.zemris.fer.hr/onto/technologies#HTML", false);
			cc.writeByteArray(VariableUtil.serializeVariableList(variables, false));
			CommandUtil.writeMapStringObject(cc, outProp);
			Command cmd = cc.getCommand(); 
			cmd.writeTo(conn.getOutputStream());
			conn.getOutputStream().flush();
			cmd.dispose();
			
			Command resp = CommandComposerFactory.getStreamCommand(conn.getInputStream());
			try {
				byte status = resp.readByte();
				if(status==0) {
					String err = resp.readString();
					displayError(request, response, 500, err);
					return "success";
				} else {
					variables = VariableUtil.deserializeVariableList(resp.readByteArray(), false);
					outProp = CommandUtil.readMapStringObject(resp);
				}
			} finally {
				resp.dispose();
			}
		} finally {
			conn.close();
		} 		

		if(traceEnabled) logger.trace("Property testWritingDone = "+outProp.get("testWritingDone"));
		if(outProp.get("testWritingDone")!=null) {
			String returnURL =	(String)outProp.get("callback.f_retURL");
			if(returnURL==null) returnURL = "";
			if(traceEnabled) logger.trace("Property returnURL = "+returnURL);
			if(!returnURL.equals("")) {
				if(returnURL.indexOf('?')!=-1) {
					returnURL += "&url="+encodeURL("studtest2:"+outProp.get("callback.f_pguri")+"/"+outProp.get("callback.f_cfuri")+"/"+outProp.get("callback.f_ipiid"));
				}
				response.sendRedirect(returnURL);
				return "success";
			}
			if(traceEnabled) logger.trace("Prazan povratni url");
		} else {
			if(traceEnabled) logger.trace("Nije testwritingdone!");
		}
		response.setContentType("text/html; charset=utf-8");
		response.setHeader("Pragma", "No-cache");
		response.setHeader("Cache-Control", "no-cache, must-revalidate");
		response.setHeader("Expires", "Thu, 01 Jan 1970 01:00:00 GMT");
		response.setDateHeader("Last-Modified",System.currentTimeMillis());
		
		request.setAttribute("partialVREN", vren.getPartialVariableRenderer((Map<String,String>)(Map)outProp,variables));
		directRender(request, response); 		

		return "success";
	}

	@SuppressWarnings("unchecked")
	public String execute2() throws Exception {
    	
    	boolean traceEnabled = logger.isTraceEnabled();
    	
    	// Ako korisnik nije logiran - van!
    	String check = checkUser(null, true);
    	if(check != null) return check;

    	ActionContext context = ActionContext.getContext();
		HttpServletRequest request = (HttpServletRequest) context.get(StrutsStatics.HTTP_REQUEST);
		HttpServletResponse response = (HttpServletResponse) context.get(StrutsStatics.HTTP_RESPONSE);

		String dataURLService = (String)JCMSSettings.getSettings().getObjects().get("studtest2.dataURLService");
		if(dataURLService==null) {
			logger.warn("StudTest2 module is unavailable due to misconfiguration (parameter studtest2.dataURLService).");
			displayError(request, response, 503, "StudTest2 module is unavailable due to misconfiguration (parameter studtest2.dataURLService).");
			return "success";
		}

    	ConnectionPool cpool = (ConnectionPool)JCMSSettings.getSettings().getObjects().get("studtest2-cpool");
    	if(cpool==null) {
			logger.warn("StudTest2 module is unavailable due to misconfiguration (no connection pool available).");
			displayError(request, response, 503, "StudTest2 module is unavailable due to misconfiguration (no connection pool available).");
			return "success";
    	}

    	String userPrefix = "http://studtest.zemris.fer.hr/users#";
    	String userToSend = userPrefix+getCurrentUser().getUsername();

    	String tdid = request.getParameter("tdid");
		if(tdid == null) tdid = "";
    	
		VariableRenderer vren = new VariableRenderer();
		Map<String,Object> outProp = new HashMap<String, Object>();
		List<Variable> variables = vren.restoreVariables(request, (Map<String,String>)(Map)outProp);
		if(variables==null) variables = new ArrayList<Variable>();
		
		String referer = request.getHeader("Referer");
		if(referer==null) {
			if(traceEnabled) logger.trace("Browser nije poslao referer!");
		}

		String pr = request.getParameter("prq");
		if(pr==null) {
			if(traceEnabled) logger.trace("Supstitucijski parametar za referer nije pronaden!");
		} else {
			pr = TestsService.RevHexCoder.decode(pr);
			if(referer==null) referer = pr;
		}
		
		if(outProp.get("callback.returnURL")==null && referer!=null) {
			referer = response.encodeURL(referer);
			if(traceEnabled) logger.trace("Postavljam da je referer: "+referer);
			outProp.put("callback.returnURL",referer);
		} else {
			if(traceEnabled) logger.trace("Koristim prethodni referer: "+outProp.get("callback.returnURL"));
		}
		
		if(outProp.get("callback.tdid")==null && !tdid.equals("")) {
			outProp.put("callback.tdid",tdid);
		}
		if(outProp.get("callback.userFQN")==null) {
			outProp.put("callback.userFQN",userToSend);
		}

		outProp.put("cparam.ip",request.getRemoteAddr());
		// Ovdje dostavi grupu studenta; za ovo jos iz Nescume-a nemam pametno (zapravo, bilo kakvo) rjesenje
		outProp.put("cparam.user.group","XXX/YY");
		outProp.put("cparam.user.fullname",getCurrentUser().getLastName()+", "+getCurrentUser().getFirstName());
		if(getCurrentUser().getJmbag()!=null) {
			outProp.put("cparam.user.jmbag",getCurrentUser().getJmbag());
		}
		outProp.put("formAction",response.encodeURL(request.getRequestURI()));
		// IN_dataURLService = "p_in_dataURLService"
		outProp.put("p_in_dataURLService", dataURLService);
		
		Connection conn = cpool.getConnection();
		if(conn==null) {
			logger.warn("StudTest2 is currently unavailable.");
			displayError(request, response, 503, "StudTest2 is currently unavailable.");
			return "success";
		}
		try {
			CommandComposer cc = CommandComposerFactory.getInstance((short)1000, false);
			cc.writeString(userToSend, false);
			// TE_HTML = "http://www.zemris.fer.hr/onto/technologies#HTML"
			cc.writeString("http://www.zemris.fer.hr/onto/technologies#HTML", false);
			cc.writeByteArray(VariableUtil.serializeVariableList(variables, false));
			CommandUtil.writeMapStringObject(cc, outProp);
			Command cmd = cc.getCommand(); 
			cmd.writeTo(conn.getOutputStream());
			conn.getOutputStream().flush();
			cmd.dispose();
			
			Command resp = CommandComposerFactory.getStreamCommand(conn.getInputStream());
			try {
				byte status = resp.readByte();
				if(status==0) {
					String err = resp.readString();
					displayError(request, response, 500, err);
					return "success";
				} else {
					variables = VariableUtil.deserializeVariableList(resp.readByteArray(), false);
					outProp = CommandUtil.readMapStringObject(resp);
				}
			} finally {
				resp.dispose();
			}
		} finally {
			conn.close();
		} 		
		
		if(traceEnabled) logger.trace("Property testWritingDone = "+outProp.get("testWritingDone"));
		if(outProp.get("testWritingDone")!=null) {
			String returnURL =	(String)outProp.get("callback.returnURL");
			if(returnURL==null) returnURL = "";
			if(traceEnabled) logger.trace("Property returnURL = "+returnURL);
			if(!returnURL.equals("")) {
				response.sendRedirect(returnURL);
				return "success";
			}
			if(traceEnabled) logger.trace("Prazan povratni url");
		} else {
			if(traceEnabled) logger.trace("Nije testwritingdone!");
		}
		response.setContentType("text/html; charset=utf-8");
		response.setHeader("Pragma", "No-cache");
		response.setHeader("Cache-Control", "no-cache, must-revalidate");
		response.setHeader("Expires", "Thu, 01 Jan 1970 01:00:00 GMT");
		response.setDateHeader("Last-Modified",System.currentTimeMillis());
		
		request.setAttribute("partialVREN", vren.getPartialVariableRenderer((Map<String,String>)(Map)outProp,variables));
		directRender(request, response); 		
		
        return SUCCESS;
    }

	private void directRender(HttpServletRequest request, HttpServletResponse response) throws IOException {
		PrintWriter pw = response.getWriter();
		pw.write("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n");
		IPartialVariableRenderer pvren = (IPartialVariableRenderer)request.getAttribute("partialVREN");
		pw.write("<html xmlns=\"http://www.w3.org/1999/xhtml\">\n");
		pw.write("<head>\n");
		pw.write(pvren.renderMeta(null, false));
		pw.write(pvren.renderScripts());
		pw.write("</head>\n");
		pw.write("<body "); pw.write(pvren.renderBodyAttributes()); pw.write(">\n");
		pw.write(pvren.renderBodyHeaderSection());
		pw.write(pvren.renderBodyCentralSection());
		pw.write(pvren.renderAutogenForm());
		pw.write(pvren.renderBodyFooterSection());
		pw.write("</body>\n");
		pw.write("</html>\n");
		pw.flush();
	}

	public void displayError(HttpServletRequest request, HttpServletResponse response, int statusCode, String message) throws IOException, ServletException {
		response.setContentType("text/html; charset=utf-8");
		PrintWriter pw = response.getWriter();
		pw.write("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Transitional//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd\">\n");
		pw.write("<html xmlns=\"http://www.w3.org/1999/xhtml\">\n");
		pw.write("<head>\n");
		pw.write("<title>Error!</title>\n");
		pw.write("</head>\n");
		pw.write("<body>\n");
		pw.write("<h1>");
		pw.write(message);
		pw.write("</h1>");
		pw.write("</body>\n");
		pw.write("</html>\n");
		pw.flush();
	}

	@Override
	public boolean acceptableParameterName(String name) {
		return false;
	}

}
