<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<h2><s:text name="Navigation.marketPlaces" /></h2>

<s:if test="data.messageLogger.hasMessages()">
	<ul class="msgList">
		<s:iterator value="data.messageLogger.messages">
			<li class="msgType_<s:property value="messageType" />"><s:property value="messageText" /></li>
		</s:iterator>
	</ul>
</s:if>

<p>U burzi grupa <a target="_jcms_help" href="<s:url action="Help"><s:param name="helpKey">000026</s:param></s:url>" onclick="blur();"><img src="img/icons/help.png"></a>
 možete promijeniti svoju grupu. Zamjenu je moguće napraviti na više načina. Ukoliko znate prijatelja s kojim biste se zamijenili, možete mu poslati direktnu ponudu. Ponekad ćete moći direktno ući u neku grupu, no najčešće ćete slati grupne ponude. Netko iz druge grupe će moći prihvatiti Vašu grupnu ponudu, a Vi možete tražiti potvrdu prije zamjene.</p>

<s:property value="data.parent.group" />
<s:if test="!data.active">
	<ul class="msgList"><li>Burza je zatvorena.</li></ul>
</s:if>
<s:else>
<s:iterator value="data.userState.allStates">
<p class="MPMyGroup">Vaša grupa je <strong><s:property value="myUserGroup.group.name"/><s:set name="mojaGrupaID" value="myUserGroup.group.id"/><s:set name="mojaUserGrupaID" value="myUserGroup.id"/></strong></p>

