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

	<h1>Pregled definiranih korijena</h1>

	<table>
	<tbody>
		<s:iterator value="data.allSeminarRoots">
		<tr><td>
		Id: <a href="<s:url action="SeminarRootEdit" method="editSeminarRoot"><s:param name="data.id" value="id"/></s:url>"><s:property value="id"/></a><br>
		Aktivan: <s:property value="active"/><br>
		Semestar: <s:property value="semester.academicYear"/> - <s:property value="semester.semester"/><br>
		Grupa: <s:property value="rootGroup.name"/><br>
		Izvor: <s:property value="source"/><br>
		</td></tr>
		</s:iterator>
	</tbody>
	</table>

</div>
