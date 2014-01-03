<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<script type="text/javascript">
		
	  function toggleScheduleUpload(planid) {
		var el = document.getElementById("scheduleUpload"+planid);
		if ( el.style.display != 'none' ) {
			el.style.display = 'none';
		}
		else {
			el.style.display = '';
		}
	  }
	 
	  function beginPlanPreparation(planID) {
	   	var status = document.getElementById("status"+planID);
	   	status.innerHTML="Priprema plana u tijeku.";
	   	$("#planActions"+planID).hide();
	   	$("#progressIndicator"+planID).css("display", "inline");
		  
		  var courseInstanceID = document.getElementById("courseInstanceID").value;
		  
		  $.get("<s:url action="Planning" method="preparePlan" />",{planID: planID, courseInstanceID: courseInstanceID}, function(xml) {
			  var generalresult = $("planresult",xml).text();
			  var planstatus = $("message",xml).text();
			  var preparelink = document.getElementById("planPreparationLink"+planID);
			  
			  
			  if(generalresult=="SUCCESS") {
			    	status.innerHTML="Plan pripremljen za izradu rasporeda.";
			    	preparelink.innerHTML = "<a href=\"javascript:beginPlanPreparation("+planID+");\">Ponovo pripremi plan</a>";
			  		$("#schedule"+planID).show();
			  		
			  } else {
					status.innerHTML="Pogreška kod pripreme plana.";
					preparelink.innerHTML = "<a href=\"javascript:beginPlanPreparation("+planID+");\">Pripremi plan</a>";
			  		$("#schedule"+planID).hide();
			  }
			  $("#planActions"+planID).show();
			  $("#progressIndicator"+planID).css("display", "none");
			  
			  // alert(document.getElementById("publicStatus").innerHTML);
		  });
	  	}

</script>  
	  
	  
		<a href="<s:url action="Planning" method="newPlan"><s:param name="courseInstanceID"><s:property value="courseInstanceID"/></s:param></s:url>">
			<s:text name = "Planning.newPlanLink" />
		</a>
				<br><br>
				
<s:hidden id="courseInstanceID" value="%{courseInstanceID}"/>

<h3><s:text name = "Planning.planOverviewSubtitle" /></h3>
		
	<s:if test="data.planBeans.size>0">
		<s:iterator  value="data.planBeans" status="stat">
			<div id="content" class="ticket">
				<div id="ticket" style="background: #EFEFF9;">
					<h2 class="summary"><s:property value="%{name}"/></h2>
					
					<table class="properties" style="border-top: 0px; background: #E8E8F9;">
						<tr>
							<th style="background: #E8E8F9;">Status:</th><td id="status<s:property value="%{ID}"/>" ><s:property value="%{status}"/></td>
						    <th style="background: #E8E8F9;">Plan izrađen:</th><td><s:property value="%{creationDate}"/></td>  
						</tr>
						<tr id="planActions<s:property value="%{ID}"/>"><th style="background: #E8E8F9;">Akcije:</th>
							<td>
							
								<div id="preparePlan">
									<div id="planPreparationLink<s:property value="%{ID}"/>">
										<a href="javascript:beginPlanPreparation(<s:property value="%{ID}"/>);">
										
											<s:if test="status==\"Plan pripremljen za izradu rasporeda\"">
												Ponovno pripremi plan
											</s:if>
											<s:else>
												<s:text name = "Planning.preparePlan" />
											</s:else>
										</a>
									</div>
								</div>
								
								<br>

									<a href="<s:url action="Planning" method="newPlan">
											<s:param name="courseInstanceID"><s:property value="courseInstanceID"/></s:param>
											<s:param name="planID"><s:property value="%{ID}"/></s:param></s:url>">
											<s:text name = "Planning.editPlanLink" />
									</a>
	
								<br>
								
								<s:if test="status==\"Plan pripremljen za izradu rasporeda\"">
								<br>
									<div id="schedule<s:property value="%{ID}"/>">
										<a href="<s:url action="Planning" method="getLocalScheduler">
												<s:param name="courseInstanceID"><s:property value="courseInstanceID"/></s:param>
												<s:param name="planID"><s:property value="%{ID}"/></s:param></s:url>">
												<s:text name = "Planning.getLocalScheduler" />
										</a>
									
												
										<br><br>
									
										<a href="javascript:toggleScheduleUpload(<s:property value="%{ID}"/>);">Unos lokalno izrađenog rasporeda</a>
										<div id="scheduleUpload<s:property value="%{ID}"/>" style="display: none;position:relative;right:25px;">
											<s:form action="ScheduleUpload" method="post" enctype="multipart/form-data">
											    <s:hidden name="courseInstanceID" value="%{data.courseInstance.id}"></s:hidden>
											    <s:hidden name="planID" value="%{ID}"></s:hidden>
											    <s:file name="schedule" ></s:file>
											    <s:submit></s:submit>
											  </s:form>
										</div>
									</div>	
								</s:if>
							</td>
						</tr>
						
					</table>
					
						<div id="progressIndicator<s:property value="%{ID}"/>" style="display:none;">
							<img style="margin-left:auto;margin-right:auto;display:block;" src="/ferko/img/progressIndicator1.gif">
						</div>					
					
						<s:if test="schedules.size>0">
							Izrađeni rasporedi
							<table class="properties" style="border-top: 0px; background: #E8E8F9;">
								<tr>
									<th style="background: #E3E4FA;font-weight:bold">Redni broj:</th>
									<th style="background: #E3E4FA;font-weight:bold">Raspored pohranjen: </th> 
								    <th style="background: #E3E4FA;font-weight:bold">Raspored objavljen: </th> 
								    <th style="background: #E3E4FA;font-weight:bold">Akcija: </th> 
								</tr>
								<s:iterator value="schedules" status="stat" >
									<tr>
										<td> Raspored <s:property value="%{#stat.index}" /> </td>
										<td> <s:property value="creationDate" />  </td>
										<td> <s:if test="publicationDate!=null">
												<s:property value="publicationDate" />  
											</s:if>
											<s:else>
												Nije objavljen.
											</s:else>
										</td>
										<td> 
											<s:if test="publicationDate==null">
												<a href="<s:url action="SchedulePublication" method="execute">
													<s:param name="courseInstanceID"><s:property value="courseInstanceID"/></s:param>
													<s:param name="scheduleID"><s:property value="id"/></s:param></s:url>">
												<s:text name = "Planning.publishSchedule" />
												</a>
											</s:if>
										</td>
									</tr>
								</s:iterator>
							</table>

						</s:if>
						<s:else>Nema izrađenih rasporeda.</s:else>
				</div>
			</div>
		</s:iterator>	
	</s:if>
	<s:else>
		<br><i><s:text name = "Planning.noPlansCreatedInfo" /></i>
	</s:else>

