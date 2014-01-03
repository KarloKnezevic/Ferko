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
        </li>
      </ul>
      </li>
    </ul>
  </li>
</ul>
<h1 align="center">Rješenja studenata</h1>
<s:if test="taskUsersList!=null && taskUsersList.size()>0">
  <table>
    <thead>
      <tr>
        <th>Broj</th>
        <th>Student</th>
        <th>Zaključano</th>
        <th>Pregledano</th>
        <th>Detalji</th>
      </tr>
    </thead>
    <tbody>
      <s:iterator value="taskUsersList" status="stat">
      <s:if test="#stat.isEven()">
    	<tr bgcolor="#F0F0F0" >
  	</s:if>
  	<s:else>
  		<tr>
  	</s:else>
        <td><s:property value="(#stat.index)+1"/></td>
        <td><s:property value="lastName"/> <s:property value="firstName"/> (<s:property value="jmbag"/>)</td>
        <td><s:property value="locked"/></td>
        <td><s:property value="reviewed"/></td>
        <td><a href="<s:url action="CCTManager" method="viewAssignmentStatus"><s:param name="id" value="assignmentID"/><s:param name="filterGroupID" value="data.filterGroupID"/></s:url>"><s:text name="Navigation.viewAssignmentStatus"/></a></td>
      </tr>
  	  </s:iterator>
    </tbody>
  </table>
  <a href="<s:url action="CCTManager" method="getZipFile"><s:param name="id" value="id"/><s:param name="filterGroupID" value="data.filterGroupID"/></s:url>"><s:text name="Navigation.getZipFile"/></a>
</s:if>
<s:else>
  Nema studenata na ovom zadatku
</s:else>
<div>
  <a href="<s:url action="Main"/>"><s:text name="Navigation.main"/></a>
 | <a href="<s:url action="ShowCourse"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.courseHome"/></a>
 | <a href="<s:url action="CCManager"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.courseComponentManager"/></a>
 | <a href="<s:url action="CCIManager" method="viewItem"><s:param name="id" value="data.courseComponentItem.id"/></s:url>"><s:text name="Navigation.viewComponentItem"/></a>
 | <a href="<s:url action="CCTManager" method="viewTaskInfo"><s:param name="id" value="data.courseComponentTask.id"/></s:url>"><s:text name="Navigation.viewTaskInfo"/></a>
</div>
</div>