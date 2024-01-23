package com.kinshuk.budgetmanaget.fragments

import android.app.Dialog
import android.os.Bundle
import android.transition.Transition
import android.util.Log
import android.view.*
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.core.text.isDigitsOnly
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.google.android.material.button.MaterialButton
import com.google.android.material.datepicker.MaterialTextInputPicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue.arrayUnion
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.kinshuk.budgetmanaget.R
import com.kinshuk.budgetmanaget.dataClasses.Transaction
import com.kinshuk.budgetmanaget.databinding.FragmentHomeBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.w3c.dom.Text
import java.util.*


class HomeFragment : Fragment() {

    private lateinit var binding:FragmentHomeBinding
    private lateinit var userRef:DocumentReference
    private lateinit var db:FirebaseFirestore
    private lateinit var auth:FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (activity as AppCompatActivity?)!!.supportActionBar!!.show()
        (activity as AppCompatActivity?)!!.supportActionBar!!.
                setBackgroundDrawable(getDrawable(context!!.applicationContext,R.color.themeBg))
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate(layoutInflater)
        db = Firebase.firestore
        auth = FirebaseAuth.getInstance()
        userRef  = db.collection("users").document(auth.currentUser?.uid ?: "")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initValues()

        binding.editBudget.setOnClickListener{
            showEditDialog()
        }
        binding.addTransaction.setOnClickListener {
            showAddDialog()
        }
    }

    private fun showAddDialog() {
        val dialog = Dialog(context!!)
        dialog.setCancelable(true)

        val view: View = activity!!.layoutInflater.inflate(R.layout.add_transaction_popup, null)
        dialog.setContentView(view)

        val cancel = view.findViewById(R.id.cancelBtn) as MaterialButton
        val done = view.findViewById(R.id.DoneBtn) as MaterialButton
        cancel.setOnClickListener {
            dialog.dismiss()
        }
        done.setOnClickListener {
            val amount = view.findViewById<EditText>(R.id.transactionEt).text.toString()
            val note = view.findViewById<EditText>(R.id.noteEt).text.toString()
            val creditiedBtn = view.findViewById<RadioButton>(R.id.radio_credit)
            val debitedBtn = view.findViewById<RadioButton>(R.id.radio_debit)
            val radioGroup = view.findViewById<RadioGroup>(R.id.radioGroup)
            val creditSelected = radioGroup.checkedRadioButtonId == R.id.radio_credit
            val debitSelected = radioGroup.checkedRadioButtonId == R.id.radio_debit
            if(amount.isNotEmpty() && (creditSelected || debitSelected))
            {
                val type = !debitSelected
                val transaction = Transaction(System.currentTimeMillis().toString(),amount.toInt(),type,note)
                userRef.update("totalTransactions",arrayUnion(transaction)).addOnFailureListener {
                    Toast.makeText(context!!.applicationContext,"Unexpected error! Try Again",Toast.LENGTH_LONG).show()
                }
                    .addOnCompleteListener {
                        addTransaction(amount,type)
                    }
                dialog.dismiss()
            }
            else
            {
                Toast.makeText(context,"Please enter both amount and type",Toast.LENGTH_LONG).show()
            }

        }


        dialog.window?.setLayout(1000, 1400)
        dialog.show();
    }

    private fun addTransaction(amount: String, type: Boolean) {
        val money = amount.toInt()

        userRef.get().addOnSuccessListener { documentSnapshot ->
            if (documentSnapshot.exists()) {
                val data = documentSnapshot.data
                val current = data?.get("spent") as? Long ?: 0L
                val finalSpent = if (!type) money + current else current - money
                userRef.update("spent", finalSpent).addOnSuccessListener {
                    Toast.makeText(context?.applicationContext, "Added Sucessfully", Toast.LENGTH_LONG).show()
                    initValues()
                }
            }
        }.addOnFailureListener {
            Toast.makeText(context?.applicationContext, "Unexpected error! Try Again", Toast.LENGTH_LONG).show()
        }
    }

    private fun showEditDialog() {
        val dialog = Dialog(context!!)
        dialog.setCancelable(true)

        val view: View = activity!!.layoutInflater.inflate(R.layout.edit_budget_popup, null)
        dialog.setContentView(view)

        val cancel = view.findViewById(R.id.cancelBtn) as MaterialButton
        val done = view.findViewById(R.id.DoneBtn) as MaterialButton
        cancel.setOnClickListener{
            dialog.cancel()
        }
        done.setOnClickListener{
            val newVal:EditText = view.findViewById(R.id.budgetEt)
            val budgetVal = newVal.text.toString()
            if(budgetVal.isNotEmpty() && budgetVal.isDigitsOnly()){
                Log.d("TAGY",budgetVal.isDigitsOnly().toString())
                userRef.update("budget",budgetVal.toInt()).addOnFailureListener {
                    Toast.makeText(context,"Unexpected error! Try Again",Toast.LENGTH_LONG).show()
                }
                    .addOnSuccessListener {
                       initValues()
                    }
                dialog.dismiss()
            }
            else
            {
                Log.d("TAGY",budgetVal)
                Toast.makeText(context,"Enter Valid Amount",Toast.LENGTH_LONG).show()
            }

        }
        dialog.window?.setLayout(1000, 800)
        dialog.show();
    }

    private   fun initValues() {

        binding.CorutineProgBar.visibility = View.VISIBLE
        binding.fullscreen.visibility = View.GONE
        CoroutineScope(Dispatchers.IO).launch {
            userRef.get()
                .addOnSuccessListener {
                    if(it.exists())
                    {
                        val data = it.data
                        val name = data?.get("name").toString()
                        val budget = data?.get("budget").toString()
                        val spent = data?.get("spent").toString()
                        binding.BudgetTv.text = "₹$budget"
                        binding.SpentTv.text = "₹$spent"
                        binding.GreetTv.text = name
                        binding.CorutineProgBar.visibility = View.GONE
                        binding.fullscreen.visibility = View.VISIBLE

                        val calendar = Calendar.getInstance()
                        val lastDay: Int = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                        val currentDay: Int = calendar.get(Calendar.DAY_OF_MONTH)
                        val daysLeft = lastDay - currentDay+1
                        var Safemoney = (budget.toInt()-spent.toInt())/daysLeft
                        if(Safemoney<0)
                            Safemoney = 0
                        binding.safeTv.text = "₹$Safemoney/day"
                        var precent:Double = (spent.toDouble()/budget.toDouble())*100
                        if(budget.toInt() == 0)
                            precent = 0.0
                        val finalPer:Double = String.format("%.2f", precent).toDouble()
                        if(budget.toInt() == 0)
                            precent = 0.0
                        binding.progressText.text = "$finalPer%"
                        binding.Prog.setProgress(finalPer.toInt(),true)

                    }
                    else
                    {
                        Toast.makeText(context,"User Not Found!",Toast.LENGTH_LONG).show()
                    }
                }
            userRef.update("lastIn",System.currentTimeMillis()).addOnFailureListener{
                Toast.makeText(context,"User Not Found!",Toast.LENGTH_LONG).show()
            }
        }
    }



    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.home_menu,menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.logout_menu->{
                auth.signOut()
                val nav = Navigation.findNavController(view!!)
                nav.navigate(R.id.action_homeFragment_to_loginFragment)
            }
        }

        return super.onOptionsItemSelected(item)
    }


}