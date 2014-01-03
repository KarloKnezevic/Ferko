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
      <ul>
        <li>
        <a href="<s:url action="CCTManager" method="viewTaskInfo"><s:param name="id" value="data.courseComponentTask.id"/></s:url>">
        <s:property value="data.courseComponentTask.title"/>
        </a>
        </li>
      </ul>
      </li>
    </ul>
  </li>
</ul>
<h1 align="center"><s:property value="data.courseComponentTask.title"/></h1>
<s:form action="CCTManager">
<s:textarea cols="50" rows="10" name="importData" label="%{getText('forms.taskAssignImport')}"/>
<s:hidden name="id" value="%{data.courseComponentTask.id}" />
<s:submit method="assignTask"/>
</s:form>
<div>
  <a href="<s:url action="Main"/>"><s:text name="Navigation.main"/></a>
 | <a href="<s:url action="ShowCourse"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.courseHome"/></a>
 | <a href="<s:url action="CCManager"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.courseComponentManager"/></a>
 | <a href="<s:url action="CCIManager" method="viewItem"><s:param name="id" value="data.courseComponentItem.id"/></s:url>"><s:text name="Navigation.viewComponentItem"/></a>
 | <a href="<s:url action="CCTManager" method="viewTaskInfo"><s:param name="id" value="data.courseComponentTask.id"/></s:url>"><s:text name="Navigation.viewTaskInfo"/></a>
</div>
</div>