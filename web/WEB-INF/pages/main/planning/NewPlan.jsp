<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

	<s:if test="data.messageLogger.hasMessages()">
		<ul>
			<s:iterator value="data.messageLogger.messages">
				<li>[<s:property value="messageType" />] <s:property
					value="messageText" /></li>
			</s:iterator>
		</ul>
	</s:if>

		<br><br>
		
	<s:if test="planID==null">	
		<h3><s:text name = "Planning.newPlanSubtitle" /></h3>
	</s:if>
	<s:else>
		<h3><s:text name = "Planning.editPlanSubtitle" /></h3>
	</s:else>
		
		<applet code="hr.fer.zemris.jcms.planning.PlanningApplet"
				archive="/ferko/applet/jcms-planning.jar,/ferko/applet/jx-layer-ferko.jar,/ferko/applet/jxfilters.jar"
				width="200" 
				height="40">
				<param name="courseInstanceID" value="<s:property value="courseInstanceID"/>">
				<s:if test="planID!=null">
					<param name="planIDToLoad" value="<s:property value="planID"/>">
				</s:if>
				
				 FERKO planning service </applet> 
 