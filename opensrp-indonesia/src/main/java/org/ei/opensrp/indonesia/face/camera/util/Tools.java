package org.ei.opensrp.indonesia.face.camera.util;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.media.MediaScannerConnection;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.qualcomm.snapdragon.sdk.face.FacialProcessing;

import org.ei.opensrp.Context;
import org.ei.opensrp.domain.ProfileImage;
import org.ei.opensrp.indonesia.face.camera.ClientsList;
import org.ei.opensrp.indonesia.face.camera.ImageConfirmation;
import org.ei.opensrp.indonesia.face.camera.SmartShutterActivity;
import org.ei.opensrp.indonesia.kartu_ibu.KIDetailActivity;
import org.ei.opensrp.repository.ImageRepository;
import org.ei.opensrp.view.activity.DrishtiApplication;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import cz.msebera.android.httpclient.Header;

import static android.support.v4.app.ActivityCompat.startActivity;
import static android.support.v4.app.ActivityCompat.startActivityForResult;


/**
 * Created by wildan on 1/4/17.
 */
public class Tools {

    private static final String TAG = Tools.class.getSimpleName();
    public static final int CONFIDENCE_VALUE = 58;
    public static org.ei.opensrp.Context appContext;
    private Canvas canvas = null;
    SmartShutterActivity ss = new SmartShutterActivity();
    ClientsList cl = new ClientsList();
    private static HashMap<String, String> hash;
    private String albumBuffer;
    private List<ProfileImage> list;
    private static String anmId = Context.getInstance().allSharedPreferences().fetchRegisteredANM();
    private static ProfileImage profileImage = new ProfileImage();
    private static ImageRepository imageRepo = (ImageRepository) org.ei.opensrp.Context.imageRepository();


    public Tools() {
        Log.e(TAG, "Tools: 1");
        imageRepo = (ImageRepository) org.ei.opensrp.Context.imageRepository();
//        hash = retrieveHash(appContext.applicationContext());
    }

    public Tools(org.ei.opensrp.Context appContext) {
        imageRepo = (ImageRepository) org.ei.opensrp.Context.imageRepository();
        Tools.appContext = appContext;
//        hash = retrieveHash(appContext.applicationContext());
    }

    /**
     * Method to Stored Bitmap as File and Buffer
     * @param bitmap Photo bitmap
     * @param entityId Base user id
     * @param faceVector Vector from face
     * @param updated capture mode
     * @return Boolean
     */
//    public static boolean WritePictureToFile(android.content.Context context, Bitmap bitmap, String entityId, byte[] faceVector, boolean updated) {
    public static boolean WritePictureToFile(Bitmap bitmap, String entityId, String[] faceVector, boolean updated) {

        File pictureFile = getOutputMediaFile(0, entityId);
        File thumbs_photo = getOutputMediaFile(1, entityId);

        if (pictureFile == null || thumbs_photo == null) {
            Log.e(TAG, "Error creating media file, check storage permissions!");
            return false;
        }

        try {
            FileOutputStream fos = new FileOutputStream(pictureFile);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.close();
            Log.e(TAG, "Wrote image to " + pictureFile);

//            MediaScannerConnection.scanFile(context, new String[]{
//                            pictureFile.toString()}, null,
//                    new MediaScannerConnection.OnScanCompletedListener() {
//                        public void onScanCompleted(String path, Uri uri) {
//                            Log.i("ExternalStorage", "Scanned " + path + ":");
//                            Log.i("ExternalStorage", "-> uri=" + uri);
//                        }
//                    });
            String photoPath = pictureFile.toString();
            Log.e(TAG, "Photo Path = " + photoPath);

//            Create Thumbs
            FileOutputStream tfos = new FileOutputStream(thumbs_photo);
            final int THUMBSIZE = FaceConstants.THUMBSIZE;

            Bitmap ThumbImage = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(photoPath),
                    THUMBSIZE, THUMBSIZE);
            ThumbImage.compress(Bitmap.CompressFormat.PNG, 100, tfos);
            tfos.close();
            Log.e(TAG, "Wrote Thumbs image to " + thumbs_photo);

//           FIXME File & Database Stored
//            saveStaticImageToDisk(entityId, ThumbImage, Arrays.toString(faceVector), updated);

            saveToDb(entityId, thumbs_photo.toString(), Arrays.toString(faceVector), updated);

            return true;

        } catch (FileNotFoundException e) {
            Log.d(TAG, "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d(TAG, "Error accessing file: " + e.getMessage());
        }
        return false;
    }

