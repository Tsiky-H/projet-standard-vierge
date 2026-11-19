<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ page import="affichage.Liste" %>
<%@ page import="affichage.PageInsert" %>
<%@ page import="cinema.FilmLib" %>
<%@ page import="cinema.SalleLib" %>
<%@ page import="cinema.Seance" %>
<%@ page import="user.UserEJB" %>
<%
    String erreur = null;
    PageInsert pi = null;
    try {
        UserEJB utilisateur = (UserEJB) session.getAttribute("u");
        if (utilisateur == null) throw new IllegalStateException("La session utilisateur a expiré.");
        pi = new PageInsert(new Seance(), request, utilisateur);
        pi.setLien((String) session.getAttribute("lien"));
        Liste films = new Liste("idfilm", new FilmLib(), "titre", "idfilm", " AND ACTIF = 1");
        Liste salles = new Liste("idsalle", new SalleLib(), "libelle", "idsalle", " AND ACTIF = 1");
        films.setEstMultiple(false);
        salles.setEstMultiple(false);
        pi.getFormu().changerEnChamp(new Liste[] { films, salles });
        if (pi.getFormu().getChamp("idseance") != null) pi.getFormu().getChamp("idseance").setVisible(false);
        pi.getFormu().getChamp("idfilm").setLibelle("Film");
        pi.getFormu().getChamp("idsalle").setLibelle("Salle");
        pi.getFormu().getChamp("dateheure_debut").setLibelle("Date et heure de début");
        pi.getFormu().getChamp("statut").setVisible(false);
        pi.getFormu().getChamp("dateheure_fin").setVisible(false);
        pi.getFormu().getChamp("idfilm").setAutre("required");
        pi.getFormu().getChamp("idsalle").setAutre("required");
        pi.getFormu().getChamp("dateheure_debut").setType("datetime-local");
        pi.getFormu().getChamp("dateheure_debut").setAutre("required");
        pi.preparerDataFormu();
        pi.getFormu().makeHtmlInsertTabIndex();
    } catch (Exception e) {
        erreur = e.getMessage() == null ? "Impossible de préparer le formulaire de séance." : e.getMessage();
        application.log("Erreur lors de la préparation de seance-saisie.jsp", e);
    }
%>
<div class="content-wrapper">
    <section class="content-header"><h1>Nouvelle séance</h1></section>
    <section class="content">
        <% if (erreur != null) { %>
            <div class="alert alert-danger"><%= erreur %></div>
        <% } else { %>
            <div class="box box-primary">
                <div class="box-header with-border"><h3 class="box-title">Programmer une séance</h3></div>
                <form action="<%= pi.getLien() %>?but=cinema/seance/apres-saisie.jsp" method="post" data-parsley-validate>
                    <div class="box-body">
                        <%= pi.getFormu().getHtmlInsert() %>
                        <p>L'heure de fin est calculée depuis la durée du film. La séance est créée ouverte; la salle doit être active et libre sur cet intervalle.</p>
                    </div>
                    <div class="box-footer">
                        <a class="btn btn-default" href="module.jsp?but=cinema/seance/seance-liste.jsp">Annuler</a>
                        <button type="submit" class="btn btn-primary">Créer la séance</button>
                    </div>
                </form>
            </div>
        <% } %>
    </section>
</div>
