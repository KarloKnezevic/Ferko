<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div class="helpTitle1">Definiranje programa za izračun vrijednosti bodova</div>

<p>Svaka provjera znanja za studenta prati dvije vrste bodova: "sirove" i "stvarne" bodove.
Primjerice, prilikom organizacije ispita na obrazac koji ima 20 pitanja, često ćete radi jednostavnosti
studentima reći da svaki točno odgovoreni zadatak nosi +1 bod, a svaki netočno odgovoreni zadatak -1/4 boda,
odnosno -0.25 jer je tako jednostavnije računati. No, kako sama provjera treba nositi 30 bodova, ostvareni
broj bodova svima ćete naknadno pomnožiti s korekcijskim faktorom 1.5. Dodatno, možete prirediti još 5 zadataka
(tako da sama provjera ima ukupno 25 zadataka) i reći studentima od tih 25 zadataka trebaju za maksimalni broj
bodova riješiti njih 20 točno, a ako ih netko riješi i više, broj bodova odsiječi ćete na maksimumu (dakle, na 20,
odnosno na 30 nakon množenja). Kada propustite ovakvu provjeru kroz skener i po pravilu +1/-0.25 izraćunate
ukupan broj bodova, sustav taj podatak prati kao <b>rawScore</b>. Vaš je zadatak temeljeći se na toj vrijednosti
sustavu pojasniti kako doći do stvarnog broja bodova, koji je označen s <b>score</b>. Evo jednog primjera programa:</p>

<pre style="background-color: #eeeeee;">
double s = rawScore() * 1.5;  // sirove bodove pomnoži s faktorom 1.5 i pohrani su pomoćnu varijablu
if(s&gt;30) s = 30;              // ako je time broj bodova prekoračio 30, odsijeci to na 30  
setScore(s);                  // postavi konačni broj bodova
setPassed(s&gt;=15);             // reci da je student položio provjeru ako ima barem 15 bodova
setPresent(rawPresent());     // stvarnu vrijednost zastavice "prisutan" preuzmi iz "sirovih" podataka
</pre>

<p>Ovaj jednostavan primjer pokazuje što sve sustav prati za svakog korisnika na provjeri:</p>
<table>
<tr><th>podatak</th><th>pojašnjenje</th></tr>
<tr><td>rawScore()</td><td>sirovi broj bodova ostvaren ocjenjivanjem provjere</td></tr>
<tr><td>rawPresent()</td><td>boolean zastavica koja je true ako je korisnik pristupio toj provjeri, a false inače</td></tr>
<tr><td>setScore(...)</td><td>definira konačan broj bodova</td></tr>
<tr><td>setPassed(...)</td><td>definira je li student položio provjeru</td></tr>
<tr><td>setPresent(...)</td><td>definira je li student pristupio provjeri</td></tr>
</table>

<p>Pogledajmo još jedan slučaj. Organizirate 3 laboratorijske vježbe, i pravila su sljedeća:</p>
<ul>
  <li>Na svakoj vježbi student može dobiti od 5 do 10 bodova, ovisno o varijanti vježbe koju odabere i kvaliteti rješenja.</li>
  <li>Da bi položio laboratorijske vježbe, student mora obaviti barem dvije vježbe, te ukupno skupiti barem 7.5 bodova.</li>
  <li>Student ukupno ne može dobiti više od 15 bodova (primijenit ćete odsijecanje).</li>
</ul>

<p>Ovakve uvjete tipično ste do sada morali programirati u tabličnim kalkulatorima. Međutim, sada više za to nema potrebe. Dovoljno je na kolegiju u strukturu
provjera dodati novu <i>virtualnu</i> provjeru: "Laboratorijske vježbe" (kratko ime LV), i kao program napisati sljedeće:</p>

<pre style="background-color: #eeeeee;">
int brojLabosa = 0;                // Na koliko je labosa ukupno bio?

if(present("LAB1")) brojLabosa++;  // Ako je bio na prvom, uvečaj za jedan
if(present("LAB2")) brojLabosa++;  // Ako je bio na drugom, uvečaj za jedan
if(present("LAB3")) brojLabosa++;  // Ako je bio na trećem, uvečaj za jedan

double s = score("LAB1") + score("LAB2") + score("LAB3");  // Koliko ukupno bodova ima?
if(s&gt;15) s = 15;              // Ako je time broj bodova prekoračio 30, odsijeci to na 30.  

setScore(s);                  // Postavi konačni broj bodova.
setPassed(s&gt;=7.5);            // Reci da je student položio provjeru ako ima barem 7.5 bodova.
setPresent(brojLabosa&gt;0);     // Na labosima je bio ako je bio na barem jednom.
</pre>

