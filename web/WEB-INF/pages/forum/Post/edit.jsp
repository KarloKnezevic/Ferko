<%@ page language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>

<div id="body" class="container courseBody">

<h2><s:property value="title" /></h2>

<s:if test="preview != null">
	<h2><s:text name="Forum.postPreview" /></h2>
	<s:property value="preview" escape="false" />
</s:if>

<s:form action="Post" theme="ferko">
	<s:textfield label="%{getText('Forum.name')}" name="name" />
	<s:fielderror><s:param>name</s:param></s:fielderror>
	<s:textarea cols="80" rows="15" name="message" label="%{getText('Forum.postMessage')}"/>
	<s:fielderror><s:param>message</s:param></s:fielderror>
	
	<s:hidden name="sid" value="%{topic.subforum.id}" />
	<s:hidden name="tid" value="%{topic.id}" />
	<s:hidden name="mid" value="%{id}" />
	<s:hidden name="reply" value="%{reply}" />
	<s:hidden name="courseInstanceID" value="%{courseInstanceID}" />
	
	<s:if test="id == null"><s:submit value="%{getText('Forum.submitPost')}" method="save" /></s:if>
	<s:else><s:submit value="%{getText('Forum.editPost')}" method="save" /></s:else>
	<s:submit value="%{getText('Forum.previewPost')}" method="preview" />
</s:form>

<s:if test="id != null && ordinal != 1">
	<h2><s:text name="Forum.deletePost" /></h2>
	<s:form action="Delete!post" theme="ferko">
		<li><s:text name="Forum.deletePostWarning" /></li>
		
		<s:hidden name="mid" value="%{id}" />
		<s:hidden name="courseInstanceID" value="%{courseInstanceID}" />
		
		<s:submit value="%{getText('Forum.deletePost')}" />
	</s:form>
</s:if>
