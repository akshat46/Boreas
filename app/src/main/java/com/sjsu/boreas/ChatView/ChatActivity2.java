package com.sjsu.boreas.ChatView;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.BitmapFactory;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

//import com.sjsu.boreas.ChatViewRelatedStuff.MessageListViewStuff.OneOnOneMessageAdapter;
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
import com.sjsu.boreas.Database.LocalDatabaseReference;
import com.sjsu.boreas.Misc.ContextHelper;
import com.sjsu.boreas.OnlineConnectionHandlers.FirebaseController;
import com.sjsu.boreas.MainActivity;
import com.sjsu.boreas.PhoneBluetoothRadio.BlueTerm;
import com.sjsu.boreas.R;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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
    private String[] FILE;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    private enum SendMode{
        ONLINE("ONLINE"),
        OFFLINE("OFFLINE");

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
        event.addListener(this);

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
        initNewUI();
        initSendButton();
        initChatMediaButton();
        initAdapter();
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

    private void initChatMediaButton(){
        Log.e(TAG, SUB_TAG+"Init chat media button");

        //Also gotta initialize the array list for the items selected
        fileSelectedList = new ArrayList<FileItem>();
        //Hide the linear layout view by default
        getFileSelectedParentLayout.setVisibility(View.GONE);

        btnChatMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

    private void loadImages() {
        Log.e(TAG, SUB_TAG + "Loading images yo");
        try {
            FILE = new String[]{
                    MediaStore.Images.ImageColumns._ID,
                    MediaStore.Images.ImageColumns.DATA,
                    MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME,
                    MediaStore.Images.ImageColumns.DATE_TAKEN,
                    MediaStore.Images.ImageColumns.MIME_TYPE,
            };

            Cursor cursor = getApplicationContext().getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    FILE, null, null, null);

            if (cursor == null) {
                Log.e(TAG, SUB_TAG + "Cursor is null");
                return;
            }
            fileSelectedList = new ArrayList<FileItem>();
            while (cursor.moveToNext()) {
                int columnIndex = cursor.getColumnIndex(FILE[0]);
                String imageDecode = cursor.getString(columnIndex);
//                    cursor.close();
                FileItem fileItem = new FileItem(BitmapFactory.decodeFile(imageDecode));
                fileSelectedList.add(fileItem);
//                imageViewLoad.setImageBitmap(BitmapFactory.decodeFile(imageDecode));
            }

            //Setting up the recycler view for the media files selected
            Log.e(TAG, SUB_TAG + "setting up the file list adapter");
            mediaFileListAdapter = new MediaFileListAdapter(fileSelectedList, this);
            fileSelectedView.setAdapter(mediaFileListAdapter);
            mediaListLayoutManager = new LinearLayoutManager(mActivity, LinearLayoutManager.HORIZONTAL, false);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    fileSelectedView.setLayoutManager(mediaListLayoutManager);
                }
            });

        } catch (Exception e) {
            Log.e(TAG, SUB_TAG + "Exception: " + e.toString());
            Toast.makeText(this, "Please try again", Toast.LENGTH_LONG)
                    .show();
        }
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Log.e(TAG, SUB_TAG+"Result received");
        try {
            if (requestCode == MEDIA_RESULT && resultCode == RESULT_OK
                    && null != data) {
                verifyStoragePermissions(this);
                inflateSelectedFilesView();
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

                ArrayList<FileItem> fl= new ArrayList<FileItem>();
                while (cursor.moveToNext()) {
                    int columnIndex = cursor.getColumnIndex(FILE[0]);
                    String imageDecode = cursor.getString(columnIndex);
//                    cursor.close();
                    FileItem fileItem = new FileItem(BitmapFactory.decodeFile(imageDecode));
                    fl.add(fileItem);
//                imageViewLoad.setImageBitmap(BitmapFactory.decodeFile(imageDecode));
                }
                fileSelectedList.addAll(fl);
                //Setting up the recycler view for the media files selected
                Log.e(TAG, SUB_TAG+"setting up the file list adapter");
                mediaFileListAdapter = new MediaFileListAdapter(fileSelectedList, mActivity);
                fileSelectedView.setAdapter(mediaFileListAdapter);
                mediaListLayoutManager = new LinearLayoutManager(mActivity, LinearLayoutManager.HORIZONTAL, false);
                fileSelectedView.setLayoutManager(mediaListLayoutManager);

                if(fileSelectedList.size() > 0){
                    Log.e(TAG, SUB_TAG+"Some stuff is selected");
                    getFileSelectedParentLayout.setVisibility(View.VISIBLE);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, SUB_TAG+"Exception: " + e.toString());
            Toast.makeText(this, "Please try again", Toast.LENGTH_LONG)
                    .show();
        }
    }

    private void inflateSelectedFilesView(){
        Log.e(TAG, SUB_TAG+"Inflate selected files view");

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
                PopupMenu popup = new PopupMenu(ChatActivity2.this, btnSend);
                popup.inflate(R.menu.menu_send);
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
                        if(item.getTitle().toString().toUpperCase().equals(SendMode.OFFLINE.getValue())) {
                            Log.e(TAG, SUB_TAG+"changing the mode to offline");
                            btnSend.setImageResource(R.drawable.ic_offline_send_f);
                            btnSend.setBackgroundResource(R.drawable.bg_button_send_offline);
                            mode =SendMode.OFFLINE;
                        }
                        else if(item.getTitle().toString().toUpperCase().equals(SendMode.ONLINE.getValue())){
                            Log.e(TAG, SUB_TAG+"Changing the mode to online");
                            btnSend.setImageResource(R.drawable.ic_online_send_f);
                            btnSend.setBackgroundResource(R.drawable.bg_button);
                            mode =SendMode.ONLINE;
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
                recyclerView.setAdapter(adapter);
                layoutManager = new LinearLayoutManager(mActivity);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        recyclerView.setLayoutManager(layoutManager);
                    }
                });
                recyclerView.scrollToPosition(adapter.getItemCount() - 1);
            }
        });
    }

    private void sendMessage(String mssg){
        Log.e(TAG, SUB_TAG+"sending message");

        // TODO: add correct online/offline implementations here
        if(mode.getValue().equals(SendMode.ONLINE.getValue())){
            Toast.makeText(ChatActivity2.this, "Sending Online.", Toast.LENGTH_SHORT).show();
        }
        else if(mode.getValue().equals(SendMode.OFFLINE.getValue())){
            Toast.makeText(ChatActivity2.this, "Sending Offline.", Toast.LENGTH_SHORT).show();
        }

        long time  = Calendar.getInstance().getTimeInMillis();
        ChatMessage chatMessage = null;

        //TODO: this if was used for debugging and testing different types of mssges being sent,
        //  during the times we were testing with radio stuff, gotta decide on whether to keep this or remove it
        //  (most likely remove it)
        if(!(mssg.equals("4"))) {
            chatMessage = new ChatMessage(MainActivity.currentUser, myChatPartner, UUID.randomUUID().toString(),
                    mssg, time, true, ChatMessage.ChatTypes.ONEONONEONLINECHAT.getValue());
        }

        mssgText.setText("");

        pushMessageToFirebase(chatMessage);
        saveMessageLocally(chatMessage);

        //TODO: gotta add a way to send stuff thru the radio
//        sendMessageThruRadio(chatMessage);
    }

    private void pushMessageToFirebase(ChatMessage chatMessage){  
        Log.e(TAG, SUB_TAG+"Push message to firebase");
        String oneOnOneChatId = "";

        String firstUser = MainActivity.currentUser.getUid();
        String secondUser = myChatPartner.getUid();

        Map<String, Object> new_chat_mssg = chatMessage.toMap();

        Map<String, Object> firebase_child_update = new HashMap<>();

        if(getOneOnOneChatFirebaseID(firstUser, secondUser)){
            oneOnOneChatId = firstUser + secondUser;
            firebase_child_update.put("/oneOnOneChats/" + oneOnOneChatId + "/user1", firstUser);
            firebase_child_update.put("/oneOnOneChats/" + oneOnOneChatId + "/user2", secondUser);
        }
        else{
            oneOnOneChatId = secondUser + firstUser;
            firebase_child_update.put("/oneOnOneChats/" + oneOnOneChatId + "/user1", secondUser);
            firebase_child_update.put("/oneOnOneChats/" + oneOnOneChatId + "/user2", firstUser);
        }

        //We are putting this data under the users branch of the firebase database
        firebase_child_update.put("/oneOnOneChats/" + oneOnOneChatId + "/messages/" + chatMessage.mssgId, new_chat_mssg);

        //Do the actual writing of the data onto firebase
        FirebaseController.getDatabaseReference().updateChildren(firebase_child_update);
    }

    private void saveMessageLocally(final ChatMessage chatMessage){
        Log.e(TAG, SUB_TAG+"Saving message locally");
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

    //This message creates a unique id which will be used to identify the chat between the 2 users
    //  to whom the 2 ID's belong to
    private boolean getOneOnOneChatFirebaseID(String user1ID, String user2ID){
        Log.e(TAG, SUB_TAG+"getting the ID to be used on firebase");
        if((user2ID.compareTo(user1ID)) > 0){
            Log.e(TAG, SUB_TAG+"This is the id: " + user1ID+user2ID);
            return true;
        }
        Log.e(TAG, SUB_TAG+"This is the id: " + user2ID+user1ID);
        return false;
    }

    private void sendMessageThruRadio(ChatMessage chatMessage){
        Log.e(TAG, SUB_TAG+"Sending message thru radio.");
        String mssgForRadio = "";
        int mssgType = 0;
        if(chatMessage != null) {
            mssgForRadio = convertMessageToString(chatMessage);
            mssgType = chatMessage.mssgType;
        }
        else{
            mssgType = ChatMessage.ChatTypes.GETMESSAGESFROMRADIO.getValue();
        }
        Log.e(TAG, SUB_TAG+mssgForRadio.getBytes());
        BlueTerm.sendMessage(mssgForRadio, mssgType);
    }

    private String convertMessageToString(ChatMessage mssg){
        if(mssg == null)
            return null;
        return mssg.toString();
    }

    @Override
    public void eventTriggered(HashMap<String, Object> packet, String type) {
        Log.e(TAG, SUB_TAG+"Event is triggered, with type: " + type);
        if(type.equals(Event.chatMssgEventID)) {
            Log.e(TAG, SUB_TAG+"this is a chat message event");
            ChatMessage mssg = MessageUtility.convertHashMapToChatMessage(packet);
            addMessageOnScreen(mssg);
        }
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