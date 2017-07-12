'use strict';

var RNSound = require('react-native').NativeModules.RNSound;
var IsAndroid = RNSound.IsAndroid;

var nextKey = 0;

function isRelativePath(path) {
  return !/^\//.test(path);
}

function Sound(filename, basePath, onError) {
  this._filename = basePath ? basePath + '/' + filename : filename;

  if (IsAndroid && !basePath && isRelativePath(filename)) {
    this._filename = filename.toLowerCase().replace(/\.[^.]+$/, '');
  }

  this._loaded = false;
  this._key = nextKey++;
  this._duration = -1;
  this._numberOfChannels = -1;
  this._volume = 1;
  this._pan = 0;
  this._numberOfLoops = 0;
  let self = this;
  (async function() {
    try {
      let result = await RNSound.prepare(self._filename, self._key);
      console.log(result);
      if (result) {
        if (typeof result.duration === 'number') {
          self._duration = result.duration;
        }
        if (typeof result.numberOfChannels === 'number') {
          self._numberOfChannels = result.numberOfChannels;
        }
        self._loaded = true;
      }
    } catch(e) {
      console.error(e);
      onError && onError(error);
    }
  })();
}

Sound.prototype.isLoaded = function() {
  return this._loaded;
};

Sound.prototype.play = function(onEnd) {
  if (this._loaded) {
    // RNSound.play(this._key, (successfully) => onEnd && onEnd(successfully));
    let self = this;
    (async function() {
      try {
        let result = await RNSound.play(self._key);
        if(result) {
          onEnd && onEnd(result)
        }
      } catch(e) {
        console.error(e);
      }
    })();
  }
  return this;
};

Sound.prototype.pause = function() {
  if (this._loaded) {
    RNSound.pause(this._key);
  }
  return this;
};

Sound.prototype.stop = function() {
  if (this._loaded) {
    RNSound.stop(this._key);
  }
  return this;
};

Sound.prototype.release = function() {
  if (this._loaded) {
    RNSound.release(this._key);
  }
  return this;
};

Sound.prototype.getDuration = function() {
  return this._duration;
};

Sound.prototype.getNumberOfChannels = function() {
  return this._numberOfChannels;
};

Sound.prototype.getVolume = function() {
  return this._volume;
};

Sound.prototype.setVolume = function(value) {
  this._volume = value;
  if (this._loaded) {
    if (IsAndroid) {
      RNSound.setVolume(this._key, value, value);
    } else {
      RNSound.setVolume(this._key, value);
    }
  }
  return this;
};

Sound.prototype.getPan = function() {
  return this._pan;
};

Sound.prototype.setPan = function(value) {
  if (this._loaded) {
    RNSound.setPan(this._key, this._pan = value);
  }
  return this;
};

Sound.prototype.getNumberOfLoops = function() {
  return this._numberOfLoops;
};

Sound.prototype.setNumberOfLoops = function(value) {
  this._numberOfLoops = value;
  if (this._loaded) {
    if (IsAndroid) {
      RNSound.setLooping(this._key, !!value);
    } else {
      RNSound.setNumberOfLoops(this._key, value);
    }
  }
  return this;
};

Sound.prototype.getCurrentTime = function(callback) {
  if (this._loaded) {
    let self = this;
    (async function() {
      try {
        callback(await RNSound.getCurrentTime(self._key));
      } catch(e) {
        console.error(e);
      }
    })();
  }
};

Sound.prototype.setCurrentTime = function(value) {
  if (this._loaded) {
    RNSound.setCurrentTime(this._key, value);
  }
  return this;
};

Sound.enable = function(enabled) {
  RNSound.enable(enabled);
};

Sound.enableInSilenceMode = function(enabled) {
  if (!IsAndroid) {
    RNSound.enableInSilenceMode(enabled);
  }
};

if (!IsAndroid) {
  Sound.enable(true);
}

Sound.MAIN_BUNDLE = RNSound.MainBundlePath;
Sound.DOCUMENT = RNSound.NSDocumentDirectory;
Sound.LIBRARY = RNSound.NSLibraryDirectory;
Sound.CACHES = RNSound.NSCachesDirectory;

module.exports = Sound;
