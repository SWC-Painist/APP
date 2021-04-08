package com.app.painist.AIUnit;

import android.content.Context;
import android.util.Log;

import com.aiunit.audio.common.AudioInputSlot;
import com.aiunit.audio.common.AudioOutputSlot;
import com.aiunit.audio.common.ConnectionCallback;
import com.aiunit.common.protocol.audio.AudioScenes;
import com.coloros.ocs.ai.audio.AudioUnit;
import com.coloros.ocs.ai.audio.AudioUnitClient;
import com.coloros.ocs.base.common.ConnectionResult;
import com.coloros.ocs.base.common.api.OnConnectionFailedListener;
import com.coloros.ocs.base.common.api.OnConnectionSucceedListener;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;

public class ASCUtil {
    AudioUnitClient mFBank;
    AudioUnitClient mASC;

    AudioInputSlot inputSlot;
    AudioOutputSlot outputSlot;

    public void initialize(Context context) {
        mASC = AudioUnit.getAscClient(context).addOnConnectionSucceedListener(new OnConnectionSucceedListener() {
            @Override
            public void onConnectionSucceed() {
                Log.i("TAG", " authorize connect: onConnectionSucceed");
            }
        }).addOnConnectionFailedListener(new OnConnectionFailedListener() {
            @Override
            public void onConnectionFailed(ConnectionResult connectionResult) {
                Log.e("TAG", " authorize connect: onFailure: " + connectionResult.getErrorCode());
            }
        });

        mFBank = AudioUnit.getFBankClient(context).addOnConnectionSucceedListener(new OnConnectionSucceedListener() {
            @Override
            public void onConnectionSucceed() {
                Log.i("TAG", " authorize connect: onConnectionSucceed");
            }
        }).addOnConnectionFailedListener(new OnConnectionFailedListener() {
            @Override
            public void onConnectionFailed(ConnectionResult connectionResult) {
                Log.e("TAG", " authorize connect: onFailure: " + connectionResult.getErrorCode());
            }
        });
    }

    public void connectToAIUnitServer(Context context, OnServerConnectCompleteListener listener) {
        Log.d("AI Unit", "Connecting to AI Unit Server");
        mASC.initService(context, new ConnectionCallback() {
            @Override
            public void onServiceConnect() {
                int startASCCode = mASC.start();
                int startFBankCode = mFBank.start();
                Log.d("Start ASC code", String.valueOf(startASCCode));
                Log.d("Start FBank code", String.valueOf(startFBankCode));
                listener.onComplete();
            }

            @Override
            public void onServiceDisconnect() { }
        });
    }

    public void setInput(byte[] pcmData) {
        byte[] pcm = new byte[2048];
        inputSlot = (AudioInputSlot) mFBank.createInputSlot();
        inputSlot.setRawData(pcm);
    }

    public void setOutput() {
        outputSlot = (AudioOutputSlot) mFBank.createOutputSlot();
    }

    int NUM_BINS = 64;
    int NUM_FRAMES = 310;
    int mPingpongAIndex = 0;
    float[][] mPingpang_buff_a = new float[NUM_BINS][NUM_FRAMES];
    int mPingpongBIndex = 0;
    float[][] mPingpang_buff_b = new float[NUM_BINS][NUM_FRAMES];
    ByteBuffer mInputData = null;

    int mIndex = 0;
    private final String[] mSenenLabel = {"street_traffic",
            "indoor", "indoor", "indoor", "metro", "metrodoor", "indoor", "indoor", "oven", "water", "familymart"};
    List<String> mResultList = new ArrayList<String>(); //最终的结果
    boolean resultUpdated;

    public synchronized boolean resultJustUpdate() {
        if (resultUpdated) {
            resultUpdated = false;
            return true;
        }
        else return false;
    }

    public void process(OnProcessCompleteListener listener) {
        mFBank.process(inputSlot, outputSlot);
        AudioScenes audioScenes = outputSlot.getFBankScenes();

        mInputData = ByteBuffer.allocateDirect(NUM_BINS * NUM_FRAMES * 4);
        mInputData.order(ByteOrder.LITTLE_ENDIAN);

        if (audioScenes != null) {
            float[] array = new float[audioScenes.getFeature().size()];
            for (int i = 0; i < array.length; i++) {
                array[i] = audioScenes.getFeature().get(i);
            }

            for (int i = 0; i < array.length / NUM_BINS; i++) {
                fillPingPongBuffer(array, i);
            }
        }
        listener.onComplete();
        resultUpdated = true;
    }

    public List<String> getResultList() {
        return mResultList;
    }

    private void fillPingPongBuffer(float[] features, int index) {
        for (int i = 0; i < NUM_BINS; i++) {
            if (mPingpongAIndex < NUM_FRAMES) {
                mPingpang_buff_a[i][mPingpongAIndex] = features[i + index*NUM_BINS];
            } else if (mPingpongBIndex < NUM_FRAMES) {
                mPingpang_buff_b[i][mPingpongBIndex] = features[i + index*NUM_BINS];
            }
        }
        if (mPingpongAIndex < NUM_FRAMES) {
            mPingpongAIndex++;
            if (mPingpongAIndex == NUM_FRAMES) {
                mPingpongBIndex = 0;

                for (int i = 0; i < NUM_BINS; i++) {
                    for (int j = 0; j < NUM_FRAMES; j++) {
                        mInputData.putFloat((i*NUM_FRAMES + j)*4, mPingpang_buff_a[i][j]);
                    }
                }

                AudioInputSlot inputSlot = (AudioInputSlot) mASC.createInputSlot();
                AudioOutputSlot outputSlot = (AudioOutputSlot) mASC.createOutputSlot();
                inputSlot.setRawData(mInputData.array());
                mASC.process(inputSlot, outputSlot);

                AudioScenes audioScenes = outputSlot.getASCScenes();
                int scene = audioScenes.getScenes().value();

                mIndex++;
                mResultList.add(0,mIndex+mSenenLabel[scene]);
            }
        } else if (mPingpongBIndex < NUM_FRAMES) {

            mPingpongBIndex++;
            if (mPingpongBIndex == NUM_FRAMES) {
                mPingpongAIndex = 0;
                for (int i = 0; i < NUM_BINS; i++) {
                    for (int j = 0; j < NUM_FRAMES; j++) {
                        mInputData.putFloat((i*NUM_FRAMES + j)*4, mPingpang_buff_b[i][j]);
                    }
                }

                AudioInputSlot inputSlot = (AudioInputSlot) mASC.createInputSlot();
                AudioOutputSlot outputSlot = (AudioOutputSlot) mASC.createOutputSlot();
                inputSlot.setRawData(mInputData.array());
                mASC.process(inputSlot, outputSlot);

                AudioScenes audioScenes = outputSlot.getASCScenes();
                int scene = audioScenes.getScenes().value();

                mIndex++;
                mResultList.add(0,mIndex+mSenenLabel[scene]);

            }
        }
    }

    public void release() {
        if (mFBank != null)
            mFBank.stop();
        if (mASC != null) {
            mASC.stop();
            mASC.releaseService();
            mASC = null;
        }
    }

    public interface OnServerConnectCompleteListener {
        void onComplete();
    }

    public interface OnProcessCompleteListener {
        void onComplete();
    }
}
