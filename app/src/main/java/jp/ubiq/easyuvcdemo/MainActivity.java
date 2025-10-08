package jp.ubiq.easyuvcdemo;

import android.hardware.usb.UsbDevice;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.serenegiant.usb.USBMonitor;
import com.serenegiant.usb.UVCCamera;
import com.serenegiant.widget.UVCCameraTextureView;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "EasyUVC";

    private USBMonitor mUSBMonitor;
    private UVCCameraTextureView mCameraView;
    private UVCCamera mCamera;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCameraView = findViewById(R.id.camera_view);

        // ✅ USBモニタ初期化
        mUSBMonitor = new USBMonitor(this, new USBMonitor.OnDeviceConnectListener() {
            @Override
            public void onAttach(UsbDevice device) {
                Toast.makeText(MainActivity.this, "USBカメラ接続検出", Toast.LENGTH_SHORT).show();
                mUSBMonitor.requestPermission(device);
            }

            @Override
            public void onDettach(UsbDevice device) {
                Toast.makeText(MainActivity.this, "USBカメラ切断", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onConnect(UsbDevice device, USBMonitor.UsbControlBlock ctrlBlock, boolean createNew) {
                Log.i(TAG, "USBカメラ接続完了");

                releaseCamera(); // 二重接続防止

                mCamera = new UVCCamera();
                mCamera.open(ctrlBlock);
                mCamera.setPreviewTexture(mCameraView.getSurfaceTexture());
                mCamera.startPreview();

                Toast.makeText(MainActivity.this, "プレビュー開始", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDisconnect(UsbDevice device, USBMonitor.UsbControlBlock ctrlBlock) {
                Toast.makeText(MainActivity.this, "USBカメラ切断", Toast.LENGTH_SHORT).show();
                releaseCamera();
            }

            @Override
            public void onCancel(UsbDevice device) {
                Toast.makeText(MainActivity.this, "USB接続キャンセル", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        mUSBMonitor.register();  // ✅ USB監視開始
    }

    @Override
    protected void onStop() {
        super.onStop();
        mUSBMonitor.unregister(); // ✅ USB監視停止
        releaseCamera();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mUSBMonitor != null) {
            mUSBMonitor.destroy();
            mUSBMonitor = null;
        }
    }

    private void releaseCamera() {
        if (mCamera != null) {
            try {
                mCamera.stopPreview();
                mCamera.destroy();
            } catch (Exception e) {
                e.printStackTrace();
            }
            mCamera = null;
        }
    }
}
