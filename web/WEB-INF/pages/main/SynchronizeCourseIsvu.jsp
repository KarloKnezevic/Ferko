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

  <h2>Upload zip datoteke s podacima</h2>
  <s:form action="SynchronizeCourseIsvu" method="post" enctype="multipart/form-data" theme="ferko">
	<s:select list="data.allYearSemesters" listKey="id" listValue="fullTitle" name="semester" value="data.currentSemesterID" label="Semestar" />
    <s:file name="archive" label="%{getText('forms.file')}" />
	<li class="fieldComment">.zip</li>
    <s:submit method="upload" />
  </s:form>

</div>
