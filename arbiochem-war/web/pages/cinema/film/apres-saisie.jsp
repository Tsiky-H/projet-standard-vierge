<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ page import="cinema.Film" %>
<%@ page import="user.UserEJB" %>
<%!
    private static String jsFilm(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("'", "\\'").replace("\r", " ").replace("\n", " ");
    }
%>
<%
    String erreur = null;
    try {
        UserEJB utilisateur = (UserEJB) session.getAttribute("u");
        if (utilisateur == null) throw new IllegalStateException("La session utilisateur a expiré. Reconnectez-vous.");

        String titre = request.getParameter("titre");
        String duree = request.getParameter("duree_minutes");
        String age = request.getParameter("classification_age");
        String actif = request.getParameter("actif");
        if (titre == null || titre.trim().isEmpty()) throw new IllegalArgumentException("Le titre du film est obligatoire.");
        if (duree == null || duree.trim().isEmpty()) throw new IllegalArgumentException("La durée du film est obligatoire.");

        Film film = new Film();
        film.setTitre(titre.trim());
        film.setDuree_minutes(Integer.parseInt(duree.trim()));
        film.setClassification_age(age == null || age.trim().isEmpty() ? 0 : Integer.parseInt(age.trim()));
        film.setActif(Integer.parseInt(actif));
        utilisateur.createObject(film);
    } catch (Exception e) {
        erreur = e.getMessage() == null ? "Le film n'a pas pu être créé." : e.getMessage();
        application.log("Erreur lors de la création du film", e);
    }
%>
<script>
    <% if (erreur == null) { %>
        document.location.replace("module.jsp?but=cinema/film/film-liste.jsp");
    <% } else { %>
        alert('<%= jsFilm(erreur) %>');
        history.back();
    <% } %>
</script>
