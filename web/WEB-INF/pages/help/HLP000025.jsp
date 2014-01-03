<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div class="helpTitle1">Prijave</div>

<p>Na ovoj stranici možete definirati novu prijavu ili uređivati postojeću. Uporabom prijava možete
studente pitati da se izjasne oko aktivnosti koje trebate organizirati. Primjerice, ako na kolegiju
dozvoljavate nadokandu samo jedne laboratorijske vježbe, možete pitati studente koji nisu odradili
sve vježbe da odaberu koju od propuštenih vježbi žele nadoknaditi. Ukoliko dozvoljavate nadoknadu
samo jedne od provjera znanja, uporabom prijava možete pitati studente da odaberu koju provjeru
žele nadoknaditi.</p>

<b>Općenito o prijavama</b>

<p>Prilikom definiranja nove prijave dužni ste unijeti naziv prijave (koji vide i studenti), kratki naziv
prijave (po kojem kasnije programski možete dohvaćati prijave studenata) te datum od kada do kada je prijava
otvorena za studente. Prijave čije vrijeme još nije nastupilo ili one čije je vrijeme prošlo studenti neće
vidjeti (osim ako se na njih nisu prijavili). Prijavu možete i dodatno podesiti, no o tome <a href="#detaljnaDef">malo kasnije</a>.</p>

<b>Što vide studenti?</b>

<p>Kada dođu na stranicu s popisom prijava, studenti vide samo one prijave na koje se mogu prijaviti. Odabirom
neke od ponuđenih prijava student dolazi do detaljnog formulara gdje popunjava sve podatke koje ga prijava traži
i pohranjuje prijavu. Nakon što ispuni prijavu, student je više ne može povući. Ponovnim dolaskom do iste prijave
student vidi status prijave, koji će u tom trenutku biti "zaprimljeno".</p>

<b>Što radite Vi?</b>

<p>Periodički možete provjeriti ima li novih prijava, i ako ima, možete ih odobriti ili odbiti. Ako prijavu odbijate,
poželjno je da studentu u odgovarajuće polje unesete i obrazloženje - zašto mu je prijava odbijena. Kada student
dođe na stranicu prijave, vidjet će status prijave kao i Vaše obrazloženje, ako ste ga unijeli.</p>

<b>Uporaba prijava</b>

<p>Jednom kada su prijave zaključene, možete ih koristiti na dva načina:</p>
<ul>
  <li><i>Istakanjem u Microsoft Excel ili Csv</i> dobit ćete podatke o svim studentima koji su se prijavili i moći ćete
      te podatke dalje obrađivati izvan Ferka.</li>
  <li><i>Kroz programsko sučelje</i> koje Vam Ferko stavlja na raspolaganje moći ćete podatcima o prijavama pristupiti
      na svim mjestima na kojima u Ferku možete koristiti Ferkov API. Primjerice, u programu za definiranje bodova
      provjere znanja, u programu za definiranje vrijednosti zastavice ili u programu za detaljno definiranje neke
      druge prijave.</li>
</ul>

<a name="detaljnaDef"></a><b>Detaljno podešavanje parametara prijave</b>

<p>Ako polje za detaljno podešavanje ostavite prazno, svaki će student prilikom ispunjavanja prijave vidjeti samo
jedno dodatno polje koje treba unijeti - "razlog". Tekst koji tamo upiše Vama će biti dostupan prilikom pregleda
prijave te prilikom istakanja podataka o prijavljenim studentima. Ukoliko Vam to nije dovoljno, možete napraviti
detaljno podešavanje prijave. Evo kako.</p>

<p>Detaljno podešavanje prijave omogućit će Vam potpunu kontrolu nad elementima prijave koji se prikazuju studentu.
Pri tome pod pojmom potpunu kontrolu doista mislimo na sve - od toga što točno pitate studenta (da li samo "razlog", 
ili mu nudite neke opcije na odabir, ili ga pitate da odabere još neke studente kada prijavljuje tim studenata) do 
toga koji studenti točno vide prijavu, odnosno koje dijelove prethodno definirane prijave. Ovo možda djeluje komplicirano,
no pogledajmo to na primjeru.</p>

