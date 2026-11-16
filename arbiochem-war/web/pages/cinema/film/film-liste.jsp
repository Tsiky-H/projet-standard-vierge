<%@page import="affichage.PageRecherche"%>
<%@page import="cinema.FilmLib"%>
<%
    try {
        FilmLib objet = new FilmLib();
        String[] criteres = {"code", "titre"};
        String[] entetes = {"code", "titre", "duree_minutes", "classification_age", "actif"};
        PageRecherche pageRecherche = new PageRecherche(objet, request, criteres, null, 3, entetes, entetes.length);
        pageRecherche.setTitre("Liste des films");
        pageRecherche.setUtilisateur((user.UserEJB) session.getValue("u"));
        pageRecherche.setLien((String) session.getValue("lien"));
        pageRecherche.setApres("cinema/film/film-liste.jsp");
        pageRecherche.creerObjetPage(entetes, null);
        pageRecherche.getFormu().getChamp("code").setLibelle("Code");
        pageRecherche.getFormu().getChamp("titre").setLibelle("Titre");
        pageRecherche.getTableau().setLibelleAffiche(new String[]{"Code", "Titre", "Dur&eacute;e (minutes)", "&Acirc;ge conseill&eacute;", "Active"});
%>
<div class="content-wrapper">
    <section class="content-header"><h1><%= pageRecherche.getTitre() %></h1></section>
    <section class="content">
        <form action="<%=pageRecherche.getLien()%>?but=<%=pageRecherche.getApres()%>" method="post">
            <%= pageRecherche.getFormu().getHtmlEnsemble() %>
        </form>
        <%= pageRecherche.getTableauRecap().getHtml() %><br>
        <%= pageRecherche.getTableau().getHtml() %>
        <%= pageRecherche.getBasPage() %>
    </section>
</div>
<% } catch (Exception e) { e.printStackTrace(); %>
<script>alert('<%= e.getMessage() %>'); history.back();</script>
<% } %>
