<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<s:url id="externalURL" value="%{data.url}" encode="true">
 <s:param name="username" value="%{data.username}" />
 <s:param name="lastname" value="%{data.lastName}" />
 <s:param name="firstname" value="%{data.firstName}" />
 <s:param name="code" value="%{data.jmbag}" />
 <s:param name="email" value="%{data.email}" />
 <s:param name="courseID" value="%{data.courseID}" />
 <s:param name="ayear" value="%{data.academicYear}" />
 <s:param name="semester" value="%{data.semester}" />
 <s:param name="timestamp" value="%{data.timestamp}" />
 <s:param name="auth" value="%{data.auth}" />
</s:url>
<s:a id="Go" href="%{externalURL}" > Go to Another Application Server page
</s:a>

<hr class="hidden"/>
