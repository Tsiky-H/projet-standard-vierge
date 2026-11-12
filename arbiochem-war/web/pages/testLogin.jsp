<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>
<%@ page import="user.UserEJB" %>
<%@ page import="user.UserEJBBean" %>
<%@ page import="java.io.FileReader" %>
<%@ page import="utilitaire.MultiLangue" %>
<%
    try {
        String login = request.getParameter("identifiant");
        String password = request.getParameter("passe");
        String interim = request.getParameter("interim");
        UserEJB user = new UserEJBBean();
        user.testLogin(login, password, interim, "");

        String langue = request.getParameter("langue");
        if (langue == null || langue.trim().isEmpty()) langue = "fr";
        user.setLangue(langue);
        if (!"fr".equalsIgnoreCase(langue)) {
            String traduction = application.getRealPath("/traduction.txt");
            if (traduction != null) user.setMapTraduction(MultiLangue.fileToHash(new FileReader(traduction)));
        }
        session.setAttribute("langue", langue);
        session.setAttribute("u", user);
        session.setAttribute("config", user.findConfiguration());
        session.setMaxInactiveInterval(21600);
        session.setAttribute("lien", "module.jsp");
        String accueil = user.getHomePageUrl();
        if (accueil == null || accueil.trim().isEmpty()) accueil = "accueil.jsp";
        response.sendRedirect("module.jsp?but=" + java.net.URLEncoder.encode(accueil, "UTF-8"));
        return;
    } catch (Exception e) {
        String message = e.getMessage() == null ? "Échec de connexion" : e.getMessage();
        response.sendRedirect("../index.jsp?erreur=" + java.net.URLEncoder.encode(message, "UTF-8"));
        return;
    }
%>
