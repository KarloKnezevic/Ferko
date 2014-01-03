<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<s:if test="data.messageLogger.hasMessages()">
	<ul class="msgList">
		<s:iterator value="data.messageLogger.messages">
			<li class="msgType_<s:property value="messageType" />"><s:property value="messageText" /></li>
		</s:iterator>
	</ul>
</s:if>
 
 <h2>Popis korisnika</h2>

  <s:if test="data.allUsers.isEmpty()">
	<p class="emptyMsg">Nema podataka.</p>
  </s:if>
  <s:else>  
    <table>
    <thead>
      <tr><th>Broj</th><th>Naziv grupe</th><th>Broj direktnih studenata</th><th>Broj studenata</th><th>Staza</th></tr>
    </thead>
    <tbody>
    <% int cntr1 = 0; %>
  <s:iterator value="data.allGroups" status="cust_stat">
    <% cntr1++; %>
    <tr class="<s:if test="#cust_stat.odd == true">oddrow</s:if><s:else>evenrow</s:else>">
      <td><%= cntr1 %>.</td>
      <td><s:property value="name"/></td>
      <td><s:property value="users.size()"/></td>
      <td><s:property value="calcTotalNumberOfUsers()"/></td>
      <td><s:property value="relativePath"/></td>
    </tr>
  </s:iterator>
    </tbody>
    </table>

    <table class="bigTable">
    <thead>
      <tr>
        <th>Broj</th><th>JMBAG</th><th class="firstCol">Prezime</th><th class="firstCol">Ime</th><th>Grupa</th><th>Oznaka</th>
        <s:if test="data.canUpdateUsersGroup"><th>Akcija</th></s:if>
      </tr>
    </thead>
    <tbody>
    <% int cntr = 0; %>
  <s:iterator value="data.allUsers" status="cust_stat">
    <% cntr++; %>
    <tr class="<s:if test="#cust_stat.odd == true">oddrow</s:if><s:else>evenrow</s:else>">
      <td><%= cntr %>.</td>
      <td><s:property value="user.jmbag"/></td>
      <td class="firstCol"><s:property value="user.lastName"/></td>
      <td class="firstCol"><s:property value="user.firstName"/></td>
      <td><s:property value="group.name"/></td>
      <td><s:property value="tag"/></td>
      <s:if test="data.canUpdateUsersGroup"><td><a href="<s:url action="ChangeUsersGroup">
          <s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param>
          <s:param name="groupID"><s:property value="group.id"/></s:param>
          <s:param name="userGroupID"><s:property value="id"/></s:param>
          <s:param name="relativePath"><s:property value="relativePath"/></s:param>
      </s:url>"><s:text name="Navigation.changeUsersGroup"/></a></td></s:if>
    </tr>
  </s:iterator>
    </tbody>
    </table>
	<s:if test="data.canUpdateUsersGroup">
		<h2>Uređivanje oznaka studenata</h2>
		<s:form action="UploadStudentTags" method="post" theme="ferko" label="Pridjeljivanje oznaka studentima">
		<s:textarea name="text" rows="5" cols="80" label="Oznake" />
		<s:hidden name="courseInstanceID" value="%{data.courseInstance.id}" />
		<s:hidden name="parentID" value="%{data.parent.id}" />
		<s:submit />
	</s:form>
	</s:if>

</s:else>

<s:if test="data.canManageUserGroupMembership">
	<h2>Uređivanje članstva</h2>
	<s:form action="UpdateUserGroupMembership" method="post" theme="ferko" label="Uređivanje članstva">
	<s:textarea name="text" rows="5" cols="20" label="JMBAGovi" />
	<s:checkbox name="removeOther" label="Izbriši ostale" />
	<s:hidden name="courseInstanceID" value="%{data.courseInstance.id}" />
	<s:hidden name="relativePath" value="%{relativePath}" />
	<s:submit />
	</s:form>
</s:if>

<s:if test="data.canCreateOrEditGroups">
	<h2>Dodatne akcije</h2>
    <div>
      <a href="<s:url action="GroupEdit" method="newGroupInput"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param><s:param name="parentGroupID"><s:property value="data.parent.id"/></s:param></s:url>">Dodaj novu grupu</a>
      <a href="<s:url action="GroupEdit" method="groupEdit"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param><s:param name="groupID"><s:property value="data.parent.id"/></s:param></s:url>">Uredi trenutnu grupu</a>
      <a href="<s:url action="DeleteGroup"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param><s:param name="groupID"><s:property value="data.parent.id"/></s:param></s:url>">Obriši trenutnu grupu</a>
    </div>
<s:if test="data.takeUsersList!=null">
	<s:form action="TransferUsersFromGroup" method="post" theme="ferko" label="Transfer korisnika">
	<s:hidden name="courseInstanceID" value="%{data.courseInstance.id}" />
	<s:hidden name="destinationGroupID" value="%{data.parent.id}" />
    <s:select list="data.takeUsersList" listKey="id" listValue="name" name="sourceGroupID" label="Izvorišna grupa"></s:select>
	<s:submit />
	</s:form>
</s:if>
</s:if>

<div>
<a href="<s:url action="ShowGroupTree"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.groupsTree"/></a>
</div>

