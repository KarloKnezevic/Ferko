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

<script type="text/javascript">
		
	  function toggleStudents(planid) {
		var el = document.getElementById("students"+planid);
		if ( el.style.display != 'none' ) {
			el.style.display = 'none';
		}
		else {
			el.style.display = '';
		}
	  }

	  function componentsChecked(eventNumber){
	  	//alert('Component radio button checked');
	  	document.getElementById("privategroupname"+eventNumber).disabled=true;
	  	document.getElementById("privategroupinformation"+eventNumber).innerHTML=" ";
	  	document.getElementById("componentgroupinformation"+eventNumber).innerHTML=" ";
	  	document.getElementById("components"+eventNumber).disabled=false;
	  	document.getElementById("componentNumbers"+eventNumber).disabled=false;
	  	document.getElementById("schedulePublicationButton").disabled=true;
	  	document.getElementById("groupValidationResult").innerHTML=" ";
	  	$("#groupCleaningPermissionDiv").css("display", "none");
	  }
	  
	  function privateGroupsChecked(eventNumber){
	  	//alert('Private groups radio button checked');
	  	document.getElementById("privategroupname"+eventNumber).disabled=false;
	  	document.getElementById("componentgroupinformation"+eventNumber).innerHTML=" ";
	  	document.getElementById("privategroupinformation"+eventNumber).innerHTML=" ";
	  	document.getElementById("components"+eventNumber).disabled=true;
	  	document.getElementById("componentNumbers"+eventNumber).disabled=true;
	  	document.getElementById("schedulePublicationButton").disabled=true;
	  	document.getElementById("groupValidationResult").innerHTML=" ";
	  	$("#groupCleaningPermissionDiv").css("display", "none");
	  }
	  
	  function togglePublicationButton(){
	  	if(document.getElementById("groupCleaningPermission").checked){
	  		document.getElementById("schedulePublicationButton").disabled=false;
	  	}else{
	  		document.getElementById("schedulePublicationButton").disabled=true;
	  	}
	  }
	  
	  function validateGroups(){
	  	
	  	$("#groupCleaningPermissionDiv").css("display", "none");
	  	document.getElementById("schedulePublicationButton").disable=true;
	  	var courseInstanceID = document.getElementById("courseInstanceID").value;
	  	var i=0;
	  	var j=0;
	  	var numberOfEvents = document.getElementById("numberOfEvents").value;
	  	var groupDuplicationCheck = true; //Inicijalno je ok
	  	//alert("events: " + numberOfEvents);
	  	//Provjera je li za svaki event odabrana razlicita grupa
	  	for(i=0; i<numberOfEvents; i++){
	  		for(j=i+1; j<numberOfEvents; j++){
	  			if(i==j) continue;
	  			if(document.getElementById("componentGroup"+i).checked && document.getElementById("componentGroup"+j).checked){
	  				//alert("both components groups. number1 " +document.getElementById("componentNumbers"+i).value + " number2 " + document.getElementById("componentNumbers"+j).value);
	  				if(document.getElementById("componentNumbers"+i).value==document.getElementById("componentNumbers"+j).value) {
	  					//alert("2");
	  					groupDuplicationCheck = false;
	  				}
	  			}else if(!document.getElementById("componentGroup"+i).checked && !document.getElementById("componentGroup"+j).checked){
	  				//alert("3");
	  				if(document.getElementById("privategroupname"+i).value==document.getElementById("privategroupname"+j).value){
	  					//alert("4");
	  					groupDuplicationCheck = false;
	  				}
	  			}
	  		}
	  	}
	  
	  
	  	if(!groupDuplicationCheck){
	  		document.getElementById("groupValidationResult").innerHTML="Pogreška. Pokušavate objaviti više od jednog događaja u istu grupu! ";
	  		return;
	  	}	
	  	
	  	var serverCheckAllowed = true;
	  	var requestData="<req>";
	  	
	  	for(i=0; i<numberOfEvents; i++){

	  		if(document.getElementById("componentGroup"+i).checked){
	  			var selectedComponent = document.getElementById("components"+i).value;
	  			var selectedComponentNumber = document.getElementById("componentNumbers"+i).value;
	  			requestData+= "<cg" + " cid=\"" + selectedComponent + "\" cn=\"" + selectedComponentNumber + "\" serial=\""+ i +"\" />";
	  			//alert(selectedComponent + ' ' + selectedComponentNumber);
	  		}else{
	  			var privateGroupName = document.getElementById("privategroupname"+i).value;
	  			var infoElement = document.getElementById("privategroupinformation"+i);
	  			if(privateGroupName.length==0){
	  				$("#privategroupinformation"+i).css("color", "red");
	  				$("#privategroupinformation"+i).css("font-weight", "bold");
	  				infoElement.innerHTML = "Nije zadano ime privatne grupe!";
	  				document.getElementById("groupValidationResult").innerHTML="Nedostaju nazivi nekih privatnih grupa.";
	  				serverCheckAllowed=false;
	  			}else{
	  				requestData+= "<pg" + " name=\"" + privateGroupName + "\" serial=\""+ i +"\" />";
	  				infoElement.innerHTML = " ";
	  			}
	  		}
	  		
	  	}
	  	requestData+="</req>";
	  	
	  	if(serverCheckAllowed){
	  	
	  		//Quick cleanup of information
	  		for(i=0; i<numberOfEvents; i++){
	  			document.getElementById("privategroupinformation"+i).innerHTML =" ";
	  			document.getElementById("componentgroupinformation"+i).innerHTML =" ";
	  		}
	  	
	  		$.get("<s:url action="SchedulePublication" method="validatePublicationGroups" />",{publicationGroups: requestData, courseInstanceID: courseInstanceID}, function(xml) {
			  var generalresult = $("planresult",xml).text();
			  var message = $("message",xml).text();
			  
			  var groupValidationResult = document.getElementById("groupValidationResult");
			  var schedulePublicationButton = document.getElementById("schedulePublicationButton");
			  if(generalresult=="VALID") {
			    	groupValidationResult.innerHTML="Sve grupe su dostupne.";
			    	schedulePublicationButton.disabled = false;
			  }else if(generalresult=="INVALID"){
					groupValidationResult.innerHTML="Neke od odabranih grupa već postoje. Za objavu rasporeda morate dozvoliti njihovo brisanje.";
					schedulePublicationButton.disabled = true;
					$("#groupCleaningPermissionDiv").css("display", "inline");
					var invalidGroups = message.split(" ");
					var j=0;
					for(j=0; j<invalidGroups.length; j++){
						if(document.getElementById("componentGroup"+j).checked){
							document.getElementById("componentgroupinformation"+j).innerHTML="Grupa zauzeta!";
							$("#componentgroupinformation"+j).css("color", "red");
	  						$("#componentgroupinformation"+j).css("font-weight", "bold");
						}else{
							document.getElementById("privategroupinformation"+j).innerHTML="Grupa zauzeta!";
							$("#privategroupinformation"+j).css("color", "red");
	  						$("#privategroupinformation"+j).css("font-weight", "bold");
						}
					}
					
			  }else if(generalresult=="ERROR"){
			  		groupValidationResult.innerHTML="Došlo je do pogreške u validaciji odabranih grupa.";
			  		schedulePublicationButton.disabled = true;
			  }
			  
		  });
	  	
	  	}
  
	  }

