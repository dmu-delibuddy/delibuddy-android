package com.example.delibuddy.data.image

import android.content.ContentResolver
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.OpenableColumns
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.StorageMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ImageRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val storage: FirebaseStorage = FirebaseStorage.getInstance(),
) {
    private val images = db.collection("images")

    suspend fun uploadImage(uri: Uri, contentResolver: ContentResolver): ImageModel =
        withContext(Dispatchers.IO) {
            val contentType = contentResolver.getType(uri)
                ?: throw IllegalArgumentException("이미지 MIME 타입을 확인할 수 없습니다.")
            require(contentType.startsWith("image/")) { "이미지 파일만 업로드할 수 있습니다." }

            val options = BitmapFactory.Options()
            options.inJustDecodeBounds = true
            contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, options)
            } ?: throw IllegalArgumentException("이미지 파일을 읽을 수 없습니다.")
            require(options.outWidth > 0 && options.outHeight > 0) {
                "이미지 해상도를 확인할 수 없습니다."
            }

            val document = images.document()
            var fileName = document.id
            contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { cursor ->
                    val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (cursor.moveToFirst() && index >= 0) {
                        fileName = cursor.getString(index) ?: fileName
                    }
                }

            val reference = storage.reference.child("images/${document.id}")
            val metadata = StorageMetadata.Builder().setContentType(contentType).build()
            // 업로드부터 문서 저장까지 마친 뒤 취소를 전달해 중간 취소로 인한 고아 파일을 줄인다.
            withContext(NonCancellable) {
                val uploaded = reference.putFile(uri, metadata).await()
                try {
                    val image = ImageModel(
                        id = document.id,
                        url = reference.downloadUrl.await().toString(),
                        storagePath = reference.path,
                        fileName = fileName,
                        contentType = uploaded.metadata?.contentType ?: contentType,
                        sizeBytes = uploaded.metadata?.sizeBytes
                            ?: reference.metadata.await().sizeBytes,
                        width = options.outWidth,
                        height = options.outHeight,
                        createdAt = Timestamp.now(),
                    )
                    document.set(image).await()
                    image
                } catch (error: Exception) {
                    try {
                        reference.delete().await()
                    } catch (cleanupError: Exception) {
                        error.addSuppressed(cleanupError)
                    }
                    throw error
                }
            }
        }

    suspend fun getImage(imageId: String): ImageModel? {
        require(imageId.isNotBlank()) { "이미지 ID가 비어 있습니다." }
        return images.document(imageId).get().await().toObject(ImageModel::class.java)
    }

    // 파일이 이미 삭제된 경우에도 메타데이터 삭제를 진행해 재시도할 수 있다.
    suspend fun deleteImage(imageId: String) {
        val image = getImage(imageId) ?: return
        require(image.storagePath.isNotBlank()) { "이미지 저장 경로가 비어 있습니다." }
        withContext(NonCancellable) {
            try {
                storage.reference.child(image.storagePath).delete().await()
            } catch (error: StorageException) {
                if (error.errorCode != StorageException.ERROR_OBJECT_NOT_FOUND) throw error
            }
        }
    }
}
