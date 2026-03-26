import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class Player implements Runnable {
    BellNote state;

    // TODO Player tells conductor when they are done, but how do you pass note length

    private final Note note; // Note player will play
    private volatile NoteLength noteLength;
    private final AudioFormat audioFormat;
    private final SourceDataLine sourceDataLine;
    private volatile Thread playerThread;

    Player(Note note) {
        this.note = note;
        noteLength = NoteLength.WHOLE;
        this.audioFormat = new AudioFormat(Note.SAMPLE_RATE, 8, 1, true, false);

        SourceDataLine sourceDataLineTemp;
        try {
            sourceDataLineTemp = AudioSystem.getSourceDataLine(audioFormat);
        } catch (LineUnavailableException e) {
            System.err.println("Error while loading source data line.");
            sourceDataLineTemp = null;
        }

        sourceDataLine = sourceDataLineTemp; // TODO, add proper error handling

    }

    public void run() {
        try {
            sourceDataLine.open();
            sourceDataLine.start();

            playNote();

            sourceDataLine.drain();
            sourceDataLine.stop();
            sourceDataLine.close();
        } catch (LineUnavailableException e) {
            System.err.println("Error while opening or closing source data line.");
        }
    }

    public void setNoteLength(NoteLength noteLength) {
        this.noteLength = noteLength;
    }

    public void startThread() {
        playerThread = new Thread(this);
        playerThread.start();
    }

    /**
     * Play note given on the line
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