package org.ei.opensrp.immunization.child;

import android.widget.TableLayout;
import android.widget.TableRow;

import org.ei.opensrp.Context;
import org.ei.opensrp.domain.Alert;
import org.ei.opensrp.repository.db.VaccineRepo;
import org.ei.opensrp.immunization.R;
import org.ei.opensrp.view.template.DetailFragment;
import org.joda.time.DateTime;
import org.joda.time.Months;
import org.joda.time.Years;

import java.util.List;
import java.util.Map;

import static org.ei.opensrp.util.VaccinatorUtils.addStatusTag;
import static org.ei.opensrp.util.VaccinatorUtils.addVaccineDetail;
import static org.ei.opensrp.util.VaccinatorUtils.generateSchedule;
import static org.ei.opensrp.util.Utils.convertDateFormat;
import static org.ei.opensrp.util.Utils.getDataRow;
import static org.ei.opensrp.util.Utils.getValue;
import static org.ei.opensrp.util.Utils.hasAnyEmptyValue;
import static org.ei.opensrp.util.Utils.nonEmptyValue;

public class ChildDetailFragment extends DetailFragment {
    @Override
    protected int layoutResId() {
        return R.layout.child_detail_activity;
    }

    @Override
    protected String pageTitle() {
        return "Child Details";
    }

    @Override
    protected String titleBarId() {
        return getEntityIdentifier();
    }

    @Override
    protected Integer profilePicContainerId() {
        return R.id.child_profilepic;
    }

    @Override
    protected Integer defaultProfilePicResId() {
        String gender = getValue(client, "gender", true);
        if(gender.equalsIgnoreCase("female")){
            return R.drawable.child_girl_infant;
        }
        else if(gender.toLowerCase().contains("trans")){
            return R.drawable.child_transgender_inflant;
        }

        return R.drawable.child_boy_infant;
    }

    @Override
    protected String bindType() {
        return "pkchild";
    }

    @Override
    protected boolean allowImageCapture() {
        return true;
    }

    public String getEntityIdentifier() {
        return nonEmptyValue(client.getColumnmaps(), true, false, "existing_program_client_id", "program_client_id");
    }

    @Override
    protected void generateView() {
        ((TableLayout) currentView.findViewById(R.id.child_detail_info_table2)).removeAllViews();
        ((TableLayout) currentView.findViewById(R.id.child_detail_info_table1)).removeAllViews();
        ((TableLayout) currentView.findViewById(R.id.child_vaccine_table1)).removeAllViews();
        ((TableLayout) currentView.findViewById(R.id.child_vaccine_table2)).removeAllViews();

        //BASIC INFORMATION
        TableLayout dt = (TableLayout) currentView.findViewById(R.id.child_detail_info_table1);

        //setting value in basic information textviews
        TableRow tr = getDataRow(getActivity(), "Program ID", getEntityIdentifier(), null);
        dt.addView(tr);

        tr = getDataRow(getActivity(), "EPI Card Number", getValue(client.getColumnmaps(), "epi_card_number", false), null);
        dt.addView(tr);

        tr = getDataRow(getActivity(), "Child's Name", getValue(client.getColumnmaps(), "first_name", true)+" "+getValue(client.getColumnmaps(), "last_name", true), null);
        dt.addView(tr);

        int months = -1;
        try{
            months = Months.monthsBetween(new DateTime(getValue(client.getColumnmaps(), "dob", false)), DateTime.now()).getMonths();
        }
        catch (Exception e){
            e.printStackTrace();
        }
        tr = getDataRow(getActivity(), "Birthdate (Age)", convertDateFormat(getValue(client.getColumnmaps(), "dob", false), "No DoB", true) + " (" + (months < 0? "":(months+"")) + " months" + ")", null);
        dt.addView(tr);

        tr = getDataRow(getActivity(), "Gender", getValue(client.getColumnmaps(), "gender", true), null);
        dt.addView(tr);

        tr = getDataRow(getActivity(), "Ethnicity", getValue(client, "ethnicity", true), null);
        dt.addView(tr);

        TableLayout dt2 = (TableLayout) currentView.findViewById(R.id.child_detail_info_table2);

        tr = getDataRow(getActivity(), "Mother's Name", getValue(client.getColumnmaps(), "mother_name", true), null);
        dt2.addView(tr);

        tr = getDataRow(getActivity(), "Father's Name", getValue(client.getColumnmaps(), "father_name", true), null);
        dt2.addView(tr);

        tr = getDataRow(getActivity(), "Contact Number", getValue(client.getColumnmaps(), "contact_phone_number", false), null);
        dt2.addView(tr);
        tr = getDataRow(getActivity(), "Address", getValue(client.getColumnmaps(), "address1", true)
                +", \nUC: "+ getValue(client.getColumnmaps(), "union_council", true)
                +", \nTown: "+ getValue(client.getColumnmaps(), "town", true)
                +", \nCity: "+ getValue(client, "city_village", true)
                +", \nProvince: "+ getValue(client, "province", true), null);
        dt2.addView(tr);

        String[] vl = new String[]{"bcg", "opv0", "penta1", "opv1","pcv1", "penta2", "opv2", "pcv2",
                "penta3", "opv3", "pcv3", "ipv", "measles1", "measles2"};

        //VACCINES INFORMATION
        TableLayout table = null;

        List<Alert> al = Context.getInstance().alertService().findByEntityIdAndAlertNames(client.entityId(),
                "BCG", "OPV 0", "Penta 1", "OPV 1", "PCV 1", "Penta 2", "OPV 2", "PCV 2",
                "Penta 3", "OPV 3", "PCV 3", "IPV", "Measles 1", "Measles2",
                "bcg", "opv0", "penta1", "opv1", "pcv1", "penta2", "opv2", "pcv2",
                "penta3", "opv3", "pcv3", "ipv", "measles1", "measles2");

        List<Map<String, Object>> sch = generateSchedule("child", months < 0 ? null:new DateTime(client.getColumnmaps().get("dob")), client.getColumnmaps(), al);
        int i = 0;
        for (Map<String, Object> m : sch){
            if (i <= 7) {
                table = (TableLayout) currentView.findViewById(R.id.child_vaccine_table1);
            } else {
                table = (TableLayout) currentView.findViewById(R.id.child_vaccine_table2);
            }
            addVaccineDetail(getActivity(), table, m.get("status").toString(), (VaccineRepo.Vaccine)m.get("vaccine"), (DateTime)m.get("date"), (Alert)m.get("alert"), true);
            i++;
        }

        int agey = -1;
        try{
            agey = Years.yearsBetween(new DateTime(getValue(client.getColumnmaps(), "dob", false)), DateTime.now()).getYears();
        }
        catch (Exception e){
            e.printStackTrace();
        }

        if(agey < 0){
            addStatusTag(getActivity(), table, "No DoB", true);
        }
        else if(!hasAnyEmptyValue(client.getColumnmaps(), "_retro", vl)){
            addStatusTag(getActivity(), table, "Fully Immunized", true);
        }
        else if(agey >= 5 && hasAnyEmptyValue(client.getColumnmaps(), "_retro", vl)){
            addStatusTag(getActivity(), table, "Partially Immunized", true);
        }
    }
}