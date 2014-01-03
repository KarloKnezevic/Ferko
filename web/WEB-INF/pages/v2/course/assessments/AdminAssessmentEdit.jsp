<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<s:if test="data.messageLogger.hasMessages()">
	<ul class="msgList">
		<s:iterator value="data.messageLogger.messages">
			<li class="msgType_<s:property value="messageType" />"><s:property value="messageText" /></li>
		</s:iterator>
	</ul>
</s:if>

<s:if test="data.bean.id==null || data.bean.id.length()==0">
  <h2><s:text name="Assessments.definingNew"></s:text></h2>
</s:if>
<s:else>
  <h2><s:text name="Assessments.editing"></s:text></h2>
</s:else>

<s:form action="AdminAssessmentEdit" theme="ferko" onsubmit="return pakiraj();">
	<s:textfield name="bean.name" label="%{getText('Assessments.name')}" />
	<s:textfield name="bean.shortName" label="%{getText('Assessments.shortName')}" />
	<s:select list="data.visibilities" listKey="name" listValue="value" name="bean.visibility" label="%{getText('Assessments.assesmentVisibility')}" />
	<s:textfield name="bean.sortIndex" label="%{getText('Assessments.sortIndex')}" />
	<s:checkbox name="bean.locked" label="%{getText('Assessments.locked')}" />
	<s:select list="data.tags" listKey="id" listValue="name" name="bean.assesmentTagID" label="%{getText('Assessments.assesmentTag')}" />
	<s:select list="data.flags" listKey="id" listValue="name" name="bean.assesmentFlagID" label="%{getText('Assessments.assesmentFlag')}" />
	<s:textfield name="bean.maxScore" label="%{getText('Assessments.maxScore')}" />
	<s:select list="data.possibleParents" listKey="id" listValue="name" name="bean.parentID" label="%{getText('Assessments.parent')}" />
	<s:select list="data.possibleChainedParents" listKey="id" listValue="name" name="bean.chainedParentID" label="%{getText('Assessments.chainedParent')}" />
	<s:textfield name="bean.startsAt" label="%{getText('Assessments.startsAt')}" />
	<s:textfield name="bean.duration" label="%{getText('Assessments.duration')}" />
	<s:checkbox name="bean.eventHidden" label="%{getText('Assessments.eventHidden')}" />
	<s:hidden name="bean.programType" label="%{getText('Assessments.programType')}" />
	<s:hidden name="bean.program" label="%{getText('Assessments.program')}" />
	<s:hidden name="data.guiConfig" />
	<s:hidden name="bean.id" />
	<s:hidden name="bean.courseInstanceID" />
	<s:hidden name="bean.programVersion" />
	<s:submit method="saveAssessment" />
</s:form>


