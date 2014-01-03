<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div class="helpTitle1">Definiranje programa za izračun vrijednosti zastavice</div>

<p>Kako bi se olakšao rad s izračunom bodova u složenim slučajevima, te podržalo filtriranje studenata
glede prava izlaska za pojedine provjere znanja, sustav uvodi pomoćni koncept - zastavicu. Na jednom 
kolegiju možete stvoriti proizvoljan broj zastavica. Vrijednost zastavice može biti true ili false.
Vrijednosti zastavica možete uploadati preko tekstovnog zapisa, ili ih pak možete računati uživo.
program za izračun bodova provjere znanja može doći do vrijednosti zastavica pozivom funkcije flagValue(...),
baš kao što i program za izračun vrijednosti zastavice može doći do broja bodova provjere znanja
pozivom funkcije score(...).</p>

<p>Svaka zastavica za studenta prati jesu li vrijednosti zastavice uploadane putem tekstualne datoteke
(pa gaze izračunsku vrijednost) ili se računaju dinamički.</p>

<p>Evo jednostavnog primjera. Definiramo vrijednost zastavice "PZIPravo" - koja treba poprimiti vrijednost true
za one studente koji imaju pravo izaći na ponovljeni završni ispit. Pravila su Vam već poznata: svi studenti
koji ukupno nemaju barem 50 bodova ili na završnom ispitu nisu ostvarili barem 10 bodova nemaju pravo automatski
izlaze na ponovljeni završni ispit. Dodatno, studenti koji se sami jave (a njih ćete uploadati) imaju pravo pristupiti
ponovljenom završnom ispitu. Evo programa koji to računa:
</P>

<pre>
if(overrideSet()) {
  setValue(overrideValue());
} else {
  double s = score("LAB")+score("DZ")+score("MI1")+score("MI2")+score("ZI");
  setValue(s&lt;50 || score("ZI")&lt;10);
}
</pre>

<p>Kako program radi? Najprije provjerava je li vrijednost zastavice uploadana ručno (overrideSet()) i ako je, kao vrijednost zastavice
uzima onu koja je bila uploadana (overrideValue()). U suprotnom, računa se ukupno ostvareni broj bodova, i provjerava je li on
manji od 50 te je li na završnom ispitu student ostvario manje od 10 bodova.</p>

<p>Ovaj jednostavan primjer pokazuje što sve sustav prati za svakog korisnika:</p>
<table>
<tr><th>podatak</th><th>pojašnjenje</th></tr>
<tr><td>overrideSet()</td><td>provjerava je li za studenta vrijednost zastavice učitana iz datoteke</td></tr>
<tr><td>overrideValue()</td><td>vraća vrijednost zastavice koja je učitana iz datoteke; pozivati samo ako overrideSet() vrati true.</td></tr>
<tr><td>setValue(...)</td><td>definira konačnu vrijednost zastavice</td></tr>
</table>

<p>Za izračun vrijednosti zastavice na raspolaganju Vam stoje sljedeće funkcije:</p>
<table>
<tr><th>funkcija</th><th>pojašnjenje</th></tr>
<tr><td>flagValue(kratkoImeZastavice)</td><td>provjerava i vraća vrijednost zastavice čije kratko ime predajete kao argument</td></tr>
<tr><td>score(kratkoImeProvjere)</td><td>dohvaća konačni broj bodova ostvarenih na provjeri čije kratko ime predajete kao argument</td></tr>
<tr><td>present(kratkoImeProvjere)</td><td>provjerava i vraća je li student bio na provjeri čije kratko ime predajete kao argument</td></tr>
<tr><td>passed(kratkoImeProvjere)</td><td>provjerava i vraća je li student položio provjeru čije kratko ime predajete kao argument</td></tr>
<tr><td>hasApplication(kratkoImePrijave)</td><td>provjerava ima li student ispunjenu prijavu čije ste predali kratko ime</td></tr>
<tr><td>hasApplicationInStatus(kratkoImePrijave,status)</td><td>provjerava ima li student ispunjenu prijavu čije ste predali kratko ime i koja je u statusu koji ste naveli. Status je po tipu string, a može biti "NEW", "ACCEPTED" ili "REJECTED".</td></tr>
<tr><td>getApplicationElementValue(kratkoImePrijave,imeElementa)</td><td>vraća što je student popunio u elementu koji se nalazi u prijavi čija su imena zadana</td></tr>
<tr><td>getApplicationDate(kratkoImePrijave)</td><td>vraća datum (java.util.Date objekt) studentove ispunjene prijave čije ste predali kratko ime ili null ako prijava nije ispunjena.</td></tr>
</table>

<p>I konačno, provjera bodove može utvrđivati i na temelju datoteka koje je student uploadao u sustav kroz komponente, i koje su
   bile pregledane i bodovane. Za to Vam na raspolaganju stoje dvije metode:</p>
