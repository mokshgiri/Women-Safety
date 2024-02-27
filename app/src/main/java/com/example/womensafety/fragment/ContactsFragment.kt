package com.example.womensafety.fragment

import com.example.womensafety.adapter.ContactsAdapter
import android.app.AlertDialog
import android.app.Dialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatEditText
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.womensafety.model.Contacts
import com.example.womensafety.R
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Locale

class ContactsFragment : Fragment() {

    private lateinit var dialog: Dialog
    private lateinit var builder: AlertDialog.Builder
    private lateinit var recyclerView: RecyclerView
    private lateinit var addContactsBtn: Button
    private lateinit var totalSelectedMembersString: String
    private lateinit var progressDialog: Dialog
    private lateinit var createButton: Button
    private lateinit var spinner: Spinner
    private lateinit var nameEditText: AppCompatEditText
    private lateinit var numberEditText: AppCompatEditText
    private lateinit var actualRelation: String
    private lateinit var otherRelationEditText: AppCompatEditText
    private lateinit var relationValues: Array<String>
    private lateinit var tvProgressText: TextView
    private lateinit var list : ArrayList<Contacts>
    private lateinit var tempList : ArrayList<Contacts>
    private lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var recyclerAdapter : ContactsAdapter
    private lateinit var databaseReference: DatabaseReference
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var storage: FirebaseStorage
    private lateinit var storageReference: StorageReference
    private var selectedImg : Uri?= null
    private lateinit var contactImageView : CircleImageView
    private lateinit var iv_camera : CircleImageView
    var selectedMembersListener: OnSelectedMembersListener? = null


    interface OnSelectedMembersListener {
        fun onTotalSelectedMembersChanged(count: Int)
    }

    fun setOnSelectedMembersListener(listener: OnSelectedMembersListener) {
        selectedMembersListener = listener
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize location manager
        list = ArrayList<Contacts>()
        tempList = ArrayList<Contacts>()

        builder = AlertDialog.Builder(context)


        lifecycleScope.launch(Dispatchers.Main) {
            databaseReference = FirebaseDatabase.getInstance().getReference("users")
            firebaseAuth = FirebaseAuth.getInstance()
            storage = FirebaseStorage.getInstance()
            storageReference = storage.reference
        }

//
//        FirebaseApp.initializeApp(requireContext())
//        databaseReference = FirebaseDatabase.getInstance().getReference("contacts")
//        storage = FirebaseStorage.getInstance()
//        storageReference = storage.reference

    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_contacts, container, false)

//        FirebaseApp.initializeApp(requireContext())

        recyclerView = view.findViewById(R.id.recyclerView)
        addContactsBtn = view.findViewById(R.id.addContactsBtn)



//        list.apply {
//            add(Contacts(R.drawable.saloon,"Rajkumar Giri", "Father"))
//            add(Contacts(R.drawable.saloon,"Rajkumar Giri", "Father"))
//            add(Contacts(R.drawable.saloon,"Rajkumar Giri", "Father"))
//            add(Contacts(R.drawable.saloon,"Rajkumar Giri", "Father"))
//            add(Contacts(R.drawable.saloon,"Rajkumar Giri", "Father"))
//            add(Contacts(R.drawable.saloon,"Rajkumar Giri", "Father"))
//            add(Contacts(R.drawable.saloon,"Rajkumar Giri", "Father"))
//            add(Contacts(R.drawable.saloon,"Rajkumar Giri", "Father"))
//            add(Contacts(R.drawable.saloon,"Rajkumar Giri", "Father"))
//            add(Contacts(R.drawable.saloon,"Rajkumar Giri", "Father"))
//            add(Contacts(R.drawable.saloon,"Rajkumar Giri", "Father"))
//            add(Contacts(R.drawable.saloon,"Rajkumar Giri", "Father"))
//            add(Contacts(R.drawable.saloon,"Rajkumar Giri", "Father"))
//        }

        layoutManager = GridLayoutManager(context, 2)
        recyclerView.layoutManager = layoutManager
        recyclerAdapter = ContactsAdapter(requireContext(), this)
        recyclerAdapter.submitList(tempList)
        recyclerView.adapter = recyclerAdapter

        loadContactsFromFirebase()

