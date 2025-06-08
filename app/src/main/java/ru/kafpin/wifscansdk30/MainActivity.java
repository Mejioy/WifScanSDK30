package ru.kafpin.wifscansdk30;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.net.wifi.rtt.WifiRttManager;
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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.android.material.snackbar.Snackbar;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private ElementMini [] nets;
    private WifiManager wifiManager;
    private List<ScanResult> wifiList;
    private int tickCounter = 0;
    private List<Result> results = new ArrayList<>();
    final String wifi1 = "AstafievLab9";
    final String wifi2 = "StaticStand24";
    final String wifi3 = "StaticStandSlave24";

    private ListView netList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        System.out.println(getFilesDir());
        Button fab = (Button) findViewById(R.id.fab);
        Button clear = (Button) findViewById(R.id.clear);
        Button save = (Button) findViewById(R.id.save);
        netList = (ListView) findViewById(R.id.listItem);

        fab.setBackgroundColor(Color.GREEN);
        save.setBackgroundColor(Color.BLUE);

        clear.setOnClickListener(view -> {
            Snackbar.make(view, "Очистка...", Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show();
            netList.setAdapter(null);
            tickCounter=0;
            results.clear();
        });

        save.setOnClickListener(view -> {
            if (!results.isEmpty()){
                Snackbar.make(view, "Сохранение...", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
                ObjectMapper mapper = new ObjectMapper();
                String filePath = getFilesDir()+"/"+LocalDateTime.now().toString()+".json";

                File file = new File(filePath);
                Set<PosixFilePermission> permissions = EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE, PosixFilePermission.OTHERS_READ, PosixFilePermission.GROUP_READ);
                try {
                // Создание файла, если его не существует
                    if (!file.exists()) {
                        file.createNewFile();
                        Files.setPosixFilePermissions(Paths.get(filePath),permissions);
                        mapper.writeValue(file, results);
                    }
                }
                catch (IOException e) {
                    Log.i("INF", "Ошибка при создании файла: " + e.getMessage());
                }
            }
            else{
                Snackbar.make(view, "Список пуст", Snackbar.LENGTH_SHORT)
                        .setAction("Action", null).show();
            }
        });


        fab.setOnClickListener(view -> {
            Snackbar.make(view, "Сканирование...", Snackbar.LENGTH_SHORT)
                    .setAction("Action", null).show();
            detectWifi(view);
        });
    }

    private void detectWifi(View view){
        this.wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);

        while(tickCounter<1200){
            this.wifiManager.startScan();
            if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            this.wifiList = this.wifiManager.getScanResults();
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
                if(nets[i].getTitle().equals(wifi1)||nets[i].getTitle().equals(wifi2)||nets[i].getTitle().equals(wifi3))
                    results.add(new Result(nets[i].getTitle(),nets[i].getLevel(),tickCounter));
            }
            try {
                Thread.sleep(250);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            tickCounter+=1;
        }

        AdapterElements adapterElements = new AdapterElements(this);
        netList.setAdapter(adapterElements);
        Snackbar.make(view, "Сканирование завершено", Snackbar.LENGTH_SHORT)
                .setAction("Action", null).show();

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