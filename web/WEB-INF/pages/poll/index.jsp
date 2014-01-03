<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<h1 class="pageh">Ankete</h1>
<div class="content course-poll-overview">

	<div class="maincontent">
	<div class="inner-padding">
	<h2>Ankete za odgovoriti</h2>
	<ul class="polls">
	<s:iterator value="data.unansweredPolls">
		<li>
			<span class="title" title="<s:property value="id" />">
				<a href="<s:url action="Answer"><s:param name="id"><s:property value="pollUser.id"/></s:param></s:url>">
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
