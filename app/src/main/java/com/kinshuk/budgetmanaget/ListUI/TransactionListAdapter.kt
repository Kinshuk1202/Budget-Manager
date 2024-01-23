package com.kinshuk.budgetmanaget.ListUI

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.kinshuk.budgetmanaget.dataClasses.Transaction
import com.kinshuk.budgetmanaget.databinding.TransactionTicketBinding
import java.text.SimpleDateFormat
import java.util.*

class TransactionListAdapter(private val txnList:MutableList<Transaction>):
    RecyclerView.Adapter<TransactionListAdapter.MyViewHolder>() {

    class MyViewHolder(private val binding:TransactionTicketBinding):RecyclerView.ViewHolder(binding.root)
    {
        fun bind(txn:Transaction){
            val date = convertTimestampToDate(txn.Date.toLong())
            var money = "+ ₹"
            if(!txn.Type)
                money = "- ₹"

            money += txn.Amount.toString()
            val note = txn.Note
            binding.moneyTv.text = money
            if(note.isNotEmpty())
                binding.NoteTv.text = note
            else
                binding.NoteTv.visibility = View.GONE
            binding.dateTv.text = date
        }


        fun convertTimestampToDate(timestamp: Long): String {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy")
            val date = Date(timestamp)
            return dateFormat.format(date)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding:TransactionTicketBinding = TransactionTicketBinding.inflate(layoutInflater)
        return MyViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return txnList.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.bind(txnList[position])
    }
}