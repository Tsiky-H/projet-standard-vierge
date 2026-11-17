<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ page import="affichage.PageConsulte" %>
<%@ page import="facturation.FactureClientLib" %>
<%@ page import="user.UserEJB" %>
<%
    try {
        FactureClientLib facture = new FactureClientLib();
        facture.setStatut(null);
        PageConsulte pageConsulte = new PageConsulte(facture, request,
                (UserEJB) session.getAttribute("u"));
        pageConsulte.setTitre("Fiche facture client");
        if (pageConsulte.getBase() == null) {
            throw new Exception("La facture demandée est introuvable.");
        }
        pageConsulte.getChampByName("idclient").setVisible(false);
        pageConsulte.getChampByName("idfacture_origine").setVisible(false);
        pageConsulte.getChampByName("idclientlib").setLibelle("Client");
        pageConsulte.getChampByName("idfacture_originelib").setLibelle("Facture d'origine");
%>
<div class="content-wrapper">
    <h1 class="box-title"><%= pageConsulte.getTitre() %></h1>
    <div class="box"><div class="box-body"><%= pageConsulte.getHtml() %></div></div>
</div>
<%
    } catch (Exception e) {
        application.log("Erreur lors de l'affichage de la fiche facture.", e);
%><div class="alert alert-danger"><%= e.getMessage() %></div><%
    }
%>
