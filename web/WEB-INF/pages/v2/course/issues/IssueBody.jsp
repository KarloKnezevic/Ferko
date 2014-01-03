<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib prefix="s" uri="/struts-tags" %>



<div id="cbody" >
			
		<h2><s:text name = "ITS.mainTitle" /></h2>
		<s:if test="data.canManageTopics">
			<a href="<s:url action="Issues" method="editTopics"><s:param name="courseInstanceID"><s:property value="courseInstanceID"/></s:param></s:url>">
					<s:text name = "ITS.topicManagementLink" />
			</a> &nbsp;&nbsp;&nbsp;&nbsp;
		</s:if>
		
		<a href="<s:url action="Issues" method="execute"><s:param name="courseInstanceID"><s:property value="courseInstanceID"/></s:param></s:url>">
			<s:text name = "ITS.activeIssuesLink" />
		</a> &nbsp;&nbsp;&nbsp;&nbsp;
		
		<a href="<s:url action="Issues" method="execute"><s:param name="courseInstanceID"><s:property value="courseInstanceID"/></s:param><s:param name="data.archive"><s:property value="true"/></s:param></s:url>">
				<s:text name = "ITS.resolvedIssuesLink" />
		</a> &nbsp;&nbsp;&nbsp;&nbsp;
		
		<s:if test="data.canCreateIssue">
			<a href="<s:url action="NewIssue" method="execute"><s:param name="courseInstanceID"><s:property value="courseInstanceID"/></s:param></s:url>">
					<s:text name = "ITS.newIssueLink" />
			</a> &nbsp;&nbsp;&nbsp;&nbsp;
		</s:if>
	<br><br>
		<tiles:insertAttribute name="body"/>
</div>

