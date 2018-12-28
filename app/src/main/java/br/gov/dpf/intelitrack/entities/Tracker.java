package br.gov.dpf.intelitrack.entities;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.firebase.firestore.GeoPoint;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.gov.dpf.intelitrack.components.GeoPointParcelable;
import br.gov.dpf.intelitrack.trackers.TK102Activity;
import br.gov.dpf.intelitrack.trackers.TK103Activity;

public class Tracker implements Parcelable {

    private String mID;

    private String mName;

    private String mDescription;

    private String mPassword;

    private String mIMEI;

    private String mPhoneNumber;

    private String mFeedID;

    private String mModel;

    private String mBatteryLevel;

    private String mSignalLevel;

    private String mNetwork;

    private String mBackgroundColor;

    private List<String> mUsers;

    private List<String> mAdmins;

    private Date mLastUpdate;

    private Map<String, Object> mLastCoordinate;

    private Map<String, Object> mLastConfiguration;

    //Required by FireStore DB
    public Tracker()
    {
        //Initialize tracker permission lists
        mUsers = new ArrayList<>();
        mAdmins = new ArrayList<>();
    }

    public String getID() {
        return mID;
    }

    public void setID(String mID) {
        this.mID = mID;
    }

    public String getName() {
        return mName;
    }

    public void setName(String mName) {
        this.mName = mName;
    }

    public String getDescription() {
        return mDescription;
    }

    public void setDescription(String mDescription) {
        this.mDescription = mDescription;
    }

    public String getPassword() {
        return mPassword;
    }

    public void setPassword(String mPassword) {
        this.mPassword = mPassword;
    }

    public String getIMEI() {
        return mIMEI;
    }

    public void setIMEI(String mIMEI) {
        this.mIMEI = mIMEI;
    }

    public String getPhoneNumber() { return mPhoneNumber; }

    public void setPhoneNumber(String mPhoneNumber) {this.mPhoneNumber = mPhoneNumber; }

    public String getFeedID() { return mFeedID; }

    public void setFeedID(String mFeedID) { this.mFeedID = mFeedID; }

    public String getModel() {
        return mModel;
    }

    public void setModel(String mModel) {
        this.mModel = mModel;
    }

    public String getNetwork() {
        return mNetwork;
    }

    public void setNetwork(String mNetwork) {
        this.mNetwork = mNetwork;
    }

    public String getBackgroundColor() {
        return mBackgroundColor;
    }

    public void setBackgroundColor(String mBackgroundColor) { this.mBackgroundColor = mBackgroundColor;  }

    public List<String> getUsers() {
        return mUsers;
    }

    public void setUsers(List<String> mUsers) {
        this.mUsers = mUsers;
    }

    public List<String> getAdmins() {
        return mAdmins;
    }

    public void setAdmins(List<String> mAdmins) {
        this.mAdmins = mAdmins;
    }

    public String getBatteryLevel()
    {
        if(mBatteryLevel == null)
            return "N/D";
        else
            return mBatteryLevel;
    }

    public void setBatteryLevel(String mBatteryLevel) { this.mBatteryLevel = mBatteryLevel; }

    public String getSignalLevel()
    {
        if(mSignalLevel == null)
            return "N/D";
        else
            return mSignalLevel;
    }

    public void setSignalLevel(String mSignalLevel) { this.mSignalLevel = mSignalLevel; }

    public Date getLastUpdate() {
        return mLastUpdate;
    }

    public void setLastUpdate(Date mLastUpdate) {
        this.mLastUpdate = mLastUpdate;
    }

    public Map<String, Object> getLastCoordinate() { return mLastCoordinate; }

    public void setLastCoordinate(Map<String, Object> mLastCoordinate) { this.mLastCoordinate = mLastCoordinate; }

    public Map<String, Object> getLastConfiguration() { return mLastConfiguration; }

    public void setLastConfiguration(Map<String, Object> mLastConfiguration) { this.mLastConfiguration = mLastConfiguration; }

