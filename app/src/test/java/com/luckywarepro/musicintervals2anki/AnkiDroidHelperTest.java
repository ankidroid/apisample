package com.luckywarepro.musicintervals2anki;

import org.junit.Test;


public class AnkiDroidHelperTest {

    @Test
    public void checkForNewApiAvailable_existingClass() throws ClassNotFoundException {
        // This subsclass is already exists in the FlashCardsContract, so it should not fail
        Class.forName("com.ichi2.anki.FlashCardsContract$Note", false, getClass().getClassLoader());
    }

    @Test(expected = ClassNotFoundException.class)
    public void checkForNewApiAvailable_notYetExistingClass() throws ClassNotFoundException {
        // This subsclass is not yet exists in the current version of API, so it should fail
        // @todo: if this test failed, use new AnkiMedia API to add mediafiles instead of current solution in method AnkiDroidHelper.addFileToAnkiMedia
        Class.forName("com.ichi2.anki.FlashCardsContract$AnkiMedia", false, getClass().getClassLoader());
    }

}
