<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<s:if test="data.messageLogger.hasMessages()">
	<ul class="msgList">
		<s:iterator value="data.messageLogger.messages">
			<li class="msgType_<s:property value="messageType" />"><s:property value="messageText" /></li>
		</s:iterator>
	</ul>
</s:if>

  <div><s:text name="Assessments.stat.on"/> <s:property value="data.assessment.name"/></div>

  <s:form action="AssessmentStat" method="get" theme="ferko">
    <s:hidden name="assessmentID" value="%{data.assessment.id}"></s:hidden>
    <s:select list="data.stat.availableStatistics" listKey="id" listValue="title" name="localID" value="%{data.statBase.id}"  label="%{getText('forms.statdata')}"></s:select>
    <s:submit value="%{getText('Assessments.stat.fetch')}"/>
  </s:form>

  <s:if test="data.statBase==null">
    <s:text name="Assessments.stat.select" />
  </s:if><s:else>
	<s:if test="data.statBase.statisticsBaseType==1">
      <div><s:text name="Assessments.stat.selected" /> <b><s:property value="%{data.stat.availableStatistics.{^ #this.kind.equals(data.statBase.kind)}[0].title}"/></b></div>
      <table>
      <tr>
        <td><s:text name="Assessments.stat.students" /></td><td><s:property value="%{data.statBase.count}"/></td>
      </tr><tr>
        <td><s:text name="Assessments.stat.average" /></td><td><s:property value="%{data.statBase.average}"/></td>
      </tr><tr>
        <td><s:text name="Assessments.stat.median" /></td><td><s:property value="%{data.statBase.median}"/></td>
      </tr>
      </table>

      <div style="margin-bottom: 20px; text-align: center;">
        <form onsubmit="return false;">
         <s:text name="Assessments.stat.bins" /> <input type="text" id="podjele" value="10"><input type="button" value="Ažuriraj" onclick="prilagodi(); return false;">
        </form>
      </div>

      <div style="text-align: center;">
      <img id="hslika" src="<s:url action="AssessmentStat" method="scoreHistogram"><s:param name="assessmentID"><s:property value="data.assessment.id"/></s:param><s:param name="localID"><s:property value="data.statBase.id"/></s:param></s:url>">
      </div>

 <script type="text/javascript">
   var u = '<s:url action="AssessmentStat" method="scoreHistogram"><s:param name="assessmentID"><s:property value="data.assessment.id"/></s:param><s:param name="localID"><s:property value="data.statBase.id"/></s:param></s:url>';
   function prilagodi() {
	 var numericExpression = /^[0-9]+$/;
     var slika = document.getElementById('hslika');
     var vrijednost = document.getElementById('podjele');
     if(!(vrijednost.value+"").match(numericExpression)) {
         alert("<s:text name="Assessments.stat.err.number" />");
         return false;
     }
     var br = parseInt(vrijednost.value);
     if(br<3) {
         alert("<s:text name="Assessments.stat.err.number3" />");
         return false;
     }
     var u2 = u.replace(/&amp;/g, '&');
     slika.src = u2+"&bins="+vrijednost.value;
     return false;
   }
 </script>

	</s:if>
	<s:if test="data.statBase.statisticsBaseType==2">
      <div><s:text name="Assessments.stat.on"/> <b><s:property value="%{data.stat.availableStatistics.{^ #this.kind.equals(data.statBase.kind)}[0].title}"/></b></div>
      <table>
      <tr>
        <th><s:text name="Assessments.stat.problemTag"/></th>
        <th><s:text name="Assessments.stat.students"/></th>
        <th><s:text name="Assessments.stat.correct"/></th>
        <th><s:text name="Assessments.stat.wrong"/></th>
        <th><s:text name="Assessments.stat.unanswered"/></th>
        <th><s:text name="Assessments.stat.discrIndex"/></th>
        <th><s:text name="Assessments.stat.absWeight"/></th>
        <th><s:text name="Assessments.stat.relWeight"/></th>
      </tr>
      <s:iterator value="data.statBase.rows">
      <s:if test="coarse"><tr style="background-color: #DDDDDD;"></s:if><s:else><tr></s:else>
        <td><s:property value="%{key}"/></td>
        <td><s:property value="%{totalStudents}"/></td>
        <td><s:property value="%{correctStudents}"/></td>
        <td><s:property value="%{wrongStudents}"/></td>
        <td><s:property value="%{unansweredStudents}"/></td>
        <td><s:property value="%{discriminationIndexAsString}"/></td>
        <td><s:property value="%{weightAbsoluteAsString}"/></td>
        <td><s:property value="%{weightRelativeAsString}"/></td>
      </tr>
      </s:iterator>
      </table>
	</s:if>


  </s:else>
