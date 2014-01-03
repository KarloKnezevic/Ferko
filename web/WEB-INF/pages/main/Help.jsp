<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div id="body" class="container">

<div class="helpPageContent">
<s:if test="helpKey != null">
	<s:include value="/WEB-INF/pages/help/HLP%{helpKey}.jsp"></s:include>
</s:if>
<s:else>
	<s:include value="/WEB-INF/pages/help/HLP000001.jsp"></s:include>
</s:else>
</div>

<div>
  <a href="javascript:window.close();">Zatvori</a>
</div>

</div>
