<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<h2><s:text name="Repository.uploadVersion"/></h2>

<s:form action="UploadVersion" method="post" theme="ferko" enctype="multipart/form-data">
<s:hidden name="courseInstanceID"/>
<s:hidden name="previousFileID"/>
<s:textfield name="fileComment" label="%{getText('Repository.comment')}"/>
<s:file name="upload" label="%{getText('forms.file')}"/>
<s:submit method="uploadVersion"/>
</s:form>

