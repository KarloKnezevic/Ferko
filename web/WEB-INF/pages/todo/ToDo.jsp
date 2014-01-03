<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div id="body" class="container">

	<s:if test="data.messageLogger.hasMessages()">
		<ul class="msgList">
			<s:iterator value="data.messageLogger.messages">
				<li class="msgType_<s:property value="messageType" />"><s:property value="messageText" /></li>
			</s:iterator>
		</ul>
	</s:if>

	
		<a href="<s:url action="ToDo" method="newTask"></s:url>"><s:text name = "Novi zadatak" /></a> &nbsp;&nbsp;&nbsp;
		<s:if test="data.isAdmin">
			<a href="<s:url action="ToDo" method="newTask"><s:param name="data.allGroups">true</s:param></s:url>"><s:text name = "Novi zadatak (SVE grupe u Ferku!)" /></a> &nbsp;&nbsp;&nbsp;
		</s:if>
		<a href="<s:url action="ToDo" method="newTask"><s:param name="data.newTemplate" value="true"></s:param></s:url>"><s:text name = "Novi predložak" /></a>
		
		<s:hidden name="data.isAdmin" />
	
	<s:hidden name="data.renderBothToDoLists" />
	
	<p>				
	<h3> Moji zadaci </h3>
<s:if test="data.ownList.size>0">	
	<table style="font-size:0.8em">
		<tr><th>Naziv zadatka</th><th>Opis</th><th>Podzadaci</th><th>Rok</th><th>Prioritet</th><th>Zadao/la</th><th>Uredi</th><th>Označi zadatak obavljenim!</th></tr>
		<s:iterator  value="data.ownList" status="stat">
			<s:if test="#stat.index%2==1"><tr style="background-color: #EEEEEE;"></s:if><s:else><tr></s:else>
				<td><s:property value="title"/></td>
				<td><s:property value="description"/></td>
				<td>
					<s:if test="subTasks.size>0">
					<table>
						<tr style="font-size:0.9em"><th>Naziv</th><th>Opis</th><th>Rok</th><th>Prioritet</th><th>Uredi</th><th>Obavljeno!</th></tr>
						<s:iterator  value="subTasks" >
							<tr>
								<td>
									<s:property value="title"/>
								</td>
								<td>
									<s:property value="description"/>
								</td>
								<td>
									<s:property value="deadlineString"/>
								</td>
								<td>
									<s:property value="priority"/>
								</td>
								<s:if test="taskOpen">
									<td style="text-align:center;"> 
										<s:if test="canEdit">
											<a href="<s:url action="ToDo" method="editTask">
													<s:param name="data.subTaskID"><s:property value="id"/></s:param></s:url>">
													<img src="/ferko/img/icons/edit2.png" border="none"/>
											</a>
										</s:if>
									</td>
									<td style="text-align:center;">
										<a href="<s:url action="ToDo" method="closeTask">
												<s:param name="data.taskId"><s:property value="id"/></s:param></s:url>"
												onClick="return confirm('Zadatak obavljen?')">
												<img src="/ferko/img/icons/famtick.png" border="none"/>
										</a>
									</td>									
								</s:if>
								<s:hidden name="id" />
								
							</tr>
						</s:iterator>
					</table>
					</s:if>
					<s:else>
						
							Nema podzadataka
						
					</s:else>
				</td>
				<td><s:property value="deadlineString"/></td>
				
				<td><s:property value="priority"/></td>
				<td><s:property value="ownerFullName"/></td>
				<td style="text-align:center;">
					<s:if test="canEdit">
						<a href="<s:url action="ToDo" method="editTask">
								<s:param name="data.taskId"><s:property value="id"/></s:param></s:url>" >
								<img src="/ferko/img/icons/edit2.png" border="none"/>
						</a>
					</s:if>
				</td>
				<td style="text-align:center;">
				<a id="close1" href="<s:url action="ToDo" method="closeTask">
						<s:param name="data.taskId"><s:property value="id"/></s:param></s:url>" 
							onClick="return confirm('Označiti zadatak kao obavljen?')">
						<img src="/ferko/img/icons/famtick.png" border="none"/>
				</a>
				</td>				
				<s:hidden name="id" />
				<s:hidden name="version" />

			</tr>
			
		</s:iterator>
		
	</table>
