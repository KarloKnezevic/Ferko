<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div class="content">
	<div class="maincontent">
	<div class="inner-padding">
		<s:form action="AddCourseGroups" theme="ferko" cssClass="cmxform" method="post">
			<li><strong>Grupe za predavanja</strong></li>
			<s:checkboxlist list="data.lectureGroups" listKey="id" listValue="name" name="group" label="Izaberi grupe za prikaz" />
			<li><strong>Grupe za labose</strong></li>
			<s:checkboxlist list="data.labGroups" listKey="id" listValue="name" name="group" label="Izaberi grupe za prikaz" />
			<li><strong>Privatne grupe</strong></li>
			<s:checkboxlist list="data.privateGroups" listKey="id" listValue="name" name="group" label="Izaberi grupe za prikaz" />
			<s:hidden name="courseInstanceID" value="%{courseInstanceID}"/>
			<s:hidden name="id" value="%{data.id}"/>
			<s:submit />
		</s:form>
	</div>
	</div>
</div>

