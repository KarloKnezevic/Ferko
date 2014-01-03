<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<s:if test="data.messageLogger.hasMessages()">
	<ul class="msgList">
		<s:iterator value="data.messageLogger.messages">
			<li class="msgType_<s:property value="messageType" />"><s:property value="messageText" /></li>
		</s:iterator>
	</ul>
</s:if>

<h2><s:text name="Navigation.newGroupsAddition"></s:text></h2>

<s:if test="data.allowMultipleAddition">
<p><s:text name="Navigation.addSubgroupsIn"></s:text> <b><s:property value="data.parent.name"/></b></p>
<p><i>Nazive grupa unesite jedan po retku. Prazni retci bit će zanemareni.</i></p>
</s:if><s:else>
<p><s:text name="Navigation.addSubgroupIn"></s:text> <b><s:property value="data.parent.name"/></b></p>
</s:else>

<s:form action="ShowGroupTree" theme="ferko">
<s:if test="data.allowMultipleAddition">
	<s:textarea name="data.text" label="%{getText('forms.groupNames')}" cols="40" rows="10" />
</s:if><s:else>
	<s:textfield name="data.text" label="%{getText('forms.groupName')}" />
</s:else>
	<s:hidden name="data.parentID" value="%{data.parent.id}"/>
	<s:submit method="newSubgroupsAdd" />
</s:form>
