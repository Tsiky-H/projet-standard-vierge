<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ page import="cinema.PlaceSeanceDispo" %>
<%@ page import="reservation.Reservation" %>
<%@ page import="service.PlanSalleService" %>
<%@ page import="service.ReservationService" %>
<%@ page import="user.UserEJB" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Arrays" %>
<%@ page import="java.util.LinkedHashSet" %>
<%@ page import="java.util.List" %>

<%!
    /** Prépare un message pour l'insérer dans une chaîne JavaScript entre apostrophes. */
    private static String js(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("'", "\\'")
                .replace("\r", " ").replace("\n", " ");
    }
%>

<%
    // Délai laissé au client pour payer (plafonné au début de la séance).
    final int DELAI_EXPIRATION_MINUTES = 30;

    UserEJB utilisateur = (UserEJB) session.getAttribute("u");

    // Page de destination : on n'accepte qu'un chemin .jsp simple.
    String bute = request.getParameter("bute");
    if (bute == null || !bute.matches("[A-Za-z0-9_/\\-]+\\.jsp")) {
        bute = "reservation/reservation-liste.jsp";
    }

    String erreur = null;
    try {
        if (utilisateur == null) {
            throw new IllegalStateException("La session utilisateur a expiré. Reconnectez-vous.");
        }

        String idClient = request.getParameter("idclient");
        String idSeance = request.getParameter("idseance");
        String idMode = request.getParameter("idmodepaiement_prevu");
        String[] choisies = request.getParameterValues("places");

        if (idClient == null || idClient.trim().isEmpty()) {
            throw new IllegalArgumentException("Le client est obligatoire.");
        }
        if (idSeance == null || idSeance.trim().isEmpty()) {
            throw new IllegalArgumentException("La séance est obligatoire.");
        }
        if (choisies == null || choisies.length == 0) {
            throw new IllegalArgumentException("Sélectionnez au moins une place.");
        }

        Reservation reservation = new Reservation();
        reservation.setIdclient(idClient.trim());
        if (idMode != null && !idMode.trim().isEmpty()) {
            reservation.setIdmodepaiement_prevu(idMode.trim());
        }
        reservation.setDate_expiration(
                PlanSalleService.calculerEcheance(idSeance, DELAI_EXPIRATION_MINUTES));

        List<PlaceSeanceDispo> selection = new ArrayList<PlaceSeanceDispo>();
        for (String idPlace : new LinkedHashSet<String>(Arrays.asList(choisies))) {
            if (idPlace == null || idPlace.trim().isEmpty()) continue;
            PlaceSeanceDispo place = new PlaceSeanceDispo();
            place.setIdseance(idSeance.trim());
            place.setIdplace(idPlace.trim());
            place.setDisponible(1); // exigé par ReservationService ; la vraie disponibilité est relue en base
            selection.add(place);
        }

        new ReservationService().creerReservation(utilisateur, reservation, selection);
    } catch (Exception e) {
        erreur = e.getMessage() != null ? e.getMessage() : "Erreur lors de la création de la réservation.";
        application.log("Erreur dans apres-reservation.jsp", e);
    }
%>
<script>
    <% if (erreur == null) { %>
        document.location.replace("module.jsp?but=<%= bute %>");
    <% } else { %>
        alert('<%= js(erreur) %>');
        history.back();
    <% } %>
</script>
