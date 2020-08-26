package com.example.led_control;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.AsyncTask;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.FileAlreadyExistsException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;


public class ledControl extends AppCompatActivity implements DialogTaskClass.DialogListener {


    public int task_n = 0;
    public static final String SHARED_PREFS = "sharedPrefs";


    Button btnBat, btnReset;
    FloatingActionButton floatBtn;

    TextView textViewBat;

    RecyclerView recyclerView;
    RecyclerAdapter recyclerAdapter;

    RecyclerView recyclerViewFinished;
    RecyclerFinishedAdapter recyclerFinihsedAdapter;

    SwipeRefreshLayout swipeRefreshLayout, swipeRefreshLayoutFinished;

    ImageView imageViewBat;

    TextView textViewTitle;

    List<String> tasksList;
    List<String> finishedTasksList;


    String address = null;
    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    int finishedTasks = 0;


    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    int counter;
    volatile boolean stopWorker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

//        requestWindowFeature(getWindow().FEATURE_NO_TITLE);
//
//        getSupportActionBar().hide();


        setContentView(R.layout.activity_led_control);

        //tasksList = new ArrayList<>();
        //finishedTasksList = new ArrayList<>();

        //Intent newint = getIntent();
        //address = newint.getStringExtra(MainActivity.EXTRA_ADDRESS); //receive the address of the bluetooth device

        //view of the ledControl
        setContentView(R.layout.activity_led_control);

        //call the widgtes


        imageViewBat = (ImageView) findViewById(R.id.imageViewBat);
        textViewTitle = (TextView) findViewById(R.id.textViewTitle);

        btnBat = (Button) findViewById(R.id.btnBat);
        btnReset = (Button) findViewById(R.id.btnReset);
        floatBtn = (FloatingActionButton) findViewById(R.id.floatBtn);

        floatBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btSocket != null) {

                    if (btSocket.isConnected() && (myBluetooth.isEnabled())) {


                        try {

                            btSocket.getOutputStream().write(String.valueOf("").getBytes());

                            DialogTaskClass dialogTaskClass = new DialogTaskClass();
                            Bundle bundle = new Bundle();

                            //bundle.putString("taskName", textViewTask.getText().toString());
                            bundle.putBoolean("editMode", false);
                            //bundle.putInt("taskId",position);

                            dialogTaskClass.setArguments(bundle);


                            dialogTaskClass.show(getSupportFragmentManager(), "example dialog");


                        } catch (IOException e) {
                            msg("Not connected. Please press Reconnect and try again");
                        }


                        //recyclerAdapter.notifyItemInserted(tasksList.size()-1);


                    } else {

                        msg("Not connected. Please press Reconnect and try again");
                    }
                } else {
                    msg("Not connected. Please press Reconnect and try again");
                }


            }
        });

        btnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (btSocket!=null) {

                    if (btSocket.isConnected() && myBluetooth.isEnabled()) {

                        try {

                            btSocket.getOutputStream().write(String.valueOf("R").getBytes());


                            tasksList.clear();
                            finishedTasksList.clear();

                            recyclerAdapter.notifyDataSetChanged();
                            recyclerFinihsedAdapter.notifyDataSetChanged();

                            saveData();
                        } catch (IOException e) {
                            msg("Not connected. Please press Reconnect and try again");
                        }


                    } else {

                        msg("Not connected. Please press Reconnect and try again");
                    }
                }
                else{

                    msg("Not connected. Please press Reconnect and try again");
                }
            }


        });

        textViewBat = (TextView) findViewById(R.id.textViewBat);

        btnBat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopWorker = true;

                new ConnectBT().execute();

            }
        });

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeTasks);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                stopWorker = true;
                Refresh(true);
                stopWorker = true;
                Refresh(false);

                //msg(String.valueOf(finishedTasksList.size()));

                swipeRefreshLayout.setRefreshing(false);

            }
        });


        getData();
        recyclerView = (RecyclerView) findViewById(R.id.recycler);
        recyclerAdapter = new RecyclerAdapter(tasksList);


        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(recyclerAdapter);

        recyclerAdapter.notifyDataSetChanged();

        //recyclerView.setNestedScrollingEnabled(false);


        recyclerViewFinished = (RecyclerView) findViewById(R.id.recyclerFinished);
        recyclerFinihsedAdapter = new RecyclerFinishedAdapter(finishedTasksList);


        recyclerViewFinished.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewFinished.setAdapter(recyclerFinihsedAdapter);


        recyclerFinihsedAdapter.notifyDataSetChanged();

        //recyclerViewFinished.setNestedScrollingEnabled(false);


        new ConnectBT().execute(); //Call the class to connect


        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);


    }

    String deletedTask = null;

    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP | ItemTouchHelper.DOWN | ItemTouchHelper.START | ItemTouchHelper.END, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
        TextView textViewTask;
        EditText editTextTask;
        FrameLayout frameTask;
        Button buttonTask;
        ImageView imageViewTask;

        private final ColorDrawable background = new ColorDrawable(Color.parseColor("#00ff00"));


        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            int fromPosition = viewHolder.getAdapterPosition();
            int toPosition = target.getAdapterPosition();

            Collections.swap(tasksList, fromPosition, toPosition);
            saveData();

            recyclerView.getAdapter().notifyItemMoved(fromPosition, toPosition);

            recyclerAdapter.notifyDataSetChanged();


            return false;
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    .addSwipeLeftBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.delete, null))
                    .addSwipeLeftActionIcon(R.drawable.ic_baseline_delete_24)
                    .addSwipeRightBackgroundColor(ResourcesCompat.getColor(getResources(), R.color.edit, null))
                    .addSwipeRightActionIcon(R.drawable.ic_baseline_edit_24)
                    .create()
                    .decorate();

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);

            //swipeRefreshLayout.setEnabled(false);


        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            final int position = viewHolder.getAdapterPosition();


            //swipeRefreshLayout.setEnabled(true);


            textViewTask = viewHolder.itemView.findViewById(R.id.textViewTask);
            frameTask = viewHolder.itemView.findViewById(R.id.frameTask);


            switch (direction) {

                case ItemTouchHelper.LEFT:

                    if (btSocket != null) {
                        if ((btSocket.isConnected()) && (myBluetooth.isEnabled())) {

                            try {
                                btSocket.getOutputStream().write(String.valueOf("").getBytes());

                                deletedTask = tasksList.get(position);
                                tasksList.remove(position);
                                saveData();
                                recyclerAdapter.notifyItemRemoved(position);

                                Snackbar.make(recyclerView, deletedTask + " was removed! ", Snackbar.LENGTH_LONG).setAction("Undo", new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        tasksList.add(position, deletedTask);
                                        recyclerAdapter.notifyItemInserted(position);

                                    }
                                }).show();


                                //btSocket.getOutputStream().write("D".toString().getBytes());
                                btSocket.getOutputStream().write(String.valueOf("D" + position + finishedTasksList.size()).getBytes());
                                msg(String.valueOf(tasksList.size()));


                            } catch (IOException e) {


                                msg("Not connected. Please press Reconnect and try again");
                                recyclerAdapter.notifyDataSetChanged();
                            }

                    } else {
                        msg("Not connected. Please press Reconnect and try again");
                        recyclerAdapter.notifyDataSetChanged();
                    }
                    }
                    else{

                        msg("Not connected. Please press Reconnect and try again");
                        recyclerAdapter.notifyDataSetChanged();
                    }



                    break;


                case ItemTouchHelper.RIGHT:

                    DialogTaskClass dialogTaskClass = new DialogTaskClass();
                    Bundle bundle = new Bundle();
                    bundle.putString("taskName", textViewTask.getText().toString());
                    bundle.putBoolean("editMode", true);
                    bundle.putInt("taskId", position);

                    dialogTaskClass.setArguments(bundle);
                    dialogTaskClass.show(getSupportFragmentManager(), "example dialog");


                    break;

            }

        }
    };

    private void msg(String s) {
        Toast.makeText(getApplicationContext(), s, Toast.LENGTH_SHORT).show();
    }

    private void Disconnect() {
        if (btSocket != null) //If the btSocket is busy
        {
            try {
                btSocket.close(); //close connection
            } catch (IOException e) {
                msg("Error");
            }
        }
        finish(); //return to the first layout
    }

    private void Refresh(final boolean once) {
        if (btSocket!=null) {

            if (btSocket.isConnected() && (myBluetooth.isEnabled())) {
                try {
                    if (once) {

                        btSocket.getOutputStream().write("N".toString().getBytes());
                    }


                    final Handler handler = new Handler();
                    stopWorker = false;
                    readBufferPosition = 0;
                    readBuffer = new byte[1024];

                    workerThread = new Thread(new Runnable() {
                        public void run() {
                            while (!Thread.currentThread().isInterrupted() && !stopWorker) {
                                if (!isBtConnected) {

                                    textViewTitle.setText("Not connected!");
                                }
                                try {
                                    int bytesAvailable = btSocket.getInputStream().available();
                                    if (bytesAvailable > 0) {
                                        byte[] packetBytes = new byte[bytesAvailable];
                                        btSocket.getInputStream().read(packetBytes);
                                        for (int i = 0; i < bytesAvailable; i++) {
                                            byte b = packetBytes[i];
                                            //msg(String.valueOf(b));


                                            if (b == '\n') {
                                                final byte[] encodedBytes = new byte[readBufferPosition];
                                                System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                                //final String data = new String(encodedBytes, "US-ASCII");
                                                readBufferPosition = 0;
                                                handler.post(new Runnable() {
                                                    public void run() {

                                                        if (encodedBytes.length != 1) {
                                                            //msg(data);

                                                            //int splitter= data.indexOf('T');
                                                            //String bat_lvl=data.substring(0,splitter);
                                                            //String finished_tasks= data.substring(splitter+1);
                                                            int batLvl = encodedBytes[0];

                                                            if (batLvl > 100) {

                                                                batLvl = 100;
                                                            }

                                                            if (encodedBytes[0] < 0) {

                                                                batLvl = 0;
                                                            }

                                                            if (batLvl < 100 && batLvl > 70) {

                                                                imageViewBat.setImageResource(R.mipmap.bat_full);

                                                                imageViewBat.setTag(R.mipmap.bat_full);
                                                            }

                                                            if (batLvl < 70 && batLvl > 50) {

                                                                imageViewBat.setImageResource(R.mipmap.bat_mid2);
                                                                imageViewBat.setTag(R.mipmap.bat_mid2);
                                                            }

                                                            if (batLvl < 50 && batLvl > 30) {

                                                                imageViewBat.setImageResource(R.mipmap.bat_mid1);
                                                                imageViewBat.setTag(R.mipmap.bat_mid1);
                                                            }

                                                            if (batLvl < 30 && batLvl > 0) {

                                                                imageViewBat.setImageResource(R.mipmap.bat_low);
                                                                imageViewBat.setTag(R.mipmap.bat_low);
                                                            }


                                                            textViewBat.setText(batLvl + "%");

                                                            int finihsed_n = encodedBytes[1];
                                                            //msg(String.valueOf(finihsed_n));
                                                            int finished_tasks = finihsed_n - finishedTasksList.size();


                                                            if (finihsed_n > finishedTasksList.size()) {
                                                                for (int f = 0; f < finished_tasks; f++) {

                                                                    finishedTasksList.add(tasksList.get(f));

                                                                }

                                                                ArrayList<String>
                                                                        arrlist2 = new ArrayList<String>();

                                                                for (int t = 0; t < finished_tasks; t++) {
                                                                    arrlist2.add(tasksList.get(t));

                                                                }

                                                                tasksList.removeAll(arrlist2);

                                                                recyclerFinihsedAdapter.notifyDataSetChanged();
                                                                recyclerAdapter.notifyDataSetChanged();

                                                                saveData();

                                                            }

                                                        }

                                                        stopWorker = once;


                                                    }
                                                });

                                            } else {

                                                readBuffer[readBufferPosition++] = b;
                                            }

                                        }
                                    }
                                } catch (IOException ex) {
                                    stopWorker = true;
                                }
                            }
                        }
                    });

                    workerThread.start();

                } catch (Exception e) {
                    // ADD THIS TO SEE ANY ERROR
                    msg("Not connected. Please press Reconnect and try again");
                }


                //recyclerAdapter.notifyDataSetChanged();
            } else {

                msg("Not connected. Please press Reconnect and try again");


            }
        }
        else{
            msg("Not connected. Please press Reconnect and try again");

        }


    }


    @Override
    public void applyText(String taskName, boolean editMode, int taskId) {


        if (editMode) {

            tasksList.set(taskId, taskName);
            saveData();
            recyclerAdapter.notifyItemChanged(taskId);
        } else {
            if (btSocket!=null) {

                if (btSocket.isConnected()) {
                    try {

                        btSocket.getOutputStream().write(String.valueOf("").getBytes());
                        tasksList.add(taskName);

                        //recyclerAdapter.notifyItemInserted(tasksList.size()-1);
                        recyclerAdapter.notifyDataSetChanged();

                        saveData();

                        btSocket.getOutputStream().write(String.valueOf(tasksList.size() + finishedTasksList.size()).getBytes());

                    } catch (IOException e) {
                        msg("Not connected. Please press Reconnect and try again");
                    }
                }
            }
            else{
                msg("Not connected. Please press Reconnect and try again");

            }


        }

    }


    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute() {
            progress = ProgressDialog.show(ledControl.this, "Connecting...", "Please wait!!!");  //show a progress dialog
        }

        @Override
        protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
        {
            try {
                //if (btSocket == null) {
                myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                BluetoothDevice dispositivo = myBluetooth.getRemoteDevice("24:6F:28:80:E4:82");//connects to the device's address and checks if it's available
                btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                btSocket.connect();//start connection


                //}
            } catch (IOException e) {
                ConnectSuccess = false;
                progress.dismiss();//if the try failed, you can check the exception here
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            if (!ConnectSuccess) {
                textViewTitle.setText("Not connected!");

                if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                    msg("Device not supported!");
                    //finish();
                }

                if (myBluetooth == null || !myBluetooth.isEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, 1);

                } else {
                    //Toast.makeText(getApplicationContext(), "Connection Failed. Please try again ", Toast.LENGTH_LONG).show();
                    //finish();


                    new ConnectBT().execute();
                    isBtConnected = false;
                    try {
                        btSocket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }


                }


            } else {
                textViewTitle.setText("The Rainmaker");

                msg("Connected.");
                isBtConnected = true;

                Refresh(true);
                stopWorker = true;
                Refresh(false);

            }
            progress.dismiss();


        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            new ConnectBT().execute();
        }
    }


    private void saveData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);


        Gson gson = new Gson();
        String json = gson.toJson(tasksList);
        String jsonFinished = gson.toJson(finishedTasksList);
        String bat = textViewBat.getText().toString();


        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString("Set1", json);
        editor.putString("Set2", jsonFinished);
        editor.putString("bat", bat);


        editor.commit();

    }

    private void getData() {
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("Set1", null);
        String jsonFinished = sharedPreferences.getString("Set2", null);
        String bat = sharedPreferences.getString("bat", "--");


        Type type = new TypeToken<ArrayList<String>>() {
        }.getType();
        tasksList = gson.fromJson(json, type);
        finishedTasksList = gson.fromJson(jsonFinished, type);

        textViewBat.setText(bat + '%');


        if (tasksList == null) {
            //msg("nothing :(");
            tasksList = new ArrayList<>();

        }
        if (finishedTasksList == null) {
            //msg("nothing :(");
            finishedTasksList = new ArrayList<>();

        }


        //recyclerAdapter.notifyDataSetChanged();}


    }


}