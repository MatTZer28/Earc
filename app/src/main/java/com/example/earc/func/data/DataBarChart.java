package com.example.earc.func.data;

import java.util.List;

public class DataBarChart {
    private int mWeekDay;

    private List<DataModel> mDataModelList;

    public DataBarChart(int weekDay, List<DataModel> dataModels) {
        this.mWeekDay = weekDay;
        this.mDataModelList = dataModels;
    }

    public int getValueX() {
        return mWeekDay;
    }

    public int getValueY() {
        if (mDataModelList.size() == 0) return 0;

        int summarize = 0;

        for (int i = 0; i < mDataModelList.size(); i++) {
            summarize += mDataModelList.get(i).getDecibel();
        }

        int average = summarize / mDataModelList.size();

        return average;
    }
}
