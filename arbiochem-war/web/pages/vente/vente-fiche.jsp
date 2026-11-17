<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ page import="affichage.PageConsulte" %>
<%@ page import="user.UserEJB" %>
<%@ page import="vente.VenteLib" %>
<%
    try {
        VenteLib vente = new VenteLib();
        vente.setStatut(null);
        PageConsulte pageConsulte = new PageConsulte(vente, request,
                (UserEJB) session.getAttribute("u"));
        pageConsulte.setTitre("Fiche vente");
        if (pageConsulte.getBase() == null) {
            throw new Exception("La vente demandée est introuvable.");
        }
        pageConsulte.getChampByName("idclient").setVisible(false);
        pageConsulte.getChampByName("idcaisse").setVisible(false);
        pageConsulte.getChampByName("idreservation").setVisible(false);
        pageConsulte.getChampByName("idfactureclient").setVisible(false);
        pageConsulte.getChampByName("idclientlib").setLibelle("Client");
        pageConsulte.getChampByName("idcaisselib").setLibelle("Caisse");
        pageConsulte.getChampByName("idreservationlib").setLibelle("Réservation");
        pageConsulte.getChampByName("idfactureclientlib").setLibelle("Facture");
%>
<div class="content-wrapper">
    <h1 class="box-title"><%= pageConsulte.getTitre() %></h1>
    <div class="box"><div class="box-body"><%= pageConsulte.getHtml() %></div></div>
</div>
<%
    } catch (Exception e) {
        application.log("Erreur lors de l'affichage de la fiche vente.", e);
%><div class="alert alert-danger"><%= e.getMessage() %></div><%
    }
%>
