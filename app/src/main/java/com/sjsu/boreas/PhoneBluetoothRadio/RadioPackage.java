package com.sjsu.boreas.PhoneBluetoothRadio;

import android.util.Log;

import com.sjsu.boreas.Database.Contacts.User;
import com.sjsu.boreas.Database.LocalDatabaseReference;
import com.sjsu.boreas.Database.Messages.ChatMessage;
import com.sjsu.boreas.Database.Messages.MessageUtility;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class RadioPackage {
    private String packg_id;
    private double number_or_sub_packgs;
    private double sub_packg_position;
    public String packg_data;

    private static final String PCKG_ID = "<packg_id>";
    private static final String NUM_SUB_PCKGS = "<number_or_sub_packgs>";
    private static final String POSITION = "<sub_packg_position>";
    private static final String DATA = "<packg_data>";


    private static String TAG = "BOREAS";
    private static String SUB_TAG = "-------RadioPackage-- ";

    private static LocalDatabaseReference localDatabaseReference = LocalDatabaseReference.get();

    public RadioPackage(String divided_chatMssg){
        this.packg_data = divided_chatMssg;
    }

    public String toString(){
        Log.e(TAG, SUB_TAG+"converting the radio package to string");
        String radioPackgStr = PCKG_ID + packg_id
                                +   NUM_SUB_PCKGS + String.valueOf(number_or_sub_packgs)
                                +   POSITION + String.valueOf(sub_packg_position)
                                +   DATA + packg_data;
        return radioPackgStr;
    }

    private RadioPackage(){
        Log.e(TAG, SUB_TAG+"Private constructor");
        packg_id = "";
        packg_data = "";
        number_or_sub_packgs = 0;
        sub_packg_position = -1;
    }

    public static RadioPackage stringToRadioPackg(String strRadioPackg){
        Log.e(TAG, SUB_TAG+"Converting from json to radio packg: " + strRadioPackg);
        JSONObject jsonPackg = null;
        String str_chat_mssg = "";

        RadioPackage radioPackage = new RadioPackage();
        int pacg_len = strRadioPackg.length();

        int packg_id = strRadioPackg.indexOf(PCKG_ID);
        int num_sub_pck_id = strRadioPackg.indexOf(NUM_SUB_PCKGS);
        int pos_id = strRadioPackg.indexOf(POSITION);
        int data_id = strRadioPackg.indexOf(DATA);

//        try {
            if (packg_id != -1) {
                int pckg_len = PCKG_ID.length();
                int amnt = num_sub_pck_id - packg_id - pckg_len;
                String data = strRadioPackg.substring(packg_id + pckg_len, num_sub_pck_id);
                Log.e(TAG, SUB_TAG + "found the index: " + packg_id + "\n" + data);
                radioPackage.packg_id = data;
            }
//
            if (num_sub_pck_id != -1) {
                int num_pckgs_len = NUM_SUB_PCKGS.length();
//                int amnt = pos_id - num_sub_pck_id - num_pckgs_len;
                String data = strRadioPackg.substring(num_sub_pck_id + num_pckgs_len, pos_id);
                Log.e(TAG, SUB_TAG + "found the index: " + num_sub_pck_id + "\n" + data);
                radioPackage.number_or_sub_packgs = Double.parseDouble(data);
            }

            if (pos_id != -1) {
                int pos_id_len = POSITION.length();
//                int amnt = data_id - pos_id - pos_id_len;
                String data = strRadioPackg.substring(pos_id + pos_id_len, data_id);
                Log.e(TAG, SUB_TAG + "found the index: " + pos_id + "\n" + data);
                radioPackage.sub_packg_position = Double.parseDouble(data);
            }

            if (data_id != -1) {
                int data_id_len = DATA.length();
//                int amnt = strRadioPackg.length() - packg_id - data_id_len;
                String data = strRadioPackg.substring(data_id + data_id_len, strRadioPackg.length());
                Log.e(TAG, SUB_TAG + "found the index: " + data_id + "\n" + data);
                radioPackage.packg_data = data;
            }
//        }
//        catch (Exception e){
//            Log.e(TAG, SUB_TAG+e);
//        }

//        try {
//            //First get json object from string
//            jsonPackg = new JSONObject(jsonRadioPackg);
//
//            radioPackage = new RadioPackage();
//
//            radioPackage.packg_id = jsonPackg.getString("packg_id");
//            radioPackage.packg_data = jsonPackg.getString("packg_data");
//            radioPackage.number_or_sub_packgs = Double.parseDouble(jsonPackg.getString("number_or_sub_packgs"));
//            radioPackage.sub_packg_position = Double.parseDouble(jsonPackg.getString("sub_packg_position"));
//
//            Log.e(TAG, SUB_TAG+"New radioPackg: "+ radioPackage);
//        } catch (JSONException e) {
//            Log.e(TAG, SUB_TAG+"JSON exception: \n\t" + e);
//            e.printStackTrace();
//        }

        return radioPackage;
    }

    public static void sortOutThePackagesReceived(ArrayList<RadioPackage> radioPackages){
        Log.e(TAG, SUB_TAG+"Sorting out the received packages");

        int num_of_pckgs = radioPackages.size();
        String str_chat_mssg = "";
        for(int i = 0; i < num_of_pckgs; i++){

//            String str_pckg = radioPackages.get(i);
            if(!(str_chat_mssg.isEmpty())) {
                Log.e(TAG, SUB_TAG + "\t" + radioPackages.get(i).packg_data);
//                RadioPackage radioPackage = jsonStringToRadioPackg(str_pckg);
//                str_chat_mssg = str_chat_mssg + radioPackage.packg_data;
            }
        }

        Log.e(TAG, SUB_TAG+"\n\t" + str_chat_mssg);
        ChatMessage chatMessage = MessageUtility.convertJsonToMessage(str_chat_mssg);

        localDatabaseReference.saveChatMessageLocally(chatMessage);
    }

    public static ArrayList<RadioPackage> getRadioPackgsToSend(ChatMessage chatMessage){
        Log.e(TAG, SUB_TAG+"Getting the list of radio packages to send out");
        ArrayList<RadioPackage> radio_packgs_to_send = new ArrayList<RadioPackage>();
        ArrayList<String> divided_chat_mssg = divideTheChatMessage(chatMessage.toString());

        //Put the divided chatmssg into the list of radio packages
        int divided_chat_mssgs_len = divided_chat_mssg.size();
        for(int i = 0; i < divided_chat_mssgs_len; i++){
            RadioPackage radioPackage = new RadioPackage();

            radioPackage.packg_id = chatMessage.mssgId;
            radioPackage.packg_data = divided_chat_mssg.get(i);
            radioPackage.sub_packg_position = i;
            radioPackage.number_or_sub_packgs = divided_chat_mssgs_len;

            radio_packgs_to_send.add(radioPackage);
        }

        return radio_packgs_to_send;
    }

    //This function takes a chatmessage string and divides it into an arraylist of strings
    private static ArrayList<String> divideTheChatMessage(String chat_mssg_str){
        Log.e(TAG, SUB_TAG+"Dividing up the chat message");
        ArrayList<String> divided_chat_mssg = new ArrayList<String>();

        //Dividing the string into sub parts of 50
        int sub_str_len = 40;
        int sub_str_ind = 0;
        int chat_mssg_str_len = chat_mssg_str.length();

        for(int i = 0; i < chat_mssg_str_len; i=i+sub_str_len){
            //Go to the position of the substring index
            sub_str_ind = i + sub_str_len;
            //Check if the position isn't bigger than the full str length
            if(sub_str_ind < chat_mssg_str_len){
                String str = chat_mssg_str.substring(i, sub_str_ind);
                Log.e(TAG, SUB_TAG+"\n"+str);
                divided_chat_mssg.add(str);
            }else{
                //Get the final part of the chatmssg string
                String str = chat_mssg_str.substring(i, chat_mssg_str_len);
                Log.e(TAG, SUB_TAG+"\n"+str);
                divided_chat_mssg.add(str);
            }
        }

        return divided_chat_mssg;
    }
}
