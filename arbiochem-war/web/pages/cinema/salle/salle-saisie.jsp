<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ page import="affichage.Liste" %>
<%@ page import="affichage.Champ" %>
<%@ page import="affichage.PageInsert" %>
<%@ page import="bean.CGenUtil" %>
<%@ page import="cinema.CategoriePlaceLib" %>
<%@ page import="cinema.Salle" %>
<%@ page import="user.UserEJB" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.List" %>

<%
    String erreur = null;
    PageInsert pi = null;
    CategoriePlaceLib[] categories = new CategoriePlaceLib[0];
    try {
        UserEJB utilisateur = (UserEJB) session.getAttribute("u");
        if (utilisateur == null) throw new IllegalStateException("La session utilisateur a expiré.");
        pi = new PageInsert(new Salle(), request, utilisateur);
        pi.setLien((String) session.getAttribute("lien"));
        Liste actif = new Liste("actif", new Object[] { "Actif", "Inactif" }, new String[] { "1", "0" });
        actif.setEstMultiple(false);
        pi.getFormu().changerEnChamp(new Liste[] { actif });
        if (pi.getFormu().getChamp("idSalle") != null) pi.getFormu().getChamp("idSalle").setVisible(false);
        pi.getFormu().getChamp("capacite").setVisible(false);
        pi.getFormu().getChamp("libelle").setLibelle("Nom de la salle");
        pi.getFormu().getChamp("actif").setLibelle("État");
        pi.getFormu().getChamp("libelle").setAutre("required maxlength=\"100\"");
        pi.getFormu().getChamp("actif").setAutre("required");
        Object[] resultat = CGenUtil.rechercher(new CategoriePlaceLib(), null, null,
                " AND ACTIF = 1 ORDER BY IDCATEGORIEPLACE");
        categories = new CategoriePlaceLib[resultat.length];
        System.arraycopy(resultat, 0, categories, 0, resultat.length);
        if (categories.length == 0) throw new IllegalStateException("Créez d'abord au moins une catégorie de places active.");
        pi.preparerDataFormu();
        List<Champ> champs = new ArrayList<Champ>();
        for (Champ champ : pi.getFormu().getListeChamp()) champs.add(champ);
        for (int i = 0; i < categories.length; i++) {
            CategoriePlaceLib categorie = categories[i];
            Champ nombrePlaces = new Champ("nbPlaces_" + categorie.getIdcategorieplace());
            nombrePlaces.setLibelle("Rangée " + (char) ('A' + i) + " — " + categorie.getLibelle() + " (nombre de places)");
            nombrePlaces.setType("number");
            nombrePlaces.setDefaut("0");
            nombrePlaces.setAutre("required min=\"0\" step=\"1\"");
            champs.add(nombrePlaces);
        }
        pi.getFormu().setListeChamp(champs.toArray(new Champ[champs.size()]));
        pi.getFormu().makeHtmlInsertTabIndex();
    } catch (Exception e) {
        erreur = e.getMessage() == null ? "Impossible de préparer le formulaire de salle." : e.getMessage();
        application.log("Erreur lors de la préparation de salle-saisie.jsp", e);
    }
%>

<div class="content-wrapper">
    <section class="content-header"><h1>Nouvelle salle</h1></section>
    <section class="content">
        <% if (erreur != null) { %>
            <div class="alert alert-danger"><%= erreur %></div>
        <% } else { %>
            <div class="box box-primary">
                <div class="box-header with-border"><h3 class="box-title">Informations et plan initial</h3></div>
                <form action="<%= pi.getLien() %>?but=cinema/salle/apres-saisie.jsp" method="post" data-parsley-validate>
                    <div class="box-body">
                        <%= pi.getFormu().getHtmlInsert() %>
                        <p>Chaque catégorie active correspond à une rangée. La capacité est calculée à partir des places saisies.</p>
                    </div>
                    <div class="box-footer">
                        <a class="btn btn-default" href="module.jsp?but=cinema/salle/salle-liste.jsp">Annuler</a>
                        <button type="submit" class="btn btn-primary">Créer la salle et son plan</button>
                    </div>
                </form>
            </div>
        <% } %>
    </section>
</div>
