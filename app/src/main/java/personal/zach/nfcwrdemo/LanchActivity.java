package personal.zach.nfcwrdemo;

import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class LanchActivity extends AppCompatActivity {
    private EditText etWriteMsg;
    private Button btnReadyWrite;

    private NfcAdapter nfcAdapter;
    private String[][] techListsArray;
    private IntentFilter[] intentFiltersArray;
    private PendingIntent pendingIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lanch);
        etWriteMsg = (EditText)findViewById(R.id.et_wirteMsg);
        btnReadyWrite = (Button)findViewById(R.id.btn_readyWrite);
        btnReadyWrite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                if(etWriteMsg.getText().length()>0){
                    Intent intent = new Intent(LanchActivity.this,MainActivity.class);
//                    intent.putExtra("msg",etWriteMsg.getText().toString());
                    startActivity(intent);
                finish();
//                }
            }
        });
//
//        pendingIntent = PendingIntent.getActivity(
//                this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
//        techListsArray = new String[][]{new String[]{MifareClassic.class.getName()}};
//        IntentFilter ndef = new IntentFilter();
//        ndef.addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
//        intentFiltersArray = new IntentFilter[]{ndef};
//        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
    }

//    @Override
//    protected void onPause() {
//        super.onPause();
//        nfcAdapter.disableForegroundDispatch(this);
//    }
//
//    @Override
//    protected void onResume() {
//        super.onResume();
//        nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, techListsArray);
//    }
}
