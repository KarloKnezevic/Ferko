<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<s:if test="data.messageLogger.hasMessages()">
	<ul class="msgList">
		<s:iterator value="data.messageLogger.messages">
			<li class="msgType_<s:property value="messageType" />"><s:property value="messageText" /></li>
		</s:iterator>
	</ul>
</s:if>

<style>
.singleItem { display:block; background:url(/ferko/img/button-left.png) no-repeat; width:350px; text-align:center; margin:0px auto; border: 0px solid black;}
.singleItem_OK { display:block; background:url(/ferko/img/icons/famtick.png) no-repeat; margin: 0; padding: 0; width: 16px; height: 16px; }
.singleItem_0 { display:block; background:url(/ferko/img/icons/flag_red.png) no-repeat; margin: 0; padding: 0; width: 16px; height: 16px; }
.singleItem_1 { display:block; background:url(/ferko/img/icons/flag_yellow.png) no-repeat; margin: 0; padding: 0; width: 16px; height: 16px; }
.singleItem_2 { display:block; background:url(/ferko/img/icons/flag_green.png) no-repeat; margin: 0; padding: 0; width: 16px; height: 16px; }
.singleItem_NONE { display:block; margin: 0; padding: 0; width: 16px; height: 16px; }
table.shema { width: 960px; border-collapse: collapse; }
table.shema tr td { padding: 0px; border-spacing: 0px; vertical-align: top; border-width: 0px; }
.itemFiller { margin: 0px auto; width: 10px; height: 20px;}
.itemFiller2 { margin: 0px auto; width: 10px; height: 10px;}
.tableCell1 { width: 400px; background-image: url(/ferko/img/sched_arrow_down.gif); background-repeat: no-repeat; background-position: bottom left; }
.tableCell2 { width: 40px; }
.tableCell3 { width: 80px; }
.tableCell4 { width: 400px; }
.tableCell6 { width: 400px; background-image: url(/ferko/img/sched_arrow_none.gif); background-repeat: no-repeat; background-position: bottom left; }
div.singleItem a {font-variant:small-caps; text-transform:lowercase; text-decoration:none;}
div.singleItem a:hover {color:white;}
</style>

<table class="shema">
  <tr>
    <td class="tableCell1"><div class="singleItem"><a href="<s:url action="AssessmentSchedule" method="synchronizeStudents"><s:param name="assessmentID" value="data.assessment.id"/></s:url>"><s:text name="Navigation.synchronizeStudents"/></a></div><div class="itemFiller"></div></td>
    <td class="tableCell2"><div class="singleItem_<s:property value="data.studentsFetched"/>"></div></td>
    <td class="tableCell3">&nbsp;</td>
    <td class="tableCell4">&nbsp;</td>
    <td class="tableCell2"><div class="singleItem_NONE"></div></td>
  </tr>
  <tr>
    <td class="tableCell6"><div class="singleItem"><a href="<s:url action="AssessmentRoomSchedule" method="editRooms"><s:param name="assessmentID" value="data.assessment.id"/></s:url>"><s:text name="Navigation.editRooms"/></a></div><div class="itemFiller2"></div></td>
    <td class="tableCell2"><div class="singleItem_<s:property value="data.roomsFetched"/>"></div></td>
    <td class="tableCell3">&nbsp;</td>
    <td class="tableCell6"><div class="singleItem"><a href="<s:url action="AssessmentAssistantSchedule" method="editAssistants"><s:param name="assessmentID" value="data.assessment.id"/></s:url>"><s:text name="Navigation.assistantSchedule"/></a></div><div class="itemFiller2"></div></td>
    <td class="tableCell2"><div class="singleItem_<s:property value="data.assistantsFetched"/>"></div></td>
  </tr>
  <tr>
    <td colspan="5"><div style="width: 960px; height: 20px; border: 0px solid black; padding: 0px; border-spacing: 0px; vertical-align: top; background-image: url(/ferko/img/sched_double_arrow_down.gif); background-repeat: no-repeat; background-position: bottom left;"></div></td>
  </tr>
  <tr>
    <td class="tableCell6"><div class="singleItem"><a href="<s:url action="AssessmentSchedule" method="makeStudentSchedule"><s:param name="assessmentID" value="data.assessment.id"/><s:param name="type">sorted</s:param></s:url>"><s:text name="Navigation.makeStudentScheduleSorted"/></a></div><div class="singleItem"><a href="<s:url action="AssessmentSchedule" method="makeStudentSchedule"><s:param name="assessmentID" value="data.assessment.id"/><s:param name="type">sorted</s:param><s:param name="proportional">true</s:param></s:url>"><s:text name="Navigation.makeStudentScheduleSortedP"/></a></div><div class="singleItem"><a href="<s:url action="AssessmentSchedule" method="makeStudentSchedule"><s:param name="assessmentID" value="data.assessment.id"/><s:param name="type">random</s:param></s:url>"><s:text name="Navigation.makeStudentScheduleRandom"/></a></div><div class="singleItem"><a href="<s:url action="AssessmentSchedule" method="makeStudentSchedule"><s:param name="assessmentID" value="data.assessment.id"/><s:param name="type">random</s:param><s:param name="proportional">true</s:param></s:url>"><s:text name="Navigation.makeStudentScheduleRandomP"/></a></div><div class="itemFiller2"></div></td>
    <td class="tableCell2"><div class="singleItem_<s:property value="data.studentScheduleCreated"/>"></div></td>
    <td class="tableCell3">&nbsp;</td>
    <td class="tableCell6"><div class="singleItem"><a href="<s:url action="AssessmentAssistantSchedule" method="editAssistantsSchedule"><s:param name="assessmentID" value="data.assessment.id"/></s:url>"><s:text name="Navigation.assistantRoomSchedule"/></a></div><div class="itemFiller2"></div></td>
    <td class="tableCell2"><div class="singleItem_<s:property value="data.assistantScheduleCreated"/>"></div></td>
  </tr>
  <tr>
    <td colspan="5"><div style="width: 960px; height: 20px; border: 0px solid black; padding: 0px; border-spacing: 0px; vertical-align: top; background-image: url(/ferko/img/sched_double_arrow_down.gif); background-repeat: no-repeat; background-position: bottom left;"></div></td>
  </tr>
  <tr>
    <td class="tableCell4"><div class="singleItem"><a href="<s:url action="AssessmentSchedule" method="downloadListings"><s:param name="assessmentID" value="data.assessment.id"/></s:url>"><s:text name="Navigation.assessmentListings"/></a></div><div class="singleItem"><a href="<s:url action="AssessmentSchedule" method="downloadSchedule"><s:param name="assessmentID" value="data.assessment.id"/></s:url>"><s:text name="Navigation.downloadAssessmentSchedule"/></a></div><div class="singleItem"><a href="<s:url action="AssessmentSchedule" method="downloadMailMerge"><s:param name="assessmentID" value="data.assessment.id"/><s:param name="data.cp">1</s:param></s:url>"><s:text name="Navigation.assessmentMailMerge"/></a></div><div class="singleItem"><a href="<s:url action="AssessmentSchedule" method="downloadMailMerge"><s:param name="assessmentID" value="data.assessment.id"/></s:url>"><s:text name="Navigation.assessmentMailMergeU"/></a></div><div class="itemFiller"></div></td>
    <td class="tableCell2"><div class="singleItem_NONE"></div></td>
    <td class="tableCell3">&nbsp;</td>
    <td class="tableCell4"><div class="singleItem"><a href="<s:url action="AssessmentSchedule" method="broadcastEvents"><s:param name="assessmentID" value="data.assessment.id"/></s:url>"><s:text name="Navigation.broadcastEvents"/></a></div><div class="itemFiller"></div></td>
    <td class="tableCell2"><div class="singleItem_<s:property value="data.schedulePublished"/>"></div></td>
  </tr>
