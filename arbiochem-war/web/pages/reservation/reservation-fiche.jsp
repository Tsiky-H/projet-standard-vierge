<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ page import="affichage.PageConsulte" %>
<%@ page import="reservation.Reservation" %>
<%@ page import="user.UserEJB" %>
<%
    try {
        UserEJB utilisateur = (UserEJB) session.getAttribute("u");
        Reservation reservation = new Reservation();
        reservation.setStatut(null);
        PageConsulte pageConsulte = new PageConsulte(reservation, request, utilisateur);
        pageConsulte.setTitre("Fiche réservation");
        Reservation reservationChargee = (Reservation) pageConsulte.getBase();
        if (reservationChargee == null) {
            throw new Exception("La réservation demandée est introuvable.");
        }
        String id = reservationChargee.getIdreservation();
        String lien = (String) session.getAttribute("lien");
%>
<div class="content-wrapper">
    <h1 class="box-title">
        <a href="<%= lien %>?but=reservation/reservation-liste.jsp">
            <i class="fa fa-angle-left"></i>
        </a>
        <%= pageConsulte.getTitre() %>
    </h1>
    <div class="box">
        <div class="box-body"><%= pageConsulte.getHtml() %></div>
        <div class="box-footer">
            <% if ("EN_ATTENTE".equals(reservationChargee.getStatut())) { %>
                <a class="btn btn-primary pull-right"
                   href="<%= lien %>?but=reservation/reservation-paiement.jsp&idreservation=<%= id %>">
                    Payer
                </a>
            <% } %>
        </div>
    </div>
</div>
<%
    } catch (Exception e) {
        application.log("Erreur lors de l'affichage de la fiche réservation.", e);
%><div class="alert alert-danger"><%= e.getMessage() %></div><%
    }
%>
