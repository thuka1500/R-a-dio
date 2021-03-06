package com.jcanseco.radio.ui.radioplayer;

import com.jcanseco.radio.loaders.RadioContentLoader;
import com.jcanseco.radio.models.Dj;
import com.jcanseco.radio.models.NowPlayingTrack;
import com.jcanseco.radio.models.RadioContent;

public class RadioPlayerPresenter implements RadioContentLoader.RadioContentListener {

    private RadioPlayerPresenter.View radioPlayerView;
    private RadioContentLoader radioContentLoader;

    private boolean isPlayerPlaying;
    private boolean isRadioPlayerServiceConnected;

    public RadioPlayerPresenter(RadioContentLoader radioContentLoader) {
        this.radioContentLoader = radioContentLoader;
        this.radioContentLoader.setRadioContentListener(this);
    }

    public void attachView(RadioPlayerPresenter.View radioPlayerView) {
        this.radioPlayerView = radioPlayerView;
    }

    public void onStart() {
        radioPlayerView.startRadioPlayerService();
        radioPlayerView.bindToRadioPlayerService();
        radioPlayerView.registerFailedToPlayStreamBroadcastReceiver();
    }

    public void onResume() {
        radioContentLoader.startScheduledLoadingOfContent();
    }

    public void onPause() {
        radioContentLoader.stopScheduledLoadingOfContent();
    }

    public void onStop() {
        radioPlayerView.unbindFromRadioPlayerService();
        radioPlayerView.unregisterFailedToPlayStreamBroadcastReceiver();
    }

    public void onRadioPlayerServiceConnected(boolean isServiceCurrentlyPlayingStream) {
        isRadioPlayerServiceConnected = true;
        if (isServiceCurrentlyPlayingStream) {
            setPlayerStateAsPlaying();
        } else {
            setPlayerStateAsPaused();
        }
    }

    public void onRadioPlayerServiceDisconnected() {
        isRadioPlayerServiceConnected = false;
    }

    public void onActionButtonClicked() {
        if (isPlayerPlaying()) {
            pausePlayer();
        } else {
            playPlayer();
        }
    }

    protected boolean isPlayerPlaying() {
        return isPlayerPlaying;
    }

    protected void pausePlayer() {
        if (isRadioPlayerServiceConnected()) {
            radioPlayerView.stopPlayingRadioStream();
            setPlayerStateAsPaused();
        }
    }

    protected void playPlayer() {
        if (isRadioPlayerServiceConnected()) {
            radioPlayerView.startPlayingRadioStream();
            setPlayerStateAsPlaying();
        } else {
            radioPlayerView.showCouldNotPlayRadioStreamErrorMessage();
        }
    }

    @Override
    public void onRadioContentLoadSuccess(RadioContent radioContent) {
        NowPlayingTrack currentTrack = radioContent.getCurrentTrack();
        Dj currentDj = radioContent.getCurrentDj();

        radioPlayerView.showCurrentTrackTitle(currentTrack.getTitle());
        radioPlayerView.showCurrentDjName(currentDj.getName());
        radioPlayerView.showNumOfListeners(radioContent.getNumOfListeners());
    }

    @Override
    public void onRadioContentLoadFailed() {
        radioPlayerView.showCouldNotLoadRadioContentErrorMessage();
        pausePlayer();
    }

    public void onFailedToPlayStreamBroadcastReceived() {
        radioPlayerView.showCouldNotPlayRadioStreamErrorMessage();
        pausePlayer();
    }

    protected boolean isRadioPlayerServiceConnected() {
        return isRadioPlayerServiceConnected;
    }

    private void setPlayerStateAsPaused() {
        radioPlayerView.showPlayButton();
        isPlayerPlaying = false;
    }

    private void setPlayerStateAsPlaying() {
        radioPlayerView.showPauseButton();
        isPlayerPlaying = true;
    }


    public interface View {

        void startRadioPlayerService();

        void bindToRadioPlayerService();

        void unbindFromRadioPlayerService();

        void registerFailedToPlayStreamBroadcastReceiver();

        void unregisterFailedToPlayStreamBroadcastReceiver();

        void showPlayButton();

        void showPauseButton();

        void showCurrentTrackTitle(String title);

        void showCurrentDjName(String name);

        void showNumOfListeners(int numOfListeners);

        void startPlayingRadioStream();

        void stopPlayingRadioStream();

        void showCouldNotLoadRadioContentErrorMessage();

        void showCouldNotPlayRadioStreamErrorMessage();
    }
}
