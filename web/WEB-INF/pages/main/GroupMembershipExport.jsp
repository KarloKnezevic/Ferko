<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div id="body" class="container">

	<s:if test="data.messageLogger.hasMessages()">
		<ul>
			<s:iterator value="data.messageLogger.messages">
				<li>[<s:property value="messageType" />] <s:property
					value="messageText" /></li>
			</s:iterator>
		</ul>
	</s:if>

    <div> 
	<s:form action="GroupMembershipExport" method="post">
		<s:select name="bean.semesterID"  label="%{getText('forms.Semester')}" list="data.allSemesters" listKey="id" listValue="fullTitle" value="%{data.yearSemester.id}"></s:select>
		<s:select name="bean.courseInstanceID"  label="%{getText('forms.courseInstances')}" list="data.allCourses" listKey="id" listValue="course.name" value="%{data.courseInstance.id}"></s:select>
		<s:textfield name="bean.parentRelativePath" label="%{getText('forms.parentRelativePath')}"></s:textfield>
		<s:textfield name="bean.relativePath" label="%{getText('forms.relativePath')}"></s:textfield>
		<s:radio list="#{'csv':'CSV','xls':'Microsoft Excel'}" name="bean.format" required="true" label="%{getText('forms.format')}"></s:radio>
		<s:checkbox name="bean.writeStudentTag" label="%{getText('forms.writeStudentTag')}"></s:checkbox>
		<s:checkbox name="bean.writeStudentName" label="%{getText('forms.writeStudentName')}"></s:checkbox>
		<s:checkbox name="bean.writeISVUCode" label="%{getText('forms.writeISVUCode')}"></s:checkbox>
		<s:submit method="input" label="%{getText('forms.fetchCourses')}" value="%{getText('forms.fetchCourses')}"></s:submit>
		<s:submit method="view" label="%{getText('forms.export')}" value="%{getText('forms.export')}"></s:submit>
	</s:form>
    </div>

<div>
  <a href="<s:url action="Main"/>"><s:text name="Navigation.main"/></a>
</div>

</div>
