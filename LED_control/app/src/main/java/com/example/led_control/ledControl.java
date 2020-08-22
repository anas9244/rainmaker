package com.example.led_control;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;

import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
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

import com.google.android.material.snackbar.Snackbar;

import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import it.xabaras.android.recyclerview.swipedecorator.RecyclerViewSwipeDecorator;


public class ledControl extends AppCompatActivity  implements DialogTaskClass.DialogListener {

    public int task_n = 0;

    Button btnOn, btnOff, btnDis,btnAdd,btnApply, btnRefresh;

    RecyclerView recyclerView;
    RecyclerAdapter recyclerAdapter;

    SwipeRefreshLayout swipeRefreshLayout;

    List<String> tasksList;






    String address = null;
    private ProgressDialog progress;
    BluetoothAdapter myBluetooth = null;
    BluetoothSocket btSocket = null;
    private boolean isBtConnected = false;
    static final UUID myUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    int finishedTasks=0;


    Thread workerThread;
    byte[] readBuffer;
    int readBufferPosition;
    int counter;
    volatile boolean stopWorker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_led_control);

        tasksList= new ArrayList<>();

        Intent newint = getIntent();
        //address = newint.getStringExtra(MainActivity.EXTRA_ADDRESS); //receive the address of the bluetooth device

        //view of the ledControl
        setContentView(R.layout.activity_led_control);

        //call the widgtes
        btnOn = (Button)findViewById(R.id.button2);
        btnOff = (Button)findViewById(R.id.button3);
        btnDis = (Button)findViewById(R.id.button4);
        btnAdd = (Button)findViewById(R.id.btnAdd);
        btnApply= (Button)findViewById(R.id.btnApply);
        btnRefresh= (Button)findViewById(R.id.btnRefresh);

        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                try {

                    btSocket.getOutputStream().write("TN".toString().getBytes());


                } catch (Exception e) {
                    // ADD THIS TO SEE ANY ERROR
                    e.printStackTrace();
                }



                final Handler handler = new Handler();
                stopWorker = false;
                readBufferPosition = 0;
                readBuffer = new byte[1024];

                workerThread = new Thread(new Runnable()
                {
                    public void run()
                    {
                        while(!Thread.currentThread().isInterrupted() && !stopWorker)
                        {
                            try
                            {
                                int bytesAvailable = btSocket.getInputStream().available();
                                if(bytesAvailable > 0)
                                {
                                    byte[] packetBytes = new byte[bytesAvailable];
                                    btSocket.getInputStream().read(packetBytes);
                                    for(int i=0;i<bytesAvailable;i++)
                                    {
                                        final byte b = packetBytes[i];


                                            handler.post(new Runnable()
                                            {
                                                public void run()
                                                {
                                                    for (int t=0; t< b ;t++){

                                                        tasksList.set(t,"done");
                                                        recyclerAdapter.notifyItemChanged(t);

                                                    }

                                                    //recyclerAdapter.notifyDataSetChanged();


                                                }
                                            });

                                    }
                                }
                            }
                            catch (IOException ex)
                            {
                                stopWorker = true;
                            }
                        }
                    }
                });

                workerThread.start();








                //recyclerAdapter.notifyDataSetChanged();
                //swipeRefreshLayout.setRefreshing(false);

            }
        });

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeTasks);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                try {
                    finishedTasks= btSocket.getInputStream().read();

                    msg(String.valueOf(finishedTasks));
                } catch (IOException e) {
                    e.printStackTrace();
                }


                //recyclerAdapter.notifyDataSetChanged();
                swipeRefreshLayout.setRefreshing(false);

            }
        });
        btnApply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


                if (btSocket!=null)
                {
                    try
                    {
                        btSocket.getOutputStream().write(String.valueOf(tasksList.size()).getBytes());

                    }
                    catch (IOException e)
                    {
                        msg("Error"+ e);
                    }
                }



            }
        });

        recyclerView = (RecyclerView)findViewById(R.id.recycler);
        recyclerAdapter= new RecyclerAdapter(tasksList);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(recyclerAdapter);

        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(this,DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);




        new ConnectBT().execute(); //Call the class to connect

        btnOn.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                turnOnLed();      //method to turn on
            }
        });

        btnOff.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {
                turnOffLed();   //method to turn off
            }
        });

        btnDis.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Disconnect(); //close connection
            }
        });

        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                DialogTaskClass dialogTaskClass = new DialogTaskClass();
                Bundle bundle = new Bundle();

                //bundle.putString("taskName", textViewTask.getText().toString());
                bundle.putBoolean("editMode",false);
                //bundle.putInt("taskId",position);

                dialogTaskClass.setArguments(bundle);


                dialogTaskClass.show(getSupportFragmentManager(),"example dialog");





                //recyclerAdapter.notifyItemInserted(tasksList.size()-1);


            }
        });
        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(simpleCallback);
        itemTouchHelper.attachToRecyclerView(recyclerView);



    }

    String deletedTask = null;

    ItemTouchHelper.SimpleCallback simpleCallback = new ItemTouchHelper.SimpleCallback(ItemTouchHelper.UP |ItemTouchHelper.DOWN | ItemTouchHelper.START | ItemTouchHelper.END,ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
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

            Collections.swap(tasksList,fromPosition,toPosition);

            recyclerView.getAdapter().notifyItemMoved(fromPosition,toPosition);


            return false;
        }

        @Override
        public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
            new RecyclerViewSwipeDecorator.Builder(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
                    .addSwipeLeftBackgroundColor(Color.parseColor("#CC0000"))
                    .addSwipeLeftActionIcon(R.drawable.ic_baseline_delete_24)
                    .addSwipeRightBackgroundColor(Color.parseColor("#0000CC"))
                    .addSwipeRightActionIcon(R.drawable.ic_baseline_edit_24)
                    .create()
                    .decorate();

            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);



        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            final int position = viewHolder.getAdapterPosition();

            textViewTask = viewHolder.itemView.findViewById(R.id.textViewTask);
            editTextTask = viewHolder.itemView.findViewById(R.id.edittextTask);
            frameTask = viewHolder.itemView.findViewById(R.id.frameTask);
            buttonTask = viewHolder.itemView.findViewById(R.id.buttonTask);
            imageViewTask = viewHolder.itemView.findViewById(R.id.imageViewTask);

            switch (direction) {

                case ItemTouchHelper.LEFT:

                    deletedTask= tasksList.get(position);
                    tasksList.remove(position);
                    recyclerAdapter.notifyItemRemoved(position);

                    Snackbar.make(recyclerView,deletedTask+" was removed! ",Snackbar.LENGTH_LONG).setAction("Undo", new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            tasksList.add(position,deletedTask);
                            recyclerAdapter.notifyItemInserted(position);

                        }
                    }).show();

                    break;
                case ItemTouchHelper.RIGHT:

                    DialogTaskClass dialogTaskClass = new DialogTaskClass();
                    Bundle bundle = new Bundle();
                    bundle.putString("taskName", textViewTask.getText().toString());
                    bundle.putBoolean("editMode",true);
                    bundle.putInt("taskId",position);

                    dialogTaskClass.setArguments(bundle);
                    dialogTaskClass.show(getSupportFragmentManager(),"example dialog");






                    break;

            }

        }
    };

    private void msg(String s)
    {
        Toast.makeText(getApplicationContext(),s,Toast.LENGTH_SHORT).show();
    }

    private void Disconnect()
    {
        if (btSocket!=null) //If the btSocket is busy
        {
            try
            {
                btSocket.close(); //close connection
            }
            catch (IOException e)
            { msg("Error");}
        }
        finish(); //return to the first layout
    }

    private void turnOffLed()
    {
        if (btSocket!=null)
        {
            try
            {
                btSocket.getOutputStream().write("TF".toString().getBytes());

            }
            catch (IOException e)
            {
                msg("Error"+ e);
            }
        }
    }


    private void turnOnLed()
    {
        if (btSocket!=null)
        {
            try
            {
                btSocket.getOutputStream().write("TO".toString().getBytes());
            }
            catch (IOException e)
            {
                msg("Error");
            }
        }
    }

    @Override
    public void applyText(String taskName, boolean editMode, int taskId) {

        if (editMode){

            tasksList.set(taskId,taskName);
            recyclerAdapter.notifyItemChanged(taskId);
        }
        else{
        tasksList.add(taskName);

        //recyclerAdapter.notifyItemInserted(tasksList.size()-1);
        recyclerAdapter.notifyDataSetChanged();

        }

    }




    private class ConnectBT extends AsyncTask<Void, Void, Void>  // UI thread
    {
        private boolean ConnectSuccess = true; //if it's here, it's almost connected

        @Override
        protected void onPreExecute()
        {
            progress = ProgressDialog.show(ledControl.this, "Connecting...", "Please wait!!!");  //show a progress dialog
        }

        @Override
        protected Void doInBackground(Void... devices) //while the progress dialog is shown, the connection is done in background
        {
            try
            {
                if (btSocket == null || !isBtConnected)
                {
                    myBluetooth = BluetoothAdapter.getDefaultAdapter();//get the mobile bluetooth device
                    BluetoothDevice dispositivo = myBluetooth.getRemoteDevice("24:6F:28:80:E4:82");//connects to the device's address and checks if it's available
                    btSocket = dispositivo.createInsecureRfcommSocketToServiceRecord(myUUID);//create a RFCOMM (SPP) connection
                    BluetoothAdapter.getDefaultAdapter().cancelDiscovery();
                    btSocket.connect();//start connection
                }
            }
            catch (IOException e)
            {
                ConnectSuccess = false;//if the try failed, you can check the exception here
            }
            return null;
        }
        @Override
        protected void onPostExecute(Void result) //after the doInBackground, it checks if everything went fine
        {
            super.onPostExecute(result);

            if (!ConnectSuccess)
            {



                if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                    msg("Device not supported!");
                    finish();
                }

                if (myBluetooth == null || !myBluetooth.isEnabled()) {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, 1);

                }

                else {
                    Toast.makeText(getApplicationContext(),"Connection Failed. Make sure the rainmaker is on. ",Toast.LENGTH_LONG).show();
                    finish();
                }


            }
            else
            {
                msg("Connected.");
                isBtConnected = true;
            }
            progress.dismiss();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode==RESULT_OK){
            recreate();
        }
    }
}