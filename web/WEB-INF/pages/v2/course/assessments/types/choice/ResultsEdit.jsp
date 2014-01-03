<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<s:if test="data.messageLogger.hasMessages()">
	<ul class="msgList">
		<s:iterator value="data.messageLogger.messages">
			<li class="msgType_<s:property value="messageType" />"><s:property value="messageText" /></li>
		</s:iterator>
	</ul>
</s:if>

  Napomena: ovu vrstu provjere nije dozvoljeno uređivati od više korisnika istovremeno.
 
  <div>
   <s:iterator value="data.availableLetters" status="stat">
     <s:if test="#stat.first == false"> | </s:if>
     <s:if test="[0].toString()==bean.letter.toString()">
       <s:property/>
     </s:if>
     <s:else>
       <a href="<s:url action="ConfChoiceScoreEdit" method="pickLetter"><s:param name="bean.assessmentID"><s:property value="data.assessment.id"/></s:param><s:param name="bean.letter"><s:property/></s:param></s:url>"><s:property/></a>
     </s:else>
   </s:iterator>
  </div>
  <s:form action="ConfChoiceScoreEdit" method="post" theme="simple">
    <table>
    <tr>
      <th>Student</th>
      <th>Grupa</th>
      <th>Pisao</th>
      <s:iterator value="bean.items[0].answers" status="stat">
        <th><s:property value="%{#stat.index+1}" />.</th>
      </s:iterator>
      <th>Bodove unio</th>
    </tr>
    <s:iterator value="bean.items" status="stat">
    <tr>
      <td>
        <s:hidden name="bean.items[%{#stat.index}].lastName" value="%{lastName}"/>
        <s:hidden name="bean.items[%{#stat.index}].problemsNum" value="%{problemsNum}"/>
        <s:hidden name="bean.items[%{#stat.index}].firstName" value="%{firstName}"/>
        <s:hidden name="bean.items[%{#stat.index}].jmbag" value="%{jmbag}"/>
        <s:hidden name="bean.items[%{#stat.index}].userID" value="%{userID}"/>
        <s:hidden name="bean.items[%{#stat.index}].id" value="%{id}"/>
        <s:hidden name="bean.items[%{#stat.index}].assigner" value="%{assigner}"/>
        <s:property value="lastName"/> <s:property value="firstName"/> (<s:property value="jmbag"/>)
      </td>
      <td>
		<s:textfield name="bean.items[%{#stat.index}].group" value="%{group}" size="1" />
      </td>
      <td>
        <s:checkbox name="bean.items[%{#stat.index}].present" value="%{present}" />
      </td>
      <s:iterator value="zanswers" status="num">
        <td>
          <s:textfield name="bean.items[%{#stat.index}].zanswers[%{#num.index}]" value="%{bean.items[#stat.index].zanswers[#num.index]}" size="1" />
        </td>
      </s:iterator>
      <td>
        <s:property value="assigner"/>
      </td>
    </tr>
    </s:iterator>
    </table>
    <s:hidden name="bean.letter" value="%{bean.letter}" />
    <s:hidden name="bean.assessmentID" value="%{bean.assessmentID}" />
    <s:submit method="save" value="%{getText('forms.update')}"></s:submit>
  </s:form>
