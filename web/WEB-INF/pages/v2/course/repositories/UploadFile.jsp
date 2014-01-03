<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<h2><s:text name="Repository.uploadFile"/></h2>

<s:form action="UploadFile" method="post" theme="ferko" enctype="multipart/form-data">
<s:hidden name="courseInstanceID"/>
<s:hidden name="categoryID"/>
<s:textfield name="fileComment" label="%{getText('Repository.comment')}"/>
<s:file name="upload" label="%{getText('forms.file')}"/>
<s:submit method="uploadFile" value="%{getText('forms.uploadFile')}"/>
</s:form>
