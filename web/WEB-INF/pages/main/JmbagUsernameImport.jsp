<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div id="body" class="container">

	<s:if test="data.messageLogger.hasMessages()">
		<ul>
			<s:iterator value="data.messageLogger.messages">
				<li>[<s:property value="messageType" />] <s:property
					value="messageText" /></li>
			</s:iterator>
		</ul>
	</s:if>

	<h2>Unos JMBAG nešto bla</h2>
	<s:form action="JMBAGUsernameImport" method="post" theme="ferko">
		<s:textarea name="text" rows="20" cols="80"></s:textarea>
		<s:submit method="importList"></s:submit>
	</s:form>

<div><a target="_jcms_help" href="<s:url action="Help"><s:param name="helpKey">000004</s:param></s:url>"><s:text name="Navigation.help"/></a></div>

</div>
