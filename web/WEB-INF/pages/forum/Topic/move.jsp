<%@ page language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>

<div id="body" class="container courseBody">

<h2><s:property value="title" /></h2>

<s:form action="MoveTopic" theme="ferko">
	<s:select label="Podforum" name="destination" value="subforum" list="%{#{}}" size="10" >
		<s:iterator value="categories">
			<s:optgroup label="%{displayName}" list="subforums" listKey="id" listValue="name" />
		</s:iterator>
	</s:select>
	
	<s:hidden name="mid" value="%{id}" />
	<s:hidden name="courseInstanceID" value="%{courseInstanceID}" />
	
	<s:submit value="%{getText('Forum.moveTopic')}" />
</s:form>