    private static void saveToDb(String entityId, String absoluteFileName, String faceVector, boolean updated) {

        // insert into the db local

        profileImage.setImageid(UUID.randomUUID().toString());
        profileImage.setAnmId(anmId);
        profileImage.setEntityID(entityId);
        profileImage.setContenttype("jpeg");
        profileImage.setFilepath(absoluteFileName);
        profileImage.setFilecategory("profilepic");
        profileImage.setFilevector(faceVector);
        profileImage.setSyncStatus(ImageRepository.TYPE_Unsynced);

        imageRepo.add(profileImage);

    }

    /**
     * Method create new file
     * @param mode capture mode.
     * @param entityId Base user id.
     * @return File
     */
    @Nullable
    private static File getOutputMediaFile(Integer mode, String entityId) {
        // Mode 0 = Original
        // Mode 1 = Thumbs

        // Location use app_dir
        String imgFolder = (mode == 0) ? DrishtiApplication.getAppDir() :
                DrishtiApplication.getAppDir() + File.separator + ".thumbs";
//        String imgFolder = (mode == 0) ? "OPENSRP_SID":"OPENSRP_SID"+File.separator+".thumbs";
//        File mediaStorageDir = new File(
//                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), imgFolder);
        File mediaStorageDir = new File(imgFolder);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            Log.e(TAG, "failed to find directory " + mediaStorageDir.getAbsolutePath());
            if (!mediaStorageDir.mkdirs()) {
                Log.e(TAG, "Created new directory " + mediaStorageDir.getAbsolutePath());
                return null;
            }
        }

