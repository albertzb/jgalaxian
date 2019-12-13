package com.spencerseidel;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import junit.framework.TestCase;

/**
 * Tests for JGSound.
 * @author azuurbier
 */
public class JGSoundTest extends TestCase{

    public JGSoundTest() {
    }

    public void testgivenSoundUrl_whenCreated_thenNotNull() {
        URL soundUrl = getClass().getClassLoader().getResource(JGGlob.SOUNDS_BASE + File.separator + "playerfired.wav");
        JGSound sound = JGSound.loadSound(soundUrl);
        assertNotNull(sound);
    }

    public void testgivenASound_whenPlaying_thenNoException() {
        URL soundUrl = getClass().getClassLoader().getResource(JGGlob.SOUNDS_BASE + File.separator + "playerfired.wav");
        JGSound sound = JGSound.loadSound(soundUrl);
        try {
            sound.play();
        } catch (Exception ex) {
            fail("Exception: " + ex.getClass().getSimpleName() + ", " + ex.getMessage());
        }
    }

    public void testshowAudioFormats() {
        System.out.println("showAudioFormats");
        String baseResourcePath = JGGlob.SOUNDS_BASE + File.separator;
        List<String> fileNames = Arrays.asList("playerfired.wav",
                "shotbadguy1.wav", "shotbadguy2.wav", "shotbigbadguy.wav",
                "badguyattacking.wav", "playerexplode.wav", "startgame.wav",
                "gameover.wav", "enemyfired.wav", "freeguy.wav",
                "getready.wav", "quit.wav", "muzak.wav");
        for (String fileName : fileNames) {
            URL soundUrl = getClass().getClassLoader().getResource(baseResourcePath + fileName);
            try {
                AudioInputStream inputStream = AudioSystem.getAudioInputStream(soundUrl);
                System.out.printf("AudioInputStream %-19s, %d, %b\n",
                        fileName,
                        inputStream.getFrameLength(),
                        inputStream.markSupported()
                );
                AudioFormat inputFormat = inputStream.getFormat();
                System.out.printf("AudioFormat      %-19s %-10s, %3.2f, %d, %d, %d, %3.2f, %b\n\n",
                        fileName,
                        inputFormat.getEncoding().toString(),
                        inputFormat.getSampleRate(),
                        inputFormat.getSampleSizeInBits(),
                        inputFormat.getChannels(),
                        inputFormat.getFrameSize(),
                        inputFormat.getFrameRate(),
                        inputFormat.isBigEndian());
            } catch (UnsupportedAudioFileException | IOException ex) {
                System.out.println("oops!" + ex.getMessage());
            }
        }
    }

    public void testgivenSound_whenPlayingTenTimes_thenYouCanHearEach() {
        URL soundUrl = getClass().getClassLoader().getResource(JGGlob.SOUNDS_BASE + File.separator + "playerfired.wav");
        JGSound sound = JGSound.loadSound(soundUrl);
        sound.volume = JGSound.Volume.HIGH;
        for (int i = 0; i < 10; ++i) {
            sound.play();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                System.out.println(ex.getMessage());
            }
        }
    }

    public void testgivenSounds_whenPlayingEachSound_thenEachSoundPlays() {
        String baseResourcePath = JGGlob.SOUNDS_BASE + File.separator;
        List<String> fileNames = Arrays.asList("playerfired.wav",
                "shotbadguy1.wav", "shotbadguy2.wav", "shotbigbadguy.wav",
                "badguyattacking.wav", "playerexplode.wav", "startgame.wav",
                "gameover.wav", "enemyfired.wav", "freeguy.wav",
                "getready.wav", "quit.wav", "muzak.wav");
        for (int i = 0; i < 3; ++i) {
            for (String fileName : fileNames) {
                System.out.printf("playing %s\n", fileName);
                URL soundUrl = getClass().getClassLoader().getResource(baseResourcePath + fileName);
                JGSound sound = JGSound.loadSound(soundUrl);
                sound.play();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    System.out.println(ex.getMessage());
                }
            }
        }
    }
}
