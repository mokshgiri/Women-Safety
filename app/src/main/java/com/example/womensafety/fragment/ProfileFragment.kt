package com.example.womensafety.fragment

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.womensafety.R
import com.example.womensafety.activity.BaseActivity
import com.example.womensafety.firebase.RealtimeDatabaseClass
import com.example.womensafety.model.Users
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import de.hdodenhof.circleimageview.CircleImageView

class ProfileFragment : Fragment() {

    private lateinit var etName : AppCompatEditText
    private lateinit var etPhone : AppCompatEditText
    private lateinit var etEmail : AppCompatEditText
    private lateinit var etAddress : AppCompatEditText
    private lateinit var sGender : Spinner
    private lateinit var editName : ImageView
    private lateinit var userImage : CircleImageView
    private lateinit var iv_camera : CircleImageView
    private lateinit var editGender : CircleImageView
    private lateinit var editAddress : CircleImageView
//    private lateinit var editEmail : CircleImageView
    private lateinit var editPhone : CircleImageView
//    private lateinit var btnUpdate : Button
    private lateinit var dialog : Dialog
    private lateinit var imageDialog: Dialog
    private var selectedImg : Uri?= null

    private lateinit var databaseReference: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var storageReference: StorageReference

    private lateinit var currentUserUid: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fetchUserData()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        etName = view.findViewById(R.id.name)
        etPhone = view.findViewById(R.id.phone)
        etEmail = view.findViewById(R.id.email)
        etAddress = view.findViewById(R.id.address)
        userImage = view.findViewById(R.id.userImageView)
        iv_camera = view.findViewById(R.id.iv_camera)
//        sGender = view.findViewById(R.id.gender)
        editName = view.findViewById(R.id.editName)
//        editGender = view.findViewById(R.id.editGender)
        editAddress = view.findViewById(R.id.editAddress)
//        editEmail = view.findViewById(R.id.editEmail)
        editPhone = view.findViewById(R.id.editPhone)
//        btnUpdate = view.findViewById(R.id.btn_update)


        databaseReference = FirebaseDatabase.getInstance().getReference("users")
        firebaseAuth = FirebaseAuth.getInstance()
        storage = FirebaseStorage.getInstance()
        storageReference = storage.reference

        // Get the current user's UID
        currentUserUid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()


        etName.setOnTouchListener { _, _ -> true }
        etAddress.setOnTouchListener { _, _ -> true }
        etEmail.setOnTouchListener { _, _ -> true }
        etPhone.setOnTouchListener { _, _ -> true }

        editName.setOnClickListener {
            showEditDetailsDialog(etName, "name")
        }
        editAddress.setOnClickListener {
            showEditDetailsDialog(etAddress, "address")
        }
        editPhone.setOnClickListener {
            showEditDetailsDialog(etPhone, "phone")
        }
//        editEmail.setOnClickListener {
//            showEditDetailsDialog(etEmail, "email")
//        }

        iv_camera.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(intent, 1)
        }

//        btnUpdate.setOnClickListener {
//            uploadImage()
//        }


