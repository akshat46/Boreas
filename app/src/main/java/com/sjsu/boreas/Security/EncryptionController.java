package com.sjsu.boreas.Security;

import android.content.Context;
import android.util.Base64;
import android.util.Log;
import android.widget.Toast;

import com.sjsu.boreas.Database.Contacts.User;
import com.sjsu.boreas.Database.Messages.ChatMessage;
import com.sjsu.boreas.MainActivity;
import com.sjsu.boreas.Misc.ContextHelper;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class EncryptionController {
    private static EncryptionController instance;
    private static String TAG = "BOREAS";
    private static String SUB_TAG = "----------------EncryptionController ";
    private static Context mContext;

    public static EncryptionController getInstance(){
        if(instance == null){
            instance = new EncryptionController();
            mContext = ContextHelper.get().getApplicationContext();
            return instance;
        }
        else return instance;
    }

    public ChatMessage getEncryptedMessage(ChatMessage message){
        Log.e(TAG, SUB_TAG+"Encrypting mssg");

        User recipient = message.recipient;

        String encryptedText = "";
        try {
            Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(Base64.decode(recipient.publicKey, Base64.DEFAULT));
            KeyFactory fac = KeyFactory.getInstance("RSA");

            cipher.init(Cipher.ENCRYPT_MODE, fac.generatePublic(keySpec));
            message.mssgText = Base64.encodeToString(cipher.doFinal(message.mssgText.getBytes("UTF-8")), Base64.DEFAULT);
            message.isEncrypted = true;
        }catch (NoSuchAlgorithmException e){
            Toast.makeText(mContext, "Error: Could not encrypt text- RSA alg not found", Toast.LENGTH_SHORT);
            e.printStackTrace();
        }catch (NoSuchPaddingException e){
            Toast.makeText(mContext, "Error: Could not encrypt text- Padding alg not found", Toast.LENGTH_SHORT);
            e.printStackTrace();
        }catch (InvalidKeySpecException | InvalidKeyException e){
            Toast.makeText(mContext, "Error: Could not parse recipient's key", Toast.LENGTH_SHORT);
            e.printStackTrace();
        }catch (UnsupportedEncodingException | BadPaddingException | IllegalBlockSizeException e){
            Toast.makeText(mContext, "Error: Could not perform encryption", Toast.LENGTH_SHORT);
            e.printStackTrace();
        }

        return message;
    }

    public ChatMessage getDecryptedMessage(ChatMessage message){

        try {
            Log.e(TAG, SUB_TAG+" initializing cupher");
            Cipher cipher = Cipher.getInstance("RSA");
            Log.e(TAG, SUB_TAG+" initializing spec");
            PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(Base64.decode(MainActivity.currentUser.privateKey, Base64.DEFAULT));
            Log.e(TAG, SUB_TAG+" initializing keyfactory");
            KeyFactory kf = KeyFactory.getInstance("RSA");

            Log.e(TAG, SUB_TAG+" cipher.init()");
            cipher.init(Cipher.DECRYPT_MODE, kf.generatePrivate(spec));
            Log.e(TAG, SUB_TAG+" decrypting text..");
            message.mssgText = new String(cipher.doFinal(Base64.decode(message.mssgText, Base64.DEFAULT)), "UTF-8");
            message.isEncrypted = false;
        }catch (NoSuchAlgorithmException e){
            Toast.makeText(mContext, "Error: Could not encrypt text- RSA alg not found", Toast.LENGTH_SHORT);
            e.printStackTrace();
        }catch (NoSuchPaddingException e){
            Toast.makeText(mContext, "Error: Could not encrypt text- Padding alg not found", Toast.LENGTH_SHORT);
            e.printStackTrace();
        }catch (InvalidKeySpecException | InvalidKeyException e){
            Toast.makeText(mContext, "Error: Could not parse recipient's key", Toast.LENGTH_SHORT);
            e.printStackTrace();
        }catch (UnsupportedEncodingException | BadPaddingException | IllegalBlockSizeException e){
            Toast.makeText(mContext, "Error: Could not perform encryption", Toast.LENGTH_SHORT);
            e.printStackTrace();
        }
        return message;
    }
}
