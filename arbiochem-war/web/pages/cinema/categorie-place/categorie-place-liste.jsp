<%@page import="affichage.PageRecherche"%>
<%@page import="cinema.CategoriePlaceLib"%>
<%
    try {
        CategoriePlaceLib objet = new CategoriePlaceLib();
        String[] criteres = {"code", "libelle"};
        String[] intervalles = null;
        String[] entetes = {"code", "libelle", "prix_base", "actif"};
        PageRecherche pageRecherche = new PageRecherche(objet, request, criteres, intervalles, 3, entetes, entetes.length);
        pageRecherche.setTitre("Liste des cat&eacute;gories de places");
        pageRecherche.setUtilisateur((user.UserEJB) session.getValue("u"));
        pageRecherche.setLien((String) session.getValue("lien"));
        pageRecherche.setApres("cinema/categorie-place/categorie-place-liste.jsp");
        pageRecherche.creerObjetPage(entetes, null);
        pageRecherche.getFormu().getChamp("code").setLibelle("Code");
        pageRecherche.getFormu().getChamp("libelle").setLibelle("Libell&eacute;");
        pageRecherche.getTableau().setLibelleAffiche(new String[]{"Code", "Libell&eacute;", "Prix de base", "Active"});
%>
<div class="content-wrapper">
    <section class="content-header"><h1><%= pageRecherche.getTitre() %></h1></section>
    <section class="content">
        <form action="<%=pageRecherche.getLien()%>?but=<%=pageRecherche.getApres()%>" method="post">
            <%= pageRecherche.getFormu().getHtmlEnsemble() %>
        </form>
        <%= pageRecherche.getTableauRecap().getHtml() %>
        <br>
        <%= pageRecherche.getTableau().getHtml() %>
        <%= pageRecherche.getBasPage() %>
    </section>
</div>
<%
    } catch (Exception e) {
        e.printStackTrace();
%>
<script>alert('<%= e.getMessage() %>'); history.back();</script>
<% } %>
