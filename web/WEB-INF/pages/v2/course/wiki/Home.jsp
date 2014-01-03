<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<s:if test="data.messageLogger.hasMessages()">
	<ul class="msgList">
		<s:iterator value="data.messageLogger.messages">
			<li class="msgType_<s:property value="messageType" />"><s:property value="messageText" /></li>
		</s:iterator>
	</ul>
</s:if>

<s:if test="data.editorMode">
  <s:form action="CourseWiki" method="post" theme="ferko">
    <s:textarea name="data.content" value="%{data.wikiPage.content}" cols="150" rows="30" label="%{getText('Wiki.source')}" wrap="off"/>
    <s:hidden name="data.version" value="%{data.wikiPage.version}" />
    <s:hidden name="data.pageURL" value="%{data.pageURL}" />
    <s:hidden name="data.courseInstanceID" value="%{data.courseInstance.id}" />
    <s:submit method="save" value="Pohrani" />
  </s:form>
</s:if><s:else>
<s:property value="data.wiki" escape="false"/>

<s:if test="data.editingEnabled">
  <s:form action="CourseWiki" method="post" theme="simple">
    <s:hidden name="data.pageURL" value="%{data.pageURL}" />
    <s:hidden name="data.courseInstanceID" value="%{data.courseInstance.id}" />
    <s:submit method="edit" value="Uredi stranicu" /><a target="_jcms_help" href="<s:url action="Help"><s:param name="helpKey">000035</s:param></s:url>" onclick="blur();"><img src="img/icons/help.png"></a>
  </s:form>
</s:if>
</s:else>

<hr class="hidden"/>
