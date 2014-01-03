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

  <h2>Podaci iz ISVU-a</h2>
  <s:form action="SynchronizeCourseStudents" method="post" theme="ferko">
	<s:select list="data.allYearSemesters" listKey="id" listValue="fullTitle" name="semester" value="data.currentSemesterID" label="Semestar" />
   <s:textarea name="text" rows="20" cols="80" label="Podaci iz ISVU-a" />
    <s:submit method="upload" />
  </s:form>

</div>
