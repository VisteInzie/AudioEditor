import javax.sound.sampled.*;
import java.io.*;
import java.util.ArrayList;
// import java.util.Arrays;

public class Main {

    public static void main(String[] args) {
        String filePath = "";
        if (args.length > 0 && !args[0].isEmpty()) {
            filePath = args[0];
        }

        if (filePath.isEmpty()) {
            System.out.println("Usage: java -jar AudioEditor.jar <path>");
            System.exit(0);
        }

        System.out.println("filePath: " + filePath);

        try {
            System.out.println("Reading audioBytes.");
            ArrayList<byte[]> audioBytes = readAudioBytes(filePath);
            long totalBytes = 0;
            for (byte[] audioByte : audioBytes) {
                // System.out.println(Arrays.toString(audioByte));
                totalBytes += (long) audioByte.length;
            }
            System.out.println("audioBytes.size(): " + audioBytes.size());
            System.out.println("audioBytes[0].length: " + audioBytes.get(0).length);
            System.out.println("totalBytes: " + totalBytes + " (" + humanReadableByteCount(totalBytes, false) + ")");
        } catch (IOException | UnsupportedAudioFileException e) {
            e.printStackTrace();
            System.exit(1);
        }

        try {
            System.out.println("Playing...");
            play(filePath);
        } catch (IOException | LineUnavailableException | UnsupportedAudioFileException e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    private static ArrayList<byte[]> readAudioBytes(String filePath) throws IOException, UnsupportedAudioFileException {
        File file = new File(filePath);
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file);
        AudioFormat audioFormat = audioInputStream.getFormat();

        int bytesPerFrame = audioFormat.getFrameSize();
        if (bytesPerFrame == AudioSystem.NOT_SPECIFIED) {
            // some audio formats may have unspecified frame size
            // in that case we may read any amount of bytes
            bytesPerFrame = 1;
        }

        int totalFramesRead = 0;
        ArrayList<byte[]> arrayList = new ArrayList<>();
        // Set an arbitrary buffer size of 1024 frames.
        int numBytesRead;
        int numFramesRead;

        // Try to read numBytes bytes from the file.
        int numBytes = 1024 * bytesPerFrame;
        byte[] audioBytes = new byte[numBytes];
        while ((numBytesRead = audioInputStream.read(audioBytes)) != -1) {
            // Calculate the number of frames actually read.
            numFramesRead = numBytesRead / bytesPerFrame;
            totalFramesRead += numFramesRead;

            // Clone bytes array then add to ArrayList
            byte[] audioBytesClone = audioBytes.clone();
            arrayList.add(audioBytesClone);
        }

        return arrayList;
    }

    private static void play(String filePath) throws IOException, LineUnavailableException, UnsupportedAudioFileException {
        File file = new File(filePath);
        AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(file);
        AudioFormat audioFormat = audioInputStream.getFormat();
        DataLine.Info info = new DataLine.Info(Clip.class, audioFormat);
        Clip clip = (Clip) AudioSystem.getLine(info);
        clip.open(audioInputStream);
        clip.start();
    }

    private static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp-1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }
}
