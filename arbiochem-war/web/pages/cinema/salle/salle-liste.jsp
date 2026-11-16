<%@page import="affichage.PageRecherche"%>
<%@page import="cinema.SalleLib"%>
<%
    try {
        SalleLib objet = new SalleLib();
        String[] criteres = {"code", "libelle"};
        String[] entetes = {"code", "libelle", "capacite", "nombre_places", "actif"};
        PageRecherche pageRecherche = new PageRecherche(objet, request, criteres, null, 3, entetes, entetes.length);
        pageRecherche.setTitre("Liste des salles");
        pageRecherche.setUtilisateur((user.UserEJB) session.getValue("u"));
        pageRecherche.setLien((String) session.getValue("lien"));
        pageRecherche.setApres("cinema/salle/salle-liste.jsp");
        pageRecherche.creerObjetPage(entetes, null);
        pageRecherche.getFormu().getChamp("code").setLibelle("Code");
        pageRecherche.getFormu().getChamp("libelle").setLibelle("Libell&eacute;");
        pageRecherche.getTableau().setLibelleAffiche(new String[]{"Code", "Libell&eacute;", "Capacit&eacute;", "Nombre de places", "Active"});
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
