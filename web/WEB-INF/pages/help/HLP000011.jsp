<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div class="helpTitle1">Ažuriranje bodova i vrijednosti zastavica</div>

<p>Ferko podržava rad s hijerarhijskim provjerama. To znači da neka nadređena provjera svoje bodove može
raćunati temeljem bodova podređenih provjera. U tom izračunu također se mogu koristiti i vrijednosti
zastavica. Zastavice su komponente slične provjerama znanja, samo što se umjesto brojeva za svakog studenta
pamte logičke vrijednosti: može/ne može, ima pravo/nema pravo i sl. Vrijednosti zastavica mogu se
učitati iz datoteke ili mogu biti određene programom koji konačnu odluku donosi temeljem bodova odabranih
provjera, statusa studentovih prijava i sl. Temeljem svega ovoga, Ferko na kraju generira još i niz
statističkih pokazatelja.</p>
<p>Zbog kompleksnosti ovih izračuna, posebice na kolegijima s velikim brojem studenata, izračun bodova nadređenih
komponenti te zastavica neće se pokrenuti automatski kod promjene bodove podređene provjere. <b>Izračun je
potrebno zatražiti ručno</b>, aktiviranjem odgovarajućeg linka. Tek po napravljenom izračunu i Vi i studenti 
vidjet ćete pravo stanje bodova i vrijednosti zastavica na kolegiju.</p>
