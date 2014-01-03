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
	<li>
	<a href="<s:url action="CCIManager" method="viewItem"><s:param name="id" value="data.courseComponentItem.id"/></s:url>">
	<s:property value="data.courseComponentItem.position"/>. <s:property value="data.courseComponent.descriptor.positionalName"/> - <s:property value="data.courseComponentItem.name"/>
	</a>
	</li>
</ul>

<h2><s:property value="data.courseComponentItem.position"/>. <s:property value="data.courseComponent.descriptor.positionalName"/> - <s:property value="data.courseComponentItem.name"/></h2>

<s:if test="data.userTask.locked"> 
	<ul class="msgList">
		<li>Zadatak je zaključan (<s:property value="data.userTask.lockingDate"/>)</li>
	</ul>
</s:if>

<h2>Zadatak: <s:property value="data.userTask.title"/></h2>

<dl class="pairvalue">
	<dt>Rok za predaju:</dt>
	<dd>
	<s:if test="data.userTask.deadline==null"> - </s:if>
	<s:else><s:property value="data.userTask.deadline"/></s:else>
	</dd>
		<dt>Produženo:</dt>
	<dd>
		<s:if test="data.userTask.extensionDate!=null"> 
		<s:property value="data.userTask.extensionDate"/>
		</s:if>
		<s:else>
		-
		</s:else>
	</dd>
	<dt>Pregledano:</dt>
	<dd>
		<s:if test="data.userTask.reviewed"> 
		da (<s:property value="data.userTask.reviewedBy"/>)
		</s:if>
		<s:else>
		ne
		</s:else>
	</dd>
	<s:if test="data.userTask.reviewed"> 
	<dt>Komentar:</dt>
	<dd>
	<s:property value="data.userTask.comment" />
	</dd>
	</s:if>
	<dt>Prolaz:</dt>
	<dd>
		<s:if test="data.userTask.reviewed"> 
		<s:property value="data.userTask.passed" /> (<s:property value="data.userTask.score"/>)<br>
		</s:if>
		<s:else>
		n/a
		</s:else>
	</dd>
</dl>

<h3>Opis zadatka</h3>
<p><s:property value="data.userTask.description"/></p>
<h3>Poslane datoteke</h3>
<s:if test="data.userTask.taskUploadList!=null && data.userTask.taskUploadList.size()>0">
<ul>
<s:iterator value="data.userTask.taskUploadList">
  <li>
    <a href="<s:url action="CCTManager" method="viewFile"><s:param name="id" value="id"/></s:url>"><s:property value="fileName" /><s:if test="tag!=null && !tag.equals('')">(<s:property value="tag"/>)</s:if></a>
    (<s:property value="uploadDate" />)

    <s:if test="!data.isLocked()">
      <a href="<s:url action="CCTManager" method="removeFile"><s:param name="id" value="id"/>
			</s:url>" class="action delete">
		<span><s:text name="Navigation.erase"/></span>
	</a>
    </s:if>

  </li>
</s:iterator>
</ul>
</s:if>
<s:else>
<p class="emptyMsg">Nema datoteka</p>
</s:else>
<s:if test="!data.isLocked()">
<p><a href="<s:url action="CCTManager" method="lockAssignment"><s:param name="id" value="data.userTask.assignmentID"/></s:url>" class="action lock">
	<span><s:text name="Navigation.lockAssignment"/></span>
</a></p>

<h3>Upload datoteke</h3>
	<s:form action="CCTManager" method="post" enctype="multipart/form-data" theme="ferko">
	  <s:file name="taskFileBean.upload" label="%{getText('forms.file')}" />
	  <s:if test="data.userTask.fileTags.size()>0">
	    <s:select list="data.userTask.fileTags" name="taskFileBean.fileTag" label="%{getText('forms.fileTags')}" />
	  </s:if>
	  <s:hidden name="id" value="%{data.userTask.assignmentID}"/>
	  <s:submit method="uploadFile"/>
	</s:form>
</s:if>

<h3>Ograničenja na datoteke</h3>
	<dl class="pairvalue">
	  <dt>
	    Maksimalna veličina datoteke: 
	</dt>
	  <dd><s:property value="data.userTask.maxFileSize"/> MB
	  </dd>
	  <dt>
	    Broj potrebnih datoteka:
	</dt>
	  <dd><s:if test="!data.userTask.filesRequiredCount.equals('-1')">
	      <s:property value="data.userTask.filesRequiredCount" />
	    </s:if>
        <s:else>
          Neograniceno (Maksimalno <s:property value="data.userTask.maxFilesCount" />)
        </s:else>
      	  </dd>
    </dl>
