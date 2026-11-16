<%@page import="affichage.PageRecherche"%>
<%@page import="client.Client"%>
<%
    try {
        Client objet = new Client();
        String[] criteres = {"codeclient", "nom", "prenom", "email"};
        String[] entetes = {"codeclient", "nom", "prenom", "email", "telephone", "actif"};
        PageRecherche pageRecherche = new PageRecherche(objet, request, criteres, null, 3, entetes, entetes.length);
        pageRecherche.setTitre("Liste des clients");
        pageRecherche.setUtilisateur((user.UserEJB) session.getValue("u"));
        pageRecherche.setLien((String) session.getValue("lien"));
        pageRecherche.setApres("client/client-liste.jsp");
        pageRecherche.creerObjetPage(entetes, null);
        pageRecherche.getFormu().getChamp("codeclient").setLibelle("Code client");
        pageRecherche.getFormu().getChamp("nom").setLibelle("Nom");
        pageRecherche.getFormu().getChamp("prenom").setLibelle("Pr&eacute;nom");
        pageRecherche.getFormu().getChamp("email").setLibelle("Email");
        pageRecherche.getTableau().setLibelleAffiche(new String[]{"Code", "Nom", "Pr&eacute;nom", "Email", "T&eacute;l&eacute;phone", "Active"});
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