</table>

<div>Dodatni poslovi: </div>

<ul>
  <li>
<a href="<s:url action="AssessmentSchedule" method="importScheduleEdit"><s:param name="assessmentID" value="data.assessment.id"/></s:url>"><s:text name="Navigation.importSchedule"/></a>
  </li>
</ul>

<div>Raspored: </div>
<s:if test="data.roomList != null && data.roomList.size()>0">
<table>
    <thead>
    <tr>
      <th>Naziv dvorane</th>
      <th>Kapacitet</th>
      <th>Broj dodijeljenih studenata</th>
      <th>Broj dodijeljenih/potrebnih asistenata</th>
      <th>Detalji</th>
    </tr>
    </thead>
    <tbody>
    <s:iterator value="data.roomList" status="cust_stat">
    <tr class="<s:if test="#cust_stat.odd == true">oddrow</s:if><s:else>evenrow</s:else>">
      <td><s:property value="roomName"/></td>
      <td><s:property value="capacity"/></td>
      <td><s:property value="userNum"/></td>
      <td><s:property value="assistantNum"/>/<s:property value="assistantRequired"/></td>
      <td><a href="<s:url action="AssessmentSchedule" method="viewRoomInfo"><s:param name="assessmentRoomID" value="assessmentRoomID"/></s:url>"><s:text name="Navigation.details"/></a></td>
    </tr>
    </s:iterator>
    </tbody>
  </table>
</s:if>
<s:else>
<div>Trenutno ne postoji raspored</div>
</s:else>

<s:if test="data.studentsFetched>0 && data.studentScheduleCreated>0">
<div>Sumarni raspored studenata po dvoranama</div>
<table>
    <thead>
    <tr>
      <th>Naziv dvorane</th>
      <th>Od studenta</th>
      <th>Do studenta</th>
      <th>Broj studenata</th>
    </tr>
    </thead>
    <tbody>
    <s:iterator value="data.roomList" status="cust_stat">
    <tr class="<s:if test="#cust_stat.odd == true">oddrow</s:if><s:else>evenrow</s:else>">
      <td><s:property value="roomName"/></td>
      <td><s:if test="firstUser!=null"><s:property value="firstUser.lastName"/>, <s:property value="firstUser.firstName"/></s:if><s:else>-</s:else></td>
      <td><s:if test="lastUser!=null"><s:property value="lastUser.lastName"/>, <s:property value="lastUser.firstName"/></s:if><s:else>-</s:else></td>
      <td><s:property value="userNum"/></td>
    </tr>
    </s:iterator>
    </tbody>
  </table>
</s:if>
