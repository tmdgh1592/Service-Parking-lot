package com.app.service.parking.model.network.retrofit.builder

import android.net.Uri
import android.util.Log
import androidx.core.net.toFile
import com.app.service.parking.model.dto.Review
import com.app.service.parking.model.network.retrofit.api.ReviewAPI
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import timber.log.Timber
import java.io.File

object ReviewAPIBuilder : BaseRetrofitBuilder() {

    /* POST 방식
     서버에 리뷰 저장
     */
    suspend fun uploadReview(review: Review, imgsUri: List<Uri>) =
        suspendCancellableCoroutine<Boolean> { continuation ->
            // 이미지를 전송하기 위한 MultiPart 리스트
            val multiPartFiles = arrayListOf<MultipartBody.Part>()
            val api = getRetrofit().create(ReviewAPI::class.java)

            // 전달받은 이미지 Uri를 반복문으로 돌려, Multipart를 만들고 ArrayList에 담아준다.
            imgsUri.forEach { imgUri ->
                val fileBody = RequestBody.create(MediaType.parse("image/jpeg"), imgUri.toFile())
                multiPartFiles.add(
                    MultipartBody.Part.createFormData(
                        "img",
                        System.currentTimeMillis().toString() + ".jpg",
                        fileBody
                    )
                )
            }

            // 리뷰를 서버에 업로드 하도록 요청
            api.uploadReview(
                review.userUid,
                review.parkCode,
                multiPartFiles,
                review.reviewText,
                review.rate
            ).enqueue(object :
                Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.body() != null && response.isSuccessful) {
                        Timber.tag("Review Upload Response").d("Success")
                        Log.d("Review Upload Response", "${response.body()}")
                        continuation.resumeWith(Result.success(true))
                    } else {
                        val error = response.errorBody()?.string()
                        if (error == "100") {
                            Timber.tag("Review Upload Response").d("Failed by error 100")
                        } else {
                            Timber.tag("Review Upload Response").d("Failed by other")
                        }
                        continuation.resumeWith(Result.failure(Exception("리뷰 업로드에 실패하였습니다."))) // 리뷰 업로드 실패시, 예외처리
                    }
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Timber.tag("Review Upload Response").d("Failed")
                    Log.d("Coroutine", "Retrofit")
                    continuation.resumeWith(Result.failure(Exception("리뷰 업로드에 실패하였습니다."))) // 리뷰 업로드 실패시, 예외처리
                }
            })
        
        }


    /* PUT 방식
     서버에 저장된 리뷰 데이터 업데이트
     */
    suspend fun updateReview(review: Review, imgPath: String) {
        suspendCancellableCoroutine<Unit> { continuation ->
            val api = getRetrofit().create(ReviewAPI::class.java)
            val fileBody = RequestBody.create(MediaType.parse("image/*"), File(imgPath))
            val filePart = MultipartBody.Part.createFormData(
                "img",
                System.currentTimeMillis().toString(),
                fileBody
            )

            api.updateReview(review.reviewUid, filePart, review.reviewText, review.rate)
                .enqueue(object :
                    Callback<Void> {
                    override fun onResponse(call: Call<Void>, response: Response<Void>) {
                        if (response.body() != null && response.isSuccessful) {
                            Timber.tag("User Register Response").d("Success")
                            Log.d("User Register Response", "${response.body()}")
                        } else {
                            val error = response.errorBody()?.string()
                            if (error == "100") {
                                Timber.tag("User Register Response").d("Failed by error 100")
                            } else {
                                Timber.tag("User Register Response").d("Failed by other")
                            }
                        }
                        continuation.resume(Unit, null)
                        Log.d("Coroutine", "register response")
                    }

                    override fun onFailure(call: Call<Void>, t: Throwable) {
                        Timber.tag("User Register Response").d("Failed")
                        Log.d("Coroutine", "Retrofit")
                        continuation.resume(Unit, null)
                    }
                })
        }
    }


    /* GET 방식
     서버로부터 주차장 코드를 바탕으로 리뷰 리스트 요청
     */
    suspend fun getReviewList(parkCode: String) {
        suspendCancellableCoroutine<Unit> { continuation ->
            val api = getRetrofit().create(ReviewAPI::class.java)
            api.getReviewList(parkCode).enqueue(object :
                Callback<List<Review>> {
                override fun onResponse(
                    call: Call<List<Review>>,
                    response: Response<List<Review>>
                ) {
                    if (response.body() != null && response.isSuccessful) {
                        Timber.tag("User Register Response").d("Success")
                        Log.d("User Register Response", "${response.body()}")
                    } else {
                        val error = response.errorBody()?.string()
                        if (error == "100") {
                            Timber.tag("User Register Response").d("Failed by error 100")
                        } else {
                            Timber.tag("User Register Response").d("Failed by other")
                        }
                    }
                    continuation.resume(Unit, null)
                    Log.d("Coroutine", "register response")
                }

                override fun onFailure(call: Call<List<Review>>, t: Throwable) {
                    Timber.tag("User Register Response").d("Failed")
                    Log.d("Coroutine", "Retrofit")
                    continuation.resume(Unit, null)
                }
            })
        }
    }


    /* DELETE 방식
     서버에 저장된 리뷰 데이터 삭제
     */
    suspend fun deleteReview(id: Int) {
        suspendCancellableCoroutine<Unit> { continuation ->
            val api = getRetrofit().create(ReviewAPI::class.java)
            api.deleteReview(id).enqueue(object :
                Callback<Void> {
                override fun onResponse(call: Call<Void>, response: Response<Void>) {
                    if (response.body() != null && response.isSuccessful) {
                        Timber.tag("User Register Response").d("Success")
                        Log.d("User Register Response", "${response.body()}")
                    } else {
                        val error = response.errorBody()?.string()
                        if (error == "100") {
                            Timber.tag("User Register Response").d("Failed by error 100")
                        } else {
                            Timber.tag("User Register Response").d("Failed by other")
                        }
                    }
                    continuation.resume(Unit, null)
                    Log.d("Coroutine", "register response")
                }

                override fun onFailure(call: Call<Void>, t: Throwable) {
                    Timber.tag("User Register Response").d("Failed")
                    Log.d("Coroutine", "Retrofit")
                    continuation.resume(Unit, null)
                }
            })
        }
    }


}