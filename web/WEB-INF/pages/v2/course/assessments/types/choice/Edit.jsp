<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%@ taglib prefix="jcms" uri="/jcms-custom-tags" %>

<s:if test="data.messageLogger.hasMessages()">
	<ul class="msgList">
		<s:iterator value="data.messageLogger.messages">
			<li class="msgType_<s:property value="messageType" />"><s:property value="messageText" /></li>
		</s:iterator>
	</ul>
</s:if>

  <!-- Uključi TODO ako je definiran broj zadataka -->
  <s:if test="data.assessment.assessmentConfiguration.problemsNum != 0">
    <s:include value="/WEB-INF/pages/main/assessments/ChoiceConfTodo.jsp"></s:include>
  </s:if>


<s:if test="!navigation.getNavigationBar('e1').isEmpty()">
<div style="width: 200px; float: left;">
    <s:iterator value="navigation.getNavigationBar('e1')">
      <s:iterator value="items" id="navitem">
        <s:if test="kind.equals('action')">
<p class="strongLink">
<s:if test="'execute'.equals(actionMethod)"><s:url action="%{actionName}" id="url1" escapeAmp="false"><jcms:navParams item="%{#navitem}" /></s:url></s:if>
<s:else><s:url action="%{actionName}" method="%{actionMethod}" id="url1" escapeAmp="false"><jcms:navParams item="%{#navitem}" /></s:url></s:else>
<a href="<s:property value="#url1"/>"><s:if test="titleIsKey"><s:text name="%{titleKey}"/></s:if><s:else><s:property value="titleKey"/></s:else></a>
</p>
        </s:if><s:elseif test="kind.equals('text')">
<p class="strongLink"><s:if test="titleIsKey"><s:text name="%{titleKey}"/></s:if><s:else><s:property value="titleKey"/></s:else></p>
        </s:elseif>
      </s:iterator>
    </s:iterator>
</div>
</s:if>

