<%@ page language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>

<h1><s:property value="displayName" /></h1>

<s:if test="subscription == null">
	<div><a href="<s:url action="ForumIndex" method="add">
		<s:param name="categoryId" value="id" />
		<s:param name="courseInstanceID" value="courseInstanceID" />
	</s:url>"><s:text name="Forum.subscribe" /></a></div>
</s:if>

<div>
	<s:if test="hidden"><s:text name="Forum.categoryHidden" /></s:if>
	<s:elseif test="closed"><s:text name="Forum.categoryClosed" /></s:elseif>
	<s:elseif test="canCreateSubforum">
		<a href="<s:url action="Subforum" method="input">
			<s:param name="cid" value="id" />
			<s:param name="courseInstanceID" value="courseInstanceID" />
		</s:url>"><s:text name="Forum.newSubforum" /></a>
	</s:elseif>
	<s:if test="canEditCategory">
		<a href="<s:url action="Category" method="input">
			<s:param name="mid" value="id" />
			<s:param name="courseInstanceID" value="courseInstanceID" />
		</s:url>"><s:text name="Forum.editCategory" /></a>
	</s:if>
</div>

<table>
<tr>
	<th><s:text name="Forum.subforum" /></th><th><s:text name="Forum.lastPost" /></th>
	<th><s:text name="Forum.topicCount" /></th><th><s:text name="Forum.postCount" /></th>
</tr>
	<s:if test="pageSubforums.size() == 0">
		<tr><td colspan="4"><s:text name="Forum.noSubforums" /></td></tr>
	</s:if>
	<s:else>
		<s:iterator value="pageSubforums">
			<tr>
			<td>
				<div>
					<s:if test="hidden">[<s:text name="Forum.subforumHidden" />]</s:if>
					<s:elseif test="closed">[<s:text name="Forum.subforumClosed" />]</s:elseif>
					<a href="<s:url action="Subforum">
						<s:param name="mid" value="id" />
						<s:param name="courseInstanceID" value="courseInstanceID" />
					</s:url>"><s:property value="name" /></a>
				</div>
				<s:if test="description != null">
					<div><s:property value="description" /></div>
				</s:if>
			</td>
			<s:if test="firstTopic != null">
				<td>
					<a href="<s:url action="Topic">
						<s:param name="mid" value="firstTopic.id" />
						<s:param name="ordinal" value="firstTopic.lastPost.ordinal" />
						<s:param name="courseInstanceID" value="courseInstanceID" />
					</s:url>&#35;post_<s:property value="firstTopic.lastPost.ordinal" />"><s:property value="firstTopic.name" /></a> <br />
					<s:text name="Forum.by" /> <s:property value="firstTopic.lastPost.author.firstName" />
					<s:property value="firstTopic.lastPost.author.lastName" />
					<div><s:date name="firstTopic.modificationDate" format="%{getText('locale.datetime')}"/></div>
				</td>
			</s:if>
			<s:else>
				<td>N/A</td>
			</s:else>
			<td><s:property value="topicCount" /></td>
			<td><s:property value="postCount" /></td>
			</tr>
		</s:iterator>
	</s:else>
</table>
