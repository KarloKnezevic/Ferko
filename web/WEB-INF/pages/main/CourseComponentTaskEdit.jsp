<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div id="body" class="container">

<s:if test="data.messageLogger.hasMessages()">
  <ul>
  <s:iterator value="data.messageLogger.messages">
      <li>[<s:property value="messageType"/>] <s:property value="messageText"/></li>
  </s:iterator>
  </ul>
</s:if>

<a href="<s:url action="ShowCourse"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>">
<s:property value="data.courseInstance.course.name"/> (<s:property value="data.courseInstance.course.isvuCode"/>)
</a>
<ul>
  <li>
    <a href="<s:url action="CCManager"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>">
    <s:property value="data.courseComponent.descriptor.name"/>
    </a>
    <ul>
      <li>
      <a href="<s:url action="CCIManager" method="viewItem"><s:param name="id" value="data.courseComponentItem.id"/></s:url>">
      <s:property value="data.courseComponentItem.position"/>. <s:property value="data.courseComponent.descriptor.positionalName"/> - <s:property value="data.courseComponentItem.name"/>
      </a>
      </li>
    </ul>
  </li>
</ul>

<s:form action="CCTManager">
	<s:textfield name="taskBean.title" label="%{getText('forms.taskTitle')}" required="true"/>
	<s:textarea  cols="80" rows="10" name="taskBean.description" label="%{getText('forms.taskDescription')}" required="true"/>
	<s:textfield name="taskBean.deadline" label="%{getText('forms.taskDeadline')}"/>
	<s:textfield name="taskBean.filesRequiredCount" label="%{getText('forms.taskFilesRequiredCount')}" required="true"/>
	<s:textfield name="taskBean.fileTags" label="%{getText('forms.taskFileTags')}" />
	<s:textfield name="taskBean.maxFileSize" label="%{getText('forms.taskMaxFileSize')}" required="true"/>
	<s:textfield name="taskBean.maxFilesCount" label="%{getText('forms.taskMaxFilesCount')}" required="true"/>
	<s:hidden name="courseComponentItemID" value="%{data.courseComponentItem.id}"/>
	<s:hidden name="taskBean.id" value="%{taskBean.id}"/>
<s:submit method="updateTask"/>
</s:form>

<div>
  <a href="<s:url action="Main"/>"><s:text name="Navigation.main"/></a>
 | <a href="<s:url action="ShowCourse"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.courseHome"/></a>
 | <a href="<s:url action="CCManager"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.courseComponentManager"/></a>
 | <a href="<s:url action="CCIManager" method="viewItem"><s:param name="id" value="data.courseComponentItem.id"/></s:url>"><s:text name="Navigation.viewComponentItem"/></a>
</div>
</div>