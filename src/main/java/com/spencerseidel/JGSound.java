package com.spencerseidel;

import java.io.IOException;
import java.net.URL;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

/**
 * Wrapper around javax.sound.sampled.Clip to reuse the data line. To repeatedly
 * reply the sound but avoid running out of data lines, this implementation
 * loads the audio input stream from the resource without renewing the data 
 * line. As long as the resource and the data line agree on the format info, 
 * this should be fine. Since the application has only a limited number of 
 * sounds, data line sharing is not needed.
 * 
 * @author albertzb
 */
public class JGSound {

    private URL soundUrl;
    private Clip clip;

    /**
     * Currently only used to mute the system or not, allows for future changing
     * of the volume.
     */
    public static enum Volume {
        MUTE, LOW, MEDIUM, HIGH
    }

    public static Volume volume = Volume.LOW;

    /**
     * Load the sound from a path to the resource. Convenience method around
     * {@link #loadSound(URL) loadSound}.
     * @param soundPath
     * @return a JGSound instance.
     */
    public static JGSound loadSound(String soundPath) {
        URL soundUrl = JGSound.class.getClassLoader().getResource(soundPath);
        return loadSound(soundUrl);
    }

    /**
     * Load the sound from a url of the resource. Implemented as a static
     * method to be able to handle the possible exceptions outside the 
     * constructor.
     * @param soundUrl
     * @return a JGSound instance.
     */
    public static JGSound loadSound(URL soundUrl) {
        try {
            //Get the information to initialize the clip for the first time
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundUrl);
            DataLine.Info info = new DataLine.Info(Clip.class, audioInputStream.getFormat());
            Clip clip = (Clip) AudioSystem.getLine(info);

            return new JGSound(soundUrl, clip);
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException ex) {

        }
        return null;
    }

    /**
     * Private constructor. For creation, use the loadSound methods.
     * @param soundUrl
     * @param clip 
     */
    private JGSound(URL soundUrl, Clip clip) {
        this.soundUrl = soundUrl;
        this.clip = clip;
    }

    /**
     * Stops the sound.
     */
    public void stop() {
        if (clip == null) {
            return;
        }

        if (clip.isRunning() || clip.isActive()) {
            clip.stop();
        }
        if (clip.isOpen()) {
            clip.close();
        }
    }

    /**
     * Plays the sound in a continuous loop.
     * {@see #play() play()}
     */
    public void loop() {
        if (volume != Volume.MUTE) {
            stop();
            try {
                //Recreate the audio input stream to reset the stream, 
                //reuse the same data line to avoid running out of data lines
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundUrl);
                clip.open(audioInputStream);
            } catch (IOException | LineUnavailableException | UnsupportedAudioFileException e) {
                System.err.println("Exception loading sound '" + soundUrl.toString() + "': " + e.getMessage());
            }
            clip.loop(Clip.LOOP_CONTINUOUSLY);
        }
    }

    /**
     * Plays the sound one time. With each call the audioInputStream is loaded
     * from the resource. This works around the fact that audio input streams in 
     * support reset to the start of the stream before replay. The data line
     * is not changed because only a limited number of data lines are available
     * in an audio system.
     */
    public void play() {
        if (volume != Volume.MUTE) {
            stop();
            try {
                //Recreate the audio input stream to reset the stream (reuse the same data line)
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(soundUrl);
                clip.open(audioInputStream);
            } catch (IOException | LineUnavailableException | UnsupportedAudioFileException e) {
                System.err.println("Exception loading sound '" + soundUrl.toString() + "': " + e.getMessage());
            }

            clip.start();
        }
    }
}
