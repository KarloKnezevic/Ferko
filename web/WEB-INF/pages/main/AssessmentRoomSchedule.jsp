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

<div><s:property value="data.courseInstance.course.name"/> (<s:property value="data.courseInstance.course.isvuCode"/>): <s:property value="data.assessment.name"/></div>

<div>Popis soba:</div>
<div align="right">Ukupno studenata: <s:property value="data.userNumber"/> | Trenutni kapacitet uzetih dvorana: <s:property value="data.currCapacity"/>
<s:if test='data.percent=="-"'>(-)</s:if>
<s:else>(<s:property value="data.percent"/>)</s:else>

</div>

<s:if test="roomList != null && roomList.size()>0">
	<s:form action="AssessmentRoomSchedule" method="POST" theme="simple">
		<table>
			<thead>
				<tr>
					<th>
						<s:url id="url" action="AssessmentRoomSchedule" method="editRooms">
							<s:param name="assessmentID"><s:property value="data.assessment.id"/></s:param>
							<s:param name="sort">name</s:param>
							<s:param name="type">
							<s:if test="sort.equals('name') && type.equals('asc')">desc</s:if>
							<s:else>asc</s:else>
							</s:param>
						</s:url>
						<s:a href="%{url}">Naziv sobe</s:a>
					</th>
					<th>
						<s:url id="url" action="AssessmentRoomSchedule" method="editRooms">
							<s:param name="assessmentID"><s:property value="data.assessment.id"/></s:param>
							<s:param name="sort">capacity</s:param>
							<s:param name="type">
							<s:if test="sort.equals('capacity') && type.equals('asc')">desc</s:if>
							<s:else>asc</s:else>
							</s:param>
						</s:url>
						<s:a href="%{url}">Kapacitet</s:a>
					</th>
					<th>
						<s:url id="url" action="AssessmentRoomSchedule" method="editRooms">
							<s:param name="assessmentID"><s:property value="data.assessment.id"/></s:param>
							<s:param name="sort">assistants</s:param>
							<s:param name="type">
							<s:if test="sort.equals('assistants') && type.equals('asc')">desc</s:if>
							<s:else>asc</s:else>
							</s:param>
						</s:url>
						<s:a href="%{url}">Potrebno asistenata</s:a>
					</th>
					<th>
						<s:url id="url" action="AssessmentRoomSchedule" method="editRooms">
							<s:param name="assessmentID"><s:property value="data.assessment.id"/></s:param>
							<s:param name="sort">roomTag</s:param>
							<s:param name="type">
							<s:if test="sort.equals('roomTag') && type.equals('asc')">desc</s:if>
							<s:else>asc</s:else>
							</s:param>
						</s:url>
						<s:a href="%{url}">Pozeljnost sobe</s:a>
					</th>
					<th>
						<s:url id="url" action="AssessmentRoomSchedule" method="editRooms">
							<s:param name="assessmentID"><s:property value="data.assessment.id"/></s:param>
							<s:param name="sort">taken</s:param>
							<s:param name="type">
							<s:if test="sort.equals('taken') && type.equals('asc')">desc</s:if>
							<s:else>asc</s:else>
							</s:param>
						</s:url>
						<s:a href="%{url}">Uzmi sobu</s:a>
					</th>
					<th>
						Status
					</th>
				</tr>
			</thead>
			<tbody>
				<s:iterator value="roomList" status="stat">
				<tr <s:if test="taken" >bgcolor="#FFFFAA"</s:if>>
					<td>
						<s:hidden name="roomList[%{#stat.index}].id" value="%{id}"/>
						<s:property value="%{name}"/>
					</td>
					<td><s:textfield name="roomList[%{#stat.index}].capacity" value="%{capacity}" /></td>
					<td><s:textfield name="roomList[%{#stat.index}].requiredAssistants" value="%{requiredAssistants}" /></td>
					<td><s:select list="roomTags" name="roomList[%{#stat.index}].roomTag" value="roomTag"/> </td>
					<td><s:checkbox name="roomList[%{#stat.index}].taken" value="%{taken}" /></td>
					<td>
					<s:if test="taken">
					  <s:if test="roomStatus.equals('RESERVED') || roomStatus.equals('MANUALLY_RESERVED') ">
						<img src="img/icons/flag_green.png" />
					  </s:if><s:elseif test="roomStatus.equals('UNAVAILABLE')">
						<img src="img/icons/flag_red.png" />
                      </s:elseif><s:elseif test="roomStatus.equals('UNCHECKED') || roomStatus.length()==0">
						<img src="img/icons/lightning.png" />
                      </s:elseif><s:else>
						<img src="img/icons/flag_yellow.png" />
                      </s:else>
					</s:if><s:else>
					  <s:if test="roomStatus.equals('RESERVED') || roomStatus.equals('MANUALLY_RESERVED') ">
						<img src="img/icons/flag_yellow.png" />
					  </s:if><s:elseif test="roomStatus.equals('UNCHECKED') || roomStatus.length()==0">
						<img src="img/icons/lightning.png" />
                      </s:elseif><s:elseif test="roomStatus.equals('UNAVAILABLE')">
						<img src="img/icons/delete.png" />
                      </s:elseif><s:elseif test="roomStatus.equals('AVAILABLE')">
						<img src="img/icons/add.png" />
                      </s:elseif><s:else>
						<img src="img/icons/asterisk_yellow.png" />
                      </s:else>
					</s:else>
					<s:property value="roomStatus"/></td>
				</tr>
				</s:iterator>
			</tbody>
		</table>
		<s:hidden name="assessmentID" value="%{data.assessment.id}"></s:hidden>
		<s:submit method="updateRooms"/>
	</s:form>
</s:if><br>
<div> 
<a href="<s:url action="AssessmentRoomSchedule" method="autoChooseRooms"><s:param name="assessmentID"><s:property value="data.assessment.id"/></s:param></s:url>"><s:text name="Navigation.autoChooseRooms"/></a>
| <a href="<s:url action="AssessmentRoomSchedule" method="getAvailableStatus"><s:param name="assessmentID" value="data.assessment.id"/></s:url>"><s:text name="Navigation.getAvailableStatus"/></a>
| <a href="<s:url action="AssessmentRoomSchedule" method="syncReservations"><s:param name="assessmentID" value="data.assessment.id"/></s:url>"><s:text name="Navigation.syncReservations"/></a>
</div>
<div>
  <a href="<s:url action="Main"/>"><s:text name="Navigation.main"/></a>
 | <a href="<s:url action="ShowCourse"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.courseHome"/></a>
 | <a href="<s:url action="AdminAssessmentList"><s:param name="courseInstanceID" value="data.courseInstance.id"/></s:url>"><s:text name="Navigation.assessments"/></a>
 | <a href="<s:url action="AssessmentSchedule"><s:param name="assessmentID" value="assessmentID"/></s:url>"><s:text name="Navigation.assessmentScheduleEdit"/></a>
</div>

</div>