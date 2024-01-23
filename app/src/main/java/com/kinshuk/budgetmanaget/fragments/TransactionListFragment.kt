package com.kinshuk.budgetmanaget.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.kinshuk.budgetmanaget.ListUI.TransactionListAdapter
import com.kinshuk.budgetmanaget.R
import com.kinshuk.budgetmanaget.dataClasses.Transaction
import com.kinshuk.budgetmanaget.databinding.FragmentTransactionListBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate

class TransactionListFragment : Fragment() {
    private lateinit var binding: FragmentTransactionListBinding
    private lateinit var usersTransationList:MutableList<Transaction>
    private lateinit var userRef: DocumentReference
    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as AppCompatActivity?)!!.supportActionBar!!.hide()
        binding = FragmentTransactionListBinding.inflate(layoutInflater)
        db = Firebase.firestore
        auth = FirebaseAuth.getInstance()
        userRef  = db.collection("users").document(auth.currentUser?.uid ?: "")
        usersTransationList = mutableListOf()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        binding.CorutineProgBar.visibility = View.VISIBLE
        binding.recycler.visibility = View.GONE

        userRef.get()
            .addOnSuccessListener {
                if(it.exists())
                {
                    val data = it.data
                    val x  = data!!.get("totalTransactions") as List<HashMap<String,*>>
                    if(x==null || x.isEmpty())
                    {

                    }
                    else {
                        for (tr in x) {
                            usersTransationList.add(
                                Transaction(
                                    tr["date"].toString(),
                                    (tr["amount"] as Long).toInt(),
                                    tr["type"] as Boolean,
                                    tr["note"].toString()
                                )
                            )
                        }
                        binding.recycler.layoutManager = LinearLayoutManager(context)
                        binding.recycler.adapter = TransactionListAdapter(usersTransationList)
                    }
                    binding.CorutineProgBar.visibility = View.GONE
                    binding.recycler.visibility = View.VISIBLE

                }
            }.addOnFailureListener{
                Toast.makeText(context!!.applicationContext,"Unexpectd error! Try Again",Toast.LENGTH_LONG).show()
            }

    }

}