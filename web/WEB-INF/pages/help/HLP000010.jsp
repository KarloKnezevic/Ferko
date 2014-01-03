<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div class="helpTitle1">Unos bodova studenata</div>

Koristeći odabrani formular možete obaviti unos bodova studenta na provjeri. 
U prostor za unos teksta za svakog korisnika potreban je jedan zapis u jednom retku. Pojedini elementi
moraju biti odvojeni znakom tab ili znakom ljestve. Elementi su redom:

<table border="1">
	<tr><th>jmbag</th><td>jmbag studenta</td></tr>
	<tr><th>grupa</th><td>grupa studenta na provjeri</td></tr>
	<tr><th>bodovi1</th><td>ostvareni broj bodova na 1. zadatku</td></tr>
	<tr><th>bodovi2</th><td>ostvareni broj bodova na 2. zadatku</td></tr>
	<tr><th>...</th><td>...</td></tr>
	<tr><th>bodoviN</th><td>ostvareni broj bodova na N-tom zadatku</td></tr>
</table>

Primjer unosa:

<code>
0012345678#1.5#2.4#1#1#1.5
0023456789#2.5#0.5#1.5#2#0.5
</code>

Napomena: za korisnike koji nisu pristupili provjeri nemojte unositi 0 bodova, jer će se time smatrati da su ti studenti
pristupili provjeri i ostvarili 0 bodova. Sustav Vam omogućava vođenje razlike između neizlaska i izlaska s ostvarenih 0
bodova pa pripazite na te detalje.
