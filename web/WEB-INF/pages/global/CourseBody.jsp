<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<%@ include file="/WEB-INF/pages/global/CourseHeader.jsp" %>

<div id="body" class="container courseBody">
		<h1><s:property value="data.courseInstance.course.name"/></h1>
		<tiles:insertAttribute name="body"/>
</div>
