<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div id="body" class="container">
<h2><s:text name="Repository.uploadVersion"/></h2>

<s:form action="UploadVersion" method="POST" enctype="multipart/form-data">
<s:hidden name="courseInstanceID"/>
<s:hidden name="previousFileID"/>
<s:text name="Repository.comment"/>:<s:textfield name="fileComment"/>
<s:file name="upload"/>
<s:submit method="uploadVersion"/>
</s:form>

<a href="<s:url action="Repository"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Repository.return"/></a>
</div>
