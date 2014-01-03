<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div class="helpTitle1">Format iCal</div>

<p>iCal format je uobičajeni format podataka za razmjenu informacija o događajima. Danas je format podržan od svih značajnijih aplikacija za rad s kalendarima, 
   poput Mozille Thunderbirda s ekstenzijom Lightning, ili Google kalendara.</p>
<p>Kako bi Vam olakšao praćenje Vaših obaveza, Ferko Vam nudi dohvat Vašeg osobnog rasporeda u iCal formatu (na naslovnici postoji link).</p>

<b>Kako koristiti iCal format</b>

<p>Mogućnost dohvata kalendara u iCal formatu možete koristiti na dva načina: <i>loš način</i> i <i>dobar način</i>.</p>

<i><u>Loš način</u></i>

<p>Dovoljno je kliknete na link koji Vas nudi na iCal format Vašeg rasporeda. Preglednik koji koristite ponudit će Vam mogućnost da raspored spremite na disk
   kao zasebnu datoteku. Aplikacije koje koristite za upravljanje rasporedom tipično nude mogućnost uvoza rasporeda iz takvih datoteka, pa je to jedan način
   kako možete povezati Ferko i druge aplikacije. No zašto je to loše? Problem je u tome što u dobivenu datoteku ulaze samo oni podatci koji su postojali u
   sustavu u trenutku izrade datoteke. Svaka buduća promjena neće Vam biti vidljiva u vanjskim aplikacijama, a ponovni uvod zna stvoriti dvostruki raspored
   koji morate ručno brisati.</p>

<i><u>Dobar način</u></i>

<p>Sve moderne aplikacije danas Vam nude mogućnost automatske sinkronizacije kalendara preko iCal formata. Međutim, da biste to koristili, programu ne smijete
   dati gotovu iCal datoteku. Umjesto da datoteku snimate na disk desnim klikom na link, napravite lijevi klik na link, i odaberite stavku "Copy link location"
   (ili sličnu; ovisno koji preglednik koristite). Link koji ste kopirali predajte Vašem programu koji koristite. Program će ga zapamtiti i sam periodički 
   kontaktirati Ferko i provjeravati ima li kakvih noviteta.</p>
<p><b>Važno:</b> provjerite oblik linka. Ukoliko započinje s "https://", promijenite protokol u "http://". Naime, trenutno, za sigurnu komunikaciju Ferko
   koristi certifikat koji nije potpisan od komercijalnih tijela kojima programi automatski vjeruju. Stoga će uporaba protokola https za komunikaciju s Ferkom
   i skidanje iCal rasporeda iz takvih programa biti onemogućena. Stvar će se riješiti jednom kada se pronađu novci na nabavu komercijalnog certifikata.</p>
