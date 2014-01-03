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
<h1 align="center"><s:property value="data.taskBean.title"/></h1>
Opcije:
<s:if test="data.isAdmin()">
  <a href="<s:url action="CCTManager" method="editTask"><s:param name="id" value="id"/></s:url>"><s:text name="Navigation.editComponentTask"/></a>
  | <a href="<s:url action="CCTManager" method="removeTask"><s:param name="id" value="id"/></s:url>"><s:text name="Navigation.erase"/></a>
  | <a href="<s:url action="CCTManager" method="viewTaskUsers"><s:param name="id" value="id"/><s:param name="filterGroupID" value="data.filterGroupID"/></s:url>"><s:text name="Navigation.viewTaskUsers"/></a>
  | <a href="<s:url action="CCTManager" method="autoAssignTask"><s:param name="id" value="id"/></s:url>"><s:text name="Navigation.autoAssign"/></a>
  | <a href="<s:url action="CCTManager" method="newAssignTask"><s:param name="id" value="id"/></s:url>"><s:text name="Navigation.assignUsers"/></a>
  | <a href="<s:url action="CCTManager" method="viewReviewers"><s:param name="id" value="id"/></s:url>"><s:text name="Navigation.editReviewers"/></a>
</s:if>
<s:elseif test="data.isStaffMember()">
  <a href="<s:url action="CCTManager" method="viewTaskUsers"><s:param name="id" value="id"/><s:param name="filterGroupID" value="data.filterGroupID"/></s:url>"><s:text name="Navigation.viewTaskUsers"/></a>
</s:elseif>
<br><br>
Rok: <s:if test="data.taskBean.deadline == null"> - </s:if>
<s:else><s:property value="data.taskBean.deadline"/><br></s:else> 
<br>
Opis:
<br>
<s:property value="data.taskBean.description"/><br><br>
Ogranicenja: 
<ul>
  <li>
    Broj potrebnih datoteka:
    <s:if test="!data.taskBean.filesRequiredCount.equals('-1')">
      <s:property value="data.taskBean.filesRequiredCount" />
    </s:if>
    <s:else>
      Neograniceno
    </s:else>	  
  </li>
  <li>
    Maksimalna veličina datoteke: <s:property value="data.taskBean.maxFileSize"/> MB
  </li>
  <li>
    Maksimalan broj datoteka: <s:property value="data.taskBean.maxFilesCount"/>
  </li>
</ul>

<div>
  <a href="<s:url action="Main"/>"><s:text name="Navigation.main"/></a>
 | <a href="<s:url action="ShowCourse"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.courseHome"/></a>
 | <a href="<s:url action="CCManager"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.courseComponentManager"/></a>
 | <a href="<s:url action="CCIManager" method="viewItem"><s:param name="id" value="data.courseComponentItem.id"/></s:url>"><s:text name="Navigation.viewComponentItem"/></a>
</div>
</div>