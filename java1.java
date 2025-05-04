package com.example.handtracking;

import android.os.Bundle;
import com.google.mediapipe.framework.PacketGetter;
import com.google.mediapipe.components.FrameProcessor;
import com.google.mediapipe.components.ExternalTextureConverter;
import com.google.mediapipe.glutil.EglManager;
import com.google.mediapipe.framework.Packet;

import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "HandTracking";
    private static final String BINARY_GRAPH_NAME = "hand_tracking_gpu.binarypb";
    private static final String INPUT_STREAM_NAME = "input_video";
    private static final String OUTPUT_STREAM_NAME = "hand_landmarks";

    private FrameProcessor processor;
    private EglManager eglManager;
    private ExternalTextureConverter converter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        eglManager = new EglManager(null);
        processor = new FrameProcessor(
                this,
                eglManager.getNativeContext(),
                BINARY_GRAPH_NAME,
                INPUT_STREAM_NAME,
                OUTPUT_STREAM_NAME
        );

        // 손 랜드마크 데이터를 받을 리스너 설정
        processor.getVideoSurfaceOutput().setFlipY(true);
        processor.addPacketCallback(OUTPUT_STREAM_NAME, packet -> {
            try {
                // 21개의 손가락 landmark 정보 (x, y, z)
                List<NormalizedLandmarkList> landmarkLists =
                        PacketGetter.getProtoVector(packet, NormalizedLandmarkList.parser());

                for (NormalizedLandmarkList landmarks : landmarkLists) {
                    for (int i = 0; i < landmarks.getLandmarkCount(); i++) {
                        NormalizedLandmark landmark = landmarks.getLandmark(i);
                        Log.d(TAG, "Landmark " + i + ": (" +
                                landmark.getX() + ", " +
                                landmark.getY() + ", " +
                                landmark.getZ() + ")");
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Failed to parse landmark packet", e);
            }
        });

        // TODO: 카메라 권한 요청 및 OpenGL 뷰 연결
    }

    @Override
    protected void onResume() {
        super.onResume();
        converter = new ExternalTextureConverter(eglManager.getContext());
        converter.setFlipY(true);
        converter.setConsumer(processor);
    }

    @Override
    protected void onPause() {
        super.onPause();
        converter.close();
    }
}