</script> 
<br><br>
<h3><s:text name = "Planning.schedulePublicationSubtitle" /><br><i><s:property value="%{data.scheduleBean.name}"/></i></h3>
<br>
	<s:form theme="simple" action="SchedulePublication" method="post">
		<s:hidden id="courseInstanceID" value="%{courseInstanceID}" name="courseInstanceID"/>
		<s:hidden value="%{scheduleID}" name="scheduleID"/>
		<s:hidden value="%{data.scheduleBean.name}" name="data.scheduleBean.name"/>
		<s:hidden id="numberOfEvents" value="%{data.scheduleBean.eventBeans.size}" />
		
		<s:iterator id="eventobj" value="data.scheduleBean.eventBeans" status="stat">

				<h3>Događaj: <s:label value="%{name}" /></h3>
							 <s:hidden name="data.scheduleBean.eventBeans[%{#stat.index}].name"  value="%{name}" />
							 <s:hidden name="data.scheduleBean.eventBeans[%{#stat.index}].id"  value="%{id}" />
				<div style="position:relative;left:20px;">			 
				
				Objavi kao:<br>
				
				<input type="radio" checked="checked" name="data.scheduleBean.eventBeans[<s:property value="%{#stat.index}"/>].groupType" id="componentGroup<s:property value="%{#stat.index}"/>" value="componentgroup" onClick="componentsChecked('<s:property value="%{#stat.index}"/>');" >
				<s:select id="components%{#stat.index}" list="data.components" listKey="id" listValue="name" name="data.scheduleBean.eventBeans[%{#stat.index}].componentID"  />  
				
				br. 
				<s:select id="componentNumbers%{#stat.index}" list="data.numbers" name="data.scheduleBean.eventBeans[%{#stat.index}].selectedNumber" />
				<div style="display:inline;position:relative;left:20px;" id="componentgroupinformation<s:property value="%{#stat.index}"/>">  </div>
				
				<br>
				<div style="position:relative;top:5px;">   
				<input type="radio" name="data.scheduleBean.eventBeans[<s:property value="%{#stat.index}"/>].groupType" id="privateGroup<s:property value="%{#stat.index}"/>" value="privategroup" onClick="privateGroupsChecked('<s:property value="%{#stat.index}"/>');" >
				privatnu grupu naziva 
				<input type="text" id="privategroupname<s:property value="%{#stat.index}"/>" size= "30" value="" name="data.scheduleBean.eventBeans[<s:property value="%{#stat.index}"/>].privateGroupName" disabled="disabled"/>
				<div style="display:inline;position:relative;left:20px;" id="privategroupinformation<s:property value="%{#stat.index}"/>">  </div>
				</div>
				
				<br><br>
				<table>
					<tr style="border-top: 0px; background: #E8E8F9;">
						<td>Termin</td>
					    <td>Početak termina</td>
					    <td>Završetak termina</td>
					    <td>Prostorija</td>
					    <td>Broj studenata</td>
					    <td>Naziv grupe</td>
					    <td>Naziv pridjeljenog događaja</td>
					   <!-- <td>Naziv događaja pridijeljenog grupi</td>  -->
					</tr>
					<s:iterator value="termBeans" status="stat2">
					<tr>
						<s:hidden name="data.scheduleBean.eventBeans[%{#stat.index}].termBeans[%{#stat2.index}].id"  value="%{id}" />
						<td><s:property value="%{#stat2.count}"/>.</td>
					    <td><s:label value="%{termStart}" name="data.scheduleBean.eventBeans[%{#stat.index}].termBeans[%{#stat2.index}].termStart" /></td>
					    <td><s:label value="%{termEnd}" name="data.scheduleBean.eventBeans[%{#stat.index}].termBeans[%{#stat2.index}].termEnd" /></td>
					    <td><s:label value="%{roomName}" name="data.scheduleBean.eventBeans[%{#stat.index}].termBeans[%{#stat2.index}].roomName"/></td>
					    <td><s:label value="%{numberOfStudents}" name="data.scheduleBean.eventBeans[%{#stat.index}].termBeans[%{#stat2.index}].numberOfStudents"/>
					    	<s:hidden value="%{students}" name="data.scheduleBean.eventBeans[%{#stat.index}].termBeans[%{#stat2.index}].students"/>
					    	
					    	<!-- <a href="javascript:toggleStudents(<s:property value="%{#stat.count}"/><s:property value="%{#stat2.count}"/>);">Popis studenata</a>
					    	
					    	<div id="students<s:property value="%{#stat.count}"/><s:property value="%{#stat2.count}"/>" style="display: none;">
								<s:property value="%{students}" />
							</div>
							-->
					    </td>
					    <td><s:textfield theme="simple" size= "40" value="%{name}" name="data.scheduleBean.eventBeans[%{#stat.index}].termBeans[%{#stat2.index}].name"/></td>
					    <td><s:textfield theme="simple" size= "40" value="%{#eventobj.name}" name="data.scheduleBean.eventBeans[%{#stat.index}].termBeans[%{#stat2.index}].eventName"/></td>
					   
					</tr>  
					</s:iterator>
				</table>
				</div>
		</s:iterator>	
		<br><br><br><hr>
		<h2>Završni koraci</h2>
		<h3>Provjera dostupnosti potrebnih grupa</h3>
		<div style="position:relative;left:20px;">
			<input type="button" value="Obavi provjeru" onClick="validateGroups();"><br>
			<div id="groupValidationResult"></div>
			<div id="groupCleaningPermissionDiv" style="display:none;">
				<input id="groupCleaningPermission" type="checkbox" name="permission" onClick="togglePublicationButton();" />
				Dozvoljavam brisanje i/ili izmjenu potrebnih grupa.
			</div>
		</div>
		<!--
		<br>
		<h3>2. Provjera zauzetosti studenata i dvorana</h3>
		<div style="position:relative;left:20px;">
			<input id="studentRoomCheckButton" type="button" value="Obavi provjeru" onClick="precheckStudentsAndRooms();" disabled="true" ><br>
		</div>
		-->
		<br>
		
		<h3><b>Objava rasporeda</b></h3>
		<div style="position:relative;left:20px;">
			<s:submit method="prepareForPublishing" id="schedulePublicationButton" type="button" disabled="true" label="%{getText('Planning.publishSchedule')}"></s:submit>
		</div>
		<br><br>
	 </s:form>	
		