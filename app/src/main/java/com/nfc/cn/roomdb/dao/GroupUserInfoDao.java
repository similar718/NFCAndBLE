package com.nfc.cn.roomdb.dao;

import com.nfc.cn.roomdb.beans.GroupUserInfo;

import java.util.List;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;


@Dao
public interface GroupUserInfoDao {
    /**
     * 查询所有
     *
     * @return
     */
    @Query("SELECT * FROM GroupUserInfo")
    List<GroupUserInfo> getDataAll();


    /**
     * 查询当前指定群内所有缓存
     * @return
     */
    @Query("SELECT * FROM GroupUserInfo where mac = :groupid")
    GroupUserInfo getData(String groupid);


    /**
     * 删除当前指定群类所有消息
     * @param groupid
     */
    @Query("DELETE FROM GroupUserInfo WHERE mac = :groupid")
    int deleteFunTypes(String groupid);

    /**
     * 项数据库添加数据
     *
     * @param keyWordsList
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertAll(List<GroupUserInfo> keyWordsList);

    /**
     * 项数据库添加数据
     * @param keyWords
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insert(GroupUserInfo keyWords);

    /**
     * 修改数据
     * @param keyWords
     */
    @Update()
    int update(GroupUserInfo keyWords);

    /**
     * 删除数据
     * @param keyWords
     */
    @Delete()
    void delete(GroupUserInfo keyWords);

    /**
     * 删除群中的这个用户
     */
    @Query("DELETE FROM GroupUserInfo WHERE mac = :groupId")
    void delete(String groupId);
}