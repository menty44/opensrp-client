package org.ei.drishti.view.activity;

import android.database.DataSetObserver;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.*;
import org.ei.drishti.R;
import org.ei.drishti.adapter.WrappedSmartRegisterPaginatedAdapter;
import org.ei.drishti.provider.WrappedSmartRegisterClientsProvider;

public abstract class SecuredNativeSmartRegisterActivity extends SecuredActivity implements View.OnClickListener {

    public static final String SORT_BY_NAME = "Name";
    public static final String SORT_BY_AGE = "Age";
    public static final String SORT_BY_EC_NO = "EC No";
    public static final String FILTER_BY_ALL = "All";
    public static final String FILTER_BY_OA = "O/A";
    public static final String FILTER_BY_LP = "L/P";

    public static final String[] DEFAULT_SORT_OPTIONS = {SORT_BY_NAME, SORT_BY_AGE, SORT_BY_EC_NO};
    public static final String[] DEFAULT_FILTER_OPTIONS = {FILTER_BY_ALL, FILTER_BY_OA, FILTER_BY_LP};

    private ListView clientsView;
    private WrappedSmartRegisterPaginatedAdapter clientsAdapter;

    private View villageFilterView;
    private View sortView;
    private Button serviceModeView;
    private LinearLayout clientsHeaderLayout;
    private ListPopupWindow villageFilterOptionsView = null;
    private ListPopupWindow sortOptionsView = null;
    private ListPopupWindow serviceModeOptionsView = null;

    private TextView appliedVillageFilterView;
    private TextView appliedSortView;

    @Override
    protected void onCreation() {
        setContentView(R.layout.smart_register_activity);

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        setupViews();
    }

    @Override
    protected void onResumption() {
    }

    private void setupViews() {
        findViewById(R.id.btn_back_to_home).setOnClickListener(this);

        TextView title = (TextView) findViewById(R.id.btn_title);
        title.setOnClickListener(this);
        title.setText(getRegisterTitle());

        clientsHeaderLayout = (LinearLayout) findViewById(R.id.clients_header_layout);
        layoutHeaderView();

        clientsView = (ListView) findViewById(R.id.list);

        setupAdapter();

        EditText searchCriteria = (EditText) findViewById(R.id.edt_search);
        searchCriteria.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
            }

