<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div class="helpTitle1">Provjera s vlastitim bodovima</div>

<p>Ako definirate provjeru koja ima vlastiti "izvor" bodova (učitani rezultati provjere s
   obrascem, učitani sumarni rezultati i sl.), te ako imate vrlo jednostavna pravila
   kako iz tih bodova doći do bodova provjere te kako utvrditi je li student provjeru
   prošao ili nije, možete iskoristiti ovaj jednostavan grafički uređivač.</p>

<p>Sustav će Vam prilikom odabira ovakvog načina konfiguracije provjere ponuditi mogućnost
   dodatne obrade "sirovih" (učitanih) bodova, pri čemu nudi mogućnost definiranja:</p>
<ul>
<li><b>Faktor skaliranja</b> - ako je definiran, bodovi se množe ovim faktorom;</li>
<li><b>Limit min</b> - ako je definiran, (eventualno prethodno skalirani) bodovi postavljaju se na
    definirani limit, ako su manji (a student je pristupio provjeri);</li>
<li><b>Limit max</b> - ako je definiran, (eventualno prethodno skalirani) bodovi postavljaju se na
    definirani limit, ako su veći (a student je pristupio provjeri).</li>
</ul>

<p>Na kraju moguće je još definirati dodatno ograničenje koje regulira kada je student položio
   provjeru (ako se ne unese, student je položio provjeru čim joj je pristupio):</p>
   
<ul>
<li><b>Konačna suma</b> - ako je definirana vrijednost, tada je student morao skupiti barem toliko
    bodova (da bi prošao); u suprotnom se evidentira pad na provjeri.</li>
</ul>
