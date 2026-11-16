<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ page import="cinema.PlaceSeanceDispo" %>
<%@ page import="service.PlanSalleService" %>
<%@ page import="user.UserEJB" %>
<%@ page import="java.text.SimpleDateFormat" %>
<%@ page import="java.util.LinkedHashMap" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Locale" %>
<%@ page import="java.util.Map" %>

<%!
    /** Échappe une valeur avant de l'écrire dans le HTML. */
    private static String h(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&#39;");
    }
%>

<%
    UserEJB utilisateur = (UserEJB) session.getAttribute("u");
    String idClient = request.getParameter("idclient");
    String idSeance = request.getParameter("idseance");
    String idMode = request.getParameter("idmodepaiement_prevu");

    String erreur = null;
    PlaceSeanceDispo[] places = new PlaceSeanceDispo[0];
    Map<String, List<PlaceSeanceDispo>> parRangee = new LinkedHashMap<String, List<PlaceSeanceDispo>>();
    Map<String, String[]> categories = new LinkedHashMap<String, String[]>();
    int nombreLibres = 0;

    if (utilisateur == null) {
        erreur = "La session utilisateur a expiré. Reconnectez-vous.";
    } else if (idClient == null || idClient.trim().isEmpty()
            || idSeance == null || idSeance.trim().isEmpty()) {
        erreur = "Choisissez d'abord un client et une séance.";
    } else {
        try {
            places = PlanSalleService.listerPlacesSeance(idSeance);
            if (places.length == 0) {
                erreur = "Aucune place n'est définie pour la salle de cette séance.";
            }
            for (PlaceSeanceDispo place : places) {
                String rangee = place.getRangee() == null ? "" : place.getRangee();
                List<PlaceSeanceDispo> ligne = parRangee.get(rangee);
                if (ligne == null) {
                    ligne = new ArrayList<PlaceSeanceDispo>();
                    parRangee.put(rangee, ligne);
                }
                ligne.add(place);
                String idCat = place.getIdcategorieplace();
                if (idCat != null && !categories.containsKey(idCat)) {
                    categories.put(idCat, new String[] {
                        place.getIdcategorieplacelib(),
                        String.format(Locale.FRANCE, "%,.0f", place.getPrix_base()),
                        String.valueOf(categories.size() % 6)
                    });
                }
                if (place.getDisponible() == 1) nombreLibres++;
            }
        } catch (Exception e) {
            erreur = "Impossible de charger le plan de la salle.";
            application.log("Erreur lors du chargement du plan de reservation-places.jsp", e);
        }
    }

    SimpleDateFormat formatDate = new SimpleDateFormat("dd/MM/yyyy 'à' HH:mm");
%>

