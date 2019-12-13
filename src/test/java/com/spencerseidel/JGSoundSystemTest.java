package com.spencerseidel;

import java.util.Random;
import junit.framework.TestCase;

/**
 * Tests the JGSoundSystem class. By its nature, you have to hear whether the
 * sounds appear to be good. All names of the sounds should appear in the output
 * console and sounds should play over each other.
 *
 * @author azuurbier
 */
public class JGSoundSystemTest extends TestCase {

  public JGSoundSystemTest() {
  }

  public void testgivenSoundSystem_whenPlayingALotOfSounds_thenNoExceptionIsThrown() {
    System.out.println("*** start testgivenSoundSystem_whenPlayingALotOfSounds_thenNoExceptionIsThrown");
    try {
      JGSoundSystem soundSystem = new JGSoundSystem();
      soundSystem.start();
      Random random = new Random();
      int soundCount = JGSoundSystem.GameSound.values().length;
      for (int i = 0; i < 150; ++i) {
        int soundChoice = random.nextInt(soundCount);
        JGSoundSystem.GameSound chosenSound = JGSoundSystem.GameSound.values()[soundChoice];
        System.out.println(chosenSound.toString());
        soundSystem.soundQueue.add(chosenSound);
        try {
          Thread.sleep(Math.round(random.nextFloat() * 500));
        } catch (InterruptedException ex) {
        }
      }
      System.out.println("*** completed testgivenSoundSystem_whenPlayingALotOfSounds_thenNoExceptionIsThrown");
      try {
        //Just wait five seconds to let all sounds play
        Thread.sleep(5000L);
      } catch (InterruptedException ex) {
        System.out.println("Interrupted sleep");
      }
      soundSystem.stop();
    } catch (Exception ex) {
      fail("Test failed with " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
    }
  }

  public void testCodeExample() {
    System.out.println("*** start testCodeExample");
    try {
      
      // Code example
      JGSoundSystem soundSystem = new JGSoundSystem();
      soundSystem.start();
      soundSystem.soundQueue.add(JGSoundSystem.GameSound.StartGame);
      soundSystem.stop();
      //
      
      try {
        Thread.sleep(Math.round(1000));
      } catch (InterruptedException ex) {
      }
      System.out.println("*** completed testCodeExample");
    } catch (Exception ex) {
      fail("Test failed with " + ex.getClass().getSimpleName() + ": " + ex.getMessage());
    }
  }
}
