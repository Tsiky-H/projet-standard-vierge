
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

            Liste[] liste = new Liste[3];

            // Liste des clients
            Client c = new Client();
            c.setNomTable("CLIENT");
            liste[0] = new Liste(
                "idclient",
                c,
                "nom",
                "idclient",
                " AND ACTIF = 1"
            );

            // Liste des modes de paiement
            ModePaiement m = new ModePaiement();
            m.setNomTable("MODEPAIEMENT");
            liste[1] = new Liste(
                "idmodepaiement_prevu",
                m,
                "libelle",
                "idmodepaiement"
            );

            // Liste des séances réservables
            SeanceLib s = new SeanceLib();
            s.setNomTable("CINE_SEANCE_RESERVABLE");
            liste[2] = new Liste(
                "idseance",
                s,
                "libelle_seance",
                "idseance"
            );

            // Transformer les champs en listes déroulantes
            pi.getFormu().changerEnChamp(liste);

            // Libellés des champs
            pi.getFormu().getChamp("idclient").setLibelle("Client");
            pi.getFormu().getChamp("idseance").setLibelle("Séance");
            pi.getFormu().getChamp("idmodepaiement_prevu")
                .setLibelle("Mode de paiement prévu");

            pi.preparerDataFormu();

            // Préparer les index des champs
            pi.getFormu().makeHtmlInsertTabIndex();

        } catch (Exception e) {
            erreurChargement = "Impossible de préparer le formulaire de réservation.";
            application.log("Erreur lors de la préparation de reservation-saisie.jsp", e);
        }
    }
%>

<div class="content-wrapper">
    <section class="content-header">
        <h1>Nouvelle réservation</h1>
    </section>

    <section class="content">
        <% if (erreurChargement != null) { %>
            <div class="alert alert-danger">
                <%= erreurChargement %>
            </div>
        <% } else { %>
            <div class="box box-primary">
                <div class="box-header with-border">
                    <h3 class="box-title">
                        Choisir le client et une séance
                    </h3>
                </div>

                <form action="module.jsp?but=reservation/reservation-places.jsp"
                      method="post"
                      data-parsley-validate>

                    <div class="box-body">
                        <%= pi.getFormu().getHtmlInsert() %>
                    </div>

                    <div class="box-footer">
                        <button type="submit" class="btn btn-primary">
                            Choisir les places
                        </button>
                    </div>
                </form>
            </div>
        <% } %>
    </section>
</div>
