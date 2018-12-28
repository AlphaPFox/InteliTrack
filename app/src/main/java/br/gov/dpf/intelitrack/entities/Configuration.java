package br.gov.dpf.intelitrack.entities;

import com.google.firebase.firestore.FieldValue;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Configuration
{
    private String mName;

    private String mDescription;

    private String mValue;

    private Map<String, Object> mStatus;

    private boolean mEnabled;

    public Configuration()
    {
    }


    public Configuration(String name, String description, String value, boolean enabled, String finishedStatus)
    {
        mName = name;
        mValue = value;
        mEnabled = enabled;
        mDescription = description;

        if(finishedStatus != null)
        {
            mStatus = new HashMap<>();
            mStatus.put("step", "SUCCESS");
            mStatus.put("description", finishedStatus);
            mStatus.put("datetime", new Date());
            mStatus.put("finished", true);
        }
        else
        {
            mStatus = new HashMap<>();
            mStatus.put("step", "REQUESTED");
            mStatus.put("description", "Aguardando envio para o rastreador");
            mStatus.put("datetime", new Date());
            mStatus.put("finished", false);
        }
    }

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public String getValue() {
        return mValue;
    }

    public void setValue(String mValue) {
        this.mValue = mValue;
    }

    public Map<String, Object> getStatus() {
        return mStatus;
    }

    public void setStatus(Map<String, Object> mStatus) {
        this.mStatus = mStatus;
    }

    public boolean isEnabled() {
        return mEnabled;
    }

    public void setEnabled(boolean mEnabled) {
        this.mEnabled = mEnabled;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String mDescription) {
        this.mDescription = mDescription;
    }
}
