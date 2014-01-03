<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div class="content course-poll-overview withsidecontent">
	<div class="maincontent">
	<div class="inner-padding">
	<h2>Rezultati anketa</h2>

	<ul class="polls">
	<s:iterator value="data.polls">
		<li>
			<span class="title" title="<s:property value="id" />">
				<a href="<s:url action="CourseResults"><s:param name="id"><s:property value="id"/></s:param><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>">
					<s:property value="title" />
				</a>
			</span>
			<div class="description"><s:property value="description" /></div>
			<div class="enddate">Otvorena od 
			<em><s:date name="startDate" format="%{getText('locale.datetime')}" /></em> do
			<em><s:date name="endDate" format="%{getText('locale.datetime')}" /></em>
			</div>
		</li>
	</s:iterator>
	</ul>
	</div>
	</div>
</div>
