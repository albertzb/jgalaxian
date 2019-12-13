/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.spencerseidel;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Simple sound system that plays sounds without blocking the main thread. 
 * Playing sounds can block the main thread in many ways, some of them operating 
 * system specific, for example when loading the sound resource to be played or
 * the first time an audio system is used. In any of these cases, the main 
 * thread may become instable or hang. By offloading the sound to another thread
 * the possibility that playing sounds cause problems for the main thread are
 * reduced. To control which sound is played a concurrent queue is used.
 * An AtomicBoolean is used to gracefully start, stop and interrupt the sound
 * system.
 * Usage:
 * <pre>
 *  // At the start of the application
 *  JGSoundSystem soundSystem = new JGSoundSystem();
 *  soundSystem.start();
 * 
 *  // Somehwere in the application
 *  soundSystem.soundQueue.add(JGSoundSystem.GameSound.StartGame);
 * 
 *  // When exiting the application
 *  soundSystem.stop();
 * </pre>
 * @author albertzb
 */
public class JGSoundSystem implements Runnable {

  public static enum GameSound {
    PlayerFired, ShotBadGuy1, ShotBadGuy2, ShotBigBadGuy,
    BadGuyAttacking, PlayerExplode, StartGame, GameOver,
    EnemyFired, FreeGuy, GetReady, Quit, Muzak
  }

  public BlockingQueue<GameSound> soundQueue;
  private Thread worker;
  private final AtomicBoolean running;
  private Map<GameSound, JGSound> soundMap;
  private long interval;

  public JGSoundSystem() {
    this(100L);
  }

  public JGSoundSystem(long sleepInterval) {
    interval = sleepInterval;
    soundQueue = new LinkedBlockingQueue<GameSound>();
    running = new AtomicBoolean(false);
    // Sounds
    soundMap = new HashMap<GameSound, JGSound>();
    soundMap.put(GameSound.PlayerFired, JGSound.loadSound(JGGlob.SOUNDS_BASE + File.separator + "playerfired.wav"));
    soundMap.put(GameSound.ShotBadGuy1, JGSound.loadSound(JGGlob.SOUNDS_BASE + File.separator + "shotbadguy1.wav"));
    soundMap.put(GameSound.ShotBadGuy2, JGSound.loadSound(JGGlob.SOUNDS_BASE + File.separator + "shotbadguy2.wav"));
    soundMap.put(GameSound.ShotBigBadGuy, JGSound.loadSound(JGGlob.SOUNDS_BASE + File.separator + "shotbigbadguy.wav"));
    soundMap.put(GameSound.BadGuyAttacking, JGSound.loadSound(JGGlob.SOUNDS_BASE + File.separator + "badguyattacking.wav"));
    soundMap.put(GameSound.PlayerExplode, JGSound.loadSound(JGGlob.SOUNDS_BASE + File.separator + "playerexplode.wav"));
    soundMap.put(GameSound.StartGame, JGSound.loadSound(JGGlob.SOUNDS_BASE + File.separator + "startgame.wav"));
    soundMap.put(GameSound.GameOver, JGSound.loadSound(JGGlob.SOUNDS_BASE + File.separator + "gameover.wav"));
    soundMap.put(GameSound.EnemyFired, JGSound.loadSound(JGGlob.SOUNDS_BASE + File.separator + "enemyfired.wav"));
    soundMap.put(GameSound.FreeGuy, JGSound.loadSound(JGGlob.SOUNDS_BASE + File.separator + "freeguy.wav"));
    soundMap.put(GameSound.GetReady, JGSound.loadSound(JGGlob.SOUNDS_BASE + File.separator + "getready.wav"));
    soundMap.put(GameSound.Quit, JGSound.loadSound(JGGlob.SOUNDS_BASE + File.separator + "quit.wav"));
    soundMap.put(GameSound.Muzak, JGSound.loadSound(JGGlob.SOUNDS_BASE + File.separator + "muzak.wav"));
  }

  public void start() {
    worker = new Thread(this);
    worker.start();
  }

  public void stop() {
    running.set(false);
  }

  public void interrupt() {
    running.set(false);
    worker.interrupt();
  }

  public boolean isRunning() {
    return running.get();
  }

  public void run() {
    running.set(true);
    while (running.get()) {
      GameSound sound;
      if (!soundQueue.isEmpty()) {
        while ((sound = soundQueue.poll()) != null) {
          soundMap.get(sound).play();
          soundQueue.remove(sound);
        }
      } else {
        try {
          Thread.sleep(interval);
        } catch (InterruptedException ex) {
          Thread.currentThread().interrupt();
          System.out.println("Sound system interruped");
        }
      }
    }
  }
}
