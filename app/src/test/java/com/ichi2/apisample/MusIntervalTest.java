package com.ichi2.apisample;

import com.ichi2.apisample.helper.AnkiDroidHelper;
import com.ichi2.apisample.model.AddingHandler;
import com.ichi2.apisample.model.AddingPrompter;
import com.ichi2.apisample.model.MusInterval;
import com.ichi2.apisample.model.NotesIntegrity;
import com.ichi2.apisample.model.ProgressIndicator;
import com.ichi2.apisample.model.RelatedIntervalSoundField;
import com.ichi2.apisample.validation.EmptyValidator;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


public class MusIntervalTest {

    final static String defaultDeckName = "Music intervals";
    final static String defaultModelName = "Music.interval";

    final static String[] ALL_NOTES = new String[]{
            "C", "C#",
            "D", "D#",
            "E",
            "F", "F#",
            "G", "G#",
            "A", "A#",
            "B",
    };
    final static String[] ALL_OCTAVES = new String[]{"1", "2", "3", "4", "5", "6"};

    final static String defaultNote = ALL_NOTES[1]; // C#
    final static String defaultOctave = ALL_OCTAVES[2]; // 3
    final static String defaultStartNote = defaultNote + defaultOctave; // C#3
    final static String note2 = ALL_NOTES[1]; // C#
    final static String octave2 = ALL_OCTAVES[1]; // 2
    final static String startNote2 = note2 + octave2; // C#2
    final static String intervalMin3 = MusInterval.Fields.Interval.VALUES[3]; //m3
    final static String[] SIGNATURE = MusInterval.Fields.getSignature(false);
    final static String corruptedTag = "corrupted";
    final static String suspiciousTag = "suspicious";
    final static String duplicateTag = "duplicate";

    @Test
    @SuppressWarnings("unchecked")
    public void checkExistence_NoSuchStartingNote() throws AnkiDroidHelper.InvalidAnkiDatabaseException, MusInterval.Exception {
        final long deckId = new Random().nextLong();
        final long modelId = new Random().nextLong();

        LinkedList<Map<String, String>> existingNotesData = new LinkedList<>();

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class);
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(SIGNATURE).when(helper).getFieldList(eq(modelId));
        doReturn(deckId).when(helper).findDeckIdByName(defaultDeckName);
        doReturn(existingNotesData).when(helper).findNotes(eq(modelId), any(ArrayList.class), any(Map.class), any(Map.class), any(Map.class));

        MusInterval mi = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .notes(new String[]{defaultNote})
                .octaves(new String[]{defaultOctave})
                .intervals(new String[]{intervalMin3})
                .build();

        assertFalse(mi.existsInAnki());
        assertEquals(0, mi.getExistingNotesCount());
        assertEquals(0, mi.getExistingMarkedNotesCount());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void checkExistence_StartingNoteExists() throws AnkiDroidHelper.InvalidAnkiDatabaseException, MusInterval.Exception {
        final long deckId = new Random().nextLong();
        final long modelId = new Random().nextLong();

        final String interval = "m2";
        final String tempo = "80";
        final String instrument = "guitar";

        LinkedList<Map<String, String>> existingNotesData = new LinkedList<>();
        Map<String, String> item1 = new HashMap<>();
        item1.put(MusInterval.Fields.START_NOTE, defaultStartNote);
        item1.put(MusInterval.Fields.DIRECTION, MusInterval.Fields.Direction.ASC);
        item1.put(MusInterval.Fields.TIMING, MusInterval.Fields.Timing.MELODIC);
        item1.put(MusInterval.Fields.INTERVAL, interval);
        item1.put(MusInterval.Fields.TEMPO, tempo);
        item1.put(MusInterval.Fields.INSTRUMENT, instrument);
        existingNotesData.add(item1);

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class);
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(SIGNATURE).when(helper).getFieldList(eq(modelId));
        doReturn(deckId).when(helper).findDeckIdByName(defaultDeckName);
        doReturn(existingNotesData).when(helper).findNotes(eq(modelId), any(ArrayList.class), any(Map.class), any(Map.class), any(Map.class));

        MusInterval mi = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .notes(new String[]{defaultNote})
                .octaves(new String[]{defaultOctave})
                .direction(MusInterval.Fields.Direction.ASC)
                .timing(MusInterval.Fields.Timing.MELODIC)
                .intervals(new String[]{interval})
                .tempo(tempo)
                .instrument(instrument)
                .build();

        assertTrue(mi.existsInAnki());
        assertEquals(1, mi.getExistingNotesCount());
        assertEquals(0, mi.getExistingMarkedNotesCount());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void checkExistence_StartingNoteExistsAlreadyMarked() throws AnkiDroidHelper.InvalidAnkiDatabaseException, MusInterval.Exception {
        final long deckId = new Random().nextLong();
        final long modelId = new Random().nextLong();

        final String interval = "m2";
        final String tempo = "80";
        final String instrument = "guitar";

        LinkedList<Map<String, String>> existingNotesData = new LinkedList<>();
        Map<String, String> item1 = new HashMap<>();
        item1.put(MusInterval.Fields.START_NOTE, defaultStartNote);
        item1.put(MusInterval.Fields.DIRECTION, MusInterval.Fields.Direction.ASC);
        item1.put(MusInterval.Fields.TIMING, MusInterval.Fields.Timing.MELODIC);
        item1.put(MusInterval.Fields.INTERVAL, interval);
        item1.put(MusInterval.Fields.TEMPO, tempo);
        item1.put(MusInterval.Fields.INSTRUMENT, instrument);
        item1.put("tags", " marked ");
        existingNotesData.add(item1);

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class);
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(SIGNATURE).when(helper).getFieldList(eq(modelId));
        doReturn(deckId).when(helper).findDeckIdByName(defaultDeckName);
        doReturn(existingNotesData).when(helper).findNotes(eq(modelId), any(ArrayList.class), any(Map.class), any(Map.class), any(Map.class));

        MusInterval mi = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .notes(new String[]{defaultNote})
                .octaves(new String[]{defaultOctave})
                .direction(MusInterval.Fields.Direction.ASC)
                .timing(MusInterval.Fields.Timing.MELODIC)
                .intervals(new String[]{interval})
                .tempo(tempo)
                .instrument(instrument)
                .build();

        assertTrue(mi.existsInAnki());
        assertEquals(1, mi.getExistingNotesCount());
        assertEquals(1, mi.getExistingMarkedNotesCount());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void checkExistence_StartingNoteExistsIgnoreSpaces() throws AnkiDroidHelper.InvalidAnkiDatabaseException, MusInterval.Exception {
        final long deckId = new Random().nextLong();
        final long modelId = new Random().nextLong();

        final String interval = "m2";
        final String tempo = "80";
        final String instrument = "guitar";

        LinkedList<Map<String, String>> existingNotesData = new LinkedList<>();
        Map<String, String> item1 = new HashMap<>();
        item1.put(MusInterval.Fields.START_NOTE, defaultStartNote);
        item1.put(MusInterval.Fields.DIRECTION, MusInterval.Fields.Direction.ASC);
        item1.put(MusInterval.Fields.TIMING, MusInterval.Fields.Timing.MELODIC);
        item1.put(MusInterval.Fields.INTERVAL, interval);
        item1.put(MusInterval.Fields.TEMPO, tempo);
        item1.put(MusInterval.Fields.INSTRUMENT, instrument);
        existingNotesData.add(item1);

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class);
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(SIGNATURE).when(helper).getFieldList(eq(modelId));
        doReturn(deckId).when(helper).findDeckIdByName(defaultDeckName);
        doReturn(existingNotesData).when(helper).findNotes(eq(modelId), any(ArrayList.class), any(Map.class), any(Map.class), any(Map.class));

        MusInterval mi = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .notes(new String[]{defaultNote})
                .octaves(new String[]{defaultOctave})
                .direction(MusInterval.Fields.Direction.ASC)
                .timing(MusInterval.Fields.Timing.MELODIC)
                .intervals(new String[]{interval})
                .tempo("  ") // should be trimmed
                .instrument(" " + instrument + " ") // should be trimmed
                .build();

        assertTrue(mi.existsInAnki());
        assertEquals(1, mi.getExistingNotesCount());
        assertEquals(0, mi.getExistingMarkedNotesCount());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void checkExistence_StartingNoteExistsWithDifferentOtherFields() throws AnkiDroidHelper.InvalidAnkiDatabaseException, MusInterval.Exception {
        final long deckId = new Random().nextLong();
        final long modelId = new Random().nextLong();

        final String interval = "m2";
        final String tempo = "80";
        final String instrument = "guitar";

        LinkedList<Map<String, String>> existingNotesData = new LinkedList<>();

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class);
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(SIGNATURE).when(helper).getFieldList(eq(modelId));
        doReturn(deckId).when(helper).findDeckIdByName(defaultDeckName);
        doReturn(existingNotesData).when(helper).findNotes(eq(modelId), any(ArrayList.class), any(Map.class), any(Map.class), any(Map.class));

        MusInterval mi = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .notes(new String[]{defaultNote})
                .octaves(new String[]{defaultOctave})
                .direction(MusInterval.Fields.Direction.ASC)
                .timing(MusInterval.Fields.Timing.MELODIC)
                .intervals(new String[]{interval})
                .tempo(tempo)
                .instrument(instrument)
                .build();

        assertFalse(mi.existsInAnki());
        assertEquals(0, mi.getExistingNotesCount());
        assertEquals(0, mi.getExistingMarkedNotesCount());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void checkExistence_StartingNoteExistsRegardlessOfSound() throws AnkiDroidHelper.InvalidAnkiDatabaseException, MusInterval.Exception {
        final long deckId = new Random().nextLong();
        final long modelId = new Random().nextLong();

        final String interval = "m2";
        final String tempo = "80";
        final String instrument = "guitar";

        LinkedList<Map<String, String>> existingNotesData = new LinkedList<>();
        Map<String, String> item1 = new HashMap<>();
        item1.put(MusInterval.Fields.START_NOTE, defaultStartNote);
        item1.put(MusInterval.Fields.SOUND, "/test1");  // sound field does not matter
        item1.put(MusInterval.Fields.DIRECTION, MusInterval.Fields.Direction.ASC);
        item1.put(MusInterval.Fields.TIMING, MusInterval.Fields.Timing.MELODIC);
        item1.put(MusInterval.Fields.INTERVAL, interval);
        item1.put(MusInterval.Fields.TEMPO, tempo);
        item1.put(MusInterval.Fields.INSTRUMENT, instrument);
        existingNotesData.add(item1);

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class);
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(SIGNATURE).when(helper).getFieldList(eq(modelId));
        doReturn(deckId).when(helper).findDeckIdByName(defaultDeckName);
        doReturn(existingNotesData).when(helper).findNotes(eq(modelId), any(ArrayList.class), any(Map.class), any(Map.class), any(Map.class));

        MusInterval mi = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .sounds(new String[]{"/test2"})
                .notes(new String[]{defaultNote})
                .octaves(new String[]{defaultOctave})
                .direction(MusInterval.Fields.Direction.ASC)
                .timing(MusInterval.Fields.Timing.MELODIC)
                .intervals(new String[]{interval})
                .tempo(tempo)
                .instrument(instrument)
                .build();

        assertTrue(mi.existsInAnki());
        assertEquals(1, mi.getExistingNotesCount());
        assertEquals(0, mi.getExistingMarkedNotesCount());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void checkExistence_EmptyModel_shouldFail() throws AnkiDroidHelper.InvalidAnkiDatabaseException, MusInterval.Exception {
        final long deckId = new Random().nextLong();
        final long modelId = new Random().nextLong();

        LinkedList<Map<String, String>> existingNotesData = new LinkedList<>(); // no notes at all

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class);
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(SIGNATURE).when(helper).getFieldList(eq(modelId));
        doReturn(deckId).when(helper).findDeckIdByName(defaultDeckName);
        doReturn(existingNotesData).when(helper).findNotes(eq(modelId), any(ArrayList.class), any(Map.class), any(Map.class), any(Map.class));

        MusInterval mi = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .notes(ALL_NOTES)
                .octaves(ALL_OCTAVES)
                .intervals(MusInterval.Fields.Interval.VALUES)
                .build();

        assertFalse(mi.existsInAnki());
        assertEquals(0, mi.getExistingNotesCount());
        assertEquals(0, mi.getExistingMarkedNotesCount());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void checkExistence_NonEmptyModel_shouldSucceed() throws AnkiDroidHelper.InvalidAnkiDatabaseException, MusInterval.Exception {
        final long deckId = new Random().nextLong();
        final long modelId = new Random().nextLong();

        LinkedList<Map<String, String>> existingNotesData = new LinkedList<>(); // at least one note
        Map<String, String> item1 = new HashMap<>();
        item1.put(MusInterval.Fields.START_NOTE, defaultStartNote);
        existingNotesData.add(item1);

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class);
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(SIGNATURE).when(helper).getFieldList(eq(modelId));
        doReturn(deckId).when(helper).findDeckIdByName(defaultDeckName);
        doReturn(existingNotesData).when(helper).findNotes(eq(modelId), any(ArrayList.class), any(Map.class), any(Map.class), any(Map.class));

        MusInterval mi = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .notes(ALL_NOTES)
                .octaves(ALL_OCTAVES)
                .intervals(MusInterval.Fields.Interval.VALUES)
                .build();