//        val genderValues = arrayOf("Male", "Female")
//
//        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, genderValues)
//        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//
//        sGender.adapter = adapter
//
//
//        editGender.setOnClickListener {
////            sGender.setOnTouchListener(null)
////            sGender.isClickable = true
////            sGender.isFocusable = true
//
//            sGender.performClick()
//
//            sGender.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//                override fun onItemSelected(
//                    parentView: AdapterView<*>?,
//                    selectedItemView: View?,
//                    position: Int,
//                    id: Long
//                ) {
//                    // Handle the selected item here
//                    val selectedRelation = genderValues[position]
//
//                    Toast.makeText(context, selectedRelation, Toast.LENGTH_SHORT).show()
//                    // Do something with the selected relation...
//                }
//
//                override fun onNothingSelected(parentView: AdapterView<*>?) {
//                    // Do nothing here
//                }
//            }
//
//        }

        return view
    }

    private fun uploadImage() {
        imageDialog.dismiss()
        showProgressDialog(resources.getString(R.string.please_wait))

        val imageRef = storageReference.child("user_images/${System.currentTimeMillis()}.jpg")

        imageRef.putFile(selectedImg!!)
            .addOnSuccessListener { taskSnapshot ->
                // Get the image download URL
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    databaseReference = FirebaseDatabase.getInstance().getReference("users")
                    // Get the current user's UID
                    currentUserUid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()
                    // Check if the user is authenticated
                    val currentUser = firebaseAuth.currentUser
                    if (currentUser != null) {
                        val currentUserUid = currentUser.uid

                        // Check if the user has a valid UID
                        if (currentUserUid.isNotEmpty()) {
                            databaseReference.child(currentUserUid).child("userImg")
                                .setValue(uri.toString())
                                .addOnSuccessListener {
                                    // Dismiss the progress dialog only after successful upload
                                    hideProgressDialog()
                                }
                                .addOnFailureListener {
                                    hideProgressDialog()
                                }
                        }
                    }
                }
            }
            .addOnFailureListener {
                // Handle failure to upload image
                hideProgressDialog()
            }
    }


    private fun showEditDetailsDialog(editTextView: AppCompatEditText, viewName: String) {
        dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_edit_details)

        val dialogEditText = dialog.findViewById<AppCompatEditText>(R.id.etDialog)
        val dialogSaveBtn = dialog.findViewById<TextView>(R.id.tv_save)
        val dialogCancelBtn = dialog.findViewById<TextView>(R.id.tv_cancel)

        // Initialize Firebase database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("users")
        // Get the current user's UID
        currentUserUid = FirebaseAuth.getInstance().currentUser?.uid.orEmpty()

        dialogEditText.text = editTextView.text
        dialog.show()


        dialogSaveBtn.setOnClickListener {
            if (dialogEditText.text.toString().isNotEmpty()){
//                val db = FirebaseDatabase.getInstance().getReference()
//                editTextView.text = dialogEditText.text


                databaseReference.child(currentUserUid).child(viewName).setValue(dialogEditText.text.toString())

                dialog.cancel()
            }
        }

        dialogCancelBtn.setOnClickListener {
            dialog.cancel()
        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(data!=null){
            if(data.data!=null){
                selectedImg = data.data!!
                imageLoadDialog(selectedImg!!)

            }
        }
    }

    private fun openKeyboard() {
        val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(etName, InputMethodManager.SHOW_IMPLICIT)
    }

    fun showProgressDialog(text: String) {
        (activity as? BaseActivity)?.showProgressDialog(text)
    }

    fun hideProgressDialog() {
        (activity as? BaseActivity)?.hideProgressDialog()
    }

    private fun fetchUserData() {
        showProgressDialog(resources.getString(R.string.please_wait))

        RealtimeDatabaseClass().getUserData(this)
//        databaseReference.child(currentUserUid).addValueEventListener(object :
//            ValueEventListener {
//            override fun onDataChange(snapshot: DataSnapshot) {
//                hideProgressDialog()
//                if (snapshot.exists()) {
//                    val userProfile = snapshot.getValue(Users::class.java)
//                    userProfile?.let {
//                        updateUIWithUserData(it)
//                    }
//                }
//            }
//
//            override fun onCancelled(error: DatabaseError) {
//                // Handle error
//                hideProgressDialog()
//            }
//        })
    }

    fun updateUIWithUserData(userProfile: Users) {

        // Check if the fragment is still attached to the activity
        if (!isAdded) {
            return
        }

        etName.setText(userProfile.name)
        etPhone.setText(userProfile.phone)
        etEmail.setText(userProfile.email)
        etAddress.setText(userProfile.address)

        if (userProfile.userImg != null) {
            Glide.with(requireContext()).load(userProfile.userImg).into(userImage)
        }
        // You may want to handle gender differently based on your UI structure
        // e.g., use a Spinner or another EditText
//         sGender.setSelection(genderValues.indexOf(userProfile.gender))
    }

    fun imageLoadDialog(selectedImg: Uri) {
        imageDialog = Dialog(requireContext())
        imageDialog.setContentView(R.layout.dialog_load_image)

        val userImgInDialog = imageDialog.findViewById<CircleImageView>(R.id.userImg)
        val btnUpload = imageDialog.findViewById<Button>(R.id.btnUpload)

        Glide.with(requireContext()).load(selectedImg).into(userImgInDialog)

        btnUpload.setOnClickListener {
            if (userImgInDialog != null) {
                uploadImage()
            }
            else{
                showProgressDialog(resources.getString(R.string.please_wait))
            }
        }
        imageDialog.show()
    }



}