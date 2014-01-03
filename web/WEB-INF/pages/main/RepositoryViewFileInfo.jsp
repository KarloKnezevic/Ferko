<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<h2><s:text name="Repository.viewFileInfo"/></h2>
<hr/>
Ime:<s:property value="fileBean.realName"/><br/>
Komentar:<s:property value="fileBean.comment"/><br/>
Veličina(MB): <s:property value="data.df(fileBean.size,2)"/><br/>
Uploadao:<s:property value="fileBean.owner"/><br/>
Kategorija:<s:property value="fileBean.category"/><br/>
Verzija: <s:property value="fileBean.fileVersion"/><br/>
MimeType:<s:property value="fileBean.mimeType"/><br/>
Datum uploada:<s:property value="fileBean.uploadDate"/>
<s:if test="data.isStaffMember()||data.isAdmin()">
	<br/>
	Status:<s:property value="fileBean.status"/>
</s:if>

<hr/>

<a href="<s:url action="Repository"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Repository.return"/></a>


