<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<s:if test="data.messageLogger.hasMessages()">
	<ul class="msgList">
		<s:iterator value="data.messageLogger.messages">
			<li class="msgType_<s:property value="messageType" />"><s:property value="messageText" /></li>
		</s:iterator>
	</ul>
</s:if>

<div> 
<a href="<s:url action="AssessmentRoomSchedule" method="autoChooseRooms"><s:param name="assessmentID"><s:property value="data.assessment.id"/></s:param></s:url>"><s:text name="Navigation.autoChooseRooms"/></a>
| <a href="<s:url action="AssessmentRoomSchedule" method="getAvailableStatus"><s:param name="assessmentID" value="data.assessment.id"/></s:url>"><s:text name="Navigation.getAvailableStatus"/></a>
| <a href="<s:url action="AssessmentRoomSchedule" method="syncReservations"><s:param name="assessmentID" value="data.assessment.id"/></s:url>"><s:text name="Navigation.syncReservations"/></a>
</div>

<div>Popis soba:</div>
<div align="right">Ukupno studenata: <s:property value="data.userNumber"/> | Trenutni kapacitet uzetih dvorana: <s:property value="data.currCapacity"/>
<s:if test='data.percent=="-"'>(-)</s:if><s:else>(<s:property value="data.percent"/>)</s:else></div>

<s:if test="data.roomList != null && data.roomList.size()>0">
	<s:form action="AssessmentRoomSchedule" method="POST" theme="simple">
		<table>
			<thead>
				<tr>
					<th>
						<s:url id="url" action="AssessmentRoomSchedule" method="editRooms">
							<s:param name="assessmentID"><s:property value="data.assessment.id"/></s:param>
							<s:param name="data.sort">name</s:param>
							<s:param name="data.type">
							<s:if test="data.sort.equals('name') && data.type.equals('asc')">desc</s:if>
							<s:else>asc</s:else>
							</s:param>
						</s:url>
						<s:a href="%{url}">Naziv sobe</s:a>
					</th>
					<th>
						<s:url id="url" action="AssessmentRoomSchedule" method="editRooms">
							<s:param name="assessmentID"><s:property value="data.assessment.id"/></s:param>
							<s:param name="data.sort">capacity</s:param>
							<s:param name="data.type">
							<s:if test="data.sort.equals('capacity') && data.type.equals('asc')">desc</s:if>
							<s:else>asc</s:else>
							</s:param>
						</s:url>
						<s:a href="%{url}">Kapacitet</s:a>
					</th>
					<th>
						<s:url id="url" action="AssessmentRoomSchedule" method="editRooms">
							<s:param name="assessmentID"><s:property value="data.assessment.id"/></s:param>
							<s:param name="data.sort">assistants</s:param>
							<s:param name="data.type">
							<s:if test="data.sort.equals('assistants') && data.type.equals('asc')">desc</s:if>
							<s:else>asc</s:else>
							</s:param>
						</s:url>
						<s:a href="%{url}">Potrebno asistenata</s:a>
					</th>
					<th>
						<s:url id="url" action="AssessmentRoomSchedule" method="editRooms">
							<s:param name="assessmentID"><s:property value="data.assessment.id"/></s:param>
							<s:param name="data.sort">roomTag</s:param>
							<s:param name="data.type">
							<s:if test="data.sort.equals('roomTag') && data.type.equals('asc')">desc</s:if>
							<s:else>asc</s:else>
							</s:param>
						</s:url>
						<s:a href="%{url}">Pozeljnost sobe</s:a>
					</th>
					<th>
						<s:url id="url" action="AssessmentRoomSchedule" method="editRooms">
							<s:param name="assessmentID"><s:property value="data.assessment.id"/></s:param>
							<s:param name="data.sort">taken</s:param>
							<s:param name="data.type">
							<s:if test="data.sort.equals('taken') && data.type.equals('asc')">desc</s:if>
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
				<s:iterator value="data.roomList" status="stat">
				<tr <s:if test="taken" >bgcolor="#FFFFAA"</s:if>>
					<td>
						<s:hidden name="data.roomList[%{#stat.index}].id" value="%{id}"/>
						<s:property value="%{name}"/>
					</td>
					<td><s:textfield name="data.roomList[%{#stat.index}].capacity" value="%{capacity}" /></td>
					<td><s:textfield name="data.roomList[%{#stat.index}].requiredAssistants" value="%{requiredAssistants}" /></td>
					<td><s:select list="data.roomTags" name="data.roomList[%{#stat.index}].roomTag" value="roomTag"/> </td>
					<td><s:checkbox name="data.roomList[%{#stat.index}].taken" value="%{taken}" /></td>
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

<h2>Dodavanje zavodskih prostorija na popis <a target="_jcms_help" href="<s:url action="Help"><s:param name="helpKey">000034</s:param></s:url>" onclick="blur();"><img src="img/icons/help.png"></a></h2>
<s:form action="AssessmentRoomSchedule" method="POST">
	<s:textfield name="data.roomVenue" label="%{getText('forms.roomVenue')}"/>  
	<s:textfield name="data.roomName" label="%{getText('forms.roomName')}"/>  
	<s:hidden name="assessmentID" value="%{data.assessment.id}"></s:hidden>
	<s:submit method="addRoomToList"  value="%{getText('forms.general.add')}" align="left"/>
</s:form>