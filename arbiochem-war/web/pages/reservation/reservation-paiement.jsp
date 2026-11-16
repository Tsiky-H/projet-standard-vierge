<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ page import="affichage.PageInsert" %>
<%@ page import="affichage.Liste" %>
<%@ page import="paiement.Paiement" %>
<%@ page import="paiement.ModePaiement" %>
<%@ page import="user.UserEJB" %>
<%@ page import="utilitaire.UtilDB" %>
<%@ page import="java.sql.Connection" %>
<%@ page import="java.sql.PreparedStatement" %>
<%@ page import="java.sql.ResultSet" %>
<%
    try {
        UserEJB utilisateur = (UserEJB) session.getAttribute("u");
        String idReservation = request.getParameter("idreservation");
        if (idReservation == null || idReservation.trim().isEmpty()) {
            throw new IllegalArgumentException("L'identifiant de la réservation est obligatoire.");
        }
        String idVente;
        double montantVente;
        try (Connection connexion = new UtilDB().GetConn()) {
            try (PreparedStatement ps = connexion.prepareStatement(
                    "SELECT IDVENTE, MONTANT_TOTAL FROM VENTE "
                    + "WHERE IDRESERVATION = ? AND STATUT = 'BROUILLON'")) {
                ps.setString(1, idReservation.trim());
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new Exception("La vente brouillon de la réservation est introuvable.");
                    }
                    idVente = rs.getString("IDVENTE");
                    montantVente = rs.getDouble("MONTANT_TOTAL");
                    if (rs.next()) {
                        throw new Exception("Plusieurs ventes brouillon existent pour cette réservation.");
                    }
                }
            }
        }
        Paiement paiement = new Paiement();
        PageInsert pageInsert = new PageInsert(paiement, request, utilisateur);
        String lien = (String) session.getAttribute("lien");
        if (lien == null || lien.trim().isEmpty()) {
            lien = "module.jsp";
        }
        pageInsert.setLien(lien);
        ModePaiement mode = new ModePaiement();
        mode.setNomTable("MODEPAIEMENT");
        Liste[] modes = {new Liste("idmodepaiement", mode, "libelle", "idmodepaiement", " AND ACTIF = 1")};
        pageInsert.getFormu().changerEnChamp(modes);
        pageInsert.getFormu().getChamp("idvente").setVisible(false);
        pageInsert.getFormu().getChamp("numero_paiement").setVisible(false);
        pageInsert.getFormu().getChamp("daty").setVisible(false);
        pageInsert.getFormu().getChamp("statut").setVisible(false);
        pageInsert.getFormu().getChamp("montant").setVisible(false);
        pageInsert.getFormu().getChamp("idmodepaiement").setLibelle("Mode de paiement");
        pageInsert.getFormu().getChamp("montant").setLibelle("Montant");
        pageInsert.getFormu().getChamp("reference_externe").setLibelle("Référence externe");
        pageInsert.preparerDataFormu();
        pageInsert.getFormu().makeHtmlInsertTabIndex();
%>
<div class="content-wrapper">
    <h1 class="box-title">Paiement de la réservation</h1>
    <div class="box">
        <div class="box-body">
            <form action="<%= pageInsert.getLien() %>?but=reservation/apres-paiement.jsp"
                  method="post" data-parsley-validate>
                <p class="lead">Montant à payer : <strong><%= montantVente %></strong></p>
                <%= pageInsert.getFormu().getHtmlInsert() %>
                <input type="hidden" name="idreservation" value="<%= idReservation %>">
                <input type="hidden" name="bute" value="reservation/reservation-fiche.jsp">
                <button type="submit" class="btn btn-primary pull-right">Valider le paiement</button>
            </form>
        </div>
    </div>
</div>
<%
    } catch (Exception e) {
        application.log("Erreur lors de la préparation du paiement.", e);
%><div class="alert alert-danger"><%= e.getMessage() %></div><%
    }
%>
