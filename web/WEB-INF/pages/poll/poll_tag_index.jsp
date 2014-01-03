<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<h1 class="pageh">Administracija</h1>
<div class="content withsidecontent">
	<div class="sidecontent">
		<h2>Administracija</h2>
		<ul class="sidenav">
			<li>
				<a href="<s:url action="CreatePollTag" method="create" />" class="action-add">Novi tag</a>
			</li>
		</ul>
	</div>

	<div class="maincontent">
	<div class="inner-padding">
	<h2>Tagovi za ankete</h2>
	<table>
	<tr><th>Naziv</th> <th>Kratki naziv</th> <th>Uredi</th> <th>Izbriši</th></tr>
	<s:iterator value="data.pollTags">
		<tr>
			<td><s:property value="name" /></td>
			<td><s:property value="shortName" /></td>
			<td>
				<a href="<s:url action="UpdatePollTag"><s:param name="id"><s:property value="id"/></s:param></s:url>">
					Uredi
				</a>
			</td>
			<td>
				<a href="<s:url action="DeletePollTag"><s:param name="id"><s:property value="id"/></s:param></s:url>">
					Izbriši
				</a>
			</td>
		</tr>
	</s:iterator>
	</table>

	</div>
	</div>
</div>
