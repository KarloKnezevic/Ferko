<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<s:if test="data.messageLogger.hasMessages()">
	<ul class="msgList">
		<s:iterator value="data.messageLogger.messages">
			<li class="msgType_<s:property value="messageType" />"><s:property value="messageText" /></li>
		</s:iterator>
	</ul>
</s:if>

	<h2>Prijave</h2>
	<p>Ovdje se možete prijaviti za nadoknade labosa, bliceva, ispita i sl.</p>

<s:if test="data.definitions == null || data.definitions.isEmpty()">
  <p class="emptyMsg">Nema definiranih prijava za ovaj kolegij.</p>
</s:if>
<s:else>
  <dl>
  <s:iterator value="data.definitions">
  	<dt><s:property value="name"/> (<s:property value="shortName"/>)</dt>
	<dd>Razdoblje prijava: <s:property value="data.formatDateTime(openFrom)"/> - <s:property value="data.formatDateTime(openUntil)"/></dd>
    <s:if test="data.renderCourseAdministration">
	    <dd><a href="<s:url action="ApplicationAdminEdit" method="editDefinition">
	    	<s:param name="bean.courseInstanceID"><s:property value="data.courseInstance.id"/></s:param>
	    	<s:param name="bean.id"><s:property value="id"/></s:param>
	    </s:url>"><s:text name="Navigation.applicationEdit"/></a> | 
	    <a href="<s:url action="ApplicationListStudents">
	    	<s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param>
	    	<s:param name="definitionID"><s:property value="id"/></s:param>
	    </s:url>"><s:text name="Navigation.applicationListStudents"/></a></dd>
    </s:if>
    <s:else>
    	<s:if test="data.filledApplications.containsKey(id)"> 
        	<dd>Prijava je predana. Status: <s:property value="data.filledApplications.get(id).status"/>
      		<a href="<s:url action="ApplicationStudentView"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param><s:param name="applicationID"><s:property value="data.filledApplications.get(id).id"/></s:param></s:url>"><s:text name="Navigation.details"/></a></dd>
      	</s:if>
   		<s:else>
      		<dd><a href="<s:url action="ApplicationStudentSubmit" method="newApplication"><s:param name="data.courseInstanceID"><s:property value="data.courseInstance.id"/></s:param><s:param name="data.applicationID"><s:property value="id"/></s:param></s:url>"><s:text name="Navigation.applicationSubmit"/></a></dd>
	  	</s:else> 
	</s:else> 
  </s:iterator>
  </dl>
</s:else>

<s:if test="data.renderCourseAdministration">
<div>
  <a href="<s:url action="ApplicationAdminEdit" method="newDefinition"><s:param name="bean.courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.applicationAdd"/></a>
| <a href="<s:url action="ApplicationAdminTable"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.applicationTable"/></a>
</div>
</s:if>
