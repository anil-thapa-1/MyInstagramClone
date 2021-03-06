package com.gameonanil.instagramcloneapp.ui.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.NavHostFragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import com.gameonanil.imatagramcloneapp.R
import com.gameonanil.instagramcloneapp.adapter.MainRecyclerAdapter
import com.gameonanil.instagramcloneapp.adapter.StoryRecyclerAdapter
import com.gameonanil.instagramcloneapp.models.Posts
import com.gameonanil.instagramcloneapp.models.User
import com.gameonanil.instagramcloneapp.ui.start.StartActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.android.synthetic.main.fragment_home.*

open class HomeFragment : Fragment(R.layout.fragment_home) {
    companion object{
        private const val TAG = "HomeFragment"
    }

    private lateinit var firestore: FirebaseFirestore
    private lateinit var adapterMain: MainRecyclerAdapter
    private lateinit var adapterStory: StoryRecyclerAdapter
    private lateinit var postList: MutableList<Posts>
    private lateinit var userList: MutableList<User>
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        /**Setting Up Toolbar*/
        val toolbar = view.findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar_main)
        appBarConfiguration = AppBarConfiguration(
            setOf(R.id.homeFragment,
                R.id.profileFragment,
                R.id.addPostFragment,
               )
        )

        val navHostFragment = NavHostFragment.findNavController(this);
        NavigationUI.setupWithNavController(toolbar, navHostFragment,appBarConfiguration)

        /** TO USE OPTIONS MENU*/
        setHasOptionsMenu(true)
        (activity as AppCompatActivity).setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener {
            findNavController(this).navigateUp()
        }


        firestore = FirebaseFirestore.getInstance()
        postList = mutableListOf()
        userList = mutableListOf()
        adapterMain = MainRecyclerAdapter(requireActivity().baseContext,postList)
        adapterStory = StoryRecyclerAdapter(requireActivity().baseContext,userList)

        recycler_main.adapter = adapterMain
        recycler_story.adapter = adapterStory

        val collectionReference = firestore
            .collection("posts")
            .orderBy("creation_time_ms",Query.Direction.DESCENDING)


        collectionReference.addSnapshotListener{snapshot, exception ->
            if (exception !=null || snapshot == null){
                Log.e(TAG, "onCreate: Exception: $exception", )
                return@addSnapshotListener
            }
            val postFromDb =  snapshot.toObjects(Posts::class.java)
            //updating out list
            postList.clear()
            postList.addAll(postFromDb)
            adapterMain.notifyDataSetChanged()

        }

        val userCollectionReference = firestore.collection("users")
        userCollectionReference.addSnapshotListener { snapshot, error ->
            if (error !=null || snapshot == null){
                Log.e(TAG, "onCreate: Exception: $error", )
                return@addSnapshotListener
            }
            val userFromDb = snapshot.toObjects(User::class.java)
            userList.clear()
            for (user in userFromDb){
                if (user.profile_image!=""){
                    userList.add(user)
                    adapterStory.notifyDataSetChanged()
                }
            }


        }

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.item_logout) {
            Log.d(TAG, "onOptionsItemSelected: logout pressed")

            FirebaseAuth.getInstance().signOut()
            val intent = Intent(activity, StartActivity::class.java)
            startActivity(intent)
            requireActivity().finish()
        }
        return super.onOptionsItemSelected(item)
    }


}