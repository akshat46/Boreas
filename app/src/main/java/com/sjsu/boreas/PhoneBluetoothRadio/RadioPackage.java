package com.sjsu.boreas.PhoneBluetoothRadio;

import android.util.Log;

import com.sjsu.boreas.Database.Contacts.User;
import com.sjsu.boreas.Database.Messages.ChatMessage;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class RadioPackage {
    private String packg_id;
    private double number_or_sub_packgs;
    private double sub_packg_position;
    private String packg_data;

    private static String TAG = "BOREAS";
    private static String SUB_TAG = "-------RadioPackage-- ";

    public RadioPackage(String divided_chatMssg){
        this.packg_data = divided_chatMssg;
    }

    public String toString(){
        Log.e(TAG, SUB_TAG+"converting the radio package to string");
        String radioPackgStr = "{" +
                "packg_id: \"" + packg_id + "\","
                +   "\"number_or_sub_packgs\": \"" + String.valueOf(number_or_sub_packgs) + "\","
                +   "\"sub_packg_position\": " + String.valueOf(sub_packg_position) + ","
                +   "\"packg_data\": " + packg_data
                + "} \n";
        return radioPackgStr;
    }

    private RadioPackage(){
        Log.e(TAG, SUB_TAG+"Private constructor");
        packg_id = "";
        packg_data = "";
        number_or_sub_packgs = 0;
        sub_packg_position = -1;
    }

    public RadioPackage jsonStringToRadioPackg(String jsonRadioPackg){
        Log.e(TAG, SUB_TAG+"Converting from json to radio packg");
        RadioPackage radioPackage = null;
        JSONObject jsonPackg = null;

        try {
            //First get json object from string
            jsonPackg = new JSONObject(jsonRadioPackg);

            radioPackage = new RadioPackage();

            radioPackage.packg_id = jsonPackg.getString("packg_id");
            radioPackage.packg_data = jsonPackg.getString("packg_data");
            radioPackage.number_or_sub_packgs = Double.parseDouble(jsonPackg.getString("number_or_sub_packgs"));
            radioPackage.sub_packg_position = Double.parseDouble(jsonPackg.getString("sub_packg_position"));

            Log.e(TAG, SUB_TAG+"New radioPackg: "+ radioPackage);
        } catch (JSONException e) {
            Log.e(TAG, SUB_TAG+"JSON exception: \n\t" + e);
            e.printStackTrace();
        }

        return radioPackage;
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
        int sub_str_len = 50;
        int sub_str_ind = 0;
        int chat_mssg_str_len = chat_mssg_str.length();

        for(int i = 0; i < chat_mssg_str_len; i=i+sub_str_len+1){
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
