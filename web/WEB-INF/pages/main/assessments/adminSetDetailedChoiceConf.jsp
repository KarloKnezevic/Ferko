<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div id="body" class="container">

<s:if test="data.messageLogger.hasMessages()">
  <ul>
  <s:iterator value="data.messageLogger.messages">
      <li>[<s:property value="messageType"/>] <s:property value="messageText"/></li>
  </s:iterator>
  </ul>
</s:if>

<div><s:property value="data.courseInstance.course.name"/> (<s:property value="data.courseInstance.course.isvuCode"/>)</div>
<h2><s:property value="data.assessment.name"/></h2>

<s:if test="data==null">
  <div>Nisam dobio niti data objekt.</div>
</s:if>
<s:else>

  	<h1><s:property value="%{getText('forms.setAssessmentParameters')}" /></h1>
  	<s:include value="/WEB-INF/pages/main/assessments/ChoiceConfTodo.jsp"></s:include>

  <div class="tabview" id="tabview1">
    <div class="tabtitles">
    </div>
    <div class="tabpages">

      <div class="tabpage">
        <div class="tabtitle">Stupac pogreške</div>
        <div class="tabbody">

		    <s:form action="AdminSetDetailedChoiceConf" method="post" theme="ferko">
		      <s:hidden name="courseInstanceID" value="%{data.courseInstance.id}"></s:hidden>
		      <s:hidden name="assessmentID" value="%{data.assessment.id}"></s:hidden>
		      <s:if test="data.assessment.assessmentConfiguration.errorColumn">
		      	<s:textfield name="errorColumnText" label="%{getText('forms.errorColumnText')}" value="%{data.assessment.assessmentConfiguration.errorColumnText}"></s:textfield>
		      </s:if>
		      <s:else>
		      	<s:hidden name="errorColumnText" value="%{data.assessment.assessmentConfiguration.errorColumnText}"></s:hidden>
		      </s:else>
		      <s:submit method="upload"></s:submit>
		    </s:form>

        </div>
      </div>

      <div class="tabpage">
        <div class="tabtitle"><s:property value="%{getText('forms.setDetailedTaskScores')}" /></div>
        <div class="tabbody">

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
		    <s:form action="AdminSetDetailTaskScoresChoiceConf" method="post" theme="ferko">
		      <s:hidden name="courseInstanceID" value="%{data.courseInstance.id}"></s:hidden>
		      <s:hidden name="assessmentID" value="%{data.assessment.id}"></s:hidden>
		      <s:textfield name="scoreCorrect" label="%{getText('forms.scoreCorrect')}" value="%{data.assessment.assessmentConfiguration.scoreCorrect == 0 ? '0' : data.assessment.assessmentConfiguration.scoreCorrect}"></s:textfield>
		      <s:textfield name="scoreIncorrect" label="%{getText('forms.scoreIncorrect')}" value="%{data.assessment.assessmentConfiguration.scoreIncorrect == 0 ? '0' : data.assessment.assessmentConfiguration.scoreIncorrect}"></s:textfield>
		      <s:textfield name="scoreUnanswered" label="%{getText('forms.scoreUnanswered')}" value="%{data.assessment.assessmentConfiguration.scoreUnanswered == 0 ? '0' : data.assessment.assessmentConfiguration.scoreUnanswered}"></s:textfield>
		      <s:textarea rows="5" cols="80" name="detailTaskScores" label="%{getText('forms.detailTaskScores')}" value="%{data.assessment.assessmentConfiguration.detailTaskScores}"></s:textarea>
		      <s:submit method="setParam"></s:submit>
		    </s:form>
		    <h3><s:property value="%{getText('forms.inputFromFile')}" /></h3>
		    <div>
		    	<s:form action="ChoiceConfSetFromFile" method="post" enctype="multipart/form-data" theme="ferko">
			        <s:hidden name="courseInstanceID" value="%{data.courseInstance.id}"></s:hidden>
			        <s:hidden name="assessmentID" value="%{data.assessment.id}"></s:hidden>
			        <s:hidden name="paramType" value="detailTaskScores"></s:hidden> 
			        <s:file name="dataFile" label="%{getText('forms.file')}"></s:file>
			        <s:submit></s:submit>
		      	</s:form>
		    </div>

        </div>
      </div>
      
      <div class="tabpage">
        <div class="tabtitle"><s:property value="%{getText('forms.setCorrectAnswers')}" /></div>
        <div class="tabbody">
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
		    <s:form action="AdminSetCorrectAnswersChoiceConf" method="post" theme="ferko">
		      <s:hidden name="courseInstanceID" value="%{data.courseInstance.id}"></s:hidden>
		      <s:hidden name="assessmentID" value="%{data.assessment.id}"></s:hidden>
		      <s:textarea rows="7" cols="80" name="correctAnswers" label="%{getText('forms.correctAnswers')}" value="%{data.assessment.assessmentConfiguration.correctAnswers}"></s:textarea>
		      <s:submit method="setParam"></s:submit>
		    </s:form>
		    <h3><s:property value="%{getText('forms.inputFromFile')}" /></h3>
		    <div>
		    	<s:form action="ChoiceConfSetFromFile" method="post" enctype="multipart/form-data" theme="ferko">
			        <s:hidden name="courseInstanceID" value="%{data.courseInstance.id}"></s:hidden>
			        <s:hidden name="assessmentID" value="%{data.assessment.id}"></s:hidden>
			        <s:hidden name="paramType" value="correctAnswers"></s:hidden> 
			        <s:file name="dataFile" label="%{getText('forms.file')}"></s:file>
			        <s:submit></s:submit>
		      	</s:form>
		    </div>

        </div>
      </div>

      <div class="tabpage">
        <div class="tabtitle"><s:property value="%{getText('forms.setGroupsLabels')}" /></div>
        <div class="tabbody">

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
		    <s:form action="AdminSetGroupsLabelsChoiceConf" method="post" theme="ferko">
		      <s:hidden name="courseInstanceID" value="%{data.courseInstance.id}"></s:hidden>
		      <s:hidden name="assessmentID" value="%{data.assessment.id}"></s:hidden>
		      <s:textfield name="intervalStart" label="%{getText('forms.intervalStart')}" value=""></s:textfield>
		      <s:textfield name="intervalEnd" label="%{getText('forms.intervalEnd')}" value=""></s:textfield>
		      <s:textarea rows="5" cols="80" name="groupsLabels" label="%{getText('forms.groupsLabels')}" value="%{data.assessment.assessmentConfiguration.groupsLabels}"></s:textarea>
		      <s:submit method="setParam"></s:submit>
		    </s:form>
		    <h3><s:property value="%{getText('forms.inputFromFile')}" /></h3>
		    <div>
		    	<s:form action="ChoiceConfSetFromFile" method="post" enctype="multipart/form-data" theme="ferko">
			        <s:hidden name="courseInstanceID" value="%{data.courseInstance.id}"></s:hidden>
			        <s:hidden name="assessmentID" value="%{data.assessment.id}"></s:hidden>
			        <s:hidden name="paramType" value="groupsLabels"></s:hidden> 
			        <s:file name="dataFile" label="%{getText('forms.file')}"></s:file>
			        <s:submit></s:submit>
		      	</s:form>
		    </div>

        </div>
      </div>
		
		<div class="tabpage">
        <div class="tabtitle"><s:property value="%{getText('forms.setProblemsLabels')}" /></div>
        <div class="tabbody">

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
    <s:form action="AdminSetProblemsLabelsChoiceConf" method="post" theme="ferko">
      <s:hidden name="courseInstanceID" value="%{data.courseInstance.id}"></s:hidden>
      <s:hidden name="assessmentID" value="%{data.assessment.id}"></s:hidden>
      <s:textfield name="intervalStart" label="%{getText('forms.intervalStart')}" value=""></s:textfield>
      <s:textfield name="intervalEnd" label="%{getText('forms.intervalEnd')}" value=""></s:textfield>
      <s:textarea rows="5" cols="80" name="problemsLabels" label="%{getText('forms.problemsLabels')}" value="%{data.assessment.assessmentConfiguration.problemsLabels}"></s:textarea>
      <s:submit method="setParam"></s:submit>
    </s:form>
    <h3><s:property value="%{getText('forms.inputFromFile')}" /></h3>
    <div>
    	<s:form action="ChoiceConfSetFromFile" method="post" enctype="multipart/form-data" theme="ferko">
	        <s:hidden name="courseInstanceID" value="%{data.courseInstance.id}"></s:hidden>
	        <s:hidden name="assessmentID" value="%{data.assessment.id}"></s:hidden>
	        <s:hidden name="paramType" value="problemsLabels"></s:hidden> 
	        <s:file name="dataFile" label="%{getText('forms.file')}"></s:file>
	        <s:submit></s:submit>
      	</s:form>
    </div>

        </div>
      </div>
		
		<div class="tabpage">
        <div class="tabtitle"><s:property value="%{getText('forms.setProblemMapping')}" /></div>
        <div class="tabbody">

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
		    <s:form action="AdminSetProblemMappingChoiceConf" method="post" theme="ferko">
		      <s:hidden name="courseInstanceID" value="%{data.courseInstance.id}"></s:hidden>
		      <s:hidden name="assessmentID" value="%{data.assessment.id}"></s:hidden>
		      <s:textarea rows="5" cols="80" name="mapping" label="%{getText('forms.problemMapping')}" value="%{data.assessment.assessmentConfiguration.problemMapping}"></s:textarea>
		      <s:submit></s:submit>
		    </s:form>
		    <h3><s:property value="%{getText('forms.inputFromFile')}" /></h3>
		    <div>
		    	<s:form action="ChoiceConfSetFromFile" method="post" enctype="multipart/form-data" theme="ferko">
			        <s:hidden name="courseInstanceID" value="%{data.courseInstance.id}"></s:hidden>
			        <s:hidden name="assessmentID" value="%{data.assessment.id}"></s:hidden>
			        <s:hidden name="paramType" value="mapping"></s:hidden> 
			        <s:file name="dataFile" label="%{getText('forms.file')}"></s:file>
			        <s:submit></s:submit>
		      	</s:form>
		    </div>

        </div>
      </div>
      
      <div class="tabpage">
        <div class="tabtitle"><s:property value="%{getText('forms.setProblemManipulations')}" /></div>
        <div class="tabbody">
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
		    <s:form action="AdminSetProblemManipulatorsChoiceConf" method="post" theme="ferko">
		      <s:hidden name="courseInstanceID" value="%{data.courseInstance.id}"></s:hidden>
		      <s:hidden name="assessmentID" value="%{data.assessment.id}"></s:hidden>
		      <s:textarea rows="7" cols="80" name="problemManipulators" label="%{getText('forms.problemManipulators')}" value="%{data.assessment.assessmentConfiguration.problemManipulators}"></s:textarea>
		      <s:submit method="setParam"></s:submit>
		    </s:form>

        </div>
      </div>
      </div>
      </div>


 <script type="text/javascript">
   tabbed_view_init("tabview1");
 </script>
  </s:else>

<div>
  <a href="<s:url action="Main"/>"><s:text name="Navigation.main"/></a>
 | <a href="<s:url action="ShowCourse"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.courseHome"/></a>
 | <a href="<s:url action="AdminAssessmentList"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param></s:url>"><s:text name="Navigation.assessments"/></a>
<s:if test="data.assessment != null">
 | <a href="<s:url action="AdminAssessmentView"><s:param name="courseInstanceID"><s:property value="data.courseInstance.id"/></s:param><s:param name="assessmentID"><s:property value="data.assessment.id"/></s:param></s:url>"><s:text name="Navigation.backToDetails"/></a>
</s:if>
</div>

</div>
