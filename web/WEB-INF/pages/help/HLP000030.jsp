<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div class="helpTitle1">Provjera koja računa bodove</div>

<p>Ako definirate provjeru koja bodove preuzima od jedne ili više drugih provjera te pri tome
   obavlja neke elementarne modifikacije (poput skaliranja, ograničavanja i sl.), umjesto
   da sami pišete odgovarajući program, pravila možete definirati grafički.</p>

<p>Sustav će Vam prilikom odabira ovakvog načina konfiguracije provjere ponuditi tablicu
   koja će sadržavati sve provjere koje su definirane u sustavu. Pri tome se inicijalno bodovi
   neće uzimati niti od jedne provjere.</p>

<p>Da bi u sumu uključili neku provjeru, potrebno je uz dotičnu provjeru definirati faktor
   kojim ona ulazi u ukupnu sumu. Provjere koje je koristite trebaju to polje imati prazno.<br>
   <i>Napomena:</i> preporuka je faktor uvijek staviti na 1, jer će time biti puno jednostavnije
   shvatiti kako je provjera zapravo došla do ukupnog broja bodova (ako je potrebno, skaliranje
   je bolje napraviti na svakoj provjeri koja se uzima u sumu - u definiciji te same provjere).</p>
   
<p>Ako je provjera uključena u sumu, tada je moguće još definirati i sljedeće:</p>

<ul>
<li><b>Limit min</b> - ako je definiran i ako student na toj provjeri ima manje od definiranog
    limita, bodovi će biti postavljeni na taj limit (samo studentima koji su bili na toj provjeri);</li>
<li><b>Limit max</b> - ako je definiran i ako student na toj provjeri ima više od definiranog
    limita, bodovi će biti postavljeni na taj limit (samo studentima koji su bili na toj provjeri);</li>
<li><b>Pristupio</b> - ako se označi uz provjeru X, tada ta provjera postaje uvijet za prolaz ove provjere koja
    se definira; konkretno, da bi prošao ovu provjeru, student je morao barem pristupiti provjeri X;</li>
<li><b>Položio</b> - ako se označi uz provjeru X, tada ta provjera postaje uvijet za prolaz ove provjere koja
    se definira; konkretno, da bi prošao ovu provjeru, student je morao pristupiti i položiti provjeru X.</li>
</ul>

<p>Nakon što se definiraju pravila temeljem kojih se dolazi do ukupnog broja bodova, tu sumu moguće je
   dodatno obraditi (polja se nalaze ispod tablice) tako da se definira:</p>
<ul>
<li><b>Faktor skaliranja</b> - ako je definiran, do tada dobivena suma množi se ovim faktorom;</li>
<li><b>Limit min</b> - ako je definiran, (eventualno prethodno skalirana) suma postavlja se na
    definirani limit, ako je bila manja (a student je pristupio provjeri);</li>
<li><b>Limit max</b> - ako je definiran, (eventualno prethodno skalirana) suma postavlja se na
    definirani limit, ako je bila veća (a student je pristupio provjeri).</li>
</ul>

<p>Provjeri se status postavlja na <code>present=true</code> samo ako je student pristupio barem
   jednoj provjeri koja ima u prethodnoj tablici definiran doprinos (faktor).</p>

<p>Na kraju moguće je još definirati dodatna ograničenja koja reguliraju kada je student položio
   provjeru:</p>
   
<ul>
<li><b>Konačna suma</b> - ako je definirana vrijednost, tada je student morao skupiti barem toliko
    bodova (da bi prošao); u suprotnom se evidentira pad na provjeri;</li>
<li><b>Ukupno mora pristupiti</b> - ako je definirana vrijednost, tada je student morao pristupiti
    na barem toliko provjera (od onih koje imaju definiran doprinos); u suprotnom se evidentira pad na provjeri;</li>
<li><b>Ukupno mora položiti</b> - ako je definirana vrijednost, tada je student morao položiti
    barem toliko provjera (od onih koje imaju definiran doprinos); u suprotnom se evidentira pad na provjeri.</li>
</ul>
