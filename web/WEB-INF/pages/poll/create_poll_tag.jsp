<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div class="content new-poll">
	<div class="maincontent">
	<div class="inner-padding">
	<h2>Stvaranje novog poll taga</h2>
	<s:form action="CreatePollTag" method="create" theme="ferko" method="post">
		<s:textfield name="shortTitle" label="Kratki naziv" />
		<s:fielderror><s:param>shortTitle</s:param></s:fielderror>
		<s:textfield name="title" label="Puni naziv" />
		<s:fielderror><s:param>title</s:param></s:fielderror>
		<s:submit name="submit" value="Stvori" />
	</s:form>
	</div>
	</div>
</div>
