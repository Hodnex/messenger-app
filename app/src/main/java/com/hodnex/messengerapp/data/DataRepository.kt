package com.hodnex.messengerapp.data

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DataRepository {

    private val firebaseAuth = FirebaseAuth.getInstance()

    private val usersCollectionReference = FirebaseFirestore.getInstance().collection("Users")
    private val messagesCollectionReference = FirebaseFirestore.getInstance().collection("Messages")
    private val invitationsCollectionReference =
        FirebaseFirestore.getInstance().collection("Invitations")
    private val dialogsCollectionReference = FirebaseFirestore.getInstance().collection("Dialogs")

    private val _messages = MutableLiveData<List<Message>>()
    val invitations = MutableLiveData<List<Invitation>>()
    val dialogs = MutableLiveData<List<Dialog>>()
    private val users = MutableLiveData<List<User>>()

    val currentUserId: String
        get() = firebaseAuth.uid!!
    val currentUser: User
        get() = users.value!!.find { it.uid == firebaseAuth.uid }!!

    val signInEvent = MutableLiveData<DataEvent>()

    init {
        getUsers()
    }


    fun getMessages(userId: String): LiveData<List<Message>> {
        val messagesFromReference = messagesCollectionReference.document(currentUserId)
            .collection(userId)
        val messagesToReference = messagesCollectionReference.document(userId ?: "")
            .collection(currentUserId)

        var messagesFrom = listOf<Message>()
        var messagesTo = listOf<Message>()

        messagesFromReference.addSnapshotListener { snapshot, e ->
            if (snapshot != null) {
                messagesFrom = snapshot.toObjects(Message::class.java)
                _messages.value = (messagesFrom + messagesTo).sortedByDescending { it.messageTime }
            }
        }
        messagesToReference.addSnapshotListener { snapshot, e ->
            if (snapshot != null) {
                messagesTo = snapshot.toObjects(Message::class.java)
                _messages.value = (messagesFrom + messagesTo).sortedByDescending { it.messageTime }
            }
        }
        return _messages
    }

    private fun getUsers() {
        usersCollectionReference.addSnapshotListener { snapshot, e ->
            if (snapshot != null) {
                val usersList = snapshot.toObjects(User::class.java)
                users.value = usersList
            }
        }
    }

    fun addMessage(userId: String, text: String) {
        val message = Message(toId = userId, text = text, fromId = currentUserId)
        messagesCollectionReference.document(currentUserId).collection(message.toId).document()
            .set(message)
        updateDialog(message.text, message.messageTimeFormatted, message.toId, message.messageTime)
    }

    private fun getInvitations() {
        invitationsCollectionReference.document(currentUserId).collection(currentUserId)
            .addSnapshotListener { snapshot, e ->
                if (snapshot != null) {
                    val invitationsList = snapshot.toObjects(Invitation::class.java)
                    invitations.value = invitationsList
                }
            }
    }

    fun addInvitation(invitation: Invitation) {
        invitationsCollectionReference.document(invitation.receiverId)
            .collection(invitation.receiverId)
            .document(invitation.senderId).set(invitation)
    }

    fun deleteInvitation(invitation: Invitation) {
        invitationsCollectionReference.document(invitation.receiverId)
            .collection(invitation.receiverId)
            .document(invitation.senderId).delete()
    }

    private fun getDialogs() {
        dialogsCollectionReference.document(currentUserId).collection(currentUserId)
            .addSnapshotListener { snapshot, e ->
                if (snapshot != null) {
                    val dialogsList = snapshot.toObjects(Dialog::class.java)
                    dialogs.value = dialogsList.sortedByDescending { it.time }
                }
            }
    }

    fun addDialog(dialog: Dialog) {
        val currentUser = users.value!!.find { it.uid == currentUserId }!!
        dialogsCollectionReference.document(currentUserId).collection(currentUserId)
            .document(dialog.uid).set(dialog)
        dialogsCollectionReference.document(dialog.uid).collection(dialog.uid)
            .document(currentUserId).set(Dialog(uid = currentUserId, currentUser.name))
    }

    private fun updateDialog(
        lastMessage: String,
        timeFormatted: String,
        userId: String,
        time: Long
    ) {
        Log.d("Main", userId)
        dialogsCollectionReference.document(currentUserId).collection(currentUserId)
            .document(userId).update(
                mapOf(
                    "timeFormatted" to timeFormatted,
                    "time" to time,
                    "lastMessage" to lastMessage
                )
            )
        dialogsCollectionReference.document(userId).collection(userId).document(currentUserId)
            .update(
                mapOf(
                    "timeFormatted" to timeFormatted,
                    "time" to time,
                    "lastMessage" to lastMessage
                )
            )
    }

    private fun addUser(user: User) {
        usersCollectionReference.document(user.uid).set(user)
    }

    fun findUserByEmail(email: String): User? {
        return users.value!!.find { it.email == email }
    }

    fun signUp(email: String, password: String) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                addUser(
                    User(
                        uid = firebaseAuth.uid!!,
                        email = email
                    )
                )
                signInEvent.value = DataEvent.SignUpSuccess
                getDialogs()
                getInvitations()
            }
            .addOnFailureListener { e ->
                signInEvent.value = DataEvent.Failure(e.message!!)
            }
    }

    fun signIn(email: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                getDialogs()
                getInvitations()
                signInEvent.value = DataEvent.SignInSuccess
            }
            .addOnFailureListener { e ->
                signInEvent.value = DataEvent.Failure(e.message!!)
            }
    }

    fun changeName(name: String) {
        usersCollectionReference.document(currentUserId).update("name", name)
    }

    fun findDialog(user: User?): Dialog? {
        return if (user != null) {
            dialogs.value!!.find { it.uid == user.uid }
        } else {
            null
        }
    }

    fun findInvitation(user: User?): Invitation? {
        return if (user != null) {
            invitations.value!!.find { it.senderId == user.uid }
        } else {
            null
        }
    }

    sealed class DataEvent {
        object SignUpSuccess : DataEvent()
        data class Failure(val msg: String) : DataEvent()
        object SignInSuccess : DataEvent()
    }
}
