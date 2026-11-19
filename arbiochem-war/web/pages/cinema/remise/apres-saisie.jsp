<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ page import="cinema.Remise" %>
<%@ page import="user.UserEJB" %>
<%!
    private static String jsRemise(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("'", "\\'").replace("\r", " ").replace("\n", " ");
    }
%>
<%
    String erreur = null;
    try {
        UserEJB utilisateur = (UserEJB) session.getAttribute("u");
        if (utilisateur == null) {
            throw new IllegalStateException("La session utilisateur a expiré.");
        }

        String catReservee = request.getParameter("cat_reservee");
        String nombreValeur = request.getParameter("nombre");
        String pourcentageValeur = request.getParameter("val");
        String catRemise = request.getParameter("cat_remise");

        if (catReservee == null || catReservee.trim().isEmpty()) {
            throw new IllegalArgumentException("La catégorie déclenchante est obligatoire.");
        }
        if (catRemise == null || catRemise.trim().isEmpty()) {
            throw new IllegalArgumentException("La catégorie bénéficiaire est obligatoire.");
        }
        if (catReservee.equals(catRemise)) {
            throw new IllegalArgumentException("La catégorie réservée et la catégorie remisée doivent être différentes.");
        }

        Remise remise = new Remise();
        remise.setCat_reservee(catReservee.trim());
        remise.setNombre(Integer.parseInt(nombreValeur));
        remise.setVal(Double.parseDouble(pourcentageValeur));
        remise.setCat_remise(catRemise.trim());
        utilisateur.createObject(remise);
    } catch (Exception e) {
        erreur = e.getMessage() == null ? "La remise n'a pas pu être créée." : e.getMessage();
        application.log("Erreur lors de la création de la remise", e);
    }
%>
<script>
    <% if (erreur == null) { %>
        document.location.replace("module.jsp?but=cinema/remise/remise-liste.jsp");
    <% } else { %>
        alert('<%= jsRemise(erreur) %>');
        history.back();
    <% } %>
</script>
