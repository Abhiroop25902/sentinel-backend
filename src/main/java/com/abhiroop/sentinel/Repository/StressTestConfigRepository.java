package com.abhiroop.sentinel.Repository;

import com.abhiroop.sentinel.entity.StressTestConfig;
import com.google.cloud.spring.data.firestore.FirestoreReactiveRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StressTestConfigRepository extends FirestoreReactiveRepository<StressTestConfig> {
}