<b>Primjer 1. Prijava za dolazak na 1. laboratorijsku vježbu.</b>

<p>Pogledajmo jedan stvaran primjer. Na kolegiju <i>Umjetna inteligencija</i> studentima smo ponudili deset varijanti
prve laboratorijske vježbe. Htjeli smo od studenata da sami odaberu koju će varijantu raditi, u kojem će jeziku raditi
tu varijantu, te ako rade u timu, koji su još studenti u timu osim onog studenta koji ispunjava prijavu. Evo kako to
možemo riješiti.</p>

<pre>
# Prijava za dolazak na labos 1
# -----------------------------

@@@def

message("m0", "Molim ispunite prijavu za labos.");
chooseOne(
  "varijanta", "Koju ste varijantu vježbe odabrali?",
  option("1.1","1.1"),
  option("1.2","1.2"),
  option("1.3","1.3"),
  option("1.4","1.4"),
  option("1.5","1.5"),
  option("1.6","1.6"),
  option("1.7","1.7"),
  option("1.8","1.8"),
  option("1.9","1.9"),
  option("1.10","1.10")
);
chooseOne(
  "jezik", "Odaberite jezik u kojem rješavate labos.",
  option("c","C"),
  option("c++","C++"),
  option("c#","C#"),
  option("java","Java"),
  option("python","python"),
  option("haskell","Haskell"),
  other("drugo","Ako radite u nekom drugom jeziku, ovdje upišite u kojem.")
);
// Ako se radi u timu, prijavu popunjava samo jedan član tima, i ovdje navodi ostale. Dozvoljavamo još max 4 studenta, tako da tim ukupuno ima maksimalno 5 studenata.
students(
  "tim", 0, 4, "Ako zadatak rješavate u timu, ovdje unesite JMBAG-ove preostalih članova tima (bez Vašeg). JMBAG-ove razdvojite enter-om, razmakom ili zarezom."
);
</pre>

