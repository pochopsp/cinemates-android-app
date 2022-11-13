package it.unina.cinemates.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import it.unina.cinemates.models.Report;

public class ReportViewModel extends ViewModel {

    private final MutableLiveData<Report> reportMutableLiveData = new MutableLiveData<>(null);

    public LiveData<Report> getReportMutableLiveData() {
        return reportMutableLiveData;
    }

    public void setReportMutableLiveData(Report report){
        reportMutableLiveData.setValue(report);
    }
}
