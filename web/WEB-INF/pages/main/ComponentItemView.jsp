<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<s:if test="data.messageLogger.hasMessages()">
	<ul class="msgList">
		<s:iterator value="data.messageLogger.messages">
			<li class="msgType_<s:property value="messageType" />"><s:property value="messageText" /></li>
		</s:iterator>
	</ul>
</s:if>

<ul>
  <li>
    <a href="<s:url action="CCManager"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>">
    <s:property value="data.courseComponent.descriptor.name"/>
    </a>
  </li>
</ul>
<h2><s:property value="data.courseComponentItem.position"/>. <s:property value="data.courseComponent.descriptor.positionalName"/> - <s:property value="data.courseComponentItem.name"/></h2>
<s:if test="data.termAssistants!=null && !data.termAssistants.empty">
  <h3>Vaše grupe</h3>
  <ul>
<s:iterator value="data.termAssistants">
  <li>Grupa: <b><s:property value="group.name"/></b><br/>
<s:if test="!events.empty">
  Termini: 
  <s:iterator value="events"><s:property value="data.formatDateTime(start)"/><s:if test="room!=null"> (<s:property value="room.name"/>)</s:if>, </s:iterator><br/>
</s:if>
<s:if test="!assistants.empty">
  Asistenti: 
  <s:iterator value="assistants"><s:property value="lastName"/>, <s:property value="firstName"/>; </s:iterator><br/>
</s:if>
  </li>
</s:iterator>
  </ul>
</s:if>

<s:if test="data.isAdmin()">
 <h3>Komponente ocjenjivanja</h3>
  <s:if test="data.defList!=null && data.defList.size()>0">
  <ul>
  <s:iterator value="data.defList">
  	<li>
    <s:property value="name"/> <a href="<s:url action="CCIManager" method="removeItemDef"><s:param name="id" value="id"/></s:url>" class="action delete"><s:text name="Navigation.componentDefRemove"/></a>
    <a href="<s:url action="CCIManager" method="changeDefPosition"><s:param name="id" value="id"/><s:param name="inputData">up</s:param></s:url>">up</a>
    <a href="<s:url action="CCIManager" method="changeDefPosition"><s:param name="id" value="id"/><s:param name="inputData">down</s:param></s:url>">down</a>
    </li>
  </s:iterator>
  </ul>
  </s:if>
  <s:else>
	<p class="emptyMsg">Nema komponenti ocjenjivanja</p>
  </s:else>
<p>
<a href="<s:url action="CCIManager" method="newItemDef"><s:param name="id" value="id"/></s:url>" class="action edit">
	<span><s:text name="Navigation.componentDefEdit"/></span>
</a> 
<a href="<s:url action="CCIManager" method="viewItemScores"><s:param name="id" value="id"/></s:url>" class="action">
	<span><s:text name="Navigation.viewComponentItemScores"/></span>
</a> 
<a href="<s:url action="CCIManager" method="editGroupOwners"><s:param name="id" value="data.courseComponentItem.id"/></s:url>" class="action people">
	<span><s:text name="Navigation.editGroupOwners"/></span>
</a> 
</p>
</s:if>

<s:if test="data.isStaffMember()">
	<h3>Moji termini</h3>
  <s:if test="data.groupList!=null && data.groupList.size()>0">
  <ul>
  <s:iterator value="data.groupList">
    <li>
    <a href="<s:url action="CCIManager" method="viewGroupScores"><s:param name="groupID" value="id"/><s:param name="id" value="data.courseComponentItem.id"/></s:url>"><s:property value="name"/></a>
    </li>
  </s:iterator>
  </ul>
  </s:if>
  <s:else>
	<p class="emptyMsg">Nema termina</p>
  </s:else>
</s:if>

<h3>Zadaci</h3>
<s:if test="data.taskList!=null && data.taskList.size()>0">
	<s:if test="data.isAdmin()">
		<a href="<s:url action="CCTManager" method="showMatrix"><s:param name="id" value="data.courseComponentItem.id"/></s:url>" class="action people">
			<span><s:text name="Navigation.matrixView"/></span>
		</a><br/>
	</s:if>
  <ul>
  <s:iterator value="data.taskList">
    <li>
    <s:if test="data.isStaffMember() || data.isAdmin()">
      <a href="<s:url action="CCTManager" method="viewTaskInfo"><s:param name="id" value="id"/></s:url>">
      	<s:property value="title"/>
      </a>
      <s:if test="data.groupList!=null && data.groupList.size()>0">
        <s:form action="CCTManager" theme="simple">
          <s:select list="data.groupList" listKey="id" listValue="name" name="filterGroupID" theme="simple"></s:select>
          <s:hidden name="id" value="%{id}" theme="simple"></s:hidden>
          <s:submit method="viewTaskUsers" value="Pregledaj rješenja za odabranu grupu"></s:submit>
        </s:form>
      
      </s:if>
    </s:if>
    <s:else>
    <a href="<s:url action="CCTManager" method="viewUserTask"><s:param name="id" value="id"/></s:url>">
		<s:property value="title"/>
	</a>
	</s:else>
    </li>
  </s:iterator>
  </ul>
