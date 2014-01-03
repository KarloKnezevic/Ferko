<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>


	<s:if test="data.messageLogger.hasMessages()">
		<ul>
			<s:iterator value="data.messageLogger.messages">
				<li>[<s:property value="messageType" />] <s:property
					value="messageText" /></li>
			</s:iterator>
		</ul>
	</s:if>
	
	<h3><s:text name = "ITS.topicManagementSubtitle" /></h3>
	
	<s:hidden name="courseInstanceID" value="%{courseInstanceID}"/>	

<script type="text/javascript">
  function toggle(cb, topicID, courseInstanceID) {
	  // alert("cb.value="+cb.value+", checked="+cb.checked);
	  cb.disabled = true;
	  if(cb.checked) {
		  // Znaci, ako ga cita kao postavljenog, to znaci da ga treba postaviti i u bazi
		  $.post("<s:url action="Issues" method="updateTopics" />",{topicID: topicID, courseInstanceID: courseInstanceID}, function(xml) {
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
		  $.post("<s:url action="Issues" method="updateTopics" />",{topicID: topicID, courseInstanceID: courseInstanceID}, function(xml) {
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


	<s:if test="data.messageTopics.size>0">
		<br><i><s:label name="%{getText('ITS.topicActivityInfo')}" value="%{getText('ITS.topicActivityInfo')}"/></i>
			<table style="width:50%;">
				<tr>
					<th><s:text name = "ITS.topicManagementTopicColumn" /></th>
					<th><s:text name = "ITS.topicManagementActivityColumn" /></th>
				</tr>
					<s:iterator  value="data.messageTopics" status="stat">
						<tr>
							<td><s:property value="%{name}"/></td>
							<td><input type="checkbox" value="0" <s:if test="active">checked="checked"</s:if> onclick="toggle(this,<s:property value="id"/>,'<s:property value="courseInstanceID"/>'); return false;"></td>
						</tr>
					</s:iterator>
			</table>
	</s:if>
	<s:else>
		<br><i><s:text name = "ITS.topicManagementNoTopicsInfo" /></i>
	</s:else>
