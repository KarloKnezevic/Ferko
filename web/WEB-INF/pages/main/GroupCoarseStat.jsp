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
	<s:form action="GroupCoarseStat" method="post">
		<s:select name="semesterID"  label="%{getText('forms.Semester')}" list="data.allSemesters" listKey="id" listValue="fullTitle" value="data.yearSemester.id"></s:select>
		<s:hidden name="parentRelativePath"></s:hidden>
		<s:submit method="view"></s:submit>
	</s:form>
    </div>

	<table>
		<tr><th>Grupa</th><th>Broj studenata</th></tr>
	<s:iterator  value="data.stats">
	<s:iterator status="stat">
		<s:if test="#stat.first"><tr><td colspan="2">Kolegij: <s:property value="courseName"/> (<s:property value="courseIsvuCode"/>)</td></tr></s:if>
		<tr><td><s:property value="groupName"/></td><td><s:property value="count"/></td></tr>
	</s:iterator>
	</s:iterator>
	</table>

<div>
  <a href="<s:url action="Main"/>"><s:text name="Navigation.main"/></a>
</div>

</div>
