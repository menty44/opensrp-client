package com.vijay.jsonwizard.widgets;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.rengwuxian.materialedittext.MaterialEditText;
import com.rey.material.util.ViewUtil;
import com.vijay.jsonwizard.R;
import com.vijay.jsonwizard.customviews.GenericTextWatcher;
import com.vijay.jsonwizard.customviews.TreeViewDialog;
import com.vijay.jsonwizard.fragments.JsonFormFragment;
import com.vijay.jsonwizard.interfaces.CommonListener;
import com.vijay.jsonwizard.interfaces.FormWidgetFactory;
import com.vijay.jsonwizard.interfaces.JsonApi;
import com.vijay.jsonwizard.validators.edittext.RequiredValidator;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import static android.view.inputmethod.InputMethodManager.HIDE_NOT_ALWAYS;

/**
 * @author Jason Rogena - jrogena@ona.io
 * @since 07/02/2017
 */
public class TreeViewFactory implements FormWidgetFactory {
    @Override
    public List<View> getViewsFromJson(String stepName, final Context context, JsonFormFragment formFragment, JSONObject
            jsonObject, CommonListener listener) throws Exception {
        List<View> views = new ArrayList<>(1);
        try {
            String openMrsEntityParent = jsonObject.getString("openmrs_entity_parent");
            String openMrsEntity = jsonObject.getString("openmrs_entity");
            String openMrsEntityId = jsonObject.getString("openmrs_entity_id");
            String relevance = jsonObject.optString("relevance");
            String constraints = jsonObject.optString("constraints");

            final MaterialEditText editText = (MaterialEditText) LayoutInflater.from(context).inflate(
                    R.layout.item_edit_text, null);
            editText.setHint(jsonObject.getString("hint"));
            editText.setFloatingLabelText(jsonObject.getString("hint"));
            editText.setId(ViewUtil.generateViewId());
            editText.setTag(R.id.key, jsonObject.getString("key"));
            editText.setTag(R.id.openmrs_entity_parent, openMrsEntityParent);
            editText.setTag(R.id.openmrs_entity, openMrsEntity);
            editText.setTag(R.id.openmrs_entity_id, openMrsEntityId);
            if (jsonObject.has("v_required")) {
                JSONObject requiredObject = jsonObject.optJSONObject("v_required");
                String requiredValue = requiredObject.getString("value");
                if (!TextUtils.isEmpty(requiredValue)) {
                    if (Boolean.TRUE.toString().equalsIgnoreCase(requiredValue)) {
                        editText.addValidator(new RequiredValidator(requiredObject.getString("err")));
                    }
                }
            }
            final String defaultValueString = jsonObject.optString("default");

            if (!TextUtils.isEmpty(jsonObject.optString("value"))) {
                editText.setText(jsonObject.optString("value"));
                if (jsonObject.has("read_only")) {
                    boolean readOnly = jsonObject.getBoolean("read_only");
                    editText.setEnabled(!readOnly);
                }
            }

            ArrayList<String> defaultValue = new ArrayList<>();
            try {
                JSONArray jsonArray = new JSONArray(defaultValueString);
                for (int i = 0; i < jsonArray.length(); i++) {
                    defaultValue.add(jsonArray.getString(i));
                }
            } catch (JSONException e){
            }

            final TreeViewDialog treeViewDialog = new TreeViewDialog(context,
                    jsonObject.getJSONArray("tree"), defaultValue);

            treeViewDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                @Override
                public void onShow(DialogInterface dialog) {
                    InputMethodManager inputManager = (InputMethodManager) context
                            .getSystemService(Context.INPUT_METHOD_SERVICE);
                    inputManager.hideSoftInputFromWindow(((Activity) context).getCurrentFocus().getWindowToken(),
                            HIDE_NOT_ALWAYS);
                }
            });

            treeViewDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    ArrayList<String> value = treeViewDialog.getValue();
                    if (value != null && value.size() > 0) {
                        JSONArray array = new JSONArray(value);
                        Log.d("TreeQ", array.toString());
                        editText.setText(array.toString());
                    }
                }
            });

            editText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showTreeDialog(editText, treeViewDialog);
                }
            });

            editText.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    editText.setText("");
                    return true;
                }
            });

            GenericTextWatcher genericTextWatcher = new GenericTextWatcher(stepName, formFragment, editText);
            genericTextWatcher.addOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    if (hasFocus) {
                        showTreeDialog(editText, treeViewDialog);
                    }
                }
            });

            editText.addTextChangedListener(genericTextWatcher);
            if (relevance != null && context instanceof JsonApi) {
                editText.setTag(R.id.relevance, relevance);
                ((JsonApi) context).addSkipLogicView(editText);
            }
            if (constraints != null && context instanceof JsonApi) {
                editText.setTag(R.id.constraints, constraints);
                editText.setTag(R.id.address, stepName + ":" + jsonObject.getString("key"));
                ((JsonApi) context).addConstrainedView(editText);
            }

            ((JsonApi) context).addFormDataView(editText);
            views.add(editText);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return views;
    }

    private static void showTreeDialog(MaterialEditText editText, TreeViewDialog treeViewDialog) {
        ArrayList<String> data = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(editText.getText().toString());
            for (int i = 0; i < jsonArray.length(); i++) {
                data.add(jsonArray.getString(i));
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        treeViewDialog.setValue(data);
        treeViewDialog.show();
    }
}