package be.ugent.mydigipill.data

import android.content.Context
import be.ugent.mydigipill.viewmodels.OverviewViewModel
import com.google.android.gms.tasks.Task
import com.google.firebase.firestore.DocumentSnapshot

interface MedicationDAOInterface {

    fun addMedication(
        med: Medication,
        id: String = "",
        context: Context
    ): String

    fun getMedicationList(viewModel: OverviewViewModel)

    fun deleteMedicationByID(medID: String)

    fun deleteAlarmByID(alarmID: String, medID: String, afterDeleteAction: (() -> Unit)?)

    fun updateMedication(
        medication: Medication,
        context: Context
    )

    fun scheduleOnBoot(context: Context)

    fun setUserHasPhoto(b: Boolean)

    fun getUserHasPhoto(): Task<DocumentSnapshot>

    fun setAlarmStatistics(alarmString: String, medID: String, status: TakenStatus)

    fun getAlarmStatistics(alarmString: String, medID: String, ifNotTakenAction: () -> Unit)

}