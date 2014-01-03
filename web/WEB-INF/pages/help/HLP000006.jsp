<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div class="helpTitle1">Upload datoteka na ispit</div>

<p>Koristeći odabrani formular možete obaviti upload datoteka na ispit. Datoteke pri tome mogu biti upute namjenjene
svim studentima, ili pak mogu biti studentski obrasci, skenirane domaće zadaće, seminari i sl, što se vodi za svakog
studenta posebno. Ukoliko odlučite uploadati datoteke, sustav će se pobrinuti da korisnici mogu vidjeti isključivo
svoje datoteke.</p>
<p>Kako se obavlja unos? Potrebno je pripremiti i uploadati jednu zip arhivu koja u vršnom direktoriju sadrži sve
datoteke. U toj se ZIP arhivi mora nalaziti i datoteka <code>opisnik.txt</code> koja sadrži detaljne informacije
o samim datotekama. Pretpostavimo da imate tri studenta, svaki od njih ima dva skenirana obrasca, i dodatno, imate
jedan PDF koji želite staviti na uvid svim studentima. Pripremite arhivu sljedećeg oblika:</p>
<pre>
 Podaci.zip
 ----------
   +- slika1.png
   +- slika2.png
   +- slika3.png
   +- slika4.png
   +- slika5.png
   +- slika6.png
   +- uputa.pdf
   +- opisnik.txt
</pre>

<p>Datoteka opisnik.txt u svakom retku ima 4 elementa odvojena znakom TAB:</p>

<table border="1">
	<tr><th>jmbag</th><td>Jmbag studenta. Može biti prazno, ako se radi o datoteci namjenjenoj svima.</td></tr>
	<tr><th>datoteka</th><td>Ime datoteke u arhivi. Ne smije sadržavati stazu.</td></tr>
	<tr><th>opisnik</th><td>U slučaju da student ima više datoteka, opisnici se koriste kako bi se razlikovale pojedine datoteke.
                            Različite vrste provjere znanja definiraju značenje ovih opisnika, pa će više o tome biti u nastavku.</td></tr>
	<tr><th>komentar</th><td>Komentar koji se želi ispisati studentu. Ne smije biti predugačak, i može biti izostavljen.</td></tr>
</table>

<p>Primjer same datoteke opisnik.txt za naš slučaj:</p>

<pre>
	uputa.pdf	A1	Uputa za pisanje provjere znanja
5509587054	slika1.png	O1	
5509587054	slika2.png	O2	
5583351973	slika3.png	O1	
5583351973	slika4.png	O2	
2799158078	slika5.png	O1	
2799158078	slika6.png	O2	
</pre>
