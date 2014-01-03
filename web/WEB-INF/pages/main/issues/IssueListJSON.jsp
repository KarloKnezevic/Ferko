<%@ page contentType="text/html; charset=UTF-8" %><%@ taglib prefix="s" uri="/struts-tags" %>
{"ResultSet":{ 
	    "totalResultsAvailable":<s:property value="data.messageCount"/>, 
	    "totalResultsReturned":<s:property value="data.messageCount"/>, 
		"firstResultPosition":1,
		"Result":[
		
<s:iterator value="data.messageBeans" status="stat">
  		{
  			"msgid" : <s:property value="ID"/>, 
  			"title" : "<s:property value="messageName"/>",
  			"topic" : "<s:property value="topicName"/>",
  			"owner" : "<s:property value="ownerName"/>",
  			"creationDate" : "<s:property value="creationDate"/>",
  			"lastModificationDate" : "<s:property value="lastModificationDate"/>",
  			"status" : "<s:property value="messageStatus"/>",
  			"public" : "<s:property value="publicity"/>",
  			"colorIndication" : "<s:property value="colorIndication"/>"
  		}
  		<s:if test="!#stat.last">,</s:if> 
</s:iterator>		
	
		]
		}
}
		


