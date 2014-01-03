<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div class="helpTitle1">Pregled i ručni unos vrijednosti zastavica</div>

<p>Na ovoj stranici moguće je pogledati trenutno stanje zastavice, kao i je li njezina vrijednost ručno fiksirana (i ako je, na koju vrijednost). Pojasnimo malo o čemu se radi.</p>

<p>U Ferku, provjere znanja, baš kao i zastavice, objekti su kojima se vrijednost najčešće računa, a ne unosi ručno. Prilikom stvaranja nove zastavice, zastavici se automatski
pridružuje sljedeći program (uvjerite se sami tako da otiđete na uređivanje zastavice): </p>

<pre>if(overrideSet()) {
  setValue(overrideValue());
} else {
  setValue(false);
}</pre>

<p>Ovaj program vrijednost zastavice računa tako da provjeri je li za promatranog studenata napravljen ručni unos (<code>overrideSet()</code>); ako je, kao vrijednost zastavice postavlja
se ono što je ručno uneseno (poziv metode <code>setValue()</code> uz argument <code>overrideValue()</code>); ako nije zabilježen ručni unos, vrijednost zastavice postavlja se na 
<code>false</code>.</p>

<p>Koristeći preglednik na ovoj stranici možete regulirati ručne unose. Ako nekom studentu želite ručno evidentirati status zastavice, obavezno postavite kvačicu na <b>Fiksiraj vrijednost</b>,
i potom regulirajte samu vrijednost (stupac <b>Vrijednost zastavice</b>). Stvarna vrijednost zastavice (stupac <b>Trenutna efektivna vrijednost</b>) ažurirat će se tek kada se
pokrene program za izračun vrijednosti, a to radite tako da na stranici s popisom svih provjera i zastavica aktivirate <b> Ažuriraj sve bodove i vrijednosti zastavica</b>.</p>

<p>Osim ručnim postavljanjem, vrijednosti zastavice možete regulirati potpuno programski. Evo primjera. Podesite program za izračun na sljedeći način:</p>

<pre>
  setValue(!present("MI1"));
</pre>

<p>Ovaj program pretpostavlja da na kolegiju već postoji provjera čije je kratko ime MI1 (dakle, prvi međuispit), i da su na toj provjeri već uneseni rezultati 1. međuispita. U tom
slučaju zastavica će poprimiti vrijednost istinitosti za one studente koji nisu pristupili prvom međuispitu, dok će za sve ostale ta vrijednost biti laž. U postavkama ispita koji je
nadoknada 1. međuispita tada ovu zastavicu možete definirati kao zastavicu-preduvijet. Time će na tu provjeru moći izaći samo oni studenti kojima je vrijednost zastavice postavljena
(istinita), pa će sustav i prilikom raspoređivanja raditi samo s njima.</p> 

<p>Konačno, za maksimalnu kontrolu, program koji računa stvarnu vrijednost zastavice može u obzir uzimati i ručne unose, i programsko ispitivanje. Evo primjera.</p>

<pre>if(overrideSet()) {
  setValue(overrideValue());
} else {
  setValue(!present("MI1"));
}</pre>

<p>U ovom primjeru najprije se provjerava je li napravljen ručni unos podatka za studenta. Ako je, to se koristi kao vrijednost zastavice (koja sama po sebi može biti istinita ili
lažna). Ako nema ručnog unosa, vrijednost se postavlja temeljem ispitivanja je li student bio prisutan na prvom međuispitu ili nije. Scenarij u kojem biste htjeli koristiti ovako 
nešto je sljedeći: postoje studenti koji nisu bili na prvom međuispitu, ali budući da nisu zadovoljili neki drugi (vanjski) uvjet, ipak nemaju pravo na nadoknadu. U tom slučaju,
za takve studente napravite ručni unos (fiksirate vrijednost zastavice na laž); za sve ostale studente, vrijednost će se postaviti temeljem ispitivanja prisutnosti na MI1.</p>

<p>Napomena: uvjet provjere ne mora biti ovako jednostavan kao u ovom primjeru. Odluka se može temeljiti na proizvoljno složenom Booleovom izrazu koji potencijalno gleda vrijednosti
drugih zastavica, vrijednosti provjera, provjerava je li student napisao kakvu prijavu i je li mu ona odobrena, je li uploadao potrebne datoteke i slično. Više primjera moguće
je pronaći u pomoći stranice za definiranje novih provjera, odnosno za definiranje novih zastavica.</p>
