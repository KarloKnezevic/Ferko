<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<s:if test="data.messageLogger.hasMessages()">
  <ul class="msgList">
  <s:iterator value="data.messageLogger.messages">
      <li>[<s:property value="messageType"/>] <s:property value="messageText"/></li>
  </s:iterator>
  </ul>
</s:if>
<h2><s:property value="data.definition.name"/></h2>

<p>Navedite razlog prijave</p>

<s:form action="ApplicationStudentSubmit" theme="ferko">
	<s:textarea name="bean.reason" cols="50" rows="10" />
	<s:hidden name="bean.id" />
	<s:hidden name="data.applicationID" />
	<s:hidden name="data.courseInstanceID" />
	<s:submit method="saveApplication" />
</s:form>

<p><a href="<s:url action="ApplicationMain"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.applicationsHome"/></a></p>
