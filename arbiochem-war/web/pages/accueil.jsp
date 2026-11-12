<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ page import="user.UserEJB" %>
<%@ page import="historique.MapUtilisateur" %>
<%
    UserEJB utilisateur = (UserEJB) session.getAttribute("u");
    MapUtilisateur compte = utilisateur == null ? null : utilisateur.getUser();
%>
<section class="content-header"><h1>Accueil cinéma</h1></section>
<section class="content">
  <div class="box box-primary"><div class="box-body">
    <h2>Bienvenue<% if (compte != null && compte.getNomuser() != null) { %>, <%= compte.getNomuser() %><% } %></h2>
    <p>La base APJ est prête. Les écrans de gestion seront ajoutés au fur et à mesure.</p>
  </div></div>
</section>
