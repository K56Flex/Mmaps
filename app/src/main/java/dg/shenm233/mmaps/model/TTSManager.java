/*
 * Copyright 2016 Shen Zhang
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dg.shenm233.mmaps.model;

import android.content.Context;
import android.os.Bundle;

import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;

public class TTSManager {
    private static TTSManager mTTSManger;

    private Context mContext;
    private boolean isFinished = true;

    private SpeechSynthesizer mSpeechSynthesizer;

    private TTSManager(Context context) {
        mContext = context;
        init();
    }

    private void init() {
        SpeechUtility.createUtility(mContext, SpeechConstant.APPID + "=" + "我没有哦");
        mSpeechSynthesizer = SpeechSynthesizer.createSynthesizer(mContext, null);
        initSpeechSynthesizer();
    }

    private void initSpeechSynthesizer() {
        // 设置发音人
        mSpeechSynthesizer.setParameter(SpeechConstant.VOICE_NAME, "xiaoyan");
        // 设置语速
        mSpeechSynthesizer.setParameter(SpeechConstant.SPEED, "50");
        // 设置音量
        mSpeechSynthesizer.setParameter(SpeechConstant.VOLUME, "50");
        // 设置语调
        mSpeechSynthesizer.setParameter(SpeechConstant.PITCH, "50");
    }

    public static TTSManager getInstance(Context context) {
        if (mTTSManger == null) {
            mTTSManger = new TTSManager(context);
        }
        return mTTSManger;
    }

    public void destroy() {
        if (mSpeechSynthesizer != null) {
            mSpeechSynthesizer.stopSpeaking();
        }
    }

    public void speakText(String s) {
        if (!isFinished) {
            return;
        }
        // 进行语音合成.
        mSpeechSynthesizer.startSpeaking(s, mSynthesizerListener);
    }

    public void stopSpeaking() {
        if (mSpeechSynthesizer != null) {
            mSpeechSynthesizer.stopSpeaking();
        }
    }

    private SynthesizerListener mSynthesizerListener = new SynthesizerListener() {
        @Override
        public void onSpeakBegin() {
            isFinished = false;
        }

        @Override
        public void onBufferProgress(int i, int i1, int i2, String s) {

        }

        @Override
        public void onSpeakPaused() {

        }

        @Override
        public void onSpeakResumed() {

        }

        @Override
        public void onSpeakProgress(int i, int i1, int i2) {

        }

        @Override
        public void onCompleted(SpeechError speechError) {
            isFinished = true;
        }

        @Override
        public void onEvent(int i, int i1, int i2, Bundle bundle) {

        }
    };
}
