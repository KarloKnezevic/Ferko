<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib prefix="s" uri="/struts-tags" %>

<div class="helpTitle1">Broj studenata u grupi</div>

<p>Stupac broj studenata u grupi prikazuje koliko se studenata nalazi direktno u navedenoj grupi (bez podgrupa).
Ukoliko grupa ima podgrupe, a sve su prikazane na istoj stranici, tipično će kod nadgrupe kao broj pisati nula,
jer sama nadgrupa ne sadrži studente. Koliki je točan broj studenata u svim podgrupama sustav Vam neće prikazati
kako ne bi stvarao zabunu prikazanim informacijama. Naime, ovisno o ulozi koju imate u sustavu, možda ne vidite sve
podgrupe već samo one za koje imate dozvolu. Kada bi kao broj studenata u grupi stajalo podatak koji odgovara sumi
Vama prikazanih grupa, taj bi podatak očito bio pogrešan.</p>
