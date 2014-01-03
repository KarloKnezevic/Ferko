<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<s:form action= "AddFilePageComment">
	<s:hidden name="courseInstanceID"/>
	<s:hidden name="fileID"/>
	<s:hidden name="pageNumber"/>
    <s:textarea label="Komentar" name="comment" cols="30" rows="10" cssStyle="word-wrap: break-word;"/>
    <s:submit method="addFilePageComment"/>
</s:form>

<a href="<s:url action="Repository"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Repository.return"/></a>


