<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ page import="affichage.PageRecherche" %>
<%@ page import="reservation.Reservation" %>
<%
    try {
        Reservation objet = new Reservation();
        String[] entetes = {"idreservation", "idclient", "idmodepaiement_prevu",
                "date_reservation", "date_expiration", "statut", "nb_places_total", "montant_total"};
        PageRecherche pageRecherche = new PageRecherche(
                objet, request, entetes, null, entetes.length, entetes, entetes.length);
        pageRecherche.setUtilisateur((user.UserEJB) session.getAttribute("u"));
        pageRecherche.setLien((String) session.getAttribute("lien"));
        pageRecherche.setApres("reservation/reservation-liste.jsp");
        pageRecherche.creerObjetPage(entetes, null);
        pageRecherche.getFormu().getChamp("idreservation").setLibelle("Réservation");
        pageRecherche.getFormu().getChamp("idclient").setLibelle("Client");
        pageRecherche.getFormu().getChamp("statut").setLibelle("Statut");
        pageRecherche.getTableau().setLibelleAffiche(new String[]{
                "Réservation", "Client", "Mode de paiement prévu", "Date", "Expiration",
                "Statut", "Places", "Montant"});
        pageRecherche.getTableau().setLien(new String[]{pageRecherche.getLien() + "?but=reservation/reservation-fiche.jsp"});
        pageRecherche.getTableau().setColonneLien(new String[]{"idreservation"});
%>
<div class="content-wrapper">
    <section class="content-header"><h1>Liste des r&eacute;servations</h1></section>
    <section class="content">
        <form action="<%=pageRecherche.getLien()%>?but=reservation/reservation-liste.jsp" method="post">
            <%=pageRecherche.getFormu().getHtmlEnsemble()%>
        </form>
        <%=pageRecherche.getTableauRecap().getHtml()%>
        <%=pageRecherche.getTableau().getHtml()%>
        <%=pageRecherche.getBasPage()%>
    </section>
</div>
<%
    } catch (Exception e) {
        application.log("Erreur lors de l'affichage de la liste des réservations.", e);
%>
<div class="alert alert-danger"><%=e.getMessage()%></div>
<%
    }
%>
