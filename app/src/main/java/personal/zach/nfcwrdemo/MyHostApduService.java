package personal.zach.nfcwrdemo;

import android.annotation.TargetApi;
import android.nfc.cardemulation.HostApduService;
import android.os.Bundle;
import android.util.Log;

@TargetApi(19)
public class MyHostApduService extends HostApduService {
    private static final String TAG = "MyHostApduService";
    public MyHostApduService() {
    }

    @Override
    public byte[] processCommandApdu(byte[] commandApdu, Bundle extras) {
        Log.e(TAG, "processCommandApdu: "+extras.toString());
        return "Hello World".getBytes();
    }

    @Override
    public void onDeactivated(int reason) {

    }
}