<p>Za izračun bodova na raspolaganju Vam stoje sljedeće funkcije:</p>
<table>
<tr><th>funkcija</th><th>pojašnjenje</th></tr>
<tr><td>score(kratkoImeProvjere)</td><td>dohvaća konačni broj bodova ostvarenih na provjeri čije kratko ime predajete kao argument</td></tr>
<tr><td>present(kratkoImeProvjere)</td><td>provjerava i vraća je li student bio na provjeri čije kratko ime predajete kao argument</td></tr>
<tr><td>passed(kratkoImeProvjere)</td><td>provjerava i vraća je li student položio provjeru čije kratko ime predajete kao argument</td></tr>
<tr><td>assessmentScore(kratkoImeProvjere)</td><td>dohvaća konačni broj bodova ostvarenih direktno na provjeri čije kratko ime predajete kao argument (ovo isključuje mehanizam preuzimanja rezultata od ulančane provjere)</td></tr>
<tr><td>assessmentPresent(kratkoImeProvjere)</td><td>provjerava i vraća je li student bio direktno na provjeri čije kratko ime predajete kao argument (ovo isključuje mehanizam preuzimanja rezultata od ulančane provjere)</td></tr>
<tr><td>assessmentPassed(kratkoImeProvjere)</td><td>provjerava i vraća je li student direktno položio provjeru čije kratko ime predajete kao argument (ovo isključuje mehanizam preuzimanja rezultata od ulančane provjere)</td></tr>
<tr><td>flagValue(kratkoImeZastavice)</td><td>provjerava i vraća vrijednost zastavice čije kratko ime predajete kao argument</td></tr>
<tr><td>hasApplication(kratkoImePrijave)</td><td>provjerava ima li student ispunjenu prijavu čije ste predali kratko ime</td></tr>
<tr><td>hasApplicationInStatus(kratkoImePrijave,status)</td><td>provjerava ima li student ispunjenu prijavu čije ste predali kratko ime i koja je u statusu koji ste naveli. Status je po tipu string, a može biti "NEW", "ACCEPTED" ili "REJECTED".</td></tr>
<tr><td>getApplicationElementValue(kratkoImePrijave,imeElementa)</td><td>vraća što je student popunio u elementu koji se nalazi u prijavi čija su imena zadana</td></tr>
<tr><td>getApplicationDate(kratkoImePrijave)</td><td>vraća datum (java.util.Date objekt) studentove ispunjene prijave čije ste predali kratko ime ili null ako prijava nije ispunjena.</td></tr>
</table>

<p>Vjerojatno je potrebno pojasniti razlike između porodica funkcija assessmentXXX() te XXX() (npr. assessmentPresent("MI1") i present("MI1")). Pretpostavimo da ste napravili 
   1. međuispit, i rezultate pohranili kao provjeru kratkog imena MI1. Za studente koji nisu bili na toj provjeri iz opravdanih razloga, dva tjedna kasnije napravili ste
   nadoknadu 1. međuispita i rezultate pohranili kao provjeru MI1N. Potom ste definirali da je ulančani roditelj od MI1N upravo MI1. Ovime ste definirali da MI1 svoje
   efektivne bodove dohvaća iz MI1N ako je student bio na toj provjeri, a ako nije, onda iz rezultata koji su učitani u samu provjeru MI1 (ovo se događa automatski, bez potrebe
   da pišete program). Uzmimo sada za primjer studenta "Pero Perić" koji nije bio na MI1, već je pisao MI1N. Za njega vrijedi sljedeće:</p>

<table>
<tr><td>present("MI1N")==true</td><td>naime, student je bio na nadoknadi i tamo ima direktno učitane bodove</td></tr>
<tr><td>assessmentPresent("MI1N")==true</td><td>jer je student bio upravo na toj provjeri</td></tr>
<tr><td>present("MI1")==true</td><td>naime, kako je MI1 ulančani roditelj od MI1N, MI1 je rezultat studenta dohvatio od MI1N i integrirao kao svoj efektivni rezultat</td></tr>
<tr><td>assessmentPresent("MI1")==false</td><td>student nije bio direktno na provjeri MI1</td></tr>
</table>

<p>Dakle, najčešće porodicu funkcija assessmentXXX() nećete trebati koristiti jer one nude specifičan dohvat rezultata same provjere; preporuka je koristiti funkcije
   koje rade s efektivnim rezultatima i koje se oslanjaju na ugrađene mehanizme.</p>

<p>Možete koristiti i sljedeću metodu:</p>
<table>
<tr><th>metoda</th><th>pojašnjenje</th></tr>
<tr><td>sumChildren();</td><td>Pozivom ove metode sustav sam računa sumu bodova na svim provjerama koje su definirane kao djeca ove provjere, i tu sumu
    postavlja kao ostvareni broj bodova. Zastavice <code>present</code> i <code>passed</code> postavljaju se na <code>true</code> ako je student pristupio
    barem jednoj provjeri - djetetu.</td></tr>
</table>

<p>Primjer uporabe <code>sumChildren();</code>:</p>

<pre style="background-color: #eeeeee;">
sumChildren();
</pre>

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

<pre style="background-color: #eeeeee;">
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

<pre style="background-color: #eeeeee;">
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
