<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div id="body" class="container">

<s:if test="data.messageLogger.hasMessages()">
  <ul>
  <s:iterator value="data.messageLogger.messages">
      <li>[<s:property value="messageType"/>] <s:property value="messageText"/></li>
  </s:iterator>
  </ul>
</s:if>

<div><s:property value="data.courseInstance.course.name"/> (<s:property value="data.courseInstance.course.isvuCode"/>)</div>

<s:if test="data==null">
  <div>Nisam dobio niti data objekt.</div>
</s:if>
<s:else>
	<s:if test="data.assessments == null || data.assessments.isEmpty()">
	  <div>Nema žalbi.</div>
	</s:if>
	<s:else>
	  <table>
	    <thead>
	    <tr>
	      <th>Provjera</th>
	      <th>JMBAG</th>
	      <th>Ime</th>
	      <th>Prezime</th>
	      <th>Datum</th>
	      <th>Status</th>
	      <th>Link</th>
	    </tr>
	    </thead>
	    <tbody>
	    <s:sort source="bean.appeals" comparator="data.statusComp"> 
		    <s:iterator status="cust_stat">
		    <tr class="<s:if test="#cust_stat.odd == true">oddrow</s:if><s:else>evenrow</s:else>">
		      <td><s:property value="assessment.name"/></td>
		      <td><s:property value="creatorUser.jmbag"/></td>
		      <td><s:property value="creatorUser.firstName"/></td>
		      <td><s:property value="creatorUser.lastName"/></td>
		      <td><s:date name="creationDate" format="dd.MM.yyyy" /></td>
		      <td><s:property value="status"/></td>
		      <td><a href="<s:url action="AdminAssessmentAppeal"><s:param name="appealID"><s:property value="id"/></s:param><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param><s:param name="assessmentID"><s:property value="assessment.id"/></s:param></s:url>">Otvori</a></td>
		    </tr>
		    </s:iterator>
	    </s:sort>
	    </tbody>
	  </table>
	</s:else>
</s:else>

<div>
  <a href="<s:url action="Main"/>"><s:text name="Navigation.main"/></a>
 | <a href="<s:url action="ShowCourse"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.courseHome"/></a>
 | <a href="<s:url action="AdminAssessmentList"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.assessments"/></a>
<s:if test="data.assessment != null">
 | <a href="<s:url action="AdminAssessmentView"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.backToDetails"/></a>
</s:if>
</div>

</div>
