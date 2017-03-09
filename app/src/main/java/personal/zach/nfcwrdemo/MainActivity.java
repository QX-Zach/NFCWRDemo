package personal.zach.nfcwrdemo;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.MifareClassic;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.nio.charset.Charset;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
    private TextView tvResult;
    private TextView tvStatus;
    private TextView tvReadData;
    private Button btnWrite;
    private Button btnRead;
    private EditText etWriteMsg;
    private NfcAdapter nfcAdapter;
    private String[][] techListsArray;
    private IntentFilter[] intentFiltersArray;
    private PendingIntent pendingIntent;
    private MifareClassic mfc;
    private Spinner spinner;
    private int writeBlock = 1;
    private String[] strArray;
    String intentMsg = "";
    NdefMessage[] msgs;
    public static final byte[] write_pass =
            {(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00,(byte)0x00};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        tvResult = (TextView) findViewById(R.id.tv_result);
        tvStatus = (TextView) findViewById(R.id.tv_status);
        tvReadData = (TextView) findViewById(R.id.tv_readData);
        btnWrite = (Button) findViewById(R.id.btn_write);
        etWriteMsg = (EditText) findViewById(R.id.et_wirteMsg);
        btnRead = (Button) findViewById(R.id.btn_Read);
        spinner = (Spinner) findViewById(R.id.spinner);
        btnWrite.setOnClickListener(this);
        btnRead.setOnClickListener(this);
        try {
            strArray = getResources().getStringArray(R.array.writeblock);
        } catch (Exception ex) {

        }

        //检查设备时候有卡模拟组件
        Log.e(TAG, "onCreate: 卡模拟组件："+getPackageManager().hasSystemFeature(PackageManager.FEATURE_NFC_HOST_CARD_EMULATION));

//        intentMsg = getIntent().getStringExtra("msg");
//        etWriteMsg.setText(intentMsg);

        pendingIntent = PendingIntent.getActivity(
                this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        techListsArray = new String[][]{new String[]{MifareClassic.class.getName()}};
        IntentFilter ndef = new IntentFilter();
        ndef.addAction(NfcAdapter.ACTION_TECH_DISCOVERED);
        intentFiltersArray = new IntentFilter[]{ndef};
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            tvResult.setText("设备不支持NFC！");
            return;
        }
        if (!nfcAdapter.isEnabled()) {
            tvResult.setText("请在系统设置中启用NFC功能！");
            return;
        }

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.e(TAG, "onItemClick: " + position);
                if (strArray != null) {
                    writeBlock = Integer.parseInt(strArray[position]);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

    }

    @Override
    protected void onNewIntent(Intent intent) {
        processIntent(intent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        nfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, intentFiltersArray, techListsArray);
        if (getIntent().getAction() != null) {
            if (NfcAdapter.ACTION_TECH_DISCOVERED.endsWith(getIntent().getAction())) {
                processIntent(getIntent());
            }
        }
    }


    @TargetApi(19)
    private void processIntent(Intent intent) {
        tvResult.setText("");
        try{
            byte[] myNFCID = intent.getByteArrayExtra(NfcAdapter.EXTRA_ID);
            Log.e(TAG, "processIntent: "+new String(myNFCID));
            Log.e(TAG, "processIntent: "+ Convert.getHexString(myNFCID, myNFCID.length));

        }catch (Exception ex){
            Log.e(TAG, "UID_Ex: "+ex.getMessage());
        }
        Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
        if (rawMsgs != null) {
            msgs = new NdefMessage[rawMsgs.length];
            for (int i = 0; i < rawMsgs.length; i++) {
                msgs[i] = (NdefMessage) rawMsgs[i];
            }
        }

        Log.e(TAG, "processIntent: type:"+Convert.getHexString(msgs[0].getRecords()[0].getType(),msgs[0].getRecords()[0].getType().length));
        Log.e(TAG, "processIntent: msgsSize:"+msgs.length+" ,msg0Size:"+msgs[0].getRecords().length);
        Log.e(TAG, "processIntent: msgs:"+Convert.getHexString(msgs[0].getRecords()[0].getPayload(),msgs[0].getRecords()[0].getPayload().length));
        Log.e(TAG, "processIntent: msgs:"+new String(msgs[0].getRecords()[0].getPayload()));
        tvResult.append("id:"+new String(msgs[0].getRecords()[0].getType())+"\n");
        tvResult.append("tnf:"+String.valueOf(msgs[0].getRecords()[0].getTnf())+"\n");
        tvResult.append("msg:"+new String(msgs[0].getRecords()[0].getPayload())+"\n");
        NdefMessage ndfmsg = new NdefMessage(NdefRecord.createApplicationRecord(getPackageName()));

        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        tvResult.append("CardID:"+Convert.getHexString(tag.getId(),tag.getId().length));
        for(String tech:tag.getTechList()){
            Log.e(TAG, "processIntent: tech:"+tech);
        }
        mfc = MifareClassic.get(tag);
//        IsoDep isodep = IsoDep.get(tag);
        try {
//            if (!mfc.isConnected())
            mfc.close();
            mfc.connect();
            int sectorCount = mfc.getSectorCount();
            int blockCount = mfc.getBlockCount();
            int size = mfc.getSize();
            tvResult.append("\n扇区数量：" + sectorCount + "\nblock：" + blockCount + "\n存储空间：" + size);
            writeData(mfc, writeBlock, etWriteMsg.getText().toString());

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            readData(mfc);
            if (mfc.authenticateSectorWithKeyA(0, write_pass)) {
                tvResult.append("\n第0 block数据：" + Convert.getHexString(mfc.readBlock(0),mfc.readBlock(0).length));
                tvResult.append("\n第1 block数据：" + new String(mfc.readBlock(1)));
                tvResult.append("\n第2 block数据：" + new String(mfc.readBlock(2)));
            }

//            byte[] temp = "遇见".getBytes(Charset.forName("UTF-8"));
//            Log.e(TAG, "processIntent_tempSize: " + temp.length);
//            final byte[] write = new byte[MifareClassic.BLOCK_SIZE];
//            for (int i = 0; i < MifareClassic.BLOCK_SIZE; i++) {
//                if (i < temp.length)
//                    write[i] = temp[i];
//                else
//                    write[i] = 0;
//            }
//            new Thread(new Runnable() {
//                @Override
//                public void run() {
//                    try {
//                        Log.e(TAG, "run: " + MifareClassic.BLOCK_SIZE + " ,value:" + new String(write));
//                        mfc.writeBlock(1, write);
//                    } catch (Exception ex) {
//                        Log.e(TAG, "writeBlock_Ex: " + ex.getMessage());
//                    } finally {
//                        try {
//                            mfc.close();
//                        } catch (Exception ex) {
//                            Log.e(TAG, "run_Ex: " + ex.getMessage());
//                        }
//                    }
//                }
//            }).start();
        } catch (IOException ex) {
            Log.e(TAG, "processIntent_Ex: " + ex.getMessage());
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_write:
                if (etWriteMsg.getText().length() > 0) {
                    writeData(mfc, 1, etWriteMsg.getText().toString());
                }
                break;
            case R.id.btn_Read:
                readData(mfc);
                break;
        }

    }

    private void readData(final MifareClassic mfc) {
        if (mfc != null) {
            if (mfc.isConnected()) {
                try {

                    String result = "";
                    tvStatus.setText("正在读取数据");
                    for (int i = 0; i < mfc.getSectorCount(); i++) {//循环读取所有的扇区
                        int bindex;
                        int bCount;
                        if (mfc.authenticateSectorWithKeyA(i, MifareClassic.KEY_NFC_FORUM)) {//读取的时候要校验key，否则无法读取
                            bindex = mfc.sectorToBlock(i);
                            bCount = mfc.getBlockCountInSector(i);
                            result += "Sector " + i + "验证成功\n";
                            for (int j = 0; j < bCount; j++) {//循环读取指定扇区所有的块，每个扇区最后一个块是该块的key，除非要加密，否则不要轻易改变
                                byte[] data = mfc.readBlock(bindex);
                                result += "Block " + bindex + " : " + new String(data, Charset.forName("UTF-8")) + "\n";
//                                result += "Block " + bindex + " : " + Convert.getHexString(data,data.length) + "\n";
                                bindex++;
                            }
                        }else if(mfc.authenticateSectorWithKeyB(i,MifareClassic.KEY_DEFAULT)){
                            bindex = mfc.sectorToBlock(i);
                            bCount = mfc.getBlockCountInSector(i);
                            result += "Sector " + i + "验证成功\n";
                            for (int j = 0; j < bCount; j++) {//循环读取指定扇区所有的块，每个扇区最后一个块是该块的key，除非要加密，否则不要轻易改变
                                byte[] data = mfc.readBlock(bindex);
                                result += "Block " + bindex + " : " + new String(data, Charset.forName("UTF-8")) + "\n";
//                                result += "Block " + bindex + " : " + Convert.getHexString(data,data.length) + "\n";
                                bindex++;
                            }
                        }
                    }
                    tvStatus.setText("数据读取完成");
                    tvReadData.setText(result);
                } catch (Exception ex) {
                    tvStatus.setText("读取失败：" + ex.getMessage());
                }
            }
        } else {
            Toast.makeText(this, "未扫描到NFC", Toast.LENGTH_SHORT).show();
        }
    }

    private void writeData(final MifareClassic mfc, final int blockIndex, String msg) {
        if (mfc != null) {
            try {
                Log.e(TAG, "writeData: isConnected:" + mfc.isConnected());
                if (mfc.isConnected()) {
                    byte[] temp = msg.getBytes(Charset.forName("UTF-8"));
                    final byte[] write = new byte[MifareClassic.BLOCK_SIZE];//每一块最大存储字节数
                    for (int i = 0; i < MifareClassic.BLOCK_SIZE; i++) {
                        if (i < temp.length)
                            write[i] = temp[i];
                        else
                            write[i] = 0;
                    }
                    final int sectorIndex = mfc.blockToSector(blockIndex);
                    tvStatus.setText("正在写入");
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Log.e(TAG, "run: 写入内容：" + new String(write));
                                if (mfc.authenticateSectorWithKeyA(sectorIndex, MifareClassic.KEY_NFC_FORUM))
                                    mfc.writeBlock(blockIndex, write);//写入方法是一个阻塞函数，不能在UI线程调用
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvStatus.setText("写入完成");
                                    }
                                });
                            } catch (final Exception ex) {
                                Log.e(TAG, "writeData_Ex: " + ex.getMessage());
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        tvStatus.setText("写入失败：" + ex.getMessage());
                                    }
                                });
                            }
                        }
                    }).start();
                } else {
                    Toast.makeText(this, "NFC连接断开", Toast.LENGTH_SHORT).show();
                }
            } catch (Exception ex) {
                Log.e(TAG, "writeData: " + ex.getMessage());
            }
        } else {
            Toast.makeText(this, "未扫描到NFC卡片", Toast.LENGTH_SHORT).show();
        }
    }


}
