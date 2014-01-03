<%@ page language="java" pageEncoding="UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ taglib prefix="tiles" uri="http://tiles.apache.org/tags-tiles" %>

<div id="body" class="container courseBody">

<ul class="depthNav">
	<li><a href="<s:url action="Category">
		<s:param name="mid" value="subforum.category.id" />
		<s:param name="courseInstanceID" value="courseInstanceID" />
	</s:url>"><s:text name="Forum.forum" /></a></li>
	<li><a href="<s:url action="Subforum">
		<s:param name="mid" value="subforum.id" />
		<s:param name="courseInstanceID" value="courseInstanceID" />
	</s:url>"><s:property value="subforum.name" /></a></li>
</ul>

<h1><s:property value="subforum.category.displayName"/></h1>
<h2>Tema: <s:property value="name" /></h2>
<s:if test="hidden"><s:text name="Forum.topicHidden" /></s:if>
<s:elseif test="closed"><s:text name="Forum.topicClosed" /></s:elseif>
<s:if test="canEditTopic">
	<a href="<s:url action="Topic" method="input">
		<s:param name="mid" value="id" />
		<s:param name="courseInstanceID" value="courseInstanceID" />
	</s:url>"><s:text name="Forum.editTopic" /></a>
	<a href="<s:url action="MoveTopic" method="input">
		<s:param name="mid" value="id" />
		<s:param name="courseInstanceID" value="courseInstanceID" />
	</s:url>"><s:text name="Forum.moveTopic" /></a>
	<a href="<s:url action="MergeTopic" method="input">
		<s:param name="mid" value="id" />
		<s:param name="courseInstanceID" value="courseInstanceID" />
		</s:url>"><s:text name="Forum.mergeTopic" /></a>
</s:if>

<ol class="postList">
	<s:iterator value="pagePosts">
		<li class="post" id="post_<s:property value="ordinal" />">
		<h4><s:property value="name" /></h4>
		<div class="postInfo">
			<span class="author">
				<s:property value="author.firstName" /> <s:property value="author.lastName" />
			</span>
			<span class="time">
				<s:date name="creationDate" format="%{getText('locale.datetime')}"/> 
			</span>
		</div>
		<a href="#post_<s:property value="ordinal" />" class="permalink">Permalink</a>		<div class="postMessage">
			<p><s:property value="message"/></p>
			<s:if test="editor != null">
				<p class="changeInfo">Zadnja promjena: <s:date name="modificationDate" format="%{getText('locale.datetime')}"/> (<s:property value="editor.firstName" /> <s:property value="editor.lastName" />)</p>
			</s:if>
		</div>
				<ul class="postActions">
					<s:if test="(author.equals(loggedUser) && canEditOwnPost) || canEditOthersPost">
						<li class="editPost"><a href="<s:url action="Post" method="input">
							<s:param name="mid" value="id" />
							<s:param name="courseInstanceID" value="courseInstanceID" />	
						</s:url>" ><s:text name="Forum.editPost" /></a>
						</li>
					</s:if>
					<s:if test="canCreatePost">
						<li class="postReply">
						<a href="<s:url action="Post" method="input">
						<s:param name="tid" value="topic.id" />
						<s:param name="reply" value="id" />
						<s:param name="courseInstanceID" value="courseInstanceID" />
						</s:url>"><s:text name="Forum.replyPost" /></a>
						</li>
					</s:if>
				</ul>
		</li>
	</s:iterator>
	</ol>
	<s:if test="canCreatePost">
		<a href="<s:url action="Post" method="input">
			<s:param name="tid" value="id" />
			<s:param name="courseInstanceID" value="courseInstanceID" />
		</s:url>"><s:text name="Forum.newPost" /></a>
	</s:if>
	<s:else><s:text name="Forum.topicClosed" /></s:else>

<p>
<s:text name="Forum.page" />:
<s:set name="postsPerPage" value="10" />
<s:set name="currentPage" value="pagePosts[0].ordinal / #postsPerPage + 1" />
<s:set name="lastPage" value="(postCount - 1) / #postsPerPage + 1" />
<%	int postsPerPage = (Integer)pageContext.getAttribute("postsPerPage");	
	int currentPage = (Integer)pageContext.getAttribute("currentPage");
	int lastPage = (Integer)pageContext.getAttribute("lastPage");
	for (int i = 0; i < lastPage; ++i) {
		if (i + 1 != currentPage) { %>
			[<a href="<s:url action="Topic">
				<s:param name="mid" value="id" />
				<s:param name="courseInstanceID" value="courseInstanceID" />
			</s:url>&amp;ordinal=<%= i * postsPerPage + 1 %>"><%= i + 1 %></a>]
<%		} else { %>
			[<%= i + 1 %>]
<%		} %>
<%	} %>
</p>

</div>
