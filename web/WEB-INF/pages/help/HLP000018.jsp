<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div class="helpTitle1">Grupe</div>

<p>Na ovoj stranici nalazi se popis svih grupa koje su definirane na trenutnom kolegiju. Detaljnim popisima studenata,
kao i drugim akcijama nad grupama, možete pristupiti kroz iskočni izbornik. Ferko za studente stvara grupe opisane u 
nastavku.</p>

<b>Grupe za predavanja</b>

<p>Grupe za predavanja sadrže studente razvrstane prema predavanjima. Temeljem ovih grupa i satnice studentima
se u kalendaru prikazuje kada moraju biti na predavanjima i u kojoj prostoriji.</p>

<b>Grupe za laboratorijske vježbe</b>

<p>Grupe za laboratorijske vježbe sadrže studente razvrstane po pojedinim laboratorijskim vježbama. Podgrupe
ove grupe bit će pojedine laboratorijske vježbe, a unutar tih podgrupa nalazit će se konkretna raspodjela
studenata za dotičnu laboratorijsku vježbu.</p>
<p>Vršne podgrupe grupe za laboratorijske vježbe ne možete direktno stvarati kroz ovaj pogled, jer je potrebno
osigurati povezanost s komponentom kolegija "Laboratorijske vježbe". Želite li sami stvarati raspored studenata
za laboratorijske vježbe, na stranici kolegija otiđite na administraciju komponenti, i tamo najprije stvorite
odgovarajuću komponentu (npr. 4. laboratorijska vježba). Nakon što ste to napravili, na toj ćete stranici već
moći napraviti inicijalni raspored studenata koji kasnije ovdje možete mijenjati.</p>

<b>Grupe za domaće zadaće</b>

<p>Grupe za domaće zadaće omogućavaju izradu grupa za potrebe domaćih zadaća, i slijede istu ideju kao i grupe
za laboratorijske vježbe.</p>

<b>Grupe za seminare</b>

<p>Grupe za seminare omogućavaju izradu grupa za potrebe seminarskih radova, i slijede istu ideju kao i grupe
za laboratorijske vježbe.</p>

<b>Grupe ispite</b>

<p>Ukoliko kroz Ferko definirate i objavite raspored za međuispite, završne ispite te ponovljene završne ispite,
studenti će taj raspored automatski vidjeti u svojem kalendaru, pa neće biti potrebe raspored oglašavati
na druge načine. Prednosti ima još niz - od automatske izrade obrazaca koje samo pošaljete na printer, do
automatske izrade cjelokupne potrebne ispitne papirologije (popisi studenata i slično). Grupe za svaku napravljenu
provjeru znanja vidjet ćete kao podgrupe ove grupe.</p>

<b>Privatne grupe</b>

<p>Privatne grupe omogućavaju Vam zakazivanje obaveza studentima na kolegiju koje nisu pokrivene prethodno opisanim
grupama. Ilustrirajmo to primjerom. Želimo napraviti nadoknadu 3. laboratorijske vježbe. Imamo 45 studenata koji
trebaju napraviti nadoknadu. Želimo iskoristiti manje laboratorije u trajanju od po 1 sat. Pretpostavimo da smo pronašli
da je laboratorij A109 2. lipnja slobodan od 14h na dalje, pa smo rezervirali termin od 14h do 17h. Analizom studentskog
rasporeda kroz Ferko također smo se uvjerili da su i studenti tada slobodni, pa možemo zakazati nadoknadu. Evo što 
trebamo napraviti.

<ol>
<li> korak: npravit ćemo novu privatnu grupu i nazvati je "Nadoknada 3. laboratorijske vježbe".</li>
<li> korak: u toj novostvorenoj grupi dodat ćemo 3 podgrupe, i nazvati ih prema terminu: "Termin 2009-06-02 14:00",
            "Termin 2009-06-02 15:00" i "Termin 2009-06-02 16:00".</li>
<li> korak: u svaku od tri grupe koje odgovaraju pojedinim terminima dodat ćemo po trećinu studenata.</p>
<li> korak: svakoj od tri grupe koje odgovaraju pojedinim terminima dodat ćemo po jedan događaj. Naziv događaja
            će uvijek biti isti: "Nadoknada 3. laboratorijske vježbe" (taj će tekst studenti vidjeti u svojim
            kalendarima). Lokacija će biti A109, trajanje 60 minuta, a vremena 14:00:00, 15:00:00 odnosno 16:00:00.
            Onog trenutka kada studente smjestite u grupu, i grupi definirate jedan ili više događaja, studenti
            te događaje automatski vide u svojim kalendarima.</p>
</ol>

<p>Privatne grupe možete koristiti za zakazivanje nadoknada predavanja, ispita, laboratorijskih vježbi ili bilo koje
slične komponente.</p>