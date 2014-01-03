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
	
	<h3><s:text name = "ITS.newIssueForm" /></h3>
	
	<s:form action="NewIssue" method="post" theme="ferko">
		<s:select list="data.messageTopics" listKey="id" listValue="name" name="data.messageBean.topicID" label="%{getText('ITS.courseTopics')}" />
		<s:textfield size= "40" name="data.messageBean.messageName" label="%{getText('ITS.messageTitle')}" />
		<s:checkbox label="%{getText('ITS.publicDeclaration')}" name="data.messageBean.declaredPublic" />
		<s:textarea rows="5" cols="40" name="data.messageBean.messageContent" label="%{getText('ITS.messageContent')}" />
		<s:label value="%{getText('ITS.noEditWarning')}" />
		<s:hidden name="courseInstanceID" value="%{courseInstanceID}"/>
		<s:submit method="newIssueAdd" type="button" label="%{getText('ITS.sendNewIssueButton')}" ></s:submit>
	</s:form>


