package com.kinshuk.budgetmanaget.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.Navigation
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.kinshuk.budgetmanaget.R
import com.kinshuk.budgetmanaget.dataClasses.User
import com.kinshuk.budgetmanaget.databinding.FragmentSignUpBinding
import java.util.*


class SignUpFragment : Fragment() {
   private lateinit var binding:FragmentSignUpBinding
   private lateinit var auth:FirebaseAuth
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        (activity as AppCompatActivity?)!!.supportActionBar!!.hide()
        binding = FragmentSignUpBinding.inflate(layoutInflater)
        auth = FirebaseAuth.getInstance()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.registerBtn.setOnClickListener{
            RegisterUser(view)
        }
        binding.gotoLogin.setOnClickListener{
            GotoLogin(view)
        }
    }


    fun RegisterUser(view: View) {
        val name = binding.nameEt.text.toString()
        val email = binding.mailEt.text.toString().trim()
        val pass = binding.passEt.text.toString().trim()
        val repas = binding.verifypassEt.text.toString().trim()

        if(name.isNotEmpty() && email.isNotEmpty() && pass.isNotEmpty() && repas.isNotEmpty())
        {
            if(pass == repas)
            {
                auth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener{
                    if (it.isSuccessful) {
                        val c = Calendar.getInstance()
                        val month = c[Calendar.MONTH]
                        val user = User(name, email,System.currentTimeMillis(), month = month)
                        val db = Firebase.firestore
                        it.result.user?.let { it1 -> db.collection("users").document(it1.uid) }
                            ?.set(user)
                        val navController = Navigation.findNavController(view)
                        navController.navigate(R.id.action_signUpFragment_to_homeFragment)
                    }
                    else
                        Toast.makeText(context?.applicationContext,it.exception?.message,Toast.LENGTH_LONG).show()

                }
                    .addOnFailureListener{
                    }
            }
            else
            {
                Toast.makeText(context?.applicationContext,"Password and Re-entered Password Do not match!",Toast.LENGTH_LONG).show()
            }
        }
        else
        {
            Toast.makeText(context?.applicationContext,"Please fill all the entries!",Toast.LENGTH_LONG).show()
        }
    }
    fun GotoLogin(view: View) {
        val navController = Navigation.findNavController(view)
        navController.navigate(R.id.action_signUpFragment_to_loginFragment)
    }
}