package nnar.learning_app.data.repository

import android.net.Uri
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import nnar.learning_app.domain.model.ContactFirestore
import java.lang.Exception

/**
 * @author Nicole Gomez
 */
class ContactFirestoreRepository {

    private val TAG = "Learning App - Contact List:"

    private val db = Firebase.firestore.collection("users")
    private val storageRef = Firebase.storage.reference

    companion object{
        val contactList =  mutableListOf<ContactFirestore>()
        private lateinit var userUIDRepository: String
    }

    /*private val contactSet: MutableSet<ContactFirestore> = mutableSetOf(
        ContactFirestore(1, "raul", "+34 11111111111", "images/avataaars1.png", "raul@gmail.com"),
        ContactFirestore(2, "sigrit", "+34 2222222222", "images/avataaars2.png", "sigrit@gmail.com"),
        ContactFirestore(3, "nathan", "+34 3333333333", "images/avataaars3.png", "nathan@gmail.com"),
        ContactFirestore(4, "alberto", "+34 4444444444", "images/avataaars4.png", "alberto@gmail.com"),
        ContactFirestore(5, "nicole", "+34 5555555555", "images/avataaars5.png", "nicole@gmail.com"),
        ContactFirestore(6, "agus", "+34 666666666", "images/avataaars6.png", "agus@gmail.com")
    )*/

    /**
     * Create a new object ContactFireStore
     * [name] String with the name of new contact
     * [phone] String with the phone number of the new contact
     * [image] String with the path of the image in Firebase
     * [email] String with the email of the new contact
     * @return ContactFirestore
     */
    fun createContact(name:String, phone:String, image: String?, email:String): ContactFirestore{
        val newId = contactList.last().id + 1
        val auxImage = image ?: "images/avataaars_default.png" // If image is null then default
        return ContactFirestore(newId, name, phone, auxImage, email)
    }

    /**
    suspend fun writeDataOnFirestoreFirstsTime(userUID: String): Boolean {
        userUIDRepository = userUID
        var response = false
        for (contact in contactSet){
            response = try{
                val doc = db.document(userUID).collection("contacts").document(contact.id.toString())
                if(!doc.get().await().exists()){
                    writeDataOnFirestore(contact)
                    Log.d(TAG, "Contact ${contact.id} added")
                }else{
                    Log.d(TAG, "Contact ${contact.id} already exists")
                }
                true
            }catch (e : Exception){
                Log.d(TAG, "Error writing contact ${contact.id}")
                false
            }
        }
        return response
    }**/

    /**
     *
     * [contact] A ContactFirestore Object
     * @return Boolean with operation result
     */

    suspend fun writeDataOnFirestore(contact: ContactFirestore):Boolean{
        return withContext(Dispatchers.IO){
            try{
                db.document(userUIDRepository).collection("contacts")
                    .document(contact.id.toString())
                    .set(contact)
                    .await()
                contactList.add(contact)
                true
            }catch (e : Exception){
                false
            }
        }
    }

    suspend fun readContactsFirestore(userUID: String): Boolean{
        return withContext(Dispatchers.IO){
            userUIDRepository = userUID
            try{
                val auxList = mutableListOf<ContactFirestore>()
                val data = db.document(userUIDRepository).collection("contacts")
                    .get()
                    .await()
                for (d in data){
                    auxList.add(d.toObject())
                }
                contactList.addAll(auxList)
                true
            }catch (e: Exception){
                false
            }
        }
    }

    suspend fun removeContactFirestore(contact: ContactFirestore): Boolean {
        return withContext(Dispatchers.IO){
            try{
                db.document(userUIDRepository).collection("contacts")
                    .document(contact.id.toString())
                    .delete()
                deleteImage(contact.image)
                contactList.remove(contact)
                true
            }catch(e: Exception){
                false
            }
        }
    }

    suspend fun addContact(image: Uri, name:String, email:String, phone:String): Boolean{
        val imageName = "images/avatar_$name".trim().replace(" ", "_")
        val contact = createContact(name, phone, imageName, email)
        return try{
            uploadImage(imageName, image)
            writeDataOnFirestore(contact)
            true
        }catch (e: Exception){
            false
        }
    }

    suspend fun uploadImage(imageName:String, image:Uri):Boolean{
        return withContext(Dispatchers.IO){
            try{
                val path = storageRef.child(imageName)
                path.putFile(image).await()
                true
            }catch (e: Exception){
                false
            }
        }
    }

    private suspend fun deleteImage(imageName:String): Boolean{
        return withContext(Dispatchers.IO){
            try{
                val imageRef = storageRef.child(imageName)
                imageRef.delete().await()
                true
            }catch(e: Exception){
                false
            }
        }
    }

    fun getItemCount() = contactList.size

    fun getContact(position: Int) = contactList[position]

    fun deleteLocalData(){
        userUIDRepository = " "
        contactList.clear()
    }
}