<%@ page language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>

<div id="body" class="container courseBody">

<h2><s:property value="title" /></h2>

<s:form action="MergeTopic" theme="ferko">
	<s:textfield label="%{getText('Forum.topicURL')}" name="url" />
	<s:fielderror><s:param>url</s:param></s:fielderror>
	
	<s:hidden name="mid" value="%{id}" />
	<s:hidden name="courseInstanceID" value="%{courseInstanceID}" />
	
	<s:submit value="%{getText('Forum.mergeTopic')}" />
</s:form>
