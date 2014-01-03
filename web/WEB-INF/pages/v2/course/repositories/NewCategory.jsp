<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>


<s:if test="categoryID != null">
	<h2><s:text name="Repository.addNewCategory"/></h2>
</s:if>
<s:else>
	<h2><s:text name="Repository.addNewRootCategory"/></h2>
</s:else>


<s:form action="NewCategory" method="post" theme="ferko">
<s:hidden name="courseInstanceID"/>
<s:hidden name="categoryID"/>
<s:textfield name="categoryName" label="%{getText('Repository.categoryName')}"/> 
<s:submit method="newCategory" value="%{getText('forms.add')}"/>
</s:form>

