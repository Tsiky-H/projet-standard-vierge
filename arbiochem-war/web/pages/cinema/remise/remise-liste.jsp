<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ page import="affichage.PageRecherche" %>
<%@ page import="cinema.RemiseLib" %>
<%
    try {
        RemiseLib objet = new RemiseLib();
        String[] criteres = {"cat_reservee_lib", "cat_remise_lib"};
        String[] entetes = {"id", "cat_reservee_lib", "nombre", "val", "cat_remise_lib"};
        PageRecherche pageRecherche = new PageRecherche(objet, request, criteres, null, 3, entetes, entetes.length);
        pageRecherche.setTitre("Liste des remises");
        pageRecherche.setUtilisateur((user.UserEJB) session.getValue("u"));
        pageRecherche.setLien((String) session.getValue("lien"));
        pageRecherche.setApres("cinema/remise/remise-liste.jsp");
        pageRecherche.creerObjetPage(entetes, null);
        pageRecherche.getFormu().getChamp("cat_reservee_lib").setLibelle("Catégorie déclenchante");
        pageRecherche.getFormu().getChamp("cat_remise_lib").setLibelle("Catégorie remisée");
        pageRecherche.getTableau().setLibelleAffiche(new String[]{"Identifiant", "Catégorie déclenchante", "Nombre minimum", "Remise (%)", "Catégorie remisée"});
%>
<div class="content-wrapper">
    <section class="content-header"><h1><%= pageRecherche.getTitre() %></h1></section>
    <section class="content">
        <p><a class="btn btn-primary" href="module.jsp?but=cinema/remise/remise-saisie.jsp">Nouvelle remise</a></p>
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
