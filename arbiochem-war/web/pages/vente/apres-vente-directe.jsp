<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ page import="service.VenteService" %>
<%@ page import="user.UserEJB" %>
<%!
    private static String jsVente(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("'", "\\'").replace("\r", " ").replace("\n", " ");
    }
%>
<%
    String erreur = null;
    String idVente = null;
    try {
        UserEJB utilisateur = (UserEJB) session.getAttribute("u");
        if (utilisateur == null) throw new IllegalStateException("La session utilisateur a expiré.");
        String[] places = request.getParameterValues("places");
        vente.Vente vente = VenteService.creerVenteDirecte(utilisateur,
                request.getParameter("idclient"), request.getParameter("idseance"),
                places == null ? null : java.util.Arrays.asList(places));
        idVente = vente.getIdvente();
    } catch (Exception e) {
        erreur = e.getMessage() == null ? "La vente directe n'a pas pu être créée." : e.getMessage();
        application.log("Erreur lors de la création de la vente directe", e);
    }
%>
<script>
    <% if (erreur == null) { %>
        document.location.replace("module.jsp?but=vente/vente-paiement-direct.jsp&idvente=<%= jsVente(idVente) %>");
    <% } else { %>
        alert('<%= jsVente(erreur) %>');
        history.back();
    <% } %>
</script>
