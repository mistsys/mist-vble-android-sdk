package com.mist.sample.indoor_location.model;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by anubhava on 26/03/18.
 */

public class OrgData implements Parcelable{

    private String orgId;
    private String sdkSecret;
    private String orgName;
    private String envType;

    protected OrgData(Parcel in) {
        orgId = in.readString();
        sdkSecret = in.readString();
        orgName = in.readString();
        envType = in.readString();
    }

    public static final Creator<OrgData> CREATOR = new Creator<OrgData>() {
        @Override
        public OrgData createFromParcel(Parcel in) {
            return new OrgData(in);
        }

        @Override
        public OrgData[] newArray(int size) {
            return new OrgData[size];
        }
    };

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.orgId);
        dest.writeString(this.sdkSecret);
        dest.writeString(this.orgName);
        dest.writeString(this.envType);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public OrgData(String orgName, String orgId, String sdkSecret, String envType) {
        this.orgId = orgId;
        this.sdkSecret = sdkSecret;
        this.orgName = orgName;
        this.envType = envType;
    }

    public String getOrgId() {
        return orgId;
    }

    public String getSdkSecret() {
        return sdkSecret;
    }

    public String getOrgName() {
        return orgName;
    }

    public String getEnvType() {
        return envType;
    }
}
