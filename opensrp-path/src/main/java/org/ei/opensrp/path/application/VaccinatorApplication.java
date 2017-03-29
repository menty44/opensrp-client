package org.ei.opensrp.path.application;

import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.FileProvider;

import com.crashlytics.android.Crashlytics;

import org.ei.opensrp.Context;
import org.ei.opensrp.commonregistry.CommonFtsObject;
import org.ei.opensrp.path.activity.LoginActivity;
import org.ei.opensrp.path.receiver.ConfigSyncReceiver;
import org.ei.opensrp.path.receiver.PathSyncBroadcastReceiver;
import org.ei.opensrp.path.repository.PathRepository;
import org.ei.opensrp.path.repository.UniqueIdRepository;
import org.ei.opensrp.path.repository.VaccineRepository;
import org.ei.opensrp.path.repository.WeightRepository;
import org.ei.opensrp.repository.Repository;
import org.ei.opensrp.sync.DrishtiSyncScheduler;
import org.ei.opensrp.view.activity.DrishtiApplication;

import java.util.Locale;

import io.fabric.sdk.android.Fabric;

import static org.ei.opensrp.util.Log.logInfo;

/**
 * Created by koros on 2/3/16.
 */
public class VaccinatorApplication extends DrishtiApplication {
    private Locale locale = null;
    private Context context;
    private static CommonFtsObject commonFtsObject;
    private WeightRepository weightRepository;
    private UniqueIdRepository uniqueIdRepository;
    private VaccineRepository vaccineRepository;
    private boolean lastModified;

    @Override
    public void onCreate() {
        super.onCreate();

        mInstance = this;
        context = Context.getInstance();
        Fabric.with(this, new Crashlytics());
        DrishtiSyncScheduler.setReceiverClass(PathSyncBroadcastReceiver.class);

        context = Context.getInstance();
        context.updateApplicationContext(getApplicationContext());
        context.updateCommonFtsObject(createCommonFtsObject());

        applyUserLanguagePreference();
        cleanUpSyncState();
        ConfigSyncReceiver.scheduleFirstSync(getApplicationContext());
        setCrashlyticsUser(context);
    }

    public static synchronized VaccinatorApplication getInstance() {
        return (VaccinatorApplication) mInstance;
    }

    @Override
    public void logoutCurrentUser() {

        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        getApplicationContext().startActivity(intent);
        context.userService().logoutSession();
    }

    private void cleanUpSyncState() {
        DrishtiSyncScheduler.stop(getApplicationContext());
        context.allSharedPreferences().saveIsSyncInProgress(false);
    }


    @Override
    public void onTerminate() {
        logInfo("Application is terminating. Stopping Bidan Sync scheduler and resetting isSyncInProgress setting.");
        cleanUpSyncState();
        super.onTerminate();
    }

    private void applyUserLanguagePreference() {
        Configuration config = getBaseContext().getResources().getConfiguration();

        String lang = context.allSharedPreferences().fetchLanguagePreference();
        if (!"".equals(lang) && !config.locale.getLanguage().equals(lang)) {
            locale = new Locale(lang);
            updateConfiguration(config);
        }
    }

    private void updateConfiguration(Configuration config) {
        config.locale = locale;
        Locale.setDefault(locale);
        getBaseContext().getResources().updateConfiguration(config,
                getBaseContext().getResources().getDisplayMetrics());
    }

    private static String[] getFtsSearchFields(String tableName) {
        if (tableName.equals("ec_child")) {
            String[] ftsSearchFileds = {"zeir_id", "epi_card_number", "first_name", "last_name"};
            return ftsSearchFileds;
        } else if (tableName.equals("ec_mother")) {
            String[] ftsSearchFileds = {"zeir_id", "epi_card_number", "first_name", "last_name", "father_name", "husband_name", "contact_phone_number"};
            return ftsSearchFileds;
        }
        return null;
    }


    private static String[] getFtsSortFields(String tableName){
        if(tableName.equals("ec_child") || tableName.equals("ec_mother")) {
            String[] sortFields = {"first_name", "dob", "zeir_id", "last_interacted_with"};
            return sortFields;
        }
        return null;
    }

    private static String[] getFtsTables() {
        String[] ftsTables = {"ec_child", "ec_mother"};
        return ftsTables;
    }

    public static CommonFtsObject createCommonFtsObject() {
        if (commonFtsObject == null) {
            commonFtsObject = new CommonFtsObject(getFtsTables());
            for (String ftsTable : commonFtsObject.getTables()) {
                commonFtsObject.updateSearchFields(ftsTable, getFtsSearchFields(ftsTable));
                commonFtsObject.updateSortFields(ftsTable, getFtsSortFields(ftsTable));
            }
        }
        return commonFtsObject;
    }

    /**
     * This method sets the Crashlytics user to whichever username was used to log in last
     *
     * @param context The user's context
     */
    public static void setCrashlyticsUser(Context context) {
        if (context != null && context.userService() != null
                && context.userService().getAllSharedPreferences() != null) {
            Crashlytics.setUserName(context.userService().getAllSharedPreferences().fetchRegisteredANM());
        }
    }

    private void grantPhotoDirectoryAccess() {
        Uri uri = FileProvider.getUriForFile(this,
                "com.vijay.jsonwizard.fileprovider",
                getExternalFilesDir(Environment.DIRECTORY_PICTURES));
        grantUriPermission("com.vijay.jsonwizard", uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
    }

    @Override
    public Repository getRepository() {
        if (repository == null) {
            repository = new PathRepository(getInstance().getApplicationContext());
            weightRepository();
            vaccineRepository();
            uniqueIdRepository();
        }
        return repository;
    }


    public WeightRepository weightRepository() {
        if (weightRepository == null) {
            weightRepository = new WeightRepository((PathRepository) getRepository());
        }
        return weightRepository;
    }

    public VaccineRepository vaccineRepository() {
        if (vaccineRepository == null) {
            vaccineRepository = new VaccineRepository((PathRepository) getRepository());
        }
        return vaccineRepository;
    }

    public UniqueIdRepository uniqueIdRepository() {
        if (uniqueIdRepository == null) {
            uniqueIdRepository = new UniqueIdRepository((PathRepository) getRepository());
        }
        return uniqueIdRepository;
    }

    public boolean isLastModified() {
        return lastModified;
    }

    public void setLastModified(boolean lastModified) {
        this.lastModified = lastModified;
    }
}