</s:if>
<s:else>
	<i>Nema dobivenih zadataka</i><br><br>
</s:else>

	<s:if test="data.renderBothToDoLists">
	<h3> Zadaci zadani drugima</h3>
<s:if test="data.assignedList.size>0">	
	<table style="font-size:0.8em">
		<tr><th>Naziv zadatka</th><th>Opis</th><th>Podzadaci</th><th>Rok</th><th>Status</th><th>Prioritet</th><th>Realizator</th><th>Uredi</th><th>Zatvori</th></tr>
		<s:iterator  value="data.assignedList" status="stat">
			<s:if test="#stat.index%2==1"><tr style="background-color: #EEEEEE;"></s:if><s:else><tr></s:else>
				<td><s:property value="title"/></td>
				<td>						
					<s:if test="groupTask">
						<s:property value="groupTaskDescription"/>
					</s:if>
					<s:else>
						<s:property value="description"/>
					</s:else>			
				</td>
				
					<s:if test="subTasks.size>0">
						<td>
							<table>
								<tr style="font-size:0.9em"><th>Naziv</th><th>Opis</th><th>Rok</th><th>Prioritet</th><th>Status</th><th>Uredi</th><th>Zatvori</th></tr>
								<s:iterator  value="subTasks">
									<tr>
										<td>
											<s:property value="title"/>
										</td>
										<td>
											<s:if test="groupTask&&masterGroupTask">
												<s:property value="groupTaskDescription"/>
											</s:if>
											<s:else>
												<s:property value="description"/>
											</s:else>			
										</td>
										<td>
											<s:property value="deadlineString"/>
										</td>
										<td>
											<s:property value="priority"/>
										</td>
										<td>
											<s:if test="groupTask">
												<s:property value="percentClosed"/>
											</s:if>
											<s:else>
												<s:property value="status"/>
											</s:else>					
										</td>										
										<s:if test="taskOpen">
											<td style="text-align:center;">
												<a href="<s:url action="ToDo" method="editTask">
														<s:param name="data.subTaskID"><s:property value="id"/></s:param></s:url>">
														<img src="/ferko/img/icons/edit2.png" border="none"/>
												</a>
											</td>
											<td style="text-align:center;">
												<a href="<s:url action="ToDo" method="closeTask">
														<s:param name="data.taskId"><s:property value="id"/></s:param></s:url>"
														onClick="return confirm('Zatvoriti zadatak?')">
														<img src="/ferko/img/icons/famcross.png" border="none"/>
												</a>
											</td>
										</s:if>
										<s:hidden name="id" />

									</tr>
								</s:iterator>
							</table>
						</td>
					</s:if>
					<s:else>
						<td>
							Nema podzadataka
						</td>
					</s:else>
				
				<td><s:property value="deadlineString"/></td>
				<td>
					<s:if test="groupTask">
						<s:property value="percentClosed"/>
					</s:if>
					<s:else>
						<s:property value="status"/>
					</s:else>					
				</td>
				<td><s:property value="priority"/></td>
				<td>
					<s:if test="groupTask">
						<s:property value="realizerGroupName"/>
					</s:if>
					<s:else>
						<s:property value="realizerFullName"/>
					</s:else>
				</td>
				<s:if test="taskOpen">
				<td style="text-align:center;">
					<a href="<s:url action="ToDo" method="editTask">
							<s:param name="data.taskId"><s:property value="id"/></s:param></s:url>">
							<img src="/ferko/img/icons/edit2.png" border="none"/>
					</a>
				</td>
					<td style="text-align:center;">
						<a href="<s:url action="ToDo" method="closeTask">
								<s:param name="data.taskId"><s:property value="id"/></s:param></s:url>"
								onClick="return confirm('Zatvoriti zadatak?')">
								<img src="/ferko/img/icons/famcross.png" border="none"/>
						</a>
					</td>					
				</s:if>
				
				<s:hidden name="id" />
				<s:hidden name="version" />
			</tr>
		</s:iterator>
	</table>	
