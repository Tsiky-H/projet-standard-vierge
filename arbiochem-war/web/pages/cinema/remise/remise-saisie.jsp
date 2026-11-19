<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ page import="affichage.Liste" %>
<%@ page import="affichage.PageInsert" %>
<%@ page import="cinema.CategoriePlaceLib" %>
<%@ page import="cinema.Remise" %>
<%@ page import="user.UserEJB" %>
<%
    UserEJB utilisateur = (UserEJB) session.getAttribute("u");
    String erreur = null;
    PageInsert pi = null;
    try {
        if (utilisateur == null) {
            throw new IllegalStateException("La session utilisateur a expiré.");
        }

        CategoriePlaceLib categorie = new CategoriePlaceLib();
        Liste categoriesReservees = new Liste("cat_reservee", categorie, "libelle", "idcategorieplace");
        categoriesReservees.setApresW(" AND ACTIF = 1");

        Liste categoriesRemise = new Liste("cat_remise", categorie, "libelle", "idcategorieplace");
        categoriesRemise.setApresW(" AND ACTIF = 1");

        pi = new PageInsert(new Remise(), request, utilisateur);
        pi.setLien((String) session.getAttribute("lien"));
        pi.getFormu().changerEnChamp(new Liste[]{categoriesReservees, categoriesRemise});

        if (pi.getFormu().getChamp("id") != null) {
            pi.getFormu().getChamp("id").setVisible(false);
        }
        pi.getFormu().getChamp("cat_reservee").setLibelle("Catégorie déclenchante");
        pi.getFormu().getChamp("nombre").setLibelle("Nombre minimum de places");
        pi.getFormu().getChamp("val").setLibelle("Pourcentage de remise");
        pi.getFormu().getChamp("cat_remise").setLibelle("Catégorie bénéficiant de la remise");

        pi.getFormu().getChamp("nombre").setType("number");
        pi.getFormu().getChamp("nombre").setAutre("required min=\"1\" step=\"1\"");
        pi.getFormu().getChamp("val").setType("number");
        pi.getFormu().getChamp("val").setAutre("required min=\"0.01\" max=\"100\" step=\"0.01\"");

        pi.preparerDataFormu();
        pi.getFormu().makeHtmlInsertTabIndex();
    } catch (Exception e) {
        erreur = e.getMessage() == null ? "Impossible de préparer le formulaire de remise." : e.getMessage();
        application.log("Erreur lors de la préparation de remise-saisie.jsp", e);
    }
%>

<div class="content-wrapper">
    <section class="content-header"><h1>Nouvelle remise</h1></section>
    <section class="content">
        <% if (erreur != null) { %>
            <div class="alert alert-danger"><%= erreur %></div>
        <% } else { %>
            <div class="box box-primary">
                <form action="<%= pi.getLien() %>?but=cinema/remise/apres-saisie.jsp" method="post" data-parsley-validate>
                    <div class="box-body">
                        <%= pi.getFormu().getHtmlInsert() %>
                        <p class="help-block"><strong>Les deux catégories doivent être différentes.</strong></p>
                    </div>
                    <div class="box-footer">
                        <a class="btn btn-default" href="module.jsp?but=cinema/remise/remise-liste.jsp">Annuler</a>
                        <button type="submit" class="btn btn-primary">Créer la remise</button>
                    </div>
                </form>
            </div>
        <% } %>
    </section>
</div>
