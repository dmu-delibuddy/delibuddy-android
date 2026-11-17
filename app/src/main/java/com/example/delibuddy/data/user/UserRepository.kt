package com.example.delibuddy.data.user

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import kotlinx.coroutines.tasks.await

class UserRepository {
    private val users = FirebaseFirestore.getInstance().collection("users")

    suspend fun exists(userId: String): Boolean =
        users.document(userId).get(Source.SERVER).await().exists()

    suspend fun create(userId: String, user: UserModel) {
        users.document(userId).set(user).await()
    }

    suspend fun getById(userId: String): UserModel? =
        users.document(userId).get().await().toObject(UserModel::class.java)

    suspend fun update(userId: String, user: UserModel) {
        users.document(userId).update(
            mapOf(
                "univEmail" to user.univEmail,
                "birthDate" to user.birthDate,
                "bankName" to user.bankName,
                "account" to user.account
            )
        ).await()
    }

    // 생년월일만 수정 (다른 필드는 건드리지 않음)
    suspend fun updateBirthDate(userId: String, birthDate: String?) {
        users.document(userId).update("birthDate", birthDate).await()
    }

    // 송금 계좌(은행, 계좌번호)만 수정 (다른 필드는 건드리지 않음)
    suspend fun updateAccount(userId: String, bankName: String, account: String) {
        users.document(userId).update(
            mapOf(
                "bankName" to bankName,
                "account" to account,
            )
        ).await()
    }

    suspend fun delete(userId: String) {
        users.document(userId).delete().await()
    }
}
