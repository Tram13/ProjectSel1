package be.ugent.mydigipill.fragments

import android.Manifest
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import be.ugent.mydigipill.MainActivity
import be.ugent.mydigipill.R
import be.ugent.mydigipill.data.Alarm
import be.ugent.mydigipill.recyclerAdapters.AddRecyclerAdapter
import be.ugent.mydigipill.viewmodels.AddViewModel
import be.ugent.mydigipill.viewmodels.OverviewViewModel
import be.ugent.mydigipill.viewmodels.TextFieldPopupViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.fragment_add.*

/**
 * This abstract class is a common super class for the
 * @see EditFragment
 * And the
 * @see AddFragment
 * We made this because those 2 fragments had a lot in common and for the most part do the same thing.
 * @property overviewViewModel is needed by both Edit and Add fragment so therefor it resides here.
 * @property viewModel speaks for itself I suppose
 * @property mainActivity is used quite often.
 * Just so we don't need to cast is every time we need the main activity we did it once and store it.
 * @property map is used to map status codes to functions so we can easily invoke them when needed.
 * @author Arthur Deruytter
 */
abstract class AbstractEditAddFragment : Fragment() {

    val viewModel: AddViewModel by activityViewModels()
    val overviewViewModel: OverviewViewModel by activityViewModels()
    val textFieldPopupViewModel: TextFieldPopupViewModel by activityViewModels()

    lateinit var mainActivity: MainActivity

    lateinit var adapter: AddRecyclerAdapter
    val map = hashMapOf(
        -1 to { savingFailed(getString(R.string.save_medication_unknown_status_code)) },
        0 to { savedSuccess() },
        1 to { savingFailed(getString(R.string.save_medication_no_name)) },
        2 to { savingFailed(getString(R.string.save_medication_no_alarm)) }
    )

    /**
     * In this onCreateView we make the view, we instantiate the
     * @see AddRecyclerAdapter for later use, we add observers to the liveData of the viewModel
     * and we add the listeners to the buttons.
     */
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view: View = inflater.inflate(R.layout.fragment_add, container, false)

        mainActivity = (requireActivity() as MainActivity)

        adapter = AddRecyclerAdapter {
            clickedOnAlarm(it)
        }

        //observe alarms to update recyclerview when needed
        viewModel.alarms.observe(viewLifecycleOwner, Observer { alarms ->
            adapter.submitList(alarms)
        })

        /**
         * if the image of the viewmodel change
         * update it in the view, if image is null or loading failed
         * this will set a placeholder
         */
        viewModel.image.observe(viewLifecycleOwner, Observer {
            it?.let {
                Glide.with(context)
                    .load(it)
                    .apply(
                        RequestOptions()
                            .placeholder(R.drawable.ic_notification_dark)
                            .error(R.drawable.ic_notification_dark)
                    )
                    .into(view.findViewById(R.id.placeholder))
            }
                ?: view.findViewById<ImageView>(R.id.placeholder)
                    .setImageResource(R.drawable.ic_notification_dark)
        })

        view.findViewById<Button>(R.id.addButton).setOnClickListener {
            // navigate
            AlarmFragment()
                .showNow(parentFragmentManager, "This is the Week and hour picker for an alarm")
        }

        //when user clicks on open gallery button
        view.findViewById<Button>(R.id.OpenGalleryButtonMed).setOnClickListener {
            openFileChooser()
        }

        /**
         * when user clicks on open take photo button
         * this will check if the user has permission, if not it will ask for permission
         * else if will open the camera
         */
        view.findViewById<Button>(R.id.TakePhotoButtonMed).setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.CAMERA),
                    AddViewModel.REQUEST_CAMERA_ACCESS
                )
            } else {
                openCamera()
            }
        }

        viewModel.image.value?.let {
            view.findViewById<ImageView>(R.id.placeholder).setImageURI(it)
        }

        return view
    }

    /**
     * If your app does not have the requested permissions the user will be presented
     * with UI for accepting them. After the user has accepted or rejected the
     * requested permissions you will receive a callback on this function
     * reporting whether the permissions were granted or not.
     */
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == AddViewModel.REQUEST_CAMERA_ACCESS) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera()
            } else {
                Toast.makeText(
                    requireActivity(),
                    getString(R.string.no_perm_camera),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    /**
     * This creates and intent to open the gallery of the user
     * to let the user select a picture
     */
    private fun openFileChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, AddViewModel.PICK_IMAGE_REQUEST)
    }

    /**
     * This creates and intent to let the user make a photo
     */
    private fun openCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "For medication")
        viewModel.takePhotoImageUri = requireActivity().contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            values
        )
        val intent = Intent()
        intent.action = MediaStore.ACTION_IMAGE_CAPTURE
        intent.putExtra(MediaStore.EXTRA_OUTPUT, viewModel.takePhotoImageUri)
        startActivityForResult(intent, AddViewModel.TAKE_PHOTO_REQUEST)
    }

    /**
     * handles the intents of @see openCamera() and @see openFileChooser()
     * update the viewmodel with the selected image
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            if (requestCode == AddViewModel.PICK_IMAGE_REQUEST
                && resultCode == Activity.RESULT_OK
                && data != null
            ) {
                viewModel.updateMedicationImage(data.data)
                view?.findViewById<ImageView>(R.id.placeholder)?.setImageURI(data.data)
            }
            if (requestCode == AddViewModel.TAKE_PHOTO_REQUEST
                && resultCode == Activity.RESULT_OK
            ) {
                viewModel.updateMedicationImage(viewModel.takePhotoImageUri)
                view?.findViewById<ImageView>(R.id.placeholder)
                    ?.setImageURI(viewModel.takePhotoImageUri)
                viewModel.takePhotoImageUri = null
            }
        } catch (ex: Exception) {
            Toast.makeText(context, getString(R.string.image_medication_failed), Toast.LENGTH_SHORT)
                .show()
        }

    }

    /**
     * In this onActivityCreated we set the adapter of the RecyclerView.
     * We also add all of the
     * @see TextWatcher objects to the EditText fields so that we can save their values live in the viewModel.
     */
    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        alarmRecycler.adapter = adapter

        editText_name.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.name.postValue(s.toString())
            }
        })
        editText_description.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.note.postValue(s.toString())
            }
        })
        editText_ingestion.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.intake.postValue(s.toString())
            }
        })
    }

    /**
     * This function is used to add an error string to the name EditText with what went wrong.
     */
    private fun savingFailed(reason: String) {
        editText_name.error = reason
    }

    /**
     * This function is used to handle everything that needs to happen after saving a medication.
     */
    open fun savedSuccess() {
        //navigate to overview
        viewModel.makeEmpty()
    }

    /**
     * when clicked on a alarm in the add or edit fragment
     */
    private fun clickedOnAlarm(alarm: Alarm) {
        //empty alarms don't need an onclick
    }

    /**
     * const values
     */
    companion object {
        const val TAG = "AbstractEditAddFragment"
    }

}
