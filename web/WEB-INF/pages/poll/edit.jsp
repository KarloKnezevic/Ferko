<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>
<!--h1 class="pageh">Ankete: <em>nova anketa</em></h1-->
<div class="content new-poll">
	<div class="maincontent">
	<div class="inner-padding">
	<h2>Uređivanje ankete</h2>
	<s:form action="CourseEdit" theme="ferko" method="post">
		<s:textfield name="title" label="Naslov" />
		<s:fielderror><s:param>title</s:param></s:fielderror>
		<s:textarea name="description" label="Opis" rows="5" cols="40"/>
		<li><label for="Create_startDate">Početak</label>
		<s:textfield name="startDate" cssClass="date" theme="simple" />
		<s:textfield name="startTime" theme="simple" value="12:00" />
		<s:fielderror><s:param>startDate</s:param></s:fielderror>

		</li>
		<li class="fieldComment">Imajte na umu da nakon što anketa započne više nije moguće uređivati pitanja.</li>
		<li><label for="Create_endDate">Kraj</label>
		<s:textfield name="endDate" cssClass="date" theme="simple" />
		<s:textfield name="endTime" theme="simple" value="12:00" />
		<s:fielderror><s:param>endDate</s:param></s:fielderror>

		</li>
		<s:if test="data.canTag">
			<s:select list="data.pollTags" listKey="id" listValue="name" name="pollTagId" label="Tag" emptyOption="true" />
		</s:if>
		<li>
			<ul id="questions">
				<li id="help"></li>
			</ul>
		</li>
		<li class="addQuestionBlock">
			<label for="questionType">Dodaj pitanje</label>
			<select name="questionType" id="questionType">
				<option value="singleChoice">Pitanje s jednostrukim izborom</option>
				<option value="multiChoice">Pitanje s višestrukim izborom</option>
				<option value="rating">Pitanje s ocjenjivanjem</option>
				<option value="bigText">Pitanje za opširniji odgovor</option>
			</select>
			<button name="addQuestion" type="button" id="addQuestionButton">Dodaj pitanje</button>
		</li>
		<li class="fieldComment">Kod pitanja s izborom odgovora svaka opcija se stavlja u novi red.</li>
		<s:hidden name="users" id="users_list" />
		<s:hidden name="courseInstanceID" />
		<s:hidden name="id" />
		<s:submit name="submit" value="Stvori anketu" />
	</s:form>
	</div>
	</div>
</div>
<script type="text/javascript" src="/ferko/js/jquery-1.2.6.min.js"></script> 
<script type="text/javascript" src="/ferko/js/jquery-ui/jquery-ui.js"></script>
<script type="text/javascript"><!--
$(document).ready(function(){

	function checkForEnter(event) {
	  if (event.keyCode == 13) {
		 currentTextboxNumber = textboxes.index(this);
		 if (textboxes[currentTextboxNumber + 1] != null) {
		   nextTextbox = textboxes[currentTextboxNumber + 1];
		   nextTextbox.select();
	  }
		 event.preventDefault();
		 return false;
	  }
	}

	var x = 0;
	var createQuestionHtml = function(type) {
		x++;
		string = '<li id="question_'+x+'" class="poll_question">';
		string += '<input type="hidden" name="question_'+x+'_type" value="'+type+'" />';
		string += '<input type="hidden" name="question_'+x+'_ordinal" value="'+x+'" />';
		string += '<label for="question_'+x+'_text">Tekst pitanja</label>';
		string += '<input type="text" name="question_'+x+'_text" id="question_'+x+'_text" />';
		if(type=='singleChoice' || type=="multiChoice") {
			string += '<div><label for="question_'+x+'_options">Opcije</label>'
			string += '<textarea stype="display:block;" name="question_'+x+'_options" id="question_'+x+'_options" cols="20" rows="5"></textarea></div>';
		}
		string += '<span title="Izbriši pitanje" class="remove_question">Izbriši</span>';
		string += '<span title="Vrsta pitanja" class="question_type_label">';
		if(type=='singleChoice') string+="Pitanje s jednostrukim izborom";
		if(type=='multiChoice') string+="Pitanje s višestrukim izborom";
		if(type=='rating') string+="Pitanje s ocjenjivanjem";
		if(type=='bigText') string+="Pitanje za opširniji odgovor";
		string += '</span>';
		string += '</li>';
		return string;
	}
	$('#addQuestionButton').mouseup(function(){
		$('#questions').append(createQuestionHtml($('#questionType').attr('value')));
		$('.remove_question').mouseup(function(){
			$(this).parent().remove();
		});
		textboxes = $("#questions input:text");
		if ($.browser.mozilla) {
		  $(textboxes).keypress(checkForEnter);
		} else {
		  $(textboxes).keydown(checkForEnter);
		}
	});
	$('#help').remove();
	$.datepicker.setDefaults($.datepicker.regional['hr']);
	$('.date').datepicker({dateFormat: 'yy-mm-dd'});

	var questions = [];
	<s:if test="%{data.JSONDescriptionOfQuestions != null}">
	questions =	<s:property value="data.JSONDescriptionOfQuestions" escape="false" />;
	</s:if>


	jQuery.each(questions, function(i, question) {
      	var qs = createQuestionHtml(question.type);
		$('#questions').append(qs);
		$('#question_'+x+'_text').val(question.question);
		$('#question_'+x+'_ordinal').val(question.ordinal);
		if(question.type=='singleChoice' || question.type=="multiChoice") {
			jQuery.each(question.options, function(i, option) {
				var v = $('#question_'+x+'_options').val();
				v += option+"\n";
				$('#question_'+x+'_options').val(v); 
			});
		}
    });

	textboxes = $("input:text");
	if ($.browser.mozilla) {
	  $(textboxes).keypress(checkForEnter);
	} else {
	  $(textboxes).keydown(checkForEnter);
	}

});
--></script>
