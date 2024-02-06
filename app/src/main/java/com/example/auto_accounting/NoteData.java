package com.example.auto_accounting;


public class NoteData {
    private long id;
    private String noteText;
    private boolean noteChecked;

    public NoteData(long id, String noteText, boolean noteChecked) {
        this.id = id;
        this.noteText = noteText;
        this.noteChecked = noteChecked;
    }

    public long getId() {
        return id;
    }

    public String getNoteText() {
        return noteText;
    }

    public void setId(long id) {
        this.id = id;
    }

    public boolean isNoteChecked() {
        return noteChecked;
    }

}