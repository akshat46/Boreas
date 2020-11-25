package com.sjsu.boreas.ChatView;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

//import com.sjsu.boreas.ChatViewRelatedStuff.MessageListViewStuff.OneOnOneMessageAdapter;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageMetadata;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.sjsu.boreas.ChatView.MediaFilesRecyclerItems.FileItem;
import com.sjsu.boreas.ChatView.MediaFilesRecyclerItems.FileItemClickedAction;
import com.sjsu.boreas.ChatView.MediaFilesRecyclerItems.MediaFileListAdapter;
import com.sjsu.boreas.Database.Contacts.User;
import com.sjsu.boreas.Database.LocalDatabaseReference;
import com.sjsu.boreas.Database.Messages.ChatMessage;
import com.sjsu.boreas.ChatView.MessageRecyclerItems.MessageAdapter;
import com.sjsu.boreas.Database.Messages.MessageUtility;
import com.sjsu.boreas.Database.PotentialContacts.PotentialContacts;
import com.sjsu.boreas.Events.Event;
import com.sjsu.boreas.Events.EventListener;
//import com.sjsu.boreas.HelperStuff.ContextHelper;
import com.sjsu.boreas.Misc.ContextHelper;
import com.sjsu.boreas.OnlineConnectionHandlers.FirebaseController;
import com.sjsu.boreas.MainActivity;
import com.sjsu.boreas.PhoneBluetoothRadio.BlueTerm;
import com.sjsu.boreas.R;
import com.sjsu.boreas.SettingsActivity;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.security.auth.Subject;

public class ChatActivity2 extends AppCompatActivity implements EventListener, FileItemClickedAction {

    private RecyclerView recyclerView;
    private RecyclerView fileSelectedView;
    private LinearLayout getFileSelectedParentLayout;
    private ImageButton btnSend;
    private EditText mssgText;
    private TextView userName;
    private ArrayList<ChatMessage> chatMessages;
    private ArrayList<FileItem> fileSelectedList;
    private MessageAdapter adapter;
    private MediaFileListAdapter mediaFileListAdapter;
    private RecyclerView.LayoutManager layoutManager;
    private RecyclerView.LayoutManager mediaListLayoutManager;
    private User myChatPartner;
    private Context mContext;
    private ChatActivity2 mActivity;
    private Toolbar toolbar;
    private ImageButton dynamicButton;
    private ImageButton dynamicButton2;
    private ImageButton btnChatMedia;
    private ImageButton btnBack;
    private PopupMenu popup;
    private String[] FILE;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private enum SendMode{
        ONLINE("ONLINE"),
        OFFLINE_CONNECT_API("OFFLINE"),
        OFFLINE_RADIO("RADIO");

        public final String label;

        private SendMode(String label){
            this.label = label;
        }

        public String getValue(){
            return label;
        }
    };
    // TODO: save it to, and set it from loggedin user database to have better ux
    private SendMode mode = SendMode.ONLINE;
    public LocalDatabaseReference localDatabaseReference = LocalDatabaseReference.get();

    private static String TAG = "BOREAS";
    private static String SUB_TAG = "----------------ChatActivity2 ";

    private static int MEDIA_RESULT = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat2);

        Event event = Event.get(Event.chatMssgEventID);
        Event event_radio_connected = Event.get(Event.radioConnected);
        Event event_radio_disconnected = Event.get(Event.radioDisconnected);
        event.addListener(this);