        // Create a media file name
        return new File(String.format("%s%s%s.jpg", mediaStorageDir.getPath(), File.separator, entityId));
    }

    /**
     * Methof for Draw Information of existing Person
     * @param rect Rectangular
     * @param mutableBitmap Bitmap
     * @param pixelDensity Pixel group area
     * @param personName name
     */
    public static void drawInfo(Rect rect, Bitmap mutableBitmap, float pixelDensity, String personName) {
//        Log.e(TAG, "drawInfo: rect " + rect);
//        Log.e(TAG, "drawInfo: bitmap" + mutableBitmap);

        // Extra padding around the faceRects
        rect.set(rect.left -= 20, rect.top -= 20, rect.right += 20, rect.bottom += 20);
        Canvas canvas = new Canvas(mutableBitmap);
        Paint paintForRectFill = new Paint();

        // Draw rect fill
        paintForRectFill.setStyle(Paint.Style.FILL);
        paintForRectFill.setColor(Color.WHITE);
        paintForRectFill.setAlpha(80);

        // Draw rectangular strokes
        Paint paintForRectStroke = new Paint();
        paintForRectStroke.setStyle(Paint.Style.STROKE);
        paintForRectStroke.setColor(Color.GREEN);
        paintForRectStroke.setStrokeWidth(5);
        canvas.drawRect(rect, paintForRectFill);
        canvas.drawRect(rect, paintForRectStroke);

//        float pixelDensity = getResources().getDisplayMetrics().density;
        int textSize = (int) (rect.width() / 25 * pixelDensity);

        Paint paintForText = new Paint();
        Paint paintForTextBackground = new Paint();
        Typeface tp = Typeface.SERIF;
        Rect backgroundRect = new Rect(rect.left, rect.bottom, rect.right, (rect.bottom + textSize));

        paintForText.setColor(Color.WHITE);
        paintForText.setTextSize(textSize);
        paintForTextBackground.setStyle(Paint.Style.FILL);
        paintForTextBackground.setColor(Color.BLACK);
        paintForText.setTypeface(tp);
        paintForTextBackground.setAlpha(80);

        if (personName != null) {
            canvas.drawRect(backgroundRect, paintForTextBackground);
            canvas.drawText(personName, rect.left, rect.bottom + (textSize), paintForText);
        } else {
            canvas.drawRect(backgroundRect, paintForTextBackground);
            canvas.drawText("Not identified", rect.left, rect.bottom + (textSize), paintForText);
        }

//        confirmationView.setImageBitmap(mutableBitmap);

    }

    /**
     * Draw Area that detected as Face
     * @param rect Rectangular
     * @param mutableBitmap Modified Bitmap
     * @param pixelDensity Pixel area density
     */
    public static void drawRectFace(Rect rect, Bitmap mutableBitmap, float pixelDensity) {

        Log.e(TAG, "drawRectFace: rect " + rect);
        Log.e(TAG, "drawRectFace: bitmap " + mutableBitmap);
        Log.e(TAG, "drawRectFace: pixelDensity " + pixelDensity);

        // Extra padding around the faceRects
        rect.set(rect.left -= 20, rect.top -= 20, rect.right += 20, rect.bottom += 20);
        Canvas canvas = new Canvas(mutableBitmap);

        // Draw rect fill
        Paint paintForRectFill = new Paint();
        paintForRectFill.setStyle(Paint.Style.FILL);
        paintForRectFill.setColor(Color.WHITE);
        paintForRectFill.setAlpha(80);

        // Draw rect strokes
        Paint paintForRectStroke = new Paint();
        paintForRectStroke.setStyle(Paint.Style.STROKE);
        paintForRectStroke.setColor(Color.GREEN);
        paintForRectStroke.setStrokeWidth(5);

        // Draw Face detected Area
        canvas.drawRect(rect, paintForRectFill);
        canvas.drawRect(rect, paintForRectStroke);

    }

    /**
     * Stored list detected Base entity ID to Shared Preference for buffered
     * @param hashMap HashMap
     * @param context context
     */
    public static void saveHash(HashMap<String, String> hashMap, android.content.Context context) {
        SharedPreferences settings = context.getSharedPreferences(FaceConstants.HASH_NAME, 0);

        SharedPreferences.Editor editor = settings.edit();
        editor.clear();
        Log.e(TAG, "Hash Save Size = " + hashMap.size());
        for (String s : hashMap.keySet()) {
            editor.putString(s, hashMap.get(s));
        }
        editor.apply();
    }

    /**
     * Get Existing Hash
     * @param context Context
     * @return hash
     */
    public static HashMap<String, String> retrieveHash(android.content.Context context) {
        Log.e(TAG, "retrieveHash: " + "Fetch");
        SharedPreferences settings = context.getSharedPreferences(FaceConstants.HASH_NAME, 0);
        HashMap<String, String> hash = new HashMap<>();
        hash.putAll((Map<? extends String, ? extends String>) settings.getAll());
        return hash;
    }

    /**
     * Save Vector array to xml
     */
    public void saveAlbum() {
//        byte[] albumBuffer = SmartShutterActivity.faceProc.serializeRecogntionAlbum();
//		saveCloud(albumBuffer);
//        Log.e(TAG, "Size of byte Array =" + albumBuffer.length);
//        SharedPreferences settings = getSharedPreferences(FaceConstants.ALBUM_NAME, 0);
//        SharedPreferences.Editor editor = settings.edit();
//        editor.putString("albumArray", Arrays.toString(albumBuffer));
//        editor.apply();
    }

    public void loadAlbum() {

        Log.e(TAG, "loadAlbum: ");
        SharedPreferences settings = appContext.applicationContext().getSharedPreferences(FaceConstants.ALBUM_NAME, 0);
        String arrayOfString = settings.getString("albumArray", null);
//
        byte[] albumArray;
        if (arrayOfString != null) {
            String[] splitStringArray = arrayOfString.substring(1,
                    arrayOfString.length() - 1).split(", ");

            albumArray = new byte[splitStringArray.length];
            for (int i = 0; i < splitStringArray.length; i++) {
                albumArray[i] = Byte.parseByte(splitStringArray[i]);
            }
            SmartShutterActivity.faceProc.deserializeRecognitionAlbum(albumArray);
            Log.e(TAG, "De-Serialized Album Success!");
        }
    }

    public static void alertDialog(android.content.Context context, int opt) {
        final AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
        Tools tools = new Tools();
//        alertDialog.setMessage(message);
        String message = "";
        switch (opt) {
            case 0:
                message = "Are you sure to empty The Album?";
//                doEmpty;
                break;
            case 1:
                message = "Are you sure to delete item";
                break;
            default:
                break;
        }
        alertDialog.setMessage(message);
//        alertDialog.setButton("OK", do);
        alertDialog.setPositiveButton("ERASE", tools.doEmpty);
        alertDialog.show();
    }

    private DialogInterface.OnClickListener doEmpty = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialog, int id) {
            boolean result = SmartShutterActivity.faceProc.resetAlbum();
            if (result) {
//                HashMap<String, String> hashMap = SmartShutterActivity.retrieveHash(getApplicationContext());
//                HashMap<String, String> hashMap = retrieveHash(getApplicationContext());
//                HashMap<String, String> hashMap = retrieveHash();
//                hashMap.clear();
//                SmartShutterActivity ss = new SmartShutterActivity();
//                saveHash(hashMap, getApplicationContext());
//                saveAlbum();
//                Toast.makeText(getApplicationContext(),
//                        "Album Reset Successful.",
//                        Toast.LENGTH_LONG).show();
            } else {
//                Toast.makeText(
//                        getApplicationContext(),
//                        "Internal Error. Reset album failed",
//                        Toast.LENGTH_LONG).show();
            }
        }
    };

    public void resetAlbum() {

        Log.e(TAG, "resetAlbum: " + "start");
        boolean result = SmartShutterActivity.faceProc.resetAlbum();

        if (result) {
            // Clear data
            // TODO: Null getApplication COntext
            HashMap<String, String> hashMap = SmartShutterActivity.retrieveHash(new ClientsList().getApplicationContext());
            hashMap.clear();
            saveHash(hashMap, cl.getApplicationContext());
            saveAlbum();

            Toast.makeText(cl.getApplicationContext(), "Reset Succesfully done!", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(cl.getApplicationContext(), "Reset Failed!", Toast.LENGTH_LONG).show();

        }
        Log.e(TAG, "resetAlbum: " + "finish");
    }

    /**
     * Stored from Local captured
     * @param entityId Base entity Id
     * @param image Photo image
     * @param contentVector Face vector
     * @param updated Boolean updated mode
     */
    public static void saveStaticImageToDisk(String entityId, Bitmap image, String contentVector, boolean updated) {
        String anmId = Context.getInstance().allSharedPreferences().fetchRegisteredANM();

        String[] res = contentVector.substring(1, contentVector.length() - 1).split(",");

        Log.e(TAG, "saveStaticImageToDisk: " + res.length);
        String[] faceVector = Arrays.copyOfRange(res, 32, 332);
        Log.e(TAG, "saveStaticImageToDisk: " + faceVector.length);

        if (image != null) {
            OutputStream os = null;
            try {

                if (entityId != null && !entityId.isEmpty()) {
                    final String absoluteFileName = DrishtiApplication.getAppDir() + File.separator + entityId + ".JPEG";

                    File outputFile = new File(absoluteFileName);
                    os = new FileOutputStream(outputFile);
                    Bitmap.CompressFormat compressFormat = Bitmap.CompressFormat.JPEG;
                    if (compressFormat != null) {
                        image.compress(compressFormat, 100, os);
                    } else {
                        throw new IllegalArgumentException(
                                "Failed to save static image, could not retrieve image compression format from name "
                                + absoluteFileName );
                    }

                    // insert into the db local
                    ProfileImage profileImage = new ProfileImage();

                    profileImage.setImageid(UUID.randomUUID().toString());
                    profileImage.setAnmId(anmId);
                    profileImage.setEntityID(entityId);
                    profileImage.setContenttype("jpeg");
                    profileImage.setFilepath(absoluteFileName);
                    profileImage.setFilecategory("profilepic");
                    profileImage.setFilevector(Arrays.toString(faceVector));
                    profileImage.setSyncStatus(ImageRepository.TYPE_Unsynced);

                    ImageRepository imageRepo = (ImageRepository) org.ei.opensrp.Context.imageRepository();
                    imageRepo.add(profileImage);
                }

            } catch (FileNotFoundException e) {
                Log.e(TAG, "Failed to save static image to disk");
            } finally {
                if (os != null) {
                    try {
                        os.close();
                    } catch (IOException e) {
                        Log.e(TAG, "Failed to close static images output stream after attempting to write image");
                    }
                }
            }
        }
    }

    /**
     * Fetch data from API (json
     */
    public void setVectorfromAPI() {

        String DRISTHI_BASE_URL = appContext.configuration().dristhiBaseURL();
        String user = appContext.allSharedPreferences().fetchRegisteredANM();
        String pwd = appContext.allSettings().fetchANMPassword();
        String api_url = DRISTHI_BASE_URL + "/multimedia-file?anm-id=" + user;

        AsyncHttpClient client = new AsyncHttpClient();

        client.setBasicAuth(user, pwd);

        client.get(api_url, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                try {
                    JSONArray response = new JSONArray(new String(responseBody));

                    for (int i = 0; i < response.length(); i++) {
                        JSONObject data = response.getJSONObject(i);

                        String uid = data.getString("caseId");

                        // To AlbumArray
                        String faceVector = data.getJSONObject("attributes").getString("faceVector");

                        // Update Table ImageList on existing record
                        imageRepo.updateByEntityId(uid, faceVector);

                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                Log.e(TAG, "onFailure: ");
            }
        });


    }

    /**
     * Save to Buffer from Local DB
     * @param context
     */
    public void parseSaveVector(Context context) {

        hash = retrieveHash(context.applicationContext());
        list = imageRepo.allVectorImages();

        int i = 0;
        for (ProfileImage pi : list) {
            i++;
            String uid = pi.getEntityID();
            String filevector = pi.getFilevector();
            Log.e(TAG, "parseSaveVector: " + filevector);

            // Skip if already existing

            if (!hash.containsValue(uid)) {
                hash.put(Integer.toString(i), uid);
                saveHash(hash, appContext.applicationContext());

                if (filevector != null) {
                    String[] res = filevector.substring(1, filevector.length() - 1).split(",");

                    SharedPreferences settings = appContext.applicationContext().
                            getSharedPreferences(FaceConstants.ALBUM_NAME, 0);
                    SharedPreferences.Editor editor = settings.edit();

                    if (settings.getString(FaceConstants.ALBUM_ARRAY, null) == null) {
                        albumBuffer = filevector;
                    } else {
                        Log.e(TAG, "parseSaveVector: " + albumBuffer.length());
//                    albumBuffer = settings.getString(FaceConstants.ALBUM_ARRAY, null).substring(0, albumBuffer.length() - 1) + "," + filevector + "]";
                    }
                    Log.e(TAG, "parseSaveVector: " + hash.size());
                    Log.e(TAG, "parseSaveVector: " + albumBuffer.length());
                    editor.putString("albumArray", albumBuffer);
                    editor.apply();


                }

            }
        }


    }


    public static void saveAndClose(android.content.Context context, String entityId, boolean updated, FacialProcessing objFace, int arrayPossition, Bitmap storedBitmap) {

        Log.e(TAG, "saveAndClose: updated "+ updated );

        byte[] faceVector = objFace.serializeRecogntionAlbum();

        int result = objFace.addPerson(arrayPossition);

        Log.e(TAG, "saveAndClose: "+ result );

        hash = retrieveHash(context);

        hash.put(entityId, Integer.toString(result));
//
        saveHash(hash, context);

        byte[] albumBuffer = SmartShutterActivity.faceProc.serializeRecogntionAlbum();

        String albumBufferArr = Arrays.toString(albumBuffer);

        String[] faceVectorContent = albumBufferArr.substring(1,  albumBufferArr.length() - 1).split(", ");

        faceVectorContent = Arrays.copyOfRange(faceVectorContent, 32, 332);

        SmartShutterActivity.faceProc.resetAlbum();

        WritePictureToFile(storedBitmap, entityId, faceVectorContent, updated);

        new ImageConfirmation().finish();

        Intent resultIntent = new Intent(appContext.applicationContext(), KIDetailActivity.class);
//        setResult(RESULT_OK, resultIntent);
        resultIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        appContext.applicationContext().startActivity(resultIntent);
        Log.e(TAG, "saveAndClose: "+"end" );
    }


}
