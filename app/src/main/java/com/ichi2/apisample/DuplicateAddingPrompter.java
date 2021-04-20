package com.ichi2.apisample;

import java.util.LinkedList;
import java.util.Map;

public interface DuplicateAddingPrompter {
    void promptAddDuplicate(LinkedList<Map<String, String>> existingNotesData, DuplicateAddingHandler handler);
}
