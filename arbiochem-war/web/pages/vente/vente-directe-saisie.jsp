<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ page import="affichage.Liste" %>
<%@ page import="affichage.PageInsert" %>
<%@ page import="cinema.SeanceLib" %>
<%@ page import="client.Client" %>
<%@ page import="reservation.ReservationSaisie" %>
<%@ page import="user.UserEJB" %>
<%
    String erreur = null;
    PageInsert pi = null;
    try {
        UserEJB utilisateur = (UserEJB) session.getAttribute("u");
        if (utilisateur == null) throw new IllegalStateException("La session utilisateur a expiré.");
        pi = new PageInsert(new ReservationSaisie(), request, utilisateur);
        pi.setLien((String) session.getAttribute("lien"));
        SeanceLib vendables = new SeanceLib();
        vendables.setNomTable("CINE_SEANCE_VENDABLE");
        Liste clients = new Liste("idclient", new Client(), "nom", "idclient", " AND ACTIF = 1");
        Liste seances = new Liste("idseance", vendables, "libelle_seance", "idseance");
        clients.setEstMultiple(false);
        seances.setEstMultiple(false);
        pi.getFormu().changerEnChamp(new Liste[] { clients, seances });
        pi.getFormu().getChamp("idclient").setLibelle("Client");
        pi.getFormu().getChamp("idseance").setLibelle("Séance disponible à la vente");
        for (String champ : new String[] { "idsaisie", "idmodepaiement_prevu", "delai_expiration_minutes" }) {
            if (pi.getFormu().getChamp(champ) != null) pi.getFormu().getChamp(champ).setVisible(false);
        }
        pi.getFormu().getChamp("idclient").setAutre("required");
        pi.getFormu().getChamp("idseance").setAutre("required");
        pi.preparerDataFormu();
        pi.getFormu().makeHtmlInsertTabIndex();
    } catch (Exception e) {
        erreur = e.getMessage() == null ? "Impossible de préparer la vente directe." : e.getMessage();
        application.log("Erreur lors de la préparation de vente-directe-saisie.jsp", e);
    }
%>
<div class="content-wrapper">
    <section class="content-header"><h1>Achat direct de billets</h1></section>
    <section class="content">
        <% if (erreur != null) { %><div class="alert alert-danger"><%= erreur %></div><% } else { %>
        <div class="box box-primary">
            <div class="box-header with-border"><h3 class="box-title">Choisir le client et la séance</h3></div>
            <form action="module.jsp?but=vente/vente-directe-places.jsp" method="post" data-parsley-validate>
                <div class="box-body"><%= pi.getFormu().getHtmlInsert() %>
                    <p>L'achat reste possible jusqu'à la fin de la séance, sous réserve de disponibilité des places.</p>
                </div>
                <div class="box-footer"><button type="submit" class="btn btn-primary">Choisir les places</button></div>
            </form>
        </div><% } %>
    </section>
</div>
