<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div class="helpTitle1">Filtriranje rezultata studenata</div>

<p>Kako bi Vam se omogućilo da na brzinu dođete do informacija koje Vam trebaju, stranica s prikazom rezultata studenata nudi
   uporabu filtera koji će prikazati samo one retke koje želite. U polje "filter" unosi se bilo kakav izraz koji se izračunava
   u boolean vrijednost. Ukoliko se filter ostavi prazan, prikazat će se svi retci.</p>

<b>Podržane funkcije</b>
<table>
<thead>
  <tr><th>Naziv funkcije</th><th>Opis funkcije</th></tr>
</thead>
<tbody>
  <tr><td>present("<i>kratkiNazivProvjere</i>")</td><td>Vraća <code>true</code> ako je student bio na provjeri; inače vraća <code>false</code></td></tr>
  <tr><td>passed("<i>kratkiNazivProvjere</i>")</td><td>Vraća <code>true</code> ako je student prošao na provjeri; inače vraća <code>false</code></td></tr>
  <tr><td>score("<i>kratkiNazivProvjere</i>")</td><td>Vraća bodove studenta na provjeri</td></tr>
  <tr><td>rank("<i>kratkiNazivProvjere</i>")</td><td>Vraća rang studenta na provjeri</td></tr>
  <tr><td>flag("<i>kratkiNazivZastavice</i>")</td><td>Vraća vrijednost zastavice</td></tr>
  <tr><td>grade()</td><td>Vraća ocjenu studenta, ukoliko su ocjene podijeljene i valjane; inaće vraća 0.</td></tr>
  <tr><td>group()</td><td>Vraća grupu studenta (za predavanje).</td></tr>
  <tr><td>student()</td><td>Vraća objekt koji predstavlja trenutnog studenta; nad tim objektom može se tražiti:<br>
                        <code>jmbag</code> - vraća JMBAG studenta<br>
                        <code>lastName</code> - vraća prezime studenta<br>
                        <code>firstName</code> - vraća ime studenta<br>
                        </td></tr>
  <tr><td>jmbagIn("<i>jmbag1</i>",...,"<i>jmbagN</i>")</td><td>Vraća <code>true</code> ako je jmbag studenta jedan od zadanih jmbagova; inače vraća <code>false</code></td></tr>
  <tr><td>lastNameIn("<i>lastName1</i>",...,"<i>lastNameN</i>")</td><td>Vraća <code>true</code> ako je prezime studenta jedno od zadanih prezimena; inače vraća <code>false</code></td></tr>
  <tr><td>firstNameIn("<i>firstName1</i>",...,"<i>firstNameN</i>")</td><td>Vraća <code>true</code> ako je ime studenta jedno od zadanih imena; inače vraća <code>false</code></td></tr>
</tbody>
</table>

<b>Primjeri</b>

<p><code>present("MI1")</code> - vratit će sve studente koji su bili na provjeri čije je kratko ime MI1.</p>
<p><code>present("MI1") && !passed("MI1")</code> - vratit će sve studente koji su bili na provjeri čije je kratko ime MI1, i koji je nisu prošli.</p>
<p><code>student().jmbag=="0012345678"</code> - vratit će samo studenta čiji je JMBAG 0012345678.</p>

