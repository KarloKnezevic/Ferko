<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<s:if test="data.messageLogger.hasMessages()">
	<ul class="msgList">
		<s:iterator value="data.messageLogger.messages">
			<li class="msgType_<s:property value="messageType" />"><s:property value="messageText" /></li>
		</s:iterator>
	</ul>
</s:if>
 
 <h2><s:text name="Navigation.groupEventsList"/></h2>

  <div><b>Naziv grupe:</b> <s:property value="data.group.name"/></div><br>

  <s:if test="data.events.empty">
	<p class="emptyMsg">Nema zakazanih događaja.</p>
  </s:if>
  <s:else>

    <table>
    <thead>
      <tr><th>Naziv događaja</th><th>Početak događaja</th><th>Trajanje događaja</th><th>Prostorija</th><th>Akcije</th></tr>
    </thead>
    <tbody>
    <s:iterator value="data.events">
    <tr>
      <td><s:property value="title"/></td>
      <td><s:date name="start"/></td>
      <td><s:property value="duration"/> min</td>
      <td><s:property value="room.name"/></td>
      <td><a href="<s:url action="EditGroupEvent" method="editEvent"><s:param name="groupID"><s:property value="data.group.id"/></s:param><s:param name="bean.id"><s:property value="id"/></s:param></s:url>"><s:text name="Navigation.editGroupEvent"/></a></td>
    </tr>
    </s:iterator>
    </tbody>
    </table>
</s:else>

<div>
<a href="<s:url action="EditGroupEvent" method="newEvent"><s:param name="groupID"><s:property value="data.group.id"/></s:param></s:url>"><s:text name="Navigation.newGroupEvent"/></a>
</div>
