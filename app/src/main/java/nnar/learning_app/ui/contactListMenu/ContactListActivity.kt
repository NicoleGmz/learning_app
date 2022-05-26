package nnar.learning_app.ui.contactListMenu

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

import nnar.learning_app.data.repository.ContactFirestoreRepository
import nnar.learning_app.databinding.ActivityContactListBinding
import nnar.learning_app.datainterface.ContactListView
import nnar.learning_app.domain.usecase.ContactFirestoreUseCase
import nnar.learning_app.ui.contactCreation.ContactCreationActivity
import nnar.learning_app.ui.login.LoginActivity

class ContactListActivity: AppCompatActivity(), ContactListView {

    private lateinit var binding: ActivityContactListBinding
    private lateinit var recyclerView:RecyclerView
    private lateinit var presenter:ContactListPresenter
    private lateinit var addButton: FloatingActionButton
    private lateinit var adapter: ContactListAdapter
    private lateinit var logoutButton:Button
    private lateinit var user: FirebaseUser

    override fun onCreate(savedInstanceState: Bundle?){
        super.onCreate(savedInstanceState)
        binding = ActivityContactListBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        user = intent.getParcelableExtra("user")!!

        addButton = binding.addContactButton
        logoutButton = binding.listmenuLogoutButton
        recyclerView = binding.listmenuRecyclerView

        presenter = ContactListPresenter(this, ContactFirestoreUseCase(
            ContactFirestoreRepository()
        ))

        adapter = ContactListAdapter(presenter)
        recyclerView.adapter = adapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        //presenter.addFirebaseContactsFirstsTime(user.uid)
        presenter.getContacts(user.uid)

        setListeners()
    }

    private fun setListeners() {
        addButton.setOnClickListener {
            val intent = Intent(it.context, ContactCreationActivity()::class.java)
            it.context.startActivity(intent)
            //presenter.addNewContact()
            adapter.updateData()
        }

        logoutButton.setOnClickListener {
            Firebase.auth.signOut()
            adapter.deleteLocalData()
            val intent = Intent(it.context, LoginActivity()::class.java)
            //intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            intent.putExtra("logout", true)
            it.context.startActivity(intent)
            finish()
        }
    }

    override fun onBackPressed() {}

    override fun updateData() {
        adapter.updateData()
    }

    override fun showMessageContactAdded(s: String) {
        adapter.notifyDataSetChanged()
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
    }

    override fun showMessageContactDeleted(s: String) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
    }

    override fun showMessageErrorContactsUpdated(s: String) {
        Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
    }

}

