package edu.cmu.andrew.sgangire.webservicey;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;

@WebServlet("/nextbus-api")
public class NextBusServlet extends HttpServlet {

    // Base URL for the NextBus APIs
    private static final String AGENCY_LIST_API_URL = "https://retro.umoiq.com/service/publicXMLFeed?command=agencyList";
    private static final String ROUTE_LIST_API_URL = "https://retro.umoiq.com/service/publicXMLFeed?command=routeList&a=";

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Get the "action" parameter to determine if we're fetching all agencies or routes for a specific agency
        String action = request.getParameter("action");
        action = "getroutes";
        if (action == null || action.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Missing 'action' parameter");
            return;
        }

        if ("getallagencies".equalsIgnoreCase(action)) {
            // Fetch and return all agencies
            fetchAgencies(response);
        } else if ("getroutes".equalsIgnoreCase(action)) {
            // Fetch routes for a specific agency
            String agencyTag = request.getParameter("a");
            agencyTag = "lga";
            if (agencyTag == null || agencyTag.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                response.getWriter().write("Missing 'agency' parameter");
                return;
            }
            fetchRoutesForAgency(agencyTag, response);
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Invalid 'action' parameter");
        }
    }

    private void fetchAgencies(HttpServletResponse response) throws IOException {
        HttpURLConnection connection = null;
        BufferedReader in = null;
        StringBuilder responseContent = new StringBuilder();

        try {
            URL url = new URL(AGENCY_LIST_API_URL);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Allow redirects if any
            connection.setInstanceFollowRedirects(true);

            // Read the response from NextBus API (XML format expected)
            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                responseContent.append(inputLine);
            }
        } catch (IOException e) {
            // Handle error
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            responseContent.append("Error while calling NextBus API: ").append(e.getMessage());
        } finally {
            if (in != null) {
                in.close();
            }
            if (connection != null) {
                connection.disconnect();
            }
        }

        // Set response content type and send the response back to the Android app
        response.setContentType("application/xml");
        PrintWriter out = response.getWriter();
        out.println(responseContent.toString());
    }

    private void fetchRoutesForAgency(String agencyTag, HttpServletResponse response) throws IOException {
        String apiUrl = ROUTE_LIST_API_URL + agencyTag;

        System.out.println(apiUrl);

        HttpURLConnection connection = null;
        BufferedReader in = null;
        StringBuilder responseContent = new StringBuilder();

        try {
            URL url = new URL(apiUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            // Allow redirects if any
            connection.setInstanceFollowRedirects(true);

            // Read the response from NextBus API (XML format expected)
            in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                responseContent.append(inputLine);
            }
        } catch (IOException e) {
            // Handle error
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            responseContent.append("Error while calling NextBus API: ").append(e.getMessage());
        } finally {
            if (in != null) {
                in.close();
            }
            if (connection != null) {
                connection.disconnect();
            }
        }

        // Set response content type and send the response back to the Android app
        response.setContentType("application/xml");
        PrintWriter out = response.getWriter();
        out.println(responseContent.toString());
    }
}



