<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<ul class="subNav">

	<li>
		<a href="<s:url action="ShowCourse"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>">
			<s:text name="Navigation.courseHome"/>
		</a>
	</li>

	<!--li><a href="#"><s:text name="Navigation.syllabus"/></a></li-->
	<li><a href="<s:url action="ShowCourseEvents"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.calendar"/></a></li>

	<li>
		<a href="<s:url action="Repository"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>">
			<s:text name="Navigation.repository"/>
		</a>
	</li>

	<!--li><a href="#">Wiki</a></li-->
	<li><a href="<s:url action="CourseCategory"><s:param name="courseInstanceID" value="data.courseInstance.id" /></s:url>">
		<s:text name="Navigation.forum" /></a></li>
</ul>
