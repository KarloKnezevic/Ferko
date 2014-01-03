<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<s:if test="data.messageLogger.hasMessages()">
	<ul class="msgList">
		<s:iterator value="data.messageLogger.messages">
			<li class="msgType_<s:property value="messageType" />"><s:property value="messageText" /></li>
		</s:iterator>
	</ul>
</s:if>
 
 <h2>Popis studenata za grupu <s:property value="data.group.name"/></h2>

<s:if test="data.allUsers.isEmpty()">
  <p class="emptyMsg">Grupa nema studenata.</p>
</s:if>
<s:else>  
    <table>
    <thead>
      <tr><th>Broj</th><th>Naziv grupe</th><th>Broj studenata u grupi <a target="_jcms_help" href="<s:url action="Help"><s:param name="helpKey">000013</s:param></s:url>" onclick="blur();"><img src="img/icons/help.png"></a></th></tr>
    </thead>
    <tbody>
    <% int cntr1 = 0; %>
  <s:iterator value="data.allGroups" status="cust_stat">
    <% cntr1++; %>
    <tr class="<s:if test="#cust_stat.odd == true">oddrow</s:if><s:else>evenrow</s:else>">
      <td><%= cntr1 %>.</td>
      <td><s:property value="name"/></td>
      <td><s:property value="users.size()"/></td>
    </tr>
  </s:iterator>
    </tbody>
    </table>

    <table class="bigTable">
    <thead>
      <tr>
        <th>Broj</th><th>JMBAG</th><th class="firstCol">Prezime</th><th class="firstCol">Ime</th><th>Grupa</th><th>Oznaka <a target="_jcms_help" href="<s:url action="Help"><s:param name="helpKey">000014</s:param></s:url>" onclick="blur();"><img src="img/icons/help.png"></a></th><th>Akcija</th>
      </tr>
    </thead>
    <tbody>
    <% int cntr = 0; %>
  <s:iterator value="data.allUsers" status="cust_stat">
    <% cntr++; %>
    <tr class="<s:if test="#cust_stat.odd == true">oddrow</s:if><s:else>evenrow</s:else>">
      <td><%= cntr %>.</td>
      <td><s:property value="jmbag"/></td>
      <td class="firstCol"><s:property value="lastName"/></td>
      <td class="firstCol"><s:property value="firstName"/></td>
      <td><s:property value="groupName"/></td>
      <td><s:property value="tag"/></td>
      <td><s:if test="perm.canManageUsers"><a href="<s:url action="ChangeUsersGroup"><s:param name="data.groupID"><s:property value="currentGroupID"/></s:param><s:param name="data.ugID"><s:property value="ugID"/></s:param><s:param name="data.mpID"><s:property value="mpID"/></s:param><s:param name="data.viewedGroupID"><s:property value="data.group.id"/></s:param></s:url>"><s:text name="Navigation.changeUsersGroup"/></a></s:if><s:else>-</s:else></td>
    </tr>
  </s:iterator>
    </tbody>
    </table>
</s:else>

<s:if test="data.gperm.canManageUsers && data.marketPlaceGroup!=null">
	<h2>Uređivanje članstva <a target="_jcms_help" href="<s:url action="Help"><s:param name="helpKey">000015</s:param></s:url>" onclick="blur();"><img src="img/icons/help.png"></a></h2>
	<s:form action="UpdateUserGroupMembership" method="post" theme="ferko">
		<s:textarea name="text" rows="5" cols="20" label="JMBAGovi" />
		<s:checkbox name="removeOther" label="Izbriši ostale" />
		<s:hidden name="groupID" value="%{data.group.id}" />
		<s:hidden name="mpID" value="%{data.marketPlaceGroup.id}" />
		<s:hidden name="data.lid" value="%{data.courseInstance.id}" />
		<s:submit value="%{getText('forms.updateMembers')}" />
	</s:form>
</s:if>

<s:if test="data.transferEnabled && !data.transferGroups.isEmpty() && data.marketPlaceGroup!=null">
	<h2>Transfer studenata iz drugih grupa u ovu grupu <a target="_jcms_help" href="<s:url action="Help"><s:param name="helpKey">000016</s:param></s:url>" onclick="blur();"><img src="img/icons/help.png"></a></h2>
	<s:form action="TransferUsersFromGroup" method="post" theme="ferko">
		<s:hidden name="groupID" value="%{data.group.id}" />
		<s:hidden name="mpID" value="%{data.marketPlaceGroup.id}" />
		<s:hidden name="data.lid" value="%{data.courseInstance.id}" />
		<s:select list="data.transferGroups" listKey="id" listValue="name" name="sourceGroupID" label="Izvorišna grupa"></s:select>
		<s:submit value="%{getText('forms.acquireGroupUsers')}" />
	</s:form>
</s:if>

<s:if test="!data.managedGroups.isEmpty() && data.marketPlaceGroup!=null">
	<h2>Ažuriranje oznaka studenata <a target="_jcms_help" href="<s:url action="Help"><s:param name="helpKey">000017</s:param></s:url>" onclick="blur();"><img src="img/icons/help.png"></a></h2>
	<s:form action="UploadStudentTags" method="post" theme="ferko">
		<s:textarea name="text" rows="5" cols="40" label="Oznake" />
		<s:hidden name="groupID" value="%{data.group.id}" />
		<s:hidden name="mpID" value="%{data.marketPlaceGroup.id}" />
		<s:hidden name="data.lid" value="%{data.courseInstance.id}" />
	<s:submit value="%{getText('forms.update')}" />
	</s:form>
</s:if>

