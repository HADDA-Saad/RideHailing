package com.ridehailing.auth.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.IOException;

final class ProblemDetailWriter {

    private ProblemDetailWriter() {
    }

    static void write(HttpServletResponse response,
                      String instance,
                      HttpStatus status,
                      String title,
                      String detail) throws IOException {

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);

        String body = "{"
                + "\"type\":\"about:blank\","
                + "\"title\":\"" + escape(title) + "\","
                + "\"status\":" + status.value() + ","
                + "\"detail\":\"" + escape(detail) + "\","
                + "\"instance\":\"" + escape(instance) + "\""
                + "}";

        response.getWriter().write(body);
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
