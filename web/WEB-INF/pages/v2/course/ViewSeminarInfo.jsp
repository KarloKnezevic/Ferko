<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div id="body" class="container">

	<s:if test="data.messageLogger.hasMessages()">
		<ul>
			<s:iterator value="data.messageLogger.messages">
				<li>[<s:property value="messageType" />] <s:property
					value="messageText" /></li>
			</s:iterator>
		</ul>
	</s:if>

	<h1>Prezentacije seminarskih radova</h1>

	<img src="img/cr2.png" align="left" style="margin-right: 20px; margin-top: 10px;" width="225" height="180">

	<h2>Odabrani seminar</h2>

    <div style="margin-bottom: 20px;">
      <b>Student: </b><s:property value="data.selectedSeminar.student.lastName"/>, <s:property value="data.selectedSeminar.student.firstName"/><br>
      <b>Naziv rada: </b><s:property value="data.selectedSeminar.title"/><br>
      <b>Mentor: </b><s:property value="data.selectedSeminar.mentor.lastName"/>, <s:property value="data.selectedSeminar.mentor.firstName"/><br>
      <b>Mjesto prezentacije: </b><s:property value="data.selectedSeminar.roomText"/><br>
      <b>Vrijeme prezentacije: </b><s:if test="data.selectedSeminar.event!=null"><s:property value="data.selectedSeminar.event.start"/></s:if>
      <s:else>Još nije utvrđeno.</s:else>
      <br>
      <b>Preuzimanje obrasca za bodovanje: </b><a href="https://www.fer.hr/_download/fer_seminar_ocjenjivanje/obrazac-za-pracenje-prezentacija.html" target="_blank" style="text-decoration: none; background-color: #AAAAFF; padding-left: 5px; padding-right: 5px; border: 1px dashed blue;" title="Vanjski link!">vanjski link na FERWeb</a><br>
    </div>
    
	<h2 style="clear: both; margin-top: 20px;">Svi seminari grupe</h2>

    <div>
      <ol>
      <s:iterator value="data.allSeminars"><li>
        <s:property value="student.lastName"/>, <s:property value="student.firstName"/>: <i><s:property value="title"/></i>. <span style="font-size: 0.7em;">
		Mentor: <s:property value="mentor.lastName"/>, <s:property value="mentor.firstName"/><s:if test="event!=null">. Vrijeme: <s:property value="data.formatDateTime(event.start)"/>.</s:if></span>
      </li></s:iterator>
      </ol>
    </div>

</div>
