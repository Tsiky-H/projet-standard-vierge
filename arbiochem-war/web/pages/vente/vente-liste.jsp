<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ page import="affichage.PageRecherche" %>
<%@ page import="vente.Vente" %>
<%
    try {
        Vente objet = new Vente();
        String[] entetes = {"idvente", "daty", "type_vente", "statut", "idclient",
                "idreservation", "idfactureclient", "montant_total"};
        PageRecherche pageRecherche = new PageRecherche(
                objet, request, entetes, null, entetes.length, entetes, entetes.length);
        pageRecherche.setUtilisateur((user.UserEJB) session.getAttribute("u"));
        pageRecherche.setLien((String) session.getAttribute("lien"));
        pageRecherche.setApres("vente/vente-liste.jsp");
        pageRecherche.creerObjetPage(entetes, null);
        pageRecherche.getFormu().getChamp("idvente").setLibelle("Vente");
        pageRecherche.getFormu().getChamp("statut").setLibelle("Statut");
        pageRecherche.getFormu().getChamp("idreservation").setLibelle("Réservation");
        pageRecherche.getTableau().setLibelleAffiche(new String[]{
                "Vente", "Date", "Type", "Statut", "Client", "Réservation",
                "Facture", "Montant"});
        pageRecherche.getTableau().setLien(new String[]{pageRecherche.getLien() + "?but=vente/vente-fiche.jsp"});
        pageRecherche.getTableau().setColonneLien(new String[]{"idvente"});
%>
<div class="content-wrapper">
    <section class="content-header"><h1>Liste des ventes brouillon</h1></section>
    <section class="content">
        <form action="<%=pageRecherche.getLien()%>?but=vente/vente-liste.jsp" method="post">
            <%=pageRecherche.getFormu().getHtmlEnsemble()%>
        </form>
        <%=pageRecherche.getTableauRecap().getHtml()%>
        <%=pageRecherche.getTableau().getHtml()%>
        <%=pageRecherche.getBasPage()%>
    </section>
</div>
<%
    } catch (Exception e) {
        application.log("Erreur lors de l'affichage de la liste des ventes.", e);
%>
<div class="alert alert-danger"><%=e.getMessage()%></div>
<%
    }
%>
