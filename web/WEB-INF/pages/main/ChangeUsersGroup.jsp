<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div id="body" class="container">

<s:if test="data.messageLogger.hasMessages()">
  <ul>
  <s:iterator value="data.messageLogger.messages">
      <li>[<s:property value="messageType"/>]<s:property value="messageText"/></li>
  </s:iterator>
  </ul>
</s:if>

<s:else>
	<h3><s:property value="data.userGroup.user.lastName"/>, <s:property value="data.userGroup.user.firstName"/> (<s:property value="data.userGroup.user.jmbag"/>)<h3>
	<p>Trenutna grupa je: <s:property value="data.userGroup.group.name"/></p>

	<s:form action="ChangeUsersGroup" theme="ferko">
	<s:select name="newGroupID" list="data.allGroups" listKey="id" listValue="name" required="true" />
	<s:hidden name="courseInstanceID" value="%{data.courseInstance.id}" />
	<s:hidden name="userGroupID" value="%{data.userGroup.id}" />
	<s:hidden name="groupID" value="%{data.userGroup.group.id}" />
	<s:hidden name="relativePath" value="%{data.relativePath}" />
	<s:submit method="change" />
	</s:form>
</s:else>


<div>
  <a href="<s:url action="Main"/>"><s:text name="Navigation.main"/></a>
 | <a href="<s:url action="ShowCourse"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.courseHome"/></a>
 | <a href="<s:url action="ShowGroupUsers"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param><s:param name="relativePath"><s:property value="relativePath"/></s:param></s:url>"><s:text name="Navigation.groupHome"/></a>
</div>

</div>
