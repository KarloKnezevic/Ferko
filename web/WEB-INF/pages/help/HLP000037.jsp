<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div class="helpTitle1">Ručni masovni unos vrijednosti zastavica</div>

<p>Na ovoj stranici moguće je obaviti masovni unos vrijednosti zastavica. U tu svrhu potrebno je pripremiti podatke u formatu opisanom u nastavku. Za svakog studenta kojemu se ručno
unose podatci treba postojati jedan redak oblika:</p>

<pre>
JMBAG tab_ili_ljestve 0_ili_1
</pre>

<p>Pri tome se kao separator koristi ili "tab" ili znak ljestvi "#", ali ne oboje, i nikako ne miješano. JMBAG pri tome mora biti punog formata (10 znamenaka), a vrijednost zastavice
je 0 ili 1. Evo primjera:</p>

<pre>
0012345678#1
0012345689#0
0012345690#1
</pre>

<p>U ovom primjeru ručna vrijednost definirana je za tri studenta. Pokretanjem ove akcije prethodne vrijednosti drugih studenata se ne brišu -- akcija radi inkrementalno.
Brisanje svih ručno unesenih vrijednosti moguće je pokrenuti s početne stranice provjera i zastavica, gdje se nalazi popis svih zastavica.</p>

<p>Više informacija o radu sa zastavicama te o ručnom i programskom izračunu možete potražiti u pomoći dostupnoj na stranici za uređivanje/dodavanje zastavice, te na stranici
za pregled/uređivanje vrijednosti zastavica.</p>
