<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ page import="cinema.Seance" %>
<%@ page import="service.SeanceService" %>
<%@ page import="user.UserEJB" %>
<%@ page import="java.sql.Timestamp" %>
<%!
    private static String jsSeance(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("'", "\\'").replace("\r", " ").replace("\n", " ");
    }
%>
<%
    String erreur = null;
    try {
        UserEJB utilisateur = (UserEJB) session.getAttribute("u");
        if (utilisateur == null) throw new IllegalStateException("La session utilisateur a expiré.");
        String idFilm = request.getParameter("idfilm");
        String idSalle = request.getParameter("idsalle");
        String debut = request.getParameter("dateheure_debut");
        if (idFilm == null || idFilm.trim().isEmpty() || idSalle == null || idSalle.trim().isEmpty()
                || debut == null || debut.trim().isEmpty()) {
            throw new IllegalArgumentException("Choisissez un film, une salle et une date/heure de début.");
        }
        String dateSql = debut.trim().replace('T', ' ');
        if (dateSql.length() == 16) dateSql += ":00";
        Seance seance = new Seance();
        seance.setIdfilm(idFilm.trim());
        seance.setIdsalle(idSalle.trim());
        seance.setDateheure_debut(Timestamp.valueOf(dateSql));
        seance.setNomTable("CINE_SEANCE");
        SeanceService.creer(utilisateur, seance);
    } catch (Exception e) {
        application.log("Erreur lors de la création de séance", e);
        erreur = e.getMessage() == null ? "La séance n'a pas pu être créée." : e.getMessage();
%>
<% } %>
<script>
    <% if (erreur == null) { %>
        document.location.replace("module.jsp?but=cinema/seance/seance-liste.jsp");
    <% } else { %>
        alert('<%= jsSeance(erreur) %>');
        history.back();
    <% } %>
</script>
