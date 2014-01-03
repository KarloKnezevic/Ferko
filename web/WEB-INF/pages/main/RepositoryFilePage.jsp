<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<img src="<s:url value="/img/testRepositoryFilePage/image1.png"/>" width=640 height=480/>

<s:iterator value="commentsList">
  <s:property /><br>
</s:iterator>

<br>


<a href="<s:url action="AddFilePageCommentPrep"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param><s:param name="fileID"><s:property value="data.fileID"/></s:param><s:param name="pageNumber"><s:property value="pageNumber"/></s:param></s:url>"><s:text name="Repository.addFilePageComment"/></a>	 



<a href="<s:url action="Repository"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param><s:param name="fileID"><s:property value="id"/></s:param></s:url>"><s:text name="Repository.return"/></a>


