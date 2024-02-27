package com.example.womensafety.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.womensafety.ContactLoader
import com.example.womensafety.R
import com.example.womensafety.activity.BaseActivity
import com.example.womensafety.adapter.ContactsAdapter
import com.example.womensafety.adapter.ContactsAdapter1
import com.example.womensafety.model.ContactModel
import com.example.womensafety.model.Contacts
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Locale

class ContactsFragment1 : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var contactLoader: ContactLoader
    private lateinit var builder: AlertDialog.Builder
    private lateinit var layoutManager: RecyclerView.LayoutManager
    lateinit var recyclerAdapter: ContactsAdapter1
    private lateinit var dialogRecAdapter: ContactsAdapter1
    private lateinit var contacts: ArrayList<ContactModel>
    private lateinit var addContacts: Button
    private lateinit var list: ArrayList<ContactModel>
    private lateinit var dialog: Dialog
    private lateinit var searchView: SearchView
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseReference: DatabaseReference
    private lateinit var totalSelectedMembersString: String
    private var validNumber: Boolean? = true
    private lateinit var filteredList: ArrayList<ContactModel>

    var selectedMembersListener: OnSelectedMembersListener? = null

    private var isAllContactsSelected = false

    interface OnSelectedMembersListener {
        fun onTotalSelectedMembersChanged(count: Int)
    }

    fun setOnSelectedMembersListener(listener: OnSelectedMembersListener) {
        selectedMembersListener = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_contacts, container, false)

        lifecycleScope.launch {
            showDialog()
            withContext(Dispatchers.IO) {
                // Perform heavy work, e.g., loading contacts from Firebase
                loadContactsFromFirebase()
            }
        }


        lifecycleScope.launch(Dispatchers.Main) {
            dialog = Dialog(requireContext())
            dialog.setContentView(R.layout.dialog_all_contacts)

            firebaseAuth = FirebaseAuth.getInstance()
            databaseReference = FirebaseDatabase.getInstance().getReference("users")

            searchView = dialog.findViewById(R.id.searchView)

            builder = AlertDialog.Builder(context)

            contactLoader = ContactLoader(requireContext())
            contacts = contactLoader.loadContacts()

            list = ArrayList()
            filteredList = ArrayList()

            layoutManager = GridLayoutManager(context, 2)
            recyclerView.layoutManager = layoutManager

            recyclerAdapter.submitList(list)
            recyclerView.adapter = recyclerAdapter
        }

        recyclerView = view.findViewById(R.id.recyclerView)
        addContacts = view.findViewById(R.id.addContactsBtn)

        recyclerAdapter = ContactsAdapter1(requireContext(), this)

        recyclerAdapter.setOnItemSelectedListener(object :
            ContactsAdapter1.OnItemSelectedListener {
            override fun onItemSelectedCountChanged(count: Int) {
                val totalSelectedMembers = count
                totalSelectedMembersString = count.toString()

                selectedMembersListener?.onTotalSelectedMembersChanged(totalSelectedMembers)

                if (totalSelectedMembers > 0) {
                    isAllContactsSelected = false
                } else {
                    clearSelections()
                }
            }
        })

        addContacts.setOnClickListener {
            showAddContactsDialog()
        }

        return view
    }

    fun selectAllContactInFragment() {
        if (isAllContactsSelected) {
            unselectAllContacts()
        } else {
            selectAllContacts()
        }
        isAllContactsSelected = !isAllContactsSelected
    }

    private fun selectAllContacts() {
        recyclerAdapter.selectAllContacts()
        recyclerAdapter.notifyDataSetChanged()
    }

    private fun unselectAllContacts() {
        recyclerAdapter.unselectAllContacts()
        recyclerAdapter.notifyDataSetChanged()
    }

    private fun selectAllContactsInDialog() {
        recyclerAdapter.selectAllContacts()
        recyclerAdapter.notifyDataSetChanged()
    }

    private fun unselectAllContactsInDialog() {
        // Select all contacts in the adapter
        dialogRecAdapter.unselectAllContacts()
        // Notify the adapter that the data set has changed
        dialogRecAdapter.notifyDataSetChanged()
    }

    fun showAddContactsDialog() {
        val dialogRecView: RecyclerView = dialog.findViewById(R.id.recyclerView)
        val dialogAddBtn: AppCompatButton = dialog.findViewById(R.id.addContactsBtn)
        val dialogSearchView: SearchView = dialog.findViewById(R.id.searchView)
        val dialogToolbar: Toolbar = dialog.findViewById(R.id.toolbar)
        val dialogSearchBackBtn: CircleImageView = dialog.findViewById(R.id.backBtn)
        val dialogSelectAllIcon: ImageView = dialog.findViewById(R.id.selectAll)
        val selectedNumber: TextView = dialog.findViewById(R.id.selectedContactNumber)

        layoutManager = GridLayoutManager(context, 2)
        dialogRecView.layoutManager = layoutManager
        dialogRecAdapter = ContactsAdapter1(requireContext(), this)
        dialogRecAdapter.submitList(filteredList)
        dialogRecView.adapter = dialogRecAdapter
        searchViewFunInDialog(searchView, dialogRecView)

        dialogSearchView.visibility = View.VISIBLE
        dialogToolbar.visibility = View.GONE

        dialogSearchView.setQuery("", false)

        dialog.show()

        dialogAddBtn.setOnClickListener {
            addContactsToFirebase()
        }

        dialogRecAdapter.setOnItemSelectedListener(object :
            ContactsAdapter1.OnItemSelectedListener {
            override fun onItemSelectedCountChanged(count: Int) {
                val totalSelectedMembers = count

                if (totalSelectedMembers > 0) {
                    dialogSearchView.visibility = View.VISIBLE
                    dialogToolbar.visibility = View.VISIBLE
                    selectedNumber.text = totalSelectedMembers.toString()

                    isAllContactsSelected = false

                    dialogSearchBackBtn.setOnClickListener {
                        clearSelectionsInDialog()
                        dialogSearchView.setQuery("", false)
                        dialogToolbar.visibility = View.GONE
                    }

                    dialogSelectAllIcon.setOnClickListener {
                        if (isAllContactsSelected) {
                            unselectAllContactsInDialog()
                        } else {
                            selectAllContactsInDialog()
                        }
                        isAllContactsSelected = !isAllContactsSelected
                    }

                } else {
                    dialogSearchView.visibility = View.VISIBLE
                    dialogToolbar.visibility = View.GONE
                    clearSelectionsInDialog()
                }
            }
        })
    }

    private suspend fun showDialog() {
        withContext(Dispatchers.Main) {
            (activity as? BaseActivity)?.showProgressDialog("Please Wait")
        }
    }

    private suspend fun hideDialog() {
        withContext(Dispatchers.Main) {
            (activity as? BaseActivity)?.hideProgressDialog()
        }
    }

    fun loadContactsFromFirebase() {
        databaseReference = FirebaseDatabase.getInstance().getReference("users")
        firebaseAuth = FirebaseAuth.getInstance()

        val currentUserUid = firebaseAuth.currentUser?.uid

        if (currentUserUid != null) {
            databaseReference.child(currentUserUid).child("contactsList").addValueEventListener(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    lifecycleScope.launch {
                        hideDialog()
                    }

                    if (snapshot.exists()) {
                        addContacts.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE

                        val newList = ArrayList<ContactModel>()

                        for ((index, contactSnapshot) in snapshot.children.withIndex()) {
                            val contact = contactSnapshot.getValue(ContactModel::class.java)
                            if (contact != null) {
                                newList.add(contact)
                            }
                        }

                        newList.sortBy { it.name }

                        list.clear()
                        list.addAll(newList)

                        recyclerAdapter.submitList(newList)
                        recyclerAdapter.notifyDataSetChanged()
                    } else {
                        addContacts.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    // Handle error
                }
            })
        }
    }

    fun deleteAlertDialog() {
        builder.setTitle("Delete")
            .setCancelable(true)
            .setIcon(R.drawable.baseline_delete_24)
            .setMessage("Are you sure you want to delete $totalSelectedMembersString contact ?")
            .setPositiveButton("Yes") { _, _ ->
                deleteContactsFun()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.cancel()
            }
            .show()
    }

    fun deleteContactsFun() {
        val selectedItemsWithNumbers = recyclerAdapter.getSelectedItemsWithNumbers()
        val currentUser = firebaseAuth.currentUser
        val currentUserUid = currentUser?.uid

        var isToastShown = false

        val tempListCopy = ArrayList(list)

        for (contactNumber in selectedItemsWithNumbers) {
            val contactRef = databaseReference.child(currentUserUid.toString()).child("contactsList")
                .child(contactNumber)

            val position = tempListCopy.indexOfFirst { it.number == contactNumber }

            if (position != -1) {
                val view = layoutManager.findViewByPosition(position)
                val animation = AnimationUtils.loadAnimation(requireContext(), R.anim.item_slide_out)
                view?.startAnimation(animation)

                Handler().postDelayed({
                    contactRef.removeValue().addOnSuccessListener {
                        val position = tempListCopy.indexOfFirst { it.number == contactNumber }
                        if (position != -1) {
                            tempListCopy.removeAt(position)
                            list.clear()
                            list.addAll(tempListCopy)

                            if (list.isEmpty()) {
                                addContacts.visibility = View.VISIBLE
                                recyclerView.visibility = View.GONE
                            }

                            if (!isToastShown) {
                                Toast.makeText(
                                    requireContext(),
                                    "Contacts deleted successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                                isToastShown = true
                            }
                        }
                    }.addOnFailureListener {
                        if (!isToastShown) {
                            Toast.makeText(
                                requireContext(),
                                "Failed to delete contacts",
                                Toast.LENGTH_SHORT
                            ).show()
                            isToastShown = true
                        }
                    }
                }, animation.duration)
            }
        }

        clearSelections()
    }

    private fun addContactsToFirebase() {
        val selectedItemsWithNumbers = dialogRecAdapter.getSelectedItemsWithNumbers()
        val currentUser = firebaseAuth.currentUser
        val currentUserUid = currentUser?.uid

        var isToastShown = false

        if (selectedItemsWithNumbers.isNotEmpty()) {
            for (contactNumber in selectedItemsWithNumbers) {
                if (contactNumber.first().isDigit() || contactNumber.startsWith('+')) {
                    val sanitizedContactNumber = contactNumber.replace("[.#\\[\\]\$]".toRegex(), "_")
                    val contactRef =
                        databaseReference.child(currentUserUid.toString()).child("contactsList")
                            .child(contactNumber)

                    val selectedContactModel = contacts.firstOrNull { it.number == contactNumber }

                    contactRef.setValue(selectedContactModel).addOnSuccessListener {
                        clearSelectionsInDialog()
                        dialog.dismiss()
                    }.addOnFailureListener {
                        if (!isToastShown) {
                            Toast.makeText(
                                requireContext(),
                                "Failed to add contacts",
                                Toast.LENGTH_SHORT
                            ).show()
                            isToastShown = true
                        }
                    }
                } else {
                    if (!isToastShown) {
                        Toast.makeText(
                            requireContext(),
                            "There is some invalid contact number in your selected contacts",
                            Toast.LENGTH_SHORT
                        ).show()
                        isToastShown = true
                    }

                    clearSelectionsInDialog()
                    break
                }
            }
        } else {
            Toast.makeText(
                requireContext(),
                "Please select a contact",
                Toast.LENGTH_SHORT
            ).show()
        }

        clearSelectionsInDialog()
    }

    fun searchViewFun(searchView: SearchView?) {
        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filteredList.clear()
                val searchText = newText!!.toLowerCase(Locale.getDefault())

                if (searchText.isNotEmpty()) {
                    list.forEach {
                        if (it.name!!.toLowerCase(Locale.getDefault()).contains(searchText)) {
                            filteredList.add(it)
                        }
                    }
                    recyclerAdapter.submitList(filteredList)
                } else {
                    recyclerAdapter.submitList(list)
                }

                recyclerAdapter.notifyDataSetChanged()

                return false
            }
        })
    }

    private fun searchViewFunInDialog(searchView: SearchView?, dialogRecView: RecyclerView) {
        searchView?.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filteredList.clear()
                val searchText = newText!!.toLowerCase(Locale.getDefault())

                if (searchText.isNotEmpty()) {
                    contacts.forEach {
                        if (it.name!!.toLowerCase(Locale.getDefault()).contains(searchText)) {
                            filteredList.add(it)
                        }
                    }
                    dialogRecView.adapter!!.notifyDataSetChanged()
                } else {
                    filteredList.clear()
                    filteredList.addAll(contacts)
                    dialogRecView.adapter!!.notifyDataSetChanged()
                }
                return false
            }
        })
    }

    private fun clearSelectionsInDialog() {
        dialogRecAdapter.clearSelectedContacts()
        dialogRecAdapter.notifyDataSetChanged()
    }

    private fun clearSelections() {
        recyclerAdapter.clearSelectedContacts()
        recyclerAdapter.notifyDataSetChanged()
        selectedMembersListener?.onTotalSelectedMembersChanged(0)
    }
}
