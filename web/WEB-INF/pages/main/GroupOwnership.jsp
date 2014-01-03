<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<div id="body" class="container">
	<h1><s:property value="data.courseInstance.course.name"/></h1>


<s:if test="bean.users.size > 0">
<s:form action="GroupOwnership" method="POST" theme="simple">
<table>
  <tr><th>Korisnik</th><th>Grupe</th></tr>
  <s:iterator value="bean.users" status="stat">
  <tr>
    <td><s:property value="firstName"/>, <s:property value="lastName"/> (<s:property value="jmbag"/>)</td>
    <td>
      <s:hidden name="bean.users[%{#stat.index}].id"></s:hidden>
      <s:hidden name="bean.users[%{#stat.index}].jmbag"></s:hidden>
      <s:hidden name="bean.users[%{#stat.index}].firstName"></s:hidden>
      <s:hidden name="bean.users[%{#stat.index}].lastName"></s:hidden>
      <s:checkboxlist list="data.allGroups" listKey="id" listValue="name" name="bean.users[%{#stat.index}].groups"></s:checkboxlist>
    </td>
  </tr>
  </s:iterator>
</table>
<s:hidden name="bean.courseInstanceID" value="%{data.courseInstance.id}"></s:hidden>
<s:hidden name="relativePath" value="%{relativePath}"></s:hidden>
<s:submit method="update"></s:submit>
</s:form>
</s:if>
<s:else>
  Nema registriranih korisnika koji mogu biti dodijeljeni grupama.
</s:else>

<div class="bottomNavMenu">
  <a href="<s:url action="Main"/>"><s:text name="Navigation.main"/></a>
 | <a href="<s:url action="ShowCourse"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.courseHome"/></a>
</div>

</div>