<p>Uočimo: detaljna specifikacija sastoji se od jedne ili više sekcija. Sekcije započinju sa tri znaka @, i ovdje 
imamo samo jednu sekciju: <code>def</code>. Ova sekcija služi za definiranje elemenata koje prikazujemo studentima. 
Sve linije koje započinju znakom ljestvi (#) sustav automatski tretira kao komentare.
Svaki element ima minimalno svoje jedinstveno kratko ime - u našem primjeru to su "m0", "varijanta", "jezik" te "tim".
U ovoj konkretnoj prijavi prikazujemo četiri elementa. Prvi element (message) studentu jednostavno ispisuje poruku koju smo naveli.
Drugi i treći element (chooseOne) studenta traže da odabere jednu od ponuđenih stavki. Prvi argument opcije je ključ koji 
ćete Vi vidjeti prilikom istakanja prijava, a drugi tekst koji se studentu prikazuje. Uočite također da drugi element chooseOne
kao zadnju opciju ima "other" što će studentu omogućiti da je odabere i da sam upiše u kojem jeziku radi (ako mu niti jedan
od ponuđenih ne odgovara). Konačno, posljednji element (students) studentu omogućava da upiše još maksimalno 4 (minimalno 0 ako
radi sam) JMBAG-ova studenata koji su s njim u timu.</p>

<b>Primjer 2. Nadoknada laboratorijske vježbe.</b>

<p>Pretpostavimo da na Vašem kolegiju imate 5 laboratorijskih vježbi, i da student može nadoknaditi svaku (uz prethodnu prijavu, kako biste
znali s koliko studenata trebate računati). Napravit ćete 5 različitih prijava, i u svaku staviti jednostavan program poput sljedećeg (konkretan
primjer pokazuje definiciju prijave za 4. laboratorijsku vježbu):</p>

<pre>
# prijava nadoknade labosa

@@@def

message("m0", "Na ovom mjestu možete se prijaviti za nadoknadu 4. laboratorijske vježbe. Moguće je nadoknaditi samo jednu laboratorijsku vježbu.");
text("razlog", "Unesite razlog prijave.");
</pre>

<p>Dapače, ovdje navedeni program uopće nije potreban - naime, ako se program ne navede, sustav automatski studentu nudi prijavu u kojoj treba popuniti
samo jedno polje: "razlog". Prednost programski definirane prijave jest u tome što možete podesiti i poruku koju ispisujete studentu.</p>

<b>Primjer 3. Nadoknada jedne laboratorijske vježbe.</b>

<p>U ovom scenariju studentima dozvoljavate da nadoknade najviše jednu od propuštenih laboratorijskih vježbi; dakako, samo uz prethodnu prijavu. Pri tome
studentima na odabir želite ponuditi samo one vježbe koje su doista i propustili. Ako evidenciju dolaznosti i bodova vodite kroz Ferko - ništa lakše. Evo
programa koji to radi.</p>

<pre>
# Prijava nadoknade labosa jedne od prve četiri vježbe
# ----------------------------------------------------

message("m0", "Na ovom mjestu možete se prijaviti za nadoknadu JEDNE propuštene laboratorijske vježbe. Moguće je nadoknaditi samo jednu laboratorijsku vježbu.");
text("razlog", "Iz kojeg razloga vježbu niste odradili u svom terminu?");
chooseOne(
  "vjezba", "Odaberite vježbu koju nadoknađujete.",
  option("lab1","Prva laboratorijska vježba"),
  option("lab2","Druga laboratorijska vježba"),
  option("lab3","Treća laboratorijska vježba"),
  option("lab4","Četvrta laboratorijska vježba")
);

@@@filter

applicationEnabled(!present("LAB1")||!present("LAB2")||!present("LAB3")||!present("LAB4"));

@@@filter("vjezba")

optionEnabled("lab1",!present("LAB1"));
optionEnabled("lab2",!present("LAB2"));
optionEnabled("lab3",!present("LAB3"));
optionEnabled("lab4",!present("LAB4"));
</pre>

<p>Program se sastoji od 3 sekcije. Objasnimo ih redom.</p>
<ul>
<li>Sekcija "def" je sada već jasna: definira se izgled prijave.</li>
<li>Sekcija "filter" (bez argumenata) je sekcija koja se izvodi odmah nakon definicije (neovisno o položaju sekcije u programu) i omogućava reguliranje
treba li prijavu uopće prikazati studentu (vidi li je student kada otiđe na stranicu s prijavama ili ne). Ovo se regulira pozivom metode 
<code>applicationEnabled(boolean isEnable)</code>. U našem slučaju, prijava je omogućena ako nije istina da je student bio na 1. laboratorijskoj vježbi,
ili ako nije istina da je student bio na 2. laboratorijskoj vježbi, ili ako nije istina da je student bio na 3. laboratorijskoj vježbi, ili
ako nije istina da je student bio na 4. laboratorijskoj vježbi. Ako je student bio na svim vježbama, uvjet će se evaluirati u <code>false</code> i prijava
će za tog studenta postati onemogućena, a time i nevidljiva.</li>
<li>Sekcija "filter" s argumentom "vježba" omogućava filtriranje opcija elementa "vjezba" (tako smo u sekciji "def" nazvali element koji studenta pita da
odabere koju vježbu nadoknađuje). Potom pozivom metode <code>optionEnabled(boolean isEnabled)</code> za svaku opciju (koristimo kratko ime opcije; opcije su
bile definirane u sekciji "def") definiramo je li omogućena (a time i vidljiva) ili nije. Logika je jasna: opcija nadoknade labosa 1 omogućena je samo ako
student nije bio na labosu 1. Slično vrijedi i za preostale opcije.</li>
</ul>

<p>Prilikom korištenja ove vrste programa važno je zapamtiti sljedeće. Sekcija "def" služi isključivo definiranju strukture prijave; u toj sekciji uporaba
funkcija koje dohvaćaju podatke o studentu nije moguća, a pokušaj uporabe prijavit će pogrešku. Od funkcija koje Vam stoje na raspolaganju implementirane su
sve koje se nude i u programima za definiranje bodova provjera i vrijednosti zastavica. U sekcijama "filter" moguća je uporaba navedenih funkcija.</p>

<b>Primjer 4. Prijava za ponavljanje međuispita odnosno završnog ispita.</b>

<p>Ovo je jedan od kompliciranijih scenarija, pa pokažimo najprije kod.</p>

<pre>
# Prijava ponovljene provjere
# ---------------------------

@@@global

int brojPalih;

@@@def

message("m0", "Na ovom mjestu možete se prijaviti za ponovljenu provjeru. Pažnja: prijavom gubite sve bodove na originalnoj provjeri!");
chooseOne(
  "provjera", "Odaberite provjeru koju ponavljate.",
  option("mi1","Prvi međuispit"),
  option("mi2","Drugi međuispit"),
  option("zi","Završni ispit")
);

@@@filter

// Ako je pao više od jedne provjere, više nema šanse pa nema prijava.
// Samo ako nije pao ništa, ili je pao jednu prijavu, studentu prikazujemo prijavu

int brojPalih = 0;
if(!passed("MI1")) brojPalih++;
if(!passed("MI2")) brojPalih++;
if(!passed("ZI")) brojPalih++;

applicationEnabled(brojPalih<2);

@@@filter("provjera")

// Ako nije prošao završni, tada ga mora ponavljati; inače može ponavljati onaj koji je pao
if(!passed("ZI")) {
  optionEnabled("mi1",false);
  optionEnabled("mi2",false);
  optionEnabled("zi",true);
} else {
// E ovo je komplicirano; mi1 i mi2 može ponavljati samo ako ih je pao ili ako ništa nije pao
// osim sto je pao kompletan kolegij (oni koji imaju ocjenu, ne mogu ponavljati MI1 ili MI2).
// Međutim, ako je prošao zi, onda ga može ponavljati ako i samo ako je sve prošao
  optionEnabled("mi1", !passed("MI1") || (brojPalih==0 && !passed("UKU")));
  optionEnabled("mi2", !passed("MI2") || (brojPalih==0 && !passed("UKU")));
  optionEnabled("zi", brojPalih==0);
}
</pre>

<p>Novitet u programu je sekcija "global" koja omogućava definiranje globalnih varijabli - varijabli koje su vidljive između svih sekcija.
U toj sekciji nije dozvoljeno pisanje naredbi. Sekcija "def" potom definira strukturu prijave. Sekcija "filter" bez argumenata izvodi se 
sljedeća i ona postavlja vrijednost globalne varijable "brojPalih", provjerava koje je sve ispite student pao i temeljem tog broja omogućava
prijavu ili ne. Studenti koji su pali više od jedne provjere prijavu neće vidjeti jer oni više ne mogu proći kolegij. Konačno, u sekciji filter
elementa "provjera" podešava se koje opcije vidi student.</p>

<b>Redosljed izvođenja sekcija</b>

<p>Ukoliko se program sastoji od više sekcija, tada je redosljed izvođenja uvijek sljedeći:</p>

<ul>
<li>Ukoliko postoji sekcija "global", ona se izvodi.</li>
<li>Izvodi se sekcija "def". Napomena: ukoliko se napiše program koji ne započinje deklaracijom sekcije, onda se podrazumijeva da naredbe
koje slijede pripadaju sekciji "def" koja je time implicitno deklarirana.</li>
<li>Ukoliko postoji sekcija "fliter" bez argumenata, ona se izvodi.</li>
<li>Ukoliko postoje sekcije "filter" s argumentima, one se izvode redosljedom kojim se pojavljuju u programu.</li>
</ul>
