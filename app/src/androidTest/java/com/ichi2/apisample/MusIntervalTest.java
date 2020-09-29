package com.ichi2.apisample;

import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.stubbing.answers.ThrowsExceptionClass;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


@RunWith(AndroidJUnit4.class)
public class MusIntervalTest {

    @Test
    public void checkExistenceNoSuchModel() {
        final String deck = "Music intervals";
        final long deckId = new Random().nextLong();
        final String model = "Music.intervals";

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class, new ThrowsExceptionClass(IllegalArgumentException.class));
        doReturn(null).when(helper).findModelIdByName(model);
        doReturn(deckId).when(helper).findDeckIdByName(deck);

        MusInterval mi = new MusInterval(helper, "", "C#3", model, deck);
        assertFalse(mi.isExistsInAnki());
    }

    @Test
    public void checkExistenceNoStartingNotes() {
        final String deck = "Music intervals";
        final long deckId = new Random().nextLong();
        final String model = "Music.intervals";
        final long modelId = new Random().nextLong();

        LinkedList<Map<String, String>> existingNotesData = new LinkedList<>();

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class, new ThrowsExceptionClass(IllegalArgumentException.class));
        doReturn(modelId).when(helper).findModelIdByName(model);
        doReturn(deckId).when(helper).findDeckIdByName(deck);
        doReturn(existingNotesData).when(helper).getNotes(modelId);

        MusInterval mi = new MusInterval(helper, "", "C#3", model, deck);
        assertFalse(mi.isExistsInAnki());
    }

    @Test
    public void checkExistenceNoSuchStartingNote() {
        final String deck = "Music intervals";
        final long deckId = new Random().nextLong();
        final String model = "Music.intervals";
        final long modelId = new Random().nextLong();

        Map<String, String> item1 = new HashMap<>();
        item1.put("start_note", "C1");
        Map<String, String> item2 = new HashMap<>();
        item2.put("start_note", "C2");

        LinkedList<Map<String, String>> existingNotesData = new LinkedList<>();
        existingNotesData.add(item1);
        existingNotesData.add(item2);

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class, new ThrowsExceptionClass(IllegalArgumentException.class));
        doReturn(modelId).when(helper).findModelIdByName(model);
        doReturn(deckId).when(helper).findDeckIdByName(deck);
        doReturn(existingNotesData).when(helper).getNotes(modelId);

        MusInterval mi = new MusInterval(helper, "", "C#3", model, deck);
        assertFalse(mi.isExistsInAnki());
    }

    @Test
    public void checkExistenceStartingNoteExists() {
        final String deck = "Music intervals";
        final long deckId = new Random().nextLong();
        final String model = "Music.intervals";
        final long modelId = new Random().nextLong();

        LinkedList<Map<String, String>> existingNotesData = new LinkedList<>();
        Map<String, String> item1 = new HashMap<>();
        item1.put("start_note", "C#3");
        existingNotesData.add(item1);

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class, new ThrowsExceptionClass(IllegalArgumentException.class));
        doReturn(modelId).when(helper).findModelIdByName(model);
        doReturn(deckId).when(helper).findDeckIdByName(deck);
        doReturn(existingNotesData).when(helper).getNotes(modelId);

        MusInterval mi = new MusInterval(helper, "", "C#3", model, deck);
        assertTrue(mi.isExistsInAnki());
    }

    @Test
    public void saveMiNoSuchModelCantCreate() {
        final String deck = "Music intervals";
        final long deckId = new Random().nextLong();
        final String model = "Music.intervals";

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class, new ThrowsExceptionClass(IllegalArgumentException.class));
        // can't create model for some reason
        doReturn(null).when(helper).findModelIdByName(model);
        doReturn(null).when(helper).addNewCustomModel(model, MusInterval.FIELDS, MusInterval.CARD_NAMES, MusInterval.QFMT, MusInterval.AFMT, MusInterval.CSS);
        // deck ok
        doReturn(deckId).when(helper).findDeckIdByName(deck);

        MusInterval mi = new MusInterval(helper, "", "C#3", model, deck);
        assertFalse(mi.addToAnki());
    }

    @Test
    public void saveMiNoSuchDeckCantCreate() {
        final String deck = "Music intervals";
        final String model = "Music.intervals";
        final long modelId = new Random().nextLong();

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class, new ThrowsExceptionClass(IllegalArgumentException.class));
        // model ok
        doReturn(modelId).when(helper).findModelIdByName(model);
        // can't create deck for some reason
        doReturn(null).when(helper).findDeckIdByName(deck);
        doReturn(null).when(helper).addNewDeck(deck);

        MusInterval mi = new MusInterval(helper, "", "C#3", model, deck);
        assertFalse(mi.addToAnki());
    }

    @Test
    public void saveMiNoSuchModelAndDeckNotCreated() {
        final String deck = "Music intervals";
        final long deckId = new Random().nextLong();
        final String model = "Music.intervals";
        final long modelId = new Random().nextLong();

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class, new ThrowsExceptionClass(IllegalArgumentException.class));
        // create model
        doReturn(null).when(helper).findModelIdByName(model);
        doReturn(modelId).when(helper).addNewCustomModel(model, MusInterval.FIELDS, MusInterval.CARD_NAMES, MusInterval.QFMT, MusInterval.AFMT, MusInterval.CSS);
        doNothing().when(helper).storeModelReference(model, modelId);
        // create deck
        doReturn(null).when(helper).findDeckIdByName(deck);
        doReturn(deckId).when(helper).addNewDeck(deck);
        doNothing().when(helper).storeDeckReference(deck, deckId);

        // can't create note for some reason
        doReturn(null).when(helper).addNote(eq(modelId), eq(deckId), any(Map.class), nullable(Set.class));

        MusInterval mi = new MusInterval(helper, "", "C#3", model, deck);
        assertFalse(mi.addToAnki());
    }

    @Test
    public void saveMiNoSuchModelAndDeckCreated() {
        final String deck = "Music intervals";
        final long deckId = new Random().nextLong();
        final String model = "Music.intervals";
        final long modelId = new Random().nextLong();
        final long noteId = new Random().nextLong();

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class, new ThrowsExceptionClass(IllegalArgumentException.class));
        // create model
        doReturn(null).when(helper).findModelIdByName(model);
        doReturn(modelId).when(helper).addNewCustomModel(model, MusInterval.FIELDS, MusInterval.CARD_NAMES, MusInterval.QFMT, MusInterval.AFMT, MusInterval.CSS);
        doNothing().when(helper).storeModelReference(model, modelId);
        // create deck
        doReturn(null).when(helper).findDeckIdByName(deck);
        doReturn(deckId).when(helper).addNewDeck(deck);
        doNothing().when(helper).storeDeckReference(deck, deckId);

        // successful note creation
        doReturn(noteId).when(helper).addNote(eq(modelId), eq(deckId), any(Map.class), nullable(Set.class));

        MusInterval mi = new MusInterval(helper, "", "C#3", model, deck);
        assertTrue(mi.addToAnki());
    }

    @Test
    public void saveMiExistingModelAndDeckCreated() {
        final String deck = "Music intervals";
        final long deckId = new Random().nextLong();
        final String model = "Music.intervals";
        final long modelId = new Random().nextLong();
        final long noteId = new Random().nextLong();

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class, new ThrowsExceptionClass(IllegalArgumentException.class));
        // existing model
        doReturn(modelId).when(helper).findModelIdByName(model);
        // existing deck
        doReturn(deckId).when(helper).findDeckIdByName(deck);

        // successful note creation
        doReturn(noteId).when(helper).addNote(eq(modelId), eq(deckId), any(Map.class), nullable(Set.class));

        MusInterval mi = new MusInterval(helper, "", "C#3", model, deck);
        assertTrue(mi.addToAnki());
    }

}
