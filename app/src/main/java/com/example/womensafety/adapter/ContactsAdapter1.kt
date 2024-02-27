package com.example.womensafety.adapter

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.womensafety.R
import com.example.womensafety.fragment.ContactsFragment
import com.example.womensafety.fragment.ContactsFragment1
import com.example.womensafety.model.ContactModel
import com.example.womensafety.model.Contacts
import de.hdodenhof.circleimageview.CircleImageView

class ContactsAdapter1(
    val context: Context,
    private val contactsFragment: ContactsFragment1
) : ListAdapter<ContactModel, ContactsAdapter1.ContactViewHolder>(ContactsAdapter1.ContactsDiffCallback()) {

    private var onItemSelectedListener: ContactsAdapter1.OnItemSelectedListener? = null

    private val selectedNumbers = mutableSetOf<String>()
    private var enable: Boolean = false


    init {
        setHasStableIds(true) // Enable stable IDs for the RecyclerView
    }

    interface OnItemSelectedListener {
        fun onItemSelectedCountChanged(count: Int)
    }

    fun setOnItemSelectedListener(listener: OnItemSelectedListener) {
        this.onItemSelectedListener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_contact, parent, false)
        return ContactViewHolder(view)
    }

    // Method to get the selected items' details and associate them with user UIDs
    fun getSelectedItemsWithNumbers(): MutableSet<String>{
        return selectedNumbers
    }
    override fun getItemId(position: Int): Long {
        return getItem(position).number.hashCode().toLong()
    }


    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val data = getItem(position)
        val number = data.number
        val img = data.image

        holder.nameTextView.text = data.name
        holder.numberTextView.text = number ?: ""

        // Clear any ongoing Glide requests for the recycled view
        Glide.with(context).clear(holder.image)
        // Load the image using Glide with a placeholder and error drawable
        Glide.with(context)
            .load(img)
            .placeholder(R.drawable.layer_circle_image_profile) // Set your default image resource here
            .error(R.drawable.layer_circle_image_profile) // Set your default image resource here
            .into(holder.image)


        holder.linearLayout.background =
            if (selectedNumbers.contains(number)) {
                context.getDrawable(R.drawable.contact_selected)
            } else {
                context.getDrawable(R.drawable.contact_unselected)
            }


        holder.linearLayout.setOnLongClickListener {
            if (!selectedNumbers.contains(number)) {
                selectItem(holder, number ?: "")
            } else {
                unselectItem(holder, number ?: "")
            }
            true
        }

        holder.linearLayout.setOnClickListener {
            if (selectedNumbers.contains(number)) {
                unselectItem(holder, number ?: "")
            } else if (enable) {
                selectItem(holder, number ?: "")
            }
        }
    }

    private fun selectItem(holder: ContactViewHolder, number: String) {
        enable = true
        selectedNumbers.add(number) // Add to selectedNumbers

        holder.linearLayout.background = context.getDrawable(R.drawable.contact_selected)
        onItemSelectedListener?.onItemSelectedCountChanged(selectedNumbers.size)
    }

    private fun unselectItem(holder: ContactViewHolder, number: String) {
        selectedNumbers.remove(number) // Remove from selectedNumbers
        holder.linearLayout.background = context.getDrawable(R.drawable.contact_unselected)
        onItemSelectedListener?.onItemSelectedCountChanged(selectedNumbers.size)
    }

    fun clearSelectedContacts() {
        selectedNumbers.clear()
        notifyDataSetChanged()
        enable = false
    }

    fun selectAllContacts() {
        // Clear the previous selections
        selectedNumbers.clear()
        // Add all contact numbers to the selection set
        selectedNumbers.addAll(currentList.mapNotNull { it.number })

        onItemSelectedListener?.onItemSelectedCountChanged(selectedNumbers.size)
    }

    fun unselectAllContacts(){
        // Clear the previous selections
        selectedNumbers.clear()
        onItemSelectedListener?.onItemSelectedCountChanged(0)
    }


    class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
        val numberTextView: TextView = itemView.findViewById(R.id.numberTextView)
        val image: CircleImageView = itemView.findViewById(R.id.contactImageView)
        val linearLayout: LinearLayout = itemView.findViewById(R.id.childLinearLayout)
    }

    class ContactsDiffCallback : DiffUtil.ItemCallback<ContactModel>() {
        override fun areItemsTheSame(oldItem: ContactModel, newItem: ContactModel): Boolean {
            return oldItem.number == newItem.number
        }

        override fun areContentsTheSame(oldItem: ContactModel, newItem: ContactModel): Boolean {
            return oldItem == newItem
        }
    }
}