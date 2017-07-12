package com.zmxv.RNSound;

import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.net.Uri;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.WritableMap;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class RNSoundModule extends ReactContextBaseJavaModule {
  Map<Integer, MediaPlayer> playerPool = new HashMap<>();
  ReactApplicationContext context;
  final static Object NULL = null;

  public RNSoundModule(ReactApplicationContext context) {
    super(context);
    this.context = context;
  }

  @Override
  public String getName() {
    return "RNSound";
  }

  @ReactMethod
  public void prepare(final String fileName, final Integer key, final Promise promise) {
    MediaPlayer player = createMediaPlayer(fileName);
    if (player == null) {
      WritableMap e = Arguments.createMap();
      e.putInt("code", -1);
      e.putString("message", "resource not found");
      promise.reject("error", "Resource not found at prepare(fileName, key)", e);
      return;
    }
    this.playerPool.put(key, player);
    WritableMap props = Arguments.createMap();
    props.putDouble("duration", player.getDuration() * .001);
    promise.resolve(props);
  }

  protected MediaPlayer createMediaPlayer(final String fileName) {
    int res = this.context.getResources().getIdentifier(fileName, "raw", this.context.getPackageName());
    if (res != 0) {
      return MediaPlayer.create(this.context, res);
    }
    File file = new File(fileName);
    if (file.exists()) {
      Uri uri = Uri.fromFile(file);
      return MediaPlayer.create(this.context, uri);
    }
    return null;
  }

  @ReactMethod
  public void play(final Integer key, final Promise promise) {
    MediaPlayer player = this.playerPool.get(key);
    if (player == null) {
      promise.reject("error", "Resource not found at play(key)");
      return;
    }
    if (player.isPlaying()) {
      return;
    }
    player.setOnCompletionListener(new OnCompletionListener() {
      @Override
      public void onCompletion(MediaPlayer mp) {
        if (!mp.isLooping()) {
          promise.resolve("Success");
        }
      }
    });
    player.setOnErrorListener(new OnErrorListener() {
      @Override
      public boolean onError(MediaPlayer mp, int what, int extra) {
        promise.reject("error", "unknown error at play(key)");
        return true;
      }
    });
    player.start();
  }

  @ReactMethod
  public void pause(final Integer key) {
    MediaPlayer player = this.playerPool.get(key);
    if (player != null && player.isPlaying()) {
      player.pause();
    }
  }

  @ReactMethod
  public void stop(final Integer key) {
    MediaPlayer player = this.playerPool.get(key);
    if (player != null && player.isPlaying()) {
      player.stop();
      try {
        player.prepare();
      } catch (Exception e) {
      }
    }
  }

  @ReactMethod
  public void release(final Integer key) {
    MediaPlayer player = this.playerPool.get(key);
    if (player != null) {
      player.release();
      this.playerPool.remove(key);
    }
  }

  @ReactMethod
  public void setVolume(final Integer key, final Float left, final Float right) {
    MediaPlayer player = this.playerPool.get(key);
    if (player != null) {
      player.setVolume(left, right);
    }
  }

  @ReactMethod
  public void setLooping(final Integer key, final Boolean looping) {
    MediaPlayer player = this.playerPool.get(key);
    if (player != null) {
      player.setLooping(looping);
    }
  }

  @ReactMethod
  public void setCurrentTime(final Integer key, final Float sec) {
    MediaPlayer player = this.playerPool.get(key);
    if (player != null) {
      player.seekTo((int)Math.round(sec * 1000));
    }
  }

  @ReactMethod
  public void getCurrentTime(final Integer key, final Promise promise) {
    MediaPlayer player = this.playerPool.get(key);
    WritableMap map = Arguments.createMap();
    if (player == null) {
      map.putInt("seconds", -1);
      map.putBoolean("isPlaying", false);
      promise.resolve(map);
      return;
    }
    map.putDouble("seconds", player.getCurrentPosition() * .001);
    map.putBoolean("isPlaying", player.isPlaying());
    promise.resolve(map);
  }

  @ReactMethod
  public void enable(final Boolean enabled) {
    // no op
  }

  @Override
  public Map<String, Object> getConstants() {
    final Map<String, Object> constants = new HashMap<>();
    constants.put("IsAndroid", true);
    return constants;
  }
}
