<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div id="body" class="container">
<s:if test="categoryID != null">
	<h2><s:text name="Repository.addNewCategory"/></h2>
</s:if>
<s:else>
	<h2><s:text name="Repository.addNewRootCategory"/></h2>
</s:else>


<s:form action="NewCategory">
<s:hidden name="courseInstanceID"/>
<s:hidden name="categoryID"/>
<s:text name="Repository.categoryName"/><s:textfield name="categoryName"/> 
<s:submit method="newCategory"/>
</s:form>

<a href="<s:url action="Repository"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Repository.return"/></a></div>
