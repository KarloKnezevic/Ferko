<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<ul class="subNav">
	<li>
		<a href="<s:url action="ShowCourse" namespace="/"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>">
			<s:text name="Navigation.courseHome"/>
		</a>
	</li>
	<li><a href="<s:url action="ShowCourseEvents" namespace="/"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.calendar"/></a></li>
	<li>
		<a href="<s:url action="Repository" namespace="/"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>">
			<s:text name="Navigation.repository"/>
		</a>
	</li>
	<li><a href="<s:url action="CourseCategory" namespace="/"><s:param name="courseInstanceID" value="data.courseInstance.id" /></s:url>">
		<s:text name="Navigation.forum" /></a></li>
</ul>

<div id="body" class="container courseBody">
		<h1><s:property value="courseName"/></h1>
		<tiles:insertAttribute name="body"/>
</div>
