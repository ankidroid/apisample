package com.ichi2.apisample;

import java.util.LinkedList;
import java.util.Map;


public class MusInterval {
    // Name of deck which will be created in AnkiDroid
    public static final String DECK_NAME = "Music intervals";
    // Name of model which will be created in AnkiDroid
    public static final String MODEL_NAME = "Music.interval";
    // List of field names that will be used in AnkiDroid model
    public static final String[] FIELDS = {"sound", "start_note"};
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
    public static final String[] QFMT = {QFMT1};
    static final String AFMT1 = "{{FrontSide}}\n" +
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

    public static final String[] AFMT = {AFMT1};
    private final AnkiDroidHelper mHelper;

    private final String mModelName;
    private Long mModelId;
    private final String mDeckName;
    private Long mDeckId;

    private final Map<String, String> mData;

    /**
     * Construct MusInterval instance.
     *
     * @param helper
     * @param modelName
     * @param deckName
     * @param data
     */
    public MusInterval(AnkiDroidHelper helper, String modelName, String deckName, Map<String, String> data) {
        mHelper = helper;

        mModelName = modelName;
        mModelId = mHelper.findModelIdByName(modelName);
        mDeckName = deckName;
        mDeckId = mHelper.findDeckIdByName(deckName);

        mData = data;
    }

    public MusInterval(AnkiDroidHelper mAnkiDroid, Map<String, String> data) {
        this(mAnkiDroid, MODEL_NAME, DECK_NAME, data);
    }

    /**
     * Check if such a data already exists in the AnkiDroid.
     *
     * @return
     */
    public boolean isExistsInAnki() {
        if (mModelId == null) {
            return false;
        }

        LinkedList<Map<String, String>> notes = mHelper.getNotes(mModelId);

        for (Map<String, String> note : notes) {
            boolean exists = true;

            for (String field : mData.keySet()) { //noinspection ConstantConditions
                if (!mData.get(field).isEmpty()
                        && note.containsKey(field)
                        && !mData.get(field).equals(note.get(field))) {
                    exists = false;
                }
            }

            if (exists) {
                return true;
            }
        }

        return false;
    }

    /**
     * Insert the data into AnkiDroid via API.
     * Also created a model and a deck if not yet created.
     *
     * @return True in case of successful action
     */
    public boolean addToAnki() {
        if (mModelId == null) {
            mModelId = mHelper.addNewCustomModel(
                    mModelName,
                    FIELDS,
                    CARD_NAMES,
                    QFMT,
                    AFMT,
                    CSS
            );
            if (mModelId == null) {
                return false;  // @todo Probably throw exception ?
            }
            mHelper.storeModelReference(mModelName, mModelId);
        }

        if (mDeckId == null) {
            mDeckId = mHelper.addNewDeck(mDeckName);
            if (mDeckId == null) {
                return false;  // @todo Probably throw exception ?
            }
            mHelper.storeDeckReference(mDeckName, mDeckId);
        }

        Long noteId = mHelper.addNote(mModelId, mDeckId, mData, null);
        return noteId != null;
    }
}
