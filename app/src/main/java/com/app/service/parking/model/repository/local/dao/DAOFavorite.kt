package com.app.service.parking.model.repository.local.dao

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.*
import com.app.service.parking.model.repository.local.DBConst
import com.app.service.parking.model.repository.local.entity.EntityFavorite


@Dao
interface DAOFavorite {

    // 데이터 베이스 불러오기
    @Query("SELECT * from ${DBConst.TABLE_NAME_USER_FAVORITE}")
    fun getAll(): LiveData<List<EntityFavorite>>

    // 데이터 추가
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(entity: EntityFavorite)

    // 데이터 전체 삭제
    @Query("DELETE FROM ${DBConst.TABLE_NAME_USER_FAVORITE}")
    fun deleteAll()

    // 데이터 업데이트
    @Update
    fun update(entity: EntityFavorite)

    // 데이터 삭제
    @Delete
    fun delete(entity: EntityFavorite)


}