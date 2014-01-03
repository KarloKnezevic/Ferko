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

<link rel="stylesheet" type="text/css" href="/ferko/css/issues.css" />

<style type="text/css">
form.cmxform fieldset li {
	padding:5px 0;
}
</style>

<script type="text/javascript">
		function myDateInit(){
			document.form1.ddate.value = document.getElementById('currTime').value;
		}
		
	  function toggleDelayManager() {
		var el = document.getElementById("delayManager");
		if ( el.style.display != 'none' ) {
			el.style.display = 'none';
		}
		else {
			el.style.display = '';
		}
	  }
	  
	   function togglePublicity() {
	   	var status = document.getElementById("publicStatus");
	   	status.innerHTML="Mijenjam...";
		  var issueID = document.getElementById("issueID").value;
		  var courseInstanceID = document.getElementById("courseInstanceID").value;
		  $.post("<s:url action="ViewIssue" method="alterPublicity" />",{issueID: issueID, courseInstanceID: courseInstanceID}, function(xml) {
			  var code = $("code",xml).text();
			  if(code=="0") {
			    	status.innerHTML="Ne&nbsp;&nbsp;<a href=\"javascript:togglePublicity();\">Promijeni u javno</a>";
			  } else {
					status.innerHTML="Da&nbsp;&nbsp;<a href=\"javascript:togglePublicity();\">Promijeni u privatno</a>";
			  }
			  // alert(document.getElementById("publicStatus").innerHTML);
		  });
	  	}

	   function delay() {
	   	var ddate = document.getElementById("delayDate").value;
	   	var dinfo = document.getElementById("delayInfo");
	   	status.innerHTML="Odgađam...";
		  var issueID = document.getElementById("issueID").value;
		  var courseInstanceID = document.getElementById("courseInstanceID").value;
		  $.post("<s:url action="ViewIssue" method="delayIssue" />",{issueID: issueID, courseInstanceID: courseInstanceID, delayDate: ddate}, function(xml) {
			  var code = $("code",xml).text()
			  //Neispravan datum - krivi format ili datum raniji od trenutnog vremena
			  if(code=="0") {
			    	dinfo.innerHTML="Neispravan datum!";
			    	dinfo.style.fontWeight = "bold";
			    	document.getElementById("delayDate").value = document.getElementById("currTime").value;
			  } else {
			  		var newStatus = $("newstatus",xml).text();
			  		var lastModifiedDate = $("lastmodified",xml).text();
					dinfo.innerHTML=ddate+"&nbsp;&nbsp;<a href=\"javascript:cancelDelay();\">Poništi odgodu</a>";
					document.getElementById("currentStatus").innerHTML = newStatus;
					document.getElementById("lastModified").innerHTML = lastModifiedDate;
					dinfo.style.fontWeight = "normal";
					toggleDelayManager();
			  }
		  });
	  	}

	   function cancelDelay() {
	   	var dinfo = document.getElementById("delayInfo");
	   	status.innerHTML="Poništavam odgodu...";
		  var issueID = document.getElementById("issueID").value;
		  var courseInstanceID = document.getElementById("courseInstanceID").value;
		  $.post("<s:url action="ViewIssue" method="cancelDelay" />",{issueID: issueID, courseInstanceID: courseInstanceID}, function(xml) {
			  var code = $("code",xml).text();
			  //Neispravan datum - krivi format ili datum raniji od trenutnog vremena
			  if(code=="0") {
			    	dinfo.innerHTML="Došlo je do pogreške u komunikaciji!";
			    	dinfo.style.fontWeight = "bold";
			  } else {
					dinfo.innerHTML="Pitanje nije odgođeno";
					var newStatus = $("newstatus",xml).text();
					var lastModifiedDate = $("lastmodified",xml).text();
					document.getElementById("currentStatus").innerHTML = newStatus;
					document.getElementById("lastModified").innerHTML = lastModifiedDate;
					dinfo.style.fontWeight = "normal";
			  }
		  });
	  	}
  
</script>


<s:hidden id="currTime" value="%{data.messageBean.currentTime}"/>
<s:hidden id="courseInstanceID" value="%{courseInstanceID}"/>
<s:hidden id="issueID" value="%{data.messageBean.ID}"/>