<table>
<tr><th>metoda</th><th>pojašnjenje</th></tr>
<tr><td>hasAssignedTask(kratkoImeKomponente, pozicijaDijelaKomponente, nazivZadatka)</td><td>metoda vraća true ili false, ovisno o tome je li studentu
        dodijeljen navedeni zadatak ili nije. Kratko ime komponente može biti bilo koje ime registrirano u sustavu; trenutno su to LAB, SEM i ZAD.</td></tr>
<tr><td>task(kratkoImeKomponente, pozicijaDijelaKomponente, nazivZadatka)</td><td>metoda vraća sam zadatak ili null, ovisno o tome je li studentu
        dodijeljen navedeni zadatak ili nije. Kratko ime komponente može biti bilo koje ime registrirano u sustavu; trenutno su to LAB, SEM i ZAD.</td></tr>
</table>

<p>Sam zadatak nudi sljedeće podatke:</p>
<table>
<tr><th>podatak</th><th>pojašnjenje</th></tr>
<tr><td>isLocked()</td><td>provjerava je li student zaključao zadatak</td></tr>
<tr><td>getLockingDate()</td><td>vraća datum zaključavanja (ili null ako nije zaključan).</td></tr>
<tr><td>isReviewed()</td><td>provjerava je li asistent ocijenio zadatak</td></tr>
<tr><td>isPassed()</td><td>provjerava je li asistent označio zadatak kao položen (ima smisla interpretirati samo ako je isReviewed()==true)</td></tr>
<tr><td>getScore()</td><td>vraća bodove koje je asistent dodijelio zadatku (ima smisla interpretirati samo ako je isReviewed()==true)</td></tr>
</table>
 
<p>Evo jednostavnog primjera. Na kolegiju ste definirali laboratorijske vježbe. Prva laboratorijska vježba ima zadatak nazvan "Upload rješenja".
   Sljedećim programom možemo uvažiti bodovanje tog zadatka:</p>

<pre>
double bodovi = 0;
boolean rjesio = hasAssignedTask("LAB",1,"Upload  rješenja")
                 && task("LAB",1,"Upload  rješenja").isLocked();
boolean prosao = false;
if(rjesio && task("LAB",1,"Upload  rješenja").isReviewed()) {
 prosao = task("LAB",1,"Upload  rješenja").isPassed();
 bodovi = task("LAB",1,"Upload  rješenja").getScore();
}

setPassed(prosao);
setPresent(rjesio);
setScore(bodovi);
</pre>

<p>Pogledajmo i jedan složeniji primjer u kojem je svakom studentu dodijeljeno više zadataka (primjerice, nekih 5 od definiranih 20). Pravila za prolaz
   su sljedeća: student je morao zaključati sve zadatke koje je dobio, i asistent je za te zadatke prilikom ocjenjivanja morao označiti prolaz. Bodovi
   su pri tome jednaki sumi bodova po svim dodijeljenim zadacima.</p>

<p>Jedna mogućnost za definiranje programa ocjenjivanja jest ručno ispitivanje svih 20 zadataka za svakog od studenata. No, to je nepotrebno, obzirom da
   sustav zna koje je zadatke od tih 20 student dobio. Ovdje u pomoć možemo pozvati konstrukt <br>
   <code>@TASKS_LOOP(kratkoImeKomponente, pozicijaDijelaKomponente, pomoćnaVarijabla)@ {...}</code><br> 
   koji će za nas dohvatiti sve studentu dodijeljene zadatke, i stvoriti te izvršiti petlju, pri čemu će u svakom prolazu pomoćnoj
   varijabli dodijeliti jedan od studentu dodijeljenih zadataka. Evo kako to možemo iskoristiti.</p>

<pre>
double totalScore = 0;
int numAssigned = 0;
int numLocked = 0;
int numPassed = 0;

// Za svaki dodijeljeni zadatak <b>t</b>
@TASKS_LOOP("LAB",1,t)@ {
  numAssigned++;
  if(t.isLocked()) {
    numLocked++;
  }
  if(t.isReviewed()) {
    totalScore += t.getScore();
    if(t.isPassed()) numPassed++;
  }
}

// Prosao je ako je broj dodijeljenih jednak broju zakljucanih i polozenih
setPassed(numAssigned==numLocked && numAssigned==numPassed);
// Bio je ako je zakljucao barem jedan zadatak
setPresent(numLocked&gt;0);
setScore(totalScore);
</pre>

<p>Pri tome je važno zapamtiti da se početak konstrukta <code>@TASKS_LOOP(</code> i njegov kraj <code>)@</code> moraju pisati upravo tako (bez razmaka).</p>
