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

        MusInterval mi = new MusInterval(helper, "", "C#3", MusInterval.Fields.Direction.ASC,
                MusInterval.Fields.Scale.MELODIC, "min2", "80", "guitar", model, deck);

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

        MusInterval mi = new MusInterval(helper, "", "C#3", MusInterval.Fields.Direction.ASC,
                MusInterval.Fields.Scale.MELODIC, "min2", "80", "guitar", model, deck);

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
        Map<String, String> item2 = new HashMap<>();
        item2.put(MusInterval.Fields.START_NOTE, "C2");

        LinkedList<Map<String, String>> existingNotesData = new LinkedList<>();
        existingNotesData.add(item1);
        existingNotesData.add(item2);

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class, new ThrowsExceptionClass(IllegalArgumentException.class));
        doReturn(modelId).when(helper).findModelIdByName(model);
        doReturn(deckId).when(helper).findDeckIdByName(deck);
        doReturn(existingNotesData).when(helper).getNotes(modelId);

        MusInterval mi = new MusInterval(helper, "", "C#3", MusInterval.Fields.Direction.ASC,
                MusInterval.Fields.Scale.MELODIC, "min2", "80", "guitar", model, deck);

        assertFalse(mi.existsInAnki());
    }

    @Test
    public void checkExistence_StartingNoteExists() {
        final String deck = "Music intervals";
        final long deckId = new Random().nextLong();
        final String model = "Music.intervals";
        final long modelId = new Random().nextLong();

        final String startNote = "C#3";
        final String interval = "min2";
        final String tempo = "80";
        final String instrument = "guitar";

        LinkedList<Map<String, String>> existingNotesData = new LinkedList<>();
        Map<String, String> item1 = new HashMap<>();
        item1.put(MusInterval.Fields.START_NOTE, startNote);
        item1.put(MusInterval.Fields.DIRECTION, MusInterval.Fields.Direction.ASC);
        item1.put(MusInterval.Fields.SCALE, MusInterval.Fields.Scale.MELODIC);
        item1.put(MusInterval.Fields.INTERVAL, interval);
        item1.put(MusInterval.Fields.TEMPO, tempo);
        item1.put(MusInterval.Fields.INSTRUMENT, instrument);
        existingNotesData.add(item1);
        Map<String, String> item2 = new HashMap<>();
        item2.put(MusInterval.Fields.START_NOTE, "C2");
        existingNotesData.add(item2);

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class, new ThrowsExceptionClass(IllegalArgumentException.class));
        doReturn(modelId).when(helper).findModelIdByName(model);
        doReturn(deckId).when(helper).findDeckIdByName(deck);
        doReturn(existingNotesData).when(helper).getNotes(modelId);

        MusInterval mi = new MusInterval(helper, "", startNote, MusInterval.Fields.Direction.ASC,
                MusInterval.Fields.Scale.MELODIC, interval, tempo, instrument, model, deck);

        assertTrue(mi.existsInAnki());
    }

    @Test
    public void checkExistence_StartingNoteExistsWithDifferentOtherFields() {
        final String deck = "Music intervals";
        final long deckId = new Random().nextLong();
        final String model = "Music.intervals";
        final long modelId = new Random().nextLong();

        final String startNote = "C#3";
        final String interval = "min2";
        final String tempo = "80";
        final String instrument = "guitar";

        LinkedList<Map<String, String>> existingNotesData = new LinkedList<>();
        Map<String, String> item1 = new HashMap<>();
        item1.put(MusInterval.Fields.START_NOTE, startNote);                        // same StartingNote
        item1.put(MusInterval.Fields.DIRECTION, MusInterval.Fields.Direction.DESC); // another AscDesc
        item1.put(MusInterval.Fields.SCALE, MusInterval.Fields.Scale.MELODIC);      // same MelHar
        item1.put(MusInterval.Fields.INTERVAL, interval);                           // same interval
        item1.put(MusInterval.Fields.TEMPO, tempo);                                 // same tempo
        item1.put(MusInterval.Fields.INSTRUMENT, instrument);                       // same instrument
        existingNotesData.add(item1);
        Map<String, String> item2 = new HashMap<>();
        item2.put(MusInterval.Fields.START_NOTE, startNote);                        // same StartingNote
        item2.put(MusInterval.Fields.DIRECTION, MusInterval.Fields.Direction.ASC);  // same AscDesc
        item2.put(MusInterval.Fields.SCALE, MusInterval.Fields.Scale.HARMONIC);     // another MelHar
        item2.put(MusInterval.Fields.INTERVAL, interval);                           // same interval
        item2.put(MusInterval.Fields.TEMPO, tempo);                                 // same tempo
        item2.put(MusInterval.Fields.INSTRUMENT, instrument);                       // same instrument
        existingNotesData.add(item2);
        Map<String, String> item3 = new HashMap<>();
        item3.put(MusInterval.Fields.START_NOTE, startNote);                        // same StartingNote
        item3.put(MusInterval.Fields.DIRECTION, MusInterval.Fields.Direction.ASC);  // same AscDesc
        item3.put(MusInterval.Fields.SCALE, MusInterval.Fields.Scale.MELODIC);      // same MelHar
        item3.put(MusInterval.Fields.INTERVAL, "min3");                             // another interval
        item3.put(MusInterval.Fields.TEMPO, tempo);                                 // same tempo
        item3.put(MusInterval.Fields.INSTRUMENT, instrument);                       // same instrument
        existingNotesData.add(item3);
        Map<String, String> item4 = new HashMap<>();
        item4.put(MusInterval.Fields.START_NOTE, startNote);                        // same StartingNote
        item4.put(MusInterval.Fields.DIRECTION, MusInterval.Fields.Direction.ASC);  // same AscDesc
        item4.put(MusInterval.Fields.SCALE, MusInterval.Fields.Scale.MELODIC);      // same MelHar
        item4.put(MusInterval.Fields.INTERVAL, interval);                           // same interval
        item4.put(MusInterval.Fields.TEMPO, "90");                                  // another tempo
        item4.put(MusInterval.Fields.INSTRUMENT, instrument);                       // same instrument
        existingNotesData.add(item4);
        Map<String, String> item5 = new HashMap<>();
        item5.put(MusInterval.Fields.START_NOTE, startNote);                        // same StartingNote
        item5.put(MusInterval.Fields.DIRECTION, MusInterval.Fields.Direction.ASC);  // same AscDesc
        item5.put(MusInterval.Fields.SCALE, MusInterval.Fields.Scale.MELODIC);      // same MelHar
        item5.put(MusInterval.Fields.INTERVAL, interval);                           // same interval
        item5.put(MusInterval.Fields.TEMPO, tempo);                                 // same tempo
        item5.put(MusInterval.Fields.INSTRUMENT, "violin");                         // another instrument
        existingNotesData.add(item5);

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class, new ThrowsExceptionClass(IllegalArgumentException.class));
        doReturn(modelId).when(helper).findModelIdByName(model);
        doReturn(deckId).when(helper).findDeckIdByName(deck);
        doReturn(existingNotesData).when(helper).getNotes(modelId);

        MusInterval mi = new MusInterval(helper, "", startNote, MusInterval.Fields.Direction.ASC,
                MusInterval.Fields.Scale.MELODIC, interval, tempo, instrument, model, deck);

        assertFalse(mi.existsInAnki());
    }

    @Test
    public void checkExistence_StartingNoteExistsRegardlessOfSound() {
        final String deck = "Music intervals";
        final long deckId = new Random().nextLong();
        final String model = "Music.intervals";
        final long modelId = new Random().nextLong();

        final String startNote = "C#3";
        final String interval = "min2";
        final String tempo = "80";
        final String instrument = "guitar";

        LinkedList<Map<String, String>> existingNotesData = new LinkedList<>();
        Map<String, String> item1 = new HashMap<>();
        item1.put(MusInterval.Fields.START_NOTE, startNote);
        item1.put(MusInterval.Fields.SOUND, "/test1");  // sound field does not matter
        item1.put(MusInterval.Fields.DIRECTION, MusInterval.Fields.Direction.ASC);
        item1.put(MusInterval.Fields.SCALE, MusInterval.Fields.Scale.MELODIC);
        item1.put(MusInterval.Fields.INTERVAL, interval);
        item1.put(MusInterval.Fields.TEMPO, tempo);
        item1.put(MusInterval.Fields.INSTRUMENT, instrument);
        existingNotesData.add(item1);

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class, new ThrowsExceptionClass(IllegalArgumentException.class));
        doReturn(modelId).when(helper).findModelIdByName(model);
        doReturn(deckId).when(helper).findDeckIdByName(deck);
        doReturn(existingNotesData).when(helper).getNotes(modelId);

        MusInterval mi = new MusInterval(helper, "/test2", startNote, MusInterval.Fields.Direction.ASC,
                MusInterval.Fields.Scale.MELODIC, interval, tempo, instrument, model, deck);

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

        MusInterval mi = new MusInterval(helper, "", "C#3", MusInterval.Fields.Direction.ASC,
                MusInterval.Fields.Scale.MELODIC, "min2", "80", "guitar", model, deck);

        mi.addToAnki(); // should not throw any exception
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

        MusInterval mi = new MusInterval(helper, "", "C#3", MusInterval.Fields.Direction.ASC,
                MusInterval.Fields.Scale.MELODIC, "min2", "80", "guitar", model, deck);

        mi.addToAnki(); // should not throw any exception
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

        MusInterval mi = new MusInterval(helper, "", "C#3", MusInterval.Fields.Direction.ASC,
                MusInterval.Fields.Scale.MELODIC, "min2", "80", "guitar", model, deck);

        mi.addToAnki(); // should not throw any exception
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

        MusInterval mi = new MusInterval(helper, "", "C#3", MusInterval.Fields.Direction.ASC,
                MusInterval.Fields.Scale.MELODIC, "min2", "80", "guitar", model, deck);

        mi.addToAnki(); // should not throw any exception
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

        MusInterval mi = new MusInterval(helper, "", "C#3", MusInterval.Fields.Direction.ASC,
                MusInterval.Fields.Scale.MELODIC, "min2", "80", "guitar", model, deck);

        mi.addToAnki(); // should not throw any exception
    }
}
