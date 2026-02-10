package com.abhiroop.sentinel.Repository;

import com.abhiroop.sentinel.entity.LoginHistory;
import com.google.cloud.spring.data.firestore.FirestoreReactiveRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoginHistoryRepository extends FirestoreReactiveRepository<LoginHistory> {
}
