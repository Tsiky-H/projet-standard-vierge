<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ page import="affichage.PageRecherche" %>
<%@ page import="facturation.FactureClientLib" %>
<%
    try {
        FactureClientLib objet = new FactureClientLib();
        String[] entetes = {"idfactureclient", "numero_facture", "type_document", "idfacture_originelib", "idclientlib",
                "daty", "statut", "montant_ht", "montant_tva", "montant_ttc"};
        PageRecherche pageRecherche = new PageRecherche(
                objet, request, entetes, null, entetes.length, entetes, entetes.length);
        pageRecherche.setUtilisateur((user.UserEJB) session.getAttribute("u"));
        pageRecherche.setLien((String) session.getAttribute("lien"));
        pageRecherche.setApres("facturation/facture-client-liste.jsp");
        pageRecherche.creerObjetPage(entetes, null);
        pageRecherche.getFormu().getChamp("numero_facture").setLibelle("N° facture");
        pageRecherche.getFormu().getChamp("idclientlib").setLibelle("Client");
        pageRecherche.getFormu().getChamp("idfacture_originelib").setLibelle("Facture d'origine");
        pageRecherche.getFormu().getChamp("statut").setLibelle("Statut");
        pageRecherche.getTableau().setLibelleAffiche(new String[]{
                "Référence", "N° facture", "Type", "Facture d'origine", "Client", "Date", "Statut",
                "Montant HT", "TVA", "Montant TTC"});
        pageRecherche.getTableau().setLien(new String[]{pageRecherche.getLien() + "?but=facturation/facture-client-fiche.jsp"});
        pageRecherche.getTableau().setColonneLien(new String[]{"idfactureclient"});
%>
<div class="content-wrapper">
    <section class="content-header"><h1>Liste des factures</h1></section>
    <section class="content">
        <form action="<%=pageRecherche.getLien()%>?but=facturation/facture-client-liste.jsp" method="post">
            <%=pageRecherche.getFormu().getHtmlEnsemble()%>
        </form>
        <%=pageRecherche.getTableauRecap().getHtml()%>
        <%=pageRecherche.getTableau().getHtml()%>
        <%=pageRecherche.getBasPage()%>
    </section>
</div>
<%
    } catch (Exception e) {
        application.log("Erreur lors de l'affichage de la liste des factures.", e);
%>
<div class="alert alert-danger"><%=e.getMessage()%></div>
<%
    }
%>
