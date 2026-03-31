import javax.sound.sampled.SourceDataLine;

public class Player implements Runnable {
    BellNote state;

    private final Note note; // Note player will play
    private volatile NoteLength noteLength;

    private final SourceDataLine sourceDataLine;
    private volatile Thread playerThread;

    private volatile boolean myTurn;

    Player(Note note, SourceDataLine sourceDataLine) {
        this.note = note;
        noteLength = NoteLength.WHOLE;
        this.sourceDataLine = sourceDataLine;
    }

    public void run() {
        playNote();
    }

    public void setNoteLength(NoteLength noteLength) {
        this.noteLength = noteLength;
    }

    public void startThread() {
        playerThread = new Thread(this); // Create new thread, because threads can't be run more than once
        playerThread.start();
    }

    /**
     * Play note on the line
     */
    private void playNote() {
        final int ms = Math.min(noteLength.timeMs(), Note.MEASURE_LENGTH_SEC * 1000);
        final int length = Note.SAMPLE_RATE * ms / 1000;
        sourceDataLine.write(note.sample(), 0, length);
        sourceDataLine.write(Note.REST.sample(), 0, 50);
    }

    public void waitToStop() {
        try {
            playerThread.join();
        } catch (InterruptedException ignored) {

        }
    }
}