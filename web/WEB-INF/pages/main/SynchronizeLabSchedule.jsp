<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div id="body" class="container">

<s:if test="data.messageLogger.hasMessages()">
  <ul>
  <s:iterator value="data.messageLogger.messages">
      <li>[<s:property value="messageType"/>]<s:property value="messageText"/></li>
  </s:iterator>
  </ul>
</s:if>

<h2>Podaci o satnici labosa</h2>
<s:form action="SynchronizeLabSchedule" method="post" theme="ferko">
	<s:select list="data.allYearSemesters" listKey="id" listValue="fullTitle" name="semester" value="data.currentSemesterID" label="Semestar" />
	<s:textarea name="text" rows="20" cols="100" label="Podaci o labosima" />
	<s:submit method="upload" />
</s:form>

</div>
