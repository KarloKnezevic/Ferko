<%@ page contentType="text/html; charset=UTF-8" %><%@ taglib prefix="s" uri="/struts-tags" %>
[
<s:iterator value="data.users" status="stat">
  ["<s:property value="lastName"/>, <s:property value="firstName"/> (<s:property value="jmbag"/>)", "<s:property value="id"/>"]<s:if test="!#stat.last">,</s:if> 
</s:iterator>
]