</s:if>
<s:else>
	<i>Nema zadanih zadataka</i><br><br>
</s:else>
	</s:if>
	
	<h3> Predlošci</h3>
<s:if test="data.templateList.size>0">	
	<table style="font-size:0.8em">
		<tr><th>Naziv predloška</th><th>Opis</th><th>Podzadaci</th><th>Rok</th><th>Status</th><th>Prioritet</th><th>Vlasnik</th><th>Novi zadatak</th><th>Uredi</th><th>Obriši</th></tr>
		<s:iterator  value="data.templateList"  status="stat">
			<s:if test="#stat.index%2==1"><tr style="background-color: #EEEEEE;"></s:if><s:else><tr></s:else>
				<td><s:property value="title"/></td>
				<td><s:property value="description"/></td>
				
					<s:if test="subTasks.size>0">
						<td>
							<table>
								<tr style="font-size:0.9em"><th>Naziv</th><th>Opis</th><th>Rok</th><th>Prioritet</th><th>Uredi</th><th>Obriši</th></tr>
								<s:iterator  value="subTasks">
									<tr>
										<td>
											<s:property value="title"/>
										</td>
										<td>
											<s:property value="description"/>
										</td>
										<td>
											<s:property value="deadlineString"/>
										</td>
										<td>
											<s:property value="priority"/>
										</td>
										<td style="text-align:center;">
											<s:if test="canEdit">
												<a href="<s:url action="ToDo" method="editTask">
														<s:param name="data.subTaskID"><s:property value="id"/></s:param></s:url>">
														<img src="/ferko/img/icons/edit2.png" border="none"/>
												</a>
											</s:if>
										</td>
										<td style="text-align:center;">
											<s:if test="canEdit">
												<a href="<s:url action="ToDo" method="deleteTask">
														<s:param name="data.taskId"><s:property value="id"/></s:param></s:url>"
														onClick="return confirm('Obrisati zadatak?')">
														<img src="/ferko/img/icons/famcross.png" border="none"/>
												</a>
											</s:if>
										</td>
										<s:hidden name="id" />

									</tr>
								</s:iterator>
							</table>
						</td>
					</s:if>
					<s:else>
						<td>
							Nema podzadataka
						</td>
					</s:else>
				
				<td><s:property value="deadlineString"/></td>
				<td><s:property value="status"/></td>
				<td><s:property value="priority"/></td>
				<td><s:property value="realizerFullName"/></td>
				
				<td style="text-align:center;">
					<a href="<s:url action="ToDo" method="newTask">
							<s:param name="data.taskToInstantiateID"><s:property value="id"/></s:param></s:url>">
							<img src="/ferko/img/icons/new.png" border="none"/>
					</a>
				</td>
				<td style="text-align:center;">
					<s:if test="canEdit">
						<a href="<s:url action="ToDo" method="editTask">
								<s:param name="data.taskId"><s:property value="id"/></s:param></s:url>">
								<img src="/ferko/img/icons/edit2.png" border="none"/>
						</a>
					</s:if>
				</td>				
				<td style="text-align:center;">
					<s:if test="canEdit">
						<a href="<s:url action="ToDo" method="deleteTask">
								<s:param name="data.taskId"><s:property value="id"/></s:param></s:url>"
								onClick="return confirm('Obrisati predložak?')">
								<img src="/ferko/img/icons/famcross.png" border="none"/>
						</a>
					</s:if>
				</td>

				<s:hidden name="id" />
				<s:hidden name="version" />
			</tr>
		</s:iterator>
	</table>	
</s:if>
<s:else>
	<i>Nema predložaka</i>
</s:else>	

</div>

