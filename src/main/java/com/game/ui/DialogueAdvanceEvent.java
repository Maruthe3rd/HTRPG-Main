package com.game.ui;

import javafx.event.Event;
import javafx.event.EventType;

public final class DialogueAdvanceEvent extends Event {

    public static final EventType<DialogueAdvanceEvent> ADVANCE =
            new EventType<>(Event.ANY, "DIALOGUE_ADVANCE");

    public DialogueAdvanceEvent() {
        super(ADVANCE);
    }
}