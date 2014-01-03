<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div id="body" class="container">

<s:if test="data.messageLogger.hasMessages()">
  <ul>
  <s:iterator value="data.messageLogger.messages">
      <li>[<s:property value="messageType"/>]<s:property value="messageText"/></li>
  </s:iterator>
  </ul>
</s:if>

  <h2>Podaci o prostorijama:</h2>
  <s:form action="SynchronizeRooms" method="post" theme="ferko">
   <s:textarea name="text" rows="20" cols="80" label="Prostorije" />
    <s:submit method="upload" />
  </s:form>

</div>
