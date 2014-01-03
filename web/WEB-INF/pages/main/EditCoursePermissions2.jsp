<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<script type="text/javascript" src="/ferko/js/jquery.autocomplete.json.js"></script>

<s:if test="data.messageLogger.hasMessages()">
  <ul>
  <s:iterator value="data.messageLogger.messages">
      <li>[<s:property value="messageType"/>]<s:property value="messageText"/></li>
  </s:iterator>
  </ul>
</s:if>

<s:if test="bean.userPermissions.size > 0">
<s:form action="EditCoursePermissions" method="POST" theme="simple">
<table>
  <tr><th>Korisnik</th><th>Dozvole</th></tr>
  <s:iterator value="bean.userPermissions" status="stat">
  <tr>
    <td><s:property value="lastName"/>, <s:property value="firstName"/> (<s:property value="jmbag"/>)</td>
    <td>
      <s:hidden name="bean.userPermissions[%{#stat.index}].id"></s:hidden>
      <s:hidden name="bean.userPermissions[%{#stat.index}].jmbag"></s:hidden>
      <s:hidden name="bean.userPermissions[%{#stat.index}].firstName"></s:hidden>
      <s:hidden name="bean.userPermissions[%{#stat.index}].lastName"></s:hidden>
      <s:checkboxlist list="data.availablePermissions" listKey="id" listValue="title" name="bean.userPermissions[%{#stat.index}].groupRelativePaths"></s:checkboxlist>
    </td>
  </tr>
  </s:iterator>
</table>
<s:hidden name="courseInstanceID" value="%{data.courseInstance.id}"></s:hidden>
<s:submit method="update"></s:submit>
</s:form>
</s:if>
<s:else>
<div>Na kolegiju još nitko nema dozvole.</div>
</s:else>

<div style="margin-top: 10px; margin-bottom: 10px; border: 1px dashed gray; padding: 0px;">
  <div style="background-color: #DDDDDD; font-weight: bold;">Dodavanje novog korisnika</div>
  <div style="padding: 5px;">
  <s:form action="EditCoursePermissions" theme="ferko">
	<s:textfield name="user" size="50" label="Korisnik"></s:textfield>
	<s:checkboxlist list="data.availablePermissions" listKey="id" listValue="title" name="bean.newUser.groupRelativePaths" />
	<s:hidden name="courseInstanceID" value="%{data.courseInstance.id}" />
	<s:submit method="add"></s:submit>
  </s:form>
  </div>
</div>

<script type="text/javascript">
  $(document).ready(function(){
    $("#EditCoursePermissions_user").autocomplete("<s:url action="StaffUsersListJSON" />", {minChars: 1, extraParams: {}});
    $("#EditCoursePermissions_user").attr("autocomplete", "off");
  });
</script>
