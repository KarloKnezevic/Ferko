<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div id="body" class="container">

	<s:if test="data.messageLogger.hasMessages()">
		<ul class="msgList">
			<s:iterator value="data.messageLogger.messages">
				<li class="msgType_<s:property value="messageType" />"><s:property value="messageText" /></li>
			</s:iterator>
		</ul>
	</s:if>

	<h1>Pregled postojećih semestara</h1>

	<table>
	<thead>
		<tr><th>Id</th><th>Akademska godina</th><th>Semestar</th><th>Početak</th><th>Kraj</th></tr>
	</thead>
	<tbody>
		<s:iterator value="data.allYearSemesters">
		<tr><td><a href="<s:url action="YearSemesterEdit" method="editYS"><s:param name="bean.id" value="id"/></s:url>"><s:property value="id"/></a></td><td><s:property value="academicYear"/></td><td><s:property value="semester"/></td><td><s:property value="data.formatDateTime(startsAt)"/></td><td><s:property value="data.formatDateTime(endsAt)"/></td></tr>
		</s:iterator>
	</tbody>
	</table>

</div>
