<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div class="helpTitle1">Podržana Wiki sintaksa</div>

<p>Wiki sustav za sada podržava vrlo jednostavan skup naredbi, koje su opisane u nastavku.</p>

<style>
<!--
 td {border-bottom: 1px solid black;}
-->
</style>

<table>
<thead>
<tr><th>Kategorija</th><th>Wiki sintaksa</th><th>Primjer</th></tr>
</thead><tbody>
<tr><td>Stilovi teksta</td><td><pre>''italic'', '''bold''', '''''bolditalic'''''</pre></td><td><i>italic</i>, <b>bold</b>, <b><i>bolditalic</i></b></td></tr>
<tr><td>Naslovi</td><td><pre>== Naslov2 ==<br>=== Naslov 3 ===</pre></td><td><h2>Naslov2</h2><br><h3>Naslov 3</h3></td></tr>
<tr><td>Paragrafi</td><td><pre>Prvi paragraf<br>kroz više redaka.<br><br>Drugi paragraf.</pre></td><td><p>Prvi paragraf kroz više redaka.</p><p>Drugi paragraf.</p></td></tr>
<tr><td>Liste</td><td><pre> 1. numerirano 1<br> 1. numerirano 2<br>    a. slovčano 1<br>    a. slovčano 2<br>       * bulet 1<br>       * bulet 2</pre><i>Napomena: kod lista se mora poštivati uvlačenje same stavke, ne oznake vrste stavke (broj, slovo, zvjezdica). Vrsta stavke uvijek mora biti "1." ili "a." za numerirane stavke - sustav će ih sam numerirati korektno.</i></td><td><ol><li>numerirano 1</li><li>numerirano 2<ol style="list-style-type: lower-alpha;"><li>slovčano 1</li><li>slovčano 2<ul><li>bulet 1</li><li>bulet 2</li></ul></li></ol></li></ol></td></tr>
<tr><td>Linkovi</td><td><pre>[[wiki:kategorija1/stranica1]]<br>[[kategorija1/stranica1]]<br>[[wiki:kategorija1/stranica1|Tekst linka 1]]<br>[[kategorija1/stranica1|Tekst linka 1]]<br>[[@link type="page" url="kategorija1/stranica1"]]na stranicu 1[[/link]]<br>[[url:http://www.google.com/|vanjski link na google]]<br></pre><br><i>Napomena: sve mora biti napisano u istom retku</i></td><td><a href="#">kategorija1/stranica1</a><br><a href="#">kategorija1/stranica1</a><br><a href="#">Tekst linka 1</a><br><a href="#">Tekst linka 1</a><br><a href="#">na stranicu 1</a><br><a href="http://www.google.com/">vanjski link na google</a></td></tr>
<tr><td>Poništavanje značenja</td><td><pre>!''nije italic!''</pre></td><td>nije italic</td></tr>
<tr><td>Link na zadatak u sustavu studtest2</td><td><code>[[@link type="external-problems/list"<br>url="studtest2:http://studtest.zemris.fer.hr/problemGenerators#custom/zad_rg_010/varijanta1"]]<br>(zadatak)<br>[[/link]]</code><br><i>Napomena: sve mora biti napisano u istom retku</i></td><td>(zadatak)</td></tr>
<tr><td>Studentovi primjerci zadatka u sustavu studtest2</td><td><code>[[@external-problems-list<br>url="studtest2:http://studtest.zemris.fer.hr/problemGenerators#custom/zad_rg_010/varijanta1"]]<br>[[/external-problems-list]]</code><br><i>Napomena: sve mora biti napisano u istom retku</i></td><td>... ispisani zadatci ...</td></tr>
</tbody>
</table>

<p>U planu je još i dodavanje linkova na datoteke u repozitoriju, na provjere znanja i zastavice, na prijave, komponente kolegija (laboratorijske vježbe, zadaće, seminare) i sl, kao i 
   wiki repozitorij za upload slika i različitih dokumenata te jednostavan način izrade linkova na iste (odnosno in-line prikaz istih).</p>