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
        <a href="<s:url action="CCTManager" method="viewTaskInfo"><s:param name="id" value="data.courseComponentTask.id"/><s:param name="filterGroupID" value="data.filterGroupID"/></s:url>">
        <s:property value="data.courseComponentTask.title"/>
        </a>
        <ul>
          <li>
          <a href="<s:url action="CCTManager" method="viewTaskUsers"><s:param name="id" value="data.courseComponentTask.id"/><s:param name="filterGroupID" value="data.filterGroupID"/></s:url>">
          <s:text name="Navigation.viewTaskUsers"/>
          </a>
          </li>
        </ul>
        </li>
      </ul>
      </li>
    </ul>
  </li>
</ul>
<h1 align="center"><s:property value="data.assignmentBean.lastName"/> <s:property value="data.assignmentBean.firstName"/> (<s:property value="data.assignmentBean.jmbag"/>)</h1>

Datoteke:
<br>
<s:if test="data.assignmentBean.fileList!=null && data.assignmentBean.fileList.size()>0">
<ul>
<s:iterator value="data.assignmentBean.fileList">
  <li>
    <s:property value="fileName" />
    <s:if test="tag!=null && !tag.equals('')">(<s:property value="tag"/>)</s:if>
    (<s:property value="uploadDate" />)
    [<a href="<s:url action="CCTManager" method="viewFile"><s:param name="id" value="id"/></s:url>"><s:text name="Navigation.viewFile"/></a>]
  </li>
</s:iterator>
</ul>
</s:if>
<s:else>
  Nema datoteka
  <br><br>
</s:else>
Zaključano: 
<s:if test="data.assignmentBean.isLocked()"> 
  da (<s:property value="data.assignmentBean.lockingDate"/>)
  [<a href="<s:url action="CCTManager" method="unlockAssignment"><s:param name="id" value="data.courseComponentTaskAssignment.id"/><s:param name="filterGroupID" value="data.filterGroupID"/></s:url>"><s:text name="Navigation.unlockAssignment"/></a>]
</s:if>
<s:else>
  ne
</s:else>
<br><br>
<s:form action="CCTManager" theme="ferko">
<s:checkbox name="reviewBean.reviewed" label="%{getText('forms.reviewed')}" />
<s:textfield name="reviewBean.extension" label="%{getText('forms.extensionDate')}" />
<s:textfield name="reviewBean.score" label="%{getText('forms.score')}"/>
<s:checkbox name="reviewBean.passed" label="%{getText('forms.passed')}" />
<s:textarea cols="60" rows="20" name="reviewBean.comment" label="%{getText('forms.comment')}"></s:textarea>
<s:hidden name="id" value="%{data.courseComponentTaskAssignment.id}"/>
<s:hidden name="filterGroupID" value="%{data.filterGroupID}"/>
<s:submit method="reviewAssignment"/>
</s:form>
<br>

<div>
  <a href="<s:url action="Main"/>"><s:text name="Navigation.main"/></a>
 | <a href="<s:url action="ShowCourse"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.courseHome"/></a>
 | <a href="<s:url action="CCManager"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.courseComponentManager"/></a>
 | <a href="<s:url action="CCIManager" method="viewItem"><s:param name="id" value="data.courseComponentItem.id"/></s:url>"><s:text name="Navigation.viewComponentItem"/></a>
 | <a href="<s:url action="CCTManager" method="viewTaskInfo"><s:param name="id" value="data.courseComponentTask.id"/></s:url>"><s:text name="Navigation.viewTaskInfo"/></a>
 | <a href="<s:url action="CCTManager" method="viewTaskUsers"><s:param name="id" value="data.courseComponentTask.id"/><s:param name="filterGroupID" value="data.filterGroupID"/></s:url>"><s:text name="Navigation.viewTaskUsers"/></a>
</div>
</div>