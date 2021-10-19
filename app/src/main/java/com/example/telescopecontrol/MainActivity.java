package com.example.telescopecontrol;

import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import com.google.android.material.textfield.TextInputEditText;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

//Declararea variabilelor
public class MainActivity extends AppCompatActivity {

    ToggleButton btonoff, sleepmode;
    Button infocom, rangeplus, rangemin, speedplus, speedmin, intro, send, listDevices;
    ListView btlistview, msglistview;
    TextInputEditText textbar;
    TextView status,msg_box;
    ScrollView scroll;

    Intent btEnabingIntent;
    int requestCodeforEnable;

    public BluetoothSocket socket_global;

    BluetoothAdapter myBluetoothAdapter;
    BluetoothDevice[] btArray;
    SendReceive sendReceive;

    static final int STATE_LISTENING = 1;
    static final int STATE_CONNECTING = 2;
    static final int STATE_CONNECTED = 3;
    static final int STATE_CONNECTION_FAILED = 4;
    static final int STATE_MESSAGE_RECIEVED = 5;

    private static final String APP_NAME = "TelescopeControl";
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    //Legarea variabilelor de entitatile grafice
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        btonoff = (ToggleButton) findViewById(R.id.btonoff);
        infocom = (Button) findViewById(R.id.inf);
        btlistview = (ListView) findViewById(R.id.paired_devices_list);
        rangeplus = (Button) findViewById(R.id.rangeplus);
        rangemin = (Button) findViewById(R.id.rangemin);
        speedplus = (Button) findViewById(R.id.speedplus);
        speedmin = (Button) findViewById(R.id.speedmin);
        send = (Button) findViewById(R.id.send);
        sleepmode = (ToggleButton) findViewById(R.id.sleep);
        textbar = (TextInputEditText) findViewById(R.id.comand);
        status = (TextView) findViewById(R.id.status);
        listDevices = (Button) findViewById(R.id.listdevices);
        msg_box = (TextView) findViewById(R.id.msg);
        intro = (Button) findViewById(R.id.intromsg);
        scroll = (ScrollView) findViewById(R.id.scroll);
        myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        btEnabingIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        requestCodeforEnable = 1;
        //Declararea metodelor create
        startMethod();
        pairedDevicesMethod();
        rangeplusMethod();
        rangeminMethod();
        speedplusMethod();
        speedminMethod();
        sendinputMethod();
        infomethod();
        sleepmodeMethod();
        //Daca bluetooth este deja enable, switch-ul este on
        bluetoothonoffMethod();
        if (myBluetoothAdapter.isEnabled()) {
            btonoff.setChecked(true);
        }
        //Comenzile sunt invizibile inainte de conectare
        msg_box.setVisibility(View.INVISIBLE);
        rangeplus.setVisibility(View.INVISIBLE);
        rangemin.setVisibility(View.INVISIBLE);
        speedplus.setVisibility(View.INVISIBLE);
        speedmin.setVisibility(View.INVISIBLE);
        sleepmode.setVisibility(View.INVISIBLE);
        send.setVisibility(View.INVISIBLE);
        infocom.setVisibility(View.INVISIBLE);
        textbar.setVisibility(View.INVISIBLE);
        btlistview.setVisibility(View.INVISIBLE);
        scroll.setVisibility(View.INVISIBLE);
    }
    //Metoda pentru a trimite comenzi ce se afla in bara de text
    private void sendinputMethod() {
        send.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View v) {
                String string2 = "\n";
                String string= String.valueOf(textbar.getText());
                String string3 = "INPUT:";
                msg_box.append(string3);
                msg_box.append(string);
                msg_box.append(string2);
                sendReceive.write(string.getBytes());
                sendReceive.write(string2.getBytes());
                textbar.getText().clear();
            }
        });
    }
    //Se construieste lista de device-uri pentru conectare
    private void pairedDevicesMethod() {
        listDevices.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View v) {
                btlistview.setVisibility(View.VISIBLE);
                Set<BluetoothDevice> bt = myBluetoothAdapter.getBondedDevices();
                String[] strings = new String[bt.size()];
                btArray = new BluetoothDevice[bt.size()];
                int index = 0;
                if (bt.size() > 0) {
                    for (BluetoothDevice device : bt) {
                        btArray[index] = device;
                        strings[index] = device.getName();
                        index++;
                    }
                    ArrayAdapter<String> arrayAdapter_name = new ArrayAdapter<String>(getApplicationContext(), R.layout.list_black_text, R.id.list_content, strings);
                    btlistview.setAdapter(arrayAdapter_name);
                }
            }
        });
        //Se selecteaza device-ul si se incearca conectarea acestuia
        btlistview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ClientClass clientClass = new ClientClass(btArray[position]);
                clientClass.start();
                status.setText("Connecting");
            }
        });
    }
    //Metoda pentru comanda de +range
    private void rangeplusMethod() {
        rangeplus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String string = "+range\n";
                sendReceive.write(string.getBytes());
            }
        });
    }
    //Metoda pentru comanda de -range
    private void rangeminMethod() {
        rangemin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String string = "-range\n";
                sendReceive.write(string.getBytes());
            }
        });
    }
    //Metoda pentru comanda de +speed
    private void speedplusMethod() {
        speedplus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String string = "+speed\n";
                sendReceive.write(string.getBytes());
            }
        });
    }
    //Metoda pentru comanda de -range
    private void speedminMethod() {
        speedmin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String string = "-speed\n";
                sendReceive.write(string.getBytes());
            }
        });
    }
    //Metoda pentru comanda de info
    private void infomethod() {
        infocom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String string = "+info\n";
                sendReceive.write(string.getBytes());
            }
        });
    }
    //Un switch ce controleaza modurile de +sleep si -sleep
    private void sleepmodeMethod() {
        sleepmode.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (sleepmode.isChecked()) {
                    String string = "-sleep\n";
                    sendReceive.write(string.getBytes());
                } else {
                    String string = "+sleep\n";
                    sendReceive.write(string.getBytes());
                }
            }
        });
    }
    //Handler ce controleaza starea aplicatiei in timpul conectarii
    Handler handler = new Handler(new Handler.Callback() {
        @SuppressLint("ResourceAsColor")
        @Override
        public boolean handleMessage(Message msg) {

            switch (msg.what) {
                case STATE_LISTENING:
                    status.setText("Listening");
                    break;
                //Se conecteaza la aplicatie
                case STATE_CONNECTING:
                    status.setText("Connecting");
                    break;
                //Conectarea a fost reusita, restul butoanelor se fac vizibile
                case STATE_CONNECTED: {
                    status.setText("Connected");
                    sendReceive = new SendReceive(socket_global);
                    sendReceive.start();
                    msg_box.setVisibility(View.VISIBLE);
                    rangeplus.setVisibility(View.VISIBLE);
                    rangemin.setVisibility(View.VISIBLE);
                    speedplus.setVisibility(View.VISIBLE);
                    speedmin.setVisibility(View.VISIBLE);
                    sleepmode.setVisibility(View.VISIBLE);
                    send.setVisibility(View.VISIBLE);
                    infocom.setVisibility(View.VISIBLE);
                    textbar.setVisibility(View.VISIBLE);
                    scroll.setVisibility(View.VISIBLE);
                }
                break;
                //Daca conectarea a esuat, alertam acest lucru
                case STATE_CONNECTION_FAILED:
                    status.setText("Connection failed");
                    break;
                //Atunci cand se primesc mesaje, se scriu in casuta de text
                case STATE_MESSAGE_RECIEVED:
                    byte[] readBuffer= (byte[]) msg.obj;
                    String tempMsg=new String(readBuffer,0,msg.arg1);
                    msg_box.append(tempMsg);
                    break;
            }
            return true;
        }
    });
    //Metoda pentru activarea functiei de Bluetooth.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == requestCodeforEnable) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(getApplicationContext(), "Bluetooth is Enabled", Toast.LENGTH_LONG).show();
            } else if (resultCode == RESULT_CANCELED) {
                Toast.makeText(getApplicationContext(), "Bluetooth Enabling Canceled", Toast.LENGTH_LONG).show();
            }
        }
    }
    //Switch pentru activarea, dezactivarea functiei telefonului de Bluetooth
    private void bluetoothonoffMethod() {
        btonoff.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (btonoff.isChecked()) {
                    if (myBluetoothAdapter == null) {
                        Toast.makeText(getApplicationContext(), "Bluetooth does not support on this device", Toast.LENGTH_LONG).show();
                    } else if (!myBluetoothAdapter.isEnabled()) {
                        startActivityForResult(btEnabingIntent, requestCodeforEnable);
                    }
                } else {
                    if (myBluetoothAdapter.isEnabled()) {
                        myBluetoothAdapter.disable();
                        Toast.makeText(getApplicationContext(), "Bluetooth is disabled", Toast.LENGTH_LONG).show();
                    }
                }
            }

        });
    }
    //Butonul ce activeaza functia de popup
    private void startMethod() {
        intro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), PopActivity.class);
                startActivity(i);
            }
        });
    }
    //Se configureaza aplicatia ca si client in trimiterea si primirea datelor
    private class ClientClass extends Thread {
        private BluetoothDevice device;
        private BluetoothSocket socket;
        public ClientClass(BluetoothDevice device1) {
            device = device1;
            BluetoothSocket tmp = null;
            try {
                tmp = device.createRfcommSocketToServiceRecord(MY_UUID);
            } catch (IOException e) {
                e.printStackTrace();
            }
            socket = tmp;
            socket_global = tmp;
        }
        //Se testeaza conexiunea dintre dispozitive
        public void run() {
            try {
                socket.connect();
                Message message = Message.obtain();
                message.what = STATE_CONNECTED;
                handler.sendMessage(message);
            } catch (IOException e) {
                e.printStackTrace();
                Message message = Message.obtain();
                message.what = STATE_CONNECTION_FAILED;
                handler.sendMessage(message);
            }
        }
            public void cancel() {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
    }
    //Functia de trimitere, primire date
    private class SendReceive extends Thread {
        private final BluetoothSocket bluetoothSocket;
        private final InputStream inputStream;
        private final OutputStream outputStream;

        private SendReceive(BluetoothSocket socket) {
            bluetoothSocket = socket;
            InputStream tempIn = null;
            OutputStream tempOut = null;
            try {
                tempIn = bluetoothSocket.getInputStream();
                tempOut = bluetoothSocket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            inputStream = tempIn;
            outputStream = tempOut;
        }
        //Primirea datelor
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;
            while (true) {
                try {
                    SystemClock.sleep(100);
                    bytes = inputStream.available();
                    bytes = inputStream.read(buffer,0, bytes);
                    handler.obtainMessage(STATE_MESSAGE_RECIEVED,bytes, 0, buffer).sendToTarget();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        //Trimiterea datelor
        public void write(byte[] bytes) {
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}