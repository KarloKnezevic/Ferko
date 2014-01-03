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

	<h1>Unos korisnika</h1>

	<s:form action="UserImport" method="post" theme="ferko">
		<s:textarea name="text" rows="20" cols="80" label="Lista korisnika"/>
		<s:checkboxlist list="data.allRoles" listKey="name" listValue="name" name="roles" label="Uloge" />
		<s:select list="data.allAuthTypes" listKey="id" listValue="description" name="authTypeID" required="true" label="Način autorizacije" />
		<s:submit method="importList" />
	</s:form>


<p><a target="_jcms_help" href="<s:url action="Help"><s:param name="helpKey">000003</s:param></s:url>"><s:text name="Navigation.help"/></a></p>

</div>
