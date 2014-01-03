<%@ page language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>

<div id="body" class="container courseBody">
<h1><s:property value="category.displayName"/></h1>
<h2>Podforum: <s:property value="name" /></h2>

<div>
	<s:if test="hidden"><s:text name="Forum.subforumHidden" /></s:if>
	<s:elseif test="closed"><s:text name="Forum.subforumClosed" /></s:elseif>
	<s:elseif test="canCreateTopic">
		<a href="<s:url action="Post" method="input">
			<s:param name="sid" value="id" />
			<s:param name="courseInstanceID" value="courseInstanceID" />
		</s:url>"><s:text name="Forum.newTopic" /></a>
	</s:elseif>
	<s:if test="canEditSubforum">
		<a href="<s:url action="Subforum" method="input">
			<s:param name="mid" value="id" />
			<s:param name="courseInstanceID" value="courseInstanceID" />
		</s:url>"><s:text name="Forum.editSubforum" /></a>
	</s:if>
</div>

<s:if test="pageTopics.size() == 0">
	<p class="emptyMsg"><s:text name="Forum.noTopics" /></p>
</s:if>
<s:else>
<table class="topicList">
	<tr><th><s:text name="Forum.topicName" /></th><th><s:text name="Forum.author" /></th>
	<th><s:text name="Forum.postCount" /></th><th><s:text name="Forum.lastPost" /></th></tr>
	<s:iterator value="pageTopics">
	<tr>
	<td>
		<s:if test="pinned">[<s:text name="Forum.topicPinned" />]</s:if>
		<s:if test="hidden">[<s:text name="Forum.topicHidden" />]</s:if>
		<s:elseif test="closed">[<s:text name="Forum.topicClosed" />]</s:elseif>
		<a href="<s:url action="Topic">
			<s:param name="mid" value="%{id}" />
			<s:param name="courseInstanceID" value="courseInstanceID" />
		</s:url>"><s:property value="name" /></a> 
	</td><td>
		<s:property value="lastPost.author.firstName" /> <s:property value="lastPost.author.lastName" />
	</td><td>
	  <s:property value="postCount" />
	</td><td>
		<s:date name="lastPost.creationDate" format="%{getText('locale.date')}" />
			<br /><s:text name="Forum.by" /> <s:property value="lastPost.author.firstName" /> <s:property value="lastPost.author.lastName" /> 
	</td>
	</tr>
	</s:iterator>
</table>
</s:else>
<p>
<s:text name="Forum.page" />:
<s:set name="topicsPerPage" value="20" />
<s:set name="currentPage" value="page" />
<s:set name="topicCount" value="topicCount" />
<%	int topicsPerPage = (Integer)pageContext.getAttribute("topicsPerPage");
	int currentPage = (Integer)pageContext.getAttribute("currentPage");
	int lastPage = ((Integer)pageContext.getAttribute("topicCount") - 1) / topicsPerPage + 1;
	for (int i = 0; i < lastPage; ++i) {
		if (i + 1 != currentPage) { %>
			[<a href="<s:url action="Subforum">
				<s:param name="mid" value="id" />
				<s:param name="courseInstanceID" value="courseInstanceID" />
			</s:url>&amp;page=<%= i + 1 %>"><%= i + 1 %></a>]
<%		} else { %>
			[<%= i + 1 %>]
<%		} %>
<%	} %></p>
</div>
