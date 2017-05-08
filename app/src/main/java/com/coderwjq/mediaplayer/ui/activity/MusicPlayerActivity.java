package com.coderwjq.mediaplayer.ui.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.view.View;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.coderwjq.mediaplayer.R;
import com.coderwjq.mediaplayer.bean.MusicItem;
import com.coderwjq.mediaplayer.binder.MusicPlayerBinder;
import com.coderwjq.mediaplayer.common.Constant;
import com.coderwjq.mediaplayer.service.MusicPlayerService;
import com.coderwjq.mediaplayer.utils.StringUtils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

import static com.coderwjq.mediaplayer.common.Constant.PLAYMODE_ALL_REPEAT;
import static com.coderwjq.mediaplayer.common.Constant.PLAYMODE_RANDOM;
import static com.coderwjq.mediaplayer.common.Constant.PLAYMODE_SINGLE_REPEAT;

/**
 * @Created by coderwjq on 2017/5/8 9:14.
 * @Desc
 */

public class MusicPlayerActivity extends BaseActivity {

    private static final int MSG_UPDATE_POSITION = 0;
    @BindView(R.id.btn_back)
    ImageView mBtnBack;
    @BindView(R.id.audio_player_tv_title)
    TextView mAudioPlayerTvTitle;
    @BindView(R.id.audio_player_tv_arties)
    TextView mAudioPlayerTvArties;
    @BindView(R.id.audio_player_tv_position)
    TextView mAudioPlayerTvPosition;
    @BindView(R.id.audio_player_sk_position)
    SeekBar mAudioPlayerSkPosition;
    @BindView(R.id.audio_player_iv_playmode)
    ImageView mAudioPlayerIvPlaymode;
    @BindView(R.id.audio_player_iv_pre)
    ImageView mAudioPlayerIvPre;
    @BindView(R.id.audio_player_iv_pause)
    ImageView mAudioPlayerIvPause;
    @BindView(R.id.audio_player_iv_next)
    ImageView mAudioPlayerIvNext;
    @BindView(R.id.btn_show_list)
    ImageView mBtnShowList;
    private MusicPlayerBinder mBinder;
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);

            switch (msg.what) {
                case MSG_UPDATE_POSITION:
                    startUpdatePosition();
                    break;
            }
        }
    };

    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mBinder = (MusicPlayerBinder) service;

            // 更新播放模式使用的图片
            getPlayModeResource();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };
    private AudioReceiver mAudioReceiver;
    ;

    public static void invoke(Activity srcActivity, ArrayList<MusicItem> musicItems, int position) {
        Intent intent = new Intent(srcActivity, MusicPlayerActivity.class);
        intent.putParcelableArrayListExtra("music_list", musicItems);
        intent.putExtra("position", position);
        srcActivity.startActivity(intent);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_musice_player;
    }

    @Override
    protected void initView() {
        ButterKnife.bind(this);
    }

    @Override
    protected void initData() {
        Intent intent = new Intent(getIntent());
        intent.setClass(this, MusicPlayerService.class);
        bindService(intent, mServiceConnection, BIND_AUTO_CREATE);
        startService(intent);
    }

    @Override
    protected void initListener() {
        // 注册广播接收界面更新
        IntentFilter filter = new IntentFilter(Constant.ACTION_MUSIC_PREPARED);
        mAudioReceiver = new AudioReceiver();
        registerReceiver(mAudioReceiver, filter);
        mAudioPlayerSkPosition.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (!fromUser) return;

                mBinder.seekTo(progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                stopUpdatePosition();
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                startUpdatePosition();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mAudioReceiver);

        if (mServiceConnection != null) {
            unbindService(mServiceConnection);
        }
    }

    @OnClick({R.id.btn_back, R.id.audio_player_iv_playmode, R.id.audio_player_iv_pre,
            R.id.audio_player_iv_pause, R.id.audio_player_iv_next, R.id.btn_show_list})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_back:
                finish();
                break;
            case R.id.audio_player_iv_playmode:
                switchPlayMode();
                break;
            case R.id.audio_player_iv_pre:
                mBinder.playPreMusic();
                break;
            case R.id.audio_player_iv_pause:
                switchPlayButton();
                break;
            case R.id.audio_player_iv_next:
                mBinder.playNextMusic();
                break;
            case R.id.btn_show_list:
                finish();
                break;
        }
    }

    /**
     * 切换播放按钮
     */
    private void switchPlayButton() {
        mBinder.switchPlayButton();
        updatePlayButton();
    }

    /**
     * 切换播放模式
     */
    private void switchPlayMode() {
        mBinder.switchPlayMode();
        getPlayModeResource();
    }

    private void getPlayModeResource() {
        switch (mBinder.getPlayMode()) {
            case PLAYMODE_ALL_REPEAT:
                mAudioPlayerIvPlaymode.setImageResource(R.drawable.audio_playmode_allrepeat_selector);
                break;
            case PLAYMODE_SINGLE_REPEAT:
                mAudioPlayerIvPlaymode.setImageResource(R.drawable.audio_playmode_singlerepeat_selector);
                break;
            case PLAYMODE_RANDOM:
                mAudioPlayerIvPlaymode.setImageResource(R.drawable.audio_playmode_random_selector);
                break;
        }
    }

    private void updatePlayButton() {
        if (mBinder.isPlaying()) {
            mAudioPlayerIvPause.setImageResource(R.drawable.audio_pause_selector);
            startUpdatePosition();
        } else {
            mAudioPlayerIvPause.setImageResource(R.drawable.audio_play_selector);
            stopUpdatePosition();
        }
    }

    private void stopUpdatePosition() {
        mHandler.removeMessages(MSG_UPDATE_POSITION);
    }

    private void startUpdatePosition() {
        // 获取播放进度
        int position = mBinder.getCurrentPosition();
        updatePostion(position);

        // 发送一个延迟更新的消息
        mHandler.sendEmptyMessageDelayed(MSG_UPDATE_POSITION, 500);
    }

    private void updatePostion(int position) {
        int duration = mBinder.getDuration();

        // 更新显示
        mAudioPlayerTvPosition.setText(StringUtils.formatDuration(position) + "/" + StringUtils.formatDuration(duration));
        mAudioPlayerSkPosition.setProgress(position);
    }

    private final class AudioReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            if (Constant.ACTION_MUSIC_PREPARED.equals(intent.getAction())) {
                // 音乐开始播放
                updatePlayButton();

                // 更新歌曲信息
                MusicItem audioItem = intent.getParcelableExtra("music_item");
                mAudioPlayerTvTitle.setText(audioItem.getTitle());
                mAudioPlayerTvArties.setText(audioItem.getArtist());

                // 开启进度更新
                mAudioPlayerSkPosition.setMax(mBinder.getDuration());
                startUpdatePosition();

            } else if (Constant.ACTION_MUSIC_COMPLETED.equals(intent.getAction())) {
                // 播放结束
                mHandler.removeMessages(MSG_UPDATE_POSITION);

                // 更新暂停按钮使用的图片
                updatePlayButton();
            }
        }
    }
}
