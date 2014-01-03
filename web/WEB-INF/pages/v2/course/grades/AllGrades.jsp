<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<s:if test="data.messageLogger.hasMessages()">
	<ul class="msgList">
		<s:iterator value="data.messageLogger.messages">
			<li class="msgType_<s:property value="messageType" />"><s:property value="messageText" /></li>
		</s:iterator>
	</ul>
</s:if>

<h2><s:text name="Navigation.grades"></s:text></h2>

<table>
<thead>
  <tr>
    <th>Redni broj</th>
    <th>Student</th>
    <th>Ocjena</th>
  </tr>
</thead>
<tbody>
<s:iterator value="data.allGrades" status="stat">
  <tr class="<s:if test="#stat.odd == true">oddrow</s:if><s:else>evenrow</s:else>">
    <td><s:property value="#stat.index+1"/></td>
    <td><s:property value="user.lastName"/>, <s:property value="user.firstName"/> (<s:property value="user.jmbag"/>)</td>
    <td><s:property value="grade"/></td>
  </tr>
</s:iterator>
</tbody>
</table>
