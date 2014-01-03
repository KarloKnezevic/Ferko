<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<h1 class="pageh">Ankete: <em><s:property value="data.poll.title"/></em></h1>
<div class="content answer-poll">
	<div class="maincontent">
	<div class="inner-padding">
		<p class="description"><s:property value="data.poll.description"/></p>

		<s:form action="Answer" theme="ferko" method="post" >
			<s:property value="data.form" escape="false"/>
			<s:hidden name="PUID" value="%{data.pollUserId}" />
		<s:submit value="Odgovori" />
		</s:form>
	</div>
	</div>
</div>