//        event_radio_connected.addListener(this);
//        event_radio_disconnected.addListener(this);

        mContext = this;
        mActivity = this;

        Log.e(TAG, SUB_TAG+"We in hea on create");
        //Getting the user object from intent
        Intent intent = getIntent();
        myChatPartner = (User) intent.getSerializableExtra("ReceiverObj");
        userName = findViewById(R.id.userName);
        userName.setText(myChatPartner.name);


        Log.e(TAG, SUB_TAG+"-=-=-=-=-==-///////// "+myChatPartner+", and me: "+MainActivity.currentUser.getName());

        initUI();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
            Window window = getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);
            window.setStatusBarColor(ContextCompat.getColor(ContextHelper.get().getApplicationContext(),R.color.backgroundAlt));
        }

        mssgText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_GO || actionId == EditorInfo.IME_ACTION_DONE || actionId == EditorInfo.IME_ACTION_SEND) {
                    //Perform your Actions here.
                    Log.e(TAG, SUB_TAG+"We our here tryna detect enter key.");
                    if (mssgText.getText().toString().trim().equals("")) {
                        // TODO: disable button
                        Toast.makeText(ChatActivity2.this, "Please input some text...", Toast.LENGTH_SHORT).show();
                    } else {
                        //add message to list
                        sendMessage(mssgText.getText().toString());
                    }
                    handled = true;
                }
                return handled;
            }
        });
    }

    private void initUI(){
        Log.e(TAG, SUB_TAG+"Initializig the ui");
        recyclerView = findViewById(R.id.list_msg);
        fileSelectedView = findViewById(R.id.files_selected);
        getFileSelectedParentLayout = findViewById(R.id.files_selected_parent_layout);
        btnSend = findViewById(R.id.btn_chat_send);
        mssgText = findViewById(R.id.msg_type);
        btnChatMedia = findViewById(R.id.btn_chat_media);
        btnBack = findViewById(R.id.btn_back);

        initNewUI();

        initSendButton();
        initChatMediaButton();
        initAdapter();
        initFileListAdapter();
        initBackButton();
    }

    private void initSendButton() {

        //event for button SEND
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, "Hea too");
                if (mssgText.getText().toString().trim().equals("")) {
                    // TODO: disable button
                    Toast.makeText(ChatActivity2.this, "Please input some text...", Toast.LENGTH_SHORT).show();
                } else {
                    //add message to list
                    sendMessage(mssgText.getText().toString());
                }
            }
        });
    }

    private void initBackButton(){
        Log.e(TAG, SUB_TAG+"Initializing the back button");

        btnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActivity.finish();
            }
        });
    }

    private void initChatMediaButton(){
        Log.e(TAG, SUB_TAG+"Init chat media button");
        //Hide the linear layout view by default
        getFileSelectedParentLayout.setVisibility(View.GONE);

        btnChatMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                verifyStoragePermissions(mActivity);
                Log.e(TAG, SUB_TAG+"Onclick chat media");
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("image/*");
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), MEDIA_RESULT);
//                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION|Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//                if (Build.VERSION.SDK_INT < 19) {
//                    intent = new Intent();
//                    intent.setAction(Intent.ACTION_GET_CONTENT);
//                    intent.setType("*/*");
//                    startActivityForResult(Intent.createChooser(intent, "Select file to upload "),MEDIA_RESULT);
//                } else {
//                    intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
//                    intent.addCategory(Intent.CATEGORY_OPENABLE);
//                    intent.setType("*/*");
//                    startActivityForResult(Intent.createChooser(intent, "Select file to upload "),MEDIA_RESULT);
//                }
//                startActivityForResult(intent, MEDIA_RESULT);
//                loadImages();
            }
        });
    }

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    //This is the returned response from the intent that was started in the
    //  initChatMediaButton function
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e(TAG, SUB_TAG+"Result received");
        try {
            if (requestCode == MEDIA_RESULT && resultCode == RESULT_OK
                    && null != data) {
                Log.e(TAG, SUB_TAG+data.getDataString());
                Uri URI = data.getData();

//                List<ResolveInfo> resInfoList = this.getPackageManager().queryIntentActivities(data, PackageManager.MATCH_DEFAULT_ONLY);
//                for (ResolveInfo resolveInfo : resInfoList) {
//                    String packageName = resolveInfo.activityInfo.packageName;
//                    this.grantUriPermission(packageName, URI, Intent.FLAG_GRANT_WRITE_URI_PERMISSION | Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                }

                String[] FILE = {MediaStore.Images.Media.DATA};

                Cursor cursor = getApplicationContext().getContentResolver().query(URI,
                        FILE, null, null, null);

                if(cursor == null) {
                    Log.e(TAG, SUB_TAG+"Cursor is null");
                    return;
                }

                while (cursor.moveToNext()) {
                    int columnIndex = cursor.getColumnIndex(FILE[0]);
                    String imageDecode = cursor.getString(columnIndex);
                    FileItem fileItem = new FileItem(imageDecode);
                    fileSelectedList.add(fileItem);
                    mediaFileListAdapter.notifyDataSetChanged();
                }
                cursor.close();

                if(fileSelectedList.size() > 0){
                    Log.e(TAG, SUB_TAG+"Some stuff is selected");
                    getFileSelectedParentLayout.setVisibility(View.VISIBLE);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, SUB_TAG+"Exception: " + e.toString());
            Toast.makeText(mActivity, "Please try again", Toast.LENGTH_LONG)
                    .show();
        }
    }

    private void initNewUI(){
        Log.e(TAG, SUB_TAG+"Initializig the ui");
        recyclerView = findViewById(R.id.list_msg);
        btnSend = findViewById(R.id.btn_chat_send);
        mssgText = findViewById(R.id.msg_type);
        toolbar = findViewById(R.id.home_toolbar);
        dynamicButton = findViewById(R.id.dynamic_button);
        dynamicButton2 = findViewById(R.id.dynamic_button2);

        //By default the dynamic buttons is disabled
        dynamicButton.setEnabled(false);
        dynamicButton.setVisibility(View.INVISIBLE);
        dynamicButton2.setEnabled(false);
        dynamicButton2.setVisibility(View.INVISIBLE);

        if(myChatPartner instanceof PotentialContacts){
            Log.e(TAG, SUB_TAG+"my chatpartner is not my contact, adding the option to ignore or add contact");
            setUpTheIgnoreButton();
            setUpTheAddToContactsButton();
        }
        btnSend.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                popup = new PopupMenu(ChatActivity2.this, btnSend);
                popup.inflate(R.menu.menu_send);

                if(!(SettingsActivity.radio_is_connected)){
                    Log.e(TAG, SUB_TAG+"The radio isn't connected yet");
                    popup.getMenu().findItem(R.id.action_send_radio).setVisible(false);
                }

                try {
                    Field[] fields = popup.getClass().getDeclaredFields();
                    for (Field field : fields) {
                        if ("mPopup".equals(field.getName())) {
                            field.setAccessible(true);
                            Object menuPopupHelper = field.get(popup);
                            Class<?> classPopupHelper = Class.forName(menuPopupHelper
                                    .getClass().getName());
                            Method setForceIcons = classPopupHelper.getMethod(
                                    "setForceShowIcon", boolean.class);
                            ((Method) setForceIcons).invoke(menuPopupHelper, true);
                            break;
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem item) {
                        Log.e(TAG, SUB_TAG+"Inside popup");
                        // cant use switch. doesn't work with enum.values
                        if(item.getTitle().toString().toUpperCase().equals(SendMode.OFFLINE_CONNECT_API.getValue())) {
                            Log.e(TAG, SUB_TAG+"changing the mode to offline");
                            btnSend.setImageResource(R.drawable.ic_offline_send_f);
                            btnSend.setBackgroundResource(R.drawable.bg_button_send_offline);
                            mode =SendMode.OFFLINE_CONNECT_API;
                        }
                        else if(item.getTitle().toString().toUpperCase().equals(SendMode.ONLINE.getValue())){
                            Log.e(TAG, SUB_TAG+"Changing the mode to online");
                            btnSend.setImageResource(R.drawable.ic_online_send_f);
                            btnSend.setBackgroundResource(R.drawable.bg_button);
                            mode =SendMode.ONLINE;
                        }
                        else if(item.getTitle().toString().toUpperCase().equals(SendMode.OFFLINE_RADIO.getValue())){
                            Log.e(TAG, SUB_TAG+"Changing the mode to radio");
                            btnSend.setImageResource(R.drawable.ic_online_send_f);
                            btnSend.setBackgroundResource(R.drawable.bg_button);
                            mode =SendMode.OFFLINE_RADIO;
                        }
                        return true;
                    }
                });
                popup.show(); //showing popup menu
                return false;
            }
        });
    }

    private void setUpTheIgnoreButton(){
        Log.e(TAG, SUB_TAG+"Setting up the ignore button");
        dynamicButton.setImageResource(R.drawable.ic_block_contact);
        dynamicButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, SUB_TAG+"Ignoring this user burr");
                localDatabaseReference.removePotentialContact((PotentialContacts) myChatPartner);
                ChatActivity2.this.finish();
                //TODO: gotta remove this user and then go to the previous screen
                // and also update the oneOnOne Fragment
                //localDatabaseReference.removePotentialUser(myChatPartner);
            }
        });
        dynamicButton.setEnabled(true);
        dynamicButton.setVisibility(View.VISIBLE);
    }

    private void setUpTheAddToContactsButton(){
        Log.e(TAG, SUB_TAG+"Setting up the adding to contact button");
        dynamicButton2.setImageResource(R.drawable.ic_add_contact);
        dynamicButton2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.e(TAG, SUB_TAG+"Add to contact burr");
                localDatabaseReference.convertPotentialContactToContact((PotentialContacts) myChatPartner);
                FirebaseController.addContact((User) myChatPartner);
                //TODO: gotta remove this user and then go to the previous screen
                // and also update the oneOnOne Fragment
                //localDatabaseReference.addContact(myChatPartner);
            }
        });
        dynamicButton2.setEnabled(true);
        dynamicButton2.setVisibility(View.VISIBLE);
    }

