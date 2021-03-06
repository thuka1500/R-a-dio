package com.jcanseco.radio.players;

import android.app.Application;
import android.content.Context;

import com.google.android.exoplayer.ExoPlaybackException;
import com.google.android.exoplayer.ExoPlayer;
import com.google.android.exoplayer.TrackRenderer;
import com.jcanseco.radio.players.trackrenderers.TrackRendererFactory;
import com.jcanseco.radio.tasks.RadioPlayerBufferTimeoutTimerTask;

import java.util.Timer;
import java.util.TimerTask;

public class RadioPlayer implements ExoPlayer.Listener {

    private static final long BUFFER_TIMEOUT_IN_MILLIS = 10000;

    private RadioPlayer.Listener radioPlayerListener;

    private ExoPlayer exoPlayer;
    private boolean isPlaying;

    private Timer timer;
    private boolean isCurrentlyCountingDownForBufferTimeout;

    private final Context applicationContext;

    public RadioPlayer(ExoPlayer exoPlayer, Application application) {
        this.exoPlayer = exoPlayer;
        this.exoPlayer.addListener(this);

        this.applicationContext = application;
    }

    public void setRadioPlayerListener(RadioPlayer.Listener radioPlayerListener) {
        this.radioPlayerListener = radioPlayerListener;
    }

    public boolean isPlaying() {
        return isPlaying;
    }

    public void play() {
        if (!isExoPlayerPreparedForPlayback()) {
            prepareExoPlayerForPlayback();
        }
        exoPlayer.setPlayWhenReady(true);
        isPlaying = true;
    }

    private void prepareExoPlayerForPlayback() {
        exoPlayer.prepare(createAudioTrackRenderer());
    }

    protected TrackRenderer createAudioTrackRenderer() {
        return TrackRendererFactory.createAudioTrackRenderer(applicationContext);
    }

    public void pause() {
        exoPlayer.setPlayWhenReady(false);
        isPlaying = false;
    }

    public void release() {
        exoPlayer.release();
        isPlaying = false;
    }

    @Override
    public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
        if(isPlayerBuffering(playbackState)) {
            startCountdownForBufferTimeout();
        } else {
            stopCountdownForBufferTimeout();
        }
    }

    private boolean isPlayerBuffering(int playbackState) {
        return playbackState == ExoPlayer.STATE_BUFFERING;
    }

    private void startCountdownForBufferTimeout() {
        if (!isCurrentlyCountingDownForBufferTimeout()) {
            scheduleTimerTaskForBufferTimeout();
            isCurrentlyCountingDownForBufferTimeout = true;
        }
    }

    private void scheduleTimerTaskForBufferTimeout() {
        timer = initNewTimer();
        TimerTask bufferTimeoutTimerTask = new RadioPlayerBufferTimeoutTimerTask(this);
        timer.schedule(bufferTimeoutTimerTask, BUFFER_TIMEOUT_IN_MILLIS);
    }

    private void stopCountdownForBufferTimeout() {
        if(getTimer() != null) {
            getTimer().cancel();
            getTimer().purge();
        }
        isCurrentlyCountingDownForBufferTimeout = false;
    }

    public void onBufferingTimedOut() {
        onPlayerError(new ExoPlaybackException("Buffering timed out."));
        isCurrentlyCountingDownForBufferTimeout = false;
    }

    @Override
    public void onPlayerError(ExoPlaybackException error) {
        radioPlayerListener.onRadioPlayerStreamError();
        exoPlayer.stop();
        isPlaying = false;
    }

    @Override
    public void onPlayWhenReadyCommitted() {}

    protected boolean isExoPlayerPreparedForPlayback() {
        return exoPlayer.getPlaybackState() != ExoPlayer.STATE_IDLE;
    }

    protected boolean isCurrentlyCountingDownForBufferTimeout() {
        return isCurrentlyCountingDownForBufferTimeout;
    }

    protected Timer initNewTimer() {
        return new Timer();
    }

    protected Timer getTimer() {
        return timer;
    }


    public interface Listener {

        void onRadioPlayerStreamError();
    }
}
