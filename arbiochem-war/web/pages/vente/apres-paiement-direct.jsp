<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ page import="service.PaiementService" %>
<%@ page import="user.UserEJB" %>
<%@ page import="utilitaire.UtilDB" %>
<%@ page import="java.sql.Connection" %>
<%@ page import="java.sql.PreparedStatement" %>
<%@ page import="java.sql.ResultSet" %>
<%
    String erreur = null;
    String idVente = request.getParameter("venteId");
    try {
        UserEJB utilisateur = (UserEJB) session.getAttribute("u");
        if (utilisateur == null) throw new IllegalStateException("La session utilisateur a expiré.");
        String idMode = request.getParameter("idmodepaiement");
        if (idVente == null || idVente.trim().isEmpty())
            throw new IllegalArgumentException("L'identifiant de la vente est absent du formulaire.");
        if (idMode == null || idMode.trim().isEmpty())
            throw new IllegalArgumentException("Choisissez un mode de paiement.");
        double montant;
        try (Connection c = new UtilDB().GetConn(); PreparedStatement ps = c.prepareStatement(
                "SELECT MONTANT_TOTAL FROM VENTE WHERE IDVENTE = ? AND TYPE_VENTE = 'DIRECTE' AND STATUT = 'BROUILLON'")) {
            ps.setString(1, idVente.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new IllegalArgumentException("La vente directe à payer est introuvable.");
                montant = rs.getDouble(1);
            }
        }
        String recuParam = request.getParameter("montantRecu");
        double recu = recuParam == null || recuParam.trim().isEmpty() ? montant : Double.parseDouble(recuParam.trim());
        new PaiementService().enregistrerPaiement(utilisateur, idVente.trim(), idMode.trim(), montant,
                request.getParameter("reference_externe"), recu, null);
    } catch (Exception e) {
        erreur = e.getMessage() == null ? "Paiement impossible." : e.getMessage();
        application.log("Erreur lors du paiement direct", e);
    }
%>
<script>
<% if (erreur == null) { %>
    document.location.replace("module.jsp?but=vente/vente-fiche.jsp&idvente=<%= idVente %>");
<% } else { %>
    alert('<%= erreur.replace("\\", "\\\\").replace("'", "\\'").replace("\r", " ").replace("\n", " ") %>');
    history.back();
<% } %>
</script>