<div id="content" class="ticket">
  <div id="ticket">
	 <h2 class="summary"><s:property value="data.messageBean.messageName"/></h2>
	 <table class="properties">
		  <tr>
			   <th>Autor:</th>
			   		<td><s:property value="data.messageBean.ownerName"/></td>
			   <th>Status:</th>
			   		<td><div id="currentStatus"><s:property value="data.messageBean.messageStatus"/></div></td>
		  </tr>
		  <tr>
			    <th>Postavljeno:</th>
				    <td><s:property value="data.messageBean.creationDate"/></td>
			    <th>Zadnja izmjena:</th>
				    <td><div id="lastModified"><s:property value="data.messageBean.lastModificationDate"/></div></td>
		  </tr>
		  <tr>
			    <th>Pitanje javno:</th>
			    	<td headers="h_component">
			    		<div id="publicStatus"><s:property value="data.messageBean.publicity"/>&nbsp;&nbsp;
				    		<s:if test="data.canChangeIssuePublicity">
								<s:if test="!data.messageBean.declaredPublic">
									<a href="javascript:togglePublicity();">Promijeni u javno</a>
								</s:if> 
								<s:if test="data.messageBean.declaredPublic">
									<a href="javascript:togglePublicity();">Promijeni u privatno</a>
								</s:if> 
							</s:if> 
						</div>
			    	</td>
			    <th>Tema:</th>
					<td><s:property value="data.messageBean.topicName"/></td>
		  </tr>
		  <tr>
			    <th>Odgođeno do:</th>
					<td>
						<div id="delayInfo">
							<s:property value="data.messageBean.delayDate"/> &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
							<s:if test="data.canDelayAnswer">
								<s:if test="data.messageBean.isDelayed">
								  <a href="javascript:cancelDelay();"><s:text name = "ITS.cancelDelay" /></a>
								</s:if>
							</s:if>
						</div>
								<s:if test="data.canDelayAnswer">
									<a href="javascript:toggleDelayManager();">Odgodi/Produlji odgodu</a>
									<div id="delayManager" style="display: none;">			    	
										<s:form action="ViewIssue" theme="ferko" method="post" >
											<s:textfield size= "20" id="delayDate"  value="%{data.messageBean.currentTime}" name="data.issueDeadline"/>
											<input type="button" value="Odgodi pitanje" onClick="delay();return false;"/>
										</s:form>
									</div>
								</s:if>
					</td>
		  </tr>		  
	 </table>
	   
	   
		<div class="description"><p><s:property value="data.messageBean.messageContent"/></p></div>
		<s:if test="data.canSendAnswer">
			<a href="<s:url action="ViewIssue" method="newAnswer"><s:param name="courseInstanceID"><s:property value="courseInstanceID"/></s:param><s:param name="issueID"><s:property value="data.messageBean.ID"/></s:param></s:url>">
					<s:text name = "ITS.answerThisIssueLink" />
			</a>
		</s:if>
  	</div>
</div>

<s:iterator  value="data.messageBean.answers" status="stat">
	<div id="content" class="ticket">
		<div id="ticket" style="background: #EFEFF9;">
			<table class="properties" style="border-top: 0px; background: #E8E8F9;">
				<tr>
					<th style="background: #E8E8F9;">Autor:</th><td><s:property value="%{user}"/></td>
				    <th style="background: #E8E8F9;">Odgovoreno:</th><td><s:property value="%{date}"/></td>
				</tr>
			</table>
			<div class="description"><p><s:property value="%{content}"/></p></div>
		</div>
	</div>
</s:iterator>		

	<s:if test="data.canSendAnswer&&data.messageBean.answers.size>0">
		<a href="<s:url action="ViewIssue" method="newAnswer"><s:param name="courseInstanceID"><s:property value="courseInstanceID"/></s:param><s:param name="issueID"><s:property value="data.messageBean.ID"/></s:param></s:url>">
				<s:text name = "ITS.answerThisIssueLink" />
		</a>
	</s:if>
		<br>
<s:if test="data.canCloseIssue">
	<s:if test="data.messageBean.offerExpliciteClosure">   
		<a href="<s:url action="ViewIssue" method="closeIssue"><s:param name="courseInstanceID"><s:property value="courseInstanceID"/></s:param><s:param name="issueID"><s:property value="data.messageBean.ID"/></s:param></s:url>">
			<s:text name = "ITS.closeThisIssue" />
		</a>
	</s:if>
</s:if>

<br><br><br>
	

