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

<div><s:property value="data.courseInstance.course.name"/> (<s:property value="data.courseInstance.course.isvuCode"/>)</div>
<div><s:property value="data.courseComponent.descriptor.name"/></div>

<s:form action="CCIManager">
	<s:hidden name="itemBean.id" value="%{itemBean.id}"/>
	<s:textfield name="itemBean.position" label="%{getText('forms.itemPosition')}" required="true"/>
	<s:textfield name="itemBean.name" label="%{getText('forms.itemName')}" required="true"/>
	<s:hidden name="courseComponentID" value="%{data.courseComponent.id}"/>
<s:submit method="updateItem"/>
</s:form>

<div>
  <a href="<s:url action="Main"/>"><s:text name="Navigation.main"/></a>
 | <a href="<s:url action="ShowCourse"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.courseHome"/></a>
 | <a href="<s:url action="CCManager"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.courseComponentManager"/></a>
</div>
</div>