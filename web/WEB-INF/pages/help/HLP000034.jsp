<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div class="helpTitle1">Dodavanje zavodskih prostorija na popis</div>

<p>Popis dvorana koji se inicijalno dohvaća za raspoređivanje studenata ne prikazuje sve dvorane. Primjer su različite zavodske prostorije koje nisu javno dostupne.
   Koristeći ovaj formular u popis možete dodati dvorane koje nisu prikazane, a jesu registrirane u sustavu. Za to je potrebno unijeti lokaciju (tipično FER) te naziv
   prostorije.</p>

<p>Ako prilikom pokušaja dodavanja sustav javi da tražena prostorija nije registrirana u Ferku, dovoljno je poslati e-mail administratoru Ferka s podacima o lokaciji
   i nazivu prostorije, kapacitetu prostorije za predavanja, kapacitetu prostorije za ispite, kapacitetu prostorije za laboratorijske vježbe te potrebnom broju asistenata.
   Ako je prostorija takva da se u njoj ne održavaju laboratorijske vježbe, dotični kapacitet prijavite kao 0. Isto vrijedi i za ostale kapacitete. Nakon unosa u sustav,
   dvorana će Vam biti dostupna za dodavanje uporabom ovog formulara.</p>
   
<h3>Zašto sve dvorane nisu odmah prikazane?</h3>

<p>Sve dvorane nisu odmah prikazane iz više razloga, od kojih su najznačajniji:</p>
<ul>
  <li>Preglednost popisa - prikazivanjem samo onih dvorana koje se tipično koriste, popis koji je prikazan većini puno je pregledniji.</li>
  <li>Sprječavanje nehotičnog odabira - kako se na popisu ne nalaze interne zavodske prostorije, manja je vjerojatnost da će ih netko pogreškom
      odabrati.</li>
</ul>
