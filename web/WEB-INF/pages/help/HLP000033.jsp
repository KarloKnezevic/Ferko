<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div class="helpTitle1">Izrada rasporeda</div>

<p>Izrada rasporeda omogućava Vam automatsko generiranje rasporeda za nadoknadu bilo koje nastavne obveze, te njihov naknadni pregled i izmjenu po potrebi.</p>
   
<h3>Izrada novog rasporeda</h3>
<p>Pokreće aplikaciju za izradu novog rasporeda.</p>
<br>
<p><b>Aplikacija za izradu rasporeda</b></p>
<img src="help/raspored/raspored-applet.jpg" width="615" height="488"  border="0" style="margin-bottom: 10px; margin-top: 10px;"/>

<p>Na samom vrhu prozora nalazi se prostor za unos naziva rasporeda, koji ne smije biti kraći od 5 znakova. Odmah desno od prostora za unos nalaze se 2 gumba. Prvi omogućuje definiranje parametara plana, dok drugi pokreće pripremu plana jednom kad je završen unos. Na lijevoj strani središnjeg dijela aplikacije nalazi se popis stvorenih događaja, a središnji dio zauzimaju parametri samih događaja. Na dnu prozora nalaze se 3 liste koje prikazuju postavke definirane na trenutnoj razini, te oznaku koja je razina trenutno aktivna.
	Aplikacija za izradu rasporeda omogućuje korisniku definiranje parametara na 3 razine:
<ul>
<li>razina čitavog plana</li>
<li>razina pojedinog događaja</li>
<li>razina pojedinog termina unutar događaja</li>
</ul>
</p>

<i><u>Definiranje parametara na razini čitavog plana</u></i>
<p>Klikom na gumb "Parametri rasporeda", kao aktivna razina postavlja se čitav plan, te se korisniku omogućuje definiranje postavki na toj razini. Upisom novog imena
   događaja i klikom na "Stvori događaj", moguće je dodati novi događaj u plan, te se tada kao aktivna razina postavlja upravo stvoreni događaj.</p>
<p>Klikom na gumb "Dodaj parametar" ispod bilo koje od lista na dnu prozora, omogućuje se definiranje određenog parametra.</p>
<p>Ukoliko se odabere definiranje korisnika, otvara se novi prozor u kojem je moguće dodati korisnike prema pripadnosti bilo kojoj od grupa definiranih na razini
   kolegija, ili ručnim unosom JMBAG-ova (svaki JMBAG u novi red). Pritiskom na "Dodaj parametar", odabrani se parametri prenose u aplikaciju i vidljivi su u listi.</p>
<img src="help/raspored/raspored-osobe.jpg" width="347" height="239"  border="0" style="margin-bottom: 10px; margin-top: 10px;"/>
<p>Ukoliko se odabere definiranje vremenskog raspona, korisniku se otvara novi prozor koji omogućuje definiranje vremenskog raspona u kojem se određeni plan mora
   izvršiti.</p>
<img src="help/raspored/raspored-vrijeme.jpg" width="330" height="226"  border="0" style="margin-bottom: 10px; margin-top: 10px;"/>
<p>Odabirom definiranja lokacija, korisniku se također prikazuje novi prozor u kojem je moguće odabrati lokacije. Korisniku se pored oznake dvorane prikazuje i
   kapacitet za pojedini način korištenja. Način korištenja (predavanje, ispit, lab. vježba) moguće je odabrati iz padajućeg izbornika. Također, moguće je i ručno
   definirati kapacitet dvorane, ukoliko se primjerice želi odraničiti broj studenata u pojedinom terminu.</p>
<img src="help/raspored/raspored-lokacije.jpg" width="323" height="386"  border="0" style="margin-bottom: 10px; margin-top: 10px;"/>
<p>Ukoliko se parametar definira na razini čitavog plana, on se prenosi na svaki događaj u planu. Primjerice, studenti definirani na razini plana, moraju se pojaviti u
   svakom od definiranih događaja. Vremenski raspored definiran na razini plana znači da se svi definirani događaji moraju odviti unutar tog vremenskog raspona.
   Dvorane definirane na razini plana stoje na raspolaganju terminima svih definiranih događaja.</p>

