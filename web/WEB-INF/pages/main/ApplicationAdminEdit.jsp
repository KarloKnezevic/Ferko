<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<s:if test="data.messageLogger.hasMessages()">
  <ul class="msgList">
  <s:iterator value="data.messageLogger.messages">
      <li>[<s:property value="messageType"/>] <s:property value="messageText"/></li>
  </s:iterator>
  </ul>
</s:if>

<s:if test="data.bean.id==null || data.bean.id.length()==0">
  <h2>Definiranje nove vrste prijave</h2>
</s:if>
<s:else>
  <h2>Uređivanje prijave</h2>
</s:else>

<s:form action="ApplicationAdminEdit" theme="ferko">
	<s:textfield name="bean.name" label="%{getText('forms.name')}" />
	<s:textfield name="bean.shortName" label="%{getText('forms.shortName')}" />
	<s:textfield name="bean.openFrom" label="Otvorena od" />
	<li class="fieldComment">(yyyy-MM-dd HH:mm:ss)</li>
	<s:textfield name="bean.openUntil" label="Otvorena do" />
	<li class="fieldComment">(yyyy-MM-dd HH:mm:ss)</li>
	<s:hidden name="bean.id" />
	<s:hidden name="bean.courseInstanceID" />
	<s:submit method="saveDefinition" />
</s:form>

<p><a href="<s:url action="ApplicationMain"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.applicationsHome"/></a></p>
