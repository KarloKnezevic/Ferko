<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ taglib prefix="jcms" uri="/jcms-custom-tags" %>

<s:if test="data.messageLogger.hasMessages()">
	<ul class="msgList">
		<s:iterator value="data.messageLogger.messages">
			<li class="msgType_<s:property value="messageType" />"><s:property value="messageText" /></li>
		</s:iterator>
	</ul>
</s:if>

<s:if test="data.renderingClues!=null && !data.renderingClues.empty">
  <h2>Definirane provjere znanja, zastavice i ostalo</h2>
</s:if>

<s:if test="data.imposter">
<s:property value="data.student.lastName"/>, <s:property value="data.student.firstName"/> (<s:property value="data.student.jmbag"/>)<br><br>
</s:if>

<s:if test="data.grade!=null">
  <div style="margin-top: 5px; margin-bottom: 10px;"><span style="font-size: 1.2em;">Vaša ocjena na kolegiju je: <s:property value="data.grade.grade"/>.</span> <span style="font-size: 0.8em;">(podjela ocjena obavljena je <s:property value="data.formatDateTime(data.grade.givenAt)"/>)</span></div>
</s:if>

<s:if test="data.renderingClues!=null && !data.renderingClues.empty">

  <ul type="circle">
  <s:iterator value="data.renderingClues">
    <s:if test="event==1">
      <li><span style="font-weight: bold;" title="<s:property value="object.name"/>"><s:property value="object.shortName"/></span>
      <s:if test="objectType==1"> <!-- Provjera znanja -->
        
         <s:if test="value.error">
          Izračun bodova rezultirao je pogreškom
         </s:if>
         <s:else>
           <s:if test="value.effectivePresent">
             <s:if test="object.maxScore!=null && object.maxScore.doubleValue()!=0">
               <s:if test="value.effectiveScore<0">
                 <img src="img/progress/prog_<s:property value="value.effectiveStatus"/>_0.png" border="0">
               </s:if><s:elseif test="value.effectiveScore<=object.maxScore.doubleValue()">
                 <img src="img/progress/prog_<s:property value="value.effectiveStatus"/>_<s:property value="data.df(value.effectiveScore/object.maxScore.doubleValue()*100,0)"/>.png" border="0">
               </s:elseif><s:else>
                 <img src="img/progress/prog_<s:property value="value.effectiveStatus"/>_100.png" border="0">
               </s:else>
             </s:if><s:else>
               <img src="img/progress/prog_<s:property value="value.effectiveStatus"/>_unknown.png" border="0">
             </s:else>
             <s:property value="data.df(value.effectiveScore,2)"/>
           </s:if>
           <s:else>
             Niste pristupili provjeri.
           </s:else>
         </s:else>
         <a href="<s:url action="AssessmentView"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param><s:param name="assessmentID"><s:property value="value.assessment.id"/></s:param><s:param name="userID"><s:property value="value.user.id"/></s:param></s:url>"><s:text name="Navigation.details"/></a>

      </s:if>
      <s:elseif test="objectType==2"> <!-- Zastavica -->

      <s:if test="value.error">
       Izračun vrijednosti zastavice rezultirao je pogreškom
      </s:if>
      <s:else>
         <span class="stat<s:property value="value.value" />"><span class="statEmpty"><s:property value="value.value" /></span></span>
      </s:else>
        
      </s:elseif>
    </s:if>
    <s:elseif test="event==2">
      <ul type="circle">
    </s:elseif>
    <s:elseif test="event==3">
      </ul>
    </s:elseif>
    <s:elseif test="event==4">
      </li>
    </s:elseif>
  </s:iterator>
  </ul>

</s:if>
<s:else>
  <div>Nema podataka o provjerama.</div>
</s:else>
