<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ taglib prefix="jcms" uri="/jcms-custom-tags" %>
<%@page import="hr.fer.zemris.jcms.model.extra.RepositoryFileStatus"%>

<h2><s:text name="Repository.title"/></h2>
<s:if test="data.isStaffMember()||data.isAdmin()">
	<a href="<s:url action="NewCategoryPrep"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Repository.newRootCategory"/></a>
</s:if>
<hr/>
Broj kategorija je: <s:property value="data.categories.size"/><br/>
Broj datoteka je: <s:property value="data.files.size"/><br/>
<hr/>

<s:if test="data.courseInstance.course.repository!=null">
	<ul>
  	<jcms:hierarchyIterator status="stat" value="data.courseInstance.course.repository.rootCategories" itemsFirst="true" itemGetter="files" childGetter="subCategories">
    <s:if test="kind==0">
    	<li>
		
		<s:if test="value.parentCategory != null">
			<font color="blue" size="3"> <s:property value="value.categoryName"/> </font> 
		</s:if>
		<s:else>
			<font color="blue" size="4"> <s:property value="value.categoryName"/> </font> 
		</s:else>
		
		
     	<s:if test="data.isStaffMember()||data.isAdmin()">
			<a href="<s:url action="NewCategoryPrep"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param><s:param name="categoryID"><s:property value="value.id"/></s:param></s:url>"><img src="img/icons/folder_add.png" title="<s:text name="Repository.newSubCategory"/>" alt="<s:text name="Repository.newSubCategory"/>"/></a>
      		<a href="<s:url action="DeleteCategory"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param><s:param name="categoryID"><s:property value="value.id"/></s:param></s:url>"><img src="img/icons/folder_delete.png" title="<s:text name="Repository.deleteCategory"/>" alt="<s:text name="Repository.deleteCategory"/>"/></a>	 
			<a href="<s:url action="UploadFilePrep"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param><s:param name="categoryID"><s:property value="value.id"/></s:param></s:url>"><img src="img/icons/new_file.png" title="<s:text name="Repository.newFile"/>" alt="<s:text name="Repository.newFile"/>"/></a>
		</s:if>
		<ul>
    </s:if>
    <s:elseif test="kind==1">
    	</ul>
		</li>
    </s:elseif>
    <s:elseif test="kind==2" >
		<s:if test="value.nextVersion==null">
			<li>
			
			<s:if test="data.isStaffMember()||data.isAdmin()">
		
				<s:if test="(value.status eq @hr.fer.zemris.jcms.model.extra.RepositoryFileStatus@HIDDEN)">
					<font color="black" size="3">Datoteka:</font> 
					<font color="red" size="3">SKRIVENO</font> 
					<font color="lightgreen" size="4"><s:property value="value.realName"/></font>
				</s:if>
				<s:else>
					<font color="black" size="3">Datoteka:</font> 
					<font color="green" size="4"><s:property value="value.realName"/></font>
				</s:else>
			
				<a href="<s:url action="DeleteFile"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param><s:param name="previousFileID"><s:property value="value.id"/></s:param></s:url>"><img src="img/icons/famcross.png" title="<s:text name="Repository.deleteFile"/>" alt="<s:text name="Repository.deleteFile"/>"/></a>
				<a href="<s:url action="DeleteFileAndVersions"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param><s:param name="previousFileID"><s:property value="value.id"/></s:param></s:url>"><img src="img/icons/bin_closed.png" title="<s:text name="Repository.deleteFileAndVersions"/>" alt="<s:text name="Repository.deleteFileAndVersions"/>"/></a>
				
				<s:if test="value.status eq @hr.fer.zemris.jcms.model.extra.RepositoryFileStatus@HIDDEN">
					<a href="<s:url action="ShowFile"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param><s:param name="fileID"><s:property value="value.id"/></s:param></s:url>"><img src="img/icons/eye.png" title="<s:text name="Repository.showFile"/>" alt="<s:text name="Repository.showFile"/>"/></a>
				</s:if>
				<s:else>
					<a href="<s:url action="HideFile"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param><s:param name="fileID"><s:property value="value.id"/></s:param></s:url>"><img src="img/icons/eye_hidden.png" title="<s:text name="Repository.hideFile"/>" alt="<s:text name="Repository.hideFile"/>"/></a>
				</s:else>
			
				<a href="<s:url action="UploadVersionPrep"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param><s:param name="previousFileID"><s:property value="value.id"/></s:param></s:url>"><img src="img/icons/new_version.png" title="<s:text name="Repository.newVersion"/>" alt="<s:text name="Repository.newVersion"/>"/></a>
				<a href="<s:url action="ViewAllVersions"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param><s:param name="fileID"><s:property value="value.id"/></s:param></s:url>"><img src="img/icons/show_all_versions.png" title="<s:text name="Repository.viewAllVersions"/>" alt="<s:text name="Repository.viewAllVersions"/>"/></a>
				<a href="<s:url action="DownloadFile"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param><s:param name="fileID"><s:property value="value.id"/></s:param></s:url>"><img src="img/icons/arrow_down.png" title="<s:text name="Repository.downloadFile"/>" alt="<s:text name="Repository.downloadFile"/>"/></a>
				<a href="<s:url action="ViewFileInfo"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param><s:param name="fileID"><s:property value="value.id"/></s:param></s:url>"><img src="img/icons/information.png" title="<s:text name="Repository.viewFileInfo"/>" alt="<s:text name="Repository.viewFileInfo"/>"/></a>
				<a href="<s:url action="ShowFilePage"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param><s:param name="fileID"><s:property value="value.id"/></s:param><s:param name="pageNumber">0</s:param></s:url>"><s:text name="Repository.showPage"/></a>

			</s:if> 	
			<s:else>		
				<s:if test="!(value.status eq @hr.fer.zemris.jcms.model.extra.RepositoryFileStatus@HIDDEN)">
					<font color="black" size="3">Datoteka:</font> 
					<font color="green" size="4"><s:property value="value.realName"/></font>
					
				<a href="<s:url action="ViewAllVersions"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param><s:param name="fileID"><s:property value="value.id"/></s:param></s:url>"><img src="img/icons/show_all_versions.png" title="<s:text name="Repository.viewAllVersions"/>" alt="<s:text name="Repository.viewAllVersions"/>"/></a>
				<a href="<s:url action="DownloadFile"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param><s:param name="fileID"><s:property value="value.id"/></s:param></s:url>"><img src="img/icons/arrow_down.png" title="<s:text name="Repository.downloadFile"/>" alt="<s:text name="Repository.downloadFile"/>"/></a>
				<a href="<s:url action="ViewFileInfo"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param><s:param name="fileID"><s:property value="value.id"/></s:param></s:url>"><img src="img/icons/information.png" title="<s:text name="Repository.viewFileInfo"/>" alt="<s:text name="Repository.viewFileInfo"/>"/></a>
				</s:if>
			</s:else>
			
			</li>
		</s:if>	
    </s:elseif>
  </jcms:hierarchyIterator>
  </ul>
</s:if>