        assertTrue(mi.existsInAnki());
        assertEquals(1, mi.getExistingNotesCount());
        assertEquals(0, mi.getExistingMarkedNotesCount());
    }

    @Test(expected = MusInterval.CreateDeckException.class)
    public void add_NoSuchDeckCantCreate() throws MusInterval.Exception, AnkiDroidHelper.InvalidAnkiDatabaseException {
        final long modelId = new Random().nextLong();

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class);
        // model ok
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(SIGNATURE).when(helper).getFieldList(eq(modelId));
        // can't create deck for some reason
        doReturn(null).when(helper).findDeckIdByName(defaultDeckName);
        doReturn(null).when(helper).addNewDeck(defaultDeckName);

        MusInterval mi = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .notes(ALL_NOTES)
                .octaves(ALL_OCTAVES)
                .intervals(MusInterval.Fields.Interval.VALUES)
                .build();

        mi.addToAnki(null, null);
    }

    static class WasCalledAnswer implements Answer {
        public boolean wasCalled;

        @Override
        public Object answer(InvocationOnMock invocation) {
            wasCalled = true;
            return null;
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void add_NoSuchDeck_CardShouldNotBeCreated() throws MusInterval.Exception, AnkiDroidHelper.InvalidAnkiDatabaseException {
        final long deckId = new Random().nextLong();
        final long modelId = new Random().nextLong();

        final String sound = "/path/to/file.m4a";
        final String newSound = "music_interval_12345.m4a";
        final String direction = MusInterval.Fields.Direction.ASC;
        final String timing = MusInterval.Fields.Timing.MELODIC;
        final String interval = "m2";
        final String tempo = "80";
        final String instrument = "guitar";

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class);
        // model ok
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(SIGNATURE).when(helper).getFieldList(eq(modelId));
        // create deck
        doReturn(null).when(helper).findDeckIdByName(defaultDeckName);
        doReturn(deckId).when(helper).addNewDeck(defaultDeckName);
        doNothing().when(helper).storeDeckReference(defaultDeckName, deckId);
        doReturn(new LinkedList<Map<String, String>>()).when(helper).findNotes(eq(modelId), any(ArrayList.class), any(Map.class), any(Map.class), any(Map.class));

        doReturn(newSound).when(helper).addFileToAnkiMedia(sound);
        doReturn(new LinkedList<Map<String, String>>()).when(helper).findNotes(eq(modelId), any(Map.class), any(Map.class), any(Map.class), any(Map.class));
        doAnswer(new Answer<Long>() {
            @Override
            public Long answer(InvocationOnMock invocation) {
                // Check passed arguments
                Map<String, String> data = invocation.getArgument(2);
                assertTrue(data.containsKey(MusInterval.Fields.SOUND));
                assertEquals("[sound:" + newSound + "]", data.get(MusInterval.Fields.SOUND));
                assertTrue(data.containsKey(MusInterval.Fields.START_NOTE));
                assertEquals(defaultStartNote, data.get(MusInterval.Fields.START_NOTE));
                assertTrue(data.containsKey(MusInterval.Fields.DIRECTION));
                assertEquals(direction, data.get(MusInterval.Fields.DIRECTION));
                assertTrue(data.containsKey(MusInterval.Fields.TIMING));
                assertEquals(timing, data.get(MusInterval.Fields.TIMING));
                assertTrue(data.containsKey(MusInterval.Fields.INTERVAL));
                assertEquals(interval, data.get(MusInterval.Fields.INTERVAL));
                assertTrue(data.containsKey(MusInterval.Fields.TEMPO));
                assertEquals(tempo, data.get(MusInterval.Fields.TEMPO));
                assertTrue(data.containsKey(MusInterval.Fields.INSTRUMENT));
                assertEquals(instrument, data.get(MusInterval.Fields.INSTRUMENT));

                Set<String> tags = invocation.getArgument(3);
                assertNull(tags);

                // can't create note for some reason
                return null;
            }

        }).when(helper).addNote(eq(modelId), eq(deckId), any(Map.class), nullable(Set.class));

        MusInterval mi = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .sounds(new String[]{sound})
                .notes(new String[]{defaultNote})
                .octaves(new String[]{defaultOctave})
                .direction(direction)
                .timing(timing)
                .intervals(new String[]{interval})
                .tempo(tempo)
                .instrument(instrument)
                .build();

        AddingPrompter prompter = mock(AddingPrompter.class);
        WasCalledAnswer answer = new WasCalledAnswer();
        doAnswer(answer).when(prompter).processException(any(MusInterval.AddToAnkiException.class));
        ProgressIndicator indicator = mock(ProgressIndicator.class);

        mi.addToAnki(prompter, indicator);

        assertTrue(answer.wasCalled);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void add_NoSuchDeck_DeckShouldBeCreated() throws MusInterval.Exception, AnkiDroidHelper.InvalidAnkiDatabaseException {
        final long deckId = new Random().nextLong();
        final long modelId = new Random().nextLong();
        final long noteId = new Random().nextLong();

        final String sound = "/path/to/file.m4a";
        final String newSound = "music_interval_12345.m4a";
        final String direction = MusInterval.Fields.Direction.ASC;
        final String timing = MusInterval.Fields.Timing.MELODIC;
        final String interval = "m2";
        final String tempo = "80";
        final String instrument = "guitar";

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class);
        // model ok
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(SIGNATURE).when(helper).getFieldList(eq(modelId));
        // create deck
        doReturn(null).when(helper).findDeckIdByName(defaultDeckName);
        doReturn(deckId).when(helper).addNewDeck(defaultDeckName);
        doNothing().when(helper).storeDeckReference(defaultDeckName, deckId);

        doReturn(newSound).when(helper).addFileToAnkiMedia(sound);

        doAnswer(new Answer<Long>() {
            @Override
            public Long answer(InvocationOnMock invocation) {
                // Check passed arguments
                Map<String, String> data = invocation.getArgument(2);
                assertTrue(data.containsKey(MusInterval.Fields.SOUND));
                assertEquals("[sound:" + newSound + "]", data.get(MusInterval.Fields.SOUND));
                assertTrue(data.containsKey(MusInterval.Fields.START_NOTE));
                assertEquals(defaultStartNote, data.get(MusInterval.Fields.START_NOTE));
                assertTrue(data.containsKey(MusInterval.Fields.DIRECTION));
                assertEquals(direction, data.get(MusInterval.Fields.DIRECTION));
                assertTrue(data.containsKey(MusInterval.Fields.TIMING));
                assertEquals(timing, data.get(MusInterval.Fields.TIMING));
                assertTrue(data.containsKey(MusInterval.Fields.INTERVAL));
                assertEquals(interval, data.get(MusInterval.Fields.INTERVAL));
                assertTrue(data.containsKey(MusInterval.Fields.TEMPO));
                assertEquals(tempo, data.get(MusInterval.Fields.TEMPO));
                assertTrue(data.containsKey(MusInterval.Fields.INSTRUMENT));
                assertEquals(instrument, data.get(MusInterval.Fields.INSTRUMENT));

                Set<String> tags = invocation.getArgument(3);
                assertNull(tags);

                // successful note creation
                return noteId;
            }

        }).when(helper).addNote(eq(modelId), eq(deckId), any(Map.class), nullable(Set.class));

        MusInterval mi = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .sounds(new String[]{sound})
                .notes(new String[]{defaultNote})
                .octaves(new String[]{defaultOctave})
                .direction(direction)
                .timing(timing)
                .intervals(new String[]{interval})
                .tempo(tempo)
                .instrument(instrument)
                .build();

        AddingPrompter prompter = mock(AddingPrompter.class);
        ProgressIndicator indicator = mock(ProgressIndicator.class);

        mi.addToAnki(prompter, indicator); // should not throw any exception
    }

    @Test
    @SuppressWarnings("unchecked")
    public void add_AllFieldsAreSet_NoteShouldBeCreated() throws MusInterval.Exception, AnkiDroidHelper.InvalidAnkiDatabaseException {
        final long deckId = new Random().nextLong();
        final long modelId = new Random().nextLong();
        final long noteId = new Random().nextLong();

        final String sound = "/path/to/file.m4a";
        final String newSound = "music_interval_12345.m4a";
        final String direction = MusInterval.Fields.Direction.ASC;
        final String timing = MusInterval.Fields.Timing.MELODIC;
        final String interval = "m3";
        final String tempo = "90";
        final String instrument = "violin";

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class);
        // existing model
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(SIGNATURE).when(helper).getFieldList(eq(modelId));
        // existing deck
        doReturn(deckId).when(helper).findDeckIdByName(defaultDeckName);

        doReturn(newSound).when(helper).addFileToAnkiMedia(sound);

        doAnswer(new Answer<Long>() {
            @Override
            public Long answer(InvocationOnMock invocation) {
                // Check passed arguments
                Map<String, String> data = invocation.getArgument(2);
                assertTrue(data.containsKey(MusInterval.Fields.SOUND));
                assertEquals("[sound:" + newSound + "]", data.get(MusInterval.Fields.SOUND));
                assertTrue(data.containsKey(MusInterval.Fields.START_NOTE));
                assertEquals(startNote2, data.get(MusInterval.Fields.START_NOTE));
                assertTrue(data.containsKey(MusInterval.Fields.DIRECTION));
                assertEquals(direction, data.get(MusInterval.Fields.DIRECTION));
                assertTrue(data.containsKey(MusInterval.Fields.TIMING));
                assertEquals(timing, data.get(MusInterval.Fields.TIMING));
                assertTrue(data.containsKey(MusInterval.Fields.INTERVAL));
                assertEquals(interval, data.get(MusInterval.Fields.INTERVAL));
                assertTrue(data.containsKey(MusInterval.Fields.TEMPO));
                assertEquals(tempo, data.get(MusInterval.Fields.TEMPO));
                assertTrue(data.containsKey(MusInterval.Fields.INSTRUMENT));
                assertEquals(instrument, data.get(MusInterval.Fields.INSTRUMENT));

                Set<String> tags = invocation.getArgument(3);
                assertNull(tags);

                // successful note creation
                return noteId;
            }

        }).when(helper).addNote(eq(modelId), eq(deckId), any(Map.class), nullable(Set.class));

        MusInterval mi = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .sounds(new String[]{sound})
                .notes(new String[]{note2})
                .octaves(new String[]{octave2})
                .direction(direction)
                .timing(timing)
                .intervals(new String[]{interval})
                .tempo(tempo)
                .instrument(instrument)
                .build();

        AddingPrompter prompter = mock(AddingPrompter.class);
        ProgressIndicator indicator = mock(ProgressIndicator.class);

        mi.addToAnki(prompter, indicator); // should not throw any exception
    }

    @Test
    @SuppressWarnings("unchecked")
    public void add_AllFieldsAreSet_NewMusicIntervalShouldBeCreated() throws MusInterval.Exception, AnkiDroidHelper.InvalidAnkiDatabaseException {
        final long deckId = new Random().nextLong();
        final long modelId = new Random().nextLong();
        final long noteId = new Random().nextLong();

        final String sound = "/path/to/file.m4a";
        final String newSound = "music_interval_12345.m4a";
        final String direction = MusInterval.Fields.Direction.ASC;
        final String timing = MusInterval.Fields.Timing.MELODIC;
        final String interval = "m3";
        final String tempo = "90";
        final String instrument = "violin";

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class);
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(SIGNATURE).when(helper).getFieldList(eq(modelId));
        doReturn(deckId).when(helper).findDeckIdByName(defaultDeckName);
        doReturn(newSound).when(helper).addFileToAnkiMedia(sound);
        doReturn(noteId).when(helper).addNote(eq(modelId), eq(deckId), any(Map.class), nullable(Set.class));

        MusInterval mi = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .sounds(new String[]{sound})
                .notes(new String[]{note2})
                .octaves(new String[]{octave2})
                .direction(direction)
                .timing(timing)
                .intervals(new String[]{interval})
                .tempo(tempo)
                .instrument(instrument)
                .build();

        final LinkedList<MusInterval> addedMusIntervals = new LinkedList<>();

        AddingPrompter prompter = mock(AddingPrompter.class);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                MusInterval.AddingResult addingResult = invocation.getArgument(0);
                addedMusIntervals.add((addingResult.getMusInterval()));
                return null;
            }
        }).when(prompter).addingFinished(any(MusInterval.AddingResult.class));

        ProgressIndicator indicator = mock(ProgressIndicator.class);

        mi.addToAnki(prompter, indicator); // should not throw any exception

        MusInterval mi2 = addedMusIntervals.getFirst();

        // everything should be the same, except "sound" field
        assertFalse(Arrays.equals(mi.sounds, mi2.sounds));
        assertArrayEquals(mi.notes, mi2.notes);
        assertArrayEquals(mi.octaves, mi2.octaves);
        assertEquals(mi.direction, mi2.direction);
        assertEquals(mi.timing, mi2.timing);
        assertArrayEquals(mi.intervals, mi2.intervals);
        assertEquals(mi.tempo, mi2.tempo);
        assertEquals(mi.instrument, mi2.instrument);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void add_AllFieldsAreSet_SoundFieldShouldBeProper() throws MusInterval.Exception, AnkiDroidHelper.InvalidAnkiDatabaseException {
        final long deckId = new Random().nextLong();
        final long modelId = new Random().nextLong();
        final long noteId = new Random().nextLong();

        final String sound = "/path/to/file.m4a";
        final String newSound = "music_interval_12345.m4a";

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class);
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(SIGNATURE).when(helper).getFieldList(eq(modelId));
        doReturn(deckId).when(helper).findDeckIdByName(defaultDeckName);
        doReturn(newSound).when(helper).addFileToAnkiMedia(sound);
        doReturn(noteId).when(helper).addNote(eq(modelId), eq(deckId), any(Map.class), nullable(Set.class));

        MusInterval mi = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .sounds(new String[]{sound})
                .notes(new String[]{note2})
                .octaves(new String[]{octave2})
                .direction(MusInterval.Fields.Direction.ASC)
                .timing(MusInterval.Fields.Timing.MELODIC)
                .intervals(new String[]{intervalMin3})
                .tempo("90")
                .instrument("violin")
                .build();

        final LinkedList<MusInterval> addedMusIntervals = new LinkedList<>();

        AddingPrompter prompter = mock(AddingPrompter.class);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                MusInterval.AddingResult addingResult = invocation.getArgument(0);
                addedMusIntervals.add((addingResult.getMusInterval()));
                return null;
            }
        }).when(prompter).addingFinished(any(MusInterval.AddingResult.class));

        ProgressIndicator indicator = mock(ProgressIndicator.class);

        mi.addToAnki(prompter, indicator); // should not throw any exception

        MusInterval mi2 = addedMusIntervals.getFirst();

        assertNotEquals(0, mi2.sounds.length);
        assertFalse(Arrays.equals(new String[]{sound}, mi2.sounds));
        String addedSound = mi2.sounds[0];
        assertTrue(addedSound.startsWith("[sound:"));
        assertTrue(addedSound.endsWith(".m4a]"));
        assertEquals("[sound:" + newSound + "]", addedSound);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void add_AllFieldsAreSet_SoundFieldShouldBeProper2() throws MusInterval.Exception, AnkiDroidHelper.InvalidAnkiDatabaseException {
        final long deckId = new Random().nextLong();
        final long modelId = new Random().nextLong();
        final long noteId = new Random().nextLong();

        final String sound = "/path/to/file.mp3";
        final String newSound = "music_interval_12345.mp3";

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class);
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(SIGNATURE).when(helper).getFieldList(eq(modelId));
        doReturn(deckId).when(helper).findDeckIdByName(defaultDeckName);
        doReturn(newSound).when(helper).addFileToAnkiMedia(sound);
        doReturn(noteId).when(helper).addNote(eq(modelId), eq(deckId), any(Map.class), nullable(Set.class));

        MusInterval mi = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .sounds(new String[]{sound})
                .notes(new String[]{note2})
                .octaves(new String[]{octave2})
                .direction(MusInterval.Fields.Direction.ASC)
                .timing(MusInterval.Fields.Timing.MELODIC)
                .intervals(new String[]{intervalMin3})
                .tempo("90")
                .instrument("violin")
                .build();

        final LinkedList<MusInterval> addedMusIntervals = new LinkedList<>();

        AddingPrompter prompter = mock(AddingPrompter.class);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                MusInterval.AddingResult addingResult = invocation.getArgument(0);
                addedMusIntervals.add(addingResult.getMusInterval());
                return null;
            }
        }).when(prompter).addingFinished(any(MusInterval.AddingResult.class));

        ProgressIndicator indicator = mock(ProgressIndicator.class);

        mi.addToAnki(prompter, indicator); // should not throw any exception

        MusInterval mi2 = addedMusIntervals.getFirst();

        assertNotEquals(0, mi2.sounds.length);
        assertFalse(Arrays.equals(new String[]{sound}, mi2.sounds));
        String addedSound = mi2.sounds[0];
        assertTrue(addedSound.startsWith("[sound:"));
        assertTrue(addedSound.endsWith(".mp3]"));
        assertEquals("[sound:" + newSound + "]", addedSound);
    }

    @Test(expected = MusInterval.UnexpectedSoundsAmountException.class)
    public void add_NoSoundSpecified_ShouldFail() throws MusInterval.Exception, AnkiDroidHelper.InvalidAnkiDatabaseException {
        final long deckId = new Random().nextLong();
        final long modelId = new Random().nextLong();

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class);
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(SIGNATURE).when(helper).getFieldList(eq(modelId));
        doReturn(deckId).when(helper).findDeckIdByName(defaultDeckName);

        MusInterval mi = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .sounds(new String[]{}) // should not be empty on adding
                .notes(ALL_NOTES)
                .octaves(ALL_OCTAVES)
                .intervals(MusInterval.Fields.Interval.VALUES)
                .build();

        mi.addToAnki(null, null); // should throw exception
    }

    @Test(expected = MusInterval.MandatorySelectionEmptyException.class)
    public void add_NoNoteSpecified_ShouldFail() throws MusInterval.Exception, AnkiDroidHelper.InvalidAnkiDatabaseException {
        final long deckId = new Random().nextLong();
        final long modelId = new Random().nextLong();

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class);
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(SIGNATURE).when(helper).getFieldList(eq(modelId));
        doReturn(deckId).when(helper).findDeckIdByName(defaultDeckName);

        new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .sounds(new String[]{"/path/to/file"})
                .notes(new String[]{})
                .build().addToAnki(null, null); // should throw exception
    }

    @Test(expected = MusInterval.MandatorySelectionEmptyException.class)
    public void add_NoOctaveSpecified_ShouldFail() throws MusInterval.Exception, AnkiDroidHelper.InvalidAnkiDatabaseException {
        final long deckId = new Random().nextLong();
        final long modelId = new Random().nextLong();

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class);
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(SIGNATURE).when(helper).getFieldList(eq(modelId));
        doReturn(deckId).when(helper).findDeckIdByName(defaultDeckName);

        new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .sounds(new String[]{"/path/to/file"})
                .notes(new String[]{defaultNote})
                .octaves(new String[]{})
                .build().addToAnki(null, null); // should throw exception
    }

    @Test(expected = MusInterval.MandatorySelectionEmptyException.class)
    public void add_NoIntervalSpecified_ShouldFail() throws MusInterval.Exception, AnkiDroidHelper.InvalidAnkiDatabaseException {
        final long deckId = new Random().nextLong();
        final long modelId = new Random().nextLong();

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class);
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(SIGNATURE).when(helper).getFieldList(eq(modelId));
        doReturn(deckId).when(helper).findDeckIdByName(defaultDeckName);

        new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .sounds(new String[]{"/path/to/file"})
                .notes(new String[]{defaultNote})
                .octaves(new String[]{defaultOctave})
                .intervals(new String[]{}) // should throw exception
                .build().addToAnki(null, null);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void add_TheSameSoundFileName2Times_shouldCreate2DifferentSoundFilesInAnki() throws MusInterval.Exception, AnkiDroidHelper.InvalidAnkiDatabaseException {
        final long deckId = new Random().nextLong();
        final long modelId = new Random().nextLong();
        final long noteId = new Random().nextLong();

        final String sound = "/path/to/file.mp3";
        final String newSound1 = "music_interval_12345.mp3";
        final String newSound2 = "music_interval_23456.mp3";

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class);
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(SIGNATURE).when(helper).getFieldList(eq(modelId));
        doReturn(deckId).when(helper).findDeckIdByName(defaultDeckName);

        doAnswer(new Answer<String>() {
            private int count = 0;

            @Override
            public String answer(InvocationOnMock invocation) {
                if (count++ == 1)
                    return newSound1;

                return newSound2;
            }
        }).when(helper).addFileToAnkiMedia(sound);

        doReturn(noteId).when(helper).addNote(eq(modelId), eq(deckId), any(Map.class), nullable(Set.class));

        MusInterval mi1 = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .sounds(new String[]{sound})
                .notes(new String[]{note2})
                .octaves(new String[]{octave2})
                .direction(MusInterval.Fields.Direction.ASC)
                .timing(MusInterval.Fields.Timing.MELODIC)
                .intervals(new String[]{intervalMin3})
                .tempo("90")
                .instrument("violin")
                .build();

        MusInterval mi2 = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .sounds(new String[]{sound})
                .notes(new String[]{note2})
                .octaves(new String[]{octave2})
                .direction(MusInterval.Fields.Direction.ASC)
                .timing(MusInterval.Fields.Timing.MELODIC)
                .intervals(new String[]{intervalMin3})
                .tempo("90")
                .instrument("violin")
                .build();

        final LinkedList<MusInterval> addedMusIntervals = new LinkedList<>();

        AddingPrompter prompter = mock(AddingPrompter.class);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                MusInterval.AddingResult addingResult = invocation.getArgument(0);
                addedMusIntervals.add((addingResult.getMusInterval()));
                return null;
            }
        }).when(prompter).addingFinished(any(MusInterval.AddingResult.class));

        ProgressIndicator indicator = mock(ProgressIndicator.class);

        mi1.addToAnki(prompter, indicator);
        mi2.addToAnki(prompter, indicator);

        MusInterval mi1_2 = addedMusIntervals.getFirst();
        MusInterval mi2_2 = addedMusIntervals.getLast();

        assertFalse(Arrays.equals(mi1_2.sounds, mi2_2.sounds));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void add_SoundFieldContainsBrackets_shouldFail() throws MusInterval.Exception, AnkiDroidHelper.InvalidAnkiDatabaseException {
        final long deckId = new Random().nextLong();
        final long modelId = new Random().nextLong();
        final long noteId = new Random().nextLong();

        final String sound = "[sound:/path/to/file.mp3]";

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class);
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(SIGNATURE).when(helper).getFieldList(eq(modelId));
        doReturn(deckId).when(helper).findDeckIdByName(defaultDeckName);
        doReturn(noteId).when(helper).addNote(eq(modelId), eq(deckId), any(Map.class), nullable(Set.class));

        MusInterval mi = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .sounds(new String[]{sound})
                .notes(new String[]{note2})
                .octaves(new String[]{octave2})
                .direction(MusInterval.Fields.Direction.ASC)
                .timing(MusInterval.Fields.Timing.MELODIC)
                .intervals(new String[]{intervalMin3})
                .tempo("90")
                .instrument("violin")
                .build();

        AddingPrompter prompter = mock(AddingPrompter.class);
        WasCalledAnswer answer = new WasCalledAnswer();
        doAnswer(answer).when(prompter).processException(any(MusInterval.SoundAlreadyAddedException.class));

        mi.addToAnki(prompter, null); // should throw exception

        assertTrue(answer.wasCalled);
    }

    @Test(expected = MusInterval.NoteNotExistsException.class)
    @SuppressWarnings("unchecked")
    public void markExistingNote_NoteNotExists() throws AnkiDroidHelper.InvalidAnkiDatabaseException, MusInterval.Exception {
        final long deckId = new Random().nextLong();
        final long modelId = new Random().nextLong();

        LinkedList<Map<String, String>> existingNotesData = new LinkedList<>(); // empty

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class);
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(SIGNATURE).when(helper).getFieldList(eq(modelId));
        doReturn(deckId).when(helper).findDeckIdByName(defaultDeckName);
        doReturn(existingNotesData).when(helper).findNotes(eq(modelId), any(ArrayList.class), any(Map.class), any(Map.class), any(Map.class));

        MusInterval mi = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .notes(new String[]{defaultNote})
                .octaves(new String[]{defaultOctave})
                .intervals(new String[]{intervalMin3})
                .build();

        assertEquals(0, mi.getExistingNotesCount());
        assertEquals(0, mi.markExistingNotes());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void markExistingNote_MarkNoteFailure() throws AnkiDroidHelper.InvalidAnkiDatabaseException, MusInterval.Exception {
        final long deckId = new Random().nextLong();
        final long modelId = new Random().nextLong();
        final long noteId = new Random().nextLong();

        final String direction = MusInterval.Fields.Direction.ASC;
        final String timing = MusInterval.Fields.Timing.MELODIC;
        final String interval = "m2";
        final String tempo = "80";
        final String instrument = "guitar";

        LinkedList<Map<String, String>> existingNotesData = new LinkedList<>();
        Map<String, String> item1 = new HashMap<>();
        item1.put("id", Long.toString(noteId));
        item1.put(MusInterval.Fields.START_NOTE, defaultStartNote);
        item1.put(MusInterval.Fields.SOUND, "/test1");  // sound field does not matter
        item1.put(MusInterval.Fields.DIRECTION, MusInterval.Fields.Direction.ASC);
        item1.put(MusInterval.Fields.TIMING, MusInterval.Fields.Timing.MELODIC);
        item1.put(MusInterval.Fields.INTERVAL, interval);
        item1.put(MusInterval.Fields.TEMPO, tempo);
        item1.put(MusInterval.Fields.INSTRUMENT, instrument);
        existingNotesData.add(item1);

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class);
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(SIGNATURE).when(helper).getFieldList(eq(modelId));
        doReturn(deckId).when(helper).findDeckIdByName(defaultDeckName);
        doReturn(existingNotesData).when(helper).findNotes(eq(modelId), any(ArrayList.class), any(Map.class), any(Map.class), any(Map.class));

        // Marking failure
        doReturn(0).when(helper).addTagToNote(noteId, " marked ");

        MusInterval mi = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .notes(new String[]{defaultNote})
                .octaves(new String[]{defaultOctave})
                .direction(direction)
                .timing(timing)
                .intervals(new String[]{interval})
                .tempo(tempo)
                .instrument(instrument)
                .build();

        assertEquals(1, mi.getExistingNotesCount());
        assertEquals(0, mi.markExistingNotes());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void markExistingNote_MarkNoteSuccess() throws AnkiDroidHelper.InvalidAnkiDatabaseException, MusInterval.Exception {
        final long deckId = new Random().nextLong();
        final long modelId = new Random().nextLong();
        final long noteId = new Random().nextLong();

        final String direction = MusInterval.Fields.Direction.ASC;
        final String timing = MusInterval.Fields.Timing.MELODIC;
        final String interval = "m2";
        final String tempo = "80";
        final String instrument = "guitar";

        LinkedList<Map<String, String>> existingNotesData = new LinkedList<>();
        Map<String, String> item1 = new HashMap<>();
        item1.put("id", Long.toString(noteId));
        item1.put(MusInterval.Fields.START_NOTE, defaultStartNote);
        item1.put(MusInterval.Fields.SOUND, "/test1");  // sound field does not matter
        item1.put(MusInterval.Fields.DIRECTION, MusInterval.Fields.Direction.ASC);
        item1.put(MusInterval.Fields.TIMING, MusInterval.Fields.Timing.MELODIC);
        item1.put(MusInterval.Fields.INTERVAL, interval);
        item1.put(MusInterval.Fields.TEMPO, tempo);
        item1.put(MusInterval.Fields.INSTRUMENT, instrument);
        existingNotesData.add(item1);

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class);
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(SIGNATURE).when(helper).getFieldList(eq(modelId));
        doReturn(deckId).when(helper).findDeckIdByName(defaultDeckName);
        doReturn(existingNotesData).when(helper).findNotes(eq(modelId), any(ArrayList.class), any(Map.class), any(Map.class), any(Map.class));

        // Marked successfully
        doReturn(1).when(helper).addTagToNote(noteId, " marked ");

        MusInterval mi = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .notes(new String[]{defaultNote})
                .octaves(new String[]{defaultOctave})
                .direction(direction)
                .timing(timing)
                .intervals(new String[]{interval})
                .tempo(tempo)
                .instrument(instrument)
                .build();

        assertEquals(1, mi.getExistingNotesCount());
        assertEquals(1, mi.markExistingNotes());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void markExistingNote_MarkTwoNoteSuccess() throws AnkiDroidHelper.InvalidAnkiDatabaseException, MusInterval.Exception {
        final long deckId = new Random().nextLong();
        final long modelId = new Random().nextLong();

        final long noteId1 = new Random().nextLong();
        final long noteId2 = new Random().nextLong();

        final String direction = MusInterval.Fields.Direction.ASC;
        final String timing = MusInterval.Fields.Timing.MELODIC;
        final String interval = "m2";
        final String tempo = "80";
        final String instrument = "guitar";

        LinkedList<Map<String, String>> existingNotesData = new LinkedList<>();
        Map<String, String> item1 = new HashMap<>();
        item1.put("id", Long.toString(noteId1));
        item1.put(MusInterval.Fields.SOUND, "/test1");  // sound field does not matter
        item1.put(MusInterval.Fields.START_NOTE, defaultStartNote);
        item1.put(MusInterval.Fields.DIRECTION, direction);
        item1.put(MusInterval.Fields.TIMING, timing);
        item1.put(MusInterval.Fields.INTERVAL, interval);
        item1.put(MusInterval.Fields.TEMPO, tempo);
        item1.put(MusInterval.Fields.INSTRUMENT, instrument);
        existingNotesData.add(item1);
        Map<String, String> item2 = new HashMap<>();
        item2.put("id", Long.toString(noteId2));
        item2.put(MusInterval.Fields.SOUND, "/test2");  // sound field does not matter
        item2.put(MusInterval.Fields.START_NOTE, defaultStartNote);
        item2.put(MusInterval.Fields.DIRECTION, direction);
        item2.put(MusInterval.Fields.TIMING, timing);
        item2.put(MusInterval.Fields.INTERVAL, interval);
        item2.put(MusInterval.Fields.TEMPO, tempo);
        item2.put(MusInterval.Fields.INSTRUMENT, instrument);
        item2.put("tags", " tag1 ");
        existingNotesData.add(item2);

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class);
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(SIGNATURE).when(helper).getFieldList(eq(modelId));
        doReturn(deckId).when(helper).findDeckIdByName(defaultDeckName);
        doReturn(existingNotesData).when(helper).findNotes(eq(modelId), any(ArrayList.class), any(Map.class), any(Map.class), any(Map.class));

        // Marked successfully
        doReturn(1).when(helper).addTagToNote(noteId1, " marked ");
        doReturn(1).when(helper).addTagToNote(noteId2, " tag1 marked ");

        MusInterval mi = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .sounds(new String[]{})
                .notes(new String[]{defaultNote})
                .octaves(new String[]{defaultOctave})
                .direction(direction)
                .timing(timing)
                .intervals(new String[]{interval})
                .tempo(tempo)
                .instrument(instrument)
                .build();

        assertEquals(2, mi.getExistingNotesCount());
        assertEquals(2, mi.markExistingNotes());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void markExistingNote_MarkNoteWithTagsSuccess() throws AnkiDroidHelper.InvalidAnkiDatabaseException, MusInterval.Exception {
        final long deckId = new Random().nextLong();
        final long modelId = new Random().nextLong();
        final long noteId = new Random().nextLong();

        final String direction = MusInterval.Fields.Direction.ASC;
        final String timing = MusInterval.Fields.Timing.MELODIC;
        final String interval = "m2";
        final String tempo = "80";
        final String instrument = "guitar";

        LinkedList<Map<String, String>> existingNotesData = new LinkedList<>();
        Map<String, String> item1 = new HashMap<>();
        item1.put("id", Long.toString(noteId));
        item1.put(MusInterval.Fields.START_NOTE, defaultStartNote);
        item1.put(MusInterval.Fields.SOUND, "/test1");  // sound field does not matter
        item1.put(MusInterval.Fields.DIRECTION, MusInterval.Fields.Direction.ASC);
        item1.put(MusInterval.Fields.TIMING, MusInterval.Fields.Timing.MELODIC);
        item1.put(MusInterval.Fields.INTERVAL, interval);
        item1.put(MusInterval.Fields.TEMPO, tempo);
        item1.put(MusInterval.Fields.INSTRUMENT, instrument);
        item1.put("tags", " some tags benchmarked marked_as_red ");
        existingNotesData.add(item1);

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class);
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(SIGNATURE).when(helper).getFieldList(eq(modelId));
        doReturn(deckId).when(helper).findDeckIdByName(defaultDeckName);
        doReturn(existingNotesData).when(helper).findNotes(eq(modelId), any(ArrayList.class), any(Map.class), any(Map.class), any(Map.class));

        // Marked successfully
        doReturn(1).when(helper).addTagToNote(noteId, " some tags benchmarked marked_as_red marked ");

        MusInterval mi = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .notes(new String[]{defaultNote})
                .octaves(new String[]{defaultOctave})
                .direction(direction)
                .timing(timing)
                .intervals(new String[]{interval})
                .tempo(tempo)
                .instrument(instrument)
                .build();

        assertEquals(1, mi.getExistingNotesCount());
        assertEquals(1, mi.markExistingNotes());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void markExistingNote_MarkAlreadyMarkedNoteSuccess() throws AnkiDroidHelper.InvalidAnkiDatabaseException, MusInterval.Exception {
        final long deckId = new Random().nextLong();
        final long modelId = new Random().nextLong();
        final long noteId = new Random().nextLong();

        final String direction = MusInterval.Fields.Direction.ASC;
        final String timing = MusInterval.Fields.Timing.MELODIC;
        final String interval = "m2";
        final String tempo = "80";
        final String instrument = "guitar";

        LinkedList<Map<String, String>> existingNotesData = new LinkedList<>();
        Map<String, String> item1 = new HashMap<>();
        item1.put("id", Long.toString(noteId));
        item1.put(MusInterval.Fields.START_NOTE, defaultStartNote);
        item1.put(MusInterval.Fields.SOUND, "/test1");  // sound field does not matter
        item1.put(MusInterval.Fields.DIRECTION, MusInterval.Fields.Direction.ASC);
        item1.put(MusInterval.Fields.TIMING, MusInterval.Fields.Timing.MELODIC);
        item1.put(MusInterval.Fields.INTERVAL, interval);
        item1.put(MusInterval.Fields.TEMPO, tempo);
        item1.put(MusInterval.Fields.INSTRUMENT, instrument);
        item1.put("tags", " marked ");
        existingNotesData.add(item1);

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class);
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(SIGNATURE).when(helper).getFieldList(eq(modelId));
        doReturn(deckId).when(helper).findDeckIdByName(defaultDeckName);
        doReturn(existingNotesData).when(helper).findNotes(eq(modelId), any(ArrayList.class), any(Map.class), any(Map.class), any(Map.class));

        // Marked successfully
        doReturn(1).when(helper).addTagToNote(noteId, " marked ");

        MusInterval mi = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .notes(new String[]{defaultNote})
                .octaves(new String[]{defaultOctave})
                .direction(direction)
                .timing(timing)
                .intervals(new String[]{interval})
                .tempo(tempo)
                .instrument(instrument)
                .build();

        assertEquals(1, mi.getExistingNotesCount());
        assertEquals(0, mi.markExistingNotes());
    }

    @Test(expected = NullPointerException.class)
    public void create_withNoHelper_shouldThrowException() throws MusInterval.Exception {
        new MusInterval.Builder(null).build();
    }

    @Test
    public void create_withOnlyHelperAndNoteAndOctaveAndInterval_shouldBeOk() throws MusInterval.Exception {
        final long modelId = new Random().nextLong();

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class);
        doReturn(modelId).when(helper).findModelIdByName(any(String.class));
        doReturn(SIGNATURE).when(helper).getFieldList(eq(modelId));
        doReturn(null).when(helper).findDeckIdByName(any(String.class));

        final String[] notes = new String[]{"F"};
        final String[] octaves = new String[]{"4"};
        final String[] intervals = new String[]{"TT"};

        MusInterval mi = new MusInterval.Builder(helper)
                .notes(notes)
                .octaves(octaves)
                .intervals(intervals)
                .build();

        assertArrayEquals(notes, mi.notes);
        assertArrayEquals(octaves, mi.octaves);
        assertArrayEquals(intervals, mi.intervals);
    }

    @Test
    public void create_withAllFields_shouldBeOk() throws MusInterval.Exception {
        final long modelId = new Random().nextLong();
        final String modelName = "Model name";
        final String deckName = "Deck name";

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class);
        doReturn(modelId).when(helper).findModelIdByName(modelName);
        doReturn(SIGNATURE).when(helper).getFieldList(eq(modelId));
        doReturn(null).when(helper).findDeckIdByName(deckName);

        final String sound = "/path/to/file";
        final String direction = MusInterval.Fields.Direction.ASC;
        final String timing = MusInterval.Fields.Timing.MELODIC;
        final String interval = "m2";
        final String tempo = "80";
        final String instrument = "guitar";

        MusInterval mi = new MusInterval.Builder(helper)
                .model(modelName)
                .deck(deckName)
                .sounds(new String[]{sound})
                .notes(new String[]{defaultNote})
                .octaves(new String[]{defaultOctave})
                .direction(direction)
                .timing(timing)
                .intervals(new String[]{interval})
                .tempo(tempo)
                .instrument(instrument)
                .build();

        assertEquals(modelName, mi.modelName);
        assertEquals(deckName, mi.deckName);
        assertArrayEquals(new String[]{sound}, mi.sounds);
        assertArrayEquals(new String[]{defaultNote}, mi.notes);
        assertArrayEquals(new String[]{defaultOctave}, mi.octaves);
        assertEquals(direction, mi.direction);
        assertEquals(timing, mi.timing);
        assertArrayEquals(new String[]{interval}, mi.intervals);
        assertEquals(tempo, mi.tempo);
        assertEquals(instrument, mi.instrument);
    }

    @Test
    public void create_MultipleBuilders_shouldNotAffectEachOther() throws MusInterval.Exception {
        final long modelId = new Random().nextLong();
        final String[] notes1 = new String[]{"C"};
        final String[] octaves1 = new String[]{"2"};
        final String[] intervals1 = new String[]{"m2"};
        final String[] notes2 = new String[]{"C"};
        final String[] octaves2 = new String[]{"3"};
        final String[] intervals2 = new String[]{"m3"};

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class);
        doReturn(modelId).when(helper).findModelIdByName(any(String.class));
        doReturn(SIGNATURE).when(helper).getFieldList(eq(modelId));
        doReturn(null).when(helper).findDeckIdByName(any(String.class));

        MusInterval.Builder builder1 = new MusInterval.Builder(helper)
                .notes(notes1)
                .octaves(octaves1)
                .intervals(intervals1);

        MusInterval.Builder builder2 = new MusInterval.Builder(helper)
                .notes(notes2)
                .octaves(octaves2)
                .intervals(intervals2);

        MusInterval mi1 = builder1.build();
        MusInterval mi2 = builder2.build();

        assertArrayEquals(notes1, mi1.notes);
        assertArrayEquals(octaves1, mi1.octaves);
        assertArrayEquals(intervals1, mi1.intervals);
        assertArrayEquals(notes2, mi2.notes);
        assertArrayEquals(octaves2, mi2.octaves);
        assertArrayEquals(intervals2, mi2.intervals);
    }


    @Test
    public void create_CorrectTempo_shouldBeOk() throws MusInterval.Exception {
        final long modelId = new Random().nextLong();

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class);
        doReturn(modelId).when(helper).findModelIdByName(any(String.class));
        doReturn(SIGNATURE).when(helper).getFieldList(eq(modelId));
        doReturn(null).when(helper).findDeckIdByName(any(String.class));

        String tempo = "80"; // correct

        new MusInterval.Builder(helper)
                .notes(ALL_NOTES)
                .octaves(ALL_OCTAVES)
                .intervals(MusInterval.Fields.Interval.VALUES)

                .tempo(tempo)
                .build();

        tempo = "     90    "; // also should be correct

        new MusInterval.Builder(helper)
                .notes(ALL_NOTES)
                .octaves(ALL_OCTAVES)
                .intervals(MusInterval.Fields.Interval.VALUES)
                .tempo(tempo)
                .build();
    }

    @Test
    @SuppressWarnings({"unchecked"})
    public void add_SimilarIntervals_shouldCreateLinks() throws MusInterval.Exception, AnkiDroidHelper.InvalidAnkiDatabaseException {
        final long deckId = new Random().nextLong();
        final long modelId = new Random().nextLong();
        final long noteId = new Random().nextLong();

        final AnkiDroidHelper helper = mock(AnkiDroidHelper.class);
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(SIGNATURE).when(helper).getFieldList(eq(modelId));
        doReturn(deckId).when(helper).findDeckIdByName(defaultDeckName);
        doAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) {
                return invocation.getArgument(0);
            }
        }).when(helper).addFileToAnkiMedia(any(String.class));
        doReturn(noteId).when(helper).addNote(eq(modelId), eq(deckId), any(Map.class), nullable(Set.class));

        final MusInterval[] musIntervals = new MusInterval[MusInterval.Fields.Interval.VALUES.length];
        for (int i = 0; i < musIntervals.length; i++) {
            String interval = MusInterval.Fields.Interval.VALUES[i];
            String sound = String.format("%s.mp3", interval);
            musIntervals[i] = new MusInterval.Builder(helper)
                    .model(defaultModelName)
                    .deck(defaultDeckName)
                    .sounds(new String[]{sound})
                    .notes(new String[]{defaultNote})
                    .octaves(new String[]{defaultOctave})
                    .direction(MusInterval.Fields.Direction.ASC)
                    .timing(MusInterval.Fields.Timing.MELODIC)
                    .intervals(new String[]{interval})
                    .tempo("90")
                    .instrument("violin")
                    .build();
        }

        final ArrayList<MusInterval> musIntervalsAdded = new ArrayList<>();

        doAnswer(new Answer<LinkedList<Map<String, String>>>() {
            @Override
            public LinkedList<Map<String, String>> answer(InvocationOnMock invocation) {
                Map<String, String> inputData = new HashMap<>((Map<String, String>) invocation.getArgument(1));
                LinkedList<Map<String, String>> result = new LinkedList<>();
                for (int i = 0; i < musIntervalsAdded.size(); i++) {
                    MusInterval mi = musIntervalsAdded.get(i);
                    String sound = mi.sounds[0];
                    Map<String, String> data;
                    try {
                        data = mi.getCollectedDataSet().get(0);
                    } catch (Throwable e) {
                        data = new HashMap<>();
                    }
                    data.remove(MusInterval.Fields.SOUND);
                    data.remove(MusInterval.Fields.SOUND_SMALLER);
                    data.remove(MusInterval.Fields.SOUND_LARGER);
                    data.remove(MusInterval.Fields.VERSION);
                    if (inputData.equals(data)) {
                        data.put(MusInterval.Fields.SOUND, sound);
                        data.put("id", String.valueOf(i));
                        result.add(data);
                    } else {
                        data.put(MusInterval.Fields.SOUND, sound);
                    }
                }
                return result;
            }
        }).when(helper).findNotes(eq(modelId), any(Map.class), any(Map.class), any(Map.class), any(Map.class));

        doAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                int idx = (int) (long) invocation.getArgument(1);
                Map<String, String> data = new HashMap<>((Map<String, String>) invocation.getArgument(2));
                String startNote = data.get(MusInterval.Fields.START_NOTE);
                String note = startNote.substring(0, startNote.length() - 1);
                String octave = String.valueOf(startNote.charAt(startNote.length() - 1));
                MusInterval updated = new MusInterval.Builder(helper)
                        .model(defaultModelName)
                        .deck(defaultDeckName)
                        .sounds(new String[]{data.get(MusInterval.Fields.SOUND)})
                        .sounds_smaller(new String[]{data.get(MusInterval.Fields.SOUND_SMALLER)})
                        .sounds_larger(new String[]{data.get(MusInterval.Fields.SOUND_LARGER)})
                        .notes(new String[]{note})
                        .octaves(new String[]{octave})
                        .direction(data.get(MusInterval.Fields.DIRECTION))
                        .timing(data.get(MusInterval.Fields.TIMING))
                        .intervals(new String[]{data.get(MusInterval.Fields.INTERVAL)})
                        .tempo(data.get(MusInterval.Fields.TEMPO))
                        .instrument(data.get(MusInterval.Fields.INSTRUMENT))
                        .build();
                musIntervalsAdded.set(idx, updated);
                return true;
            }
        }).when(helper).updateNote(eq(modelId), any(Long.class), any(Map.class));

        final LinkedList<MusInterval> addedMusIntervals = new LinkedList<>();

        AddingPrompter prompter = mock(AddingPrompter.class);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                MusInterval.AddingResult addingResult = invocation.getArgument(0);
                addedMusIntervals.add((addingResult.getMusInterval()));
                return null;
            }
        }).when(prompter).addingFinished(any(MusInterval.AddingResult.class));

        ProgressIndicator indicator = mock(ProgressIndicator.class);

        musIntervals[0].addToAnki(prompter, indicator);
        musIntervalsAdded.add(addedMusIntervals.getLast());
        assertArrayEquals(new String[]{""}, musIntervalsAdded.get(0).soundsSmaller);
        assertArrayEquals(new String[]{""}, musIntervalsAdded.get(0).soundsLarger);
        for (int i = 1; i < musIntervals.length; i++) {
            musIntervals[i].addToAnki(prompter, indicator);
            musIntervalsAdded.add(addedMusIntervals.getLast());
            assertArrayEquals(musIntervalsAdded.get(i - 1).sounds, musIntervalsAdded.get(i).soundsSmaller);
            assertArrayEquals(musIntervalsAdded.get(i).sounds, musIntervalsAdded.get(i - 1).soundsLarger);
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void add_DifferentIntervals_shouldNotCreateLinks() throws MusInterval.Exception, AnkiDroidHelper.InvalidAnkiDatabaseException {
        final long deckId = new Random().nextLong();
        final long modelId = new Random().nextLong();
        final long noteId = new Random().nextLong();

        final AnkiDroidHelper helper = mock(AnkiDroidHelper.class);
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(SIGNATURE).when(helper).getFieldList(eq(modelId));
        doReturn(deckId).when(helper).findDeckIdByName(defaultDeckName);
        doAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) {
                return invocation.getArgument(0);
            }
        }).when(helper).addFileToAnkiMedia(any(String.class));
        doReturn(noteId).when(helper).addNote(eq(modelId), eq(deckId), any(Map.class), nullable(Set.class));

        final MusInterval[] musIntervals = new MusInterval[MusInterval.Fields.Interval.VALUES.length];
        for (int i = 0; i < musIntervals.length; i++) {
            String interval = MusInterval.Fields.Interval.VALUES[i];
            String sound = String.format("%s.mp3", interval);
            musIntervals[i] = new MusInterval.Builder(helper)
                    .model(defaultModelName)
                    .deck(defaultDeckName)
                    .sounds(new String[]{sound})
                    .notes(new String[]{defaultNote})
                    .octaves(new String[]{defaultOctave})
                    .direction(MusInterval.Fields.Direction.ASC)
                    .timing(MusInterval.Fields.Timing.MELODIC)
                    .intervals(new String[]{interval})
                    .tempo("90")
                    .instrument(String.format("instrument%d", i)) // different instruments
                    .build();
        }

        final ArrayList<MusInterval> musIntervalsAdded = new ArrayList<>();

        doAnswer(new Answer<LinkedList<Map<String, String>>>() {
            @Override
            public LinkedList<Map<String, String>> answer(InvocationOnMock invocation) {
                Map<String, String> inputData = new HashMap<>((Map<String, String>) invocation.getArgument(1));
                LinkedList<Map<String, String>> result = new LinkedList<>();
                for (int i = 0; i < musIntervalsAdded.size(); i++) {
                    MusInterval mi = musIntervalsAdded.get(i);
                    String sound = mi.sounds[0];
                    Map<String, String> data;
                    try {
                        data = mi.getCollectedDataSet().get(0);
                    } catch (Throwable e) {
                        data = new HashMap<>();
                    }
                    data.remove(MusInterval.Fields.SOUND);
                    data.remove(MusInterval.Fields.SOUND_SMALLER);
                    data.remove(MusInterval.Fields.SOUND_LARGER);
                    if (inputData.equals(data)) {
                        data.put(MusInterval.Fields.SOUND, sound);
                        data.put("id", String.valueOf(i));
                        result.add(data);
                    } else {
                        data.put(MusInterval.Fields.SOUND, sound);
                    }
                }
                return result;
            }
        }).when(helper).findNotes(eq(modelId), any(Map.class), any(Map.class), any(Map.class), any(Map.class));

        doAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                int idx = (int) (long) invocation.getArgument(1);
                Map<String, String> data = new HashMap<>((Map<String, String>) invocation.getArgument(2));
                String startNote = data.get(MusInterval.Fields.START_NOTE);
                String note = startNote.substring(0, startNote.length() - 1);
                String octave = String.valueOf(startNote.charAt(startNote.length() - 1));
                MusInterval updated = new MusInterval.Builder(helper)
                        .model(defaultModelName)
                        .deck(defaultDeckName)
                        .sounds(new String[]{data.get(MusInterval.Fields.SOUND)})
                        .sounds_smaller(new String[]{data.get(MusInterval.Fields.SOUND_SMALLER)})
                        .sounds_larger(new String[]{data.get(MusInterval.Fields.SOUND_LARGER)})
                        .notes(new String[]{note})
                        .octaves(new String[]{octave})
                        .direction(data.get(MusInterval.Fields.DIRECTION))
                        .timing(data.get(MusInterval.Fields.TIMING))
                        .intervals(new String[]{data.get(MusInterval.Fields.INTERVAL)})
                        .tempo(data.get(MusInterval.Fields.TEMPO))
                        .instrument(data.get(MusInterval.Fields.INSTRUMENT))
                        .build();
                musIntervalsAdded.set(idx, updated);
                return true;
            }
        }).when(helper).updateNote(eq(modelId), any(Long.class), any(Map.class));

        final LinkedList<MusInterval> addedMusIntervals = new LinkedList<>();

        AddingPrompter prompter = mock(AddingPrompter.class);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                MusInterval.AddingResult addingResult = invocation.getArgument(0);
                addedMusIntervals.add((addingResult.getMusInterval()));
                return null;
            }
        }).when(prompter).addingFinished(any(MusInterval.AddingResult.class));

        ProgressIndicator indicator = mock(ProgressIndicator.class);

        for (MusInterval musInterval : musIntervals) {
            musInterval.addToAnki(prompter, indicator);
            musIntervalsAdded.add(addedMusIntervals.getLast());
            assertArrayEquals(new String[]{}, musInterval.soundsSmaller);
            assertArrayEquals(new String[]{}, musInterval.soundsLarger);
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void add_SimilarIntervalToDuplicates_shouldCreateLinkToLatest() throws MusInterval.Exception, AnkiDroidHelper.InvalidAnkiDatabaseException {
        final long deckId = new Random().nextLong();
        final long modelId = new Random().nextLong();
        final long noteId = new Random().nextLong();

        final AnkiDroidHelper helper = mock(AnkiDroidHelper.class);
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(SIGNATURE).when(helper).getFieldList(eq(modelId));
        doReturn(deckId).when(helper).findDeckIdByName(defaultDeckName);
        doAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) {
                return invocation.getArgument(0);
            }
        }).when(helper).addFileToAnkiMedia(any(String.class));
        doReturn(noteId).when(helper).addNote(eq(modelId), eq(deckId), any(Map.class), nullable(Set.class));

        final String interval = MusInterval.Fields.Interval.VALUES[2];
        final String intervalSmaller = MusInterval.Fields.Interval.VALUES[1];
        final String intervalLarger = MusInterval.Fields.Interval.VALUES[3];

        final MusInterval[] musIntervals = new MusInterval[2];
        for (int i = 0; i < musIntervals.length; i++) {
            String sound = String.format("musInterval%d.mp3", i);
            musIntervals[i] = new MusInterval.Builder(helper)
                    .model(defaultModelName)
                    .deck(defaultDeckName)
                    .sounds(new String[]{sound})
                    .notes(new String[]{defaultNote})
                    .octaves(new String[]{defaultOctave})
                    .direction(MusInterval.Fields.Direction.ASC)
                    .timing(MusInterval.Fields.Timing.MELODIC)
                    .intervals(new String[]{interval})
                    .tempo("90")
                    .instrument("violin")
                    .build();
        }
        final MusInterval musIntervalSmaller = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .sounds(new String[]{"intervalSmaller.mp3"})
                .notes(new String[]{defaultNote})
                .octaves(new String[]{defaultOctave})
                .direction(MusInterval.Fields.Direction.ASC)
                .timing(MusInterval.Fields.Timing.MELODIC)
                .intervals(new String[]{intervalSmaller})
                .tempo("90")
                .instrument("violin")
                .build();
        final MusInterval musIntervalLarger = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .sounds(new String[]{"intervalLarger.mp3"})
                .notes(new String[]{defaultNote})
                .octaves(new String[]{defaultOctave})
                .direction(MusInterval.Fields.Direction.ASC)
                .timing(MusInterval.Fields.Timing.MELODIC)
                .intervals(new String[]{intervalLarger})
                .tempo("90")
                .instrument("violin")
                .build();

        final LinkedList<MusInterval> musIntervalsAdded = new LinkedList<>();

        doAnswer(new Answer<LinkedList<Map<String, String>>>() {
            @Override
            public LinkedList<Map<String, String>> answer(InvocationOnMock invocation) {
                Map<String, String> inputData = new HashMap<>((Map<String, String>) invocation.getArgument(1));
                LinkedList<Map<String, String>> result = new LinkedList<>();
                for (int i = 0; i < musIntervalsAdded.size(); i++) {
                    MusInterval mi = musIntervalsAdded.get(i);
                    String sound = mi.sounds[0];
                    Map<String, String> data;
                    try {
                        data = mi.getCollectedDataSet().get(0);
                    } catch (Throwable e) {
                        data = new HashMap<>();
                    }
                    data.remove(MusInterval.Fields.SOUND);
                    data.remove(MusInterval.Fields.SOUND_SMALLER);
                    data.remove(MusInterval.Fields.SOUND_LARGER);
                    data.remove(MusInterval.Fields.VERSION);
                    if (inputData.equals(data)) {
                        data.put(MusInterval.Fields.SOUND, sound);
                        data.put("id", String.valueOf(i));
                        result.add(data);
                    } else {
                        data.put(MusInterval.Fields.SOUND, sound);
                    }
                }
                return result;
            }
        }).when(helper).findNotes(eq(modelId), any(Map.class), any(Map.class), any(Map.class), any(Map.class));

        doAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                int idx = (int) (long) invocation.getArgument(1);
                Map<String, String> data = new HashMap<>((Map<String, String>) invocation.getArgument(2));
                String startNote = data.get(MusInterval.Fields.START_NOTE);
                String note = startNote.substring(0, startNote.length() - 1);
                String octave = String.valueOf(startNote.charAt(startNote.length() - 1));
                MusInterval updated = new MusInterval.Builder(helper)
                        .model(defaultModelName)
                        .deck(defaultDeckName)
                        .sounds(new String[]{data.get(MusInterval.Fields.SOUND)})
                        .sounds_smaller(new String[]{data.get(MusInterval.Fields.SOUND_SMALLER)})
                        .sounds_larger(new String[]{data.get(MusInterval.Fields.SOUND_LARGER)})
                        .notes(new String[]{note})
                        .octaves(new String[]{octave})
                        .direction(data.get(MusInterval.Fields.DIRECTION))
                        .timing(data.get(MusInterval.Fields.TIMING))
                        .intervals(new String[]{data.get(MusInterval.Fields.INTERVAL)})
                        .tempo(data.get(MusInterval.Fields.TEMPO))
                        .instrument(data.get(MusInterval.Fields.INSTRUMENT))
                        .build();
                musIntervalsAdded.set(idx, updated);
                return true;
            }
        }).when(helper).updateNote(eq(modelId), any(Long.class), any(Map.class));

        AddingPrompter prompter = mock(AddingPrompter.class);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                musIntervalsAdded.add(((AddingHandler) invocation.getArgument(1)).add());
                return null;
            }
        }).when(prompter).promptAddDuplicate(any(MusInterval[].class), any(AddingHandler.class));

        final LinkedList<MusInterval> addedMusIntervals = new LinkedList<>();
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                MusInterval.AddingResult addingResult = invocation.getArgument(0);
                addedMusIntervals.add(addingResult.getMusInterval());
                return null;
            }
        }).when(prompter).addingFinished(any(MusInterval.AddingResult.class));

        ProgressIndicator indicator = mock(ProgressIndicator.class);

        musIntervals[0].addToAnki(prompter, indicator);
        musIntervalsAdded.add(addedMusIntervals.getLast());
        for (int i = 1; i < musIntervals.length; i++) {
            musIntervals[i].addToAnki(prompter, indicator);
            musIntervalsAdded.add(addedMusIntervals.getLast());
        }
        musIntervalSmaller.addToAnki(prompter, indicator);
        MusInterval musIntervalSmallerAdded = addedMusIntervals.getLast();
        assertArrayEquals(musIntervalsAdded.getLast().sounds, musIntervalSmallerAdded.soundsLarger);
        musIntervalLarger.addToAnki(prompter, indicator);
        MusInterval musIntervalLargerAdded = addedMusIntervals.getLast();
        assertArrayEquals(musIntervalsAdded.getLast().sounds, musIntervalLargerAdded.soundsSmaller);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void add_DuplicateSimilarInterval_shouldUpdateLink() throws MusInterval.Exception, AnkiDroidHelper.InvalidAnkiDatabaseException {
        final long deckId = new Random().nextLong();
        final long modelId = new Random().nextLong();
        final long noteId = new Random().nextLong();

        final AnkiDroidHelper helper = mock(AnkiDroidHelper.class);
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(SIGNATURE).when(helper).getFieldList(eq(modelId));
        doReturn(deckId).when(helper).findDeckIdByName(defaultDeckName);
        doAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) {
                return invocation.getArgument(0);
            }
        }).when(helper).addFileToAnkiMedia(any(String.class));
        doReturn(noteId).when(helper).addNote(eq(modelId), eq(deckId), any(Map.class), nullable(Set.class));

        final String interval = MusInterval.Fields.Interval.VALUES[2];
        final String intervalSmaller = MusInterval.Fields.Interval.VALUES[1];
        final String intervalLarger = MusInterval.Fields.Interval.VALUES[3];

        final MusInterval musInterval = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .sounds(new String[]{"musInterval.mp3"})
                .notes(new String[]{defaultNote})
                .octaves(new String[]{defaultOctave})
                .direction(MusInterval.Fields.Direction.ASC)
                .timing(MusInterval.Fields.Timing.MELODIC)
                .intervals(new String[]{interval})
                .tempo("90")
                .instrument("violin")
                .build();
        final MusInterval[] musIntervalsSmaller = new MusInterval[2];
        for (int i = 0; i < musIntervalsSmaller.length; i++) {
            String sound = String.format("musIntervalSmaller%d.mp3", i);
            musIntervalsSmaller[i] = new MusInterval.Builder(helper)
                    .model(defaultModelName)
                    .deck(defaultDeckName)
                    .sounds(new String[]{sound})
                    .notes(new String[]{defaultNote})
                    .octaves(new String[]{defaultOctave})
                    .direction(MusInterval.Fields.Direction.ASC)
                    .timing(MusInterval.Fields.Timing.MELODIC)
                    .intervals(new String[]{intervalSmaller})
                    .tempo("90")
                    .instrument("violin")
                    .build();
        }
        final MusInterval[] musIntervalsLarger = new MusInterval[2];
        for (int i = 0; i < musIntervalsLarger.length; i++) {
            String sound = String.format("musIntervalLarger%d", i);
            musIntervalsLarger[i] = new MusInterval.Builder(helper)
                    .model(defaultModelName)
                    .deck(defaultDeckName)
                    .sounds(new String[]{sound})
                    .notes(new String[]{defaultNote})
                    .octaves(new String[]{defaultOctave})
                    .direction(MusInterval.Fields.Direction.ASC)
                    .timing(MusInterval.Fields.Timing.MELODIC)
                    .intervals(new String[]{intervalLarger})
                    .tempo("90")
                    .instrument("violin")
                    .build();
        }

        final LinkedList<MusInterval> musIntervalsAdded = new LinkedList<>();

        doAnswer(new Answer<LinkedList<Map<String, String>>>() {
            @Override
            public LinkedList<Map<String, String>> answer(InvocationOnMock invocation) {
                Map<String, String> inputData = new HashMap<>((Map<String, String>) invocation.getArgument(1));
                LinkedList<Map<String, String>> result = new LinkedList<>();
                for (int i = 0; i < musIntervalsAdded.size(); i++) {
                    MusInterval mi = musIntervalsAdded.get(i);
                    String sound = mi.sounds[0];
                    Map<String, String> data;
                    try {
                        data = mi.getCollectedDataSet().get(0);
                    } catch (Throwable e) {
                        data = new HashMap<>();
                    }
                    data.remove(MusInterval.Fields.SOUND);
                    data.remove(MusInterval.Fields.SOUND_SMALLER);
                    data.remove(MusInterval.Fields.SOUND_LARGER);
                    data.remove(MusInterval.Fields.VERSION);
                    if (inputData.equals(data)) {
                        data.put(MusInterval.Fields.SOUND, sound);
                        data.put("id", String.valueOf(i));
                        result.add(data);
                    } else {
                        data.put(MusInterval.Fields.SOUND, sound);
                    }
                }
                return result;
            }
        }).when(helper).findNotes(eq(modelId), any(Map.class), any(Map.class), any(Map.class), any(Map.class));

        doAnswer(new Answer<Boolean>() {
            @Override
            public Boolean answer(InvocationOnMock invocation) throws Throwable {
                int idx = (int) (long) invocation.getArgument(1);
                Map<String, String> data = new HashMap<>((Map<String, String>) invocation.getArgument(2));
                String startNote = data.get(MusInterval.Fields.START_NOTE);
                String note = startNote.substring(0, startNote.length() - 1);
                String octave = String.valueOf(startNote.charAt(startNote.length() - 1));
                MusInterval updated = new MusInterval.Builder(helper)
                        .model(defaultModelName)
                        .deck(defaultDeckName)
                        .sounds(new String[]{data.get(MusInterval.Fields.SOUND)})
                        .sounds_smaller(new String[]{data.get(MusInterval.Fields.SOUND_SMALLER)})
                        .sounds_larger(new String[]{data.get(MusInterval.Fields.SOUND_LARGER)})
                        .notes(new String[]{note})
                        .octaves(new String[]{octave})
                        .direction(data.get(MusInterval.Fields.DIRECTION))
                        .timing(data.get(MusInterval.Fields.TIMING))
                        .intervals(new String[]{data.get(MusInterval.Fields.INTERVAL)})
                        .tempo(data.get(MusInterval.Fields.TEMPO))
                        .instrument(data.get(MusInterval.Fields.INSTRUMENT))
                        .build();
                musIntervalsAdded.set(idx, updated);
                return true;
            }
        }).when(helper).updateNote(eq(modelId), any(Long.class), any(Map.class));

        final LinkedList<MusInterval> addedMusIntervals = new LinkedList<>();

        AddingPrompter prompter = mock(AddingPrompter.class);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                MusInterval.AddingResult addingResult = invocation.getArgument(0);
                addedMusIntervals.add((addingResult.getMusInterval()));
                return null;
            }
        }).when(prompter).addingFinished(any(MusInterval.AddingResult.class));

        ProgressIndicator indicator = mock(ProgressIndicator.class);

        musInterval.addToAnki(prompter, indicator);
        musIntervalsAdded.add(addedMusIntervals.getLast());
        for (MusInterval value : musIntervalsSmaller) {
            value.addToAnki(prompter, indicator);
            MusInterval musIntervalSmallerAdded = addedMusIntervals.getLast();
            assertArrayEquals(musIntervalSmallerAdded.sounds, musIntervalsAdded.getFirst().soundsSmaller);
        }
        for (MusInterval value : musIntervalsLarger) {
            value.addToAnki(prompter, indicator);
            MusInterval musIntervalLargerAdded = addedMusIntervals.getLast();
            assertArrayEquals(musIntervalLargerAdded.sounds, musIntervalsAdded.getFirst().soundsLarger);
        }
    }

    @Test(expected = MusInterval.ModelDoesNotExistException.class)
    public void create_UnknownModel_shouldFail() throws MusInterval.Exception {
        AnkiDroidHelper helper = mock(AnkiDroidHelper.class);
        doReturn(null).when(helper).findModelIdByName(eq(defaultModelName));
        new MusInterval.Builder(helper).model(defaultModelName).build();
    }

    @Test(expected = MusInterval.NotEnoughFieldsException.class)
    public void create_TooFewFields_shouldFail() throws MusInterval.Exception {
        long modelId = new Random().nextLong();
        AnkiDroidHelper helper = mock(AnkiDroidHelper.class);
        doReturn(modelId).when(helper).findModelIdByName(eq(defaultModelName));
        doReturn(new String[]{}).when(helper).getFieldList(eq(modelId));
        new MusInterval.Builder(helper).model(defaultModelName).build();
    }

    @Test(expected = MusInterval.ModelNotConfiguredException.class)
    public void create_MissingModelFields_shouldFail() throws MusInterval.Exception {
        long modelId = new Random().nextLong();
        AnkiDroidHelper helper = mock(AnkiDroidHelper.class);
        doReturn(modelId).when(helper).findModelIdByName(eq(defaultModelName));
        doReturn(SIGNATURE).when(helper).getFieldList(eq(modelId));
        Map<String, String> modelFields = new HashMap<String, String>() {{
            put(MusInterval.Fields.SOUND, MusInterval.Fields.SOUND);
        }};
        new MusInterval.Builder(helper)
                .model(defaultModelName)
                .model_fields(modelFields)
                .build();
    }

    @SuppressWarnings({"unchecked"})
    @Test
    public void add_Duplicate_handlerShouldBeAbleToAdd() throws MusInterval.Exception, AnkiDroidHelper.InvalidAnkiDatabaseException {
        final long deckId = new Random().nextLong();
        final long modelId = new Random().nextLong();
        final long noteId = new Random().nextLong();
        final long duplicateNoteId = new Random().nextLong();
        final LinkedList<Long> noteIds = new LinkedList<Long>() {{
            add(noteId);
            add(duplicateNoteId);
        }};
        final LinkedList<Long> addedNoteIds = new LinkedList<>();

        final AnkiDroidHelper helper = mock(AnkiDroidHelper.class);
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(SIGNATURE).when(helper).getFieldList(eq(modelId));
        doReturn(deckId).when(helper).findDeckIdByName(defaultDeckName);
        doAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) {
                return invocation.getArgument(0);
            }
        }).when(helper).addFileToAnkiMedia(any(String.class));
        doAnswer(new Answer<Long>() {
            @Override
            public Long answer(InvocationOnMock invocation) {
                addedNoteIds.add(noteIds.removeFirst());
                return addedNoteIds.getLast();
            }
        }).when(helper).addNote(eq(modelId), eq(deckId), any(Map.class), nullable(Set.class));

        final MusInterval musInterval = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .sounds(new String[]{"/path/to/file.mp3"})
                .notes(new String[]{defaultNote})
                .octaves(new String[]{defaultOctave})
                .direction(MusInterval.Fields.Direction.ASC)
                .timing(MusInterval.Fields.Timing.MELODIC)
                .intervals(new String[]{MusInterval.Fields.Interval.VALUES[1]})
                .tempo("90")
                .instrument("violin")
                .build();

        final MusInterval duplicateMusInterval = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .sounds(new String[]{"/path/to/duplicate.mp3"})
                .notes(new String[]{defaultStartNote})
                .octaves(new String[]{defaultOctave})
                .direction(MusInterval.Fields.Direction.ASC)
                .timing(MusInterval.Fields.Timing.MELODIC)
                .intervals(new String[]{MusInterval.Fields.Interval.VALUES[1]})
                .tempo("90")
                .instrument("violin")
                .build();

        doReturn(new LinkedList<Map<String, String>>()).when(helper).findNotes(eq(modelId), any(ArrayList.class), any(Map.class), any(Map.class), any(Map.class));

        final LinkedList<MusInterval> addedMusIntervals = new LinkedList<>();

        AddingPrompter prompter = mock(AddingPrompter.class);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                MusInterval.AddingResult addingResult = invocation.getArgument(0);
                addedMusIntervals.add((addingResult.getMusInterval()));
                return null;
            }
        }).when(prompter).addingFinished(any(MusInterval.AddingResult.class));

        ProgressIndicator indicator = mock(ProgressIndicator.class);

        musInterval.addToAnki(prompter, indicator);
        final MusInterval musIntervalAdded = addedMusIntervals.getLast();
        assertNotNull(musIntervalAdded);
        assertTrue(addedNoteIds.contains(noteId));

        doReturn(new LinkedList<Map<String, String>>() {{
            add(new HashMap<String, String>(musIntervalAdded.getCollectedDataSet().get(0)) {{
                put("id", String.valueOf(addedNoteIds.getLast()));
                put("tags", "");
            }});
        }}).when(helper).findNotes(eq(modelId), any(ArrayList.class), any(Map.class), any(Map.class), any(Map.class));
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                AddingHandler handler = invocation.getArgument(1);
                handler.add();
                handler.proceed();
                return null;
            }
        }).when(prompter).promptAddDuplicate(any(MusInterval[].class), any(AddingHandler.class));

        duplicateMusInterval.addToAnki(prompter, indicator);
        assertTrue(addedNoteIds.contains(duplicateNoteId));
    }

    @SuppressWarnings({"unchecked"})
    @Test
    public void add_Duplicate_handlerShouldBeAbleToMark() throws MusInterval.Exception, AnkiDroidHelper.InvalidAnkiDatabaseException {
        final long deckId = new Random().nextLong();
        final long modelId = new Random().nextLong();
        final long noteId = new Random().nextLong();

        final AnkiDroidHelper helper = mock(AnkiDroidHelper.class);
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(SIGNATURE).when(helper).getFieldList(eq(modelId));
        doReturn(deckId).when(helper).findDeckIdByName(defaultDeckName);
        doAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) {
                return invocation.getArgument(0);
            }
        }).when(helper).addFileToAnkiMedia(any(String.class));
        doReturn(noteId).when(helper).addNote(eq(modelId), eq(deckId), any(Map.class), nullable(Set.class));

        final MusInterval musInterval = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .sounds(new String[]{"/path/to/file.mp3"})
                .notes(new String[]{defaultStartNote})
                .octaves(new String[]{defaultOctave})
                .direction(MusInterval.Fields.Direction.ASC)
                .timing(MusInterval.Fields.Timing.MELODIC)
                .intervals(new String[]{MusInterval.Fields.Interval.VALUES[1]})
                .tempo("90")
                .instrument("violin")
                .build();

        final MusInterval duplicateMusInterval = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .sounds(new String[]{"/path/to/duplicate.mp3"})
                .notes(new String[]{defaultStartNote})
                .octaves(new String[]{defaultOctave})
                .direction(MusInterval.Fields.Direction.ASC)
                .timing(MusInterval.Fields.Timing.MELODIC)
                .intervals(new String[]{MusInterval.Fields.Interval.VALUES[1]})
                .tempo("90")
                .instrument("violin")
                .build();

        final LinkedList<MusInterval> addedMusIntervals = new LinkedList<>();

        AddingPrompter prompter = mock(AddingPrompter.class);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                MusInterval.AddingResult addingResult = invocation.getArgument(0);
                addedMusIntervals.add((addingResult.getMusInterval()));
                return null;
            }
        }).when(prompter).addingFinished(any(MusInterval.AddingResult.class));

        ProgressIndicator indicator = mock(ProgressIndicator.class);

        doReturn(new LinkedList<Map<String, String>>()).when(helper).findNotes(eq(modelId), any(ArrayList.class), any(Map.class), any(Map.class), any(Map.class));
        musInterval.addToAnki(prompter, indicator);
        final MusInterval musIntervalAdded = addedMusIntervals.getLast();
        assertNotNull(musIntervalAdded);

        final LinkedList<String> tags = new LinkedList<>();

        doReturn(new LinkedList<Map<String, String>>() {{
            add(new HashMap<String, String>(musIntervalAdded.getCollectedDataSet().get(0)) {{
                put("id", String.valueOf(noteId));
                put("tags", "");
            }});
        }}).when(helper).findNotes(eq(modelId), any(ArrayList.class), any(Map.class), any(Map.class), any(Map.class));
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                AddingHandler handler = invocation.getArgument(1);
                handler.mark();
                handler.proceed();
                return null;
            }
        }).when(prompter).promptAddDuplicate(any(MusInterval[].class), any(AddingHandler.class));
        doAnswer(new Answer() {
            @Override
            public Integer answer(InvocationOnMock invocation) {
                tags.add((String) invocation.getArgument(1));
                return 1;
            }
        }).when(helper).addTagToNote(eq(noteId), any(String.class));

        duplicateMusInterval.addToAnki(prompter, indicator);
        MusInterval duplicateMusIntervalAdded = addedMusIntervals.getLast();
        assertArrayEquals(duplicateMusIntervalAdded.sounds, new String[]{});
        assertTrue(tags.contains("marked "));
    }

    @SuppressWarnings({"unchecked"})
    @Test
    public void add_Duplicate_handlerShouldBeAbleToTag() throws MusInterval.Exception, AnkiDroidHelper.InvalidAnkiDatabaseException {
        final long deckId = new Random().nextLong();
        final long modelId = new Random().nextLong();
        final long noteId = new Random().nextLong();

        final AnkiDroidHelper helper = mock(AnkiDroidHelper.class);
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(SIGNATURE).when(helper).getFieldList(eq(modelId));
        doReturn(deckId).when(helper).findDeckIdByName(defaultDeckName);
        doAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) {
                return invocation.getArgument(0);
            }
        }).when(helper).addFileToAnkiMedia(any(String.class));
        doReturn(noteId).when(helper).addNote(eq(modelId), eq(deckId), any(Map.class), nullable(Set.class));

        final MusInterval musInterval = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .sounds(new String[]{"/path/to/file.mp3"})
                .notes(new String[]{defaultStartNote})
                .octaves(new String[]{defaultOctave})
                .direction(MusInterval.Fields.Direction.ASC)
                .timing(MusInterval.Fields.Timing.MELODIC)
                .intervals(new String[]{MusInterval.Fields.Interval.VALUES[1]})
                .tempo("90")
                .instrument("violin")
                .build();

        final MusInterval duplicateMusInterval = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .sounds(new String[]{"/path/to/duplicate.mp3"})
                .notes(new String[]{defaultStartNote})
                .octaves(new String[]{defaultOctave})
                .direction(MusInterval.Fields.Direction.ASC)
                .timing(MusInterval.Fields.Timing.MELODIC)
                .intervals(new String[]{MusInterval.Fields.Interval.VALUES[1]})
                .tempo("90")
                .instrument("violin")
                .build();

        final LinkedList<MusInterval> addedMusIntervals = new LinkedList<>();

        AddingPrompter prompter = mock(AddingPrompter.class);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                MusInterval.AddingResult addingResult = invocation.getArgument(0);
                addedMusIntervals.add((addingResult.getMusInterval()));
                return null;
            }
        }).when(prompter).addingFinished(any(MusInterval.AddingResult.class));

        ProgressIndicator indicator = mock(ProgressIndicator.class);

        doReturn(new LinkedList<Map<String, String>>()).when(helper).findNotes(eq(modelId), any(ArrayList.class), any(Map.class), any(Map.class), any(Map.class));
        musInterval.addToAnki(prompter, indicator);
        final MusInterval musIntervalAdded = addedMusIntervals.getLast();
        assertNotNull(musIntervalAdded);

        final String duplicateTag = "duplicate";
        final LinkedList<String> tags = new LinkedList<>();

        doReturn(new LinkedList<Map<String, String>>() {{
            add(new HashMap<String, String>(musIntervalAdded.getCollectedDataSet().get(0)) {{
                put("id", String.valueOf(noteId));
                put("tags", "");
            }});
        }}).when(helper).findNotes(eq(modelId), any(ArrayList.class), any(Map.class), any(Map.class), any(Map.class));
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                AddingHandler handler = invocation.getArgument(1);
                handler.tag(duplicateTag);
                handler.proceed();
                return null;
            }
        }).when(prompter).promptAddDuplicate(any(MusInterval[].class), any(AddingHandler.class));
        doAnswer(new Answer() {
            @Override
            public Integer answer(InvocationOnMock invocation) {
                tags.add((String) invocation.getArgument(1));
                return 1;
            }
        }).when(helper).addTagToNote(eq(noteId), any(String.class));

        duplicateMusInterval.addToAnki(prompter, indicator);
        MusInterval duplicateMusIntervalAdded = addedMusIntervals.getLast();
        assertArrayEquals(duplicateMusIntervalAdded.sounds, new String[]{});
        assertTrue(tags.contains(String.format("%s ", duplicateTag)));
    }

    @SuppressWarnings({"unchecked"})
    @Test
    public void add_Duplicate_handlerShouldBeAbleToReplace() throws MusInterval.Exception, AnkiDroidHelper.InvalidAnkiDatabaseException {
        final long deckId = new Random().nextLong();
        final long modelId = new Random().nextLong();
        final long noteId = new Random().nextLong();

        final AnkiDroidHelper helper = mock(AnkiDroidHelper.class);
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(SIGNATURE).when(helper).getFieldList(eq(modelId));
        doReturn(deckId).when(helper).findDeckIdByName(defaultDeckName);
        doAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) {
                return invocation.getArgument(0);
            }
        }).when(helper).addFileToAnkiMedia(any(String.class));
        doReturn(noteId).when(helper).addNote(eq(modelId), eq(deckId), any(Map.class), nullable(Set.class));

        final String sound = "/path/to/file.mp3";
        final MusInterval musInterval = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .sounds(new String[]{sound})
                .notes(new String[]{defaultStartNote})
                .octaves(new String[]{defaultOctave})
                .direction(MusInterval.Fields.Direction.ASC)
                .timing(MusInterval.Fields.Timing.MELODIC)
                .intervals(new String[]{MusInterval.Fields.Interval.VALUES[1]})
                .tempo("90")
                .instrument("violin")
                .build();

        final String duplicateSound = "/path/to/duplicate.mp3";
        final MusInterval duplicateMusInterval = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .sounds(new String[]{duplicateSound})
                .notes(new String[]{defaultStartNote})
                .octaves(new String[]{defaultOctave})
                .direction(MusInterval.Fields.Direction.ASC)
                .timing(MusInterval.Fields.Timing.MELODIC)
                .intervals(new String[]{MusInterval.Fields.Interval.VALUES[1]})
                .tempo("90")
                .instrument("violin")
                .build();

        final LinkedList<MusInterval> addedMusIntervals = new LinkedList<>();

        AddingPrompter prompter = mock(AddingPrompter.class);
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                MusInterval.AddingResult addingResult = invocation.getArgument(0);
                addedMusIntervals.add((addingResult.getMusInterval()));
                return null;
            }
        }).when(prompter).addingFinished(any(MusInterval.AddingResult.class));

        ProgressIndicator indicator = mock(ProgressIndicator.class);

        doReturn(new LinkedList<Map<String, String>>()).when(helper).findNotes(eq(modelId), any(ArrayList.class), any(Map.class), any(Map.class), any(Map.class));
        musInterval.addToAnki(prompter, indicator);
        final MusInterval musIntervalAdded = addedMusIntervals.getLast();
        final Map<String, String> addedData = musIntervalAdded.getCollectedDataSet().get(0);
        assertNotNull(musIntervalAdded);
        assertEquals(String.format("[sound:%s]", sound), addedData.get(MusInterval.Fields.SOUND));

        doReturn(new LinkedList<Map<String, String>>() {{
            add(new HashMap<String, String>(musIntervalAdded.getCollectedDataSet().get(0)) {{
                put("id", String.valueOf(noteId));
                put("tags", "");
            }});
        }}).when(helper).findNotes(eq(modelId), any(ArrayList.class), any(Map.class), any(Map.class), any(Map.class));
        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) throws Throwable {
                AddingHandler handler = invocation.getArgument(1);
                handler.replace();
                handler.proceed();
                return null;
            }
        }).when(prompter).promptAddDuplicate(any(MusInterval[].class), any(AddingHandler.class));
        doAnswer(new Answer() {
            @Override
            public Boolean answer(InvocationOnMock invocation) {
                Map<String, String> data = invocation.getArgument(2);
                addedData.put(MusInterval.Fields.SOUND, data.get(MusInterval.Fields.SOUND));
                return true;
            }
        }).when(helper).updateNote(eq(modelId), eq(noteId), any(Map.class));

        duplicateMusInterval.addToAnki(prompter, indicator);
        assertEquals(String.format("[sound:%s]", duplicateSound), addedData.get(MusInterval.Fields.SOUND));
    }

    @Test(expected = MusInterval.UnexpectedSoundsAmountException.class)
    public void add_BatchIncorrectNumberOfSounds_shouldFail() throws MusInterval.Exception, AnkiDroidHelper.InvalidAnkiDatabaseException {
        long modelId = new Random().nextLong();
        AnkiDroidHelper helper = mock(AnkiDroidHelper.class);
        doReturn(modelId).when(helper).findModelIdByName(eq(defaultModelName));
        doReturn(SIGNATURE).when(helper).getFieldList(eq(modelId));

        new MusInterval.Builder(helper)
                .sounds(new String[]{"/path/to/file.mp3"})
                .notes(ALL_NOTES)
                .octaves(ALL_OCTAVES)
                .intervals(MusInterval.Fields.Interval.VALUES)
                .build().addToAnki(null, null);
    }

    @Test
    @SuppressWarnings("unchecked")
    public void add_Batch_ShouldCorrectlyAssignSoundFiles() throws MusInterval.Exception, AnkiDroidHelper.InvalidAnkiDatabaseException {
        final long deckId = new Random().nextLong();
        final long modelId = new Random().nextLong();

        AnkiDroidHelper helper = mock(AnkiDroidHelper.class);
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(SIGNATURE).when(helper).getFieldList(eq(modelId));
        doReturn(deckId).when(helper).findDeckIdByName(defaultDeckName);
        doAnswer(new Answer<String>() {
            @Override
            public String answer(InvocationOnMock invocation) {
                return invocation.getArgument(0);
            }
        }).when(helper).addFileToAnkiMedia(any(String.class));
        final ArrayList<Map<String, String>> addedNotesData = new ArrayList<>();
        doAnswer(new Answer<Long>() {
            private long noteId = 1;

            @Override
            public Long answer(InvocationOnMock invocation) {
                Map<String, String> data = invocation.getArgument(2);
                addedNotesData.add(data);
                return noteId++;
            }
        }).when(helper).addNote(eq(modelId), eq(deckId), any(Map.class), nullable(Set.class));

        final int permutations = ALL_NOTES.length * ALL_OCTAVES.length * MusInterval.Fields.Interval.VALUES.length;
        String[] sounds = new String[permutations];
        for (int i = 0; i < permutations; i++) {
            sounds[i] = String.format("/path/to/file%d.mp3", i);
        }

        AddingPrompter prompter = mock(AddingPrompter.class);
        ProgressIndicator indicator = mock(ProgressIndicator.class);

        new MusInterval.Builder(helper)
                .model(defaultModelName)
                .deck(defaultDeckName)
                .sounds(sounds)
                .notes(ALL_NOTES)
                .octaves(ALL_OCTAVES)
                .direction(MusInterval.Fields.Direction.ASC)
                .timing(MusInterval.Fields.Timing.MELODIC)
                .intervals(MusInterval.Fields.Interval.VALUES)
                .tempo("90")
                .instrument("violin")
                .build()
                .addToAnki(prompter, indicator);

        assertEquals(permutations, addedNotesData.size());
        int i = 0;
        for (String octave : ALL_OCTAVES) {
            for (String note : ALL_NOTES) {
                for (String interval : MusInterval.Fields.Interval.VALUES) {
                    Map<String, String> data = addedNotesData.get(i);
                    assertEquals(data.get(MusInterval.Fields.SOUND), String.format("[sound:%s]", sounds[i]));
                    assertEquals(data.get(MusInterval.Fields.START_NOTE), note + octave);
                    assertEquals(data.get(MusInterval.Fields.INTERVAL), interval);
                    i++;
                }
            }
        }
    }

    @Test
    @SuppressWarnings("unchecked")
    public void checkIntegrity_corrupted_shouldCount() throws MusInterval.Exception, AnkiDroidHelper.InvalidAnkiDatabaseException {
        final long modelId = new Random().nextLong();
        final long corruptedNoteId = new Random().nextLong();

        final AnkiDroidHelper helper = mock(AnkiDroidHelper.class);
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(SIGNATURE).when(helper).getFieldList(eq(modelId));

        final Map<String, String> corruptedNoteData = new HashMap<String, String>() {{
            put(MusInterval.Fields.SOUND, "[sound:dir/file.mp3]");
            put(MusInterval.Fields.START_NOTE, ""); // empty start note
            put(MusInterval.Fields.INTERVAL, "min2");
            put(MusInterval.Fields.TIMING, "melodic");
            put(MusInterval.Fields.DIRECTION, "asending"); // invalid direction
            put(MusInterval.Fields.TEMPO, "80");
            put(MusInterval.Fields.INSTRUMENT, "guitar");
            put(AnkiDroidHelper.KEY_ID, String.valueOf(corruptedNoteId));
            put(AnkiDroidHelper.KEY_TAGS, "");
        }};

        final Map<String, String> searchData = new HashMap<String, String>() {{
            put(MusInterval.Fields.START_NOTE, "%%");
            put(MusInterval.Fields.INTERVAL, "%");
            put(MusInterval.Fields.TIMING, "");
            put(MusInterval.Fields.DIRECTION, "");
            put(MusInterval.Fields.TEMPO, "");
            put(MusInterval.Fields.INSTRUMENT, "");
            put(MusInterval.Fields.FIRST_NOTE_DURATION_COEFFICIENT, "");
        }};

        LinkedList<Map<String, String>> searchResult = new LinkedList<Map<String, String>>() {{
            add(corruptedNoteData);
        }};
        doReturn(searchResult).when(helper).findNotes(eq(modelId), eq(new HashMap<String, String>()), any(Map.class), any(Map.class), any(Map.class));
        ArrayList<Map<String, String>> searchDataset = new ArrayList<Map<String, String>>() {{
            add(searchData);
        }};
        doReturn(searchResult).when(helper).findNotes(eq(modelId), eq(searchDataset), any(Map.class), any(Map.class), any(Map.class));

        MusInterval mi = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .notes(null)
                .octaves(null)
                .intervals(null)
                .build();
        ProgressIndicator progressIndicator = mock(ProgressIndicator.class);
        NotesIntegrity.Summary is = new NotesIntegrity(helper, mi, corruptedTag, suspiciousTag, duplicateTag, progressIndicator).check();

        assertEquals(1, is.getNotesCount());
        assertEquals(1, is.getCorruptedNotesCount());
        final Map<String, Integer> corruptedFieldCounts = is.getCorruptedFieldCounts();
        assertEquals(new Integer(1), corruptedFieldCounts.get(MusInterval.Fields.START_NOTE));
        assertEquals(new Integer(1), corruptedFieldCounts.get(MusInterval.Fields.DIRECTION));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void checkIntegrity_corruptedFixed_shouldCount() throws MusInterval.Exception, AnkiDroidHelper.InvalidAnkiDatabaseException {
        final long modelId = new Random().nextLong();
        final long noteId = new Random().nextLong();

        final AnkiDroidHelper helper = mock(AnkiDroidHelper.class);
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(SIGNATURE).when(helper).getFieldList(eq(modelId));

        final Map<String, String> fixedNoteData = new HashMap<String, String>() {{
            put(MusInterval.Fields.SOUND, "[sound:dir/file.mp3]");
            put(MusInterval.Fields.START_NOTE, "C2");
            put(MusInterval.Fields.INTERVAL, "min2");
            put(MusInterval.Fields.TIMING, "melodic");
            put(MusInterval.Fields.DIRECTION, "ascending");
            put(MusInterval.Fields.TEMPO, "80");
            put(MusInterval.Fields.INSTRUMENT, "guitar");
            put(AnkiDroidHelper.KEY_ID, String.valueOf(noteId));
            put(AnkiDroidHelper.KEY_TAGS, String.format(" %s ",
                    corruptedTag
                            + AnkiDroidHelper.HIERARCHICAL_TAG_SEPARATOR
                            + MusInterval.Fields.START_NOTE
                            + AnkiDroidHelper.HIERARCHICAL_TAG_SEPARATOR
                            + new EmptyValidator().getErrorTag()
            )); // ok but has tag
        }};

        final Map<String, String> searchData = new HashMap<String, String>() {{
            put(MusInterval.Fields.START_NOTE, "%%");
            put(MusInterval.Fields.INTERVAL, "%");
            put(MusInterval.Fields.TIMING, "");
            put(MusInterval.Fields.DIRECTION, "");
            put(MusInterval.Fields.TEMPO, "");
            put(MusInterval.Fields.INSTRUMENT, "");
            put(MusInterval.Fields.FIRST_NOTE_DURATION_COEFFICIENT, "");
        }};

        LinkedList<Map<String, String>> searchResult = new LinkedList<Map<String, String>>() {{
            add(fixedNoteData);
        }};
        doReturn(searchResult).when(helper).findNotes(eq(modelId), eq(new HashMap<String, String>()), any(Map.class), any(Map.class), any(Map.class));
        ArrayList<Map<String, String>> searchDataset = new ArrayList<Map<String, String>>() {{
            add(searchData);
        }};
        doReturn(searchResult).when(helper).findNotes(eq(modelId), eq(searchDataset), any(Map.class), any(Map.class), any(Map.class));

        MusInterval mi = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .notes(null)
                .octaves(null)
                .intervals(null)
                .build();
        ProgressIndicator progressIndicator = mock(ProgressIndicator.class);
        NotesIntegrity.Summary is = new NotesIntegrity(helper, mi, corruptedTag, suspiciousTag, duplicateTag, progressIndicator).check();

        assertEquals(1, is.getNotesCount());
        assertEquals(0, is.getCorruptedNotesCount());
        assertEquals(1, is.getFixedCorruptedFieldsCount());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void checkIntegrity_brokenLink_shouldCount() throws MusInterval.Exception, AnkiDroidHelper.InvalidAnkiDatabaseException {
        final long modelId = new Random().nextLong();
        final long smallerNoteId = new Random().nextLong();
        final long noteId = new Random().nextLong();
        final long largerNoteId = new Random().nextLong();

        final AnkiDroidHelper helper = mock(AnkiDroidHelper.class);
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(SIGNATURE).when(helper).getFieldList(eq(modelId));

        final String smallerNoteSound = "[sound:dir/file_smaller.mp3]";
        final String noteSound = "[sound:dir/file.mp3]";
        final String largerNoteSound = "[sound:dir/file_larger.mp3]";

        final Map<String, String> smallerNoteData = new HashMap<String, String>() {{
            put(MusInterval.Fields.SOUND, smallerNoteSound);
            put(MusInterval.Fields.SOUND_SMALLER, "");
            put(MusInterval.Fields.SOUND_LARGER, noteSound);
            put(MusInterval.Fields.START_NOTE, "C2");
            put(MusInterval.Fields.INTERVAL, "min2");
            put(MusInterval.Fields.TIMING, "melodic");
            put(MusInterval.Fields.DIRECTION, "ascending");
            put(MusInterval.Fields.TEMPO, "80");
            put(MusInterval.Fields.INSTRUMENT, "guitar");
            put(AnkiDroidHelper.KEY_ID, String.valueOf(smallerNoteId));
            put(AnkiDroidHelper.KEY_TAGS, "");
        }};
        final Map<String, String> noteData = new HashMap<String, String>() {{
            put(MusInterval.Fields.SOUND, noteSound);
            put(MusInterval.Fields.SOUND_SMALLER, smallerNoteSound);
            put(MusInterval.Fields.SOUND_LARGER, largerNoteSound);
            put(MusInterval.Fields.START_NOTE, "D2"); // different start note
            put(MusInterval.Fields.INTERVAL, "Maj2");
            put(MusInterval.Fields.TIMING, "melodic");
            put(MusInterval.Fields.DIRECTION, "ascending");
            put(MusInterval.Fields.TEMPO, "80");
            put(MusInterval.Fields.INSTRUMENT, "guitar");
            put(AnkiDroidHelper.KEY_ID, String.valueOf(noteId));
            put(AnkiDroidHelper.KEY_TAGS, "");
        }};
        final Map<String, String> largerNoteData = new HashMap<String, String>() {{
            put(MusInterval.Fields.SOUND, largerNoteSound);
            put(MusInterval.Fields.SOUND_SMALLER, noteSound);
            put(MusInterval.Fields.SOUND_LARGER, "");
            put(MusInterval.Fields.START_NOTE, "C2");
            put(MusInterval.Fields.INTERVAL, "min3");
            put(MusInterval.Fields.TIMING, "melodic");
            put(MusInterval.Fields.DIRECTION, "ascending");
            put(MusInterval.Fields.TEMPO, "80");
            put(MusInterval.Fields.INSTRUMENT, "guitar");
            put(AnkiDroidHelper.KEY_ID, String.valueOf(largerNoteId));
            put(AnkiDroidHelper.KEY_TAGS, "");
        }};

        final Map<String, String> searchData = new HashMap<String, String>() {{
            put(MusInterval.Fields.START_NOTE, "%%");
            put(MusInterval.Fields.INTERVAL, "%");
            put(MusInterval.Fields.TIMING, "");
            put(MusInterval.Fields.DIRECTION, "");
            put(MusInterval.Fields.TEMPO, "");
            put(MusInterval.Fields.INSTRUMENT, "");
            put(MusInterval.Fields.FIRST_NOTE_DURATION_COEFFICIENT, "");
        }};

        LinkedList<Map<String, String>> searchResult = new LinkedList<Map<String, String>>() {{
            add(smallerNoteData);
            add(noteData);
            add(largerNoteData);
        }};
        doReturn(searchResult).when(helper).findNotes(eq(modelId), eq(new HashMap<String, String>()), any(Map.class), any(Map.class), any(Map.class));
        ArrayList<Map<String, String>> searchDataset = new ArrayList<Map<String, String>>() {{
            add(searchData);
        }};
        doReturn(searchResult).when(helper).findNotes(eq(modelId), eq(searchDataset), any(Map.class), any(Map.class), any(Map.class));

        MusInterval mi = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .notes(null)
                .octaves(null)
                .intervals(null)
                .build();
        ProgressIndicator progressIndicator = mock(ProgressIndicator.class);
        NotesIntegrity.Summary is = new NotesIntegrity(helper, mi, corruptedTag, suspiciousTag, duplicateTag, progressIndicator).check();

        assertEquals(3, is.getNotesCount());
        assertEquals(3, is.getSuspiciousNotesCount());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void checkIntegrity_suspiciousFixed_shouldCount() throws MusInterval.Exception, AnkiDroidHelper.InvalidAnkiDatabaseException {
        final long modelId = new Random().nextLong();
        final long smallerNoteId = new Random().nextLong();
        final long noteId = new Random().nextLong();
        final long largerNoteId = new Random().nextLong();

        final AnkiDroidHelper helper = mock(AnkiDroidHelper.class);
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(SIGNATURE).when(helper).getFieldList(eq(modelId));

        final String smallerNoteSound = "[sound:dir/file_smaller.mp3]";
        final String noteSound = "[sound:dir/file.mp3]";
        final String largerNoteSound = "[sound:dir/file_larger.mp3]";

        final Map<String, String> smallerNoteData = new HashMap<String, String>() {{
            put(MusInterval.Fields.SOUND, smallerNoteSound);
            put(MusInterval.Fields.SOUND_SMALLER, "");
            put(MusInterval.Fields.SOUND_LARGER, noteSound);
            put(MusInterval.Fields.START_NOTE, "C2");
            put(MusInterval.Fields.INTERVAL, "min2");
            put(MusInterval.Fields.TIMING, "melodic");
            put(MusInterval.Fields.DIRECTION, "ascending");
            put(MusInterval.Fields.TEMPO, "80");
            put(MusInterval.Fields.INSTRUMENT, "guitar");
            put(AnkiDroidHelper.KEY_ID, String.valueOf(smallerNoteId));
            put(AnkiDroidHelper.KEY_TAGS, String.format(" %s %s ",
                    suspiciousTag
                            + AnkiDroidHelper.HIERARCHICAL_TAG_SEPARATOR
                            + MusInterval.Fields.SOUND_LARGER
                            + AnkiDroidHelper.HIERARCHICAL_TAG_SEPARATOR
                            + RelatedIntervalSoundField.TAG_POINTING,
                    suspiciousTag
                            + AnkiDroidHelper.HIERARCHICAL_TAG_SEPARATOR
                            + MusInterval.Fields.SOUND_SMALLER
                            + AnkiDroidHelper.HIERARCHICAL_TAG_SEPARATOR
                            + RelatedIntervalSoundField.TAG_POINTED
                    )
            ); // ok but has tags
        }};
        final Map<String, String> noteData = new HashMap<String, String>() {{
            put(MusInterval.Fields.SOUND, noteSound);
            put(MusInterval.Fields.SOUND_SMALLER, smallerNoteSound);
            put(MusInterval.Fields.SOUND_LARGER, largerNoteSound);
            put(MusInterval.Fields.START_NOTE, "C2");
            put(MusInterval.Fields.INTERVAL, "Maj2");
            put(MusInterval.Fields.TIMING, "melodic");
            put(MusInterval.Fields.DIRECTION, "ascending");
            put(MusInterval.Fields.TEMPO, "80");
            put(MusInterval.Fields.INSTRUMENT, "guitar");
            put(AnkiDroidHelper.KEY_ID, String.valueOf(noteId));
            put(AnkiDroidHelper.KEY_TAGS, String.format(" %s %s %s %s ",
                    suspiciousTag
                            + AnkiDroidHelper.HIERARCHICAL_TAG_SEPARATOR
                            + MusInterval.Fields.SOUND_SMALLER
                            + AnkiDroidHelper.HIERARCHICAL_TAG_SEPARATOR
                            + RelatedIntervalSoundField.TAG_POINTING,
                    suspiciousTag
                            + AnkiDroidHelper.HIERARCHICAL_TAG_SEPARATOR
                            + MusInterval.Fields.SOUND_LARGER
                            + AnkiDroidHelper.HIERARCHICAL_TAG_SEPARATOR
                            + RelatedIntervalSoundField.TAG_POINTING,
                    suspiciousTag
                            + AnkiDroidHelper.HIERARCHICAL_TAG_SEPARATOR
                            + MusInterval.Fields.SOUND_SMALLER
                            + AnkiDroidHelper.HIERARCHICAL_TAG_SEPARATOR
                            + RelatedIntervalSoundField.TAG_POINTED,
                    suspiciousTag
                            + AnkiDroidHelper.HIERARCHICAL_TAG_SEPARATOR
                            + MusInterval.Fields.SOUND_LARGER
                            + AnkiDroidHelper.HIERARCHICAL_TAG_SEPARATOR
                            + RelatedIntervalSoundField.TAG_POINTED
                    )
            ); // ok but has tags
        }};
        final Map<String, String> largerNoteData = new HashMap<String, String>() {{
            put(MusInterval.Fields.SOUND, largerNoteSound);
            put(MusInterval.Fields.SOUND_SMALLER, noteSound);
            put(MusInterval.Fields.SOUND_LARGER, "");
            put(MusInterval.Fields.START_NOTE, "C2");
            put(MusInterval.Fields.INTERVAL, "min3");
            put(MusInterval.Fields.TIMING, "melodic");
            put(MusInterval.Fields.DIRECTION, "ascending");
            put(MusInterval.Fields.TEMPO, "80");
            put(MusInterval.Fields.INSTRUMENT, "guitar");
            put(AnkiDroidHelper.KEY_ID, String.valueOf(largerNoteId));
            put(AnkiDroidHelper.KEY_TAGS, String.format(" %s %s ",
                    suspiciousTag
                            + AnkiDroidHelper.HIERARCHICAL_TAG_SEPARATOR
                            + MusInterval.Fields.SOUND_SMALLER
                            + AnkiDroidHelper.HIERARCHICAL_TAG_SEPARATOR
                            + RelatedIntervalSoundField.TAG_POINTING,
                    suspiciousTag
                            + AnkiDroidHelper.HIERARCHICAL_TAG_SEPARATOR
                            + MusInterval.Fields.SOUND_LARGER
                            + AnkiDroidHelper.HIERARCHICAL_TAG_SEPARATOR
                            + RelatedIntervalSoundField.TAG_POINTED
                    )
            ); // ok but has tags
        }};

        final Map<String, String> searchData = new HashMap<String, String>() {{
            put(MusInterval.Fields.START_NOTE, "%%");
            put(MusInterval.Fields.INTERVAL, "%");
            put(MusInterval.Fields.TIMING, "");
            put(MusInterval.Fields.DIRECTION, "");
            put(MusInterval.Fields.TEMPO, "");
            put(MusInterval.Fields.INSTRUMENT, "");
            put(MusInterval.Fields.FIRST_NOTE_DURATION_COEFFICIENT, "");
        }};

        LinkedList<Map<String, String>> searchResult = new LinkedList<Map<String, String>>() {{
            add(smallerNoteData);
            add(noteData);
            add(largerNoteData);
        }};
        doReturn(searchResult).when(helper).findNotes(eq(modelId), eq(new HashMap<String, String>()), any(Map.class), any(Map.class), any(Map.class));
        ArrayList<Map<String, String>> searchDataset = new ArrayList<Map<String, String>>() {{
            add(searchData);
        }};
        doReturn(searchResult).when(helper).findNotes(eq(modelId), eq(searchDataset), any(Map.class), any(Map.class), any(Map.class));

        MusInterval mi = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .notes(null)
                .octaves(null)
                .intervals(null)
                .build();
        ProgressIndicator progressIndicator = mock(ProgressIndicator.class);
        NotesIntegrity.Summary is = new NotesIntegrity(helper, mi, corruptedTag, suspiciousTag, duplicateTag, progressIndicator).check();

        assertEquals(3, is.getNotesCount());
        assertEquals(0, is.getSuspiciousNotesCount());
        assertEquals(8, is.getFixedSuspiciousRelationsCount());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void checkIntegrity_missingLink_shouldFill() throws MusInterval.Exception, AnkiDroidHelper.InvalidAnkiDatabaseException {
        final long modelId = new Random().nextLong();
        final long smallerNoteId = new Random().nextLong();
        final long noteId = new Random().nextLong();
        final long largerNoteId = new Random().nextLong();

        final AnkiDroidHelper helper = mock(AnkiDroidHelper.class);
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(SIGNATURE).when(helper).getFieldList(eq(modelId));

        final String smallerNoteSound = "[sound:dir/file_smaller.mp3]";
        final String noteSound = "[sound:dir/file.mp3]";
        final String largerNoteSound = "[sound:dir/file_larger.mp3]";

        final Map<String, String> smallerNoteData = new HashMap<String, String>() {{
            put(MusInterval.Fields.SOUND, smallerNoteSound);
            put(MusInterval.Fields.SOUND_SMALLER, "");
            put(MusInterval.Fields.SOUND_LARGER, "");
            put(MusInterval.Fields.START_NOTE, "C2");
            put(MusInterval.Fields.INTERVAL, "min2");
            put(MusInterval.Fields.TIMING, "melodic");
            put(MusInterval.Fields.DIRECTION, "ascending");
            put(MusInterval.Fields.TEMPO, "80");
            put(MusInterval.Fields.INSTRUMENT, "guitar");
            put(AnkiDroidHelper.KEY_ID, String.valueOf(smallerNoteId));
            put(AnkiDroidHelper.KEY_TAGS, "");
        }};
        final Map<String, String> noteData = new HashMap<String, String>() {{
            put(MusInterval.Fields.SOUND, noteSound);
            put(MusInterval.Fields.SOUND_SMALLER, "");
            put(MusInterval.Fields.SOUND_LARGER, "");
            put(MusInterval.Fields.START_NOTE, "C2");
            put(MusInterval.Fields.INTERVAL, "Maj2");
            put(MusInterval.Fields.TIMING, "melodic");
            put(MusInterval.Fields.DIRECTION, "ascending");
            put(MusInterval.Fields.TEMPO, "80");
            put(MusInterval.Fields.INSTRUMENT, "guitar");
            put(AnkiDroidHelper.KEY_ID, String.valueOf(noteId));
            put(AnkiDroidHelper.KEY_TAGS, "");
        }};
        final Map<String, String> largerNoteData = new HashMap<String, String>() {{
            put(MusInterval.Fields.SOUND, largerNoteSound);
            put(MusInterval.Fields.SOUND_SMALLER, "");
            put(MusInterval.Fields.SOUND_LARGER, "");
            put(MusInterval.Fields.START_NOTE, "C2");
            put(MusInterval.Fields.INTERVAL, "min3");
            put(MusInterval.Fields.TIMING, "melodic");
            put(MusInterval.Fields.DIRECTION, "ascending");
            put(MusInterval.Fields.TEMPO, "80");
            put(MusInterval.Fields.INSTRUMENT, "guitar");
            put(AnkiDroidHelper.KEY_ID, String.valueOf(largerNoteId));
            put(AnkiDroidHelper.KEY_TAGS, "");
        }};

        final Map<String, String> searchData = new HashMap<String, String>() {{
            put(MusInterval.Fields.START_NOTE, "%%");
            put(MusInterval.Fields.INTERVAL, "%");
            put(MusInterval.Fields.TIMING, "");
            put(MusInterval.Fields.DIRECTION, "");
            put(MusInterval.Fields.TEMPO, "");
            put(MusInterval.Fields.INSTRUMENT, "");
            put(MusInterval.Fields.FIRST_NOTE_DURATION_COEFFICIENT, "");
        }};

        LinkedList<Map<String, String>> searchResult = new LinkedList<Map<String, String>>() {{
            add(smallerNoteData);
            add(noteData);
            add(largerNoteData);
        }};
        doReturn(searchResult).when(helper).findNotes(eq(modelId), eq(new HashMap<String, String>()), any(Map.class), any(Map.class), any(Map.class));
        ArrayList<Map<String, String>> searchDataset = new ArrayList<Map<String, String>>() {{
            add(searchData);
        }};
        doReturn(searchResult).when(helper).findNotes(eq(modelId), eq(searchDataset), any(Map.class), any(Map.class), any(Map.class));
        Map<String, String> smallerNoteKeyData = new HashMap<String, String>(smallerNoteData) {{
            remove(MusInterval.Fields.SOUND);
            remove(MusInterval.Fields.SOUND_SMALLER);
            remove(MusInterval.Fields.SOUND_LARGER);
            remove(AnkiDroidHelper.KEY_ID);
            remove(AnkiDroidHelper.KEY_TAGS);
        }};
        doReturn(
                new LinkedList<Map<String, String>>() {{
                    add(smallerNoteData);
                }}
        ).when(helper).findNotes(eq(modelId), eq(smallerNoteKeyData), any(Map.class), any(Map.class), any(Map.class));
        Map<String, String> noteKeyData = new HashMap<String, String>(noteData) {{
            remove(MusInterval.Fields.SOUND);
            remove(MusInterval.Fields.SOUND_SMALLER);
            remove(MusInterval.Fields.SOUND_LARGER);
            remove(AnkiDroidHelper.KEY_ID);
            remove(AnkiDroidHelper.KEY_TAGS);
        }};
        doReturn(
                new LinkedList<Map<String, String>>() {{
                    add(noteData);
                }}
        ).when(helper).findNotes(eq(modelId), eq(noteKeyData), any(Map.class), any(Map.class), any(Map.class));
        Map<String, String> largerNoteKeyData = new HashMap<String, String>(largerNoteData) {{
            remove(MusInterval.Fields.SOUND);
            remove(MusInterval.Fields.SOUND_SMALLER);
            remove(MusInterval.Fields.SOUND_LARGER);
            remove(AnkiDroidHelper.KEY_ID);
            remove(AnkiDroidHelper.KEY_TAGS);
        }};
        doReturn(
                new LinkedList<Map<String, String>>() {{
                    add(largerNoteData);
                }}
        ).when(helper).findNotes(eq(modelId), eq(largerNoteKeyData), any(Map.class), any(Map.class), any(Map.class));

        MusInterval mi = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .notes(null)
                .octaves(null)
                .intervals(null)
                .build();
        ProgressIndicator progressIndicator = mock(ProgressIndicator.class);
        NotesIntegrity.Summary is = new NotesIntegrity(helper, mi, corruptedTag, suspiciousTag, duplicateTag, progressIndicator).check();

        assertEquals(3, is.getNotesCount());
        assertEquals(4, is.getAutoFilledRelationsCount());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void checkIntegrity_linkToMissing_shouldCount() throws MusInterval.Exception, AnkiDroidHelper.InvalidAnkiDatabaseException {
        final long modelId = new Random().nextLong();
        final long smallerNoteId = new Random().nextLong();
        final long noteId = new Random().nextLong();
        final long largerNoteId = new Random().nextLong();

        final AnkiDroidHelper helper = mock(AnkiDroidHelper.class);
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(SIGNATURE).when(helper).getFieldList(eq(modelId));

        final String smallerNoteSound = "[sound:dir/file_smaller.mp3]";
        final String noteSound = "[sound:dir/file.mp3]";
        final String largerNoteSound = "[sound:dir/file_larger.mp3]";

        final Map<String, String> smallerNoteData = new HashMap<String, String>() {{
            put(MusInterval.Fields.SOUND, smallerNoteSound);
            put(MusInterval.Fields.SOUND_SMALLER, "");
            put(MusInterval.Fields.SOUND_LARGER, noteSound);
            put(MusInterval.Fields.START_NOTE, "C2");
            put(MusInterval.Fields.INTERVAL, "min2");
            put(MusInterval.Fields.TIMING, "melodic");
            put(MusInterval.Fields.DIRECTION, "ascending");
            put(MusInterval.Fields.TEMPO, "80");
            put(MusInterval.Fields.INSTRUMENT, "guitar");
            put(AnkiDroidHelper.KEY_ID, String.valueOf(smallerNoteId));
            put(AnkiDroidHelper.KEY_TAGS, "");
        }};
        final Map<String, String> noteData = new HashMap<String, String>() {{
            put(MusInterval.Fields.SOUND, noteSound);
            put(MusInterval.Fields.SOUND_SMALLER, "[sound:dir/unknown_smaller.mp3]");
            put(MusInterval.Fields.SOUND_LARGER, "[sound:dir/unknown_larger.mp3]");
            put(MusInterval.Fields.START_NOTE, "C2");
            put(MusInterval.Fields.INTERVAL, "Maj2");
            put(MusInterval.Fields.TIMING, "melodic");
            put(MusInterval.Fields.DIRECTION, "ascending");
            put(MusInterval.Fields.TEMPO, "80");
            put(MusInterval.Fields.INSTRUMENT, "guitar");
            put(AnkiDroidHelper.KEY_ID, String.valueOf(noteId));
            put(AnkiDroidHelper.KEY_TAGS, "");
        }};
        final Map<String, String> largerNoteData = new HashMap<String, String>() {{
            put(MusInterval.Fields.SOUND, largerNoteSound);
            put(MusInterval.Fields.SOUND_SMALLER, noteSound);
            put(MusInterval.Fields.SOUND_LARGER, "");
            put(MusInterval.Fields.START_NOTE, "C2");
            put(MusInterval.Fields.INTERVAL, "min3");
            put(MusInterval.Fields.TIMING, "melodic");
            put(MusInterval.Fields.DIRECTION, "ascending");
            put(MusInterval.Fields.TEMPO, "80");
            put(MusInterval.Fields.INSTRUMENT, "guitar");
            put(AnkiDroidHelper.KEY_ID, String.valueOf(largerNoteId));
            put(AnkiDroidHelper.KEY_TAGS, "");
        }};

        final Map<String, String> searchData = new HashMap<String, String>() {{
            put(MusInterval.Fields.START_NOTE, "%%");
            put(MusInterval.Fields.INTERVAL, "%");
            put(MusInterval.Fields.TIMING, "");
            put(MusInterval.Fields.DIRECTION, "");
            put(MusInterval.Fields.TEMPO, "");
            put(MusInterval.Fields.INSTRUMENT, "");
            put(MusInterval.Fields.FIRST_NOTE_DURATION_COEFFICIENT, "");
        }};

        LinkedList<Map<String, String>> searchResult = new LinkedList<Map<String, String>>() {{
            add(smallerNoteData);
            add(noteData);
            add(largerNoteData);
        }};
        doReturn(searchResult).when(helper).findNotes(eq(modelId), eq(new HashMap<String, String>()), any(Map.class), any(Map.class), any(Map.class));
        ArrayList<Map<String, String>> searchDataset = new ArrayList<Map<String, String>>() {{
            add(searchData);
        }};
        doReturn(searchResult).when(helper).findNotes(eq(modelId), eq(searchDataset), any(Map.class), any(Map.class), any(Map.class));
        Map<String, String> smallerNoteKeyData = new HashMap<String, String>(smallerNoteData) {{
            remove(MusInterval.Fields.SOUND);
            remove(MusInterval.Fields.SOUND_SMALLER);
            remove(MusInterval.Fields.SOUND_LARGER);
            remove(AnkiDroidHelper.KEY_ID);
            remove(AnkiDroidHelper.KEY_TAGS);
        }};
        doReturn(
                new LinkedList<Map<String, String>>() {{
                    add(smallerNoteData);
                }}
        ).when(helper).findNotes(eq(modelId), eq(smallerNoteKeyData), any(Map.class), any(Map.class), any(Map.class));
        Map<String, String> noteKeyData = new HashMap<String, String>(noteData) {{
            remove(MusInterval.Fields.SOUND);
            remove(MusInterval.Fields.SOUND_SMALLER);
            remove(MusInterval.Fields.SOUND_LARGER);
            remove(AnkiDroidHelper.KEY_ID);
            remove(AnkiDroidHelper.KEY_TAGS);
        }};
        doReturn(
                new LinkedList<Map<String, String>>() {{
                    add(noteData);
                }}
        ).when(helper).findNotes(eq(modelId), eq(noteKeyData), any(Map.class), any(Map.class), any(Map.class));
        Map<String, String> largerNoteKeyData = new HashMap<String, String>(largerNoteData) {{
            remove(MusInterval.Fields.SOUND);
            remove(MusInterval.Fields.SOUND_SMALLER);
            remove(MusInterval.Fields.SOUND_LARGER);
            remove(AnkiDroidHelper.KEY_ID);
            remove(AnkiDroidHelper.KEY_TAGS);
        }};
        doReturn(
                new LinkedList<Map<String, String>>() {{
                    add(largerNoteData);
                }}
        ).when(helper).findNotes(eq(modelId), eq(largerNoteKeyData), any(Map.class), any(Map.class), any(Map.class));

        MusInterval mi = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .notes(null)
                .octaves(null)
                .intervals(null)
                .build();
        ProgressIndicator progressIndicator = mock(ProgressIndicator.class);
        NotesIntegrity.Summary is = new NotesIntegrity(helper, mi, corruptedTag, suspiciousTag, duplicateTag, progressIndicator).check();

        assertEquals(3, is.getNotesCount());
        assertEquals(0, is.getAutoFilledRelationsCount());
        assertEquals(1, is.getSuspiciousNotesCount());
        Map<String, Integer> suspiciousFieldCounts = is.getSuspiciousFieldCounts();
        assertEquals(new Integer(1), suspiciousFieldCounts.get(MusInterval.Fields.SOUND_SMALLER));
        assertEquals(new Integer(1), suspiciousFieldCounts.get(MusInterval.Fields.SOUND_LARGER));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void checkIntegrity_Duplicate_ShouldCount() throws MusInterval.Exception, AnkiDroidHelper.InvalidAnkiDatabaseException {
        final long modelId = new Random().nextLong();
        final long noteId = new Random().nextLong();
        final long duplicateNoteId = new Random().nextLong();

        final AnkiDroidHelper helper = mock(AnkiDroidHelper.class);
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(SIGNATURE).when(helper).getFieldList(eq(modelId));

        final Map<String, String> noteData = new HashMap<String, String>() {{
            put(MusInterval.Fields.SOUND, "[sound:dir/file.mp3]");
            put(MusInterval.Fields.START_NOTE, "C3");
            put(MusInterval.Fields.INTERVAL, "min2");
            put(MusInterval.Fields.TIMING, "melodic");
            put(MusInterval.Fields.DIRECTION, "ascending");
            put(MusInterval.Fields.TEMPO, "80");
            put(MusInterval.Fields.INSTRUMENT, "guitar");
            put(AnkiDroidHelper.KEY_ID, String.valueOf(noteId));
            put(AnkiDroidHelper.KEY_TAGS, "");
        }};

        final Map<String, String> duplicateNoteData = new HashMap<String, String>() {{
            put(MusInterval.Fields.SOUND, "[sound:dir/file.mp3]");
            put(MusInterval.Fields.START_NOTE, "C3");
            put(MusInterval.Fields.INTERVAL, "min2");
            put(MusInterval.Fields.TIMING, "melodic");
            put(MusInterval.Fields.DIRECTION, "ascending");
            put(MusInterval.Fields.TEMPO, "80");
            put(MusInterval.Fields.INSTRUMENT, "guitar");
            put(AnkiDroidHelper.KEY_ID, String.valueOf(duplicateNoteId));
            put(AnkiDroidHelper.KEY_TAGS, "");
        }};

        final Map<String, String> searchData = new HashMap<String, String>() {{
            put(MusInterval.Fields.START_NOTE, "%%");
            put(MusInterval.Fields.INTERVAL, "%");
            put(MusInterval.Fields.TIMING, "");
            put(MusInterval.Fields.DIRECTION, "");
            put(MusInterval.Fields.TEMPO, "");
            put(MusInterval.Fields.INSTRUMENT, "");
            put(MusInterval.Fields.FIRST_NOTE_DURATION_COEFFICIENT, "");
        }};

        LinkedList<Map<String, String>> searchResult = new LinkedList<Map<String, String>>() {{
            add(noteData);
            add(duplicateNoteData);
        }};
        doReturn(searchResult).when(helper).findNotes(eq(modelId), eq(new HashMap<String, String>()), any(Map.class), any(Map.class), any(Map.class));
        ArrayList<Map<String, String>> searchDataSet = new ArrayList<Map<String, String>>() {{
            add(searchData);
        }};
        doReturn(searchResult).when(helper).findNotes(eq(modelId), eq(searchDataSet), any(Map.class), any(Map.class), any(Map.class));

        MusInterval mi = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .notes(null)
                .octaves(null)
                .intervals(null)
                .build();
        ProgressIndicator progressIndicator = mock(ProgressIndicator.class);
        NotesIntegrity.Summary is = new NotesIntegrity(helper, mi, corruptedTag, suspiciousTag, duplicateTag, progressIndicator).check();

        assertEquals(2, is.getDuplicateNotesCount());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void checkIntegrity_Duplicate_ShouldCountFixed() throws MusInterval.Exception, AnkiDroidHelper.InvalidAnkiDatabaseException {
        final long modelId = new Random().nextLong();
        final long noteId = new Random().nextLong();
        final long duplicateNoteId = new Random().nextLong();

        final AnkiDroidHelper helper = mock(AnkiDroidHelper.class);
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(SIGNATURE).when(helper).getFieldList(eq(modelId));

        final Map<String, String> noteData = new HashMap<String, String>() {{
            put(MusInterval.Fields.SOUND, "[sound:dir/file.mp3]");
            put(MusInterval.Fields.START_NOTE, "C3");
            put(MusInterval.Fields.INTERVAL, "min2");
            put(MusInterval.Fields.TIMING, "melodic");
            put(MusInterval.Fields.DIRECTION, "ascending");
            put(MusInterval.Fields.TEMPO, "80");
            put(MusInterval.Fields.INSTRUMENT, "guitar");
            put(AnkiDroidHelper.KEY_ID, String.valueOf(noteId));
            put(AnkiDroidHelper.KEY_TAGS, "");
        }};

        final Map<String, String> duplicateNoteData = new HashMap<String, String>() {{
            put(MusInterval.Fields.SOUND, "[sound:dir/file.mp3]");
            put(MusInterval.Fields.START_NOTE, "C3");
            put(MusInterval.Fields.INTERVAL, "min2");
            put(MusInterval.Fields.TIMING, "melodic");
            put(MusInterval.Fields.DIRECTION, "ascending");
            put(MusInterval.Fields.TEMPO, "80");
            put(MusInterval.Fields.INSTRUMENT, "guitar");
            put(AnkiDroidHelper.KEY_ID, String.valueOf(duplicateNoteId));
            put(AnkiDroidHelper.KEY_TAGS, "");
        }};

        final Map<String, String> searchData = new HashMap<String, String>() {{
            put(MusInterval.Fields.START_NOTE, "%%");
            put(MusInterval.Fields.INTERVAL, "%");
            put(MusInterval.Fields.TIMING, "");
            put(MusInterval.Fields.DIRECTION, "");
            put(MusInterval.Fields.TEMPO, "");
            put(MusInterval.Fields.INSTRUMENT, "");
            put(MusInterval.Fields.FIRST_NOTE_DURATION_COEFFICIENT, "");
        }};

        final LinkedList<Map<String, String>> searchResult = new LinkedList<Map<String, String>>() {{
            add(noteData);
            add(duplicateNoteData);
        }};
        doReturn(searchResult).when(helper).findNotes(eq(modelId), eq(new HashMap<String, String>()), any(Map.class), any(Map.class), any(Map.class));
        ArrayList<Map<String, String>> searchDataSet = new ArrayList<Map<String, String>>() {{
            add(searchData);
        }};
        doReturn(searchResult).when(helper).findNotes(eq(modelId), eq(searchDataSet), any(Map.class), any(Map.class), any(Map.class));

        doAnswer(new Answer() {
            @Override
            public Integer answer(InvocationOnMock invocation) {
                long id = invocation.getArgument(0);
                String tag = invocation.getArgument(1);
                for (Map<String, String> data : searchResult) {
                    if (Long.parseLong(data.get(AnkiDroidHelper.KEY_ID)) == id) {
                        String current = data.get(AnkiDroidHelper.KEY_TAGS);
                        data.put(AnkiDroidHelper.KEY_TAGS, current + tag);
                        break;
                    }
                }
                return 1;
            }
        }).when(helper).addTagToNote(any(Long.class), any(String.class));

        MusInterval mi = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .notes(null)
                .octaves(null)
                .intervals(null)
                .build();
        ProgressIndicator progressIndicator = mock(ProgressIndicator.class);
        new NotesIntegrity(helper, mi, corruptedTag, suspiciousTag, duplicateTag, progressIndicator).check();

        assertTrue(noteData.get(AnkiDroidHelper.KEY_TAGS).contains(duplicateTag));
        assertTrue(duplicateNoteData.get(AnkiDroidHelper.KEY_TAGS).contains(duplicateTag));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void checkIntegrity_DuplicateFixed_ShouldRemoveTag() throws MusInterval.Exception, AnkiDroidHelper.InvalidAnkiDatabaseException {
        final long modelId = new Random().nextLong();
        final long noteId = new Random().nextLong();
        final long anotherNoteId = new Random().nextLong();

        final AnkiDroidHelper helper = mock(AnkiDroidHelper.class);
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(SIGNATURE).when(helper).getFieldList(eq(modelId));

        final Map<String, String> noteData = new HashMap<String, String>() {{
            put(MusInterval.Fields.SOUND, "[sound:dir/file.mp3]");
            put(MusInterval.Fields.START_NOTE, "B3");
            put(MusInterval.Fields.INTERVAL, "min2");
            put(MusInterval.Fields.TIMING, "melodic");
            put(MusInterval.Fields.DIRECTION, "ascending");
            put(MusInterval.Fields.TEMPO, "80");
            put(MusInterval.Fields.INSTRUMENT, "guitar");
            put(AnkiDroidHelper.KEY_ID, String.valueOf(noteId));
            put(AnkiDroidHelper.KEY_TAGS, String.format(" %s ", duplicateTag));
        }};

        final Map<String, String> anotherNoteData = new HashMap<String, String>() {{
            put(MusInterval.Fields.SOUND, "[sound:dir/file.mp3]");
            put(MusInterval.Fields.START_NOTE, "C4");
            put(MusInterval.Fields.INTERVAL, "Maj2");
            put(MusInterval.Fields.TIMING, "melodic");
            put(MusInterval.Fields.DIRECTION, "ascending");
            put(MusInterval.Fields.TEMPO, "80");
            put(MusInterval.Fields.INSTRUMENT, "guitar");
            put(AnkiDroidHelper.KEY_ID, String.valueOf(anotherNoteId));
            put(AnkiDroidHelper.KEY_TAGS, String.format(" %s ", duplicateTag));
        }};

        final Map<String, String> searchData = new HashMap<String, String>() {{
            put(MusInterval.Fields.START_NOTE, "%%");
            put(MusInterval.Fields.INTERVAL, "%");
            put(MusInterval.Fields.TIMING, "");
            put(MusInterval.Fields.DIRECTION, "");
            put(MusInterval.Fields.TEMPO, "");
            put(MusInterval.Fields.INSTRUMENT, "");
            put(MusInterval.Fields.FIRST_NOTE_DURATION_COEFFICIENT, "");
        }};

        final LinkedList<Map<String, String>> searchResult = new LinkedList<Map<String, String>>() {{
            add(noteData);
            add(anotherNoteData);
        }};
        doReturn(searchResult).when(helper).findNotes(eq(modelId), eq(new HashMap<String, String>()), any(Map.class), any(Map.class), any(Map.class));
        ArrayList<Map<String, String>> searchDataSet = new ArrayList<Map<String, String>>() {{
            add(searchData);
        }};
        doReturn(searchResult).when(helper).findNotes(eq(modelId), eq(searchDataSet), any(Map.class), any(Map.class), any(Map.class));

        doAnswer(new Answer() {
            @Override
            public Boolean answer(InvocationOnMock invocation) {
                long id = invocation.getArgument(0);
                String tags = invocation.getArgument(1);
                for (Map<String, String> data : searchResult) {
                    if (Long.parseLong(data.get(AnkiDroidHelper.KEY_ID)) == id) {
                        data.put(AnkiDroidHelper.KEY_TAGS, tags);
                        return true;
                    }
                }
                return false;
            }
        }).when(helper).updateNoteTags(any(Long.class), any(String.class));

        MusInterval mi = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .notes(null)
                .octaves(null)
                .intervals(null)
                .build();
        ProgressIndicator progressIndicator = mock(ProgressIndicator.class);
        new NotesIntegrity(helper, mi, corruptedTag, suspiciousTag, duplicateTag, progressIndicator).check();

        assertFalse(noteData.get(AnkiDroidHelper.KEY_TAGS).contains(duplicateTag));
        assertFalse(anotherNoteData.get(AnkiDroidHelper.KEY_TAGS).contains(duplicateTag));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void checkIntegrity_DifferentFirstNoteDurationCoefficient_ShouldNotCountAsDuplicates() throws AnkiDroidHelper.InvalidAnkiDatabaseException, MusInterval.ValidationException {
        final long modelId = new Random().nextLong();
        final long noteId = new Random().nextLong();
        final long anotherNoteId = new Random().nextLong();

        final AnkiDroidHelper helper = mock(AnkiDroidHelper.class);
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(SIGNATURE).when(helper).getFieldList(eq(modelId));

        final Map<String, String> noteData = new HashMap<String, String>() {{
            put(MusInterval.Fields.SOUND, "[sound:dir/file.mp3]");
            put(MusInterval.Fields.START_NOTE, "C3");
            put(MusInterval.Fields.INTERVAL, "min2");
            put(MusInterval.Fields.TIMING, "melodic");
            put(MusInterval.Fields.DIRECTION, "ascending");
            put(MusInterval.Fields.TEMPO, "80");
            put(MusInterval.Fields.INSTRUMENT, "guitar");
            put(MusInterval.Fields.FIRST_NOTE_DURATION_COEFFICIENT, "2.0");
            put(AnkiDroidHelper.KEY_ID, String.valueOf(noteId));
            put(AnkiDroidHelper.KEY_TAGS, "");
        }};

        final Map<String, String> anotherNoteData = new HashMap<String, String>() {{
            put(MusInterval.Fields.SOUND, "[sound:dir/file.mp3]");
            put(MusInterval.Fields.START_NOTE, "C3");
            put(MusInterval.Fields.INTERVAL, "min2");
            put(MusInterval.Fields.TIMING, "melodic");
            put(MusInterval.Fields.DIRECTION, "ascending");
            put(MusInterval.Fields.TEMPO, "80");
            put(MusInterval.Fields.INSTRUMENT, "guitar");
            put(MusInterval.Fields.FIRST_NOTE_DURATION_COEFFICIENT, "1");
            put(AnkiDroidHelper.KEY_ID, String.valueOf(anotherNoteId));
            put(AnkiDroidHelper.KEY_TAGS, "");
        }};

        final Map<String, String> searchData = new HashMap<String, String>() {{
            put(MusInterval.Fields.START_NOTE, "%%");
            put(MusInterval.Fields.INTERVAL, "%");
            put(MusInterval.Fields.TIMING, "");
            put(MusInterval.Fields.DIRECTION, "");
            put(MusInterval.Fields.TEMPO, "");
            put(MusInterval.Fields.INSTRUMENT, "");
            put(MusInterval.Fields.FIRST_NOTE_DURATION_COEFFICIENT, "");
        }};

        LinkedList<Map<String, String>> searchResult = new LinkedList<Map<String, String>>() {{
            add(noteData);
            add(anotherNoteData);
        }};
        doReturn(searchResult).when(helper).findNotes(eq(modelId), eq(new HashMap<String, String>()), any(Map.class), any(Map.class), any(Map.class));
        ArrayList<Map<String, String>> searchDataSet = new ArrayList<Map<String, String>>() {{
            add(searchData);
        }};
        doReturn(searchResult).when(helper).findNotes(eq(modelId), eq(searchDataSet), any(Map.class), any(Map.class), any(Map.class));

        MusInterval mi = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .notes(null)
                .octaves(null)
                .intervals(null)
                .build();
        ProgressIndicator progressIndicator = mock(ProgressIndicator.class);
        NotesIntegrity.Summary is = new NotesIntegrity(helper, mi, corruptedTag, suspiciousTag, duplicateTag, progressIndicator).check();

        assertEquals(2, is.getNotesCount());
        assertEquals(0, is.getDuplicateNotesCount());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void checkIntegrity_InvalidFirstNoteDurationCoefficient_ShouldCountAsCorrupted() throws AnkiDroidHelper.InvalidAnkiDatabaseException, MusInterval.ValidationException {
        final long modelId = new Random().nextLong();
        final long corruptedNoteId = new Random().nextLong();

        final AnkiDroidHelper helper = mock(AnkiDroidHelper.class);
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(SIGNATURE).when(helper).getFieldList(eq(modelId));

        final Map<String, String> corruptedNoteData = new HashMap<String, String>() {{
            put(MusInterval.Fields.SOUND, "[sound:dir/file.mp3]");
            put(MusInterval.Fields.START_NOTE, "C3");
            put(MusInterval.Fields.INTERVAL, "min2");
            put(MusInterval.Fields.TIMING, "melodic");
            put(MusInterval.Fields.DIRECTION, "ascending");
            put(MusInterval.Fields.TEMPO, "80");
            put(MusInterval.Fields.INSTRUMENT, "guitar");
            put(MusInterval.Fields.FIRST_NOTE_DURATION_COEFFICIENT, "1,0"); // invalid separator
            put(AnkiDroidHelper.KEY_ID, String.valueOf(corruptedNoteId));
            put(AnkiDroidHelper.KEY_TAGS, "");
        }};

        final Map<String, String> searchData = new HashMap<String, String>() {{
            put(MusInterval.Fields.START_NOTE, "%%");
            put(MusInterval.Fields.INTERVAL, "%");
            put(MusInterval.Fields.TIMING, "");
            put(MusInterval.Fields.DIRECTION, "");
            put(MusInterval.Fields.TEMPO, "");
            put(MusInterval.Fields.INSTRUMENT, "");
            put(MusInterval.Fields.FIRST_NOTE_DURATION_COEFFICIENT, "");
        }};

        LinkedList<Map<String, String>> searchResult = new LinkedList<Map<String, String>>() {{
            add(corruptedNoteData);
        }};
        doReturn(searchResult).when(helper).findNotes(eq(modelId), eq(new HashMap<String, String>()), any(Map.class), any(Map.class), any(Map.class));
        ArrayList<Map<String, String>> searchDataset = new ArrayList<Map<String, String>>() {{
            add(searchData);
        }};
        doReturn(searchResult).when(helper).findNotes(eq(modelId), eq(searchDataset), any(Map.class), any(Map.class), any(Map.class));

        MusInterval mi = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .notes(null)
                .octaves(null)
                .intervals(null)
                .build();
        ProgressIndicator progressIndicator = mock(ProgressIndicator.class);
        NotesIntegrity.Summary is = new NotesIntegrity(helper, mi, corruptedTag, suspiciousTag, duplicateTag, progressIndicator).check();

        assertEquals(1, is.getNotesCount());
        assertEquals(1, is.getCorruptedNotesCount());
        final Map<String, Integer> corruptedFieldCounts = is.getCorruptedFieldCounts();
        assertEquals(new Integer(1), corruptedFieldCounts.get(MusInterval.Fields.FIRST_NOTE_DURATION_COEFFICIENT));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void checkIntegrity_DefaultAndEmptyFirstNoteDurationCoefficient_ShouldCountAsDuplicates() throws AnkiDroidHelper.InvalidAnkiDatabaseException, MusInterval.ValidationException {
        final long modelId = new Random().nextLong();
        final long noteId = new Random().nextLong();
        final long duplicateNoteId = new Random().nextLong();

        final AnkiDroidHelper helper = mock(AnkiDroidHelper.class);
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(SIGNATURE).when(helper).getFieldList(eq(modelId));

        final Map<String, String> noteData = new HashMap<String, String>() {{
            put(MusInterval.Fields.SOUND, "[sound:dir/file.mp3]");
            put(MusInterval.Fields.START_NOTE, "C3");
            put(MusInterval.Fields.INTERVAL, "min2");
            put(MusInterval.Fields.TIMING, "melodic");
            put(MusInterval.Fields.DIRECTION, "ascending");
            put(MusInterval.Fields.TEMPO, "80");
            put(MusInterval.Fields.INSTRUMENT, "guitar");
            put(MusInterval.Fields.FIRST_NOTE_DURATION_COEFFICIENT, String.valueOf(MusInterval.Fields.FirstNoteDurationCoefficient.DEFAULT_VALUE));
            put(AnkiDroidHelper.KEY_ID, String.valueOf(noteId));
            put(AnkiDroidHelper.KEY_TAGS, "");
        }};

        final Map<String, String> duplicateNoteData = new HashMap<String, String>() {{
            put(MusInterval.Fields.SOUND, "[sound:dir/file.mp3]");
            put(MusInterval.Fields.START_NOTE, "C3");
            put(MusInterval.Fields.INTERVAL, "min2");
            put(MusInterval.Fields.TIMING, "melodic");
            put(MusInterval.Fields.DIRECTION, "ascending");
            put(MusInterval.Fields.TEMPO, "80");
            put(MusInterval.Fields.INSTRUMENT, "guitar");
            put(MusInterval.Fields.FIRST_NOTE_DURATION_COEFFICIENT, ""); // empty value should be interpreted as default
            put(AnkiDroidHelper.KEY_ID, String.valueOf(duplicateNoteId));
            put(AnkiDroidHelper.KEY_TAGS, "");
        }};

        final Map<String, String> searchData = new HashMap<String, String>() {{
            put(MusInterval.Fields.START_NOTE, "%%");
            put(MusInterval.Fields.INTERVAL, "%");
            put(MusInterval.Fields.TIMING, "");
            put(MusInterval.Fields.DIRECTION, "");
            put(MusInterval.Fields.TEMPO, "");
            put(MusInterval.Fields.INSTRUMENT, "");
            put(MusInterval.Fields.FIRST_NOTE_DURATION_COEFFICIENT, "");
        }};

        LinkedList<Map<String, String>> searchResult = new LinkedList<Map<String, String>>() {{
            add(noteData);
            add(duplicateNoteData);
        }};
        doReturn(searchResult).when(helper).findNotes(eq(modelId), eq(new HashMap<String, String>()), any(Map.class), any(Map.class), any(Map.class));
        ArrayList<Map<String, String>> searchDataSet = new ArrayList<Map<String, String>>() {{
            add(searchData);
        }};
        doReturn(searchResult).when(helper).findNotes(eq(modelId), eq(searchDataSet), any(Map.class), any(Map.class), any(Map.class));

        MusInterval mi = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .notes(null)
                .octaves(null)
                .intervals(null)
                .build();
        ProgressIndicator progressIndicator = mock(ProgressIndicator.class);
        NotesIntegrity.Summary is = new NotesIntegrity(helper, mi, corruptedTag, suspiciousTag, duplicateTag, progressIndicator).check();

        assertEquals(2, is.getNotesCount());
        assertEquals(2, is.getDuplicateNotesCount());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void checkIntegrity_DefaultAndEmptyFirstNoteDurationCoefficientMissingLink_ShouldFill() throws MusInterval.Exception, AnkiDroidHelper.InvalidAnkiDatabaseException {
        final long modelId = new Random().nextLong();
        final long smallerNoteId = new Random().nextLong();
        final long noteId = new Random().nextLong();
        final long largerNoteId = new Random().nextLong();

        final AnkiDroidHelper helper = mock(AnkiDroidHelper.class);
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(SIGNATURE).when(helper).getFieldList(eq(modelId));

        final String smallerNoteSound = "[sound:dir/file_smaller.mp3]";
        final String noteSound = "[sound:dir/file.mp3]";
        final String largerNoteSound = "[sound:dir/file_larger.mp3]";

        final Map<String, String> smallerNoteData = new HashMap<String, String>() {{
            put(MusInterval.Fields.SOUND, smallerNoteSound);
            put(MusInterval.Fields.SOUND_SMALLER, "");
            put(MusInterval.Fields.SOUND_LARGER, "");
            put(MusInterval.Fields.START_NOTE, "C2");
            put(MusInterval.Fields.INTERVAL, "min2");
            put(MusInterval.Fields.TIMING, "melodic");
            put(MusInterval.Fields.DIRECTION, "ascending");
            put(MusInterval.Fields.TEMPO, "80");
            put(MusInterval.Fields.INSTRUMENT, "guitar");
            put(MusInterval.Fields.FIRST_NOTE_DURATION_COEFFICIENT, "");
            put(AnkiDroidHelper.KEY_ID, String.valueOf(smallerNoteId));
            put(AnkiDroidHelper.KEY_TAGS, "");
        }};
        final Map<String, String> noteData = new HashMap<String, String>() {{
            put(MusInterval.Fields.SOUND, noteSound);
            put(MusInterval.Fields.SOUND_SMALLER, "");
            put(MusInterval.Fields.SOUND_LARGER, "");
            put(MusInterval.Fields.START_NOTE, "C2");
            put(MusInterval.Fields.INTERVAL, "Maj2");
            put(MusInterval.Fields.TIMING, "melodic");
            put(MusInterval.Fields.DIRECTION, "ascending");
            put(MusInterval.Fields.TEMPO, "80");
            put(MusInterval.Fields.INSTRUMENT, "guitar");
            put(MusInterval.Fields.FIRST_NOTE_DURATION_COEFFICIENT, String.valueOf(MusInterval.Fields.FirstNoteDurationCoefficient.DEFAULT_VALUE));
            put(AnkiDroidHelper.KEY_ID, String.valueOf(noteId));
            put(AnkiDroidHelper.KEY_TAGS, "");
        }};
        final Map<String, String> largerNoteData = new HashMap<String, String>() {{
            put(MusInterval.Fields.SOUND, largerNoteSound);
            put(MusInterval.Fields.SOUND_SMALLER, "");
            put(MusInterval.Fields.SOUND_LARGER, "");
            put(MusInterval.Fields.START_NOTE, "C2");
            put(MusInterval.Fields.INTERVAL, "min3");
            put(MusInterval.Fields.TIMING, "melodic");
            put(MusInterval.Fields.DIRECTION, "ascending");
            put(MusInterval.Fields.TEMPO, "80");
            put(MusInterval.Fields.INSTRUMENT, "guitar");
            put(MusInterval.Fields.FIRST_NOTE_DURATION_COEFFICIENT, "");
            put(AnkiDroidHelper.KEY_ID, String.valueOf(largerNoteId));
            put(AnkiDroidHelper.KEY_TAGS, "");
        }};

        final Map<String, String> searchData = new HashMap<String, String>() {{
            put(MusInterval.Fields.START_NOTE, "%%");
            put(MusInterval.Fields.INTERVAL, "%");
            put(MusInterval.Fields.TIMING, "");
            put(MusInterval.Fields.DIRECTION, "");
            put(MusInterval.Fields.TEMPO, "");
            put(MusInterval.Fields.INSTRUMENT, "");
            put(MusInterval.Fields.FIRST_NOTE_DURATION_COEFFICIENT, "");
        }};

        LinkedList<Map<String, String>> searchResult = new LinkedList<Map<String, String>>() {{
            add(smallerNoteData);
            add(noteData);
            add(largerNoteData);
        }};
        doReturn(searchResult).when(helper).findNotes(eq(modelId), eq(new HashMap<String, String>()), any(Map.class), any(Map.class), any(Map.class));
        ArrayList<Map<String, String>> searchDataset = new ArrayList<Map<String, String>>() {{
            add(searchData);
        }};
        doReturn(searchResult).when(helper).findNotes(eq(modelId), eq(searchDataset), any(Map.class), any(Map.class), any(Map.class));
        Map<String, String> smallerNoteKeyData = new HashMap<String, String>(smallerNoteData) {{
            remove(MusInterval.Fields.SOUND);
            remove(MusInterval.Fields.SOUND_SMALLER);
            remove(MusInterval.Fields.SOUND_LARGER);
            replace(MusInterval.Fields.FIRST_NOTE_DURATION_COEFFICIENT, String.valueOf(MusInterval.Fields.FirstNoteDurationCoefficient.DEFAULT_VALUE));
            remove(AnkiDroidHelper.KEY_ID);
            remove(AnkiDroidHelper.KEY_TAGS);
        }};
        doReturn(
                new LinkedList<Map<String, String>>() {{
                    add(smallerNoteData);
                }}
        ).when(helper).findNotes(eq(modelId), eq(smallerNoteKeyData), any(Map.class), any(Map.class), any(Map.class));
        Map<String, String> noteKeyData = new HashMap<String, String>(noteData) {{
            remove(MusInterval.Fields.SOUND);
            remove(MusInterval.Fields.SOUND_SMALLER);
            remove(MusInterval.Fields.SOUND_LARGER);
            replace(MusInterval.Fields.FIRST_NOTE_DURATION_COEFFICIENT, "");
            remove(AnkiDroidHelper.KEY_ID);
            remove(AnkiDroidHelper.KEY_TAGS);
        }};
        doReturn(
                new LinkedList<Map<String, String>>() {{
                    add(noteData);
                }}
        ).when(helper).findNotes(eq(modelId), eq(noteKeyData), any(Map.class), any(Map.class), any(Map.class));
        Map<String, String> largerNoteKeyData = new HashMap<String, String>(largerNoteData) {{
            remove(MusInterval.Fields.SOUND);
            remove(MusInterval.Fields.SOUND_SMALLER);
            remove(MusInterval.Fields.SOUND_LARGER);
            replace(MusInterval.Fields.FIRST_NOTE_DURATION_COEFFICIENT, String.valueOf(MusInterval.Fields.FirstNoteDurationCoefficient.DEFAULT_VALUE));
            remove(AnkiDroidHelper.KEY_ID);
            remove(AnkiDroidHelper.KEY_TAGS);

        }};
        doReturn(
                new LinkedList<Map<String, String>>() {{
                    add(largerNoteData);
                }}
        ).when(helper).findNotes(eq(modelId), eq(largerNoteKeyData), any(Map.class), any(Map.class), any(Map.class));

        MusInterval mi = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .notes(null)
                .octaves(null)
                .intervals(null)
                .build();
        ProgressIndicator progressIndicator = mock(ProgressIndicator.class);
        NotesIntegrity.Summary is = new NotesIntegrity(helper, mi, corruptedTag, suspiciousTag, duplicateTag, progressIndicator).check();

        assertEquals(3, is.getNotesCount());
        assertEquals(4, is.getAutoFilledRelationsCount());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void checkIntegrity_DefaultAndEmptyFirstNoteDurationCoefficientLink_ShouldNotCountAsSuspicious() throws MusInterval.Exception, AnkiDroidHelper.InvalidAnkiDatabaseException {
        final long modelId = new Random().nextLong();
        final long smallerNoteId = new Random().nextLong();
        final long noteId = new Random().nextLong();
        final long largerNoteId = new Random().nextLong();

        final AnkiDroidHelper helper = mock(AnkiDroidHelper.class);
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(SIGNATURE).when(helper).getFieldList(eq(modelId));

        final String smallerNoteSound = "[sound:dir/file_smaller.mp3]";
        final String noteSound = "[sound:dir/file.mp3]";
        final String largerNoteSound = "[sound:dir/file_larger.mp3]";

        final Map<String, String> smallerNoteData = new HashMap<String, String>() {{
            put(MusInterval.Fields.SOUND, smallerNoteSound);
            put(MusInterval.Fields.SOUND_SMALLER, "");
            put(MusInterval.Fields.SOUND_LARGER, noteSound);
            put(MusInterval.Fields.START_NOTE, "C2");
            put(MusInterval.Fields.INTERVAL, "min2");
            put(MusInterval.Fields.TIMING, "melodic");
            put(MusInterval.Fields.DIRECTION, "ascending");
            put(MusInterval.Fields.TEMPO, "80");
            put(MusInterval.Fields.INSTRUMENT, "guitar");
            put(MusInterval.Fields.FIRST_NOTE_DURATION_COEFFICIENT, "");
            put(AnkiDroidHelper.KEY_ID, String.valueOf(smallerNoteId));
            put(AnkiDroidHelper.KEY_TAGS, "");
        }};
        final Map<String, String> noteData = new HashMap<String, String>() {{
            put(MusInterval.Fields.SOUND, noteSound);
            put(MusInterval.Fields.SOUND_SMALLER, smallerNoteSound);
            put(MusInterval.Fields.SOUND_LARGER, largerNoteSound);
            put(MusInterval.Fields.START_NOTE, "C2");
            put(MusInterval.Fields.INTERVAL, "Maj2");
            put(MusInterval.Fields.TIMING, "melodic");
            put(MusInterval.Fields.DIRECTION, "ascending");
            put(MusInterval.Fields.TEMPO, "80");
            put(MusInterval.Fields.INSTRUMENT, "guitar");
            put(MusInterval.Fields.FIRST_NOTE_DURATION_COEFFICIENT, String.valueOf(MusInterval.Fields.FirstNoteDurationCoefficient.DEFAULT_VALUE));
            put(AnkiDroidHelper.KEY_ID, String.valueOf(noteId));
            put(AnkiDroidHelper.KEY_TAGS, "");
        }};
        final Map<String, String> largerNoteData = new HashMap<String, String>() {{
            put(MusInterval.Fields.SOUND, largerNoteSound);
            put(MusInterval.Fields.SOUND_SMALLER, noteSound);
            put(MusInterval.Fields.SOUND_LARGER, "");
            put(MusInterval.Fields.START_NOTE, "C2");
            put(MusInterval.Fields.INTERVAL, "min3");
            put(MusInterval.Fields.TIMING, "melodic");
            put(MusInterval.Fields.DIRECTION, "ascending");
            put(MusInterval.Fields.TEMPO, "80");
            put(MusInterval.Fields.INSTRUMENT, "guitar");
            put(MusInterval.Fields.FIRST_NOTE_DURATION_COEFFICIENT, "");
            put(AnkiDroidHelper.KEY_ID, String.valueOf(largerNoteId));
            put(AnkiDroidHelper.KEY_TAGS, "");
        }};

        final Map<String, String> searchData = new HashMap<String, String>() {{
            put(MusInterval.Fields.START_NOTE, "%%");
            put(MusInterval.Fields.INTERVAL, "%");
            put(MusInterval.Fields.TIMING, "");
            put(MusInterval.Fields.DIRECTION, "");
            put(MusInterval.Fields.TEMPO, "");
            put(MusInterval.Fields.INSTRUMENT, "");
            put(MusInterval.Fields.FIRST_NOTE_DURATION_COEFFICIENT, "");
        }};

        LinkedList<Map<String, String>> searchResult = new LinkedList<Map<String, String>>() {{
            add(smallerNoteData);
            add(noteData);
            add(largerNoteData);
        }};
        doReturn(searchResult).when(helper).findNotes(eq(modelId), eq(new HashMap<String, String>()), any(Map.class), any(Map.class), any(Map.class));
        ArrayList<Map<String, String>> searchDataset = new ArrayList<Map<String, String>>() {{
            add(searchData);
        }};
        doReturn(searchResult).when(helper).findNotes(eq(modelId), eq(searchDataset), any(Map.class), any(Map.class), any(Map.class));

        MusInterval mi = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .notes(null)
                .octaves(null)
                .intervals(null)
                .build();
        ProgressIndicator progressIndicator = mock(ProgressIndicator.class);
        NotesIntegrity.Summary is = new NotesIntegrity(helper, mi, corruptedTag, suspiciousTag, duplicateTag, progressIndicator).check();

        assertEquals(3, is.getNotesCount());
        assertEquals(0, is.getSuspiciousNotesCount());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void checkIntegrity_EqualButDifferentStringFirstNoteDurationCoefficient_ShouldCountAsDuplicates() throws AnkiDroidHelper.InvalidAnkiDatabaseException, MusInterval.ValidationException {
        final long modelId = new Random().nextLong();
        final long noteId = new Random().nextLong();
        final long duplicateNoteId = new Random().nextLong();

        final AnkiDroidHelper helper = mock(AnkiDroidHelper.class);
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(SIGNATURE).when(helper).getFieldList(eq(modelId));

        final Map<String, String> noteData = new HashMap<String, String>() {{
            put(MusInterval.Fields.SOUND, "[sound:dir/file.mp3]");
            put(MusInterval.Fields.START_NOTE, "C3");
            put(MusInterval.Fields.INTERVAL, "min2");
            put(MusInterval.Fields.TIMING, "melodic");
            put(MusInterval.Fields.DIRECTION, "ascending");
            put(MusInterval.Fields.TEMPO, "80");
            put(MusInterval.Fields.INSTRUMENT, "guitar");
            put(MusInterval.Fields.FIRST_NOTE_DURATION_COEFFICIENT, "1.0");
            put(AnkiDroidHelper.KEY_ID, String.valueOf(noteId));
            put(AnkiDroidHelper.KEY_TAGS, "");
        }};

        final Map<String, String> duplicateNoteData = new HashMap<String, String>() {{
            put(MusInterval.Fields.SOUND, "[sound:dir/file.mp3]");
            put(MusInterval.Fields.START_NOTE, "C3");
            put(MusInterval.Fields.INTERVAL, "min2");
            put(MusInterval.Fields.TIMING, "melodic");
            put(MusInterval.Fields.DIRECTION, "ascending");
            put(MusInterval.Fields.TEMPO, "80");
            put(MusInterval.Fields.INSTRUMENT, "guitar");
            put(MusInterval.Fields.FIRST_NOTE_DURATION_COEFFICIENT, "01");
            put(AnkiDroidHelper.KEY_ID, String.valueOf(duplicateNoteId));
            put(AnkiDroidHelper.KEY_TAGS, "");
        }};

        final Map<String, String> searchData = new HashMap<String, String>() {{
            put(MusInterval.Fields.START_NOTE, "%%");
            put(MusInterval.Fields.INTERVAL, "%");
            put(MusInterval.Fields.TIMING, "");
            put(MusInterval.Fields.DIRECTION, "");
            put(MusInterval.Fields.TEMPO, "");
            put(MusInterval.Fields.INSTRUMENT, "");
            put(MusInterval.Fields.FIRST_NOTE_DURATION_COEFFICIENT, "");
        }};

        LinkedList<Map<String, String>> searchResult = new LinkedList<Map<String, String>>() {{
            add(noteData);
            add(duplicateNoteData);
        }};
        doReturn(searchResult).when(helper).findNotes(eq(modelId), eq(new HashMap<String, String>()), any(Map.class), any(Map.class), any(Map.class));
        ArrayList<Map<String, String>> searchDataSet = new ArrayList<Map<String, String>>() {{
            add(searchData);
        }};
        doReturn(searchResult).when(helper).findNotes(eq(modelId), eq(searchDataSet), any(Map.class), any(Map.class), any(Map.class));

        MusInterval mi = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .notes(null)
                .octaves(null)
                .intervals(null)
                .build();
        ProgressIndicator progressIndicator = mock(ProgressIndicator.class);
        NotesIntegrity.Summary is = new NotesIntegrity(helper, mi, corruptedTag, suspiciousTag, duplicateTag, progressIndicator).check();

        assertEquals(2, is.getNotesCount());
        assertEquals(2, is.getDuplicateNotesCount());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void checkIntegrity_EmptyDirectionUnison_ShouldFillLinks() throws MusInterval.ValidationException, AnkiDroidHelper.InvalidAnkiDatabaseException {
        final long modelId = new Random().nextLong();
        final long uniNoteId = new Random().nextLong();
        final long min2NoteId = new Random().nextLong();

        final AnkiDroidHelper helper = mock(AnkiDroidHelper.class);
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(SIGNATURE).when(helper).getFieldList(eq(modelId));

        final Map<String, String> uniNoteData = new HashMap<String, String>() {{
            put(MusInterval.Fields.SOUND, "[sound:dir/uni.mp3]");
            put(MusInterval.Fields.SOUND_SMALLER, "");
            put(MusInterval.Fields.SOUND_LARGER, "");
            put(MusInterval.Fields.START_NOTE, "C2");
            put(MusInterval.Fields.INTERVAL, MusInterval.Fields.Interval.VALUE_UNISON); // unison
            put(MusInterval.Fields.TIMING, "melodic");
            put(MusInterval.Fields.DIRECTION, ""); // empty direction
            put(MusInterval.Fields.TEMPO, "80");
            put(MusInterval.Fields.INSTRUMENT, "guitar");
            put(MusInterval.Fields.FIRST_NOTE_DURATION_COEFFICIENT, "");
            put(AnkiDroidHelper.KEY_ID, String.valueOf(uniNoteId));
            put(AnkiDroidHelper.KEY_TAGS, "");
        }};
        final Map<String, String> min2NoteData = new HashMap<String, String>() {{
            put(MusInterval.Fields.SOUND, "[sound:dir/min2.mp3]");
            put(MusInterval.Fields.SOUND_SMALLER, "");
            put(MusInterval.Fields.SOUND_LARGER, "");
            put(MusInterval.Fields.START_NOTE, "C2");
            put(MusInterval.Fields.INTERVAL, "min2");
            put(MusInterval.Fields.TIMING, "melodic");
            put(MusInterval.Fields.DIRECTION, "ascending");
            put(MusInterval.Fields.TEMPO, "80");
            put(MusInterval.Fields.INSTRUMENT, "guitar");
            put(MusInterval.Fields.FIRST_NOTE_DURATION_COEFFICIENT, "");
            put(AnkiDroidHelper.KEY_ID, String.valueOf(min2NoteId));
            put(AnkiDroidHelper.KEY_TAGS, "");
        }};
        final Map<String, String> searchData = new HashMap<String, String>() {{
            put(MusInterval.Fields.START_NOTE, "%%");
            put(MusInterval.Fields.INTERVAL, "%");
            put(MusInterval.Fields.TIMING, "");
            put(MusInterval.Fields.DIRECTION, "");
            put(MusInterval.Fields.TEMPO, "");
            put(MusInterval.Fields.INSTRUMENT, "");
            put(MusInterval.Fields.FIRST_NOTE_DURATION_COEFFICIENT, "");
        }};

        LinkedList<Map<String, String>> searchResult = new LinkedList<Map<String, String>>() {{
            add(uniNoteData);
            add(min2NoteData);
        }};
        doReturn(searchResult).when(helper).findNotes(eq(modelId), eq(new HashMap<String, String>()), any(Map.class), any(Map.class), any(Map.class));
        ArrayList<Map<String, String>> searchDataset = new ArrayList<Map<String, String>>() {{
            add(searchData);
        }};
        doReturn(searchResult).when(helper).findNotes(eq(modelId), eq(searchDataset), any(Map.class), any(Map.class), any(Map.class));
        Map<String, String> uniNoteKeyData = new HashMap<String, String>(uniNoteData) {{
            remove(MusInterval.Fields.SOUND);
            remove(MusInterval.Fields.SOUND_SMALLER);
            remove(MusInterval.Fields.SOUND_LARGER);
            remove(AnkiDroidHelper.KEY_ID);
            remove(AnkiDroidHelper.KEY_TAGS);
        }};
        doReturn(
                new LinkedList<Map<String, String>>() {{
                    add(uniNoteData);
                }}
        ).when(helper).findNotes(eq(modelId), eq(uniNoteKeyData), any(Map.class), any(Map.class), any(Map.class));
        Map<String, String> min2NoteKeyData = new HashMap<String, String>(min2NoteData) {{
            remove(MusInterval.Fields.SOUND);
            remove(MusInterval.Fields.SOUND_SMALLER);
            remove(MusInterval.Fields.SOUND_LARGER);
            replace(MusInterval.Fields.DIRECTION, "");
            remove(AnkiDroidHelper.KEY_ID);
            remove(AnkiDroidHelper.KEY_TAGS);
        }};
        doReturn(
                new LinkedList<Map<String, String>>() {{
                    add(min2NoteData);
                }}
        ).when(helper).findNotes(eq(modelId), eq(min2NoteKeyData), any(Map.class), any(Map.class), any(Map.class));

        MusInterval mi = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .notes(null)
                .octaves(null)
                .intervals(null)
                .build();
        ProgressIndicator progressIndicator = mock(ProgressIndicator.class);
        NotesIntegrity.Summary is = new NotesIntegrity(helper, mi, corruptedTag, suspiciousTag, duplicateTag, progressIndicator).check();

        assertEquals(2, is.getNotesCount());
        assertEquals(2, is.getAutoFilledRelationsCount());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void checkIntegrity_NonEmptyDirectionUnison_ShouldClear() throws MusInterval.ValidationException, AnkiDroidHelper.InvalidAnkiDatabaseException {
        final long modelId = new Random().nextLong();
        final long uniNoteId = new Random().nextLong();

        final AnkiDroidHelper helper = mock(AnkiDroidHelper.class);
        doReturn(modelId).when(helper).findModelIdByName(defaultModelName);
        doReturn(SIGNATURE).when(helper).getFieldList(eq(modelId));

        final Map<String, String> uniNoteData = new HashMap<String, String>() {{
            put(MusInterval.Fields.SOUND, "[sound:dir/uni.mp3]");
            put(MusInterval.Fields.SOUND_SMALLER, "");
            put(MusInterval.Fields.SOUND_LARGER, "");
            put(MusInterval.Fields.START_NOTE, "C2");
            put(MusInterval.Fields.INTERVAL, MusInterval.Fields.Interval.VALUE_UNISON); // unison
            put(MusInterval.Fields.TIMING, "melodic");
            put(MusInterval.Fields.DIRECTION, "ascending"); // non-empty direction
            put(MusInterval.Fields.TEMPO, "80");
            put(MusInterval.Fields.INSTRUMENT, "guitar");
            put(MusInterval.Fields.FIRST_NOTE_DURATION_COEFFICIENT, "");
            put(AnkiDroidHelper.KEY_ID, String.valueOf(uniNoteId));
            put(AnkiDroidHelper.KEY_TAGS, "");
        }};
        final Map<String, String> searchData = new HashMap<String, String>() {{
            put(MusInterval.Fields.START_NOTE, "%%");
            put(MusInterval.Fields.INTERVAL, "%");
            put(MusInterval.Fields.TIMING, "");
            put(MusInterval.Fields.DIRECTION, "");
            put(MusInterval.Fields.TEMPO, "");
            put(MusInterval.Fields.INSTRUMENT, "");
            put(MusInterval.Fields.FIRST_NOTE_DURATION_COEFFICIENT, "");
        }};

        LinkedList<Map<String, String>> searchResult = new LinkedList<Map<String, String>>() {{
            add(uniNoteData);
        }};
        doReturn(searchResult).when(helper).findNotes(eq(modelId), eq(new HashMap<String, String>()), any(Map.class), any(Map.class), any(Map.class));
        ArrayList<Map<String, String>> searchDataset = new ArrayList<Map<String, String>>() {{
            add(searchData);
        }};
        doReturn(searchResult).when(helper).findNotes(eq(modelId), eq(searchDataset), any(Map.class), any(Map.class), any(Map.class));

        doAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                Map<String, String> data = invocation.getArgument(2);
                for (Map.Entry<String, String> entry : data.entrySet()) {
                    uniNoteData.replace(entry.getKey(), entry.getValue());
                }
                return true;
            }
        }).when(helper).updateNote(eq(modelId), eq(uniNoteId), any(Map.class));

        MusInterval mi = new MusInterval.Builder(helper)
                .model(defaultModelName)
                .notes(null)
                .octaves(null)
                .intervals(null)
                .build();
        ProgressIndicator progressIndicator = mock(ProgressIndicator.class);
        NotesIntegrity.Summary is = new NotesIntegrity(helper, mi, corruptedTag, suspiciousTag, duplicateTag, progressIndicator).check();

        assertEquals(1, is.getNotesCount());
        assertEquals(0, is.getCorruptedNotesCount());
        assertEquals("", uniNoteData.get(MusInterval.Fields.DIRECTION));
    }
}
