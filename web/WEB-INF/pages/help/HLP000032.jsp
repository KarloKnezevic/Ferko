<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div class="helpTitle1">Upravljanje burzom grupa</div>

<p>Na ovoj stranici moguće je podešavati postavke burze grupe.</p>

<h2>Osnovno upravljanje</h2>
<p><b>Burza je otvorena</b> - postavljanjem ove zastavice dopušta se rad burze grupe. Hoće li studenti moći koristiti burzu, može biti 
   definirano još i dodatnim ograničenima <i>Otvoreno od</i>, <i>Otvoreno do</i> te <i>Vremenski buffer</i>.</p>
<p><b>Otvoreno od</b> - ako je zadano, definira trenutak u vremenu prije kojega burza neće dopuštati izmjene. Format je 
   <code>yyyy-MM-dd HH:mm:ss</code>, npr. <code>2009-11-25 09:00:00</code>.</p>
<p><b>Otvoreno do</b> - ako je zadano, definira trenutak u vremenu nakon kojega burza neće dopuštati izmjene. Format je 
   <code>yyyy-MM-dd HH:mm:ss</code>, npr. <code>2009-11-27 23:59:59</code>.</p>
<p><b>Ograničenja na grupe</b> - nudi mogućnost finog podešavanja kontrole na razini grupa i vrsta studenata u grupama (npr. demosa između 1 i 3,
    regularnih studenata između 27 i 30). <smal><code>TODO: opisati format.</code></smal></p>
<p><b>Sigurnosna ograničenja</b> - nudi mogućnost finog podešavanja kontrole isključivo na razini grupa. Format je opisan u nastavku.</p>
<p><b>Vremenski buffer</b> - vrijeme u sekundama; ako je zadano, definira da student <b>svoju grupu</b> ne može napustiti toliko sekundi od početka
   termina pridruženog toj grupi (grupa mora biti definirana, i mora imati barem jedan pridruženi događaj). Studenti koji još nisu blizu početka
   svojeg termina mogu neometano koristiti burzu za zamjene. -1 isključuje ovu kontrolu.</p>
 
<h2>Detaljnije upravljanje</h2>
<p>Nakon elemenata za osnovno upravljanje prikazana je tablica svih grupa koja nudi podešavanje postavki na razini grupe. Postavke su:</p>

<p><b>Kapacitet</b> - ako je nenegativan, burza neće dopuštati izmjene kojima bi ukupni broj studenata porastao iznad tog broja; ako je -1, 
   ovo ograničenje je isključeno (pa teoretski svi studenti mogu ući u tu grupu).</p>
<p><b>Moguć ulazak</b> - ako je uključen, dopušta se ulazak studentima u tu grupu; ako nije, nitko ne može ući u tu grupu.</p>
<p><b>Moguć izlazak</b> - ako je uključen, dopušta se izlazak studentima u tu grupu; ako nije, nitko ne može izići iz te grupe.</p>
<p><b>Tag grupe</b> - ako burza upravlja različitim vrstama grupa, grupe se mogu "tagirati". Primjer uporabe opisan je u nastavku.</p>

<h2>Uporaba tagova i sigurnosnih ograničenja</h2>
<p>Razmotrimo jednostavan scenarij: kolegij koji ima dva nastavnika želi organizirati usmeni ispit. Pri tome se studentima želi ponuditi
   popis termina usmenih (ideja je održati više manjih grupa), a studenti bi trebali imati mogućnost da sami odaberu termin uporabom
   burze. Dodatno treba paziti da je svaki nastavnik ponudio svoje termine, i u njih smiju ući samo studenti kojima je on predavao.</p>

<p>Zadatak možemo riješiti na sljedeći način. U <i>privatnim grupama</i> definirat ćemo podgrupu <i>Usmeni ispit</i>, te unutar nje
   podgrupe: <i>A neraspoređeni</i>, <i>B neraspoređeni</i>, <i>A termin 1</i>, <i>A termin 2</i>, <i>A termin 3</i>,
   <i>B termin 1</i> te <i>B termin 2</i>. Studente prvog nastavnika smjestit ćemo u grupu <i>A neraspoređeni</i>, a drugog u grupu
   <i>B neraspoređeni</i>. Svim grupama koje počinju s "A" dodijelit ćemo tag "A", a svim koje počinju s "B" dodijelit ćemo tag "B",
   i podesiti detalje na sljedeći način.</p>
   
<table>
<thead>
  <tr>
    <th>Grupa</th>
    <th>Ograničenje kapaciteta</th>
    <th>Moguć ulazak</th>
    <th>Moguć izlazak</th>
    <th>Tag</th>
  </tr>
</thead>
<tbody>
  <tr>
    <td>A neraspoređeni</td>
    <td>-1</td>
    <td>NE</td>
    <td>DA</td>
    <td>A</td>
  </tr>
  <tr>
    <td>A termin 1</td>
    <td>10</td>
    <td>DA</td>
    <td>DA</td>
    <td>A</td>
  </tr>
  <tr>
    <td>A termin 2</td>
    <td>10</td>
    <td>DA</td>
    <td>DA</td>
    <td>A</td>
  </tr>
  <tr>
    <td>A termin 3</td>
    <td>10</td>
    <td>DA</td>
    <td>DA</td>
    <td>A</td>
  </tr>
  <tr>
    <td>B neraspoređeni</td>
    <td>-1</td>
    <td>NE</td>
    <td>DA</td>
    <td>B</td>
  </tr>
  <tr>
    <td>B termin 1</td>
    <td>15</td>
    <td>DA</td>
    <td>DA</td>
    <td>B</td>
  </tr>
  <tr>
    <td>B termin 2</td>
    <td>15</td>
    <td>DA</td>
    <td>DA</td>
    <td>B</td>
  </tr>
</tbody>
</table>

<p>Potom ćemo kao sigurnosno ograničenje definirati:</p>
<pre>?:A/A,?:B/B</pre>
<p>i možemo otvoriti burzu.</p>

<h3>Struktura sigurnosnog ograničenja</h3>

<p>Sigurnosno ograničenje sastoji se od niza pojedinačnih ograničenja odvojenih zarezima. Jedno sigurnosno ograničenje sljedećeg je formata: 
   <code>vrstaStudenta:tagIzvorišneGrupe/tagOdredišneGrupe</code>.</p>
<ul>
<li><b>vrstaStudenta</b> - ako je definirana (u članstvu grupe) može se koristiti za razlikovanje "klasičnih" studenta od demosa i sl.</li>
<li><b>tagIzvorišneGrupe</b> - tag grupe u kojoj je student trenutno</li>
<li><b>tagOdredišneGrupe</b> - tag grupe u koju student želi</li>
</ul>

<p>Na svakom se mjestu mogu koristiti zamjenski znak '?' koji znači "bilo što", odnosno '#' koje znače "prazno ili nedefinirano".</p>
<p>Ovime je jasno što znači sljedeće sigurnosno ograničenje: <pre>?:A/A,?:B/B</pre>: bilo koja vrsta studenta može iz grupe tagirane s "A" u grupu
   tagiranu s "A", odnosno bilo koja vrsta studenta može iz grupe tagirane s "B" u grupu tagiranu s "B". Obzirom da su sigurnosna pravila definirana,
   sustav automatski pretpostavlja da sve što nije definirano ne vrijedi, pa će tako spriječavati da student iz grupe tagirane s "A" ode u grupu
   tagiranu s "B", i obrnuto.</p>
   