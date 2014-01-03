<%@ page language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>

<div><a href="<s:url action="ForumIndex" />"><s:text name="Forum.forum" /></a>
	&gt; <a href="<s:url action="Category">
		<s:param name="mid" value="subforum.category.id" />
		<s:param name="courseInstanceID" value="courseInstanceID" />
	</s:url>"><s:property value="subforum.category.displayName" /></a>
	&gt; <a href="<s:url action="Subforum">
		<s:param name="mid" value="subforum.id" />
		<s:param name="courseInstanceID" value="courseInstanceID" />
	</s:url>"><s:property value="subforum.name" /></a>	
</div>
<div> - <s:property value="title" /></div>

<s:form action="Topic!save" theme="ferko">
	<s:textfield label="%{getText('Forum.name')}" name="name" />
	<s:fielderror><s:param></s:param></s:fielderror>
	<s:checkbox label="%{getText('Forum.topicPinned')}" name="pinned" />
	<s:select label="%{getText('Forum.status')}" name="status" list="%{#{
			'open' : getText('Forum.topicOpened'),
			'closed' : getText('Forum.topicClosed'),
			'hidden' : getText('Forum.topicHidden')}}" />

	<s:hidden name="mid" value="%{id}" />
	<s:hidden name="courseInstanceID" value="%{courseInstanceID}" />
	
	<s:submit value="%{getText('Forum.editTopic')}" />
</s:form>

<s:if test="canEditTopic">
	<h2><s:text name="Forum.deleteTopic" /></h2>
	<s:if test="!hidden">
		<p><s:text name="Forum.topicDeleteHidden" /></p>
	</s:if>
	<s:else>
		<s:form action="Delete!topic" theme="ferko">
			<s:text name="Forum.deleteTopicWarning" />
			
			<s:hidden name="mid" value="%{id}" />
			<s:hidden name="courseInstanceID" value="%{courseInstanceID}" />
			
			<s:submit value="%{getText('Forum.deleteTopic')}" />
		</s:form>
	</s:else>
</s:if>