<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ page import="reservation.ReservationSaisie" %>
<%@ page import="client.Client" %>
<%@ page import="cinema.SeanceLib" %>
<%@ page import="paiement.ModePaiement" %>
<%@ page import="affichage.PageInsert" %>
<%@ page import="affichage.Liste" %>
<%@ page import="user.UserEJB" %>

<%
    UserEJB utilisateur = (UserEJB) session.getAttribute("u");
    String erreurChargement = null;
    PageInsert pi = null;

    if (utilisateur == null) {
        erreurChargement = "La session utilisateur a expiré. Reconnectez-vous.";
    } else {
        try {
            ReservationSaisie saisie = new ReservationSaisie();
            pi = new PageInsert(saisie, request, utilisateur);

            SeanceLib seancesReservables = new SeanceLib();
            seancesReservables.setNomTable("CINE_SEANCE_RESERVABLE");
            Liste clientListe = new Liste("idclient", new Client(), "nom", "idclient", " AND ACTIF = 1");
            Liste seanceListe = new Liste("idseance", seancesReservables, "libelle_seance", "idseance");
            Liste modeListe = new Liste("idmodepaiement_prevu", new ModePaiement(), "libelle", "idmodepaiement");
            clientListe.setEstMultiple(false);
            seanceListe.setEstMultiple(false);
            modeListe.setEstMultiple(false);
            pi.getFormu().changerEnChamp(new Liste[] { clientListe, seanceListe, modeListe });

            pi.getFormu().getChamp("idclient").setLibelle("Client");
            pi.getFormu().getChamp("idseance").setLibelle("Séance");
            pi.getFormu().getChamp("idmodepaiement_prevu").setLibelle("Mode de paiement prévu");
            pi.getFormu().getChamp("delai_expiration_minutes")
                    .setLibelle("Délai avant expiration (minutes ; vide = début de séance)");
            pi.getFormu().getChamp("delai_expiration_minutes").setType("number");
            pi.getFormu().getChamp("delai_expiration_minutes").setAutre("min=\"1\" step=\"1\"");
            pi.getFormu().getChamp("idclient").setAutre("required");
            pi.getFormu().getChamp("idseance").setAutre("required");
            pi.getFormu().getChamp("idmodepaiement_prevu").setAutre("required");

            pi.preparerDataFormu();
            pi.getFormu().makeHtmlInsertTabIndex();
        } catch (Exception e) {
            erreurChargement = "Impossible de préparer le formulaire de réservation.";
            application.log("Erreur lors de la préparation de reservation-saisie.jsp", e);
        }
    }
%>

<div class="content-wrapper">
    <section class="content-header"><h1>Nouvelle réservation</h1></section>
    <section class="content">
        <% if (erreurChargement != null) { %>
            <div class="alert alert-danger"><%= erreurChargement %></div>
        <% } else { %>
            <div class="box box-primary">
                <div class="box-header with-border"><h3 class="box-title">Choisir le client et la séance</h3></div>
                <form action="module.jsp?but=reservation/reservation-places.jsp" method="post" data-parsley-validate>
                    <div class="box-body"><%= pi.getFormu().getHtmlInsert() %></div>
                    <div class="box-footer">
                        <button type="submit" class="btn btn-primary">Choisir les places</button>
                    </div>
                </form>
            </div>
        <% } %>
    </section>
</div>
