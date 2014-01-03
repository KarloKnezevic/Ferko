<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div class="helpTitle1">Unos bodova studenata</div>

Koristeći odabrani formular možete obaviti unos bodova studenta na provjeri. 
U prostor za unos teksta za svakog korisnika potreban je jedan zapis u jednom retku. Pojedini elementi
moraju biti odvojeni znakom tab ili znakom ljestve. Elementi su redom:

<table border="1">
	<tr><th>jmbag</th><td>jmbag studenta</td></tr>
	<tr><th>broj bodova</th><td>ostvareni broj bodova</td></tr>
</table>

Primjer unosa:

<code>
0012345678#11.5
0023456789#21
</code>

Napomena: za korisnike koji nisu pristupili provjeri nemojte unositi 0 bodova, jer će se time smatrati da su ti studenti
pristupili provjeri i ostvarili 0 bodova. Sustav Vam omogućava vođenje razlike između neizlaska i izlaska s ostvarenih 0
bodova pa pripazite na te detalje.