<div class="mpSection">
	<s:if test="active">
		<h3>Moje grupne ponude (bez potvrde)</h3>
		<s:if test="myGroupOffersNoAck.size == 0">
		<p class="emptyMsg">Nema ih</p>
		</s:if>
		<s:else>
		<ul>
			<s:iterator value="myGroupOffersNoAck">
				<li><s:property value="toGroup.name"/> <s:if test="expired">(istekla)</s:if>
					<a class="deleteLink" title="<s:text name="Navigation.delete"/>" href="<s:url action="MPDeleteOffer"><s:param name="bean.courseInstanceID" value="data.courseInstance.id"/><s:param name="bean.parentID" value="data.parent.id"/><s:param name="bean.offerID" value="id"/><s:param name="bean.myUserGroupID" value="%{#mojaUserGrupaID}"/><s:param name="bean.myGroupID" value="%{#mojaGrupaID}"/></s:url>"><s:text name="Navigation.delete"/></a>
				</li>
			</s:iterator>
		</ul>
		</s:else>

		<h3>Moje grupne ponude (sa potvrdom)</h3>
		<s:if test="myGroupOffersWithAck.size == 0">
		<p class="emptyMsg">Nema ih</p>
		</s:if>
		<s:else>
		<ul>
			<s:iterator value="myGroupOffersWithAck">
				<li><s:property value="toGroup.name"/> <s:if test="expired">(istekla)</s:if> 
					<a class="deleteLink" title="<s:text name="Navigation.delete"/>" href="<s:url action="MPDeleteOffer"><s:param name="bean.courseInstanceID" value="data.courseInstance.id"/><s:param name="bean.parentID" value="data.parent.id"/><s:param name="bean.offerID" value="id"/><s:param name="bean.myUserGroupID" value="%{#mojaUserGrupaID}"/><s:param name="bean.myGroupID" value="%{#mojaGrupaID}"/></s:url>"><s:text name="Navigation.delete"/></a>
				</li>
			</s:iterator>
		</ul>
		</s:else>

		<h3>Moje direktne ponude</h3>
		<s:if test="myDirectOffers.size == 0">
		<p class="emptyMsg">Nema ih</p>
		</s:if>
		<s:else>
		<ul>
			<s:iterator value="myDirectOffers">
				<li><s:property value="toGroup.name"/>, korisniku <s:property value="toUser.username"/> <s:if test="expired">(istekla)</s:if> 
					<a class="deleteLink" title="<s:text name="Navigation.delete"/>" href="<s:url action="MPDeleteOffer"><s:param name="bean.courseInstanceID" value="data.courseInstance.id"/><s:param name="bean.parentID" value="data.parent.id"/><s:param name="bean.offerID" value="id"/><s:param name="bean.myUserGroupID" value="%{#mojaUserGrupaID}"/><s:param name="bean.myGroupID" value="%{#mojaGrupaID}"/></s:url>"><s:text name="Navigation.delete"/></a>
				</li>
			</s:iterator>
		</ul>
		</s:else>

		<h3>Moji poslani odgovori na ponude</h3>
		<s:if test="myAckReqForGroupOffers.size == 0">
		<p class="emptyMsg">Nema ih</p>
		</s:if>
		<s:else>
		<ul>
			<s:iterator value="myAckReqForGroupOffers">
				<li><s:property value="toGroup.name"/>, korisniku <s:property value="toUser.username"/> <s:if test="expired">(istekla)</s:if> 
					<a class="deleteLink" title="<s:text name="Navigation.delete"/>" href="<s:url action="MPDeleteOffer"><s:param name="bean.courseInstanceID" value="data.courseInstance.id"/><s:param name="bean.parentID" value="data.parent.id"/><s:param name="bean.offerID" value="id"/><s:param name="bean.myUserGroupID" value="%{#mojaUserGrupaID}"/><s:param name="bean.myGroupID" value="%{#mojaGrupaID}"/></s:url>"><s:text name="Navigation.delete"/></a>
				</li>
			</s:iterator>
		</ul>
		</s:else>
	</div>
	<div class="mpSection">
		<h3>Pristigle grupne ponude (bez potvrde)</h3>
		<s:if test="groupOffersNoAckForMe.size == 0">
		<p class="emptyMsg">Nema ih</p>
		</s:if>
		<s:else>
		<ul>
			<s:iterator value="groupOffersNoAckForMe">
				<li><s:property value="fromGroup.name"/>, od korisnika <s:property value="fromUser.username"/> <s:if test="fromTag!=null">(<s:property value="fromTag"/>)</s:if> <s:if test="expired">(istekla)</s:if> 
					<a class="acceptLink" title="<s:text name="Navigation.exchangeGroups"/>" href="<s:url action="MPAcceptOffer" method="acceptGroupOffer"><s:param name="bean.courseInstanceID" value="data.courseInstance.id"/><s:param name="bean.parentID" value="data.parent.id"/><s:param name="bean.offerID" value="id"/><s:param name="bean.myUserGroupID" value="%{#mojaUserGrupaID}"/><s:param name="bean.myGroupID" value="%{#mojaGrupaID}"/></s:url>"><s:text name="Navigation.exchangeGroups"/></a>
				</li>
			</s:iterator>
		</ul>
		</s:else>

		<h3>Pristigle grupne ponude (sa potvrdom)</h3>
		<s:if test="groupOffersWithAckForMe.size == 0">
		<p class="emptyMsg">Nema ih</p>
		</s:if>
		<s:else>
		<ul>
			<s:iterator value="groupOffersWithAckForMe">
				<li><s:property value="fromGroup.name"/>, od korisnika <s:property value="fromUser.username"/> <s:if test="fromTag!=null">{<s:property value="fromTag"/>}</s:if> <s:if test="expired">(istekla)</s:if>  
					<a class="acceptLink" title="<s:text name="Navigation.exchangeGroups"/>" href="<s:url action="MPAcceptOffer" method="sendApprovalRequest"><s:param name="bean.courseInstanceID" value="data.courseInstance.id"/><s:param name="bean.parentID" value="data.parent.id"/><s:param name="bean.offerID" value="id"/><s:param name="bean.myUserGroupID" value="%{#mojaUserGrupaID}"/><s:param name="bean.myGroupID" value="%{#mojaGrupaID}"/></s:url>"><s:text name="Navigation.sendExchangeRequest"/></a>
				</li>
			</s:iterator>
		</ul>
		</s:else>

		<h3>Pristigle direktne ponude</h3>
		<s:if test="directOffersForMe.size == 0">
		<p class="emptyMsg">Nema ih</p>
		</s:if>
		<s:else>
		<ul>
			<s:iterator value="directOffersForMe">
				<li><s:property value="fromGroup.name"/>, od korisnika <s:property value="fromUser.username"/> <s:if test="fromTag!=null">{<s:property value="fromTag"/>}</s:if> <s:if test="expired">(istekla)</s:if> 
					<a class="acceptLink" title="<s:text name="Navigation.exchangeGroups"/>" href="<s:url action="MPAcceptOffer" method="acceptDirectOffer"><s:param name="bean.courseInstanceID" value="data.courseInstance.id"/><s:param name="bean.parentID" value="data.parent.id"/><s:param name="bean.offerID" value="id"/><s:param name="bean.myUserGroupID" value="%{#mojaUserGrupaID}"/><s:param name="bean.myGroupID" value="%{#mojaGrupaID}"/></s:url>"><s:text name="Navigation.exchangeGroups"/></a>
				</li>
			</s:iterator>
		</ul>
		</s:else>

		<h3>Pristigli zahtjevi za zamjenom (potvrde)</h3>
		<s:if test="groupOfferAcksForMe.size == 0">
		<p class="emptyMsg">Nema ih</p>
		</s:if>
		<s:else>
		<ul>
			<s:iterator value="groupOfferAcksForMe">
				<li><s:property value="fromGroup.name"/>, od korisnika <s:property value="fromUser.username"/> <s:if test="fromTag!=null">{<s:property value="fromTag"/>}</s:if> <s:if test="expired">(istekla)</s:if>  
					<a class="acceptLink" title="<s:text name="Navigation.exchangeGroups"/>" href="<s:url action="MPAcceptOffer" method="acceptApproval"><s:param name="bean.courseInstanceID" value="data.courseInstance.id"/><s:param name="bean.parentID" value="data.parent.id"/><s:param name="bean.offerID" value="id"/><s:param name="bean.myUserGroupID" value="%{#mojaUserGrupaID}"/><s:param name="bean.myGroupID" value="%{#mojaGrupaID}"/></s:url>"><s:text name="Navigation.exchangeGroups"/></a>
				</li>
			</s:iterator>
		</ul>
		</s:else>
	</div>

	<hr class="hidden" />

	<div class="mpMainSection">
		<s:if test="availForMove.size != 0">
			<h3>Direktni ulazak u grupu</h3>
			<s:form action="MPDirectMove" theme="ferko" method="post">
				<s:select list="availForMove" listKey="id" listValue="name" name="bean.groupID" required="true" label="Grupa u koju želim ući" />
				<s:hidden name="bean.courseInstanceID" value="%{data.courseInstance.id}"></s:hidden>
				<s:hidden name="bean.parentID" value="%{data.parent.id}"></s:hidden>
				<s:hidden name="bean.myUserGroupID" value="%{#mojaUserGrupaID}"></s:hidden>
				<s:hidden name="bean.myGroupID" value="%{#mojaGrupaID}"></s:hidden>
				<s:submit />
			</s:form>
		</s:if>

		<hr class="hidden" />

		<h3>Slanje grupne ponude</h3>
		<s:if test="availForGroupOffers.size != 0">
		<s:form action="MPSendGroupOffer" theme="ferko" method="post">
			<s:select list="availForGroupOffers" listKey="id" listValue="name" name="bean.groupID" required="true" label="Izaberi grupu" />
			<s:checkbox label="Zahtijevaj moju potvrdu prije zamjene" name="bean.requireApr" value="false" fieldValue="true"/>
			<s:textfield name="bean.reason" label="Razlog slanja"/>
			<s:textfield name="bean.validUntil" label="Ponuda vrijedi do"/>
			<li class="fieldComment">(Unesite oblika: <em>yyyy-MM-dd HH:mm:ss</em> npr. 2008-09-23 17:30:00 ili ostavite prazno)</li>
			<s:hidden name="bean.courseInstanceID" value="%{data.courseInstance.id}"></s:hidden>
			<s:hidden name="bean.parentID" value="%{data.parent.id}"></s:hidden>
			<s:hidden name="bean.myUserGroupID" value="%{#mojaUserGrupaID}"></s:hidden>
			<s:hidden name="bean.myGroupID" value="%{#mojaGrupaID}"></s:hidden>
			<s:submit />
		</s:form>
		</s:if><s:else>
			<p class="emptyMsg">Nema grupa u koje možete poslati grupnu ponudu.</p>
		</s:else>
		<hr class="hidden" />

		<h3>Slanje direktne ponude korisniku</h3>
		<s:if test="availForDirectOffers.size != 0">
		<s:form action="MPSendDirectOffer" theme="ferko" method="post">
			<s:textfield name="bean.toUsername" label="Korisničko ime korisnika"/>
			<s:select list="availForDirectOffers" listKey="id" listValue="name" name="bean.groupID" required="true" label="Grupa korisnika" />
			<s:textfield name="bean.reason" label="Razlog slanja"/>
			<s:textfield name="bean.validUntil" label="Ponuda vrijedi do"/>
			<li class="fieldComment">(Unesite oblika: <em>yyyy-MM-dd HH:mm:ss</em> npr. 2008-09-23 17:30:00 ili ostavite prazno)</li>
			<s:hidden name="bean.courseInstanceID" value="%{data.courseInstance.id}" />
			<s:hidden name="bean.parentID" value="%{data.parent.id}" />
			<s:hidden name="bean.myUserGroupID" value="%{#mojaUserGrupaID}" />
			<s:hidden name="bean.myGroupID" value="%{#mojaGrupaID}" />
			<s:submit />
		</s:form>
		</s:if><s:else>
			<p class="emptyMsg">Nema grupa u koje možete poslati direktnu ponudu.</p>
		</s:else>

	</div>
	</s:if><s:else>
		Ovu grupu ne možete mijenjati.
	</s:else>
</s:iterator>

</s:else>
