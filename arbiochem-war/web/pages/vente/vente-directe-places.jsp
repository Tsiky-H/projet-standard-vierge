<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ page import="cinema.PlaceSeanceDispo" %>
<%@ page import="service.PlanSalleService" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.LinkedHashMap" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Locale" %>
<%@ page import="java.util.Map" %>
<%@ page import="user.UserEJB" %>
<%!
    private static String hVente(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }
%>
<%
    String idClient = request.getParameter("idclient");
    String idSeance = request.getParameter("idseance");
    String erreur = null;
    PlaceSeanceDispo[] places = new PlaceSeanceDispo[0];
    Map<String, List<PlaceSeanceDispo>> rangees = new LinkedHashMap<String, List<PlaceSeanceDispo>>();
    Map<String, String[]> categories = new LinkedHashMap<String, String[]>();
    int libres = 0;
    if (session.getAttribute("u") == null) erreur = "La session utilisateur a expiré.";
    else if (idClient == null || idClient.trim().isEmpty() || idSeance == null || idSeance.trim().isEmpty())
        erreur = "Choisissez un client et une séance.";
    else try {
        places = PlanSalleService.listerPlacesSeance(idSeance.trim());
        if (places.length == 0) erreur = "Aucune place n'est définie pour cette séance.";
        for (PlaceSeanceDispo p : places) {
            String rangee = p.getRangee() == null ? "" : p.getRangee();
            List<PlaceSeanceDispo> l = rangees.get(rangee);
            if (l == null) { l = new ArrayList<PlaceSeanceDispo>(); rangees.put(rangee, l); }
            l.add(p);
            String idCat = p.getIdcategorieplace();
            if (idCat != null && !categories.containsKey(idCat)) categories.put(idCat, new String[] {
                p.getIdcategorieplacelib(), String.format(Locale.FRANCE, "%,.0f", p.getPrix_base()),
                String.valueOf(categories.size() % 6) });
            if (p.getDisponible() == 1) libres++;
        }
    } catch (Exception e) {
        erreur = "Impossible de charger le plan de places.";
        application.log("Erreur lors du chargement de vente-directe-places.jsp", e);
    }
    SimpleDateFormat date = new SimpleDateFormat("dd/MM/yyyy 'à' HH:mm");
%>
<style>
    .plan-ecran { text-align:center; margin:15px auto 25px; padding:6px; max-width:60%; background:#ddd; border-radius:0 0 40px 40px; font-weight:bold; letter-spacing:4px; }
    .plan-rangee { display:flex; align-items:center; justify-content:center; margin-bottom:8px; }
    .plan-rangee-nom { width:32px; font-weight:bold; text-align:center; }
    .plan-place { position:relative; margin:0 3px; }
    .plan-place input { position:absolute; opacity:0; width:0; height:0; }
    .plan-place label { display:block; min-width:42px; padding:6px 4px; margin:0; text-align:center; border-radius:6px 6px 3px 3px; border:2px solid transparent; font-size:12px; color:#fff; cursor:pointer; }
    .plan-place .cat0 { background:#3c8dbc; } .plan-place .cat1 { background:#d4a017; } .plan-place .cat2 { background:#00a65a; } .plan-place .cat3 { background:#8e44ad; } .plan-place .cat4 { background:#e67e22; } .plan-place .cat5 { background:#16a085; }
    .plan-place input:checked + label { border-color:#222; box-shadow:0 0 0 2px #fff inset; font-weight:bold; }
    .plan-place input:disabled + label { background:#c7c7c7; color:#777; cursor:not-allowed; text-decoration:line-through; }
    .plan-puce { display:inline-block; width:14px; height:14px; border-radius:3px; vertical-align:middle; margin-right:4px; }
    .plan-puce.cat0 { background:#3c8dbc; } .plan-puce.cat1 { background:#d4a017; } .plan-puce.cat2 { background:#00a65a; } .plan-puce.cat3 { background:#8e44ad; } .plan-puce.cat4 { background:#e67e22; } .plan-puce.cat5 { background:#16a085; } .plan-puce.occupee { background:#c7c7c7; }
</style>
<div class="content-wrapper"><section class="content-header"><h1>Choisir les places à acheter</h1></section><section class="content">
<% if (erreur != null) { %><div class="alert alert-danger"><%= hVente(erreur) %></div><a class="btn btn-default" href="module.jsp?but=vente/vente-directe-saisie.jsp">Retour</a>
<% } else { PlaceSeanceDispo premiere = places[0]; %>
<div class="box box-primary"><div class="box-header with-border"><h3 class="box-title"><%= hVente(premiere.getIdfilmlib()) %> — <%= hVente(premiere.getIdsallelib()) %> — <%= premiere.getDateheure_debut() == null ? "" : date.format(premiere.getDateheure_debut()) %></h3></div>
<form action="module.jsp?but=vente/apres-vente-directe.jsp" method="post">
<input type="hidden" name="idclient" value="<%= hVente(idClient) %>"><input type="hidden" name="idseance" value="<%= hVente(idSeance) %>">
<div class="box-body"><div class="plan-legende"><% for (String[] c : categories.values()) { %><span style="margin-right:16px"><i class="plan-puce cat<%= c[2] %>"></i><%= hVente(c[0]) %> — <%= c[1] %></span><% } %><span><i class="plan-puce occupee"></i>Indisponible</span></div>
<div class="plan-ecran">ÉCRAN</div>
<% for (Map.Entry<String, List<PlaceSeanceDispo>> rangee : rangees.entrySet()) { %><div class="plan-rangee"><span class="plan-rangee-nom"><%= hVente(rangee.getKey()) %></span><% for (PlaceSeanceDispo p : rangee.getValue()) { boolean libre = p.getDisponible() == 1; String[] cat = categories.get(p.getIdcategorieplace()); String classe = cat == null ? "cat0" : "cat" + cat[2]; String identifiant = "place_" + hVente(p.getIdplace()); %><span class="plan-place"><input type="checkbox" name="places" id="<%= identifiant %>" value="<%= hVente(p.getIdplace()) %>" data-prix="<%= p.getPrix_base() %>" <%= libre ? "" : "disabled" %>><label for="<%= identifiant %>" class="<%= classe %>" title="<%= libre ? "Disponible" : "Indisponible" %>"><%= hVente(p.getNumero()) %></label></span><% } %></div><% } %>
<% if (libres == 0) { %><div class="alert alert-warning">Aucune place n'est disponible pour cette séance.</div><% } %>
<p><strong id="nbPlaces">0</strong> place(s) — total : <strong id="totalPlaces">0</strong></p></div>
<div class="box-footer"><a class="btn btn-default" href="module.jsp?but=vente/vente-directe-saisie.jsp">Retour</a><button id="btnVente" type="submit" class="btn btn-primary" disabled>Créer la vente</button></div></form></div>
<script>(function(){var c=document.querySelectorAll('.plan-place input[type=checkbox]'),n=document.getElementById('nbPlaces'),t=document.getElementById('totalPlaces'),b=document.getElementById('btnVente');function maj(){var x=0,s=0;for(var i=0;i<c.length;i++)if(c[i].checked){x++;s+=parseFloat(c[i].getAttribute('data-prix'))||0;}n.textContent=x;t.textContent=s.toLocaleString('fr-FR');b.disabled=x===0;}for(var i=0;i<c.length;i++)c[i].addEventListener('change',maj);maj();})();</script>
<% } %></section></div>
