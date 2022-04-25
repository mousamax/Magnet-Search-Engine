package com.magnet.Magnet;

import org.netpreserve.urlcanon.Canonicalizer;
import org.netpreserve.urlcanon.ParsedUrl;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;

public final class UrlUtils {

    private UrlUtils() {
        // nothing here ...
    }

    public static String normalizeUrl(String url) throws URISyntaxException {
        URI uri = new URI(url);

        String scheme = uri.getScheme();

        if (scheme == null) {
            throw new RuntimeException("URL scheme is required.");
        }

        String user = uri.getUserInfo();
        String host = uri.getHost();

        int port = normalizePort(scheme, uri.getPort());
        String path = normalizePath(uri.getPath());
        String query = normalizeQuery(uri.getQuery());
        String fragment = normalizeFragment(uri.getFragment());

        URI result = new URI(scheme, user, host, port, path, query, fragment);
        //second layer of normalization
        String resultString = result.toString();
        Canonicalizer.WHATWG.canonicalize(ParsedUrl.parseUrl(resultString));

        return resultString;
    }

    private static int normalizePort(String scheme, int port) {
        switch (port) {
            case 80:
                if ("http".equals(scheme)) {
                    return -1;
                }
                break;

            case 443:
                if ("https".equals(scheme)) {
                    return -1;
                }
                break;
        }
        return port;
    }

    private static String normalizePath(String path) {
        String result = removeDuplicates(path, '/');
        if (result == null || result.isEmpty()) {
            return null;
        }
        int length = result.length();
        char value = result.charAt(length - 1);
        if (value == '/') {
            return result.substring(0, length - 1);
        }
        return result;
    }

    private static String normalizeQuery(String query) {
        if (query == null || query.isEmpty()) {
            return null;
        }
        String[] parts = query.split("&");
        if (parts.length > 1) {
            Arrays.sort(parts);
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < parts.length; ++i) {
                String part = parts[i];
                if (part.isEmpty()) {
                    continue;
                }
                int length = builder.length();
                if (length > 0) {
                    builder.append("&");
                }
                builder.append(part);
            }
            return builder.toString();
        }
        return query;
    }

    private static String normalizeFragment(String fragment) {
        if (fragment == null || fragment.isEmpty()) {
            return null;
        }
        return fragment;
    }

    private static String removeDuplicates(String text, char character) {
        if (text == null || text.isEmpty()) {
            return text;
        }
        StringBuilder builder = new StringBuilder();
        int duplicatesCount = 0;
        int textLength = text.length();
        for (int i = 0; i < textLength; ++i) {
            char value = text.charAt(i);
            if (value == character) {
                duplicatesCount += 1;
                if (duplicatesCount > 1) {
                    continue;
                }
            } else {
                duplicatesCount = 0;
            }
            builder.append(value);
        }
        return builder.toString();
    }
}