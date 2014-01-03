<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div id="body" class="container">

<div class="content">
	<div class="maincontent">
		<div class="innerpadding">
			<s:if test="data.messageLogger.hasMessages()">
				<ul class="msgList">
					<s:iterator value="data.messageLogger.messages">
						<li class="msgType_<s:property value="messageType" />"><s:property value="messageText" /></li>
					</s:iterator>
				</ul>
			</s:if>
			
			<h1><s:property value="data.courseInstance.course.name"/> (<s:property value="data.courseInstance.course.isvuCode"/>): <s:property value="data.assessment.name"/></h1>
			<s:if test="data.assessment!=null && !data.assessment.event!=null">
			<b>Početak: </b><s:property value="data.formatDateTime(data.assessment.event.start)"/><br>
			<b>Trajanje: </b><s:property value="data.assessment.event.duration"/> min<br>
			<s:if test="data.userSpecificEvent!=null">
			<b>Dvorana: </b><s:property value="data.userSpecificEvent.room.name"/><br>
			</s:if>
			<br><br>
			</s:if><s:else>Nema podataka o početku i trajanju.</s:else>

			<s:if test="data.files!=null && data.files.size() != 0">
			  <h2><s:property value="%{getText('Assessments.filesAttachedToAssessment')}"/>:</h2>
			    <ul>
			      <s:iterator value="data.files">
			      <s:if test="user==null">
			        <li>
			          <a href="<s:url action="AssessmentFileDownload"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param><s:param name="assessmentID"><s:property value="data.assessment.id"/></s:param><s:param name="assessmentFileID"><s:property value="id"/></s:param></s:url>"><s:property value="descriptor"/></a>
					  <s:if test="originalFileName==null">
				        <s:property value="id"/> (<s:property value="mimeType"/>)
			          </s:if>
			          <s:else>
			            <s:property value="originalFileName"/> (<s:property value="mimeType"/>)
			          </s:else>
			          <s:if test="description!=null">
			            <br/><s:property value="description"/>
			          </s:if>
			        </li>
			      </s:if>
			      </s:iterator>
			    </ul>
			</s:if>

		</div>
	</div>
</div>

</div>

