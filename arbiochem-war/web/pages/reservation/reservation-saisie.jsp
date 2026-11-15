<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ page import="bean.CGenUtil" %>
<%@ page import="client.Client" %>
<%@ page import="cinema.SeanceLib" %>
<%@ page import="user.UserEJB" %>
<%@ page import="utilitaire.UtilDB" %>
<%@ page import="java.sql.Connection" %>
<%@ page import="java.text.SimpleDateFormat" %>

<%!
    private String echapperHtmlReservation(String valeur) {
        if (valeur == null) return "";
        return valeur.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
%>

<%
    UserEJB utilisateur = (UserEJB) session.getAttribute("u");
    String lien = (String) session.getAttribute("lien");
    Client[] clients = new Client[0];
    SeanceLib[] seances = new SeanceLib[0];
    String erreurChargement = null;

    if (utilisateur == null) {
        erreurChargement = "La session utilisateur a expiré. Reconnectez-vous.";
    } else {
        try (Connection connexion = new UtilDB().GetConn()) {
            clients = (Client[]) CGenUtil.rechercher(new Client(), null, null, connexion,
                    " AND ACTIF = 1 ORDER BY NOM, PRENOM");
            seances = (SeanceLib[]) CGenUtil.rechercher(new SeanceLib(), null, null, connexion,
                    " AND STATUT IN ('PLANIFIEE', 'OUVERTE') AND DATEHEURE_DEBUT > SYSDATE "
                    + "ORDER BY DATEHEURE_DEBUT, IDSEANCE");
            if (clients == null) clients = new Client[0];
            if (seances == null) seances = new SeanceLib[0];
        } catch (Exception e) {
            erreurChargement = "Impossible de charger les clients et les séances disponibles.";
            application.log("Erreur pendant le chargement du formulaire de réservation", e);
        }
    }

    SimpleDateFormat formatDateHeure = new SimpleDateFormat("dd/MM/yyyy HH:mm");
%>

<section class="content-header">
    <h1>Nouvelle réservation</h1>
</section>
<section class="content">
    <% if (erreurChargement != null) { %>
        <div class="alert alert-danger"><%= echapperHtmlReservation(erreurChargement) %></div>
    <% } else if (clients.length == 0) { %>
        <div class="alert alert-warning">Aucun client actif n'est disponible.</div>
    <% } else if (seances.length == 0) { %>
        <div class="alert alert-warning">Aucune séance future ouverte à la réservation.</div>
    <% } else { %>
        <div class="box box-primary">
            <div class="box-header with-border">
                <h3 class="box-title">Choisir le client et une première séance</h3>
            </div>
            <form action="<%= echapperHtmlReservation(lien) %>?but=reservation/reservation-places.jsp"
                  method="post" data-parsley-validate>
                <div class="box-body">
                    <div class="form-group">
                        <label for="idclient">Client</label>
                        <select class="form-control" id="idclient" name="idclient" required>
                            <option value="">Sélectionner un client</option>
                            <% for (Client client : clients) {
                                if (client == null) continue;
                                String nomClient = ((client.getNom() == null) ? "" : client.getNom())
                                        + ((client.getPrenom() == null || client.getPrenom().trim().isEmpty())
                                                ? "" : " " + client.getPrenom());
                                if (nomClient.trim().isEmpty()) nomClient = client.getIdclient();
                            %>
                                <option value="<%= echapperHtmlReservation(client.getIdclient()) %>">
                                    <%= echapperHtmlReservation(nomClient) %>
                                </option>
                            <% } %>
                        </select>
                    </div>

                    <div class="form-group">
                        <label for="idseance">Séance</label>
                        <select class="form-control" id="idseance" name="idseance" required>
                            <option value="">Sélectionner une séance</option>
                            <% for (SeanceLib seance : seances) {
                                if (seance == null) continue;
                                String libelleSeance = seance.getIdfilmlib() + " — " + seance.getIdsallelib()
                                        + " — " + formatDateHeure.format(seance.getDateheure_debut());
                            %>
                                <option value="<%= echapperHtmlReservation(seance.getIdseance()) %>">
                                    <%= echapperHtmlReservation(libelleSeance) %>
                                </option>
                            <% } %>
                        </select>
                    </div>
                </div>
                <div class="box-footer">
                    <button type="submit" class="btn btn-primary">Choisir les places</button>
                </div>
            </form>
        </div>
    <% } %>
</section>
