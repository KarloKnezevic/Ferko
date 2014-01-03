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
<h1 align="center">Dodjela asistenata</h1>
Popis osoba:
<s:if test="reviewersList != null && reviewersList.size()>0">
	<s:form action="CCTManager" theme="simple">
	<table>
			<thead>
				<tr>
					<th>
						Prezime
					</th>
					<th>
						Ime
					</th>
					<th>
						Odabrati
					</th>
				</tr>
			</thead>
			<tbody>
				<s:iterator value="reviewersList" status="stat">
				<tr>
					<td>
						<s:hidden name="reviewersList[%{#stat.index}].userID" value="%{userID}"/>
						<s:property value="%{lastName}"/>
					</td>
					<td><s:property value="%{firstName}" /></td>
					<td><s:checkbox name="reviewersList[%{#stat.index}].taken" value="%{taken}" /></td>
				</tr>
				</s:iterator>			
			</tbody>
	</table>
	<s:hidden name="id" value="%{data.courseComponentTask.id}"></s:hidden>
	<s:submit method="updateReviewers"/>
	</s:form>
</s:if>
<s:else>
Nema osoba za odabrati
</s:else>
<div>
  <a href="<s:url action="Main"/>"><s:text name="Navigation.main"/></a>
 | <a href="<s:url action="ShowCourse"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.courseHome"/></a>
 | <a href="<s:url action="CCManager"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.courseComponentManager"/></a>
 | <a href="<s:url action="CCIManager" method="viewItem"><s:param name="id" value="data.courseComponentItem.id"/></s:url>"><s:text name="Navigation.viewComponentItem"/></a>
 | <a href="<s:url action="CCTManager" method="viewTaskInfo"><s:param name="id" value="data.courseComponentTask.id"/></s:url>"><s:text name="Navigation.viewTaskInfo"/></a>
</div>
</div>