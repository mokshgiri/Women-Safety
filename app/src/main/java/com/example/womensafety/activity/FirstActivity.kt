package com.example.womensafety.activity


import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.example.womensafety.fragment.EmpergencyFragment
import com.example.womensafety.fragment.MapsFragment
import com.example.womensafety.fragment.ProfileFragment
import com.example.womensafety.R
import com.example.womensafety.firebase.RealtimeDatabaseClass
import com.example.womensafety.fragment.ContactsFragment1
import com.example.womensafety.fragment.SettingsFragment
import com.example.womensafety.model.Users
import de.hdodenhof.circleimageview.CircleImageView


class FirstActivity : BaseActivity(), ContactsFragment1.OnSelectedMembersListener {

    private val tabItems = mutableListOf<TabItem>()

    private lateinit var searchView : SearchView
    private lateinit var searchBar : Toolbar
    private lateinit var searchBackBtn : CircleImageView
    private lateinit var searchImg : ImageView
    private lateinit var header : RelativeLayout
    private lateinit var addContact : ImageView
    private lateinit var deleteContact : ImageView
    private lateinit var selectedContactNumber : TextView
    private lateinit var backBtn : CircleImageView
    private lateinit var toolbar : Toolbar
    private lateinit var fragmentName : TextView
    private lateinit var rightToolsLinearLayout : LinearLayout
    private lateinit var notificationIcon : ImageView
    private lateinit var selectAll : ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_first)

        searchView = findViewById(R.id.searchView)
        searchBar = findViewById(R.id.search_bar)
        searchImg = findViewById(R.id.searchImage)
        searchBackBtn = findViewById(R.id.searchBackBtn)
        header = findViewById(R.id.parentRelLayout)
        addContact = findViewById(R.id.addContact)
        deleteContact = findViewById(R.id.deleteContact)
        backBtn = findViewById(R.id.backBtn)
        selectedContactNumber = findViewById(R.id.selectedContactNumber)
        toolbar = findViewById(R.id.toolbar)
//        appName = findViewById(R.id.appName)
        fragmentName = findViewById(R.id.fragmentName)
        fragmentName = findViewById(R.id.fragmentName)
        rightToolsLinearLayout = findViewById(R.id.rightToolsLinearLayout)
        notificationIcon = findViewById(R.id.notificationIcon)
        selectAll = findViewById(R.id.selectAll)

        initializeTabItems()

        for (tabItem in tabItems) {
            tabItem.layout.setOnClickListener {
                handleTabItemClick(tabItem)
            }
        }

        // Load the initial fragment
        loadFragment(EmpergencyFragment())
        tabItems[2].select()

//        val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container_view)
//
//        if (fragment is MyBookingFragment) {
//            // Call the method in your fragment
//            searchImg.isGone = true
//            addContact.isVisible = true
//
//
////            (fragment as HomeFragment).handleVolumeDownButton()
//
//        }
    }

    private fun initializeTabItems() {
        tabItems.add(
            TabItem(
                findViewById(R.id.bookingLinearLayout),
                findViewById(R.id.bookingEnableImg1),
                findViewById(R.id.bookingImg),
                findViewById(R.id.bookingTxt),
                ContactsFragment1()
            )
        )
        tabItems.add(
            TabItem(
                findViewById(R.id.scanQrLinLayout),
                findViewById(R.id.scanQrEnableImg),
                findViewById(R.id.scanImageView),
                findViewById(R.id.scanQrTxt),
                MapsFragment()
            )
        )
        tabItems.add(
            TabItem(
                findViewById(R.id.homeLinearLayout),
                findViewById(R.id.homeEnableImg),
                findViewById(R.id.homeImageView),
                findViewById(R.id.homeTextView),
                EmpergencyFragment()
            )
        )
        tabItems.add(
            TabItem(
                findViewById(R.id.myQrLinearLayout),
                findViewById(R.id.MyQrEnableImg),
                findViewById(R.id.myQrImageView),
                findViewById(R.id.myQrTextView),
                SettingsFragment()
            )
        )
        tabItems.add(
            TabItem(
                findViewById(R.id.profileLinearLayout),
                findViewById(R.id.profileEnableImg),
                findViewById(R.id.profileImgView),
                findViewById(R.id.profileTextView),
                ProfileFragment()
            )
        )
    }

    private fun handleTabItemClick(selectedTabItem: TabItem) {
        for (tabItem in tabItems) {
            tabItem.reset()
        }
        selectedTabItem.select()

        // Load the corresponding fragment
        loadFragment(selectedTabItem.fragment)
    }

    private fun loadFragment(fragment: androidx.fragment.app.Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container_view, fragment)
            .commit()