<script type="text/javascript">

  // Podrska za graficko uredivanje virtualne provjere koja bodove vadi iz drugih provjera
  // -------------------------------------------------------------------------------------
  function redakTablice(sname,faktor,gmin,gmax,obav,poloz) {
	  this.sname = sname;
	  this.faktor = faktor;
	  this.gmin = gmin;
	  this.gmax = gmax;
	  this.obav = obav;
	  this.poloz = poloz;
  }

  function konfiguracija() {
	  this.retci = new Array();
	  this.limitMin = "";
	  this.limitMax = "";
	  this.minSum = "";
	  this.atLeastPresent = "";
	  this.atLeastPassed = "";
	  this.scalingFactor = "";
	  
	  this.dodajRedak = function(redak) {
		  this.retci.push(redak);
	  }

	  this.pronadiRedak = function(ime) {
		  for(var i = 0; i < this.retci.length; i++) {
			  var r = this.retci[i];
			  if(r.sname==ime) return r;
		  }
		  return new redakTablice(ime,"","","","","");
	  }
  }

  function praznaKonfiguracija() {
	  return new konfiguracija();
  }

  function parsiraj(str) {
	  var r = str.split("\n");
	  if(r.length<1) return new konfiguracija();
	  if(r[0].substring(0,5)!="//@@1") return new konfiguracija();
	  var podatci = r[0].substr(5);
	  var r2 = podatci.split("\t");
	  if(r2.length<2) return new konfiguracija();
	  var i = 0;
	  var k = new konfiguracija();
	  while(i<r2.length) {
		  if(r2[i].substring(0,2)=="@A") { // Imam provjeru
			  k.dodajRedak(new redakTablice(r2[i].substr(2),denull(r2[i+1]),denull(r2[i+2]),denull(r2[i+3]),denull(r2[i+4]),denull(r2[i+5])));
			  i += 6;
			  continue;
		  }
		  if(r2[i]=="@ClimitMin") {
			  k.limitMin = denull(r2[i+1]);
			  i+=2;
			  continue;
		  }
		  if(r2[i]=="@ClimitMax") {
			  k.limitMax = denull(r2[i+1]);
			  i+=2;
			  continue;
		  }
		  if(r2[i]=="@CminSum") {
			  k.minSum = denull(r2[i+1]);
			  i+=2;
			  continue;
		  }
		  if(r2[i]=="@CatLeastPresent") {
			  k.atLeastPresent = denull(r2[i+1]);
			  i+=2;
			  continue;
		  }
		  if(r2[i]=="@CatLeastPassed") {
			  k.atLeastPassed = denull(r2[i+1]);
			  i+=2;
			  continue;
		  }
		  if(r2[i]=="@CscalingFactor") {
			  k.scalingFactor = denull(r2[i+1]);
			  i+=2;
			  continue;
		  }
		  // Greska u formatu podataka! Prekini parsiranje ovdje 
		  return new konfiguracija();
	  }
	  return k;
  }
  
  // Podrska za graficko uredivanje provjere koja sama ima definirane bodove
  // -------------------------------------------------------------------------------------
  function konfiguracija2() {
	  this.scalingFactor = "";
	  this.limitMin = "";
	  this.limitMax = "";
	  this.minSum = "";
  }
  
  function praznaKonfiguracija2() {
	  return new konfiguracija2();
  }

  function parsiraj2(str) {
	  var r = str.split("\n");
	  if(r.length<1) return new konfiguracija2();
	  if(r[0].substring(0,5)!="//@@2") return new konfiguracija2();
	  var podatci = r[0].substr(5);
	  var r2 = podatci.split("\t");
	  if(r2.length<2) return new konfiguracija2();
	  var i = 0;
	  var k = new konfiguracija2();
	  while(i<r2.length) {
		  if(r2[i]=="@ClimitMin") {
			  k.limitMin = denull(r2[i+1]);
			  i+=2;
			  continue;
		  }
		  if(r2[i]=="@ClimitMax") {
			  k.limitMax = denull(r2[i+1]);
			  i+=2;
			  continue;
		  }
		  if(r2[i]=="@CminSum") {
			  k.minSum = denull(r2[i+1]);
			  i+=2;
			  continue;
		  }
		  if(r2[i]=="@CscalingFactor") {
			  k.scalingFactor = denull(r2[i+1]);
			  i+=2;
			  continue;
		  }
		  // Greska u formatu podataka! Prekini parsiranje ovdje 
		  return new konfiguracija2();
	  }
	  return k;
  }
  
  // Pomocne funkcije
  // -------------------------------------------------------------------------------------
  function denull(n) {
	  if(n=="null") return "";
	  return n;
  }
  function makenull(n) {
	  if(n=="") return "null";
	  return n;
  }

  function pakiraj() {
	  $("#AdminAssessmentEdit_bean_programType").attr("value", trenutnaVrsta);
	  if(trenutnaVrsta=="gui1") {
		  return pakirajKonfiguraciju1();
	  } else if(trenutnaVrsta=="gui2") {
		  return pakirajKonfiguraciju2();
	  } else {
		  $("#AdminAssessmentEdit_bean_program").attr("value", $("#javaProg").attr("value"));
		  return true;
	  }
  }
  
  function provjera(id, sname) {
    this.id = id;
    this.sname = sname;
  }
  
  var provjere = new Array();
  var provjereIndex = 0;
<s:iterator value="data.possibleScoreSources">
  provjere[provjereIndex++] = new provjera("<s:property value="id"/>", "<s:property value="shortName"/>");
