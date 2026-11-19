<%@page import="affichage.PageRecherche"%>
<%@page import="cinema.SeanceLib"%>
<%
    try {
        SeanceLib objet = new SeanceLib();
        String[] criteres = {"idfilmlib", "idsallelib"};
        String[] entetes = {"libelle_seance", "dateheure_debut", "dateheure_fin", "statut"};
        PageRecherche pageRecherche = new PageRecherche(objet, request, criteres, null, 3, entetes, entetes.length);
        pageRecherche.setTitre("Liste des séances");
        pageRecherche.setUtilisateur((user.UserEJB) session.getAttribute("u"));
        pageRecherche.setLien((String) session.getAttribute("lien"));
        pageRecherche.setApres("cinema/seance/seance-liste.jsp");
        pageRecherche.creerObjetPage(entetes, null);
        pageRecherche.getFormu().getChamp("idfilmlib").setLibelle("Film");
        pageRecherche.getFormu().getChamp("idsallelib").setLibelle("Salle");
        pageRecherche.getTableau().setLibelleAffiche(new String[]{"Séance", "Début", "Fin", "Statut"});
%>
<div class="content-wrapper">
    <section class="content-header"><h1><%= pageRecherche.getTitre() %></h1></section>
    <section class="content">
        <p><a class="btn btn-primary" href="module.jsp?but=cinema/seance/seance-saisie.jsp">Nouvelle séance</a></p>
        <form action="<%= pageRecherche.getLien() %>?but=<%= pageRecherche.getApres() %>" method="post">
            <%= pageRecherche.getFormu().getHtmlEnsemble() %>
        </form>
        <%= pageRecherche.getTableauRecap().getHtml() %><br>
        <%= pageRecherche.getTableau().getHtml() %>
        <%= pageRecherche.getBasPage() %>
    </section>
</div>
<% } catch (Exception e) { application.log("Erreur lors du chargement de seance-liste.jsp", e); %>
<div class="alert alert-danger">Impossible de charger la liste des séances.</div>
<% } %>
