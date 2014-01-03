<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div class="helpTitle1">Bodovi na provjerama znanja</div>

<p>Ferko za svaku provjeru prati tri vrste bodova. Na stranicama za pregled bodova vste će tipično imati sljedeće nazive:</p>
<ul>
<li><s:text name="Assessments.score"></s:text> - ovo su Vaši bodovi koji Vam se priznaju kao ostvareni na provjeri.</li>
<li><s:text name="Assessments.thisAssessmentScore"></s:text> - ovo su Vaši bodovi ostvareni baš na ovoj provjeri (razlika je opisana u nastavku).</li>
<li><s:text name="Assessments.nonProcessedScore"></s:text> - ovo su Vaši bodovi ostvareni prije bilo kakvih manipulacija (skaliranje, odsjecanje i sl).</li>
</ul>

<p>Razliku, odnosno ulogu pojedine vrste najbolje ćemo objasniti na primjeru. Pretpostavimo da je student X pisao 1. međuispit iz nekog kolegija. Ispit nosi 20 bodova.
   Ispit se sastoji od 15 pitanja i rješava popunjavanjem obrasca (abc-pitalice). Kako bi se lakše računalo s bodovima, asistent je studentima rekao da svako točno
   odgovoreno pitanje nosi +1 bod, a netočno -0.25.</p>

<ul>
<li><s:text name="Assessments.nonProcessedScore"></s:text> - u našem primjeru, ovdje će maksimum biti 15 bodova (15 pitanja puta 1 bod).</li>
<li><s:text name="Assessments.thisAssessmentScore"></s:text> - u našem primjeru ovdje će maksimum biti izračunat kao (15 pitanja puta 1 bod)*20/15, tj. bit će 20.
    i predstavlja bodove nakon obrade, koji su u određenoj mjeri konačni.</li>
<li><s:text name="Assessments.score"></s:text> - u našem primjeru, ovo će biti također maksimalno 20.</li>
</ul>

<p>Pogledajmo sada primjer studenta Y koji je izašao na nadoknadu 1. međuispita, i rješio 7 zadataka, i to točno. Asistent će u Ferku tipično definirati da provjera MI1 preuzima (ili nasljeđuje) bodove
   od nadoknade (MI1N). Tako će na stranici provjere MI1 prve dvije kategorije bodova biti 0, no kategorija "<s:text name="Assessments.score"></s:text>" imat će 9,3333 bodova.
   Naime, kada razmišljamo o tome koliko je bodova student ostvario na prvom međuispitu, za izračun ukupnih bodova na kolegiju ne zanima nas dolaze li ti bodovi iz nadoknade
   ili originalne provjere znanja. Na stranici provjere MI1N, bodovi će biti 7, 9,3333, 9,3333.</p>
