<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<s:if test="data.messageLogger.hasMessages()">
  <ul>
  <s:iterator value="data.messageLogger.messages">
      <li>[<s:property value="messageType"/>]<s:property value="messageText"/></li>
  </s:iterator>
  </ul>
</s:if>

<s:if test="data != null && data.courseInstance != null">

  <div><s:property value="data.courseInstance.course.name"/> (<s:property value="data.courseInstance.course.isvuCode"/>)</div>

  <div>Statistički podaci o provjeri znanja: <s:property value="data.assessment.name"/></div>

  <s:form action="AssessmentStat" method="get" theme="ferko">
    <s:hidden name="courseInstanceID" value="%{data.courseInstance.id}"></s:hidden>
    <s:hidden name="assessmentID" value="%{data.assessment.id}"></s:hidden>
    <s:select list="data.stat.availableStatistics" listKey="id" listValue="title" name="localID" value="%{data.statBase.id}"  label="%{getText('forms.statdata')}"></s:select>
    <s:submit></s:submit>
  </s:form>

  <s:if test="data.statBase==null">
    Molim odaberite koje podatke želite.
  </s:if><s:else>
	<s:if test="data.statBase.statisticsBaseType==1">
      <div>Odabrana je statistika za: <s:property value="%{data.stat.availableStatistics[data.statBase.id].title}"/></div>
      <table>
      <tr>
        <td>Broj studenata koji su pristupili</td><td><s:property value="%{data.statBase.count}"/></td>
      </tr><tr>
        <td>Prosječan proj bodova</td><td><s:property value="%{data.statBase.average}"/></td>
      </tr><tr>
        <td>Medijan bodova</td><td><s:property value="%{data.statBase.median}"/></td>
      </tr>
      </table>

      <div style="margin-bottom: 20px; text-align: center;">
        <form>
         Broj podjela: <input type="text" id="podjele" value="10"><input type="button" value="Ažuriraj" onclick="prilagodi(); return false;">
        </form>
      </div>

      <div style="text-align: center;">
      <img id="hslika" src="<s:url action="AssessmentStat" method="scoreHistogram"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param><s:param name="assessmentID"><s:property value="data.assessment.id"/></s:param><s:param name="localID"><s:property value="data.statBase.id"/></s:param></s:url>">
      </div>

 <script type="text/javascript">
   var u = '<s:url action="AssessmentStat" method="scoreHistogram"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param><s:param name="assessmentID"><s:property value="data.assessment.id"/></s:param><s:param name="localID"><s:property value="data.statBase.id"/></s:param></s:url>';
   function prilagodi() {
	 var numericExpression = /^[0-9]+$/;
     var slika = document.getElementById('hslika');
     var vrijednost = document.getElementById('podjele');
     if(!(vrijednost.value+"").match(numericExpression)) {
         alert("Morate unijeti valjani cijeli broj!");
         return false;
     }
     var br = parseInt(vrijednost.value);
     if(br<3) {
         alert("Morate unijeti valjani cijeli broj koji nije manji od 3!");
         return false;
     }
     var u2 = u.replace(/&amp;/g, '&');
     slika.src = u2+"&bins="+vrijednost.value;
     return false;
   }
 </script>

	</s:if>
	<s:if test="data.statBase.statisticsBaseType==2">
      <div>Odabrana je statistika za: <s:property value="%{data.stat.availableStatistics[data.statBase.id].title}"/></div>
      <table>
      <tr>
        <th>Oznaka zadatka</th>
        <th>Ukupni broj studenata</th>
        <th>Točno</th>
        <th>Netočno</th>
        <th>Neodgovoreno</th>
        <th>Indeks diskriminacije</th>
        <th>Apsolutna težina</th>
        <th>Relativna težina</th>
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
  

<div class="bottomNavMenu">
  <a href="<s:url action="Main"/>"><s:text name="Navigation.main"/></a>
 | <a href="<s:url action="ShowCourse"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.courseHome"/></a>
 | <a href="<s:url action="AdminAssessmentList"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.assessments"/></a>
</div>

</s:if>