        recyclerAdapter.setOnItemSelectedListener(object :
            ContactsAdapter.OnItemSelectedListener {
            override fun onItemSelectedCountChanged(count: Int) {
                // Handle the count change here, for example, update a TextView
                val totalSelectedMembers = count
                totalSelectedMembersString = count.toString()
                Log.d("totalSelectedMembers", totalSelectedMembers.toString())

                selectedMembersListener?.onTotalSelectedMembersChanged(totalSelectedMembers)
//                if (count > 0){
//                    searchImg.visibility = View.GONE
//                }
//                else{
////                                header.isVisible = true
////                                customToolBar.isInvisible = true
//                    searchImg.visibility = View.GONE
//                }


            }
        })

        // Load data from Firebase
//        databaseReference.get().addOnSuccessListener {
//            if (it.value != null){

//                addContactsBtn.visibility = View.GONE
//            }
//            else{
//                addContactsBtn.visibility = View.VISIBLE
//            }
//        }


        Log.d("listt", list.toString())


        return view
    }

    private fun addUpdateContactDialogInitialize() {
        dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_add_contacts)

        val otherRelationLayout : TextInputLayout = dialog.findViewById(R.id.otherRelationLayout)
        otherRelationEditText  = dialog.findViewById(R.id.otherRelation)
        contactImageView = dialog.findViewById(R.id.contactImageView)
        iv_camera = dialog.findViewById(R.id.iv_camera)
        createButton = dialog.findViewById(R.id.btn_create)
        spinner = dialog.findViewById(R.id.contactRelationSpinner)
        nameEditText = dialog.findViewById(R.id.contactName)
        numberEditText = dialog.findViewById(R.id.contactNumber)


        hideProgressDialog()

        iv_camera.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(intent, 1)
        }

        relationValues = arrayOf("Father", "Mother", "Brother", "Sister", "Friend", "Other")

        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, relationValues)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinner.adapter = adapter

        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parentView: AdapterView<*>?, selectedItemView: View?, position: Int, id: Long) {
                // Handle the selected item here
                val selectedRelation = relationValues[position]

                if (selectedRelation == "Other"){
                    otherRelationLayout.visibility = View.VISIBLE

                }
                else{
                    otherRelationLayout.visibility = View.GONE
                }
                // Do something with the selected relation...
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
                // Do nothing here
            }
        }

        dialog.show()
    }

    private fun uploadContactData(
        newContactRef: DatabaseReference,
        dialog: Dialog,
        newContact: Contacts
    ) {
        newContactRef.setValue(newContact).addOnSuccessListener {

            // Dismiss the dialog
            dialog.dismiss()
            hideProgressDialog()

            // Clear selectedImg when dialog is dismissed
            selectedImg = null
        }
    }


    fun loadContactsFromFirebase() {
        showProgressDialog("Please wait")

        databaseReference = FirebaseDatabase.getInstance().getReference("users")
        firebaseAuth = FirebaseAuth.getInstance()

        val currentUserUid = firebaseAuth.currentUser?.uid

        if (currentUserUid != null) {
            databaseReference.child(currentUserUid).child("contactsList").addValueEventListener(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    hideProgressDialog()

                    if (snapshot.exists()) {
                        list.clear()
//                        for (contactSnapshot in snapshot.children) {
//                            val contact = contactSnapshot.getValue(Contacts::class.java)
//                            if (contact != null) {
//                                list.add(contact)
//
//                            }
//                        }

                        for ((index, contactSnapshot) in snapshot.children.withIndex()) {
                            val contact = contactSnapshot.getValue(Contacts::class.java)
                            if (contact != null) {
                                contact.originalPosition = index
                                list.add(contact)
                            }
                        }




                        tempList.clear()
                        tempList.addAll(list)
                        recyclerAdapter.notifyDataSetChanged()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    hideProgressDialog()
                    // Handle error
                }
            })
        } else {
            hideProgressDialog()
            // Handle the case when the user is not authenticated
        }
    }

    fun showProgressDialog(text: String) {
        progressDialog = Dialog(requireContext())
        progressDialog.setContentView(R.layout.dialog_progress)
        progressDialog.setCancelable(false)
        tvProgressText = progressDialog.findViewById(R.id.tv_progress_text)
        tvProgressText.text = text
        progressDialog.show()
    }

    fun hideProgressDialog() {
        progressDialog.dismiss()
    }


    fun addContactsFun() {
        addUpdateContactDialogInitialize()

        databaseReference = FirebaseDatabase.getInstance().getReference("users")
        firebaseAuth = FirebaseAuth.getInstance()

        createButton.setOnClickListener {

            val selectedRelation = spinner.selectedItem.toString()
            actualRelation =
                if (selectedRelation == "Other") otherRelationEditText.text.toString() else selectedRelation

            if (nameEditText.text.isNullOrBlank() || numberEditText.text.isNullOrBlank()) {
                Toast.makeText(
                    requireContext(),
                    "Please enter both name and contact number",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            if (selectedImg != null){
                showProgressDialog("Please wait")
                dialog.dismiss()

            val imageRef =
                storageReference.child("contact_images/${System.currentTimeMillis()}.jpg")

            imageRef.putFile(selectedImg!!)
                .addOnSuccessListener { taskSnapshot ->
                    // Get the image download URL
                    imageRef.downloadUrl.addOnSuccessListener { uri ->

                        // Check if the user is authenticated
                        val currentUser = firebaseAuth.currentUser
                        if (currentUser != null) {
                            val currentUserUid = currentUser.uid

                            // Check if the user has a valid UID
                            if (currentUserUid.isNotEmpty()) {
                                val newContactRef =
                                    databaseReference.child(currentUserUid).child("contactsList")
                                        .push()
                                val newContactKey = newContactRef.key

                                val newContact = Contacts(
                                    newContactKey, uri.toString(),
                                    nameEditText.text.toString(),
                                    numberEditText.text.toString(),
                                    actualRelation
                                )

                                uploadContactData(newContactRef, dialog, newContact)
                            } else {
                                // Handle the case when the user does not have a valid UID
                                Toast.makeText(
                                    requireContext(),
                                    "User does not have a valid UID",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            // Handle the case when the user is not authenticated
                            Toast.makeText(
                                requireContext(),
                                "User not authenticated",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
                }
            else {
                // If no image is selected, create a contact without an image

                showProgressDialog("Please wait")
                dialog.dismiss()

                // Check if the user is authenticated
                val currentUser = firebaseAuth.currentUser
                if (currentUser != null) {
                    val currentUserUid = currentUser.uid

                    // Check if the user has a valid UID
                    if (currentUserUid.isNotEmpty()) {
                        val newContactRef =
                            databaseReference.child(currentUserUid).child("contactsList")
                                .push()
                        val newContactKey = newContactRef.key

                        val newContact = Contacts(
                            newContactKey,
                            "",
                            nameEditText.text.toString(),
                            numberEditText.text.toString(),
                            actualRelation
                        )

                        uploadContactData(newContactRef, dialog, newContact)
                    } else {
                        // Handle the case when the user does not have a valid UID
                        Toast.makeText(
                            requireContext(),
                            "User does not have a valid UID",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    // Handle the case when the user is not authenticated
                    Toast.makeText(
                        requireContext(),
                        "User not authenticated",
                        Toast.LENGTH_SHORT
                    ).show()
                }

            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(data!=null){
            if(data.data!=null){
                selectedImg = data.data!!
                contactImageView.setImageURI(selectedImg)

            }
        }
    }

    fun deleteAlertDialog(){
        builder.setTitle("Delete")
            .setCancelable(true)
            .setIcon(R.drawable.baseline_delete_24)
            .setMessage("Are you sure you want to delete $totalSelectedMembersString contact ?")
            .setPositiveButton("Yes") { _, _ ->
                deleteContactsFun()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.cancel()
                clearSelections()
            }
            .show()
    }

    fun deleteContactsFun() {

        // Get the selected items with UIDs from the adapter
        val selectedItemsWithUIDs = recyclerAdapter.getSelectedItemsWithUIDs()
        val currentUser = firebaseAuth.currentUser
        val currentUserUid = currentUser?.uid

        Log.d("selectedItemsWithUIDs", selectedItemsWithUIDs.toString())

        var isToastShown = false // Flag to track whether the toast has been shown

        // Delete selected items from Firebase
        for ((contactUid, _) in selectedItemsWithUIDs) {
            val contactRef = databaseReference.child(currentUserUid.toString()).child("contactsList").child(contactUid)

            // Remove the contact from Firebase
            contactRef.removeValue().addOnSuccessListener {
                // Successfully removed the contact
                val position = tempList.indexOfFirst { it.contactKey == contactUid }
                if (position != -1) {
                    tempList.removeAt(position)
                    // Notify the adapter about the removal
                    recyclerAdapter.notifyItemRemoved(position)
                }
            }.addOnFailureListener {
                // Handle failure to remove the contact
                if (!isToastShown) {
                    Toast.makeText(
                        requireContext(),
                        "Failed to delete contacts",
                        Toast.LENGTH_SHORT
                    ).show()
                    isToastShown = true // Set the flag to true after showing the toast
                }
            }
        }
//        recyclerAdapter.notifyItemRemoved()

        clearSelections()

        Log.d("tempListAfterDelete", tempList.toString())




        // Show a toast if contacts are deleted successfully
        if (!isToastShown) {
            Toast.makeText(requireContext(), "Contacts deleted successfully", Toast.LENGTH_SHORT).show()
        }

    }

    private fun clearSelections() {

        // Clear the selected contacts in the adapter
        recyclerAdapter.clearSelectedContacts()

        // Notify the adapter that the data set has changed
        recyclerAdapter.notifyDataSetChanged()

        // Inform the listener about the change in the selected members count
        selectedMembersListener?.onTotalSelectedMembersChanged(0)

    }

    fun updateContactsFun(contactDetails : Contacts) {
        addUpdateContactDialogInitialize()

//        contactImageView.setImageURI(contactDetails.contactImg?.toUri())

        Glide.with(requireContext()).load(contactDetails.contactImg).into(contactImageView)
        nameEditText.setText(contactDetails.contactName)
        numberEditText.setText(contactDetails.contactNumber)
        otherRelationEditText.setText(contactDetails.contactRelation)

        val currentUser = firebaseAuth.currentUser
        val currentUserUid = currentUser?.uid


        // Set selected value in the spinner
        val selectedRelationIndex = relationValues.indexOf(contactDetails.contactRelation)
        if (selectedRelationIndex != -1) {
            spinner.setSelection(selectedRelationIndex)
        }
        else{
            spinner.setSelection(5)
        }

        createButton.text = resources.getString(R.string.update)

        createButton.setOnClickListener {

            val selectedRelation = spinner.selectedItem.toString()
            actualRelation = if (selectedRelation == "Other") otherRelationEditText.text.toString() else selectedRelation


            if (nameEditText.text.isNullOrBlank() || numberEditText.text.isNullOrBlank()) {
                // Show a Toast if either name or contact number is empty
                Toast.makeText(
                    requireContext(),
                    "Please enter both name and contact number",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }


            // Check if an image is selected
            if (selectedImg != null) {
                // Upload image to Firebase Storage
                val imageRef = storageReference.child("contact_images/${System.currentTimeMillis()}.jpg")
                showProgressDialog("Please wait")

                imageRef.putFile(selectedImg!!)
                    .addOnSuccessListener { taskSnapshot ->
                        // Get the image download URL
                        imageRef.downloadUrl.addOnSuccessListener { uri ->

                            val newContact = Contacts(
                                contactDetails.contactKey,
                                uri.toString() ?: "",
                                nameEditText.text.toString(),
                                numberEditText.text.toString(),
                                actualRelation
                            )

                            uploadContactData(
                                databaseReference.child(currentUserUid.toString()).child("contactsList").child(contactDetails.contactKey.toString()),
                                dialog,
                                newContact
                            )

                            // Dismiss the dialog
                            dialog.dismiss()
                            hideProgressDialog()

                            // Clear selectedImg when dialog is dismissed
                            selectedImg = null
                        }
                    }
                    .addOnFailureListener {
                        // Handle image upload failure
                    }
            } else {
                // If no image is selected, create a contact without an image

                showProgressDialog("Please wait")

                val newContact = Contacts(
                    contactDetails.contactKey,
                    contactDetails.contactImg ?: "",
                    nameEditText.text.toString(),
                    numberEditText.text.toString(),
                    actualRelation
                )

                uploadContactData(
                    databaseReference.child(currentUserUid.toString()).child("contactsList").child(contactDetails.contactKey.toString()),
                    dialog,
                    newContact
                )

            }

        }
    }

    fun searchViewFun(searchView: SearchView?) {
        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {

                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {

                tempList.clear()
                val searchText = newText!!.toLowerCase(Locale.getDefault())

                if (searchText.isNotEmpty()){
                    list.forEach{
                        if (it.contactName!!.toLowerCase(Locale.getDefault()).contains(searchText)){
                            tempList.add(it)
                        }
                    }
                    recyclerView.adapter!!.notifyDataSetChanged()
                } else{
                    tempList.clear()
                    tempList.addAll(list)
                    recyclerView.adapter!!.notifyDataSetChanged()

                }
                return false
            }

        })

//        searchView?.icon {
//                Toast.makeText(context, "Search icon clicked", Toast.LENGTH_SHORT).show()
//        }
    }

}