package com.sjapps.jsonlist.core.controllers;

import com.sjapps.jsonlist.core.AppState;

public abstract class RawJsonView{


    int textColor;
    int keyColor;
    int numberColor;
    int booleanAndNullColor;
    int bgColor;
    public boolean showJson;
    public boolean isRawJsonLoaded;

    public RawJsonView(int textColor, int keyColor, int numberColor, int booleanAndNullColor, int bgColor) {
        this.textColor = textColor;
        this.keyColor = keyColor;
        this.numberColor = numberColor;
        this.booleanAndNullColor = booleanAndNullColor;
        this.bgColor = bgColor;
    }

    public String generateHtml(String jsonStr, AppState state) {

        String textColorHex = String.format("#%06X", (0xFFFFFF & textColor));
        String keyColorHex = String.format("#%06X", (0xFFFFFF & keyColor));
        String numberColorHex = String.format("#%06X", (0xFFFFFF & numberColor));
        String booleanAndNullColorHex = String.format("#%06X", (0xFFFFFF & booleanAndNullColor));
        String bgColorHex = String.format("#%06X", (0xFFFFFF & bgColor));

        if (state != null && state.isSyntaxHighlighting())
            jsonStr = highlightJsonSyntax(jsonStr);

        String style =
                ".key { color: " + keyColorHex + "; }" +
                        ".string { color: " + textColorHex + "; }" +
                        ".number { color: " + numberColorHex + "; }" +
                        ".boolean { color: " + booleanAndNullColorHex + "; }" +
                        ".null { color: " + booleanAndNullColorHex + "; }";

        return "<html>" +
                "<head>" +
                "<style>" +
                "body { background-color: " + bgColorHex + "; color: " + textColorHex + "; padding: 10px; }" +
                style +
                "</style>" +
                "</head>" +
                "<body>" +
                "<pre>" + jsonStr + "</pre>" +
                "</body>" +
                "</html>";
    }

    private String highlightJsonSyntax(String json) {
        json = json.replaceAll("&", "&amp;")
                .replaceAll("<", "&lt;")
                .replaceAll(">", "&gt;")
                .replaceAll("\"(.*?)\"(?=\\s*:)", "<span class='key'>\"$1\"</span>") // Keys
                .replaceAll(":\\s*\"(.*?)\"", ": <span class='string'>\"$1\"</span>") // Strings
                .replaceAll(":\\s*(-?\\d+(\\.\\d+)?)", ": <span class='number'>$1</span>") // Numbers
                .replaceAll(":\\s*(true|false)", ": <span class='boolean'>$1</span>") // Booleans
                .replaceAll(":\\s*(null)", ": <span class='null'>$1</span>"); // Null
        return json;
    }

    public abstract void toggleSplitView();
    public abstract void ShowJSON();
    public abstract void updateRawJson(String string);
}