//
//        if (fragment is ContactsFragment) {
//
//        }

        onTabChanged(fragment)
    }

    private fun onTabChanged(fragment: Fragment) {
        addContact.visibility = View.GONE

        when (fragment) {
            is EmpergencyFragment -> {
//                appName.visibility = View.GONE
                toolbar.visibility = View.VISIBLE
                searchImg.visibility = View.GONE
                backBtn.visibility = View.GONE
                selectedContactNumber.visibility = View.GONE
                deleteContact.visibility = View.GONE

                searchBar.visibility = View.GONE
                toolbar.visibility = View.VISIBLE
                rightToolsLinearLayout.visibility = View.VISIBLE

                fragmentName.text = getString(R.string.family_safety)

            }

            is ContactsFragment1 -> {
//                appName.visibility = View.GONE
                toolbar.visibility = View.VISIBLE
                searchImg.visibility = View.VISIBLE
                addContact.visibility = View.VISIBLE
                deleteContact.visibility = View.GONE
                backBtn.visibility = View.GONE
                selectAll.visibility = View.GONE
                selectedContactNumber.visibility = View.GONE
                fragmentName.text = getString(R.string.contacts)
//
                searchImg.setOnClickListener {
                    searchBar.visibility = View.VISIBLE
                    toolbar.visibility = View.GONE
                    rightToolsLinearLayout.visibility = View.GONE

                    searchView.requestFocus()
                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)

                    (fragment as ContactsFragment1).searchViewFun(searchView)
                }
////1
                searchBackBtn.setOnClickListener {
                    searchBar.visibility = View.GONE
                    toolbar.visibility = View.VISIBLE
                    rightToolsLinearLayout.visibility = View.VISIBLE

                    searchView.setQuery("", false)
                }

                addContact.setOnClickListener {
                    (fragment as ContactsFragment1).showAddContactsDialog()
//                    (fragment as ContactsFragment).loadContactsFromFirebase()
                }
//
                (fragment as ContactsFragment1).setOnSelectedMembersListener(this)
//
                deleteContact.setOnClickListener {
                    (fragment as ContactsFragment1).deleteAlertDialog()
                }

                selectAll.setOnClickListener {
                    (fragment as ContactsFragment1).selectAllContactInFragment()
                }
//
                backBtn.setOnClickListener {

                    (fragment as ContactsFragment1).recyclerAdapter.apply {
                        // Clear the selected contacts in the adapter
                        clearSelectedContacts()
                        // Notify the adapter that the data set has changed
                        notifyDataSetChanged()
                    }
                    // Inform the listener about the change in the selected members count
                    (fragment as ContactsFragment1).selectedMembersListener?.onTotalSelectedMembersChanged(0)

                }


//                val contactsFragment = ContactsFragment()
//                contactsFragment.setOnSelectedMembersListener(this)

//                val adapter = Con
//                (fragment as ContactsFragment).recyclerAdapter.setOnItemSelectedListener(object :
//                    com.example.womensafety.adapter.ContactsAdapter.OnItemSelectedListener {
//                    override fun onItemSelectedCountChanged(count: Int) {
//                        // Handle the count change here, for example, update a TextView
//                        val totalSelectedMembers = count
//                        Log.d("totalSelectedMembers", totalSelectedMembers.toString())
//
//                        if (count > 0){
//                            searchImg.visibility = View.GONE
//                        }
//                        else{
////                                header.isVisible = true
////                                customToolBar.isInvisible = true
//                            searchImg.visibility = View.GONE
//                        }
//
//
//                    }
//                })

            }

            is MapsFragment -> {
//                appName.visibility = View.GONE
                toolbar.visibility = View.VISIBLE
                backBtn.visibility = View.GONE
                selectedContactNumber.visibility = View.GONE
                fragmentName.text = getString(R.string.map)
            }

            is SettingsFragment -> {
//                appName.visibility = View.GONE
                toolbar.visibility = View.VISIBLE
                backBtn.visibility = View.GONE
                selectedContactNumber.visibility = View.GONE
                fragmentName.text = getString(R.string.settings)
            }

            else -> {
//                appName.visibility = View.GONE
                toolbar.visibility = View.VISIBLE
                backBtn.visibility = View.GONE
                selectedContactNumber.visibility = View.GONE
                fragmentName.text = getString(R.string.profile)

            }
        }
    }


    private data class TabItem(
        val layout: LinearLayout,
        val enableImg: CircleImageView,
        val img: ImageView,
        val txt: TextView,
        val fragment: androidx.fragment.app.Fragment
    ) {
        fun reset() {
            enableImg.isInvisible = true
            img.isVisible = true
            txt.setTextColor(layout.resources.getColor(R.color.grey_txt))
        }

        fun select() {
            enableImg.isVisible = true
            img.isInvisible = true
            txt.setTextColor(layout.resources.getColor(R.color.blue_txt))
        }
    }

    override fun onTotalSelectedMembersChanged(count: Int) {
        // Handle the total selected members change in your activity
        Log.d("countss", "Total selected members: $count")

        if (count > 0) {

            deleteContact.visibility = View.VISIBLE
            backBtn.visibility = View.VISIBLE
            selectedContactNumber.visibility = View.VISIBLE
            addContact.visibility = View.GONE
            searchImg.visibility = View.GONE
            fragmentName.visibility = View.GONE

            selectAll.visibility = View.VISIBLE

            selectedContactNumber.text = count.toString()

        }
        else{
            deleteContact.visibility = View.GONE
            backBtn.visibility = View.GONE
            selectedContactNumber.visibility = View.GONE
            addContact.visibility = View.VISIBLE
            searchImg.visibility = View.VISIBLE
            fragmentName.visibility = View.VISIBLE

            selectAll.visibility = View.GONE
        }
    }


//    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
//        val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container_view)
//
//        if (fragment is EmpergencyFragment) {
//            // Call the method in your fragment
//
//            (fragment as EmpergencyFragment).handleVolumeDownButton()
//            return true
//        }
//
//        return super.onKeyDown(keyCode, event)
//    }



}