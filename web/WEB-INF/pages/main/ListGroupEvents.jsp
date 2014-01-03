<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<s:if test="data.messageLogger.hasMessages()">
	<ul class="msgList">
		<s:iterator value="data.messageLogger.messages">
			<li class="msgType_<s:property value="messageType" />"><s:property value="messageText" /></li>
		</s:iterator>
	</ul>
</s:if>
 
 <h2>Grupni događaji</h2>

  <div>Grupa: <s:property value="data.group.name"/></div>

  <s:if test="data.events.empty">
	<p class="emptyMsg">Nema podataka.</p>
  </s:if>
  <s:else>

    <table>
    <s:iterator value="data.events">
    <tr>
      <td><s:property value="title"/></td>
      <td><s:date name="start"/></td>
      <td><s:property value="duration"/> min</td>
      <td><s:property value="room.name"/></td>
      <td><a href="<s:url action="EditGroupEvent" method="editEvent"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param><s:param name="relativePath"><s:property value="data.group.relativePath"/></s:param><s:param name="bean.id"><s:property value="id"/></s:param></s:url>"><s:text name="Navigation.editGroupEvent"/></a></td>
    </tr>
    </s:iterator>
    </table>
</s:else>

<div>
<a href="<s:url action="EditGroupEvent" method="newEvent"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param><s:param name="relativePath"><s:property value="data.group.relativePath"/></s:param></s:url>"><s:text name="Navigation.newGroupEvent"/></a>
</div>

<div>
<a href="<s:url action="ShowGroupTree"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.groupsTree"/></a>
</div>
