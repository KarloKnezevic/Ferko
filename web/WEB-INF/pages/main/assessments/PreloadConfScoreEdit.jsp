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
<div><s:property value="data.assessment.name"/></div>

<s:if test="data==null">
  <div>Nisam dobio niti data objekt.</div>
</s:if>
<s:else>

  <div>
   <s:iterator value="data.availableLetters" status="stat">
     <s:if test="#stat.first == false"> | </s:if>
     <s:if test="[0].toString()==bean.letter.toString()">
       <s:property/>
     </s:if>
     <s:else>
       <a href="<s:url action="ConfPreloadScoreEdit" method="pickLetter"><s:param name="bean.courseInstanceID"><s:property value="data.courseInstance.id"/></s:param><s:param name="bean.assessmentID"><s:property value="data.assessment.id"/></s:param><s:param name="bean.letter"><s:property/></s:param></s:url>"><s:property/></a>
     </s:else>
   </s:iterator>
  </div>

  <s:form action="ConfPreloadScoreEdit" method="post" theme="simple">
    <table>
    <tr>
      <th>Student</th>
      <th>Bodovi</th>
      <th>Bodove unio</th>
    </tr>
    <s:iterator value="bean.items" status="stat">
    <tr>
      <td width="40%">
        <s:hidden name="bean.items[%{#stat.index}].lastName" value="%{lastName}"/>
        <s:hidden name="bean.items[%{#stat.index}].firstName" value="%{firstName}"/>
        <s:hidden name="bean.items[%{#stat.index}].jmbag" value="%{jmbag}"/>
        <s:hidden name="bean.items[%{#stat.index}].userID" value="%{userID}"/>
        <s:hidden name="bean.items[%{#stat.index}].id" value="%{id}"/>
        <s:hidden name="bean.items[%{#stat.index}].assigner" value="%{assigner}"/>
        <s:hidden name="bean.items[%{#stat.index}].oScore" value="%{bean.items[#stat.index].oScore}"/>
        <s:hidden name="bean.items[%{#stat.index}].version" value="%{bean.items[#stat.index].version}"/>
        <s:property value="lastName"/> <s:property value="firstName"/> (<s:property value="jmbag"/>)
      </td>
      <td width="20%">
        <s:textfield name="bean.items[%{#stat.index}].score" value="%{score}"/>
      </td>
      <td width="40%">
        <s:property value="assigner"/>
      </td>
    </tr>
    </s:iterator>
    </table>
    <s:hidden name="bean.letter"></s:hidden>
    <s:hidden name="bean.courseInstanceID"></s:hidden>
    <s:hidden name="bean.assessmentID"></s:hidden>
    <s:submit method="save" value="%{getText('forms.update')}"></s:submit>
  </s:form>

</s:else>

<div>
  <a href="<s:url action="Main"/>"><s:text name="Navigation.main"/></a>
 | <a href="<s:url action="ShowCourse"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.courseHome"/></a>
 | <a href="<s:url action="AdminAssessmentList"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.assessments"/></a>
<s:if test="data.assessment != null">
 | <a href="<s:url action="AdminAssessmentView"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param><s:param name="assessmentID"><s:property value="data.assessment.id"/></s:param></s:url>"><s:text name="Navigation.backToDetails"/></a>
</s:if>
</div>

</div>
