<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<h2><s:text name="Repository.allVersions"/></h2>
<hr/>
<s:if test="data.courseInstance.course.repository!=null">
	<s:iterator value="data.files" status="stat">
	<li>	
	Datoteka: <s:property value="realName"/>  VERZIJA:<s:property value="fileVersion"/>	
		<a href="<s:url action="DownloadFile"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param><s:param name="fileID"><s:property value="id"/></s:param></s:url>"><s:text name="Repository.downloadFile"/></a>
		<a href="<s:url action="ViewFileInfo"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param><s:param name="fileID"><s:property value="id"/></s:param></s:url>"><s:text name="Repository.viewFileInfo"/></a>
	</li>
	</s:iterator>
</s:if>
<hr/>
<a href="<s:url action="Repository"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Repository.return"/></a>
	