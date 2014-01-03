<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div class="helpTitle1">Unos korisnika</div>

Koristeći odabrani formular možete obaviti unos novih korisnika u sustav. U prostor za unos teksta
za svakog korisnika potreban je jedan zapis u jednom retku. Pojedini elementi moraju biti odvojeni
znakom tab ili znakom ljestve. Elementi su redom:

<table border="1">
	<tr><th>jmbag</th><td>jmbag studenta</td></tr>
	<tr><th>lastName</th><td>prezime studenta</td></tr>
	<tr><th>firstName</th><td>ime studenta</td></tr>
	<tr><th>username</th><td>korisničko ime; opcionalno</td></tr>
	<tr><th>email</th><td>email; opcionalno</td></tr>
</table>

Primjer unosa:

<code>
0012345678#Perić#Pero#pp34567#pp34567@pinus.cc.fer.hr
0023456789#Ivić#Ivo#ii45678#ii45678@pinus.cc.fer.hr
</code>
