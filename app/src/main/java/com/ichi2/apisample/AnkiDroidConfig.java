package com.ichi2.apisample;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/** Some fields to store configuration details for AnkiDroid **/
final class AnkiDroidConfig {
    // Name of deck which will be created in AnkiDroid
    public static final String DECK_NAME = "Music intervals";

    // Name of model which will be created in AnkiDroid
    public static final String MODEL_NAME = "Music.interval";

    // Optional space separated list of tags to add to every note
    public static final Set<String> TAGS = new HashSet<>(Collections.singletonList("API_Sample_App"));

    // List of field names that will be used in AnkiDroid model
    public static final String[] FIELDS = {"sound", "interval_description", "start_note",
            "ascending_descending", "melodic_harmonic", "interval", "tempo", "instrument"};

    // List of card names that will be used in AnkiDroid (one for each direction of learning)
    public static final String[] CARD_NAMES = {"Question > Answer"};

    // CSS to share between all the cards
    public static final String CSS = ".card {\n" +
            "  font-family: arial;\n" +
            "  font-size: 20px;\n" +
            "  text-align: center;\n" +
            "  color: black;\n" +
            "  background-color: white;\n" +
            "}\n" +
            "\n" +
            ".the_answer {\n" +
            "  font-size:40px;\n" +
            "  font-face:bold;\n" +
            "  color:green;\n" +
            "}";

    // Template for the question of each card
    static final String QFMT1 = "{{sound}}\n" +
            "Which interval is it?";

    static final String QFMT2 = "{{FrontSide}}\n" +
            "\n" +
            "<hr id=answer>\n" +
            "\n" +
            "{{interval_description}}\n" +
            "<img src=\"_wils_{{start_note}}_{{ascending_descending}}_{{melodic_harmonic}}_{{interval}}.jpg\" onerror=\"this.style.display='none'\"/>\n" +
            "<img src=\"_wila_{{interval}}_.jpg\" onerror=\"this.style.display='none'\"/>\n" +
            "<div id=\"interval_longer_name\" class=\"the_answer\"></div>\n" +
            "{{start_note}}, {{ascending_descending}}, {{melodic_harmonic}}, <span id=\"interval_short_name\">{{interval}}</span>; {{tempo}}BPM, {{instrument}}\n" +
            "\n" +
            "<script>\n" +
            "function intervalLongerName(intervalShortName) {\n" +
            "  var longerName = {\n" +
            "    'min2': 'minor 2nd',\n" +
            "    'Maj2': 'Major 2nd'\n" +
            "  };\n" +
            "  return longerName[intervalShortName];\n" +
            "}\n" +
            "\n" +
            "document.getElementById(\"interval_longer_name\").innerText =\n" +
            "    intervalLongerName(document.getElementById(\"interval_short_name\").innerText);\n" +
            "\n" +
            "</script>\n" +
            "\n";

    public static final String[] QFMT = {QFMT1, QFMT2};
}