    protected Tracker(Parcel in)
    {
        //Initialize tracker permission lists
        mUsers = new ArrayList<>();
        mAdmins = new ArrayList<>();

        //Read from parcelable
        mID = in.readString();
        mName = in.readString();
        mDescription = in.readString();
        mPassword = in.readString();
        mIMEI = in.readString();
        mPhoneNumber = in.readString();
        mFeedID = in.readString();
        mModel = in.readString();
        mBatteryLevel = in.readString();
        mSignalLevel = in.readString();
        mNetwork = in.readString();
        mBackgroundColor = in.readString();
        in.readStringList(mUsers);
        in.readStringList(mAdmins);
        long tmpLastUpdate = in.readLong();
        mLastUpdate = tmpLastUpdate != -1 ? new Date(tmpLastUpdate) : null;
        long tmpLastCoordinate = in.readLong();

        //Check if last coordinate is available
        if(tmpLastCoordinate != -1)
        {
            //Get last coordinate data
            mLastCoordinate = new HashMap<>();
            mLastCoordinate.put("datetime", new Date(tmpLastCoordinate));
            mLastCoordinate.put("type", in.readString());
            mLastCoordinate.put("location", in.readParcelable(GeoPointParcelable.class.getClassLoader()));
        }
        else
        {
            //No coordinates available, read from parcel but ignore data
            in.readString();
            in.readParcelable(GeoPointParcelable.class.getClassLoader());
        }

        //Check if last configuration is available
        String tmpConfigurationStep = in.readString();

        //If configuration data available
        if(!tmpConfigurationStep.isEmpty())
        {
            mLastConfiguration = new HashMap<>();
            mLastConfiguration.put("step", tmpConfigurationStep);
            mLastConfiguration.put("pending", in.readInt());
            mLastConfiguration.put("description", in.readString());
            mLastConfiguration.put("status", in.readString());
            mLastConfiguration.put("progress", in.readString());
            mLastConfiguration.put("datetime", new Date(in.readLong()));
        }
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mID);
        dest.writeString(mName);
        dest.writeString(mDescription);
        dest.writeString(mPassword);
        dest.writeString(mIMEI);
        dest.writeString(mPhoneNumber);
        dest.writeString(mFeedID);
        dest.writeString(mModel);
        dest.writeString(mBatteryLevel);
        dest.writeString(mSignalLevel);
        dest.writeString(mNetwork);
        dest.writeString(mBackgroundColor);
        dest.writeStringList(mUsers);
        dest.writeStringList(mAdmins);
        dest.writeLong(mLastUpdate != null ? mLastUpdate.getTime() : -1L);
        dest.writeLong(mLastCoordinate != null ? ((Date) mLastCoordinate.get("datetime")).getTime() : -1L);
        dest.writeString(mLastCoordinate != null ? mLastCoordinate.get("type").toString() : "");
        dest.writeParcelable(mLastCoordinate != null ? new GeoPointParcelable((GeoPoint) mLastCoordinate.get("location")) : null, 0);
        dest.writeString(mLastConfiguration != null ? mLastConfiguration.get("step").toString() : "");
        dest.writeInt(mLastConfiguration != null ? Integer.valueOf(mLastConfiguration.get("pending").toString()) : 0);
        dest.writeString(mLastConfiguration != null ? mLastConfiguration.get("description").toString() : "");
        dest.writeString(mLastConfiguration != null ? mLastConfiguration.get("status").toString() : "");
        dest.writeString(mLastConfiguration != null ? mLastConfiguration.get("progress").toString() : "");
        dest.writeLong(mLastConfiguration != null ? ((Date) mLastConfiguration.get("datetime")).getTime() : -1L);
    }

    public static final Parcelable.Creator<Tracker> CREATOR = new Parcelable.Creator<Tracker>() {
        @Override
        public Tracker createFromParcel(Parcel in) {
            return new Tracker(in);
        }

        @Override
        public Tracker[] newArray(int size) {
            return new Tracker[size];
        }
    };

    public String formatTrackerModel()
    {
        switch (mModel)
        {
            case "tk102":
                return "TK 102";
            case "tk103":
                return "TK 102B";
            case "spot":
                return "SPOT Trace";
            case "st940":
                return "ST-940";
            case "pt39":
                return "PT-39";
            case "pt50x":
                return "PT-50x";
            default:
                return "Modelo desconhecido";
        }
    }

    public Class<?> loadActivity()
    {
        switch (mModel)
        {
            case "tk102":
                return TK102Activity.class;
            case "tk103":
                return TK103Activity.class;
            case "spot":
                return null;
            case "st940":
                return null;
            case "pt39":
                return null;
            case "pt50x":
                return null;
            default:
                return null;
        }
    }

    public String loadAPN()
    {
        switch(mNetwork)
        {
            case "VIVO":
                return "zap.vivo.com.br";
            case "TIM":
                return "tim.br";
            case "OI":
                return "gprs.oi.com.br";
            case "CLARO":
                return "claro.com.br";
            case "VODAFONE":
                return "m2m.vodafone.com.br";
            case "VIVO M2M":
                return "smart.m2m.vivo.com.br";
            default:
                return "tim.br";
        }
    }

    public String loadAPNUserPass()
    {
        switch(mNetwork)
        {
            case "VIVO":
                return "vivo";
            case "TIM":
                return "tim";
            case "OI":
                return "oi";
            case "CLARO":
                return "claro";
            case "VODAFONE":
                return "vodafone";
            case "VIVO M2M":
                return "vivo";
            default:
                return "tim";
        }
    }

    public void addUser(String userID)
    {
        //Add user to the permission list
        mUsers.add(userID);
    }
    public void addAdmin(String userID)
    {
        //Add user to the admin list
        mAdmins.add(userID);
    }
}
