<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div class="helpTitle1">Repozitorij ključeva</div>

Sustav za svoj rad koristi ključeve prikazane u nastavku.

<table>
  <tr><th>Ključ</th><th>Naziv ključa</th></tr>
  <tr><td>AdminMessage</td><td>Poruka administratora.</td></tr>
  <tr><td>currentSemester</td><td>Trenutni semestar. Format je: YYYYS, primjerice 2007L.</td></tr>
  <tr><td>academicYear</td><td>Trenutna akademska godina. Format je: YYYY/YYYY, primjerice 2007/2008.</td></tr>
  <tr><td>marketPlace</td><td>Jesu li burze otvorene. Format je "yes" ili "no".</td></tr>
  <tr><td>systemInstalled</td><td>Je li sustav instaliran. Format je "true" ili "false". Ako nije "true", dopušten je poziv akcije Prepare.</td></tr>
  <tr><td>miScheduleParam</td><td>Za koji semestar je dozvoljen unos parametara za izradu rasporeda međuispita. Format je: YYYYS, primjerice 2007L.</td></tr>
  <tr><td>miScheduleParamDate</td><td>Ako je dozvoljen unos parametara za izradu rasporeda međuispita (vidi <i>miScheduleParam</i>), onda do kada. Format je "yyyy-MM-dd HH:mm:ss".</td></tr>
  <tr><td>miScheduleParamFlags</td><td>Ako je dozvoljen unos parametara za izradu rasporeda međuispita (vidi <i>miScheduleParam</i>), za koje točno međuispite? Format je zarezima razdvojen niz kratkih naziva tagova provjera znanja, npr. "MI1,ZI".</td></tr>
</table>