<i><u>Definiranje parametara na razini događaja</u></i>
<p>Osnovne stvari koje je moguće definirati na razini događaja jesu: trajanje termina pojedinog događaja, preduvjeti za događaj, te način raspodjele. Uz to, moguće
   je definirati i parametre vezane uz studente, vremenski raspon i lokacije, na način jednak kao kod definiranja na razini plana.</p>
<p>Trajanje termina moguće je postaviti na vrijednosti od 15 minuta do 12 sati.
<p>Preduvjeti za događaj definiraju koji se događaj mora dogoditi prije događaja koji uređujemo, te sa kolikim vremenskim odmakom. Klikom na dodavanje preduvjeta,
   korisniku se prikazuje novi prozor u kojem mu se omogućava odabir već postojećeg događaja kao preduvjeta, te definiranje vremenskog razmaka između događaja.
   Vremenski razmak od 1 dana primjerice znači da ukoliko se događaj koji je preduvjet odvija 13.12., trenutni događaj se može odviti tek 15.12. Ili kasnije. Moguće
   je definirati više preduvjeta za pojedini događaj.</p>
<img src="help/raspored/raspored-preduvjeti.jpg" width="482" height="227"  border="0" style="margin-bottom: 10px; margin-top: 10px;"/>
<p>Način raspodjele je moguće definirati na 2 načina. Kao slučajnu distribuciju, te kao zadanu distribuciju. Ukoliko je odabrana slučajna distribucija, potrebno je
   unijeti minimalan i maksimalan broj termina koji će se stvoriti za pojedini događaj. Ukoliko je distribucija zadana, korisniku se omogućuje dodavanje proizvoljnog
   broja termina, te kasnije definiranje parametara vezanih uz taj termin. Klikom na dodani termin, on postaje aktivnom razinom za definiranje postavki studenata,
   vremena i lokacija.</p>
<p>Ukoliko se postavke studenata definiraju na razini događaja (ili su preuzete sa razine plana) tada se svi zadani studenti raspoređuju po terminima, tako da jedan
   student ne može biti u 2 termina. Vremenski rasponi koji su tako definirani znače da se svi termini moraju odviti u tim određenim vremenskom rasponima. Lokacije
   koje su definirane na taj način znače da svaka definirana lokacija stoji na raspolaganju svim terminima tog događaja.</p>

<i><u>Definiranje parametara na razini termina</u></i>
<p>Parametri koji se mogu definirati na razini termina su parametri vezani uz studente, vremenske raspone i lokacije, Zadaju se na jednak način kao i za razinu plana.
   Ukoliko su studenti zadani na razini termina, to znači da se svi studenti moraju nalaziti u određenom terminu. Ukoliko su za termin
   definirani vremenski rasponi, to znači da se taj termin mora odviti unutar jednog od tih definiranih raspona. Ukoliko su lokacije definirane na razini termina, to
   znači da sve te lokacije stoje na raspolaganju trenutnom terminu.</p>

<br>
<p><b>Priprema plana i lokalna izrada rasporeda</b></p>
<p>Klikom na gumb "Pripremi plan" pokreće se provjera ispravnosti plana, te dohvaćanje podataka o zauzećima studenata i dvorana. Rezultat tog procesa jest .jar
   datoteka koju je moguće preuzeti sa poslužitelja klikom na gumb "Pokreni izradu". Pokretanjem .jar datoteke otvara se aplikacija za lokalnu izradu rasporeda.</p>
