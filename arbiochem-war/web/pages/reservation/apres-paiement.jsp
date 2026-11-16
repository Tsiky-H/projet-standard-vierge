<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ page import="service.PaiementService" %>
<%@ page import="user.UserEJB" %>
<%@ page import="utilitaire.UtilDB" %>
<%@ page import="java.sql.Connection" %>
<%@ page import="java.sql.PreparedStatement" %>
<%@ page import="java.sql.ResultSet" %>
<%
    String erreur = null;
    String idReservation = request.getParameter("idreservation");
    try {
        UserEJB utilisateur = (UserEJB) session.getAttribute("u");
        String idMode = request.getParameter("idmodepaiement");
        if (idReservation == null || idReservation.trim().isEmpty()
                || idMode == null || idMode.trim().isEmpty()) {
            throw new IllegalArgumentException("La réservation et le mode de paiement sont obligatoires.");
        }
        String idVente;
        double montant;
        try (Connection connexion = new UtilDB().GetConn();
             PreparedStatement ps = connexion.prepareStatement(
                     "SELECT IDVENTE, MONTANT_TOTAL FROM VENTE "
                     + "WHERE IDRESERVATION = ? AND STATUT = 'BROUILLON'")) {
            ps.setString(1, idReservation.trim());
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    throw new Exception("La vente brouillon de la réservation est introuvable.");
                }
                idVente = rs.getString("IDVENTE");
                montant = rs.getDouble("MONTANT_TOTAL");
                if (rs.next()) {
                    throw new Exception("Plusieurs ventes brouillon existent pour cette réservation.");
                }
            }
        }
        new PaiementService().enregistrerPaiement(utilisateur, idVente, idMode, montant,
                request.getParameter("reference_externe"), montant, null);
    } catch (Exception e) {
        erreur = e.getMessage() == null ? "Paiement impossible." : e.getMessage();
        application.log("Erreur lors du paiement de la réservation.", e);
    }
%>
<script>
<% if (erreur == null) { %>
window.location.replace("module.jsp?but=reservation/reservation-fiche.jsp&idreservation=<%= idReservation %>");
<% } else { %>
alert('<%= erreur.replace("\\", "\\\\").replace("'", "\\'").replace("\r", " ").replace("\n", " ") %>');
history.back();
<% } %>
</script>