            @Override
            public void onTextChanged(CharSequence cs, int start, int before, int count) {
                clientsAdapter.getFilter().filter(cs);
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });

        clientsAdapter.updateFooter();

        villageFilterView = findViewById(R.id.filter_selection);
        villageFilterView.setOnClickListener(this);
        sortView = findViewById(R.id.sort_selection);
        sortView.setOnClickListener(this);
        serviceModeView = (Button) findViewById(R.id.section_type_selection);
        serviceModeView.setOnClickListener(this);
        serviceModeView.setText(getDefaultTypeName());

        appliedSortView = (TextView) findViewById(R.id.sorted_by);
        appliedVillageFilterView = (TextView) findViewById(R.id.village);

        updateStatusBar();

    }

    private void updateStatusBar() {
        appliedSortView.setText(getDefaultSortOption());
        appliedVillageFilterView.setText(getDefaultVillageFilterOption());
    }

    private void layoutHeaderView() {
        LinearLayout listHeader = clientsHeaderLayout;
        listHeader.removeAllViewsInLayout();

        listHeader.setWeightSum(getColumnWeightSum());
        int columnCount = getColumnCount();
        int[] weights = getColumnWeights();
        int[] headerTxtResIds = getColumnHeaderTextResourceIds();

        for (int i = 0; i < columnCount; i++) {
            listHeader.addView(getColumnHeaderView(i, weights, headerTxtResIds));
            listHeader.addView(getSeparatorView());
        }
    }

    private View getSeparatorView() {
        ImageView iv = new ImageView(this);
        iv.setLayoutParams(getDividerLayoutParams());
        iv.setImageResource(R.color.list_divider_color);
        return iv;
    }

    private LinearLayout.LayoutParams getDividerLayoutParams() {
        return new LinearLayout.LayoutParams(
                (int) getResources().getDimension(R.dimen.list_item_divider_height),
                ViewGroup.LayoutParams.MATCH_PARENT);

    }

    private View getColumnHeaderView(int i, int[] weights, int[] headerTxtResIds) {
        TextView tv = new TextView(this, null, R.style.TextAppearance_Header);
        LinearLayout.LayoutParams lp =
                new LinearLayout.LayoutParams(
                        0,
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        weights[i]);

        tv.setLayoutParams(lp);
        tv.setText(headerTxtResIds[i]);
        tv.setPadding(10, 0, 0, 0);

        return tv;
    }

    private void setupAdapter() {
        clientsAdapter = adapter();
        clientsView.setAdapter(clientsAdapter);
        clientsAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                clientsAdapter.updateFooter();
            }
        });
    }

    protected WrappedSmartRegisterPaginatedAdapter adapter() {
        return new WrappedSmartRegisterPaginatedAdapter(this, listItemProvider());
    }

    protected abstract WrappedSmartRegisterClientsProvider listItemProvider();

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_title:
            case R.id.btn_back_to_home:
                goBack();
                break;
            case R.id.filter_selection:
                showFilterSelectionView(villageFilterView);
                break;
            case R.id.sort_selection:
                showSortSelectionView(sortView);
                break;
            case R.id.section_type_selection:
                showTypeSelectionView(serviceModeView);
                break;
        }
    }

    private void showTypeSelectionView(View typeSelectionView) {
        typeSelectionPopupWindow(typeSelectionView).show();
    }

    protected void onTypeSelection(String type) {
        clientsAdapter.showSection(type);
        clientsAdapter.notifyDataSetChanged();
        serviceModeView.setText(type);
    }

    private void showSortSelectionView(View sortSelectionView) {
        sortSelectionPopupWindow(sortSelectionView).show();
    }

    protected void onSortSelection(String sortBy) {
        clientsAdapter.sortBy(sortBy);
        clientsAdapter.notifyDataSetChanged();
        appliedSortView.setText(sortBy);
    }

    private void showFilterSelectionView(View filterSelectionView) {
        filterSelectionPopupWindow(filterSelectionView).show();
    }

    protected void onFilterSelection(String filter) {
        clientsAdapter.getFilter().filter(filter);
        clientsAdapter.notifyDataSetChanged();
        appliedVillageFilterView.setText(filter);
    }

    private ListPopupWindow filterSelectionPopupWindow(View anchorView) {
        if (villageFilterOptionsView == null) {
            villageFilterOptionsView = createFilterSelectionPopupWindow(anchorView);
        }
        return villageFilterOptionsView;
    }

    private ListPopupWindow sortSelectionPopupWindow(View anchorView) {
        if (sortOptionsView == null) {
            sortOptionsView = createSortSelectionPopupWindow(anchorView);
        }
        return sortOptionsView;
    }

    private ListPopupWindow typeSelectionPopupWindow(View anchorView) {
        if (serviceModeOptionsView == null) {
            serviceModeOptionsView = createTypeSelectionPopupWindow(anchorView);
        }
        return serviceModeOptionsView;
    }

    private ListPopupWindow createFilterSelectionPopupWindow(View anchorView) {
        final ListPopupWindow pw = new ListPopupWindow(this);
        pw.setAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_checked, getFilterOptions()));
        pw.setAnchorView(anchorView);
        pw.setContentWidth(250);
        pw.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                pw.dismiss();
                onFilterSelection(((TextView) view).getText().toString());
            }
        });
        return pw;
    }

    private ListPopupWindow createSortSelectionPopupWindow(View anchorView) {
        final ListPopupWindow pw = new ListPopupWindow(this);
        pw.setAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_checked, getSortingOptions()));
        pw.setAnchorView(anchorView);
        pw.setContentWidth(150);
        pw.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                pw.dismiss();
                onSortSelection(((TextView) view).getText().toString());

            }
        });
        return pw;
    }

    private ListPopupWindow createTypeSelectionPopupWindow(View anchorView) {
        final ListPopupWindow pw = new ListPopupWindow(this);
        pw.setAdapter(new ArrayAdapter(this, android.R.layout.simple_list_item_checked, getTypeOptions()));
        pw.setAnchorView(serviceModeView);
        pw.setContentWidth(150);

        pw.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                pw.dismiss();
                onTypeSelection(((TextView) view).getText().toString());
                layoutHeaderView();
            }
        });

        return pw;
    }

    private void goBack() {
        finish();
    }

    @Override
    public void onBackPressed() {
        if (isAnyPopupIsShowing()) {
            closeAllPopups();
        } else {
            super.onBackPressed();
        }
    }

    private void closeAllPopups() {
        closePopup(villageFilterOptionsView);
        closePopup(sortOptionsView);
        closePopup(serviceModeOptionsView);
    }

    private boolean isAnyPopupIsShowing() {
        return isPopupShowing(villageFilterOptionsView)
                || isPopupShowing(sortOptionsView)
                || isPopupShowing(serviceModeOptionsView);
    }

    private boolean isPopupShowing(ListPopupWindow popup) {
        return (popup != null && popup.isShowing());
    }

    private void closePopup(ListPopupWindow popup) {
        if (popup != null && popup.isShowing()) {
            popup.dismiss();
        }
    }

    protected abstract String getDefaultTypeName();

    protected abstract String getDefaultVillageFilterOption();

    protected abstract String getDefaultSortOption();

    public abstract int getColumnCount();

    public abstract int getColumnWeightSum();

    public abstract int[] getColumnWeights();

    protected abstract int[] getColumnHeaderTextResourceIds();

    protected abstract String getRegisterTitle();

    protected abstract String[] getFilterOptions();

    protected abstract String[] getTypeOptions();

    protected abstract String[] getSortingOptions();


}
