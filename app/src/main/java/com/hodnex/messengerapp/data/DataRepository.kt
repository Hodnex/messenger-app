package com.hodnex.messengerapp.data

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.*

class DataRepository {

    private val firebaseAuth = FirebaseAuth.getInstance()

    private val usersCollectionReference = FirebaseFirestore.getInstance().collection("Users")
    private val messagesCollectionReference = FirebaseFirestore.getInstance().collection("Messages")
    private val invitationsCollectionReference =
        FirebaseFirestore.getInstance().collection("Invitations")
    private val dialogsCollectionReference = FirebaseFirestore.getInstance().collection("Dialogs")

    private val _messages = MutableStateFlow(listOf<Message>())

    private val _invitations = MutableStateFlow(listOf<Invitation>())
    val invitations by lazy { getInvitations() }

    private val _dialogs = MutableStateFlow(listOf<Dialog>())
    val dialogs by lazy{ getDialogs() }

    private var users = listOf<User>()

    val currentUserId: String
        get() = firebaseAuth.uid!!
    val currentUser: User
        get() = users.find { it.uid == firebaseAuth.uid }!!

    private val _authorizationEvent = MutableLiveData<DataEvent>()
    val authorizationEvent: LiveData<DataEvent>
        get() = _authorizationEvent

    init {
        getUsers()
    }


    fun getMessages(userId: String): Flow<List<Message>> {
        val messagesFromReference = messagesCollectionReference.document(currentUserId)
            .collection(userId)
        val messagesToReference = messagesCollectionReference.document(userId)
            .collection(currentUserId)

        var messagesFrom = listOf<Message>()
        var messagesTo = listOf<Message>()

        messagesFromReference.addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                messagesFrom = snapshot.toObjects(Message::class.java)
                _messages.value = (messagesFrom + messagesTo).sortedByDescending { it.messageTime }
            }
        }
        messagesToReference.addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                messagesTo = snapshot.toObjects(Message::class.java)
                _messages.value = (messagesFrom + messagesTo).sortedByDescending { it.messageTime }
            }
        }
        return _messages
    }

    private fun getUsers() {
        usersCollectionReference.addSnapshotListener { snapshot, _ ->
            if (snapshot != null) {
                val usersList = snapshot.toObjects(User::class.java)
                users = usersList
            }
        }
    }

    fun addMessage(userId: String, text: String) {
        val message = Message(toId = userId, text = text, fromId = currentUserId)
        messagesCollectionReference.document(currentUserId).collection(message.toId).document()
            .set(message)
        updateDialog(message.text, message.messageTimeFormatted, message.toId, message.messageTime)
    }

    @JvmName("getInvitations1")
    private fun getInvitations(): Flow<List<Invitation>> {
        invitationsCollectionReference.document(currentUserId).collection(currentUserId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    _invitations.value = snapshot.toObjects(Invitation::class.java)
                }
            }
        return _invitations
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

    @JvmName("getDialogs1")
    private fun getDialogs() : Flow<List<Dialog>> {
        dialogsCollectionReference.document(currentUserId).collection(currentUserId)
            .addSnapshotListener { snapshot, _ ->
                if (snapshot != null) {
                    _dialogs.value = snapshot.toObjects(Dialog::class.java)
                    _dialogs.value.sortedByDescending { it.time }
                }
            }
        return _dialogs
    }

    fun addDialog(dialog: Dialog) {
        val currentUser = users.find { it.uid == currentUserId }!!
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
        return users.find { it.email == email }
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
                _authorizationEvent.value = DataEvent.SignUpSuccess
            }
            .addOnFailureListener { e ->
                _authorizationEvent.value = DataEvent.Failure(e.message!!)
            }
    }

    fun signIn(email: String, password: String) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener {
                _authorizationEvent.value = DataEvent.SignInSuccess
            }
            .addOnFailureListener { e ->
                _authorizationEvent.value = DataEvent.Failure(e.message!!)
            }
    }

    fun changeName(name: String) {
        usersCollectionReference.document(currentUserId).update("name", name)
    }

    fun findDialog(user: User?): Dialog? {
        return if (user != null) {
            _dialogs.value.find { it.uid == user.uid }
        } else {
            null
        }
    }

    fun findInvitation(user: User?): Invitation? {
        return if (user != null) {
            _invitations.value.find { it.senderId == user.uid }
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
