<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<script type="text/javascript">
  function toggle(cb, userID, assessmentID) {
	  // alert("cb.value="+cb.value+", checked="+cb.checked);
	  cb.disabled = true;
	  if(cb.checked) {
		  // Znaci, ako ga cita kao postavljenog, to znaci da ga treba postaviti i u bazi
		  $.post("<s:url action="CCIAManager" method="matrixAddItem" />",{userID: userID, id: assessmentID}, function(xml) {
			  var code = $("code",xml).text();
			  if(code=="0" || code=="1") {
			    var pres = $("present",xml).text();
			    cb.checked=(pres=="1");
			  } else {
				alert("Poslužitelj je prijavio pogrešku: "+code);
			  }
			  cb.disabled = false;
			  // alert("Vraceno je: " + pres + ", code=" + $("code",xml).text());
		  });
	  } else {
		  // Znaci, ako ga cita kao ugasenog, to znaci da ga treba ugasiti i u bazi
		  $.post("<s:url action="CCIAManager" method="matrixRemoveItem" />",{userID: userID, id: assessmentID}, function(xml) {
			  var code = $("code",xml).text();
			  if(code=="0" || code=="1") {
			    var pres = $("present",xml).text();
			    cb.checked=(pres=="1");
			  } else {
				alert("Poslužitelj je prijavio pogrešku: "+code);
			  }
			  cb.disabled = false;
			  // alert("Vraceno je: " + pres + ", code=" + $("code",xml).text());
		  });
	  }
  }
</script>

<div id="body" class="container">

<s:if test="data.messageLogger.hasMessages()">
  <ul>
  <s:iterator value="data.messageLogger.messages">
      <li>[<s:property value="messageType"/>] <s:property value="messageText"/></li>
  </s:iterator>
  </ul>
</s:if>

<s:if test="data.cciaMatrix==null">
 Nema podataka.
</s:if>
<s:elseif test="data.cciaMatrix.rows.empty || data.cciaMatrix.assessmentNames.length==0">
 Nema definiranih provjera.
</s:elseif>
<s:else>
  <table>
  <tr>
    <th>JMBAG</th>
    <th>Prezime, ime</th>
    <s:iterator value="data.cciaMatrix.assessmentNames"><th><s:property/></th></s:iterator>
  </tr>
  <s:iterator value="data.cciaMatrix.rows">
  <tr>
    <td><s:property value="jmbag"/></td>
    <td><s:property value="lastName"/>, <s:property value="firstName"/></td>
    <s:iterator value="columns"><td><input type="checkbox" value="0" <s:if test="present">checked="checked"</s:if> onclick="toggle(this,<s:property value="userID"/>,<s:property value="courseComponentItemAssessmentID"/>); return false;"></td></s:iterator>
  </tr>
  </s:iterator>
  </table>
</s:else>

<div>
  <a href="<s:url action="Main"/>"><s:text name="Navigation.main"/></a>
 | <a href="<s:url action="ShowCourse"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.courseHome"/></a>
 | <a href="<s:url action="CCManager"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.courseComponentManager"/></a>
 | <a href="<s:url action="CCIManager" method="viewItem"><s:param name="id" value="data.courseComponentItem.id"/></s:url>"><s:text name="Navigation.viewComponentItem"/></a>
</div>
</div>