<div style="margin-left: 240px;">
  <s:if test="data.selectedView.equals('default')">
	  <div>
	    <s:form action="AdminSetDetailedChoiceConf" theme="ferko">
	      <s:hidden name="assessmentID" value="%{data.assessment.id}"></s:hidden>
	      <s:hidden name="selectedView" value="%{data.selectedView}"></s:hidden>
	      <s:textfield name="data.problemsNum" label="%{getText('forms.problemsNum')}"></s:textfield>
	      <s:textfield name="data.groupsNum" label="%{getText('forms.groupsNum')}"></s:textfield>
	      <s:checkbox name="data.personalizedGroups" label="%{getText('forms.personalizedGroups')}"/>
	      <s:textfield name="data.answersNumber" label="%{getText('forms.answersNumber')}"></s:textfield>
	      <s:checkbox name="data.errorColumn" label="%{getText('forms.errorColumn')}"/>
		  <s:textfield name="data.errorColumnText" label="%{getText('forms.errorColumnText')}"></s:textfield>
	      <s:submit method="setBasicProperties" value="%{getText('forms.general.update')}" />
	    </s:form>
	  </div>
  </s:if>
  <s:if test="data.selectedView.equals('forms')">
	  <div>
        <s:if test="data.assessment.assessmentConfiguration.problemsNum != 0">
		<ul>
		  <li>
		    <a href="<s:url action="AssessmentSchedule" method="downloadSheets"><s:param name="assessmentID" value="%{data.assessment.id}"/></s:url>"><s:text name="Navigation.assessmentAnswerSheets"/></a>
		  </li>
		</ul>
		Ukoliko ste raspored napravili na nekoj drugoj provjeri (primjerice, kod višedjelnih provjera), odaberite provjeru o kojoj se radi i zatražite obrasce.<br><br>
		<s:form action="AssessmentSchedule" method="post" theme="simple">
			<s:hidden name="assessmentID" value="%{data.assessment.id}" />
			<s:select list="data.availableAssessments" listKey="id" listValue="name" name="data.scheduleAssessmentID" value="data.assessment.id"></s:select>
			<s:submit method="downloadSheets" value="%{getText('Navigation.assessmentAnswerSheets')}"></s:submit>
		</s:form>
        </s:if><s:else>
			<s:text name="Error.defineParametersFirst" />
        </s:else>
	  </div>
  </s:if>
  <s:if test="data.selectedView.equals('scoring')">
	  <div>
        <s:if test="data.assessment.assessmentConfiguration.problemsNum != 0">

		    <div>
		    	<strong>Format unosa:</strong>
		    	<table>
		    		<tr>
		    			<td>Grupa</td>
						<td>Zadatak</td>
		    			<td>Bodovi za točan odgovor</td>
		    			<td>Bodovi za netočan odgovor</td>
		    			<td>Bodovi za neodgovoreno pitanje</td>
		    		</tr>
		    	</table>
		    	<div>
			    	<em>
			    		Bodovi se moraju definirati za svaku grupu i svaki zadatak (ukupno: broj zadataka * broj grupa unosa).<br />
			    		Broj bodova za točan zadatak ne smije biti nula (0).<br />
			    		Elementi su međusobno odvojeni tabom.
			    	</em>
		    	</div>
		    	Primjer:
		    	<table>
		    		<tr>
		    			<td>A</td>
						<td>1</td>
		    			<td>1</td>
		    			<td>-0.25</td>
		    			<td>0</td>
		    		</tr>
		    		<tr>
		    			<td>A</td>
						<td>2</td>
		    			<td>1</td>
		    			<td>-0.25</td>
		    			<td>-0.5</td>
		    		</tr>
		    		<tr>
		    			<td>...</td>
						<td>&nbsp;</td>
		    			<td>&nbsp;</td>
		    			<td>&nbsp;</td>
		    			<td>&nbsp;</td>
		    		</tr>
		    		<tr>
		    			<td>B</td>
						<td>1</td>
		    			<td>3</td>
		    			<td>0</td>
		    			<td>0</td>
		    		</tr>
		    		<tr>
		    			<td>...</td>
						<td>&nbsp;</td>
		    			<td>&nbsp;</td>
		    			<td>&nbsp;</td>
		    			<td>&nbsp;</td>
		    		</tr>
		    	</table>
		    </div>
		    <s:if test="data.assessment.assessmentConfiguration.scoreCorrect == 0 && (data.assessment.assessmentConfiguration.detailTaskScores == null || data.assessment.assessmentConfiguration.detailTaskScores.equals(''))">
		  		<div><strong><s:property value="%{getText('forms.scoreValuesNotSet')}" /></strong></div>
		  	</s:if>
		  	<s:if test="data.assessment.assessmentConfiguration.detailTaskScores != null && !data.assessment.assessmentConfiguration.detailTaskScores.equals('')">
		  		<div><strong><s:property value="%{getText('forms.useingDetailedTaskScores')}" /></strong></div>
		  	</s:if>
		    <s:form action="AdminSetDetailedChoiceConf" method="post" theme="ferko">
		      <s:hidden name="assessmentID" value="%{data.assessment.id}"></s:hidden>
			  <s:hidden name="selectedView" value="%{data.selectedView}"></s:hidden>
		      <s:textfield name="data.scoreCorrect" label="%{getText('forms.scoreCorrect')}"></s:textfield>
		      <s:textfield name="data.scoreIncorrect" label="%{getText('forms.scoreIncorrect')}"></s:textfield>
		      <s:textfield name="data.scoreUnanswered" label="%{getText('forms.scoreUnanswered')}"></s:textfield>
		      <s:textarea rows="5" cols="50" name="data.detailTaskScores" label="%{getText('forms.detailTaskScores')}"></s:textarea>
			  <s:submit method="setDetailedScore" value="%{getText('forms.general.update')}" />
		    </s:form>
		    <h3><s:property value="%{getText('forms.inputFromFile')}" /></h3>
		    <div>
		    	<s:form action="AdminSetDetailedChoiceConf" method="post" enctype="multipart/form-data" theme="ferko">
					<s:hidden name="assessmentID" value="%{data.assessment.id}"></s:hidden>
					<s:hidden name="selectedView" value="%{data.selectedView}"></s:hidden>
					<s:file name="data.dataFile" label="%{getText('forms.file')}"></s:file>
					<s:submit method="uploadDetailedScore" value="%{getText('forms.general.update')}" />
		      	</s:form>
		    </div>

        </s:if><s:else>
			<s:text name="Error.defineParametersFirst" />
        </s:else>
	  </div>
  </s:if>
  <s:if test="data.selectedView.equals('answers')">
	  <div>
        <s:if test="data.assessment.assessmentConfiguration.problemsNum != 0">
		    <div>
		    	<strong>Format unosa:</strong><br />
			 	<table>
		    		<tr>
		    			<td>Grupa 1</td>
		    			<td>Odgovor za 1. zad.</td>
		    			<td>Odgovor za 2. zad.</td>
		    			<td>Odgovor za 3. zad.</td>
		    			<td>...</td>
		    			<td>Odgovor za N. zad.</td>
		    		</tr>
		    	</table>
			 	Dopušteno je umjesto: <em>Odgovor za k-ti zad.</em> navesti više odgovora odvojenih zarezom (više točnih odgovora po pitanju), npr. A,C,D
			</div>
		    <s:form action="AdminSetDetailedChoiceConf" method="post" theme="ferko">
		      <s:hidden name="assessmentID" value="%{data.assessment.id}"></s:hidden>
					<s:hidden name="selectedView" value="%{data.selectedView}"></s:hidden>
		      <s:textarea rows="7" cols="80" name="data.correctAnswers" label="%{getText('forms.correctAnswers')}"></s:textarea>
		      <s:submit method="setCorrectAnswers" value="%{getText('forms.general.update')}" />
		    </s:form>
		    <h3><s:property value="%{getText('forms.inputFromFile')}" /></h3>
		    <div>
		    	<s:form action="AdminSetDetailedChoiceConf" method="post" enctype="multipart/form-data" theme="ferko">
			        <s:hidden name="assessmentID" value="%{data.assessment.id}"></s:hidden>
					<s:hidden name="selectedView" value="%{data.selectedView}"></s:hidden>
			        <s:file name="data.dataFile" label="%{getText('forms.file')}"></s:file>
			        <s:submit method="uploadCorrectAnswers" value="%{getText('forms.general.update')}" />
		      	</s:form>
		    </div>
        </s:if><s:else>
			<s:text name="Error.defineParametersFirst" />
        </s:else>
	  </div>
  </s:if>
  <s:if test="data.selectedView.equals('groups')">
	  <div>
        <s:if test="data.assessment.assessmentConfiguration.problemsNum != 0">
		    <div>
		    	<strong>Format unosa:</strong><br />
		    	<strong>Unos intervala (ima viši prioritet naspram tabličnog unosa):</strong><br />
		    	Početak intervala: BROJ ili SLOVO<BR /> 
		    	Kraj intervala: BROJ ili SLOVO<BR />
		    	Generira se lista znakova (brojeva ili slova) od početka intervala do kraja intervala.<br /> 
		    	<br />
		    	<table>
		    		<tr>
		    			<td>oznaka 1. grupe</td>
		    			<td>oznaka 2. grupe</td>
		    			<td>oznaka 3. grupe</td>
		    			<td>...</td>
		    			<td>oznaka n. grupe</td>
		    		</tr>
		    	</table>
		    	Oznake su međusobno odvojene tabovima.
		    </div>
		    <s:form action="AdminSetDetailedChoiceConf" method="post" theme="ferko">
		      <s:hidden name="assessmentID" value="%{data.assessment.id}"></s:hidden>
			  <s:hidden name="selectedView" value="%{data.selectedView}"></s:hidden>
		      <s:textfield name="data.intervalStart" label="%{getText('forms.intervalStart')}" value=""></s:textfield>
		      <s:textfield name="data.intervalEnd" label="%{getText('forms.intervalEnd')}" value=""></s:textfield>
		      <s:textarea rows="5" cols="80" name="data.groupsLabels" label="%{getText('forms.groupsLabels')}"></s:textarea>
			  <s:submit method="setGroupLabels" value="%{getText('forms.general.update')}" />
		    </s:form>
		    <h3><s:property value="%{getText('forms.inputFromFile')}" /></h3>
		    <div>
		    	<s:form action="AdminSetDetailedChoiceConf" method="post" enctype="multipart/form-data" theme="ferko">
			        <s:hidden name="assessmentID" value="%{data.assessment.id}"></s:hidden>
					<s:hidden name="selectedView" value="%{data.selectedView}"></s:hidden>
			        <s:file name="data.dataFile" label="%{getText('forms.file')}"></s:file>
			        <s:submit method="uploadGroupLabels" value="%{getText('forms.general.update')}" />
		      	</s:form>
		    </div>
        </s:if><s:else>
			<s:text name="Error.defineParametersFirst" />
        </s:else>
	  </div>
  </s:if>
  <s:if test="data.selectedView.equals('plabels')">
	  <div>
        <s:if test="data.assessment.assessmentConfiguration.problemsNum != 0">
		    <div>
		    	<strong>Format unosa:</strong><br />
		    	<strong>Unos intervala (ima viši prioritet naspram tabličnog unosa):</strong><br />
		    	Početak intervala: BROJ ili SLOVO<BR /> 
		    	Kraj intervala: BROJ ili SLOVO<BR />
		    	Generira se lista znakova (brojeva ili slova) od početka intervala do kraja intervala.<br /> 
		    	<br />
		    	<strong>Tablični unos:</strong>
		    	<table>
		    		<tr>
		    			<td>oznaka 1. zadatka</td>
		    			<td>oznaka 2. zadatka</td>
		    			<td>oznaka 3. zadatka</td>
		    			<td>...</td>
		    			<td>oznaka n. zadatka</td>
		    		</tr>
		    	</table>
		    	Oznake su međusobno odvojene tabovima.<br />
		    </div>
		    <s:form action="AdminSetDetailedChoiceConf" method="post" theme="ferko">
		      <s:hidden name="assessmentID" value="%{data.assessment.id}"></s:hidden>
			  <s:hidden name="selectedView" value="%{data.selectedView}"></s:hidden>
		      <s:textfield name="data.intervalStart" label="%{getText('forms.intervalStart')}" value=""></s:textfield>
		      <s:textfield name="data.intervalEnd" label="%{getText('forms.intervalEnd')}" value=""></s:textfield>
		      <s:textarea rows="5" cols="80" name="data.problemsLabels" label="%{getText('forms.problemsLabels')}"></s:textarea>
			  <s:submit method="setProblemLabels" value="%{getText('forms.general.update')}" />
		    </s:form>
		    <h3><s:property value="%{getText('forms.inputFromFile')}" /></h3>
		    <div>
		    	<s:form action="AdminSetDetailedChoiceConf" method="post" enctype="multipart/form-data" theme="ferko">
			        <s:hidden name="assessmentID" value="%{data.assessment.id}"></s:hidden>
					<s:hidden name="selectedView" value="%{data.selectedView}"></s:hidden>
			        <s:file name="data.dataFile" label="%{getText('forms.file')}"></s:file>
			        <s:submit method="uploadProblemLabels" value="%{getText('forms.general.update')}" />
		      	</s:form>
		    </div>
        </s:if><s:else>
			<s:text name="Error.defineParametersFirst" />
        </s:else>
	  </div>
  </s:if>
  <s:if test="data.selectedView.equals('pmapping')">
	  <div>
        <s:if test="data.assessment.assessmentConfiguration.problemsNum != 0">
		    <div>
		    	<strong>Format unosa:</strong><br />
		    	<table>
		    		<tr>
		    			<td>Grupa 1</td>
		    			<td>Oznaka 1. zadatka</td>
		    			<td>Tip zadatka</td>
		    			<td>Verzija tipa zadatka</td>
		    		</tr>
		    		<tr>
		    			<td>Grupa 1</td>
		    			<td>Oznaka 2. zadatka</td>
		    			<td>Tip zadatka</td>
		    			<td>Verzija tipa zadatka</td>
		    		</tr>
		    		<tr>
		    			<td>Grupa 1</td>
		    			<td>Oznaka zadnjeg zadatka</td>
		    			<td>Tip zadatka</td>
		    			<td>Verzija tipa zadatka</td>
		    		</tr>
		    		<tr>
		    			<td>Grupa 2</td>
		    			<td>Oznaka 1. zadatka</td>
		    			<td>Tip zadatka</td>
		    			<td>Verzija tipa zadatka</td>
		    		</tr>
		    		<tr>
		    			<td>Grupa 2</td>
		    			<td>Oznaka 2. zadatka</td>
		    			<td>Tip zadatka</td>
		    			<td>Verzija tipa zadatka</td>
		    		</tr>
		    		<tr>
		    			<td>Grupa 2</td>
		    			<td>Oznaka zadnjeg zadatka</td>
		    			<td>Tip zadatka</td>
		    			<td>Verzija tipa zadatka</td>
		    		</tr>
		    	</table>
		    	Mapiranje je potrebno definirati za sve zadatke i sve grupe!<br />
		    	Ako mapiranje nije definirano, svaka grupa na istom mjestu ima isti tip zadatka.<br /> 
		    	Podatci o mapiranju za pojedini zadatak pojedine grupe su odvojeni tabom.<br /> 
		    </div>
		    <s:form action="AdminSetDetailedChoiceConf" method="post" theme="ferko">
		      <s:hidden name="assessmentID" value="%{data.assessment.id}"></s:hidden>
			  <s:hidden name="selectedView" value="%{data.selectedView}"></s:hidden>
		      <s:textarea rows="5" cols="80" name="data.mapping" label="%{getText('forms.problemMapping')}"></s:textarea>
			  <s:submit method="setProblemMapping" value="%{getText('forms.general.update')}" />
		    </s:form>
		    <h3><s:property value="%{getText('forms.inputFromFile')}" /></h3>
		    <div>
		    	<s:form action="AdminSetDetailedChoiceConf" method="post" enctype="multipart/form-data" theme="ferko">
			        <s:hidden name="assessmentID" value="%{data.assessment.id}"></s:hidden>
					<s:hidden name="selectedView" value="%{data.selectedView}"></s:hidden>
			        <s:file name="data.dataFile" label="%{getText('forms.file')}"></s:file>
			        <s:submit method="uploadProblemMapping" value="%{getText('forms.general.update')}" />
		      	</s:form>
		    </div>

        </s:if><s:else>
			<s:text name="Error.defineParametersFirst" />
        </s:else>
	  </div>
  </s:if>
  <s:if test="data.selectedView.equals('manip')">
	  <div>
        <s:if test="data.assessment.assessmentConfiguration.problemsNum != 0">
		    <div>
		    	<strong>Format unosa:</strong><br />
			 	<table>
		    		<tr>
		    			<td>Grupa 1</td>
		    			<td>Indeks zadatka</td>
		    			<td>Manipulator</td>
		    		</tr>
		    		<tr>
		    			<td>Grupa 2</td>
		    			<td>Indeks zadatka</td>
		    			<td>Manipulator</td>
		    		</tr>
		    	</table>
			 	Trenutno su podržani manipulatori:
			 	<table>
		    		<tr>
		    			<th>Manipulator</th>
		    			<th>Opis djelovanja</th>
		    		</tr>
		    		<tr>
		    			<td>X</td>
		    			<td>Zadatak na koji manipulator djeluje se poništava. To znači da će ukupni broj bodova koje student dobiva biti
                            preskaliran temeljem bodova ostvarenih na neponištenim zadatacima prema maksimalnom broju bodova. Ovaj manipulator
                            djelovat će čim je student dobio taj zadatak (neovisno o tome je li ga riješavao, ili ga je ostavio praznim).</td>
		    		</tr>
		    		<tr>
		    			<td>x</td>
		    			<td>Zadatak na koji manipulator djeluje se poništava. To znači da će ukupni broj bodova koje student dobiva biti
                            preskaliran temeljem bodova ostvarenih na neponištenim zadatacima prema maksimalnom broju bodova. Ovaj manipulator
                            djelovat će samo ako je student dobio taj zadatak i unio rješenje (bilo ono točno ili ne, nije bitno).</td>
		    		</tr>
		    	</table>
			</div>
		    <s:form action="AdminSetDetailedChoiceConf" method="post" theme="ferko">
		      <s:hidden name="assessmentID" value="%{data.assessment.id}"></s:hidden>
			  <s:hidden name="selectedView" value="%{data.selectedView}"></s:hidden>
		      <s:textarea rows="7" cols="80" name="data.problemManipulators" label="%{getText('forms.problemManipulators')}"></s:textarea>
			  <s:submit method="setProblemManipulators" value="%{getText('forms.general.update')}" />
		    </s:form>
		    <h3><s:property value="%{getText('forms.inputFromFile')}" /></h3>
		    <div>
		    	<s:form action="AdminSetDetailedChoiceConf" method="post" enctype="multipart/form-data" theme="ferko">
			        <s:hidden name="assessmentID" value="%{data.assessment.id}"></s:hidden>
					<s:hidden name="selectedView" value="%{data.selectedView}"></s:hidden>
			        <s:file name="data.dataFile" label="%{getText('forms.file')}"></s:file>
			        <s:submit method="uploadProblemManipulators" value="%{getText('forms.general.update')}" />
		      	</s:form>
		    </div>
        </s:if><s:else>
			<s:text name="Error.defineParametersFirst" />
        </s:else>
	  </div>
  </s:if>
</div>  