<img src="help/raspored/raspored-priprema.jpg" width="557" height="512"  border="0" style="margin-bottom: 10px; margin-top: 10px;"/>
<p>Aplikacija za lokalnu izradu rasporeda omogućuje korisniku odabir algoritma kojim će se njegov plan pokušati izraditi. Algoritmi se biraju tako da se u prikazanom dijalogu
   postavi prioritet svakom od algoritama. Prioritetom se određuje odnos u izvođenju algoritama (u primjeru prikazanom na slici će se BCO i Clonalg
   algoritmi odvijati istim prioritetom, dok bi u slučaju da je BCO postavljen na 2, odnos BCO:Clonalg bio 2:5). Klikom na "Pokreni izvođenje",
   počinje izrada rasporeda. Korisnik u svakom trenutku može pratiti napredak algoritma putem grafa koji prikazuje trenutno najbolje rješenje.</p>
<img src="help/raspored/raspored-lokalno.jpg" width="795" height="557"  border="0" style="margin-bottom: 10px; margin-top: 10px;"/>
<p>Parametri rješenja koji se prikazuju su kako slijedi: broj preduvjeta koji nisu zadovoljeni, broj konflikata za prostorije, broj konflikata za studente, broj
   prenapučenih dvorana, broj termina, te broj nepopunjenih mjesta u odabranim prostorijama. Broj konflikata za prostorije predstavlja broj 15-minutnih intervala u
   kojima je neka od prostorija zauzeta od strane 2 termina. Broj konflikata studenata predstavlja broj 15-minutnih intervala u kojima neki od studenata ima 2 zauzeća.
   Crvenom bojom su prikazana teška ograničenja, odnosno parametri koji moraju biti jednaki 0 da bi raspored bio ispravan (primjerice, u ovom slučaju su prva 4
   parametra teška ograničenja), dok su laka ograničenja prikazana plavom bojom i ona služe samo kako bi se dobio još bolji ispravan raspored.</p>
<img src="help/raspored/raspored-lokalno-rezultati.jpg" width="723" height="743"  border="0" style="margin-bottom: 10px; margin-top: 10px;"/>
<p>Klikom na "Prekid izvođenja", zaustavlja se rad algoritma, te se prikazuje najbolje rješenje, koje ne mora nužno biti ispravno. Klikom na "Pohranjivanje rješenja"
   rješenje se prebacuje u XML format pogodan za korištenje unutar sustava Ferko.</p>
<img src="help/raspored/raspored-lokalno-kraj.jpg" width="725" height="743"  border="0" style="margin-bottom: 10px; margin-top: 10px;"/>
<br>
<br>


<h3>Moji rasporedi</h3>
<p>Omogućava Vam pregled popisa svih dosad pohranjenih rasporeda, te prikazuje dodatne opcije vezane uz te rasporede.
  Dodatne opcije su:
<ul>
<li>pokretanje lokalne izrade rasporeda</li>
<li>unos lokalno izrađenog raporeda</li>
<li>izmjena plana</li>
<li>priprema plana</li>
</ul>
<img src="help/raspored/raspored-mainpage.jpg" width="554" height="173"  border="0" style="margin-bottom: 10px; margin-top: 10px;"/>
<br>

<p><b>Pokretanje lokalne izrade rasporeda</b>
Pokretanje lokalne izrade ponovno Vam omogućuje pruzimanje .jar datoteke čijim se pokretanjem otvara aplikacija za lokalnu izradu rasporeda.</p>
	
<p><b>Unos lokalno izrađenog rasporeda</b>
Omogućuje upload XML datoteke koju je generirala aplikacija za lokalnu izradu, kako bi se taj raspored mogao prenijeti u sustav Ferko.</p>

<p><b>Izmjena plana</b>
Izmjena plana pokreće aplikaciju identičnu onoj za izradu novog rasporeda, no sa već unesenim parametrima od kojih se sastojao raspored koji želite izmijeniti.</p>

<p><b>Priprema plana</b>
Omogućuje Vam automatsko osvježavanje plana sa novim podacima o zauzećima studenata. Ovo je posebno korisno ukoliko ponovno želite pokrenuti izradu istog rasporeda, ali želite uzeti u obzir i moguća nova zauzeća.</p>