<style>
    .plan-ecran { text-align: center; margin: 15px auto 25px; padding: 6px; max-width: 60%;
                  background: #ddd; border-radius: 0 0 40px 40px; font-weight: bold; letter-spacing: 4px; }
    .plan-rangee { display: flex; align-items: center; justify-content: center; margin-bottom: 8px; }
    .plan-rangee-nom { width: 32px; font-weight: bold; text-align: center; }
    .plan-place { position: relative; margin: 0 3px; }
    .plan-place input { position: absolute; opacity: 0; width: 0; height: 0; }
    .plan-place label { display: block; min-width: 42px; padding: 6px 4px; margin: 0; text-align: center;
                        border-radius: 6px 6px 3px 3px; border: 2px solid transparent; font-size: 12px;
                        color: #fff; cursor: pointer; }
    .plan-place .cat0 { background: #3c8dbc; } .plan-place .cat1 { background: #d4a017; }
    .plan-place .cat2 { background: #00a65a; } .plan-place .cat3 { background: #8e44ad; }
    .plan-place .cat4 { background: #e67e22; } .plan-place .cat5 { background: #16a085; }
    .plan-place input:checked + label { border-color: #222; box-shadow: 0 0 0 2px #fff inset; font-weight: bold; }
    .plan-place input:disabled + label { background: #c7c7c7; color: #777; cursor: not-allowed; text-decoration: line-through; }
    .plan-legende span { display: inline-block; margin-right: 18px; }
    .plan-puce { display: inline-block; width: 14px; height: 14px; border-radius: 3px; vertical-align: middle; margin-right: 4px; }
    .plan-puce.cat0 { background: #3c8dbc; } .plan-puce.cat1 { background: #d4a017; }
    .plan-puce.cat2 { background: #00a65a; } .plan-puce.cat3 { background: #8e44ad; }
    .plan-puce.cat4 { background: #e67e22; } .plan-puce.cat5 { background: #16a085; }
    .plan-puce.occupee { background: #c7c7c7; }
    .plan-resume { margin-top: 20px; font-size: 16px; }
</style>

<div class="content-wrapper">
    <section class="content-header">
        <h1>Choisir les places</h1>
    </section>

    <section class="content">
        <% if (erreur != null) { %>
            <div class="alert alert-danger"><%= h(erreur) %></div>
            <a class="btn btn-default" href="module.jsp?but=reservation/reservation-saisie.jsp">Retour</a>
        <% } else {
            PlaceSeanceDispo premiere = places[0];
        %>
            <div class="box box-primary">
                <div class="box-header with-border">
                    <h3 class="box-title">
                        <%= h(premiere.getIdfilmlib()) %> &mdash; <%= h(premiere.getIdsallelib()) %>
                        <% if (premiere.getDateheure_debut() != null) { %>
                            &mdash; <%= formatDate.format(premiere.getDateheure_debut()) %>
                        <% } %>
                    </h3>
                </div>

                <form action="module.jsp?but=reservation/apres-reservation.jsp" method="post">
                    <input type="hidden" name="idclient" value="<%= h(idClient) %>">
                    <input type="hidden" name="idseance" value="<%= h(idSeance) %>">
                    <input type="hidden" name="idmodepaiement_prevu" value="<%= h(idMode) %>">
                    <input type="hidden" name="bute" value="reservation/reservation-liste.jsp">

                    <div class="box-body">
                        <div class="plan-legende">
                            <% for (String[] categorie : categories.values()) { %>
                                <span><i class="plan-puce cat<%= categorie[2] %>"></i>
                                    <%= h(categorie[0]) %> &ndash; <%= categorie[1] %></span>
                            <% } %>
                            <span><i class="plan-puce occupee"></i>Indisponible</span>
                        </div>

                        <div class="plan-ecran">&Eacute;CRAN</div>

                        <% for (Map.Entry<String, List<PlaceSeanceDispo>> ligne : parRangee.entrySet()) { %>
                            <div class="plan-rangee">
                                <span class="plan-rangee-nom"><%= h(ligne.getKey()) %></span>
                                <% for (PlaceSeanceDispo place : ligne.getValue()) {
                                    boolean libre = place.getDisponible() == 1;
                                    String[] categorie = categories.get(place.getIdcategorieplace());
                                    String classe = categorie == null ? "cat0" : "cat" + categorie[2];
                                    String identifiant = "place_" + h(place.getIdplace());
                                    String titre = libre ? "Disponible"
                                            : "Indisponible (" + h(place.getStatut_occupation()) + ")";
                                %>
                                    <span class="plan-place">
                                        <input type="checkbox" name="places" id="<%= identifiant %>"
                                               value="<%= h(place.getIdplace()) %>"
                                               data-prix="<%= place.getPrix_base() %>"
                                               <%= libre ? "" : "disabled" %>>
                                        <label for="<%= identifiant %>" class="<%= classe %>"
                                               title="<%= titre %>"><%= h(place.getNumero()) %></label>
                                    </span>
                                <% } %>
                            </div>
                        <% } %>

                        <% if (nombreLibres == 0) { %>
                            <div class="alert alert-warning" style="margin-top:20px;">
                                Il ne reste aucune place disponible pour cette séance.
                            </div>
                        <% } %>

                        <div class="plan-resume">
                            <strong id="nbPlaces">0</strong> place(s) s&eacute;lectionn&eacute;e(s)
                            &mdash; total : <strong id="totalPlaces">0</strong>
                        </div>
                    </div>

                    <div class="box-footer">
                        <a class="btn btn-default" href="module.jsp?but=reservation/reservation-saisie.jsp">Retour</a>
                        <button type="submit" id="btnReserver" class="btn btn-primary" disabled>
                            Valider la r&eacute;servation
                        </button>
                    </div>
                </form>
            </div>

            <script>
                (function () {
                    var cases = document.querySelectorAll('.plan-place input[type=checkbox]');
                    var nb = document.getElementById('nbPlaces');
                    var total = document.getElementById('totalPlaces');
                    var bouton = document.getElementById('btnReserver');
                    function maj() {
                        var n = 0, somme = 0;
                        for (var i = 0; i < cases.length; i++) {
                            if (cases[i].checked) {
                                n++;
                                somme += parseFloat(cases[i].getAttribute('data-prix')) || 0;
                            }
                        }
                        nb.textContent = n;
                        total.textContent = somme.toLocaleString('fr-FR');
                        bouton.disabled = (n === 0);
                    }
                    for (var i = 0; i < cases.length; i++) cases[i].addEventListener('change', maj);
                    maj();
                })();
            </script>
        <% } %>
    </section>
</div>