//    private void initializeAdapter(){

    private void initAdapter(){
        Log.e(TAG, SUB_TAG+"initializing adapter");
        chatMessages = new ArrayList<ChatMessage>();
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                chatMessages = new ArrayList<ChatMessage>(localDatabaseReference.getLastTwentyMessagesForSpecificUser(myChatPartner));
                //set ListView adapter first
                adapter = new MessageAdapter(chatMessages);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        recyclerView.setAdapter(adapter);
                        layoutManager = new LinearLayoutManager(mActivity);
                        recyclerView.setLayoutManager(layoutManager);
                    }
                });
                recyclerView.scrollToPosition(adapter.getItemCount() - 1);
            }
        });
    }

    private void initFileListAdapter(){
        //Setting up the recycler view for the media files selected
        Log.e(TAG, SUB_TAG+"setting up the file list adapter");
        fileSelectedList = new ArrayList<FileItem>();
        mediaFileListAdapter = new MediaFileListAdapter(fileSelectedList, mActivity);
        fileSelectedView.setAdapter(mediaFileListAdapter);
        mediaListLayoutManager = new LinearLayoutManager(mActivity, LinearLayoutManager.HORIZONTAL, false);
        fileSelectedView.setLayoutManager(mediaListLayoutManager);
    }

    private void sendMessage(String mssg){
        Log.e(TAG, SUB_TAG+"Getting ready to send message");

        //Check if any files are attached/selected
        if(fileSelectedList.size() > 0){
            setUpMediaChatMessages(mssg);
        }else {
            //Clear the text box on the screen
            mssgText.setText("");

            long time  = Calendar.getInstance().getTimeInMillis();
            ChatMessage chatMessage = new ChatMessage(MainActivity.currentUser, myChatPartner, UUID.randomUUID().toString(),
                    mssg, time, true, ChatMessage.ChatTypes.ONEONONEONLINECHAT.getValue());

            actuallySendingTheMessage(chatMessage);
        }
    }

    //This function is where messages containing media will be readied up before sending
    private void setUpMediaChatMessages(String mssg){
        Log.e(TAG, SUB_TAG+"Sending media related messages");

        //Every attached item will be sent in its own message
        int fl_size = fileSelectedList.size();
        for(int i = 0; i < fl_size; i++){
            FileItem fi_tmp = fileSelectedList.get(i);

            long time  = Calendar.getInstance().getTimeInMillis();
            ChatMessage chatMessage = new ChatMessage(MainActivity.currentUser, myChatPartner, UUID.randomUUID().toString(),
                    mssg, time, true, ChatMessage.ChatTypes.ONEONONEONLINECHAT.getValue());

            //The image data is used to push the image online
            chatMessage.imgData = FileItem.picBitmapToString(fi_tmp.getPic());
            chatMessage.contains_img = true;
            chatMessage.imgUri = fi_tmp.getPicUri().toString();

            //The text typed (if there is any) should only appear in the first mssg of the list
            if(i > 0) {chatMessage.mssgText = "";}

            actuallySendingTheMessage(chatMessage);
        }

        clearSelectedFileList();
        mssgText.setText("");
    }

    //This function is the function that will push the message to the
    //  outside world, whether thats offline or online
    private void actuallySendingTheMessage(ChatMessage chatMessage){
        Log.e(TAG, SUB_TAG+"This function sends the message, actually!!!!");

        // TODO: add correct online/offline implementations here
        if(mode.getValue().equals(SendMode.ONLINE.getValue())){
            Toast.makeText(ChatActivity2.this, "Sending Online.", Toast.LENGTH_SHORT).show();
            FirebaseController.pushMessageToFirebase(chatMessage, mActivity);
        }
        else if(mode.getValue().equals(SendMode.OFFLINE_CONNECT_API.getValue())){
            Toast.makeText(ChatActivity2.this, "Sending Offline.", Toast.LENGTH_SHORT).show();
        }
        else if(mode.getValue().equals(SendMode.OFFLINE_RADIO.getValue())){
            Toast.makeText(ChatActivity2.this, "Sending thru the radio.", Toast.LENGTH_SHORT).show();
            sendMessageThruRadio(chatMessage);
        }

        //Then save a local copy
        saveMessageLocally(chatMessage);
    }

    private void saveMessageLocally(final ChatMessage chatMessage){
        Log.e(TAG, SUB_TAG+"Saving message locally: " + chatMessage);
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
                localDatabaseReference.saveChatMessageLocally(chatMessage);
            }
        });
    }

    private void addMessageOnScreen(final ChatMessage mssg){
        Log.e(TAG, SUB_TAG+"Adding the mssg on screen: " + mssg);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                chatMessages.add(mssg);
                adapter.notifyDataSetChanged();
                recyclerView.scrollToPosition(adapter.getItemCount() - 1);
                if(!(mssg.sender.getUid().equals(MainActivity.currentUser.getUid())))
                    mssgText.setText("");
            }
        });
    }

    private void sendMessageThruRadio(ChatMessage chatMessage){
        Log.e(TAG, SUB_TAG+"Sending message thru radio.");
        BlueTerm.sendMessage(chatMessage);
    }

    private void showRadioPopupButton(){
        Log.e(TAG, SUB_TAG+"Showing radio popup button");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                popup.getMenu().getItem(2).getActionView().setVisibility(View.VISIBLE);
            }
        });
    }

    private void hideRadioPopupButton(){
        Log.e(TAG, SUB_TAG+"Hide radio popup button");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                popup.getMenu().getItem(2).getActionView().setVisibility(View.VISIBLE);
            }
        });
    }

    @Override
    public void eventTriggered(HashMap<String, Object> packet, String type) {
        Log.e(TAG, SUB_TAG+"Event is triggered, with type: " + type);
        if(type.equals(Event.chatMssgEventID)) {
            Log.e(TAG, SUB_TAG+"this is a chat message event");
            ChatMessage mssg = MessageUtility.convertHashMapToChatMessage(packet);
            addMessageOnScreen(mssg);
        }
        else if(type.equals(Event.radioConnected)){
            Log.e(TAG, SUB_TAG+"Radio device connected");
            showRadioPopupButton();
        }
        else if(type.equals(Event.radioDisconnected)){
            Log.e(TAG, SUB_TAG+"Radio device disconnected");
            hideRadioPopupButton();
        }
    }

    private void clearSelectedFileList(){
        Log.e(TAG, SUB_TAG+"Clearing all the selected files in list");
        int fl_size = fileSelectedList.size();

        fileSelectedList.clear();
        mediaFileListAdapter.notifyItemRangeRemoved(0,fl_size);
        getFileSelectedParentLayout.setVisibility(View.GONE);
    }

    public void onItemClicked(int position){
        Log.e(TAG, SUB_TAG+"clicked on something: " + position);

        //Remove the clicked item
        fileSelectedList.remove(position);
        mediaFileListAdapter.notifyItemRemoved(position);

        if(fileSelectedList.size() <= 0){
            Log.e(TAG, SUB_TAG+"Nothing is selected, hiding");
            getFileSelectedParentLayout.setVisibility(View.GONE);
        }
    }
}