</s:iterator>

  var trenutnaVrsta = $("#AdminAssessmentEdit_bean_programType").attr("value");
  if(trenutnaVrsta!="gui1" && trenutnaVrsta!="gui2") {
	  trenutnaVrsta="java";
  }
  
  var guiConfString = "<s:property value="data.guiConfig"/>";
  var programString = $("#AdminAssessmentEdit_bean_program").attr("value");
  if(guiConfString=="") guiConfString = programString;
  
  //trenutnaVrsta="gui1";
  //programString = "//@@1@ALAB1\t1\tnull\tnull\tnull\tnull\t@ALAB3\t1\tnull\tnull\t1\t1\t@ClimitMin\t1\t@ClimitMax\t2\t@CminSum\t3\t@CatLeastPresent\t1\t@CatLeastPassed\t2\t@CscalingFactor\t1\nBla2";
  //trenutnaVrsta="gui2";
  //programString = "//@@2@ClimitMin\t1\t@ClimitMax\t2\t@CminSum\t3\t@CscalingFactor\t1\nBla2";
  
  var guiConfig;
  if(trenutnaVrsta=="gui1") {
	  guiConfig = parsiraj(guiConfString);
  } else {
	  guiConfig = praznaKonfiguracija();
  }
  var guiConfig2;
  if(trenutnaVrsta=="gui2") {
	  guiConfig2 = parsiraj2(guiConfString);
  } else {
	  guiConfig2 = praznaKonfiguracija2();
  }
  
  var redak = $("<li><div style='width: 800px; margin-left: 180px;' id='myDivCont'></div></li>");
  $("#AdminAssessmentEdit li:last-child").before(redak);
  var myDiv = $("#myDivCont");
  var grafLink = $("<a href='javascript:return false;' id='grafUred'>Provjera koja računa bodove</a>");
  var grafLink2 = $("<a href='javascript:return false;' id='graf2Ured'>Provjera s vlastitim bodovima</a>");
  var javaLink = $("<a href='javascript:return false;' id='javaUred'>Detaljno uređivanje</a>");
  var naslov = $("<div></div>");
  naslov.append(grafLink2);
  naslov.append($('<a target="_jcms_help" href="<s:url action="Help"><s:param name="helpKey">000031</s:param></s:url>" onclick="blur();"><img src="img/icons/help.png"></a>'));
  naslov.append(" | ");
  naslov.append(grafLink);
  naslov.append($('<a style="border: 0;" target="_jcms_help" href="<s:url action="Help"><s:param name="helpKey">000030</s:param></s:url>" onclick="blur();"><img style="border: 0;" src="img/icons/help.png"></a>'));
  naslov.append(" | ");
  naslov.append(javaLink);
  naslov.append($('<a target="_jcms_help" href="<s:url action="Help"><s:param name="helpKey">000007</s:param></s:url>" onclick="blur();"><img src="img/icons/help.png"></a>'));
  myDiv.append(naslov);
  
  // Definiranje uredivaca izracunske provjere
  var tablDiv = $("<div style='background-color: #EEEEEE;'></div>");
  if(trenutnaVrsta=="gui1") {
	  tablDiv.show();
  } else {
	  tablDiv.hide();
  }
  var tbl = "<table style='border: 1px solid black;'>";
  tbl += "<thead><tr><th title='Naziv provjere'>Provjera</th><th title='Faktor kojim provjera ulazi u sumu'>Doprinos</th><th title='Minimum na koji će se bodovi nakon skaliranja resetirati ako su manji'>Limit Min</th><th title='Maksimum na koji će se bodovi nakon skaliranja resetirati ako su veći'>Limit Max</th><th title='Prisutnost studenta na provjeri je uvjet za prolaz ove provjere'>Pristupio</th><th title='Položena provjera je uvjet za prolaz ove provjere'>Položio</th></tr></thead><tbody>";
  for(var provInd=0; provInd<provjere.length; provInd++) {
	  var prov = provjere[provInd];
	  var r = guiConfig.pronadiRedak(prov.sname);
	  var redak = "<tr>";
	  redak += "<td>"+prov.sname+"</td><td>";
	  redak += "<input id='gui1_r"+provInd+"_f' type='text' value='"+r.faktor+"'>"+"</td><td>";
	  redak += "<input id='gui1_r"+provInd+"_gmin' type='text' value='"+r.gmin+"'>"+"</td><td>";
	  redak += "<input id='gui1_r"+provInd+"_gmax' type='text' value='"+r.gmax+"'>"+"</td><td>";
	  redak += "<input id='gui1_r"+provInd+"_obav' type='checkbox'"+(r.obav=="1"?" checked":"")+">"+"</td><td>";
	  redak += "<input id='gui1_r"+provInd+"_poloz' type='checkbox'"+(r.poloz=="1"?" checked":"")+">"+"</td>";
	  redak += "</tr>";
	  tbl += redak;
  }
  tbl += "</tbody></table>";
  var dod = "<b>Dodatna obrada:</b><br>Prethodnu sumu pomnoži faktorom ";
  dod += "<input id='gui1_sf' type='text' value='"+guiConfig.scalingFactor+"' size='4'> ";
  dod += "i zatim ograniči na interval ";
  dod += "<input id='gui1_lmin' type='text' value='"+guiConfig.limitMin+"' size='4'> do ";
  dod += "<input id='gui1_lmax' type='text' value='"+guiConfig.limitMax+"' size='4'>.<br><br>";

  dod += "<b>Uvjeti za prolaz:</b><br>";
  dod += "Konačna suma mora biti barem: <input id='gui1_ms' type='text' value='"+guiConfig.minSum+"' size='4'><br>";
  dod += "Ukupno mora pristupiti na barem ovoliko provjera: <input id='gui1_apr' type='text' value='"+guiConfig.atLeastPresent+"' size='2'><br>";
  dod += "Ukupno mora položiti barem ovoliko provjera: <input id='gui1_aps' type='text' value='"+guiConfig.atLeastPassed+"' size='2'><br>";
  tbl += dod;
  tablDiv.append(tbl);
  myDiv.append(tablDiv);

  function pakirajKonfiguraciju1() {
	  var d = "//@@1";
	  for(var provInd=0; provInd<provjere.length; provInd++) {
		  var prov = provjere[provInd];
		  var faktor = $("#gui1_r"+provInd+"_f").attr("value");
		  if(faktor && faktor!="" && faktor!="0") {
			  d += "@A" + prov.sname + "\t";
			  d += makenull($("#gui1_r"+provInd+"_f").attr("value")) + "\t";
			  d += makenull($("#gui1_r"+provInd+"_gmin").attr("value")) + "\t";
			  d += makenull($("#gui1_r"+provInd+"_gmax").attr("value")) + "\t";
			  if($("#gui1_r"+provInd+"_obav").attr("checked")) {
				  d += "1\t";
			  } else {
				  d += "0\t";
			  }
			  if($("#gui1_r"+provInd+"_poloz").attr("checked")) {
				  d += "1\t";
			  } else {
				  d += "0\t";
			  }
		  }
	  }
	  d += "@ClimitMin\t" + makenull($("#gui1_lmin").attr("value"));
	  d += "\t@ClimitMax\t" + makenull($("#gui1_lmax").attr("value"));
	  d += "\t@CminSum\t" + makenull($("#gui1_ms").attr("value"));
	  d += "\t@CscalingFactor\t" + makenull($("#gui1_sf").attr("value"));
	  d += "\t@CatLeastPresent\t" + makenull($("#gui1_apr").attr("value"));
	  d += "\t@CatLeastPassed\t" + makenull($("#gui1_aps").attr("value"));
	  $("#AdminAssessmentEdit_data_guiConfig").attr("value", d);
	  return true;
  }
  
  
  // Definiranje uredivaca same provjere
  var tabl2Div = $("<div style='background-color: #EEEEEE; border: 1px dotted black;'></div>");
  if(trenutnaVrsta=="gui2") {
	  tabl2Div.show();
  } else {
	  tabl2Div.hide();
  }
  var dod2 = "<b>Dodatna obrada:</b><br>Unesene bodove provjere pomnoži faktorom ";
  dod2 += "<input id='gui2_sf' type='text' value='"+guiConfig2.scalingFactor+"' size='4'> ";
  dod2 += "i zatim ograniči na interval ";
  dod2 += "<input id='gui2_lmin' type='text' value='"+guiConfig2.limitMin+"' size='4'> do ";
  dod2 += "<input id='gui2_lmax' type='text' value='"+guiConfig2.limitMax+"' size='4'>.<br><br>";

  dod2 += "<b>Uvjet za prolaz:</b><br>";
  dod2 += "Konačna suma mora biti barem: <input id='gui2_ms' type='text' value='"+guiConfig2.minSum+"' size='4'><br>";
  tabl2Div.append(dod2);
  myDiv.append(tabl2Div);

  function pakirajKonfiguraciju2() {
	  var d = "//@@2";
	  d += "@ClimitMin\t" + makenull($("#gui2_lmin").attr("value"));
	  d += "\t@ClimitMax\t" + makenull($("#gui2_lmax").attr("value"));
	  d += "\t@CminSum\t" + makenull($("#gui2_ms").attr("value"));
	  d += "\t@CscalingFactor\t" + makenull($("#gui2_sf").attr("value"));
	  $("#AdminAssessmentEdit_data_guiConfig").attr("value", d);
	  return true;
  }
  
  // Definiranje detaljnog uredivaca
  var javaDiv = $("<div></div>");
  if(trenutnaVrsta!="gui1" && trenutnaVrsta!="gui2") {
	  javaDiv.show();
  } else {
	  javaDiv.hide();
  }
  javaDiv.append("<textarea id='javaProg' rows='20' cols='80'></textarea>");
  myDiv.append(javaDiv);
  $("#javaProg").attr("value", $("#AdminAssessmentEdit_bean_program").attr("value"));
  
  grafLink.bind("click", function(e) {
	  if(trenutnaVrsta=="gui1") return false;
	  trenutnaVrsta="gui1";
	  podesiBoje();
	  javaDiv.hide();
	  tabl2Div.hide();
	  tablDiv.show();
	  return false;
	  });
  grafLink2.bind("click", function(e) {
	  if(trenutnaVrsta=="gui2") return false;
	  trenutnaVrsta="gui2";
	  podesiBoje();
	  javaDiv.hide();
	  tablDiv.hide();
	  tabl2Div.show();
	  return false;
	  });
  javaLink.bind("click", function(e) {
	  if(trenutnaVrsta=="java") return false;
	  trenutnaVrsta="java";
	  podesiBoje();
	  tablDiv.hide();
	  tabl2Div.hide();
	  javaDiv.show();
	  return false;
	  });
  podesiBoje();

  function podesiBoje() {
	  if(trenutnaVrsta=="gui1") {
		  javaLink.css("border", "0px solid black");
		  javaLink.css("text-decoration", "underline");
		  grafLink.css("border", "1px solid black");
		  grafLink.css("text-decoration", "none");
		  grafLink2.css("border", "0px solid black");
		  grafLink2.css("text-decoration", "underline");
	  } else if(trenutnaVrsta=="gui2") {
			  javaLink.css("border", "0px solid black");
			  javaLink.css("text-decoration", "underline");
			  grafLink.css("border", "0px solid black");
			  grafLink.css("text-decoration", "underline");
			  grafLink2.css("border", "1px solid black");
			  grafLink2.css("text-decoration", "none");
	  } else {
		  javaLink.css("border", "1px solid black");
		  javaLink.css("text-decoration", "none");
		  grafLink.css("border", "0px solid black");
		  grafLink.css("text-decoration", "underline");
		  grafLink2.css("border", "0px solid black");
		  grafLink2.css("text-decoration", "underline");
	  }
  }
  //$("#AdminAssessmentEdit li:last-child").before("<li><table style='margin-left: 180px; width: 800px; border: 1px solid black;'><tr><td>a</td><td>b</td></tr></table></li>");
</script>

<p><a target="_jcms_help" href="<s:url action="Help"><s:param name="helpKey">000007</s:param></s:url>"><s:text name="Navigation.help"/></a></p>
