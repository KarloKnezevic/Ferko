<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<h2><s:text name="Repository.allVersions"/></h2>
<hr/>
<s:if test="data.courseInstance.course.repository!=null">
	<s:iterator value="data.files" status="stat">
	<li>	
		<s:property value="realName"/>  VERZIJA:<s:property value="fileVersion"/>	
		<a href="<s:url action="ViewFileInfo"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param><s:param name="fileID"><s:property value="id"/></s:param></s:url>"><img src="img/icons/information.png" title="<s:text name="Repository.viewFileInfo"/>" alt="<s:text name="Repository.viewFileInfo"/>"/></a>
		<a href="<s:url action="DownloadFile"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param><s:param name="fileID"><s:property value="id"/></s:param></s:url>"><img src="img/icons/arrow_down.png" title="<s:text name="Repository.downloadFile"/>" alt="<s:text name="Repository.downloadFile"/>"/></a>	
	</li>
	</s:iterator>
</s:if>
<hr/>
