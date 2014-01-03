<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div class="helpTitle1">Ručno stvaranje grupa za odabranu komponentu.</div>

<p>Ovisno o komponenti na kojoj se nalazite moguće je samostalno definirati grupe i pripadne termine, te omogućiti 
burzu grupa nad tako stvorenim grupama. Međutim, ako se za Vaš kolegij i odabranu komponentu raspored termina radi
centralizirano, ovo ne smijete koristiti, jer će doći do kolizije između centralno napravljenog rasporeda i rasporeda
koji radite sami. Ukoliko se ipak odlučite ovo raditi sami, konzultirajte se s administratorom ovog sustava, prije no
što krenete u samostalnu izradu grupa.</p>

<div class="helpTitle2">Format podataka</div>

<p>Ručno stvaranje grupa radi se tako da priredite podatke u tekstualnom formatu, i potom ih pošaljete na poslužitelj
uporabom za to predviđenog formulara. Sustav će time automatski stvoriti grupe i pripadne događaje. Onog trenutka kada
u grupe ubacite studente, studenti će povezane događaje vidjeti u svom osobnom kalendaru.</p>

<p>Svaki redak sastoji se od sljedećih podataka, koji su razdvojeni znakom ljestvi (#):</p>

<table>
<tr><th>podatak</th><th>pojašnjenje</th></tr>
<tr><td>Naziv događaja</td><td>Naziv koji želite dati događaju. Primjerice: "Digitalna logika - lab. vježba 3".</td></tr>
<tr><td>Datum</td><td>Datum kada se termin odvija. Format je YYYY-MM-DD, primjerice 2009-02-20.</td></tr>
<tr><td>Početak termina</td><td>Vrijeme kada termin počinje. Format je HH:mm, primjerice 13:45.</td></tr>
<tr><td>Kraj termina</td><td>Vrijeme kada termin završava. Format je HH:mm, primjerice 15:00.</td></tr>
<tr><td>Lokacija</td><td>Gdje se događaj odvija? Primjerice: FER. Ovo je važno zadati ispravno, kako bi se obavilo ispravno stvaranje događaja.
                         Sustav neće dozvoliti stvaranje događaja za lokacije koje nisu u sustavu, pa se u tom slučaju javite administratoru 
                         sustava.</td></tr>
<tr><td>Prostorija</td><td>U kojoj prostoriji se događaj odvija? Primjerice: PCLAB2. Ovo je važno zadati ispravno, kako bi se obavilo ispravno 
                         stvaranje događaja. Sustav neće dozvoliti stvaranje događaja za prostorije koje nisu u sustavu, pa se u tom slučaju 
                         javite administratoru sustava.</td></tr>
</table>

<p>Važno: uporabom ove funkcije automatski će biti stvorena i grupa "Neraspoređeni", koja neće imati povezanih termina (događaja). Također,
   ako koristite ovu mogućnost, snosite potpunu odgovornost za rezervaciju dvorana i studenata. Sustav neće samostalno obavljati rezervacije
   dvorana.</p>

<div class="helpTitle2">Dodatna pojašnjenja</div>

<p>Naziv događaja je ono što će se studentima prikazati u njihovom kalendaru. Kako svaki događaj ima svoj početak, trajanje i prostoriju,
   nije potrebno u naziv uključivati te podatke. Stoga je dobra strategija u naziv uključiti naziv kolegija te kratki opis. Primjerice, 
   "Digitalna logika - lab. vježba 3".</p>

<p>Naziv grupe će studenti vidjeti u kada se prijave na burzu grupa. Naziv grupe stvara sam sustav, tako da uključi godinu, početak, kraj i dvoranu;
   primjerice: "2008-10-06 10:00 12:00 A209".</p>
