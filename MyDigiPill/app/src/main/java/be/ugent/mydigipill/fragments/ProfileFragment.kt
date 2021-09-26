package be.ugent.mydigipill.fragments

import android.Manifest
import android.app.Activity.RESULT_OK
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import be.ugent.mydigipill.LoginActivity
import be.ugent.mydigipill.R
import be.ugent.mydigipill.viewmodels.ProfileViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.fragment_profile.*

class ProfileFragment : Fragment() {

    private val viewModel: ProfileViewModel by activityViewModels()

    /**
     * here we create some observers for the viewmodel so we can display a correct toast
     */
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        val view: View = inflater.inflate(R.layout.fragment_profile, container, false)

        //create observers for viewmodel stuff
        viewModel.wrongPassword.observe(viewLifecycleOwner, Observer {
            if (it != null && it) {
                createToast(getString(R.string.password_wrong))
                viewModel.wrongPassword.postValue(null)
            }
        })
        viewModel.deleteSuccess.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                if (it) {
                    createToast(getString(R.string.delete_account_succes))
                    viewModel.deleteSuccess.postValue(null)
                    //navigeer naar inlog scherm
                    startActivity(Intent(context, LoginActivity::class.java))
                    requireActivity().finish()
                } else {
                    viewModel.deleteSuccess.postValue(null)
                    createToast(getString(R.string.delete_account_failed))
                }
            }
        })
        viewModel.signOutSuccess.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                if (it) {
                    createToast(getString(R.string.sign_out_succes))
                    viewModel.signOutSuccess.postValue(null)
                    //navigeer naar inlog scherm
                    startActivity(Intent(context, LoginActivity::class.java))
                    requireActivity().finish()
                } else {
                    viewModel.signOutSuccess.postValue(null)
                    createToast(getString(R.string.sign_out_failed))
                }
            }
        })
        viewModel.saveNameSuccess.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                if (it) {
                    createToast(getString(R.string.name_change_succes))

                    //update namefield
                    profile_username.setText(viewModel.currentName)

                    //zet de buttons terug goed
                    fixUsernameButtonsOnCancelOrSave()
                } else {
                    createToast(getString(R.string.name_change_failed))
                }
                viewModel.saveNameSuccess.postValue(null)
            }
        })
        viewModel.saveEmailSuccess.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                if (it) {
                    createToast(getString(R.string.email_change_success))

                    //update namefield
                    profile_emailAdress.setText(viewModel.currentEmail)

                    //zet de buttons terug goed
                    fixEmailButtonsOnCancelOrSave()
                } else {
                    createToast(getString(R.string.email_change_failed))
                }
                viewModel.startSaveEmail = false
                viewModel.saveEmailSuccess.postValue(null)
            }
        })
        viewModel.savePasswordSuccess.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                if (it) {
                    createToast(getString(R.string.password_change_succes))

                    //zet de buttons terug goed
                    fixPasswordButtonsOnCancelOrSave()
                } else {
                    createToast(getString(R.string.password_change_failed))
                }
                viewModel.savePasswordSuccess.postValue(null)
            }
        })
        viewModel.saveProfilePictureSuccess.observe(viewLifecycleOwner, Observer {
            if (it != null) {
                if (it) {
                    createToast(getString(R.string.profile_picture_change_succes))
                } else {
                    createToast(getString(R.string.profile_picture_change_failed))
                }
                viewModel.saveProfilePictureSuccess.postValue(null)
            }
        })

        return view
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        //zet de textfields juist
        profile_username.setText(viewModel.currentName)
        profile_emailAdress.setText(viewModel.currentEmail)
        profile_password.setText(viewModel.currentPassword)

        //observe huidige profielfoto
        viewModel.profilePicture.observe(viewLifecycleOwner, Observer {
            it?.let {
                Glide.with(requireActivity())
                    .load(it)
                    .apply(
                        RequestOptions()
                            .placeholder(R.drawable.ic_profile_icon)
                            .error(R.drawable.ic_profile_icon)
                    )
                    .into(profile_image)
            } ?: profile_image.setImageResource(R.drawable.ic_profile_icon)
        })

        //zet huidige profielfoto juist
        viewModel.downloadPicture()

        //zet data in viewmodel juist
        viewModel.name.postValue(viewModel.currentName)
        viewModel.password.postValue(viewModel.currentPassword)
        viewModel.email.postValue(viewModel.currentEmail)

        //laat de gebruiker een foto kiezen uit de gallerij
        OpenGalleryButton.setOnClickListener {
            openFileChooser()
        }

        //laat de gebruiker een foto maken indien permissie voor de camera
        TakePhotoButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.CAMERA),
                    ProfileViewModel.REQUEST_CAMERA_ACCESS
                )
            } else {
                openCamera()
            }
        }

        //add listeners for edit username, email & password
        setUsernameListeners()
        setEmailListeners()
        setPasswordListeners()

        //add listeners for sign out & delete account
        setSignOutListener()
        setDeleteAccountListener()
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
        if (requestCode == ProfileViewModel.REQUEST_CAMERA_ACCESS) {
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
     * add listener for sign out
     */
    private fun setSignOutListener() {
        //add listener to sign out button
        SignOutButton.setOnClickListener {
            //log uit
            viewModel.startSignOut = true
            findNavController().navigate(R.id.action_ProfileFragment_to_confirmSignOut)
        }
    }

    /**
     *  add listener for delete account
     */
    private fun setDeleteAccountListener() {
        //add listener to delete account button
        DeleteAccountButton.setOnClickListener {
            //delete account
            viewModel.startDelete = true
            findNavController().navigate(R.id.action_ProfileFragment_to_confirmDelete)
        }
    }

    /**
     * add listener for edit username
     */
    private fun setUsernameListeners() {
        //add listener to name field
        profile_username.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.name.postValue(s.toString().replace("\n", ""))
            }
        })

        //add listener to edit name button
        EditUsernameButton.setOnClickListener {
            //zet de editView enabled
            if (!(profile_emailAdress.isEnabled || profile_password.isEnabled)) {
                //disable edit
                EditUsernameButton.visibility = View.INVISIBLE

                //enable save en cancel
                cancel_username.visibility = View.VISIBLE
                save_username.visibility = View.VISIBLE

                //enable de editText
                profile_username.isEnabled = true
                profile_username.setBackgroundResource(R.drawable.textfield_placeholder)
            } else {
                createToast(getString(R.string.email_pw_enabled))
            }
        }

        //add username save
        save_username.setOnClickListener {
            //save de nieuwe username
            viewModel.saveName()
        }

        cancel_username.setOnClickListener {
            //zet de oude data er terug in
            viewModel.name.postValue(viewModel.currentName)

            //zet de buttons terug goed
            fixUsernameButtonsOnCancelOrSave()
        }
    }

    /**
     * add listener for edit password
     */
    private fun setPasswordListeners() {
        //add listener to password field
        profile_password.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.password.postValue(s.toString().replace("\n", ""))
            }
        })

        //add listener to edit password button
        EditPasswordButton.setOnClickListener {
            if (!(profile_username.isEnabled || profile_emailAdress.isEnabled)) {
                //enable de editText
                profile_password.isEnabled = true
                profile_password.setBackgroundResource(R.drawable.textfield_placeholder)

                //disable edit
                EditPasswordButton.visibility = View.INVISIBLE

                //enable save en cancel
                cancel_password.visibility = View.VISIBLE
                save_password.visibility = View.VISIBLE
            } else {
                createToast(getString(R.string.user_pw_enabled))
            }
        }

        //add username save
        save_password.setOnClickListener {
            //save het nieuwe password
            findNavController().navigate(R.id.action_ProfileFragment_to_confirmPassword)
        }

        cancel_password.setOnClickListener {
            //zet de oude data er terug in
            profile_password.setText(viewModel.currentPassword)
            viewModel.password.postValue(viewModel.currentPassword)

            //zet de buttons terug goed
            fixPasswordButtonsOnCancelOrSave()
        }
    }

    /**
     * add listener for edit email
     */
    private fun setEmailListeners() {
        //add listener to email field
        profile_emailAdress.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                viewModel.email.postValue(s.toString().replace("\n", ""))
            }
        })

        //add listener to edit email button
        EditEmailAdress.setOnClickListener {
            if (!(profile_username.isEnabled || profile_password.isEnabled)) {
                //disable edit
                EditEmailAdress.visibility = View.INVISIBLE

                //enable save en cancel
                cancel_email.visibility = View.VISIBLE
                save_email.visibility = View.VISIBLE

                //enable de editText
                profile_emailAdress.isEnabled = true
                profile_emailAdress.setBackgroundResource(R.drawable.textfield_placeholder)
            } else {
                createToast(getString(R.string.user_pw_enabled))
            }
        }

        //add username save
        save_email.setOnClickListener {
            //save de nieuwe email
            viewModel.startSaveEmail = true
            findNavController().navigate(R.id.action_ProfileFragment_to_confirmPassword)
            //viewModel.saveEmail()
        }

        cancel_email.setOnClickListener {
            //zet de oude data er terug in
            viewModel.email.postValue(viewModel.currentEmail)

            //zet de buttons terug goed
            fixEmailButtonsOnCancelOrSave()
        }
    }

    /**
     * used after savebutton or cancelbutton clicked of the edit username
     * this will disable these buttons and enable the correct ones
     */
    private fun fixUsernameButtonsOnCancelOrSave() {
        //disable de editText
        profile_username.isEnabled = false
        profile_username.setBackgroundColor(Color.TRANSPARENT)

        //disable save en cancel
        cancel_username.visibility = View.INVISIBLE
        save_username.visibility = View.INVISIBLE

        //enable edit
        EditUsernameButton.visibility = View.VISIBLE
    }

    /**
     * used after savebutton or cancelbutton clicked of the edit password
     * this will disable these buttons and enable the correct ones
     */
    private fun fixPasswordButtonsOnCancelOrSave() {
        //set the textview to stars
        profile_password.setText(viewModel.currentPassword)

        //disable de editText
        profile_password.isEnabled = false
        profile_password.setBackgroundColor(Color.TRANSPARENT)

        //disable save en cancel
        cancel_password.visibility = View.INVISIBLE
        save_password.visibility = View.INVISIBLE

        //enable edit
        EditPasswordButton.visibility = View.VISIBLE
    }

    /**
     * used after savebutton or cancelbutton clicked of the edit email
     * this will disable these buttons and enable the correct ones
     */
    private fun fixEmailButtonsOnCancelOrSave() {
        //disable de editText
        profile_emailAdress.isEnabled = false
        profile_emailAdress.setBackgroundColor(Color.TRANSPARENT)

        //disable save en cancel
        cancel_email.visibility = View.INVISIBLE
        save_email.visibility = View.INVISIBLE

        //enable edit
        EditEmailAdress.visibility = View.VISIBLE
    }

    /**
     * Creates a toast with
     * @param message as the message
     */
    private fun createToast(message: String) {
        Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show()
    }

    /**
     * This creates and intent to open the gallery of the user
     * to let the user select a picture
     */
    private fun openFileChooser() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, ProfileViewModel.PICK_IMAGE_REQUEST)
    }

    /**
     * This creates and intent to let the user make a photo
     */
    private fun openCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "New picture")
        values.put(MediaStore.Images.Media.DESCRIPTION, "For profile")
        viewModel.takePhotoImageUri = requireActivity().contentResolver.insert(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            values
        )
        val intent = Intent()
        intent.action = MediaStore.ACTION_IMAGE_CAPTURE
        intent.putExtra(MediaStore.EXTRA_OUTPUT, viewModel.takePhotoImageUri)
        startActivityForResult(intent, ProfileViewModel.TAKE_PHOTO_REQUEST)
    }

    /**
     * handles the intents of @see openCamera() and @see openFileChooser()
     * update the viewmodel with the selected image
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == ProfileViewModel.PICK_IMAGE_REQUEST
            && resultCode == RESULT_OK
            && data != null
        ) {
            viewModel.updateProfilePicture(data.data)
        }
        if (requestCode == ProfileViewModel.TAKE_PHOTO_REQUEST
            && resultCode == RESULT_OK
        ) {
            viewModel.updateProfilePicture(viewModel.takePhotoImageUri)
            viewModel.takePhotoImageUri = null
        }

    }

    /**
     * const values
     */
    companion object {
        const val TAG: String = "PROFILEFRAGMENT"
    }

}
