<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ page import="affichage.PageInsert" %>
<%@ page import="bean.CGenUtil" %>
<%@ page import="cinema.CategoriePlaceLib" %>
<%@ page import="cinema.Salle" %>
<%@ page import="java.util.LinkedHashMap" %>
<%@ page import="user.UserEJB" %>
<%@ page import="service.SalleService" %>
<%!
    private static String jsSalle(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("'", "\\'").replace("\r", " ").replace("\n", " ");
    }
%>
<%
    String erreur = null;
    try {
        UserEJB utilisateur = (UserEJB) session.getAttribute("u");
        if (utilisateur == null) throw new IllegalStateException("La session utilisateur a expiré.");

        Salle salle = (Salle) new PageInsert(new Salle(), request).getObjectAvecValeur();
        salle.setNomTable("CINE_SALLE");
        Object[] resultat = CGenUtil.rechercher(new CategoriePlaceLib(), null, null,
                " AND ACTIF = 1 ORDER BY IDCATEGORIEPLACE");
        LinkedHashMap<String, Integer> nombreParCategorie = new LinkedHashMap<String, Integer>();
        for (Object objet : resultat) {
            CategoriePlaceLib categorie = (CategoriePlaceLib) objet;
            String valeur = request.getParameter("nbPlaces_" + categorie.getIdcategorieplace());
            int nombre;
            try {
                nombre = Integer.parseInt(valeur);
            } catch (Exception e) {
                throw new IllegalArgumentException("Saisissez un nombre de places valide pour chaque catégorie.");
            }
            if (nombre < 0) throw new IllegalArgumentException("Le nombre de places ne peut pas être négatif.");
            nombreParCategorie.put(categorie.getIdcategorieplace(), nombre);
        }
        SalleService.creerAvecPlan(utilisateur, salle, nombreParCategorie);
    } catch (Exception e) {
        application.log("Erreur lors de la création de salle et de son plan", e);
        erreur = e.getMessage() == null ? "La salle n'a pas pu être créée." : e.getMessage();
%>
<% } %>
<script>
    <% if (erreur == null) { %>
        document.location.replace("module.jsp?but=cinema/salle/salle-liste.jsp");
    <% } else { %>
        alert('<%= jsSalle(erreur) %>');
        history.back();
    <% } %>
</script>
