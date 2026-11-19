<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ page import="affichage.Liste" %>
<%@ page import="affichage.PageInsert" %>
<%@ page import="cinema.Film" %>
<%@ page import="user.UserEJB" %>

<%
    UserEJB utilisateur = (UserEJB) session.getAttribute("u");
    String erreur = null;
    PageInsert pi = null;
    try {
        if (utilisateur == null) throw new IllegalStateException("La session utilisateur a expiré.");

        Film film = new Film();
        pi = new PageInsert(film, request, utilisateur);
        pi.setLien((String) session.getAttribute("lien"));
        Liste actif = new Liste("actif", new Object[] { "Actif", "Inactif" }, new String[] { "1", "0" });
        actif.setEstMultiple(false);
        pi.getFormu().changerEnChamp(new Liste[] { actif });

        if (pi.getFormu().getChamp("idfilm") != null) {
            pi.getFormu().getChamp("idfilm").setVisible(false);
        }
        pi.getFormu().getChamp("titre").setLibelle("Titre");
        pi.getFormu().getChamp("duree_minutes").setLibelle("Durée (minutes)");
        pi.getFormu().getChamp("classification_age").setLibelle("Classification d'âge (facultative)");
        pi.getFormu().getChamp("actif").setLibelle("État");
        pi.getFormu().getChamp("titre").setAutre("required maxlength=\"200\"");
        pi.getFormu().getChamp("duree_minutes").setType("number");
        pi.getFormu().getChamp("duree_minutes").setAutre("required min=\"1\" step=\"1\"");
        pi.getFormu().getChamp("classification_age").setType("number");
        pi.getFormu().getChamp("classification_age").setAutre("min=\"0\" step=\"1\"");
        pi.getFormu().getChamp("actif").setAutre("required");
        pi.preparerDataFormu();
        pi.getFormu().makeHtmlInsertTabIndex();
    } catch (Exception e) {
        erreur = e.getMessage() == null ? "Impossible de préparer le formulaire du film." : e.getMessage();
        application.log("Erreur lors de la préparation de film-saisie.jsp", e);
    }
%>

<div class="content-wrapper">
    <section class="content-header"><h1>Nouveau film</h1></section>
    <section class="content">
        <% if (erreur != null) { %>
            <div class="alert alert-danger"><%= erreur %></div>
        <% } else { %>
            <div class="box box-primary">
                <form action="<%= pi.getLien() %>?but=cinema/film/apres-saisie.jsp" method="post" data-parsley-validate>
                    <div class="box-body"><%= pi.getFormu().getHtmlInsert() %></div>
                    <div class="box-footer">
                        <a class="btn btn-default" href="module.jsp?but=cinema/film/film-liste.jsp">Annuler</a>
                        <button type="submit" class="btn btn-primary">Créer le film</button>
                    </div>
                </form>
            </div>
        <% } %>
    </section>
</div>
