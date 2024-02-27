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
import com.example.womensafety.model.Contacts
import com.example.womensafety.fragment.ContactsFragment
import com.example.womensafety.R
import de.hdodenhof.circleimageview.CircleImageView

class ContactsAdapter(
    private val context: Context,
    private val contactsFragment: ContactsFragment
) : ListAdapter<Contacts, ContactsAdapter.ContactsViewHolder>(ContactsDiffCallback()) {

    private var onItemSelectedListener: OnItemSelectedListener? = null
    private val selectedPositions = mutableListOf<Int>()
    private var enable: Boolean = false

    // Map to store selected items using contactKey as the key
    private val selectedItems = mutableMapOf<String, Contacts>()

    init {
        setHasStableIds(true)
    }

    interface OnItemSelectedListener {
        fun onItemSelectedCountChanged(count: Int)
    }

    fun setOnItemSelectedListener(listener: OnItemSelectedListener) {
        this.onItemSelectedListener = listener
    }

    // Method to get the selected items' details and associate them with user UIDs
    fun getSelectedItemsWithUIDs(): Map<String, Contacts> {
        return selectedItems
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactsViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.single_item_contact, parent, false)
        return ContactsViewHolder(view)
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).contactKey.hashCode().toLong()
    }

    override fun onBindViewHolder(holder: ContactsViewHolder, position: Int) {
        val data = getItem(position)
        val originalPosition = data.originalPosition
        holder.contactName.text = data.contactName
        holder.contactRelation.text = data.contactRelation
        Glide.with(context).load(data.contactImg).centerCrop().into(holder.img)

        // Set the background based on the selected state
        holder.linearLayout.background =
            if (selectedPositions.contains(originalPosition)) {
                context.getDrawable(R.drawable.contact_selected)
            } else {
                context.getDrawable(R.drawable.contact_unselected)
            }

        holder.linearLayout.setOnLongClickListener {
            if (!selectedPositions.contains(originalPosition)) {
                selectItem(holder, data, originalPosition)
            } else {
                Toast.makeText(context, "Already selected", Toast.LENGTH_SHORT).show()
            }
            true
        }

        holder.linearLayout.setOnClickListener {
            if (selectedPositions.isEmpty()) {
                // If no contacts are selected, show a dialog
                // Use the instance of ContactsFragment to call addContactsFun
                contactsFragment.updateContactsFun(data)
            } else {
                // Contacts are selected, handle the selection logic
                if (selectedPositions.contains(originalPosition)) {
                    selectedPositions.remove(originalPosition)
                    data.selected = false
                } else if (enable) {
                    selectItem(holder, data, originalPosition)
                }

                // Update the background based on the selected state
                holder.linearLayout.background =
                    if (selectedPositions.contains(originalPosition)) {
                        context.getDrawable(R.drawable.contact_selected)
                    } else {
                        context.getDrawable(R.drawable.contact_unselected)
                    }

                onItemSelectedListener?.onItemSelectedCountChanged(selectedPositions.size)
            }
        }
    }

    private fun selectItem(holder: ContactsViewHolder, data: Contacts, position: Int) {
        Log.d("currentSelected", position.toString())
        enable = true
        selectedPositions.add(position) // Add to selectedPositions
        data.selected = true

        // Use contactKey as the key in the map
        selectedItems[data.contactKey.toString()] = data

        holder.linearLayout.background = context.getDrawable(R.drawable.contact_selected)
        onItemSelectedListener?.onItemSelectedCountChanged(selectedPositions.size)
    }

    fun clearSelectedContacts() {
        for (position in selectedPositions) {
            val data = getItem(position)
            data.selected = false

            // Remove the contactKey from the map
            selectedItems.remove(data.contactKey.toString())
        }
        selectedPositions.clear()
        notifyDataSetChanged()
        enable = false
    }

    class ContactsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val img: CircleImageView = itemView.findViewById(R.id.contactImageView)
        val contactName: TextView = itemView.findViewById(R.id.contactName)
        val contactRelation: TextView = itemView.findViewById(R.id.contactRelation)
        val linearLayout: LinearLayout = itemView.findViewById(R.id.childLinearLayout)
    }

    class ContactsDiffCallback : DiffUtil.ItemCallback<Contacts>() {
        override fun areItemsTheSame(oldItem: Contacts, newItem: Contacts): Boolean {
            return oldItem.contactKey == newItem.contactKey
        }

        override fun areContentsTheSame(oldItem: Contacts, newItem: Contacts): Boolean {
            return oldItem == newItem
        }
    }
}
