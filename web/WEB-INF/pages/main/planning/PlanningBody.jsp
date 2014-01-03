<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<%@ include file="/WEB-INF/pages/global/CourseHeader.jsp" %>

<div id="body" class="container courseBody">
		<h1><s:property value="data.courseInstance.course.isvuCode"/>&nbsp;&nbsp;<s:property value="data.courseInstance.course.name"/></h1>
			
		<h2><s:text name = "Planning.mainTitle" />
		<a target="_jcms_help" href="<s:url action="Help"><s:param name="helpKey">000033</s:param></s:url>" onclick="blur();"><img src="img/icons/help.png"></a></h2>
		
		
		<s:if test="data.messageLogger.hasMessages()">
			<ul>
				<s:iterator value="data.messageLogger.messages">
					<li>[<s:property value="messageType" />] <s:property
						value="messageText" /></li>
				</s:iterator>
			</ul>
		</s:if>
		
		<a href="<s:url action="Planning" method="execute"><s:param name="courseInstanceID"><s:property value="courseInstanceID"/></s:param></s:url>">
			<s:text name = "Planning.myPlansLink" />
		</a> &nbsp;&nbsp;&nbsp;&nbsp;


		<link rel="stylesheet" type="text/css" href="/ferko/css/plans.css" />
		
		
		<tiles:insertAttribute name="body"/>
</div>

