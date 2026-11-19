<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ page import="affichage.Liste" %>
<%@ page import="affichage.PageInsert" %>
<%@ page import="paiement.ModePaiement" %>
<%@ page import="paiement.Paiement" %>
<%@ page import="user.UserEJB" %>
<%@ page import="utilitaire.UtilDB" %>
<%@ page import="java.sql.Connection" %>
<%@ page import="java.sql.PreparedStatement" %>
<%@ page import="java.sql.ResultSet" %>
<%
    try {
        UserEJB utilisateur = (UserEJB) session.getAttribute("u");
        if (utilisateur == null) throw new IllegalStateException("La session utilisateur a expiré.");
        String idVente = request.getParameter("idvente");
        if (idVente == null || idVente.trim().isEmpty()) throw new IllegalArgumentException("La vente est obligatoire.");
        double montant;
        try (Connection c = new UtilDB().GetConn(); PreparedStatement ps = c.prepareStatement(
                "SELECT MONTANT_TOTAL FROM VENTE WHERE IDVENTE = ? AND TYPE_VENTE = 'DIRECTE' AND STATUT = 'BROUILLON'")) {
            ps.setString(1, idVente.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new IllegalArgumentException("La vente directe à payer est introuvable.");
                montant = rs.getDouble(1);
            }
        }
        Paiement paiement = new Paiement();
        PageInsert pi = new PageInsert(paiement, request, utilisateur);
        String lien = (String) session.getAttribute("lien");
        pi.setLien(lien == null ? "module.jsp" : lien);
        ModePaiement modes = new ModePaiement();
        modes.setNomTable("MODEPAIEMENT");
        Liste listeMode = new Liste("idmodepaiement", modes, "libelle", "idmodepaiement", " AND ACTIF = 1");
        listeMode.setEstMultiple(false);
        pi.getFormu().changerEnChamp(new Liste[] { listeMode });
        for (String champ : new String[] { "idvente", "numero_paiement", "daty", "statut", "montant" }) {
            if (pi.getFormu().getChamp(champ) != null) pi.getFormu().getChamp(champ).setVisible(false);
        }
        pi.getFormu().getChamp("idmodepaiement").setLibelle("Mode de paiement");
        pi.getFormu().getChamp("idmodepaiement").setAutre("required");
        pi.getFormu().getChamp("reference_externe").setLibelle("Référence (paiement mobile/carte)");
        pi.preparerDataFormu();
        pi.getFormu().makeHtmlInsertTabIndex();
%>
<div class="content-wrapper"><section class="content-header"><h1>Paiement de l'achat direct</h1></section><section class="content">
<div class="box box-primary"><div class="box-body"><p class="lead">Montant à payer : <strong><%= montant %></strong></p>
<form action="module.jsp?but=vente/apres-paiement-direct.jsp" method="post" data-parsley-validate>
<%= pi.getFormu().getHtmlInsert() %>
<input type="hidden" name="venteId" value="<%= idVente %>">
<label for="montantRecu">Montant reçu en espèces</label><input class="form-control" type="number" name="montantRecu" id="montantRecu" min="0" step="0.01">
<div class="help-block">Pour les paiements mobile ou carte, renseignez la référence et laissez le montant reçu vide.</div>
<button type="submit" class="btn btn-primary">Enregistrer le paiement</button></form></div></div>
</section></div>
<% } catch (Exception e) { application.log("Erreur lors de la préparation du paiement direct", e); %><div class="alert alert-danger"><%= e.getMessage() %></div><% } %>
