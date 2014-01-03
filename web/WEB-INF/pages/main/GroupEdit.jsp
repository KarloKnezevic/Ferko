<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<s:if test="data.bean.id==null">
  <h2>Definiranje nove grupe</h2>
</s:if>
<s:else>
  <h2>Uređivanje grupe</h2>
</s:else>

<s:if test="data.messageLogger.hasMessages()">
	<ul class="msgList">
		<s:iterator value="data.messageLogger.messages">
			<li class="msgType_<s:property value="messageType" />"><s:property value="messageText" /></li>
		</s:iterator>
	</ul>
</s:if>

<s:form action="GroupEdit" theme="ferko">
	<s:textfield name="bean.name" label="%{getText('forms.name')}" />
	<s:hidden name="bean.id" />
	<s:hidden name="parentGroupID" />
	<s:hidden name="groupID" />
	<s:hidden name="courseInstanceID" />
	<s:submit method="save" />
</s:form>

<div class="bottomNavMenu">
<a href="<s:url action="ShowGroupTree"><s:param name="courseInstanceID"><s:property value="courseInstanceID"/></s:param></s:url>"><s:text name="Navigation.groupsTree"/></a>
</div>
