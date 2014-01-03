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

<h3>Podaci o ograničenjima</h3>
	<s:form action="ImportCourseMPConstraints" method="post" theme="ferko">
	<s:select list="data.allYearSemesters" listKey="id" listValue="fullTitle" name="semester" value="data.currentSemesterID" label="Semestar" />
	<s:textfield name="parentGroupRelativePath" label="Rel. staza roditeljske grupe" />
	<s:checkbox name="resetCapacities" label="Resetiraj neograničene kapacitete"/>
	<s:checkbox name="resetConstraints" label=" Resetiraj nespomenuta ograničenja"/>
	<s:textarea name="text" rows="20" cols="80" label="Ograničenja" />
	<s:submit method="upload" />
</s:form>

</div>
