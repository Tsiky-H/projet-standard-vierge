<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ page import="affichage.PageConsulte" %>
<%@ page import="user.UserEJB" %>
<%@ page import="vente.Vente" %>
<%
    try {
        Vente vente = new Vente();
        vente.setStatut(null);
        PageConsulte pageConsulte = new PageConsulte(vente, request,
                (UserEJB) session.getAttribute("u"));
        pageConsulte.setTitre("Fiche vente");
        if (pageConsulte.getBase() == null) {
            throw new Exception("La vente demandée est introuvable.");
        }
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
