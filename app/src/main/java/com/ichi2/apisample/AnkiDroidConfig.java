package com.ichi2.apisample;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/** Some fields to store configuration details for AnkiDroid **/
final class AnkiDroidConfig {
    // Name of deck which will be created in AnkiDroid
    public static final String DECK_NAME = "API Sample";

    // Name of model which will be created in AnkiDroid
    public static final String MODEL_NAME = "com.ichi2.apisample";

    // Optional space separated list of tags to add to every note
    public static final Set<String> TAGS = new HashSet<>(Collections.singletonList("API_Sample_App"));

    // List of field names that will be used in AnkiDroid model
    public static final String[] FIELDS = {"Question", "Answer"};

    // List of card names that will be used in AnkiDroid (one for each direction of learning)
    public static final String[] CARD_NAMES = {"Question>Answer", "Answer>Question"};

    // CSS to share between all the cards
    public static final String CSS = ".card { color: black; }\n" +
            ".big { font-size: 48px; }\n" +
            ".small { font-size: 18px;}\n";

    // Template for the question of each card
    static final String QFMT1 = "<div class=big>QFMT1 Question = {{Question}}</div> <br/>What's the answer?";
    static final String QFMT2 = "<div class=big>QFMT2 Answer = {{Answer}}</div> <br/>What's the question?";
    public static final String[] QFMT = {QFMT1, QFMT2};

    // Template for the answer
    static final String AFMT1 = "<div class=big>AFMT1 Answer = {{Answer}}";
    static final String AFMT2 = "<div class=big>AFMT2 Question = {{Question}}";
    public static final String[] AFMT = {AFMT1, AFMT2};
}
