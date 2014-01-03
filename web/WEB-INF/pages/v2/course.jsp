<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<h1 class="pageh"><s:property value="data.courseInstance.course.name"/></h1>
<ul class="subNav">
	<li>
		<a href="<s:url action="ShowCourse" namespace="/"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>">
			<s:text name="Navigation.courseHome"/>
		</a>
	</li>
	<!--li><a href="#"><s:text name="Navigation.syllabus"/></a></li-->
	<li><a href="<s:url action="ShowCourseEvents" namespace="/"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.calendar"/></a></li>
	<li>
		<a href="<s:url action="Repository" namespace="/"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>">
			<s:text name="Navigation.repository"/>
		</a>
	</li>
	<!--li><a href="#">Wiki</a></li-->
	<li><a href="<s:url action="CourseCategory" namespace="/"><s:param name="courseInstanceID" value="data.courseInstance.id" /></s:url>">
		<s:text name="Navigation.forum" /></a></li>
</ul>

<tiles:insertAttribute name="body"/>

