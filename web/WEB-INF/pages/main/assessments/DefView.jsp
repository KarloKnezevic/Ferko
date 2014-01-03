<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<s:if test="data==null">
  <div>Nisam dobio niti data objekt.</div>
</s:if>
<s:elseif test="data.assessment.assessmentConfiguration==null">
  <div>Konfiguracija je null! Ovo se nije smjelo dogoditi!</div>
</s:elseif>
<s:else>
  <div>
	<!-- Ova vrsta provjere nema dodatnih podataka. -->
  </div>
</s:else>
