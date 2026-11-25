package com.example.delibuddy.data.user

import com.example.delibuddy.data.crypto.EncryptionProvider
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.tasks.await

class UserRepository {
    private val users = FirebaseFirestore.getInstance().collection("users")

    private val encryption = EncryptionProvider.instance

    private fun UserModel.encrypted(): UserModel = copy(
        birthDate = encryption.encrypt(birthDate),
        bankName = encryption.encrypt(bankName),
        account = encryption.encrypt(account)
    )

    private fun UserModel.decrypted(): UserModel = copy(
        birthDate = encryption.decrypt(birthDate),
        bankName = encryption.decrypt(bankName),
        account = encryption.decrypt(account)
    )

    suspend fun exists(userId: String): Boolean =
        users.document(userId).get(Source.SERVER).await().exists()

    suspend fun create(userId: String, user: UserModel) {
        users.document(userId).set(user.encrypted()).await()
    }

    suspend fun getById(userId: String): UserModel? =
        users.document(userId).get().await().toObject(UserModel::class.java)?.decrypted()

    suspend fun update(userId: String, user: UserModel) {
        users.document(userId).update(
            mapOf(
                "univEmail" to user.univEmail,
                "birthDate" to encryption.encrypt(user.birthDate),
                "bankName" to encryption.encrypt(user.bankName),
                "account" to encryption.encrypt(user.account)
            )
        ).await()
    }

    suspend fun updateBirthDate(userId: String, birthDate: String?) {
        users.document(userId).update("birthDate", encryption.encrypt(birthDate)).await()
    }

    suspend fun updateAccount(userId: String, bankName: String, account: String) {
        users.document(userId).update(
            mapOf(
                "bankName" to encryption.encrypt(bankName),
                "account" to encryption.encrypt(account),
            )
        ).await()
    }

    suspend fun delete(userId: String) {
        users.document(userId).delete().await()
    }
}
