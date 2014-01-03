<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div class="helpTitle1">Ažuriranje oznaka studenata</div>

<p>Ovom akcijom možete ažurirati oznake studenata u prikazanoj grupi. U kutiju za unos potrebno
je upisati ili kopirati podatke o željenim oznakama, pri čemu je svaki redak sljedećeg formata:</p>

<table border="1">
  <tr><th>jmbag</th><td>jmbag studenta</td></tr>
  <tr><th>groupName</th><td>naziv grupe, ili % za bilo koja grupa</td></tr>
  <tr><th>tagName</th><td>naziv grupe, ili može ostati prazno ako tag treba postaviti na null.</td></tr>
</table>

<p>Elementi u svakom retku međusobno moraju biti razdvojeni znakom "tab" ili znakom ljestvi ("#").</p>

<p>Podešavanje oznaka studenata (ukoliko već nisu izvorno dobro podešene) nužno je ukoliko želite koristiti
burzu grupa za zamjene studenata, kako biste mogli podesiti da se demosi mogu mijenjati s demosima, a klasični
studenti s klasičnima.</p>
