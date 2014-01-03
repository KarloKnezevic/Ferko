<%@ page language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>
  
<div><a href="<s:url action="ForumIndex" />"><s:text name="Forum.forum" /></a>
	&gt; <a href="<s:url action="Category">
		<s:param name="mid" value="category.id" />
		<s:param name="courseInstanceID" value="courseInstanceID" />
	</s:url>"><s:property value="category.displayName" /></a>
</div>
<div> - <s:property value="title" /></div>

<s:form action="Subforum!save" theme="ferko">
	<s:textfield label="%{getText('Forum.name')}" name="name" />
	<s:fielderror><s:param>name</s:param></s:fielderror>
	<s:textfield label="%{getText('Forum.subforumDescription')}" name="description" />
	<s:fielderror><s:param>description</s:param></s:fielderror>
	<s:select label="%{getText('Forum.status')}" name="status" list="%{#{
			'open' : getText('Forum.subforumOpened'),
			'closed' : getText('Forum.subforumClosed'),
			'hidden' : getText('Forum.subforumHidden')}}" />

	<s:hidden name="cid" value="%{category.id}" />
	<s:hidden name="mid" value="%{id}" />
	<s:hidden name="courseInstanceID" value="%{courseInstanceID}" />

	<s:if test="id == null"><s:submit value="%{getText('Forum.newSubforum')}"/></s:if>
	<s:else><s:submit value="%{getText('Forum.editSubforum')}" /></s:else>
</s:form>
