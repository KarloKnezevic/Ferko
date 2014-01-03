<%@ page language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<a href="<s:url action="ForumIndex" method="input" />"><s:text name="Forum.editSubscriptions" /></a>
<s:if test="canCreateCategory">
	<a href="<s:url action="Category" method="input" />"><s:text name="Forum.newNonCourseCategory" /></a>
</s:if>

<s:if test="subscriptions.size() == 0">
	<p><s:text name="Forum.noSubscriptions" /></p>
</s:if>
<s:else>
	<table>
	<tr>
		<th><s:text name="Forum.subforum" /></th><th><s:text name="Forum.lastPost" /></th>
		<th><s:text name="Forum.topicCount" /></th><th><s:text name="Forum.postCount" /></th>
	</tr>
	<s:iterator value="subscriptions">
		<tr><td colspan="4">
			<s:if test="category.hidden">[<s:text name="Forum.categoryHidden" />]</s:if>
			<s:elseif test="category.closed">[<s:text name="Forum.categoryClosed" />]</s:elseif>
			<a href="<s:url action="Category">
			<s:param name="mid" value="category.id" />
			</s:url>"><s:property value="category.displayName" /></a></td></tr>
		<s:if test="category.subforums.size() == 0">
			<tr><td colspan="4"><s:text name="Forum.noSubforums" /></td></tr>
		</s:if>
		<s:else>
			<s:iterator value="category.subforums">
				<tr>
				<td>
					<div>
						<s:if test="hidden">[<s:text name="Forum.subforumHidden" />]</s:if>
						<s:elseif test="closed">[<s:text name="Forum.subforumClosed" />]</s:elseif>
						<a href="<s:url action="Subforum">
							<s:param name="mid" value="id" />
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
	</s:iterator>
	</table>
</s:else>