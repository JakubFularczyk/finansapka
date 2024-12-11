package com.example.androidproject.baza.dao;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.example.androidproject.baza.encje.UserEntity;

@Dao
public interface UserDAO {
    @Insert
    void insert(UserEntity user);

    @Query("SELECT * FROM users WHERE id = :userId")
    UserEntity findById(int userId);

    @Query("SELECT * FROM users WHERE pin = :pin LIMIT 1")
    UserEntity findByPin(String pin);

    @Query("DELETE FROM users WHERE id = :userId")
    void deleteUserById(int userId);

    @Query("SELECT * FROM users LIMIT 1")
    UserEntity findFirstUser();
    @Query("SELECT COUNT(*) FROM users")
    int getUserCount();

    @Query("UPDATE users SET pin = :newPin WHERE id = :userId")
    void updateUserPin(int userId, String newPin);
}