</s:if>
<s:else>
<p class="emptyMsg">Nema zadataka</p>
</s:else>
<s:if test="data.isAdmin()">
<p>
<a href="<s:url action="CCTManager" method="newTask"><s:param name="courseComponentItemID" value="id"/></s:url>" class="action add">
	<span><s:text name="Navigation.addComponentTask"/></span>
</a>
</p>
</s:if>

<h3>Provjere</h3>
<s:if test="data.itemAssessmentsList!=null && data.itemAssessmentsList.size()>0">
	<s:if test="data.isAdmin()">
		<a href="<s:url action="CCIAManager" method="showMatrix"><s:param name="id" value="data.courseComponentItem.id"/></s:url>" class="action people">
			<span><s:text name="Navigation.matrixView"/></span>
		</a><br/>
	</s:if>
  <ul>
  <s:iterator value="data.itemAssessmentsList">
	<li>
	<s:property value="assessmentIdentifier"/>
	<s:if test="data.isAdmin()">
		<a href="<s:url action="CCIAManager" method="editItemAssessment"><s:param name="id" value="id"/></s:url>" class="action edit">
			<span><s:text name="Navigation.edit"/></span>
		</a>
		<a href="<s:url action="CCIAManager" method="autoAssign"><s:param name="id" value="id"/></s:url>" class="action people">
			<span><s:text name="Navigation.autoAssign"/></span>
		</a>
	</s:if>
	</li>
  </s:iterator>
  </ul>
  <!-- Ovo je drugi pogled na istu stvar, preko tablica... -->
  <table border="1" width="100%"> 
    <tr><td>Id</td><td>Title</td><td>Started</td><td>Finished</td><td>Status</td><td>Score</td></tr>
    <s:iterator value="data.itemAssessmentsList">
      <s:if test="testDataBean.valid">
	      <s:iterator value="testDataBean.testInstanceData">
	      <tr>
	        <td><a href="<s:property value="url"/>"><s:property value="id"/></a></td>
	        <td><s:property value="title"/></td>
	        <td><s:property value="startedAt"/></td>
	        <td><s:property value="finishedAt"/></td>
	        <td><s:property value="testStatus"/></td>
	        <td><s:property value="testScore"/></td>
	      </tr> 
	      </s:iterator>
	      <tr>
	        <td colspan="5"><b>Overall</b><br/><s:property value="testDataBean.overallStatus"/></td><td>&nbsp;<br/><s:property value="testDataBean.testScore"/></td>
	      </tr>
      </s:if>
      <s:else>
	      <tr>
	        <td colspan="6"><s:property value="assessmentIdentifier"/><br/>Razlog: <s:property value="testDataBean.invalidReason"/></td>
	      </tr>
      </s:else>
     </s:iterator>
  </table>
</s:if>
<s:else>
<p class="emptyMsg">Nema provjera</p>
</s:else>
<s:if test="data.isAdmin()">
<p>
<a href="<s:url action="CCIAManager" method="newItemAssessment"><s:param name="courseComponentItemID" value="id"/></s:url>" class="action add">
	<span><s:text name="Navigation.addComponentItemAssessment"/></span>
</a>
</p>
</s:if>

<h3>Datoteke</h3>
<s:if test="data.itemFiles!=null && data.itemFiles.size()>0">
<ul>
<s:iterator value="data.itemFiles">
  <li>
	<a href="<s:url action="CCIManager" method="viewFile"><s:param name="id" value="id"/></s:url>"><s:property value="fileName" /></a>
	<s:if test="data.isAdmin()">
	<a href="<s:url action="CCIManager" method="removeFile"><s:param name="id" value="id"/></s:url>" class="action delete">
		<span><s:text name="Navigation.removeItemFile"/></span>
	</a>
    </s:if>
  </li>
</s:iterator>
</ul>
</s:if>
<s:else>
<p class="emptyMsg">Nema datoteka</p>
</s:else>
<s:if test="data.isAdmin()">
	<s:form action="CCIManager" method="post" enctype="multipart/form-data" theme="ferko" label="Dodaj datoteku">
	  <li><s:file name="uploadBean.upload" label="%{getText('forms.file')}" />
	  <s:submit method="uploadFile"/>
	  <s:hidden name="id" value="%{data.courseComponentItem.id}"/>
	</s:form>
</s:if>

<s:if test="data.isAdmin()">
<h3>Ručno stvaranje grupa</h3>
<p>Na ovom mjestu možete sami kreirati grupe za ovu komponentu. Međutim, ovo može biti potencijalno opasno. Pročitaj više: <a target="_jcms_help" href="<s:url action="Help"><s:param name="helpKey">000009</s:param></s:url>"><s:text name="Navigation.help"/></a></p>
	<s:form action="CCIManager" method="post" theme="ferko" label="Ručno definiranje grupa">
	  <s:textarea rows="5" cols="80" name="groupText" label="%{getText('forms.groupsToCreate')}"></s:textarea>
	  <s:submit method="createComponentItemGroups"/>
	  <s:hidden name="id" value="%{data.courseComponentItem.id}"/>
	</s:form>
</s:if>
