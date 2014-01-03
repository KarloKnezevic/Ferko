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

    <div> 
	<s:form action="User" method="get" theme="ferko">
		<s:textfield name="bean.id"  label="%{getText('forms.id')}"></s:textfield>
		<s:textfield name="bean.username"  label="%{getText('forms.username')}"></s:textfield>
		<s:textfield name="bean.jmbag"  label="%{getText('forms.jmbag')}"></s:textfield>
		<s:submit method="find"></s:submit>
	</s:form>
    </div>

</div>
