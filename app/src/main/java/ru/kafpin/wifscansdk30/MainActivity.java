package ru.kafpin.wifscansdk30;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.google.android.material.snackbar.Snackbar;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private ElementMini [] nets;
    private WifiManager wifiManager;
    private List<ScanResult> wifiList;
    private boolean state = false;

    private ListView netList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);


        Button fab = (Button) findViewById(R.id.fab);
        netList = (ListView) findViewById(R.id.listItem);

        fab.setBackgroundColor(Color.GREEN);
        fab.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceAsColor")
            @Override
            public void onClick(View view) {
                if(!state){
                    fab.setText("Stop");
                    fab.setBackgroundColor(Color.RED);
                    detectWifi();
                    Snackbar.make(view, "Сканирование...", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
                    state = true;
                }
                else{
                    fab.setText("Start");
                    fab.setBackgroundColor(Color.GREEN);
                    state=false;
                    wifiList.clear();
                    netList.setAdapter(null);
                }

            }
        });
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
//            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
//            return insets;
//        });
    }
    public void detectWifi(){
        this.wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        this.wifiManager.startScan();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        this.wifiList = this.wifiManager.getScanResults();

        Log.d("TAG", wifiList.toString());

        this.nets = new ElementMini[wifiList.size()];

        for (int i = 0; i<wifiList.size(); i++){
            String item = wifiList.get(i).toString();
            String[] vector_item = item.split(",");
            String item_essid = vector_item[0];
            String item_level = vector_item[3];


            String ssid;
            if(item_essid.split(":")[1].isEmpty())
                ssid="empty";
            else
                ssid=item_essid.split(":")[1].trim();

            String level;
            if(item_level.split(":")[1].isEmpty())
                 level = "empty";
            else
                level = item_level.split(":")[1].trim();

            nets[i] = new ElementMini(ssid, level);
        }

        AdapterElements adapterElements = new AdapterElements(this);
        netList.setAdapter(adapterElements);
    }

    class AdapterElements extends ArrayAdapter<Object> {
        Activity context;

        public AdapterElements(Activity context) {
            super(context, R.layout.items, nets);
            this.context = context;
        }

        @SuppressLint("SetTextI18n")
        public View getView(int position, View convertView, ViewGroup parent){
            LayoutInflater inflater = context.getLayoutInflater();
            View item = inflater.inflate(R.layout.items, null);

            TextView tvSsid = (TextView) item.findViewById(R.id.tvSSID);
            tvSsid.setText(nets[position].getTitle());

            TextView tvLevel = (TextView)item.findViewById(R.id.tvLevel);
            String level = nets[position].getLevel();
            try{
                int i = Integer.parseInt(level.trim());
                if (i>-50){
                    tvLevel.setText("Высокий: " + i);
                } else if (i<=-50 && i>-80){
                    tvLevel.setText("Средний: " + i);
                } else if (i<=-80){
                    tvLevel.setText("Низкий: " + i);
                }
            } catch (NumberFormatException e){
                Log.d("TAG", "Неверный формат строки");
            }
            return item;
        }
    }
}