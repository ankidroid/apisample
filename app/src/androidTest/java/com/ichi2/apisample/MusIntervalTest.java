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
    public void checkExistence_NoSuchModel() {
        final String deck = "Music intervals";
        final long deckId = new Random().nextLong();
        final String model = "Music.intervals";

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class, new ThrowsExceptionClass(IllegalArgumentException.class));
        doReturn(null).when(helper).findModelIdByName(model);
        doReturn(deckId).when(helper).findDeckIdByName(deck);

        MusInterval mi = new MusInterval(helper, "", "C#3", MusInterval.Fields.AscDesc.ASC, model, deck);
        assertFalse(mi.existsInAnki());
    }

    @Test
    public void checkExistence_NoStartingNotes() {
        final String deck = "Music intervals";
        final long deckId = new Random().nextLong();
        final String model = "Music.intervals";
        final long modelId = new Random().nextLong();

        LinkedList<Map<String, String>> existingNotesData = new LinkedList<>();

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class, new ThrowsExceptionClass(IllegalArgumentException.class));
        doReturn(modelId).when(helper).findModelIdByName(model);
        doReturn(deckId).when(helper).findDeckIdByName(deck);
        doReturn(existingNotesData).when(helper).getNotes(modelId);

        MusInterval mi = new MusInterval(helper, "", "C#3", MusInterval.Fields.AscDesc.ASC, model, deck);
        assertFalse(mi.existsInAnki());
    }

    @Test
    public void checkExistence_NoSuchStartingNote() {
        final String deck = "Music intervals";
        final long deckId = new Random().nextLong();
        final String model = "Music.intervals";
        final long modelId = new Random().nextLong();

        Map<String, String> item1 = new HashMap<>();
        item1.put(MusInterval.Fields.START_NOTE, "C1");
        item1.put(MusInterval.Fields.ASC_DESC, MusInterval.Fields.AscDesc.ASC);
        Map<String, String> item2 = new HashMap<>();
        item2.put(MusInterval.Fields.START_NOTE, "C2");
        item2.put(MusInterval.Fields.ASC_DESC, MusInterval.Fields.AscDesc.DESC);

        LinkedList<Map<String, String>> existingNotesData = new LinkedList<>();
        existingNotesData.add(item1);
        existingNotesData.add(item2);

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class, new ThrowsExceptionClass(IllegalArgumentException.class));
        doReturn(modelId).when(helper).findModelIdByName(model);
        doReturn(deckId).when(helper).findDeckIdByName(deck);
        doReturn(existingNotesData).when(helper).getNotes(modelId);

        MusInterval mi = new MusInterval(helper, "", "C#3", MusInterval.Fields.AscDesc.ASC, model, deck);
        assertFalse(mi.existsInAnki());
    }

    @Test
    public void checkExistence_StartingNoteExists() {
        final String deck = "Music intervals";
        final long deckId = new Random().nextLong();
        final String model = "Music.intervals";
        final long modelId = new Random().nextLong();
        final String startNote = "C#3";

        LinkedList<Map<String, String>> existingNotesData = new LinkedList<>();
        Map<String, String> item1 = new HashMap<>();
        item1.put(MusInterval.Fields.START_NOTE, startNote);
        item1.put(MusInterval.Fields.ASC_DESC, MusInterval.Fields.AscDesc.ASC);
        existingNotesData.add(item1);
        Map<String, String> item2 = new HashMap<>();
        item2.put(MusInterval.Fields.START_NOTE, "C2");
        item2.put(MusInterval.Fields.ASC_DESC, MusInterval.Fields.AscDesc.DESC);
        existingNotesData.add(item2);

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class, new ThrowsExceptionClass(IllegalArgumentException.class));
        doReturn(modelId).when(helper).findModelIdByName(model);
        doReturn(deckId).when(helper).findDeckIdByName(deck);
        doReturn(existingNotesData).when(helper).getNotes(modelId);

        MusInterval mi = new MusInterval(helper, "", startNote, MusInterval.Fields.AscDesc.ASC, model, deck);
        assertTrue(mi.existsInAnki());
    }

    @Test
    public void checkExistence_StartingNoteExistsRegardlessOfSound() {
        final String deck = "Music intervals";
        final long deckId = new Random().nextLong();
        final String model = "Music.intervals";
        final long modelId = new Random().nextLong();
        final String startNote = "C#3";

        LinkedList<Map<String, String>> existingNotesData = new LinkedList<>();
        Map<String, String> item1 = new HashMap<>();
        item1.put(MusInterval.Fields.START_NOTE, startNote);
        item1.put(MusInterval.Fields.SOUND, "/test1");
        item1.put(MusInterval.Fields.ASC_DESC, MusInterval.Fields.AscDesc.ASC);
        existingNotesData.add(item1);

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class, new ThrowsExceptionClass(IllegalArgumentException.class));
        doReturn(modelId).when(helper).findModelIdByName(model);
        doReturn(deckId).when(helper).findDeckIdByName(deck);
        doReturn(existingNotesData).when(helper).getNotes(modelId);

        MusInterval mi = new MusInterval(helper, "/test2", startNote, MusInterval.Fields.AscDesc.ASC, model, deck);
        assertTrue(mi.existsInAnki());
    }

    @Test(expected = MusInterval.NoSuchModelException.class)
    public void add_NoSuchModel() throws MusInterval.NoSuchModelException, MusInterval.CreateDeckException, MusInterval.AddToAnkiException {
        final String deck = "Music intervals";
        final long deckId = new Random().nextLong();
        final String model = "Music.intervals";

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class, new ThrowsExceptionClass(IllegalArgumentException.class));
        // can't create model for some reason
        doReturn(null).when(helper).findModelIdByName(model);
        // deck ok
        doReturn(deckId).when(helper).findDeckIdByName(deck);

        MusInterval mi = new MusInterval(helper, "", "C#3", MusInterval.Fields.AscDesc.ASC, model, deck);
        mi.addToAnki();
    }

    @Test(expected = MusInterval.CreateDeckException.class)
    public void add_NoSuchDeckCantCreate() throws MusInterval.NoSuchModelException, MusInterval.CreateDeckException, MusInterval.AddToAnkiException {
        final String deck = "Music intervals";
        final String model = "Music.intervals";
        final long modelId = new Random().nextLong();

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class, new ThrowsExceptionClass(IllegalArgumentException.class));
        // model ok
        doReturn(modelId).when(helper).findModelIdByName(model);
        // can't create deck for some reason
        doReturn(null).when(helper).findDeckIdByName(deck);
        doReturn(null).when(helper).addNewDeck(deck);

        MusInterval mi = new MusInterval(helper, "", "C#3", MusInterval.Fields.AscDesc.ASC, model, deck);
        mi.addToAnki();
    }

    @Test(expected = MusInterval.AddToAnkiException.class)
    public void add_NoSuchDeckNotCreated() throws MusInterval.NoSuchModelException, MusInterval.CreateDeckException, MusInterval.AddToAnkiException {
        final String deck = "Music intervals";
        final long deckId = new Random().nextLong();
        final String model = "Music.intervals";
        final long modelId = new Random().nextLong();

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class, new ThrowsExceptionClass(IllegalArgumentException.class));
        // model ok
        doReturn(modelId).when(helper).findModelIdByName(model);
        // create deck
        doReturn(null).when(helper).findDeckIdByName(deck);
        doReturn(deckId).when(helper).addNewDeck(deck);
        doNothing().when(helper).storeDeckReference(deck, deckId);

        // can't create note for some reason
        doReturn(null).when(helper).addNote(eq(modelId), eq(deckId), any(Map.class), nullable(Set.class));

        MusInterval mi = new MusInterval(helper, "", "C#3", MusInterval.Fields.AscDesc.ASC, model, deck);
        mi.addToAnki();
    }

    @Test
    public void add_NoSuchDeckCreated() throws MusInterval.NoSuchModelException, MusInterval.CreateDeckException, MusInterval.AddToAnkiException {
        final String deck = "Music intervals";
        final long deckId = new Random().nextLong();
        final String model = "Music.intervals";
        final long modelId = new Random().nextLong();
        final long noteId = new Random().nextLong();

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class, new ThrowsExceptionClass(IllegalArgumentException.class));
        // model ok
        doReturn(modelId).when(helper).findModelIdByName(model);
        // create deck
        doReturn(null).when(helper).findDeckIdByName(deck);
        doReturn(deckId).when(helper).addNewDeck(deck);
        doNothing().when(helper).storeDeckReference(deck, deckId);

        // successful note creation
        doReturn(noteId).when(helper).addNote(eq(modelId), eq(deckId), any(Map.class), nullable(Set.class));

        MusInterval mi = new MusInterval(helper, "", "C#3", MusInterval.Fields.AscDesc.ASC, model, deck);
        mi.addToAnki();
    }

    @Test
    public void add_ExistingModelAndDeckCreated() throws MusInterval.NoSuchModelException, MusInterval.CreateDeckException, MusInterval.AddToAnkiException {
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

        MusInterval mi = new MusInterval(helper, "", "C#3", MusInterval.Fields.AscDesc.ASC, model, deck);
        mi.addToAnki();
    }
}
