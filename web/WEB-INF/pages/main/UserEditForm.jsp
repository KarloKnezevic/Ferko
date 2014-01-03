<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div id="body" class="container">

<script>
<!--
  function pitaj() {
	  return confirm("Jeste li sigurni?");
  }
//-->
</script>

	<s:if test="data.messageLogger.hasMessages()">
		<ul class="msgList">
			<s:iterator value="data.messageLogger.messages">
				<li class="msgType_<s:property value="messageType" />"><s:property value="messageText" /></li>
			</s:iterator>
		</ul>
	</s:if>

	<h1>Uređivanje podataka o korisniku</h1>
	<s:form action="User" method="get" theme="ferko">
		<s:if test="data.canViewAll">
		<s:textfield name="bean.jmbag"  label="%{getText('forms.jmbag')}" />
		<s:textfield name="bean.firstName"  label="%{getText('forms.firstName')}" />
		<s:textfield name="bean.lastName"  label="%{getText('forms.lastName')}" />
		<s:textfield name="bean.username"  label="%{getText('forms.username')}" />
		<s:textfield name="bean.authUsername"  label="%{getText('forms.authUsername')}" />
		</s:if>

		<s:password name="bean.password"  label="%{getText('forms.password')}" />
		<s:password name="bean.doublePassword" label="%{getText('forms.doublePassword')}" />
		<s:textfield name="bean.email" label="%{getText('forms.email')}" />
		<s:checkbox name="bean.preferences.mail" value="bean.preferences.mail" label="%{getText('forms.act.email')}"></s:checkbox>
		<s:if test="data.canViewAll">
			<s:select name="bean.authTypeID" list="data.availableAuthTypes" listKey="id" listValue="description" label="%{getText('forms.authType')}" />
			<s:checkboxlist list="data.availableRoles" listKey="name" listValue="name" name="bean.roles" label="%{getText('forms.roles')}" />
			<s:checkbox name="bean.dataValid" label="%{getText('forms.dataValid')}" />
			<s:checkbox name="bean.locked" label="%{getText('forms.locked')}" />
		</s:if>
		<s:hidden name="bean.id" />
		<s:submit method="update"></s:submit>
	</s:form>

    <h2>Resetiranje vanjskog ključa</h2>

    <p>Sustav Ferko određene informacije stavlja dostupnima drugim programima. Na ovaj način omogućeno
       Vam je, primjerice, podešavanje sinkronizacije Vašeg Google kalendara i Vašeg osobnog kalendara koji se nalazi u Ferku. Informacije
       koje Ferko na ovaj način otvara su probrane, i drugi programi u načelu ne mogu ništa mijenjati, već imaju samo dozvolu
       čitanja. Pa ipak, kako bi se spriječilo da bilo tko može doći do Vaših podataka, vanjske sustave morate konfigurirati tako da Ferku
       šalju Vaš vanjski ključ (Google Calendar taj ključ dobije kao dio URL-a koji kopirate iz Ferka). Ukoliko mislite da je netko drugi
       doznao Vaš vanjski ključ, ovdje možete zatražiti resetiranje (tj. novo generiranje ključa). Nakon te akcije svi sustavi koje ste podesili
       da pristupaju Ferku više neće od Ferka dobivati informacije, tako dugo dok ih ponovno ne konfigurirate novim ključem. </p>
    <p>Ako ste sigurni da želite napraviti reset ključa, molim kliknite 
       <a onclick="return pitaj();" href="<s:url action="User" method="resetExternalID"><s:param name="bean.id" value="bean.id"/></s:url>"><s:text name="Navigation.resetExternalID" /></a>.
    </p>
</div>
